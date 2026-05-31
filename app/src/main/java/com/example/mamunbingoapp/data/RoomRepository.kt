package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.data.db.DatabaseProvider
import com.example.mamunbingoapp.data.db.LiveRoomEntity
import com.example.mamunbingoapp.data.db.RoomCalledNumberEntity
import com.example.mamunbingoapp.data.db.RoomSettingsEntity
import com.example.mamunbingoapp.data.db.RoomTicketEntity
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.core.RoomStatusResolver
import com.example.mamunbingoapp.core.SundayBingoSchedule
import com.example.mamunbingoapp.ui.model.RoomStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import java.util.UUID

data class RoomSettings(
    val roomId: String,
    val autoCallEnabled: Boolean,
    val intervalSeconds: Int,
    val lastStartedAt: Long?,
    val isRunning: Boolean,
    val isArchived: Boolean = false
)

// Live rooms enforce single-room-per-ticket rule.
// Conflicts are handled via AssignTicketResult and must never fail silently.
sealed class AssignTicketResult {
    data object Success : AssignTicketResult()
    data class AlreadyInRoom(val existingRoomId: String) : AssignTicketResult()
    data class Error(val message: String) : AssignTicketResult()
}

private data class RoomCalledCountEntry(val roomId: String, val count: Int)

private data class RoomCalledNumbersEntry(val roomId: String, val numbers: List<Int>)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
object RoomRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun db() = DatabaseProvider.db
    private fun roomDao() = db().liveRoomDao()
    private fun ticketDao() = db().roomTicketDao()
    private fun calledDao() = db().roomCalledNumberDao()
    private fun settingsDao() = db().roomSettingsDao()

    fun roomsFlow(): Flow<List<LiveRoom>> =
        roomDao().observeRooms().map { it.map { e -> e.toLiveRoom() } }

    fun roomFlow(roomId: String): Flow<LiveRoom?> =
        roomDao().observeRoom(roomId).map { it?.toLiveRoom() }

    fun roomTicketsFlow(roomId: String): Flow<List<String>> =
        ticketDao().observeTickets(roomId).map { list -> list.map { it.ticketId } }

    fun calledNumbersFlow(roomId: String): Flow<List<Int>> =
        calledDao().observeCalled(roomId).map { list -> list.map { it.number } }

    fun calledNumbersWithLastCalledAtFlow(roomId: String): Flow<Pair<List<Int>, Long?>> =
        calledDao().observeCalled(roomId).map { list ->
            list.map { it.number } to list.lastOrNull()?.calledAt
        }

    fun roomStatusFlow(roomId: String): Flow<RoomStatus> =
        calledNumbersFlow(roomId).map { RoomStatusResolver.resolve(it.size) }

    fun lastCalledAtFlow(roomId: String): Flow<Long?> =
        calledDao().observeCalled(roomId).map { list -> list.lastOrNull()?.calledAt }

    fun roomSettingsFlow(roomId: String): Flow<RoomSettings?> =
        settingsDao().observeSettings(roomId).map { it?.toRoomSettings() }

    fun roomArchivedFlow(roomId: String): Flow<Boolean> =
        settingsDao().observeSettings(roomId).map { it?.isArchived ?: false }

    fun roomsArchivedMapFlow(): Flow<Map<String, Boolean>> =
        settingsDao().observeAll().map { list -> list.associate { it.roomId to it.isArchived } }

    suspend fun setRoomArchived(roomId: String, archived: Boolean) = withContext(Dispatchers.IO) {
        val updated = settingsDao().updateArchived(roomId, archived)
        if (updated == 0) {
            settingsDao().upsertSettings(RoomSettingsEntity(roomId = roomId, isArchived = archived))
        }
    }

    fun liveTicketIdsFlow(): Flow<Set<String>> =
        ticketDao().observeAllTickets().map { list -> list.map { it.ticketId }.toSet() } 

    private fun ticketSnapshotFlow(): Flow<Map<String, Set<String>>> =
        ticketDao().observeAllTickets().map { list ->
            list.groupBy { it.roomId }.mapValues { (_, v) -> v.map { it.ticketId }.toSet() }
        }

    fun roomTicketCountsFlow(): Flow<Map<String, Int>> =
        ticketDao().observeValidTicketCountsByRoom().map { list ->
            list.associate { it.roomId to it.count }
        }

    fun assignedRoomIdFlow(ticketId: String): Flow<String?> =
        ticketSnapshotFlow().map { snapshot ->
            snapshot.entries.firstOrNull { it.value.contains(ticketId) }?.key
        }

    fun ticketToRoomFlow(): Flow<Map<String, String>> =
        ticketSnapshotFlow().map { snapshot ->
            snapshot.entries.flatMap { (roomId, ticketIds) -> ticketIds.map { it to roomId } }.toMap()
        }

    fun getRooms(): Flow<List<LiveRoom>> = roomsFlow()

    fun allRoomsCalledCountsFlow(): Flow<Map<String, Int>> =
        roomsFlow().flatMapLatest { rooms ->
            if (rooms.isEmpty()) flowOf(emptyMap())
            else combine(
                rooms.map { r ->
                    calledNumbersFlow(r.roomId).map { nums ->
                        RoomCalledCountEntry(r.roomId, nums.size)
                    }
                }
            ) { arr ->
                arr.associate { it.roomId to it.count }
            }
        }

    fun allRoomsCalledNumbersFlow(): Flow<Map<String, List<Int>>> =
        roomsFlow().flatMapLatest { rooms ->
            if (rooms.isEmpty()) flowOf(emptyMap())
            else combine(
                rooms.map { r ->
                    calledNumbersFlow(r.roomId).map { nums ->
                        RoomCalledNumbersEntry(r.roomId, nums)
                    }
                }
            ) { arr ->
                arr.associate { it.roomId to it.numbers }
            }
        }

    suspend fun getRoom(roomId: String): LiveRoom? = withContext(Dispatchers.IO) {
        roomDao().observeRoom(roomId).map { it?.toLiveRoom() }.first()
    }

    suspend fun createRoom(name: String): String = withContext(Dispatchers.IO) {
        val roomId = "room-${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val room = LiveRoomEntity(roomId, name.trim().ifBlank { "Room" }, now, now)
        roomDao().upsertRoom(room)
        settingsDao().upsertSettings(RoomSettingsEntity(roomId = roomId))
        roomId
    }

    fun renameRoom(roomId: String, name: String) {
        scope.launch {
            val current = roomDao().observeRoom(roomId).first() ?: return@launch
            roomDao().upsertRoom(current.copy(name = name.trim().ifBlank { current.name }, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteRoom(roomId: String) {
        scope.launch { deleteRoomSync(roomId) }
    }

    suspend fun deleteRoomSync(roomId: String) = withContext(Dispatchers.IO) {
        db().withTransaction {
            calledDao().clearCalled(roomId)
            ticketDao().clearTickets(roomId)
            settingsDao().deleteSettings(roomId)
            roomDao().deleteRoom(roomId)
        }
    }

    suspend fun getCalledNumbersForRoom(roomId: String): List<Int> = withContext(Dispatchers.IO) {
        calledDao().observeCalled(roomId).first().map { it.number }
    }

    fun addCalledNumberToRoom(roomId: String, number: Int) {
        if (number !in 1..75) return
        scope.launch {
            val existing = calledDao().observeCalled(roomId).first().map { it.number }
            if (existing.size >= MAX_LIVE_CALLS) return@launch
            if (number in existing) return@launch
            calledDao().insertCalled(RoomCalledNumberEntity(roomId, number))
            if (existing.size + 1 >= MAX_LIVE_CALLS) setRoomArchived(roomId, true)
        }
    }

    suspend fun addCalledNumberIfAllowed(roomId: String, number: Int): Boolean = withContext(Dispatchers.IO) {
        if (number !in 1..75) return@withContext false
        val room = roomDao().observeRoom(roomId).first()
        if (room != null && SundayBingoSchedule.isSundayFeaturedRoom(room.name)) {
            if (!SundayBingoSchedule.isLiveCallingUnlocked()) return@withContext false
        }
        val existing = calledDao().observeCalled(roomId).first().map { it.number }
        if (existing.size >= MAX_LIVE_CALLS) return@withContext false
        if (number in existing) return@withContext false
        calledDao().insertCalled(RoomCalledNumberEntity(roomId, number))
        if (existing.size + 1 >= MAX_LIVE_CALLS) setRoomArchived(roomId, true)
        true
    }

    suspend fun undoLastCall(roomId: String): Boolean = withContext(Dispatchers.IO) {
        val list = calledDao().observeCalled(roomId).first()
        val last = list.lastOrNull() ?: return@withContext false
        calledDao().deleteCalledNumber(roomId, last.number)
        true
    }

    suspend fun undoLastCalledNumber(roomId: String): Int? = withContext(Dispatchers.IO) {
        val list = calledDao().observeCalled(roomId).first()
        val last = list.lastOrNull() ?: return@withContext null
        calledDao().deleteCalledNumber(roomId, last.number)
        if (list.size - 1 < MAX_LIVE_CALLS) setRoomArchived(roomId, false)
        last.number
    }

    suspend fun getTicketsForRoom(roomId: String): List<String> = withContext(Dispatchers.IO) {
        ticketDao().observeTickets(roomId).first().map { it.ticketId }
    }

    suspend fun findAssignedRoomId(ticketId: String): String? = withContext(Dispatchers.IO) {
        ticketDao().getRoomIdForTicket(ticketId)
    }

    // Live rooms store ticketId only. Never use sessionId for RoomTicketEntity assignment.
    suspend fun assignTicketToRoom(roomId: String, ticketId: String): AssignTicketResult =
        withContext(Dispatchers.IO) {
            val existingRoomId = ticketDao().getRoomIdForTicket(ticketId)
            when {
                existingRoomId == null -> {
                    ticketDao().addTicket(RoomTicketEntity(roomId, ticketId))
                    AssignTicketResult.Success
                }
                existingRoomId == roomId -> AssignTicketResult.Success
                else -> AssignTicketResult.AlreadyInRoom(existingRoomId)
            }
        }

    suspend fun addTicketToRoom(roomId: String, ticketId: String): AssignTicketResult =
        assignTicketToRoom(roomId, ticketId)

    suspend fun moveTicketToRoom(ticketId: String, fromRoomId: String, toRoomId: String): AssignTicketResult =
        withContext(Dispatchers.IO) {
            try {
                ticketDao().moveTicket(ticketId, fromRoomId, toRoomId)
                AssignTicketResult.Success
            } catch (t: Throwable) {
                AssignTicketResult.Error(t.message ?: "Unknown error")
            }
        }

    fun removeTicketFromRoom(roomId: String, ticketId: String) {
        scope.launch { ticketDao().removeTicket(roomId, ticketId) }
    }

    fun unassignTicket(ticketId: String) {
        scope.launch {
            val roomId = ticketDao().getRoomIdForTicket(ticketId)
            if (roomId != null) ticketDao().removeTicket(roomId, ticketId)
        }
    }

    fun unassignTickets(ticketIds: Collection<String>) {
        ticketIds.distinct().forEach { unassignTicket(it) }
    }

    fun clearRoomCalledNumbers(roomId: String) {
        scope.launch { calledDao().clearCalled(roomId) }
    }

    suspend fun resetCalledNumbers(roomId: String) = withContext(Dispatchers.IO) {
        calledDao().clearCalled(roomId)
    }

    suspend fun ensureSundayFeaturedRoomCallsReset(roomId: String, roomName: String) =
        withContext(Dispatchers.IO) {
            if (!SundayBingoSchedule.isSundayFeaturedRoom(roomName)) return@withContext
            if (!SundayBingoSchedule.shouldResetStaleSundayCalls()) return@withContext
            val count = calledDao().observeCalled(roomId).first().size
            if (count == 0) return@withContext
            calledDao().clearCalled(roomId)
            setRoomArchived(roomId, false)
        }

    suspend fun ensureSundayFeaturedRoomCallsResetAll() = withContext(Dispatchers.IO) {
        roomDao().observeRooms().first().forEach { entity ->
            ensureSundayFeaturedRoomCallsReset(entity.roomId, entity.name)
        }
    }

    fun scheduleSundayFeaturedRoomMaintenance() {
        scope.launch { ensureSundayFeaturedRoomCallsResetAll() }
    }

    fun clearRoomTickets(roomId: String) {
        scope.launch { ticketDao().clearTickets(roomId) }
    }

    suspend fun isTicketInRoom(ticketId: String, roomId: String): Boolean = withContext(Dispatchers.IO) {
        ticketDao().observeTickets(roomId).first().any { it.ticketId == ticketId }
    }

    fun observeTicketCount(roomId: String): Flow<Int> =
        ticketDao().observeTickets(roomId).map { it.size }

    fun observeCalledCount(roomId: String): Flow<Int> =
        calledDao().observeCalled(roomId).map { it.size }

    suspend fun getTicketCountForRoom(roomId: String): Int = withContext(Dispatchers.IO) {
        ticketDao().observeTickets(roomId).first().size
    }

    suspend fun getCalledCountForRoom(roomId: String): Int = withContext(Dispatchers.IO) {
        calledDao().observeCalled(roomId).first().size
    }

    suspend fun upsertRoomSettings(settings: RoomSettings) {
        settingsDao().upsertSettings(settings.toEntity())
    }

    suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        val existing = roomDao().observeRooms().first()
        if (existing.isNotEmpty()) return@withContext
        val demoRooms = DemoDataFactory.createDemoRooms()
        demoRooms.forEach { room ->
            roomDao().upsertRoom(LiveRoomEntity(room.roomId, room.name, room.createdAt, room.createdAt))
            settingsDao().upsertSettings(RoomSettingsEntity(roomId = room.roomId))
            ticketDao().addTicket(RoomTicketEntity(room.roomId, DemoDataFactory.SESSION_1))
            ticketDao().addTicket(RoomTicketEntity(room.roomId, DemoDataFactory.SESSION_2))
        }
        DemoDataFactory.createDemoCalledNumbers().forEach { n ->
            calledDao().insertCalled(RoomCalledNumberEntity(DemoDataFactory.ROOM_1, n))
        }
    }
}

private fun LiveRoomEntity.toLiveRoom() = LiveRoom(roomId, name, createdAt, true)
private fun RoomSettingsEntity.toRoomSettings() = RoomSettings(roomId, autoCallEnabled, intervalSeconds, lastStartedAt, isRunning, isArchived)
private fun RoomSettings.toEntity() = RoomSettingsEntity(roomId, autoCallEnabled, intervalSeconds, lastStartedAt, isRunning, isArchived)

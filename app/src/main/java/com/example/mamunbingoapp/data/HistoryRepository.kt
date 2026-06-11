package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.core.BingoPlayableNumbers
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.core.SundayBingoSchedule
import com.example.mamunbingoapp.data.db.TicketCellEntity
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.model.TicketPickerMiniGridCell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class TicketDetailData(
    val sheetName: String,
    val playedAtMillis: Long,
    val cells: List<BingoCellUi>,
    val calledNumbers: List<Int>,
    val losNumber: String? = null,
    val serialNumber: String? = null,
    val playLogs: List<TicketPlayLog> = emptyList(),
)

const val HOME_ACTIVE_TICKET_MAX_AGE_MS = 14L * 24 * 60 * 60 * 1000

enum class HomeActiveTicketTier(val sortOrder: Int) {
    LIVE_SUNDAY(0),
    PRACTICE(1),
    ARCHIVED(2),
}

data class HomeActiveTicketQuery(
    val sessions: List<HistorySession>,
    val cellsByTicket: Map<String, List<TicketCellEntity>>,
    val ticketToRoom: Map<String, String>,
    val roomNamesById: Map<String, String>,
    val calledNumbersByRoom: Map<String, List<Int>>,
    val archivedByRoom: Map<String, Boolean>,
    val archivedCalledByTicket: Map<String, List<Int>>,
    val latestPlayLogByTicket: Map<String, TicketPlayLog>,
    val testDatesBySession: Map<String, Long>,
    val nowMillis: Long = System.currentTimeMillis(),
)

data class HomeActiveTicketSelection(
    val session: HistorySession,
    val tier: HomeActiveTicketTier,
    val hasWin: Boolean,
    val updatedAtMillis: Long,
)

object HistoryRepository {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var _activeTicketId: String? = null

    fun setActiveTicketId(ticketId: String) { _activeTicketId = ticketId }
    fun getActiveTicketId(): String? = _activeTicketId

    private val _demoSessionsFlow = MutableStateFlow<List<HistorySession>>(emptyList())
    private val _demoTicketCells = mutableMapOf<String, List<BingoCellUi>>()

    private val _sessionsFlow = MutableStateFlow<List<HistorySession>>(emptyList())
    val sessionsFlow: StateFlow<List<HistorySession>> = _sessionsFlow.asStateFlow()

    init {
        scope.launch {
            combine(
                SettingsRepository.showDemoDataFlow.distinctUntilChanged(),
                _demoSessionsFlow,
                TicketRepository.ticketsFlow()
            ) { showDemo, demo, db ->
                if (showDemo) demo + db else db
            }.collect { _sessionsFlow.value = it }
        }
    }

    fun getLatestTicketId(): String? = _sessionsFlow.value.firstOrNull()?.id

    suspend fun getTicketCells(ticketId: String): List<BingoCellUi>? =
        getTicketData(ticketId)?.cells?.takeIf { it.size == 25 }

    fun getAll(): List<HistorySession> = _sessionsFlow.value

    /**
     * Best home carousel ticket from the last [HOME_ACTIVE_TICKET_MAX_AGE_MS]:
     * live Sunday room → practice room → archived room; then win, then newest.
     */
    fun getHomeActiveTicket(query: HomeActiveTicketQuery): HomeActiveTicketSelection? {
        val cutoff = query.nowMillis - HOME_ACTIVE_TICKET_MAX_AGE_MS
        return query.sessions.mapNotNull { session ->
            val ticketId = session.ticketId
            val roomId = query.ticketToRoom[ticketId] ?: query.ticketToRoom[session.id]
            val updatedAtMillis = homeActiveTicketUpdatedAtMillis(
                session = session,
                ticketId = ticketId,
                latestPlayLog = query.latestPlayLogByTicket[ticketId],
                testDateMillis = query.testDatesBySession[session.id],
            )
            if (updatedAtMillis < cutoff) return@mapNotNull null
            val tier = resolveHomeActiveTicketTier(
                roomId = roomId,
                roomName = roomId?.let { query.roomNamesById[it] },
                archivedByRoom = query.archivedByRoom,
                archivedCalled = query.archivedCalledByTicket[ticketId].orEmpty(),
            ) ?: return@mapNotNull null
            val calledNumbers = resolveHomeActiveTicketCalledNumbers(
                session = session,
                ticketId = ticketId,
                roomId = roomId,
                query = query,
            )
            val hasWin = homeActiveTicketHasWin(
                cells = query.cellsByTicket[ticketId].orEmpty(),
                calledNumbers = calledNumbers,
                latestPlayLog = query.latestPlayLogByTicket[ticketId],
            )
            HomeActiveTicketSelection(
                session = session,
                tier = tier,
                hasWin = hasWin,
                updatedAtMillis = updatedAtMillis,
            )
        }.sortedWith(
            compareBy<HomeActiveTicketSelection> { it.tier.sortOrder }
                .thenByDescending { it.hasWin }
                .thenByDescending { it.updatedAtMillis },
        ).firstOrNull()
    }

    fun homeActiveTicketUpdatedAtMillis(
        session: HistorySession,
        ticketId: String,
        latestPlayLog: TicketPlayLog?,
        testDateMillis: Long?,
    ): Long = listOfNotNull(
        session.effectivePlayedAtMillis(),
        latestPlayLog?.archivedAt?.takeIf { it > 0L },
        latestPlayLog?.addedAt?.takeIf { it > 0L },
        testDateMillis,
    ).maxOrNull() ?: session.effectivePlayedAtMillis()

    private fun resolveHomeActiveTicketTier(
        roomId: String?,
        roomName: String?,
        archivedByRoom: Map<String, Boolean>,
        archivedCalled: List<Int>,
    ): HomeActiveTicketTier? {
        if (!roomId.isNullOrBlank()) {
            if (archivedByRoom.getOrDefault(roomId, false)) {
                return HomeActiveTicketTier.ARCHIVED
            }
            return if (SundayBingoSchedule.isSundayFeaturedRoom(roomName.orEmpty())) {
                HomeActiveTicketTier.LIVE_SUNDAY
            } else {
                HomeActiveTicketTier.PRACTICE
            }
        }
        if (archivedCalled.isNotEmpty()) {
            return HomeActiveTicketTier.ARCHIVED
        }
        return null
    }

    private fun resolveHomeActiveTicketCalledNumbers(
        session: HistorySession,
        ticketId: String,
        roomId: String?,
        query: HomeActiveTicketQuery,
    ): List<Int> {
        if (!roomId.isNullOrBlank() && !query.archivedByRoom.getOrDefault(roomId, false)) {
            return query.calledNumbersByRoom[roomId].orEmpty()
        }
        return query.archivedCalledByTicket[ticketId]
            ?: query.latestPlayLogByTicket[ticketId]?.calledNumbers
            ?: session.calledNumbersFull
    }

    private fun homeActiveTicketHasWin(
        cells: List<TicketCellEntity>,
        calledNumbers: List<Int>,
        latestPlayLog: TicketPlayLog?,
    ): Boolean {
        if ((latestPlayLog?.bingoLineCount ?: 0) > 0) return true
        if (cells.isEmpty()) return false
        val markedSet = TicketCalledNumbersResolver
            .buildActiveTicketCellStates(cells, calledNumbers)
            .mapIndexed { index, cell -> index.takeIf { cell.isCalled } }
            .filterNotNull()
            .toSet()
        return BingoWinChecker.check(markedSet).isWin
    }

    suspend fun getAllTicketsForPicker(roomId: String? = null): List<com.example.mamunbingoapp.ui.model.TicketUiModel> =
        withContext(Dispatchers.Default) {
            val ticketToRoom = RoomRepository.ticketToRoomFlow().first()
            val cellsByTicket = TicketRepository.cellsByTicketFlow().first()
            _sessionsFlow.value.map { session ->
                toPickerTicketUiModel(session, ticketToRoom, cellsByTicket, roomId)
            }
        }

    fun ticketsForPickerFlow(roomId: String?): Flow<List<com.example.mamunbingoapp.ui.model.TicketUiModel>> =
        combine(
            _sessionsFlow,
            RoomRepository.ticketToRoomFlow(),
            TicketRepository.cellsByTicketFlow(),
        ) { sessions, ticketToRoom, cellsByTicket ->
            sessions.map { session ->
                toPickerTicketUiModel(session, ticketToRoom, cellsByTicket, roomId)
            }
        }

    suspend fun getById(id: String): HistorySession? = withContext(Dispatchers.Default) {
        TicketRepository.getSessionById(id) ?: _demoSessionsFlow.value.find { it.id == id }
    }

    fun observeSession(sessionId: String): kotlinx.coroutines.flow.Flow<HistorySession?> =
        sessionsFlow.map { list -> list.find { it.id == sessionId } }

    fun observeHistoryTestDate(sessionId: String): Flow<Long?> =
        HistoryTestDateRepository.observeTestDate(sessionId)

    fun observeAllHistoryTestDates(): Flow<Map<String, Long>> =
        HistoryTestDateRepository.observeAllTestDates()

    suspend fun updateHistorySessionTestDate(sessionId: String, dateMillis: Long) {
        HistoryTestDateRepository.setTestDate(sessionId, dateMillis)
    }

    suspend fun clearHistorySessionTestDate(sessionId: String) {
        HistoryTestDateRepository.clearTestDate(sessionId)
    }

    suspend fun getSessionIdForTicket(ticketId: String): String? = withContext(Dispatchers.Default) {
        TicketRepository.getSessionById(ticketId)?.id
            ?: getById(ticketId)?.id
            ?: _demoSessionsFlow.value.firstOrNull { it.sheetsPlayed.any { s -> s.ticketId == ticketId } }?.id
    }

    fun getLiveSessions(): List<HistorySession> = _sessionsFlow.value.filter { !it.isCompleted }

    suspend fun getSessionById(id: String): HistorySession? = getById(id)

    fun removeFromLive(sessionId: String) {
        val list = _demoSessionsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == sessionId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(isCompleted = true)
            _demoSessionsFlow.value = list
        }
    }

    fun addToLive(sessionId: String) {
        val list = _demoSessionsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == sessionId }
        if (idx >= 0) {
            list[idx] = list[idx].copy(isCompleted = false)
            _demoSessionsFlow.value = list
        }
    }

    fun deleteSession(sessionId: String) {
        val demo = _demoSessionsFlow.value.find { it.id == sessionId }
        if (demo != null) {
            demo.sheetsPlayed.forEach { _demoTicketCells.remove(it.ticketId) }
            _demoTicketCells.remove(sessionId)
            _demoSessionsFlow.value = _demoSessionsFlow.value.filter { it.id != sessionId }
        } else {
            scope.launch { TicketRepository.deleteTicket(sessionId) }
        }
        RoomRepository.unassignTicket(sessionId)
    }

    fun deleteSessions(sessionIds: Collection<String>) {
        sessionIds.distinct().forEach { deleteSession(it) }
    }

    fun softDeleteSession(sessionId: String) {
        scope.launch { softDeleteSessionSync(sessionId) }
    }

    suspend fun softDeleteSessionSync(sessionId: String) = withContext(Dispatchers.IO) {
        val demo = _demoSessionsFlow.value.find { it.id == sessionId }
        if (demo != null) {
            demo.sheetsPlayed.forEach { _demoTicketCells.remove(it.ticketId) }
            _demoTicketCells.remove(sessionId)
            _demoSessionsFlow.value = _demoSessionsFlow.value.filter { it.id != sessionId }
        } else {
            TicketRepository.softDeleteTicket(sessionId)
        }
        RoomRepository.unassignTicket(sessionId)
    }

    fun restoreSession(sessionId: String) {
        scope.launch { TicketRepository.restoreTicket(sessionId) }
    }

    suspend fun getTicketData(ticketId: String): TicketDetailData? = withContext(Dispatchers.Default) {
        val fromDb = TicketRepository.getTicketData(ticketId)
        if (fromDb != null) return@withContext fromDb.copy(
            cells = fromDb.cells.map { it.copy(isEditable = false, isSelected = false) }
        )
        val toDisplayCells: (List<BingoCellUi>) -> List<BingoCellUi> = { list ->
            (list.ifEmpty { List(25) { BingoCellUi(null, false, false, false, false) } })
                .map { it.copy(isEditable = false, isSelected = false) }
        }
        val demo = _demoSessionsFlow.value
        val sessionById = demo.find { it.id == ticketId }
        if (sessionById != null) {
            val cells = toDisplayCells(_demoTicketCells[ticketId] ?: emptyList())
            return@withContext TicketDetailData(
                sheetName = sessionById.effectiveSheetName(),
                playedAtMillis = sessionById.effectivePlayedAtMillis(),
                cells = cells,
                calledNumbers = sessionById.calledNumbersFull,
                losNumber = sessionById.losNumber,
                serialNumber = sessionById.serialNumber,
            )
        }
        for (session in demo) {
            val sheet = session.sheetsPlayed.find { it.ticketId == ticketId } ?: continue
            val cells = toDisplayCells(_demoTicketCells[ticketId] ?: emptyList())
            return@withContext TicketDetailData(
                sheetName = sheet.title,
                playedAtMillis = session.effectivePlayedAtMillis(),
                cells = cells,
                calledNumbers = session.calledNumbersFull,
                losNumber = session.losNumber,
                serialNumber = session.serialNumber,
            )
        }
        return@withContext null
    }

    fun seedDemoData() {
        _demoSessionsFlow.value = DemoDataFactory.createDemoSessions()
        _demoTicketCells.clear()
        _demoTicketCells.putAll(DemoDataFactory.createDemoTicketCells())
    }

    fun clearDemoCaches() {
        _demoSessionsFlow.value = emptyList()
        _demoTicketCells.clear()
    }

    suspend fun duplicateSession(sessionId: String): String? = withContext(Dispatchers.Default) {
        try {
            val demo = _demoSessionsFlow.value.find { it.id == sessionId }
            if (demo != null) {
                val newSessionId = UUID.randomUUID().toString()
                val oldToNewTicketIds = demo.sheetsPlayed.map { it.ticketId to UUID.randomUUID().toString() }
                val newSheetsPlayed = demo.sheetsPlayed.mapIndexed { index, sheet ->
                    val newTicketId = oldToNewTicketIds[index].second
                    SheetPlayed(
                        ticketId = newTicketId,
                        title = sheet.title,
                        subtitle = sheet.subtitle,
                        markedCount = sheet.markedCount,
                        totalCount = sheet.totalCount
                    )
                }
                val newTitle = demo.title.removeSuffix(" (Copy)") + " (Copy)"
                val duplicated = HistorySession(
                    id = newSessionId,
                    title = newTitle,
                    isCompleted = demo.isCompleted,
                    sheetsCount = demo.sheetsCount,
                    calledCount = demo.calledCount,
                    calledNumbersPreview = demo.calledNumbersPreview,
                    calledNumbersFull = demo.calledNumbersFull,
                    sheetsPlayed = newSheetsPlayed,
                    sheetName = demo.sheetName,
                    playedAtMillis = System.currentTimeMillis(),
                    ocrSource = demo.ocrSource,
                    ocrConfidence = demo.ocrConfidence,
                    originalOcrNumbers = demo.originalOcrNumbers,
                    losNumber = demo.losNumber,
                    serialNumber = demo.serialNumber,
                )
                oldToNewTicketIds.forEach { (oldId, newId) ->
                    _demoTicketCells[newId] = (_demoTicketCells[oldId] ?: emptyList()).map { it.copy() }
                }
                _demoTicketCells[sessionId]?.let { cells ->
                    _demoTicketCells[newSessionId] = cells.map { it.copy() }
                }
                _demoSessionsFlow.value = listOf(duplicated) + _demoSessionsFlow.value
                newSessionId
            } else {
                TicketRepository.duplicateTicket(sessionId)
            }
        } catch (e: Exception) {
            android.util.Log.e("HistoryRepository", "duplicateSession failed", e)
            null
        }
    }

    private fun toPickerTicketUiModel(
        session: HistorySession,
        ticketToRoom: Map<String, String>,
        cellsByTicket: Map<String, List<TicketCellEntity>>,
        roomId: String?,
    ): com.example.mamunbingoapp.ui.model.TicketUiModel {
        val ticketId = session.ticketId
        val assignedRoomId = ticketToRoom[ticketId] ?: ticketToRoom[session.id]
        return com.example.mamunbingoapp.ui.model.TicketUiModel(
            id = ticketId,
            sessionId = session.id,
            title = session.effectiveSheetName().ifBlank { "Unnamed" },
            createdAt = session.effectivePlayedAtMillis(),
            serialNumber = session.serialNumber,
            losNumber = session.losNumber,
            isInRoom = roomId != null && assignedRoomId == roomId,
            assignedRoomId = assignedRoomId,
            miniGridCells = pickerMiniGridForTicket(ticketId, cellsByTicket),
        )
    }

    private fun pickerMiniGridForTicket(
        ticketId: String,
        cellsByTicket: Map<String, List<TicketCellEntity>>,
    ): List<TicketPickerMiniGridCell> {
        cellsByTicket[ticketId]?.let { return pickerMiniGridFromCellEntities(it) }
        _demoTicketCells[ticketId]?.let { return pickerMiniGridFromBingoCells(it) }
        return pickerMiniGridFromCellEntities(null)
    }

    private fun pickerMiniGridFromCellEntities(cells: List<TicketCellEntity>?): List<TicketPickerMiniGridCell> =
        List(BingoPlayableNumbers.GRID_CELL_COUNT) { i ->
            val cell = cells?.getOrNull(i)
            val display = cell?.value?.trim().orEmpty().let { raw ->
                when {
                    raw.isEmpty() -> ""
                    raw.equals("FREE", ignoreCase = true) -> "FREE"
                    else -> raw
                }
            }
            TicketPickerMiniGridCell(display = display, isMarked = cell?.isMarked == true)
        }

    private fun pickerMiniGridFromBingoCells(cells: List<BingoCellUi>): List<TicketPickerMiniGridCell> =
        List(BingoPlayableNumbers.GRID_CELL_COUNT) { i ->
            val cell = cells.getOrNull(i)
            val display = cell?.number?.trim().orEmpty().let { raw ->
                when {
                    raw.isEmpty() -> ""
                    raw.equals("FREE", ignoreCase = true) -> "FREE"
                    else -> raw
                }
            }
            TicketPickerMiniGridCell(display = display, isMarked = cell?.isMarked == true)
        }
}

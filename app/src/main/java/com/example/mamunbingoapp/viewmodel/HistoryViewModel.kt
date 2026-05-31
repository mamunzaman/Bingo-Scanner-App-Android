package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.core.AlmostBingoInfo
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.core.SheetStatusResolver
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.LiveRoom
import com.example.mamunbingoapp.data.TicketPlayLogRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.data.db.TicketCellEntity
import com.example.mamunbingoapp.ui.model.SheetStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class HistorySortOption(val displayName: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    NAME_AZ("Name (A–Z)"),
    DATE("Date")
}

enum class HistoryFilter(val displayName: String) {
    ALL("All"),
    SAVED("Saved"),
    COMPLETED("Completed"),
    ARCHIVED("Archived")
}

enum class HistorySourceFilter(val displayName: String) {
    ALL("All"),
    OCR_IMPORTS("OCR Imports"),
    MANUAL("Manual")
}

data class HistoryFilterCounts(
    val all: Int = 0,
    val saved: Int = 0,
    val completed: Int = 0,
    val archived: Int = 0
)

private data class HistoryMarkedCellsBundle(
    val markedCountsByTicket: Map<String, Int>,
    val cellsByTicket: Map<String, List<TicketCellEntity>>
)

private data class HistorySessionsCoreSnapshot(
    val sessionsList: List<HistorySession>,
    val ticketToRoom: Map<String, String>,
    val rooms: List<LiveRoom>
)

private data class HistoryCalledSnapshot(
    val calledCountsByRoom: Map<String, Int>,
    val calledNumbersByRoom: Map<String, List<Int>>
)

private fun Long.isToday(): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = this@isToday }
    val today = Calendar.getInstance()
    return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

private fun Long.isThisWeek(): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = this@isThisWeek }
    val now = Calendar.getInstance()
    val weekStart = now.clone() as Calendar
    weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
    weekStart.set(Calendar.HOUR_OF_DAY, 0)
    weekStart.set(Calendar.MINUTE, 0)
    weekStart.set(Calendar.SECOND, 0)
    val weekEnd = weekStart.clone() as Calendar
    weekEnd.add(Calendar.DAY_OF_YEAR, 7)
    return !cal.before(weekStart) && cal.before(weekEnd)
}

data class HistorySessionUi(
    val session: HistorySession,
    val isLive: Boolean,
    val roomId: String? = null,
    val roomName: String? = null,
    val resolvedCalledCount: Int = 0,
    val resolvedMarkedCount: Int = 0,
    val resolvedMarkedCells: List<Boolean> = emptyList(),
    val almostBingo: AlmostBingoInfo? = null,
    val bingoWinLineCount: Int? = null,
    val editedAfterOcr: Boolean = false,
    val ocrCorrectionCount: Int = 0
)

private data class HistoryFilterSortQuery(
    val list: List<HistorySessionUi>,
    val query: String,
    val filterOpt: HistoryFilter,
    val sortOpt: HistorySortOption
)

private data class HistoryFilterInputs(
    val list: List<HistorySessionUi>,
    val query: String,
    val filterOpt: HistoryFilter,
    val sortOpt: HistorySortOption,
    val sourceFilter: HistorySourceFilter,
    val archivedByRoom: Map<String, Boolean>
)

class HistoryViewModel : ViewModel() {
    val sessions: StateFlow<List<HistorySession>> = HistoryRepository.sessionsFlow

    val liveRooms: StateFlow<List<LiveRoom>> = RoomRepository.getRooms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sessionsWithLiveStatus: StateFlow<List<HistorySessionUi>> = combine(
        combine(
            HistoryRepository.sessionsFlow,
            RoomRepository.ticketToRoomFlow(),
            RoomRepository.getRooms()
        ) { sessionsList, ticketToRoom, rooms ->
            HistorySessionsCoreSnapshot(sessionsList, ticketToRoom, rooms)
        },
        combine(
            RoomRepository.allRoomsCalledCountsFlow(),
            RoomRepository.allRoomsCalledNumbersFlow()
        ) { calledCountsByRoom, calledNumbersByRoom ->
            HistoryCalledSnapshot(calledCountsByRoom, calledNumbersByRoom)
        },
        combine(
            TicketRepository.markedCountsByTicketFlow(),
            TicketRepository.cellsByTicketFlow()
        ) { markedCountsByTicket, cellsByTicket ->
            HistoryMarkedCellsBundle(markedCountsByTicket, cellsByTicket)
        },
        TicketPlayLogRepository.observeLatestCalledNumbersByTicket(),
    ) { core, called, markedBundle, archivedCalledByTicket ->
        val sessionsList = core.sessionsList
        val ticketToRoom = core.ticketToRoom
        val rooms = core.rooms
        val calledCountsByRoom = called.calledCountsByRoom
        val calledNumbersByRoom = called.calledNumbersByRoom
        val markedCountsByTicket = markedBundle.markedCountsByTicket
        val cellsByTicket = markedBundle.cellsByTicket
        sessionsList.map { session ->
            val roomId = ticketToRoom[session.id]
            val roomName = roomId?.let { id -> rooms.find { it.roomId == id }?.name }
            val archivedCalled = archivedCalledByTicket[session.id].orEmpty()
            val resolvedCalledCount = if (roomId != null) {
                calledCountsByRoom[roomId] ?: session.calledNumbersFull.size
            } else {
                archivedCalled.size.takeIf { it > 0 } ?: session.calledNumbersFull.size
            }
            val resolvedMarkedCount = markedCountsByTicket[session.ticketId]
                ?: session.sheetsPlayed.firstOrNull()?.markedCount ?: 0
            val cells = cellsByTicket[session.ticketId] ?: emptyList()
            val calledNumbers = (
                roomId?.let { calledNumbersByRoom[it] }
                    ?: archivedCalled.ifEmpty { session.calledNumbersFull }
                ).toSet()
            val resolvedMarkedCells = List(25) { i ->
                val cell = cells.getOrNull(i)
                val num = cell?.value?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
                (num != null && num in 1..75 && num in calledNumbers) || (cell?.isMarked == true)
            }
            val winResult = BingoWinChecker.check(resolvedMarkedCells.mapIndexed { i, b -> i.takeIf { b } }.filterNotNull().toSet())
            val almostBingo = BingoWinChecker.bestAlmostBingo(resolvedMarkedCells)
            val bingoWinLineCount = if (winResult.isWin) winResult.winningLines.size else null
            val (editedAfterOcr, ocrCorrectionCount) = if (session.ocrSource in listOf("GEMINI", "ML_KIT") && session.originalOcrNumbers?.isNotBlank() == true && cells.size >= 25) {
                val originalList = session.originalOcrNumbers!!.split(",").mapNotNull { it.trim().toIntOrNull() }.take(25)
                val savedRowMajor = cells.take(25).map { it.value?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull() ?: 0 }
                val savedForCompare = if (session.ocrSource == "ML_KIT") rowMajorToColumnMajor(savedRowMajor) else savedRowMajor
                val n = minOf(originalList.size, savedForCompare.size)
                val count = (0 until n).count { originalList[it] != savedForCompare[it] }
                (n > 0 && count > 0) to count
            } else false to 0
            HistorySessionUi(
                session = session,
                isLive = roomId != null,
                roomId = roomId,
                roomName = roomName,
                resolvedCalledCount = resolvedCalledCount,
                resolvedMarkedCount = resolvedMarkedCount,
                resolvedMarkedCells = resolvedMarkedCells,
                almostBingo = almostBingo,
                bingoWinLineCount = bingoWinLineCount,
                editedAfterOcr = editedAfterOcr,
                ocrCorrectionCount = ocrCorrectionCount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(HistorySortOption.NEWEST)
    val selectedSort: StateFlow<HistorySortOption> = _selectedSort.asStateFlow()

    private val _selectedSourceFilter = MutableStateFlow(HistorySourceFilter.ALL)
    val selectedSourceFilter: StateFlow<HistorySourceFilter> = _selectedSourceFilter.asStateFlow()

    fun setQuery(s: String) { _query.value = s }
    fun setFilter(option: HistoryFilter) { _selectedFilter.value = option }
    fun setSort(option: HistorySortOption) { _selectedSort.value = option }
    fun setSourceFilter(option: HistorySourceFilter) { _selectedSourceFilter.value = option }

    val filterCounts: StateFlow<HistoryFilterCounts> = combine(
        sessionsWithLiveStatus,
        RoomRepository.roomsArchivedMapFlow()
    ) { list, archivedByRoom ->
        HistoryFilterCounts(
            all = list.size,
            saved = list.count { matchesFilter(it, HistoryFilter.SAVED, archivedByRoom) },
            completed = list.count { matchesFilter(it, HistoryFilter.COMPLETED, archivedByRoom) },
            archived = list.count { matchesFilter(it, HistoryFilter.ARCHIVED, archivedByRoom) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryFilterCounts()
    )

    val filteredSessions: StateFlow<List<HistorySessionUi>> = combine(
        combine(
            sessionsWithLiveStatus,
            _query,
            _selectedFilter,
            _selectedSort
        ) { list, q, filterOpt, sortOpt ->
            HistoryFilterSortQuery(list, q, filterOpt, sortOpt)
        },
        combine(_selectedSourceFilter, RoomRepository.roomsArchivedMapFlow()) { sourceFilter, archivedByRoom ->
            sourceFilter to archivedByRoom
        }
    ) { fsq, sourceAndArchived ->
        val (sourceFilter, archivedByRoom) = sourceAndArchived
        val inputs = HistoryFilterInputs(
            fsq.list,
            fsq.query,
            fsq.filterOpt,
            fsq.sortOpt,
            sourceFilter,
            archivedByRoom
        )
        var result = inputs.list
        val trimmed = inputs.query.trim()
        if (trimmed.isNotBlank()) {
            result = result.filter { item ->
                item.session.effectiveSheetName().contains(trimmed, ignoreCase = true) ||
                    (item.roomName?.contains(trimmed, ignoreCase = true) == true)
            }
        }
        result = result.filter { item ->
            matchesFilter(item, inputs.filterOpt, inputs.archivedByRoom)
        }
        result = result.filter { item ->
            when (inputs.sourceFilter) {
                HistorySourceFilter.ALL -> true
                HistorySourceFilter.OCR_IMPORTS -> item.session.ocrSource in listOf("GEMINI", "ML_KIT")
                HistorySourceFilter.MANUAL -> item.session.ocrSource == null || item.session.ocrSource !in listOf("GEMINI", "ML_KIT")
            }
        }
        result = when (inputs.sortOpt) {
            HistorySortOption.NEWEST -> result.sortedByDescending { it.session.effectivePlayedAtMillis() }
            HistorySortOption.OLDEST -> result.sortedBy { it.session.effectivePlayedAtMillis() }
            HistorySortOption.NAME_AZ -> result.sortedBy { it.session.effectiveSheetName().lowercase() }
            HistorySortOption.DATE -> result.sortedByDescending { it.session.effectivePlayedAtMillis() }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    suspend fun getSession(id: String): HistorySession? = HistoryRepository.getById(id)

    /**
     * Adds eligible sessions to [roomId]. Sessions already assigned to any room are silently
     * skipped to prevent duplicates. State refreshes automatically via the DB flow.
     */
    fun addSessionsToRoom(roomId: String, sessionIds: Collection<String>) {
        viewModelScope.launch {
            val allSessions = sessions.first()
            sessionIds.forEach { sid ->
                val session = allSessions.firstOrNull { it.id == sid } ?: return@forEach
                val ticketId = session.ticketId
                val alreadyAssigned = RoomRepository.findAssignedRoomId(ticketId) != null
                if (!alreadyAssigned) {
                    RoomRepository.assignTicketToRoom(roomId, ticketId)
                }
            }
        }
    }
}

private fun matchesFilter(
    item: HistorySessionUi,
    filter: HistoryFilter,
    archivedByRoom: Map<String, Boolean>
): Boolean {
    return when (filter) {
        HistoryFilter.ALL -> true
        HistoryFilter.SAVED -> SheetStatusResolver.resolve(
            assignedRoomId = item.roomId,
            calledCount = item.resolvedCalledCount
        ) == SheetStatus.IDLE
        HistoryFilter.COMPLETED -> SheetStatusResolver.resolve(
            assignedRoomId = item.roomId,
            calledCount = item.resolvedCalledCount
        ) == SheetStatus.COMPLETED
        HistoryFilter.ARCHIVED -> item.roomId != null && (archivedByRoom[item.roomId] == true)
    }
}

private fun rowMajorToColumnMajor(rowMajor: List<Int>): List<Int> =
    if (rowMajor.size < 25) rowMajor else (0..24).map { i -> rowMajor[(i % 5) * 5 + i / 5] }

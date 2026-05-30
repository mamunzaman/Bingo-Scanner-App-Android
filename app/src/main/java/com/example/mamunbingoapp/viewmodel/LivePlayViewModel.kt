package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.data.AssignTicketResult
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.data.db.TicketEntity
import com.example.mamunbingoapp.ui.components.RoomConflictUi
import com.example.mamunbingoapp.ui.model.BingoCellUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import kotlin.OptIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.ui.model.RoomStatus

sealed class LivePlayUiEvent {
    data object CallLimitReachedDialog : LivePlayUiEvent()
}

data class LiveSheetUi(
    val ticketId: String,
    val title: String,
    val playedAtMillis: Long,
    val cells: List<BingoCellUi>,
    val markedCount: Int,
    val losNumber: String? = null,
    val serialNumber: String? = null
)

data class LivePlayUiState(
    val sheetName: String = "",
    val playedAtMillis: Long = System.currentTimeMillis(),
    val cells: List<BingoCellUi> = emptyList(),
    val calledNumbers: List<Int> = emptyList(),
    val lastCalled: Int? = null,
    val lastCalledAtMillis: Long? = null,
    val sheets: List<LiveSheetUi> = emptyList(),
    val selectedIndex: Int = 0,
    val winningCells: Set<Int> = emptySet(),
    val effectiveStatus: RoomStatus = RoomStatus.RUNNING
) {
    val hasData: Boolean get() = cells.isNotEmpty() || calledNumbers.isNotEmpty()
    val isCallLimitReached: Boolean get() = calledNumbers.size >= MAX_LIVE_CALLS
}

@OptIn(ExperimentalCoroutinesApi::class)
class LivePlayViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val currentRoomId = MutableStateFlow(savedStateHandle.get<String>("roomId")?.takeIf { it.isNotBlank() })
    private val selectedTicketIdFlow = MutableStateFlow(savedStateHandle.get<String>("ticketId")?.takeIf { it.isNotBlank() })
    private val _showResetConfirm = MutableStateFlow(false)
    val showResetConfirm: StateFlow<Boolean> = _showResetConfirm.asStateFlow()
    private val _showResetProtectionDialog = MutableStateFlow(false)
    val showResetProtectionDialog: StateFlow<Boolean> = _showResetProtectionDialog.asStateFlow()
    private val _roomConflict = MutableStateFlow(RoomConflictUi())
    val roomConflict: StateFlow<RoomConflictUi> = _roomConflict.asStateFlow()
    private val _pendingNavigateToRoomId = MutableStateFlow<String?>(null)
    val pendingNavigateToRoomId: StateFlow<String?> = _pendingNavigateToRoomId.asStateFlow()
    private val _events = MutableSharedFlow<LivePlayUiEvent>()
    val events = _events.asSharedFlow()
    private var hasShownLimitDialog = false
    private var _conflictTicketId: String? = null
    private var lastArchiveCorrectionRoomId: String? = null

    fun bind(roomId: String) {
        val safeId = roomId.takeIf { it.isNotBlank() }
        if (currentRoomId.value == safeId) return
        currentRoomId.value = safeId
        savedStateHandle["roomId"] = roomId
    }

    fun undoLastCalledNumber() {
        viewModelScope.launch {
            val rid = currentRoomId.value ?: return@launch
            if (rid.isBlank()) return@launch
            RoomRepository.undoLastCalledNumber(rid)
        }
    }

    // Crash fix: only run repository flows when roomId is non-blank (avoid DAO/Flow with empty id)
    val state: StateFlow<LivePlayUiState> = currentRoomId
        .flatMapLatest { rid ->
            if (rid.isNullOrBlank()) return@flatMapLatest flowOf(LivePlayUiState())
            val safeRid = rid
            val ticketIdsFlow = RoomRepository.roomTicketsFlow(safeRid)
            val baseStateFlow = combine(
                ticketIdsFlow.flatMapLatest { ticketIds ->
                    Log.d("LivePlayVM", "Room $safeRid has tickets: $ticketIds")
                    if (ticketIds.isEmpty()) flowOf(emptyList<LiveSheetUi>())
                    else combine(ticketIds.map { tid -> flowForOneTicket(tid) }) { arr -> arr.filterNotNull() }
                }.distinctUntilChanged(),
                RoomRepository.calledNumbersWithLastCalledAtFlow(safeRid),
                selectedTicketIdFlow
            ) { baseSheets, calledAndLast, selectedId ->
                val (calledNumbers, lastCalledAtMillis) = calledAndLast
                buildStateFromSheets(baseSheets, calledNumbers, lastCalledAtMillis, selectedId)
            }
            combine(baseStateFlow, RoomRepository.roomArchivedFlow(safeRid)) { s, isArchived ->
                val isCallLimitReached = s.calledNumbers.size >= MAX_LIVE_CALLS
                if (isArchived && !isCallLimitReached) {
                    if (lastArchiveCorrectionRoomId != safeRid) {
                        lastArchiveCorrectionRoomId = safeRid
                        viewModelScope.launch { RoomRepository.setRoomArchived(safeRid, false) }
                    }
                } else {
                    lastArchiveCorrectionRoomId = null
                }
                val effectiveStatus = when {
                    !isCallLimitReached -> RoomStatus.RUNNING
                    isArchived -> RoomStatus.FINISHED
                    else -> RoomStatus.IDLE
                }
                s.copy(effectiveStatus = effectiveStatus)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LivePlayUiState())

    init {
        viewModelScope.launch {
            state.collect { s ->
                if (s.calledNumbers.size >= MAX_LIVE_CALLS) {
                    if (!hasShownLimitDialog) {
                        hasShownLimitDialog = true
                        _events.emit(LivePlayUiEvent.CallLimitReachedDialog)
                    }
                } else {
                    hasShownLimitDialog = false
                }
            }
        }
    }

    fun refresh(ticketIdOverride: String? = null) {
        if (!ticketIdOverride.isNullOrBlank()) selectedTicketIdFlow.value = ticketIdOverride
    }

    fun selectTicket(ticketId: String) {
        if (ticketId.isBlank()) return
        selectedTicketIdFlow.value = ticketId
        savedStateHandle["ticketId"] = ticketId
    }

    fun addTicketToRoom(ticketId: String) {
        val rid = currentRoomId.value ?: return
        viewModelScope.launch {
            when (val r = RoomRepository.assignTicketToRoom(rid, ticketId)) {
                is AssignTicketResult.Success -> refresh(ticketId)
                is AssignTicketResult.AlreadyInRoom -> {
                    val roomName = RoomRepository.getRoom(r.existingRoomId)?.name ?: "another room"
                    _conflictTicketId = ticketId
                    _roomConflict.value = RoomConflictUi(
                        visible = true,
                        existingRoomId = r.existingRoomId,
                        existingRoomName = roomName,
                        targetRoomId = rid
                    )
                }
                is AssignTicketResult.Error -> { }
            }
        }
    }

    fun dismissConflict() {
        _roomConflict.value = RoomConflictUi()
        _conflictTicketId = null
    }

    fun openExistingRoom() {
        val id = _roomConflict.value.existingRoomId ?: return
        _roomConflict.value = RoomConflictUi()
        _conflictTicketId = null
        _pendingNavigateToRoomId.value = id
    }

    fun moveToTargetRoom() {
        val c = _roomConflict.value
        val from = c.existingRoomId ?: return
        val to = c.targetRoomId ?: return
        val tid = _conflictTicketId ?: return
        viewModelScope.launch {
            when (RoomRepository.moveTicketToRoom(tid, from, to)) {
                is AssignTicketResult.Success -> {
                    _roomConflict.value = RoomConflictUi()
                    _conflictTicketId = null
                    refresh(tid)
                }
                is AssignTicketResult.AlreadyInRoom -> { }
                is AssignTicketResult.Error -> { }
            }
        }
    }

    fun clearPendingNavigate() {
        _pendingNavigateToRoomId.value = null
    }

    fun callNumber(number: Int, onResult: ((Boolean) -> Unit)? = null) {
        if (number !in 1..75) return
        val rid = currentRoomId.value ?: return
        viewModelScope.launch {
            val added = RoomRepository.addCalledNumberIfAllowed(rid, number)
            onResult?.invoke(added)
        }
    }

    fun callRandomNumber(onResult: ((Boolean) -> Unit)? = null) {
        val rid = currentRoomId.value ?: run { onResult?.invoke(false); return }
        viewModelScope.launch {
            val calledNumbers = RoomRepository.getCalledNumbersForRoom(rid)
            val pool = (1..75).filterNot { it in calledNumbers }
            if (pool.isEmpty()) {
                onResult?.invoke(false)
                return@launch
            }
            val picked = pool.random()
            val added = RoomRepository.addCalledNumberIfAllowed(rid, picked)
            onResult?.invoke(added)
        }
    }

    fun leaveRoom() {
        selectedTicketIdFlow.value = null
    }

    fun onResetClick() {
        if (state.value.calledNumbers.isNotEmpty()) {
            _showResetProtectionDialog.value = true
        } else {
            _showResetConfirm.value = true
        }
    }

    fun onResetConfirm() {
        val rid = currentRoomId.value ?: return
        viewModelScope.launch {
            RoomRepository.resetCalledNumbers(rid)
            RoomRepository.setRoomArchived(rid, false)
            _showResetConfirm.value = false
            _showResetProtectionDialog.value = false
        }
    }

    fun onResetDismiss() {
        _showResetConfirm.value = false
        _showResetProtectionDialog.value = false
    }

    fun onStartNewRoomFromReset() {
        viewModelScope.launch {
            _showResetProtectionDialog.value = false
            val roomCount = RoomRepository.getRooms().first().size
            val newId = RoomRepository.createRoom("Room ${roomCount + 1}")
            _pendingNavigateToRoomId.value = newId
        }
    }

    fun markRoomArchived() {
        val rid = currentRoomId.value ?: return
        viewModelScope.launch {
            RoomRepository.setRoomArchived(rid, true)
        }
    }

    private fun flowForOneTicket(ticketId: String): kotlinx.coroutines.flow.Flow<LiveSheetUi?> =
        combine(
            TicketRepository.observeTicket(ticketId),
            TicketRepository.ticketCellsFlow(ticketId)
        ) { ticket: TicketEntity?, cells: List<BingoCellUi> ->
            if (ticket == null) null
            else {
                val cells25 = if (cells.size == 25) cells
                else cells + List(25 - cells.size.coerceAtMost(25)) { BingoCellUi(null, false, false, false, false) }
                LiveSheetUi(
                    ticketId = ticketId,
                    title = ticket.sheetName.ifBlank { "Unnamed" },
                    playedAtMillis = ticket.playedAtMillis,
                    cells = cells25,
                    markedCount = 0,
                    losNumber = ticket.losNumber,
                    serialNumber = ticket.serialNumber
                )
            }
        }

    private fun buildStateFromSheets(
        baseSheets: List<LiveSheetUi>,
        rawCalledNumbers: List<Int>,
        lastCalledAtMillis: Long?,
        selectedId: String?
    ): LivePlayUiState {
        val calledNumbers = rawCalledNumbers.take(MAX_LIVE_CALLS)
        val displaySheets = baseSheets.map { sheet ->
            val markedCells = sheet.cells.map { cell ->
                val num = cell.number?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
                val shouldMark = num != null && num in 1..75 && calledNumbers.contains(num)
                cell.copy(isMarked = cell.isMarked || shouldMark)
            }
            sheet.copy(
                cells = markedCells,
                markedCount = markedCells.count { it.isMarked }
            )
        }
        val selectedIndex = if (selectedId != null && displaySheets.isNotEmpty()) {
            displaySheets.indexOfFirst { it.ticketId == selectedId }.coerceAtLeast(0)
        } else 0
        val primaryData = displaySheets.getOrNull(selectedIndex)
        val primaryCells = primaryData?.cells ?: BingoCellUi.placeholderCells25()
        val markedSet = primaryCells.take(25).mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet()
        val winResult = BingoWinChecker.check(markedSet)
        return LivePlayUiState(
            sheetName = primaryData?.title ?: "",
            playedAtMillis = primaryData?.playedAtMillis ?: System.currentTimeMillis(),
            cells = primaryCells,
            calledNumbers = calledNumbers,
            lastCalled = calledNumbers.lastOrNull(),
            lastCalledAtMillis = lastCalledAtMillis,
            sheets = displaySheets,
            selectedIndex = selectedIndex,
            winningCells = winResult.winningCells
        )
    }
}

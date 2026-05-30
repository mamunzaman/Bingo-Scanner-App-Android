package com.example.mamunbingoapp.viewmodel

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.AssignTicketResult
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketPlayLog
import com.example.mamunbingoapp.data.TicketCalledNumbersResolver
import com.example.mamunbingoapp.data.TicketPlayLogRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.core.MAX_CALLED_NUMBERS
import com.example.mamunbingoapp.core.RoomStatusResolver
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.model.RoomStatus
import com.example.mamunbingoapp.ui.model.SheetStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import android.util.Log
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryDetailAddToLivePlayHandoff(
    val sessionId: String,
    val roomId: String,
    val ticketId: String
)

private data class SessionWithAssignedRoom(
    val session: HistorySession?,
    val assignedRoomId: String?,
    val playLogs: List<TicketPlayLog> = emptyList(),
)

data class HistoryDetailUiState(
    val isLoading: Boolean = true,
    val actionLoading: Boolean = false,
    val session: HistorySession? = null,
    val ticketId: String? = null,
    val assignedRoomId: String? = null,
    val roomStatus: RoomStatus? = null,
    val sheetStatus: SheetStatus = SheetStatus.IDLE,
    val calledNumbers: List<Int> = emptyList(),
    val cells: List<BingoCellUi>? = null,
    val playLogs: List<TicketPlayLog> = emptyList(),
    val testDateMillis: Long? = null,
    val testDrawDateLabel: String? = null,
    val testDateInfoMessage: String? = null,
    val isTestDateLoading: Boolean = false,
    val errorMessage: String? = null,
    val showRoomConflictDialog: Boolean = false,
    val conflictExistingRoomId: String? = null,
    val conflictExistingRoomName: String? = null,
    val pendingTargetRoomId: String? = null,
    val pendingAddToLivePlay: HistoryDetailAddToLivePlayHandoff? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryDetailViewModel(
    application: Application,
    private val sessionId: String,
) : AndroidViewModel(application) {

    private val app get() = getApplication<Application>()

    private val _state = MutableStateFlow(HistoryDetailUiState())
    val state: StateFlow<HistoryDetailUiState> = _state.asStateFlow()
    val snackbarMessage = MutableSharedFlow<String>()

    init {
        HistoryRepository.observeSession(sessionId)
            .flatMapLatest { session ->
                val tid = session?.ticketId
                if (tid.isNullOrBlank()) {
                    flowOf(SessionWithAssignedRoom(session, null))
                } else {
                    combine(
                        RoomRepository.assignedRoomIdFlow(tid),
                        TicketPlayLogRepository.observeForTicket(tid),
                    ) { rid, playLogs ->
                        SessionWithAssignedRoom(session, rid, playLogs)
                    }
                }
            }
            .flatMapLatest { holder -> buildDetailFlow(holder) }
            .catch { e ->
                emit(
                    HistoryDetailUiState(
                        isLoading = false,
                        errorMessage = e.message ?: app.getString(R.string.history_detail_load_error),
                        roomStatus = null,
                        sheetStatus = SheetStatus.IDLE
                    )
                )
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    private fun buildDetailFlow(holder: SessionWithAssignedRoom) = run {
        val session = holder.session
        val assignedRoomId = holder.assignedRoomId
        val playLogs = holder.playLogs
        val archivedCalledNumbers = playLogs.maxByOrNull { it.archivedAt }?.calledNumbers.orEmpty()
        when {
            session == null -> flowOf(
                HistoryDetailUiState(
                    isLoading = false,
                    session = null,
                    ticketId = null,
                    assignedRoomId = null,
                    roomStatus = null,
                    sheetStatus = SheetStatus.IDLE,
                    calledNumbers = emptyList(),
                    cells = null,
                    playLogs = emptyList(),
                    errorMessage = app.getString(R.string.history_detail_session_not_found)
                )
            )
                    assignedRoomId == null -> combine(
                        TicketRepository.ticketCellsFlow(session.ticketId),
                        HistoryRepository.observeHistoryTestDate(sessionId),
                    ) { cells, testDateMillis ->
                        cells to testDateMillis
                    }.flatMapLatest { (cells, testDateMillis) ->
                        flow {
                            val archivedCalledNumbers = playLogs.maxByOrNull { it.archivedAt }?.calledNumbers.orEmpty()
                            if (testDateMillis != null) {
                                emit(
                                    buildOfflineDetailState(
                                        session = session,
                                        playLogs = playLogs,
                                        cells = cells,
                                        calledNumbers = archivedCalledNumbers.ifEmpty { session.calledNumbersFull },
                                        testDateMillis = testDateMillis,
                                        testDrawDateLabel = null,
                                        testDateInfoMessage = null,
                                        isTestDateLoading = true,
                                    )
                                )
                                val resolved = TicketCalledNumbersResolver.forOfflineTicket(
                                    testDateMillis = testDateMillis,
                                    archivedNumbers = archivedCalledNumbers.ifEmpty { session.calledNumbersFull },
                                )
                                val infoMessage = when (resolved.testDateError) {
                                    TicketCalledNumbersResolver.TestDateError.NO_DRAW ->
                                        app.getString(R.string.history_detail_test_date_no_draw)
                                    TicketCalledNumbersResolver.TestDateError.LOAD_ERROR ->
                                        app.getString(R.string.history_detail_test_date_load_error)
                                    null -> null
                                }
                                emit(
                                    buildOfflineDetailState(
                                        session = session,
                                        playLogs = playLogs,
                                        cells = cells,
                                        calledNumbers = resolved.calledNumbers,
                                        testDateMillis = testDateMillis,
                                        testDrawDateLabel = resolved.drawDateLabel,
                                        testDateInfoMessage = infoMessage,
                                        isTestDateLoading = false,
                                    )
                                )
                            } else {
                                emit(
                                    buildOfflineDetailState(
                                        session = session,
                                        playLogs = playLogs,
                                        cells = cells,
                                        calledNumbers = archivedCalledNumbers.ifEmpty { session.calledNumbersFull },
                                        testDateMillis = null,
                                        testDrawDateLabel = null,
                                        testDateInfoMessage = null,
                                        isTestDateLoading = false,
                                    )
                                )
                            }
                        }
                    }
            else -> combine(
                RoomRepository.calledNumbersFlow(assignedRoomId),
                TicketRepository.ticketCellsFlow(session.ticketId)
            ) { called: List<Int>, baseCells: List<BingoCellUi> ->
                val roomStatus = RoomStatusResolver.resolve(called.size)
                val calledSet = called.toSet()
                val merged = mergeCellsWithCalledNumbers(baseCells, calledSet)
                val sheetStatus = when (roomStatus) {
                    RoomStatus.RUNNING -> SheetStatus.ACTIVE
                    RoomStatus.IDLE -> SheetStatus.COMPLETED
                    RoomStatus.FINISHED -> SheetStatus.COMPLETED
                }
                HistoryDetailUiState(
                    isLoading = false,
                    session = session,
                    ticketId = session.ticketId,
                    assignedRoomId = assignedRoomId,
                    roomStatus = roomStatus,
                    sheetStatus = sheetStatus,
                    calledNumbers = called,
                    cells = merged.takeIf { it.size == MAX_CALLED_NUMBERS },
                    playLogs = playLogs,
                    errorMessage = null
                )
            }
        }
    }

    private fun buildOfflineDetailState(
        session: HistorySession,
        playLogs: List<TicketPlayLog>,
        cells: List<BingoCellUi>,
        calledNumbers: List<Int>,
        testDateMillis: Long?,
        testDrawDateLabel: String?,
        testDateInfoMessage: String?,
        isTestDateLoading: Boolean,
    ): HistoryDetailUiState {
        val calledCount = calledNumbers.size
        val calledSet = calledNumbers.toSet()
        val merged = mergeCellsWithCalledNumbers(cells, calledSet)
        val sheetStatus = when {
            calledCount == 0 -> SheetStatus.IDLE
            calledCount >= MAX_CALLED_NUMBERS -> SheetStatus.COMPLETED
            else -> SheetStatus.IN_PROGRESS
        }
        return HistoryDetailUiState(
            isLoading = false,
            session = session,
            ticketId = session.ticketId,
            assignedRoomId = null,
            roomStatus = null,
            sheetStatus = sheetStatus,
            calledNumbers = calledNumbers,
            cells = merged.takeIf { it.size == MAX_CALLED_NUMBERS },
            playLogs = playLogs,
            testDateMillis = testDateMillis,
            testDrawDateLabel = testDrawDateLabel,
            testDateInfoMessage = testDateInfoMessage,
            isTestDateLoading = isTestDateLoading,
            errorMessage = null,
        )
    }

    private fun mergeCellsWithCalledNumbers(
        cells: List<BingoCellUi>,
        calledSet: Set<Int>,
    ): List<BingoCellUi> = cells.map { cell ->
        val num = cell.number?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
        cell.copy(isMarked = (num != null && num in calledSet) || cell.isMarked)
    }

    fun setTestDate(dateMillis: Long) {
        viewModelScope.launch {
            HistoryRepository.updateHistorySessionTestDate(sessionId, dateMillis)
        }
    }

    fun clearTestDate() {
        viewModelScope.launch {
            HistoryRepository.clearHistorySessionTestDate(sessionId)
        }
    }

    fun addToLivePlay(roomId: String) = addToRoom(roomId)

    fun addToRoom(roomId: String) {
        val tid = _state.value.ticketId
        if (tid.isNullOrBlank()) {
            Log.e("HistoryDetail", "addToRoom: ticketId is null or blank")
            throw IllegalStateException("ticketId must be resolved from session before assigning to room")
        }
        viewModelScope.launch {
            _state.update { it.copy(actionLoading = true) }
            try {
                when (val result = RoomRepository.assignTicketToRoom(roomId, tid)) {
                    is AssignTicketResult.Success -> {
                        Log.d("HistoryDetail", "Assigning ticketId=$tid to roomId=$roomId")
                        _state.update {
                            it.copy(
                                pendingAddToLivePlay = HistoryDetailAddToLivePlayHandoff(
                                    sessionId = sessionId,
                                    roomId = roomId,
                                    ticketId = tid
                                )
                            )
                        }
                        snackbarMessage.emit(app.getString(R.string.ticket_detail_snackbar_added))
                    }
                    is AssignTicketResult.AlreadyInRoom -> {
                        val roomName = RoomRepository.getRoom(result.existingRoomId)?.name
                            ?: app.getString(R.string.history_detail_another_room_fallback)
                        _state.update {
                            it.copy(
                                showRoomConflictDialog = true,
                                conflictExistingRoomId = result.existingRoomId,
                                conflictExistingRoomName = roomName,
                                pendingTargetRoomId = roomId
                            )
                        }
                    }
                    is AssignTicketResult.Error -> {
                        _state.update { it.copy(errorMessage = result.message) }
                        snackbarMessage.emit(app.getString(R.string.ticket_detail_snackbar_add_failed))
                    }
                }
            } finally {
                _state.update { it.copy(actionLoading = false) }
            }
        }
    }

    fun resolveMoveConflict() {
        val from = _state.value.conflictExistingRoomId ?: return
        val to = _state.value.pendingTargetRoomId ?: return
        val tid = _state.value.ticketId ?: return
        viewModelScope.launch {
            _state.update { it.copy(actionLoading = true) }
            try {
                when (RoomRepository.moveTicketToRoom(tid, from, to)) {
                    is AssignTicketResult.Success -> {
                        _state.update {
                            it.copy(
                                showRoomConflictDialog = false,
                                conflictExistingRoomId = null,
                                conflictExistingRoomName = null,
                                pendingTargetRoomId = null,
                                pendingAddToLivePlay = HistoryDetailAddToLivePlayHandoff(
                                    sessionId = sessionId,
                                    roomId = to,
                                    ticketId = tid
                                )
                            )
                        }
                        snackbarMessage.emit(app.getString(R.string.ticket_detail_snackbar_moved))
                    }
                    is AssignTicketResult.AlreadyInRoom -> { }
                    is AssignTicketResult.Error -> {
                        _state.update { it.copy(errorMessage = (it.errorMessage ?: "") + " Move failed.") }
                        snackbarMessage.emit(app.getString(R.string.ticket_detail_snackbar_move_failed))
                    }
                }
            } finally {
                _state.update { it.copy(actionLoading = false) }
            }
        }
    }

    fun dismissConflictDialog() {
        _state.update {
            it.copy(
                showRoomConflictDialog = false,
                conflictExistingRoomId = null,
                conflictExistingRoomName = null,
                pendingTargetRoomId = null
            )
        }
    }

    fun clearPendingNavigate() {
        _state.update { it.copy(pendingAddToLivePlay = null) }
    }

    suspend fun performSoftDelete() = HistoryRepository.softDeleteSessionSync(sessionId)

    fun restoreSession() = viewModelScope.launch {
        HistoryRepository.restoreSession(sessionId)
    }
}

class HistoryDetailViewModelFactory(
    private val sessionId: String,
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        HistoryDetailViewModel(application, sessionId) as T
}

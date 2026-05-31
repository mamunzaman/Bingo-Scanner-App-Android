package com.example.mamunbingoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.AssignTicketResult
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketDetailData
import com.example.mamunbingoapp.data.TicketPlayLogRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.ui.components.RoomConflictUi
import com.example.mamunbingoapp.ui.model.BingoCellUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TicketDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val ticketId: String = savedStateHandle.get<String>("ticketId") ?: ""

    private val _state = MutableStateFlow<TicketDetailData?>(null)
    val state: StateFlow<TicketDetailData?> = _state.asStateFlow()

    init {
        if (ticketId.isNotBlank()) {
            combine(
                TicketRepository.observeTicket(ticketId),
                TicketRepository.ticketCellsFlow(ticketId),
                RoomRepository.assignedRoomIdFlow(ticketId),
                TicketPlayLogRepository.observeForTicket(ticketId),
            ) { ticket, cells, assignedRoomId, playLogs ->
                TicketDetailSnapshot(ticket, cells, assignedRoomId, playLogs)
            }.flatMapLatest { snapshot ->
                val ticket = snapshot.ticket
                if (ticket == null) {
                    flowOf(null)
                } else if (snapshot.assignedRoomId != null) {
                    RoomRepository.calledNumbersFlow(snapshot.assignedRoomId).map { called ->
                        snapshot.toDetailData(called)
                    }
                } else {
                    val archivedCalled = snapshot.playLogs.maxByOrNull { it.archivedAt }?.calledNumbers.orEmpty()
                    flowOf(snapshot.toDetailData(archivedCalled))
                }
            }.onEach { _state.value = it }
                .launchIn(viewModelScope)
        }
    }

    private val _roomConflict = MutableStateFlow(RoomConflictUi())
    val roomConflict: StateFlow<RoomConflictUi> = _roomConflict.asStateFlow()

    private val _pendingNavigateToRoomId = MutableStateFlow<String?>(null)
    val pendingNavigateToRoomId: StateFlow<String?> = _pendingNavigateToRoomId.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private fun anotherRoomFallback(): String =
        getApplication<Application>().getString(R.string.history_detail_another_room_fallback)

    fun addToRoom(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val r = RoomRepository.assignTicketToRoom(roomId, ticketId)) {
                    is AssignTicketResult.Success -> {
                        _snackbarMessage.emit(
                            getApplication<Application>().getString(R.string.ticket_detail_snackbar_added)
                        )
                        _pendingNavigateToRoomId.value = roomId
                    }
                    is AssignTicketResult.AlreadyInRoom -> {
                        val roomName = RoomRepository.getRoom(r.existingRoomId)?.name ?: anotherRoomFallback()
                        _roomConflict.value = RoomConflictUi(
                            visible = true,
                            existingRoomId = r.existingRoomId,
                            existingRoomName = roomName,
                            targetRoomId = roomId
                        )
                    }
                    is AssignTicketResult.Error -> _snackbarMessage.emit(
                        getApplication<Application>().getString(R.string.ticket_detail_snackbar_add_failed)
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissConflict() {
        _roomConflict.value = RoomConflictUi()
    }

    fun openExistingRoom() {
        val id = _roomConflict.value.existingRoomId ?: return
        _roomConflict.value = RoomConflictUi()
        _pendingNavigateToRoomId.value = id
    }

    fun moveToTargetRoom() {
        val c = _roomConflict.value
        val from = c.existingRoomId ?: return
        val to = c.targetRoomId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val moveResult = RoomRepository.moveTicketToRoom(ticketId, from, to)) {
                    is AssignTicketResult.Success -> {
                        _roomConflict.value = RoomConflictUi()
                        _snackbarMessage.emit(
                            getApplication<Application>().getString(R.string.ticket_detail_snackbar_moved)
                        )
                        _pendingNavigateToRoomId.value = to
                    }
                    is AssignTicketResult.AlreadyInRoom -> { }
                    is AssignTicketResult.Error -> _snackbarMessage.emit(
                        getApplication<Application>().getString(R.string.ticket_detail_snackbar_move_failed)
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPendingNavigate() {
        _pendingNavigateToRoomId.value = null
    }

    private data class TicketDetailSnapshot(
        val ticket: com.example.mamunbingoapp.data.db.TicketEntity?,
        val cells: List<BingoCellUi>,
        val assignedRoomId: String?,
        val playLogs: List<com.example.mamunbingoapp.data.TicketPlayLog>,
    ) {
        fun toDetailData(calledNumbers: List<Int>): TicketDetailData? {
            val entity = ticket ?: return null
            val calledSet = calledNumbers.toSet()
            val mergedCells = cells.map { cell ->
                val num = cell.number?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
                cell.copy(isMarked = (num != null && num in calledSet) || cell.isMarked)
            }
            return TicketDetailData(
                sheetName = entity.sheetName,
                playedAtMillis = entity.playedAtMillis,
                cells = mergedCells,
                calledNumbers = calledNumbers,
                losNumber = entity.losNumber,
                serialNumber = entity.serialNumber,
                playLogs = playLogs,
            )
        }
    }
}

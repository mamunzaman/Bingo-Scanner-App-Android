package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.AssignTicketResult
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketDetailData
import com.example.mamunbingoapp.ui.components.RoomConflictUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val ticketId: String = savedStateHandle.get<String>("ticketId") ?: ""

    private val _state = MutableStateFlow<TicketDetailData?>(null)
    val state: StateFlow<TicketDetailData?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = HistoryRepository.getTicketData(ticketId)
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

    fun addToRoom(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val r = RoomRepository.assignTicketToRoom(roomId, ticketId)) {
                    is AssignTicketResult.Success -> {
                        _snackbarMessage.emit("Ticket added to room")
                        _pendingNavigateToRoomId.value = roomId
                    }
                    is AssignTicketResult.AlreadyInRoom -> {
                        val roomName = RoomRepository.getRoom(r.existingRoomId)?.name ?: "another room"
                        _roomConflict.value = RoomConflictUi(
                            visible = true,
                            existingRoomId = r.existingRoomId,
                            existingRoomName = roomName,
                            targetRoomId = roomId
                        )
                    }
                    is AssignTicketResult.Error -> _snackbarMessage.emit("Could not add ticket")
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
                        _snackbarMessage.emit("Ticket moved to room")
                        _pendingNavigateToRoomId.value = to
                    }
                    is AssignTicketResult.AlreadyInRoom -> { }
                    is AssignTicketResult.Error -> _snackbarMessage.emit("Could not move ticket")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPendingNavigate() {
        _pendingNavigateToRoomId.value = null
    }
}

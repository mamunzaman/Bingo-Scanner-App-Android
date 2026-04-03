package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.LiveRoom
import com.example.mamunbingoapp.data.RoomRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoomWithStats(val room: LiveRoom, val ticketCount: Int, val calledCount: Int)

class LiveRoomsViewModel : ViewModel() {
    val rooms: StateFlow<List<LiveRoom>> = RoomRepository.getRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _roomsWithStats = MutableStateFlow<List<RoomWithStats>>(emptyList())
    val roomsWithStats: StateFlow<List<RoomWithStats>> = _roomsWithStats.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: kotlinx.coroutines.flow.SharedFlow<String> = _snackbarMessage

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _lastCreatedRoomId = MutableStateFlow<String?>(null)
    val lastCreatedRoomId: StateFlow<String?> = _lastCreatedRoomId.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                RoomRepository.getRooms(),
                RoomRepository.roomTicketCountsFlow(),
                RoomRepository.allRoomsCalledCountsFlow()
            ) { roomList, ticketCountsByRoom, calledCountsByRoom ->
                roomList.map { r ->
                    RoomWithStats(
                        r,
                        ticketCount = ticketCountsByRoom[r.roomId] ?: 0,
                        calledCount = calledCountsByRoom[r.roomId] ?: 0
                    )
                }
            }.collect { _roomsWithStats.value = it }
        }
    }

    fun createRoom(name: String) {
        viewModelScope.launch {
            _isCreating.value = true
            try {
                val id = RoomRepository.createRoom(name)
                _snackbarMessage.emit("Room created")
                _lastCreatedRoomId.value = id
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun clearLastCreatedRoomId() {
        _lastCreatedRoomId.value = null
    }
}

package com.example.mamunbingoapp.ui.model

data class TicketUiModel(
    val id: String,
    val sessionId: String,
    val title: String,
    val createdAt: Long,
    val status: String? = null,
    val isInRoom: Boolean = false,
    val assignedRoomId: String? = null
)

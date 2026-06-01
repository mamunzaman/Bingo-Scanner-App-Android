package com.example.mamunbingoapp.ui.model

/** One cell for My Tickets / picker list B–O preview (same role as history mini grid). */
data class TicketPickerMiniGridCell(
    val display: String,
    val isMarked: Boolean,
)

data class TicketUiModel(
    val id: String,
    val sessionId: String,
    val title: String,
    val createdAt: Long,
    val status: String? = null,
    val serialNumber: String? = null,
    val losNumber: String? = null,
    val isInRoom: Boolean = false,
    val assignedRoomId: String? = null,
    val miniGridCells: List<TicketPickerMiniGridCell> = emptyList(),
)

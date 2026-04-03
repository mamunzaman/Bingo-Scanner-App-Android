package com.example.mamunbingoapp.data.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "ticket_cells",
    primaryKeys = ["ticketId", "cellIndex"],
    indices = [Index("ticketId")]
)
data class TicketCellEntity(
    val ticketId: String,
    val cellIndex: Int,
    val value: String?,
    val isMarked: Boolean
)

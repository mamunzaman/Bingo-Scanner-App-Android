package com.example.mamunbingoapp.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ticket_play_logs",
    indices = [Index("ticketId"), Index("roomId"), Index("archivedAt")],
)
data class TicketPlayLogEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val roomId: String,
    val roomName: String,
    val addedAt: Long,
    val archivedAt: Long,
    val drawDate: String? = null,
    val calledNumbersSnapshot: String,
    val markedCount: Int = 0,
    val bingoLineCount: Int = 0,
)

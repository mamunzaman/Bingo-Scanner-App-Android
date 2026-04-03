package com.example.mamunbingoapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketId: String,
    val sheetName: String,
    val playedAtMillis: Long,
    val createdAt: Long,
    val source: String = "manual",
    val isDeleted: Boolean = false,
    val ocrConfidence: Float? = null,
    val originalOcrNumbers: String? = null,
    val losNumber: String? = null,
    val serialNumber: String? = null,
)

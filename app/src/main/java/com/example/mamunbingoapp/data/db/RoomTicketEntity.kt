package com.example.mamunbingoapp.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "room_tickets", primaryKeys = ["roomId", "ticketId"], indices = [Index("roomId"), Index("ticketId")])
data class RoomTicketEntity(
    val roomId: String,
    val ticketId: String,
    val addedAt: Long = System.currentTimeMillis()
)

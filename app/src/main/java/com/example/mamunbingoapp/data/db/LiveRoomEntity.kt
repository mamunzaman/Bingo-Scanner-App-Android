package com.example.mamunbingoapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "live_rooms")
data class LiveRoomEntity(
    @PrimaryKey val roomId: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt
)

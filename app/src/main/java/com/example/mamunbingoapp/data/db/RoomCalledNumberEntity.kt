package com.example.mamunbingoapp.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "room_called_numbers", primaryKeys = ["roomId", "number"], indices = [Index("roomId")])
data class RoomCalledNumberEntity(
    val roomId: String,
    val number: Int,
    val calledAt: Long = System.currentTimeMillis()
)

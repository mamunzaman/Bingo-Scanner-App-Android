package com.example.mamunbingoapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_settings")
data class RoomSettingsEntity(
    @PrimaryKey val roomId: String,
    val autoCallEnabled: Boolean = false,
    val intervalSeconds: Int = 5,
    val lastStartedAt: Long? = null,
    val isRunning: Boolean = false,
    val isArchived: Boolean = false
)

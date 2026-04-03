package com.example.mamunbingoapp.data

data class LiveRoom(
    val roomId: String,
    val name: String,
    val createdAt: Long,
    val isActive: Boolean = true
)

package com.example.mamunbingoapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QrTicketPayload(
    val v: Int = 1,
    val type: String = "bingo_ticket",
    val grid: List<List<String>>,
    val sheetName: String = "",
    val serial: String? = null,
    val los: String? = null,
)

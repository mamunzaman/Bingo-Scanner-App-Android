package com.example.mamunbingoapp.data.model.live

data class LiveScanResult(
    val detectedNumbersCount: Int,
    val flatNumbers: List<Int> = emptyList(),
    val gridRows: List<List<Int>>? = null,
    val confidence: Float? = null,
    val isCompleteGrid: Boolean = false
)

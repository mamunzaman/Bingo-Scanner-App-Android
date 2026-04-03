package com.example.mamunbingoapp.scanner

data class BingoTicketScanResult(
    val serialNumber: String? = null,
    val gridNumbers: List<Int> = emptyList(),
    val isValid: Boolean = false,
    val errorMessage: String? = null
)

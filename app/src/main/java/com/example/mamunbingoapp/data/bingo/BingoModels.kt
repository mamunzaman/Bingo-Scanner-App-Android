package com.example.mamunbingoapp.data.bingo

import java.time.Instant
import java.time.LocalDate

data class BingoStatus(
    val jackpotEur: Double?,
    val nextDrawAt: Instant?,
    val latestDrawDate: LocalDate?,
    val checkedAt: Instant?,
)

data class BingoDrawResult(
    val drawDate: LocalDate,
    val winningNumbers: List<Int>,
    val sourceUpdatedAt: Instant?,
    val fetchedAt: Instant?,
)

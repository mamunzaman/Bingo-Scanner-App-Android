package com.example.mamunbingoapp.data.bingo

import com.example.mamunbingoapp.data.remote.BingoDrawResultDto
import com.example.mamunbingoapp.data.remote.BingoStatusDto
import java.time.LocalDate

object BingoDrawValidator {
    const val REQUIRED_NUMBER_COUNT = 22
    const val MIN_NUMBER = 1
    const val MAX_NUMBER = 75

    fun validateJackpot(amount: Double?): Double? {
        if (amount == null) return null
        if (!amount.isFinite() || amount <= 0.0) return null
        return amount
    }

    fun validateStatus(dto: BingoStatusDto): BingoStatus? {
        val jackpot = validateJackpot(dto.currentJackpotEur)
        val nextDrawAt = BingoBerlinDates.parseInstant(dto.nextDrawAt)
        val latestDrawDate = BingoBerlinDates.parseIsoDate(dto.latestDrawDate)
            ?.takeIf { BingoBerlinDates.isSunday(it) }
        val checkedAt = BingoBerlinDates.parseInstant(dto.checkedAt)
        if (jackpot == null && nextDrawAt == null && latestDrawDate == null && checkedAt == null) {
            return null
        }
        return BingoStatus(
            jackpotEur = jackpot,
            nextDrawAt = nextDrawAt,
            latestDrawDate = latestDrawDate,
            checkedAt = checkedAt,
        )
    }

    fun validateDraw(requestedDate: LocalDate, dto: BingoDrawResultDto): BingoDrawResult? {
        val responseDate = BingoBerlinDates.parseIsoDate(dto.drawDate) ?: return null
        if (responseDate != requestedDate) return null
        if (!BingoBerlinDates.isSunday(responseDate)) return null
        val numbers = dto.winningNumbers
        if (numbers.size != REQUIRED_NUMBER_COUNT) return null
        if (numbers.any { it !in MIN_NUMBER..MAX_NUMBER }) return null
        if (numbers.toSet().size != REQUIRED_NUMBER_COUNT) return null
        return BingoDrawResult(
            drawDate = responseDate,
            winningNumbers = numbers.toList(),
            sourceUpdatedAt = BingoBerlinDates.parseInstant(dto.sourceUpdatedAt),
            fetchedAt = BingoBerlinDates.parseInstant(dto.fetchedAt),
        )
    }
}

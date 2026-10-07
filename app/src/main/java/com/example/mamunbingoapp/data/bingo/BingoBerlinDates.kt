package com.example.mamunbingoapp.data.bingo

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object BingoBerlinDates {
    val berlinZone: ZoneId = ZoneId.of("Europe/Berlin")
    private val isoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun sundayContaining(instant: Instant): LocalDate =
        instant.atZone(berlinZone).toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    fun sundayContainingMillis(dateMillis: Long): LocalDate =
        sundayContaining(Instant.ofEpochMilli(dateMillis))

    fun parseIsoDate(raw: String?): LocalDate? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return null
        return runCatching { LocalDate.parse(text, isoDate) }.getOrNull()
    }

    fun formatIsoDate(date: LocalDate): String = date.format(isoDate)

    fun isSunday(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.SUNDAY

    fun parseInstant(raw: String?): Instant? {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return null
        return runCatching { Instant.parse(text) }.getOrNull()
    }

    fun fallbackNextDrawAt(from: ZonedDateTime = ZonedDateTime.now(berlinZone)): Instant {
        var target = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .withHour(17)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        if (!target.isAfter(from)) {
            target = target.plusWeeks(1)
        }
        return target.toInstant()
    }
}

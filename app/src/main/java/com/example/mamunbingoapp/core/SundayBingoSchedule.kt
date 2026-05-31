package com.example.mamunbingoapp.core

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object SundayBingoSchedule {
    val berlinZone: ZoneId = ZoneId.of("Europe/Berlin")

    private val knownFeaturedRoomNames = setOf(
        "Sunday 17:00 Bingo",
        "Sonntag 17:00 Bingo",
    )

    fun isSundayFeaturedRoom(roomName: String, featuredTitle: String? = null): Boolean {
        if (!featuredTitle.isNullOrBlank() && roomName.equals(featuredTitle, ignoreCase = true)) {
            return true
        }
        return knownFeaturedRoomNames.any { roomName.equals(it, ignoreCase = true) }
    }

    fun activeSessionStart(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
    ): ZonedDateTime? {
        val berlinNow = now.withZoneSameInstant(berlinZone)
        if (!test.enabled) return productionActiveSessionStart(berlinNow)
        return testActiveSessionStart(berlinNow, test)
    }

    fun isLiveCallingUnlocked(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
    ): Boolean = activeSessionStart(now, test) != null

    /** Exclusive end of the active session window (Berlin); null when not in a live window. */
    fun activeSessionEndExclusive(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
    ): ZonedDateTime? {
        val sessionStart = activeSessionStart(now, test) ?: return null
        if (!test.enabled) {
            return sessionAt(sessionStart.toLocalDate(), SundayTestTimeSettings.PRODUCTION_END)
        }
        return test.testSessionEndExclusive()
    }

    /**
     * Hero "Live will end in" target. Production uses [activeSessionEndExclusive] only.
     * When [useDemoFallback] and live without a schedule end: now + test live duration (2 min) or +2 min.
     */
    fun heroSessionEndExclusive(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
        useDemoFallback: Boolean = false,
    ): ZonedDateTime? {
        if (activeSessionStart(now, test) == null) return null
        activeSessionEndExclusive(now, test)?.let { return it }
        if (!useDemoFallback) return null
        val berlinNow = now.withZoneSameInstant(berlinZone)
        val durationMinutes = if (test.enabled) {
            SundayTestTimeSettings.TEST_SESSION_DURATION_MINUTES
        } else {
            2
        }
        return berlinNow.plusMinutes(durationMinutes.toLong())
    }

    fun shouldResetStaleSundayCalls(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
    ): Boolean = activeSessionStart(now, test) == null

    fun lastCompletedSundaySessionStart(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
    ): ZonedDateTime? {
        val berlinNow = now.withZoneSameInstant(berlinZone)
        if (!test.enabled) return productionLastCompletedSessionStart(berlinNow)
        return testLastCompletedSessionStart(berlinNow, test)
    }

    fun nextSessionStartBerlin(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
    ): ZonedDateTime {
        val berlinNow = now.withZoneSameInstant(berlinZone)
        if (!test.enabled) return productionNextSessionStartBerlin(berlinNow)
        val sessionStart = test.testSessionStart() ?: return productionNextSessionStartBerlin(berlinNow)
        val sessionEnd = sessionStart.plusMinutes(SundayTestTimeSettings.TEST_SESSION_DURATION_MINUTES.toLong())
        if (!berlinNow.isBefore(sessionEnd)) return productionNextSessionStartBerlin(berlinNow)
        if (berlinNow.isBefore(sessionStart)) return sessionStart
        return sessionStart
    }

    fun isAfterLastCompletedSundaySessionEnd(
        now: ZonedDateTime = ZonedDateTime.now(berlinZone),
        test: SundayTestTimeSettings = SundayTestTimeSettings(),
    ): Boolean = lastCompletedSundaySessionStart(now, test) != null

    fun formatArchivedSessionDisplayName(sessionStart: ZonedDateTime): String {
        val berlin = sessionStart.withZoneSameInstant(berlinZone)
        val dateLabel = berlin.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY))
        val timeLabel = berlin.format(DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY))
        return "Sonntag Bingo • $dateLabel • $timeLabel Uhr"
    }

    private fun productionNextSessionStartBerlin(berlinNow: ZonedDateTime): ZonedDateTime {
        val sunday = berlinNow.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate()
        var target = sessionAt(sunday, SundayTestTimeSettings.PRODUCTION_START)
        if (!target.isAfter(berlinNow)) {
            target = sessionAt(sunday.plusWeeks(1), SundayTestTimeSettings.PRODUCTION_START)
        }
        return target
    }

    private fun productionActiveSessionStart(berlinNow: ZonedDateTime): ZonedDateTime? {
        if (berlinNow.dayOfWeek != DayOfWeek.SUNDAY) return null
        val sessionStart = sessionAt(berlinNow.toLocalDate(), SundayTestTimeSettings.PRODUCTION_START)
        val sessionEnd = sessionAt(berlinNow.toLocalDate(), SundayTestTimeSettings.PRODUCTION_END)
        return if (isWithinWindow(berlinNow, sessionStart, sessionEnd)) sessionStart else null
    }

    private fun productionLastCompletedSessionStart(berlinNow: ZonedDateTime): ZonedDateTime? {
        val thisSunday = berlinNow.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).toLocalDate()
        val thisStart = sessionAt(thisSunday, SundayTestTimeSettings.PRODUCTION_START)
        val thisEnd = sessionAt(thisSunday, SundayTestTimeSettings.PRODUCTION_END)
        return if (!berlinNow.isBefore(thisEnd)) {
            thisStart
        } else {
            val prevSunday = thisSunday.minusWeeks(1)
            val prevEnd = sessionAt(prevSunday, SundayTestTimeSettings.PRODUCTION_END)
            if (!berlinNow.isBefore(prevEnd)) sessionAt(prevSunday, SundayTestTimeSettings.PRODUCTION_START) else null
        }
    }

    private fun testActiveSessionStart(
        berlinNow: ZonedDateTime,
        test: SundayTestTimeSettings,
    ): ZonedDateTime? {
        val sessionStart = test.testSessionStart() ?: return null
        val sessionEnd = test.testSessionEndExclusive() ?: return null
        return if (isWithinWindow(berlinNow, sessionStart, sessionEnd)) sessionStart else null
    }

    private fun testLastCompletedSessionStart(
        berlinNow: ZonedDateTime,
        test: SundayTestTimeSettings,
    ): ZonedDateTime? {
        val sessionStart = test.testSessionStart() ?: return null
        val sessionEnd = test.testSessionEndExclusive() ?: return null
        return if (!berlinNow.isBefore(sessionEnd)) sessionStart else null
    }

    private fun sessionAt(date: LocalDate, time: LocalTime): ZonedDateTime =
        ZonedDateTime.of(date, time, berlinZone)

    private fun isWithinWindow(
        now: ZonedDateTime,
        start: ZonedDateTime,
        end: ZonedDateTime,
    ): Boolean = !now.isBefore(start) && now.isBefore(end)
}

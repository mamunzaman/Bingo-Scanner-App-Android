package com.example.mamunbingoapp.core

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

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

    /** Sunday 17:00 Berlin start of the current 24h live window, or null if outside that window. */
    fun activeSessionStart(now: ZonedDateTime = ZonedDateTime.now(berlinZone)): ZonedDateTime? {
        val sessionStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            .withHour(17)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        if (now.isBefore(sessionStart)) return null
        val resetAt = sessionStart.plusHours(24)
        return if (now.isBefore(resetAt)) sessionStart else null
    }

    fun isLiveCallingUnlocked(now: ZonedDateTime = ZonedDateTime.now(berlinZone)): Boolean =
        activeSessionStart(now) != null

    fun shouldResetStaleSundayCalls(now: ZonedDateTime = ZonedDateTime.now(berlinZone)): Boolean =
        activeSessionStart(now) == null
}

package com.example.mamunbingoapp.core

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** Dev/test only. Production window stays Sun 17:00–18:05 Berlin. */
data class SundayTestTimeSettings(
    val enabled: Boolean = false,
    val startInMinutes: Int = DEFAULT_START_IN_MINUTES,
    /** Set when test mode is enabled or start-in changes; drives a fixed 2-minute live window. */
    val scheduledStartEpochMillis: Long? = null,
) {
    fun testSessionStart(zone: ZoneId = SundayBingoSchedule.berlinZone): ZonedDateTime? =
        scheduledStartEpochMillis?.let { Instant.ofEpochMilli(it).atZone(zone) }

    fun testSessionEndExclusive(zone: ZoneId = SundayBingoSchedule.berlinZone): ZonedDateTime? =
        testSessionStart(zone)?.plusMinutes(TEST_SESSION_DURATION_MINUTES.toLong())

    companion object {
        const val TEST_SESSION_DURATION_MINUTES = 2
        const val DEFAULT_START_IN_MINUTES = 3
        const val MIN_START_IN_MINUTES = 1
        const val MAX_START_IN_MINUTES = 10
        val PRODUCTION_START: LocalTime = LocalTime.of(17, 0)
        val PRODUCTION_END: LocalTime = LocalTime.of(18, 5)

        fun coerceStartInMinutes(value: Int): Int =
            value.coerceIn(MIN_START_IN_MINUTES, MAX_START_IN_MINUTES)

        fun scheduleStartFromNow(
            startInMinutes: Int,
            now: ZonedDateTime = ZonedDateTime.now(SundayBingoSchedule.berlinZone),
        ): Long =
            now.plusMinutes(coerceStartInMinutes(startInMinutes).toLong()).toInstant().toEpochMilli()
    }
}

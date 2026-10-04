package com.example.mamunbingoapp.core

import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SundayBingoScheduleTest {
    private val zone = SundayBingoSchedule.berlinZone

    @Test
    fun sundayBeforeStart_countsToSameSundayAt1700() {
        val state = stateAt("2026-09-27T16:59:59")

        assertTrue(state is SundayBingoSchedule.RoomState.StartsIn)
        assertEquals("2026-09-27T17:00+02:00[Europe/Berlin]", state.target.toString())
        assertEquals(1, Duration.between(state.now, state.target).seconds)
    }

    @Test
    fun sundayAt1700_isLiveAndClosesAt1800() {
        val state = stateAt("2026-09-27T17:00:00")

        assertTrue(state is SundayBingoSchedule.RoomState.LiveNow)
        assertEquals("2026-09-27T18:00+02:00[Europe/Berlin]", state.target.toString())
        assertEquals(3_600, Duration.between(state.now, state.target).seconds)
    }

    @Test
    fun sundayAt175959_isStillLive() {
        val state = stateAt("2026-09-27T17:59:59")

        assertTrue(state is SundayBingoSchedule.RoomState.LiveNow)
        assertEquals(1, Duration.between(state.now, state.target).seconds)
    }

    @Test
    fun sundayAt1800_isClosedAndTargetsFollowingSunday() {
        val state = stateAt("2026-09-27T18:00:00")

        assertTrue(state is SundayBingoSchedule.RoomState.StartsIn)
        assertEquals("2026-10-04T17:00+02:00[Europe/Berlin]", state.target.toString())
    }

    @Test
    fun mondayAfterGame_targetsFollowingSunday() {
        val state = stateAt("2026-09-28T09:00:00")

        assertTrue(state is SundayBingoSchedule.RoomState.StartsIn)
        assertEquals("2026-10-04T17:00+02:00[Europe/Berlin]", state.target.toString())
    }

    @Test
    fun summerAndWinterSessionsUseBerlinOffsets() {
        val summer = stateAt("2026-07-12T17:30:00")
        val winter = stateAt("2026-01-11T17:30:00")

        assertTrue(summer is SundayBingoSchedule.RoomState.LiveNow)
        assertTrue(winter is SundayBingoSchedule.RoomState.LiveNow)
        assertEquals("+02:00", summer.now.offset.toString())
        assertEquals("+01:00", winter.now.offset.toString())
    }

    @Test
    fun dstStartWeekend_targetsSunday1700AfterOffsetChange() {
        val state = stateAt("2026-03-28T12:00:00")

        assertEquals("2026-03-29T17:00+02:00[Europe/Berlin]", state.target.toString())
        assertEquals(28, Duration.between(state.now, state.target).toHours())
    }

    @Test
    fun dstEndWeekend_targetsSunday1700AfterOffsetChange() {
        val state = stateAt("2026-10-24T12:00:00")

        assertEquals("2026-10-25T17:00+01:00[Europe/Berlin]", state.target.toString())
        assertEquals(30, Duration.between(state.now, state.target).toHours())
    }

    @Test
    fun sundayRoomCardOpensBeforeDuringAndAfterLiveWindow() {
        listOf(
            "2026-09-27T16:59:59",
            "2026-09-27T17:00:00",
            "2026-09-27T17:59:59",
            "2026-09-27T18:00:00",
            "2026-09-28T09:00:00",
        ).forEach { localDateTime ->
            assertTrue(localDateTime, SundayBingoSchedule.canOpenSundayRoom())
            assertTrue(localDateTime, SundayBingoSchedule.canUseCalledNumbersQrTools())
        }
    }

    @Test
    fun keypadDisabledBeforeSunday1700() {
        val now = berlinAt("2026-09-27T16:59:59")
        assertFalse(SundayBingoSchedule.isSundayKeypadEnabled(now))
        assertFalse(SundayBingoSchedule.isSundayGameplayEnabled(isSundayRoom = true, now = now))
        assertTrue(SundayBingoSchedule.isSundayGameplayEnabled(isSundayRoom = false, now = now))
    }

    @Test
    fun keypadEnabledFrom1700UntilBefore1800() {
        val open = berlinAt("2026-09-27T17:00:00")
        val lastSecond = berlinAt("2026-09-27T17:59:59")
        assertTrue(SundayBingoSchedule.isSundayKeypadEnabled(open))
        assertTrue(SundayBingoSchedule.isSundayKeypadEnabled(lastSecond))
        assertTrue(SundayBingoSchedule.isSundayGameplayEnabled(isSundayRoom = true, now = open))
        assertTrue(SundayBingoSchedule.isSundayGameplayEnabled(isSundayRoom = true, now = lastSecond))
    }

    @Test
    fun keypadDisabledFromSunday1800() {
        val close = berlinAt("2026-09-27T18:00:00")
        val monday = berlinAt("2026-09-28T09:00:00")
        assertFalse(SundayBingoSchedule.isSundayKeypadEnabled(close))
        assertFalse(SundayBingoSchedule.isSundayKeypadEnabled(monday))
        assertFalse(SundayBingoSchedule.isSundayGameplayEnabled(isSundayRoom = true, now = close))
        assertFalse(SundayBingoSchedule.isSundayGameplayEnabled(isSundayRoom = true, now = monday))
    }

    @Test
    fun resumeAfterClose_recalculatesToNextSunday() {
        val beforePause = stateAt("2026-09-27T17:59:59")
        val afterResume = stateAt("2026-09-27T18:02:00")

        assertTrue(beforePause is SundayBingoSchedule.RoomState.LiveNow)
        assertTrue(afterResume is SundayBingoSchedule.RoomState.StartsIn)
        assertEquals("2026-10-04T17:00+02:00[Europe/Berlin]", afterResume.target.toString())
    }

    private fun berlinAt(localDateTime: String): ZonedDateTime =
        LocalDateTime.parse(localDateTime).atZone(zone)

    private fun stateAt(localDateTime: String): SundayBingoSchedule.RoomState {
        val berlinTime = berlinAt(localDateTime)
        val clock = Clock.fixed(berlinTime.toInstant(), zone)
        return SundayBingoSchedule.roomState(clock)
    }
}

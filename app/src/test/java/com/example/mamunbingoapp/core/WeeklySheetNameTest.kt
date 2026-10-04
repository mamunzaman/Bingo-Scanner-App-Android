package com.example.mamunbingoapp.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class WeeklySheetNameTest {
    private val zone = ZoneId.of("Europe/Berlin")

    @Test
    fun exactDuplicateInSameWeek_isBlocked() {
        val playedAt = at(2026, 9, 23, 12, 0)
        val existing = candidate("t1", "Friday Night", at(2026, 9, 22, 9, 0))
        assertEquals("t1", duplicate("Friday Night", playedAt, listOf(existing)))
    }

    @Test
    fun differentLetterCase_isBlocked() {
        val playedAt = at(2026, 9, 23, 12, 0)
        val existing = candidate("t1", "Friday Night", playedAt)
        assertEquals("t1", duplicate("friday NIGHT", playedAt, listOf(existing)))
    }

    @Test
    fun leadingTrailingAndRepeatedSpaces_areBlocked() {
        val playedAt = at(2026, 9, 23, 12, 0)
        val existing = candidate("t1", "Friday Night", playedAt)
        assertEquals("t1", duplicate("  Friday   Night  ", playedAt, listOf(existing)))
    }

    @Test
    fun sameNameInAnotherWeek_isAllowed() {
        val thisWeek = at(2026, 9, 23, 12, 0)
        val lastWeek = at(2026, 9, 16, 12, 0)
        val existing = candidate("t1", "Friday Night", lastWeek)
        assertNull(duplicate("Friday Night", thisWeek, listOf(existing)))
    }

    @Test
    fun editingSameTicket_isAllowed() {
        val playedAt = at(2026, 9, 23, 12, 0)
        val existing = candidate("t1", "Friday Night", playedAt)
        assertNull(
            WeeklySheetName.findDuplicateTicketId(
                sheetName = "Friday Night",
                playedAtMillis = playedAt,
                candidates = listOf(existing),
                excludeTicketId = "t1",
                zone = zone,
            )
        )
    }

    @Test
    fun sundayBelongsToWeekThatStartedPreviousMonday() {
        val sunday = at(2026, 9, 20, 23, 59)
        val monday = at(2026, 9, 21, 0, 0)
        val sundayTicket = candidate("sun", "Night Sheet", sunday)
        assertEquals("sun", duplicate("Night Sheet", at(2026, 9, 14, 8, 0), listOf(sundayTicket)))
        assertNull(duplicate("Night Sheet", monday, listOf(sundayTicket)))
    }

    @Test
    fun mondayMidnightStartsANewWeek() {
        val sundayLate = at(2026, 9, 20, 23, 59)
        val mondayStart = at(2026, 9, 21, 0, 0)
        val mondayWindow = WeeklySheetName.weekWindow(mondayStart, zone)
        val sundayWindow = WeeklySheetName.weekWindow(sundayLate, zone)
        assertEquals(mondayStart, mondayWindow.startInclusiveMillis)
        assertEquals(mondayStart, sundayWindow.endExclusiveMillis)
    }

    private fun duplicate(
        name: String,
        playedAtMillis: Long,
        candidates: List<WeeklySheetName.Candidate>,
    ): String? = WeeklySheetName.findDuplicateTicketId(
        sheetName = name,
        playedAtMillis = playedAtMillis,
        candidates = candidates,
        zone = zone,
    )

    private fun candidate(id: String, name: String, playedAtMillis: Long) =
        WeeklySheetName.Candidate(id, name, playedAtMillis)

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant().toEpochMilli()
}

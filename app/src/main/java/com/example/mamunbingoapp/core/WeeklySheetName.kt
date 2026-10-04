package com.example.mamunbingoapp.core

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Calendar week for sheet-name uniqueness: Monday 00:00 through the next Monday
 * in [zone] (exclusive end). Name match ignores case and repeated whitespace.
 */
object WeeklySheetName {
    data class WeekWindow(
        val startInclusiveMillis: Long,
        val endExclusiveMillis: Long,
    )

    data class Candidate(
        val ticketId: String,
        val sheetName: String,
        val playedAtMillis: Long,
    )

    fun normalize(raw: String): String =
        raw.trim().replace(WHITESPACE, " ").lowercase(Locale.ROOT)

    fun weekWindow(playedAtMillis: Long, zone: ZoneId): WeekWindow {
        val localDate = Instant.ofEpochMilli(playedAtMillis).atZone(zone).toLocalDate()
        val monday = localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val start = monday.atStartOfDay(zone)
        val end = start.plusWeeks(1)
        return WeekWindow(
            startInclusiveMillis = start.toInstant().toEpochMilli(),
            endExclusiveMillis = end.toInstant().toEpochMilli(),
        )
    }

    fun findDuplicateTicketId(
        sheetName: String,
        playedAtMillis: Long,
        candidates: List<Candidate>,
        excludeTicketId: String? = null,
        zone: ZoneId,
    ): String? {
        val normalized = normalize(sheetName)
        if (normalized.isEmpty()) return null
        val window = weekWindow(playedAtMillis, zone)
        val exclude = excludeTicketId?.trim()?.takeIf { it.isNotEmpty() }
        return candidates.firstOrNull { candidate ->
            candidate.ticketId != exclude &&
                candidate.playedAtMillis >= window.startInclusiveMillis &&
                candidate.playedAtMillis < window.endExclusiveMillis &&
                normalize(candidate.sheetName) == normalized
        }?.ticketId
    }

    private val WHITESPACE = Regex("\\s+")
}

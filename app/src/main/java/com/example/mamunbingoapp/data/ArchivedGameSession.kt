package com.example.mamunbingoapp.data

// TODO: Add session-level entity (archiveSessionId, startedAt, endedAt) instead of inferring only from ticket_play_logs rows.
data class ArchivedGameSession(
    val roomId: String,
    val archivedAt: Long,
    val title: String,
    val ticketCount: Int,
    val calledNumberCount: Int,
    val calledNumbers: List<Int>,
    val ticketIds: List<String>,
)

private fun List<TicketPlayLog>.archivedSessionTicketIds(): List<String> =
    map { it.ticketId }
        .filter { !it.isArchivedCallsOnlyPlaceholderTicketId() }
        .distinct()

/** Prefer a real ticket row for metadata; calls-only sessions use the placeholder row. */
private fun List<TicketPlayLog>.archivedSessionRepresentativeLog(): TicketPlayLog =
    firstOrNull { !it.ticketId.isArchivedCallsOnlyPlaceholderTicketId() } ?: first()

fun List<TicketPlayLog>.toArchivedGameSessions(): List<ArchivedGameSession> =
    groupBy { it.roomId to it.archivedAt }
        .map { (key, entries) ->
            val representative = entries.archivedSessionRepresentativeLog()
            val title = representative.drawDate?.takeIf { it.isNotBlank() } ?: representative.roomName
            val calledNumbers = representative.calledNumbers
            val ticketIds = entries.archivedSessionTicketIds()
            ArchivedGameSession(
                roomId = key.first,
                archivedAt = key.second,
                title = title,
                ticketCount = ticketIds.size,
                calledNumberCount = calledNumbers.size,
                calledNumbers = calledNumbers,
                ticketIds = ticketIds,
            )
        }
        .sortedByDescending { it.archivedAt }

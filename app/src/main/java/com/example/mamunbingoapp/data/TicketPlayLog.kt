package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.data.db.TicketPlayLogEntity
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.core.BingoWinChecker

/** Placeholder [TicketPlayLog.ticketId] when a session is archived with calls but no tickets. */
const val ARCHIVED_CALLS_ONLY_TICKET_ID = "__archived_calls_only__"

fun String.isArchivedCallsOnlyPlaceholderTicketId(): Boolean =
    this == ARCHIVED_CALLS_ONLY_TICKET_ID

data class TicketPlayLog(
    val id: String,
    val ticketId: String,
    val roomId: String,
    val roomName: String,
    val addedAt: Long,
    val archivedAt: Long,
    val drawDate: String?,
    val calledNumbers: List<Int>,
    val markedCount: Int,
    val bingoLineCount: Int,
)

fun TicketPlayLogEntity.toTicketPlayLog(): TicketPlayLog = TicketPlayLog(
    id = id,
    ticketId = ticketId,
    roomId = roomId,
    roomName = roomName,
    addedAt = addedAt,
    archivedAt = archivedAt,
    drawDate = drawDate,
    calledNumbers = calledNumbersSnapshot.toCalledNumbersSnapshot(),
    markedCount = markedCount,
    bingoLineCount = bingoLineCount,
)

fun String.toCalledNumbersSnapshot(): List<Int> =
    split(",").mapNotNull { it.trim().toIntOrNull() }

fun List<Int>.toCalledNumbersSnapshot(): String = joinToString(",")

fun mergeTicketCellsWithArchivedCalledNumbers(
    cells: List<BingoCellUi>,
    calledNumbers: List<Int>,
): List<BingoCellUi> {
    val calledSet = calledNumbers.toSet()
    return cells.map { cell ->
        val num = cell.number?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
        cell.copy(isMarked = (num != null && num in calledSet) || cell.isMarked)
    }
}

object TicketPlayLogStats {
    fun compute(cells: List<BingoCellUi>, calledNumbers: List<Int>): Pair<Int, Int> {
        val calledSet = calledNumbers.toSet()
        val markedSet = cells.take(25).mapIndexed { index, cell ->
            val num = cell.number?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
            index.takeIf { (num != null && num in calledSet) || cell.isMarked }
        }.filterNotNull().toSet()
        val win = BingoWinChecker.check(markedSet)
        return markedSet.size to win.winningLines.size
    }
}

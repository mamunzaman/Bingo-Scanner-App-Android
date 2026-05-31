package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.data.db.TicketPlayLogEntity
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.core.BingoWinChecker

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

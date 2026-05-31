package com.example.mamunbingoapp.core

import com.example.mamunbingoapp.ui.model.BingoCellUi

object BingoPlayableNumbers {
    const val GRID_CELL_COUNT = 25
    const val FREE_CENTER_CELL_INDEX = 12
    const val PLAYABLE_COUNT = 22

    fun isPlayableCellIndex(index: Int): Boolean = index != FREE_CENTER_CELL_INDEX

    fun countMarkedPlayableCells(cells: List<BingoCellUi>): Int =
        cells.take(GRID_CELL_COUNT)
            .mapIndexed { index, cell -> index to cell }
            .count { (index, cell) ->
                isPlayableCellIndex(index) && cell.isMarked && cellHasPlayableNumber(cell)
            }

    fun countMarkedPlayableFlags(markedCells: List<Boolean>): Int =
        markedCells.take(GRID_CELL_COUNT)
            .mapIndexed { index, marked -> index to marked }
            .count { (index, marked) -> isPlayableCellIndex(index) && marked }

    fun coercePlayableMarkedCount(count: Int): Int = count.coerceIn(0, PLAYABLE_COUNT)

    fun formatMarkedProgress(marked: Int): String =
        "${coercePlayableMarkedCount(marked)}/$PLAYABLE_COUNT"

    private fun cellHasPlayableNumber(cell: BingoCellUi): Boolean {
        val raw = cell.number?.trim()?.takeIf { it.isNotEmpty() } ?: return false
        return !raw.equals("FREE", ignoreCase = true)
    }
}

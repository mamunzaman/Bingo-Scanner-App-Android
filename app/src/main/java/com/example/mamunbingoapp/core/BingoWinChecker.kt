package com.example.mamunbingoapp.core

data class WinningLine(val type: LineType, val cellIndices: List<Int>) {
    init {
        require(cellIndices.size == 5) { "WinningLine must have exactly 5 cells" }
    }
}

enum class LineType { ROW, COLUMN, DIAGONAL_MAIN, DIAGONAL_ANTI }

data class BingoWinResult(
    val isWin: Boolean,
    val winningLines: List<WinningLine>,
    val winningCells: Set<Int>
) {
    companion object {
        val NONE = BingoWinResult(false, emptyList(), emptySet())
    }
}

object BingoWinChecker {
    private val ROWS: List<List<Int>> = (0..4).map { r -> (0..4).map { c -> r * 5 + c } }
    private val COLS: List<List<Int>> = (0..4).map { c -> (0..4).map { r -> r * 5 + c } }
    private val DIAG_MAIN = listOf(0, 6, 12, 18, 24)
    private val DIAG_ANTI = listOf(4, 8, 12, 16, 20)
    private val ALL_LINES: List<Pair<LineType, List<Int>>> = listOf(
        *ROWS.mapIndexed { i, indices -> LineType.ROW to indices }.toTypedArray(),
        *COLS.mapIndexed { i, indices -> LineType.COLUMN to indices }.toTypedArray(),
        LineType.DIAGONAL_MAIN to DIAG_MAIN,
        LineType.DIAGONAL_ANTI to DIAG_ANTI
    )

    fun check(marked: Set<Int>): BingoWinResult {
        if (marked.size < 5) return BingoWinResult.NONE
        val winningLines = mutableListOf<WinningLine>()
        for ((type, indices) in ALL_LINES) {
            if (indices.all { it in marked }) {
                winningLines.add(WinningLine(type, indices))
            }
        }
        val cells = winningLines.flatMap { it.cellIndices }.toSet()
        return BingoWinResult(winningLines.isNotEmpty(), winningLines, cells)
    }

    fun check(marked: BooleanArray): BingoWinResult {
        require(marked.size >= 25) { "marked must have at least 25 elements" }
        val set = marked.take(25).mapIndexed { index, b -> index.takeIf { b } }.filterNotNull().toSet()
        return check(set)
    }

    fun bestAlmostBingo(marked: List<Boolean>): AlmostBingoInfo? {
        if (marked.size < 25) return null
        val arr = marked.take(25)
        var best: AlmostBingoInfo? = null
        val lineLabels = listOf(
            *ROWS.mapIndexed { i, _ -> "Row ${i + 1}" }.toTypedArray(),
            *COLS.mapIndexed { i, _ -> "Column ${i + 1}" }.toTypedArray(),
            "Diagonal",
            "Diagonal"
        )
        ALL_LINES.forEachIndexed { idx, (_, indices) ->
            val count = indices.count { arr.getOrNull(it) == true }
            if (count >= 4 && count < 5 && (best == null || count > best!!.marked)) {
                best = AlmostBingoInfo(lineLabels.getOrElse(idx) { "Line" }, count, 5)
            }
        }
        return best
    }
}

data class AlmostBingoInfo(val lineLabel: String, val marked: Int, val total: Int)

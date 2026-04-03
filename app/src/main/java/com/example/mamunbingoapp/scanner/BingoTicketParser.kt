package com.example.mamunbingoapp.scanner

object BingoTicketParser {
    private val B_RANGE = 1..15
    private val I_RANGE = 16..30
    private val N_RANGE = 31..45
    private val G_RANGE = 46..60
    private val O_RANGE = 61..75
    private val COLUMN_RANGES = listOf(B_RANGE, I_RANGE, N_RANGE, G_RANGE, O_RANGE)
    private val GRID_SIZE = 25

    fun parseFromOcrText(rawText: String): BingoTicketScanResult {
        tryRowBasedParse(rawText)?.let { return it }
        return flatWindowParse(rawText)
    }

    /**
     * Extra whitespace / punctuation normalization, then [parseFromOcrText].
     * Used when ML Kit yields few spatial elements but full text may still contain the grid.
     */
    fun parseFromOcrTextRelaxed(rawText: String): BingoTicketScanResult {
        val first = parseFromOcrText(rawText)
        if (first.isValid) return first
        val cleaned = rawText
            .replace(Regex("[|;,•]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (cleaned.isEmpty() || cleaned == rawText.trim()) return first
        val second = parseFromOcrText(cleaned)
        return if (second.isValid) second else first
    }

    /** True when [rowMajor] has 25 slots and each column matches B/I/N/G/O ranges. */
    fun isValidRowMajorGrid(rowMajor: List<Int>): Boolean {
        if (rowMajor.size != 25) return false
        return isValidBingoGrid(rowMajor)
    }

    /** Counts non-zero cells whose value lies in the expected column range (row-major layout). */
    fun columnRangeMatchCount(rowMajor: List<Int>): Int {
        if (rowMajor.isEmpty()) return 0
        if (rowMajor.size < 25) return rowMajor.count { it in 1..75 }
        var n = 0
        for (r in 0..4) {
            for (c in 0..4) {
                val v = rowMajor[r * 5 + c]
                if (v == 0) continue
                if (v in COLUMN_RANGES[c]) n++
            }
        }
        return n
    }

    private fun tryRowBasedParse(rawText: String): BingoTicketScanResult? {
        val candidateRows = rawText
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { lineToNumbers(it) }

        if (candidateRows.size < 5) return null

        for (start in 0..candidateRows.size - 5) {
            val rows = candidateRows.subList(start, start + 5)
            if (!areValidBingoRows(rows)) continue
            val columnOrder = rowsToColumnOrder(rows)
            return BingoTicketScanResult(gridNumbers = columnOrder, isValid = true)
        }
        return null
    }

    private fun lineToNumbers(line: String): List<Int>? {
        val tokens = extractDigitTokens(line)
        if (tokens.isEmpty()) return null
        return buildNumbersForFixedWidth(tokens, expectedCount = 5)
    }

    private fun areValidBingoRows(rows: List<List<Int>>): Boolean {
        for (c in 0..4) {
            val col = (0..4).map { rows[it][c] }
            val range = when (c) {
                0 -> B_RANGE
                1 -> I_RANGE
                2 -> N_RANGE
                3 -> G_RANGE
                else -> O_RANGE
            }
            if (!col.all { it in range }) return false
        }
        return true
    }

    private fun rowsToColumnOrder(rows: List<List<Int>>): List<Int> =
        (0..4).flatMap { col -> (0..4).map { row -> rows[row][col] } }

    private fun flatWindowParse(rawText: String): BingoTicketScanResult {
        val normalized = rawText.trim().replace(Regex("\\s+"), " ")
        val tokens = extractDigitTokens(normalized)
        if (tokens.isEmpty()) {
            return BingoTicketScanResult(
                isValid = false,
                errorMessage = "Not enough numbers found (need 25, got 0)"
            )
        }
        for (start in tokens.indices) {
            val window = buildRowMajorWindow(tokens, start) ?: continue
            if (isValidBingoGrid(window)) {
                val columnOrder = toColumnOrder(window)
                return BingoTicketScanResult(gridNumbers = columnOrder, isValid = true)
            }
        }
        return BingoTicketScanResult(
            isValid = false,
            errorMessage = "No valid 25-number Bingo grid found (B:1-15, I:16-30, N:31-45, G:46-60, O:61-75)"
        )
    }

    private fun isValidBingoGrid(rowMajor: List<Int>): Boolean {
        for (c in 0..4) {
            val col = (0..4).map { rowMajor[it * 5 + c] }
            val range = when (c) {
                0 -> B_RANGE
                1 -> I_RANGE
                2 -> N_RANGE
                3 -> G_RANGE
                else -> O_RANGE
            }
            if (!col.all { it in range }) return false
        }
        return true
    }

    private fun toColumnOrder(rowMajor: List<Int>): List<Int> {
        return (0..4).flatMap { col ->
            (0..4).map { row -> rowMajor[row * 5 + col] }
        }
    }

    private fun extractDigitTokens(text: String): List<String> =
        Regex("\\d+").findAll(text).map { it.value }.toList()

    private fun buildNumbersForFixedWidth(tokens: List<String>, expectedCount: Int): List<Int>? =
        buildFixedWidthRecursive(tokens, tokenIndex = 0, expectedCount = expectedCount, current = emptyList())

    private fun buildFixedWidthRecursive(
        tokens: List<String>,
        tokenIndex: Int,
        expectedCount: Int,
        current: List<Int>
    ): List<Int>? {
        if (tokenIndex == tokens.size) {
            return if (current.size == expectedCount) current else null
        }
        if (current.size >= expectedCount) return null

        for (candidate in splitTokenCandidates(tokens[tokenIndex])) {
            if (current.size + candidate.size > expectedCount) continue
            if (!candidateFitsPositions(candidate, current.size)) continue
            val result = buildFixedWidthRecursive(
                tokens = tokens,
                tokenIndex = tokenIndex + 1,
                expectedCount = expectedCount,
                current = current + candidate
            )
            if (result != null) return result
        }
        return null
    }

    private fun buildRowMajorWindow(tokens: List<String>, startIndex: Int): List<Int>? =
        buildWindowRecursive(tokens, tokenIndex = startIndex, current = emptyList())

    private fun buildWindowRecursive(
        tokens: List<String>,
        tokenIndex: Int,
        current: List<Int>
    ): List<Int>? {
        if (current.size == GRID_SIZE) return current
        if (tokenIndex >= tokens.size) return null

        for (candidate in splitTokenCandidates(tokens[tokenIndex])) {
            if (current.size + candidate.size > GRID_SIZE) continue
            if (!candidateFitsPositions(candidate, current.size)) continue
            val result = buildWindowRecursive(
                tokens = tokens,
                tokenIndex = tokenIndex + 1,
                current = current + candidate
            )
            if (result != null) return result
        }
        return null
    }

    private fun candidateFitsPositions(candidate: List<Int>, startPosition: Int): Boolean =
        candidate.indices.all { offset ->
            candidate[offset] in rangeForPosition(startPosition + offset)
        }

    private fun rangeForPosition(position: Int): IntRange = COLUMN_RANGES[position % 5]

    private fun splitTokenCandidates(token: String): List<List<Int>> {
        if (token.isEmpty()) return emptyList()
        if (token.length <= 2) {
            val value = token.toIntOrNull()
            return if (value != null && value in 1..75) listOf(listOf(value)) else emptyList()
        }
        val results = mutableListOf<List<Int>>()
        collectTokenSplits(token = token, index = 0, current = mutableListOf(), results = results)
        return results.sortedBy { it.size }
    }

    private fun collectTokenSplits(
        token: String,
        index: Int,
        current: MutableList<Int>,
        results: MutableList<List<Int>>
    ) {
        if (index == token.length) {
            if (current.isNotEmpty()) results.add(current.toList())
            return
        }

        for (length in 1..2) {
            val end = index + length
            if (end > token.length) continue
            val part = token.substring(index, end)
            if (part.startsWith("0")) continue
            val value = part.toIntOrNull() ?: continue
            if (value !in 1..75) continue
            current.add(value)
            collectTokenSplits(token, end, current, results)
            current.removeAt(current.lastIndex)
        }
    }
}

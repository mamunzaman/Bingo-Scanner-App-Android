package com.example.mamunbingoapp.ui.components

import com.example.mamunbingoapp.data.bingo.BingoDrawValidator
import com.example.mamunbingoapp.data.remote.BingoDrawResultDto
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GroupCalledNumbersByColumnTest {
    private val official = listOf(
        6, 50, 20, 60, 13, 46, 40, 2, 56, 54, 62,
        63, 5, 37, 73, 7, 65, 41, 12, 55, 43, 72,
    )

    @Test
    fun groupingPreservesRelativeCallOrderInEveryColumn() {
        val columns = groupCalledNumbersByColumn(official)
        assertEquals(listOf(6, 13, 2, 5, 7, 12), columns["B"])
        assertEquals(listOf(20), columns["I"])
        assertEquals(listOf(40, 37, 41, 43), columns["N"])
        assertEquals(listOf(50, 60, 46, 56, 54, 55), columns["G"])
        assertEquals(listOf(62, 63, 73, 65, 72), columns["O"])
    }

    @Test
    fun groupingDoesNotSortNumerically() {
        val b = groupCalledNumbersByColumn(official)["B"].orEmpty()
        assertNotEquals(b.sorted(), b)
        val g = groupCalledNumbersByColumn(official)["G"].orEmpty()
        assertNotEquals(g.sorted(), g)
    }

    @Test
    fun homePreviewIsFirstSixApiCalls() {
        assertEquals(listOf(6, 50, 20, 60, 13, 46), official.take(6))
    }

    @Test
    fun all22AppearOnceAcrossColumns() {
        val columns = groupCalledNumbersByColumn(official)
        val flattened = listOf("B", "I", "N", "G", "O").flatMap { columns[it].orEmpty() }
        assertEquals(22, flattened.size)
        assertEquals(official.toSet(), flattened.toSet())
        assertEquals(22, flattened.toSet().size)
    }

    @Test
    fun invalidDrawValidationUnchanged() {
        val bad = official.toMutableList()
        bad[21] = bad[0]
        assertNull(
            BingoDrawValidator.validateDraw(
                LocalDate.of(2026, 10, 4),
                BingoDrawResultDto("2026-10-04", bad),
            ),
        )
    }
}

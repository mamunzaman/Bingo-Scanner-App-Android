package com.example.mamunbingoapp.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BingoTicketParserTest {

    @Test
    fun parseFromOcrText_extracts_valid_bingo_grid_from_ticket_text() {
        val rawText = """
            M. Niemeyer GmbH & Co. KG
            Duckwitzstr. 55
            Bremen
            BINGO
            Seriennummer-Losnummer
            6958 16996
            B I N G O
            6 29 38 58 61
            9 28 43 56 74
            11 18 44 49 62
            8 17 34 60 72
            14 16 40 50 69
            SUPER 6 - NEIN
            8836637
        """.trimIndent()

        val result = BingoTicketParser.parseFromOcrText(rawText)

        assertTrue(result.isValid)
        assertEquals(
            listOf(
                6, 9, 11, 8, 14,
                29, 28, 18, 17, 16,
                38, 43, 44, 34, 40,
                58, 56, 49, 60, 50,
                61, 74, 62, 72, 69
            ),
            result.gridNumbers
        )
    }

    @Test
    fun parseFromOcrText_returns_invalid_when_5x5_block_does_not_match_bingo_column_ranges() {
        val rawText = """
            BINGO
            22 29 38 58 61
            9 28 43 56 74
            11 18 44 49 62
            8 17 34 60 72
            14 16 40 50 69
        """.trimIndent()

        val result = BingoTicketParser.parseFromOcrText(rawText)

        assertFalse(result.isValid)
        assertTrue(result.gridNumbers.isEmpty())
    }

    @Test
    fun parseFromOcrText_extracts_valid_grid_from_merged_token_ocr_lines() {
        val rawText = """
            BINGO
            15 22 345374
            9 29 3850 75
            4 28 42 6065
            12741 55 63
            11 2045 51 62
        """.trimIndent()

        val result = BingoTicketParser.parseFromOcrText(rawText)

        assertTrue(result.isValid)
        assertEquals(
            listOf(
                15, 9, 4, 1, 11,
                22, 29, 28, 27, 20,
                34, 38, 42, 41, 45,
                53, 50, 60, 55, 51,
                74, 75, 65, 63, 62
            ),
            result.gridNumbers
        )
    }
}

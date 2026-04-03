package com.example.mamunbingoapp.ui.components.common

import org.junit.Assert.assertEquals
import org.junit.Test

class BingoLetterTest {

    @Test
    fun null_returnsEmpty() {
        assertEquals("", bingoLetter(null))
    }

    @Test
    fun zero_returnsEmpty() {
        assertEquals("", bingoLetter(0))
    }

    @Test
    fun one_returnsB() {
        assertEquals("B", bingoLetter(1))
    }

    @Test
    fun fifteen_returnsB() {
        assertEquals("B", bingoLetter(15))
    }

    @Test
    fun sixteen_returnsI() {
        assertEquals("I", bingoLetter(16))
    }

    @Test
    fun thirty_returnsI() {
        assertEquals("I", bingoLetter(30))
    }

    @Test
    fun thirtyOne_returnsN() {
        assertEquals("N", bingoLetter(31))
    }

    @Test
    fun fortyFive_returnsN() {
        assertEquals("N", bingoLetter(45))
    }

    @Test
    fun fortySix_returnsG() {
        assertEquals("G", bingoLetter(46))
    }

    @Test
    fun sixty_returnsG() {
        assertEquals("G", bingoLetter(60))
    }

    @Test
    fun sixtyOne_returnsO() {
        assertEquals("O", bingoLetter(61))
    }

    @Test
    fun seventyFive_returnsO() {
        assertEquals("O", bingoLetter(75))
    }

    @Test
    fun seventySix_returnsEmpty() {
        assertEquals("", bingoLetter(76))
    }
}

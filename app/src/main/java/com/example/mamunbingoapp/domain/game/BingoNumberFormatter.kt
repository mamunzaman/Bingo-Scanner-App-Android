package com.example.mamunbingoapp.domain.game

object BingoNumberFormatter {

    fun toDisplay(number: Int): String {
        val letter = when (number) {
            in 1..15 -> "B"
            in 16..30 -> "I"
            in 31..45 -> "N"
            in 46..60 -> "G"
            in 61..75 -> "O"
            else -> "?"
        }
        return "$letter-$number"
    }
}

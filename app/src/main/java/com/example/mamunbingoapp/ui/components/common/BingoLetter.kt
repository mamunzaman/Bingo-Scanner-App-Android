package com.example.mamunbingoapp.ui.components.common

fun bingoLetter(number: Int?): String {
    return when (number) {
        null -> ""
        in 1..15 -> "B"
        in 16..30 -> "I"
        in 31..45 -> "N"
        in 46..60 -> "G"
        in 61..75 -> "O"
        else -> ""
    }
}

package com.example.mamunbingoapp.core

fun formatCallProgress(calledCount: Int): String {
    val clamped = calledCount.coerceAtMost(MAX_CALLED_NUMBERS)
    return "$clamped/$MAX_CALLED_NUMBERS"
}

fun progressFraction(calledCount: Int): Float {
    return calledCount.coerceAtMost(MAX_CALLED_NUMBERS).toFloat() / MAX_CALLED_NUMBERS.toFloat()
}

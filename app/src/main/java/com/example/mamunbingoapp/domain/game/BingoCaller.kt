package com.example.mamunbingoapp.domain.game

import kotlin.random.Random

class BingoCaller {

    private val remainingNumbers = (1..75).toMutableList()
    private val random = Random

    fun reset() {
        remainingNumbers.clear()
        remainingNumbers.addAll(1..75)
    }

    fun callNext(): Int? {
        if (remainingNumbers.isEmpty()) return null

        val index = random.nextInt(remainingNumbers.size)
        return remainingNumbers.removeAt(index)
    }

    fun remainingCount(): Int {
        return remainingNumbers.size
    }

    fun restore(number: Int) {
        if (number !in remainingNumbers && number in 1..75) {
            remainingNumbers.add(number)
        }
    }

    fun callSpecific(number: Int): Int? {
        if (number !in 1..75) return null
        val index = remainingNumbers.indexOf(number)
        if (index == -1) return null
        return remainingNumbers.removeAt(index)
    }
}

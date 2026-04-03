package com.example.mamunbingoapp.domain.game

class CalledNumbersManager {

    private val calledNumbers = mutableListOf<Int>()

    fun add(number: Int) {
        if (!calledNumbers.contains(number)) {
            calledNumbers.add(number)
        }
    }

    fun getAll(): List<Int> {
        return calledNumbers.toList()
    }

    fun last(): Int? {
        return calledNumbers.lastOrNull()
    }

    fun removeLast(): Int? {
        return if (calledNumbers.isNotEmpty()) {
            calledNumbers.removeAt(calledNumbers.lastIndex)
        } else {
            null
        }
    }

    fun clear() {
        calledNumbers.clear()
    }

    fun count(): Int {
        return calledNumbers.size
    }
}

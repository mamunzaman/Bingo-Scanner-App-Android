package com.example.mamunbingoapp.engine

import com.example.mamunbingoapp.ui.model.BingoCellUi
import kotlin.random.Random

object LiveSessionManager {

    private var _remainingNumbers = (1..75).toMutableList()
    private var _calledNumbers = mutableListOf<Int>()

    fun leaveRoom(roomId: String) {
    }

    val remainingNumbers: List<Int> get() = _remainingNumbers.toList()
    val calledNumbers: List<Int> get() = _calledNumbers.toList()

    init {
        shuffleRemaining()
    }

    fun callNextNumber(): Int? {
        if (_remainingNumbers.isEmpty()) return null
        val number = _remainingNumbers.removeAt(0)
        _calledNumbers.add(number)
        return number
    }

    fun resetSession() {
        _remainingNumbers = (1..75).toMutableList()
        _calledNumbers.clear()
        shuffleRemaining()
    }

    fun autoMarkCells(cells: List<BingoCellUi>, number: Int): List<BingoCellUi> {
        val formatted = number.toString().padStart(2, '0')
        return cells.map { cell ->
            if (cell.number == formatted && !cell.isMarked) {
                cell.copy(isMarked = true, isCalled = true)
            } else {
                cell
            }
        }
    }

    fun checkBingo(cells: List<BingoCellUi>): Boolean {
        if (cells.size != 25) return false
        return hasHorizontalWin(cells) || hasVerticalWin(cells) || hasDiagonalWin(cells)
    }

    private fun shuffleRemaining() {
        _remainingNumbers.shuffle(Random.Default)
    }

    private fun hasHorizontalWin(cells: List<BingoCellUi>): Boolean {
        for (row in 0..4) {
            if ((0..4).all { col -> cells[row * 5 + col].isMarked }) return true
        }
        return false
    }

    private fun hasVerticalWin(cells: List<BingoCellUi>): Boolean {
        for (col in 0..4) {
            if ((0..4).all { row -> cells[row * 5 + col].isMarked }) return true
        }
        return false
    }

    private fun hasDiagonalWin(cells: List<BingoCellUi>): Boolean {
        val diagBack = listOf(0, 6, 12, 18, 24).all { cells[it].isMarked }
        val diagForward = listOf(4, 8, 12, 16, 20).all { cells[it].isMarked }
        return diagBack || diagForward
    }
}

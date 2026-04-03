package com.example.mamunbingoapp.domain.game

class BingoGameController {

    private var state: BingoGameState = BingoGameState.IDLE

    fun reset() {
        state = BingoGameState.IDLE
    }

    fun start() {
        state = BingoGameState.RUNNING
    }

    fun finish() {
        state = BingoGameState.FINISHED
    }

    fun isRunning(): Boolean = state == BingoGameState.RUNNING

    fun getState(): BingoGameState = state
}

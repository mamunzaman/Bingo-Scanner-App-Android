package com.example.mamunbingoapp.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.mamunbingoapp.domain.game.BingoRoundManager
import com.example.mamunbingoapp.ui.model.BingoCallerUiState

class BingoCallerViewModel : ViewModel() {

    private val roundManager = BingoRoundManager()

    private val _uiState = mutableStateOf(BingoCallerUiState())
    val uiState: State<BingoCallerUiState> = _uiState

    init {
        updateUiState()
    }

    fun startGame() {
        roundManager.startGame()
        updateUiState()
    }

    private var lastCalledAtMillis: Long? = null

    fun callNextNumber() {
        val n = roundManager.callNextNumber()
        if (n != null) lastCalledAtMillis = System.currentTimeMillis()
        updateUiState()
    }

    fun callSpecificNumber(number: Int): Boolean {
        val beforeLast = roundManager.getLastCalledNumber()
        val result = roundManager.callSpecificNumber(number)
        if (result != null) lastCalledAtMillis = System.currentTimeMillis()
        updateUiState()
        return result != null && result != beforeLast
    }

    fun undoLastCall() {
        roundManager.undoLastCall()
        lastCalledAtMillis = null
        updateUiState()
    }

    private fun updateUiState() {
        val last = roundManager.getLastCalledNumber()
        if (last == null) lastCalledAtMillis = null
        _uiState.value = BingoCallerUiState(
            lastCalledNumber = last,
            lastCalledAtMillis = lastCalledAtMillis,
            calledNumbers = roundManager.getCalledNumbers(),
            remainingCount = roundManager.getRemainingCount(),
            gameState = roundManager.getGameState()
        )
    }
}

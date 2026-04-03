package com.example.mamunbingoapp.ui.model

import com.example.mamunbingoapp.domain.game.BingoGameState

data class BingoCallerUiState(
    val lastCalledNumber: Int? = null,
    val lastCalledAtMillis: Long? = null,
    val calledNumbers: List<Int> = emptyList(),
    val remainingCount: Int = 75,
    val gameState: BingoGameState = BingoGameState.IDLE
)

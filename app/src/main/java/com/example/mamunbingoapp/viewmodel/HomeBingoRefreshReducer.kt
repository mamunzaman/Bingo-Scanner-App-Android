package com.example.mamunbingoapp.viewmodel

import com.example.mamunbingoapp.data.bingo.BingoDrawResult
import com.example.mamunbingoapp.data.bingo.BingoStatus

internal data class HomeBingoUiSnapshot(
    val status: BingoStatus? = null,
    val draw: BingoDrawResult? = null,
    val isLoading: Boolean = false,
    val blockingError: String? = null,
    val refreshWarning: String? = null,
) {
    val hasDisplayableCache: Boolean get() = status != null || draw != null
}

internal object HomeBingoRefreshReducer {
    fun applyCached(
        state: HomeBingoUiSnapshot,
        status: BingoStatus?,
        draw: BingoDrawResult?,
    ): HomeBingoUiSnapshot = state.copy(
        status = status ?: state.status,
        draw = draw ?: state.draw,
        blockingError = null,
    )

    fun applyStatusSuccess(
        state: HomeBingoUiSnapshot,
        status: BingoStatus,
    ): HomeBingoUiSnapshot = state.copy(
        status = status,
        blockingError = null,
        refreshWarning = if (state.draw != null) state.refreshWarning else null,
    )

    fun applyStatusFailure(
        state: HomeBingoUiSnapshot,
        warning: String,
        blocking: String,
    ): HomeBingoUiSnapshot = if (state.hasDisplayableCache) {
        state.copy(refreshWarning = warning, blockingError = null)
    } else {
        state.copy(blockingError = blocking, refreshWarning = null)
    }

    fun applyDrawSuccess(
        state: HomeBingoUiSnapshot,
        draw: BingoDrawResult,
    ): HomeBingoUiSnapshot = state.copy(
        draw = draw,
        blockingError = null,
        refreshWarning = if (state.status != null) null else state.refreshWarning,
    )

    fun applyDrawFailure(
        state: HomeBingoUiSnapshot,
        warning: String,
        blocking: String,
    ): HomeBingoUiSnapshot = if (state.hasDisplayableCache) {
        state.copy(refreshWarning = warning, blockingError = null)
    } else {
        state.copy(blockingError = blocking, refreshWarning = null)
    }
}

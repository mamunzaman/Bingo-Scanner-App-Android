package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.remote.BingoDrawDto
import com.example.mamunbingoapp.data.remote.BingoPrizeDto
import com.example.mamunbingoapp.data.remote.BingoRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _latestDraw = MutableStateFlow<BingoDrawDto?>(null)
    val latestDraw: StateFlow<BingoDrawDto?> = _latestDraw.asStateFlow()

    private val _latestPrizes = MutableStateFlow<List<BingoPrizeDto>>(emptyList())
    val latestPrizes: StateFlow<List<BingoPrizeDto>> = _latestPrizes.asStateFlow()

    private val _isRemoteLoading = MutableStateFlow(false)
    val isRemoteLoading: StateFlow<Boolean> = _isRemoteLoading.asStateFlow()

    private val _remoteError = MutableStateFlow<String?>(null)
    val remoteError: StateFlow<String?> = _remoteError.asStateFlow()

    init {
        loadLatestBingoDraw()
    }

    private fun loadLatestBingoDraw() {
        viewModelScope.launch {
            _isRemoteLoading.value = true
            _remoteError.value = null
            BingoRemoteRepository.getLatestDraw()
                .onSuccess { draw ->
                    _latestDraw.value = draw
                    BingoRemoteRepository.getPrizesForDraw(draw.id)
                        .onSuccess { prizes -> _latestPrizes.value = prizes }
                        .onFailure { error ->
                            _remoteError.value = error.message ?: "Could not load prizes."
                        }
                }
                .onFailure { error ->
                    _latestDraw.value = null
                    _latestPrizes.value = emptyList()
                    _remoteError.value = error.message ?: "Could not load latest draw."
                }
            _isRemoteLoading.value = false
        }
    }
}

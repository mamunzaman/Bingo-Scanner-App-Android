package com.example.mamunbingoapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.remote.BingoDrawDto
import com.example.mamunbingoapp.data.remote.BingoPrizeDto
import com.example.mamunbingoapp.data.remote.BingoRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val TAG = "HomeViewModel"
    }

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
                            Log.w(TAG, "Failed to load prizes", error)
                            _remoteError.value = getApplication<Application>().getString(
                                R.string.home_error_load_prizes,
                            )
                        }
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to load latest draw", error)
                    _latestDraw.value = null
                    _latestPrizes.value = emptyList()
                    _remoteError.value = getApplication<Application>().getString(
                        R.string.home_error_load_latest_draw,
                    )
                }
            _isRemoteLoading.value = false
        }
    }
}

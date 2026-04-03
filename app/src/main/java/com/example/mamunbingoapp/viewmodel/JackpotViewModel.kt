package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JackpotViewModel : ViewModel() {
    private val _sheetCount = MutableStateFlow(3)
    val sheetCount: StateFlow<Int> = _sheetCount.asStateFlow()

    private val _calledCount = MutableStateFlow(12)
    val calledCount: StateFlow<Int> = _calledCount.asStateFlow()
}

package com.example.mamunbingoapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mamunbingoapp.data.RoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CalledNumbersViewModel(
    private val roomId: String,
) {
    var selectedCalledNumber by mutableStateOf<Int?>(null)
        private set
    var isReplaceMode by mutableStateOf(false)
        private set

    fun onNumberTapped(number: Int) {
        if (selectedCalledNumber == number) {
            clearSelection()
        } else {
            selectedCalledNumber = number
            isReplaceMode = false
        }
    }

    fun clearSelection() {
        selectedCalledNumber = null
        isReplaceMode = false
    }

    fun startReplace() {
        if (selectedCalledNumber != null) {
            isReplaceMode = true
        }
    }

    fun deleteSelected(scope: CoroutineScope, onDone: (Boolean) -> Unit) {
        val number = selectedCalledNumber ?: return
        if (roomId.isBlank()) return
        scope.launch {
            val removed = RoomRepository.removeCalledNumber(roomId, number)
            if (removed) clearSelection()
            onDone(removed)
        }
    }

    fun replaceWith(scope: CoroutineScope, newNumber: Int, onDone: (Boolean) -> Unit) {
        val oldNumber = selectedCalledNumber ?: return
        if (roomId.isBlank()) return
        scope.launch {
            val replaced = RoomRepository.replaceCalledNumber(roomId, oldNumber, newNumber)
            if (replaced) clearSelection()
            onDone(replaced)
        }
    }
}

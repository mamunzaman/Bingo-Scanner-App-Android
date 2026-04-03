package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.ui.components.AppTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainTabsViewModel : ViewModel() {
    private val _selectedTab = MutableStateFlow(AppTab.Home)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _lastActiveRoomId = MutableStateFlow<String?>(null)
    val lastActiveRoomId: StateFlow<String?> = _lastActiveRoomId.asStateFlow()

    fun setSelectedTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setLastActiveRoomId(roomId: String?) {
        _lastActiveRoomId.value = roomId
    }

    fun callNumber(number: Int, onResult: (Boolean) -> Unit) {
        if (number !in 1..75) {
            onResult(false)
            return
        }
        val rid = _lastActiveRoomId.value ?: run {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val added = RoomRepository.addCalledNumberIfAllowed(rid, number)
            onResult(added)
        }
    }
}

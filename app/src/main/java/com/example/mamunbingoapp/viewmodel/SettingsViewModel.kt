package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.SettingsRepository
import com.example.mamunbingoapp.data.preferences.LiveHeaderStyle
import com.example.mamunbingoapp.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    val showDemoData: StateFlow<Boolean> = SettingsRepository.showDemoDataFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setShowDemoData(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setShowDemoData(value) }
    }

    val liveHeaderStyle: StateFlow<LiveHeaderStyle> = UserPreferencesRepository.liveHeaderStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiveHeaderStyle.V1_CLEAN)

    fun setLiveHeaderStyle(value: LiveHeaderStyle) {
        viewModelScope.launch { UserPreferencesRepository.setLiveHeaderStyle(value) }
    }

    private val _pushNotifications = MutableStateFlow(true)
    val pushNotifications: StateFlow<Boolean> = _pushNotifications.asStateFlow()

    private val _dailyReminders = MutableStateFlow(false)
    val dailyReminders: StateFlow<Boolean> = _dailyReminders.asStateFlow()

    private val _faceIdTouchId = MutableStateFlow(true)
    val faceIdTouchId: StateFlow<Boolean> = _faceIdTouchId.asStateFlow()

    private val _dataSharing = MutableStateFlow(false)
    val dataSharing: StateFlow<Boolean> = _dataSharing.asStateFlow()

    fun setPushNotifications(value: Boolean) { _pushNotifications.value = value }
    fun setDailyReminders(value: Boolean) { _dailyReminders.value = value }
    fun setFaceIdTouchId(value: Boolean) { _faceIdTouchId.value = value }
    fun setDataSharing(value: Boolean) { _dataSharing.value = value }
}

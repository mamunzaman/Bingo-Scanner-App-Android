package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.core.SundayTestTimeSettings
import com.example.mamunbingoapp.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    val showDemoData: StateFlow<Boolean> = SettingsRepository.showDemoDataFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val keepScreenOnDuringGame: StateFlow<Boolean> = SettingsRepository.keepScreenOnDuringGameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val pushNotifications: StateFlow<Boolean> = SettingsRepository.pushNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailyReminders: StateFlow<Boolean> = SettingsRepository.dailyRemindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val faceIdTouchId: StateFlow<Boolean> = SettingsRepository.faceIdTouchIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dataSharing: StateFlow<Boolean> = SettingsRepository.dataSharingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val sundayTestTimeSettings: StateFlow<SundayTestTimeSettings> =
        SettingsRepository.sundayTestTimeSettingsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SundayTestTimeSettings())

    fun setShowDemoData(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setShowDemoData(value) }
    }

    fun setKeepScreenOnDuringGame(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setKeepScreenOnDuringGame(value) }
    }

    fun setPushNotifications(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setPushNotifications(value) }
    }

    fun setDailyReminders(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setDailyReminders(value) }
    }

    fun setFaceIdTouchId(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setFaceIdTouchId(value) }
    }

    fun setDataSharing(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setDataSharing(value) }
    }

    fun setSundayTestTimeEnabled(value: Boolean) {
        viewModelScope.launch { SettingsRepository.setSundayTestTimeEnabled(value) }
    }

    fun setSundayTestStartInMinutes(value: Int) {
        viewModelScope.launch { SettingsRepository.setSundayTestStartInMinutes(value) }
    }
}

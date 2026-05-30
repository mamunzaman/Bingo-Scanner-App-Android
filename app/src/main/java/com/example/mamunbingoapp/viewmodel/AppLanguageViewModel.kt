package com.example.mamunbingoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.SettingsRepository
import com.example.mamunbingoapp.data.localization.AppLanguage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class AppLanguageViewModel(application: Application) : AndroidViewModel(application) {
    val appLanguage: StateFlow<AppLanguage> = SettingsRepository.appLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.DEFAULT)

    init {
        viewModelScope.launch {
            SettingsRepository.ensureDefaultAppLanguage(Locale.getDefault())
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { SettingsRepository.setAppLanguage(language) }
    }
}

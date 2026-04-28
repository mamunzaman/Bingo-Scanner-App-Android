package com.example.mamunbingoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")
private val SHOW_DEMO_DATA = booleanPreferencesKey("show_demo_data")
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
private val KEEP_SCREEN_ON_DURING_GAME = booleanPreferencesKey("keep_screen_on_during_game")

object SettingsRepository {
    private var _context: Context? = null

    fun init(context: Context) {
        _context = context.applicationContext
    }

    private fun store(): DataStore<Preferences> = checkNotNull(_context) { "SettingsRepository not initialized. Call init(context) from MainActivity." }.settingsDataStore

    val showDemoDataFlow: Flow<Boolean>
        get() = store().data.map { it[SHOW_DEMO_DATA] ?: false }

    val onboardingCompletedFlow: Flow<Boolean>
        get() = store().data.map { it[ONBOARDING_COMPLETED] ?: false }

    val keepScreenOnDuringGameFlow: Flow<Boolean>
        get() = store().data.map { it[KEEP_SCREEN_ON_DURING_GAME] ?: true }

    suspend fun getOnboardingCompleted(): Boolean =
        store().data.first()[ONBOARDING_COMPLETED] ?: false

    suspend fun setOnboardingCompleted(value: Boolean) {
        store().edit { it[ONBOARDING_COMPLETED] = value }
    }

    suspend fun setShowDemoData(value: Boolean) {
        store().edit { it[SHOW_DEMO_DATA] = value }
    }

    suspend fun setKeepScreenOnDuringGame(value: Boolean) {
        store().edit { it[KEEP_SCREEN_ON_DURING_GAME] = value }
    }

    suspend fun clearAll() {
        store().edit { it.clear() }
    }
}

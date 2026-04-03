package com.example.mamunbingoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")
private val SHOW_DEMO_DATA = booleanPreferencesKey("show_demo_data")

object SettingsRepository {
    private var _context: Context? = null

    fun init(context: Context) {
        _context = context.applicationContext
    }

    private fun store(): DataStore<Preferences> = checkNotNull(_context) { "SettingsRepository not initialized. Call init(context) from MainActivity." }.settingsDataStore

    val showDemoDataFlow: Flow<Boolean>
        get() = store().data.map { it[SHOW_DEMO_DATA] ?: false }

    suspend fun setShowDemoData(value: Boolean) {
        store().edit { it[SHOW_DEMO_DATA] = value }
    }

    suspend fun clearAll() {
        store().edit { it.clear() }
    }
}

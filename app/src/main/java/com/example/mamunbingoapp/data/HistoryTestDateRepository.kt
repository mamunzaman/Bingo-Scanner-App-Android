package com.example.mamunbingoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.historyTestDateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "history_test_date_prefs",
)

object HistoryTestDateRepository {
    private var _context: Context? = null

    fun init(context: Context) {
        _context = context.applicationContext
    }

    private fun store(): DataStore<Preferences> = checkNotNull(_context) {
        "HistoryTestDateRepository not initialized. Call init(context) from MainActivity."
    }.historyTestDateDataStore

    private fun key(sessionId: String) = stringPreferencesKey("history_test_date_$sessionId")

    fun observeTestDate(sessionId: String): Flow<Long?> =
        store().data.map { prefs ->
            prefs[key(sessionId)]?.toLongOrNull()
        }

    fun observeAllTestDates(): Flow<Map<String, Long>> =
        store().data.map { prefs ->
            buildMap {
                prefs.asMap().forEach { (prefKey, value) ->
                    if (!prefKey.name.startsWith("history_test_date_")) return@forEach
                    val sessionId = prefKey.name.removePrefix("history_test_date_")
                    (value as? String)?.toLongOrNull()?.let { put(sessionId, it) }
                }
            }
        }

    suspend fun setTestDate(sessionId: String, dateMillis: Long) {
        store().edit { prefs ->
            prefs[key(sessionId)] = dateMillis.toString()
        }
    }

    suspend fun clearTestDate(sessionId: String) {
        store().edit { prefs ->
            prefs.remove(key(sessionId))
        }
    }
}

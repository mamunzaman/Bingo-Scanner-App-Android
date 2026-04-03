package com.example.mamunbingoapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
private val LIVE_HEADER_STYLE = stringPreferencesKey("live_header_style")

enum class LiveHeaderStyle(val value: String) {
    V1_CLEAN("V1_CLEAN"),
    V2_SPLIT("V2_SPLIT"),
    V3_BAND("V3_BAND"),
    V4_CLEAN_NEW("V4_CLEAN_NEW"),
    V5_SPLIT_NEW("V5_SPLIT_NEW"),
    V6_BAND_NEW("V6_BAND_NEW");
    companion object {
        fun from(value: String?) = when (value) {
            "V1_CLEAN", "CLASSIC" -> V1_CLEAN
            "V2_SPLIT", "SPLIT" -> V2_SPLIT
            "V3_BAND", "STRIP" -> V3_BAND
            "V4_CLEAN_NEW", "V4_CLAUDE_A" -> V4_CLEAN_NEW
            "V5_SPLIT_NEW", "V5_CLAUDE_B" -> V5_SPLIT_NEW
            "V6_BAND_NEW", "V6_CLAUDE_C" -> V6_BAND_NEW
            else -> entries.find { it.value == value } ?: V1_CLEAN
        }
    }
}

object UserPreferencesRepository {
    private var _context: Context? = null

    fun init(context: Context) {
        _context = context.applicationContext
    }

    private fun store(): DataStore<Preferences> =
        checkNotNull(_context) { "UserPreferencesRepository not initialized. Call init(context)." }.userPrefsDataStore

    val liveHeaderStyleFlow: Flow<LiveHeaderStyle>
        get() = store().data.map { LiveHeaderStyle.from(it[LIVE_HEADER_STYLE]) }

    suspend fun setLiveHeaderStyle(value: LiveHeaderStyle) {
        store().edit { it[LIVE_HEADER_STYLE] = value.value }
    }
}

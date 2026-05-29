package com.example.mamunbingoapp.data.projects

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mamunbingoapp.ui.projects.ProjectUiModel
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.projectsCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "projects_cache",
)

object ProjectsCache {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val cachePayloadKey = stringPreferencesKey("projects_payload")

    private var _context: Context? = null

    fun init(context: Context) {
        _context = context.applicationContext
    }

    suspend fun read(): ProjectsCachePayload? {
        val raw = store().data.first()[cachePayloadKey]?.trim().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { json.decodeFromString<ProjectsCachePayload>(raw) }.getOrNull()
    }

    suspend fun write(projects: List<ProjectUiModel>, updatedAtMillis: Long) {
        val payload = ProjectsCachePayload(
            updatedAtMillis = updatedAtMillis,
            projects = projects.map { it.toCachedDto() },
        )
        store().edit { prefs ->
            prefs[cachePayloadKey] = json.encodeToString(payload)
        }
    }

    private fun store(): DataStore<Preferences> = checkNotNull(_context) {
        "ProjectsCache not initialized. Call ProjectsRepository.init(context) from MainActivity."
    }.projectsCacheDataStore
}

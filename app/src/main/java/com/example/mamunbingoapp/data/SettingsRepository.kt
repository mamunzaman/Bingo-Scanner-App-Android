package com.example.mamunbingoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mamunbingoapp.data.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")
private val SHOW_DEMO_DATA = booleanPreferencesKey("show_demo_data")
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
private val KEEP_SCREEN_ON_DURING_GAME = booleanPreferencesKey("keep_screen_on_during_game")
private val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
private val DAILY_REMINDERS = booleanPreferencesKey("daily_reminders")
private val FACE_ID_TOUCH_ID = booleanPreferencesKey("face_id_touch_id")
private val DATA_SHARING = booleanPreferencesKey("data_sharing")
private val APP_LANGUAGE = stringPreferencesKey("app_language")
private val APP_LANGUAGE_USER_SET = booleanPreferencesKey("app_language_user_set")

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

    val pushNotificationsFlow: Flow<Boolean>
        get() = store().data.map { it[PUSH_NOTIFICATIONS] ?: true }

    val dailyRemindersFlow: Flow<Boolean>
        get() = store().data.map { it[DAILY_REMINDERS] ?: false }

    val faceIdTouchIdFlow: Flow<Boolean>
        get() = store().data.map { it[FACE_ID_TOUCH_ID] ?: true }

    val dataSharingFlow: Flow<Boolean>
        get() = store().data.map { it[DATA_SHARING] ?: false }

    val appLanguageFlow: Flow<AppLanguage>
        get() = store().data.map { prefs ->
            AppLanguage.fromCode(prefs[APP_LANGUAGE])
        }

    suspend fun ensureDefaultAppLanguage(deviceLocale: Locale = Locale.getDefault()) {
        val prefs = store().data.first()
        if (prefs[APP_LANGUAGE_USER_SET] == true || prefs[APP_LANGUAGE] != null) return
        store().edit { it[APP_LANGUAGE] = AppLanguage.fromDeviceLocale(deviceLocale).code }
    }

    suspend fun setAppLanguage(language: AppLanguage, userSelected: Boolean = true) {
        store().edit {
            it[APP_LANGUAGE] = language.code
            if (userSelected) it[APP_LANGUAGE_USER_SET] = true
        }
    }

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

    suspend fun setPushNotifications(value: Boolean) {
        store().edit { it[PUSH_NOTIFICATIONS] = value }
    }

    suspend fun setDailyReminders(value: Boolean) {
        store().edit { it[DAILY_REMINDERS] = value }
    }

    suspend fun setFaceIdTouchId(value: Boolean) {
        store().edit { it[FACE_ID_TOUCH_ID] = value }
    }

    suspend fun setDataSharing(value: Boolean) {
        store().edit { it[DATA_SHARING] = value }
    }

    suspend fun clearAll() {
        store().edit { it.clear() }
    }
}

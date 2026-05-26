package com.example.mamunbingoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.accountProfileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "account_profile_prefs",
)

private val KEY_FULL_NAME = stringPreferencesKey("full_name")
private val KEY_EMAIL = stringPreferencesKey("email")
private val KEY_PHONE = stringPreferencesKey("phone")
private val KEY_COUNTRY = stringPreferencesKey("country")
private val KEY_CITY = stringPreferencesKey("city")

object AccountRepository {
    private var _context: Context? = null

    fun init(context: Context) {
        _context = context.applicationContext
    }

    private fun store(): DataStore<Preferences> = checkNotNull(_context) {
        "AccountRepository not initialized. Call init(context) from MainActivity."
    }.accountProfileDataStore

    val profileFlow: Flow<AccountProfile>
        get() = store().data.map { prefs -> prefs.toProfile() }

    suspend fun saveProfile(profile: AccountProfile) {
        store().edit { prefs ->
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_EMAIL] = profile.email
            prefs[KEY_PHONE] = profile.phone
            prefs[KEY_COUNTRY] = profile.country
            prefs[KEY_CITY] = profile.city
        }
    }

    private fun Preferences.toProfile(): AccountProfile = AccountProfile(
        fullName = this[KEY_FULL_NAME].orEmpty(),
        email = this[KEY_EMAIL].orEmpty(),
        phone = this[KEY_PHONE].orEmpty(),
        country = this[KEY_COUNTRY].orEmpty(),
        city = this[KEY_CITY].orEmpty(),
    )
}

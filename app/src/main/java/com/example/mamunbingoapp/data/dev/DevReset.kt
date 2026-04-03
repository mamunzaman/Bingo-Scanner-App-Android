package com.example.mamunbingoapp.data.dev

import android.content.Context
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.SettingsRepository
import com.example.mamunbingoapp.data.ThemePreferences
import com.example.mamunbingoapp.data.db.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DevReset {

    suspend fun resetAll(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        DatabaseProvider.closeAndClear(appContext)
        SettingsRepository.clearAll()
        ThemePreferences.clearAll(appContext)
        HistoryRepository.clearDemoCaches()
    }
}

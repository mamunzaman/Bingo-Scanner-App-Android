package com.example.mamunbingoapp.data.localization

import java.util.Locale

data class AppLanguage(
    val code: String,
    val displayName: String,
) {
    companion object {
        val English = AppLanguage(code = "en", displayName = "English")
        val Deutsch = AppLanguage(code = "de", displayName = "Deutsch")

        val DEFAULT: AppLanguage = English

        val supported: List<AppLanguage> = listOf(English, Deutsch)

        fun fromCode(code: String?): AppLanguage {
            val normalized = code?.trim()?.lowercase(Locale.ROOT).orEmpty()
            return supported.firstOrNull { it.code == normalized } ?: DEFAULT
        }

        fun fromDeviceLocale(locale: Locale = Locale.getDefault()): AppLanguage =
            if (locale.language.lowercase(Locale.ROOT).startsWith("de")) Deutsch else DEFAULT
    }
}

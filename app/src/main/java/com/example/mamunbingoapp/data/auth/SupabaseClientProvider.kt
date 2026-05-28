package com.example.mamunbingoapp.data.auth

import android.content.Context
import android.util.Log
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import com.example.mamunbingoapp.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Lazy singleton Supabase client with **Auth**, **Postgrest** (profiles), and **Storage** (avatars).
 *
 * Call [ensureInitialized] from [android.app.Application] or [android.app.Activity] onCreate
 * before first [client] access so Android session storage can start safely.
 */
object SupabaseClientProvider {

    private const val TAG = "SupabaseClientProvider"
    private const val SUPABASE_INITIALIZER_CLASS =
        "io.github.jan.supabase.auth.SupabaseInitializer"

    const val MISSING_KEYS_MESSAGE: String =
        "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to the project " +
            "root local.properties (gitignored), then rebuild. See SupabaseAuthPlan."

    const val INVALID_URL_MESSAGE: String =
        "SUPABASE_URL is invalid. Use https://YOUR_PROJECT_REF.supabase.co in local.properties."

    @Volatile
    private var clientInitFailed: Boolean = false

    val client: SupabaseClient by lazy {
        createConfiguredClient()
    }

    fun ensureInitialized(context: Context) {
        val appContext = context.applicationContext
        runCatching {
            @Suppress("UNCHECKED_CAST")
            val initializerClass = Class.forName(SUPABASE_INITIALIZER_CLASS)
                as Class<out Initializer<Any>>
            AppInitializer.getInstance(appContext).initializeComponent(initializerClass)
            Log.d(TAG, "Supabase Android context initialized (fresh install safe)")
        }.onFailure { error ->
            Log.w(TAG, "Supabase Startup initializer skipped: ${error.message}")
        }
    }

    fun isConfigured(): Boolean {
        val url = BuildConfig.SUPABASE_URL.trim()
        val key = BuildConfig.SUPABASE_ANON_KEY.trim()
        return url.startsWith("https://") && url.contains("supabase") && key.isNotBlank()
    }

    fun configurationErrorMessage(): String? = when {
        BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank() ->
            MISSING_KEYS_MESSAGE
        !isConfigured() -> INVALID_URL_MESSAGE
        else -> null
    }

    fun requireConfigured() {
        check(isConfigured()) { configurationErrorMessage() ?: MISSING_KEYS_MESSAGE }
    }

    fun getClientOrNull(): SupabaseClient? {
        if (!isConfigured() || clientInitFailed) return null
        return runCatching { client }.getOrElse { error ->
            clientInitFailed = true
            Log.e(TAG, "Supabase client unavailable", error)
            null
        }
    }

    private fun createConfiguredClient(): SupabaseClient {
        requireConfigured()
        return try {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL.trim(),
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY.trim(),
            ) {
                install(Auth) {
                    scheme = SupabaseAuthDeepLink.SCHEME
                    host = SupabaseAuthDeepLink.HOST
                    defaultRedirectUrl = SupabaseAuthDeepLink.CALLBACK_URL
                }
                install(Postgrest)
                install(Storage)
            }.also {
                Log.d(TAG, "Supabase client created (Auth + Postgrest + Storage)")
            }
        } catch (error: Throwable) {
            clientInitFailed = true
            Log.e(TAG, "Failed to create Supabase client", error)
            throw error
        }
    }
}

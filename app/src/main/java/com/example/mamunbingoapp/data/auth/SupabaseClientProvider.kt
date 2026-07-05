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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import okhttp3.OkHttpClient

private const val SUPABASE_REQUEST_TIMEOUT_MS = 30_000L
private const val SUPABASE_CONNECT_TIMEOUT_MS = 15_000L
private const val SUPABASE_SOCKET_TIMEOUT_MS = 30_000L

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

    @Volatile
    private var supabaseHostLogged: Boolean = false

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

    /** DEBUG-only reachability probes before auth; does not log secrets. */
    suspend fun runDebugNetworkProbes() {
        if (!BuildConfig.DEBUG || !isConfigured()) return
        val baseUrl = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
        val anonKey = BuildConfig.SUPABASE_ANON_KEY.trim()
        probeGet(
            endpointName = "auth-health",
            url = "$baseUrl/auth/v1/health",
        )
        probeGet(
            endpointName = "rest-bingo-draws",
            url = "$baseUrl/rest/v1/bingo_draws?select=draw_date&limit=1",
            apiKeyOnly = true,
            anonKey = anonKey,
        )
    }

    private val debugProbeHttp: HttpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                preconfigured = createSupabaseOkHttpClient()
            }
        }
    }

    private suspend fun probeGet(
        endpointName: String,
        url: String,
        apiKeyOnly: Boolean = false,
        anonKey: String = "",
    ) {
        val startedAt = System.currentTimeMillis()
        try {
            val response = debugProbeHttp.get(url) {
                if (apiKeyOnly && anonKey.isNotBlank()) {
                    header("apikey", anonKey)
                }
            }
            val durationMs = System.currentTimeMillis() - startedAt
            val bodyPreview = runCatching { response.bodyAsText().take(120) }.getOrDefault("")
            Log.d(
                TAG,
                "Network probe [$endpointName]: duration=${durationMs}ms " +
                    "status=${response.status.value} bodyPreview=\"$bodyPreview\"",
            )
        } catch (error: Throwable) {
            val durationMs = System.currentTimeMillis() - startedAt
            Log.e(
                TAG,
                "Network probe [$endpointName]: duration=${durationMs}ms " +
                    "failed root=${error::class.java.name} msg=\"${error.message}\"\n" +
                    formatCauseChain(error),
            )
        }
    }

    private fun formatCauseChain(error: Throwable): String = buildString {
        var current: Throwable? = error
        var depth = 0
        while (current != null) {
            append("  cause[$depth]=").append(current::class.java.name)
            current.message?.takeIf { it.isNotBlank() }?.let { append(" msg=\"").append(it).append('"') }
            appendLine()
            current = current.cause
            depth++
        }
    }

    private fun createSupabaseOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(SUPABASE_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(SUPABASE_SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(SUPABASE_SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .build()

    private fun createConfiguredClient(): SupabaseClient {
        requireConfigured()
        logSupabaseHostOnce()
        return try {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL.trim(),
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY.trim(),
            ) {
                requestTimeout = 30.seconds
                httpEngine = OkHttp.create {
                    preconfigured = createSupabaseOkHttpClient()
                }
                install(Auth) {
                    scheme = SupabaseAuthDeepLink.SCHEME
                    host = SupabaseAuthDeepLink.HOST
                    defaultRedirectUrl = SupabaseAuthDeepLink.CALLBACK_URL
                }
                install(Postgrest)
                install(Storage)
            }.also {
                if (BuildConfig.DEBUG) {
                    Log.d(
                        TAG,
                        "Supabase client created (Auth + Postgrest + Storage); " +
                            "timeouts request=${SUPABASE_REQUEST_TIMEOUT_MS}ms " +
                            "connect=${SUPABASE_CONNECT_TIMEOUT_MS}ms " +
                            "socket=${SUPABASE_SOCKET_TIMEOUT_MS}ms",
                    )
                } else {
                    Log.d(TAG, "Supabase client created (Auth + Postgrest + Storage)")
                }
            }
        } catch (error: Throwable) {
            clientInitFailed = true
            Log.e(TAG, "Failed to create Supabase client", error)
            throw error
        }
    }

    private fun logSupabaseHostOnce() {
        if (!BuildConfig.DEBUG || supabaseHostLogged) return
        supabaseHostLogged = true
        val host = runCatching { java.net.URI(BuildConfig.SUPABASE_URL.trim()).host }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "unknown"
        Log.d(TAG, "Supabase host (debug): $host")
    }
}

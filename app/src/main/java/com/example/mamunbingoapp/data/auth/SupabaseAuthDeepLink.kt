package com.example.mamunbingoapp.data.auth

import android.content.Intent
import android.net.Uri

/**
 * Supabase Auth deep-link callback for email verification and password recovery.
 *
 * ## Supabase dashboard — Authentication → URL Configuration
 *
 * - **Site URL:** `https://bingo-hub.de`
 * - **Redirect URLs:** `https://bingo-hub.de`, `mamunbingo://auth/callback`
 *
 * Android app auth callbacks (verify/recovery) still use `mamunbingo://auth/callback`.
 *
 * ## Troubleshooting password-reset emails
 *
 * - Reset links use the same callback as sign-up, with `type=recovery` (query or URL fragment).
 * - Emails sent **before** dashboard URL changes may still contain old redirect values; request a **new**
 *   reset email after updating config and use only the newest message.
 */
object SupabaseAuthDeepLink {
    const val WEB_SITE_URL: String = "https://bingo-hub.de"
    const val SCHEME: String = "mamunbingo"
    const val HOST: String = "auth"
    const val PATH_PREFIX: String = "/callback"
    const val CALLBACK_URL: String = "$SCHEME://$HOST$PATH_PREFIX"

    private const val RECOVERY_TYPE = "recovery"

    fun matches(uri: Uri?): Boolean {
        if (uri == null) return false
        if (uri.scheme != SCHEME || uri.host != HOST) return false
        val path = uri.path.orEmpty()
        return path == PATH_PREFIX || path.startsWith(PATH_PREFIX)
    }

    fun isRecoveryLink(uri: Uri?): Boolean {
        if (uri == null || !matches(uri)) return false
        if (typeFromQuery(uri) == RECOVERY_TYPE) return true
        val fragment = uri.encodedFragment ?: uri.fragment.orEmpty()
        if (fragment.isBlank()) return false
        if (typeFromFragment(fragment) == RECOVERY_TYPE) return true
        return fragment.contains("type=$RECOVERY_TYPE", ignoreCase = true) ||
            fragment.contains("type%3D$RECOVERY_TYPE", ignoreCase = true)
    }

    /** Use [CALLBACK_URL] for Android app auth emails (verify + recovery). */
    fun validatedRedirectUrl(redirectUrl: String = CALLBACK_URL): String {
        require(redirectUrl == CALLBACK_URL) {
            "Auth redirect must be $CALLBACK_URL (got: $redirectUrl)"
        }
        return redirectUrl
    }

    private fun typeFromQuery(uri: Uri): String? =
        uri.getQueryParameter("type")?.lowercase()

    private fun typeFromFragment(fragment: String): String? =
        fragment.split('&').firstNotNullOfOrNull { part ->
            val separator = part.indexOf('=')
            if (separator <= 0) return@firstNotNullOfOrNull null
            val key = Uri.decode(part.substring(0, separator))
            if (!key.equals("type", ignoreCase = true)) return@firstNotNullOfOrNull null
            Uri.decode(part.substring(separator + 1)).lowercase()
        }

    fun matches(intent: Intent?): Boolean {
        if (intent?.action != Intent.ACTION_VIEW) return false
        return matches(intent.data)
    }

    fun isRecoveryIntent(intent: Intent?): Boolean {
        if (intent?.action != Intent.ACTION_VIEW) return false
        return isRecoveryLink(intent.data)
    }
}

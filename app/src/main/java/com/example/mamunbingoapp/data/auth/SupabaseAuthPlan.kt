package com.example.mamunbingoapp.data.auth

/**
 * Architecture plan for Supabase integration in MamunBingoApp.
 *
 * ## Supabase dashboard — URL Configuration (email verify + password reset)
 *
 * Authentication → URL Configuration:
 *
 * - **Site URL:** `mamunbingo://auth/callback`
 * - **Redirect URLs:** `mamunbingo://auth/callback`
 *
 * Email confirmation and password-reset links must use this redirect (not `http://localhost:3000`).
 * Reset emails include `type=recovery` on the same callback URL (query or fragment).
 *
 * **Troubleshooting:** Emails sent before URL config was updated may still open localhost.
 * Update dashboard URLs, then request a **new** password-reset email and tap only that message.
 * App logs `Password reset email redirectUrl=mamunbingo://auth/callback` when sending reset mail.
 *
 * ## App deep link (implemented)
 *
 * - Manifest intent-filter: `mamunbingo` / `auth` / `/callback`
 * - Auth plugin: matching `scheme` + `host` in [SupabaseClientProvider]
 * - [MainActivity] → [AuthRepository.handleAuthDeepLink] → `handleDeeplinks`
 * - [SupabaseAuthDeepLink.isRecoveryLink] accepts `type=recovery` (query or fragment)
 * - Recovery sets [AuthPasswordRecoveryState.PendingSetNewPassword]; NavGraph routes to `auth/forgot`
 * - [ForgotPasswordScreen] step 2: new + confirm password → [AuthRepository.updatePasswordAfterRecovery]
 * - On success: clear recovery → main (signed in) or login
 * - Session restore via `sessionStatus`; NavGraph routes SignedIn → main (except recovery pending)
 *
 * ## Data boundary (non-negotiable)
 *
 * **Supabase (cloud)** — user identity and access only. **Device (local)** — all Bingo data in Room.
 *
 * @see com.example.mamunbingoapp.data.AccountProfile Local profile aligned with future `profiles` table
 */
object SupabaseAuthPlan {
    const val PHASE_CONFIG = "buildconfig_keys" // complete
    const val PHASE_CLIENT = "supabase_client_auth_only" // complete
    const val PHASE_SESSION = "auth_repository" // complete
    const val PHASE_GATE = "nav_access_gate" // complete
    const val PHASE_EMAIL_VERIFY = "auth_deeplink_callback" // complete — mamunbingo://auth/callback
    const val PHASE_PASSWORD_RESET = "auth_recovery_deeplink" // complete — type=recovery + set password UI
    const val PHASE_PROFILE = "profiles_optional" // TODO
    const val PHASE_ACCESS = "subscription_access_status" // TODO
}

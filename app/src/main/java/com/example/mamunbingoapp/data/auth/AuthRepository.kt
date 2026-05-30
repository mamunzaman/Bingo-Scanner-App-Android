package com.example.mamunbingoapp.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import com.example.mamunbingoapp.R
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Supabase Auth session layer only. Bingo tickets/history remain in Room.
 *
 * Call [startup] from [android.app.Activity.onCreate] before relying on [authState].
 */
object AuthRepository {

    private const val TAG = "AuthRepository"
    private const val SESSION_RESTORE_TIMEOUT_MS = 12_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authActionError = MutableStateFlow<String?>(null)
    val authActionError: StateFlow<String?> = _authActionError.asStateFlow()

    private val _authRecoveryHint = MutableStateFlow<String?>(null)
    val authRecoveryHint: StateFlow<String?> = _authRecoveryHint.asStateFlow()

    private val _passwordRecovery = MutableStateFlow<AuthPasswordRecoveryState>(
        AuthPasswordRecoveryState.Idle,
    )
    val passwordRecovery: StateFlow<AuthPasswordRecoveryState> = _passwordRecovery.asStateFlow()

    private val _authActionInProgress = MutableStateFlow(false)
    val authActionInProgress: StateFlow<Boolean> = _authActionInProgress.asStateFlow()

    private var lastHandledAuthDeepLink: String? = null
    private var pendingRecoveryFromDeepLink: Boolean = false
    private var sessionObserverStarted: Boolean = false
    private lateinit var appContext: Context

    private fun str(@StringRes id: Int, vararg formatArgs: Any): String =
        appContext.getString(id, *formatArgs)

    fun startup(context: Context) {
        appContext = context.applicationContext
        SupabaseClientProvider.ensureInitialized(context)
        if (sessionObserverStarted) return
        sessionObserverStarted = true

        Log.d(TAG, "Auth startup: observing session (no stored session expected on fresh install)")

        SupabaseClientProvider.configurationErrorMessage()?.let { message ->
            _authState.value = AuthState.SignedOut
            _authActionError.value = message
            Log.w(TAG, "Auth startup: Supabase not configured — routing signed out")
            return
        }

        observeSessionStatus()
    }

    fun clearAuthActionError() {
        _authActionError.value = null
    }

    fun clearAuthRecoveryHint() {
        _authRecoveryHint.value = null
    }

    fun clearPasswordRecovery() {
        _passwordRecovery.value = AuthPasswordRecoveryState.Idle
        clearAuthRecoveryHint()
    }

    fun clearRecoveryState(
        clearError: Boolean = true,
        clearHint: Boolean = true,
        clearHandledDeepLink: Boolean = false,
        reason: String = "manual",
    ) {
        pendingRecoveryFromDeepLink = false
        _passwordRecovery.value = AuthPasswordRecoveryState.Idle
        if (clearHint) _authRecoveryHint.value = null
        if (clearError) _authActionError.value = null
        if (clearHandledDeepLink) lastHandledAuthDeepLink = null
        Log.d(
            TAG,
            "Recovery state cleared: reason=$reason clearError=$clearError clearHint=$clearHint clearHandledDeepLink=$clearHandledDeepLink",
        )
    }

    private fun enterPendingPasswordRecovery(email: String?) {
        _passwordRecovery.value = AuthPasswordRecoveryState.PendingSetNewPassword(email)
        _authRecoveryHint.value = str(R.string.auth_recovery_reset_accepted)
        Log.d(TAG, "Recovery state entered: pending set-new-password email=$email")
    }

    fun isPasswordRecoveryPending(): Boolean =
        _passwordRecovery.value is AuthPasswordRecoveryState.PendingSetNewPassword

    suspend fun signInWithEmail(email: String, password: String): Boolean {
        clearRecoveryState(reason = "sign_in")
        clearAuthActionError()
        val success = runAuthAction {
            SupabaseClientProvider.client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
        }
        if (!success) return false
        syncAuthStateFromCurrentSession()
        return if (_authState.value is AuthState.SignedIn) {
            clearAuthActionError()
            true
        } else {
            false
        }
    }

    suspend fun signUpWithEmail(email: String, password: String) {
        clearRecoveryState(reason = "sign_up")
        clearAuthActionError()
        val success = runAuthAction {
            SupabaseClientProvider.client.auth.signUpWith(
                Email,
                redirectUrl = SupabaseAuthDeepLink.validatedRedirectUrl(),
            ) {
                this.email = email.trim()
                this.password = password
            }
        }
        if (success && SupabaseClientProvider.client.auth.currentSessionOrNull() == null) {
            _authRecoveryHint.value = null
            _authActionError.value = str(R.string.auth_signup_confirm_email)
        }
    }

    suspend fun requestPasswordResetEmail(email: String) {
        clearRecoveryState(
            clearError = true,
            clearHint = true,
            clearHandledDeepLink = true,
            reason = "request_reset_email",
        )
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains("@")) {
            _authActionError.value = str(R.string.auth_error_valid_email)
            return
        }
        val redirectTo = SupabaseAuthDeepLink.validatedRedirectUrl()
        Log.d(TAG, "Password reset email redirect_to=$redirectTo")
        val success = runAuthAction {
            SupabaseClientProvider.client.auth.resetPasswordForEmail(
                email = trimmed,
                redirectUrl = redirectTo,
            )
        }
        if (success) {
            _authRecoveryHint.value = str(R.string.auth_reset_email_sent)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Boolean {
        if (_authState.value !is AuthState.SignedIn) {
            _authActionError.value = str(R.string.auth_error_sign_in_change_password)
            return false
        }
        clearAuthActionError()
        val success = runAuthAction {
            SupabaseClientProvider.client.auth.updateUser {
                password = newPassword
                this.currentPassword = currentPassword
            }
        }
        if (success) {
            clearAuthActionError()
        }
        return success
    }

    fun currentSignedInEmail(): String? =
        SupabaseClientProvider.getClientOrNull()?.auth?.currentSessionOrNull()?.user?.email

    fun currentSignedInUserId(): String? =
        SupabaseClientProvider.getClientOrNull()?.auth?.currentSessionOrNull()?.user?.id

    fun currentDisplayName(): String? {
        val raw = SupabaseClientProvider.getClientOrNull()
            ?.auth
            ?.currentSessionOrNull()
            ?.user
            ?.userMetadata
            ?.get("display_name")
            ?.toString()
            ?.trim()
            ?.trim('"')
        return raw?.takeIf { it.isNotBlank() }
    }

    suspend fun updateDisplayName(displayName: String): Boolean {
        if (_authState.value !is AuthState.SignedIn) {
            _authActionError.value = str(R.string.auth_error_sign_in_update_profile)
            return false
        }
        clearAuthActionError()
        val success = runAuthAction {
            SupabaseClientProvider.client.auth.updateUser {
                data {
                    put("display_name", JsonPrimitive(displayName.trim()))
                }
            }
        }
        if (success) {
            clearAuthActionError()
        }
        // TODO: when account status/subscription is introduced, mirror profile data in a dedicated `profiles` table.
        return success
    }

    suspend fun updateEmail(newEmail: String): Boolean {
        val email = newEmail.trim()
        if (email.isBlank() || !email.contains("@")) {
            _authActionError.value = str(R.string.auth_error_valid_email)
            return false
        }
        if (_authState.value !is AuthState.SignedIn) {
            _authActionError.value = str(R.string.auth_error_sign_in_update_email)
            return false
        }
        clearAuthActionError()
        val success = runAuthAction {
            SupabaseClientProvider.client.auth.updateUser {
                this.email = email
            }
        }
        if (success) {
            _authRecoveryHint.value = str(R.string.auth_recovery_email_confirm_change)
        }
        // TODO: depending Supabase dashboard policy, confirmation may be required on both old and new email.
        return success
    }

    suspend fun updatePasswordAfterRecovery(newPassword: String): Boolean {
        if (newPassword.isBlank()) {
            _authActionError.value = str(R.string.auth_error_enter_new_password)
            return false
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            _authActionError.value = str(R.string.change_password_error_min_length)
            return false
        }
        clearAuthActionError()
        val success = runAuthAction {
            SupabaseClientProvider.client.auth.updateUser {
                password = newPassword
            }
        }
        if (success) {
            clearPasswordRecovery()
            _authRecoveryHint.value = str(R.string.auth_recovery_password_updated)
        }
        return success
    }

    fun handleAuthDeepLink(intent: Intent?): Boolean {
        if (intent == null) {
            Log.d(TAG, "Auth deep link: null intent, ignored")
            return false
        }
        if (!SupabaseAuthDeepLink.matches(intent)) return false

        val uri = intent.data
        val isRecovery = SupabaseAuthDeepLink.isRecoveryLink(uri)
        val uriKey = uri?.toString().orEmpty()
        if (uriKey.isNotBlank() && uriKey == lastHandledAuthDeepLink) return true
        lastHandledAuthDeepLink = uriKey.takeIf { it.isNotBlank() }

        Log.d(
            TAG,
            "Auth deep link received: recovery=$isRecovery uri=$uri callback=${SupabaseAuthDeepLink.CALLBACK_URL}",
        )

        if (!SupabaseClientProvider.isConfigured()) {
            _authActionError.value =
                SupabaseClientProvider.configurationErrorMessage()
                    ?: SupabaseClientProvider.MISSING_KEYS_MESSAGE
            return true
        }

        clearAuthActionError()
        if (isRecovery) {
            pendingRecoveryFromDeepLink = true
            Log.d(TAG, "Recovery state set: pendingRecoveryFromDeepLink=true")
            // Enter recovery UI immediately so auth gate can't bounce to login/main first.
            enterPendingPasswordRecovery(email = null)
        } else {
            pendingRecoveryFromDeepLink = false
            clearRecoveryState(
                clearError = false,
                clearHint = true,
                clearHandledDeepLink = false,
                reason = "non_recovery_deeplink",
            )
        }
        _authActionInProgress.value = true
        val result = runCatching {
            val client = SupabaseClientProvider.getClientOrNull()
                ?: error(SupabaseClientProvider.configurationErrorMessage() ?: "Auth unavailable")
            client.handleDeeplinks(intent)
        }
        _authActionInProgress.value = false

        result.onFailure { error ->
            Log.w(TAG, "Auth deep link failed", error)
            _authActionError.value = mapAuthError(error)
            pendingRecoveryFromDeepLink = false
            if (isRecovery) {
                clearRecoveryState(
                    clearError = false,
                    clearHint = true,
                    clearHandledDeepLink = true,
                    reason = "recovery_deeplink_failed",
                )
            }
        }.onSuccess {
            Log.d(TAG, "Recovery link handled: success isRecovery=$isRecovery")
            if (isRecovery) {
                scope.launch { finalizePasswordRecoveryDeepLink() }
            } else {
                pendingRecoveryFromDeepLink = false
                clearRecoveryState(
                    clearError = false,
                    clearHint = true,
                    clearHandledDeepLink = false,
                    reason = "non_recovery_deeplink_success",
                )
            }
        }
        return true
    }

    private suspend fun finalizePasswordRecoveryDeepLink() {
        if (!pendingRecoveryFromDeepLink) return
        val auth = SupabaseClientProvider.getClientOrNull()?.auth ?: run {
            pendingRecoveryFromDeepLink = false
            clearPasswordRecovery()
            _authActionError.value = str(R.string.auth_error_reset_unavailable)
            return
        }
        repeat(5) {
            if (auth.currentSessionOrNull() != null) {
                onPasswordRecoveryDeepLinkHandled()
                pendingRecoveryFromDeepLink = false
                return
            }
            delay(200)
        }
        pendingRecoveryFromDeepLink = false
        onPasswordRecoveryDeepLinkHandled()
    }

    private fun onPasswordRecoveryDeepLinkHandled() {
        val session = runCatching {
            SupabaseClientProvider.getClientOrNull()?.auth?.currentSessionOrNull()
        }.getOrNull()
        Log.d(TAG, "Recovery session detected: hasSession=${session != null}")
        val email = session?.user?.email
        if (session != null && !email.isNullOrBlank()) {
            enterPendingPasswordRecovery(email = email)
            return
        }
        if (session != null) {
            enterPendingPasswordRecovery(email = null)
            return
        }
        clearRecoveryState(
            clearError = false,
            clearHint = true,
            clearHandledDeepLink = true,
            reason = "invalid_recovery_link",
        )
        _authActionError.value = str(R.string.auth_error_reset_link_invalid)
    }

    suspend fun signOut(): Boolean {
        clearRecoveryState(
            clearError = true,
            clearHint = true,
            clearHandledDeepLink = true,
            reason = "sign_out",
        )
        _authActionInProgress.value = true
        _authState.value = AuthState.SignedOut
        Log.d(TAG, "Auth sign-out: local state cleared")
        if (!SupabaseClientProvider.isConfigured()) {
            _authActionInProgress.value = false
            return true
        }
        val success = runCatching {
            SupabaseClientProvider.getClientOrNull()?.auth?.signOut()
        }.onFailure { error ->
            Log.w(TAG, "Supabase sign-out failed (local session already cleared)", error)
            _authActionError.value = mapAuthError(error)
        }.isSuccess
        _authActionInProgress.value = false
        return success
    }

    private fun observeSessionStatus() {
        scope.launch {
            val completed = withContext(Dispatchers.Default) {
                withTimeoutOrNull(SESSION_RESTORE_TIMEOUT_MS) {
                    runCatching {
                        val auth = SupabaseClientProvider.getClientOrNull()?.auth
                            ?: error("Supabase Auth client unavailable")
                        auth.sessionStatus.collect { status ->
                            applySessionStatus(status)
                        }
                    }.onFailure { error ->
                        Log.e(TAG, "Session observer failed", error)
                        withContext(Dispatchers.Main) {
                            applySessionFailure(mapAuthError(error))
                        }
                    }
                }
            }
            if (completed == null && _authState.value is AuthState.Loading) {
                Log.w(TAG, "Session restore timed out; treating as signed out (fresh install safe)")
                withContext(Dispatchers.Main) {
                    applySessionFailure(null)
                }
            }
        }
    }

    private suspend fun applySessionStatus(status: SessionStatus) {
        withContext(Dispatchers.Main) {
            runCatching {
                val mapped = status.toAuthState()
                if (_authActionInProgress.value &&
                    (mapped is AuthState.SignedOut || mapped is AuthState.SignedIn)
                ) {
                    return@runCatching
                }
                if (mapped is AuthState.SignedOut && status is SessionStatus.NotAuthenticated) {
                    Log.d(TAG, "Auth session: signed out (no stored session)")
                }
                _authState.value = mapped
                if (pendingRecoveryFromDeepLink && mapped is AuthState.SignedIn) {
                    onPasswordRecoveryDeepLinkHandled()
                    pendingRecoveryFromDeepLink = false
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to map session status", error)
                applySessionFailure(mapAuthError(error))
            }
        }
    }

    private fun applySessionFailure(message: String?) {
        _authState.value = AuthState.SignedOut
        if (!message.isNullOrBlank()) {
            _authActionError.value = message
        }
    }

    /** Apply [AuthState.SignedIn] from [currentSessionOrNull] without waiting for [sessionStatus] emit. */
    private fun syncAuthStateFromCurrentSession() {
        val session = runCatching {
            SupabaseClientProvider.getClientOrNull()?.auth?.currentSessionOrNull()
        }.getOrNull()
        val user = session?.user
        val userId = user?.id.orEmpty()
        if (userId.isBlank()) {
            Log.d(TAG, "Auth session sync: no active session")
            return
        }
        _authState.value = AuthState.SignedIn(userId = userId, email = user?.email)
        Log.d(TAG, "Auth session sync: signed in userId=$userId")
    }

    private suspend fun runAuthAction(block: suspend () -> Unit): Boolean {
        _authActionInProgress.value = true
        val success = runCatching {
            SupabaseClientProvider.requireConfigured()
            val client = SupabaseClientProvider.getClientOrNull()
                ?: error(SupabaseClientProvider.configurationErrorMessage() ?: "Auth unavailable")
            block()
        }.onFailure { error ->
            _authActionError.value = mapAuthError(error)
        }.isSuccess
        _authActionInProgress.value = false
        return success
    }

    private fun SessionStatus.toAuthState(): AuthState = when (this) {
        SessionStatus.Initializing -> AuthState.Loading
        is SessionStatus.Authenticated -> {
            val user = session.user
            val userId = user?.id.orEmpty()
            if (userId.isBlank()) {
                AuthState.SignedOut
            } else {
                AuthState.SignedIn(userId = userId, email = user?.email)
            }
        }
        is SessionStatus.NotAuthenticated -> AuthState.SignedOut
        is SessionStatus.RefreshFailure -> {
            _authActionError.value = str(R.string.auth_error_session_expired)
            AuthState.SignedOut
        }
    }

    private fun mapAuthError(error: Throwable): String {
        Log.w(TAG, "Auth action failed", error)
        val message = collectAuthErrorText(error)
        if (error is IllegalStateException && isSafeUserFacingAuthMessage(message)) {
            return message.lineSequence().first().trim()
        }
        if (error is UnknownHostException || error is IOException) {
            return str(R.string.auth_error_network)
        }
        return mapAuthErrorMessage(message)
    }

    private fun collectAuthErrorText(error: Throwable): String = buildString {
        var current: Throwable? = error
        while (current != null) {
            current.message?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
            current = current.cause
        }
    }.trim()

    private fun mapAuthErrorMessage(message: String): String = when {
            message.contains("over_email_send_rate_limit", ignoreCase = true) ->
                str(R.string.auth_error_rate_limit)
            message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("Failed to connect", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) ||
                (message.contains("Network", ignoreCase = true) &&
                    !message.contains("error_code", ignoreCase = true)) ->
                str(R.string.auth_error_network)
            message.contains("Invalid login credentials", ignoreCase = true) ->
                str(R.string.auth_error_invalid_credentials)
            message.contains("User already registered", ignoreCase = true) ->
                str(R.string.auth_error_user_exists)
            message.contains("Password should be at least", ignoreCase = true) ->
                str(R.string.change_password_error_min_length)
            message.contains("Unable to validate email address", ignoreCase = true) ->
                str(R.string.auth_error_valid_email)
            message.contains("otp", ignoreCase = true) ||
                message.contains("email link", ignoreCase = true) ||
                message.contains("recovery", ignoreCase = true) ||
                message.contains("verification", ignoreCase = true) ->
                str(R.string.auth_error_link_invalid)
            isSafeUserFacingAuthMessage(message) -> message.lineSequence().first().trim()
            else -> str(R.string.auth_error_auth_failed)
    }

    private fun isSafeUserFacingAuthMessage(message: String): Boolean {
        if (message.isBlank() || message.length > 240) return false
        val lower = message.lowercase()
        if (lower.contains("http://") || lower.contains("https://")) return false
        if (lower.contains("supabase.co") || lower.contains("supabase.com")) return false
        if (lower.contains("authorization") || lower.contains("apikey") || lower.contains("bearer ")) {
            return false
        }
        if (lower.contains("header") && lower.contains(":")) return false
        if (lower.contains("ktor") || lower.contains("http request") || lower.contains("httpresponse")) {
            return false
        }
        if (message.contains("{") && message.contains("error_code")) return false
        if (message.contains("over_") && message.contains("_rate_limit")) return false
        return true
    }

    private const val MIN_PASSWORD_LENGTH = 8
}

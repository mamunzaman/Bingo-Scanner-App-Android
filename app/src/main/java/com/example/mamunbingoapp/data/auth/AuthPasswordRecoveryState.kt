package com.example.mamunbingoapp.data.auth

/**
 * Password-reset deep link outcome. UI observes [AuthRepository.passwordRecovery] for step 2.
 */
sealed interface AuthPasswordRecoveryState {
    data object Idle : AuthPasswordRecoveryState

    data class PendingSetNewPassword(
        val email: String?,
    ) : AuthPasswordRecoveryState
}

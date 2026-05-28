package com.example.mamunbingoapp.data.auth

sealed interface AuthState {
    /** Session restore or initial Supabase auth read in progress. */
    data object Loading : AuthState

    data object SignedOut : AuthState

    data class SignedIn(
        val userId: String,
        val email: String?,
    ) : AuthState

    /** Legacy; prefer [AuthRepository.authActionError] for transient failures. */
    data class Error(val message: String) : AuthState
}

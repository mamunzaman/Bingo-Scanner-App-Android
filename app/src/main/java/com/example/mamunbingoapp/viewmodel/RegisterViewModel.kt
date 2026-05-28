package com.example.mamunbingoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.data.auth.AuthRepository
import com.example.mamunbingoapp.data.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    val authState: StateFlow<AuthState> = AuthRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    val isLoading: StateFlow<Boolean> = combine(
        AuthRepository.authState,
        AuthRepository.authActionInProgress,
    ) { state, inProgress ->
        state is AuthState.Loading || inProgress
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError = _validationError.asStateFlow()

    val authActionError: StateFlow<String?> = AuthRepository.authActionError
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun signUp(email: String, password: String) {
        val trimmedEmail = email.trim()
        when {
            trimmedEmail.isBlank() || !trimmedEmail.contains("@") -> {
                _validationError.value = "Enter a valid email address."
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                _validationError.value = "Password must be at least $MIN_PASSWORD_LENGTH characters."
            }
            else -> {
                _validationError.value = null
                viewModelScope.launch {
                    AuthRepository.signUpWithEmail(trimmedEmail, password)
                }
            }
        }
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}

package com.example.mamunbingoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.AccountProfile
import com.example.mamunbingoapp.data.AccountRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountFormUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val country: String = "",
    val city: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val submittedOnce: Boolean = false,
)

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<Application>()
    val profile: StateFlow<AccountProfile> = AccountRepository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountProfile())

    private val _uiState = MutableStateFlow(AccountFormUiState())
    val uiState = _uiState.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    init {
        viewModelScope.launch {
            val profile = AccountRepository.profileFlow.first()
            _uiState.update { state ->
                state.copy(
                    fullName = profile.fullName,
                    email = profile.email,
                    phone = profile.phone,
                    country = profile.country,
                    city = profile.city,
                )
            }
        }
    }

    fun updateFullName(value: String) {
        _uiState.update { state ->
            state.copy(
                fullName = value,
                fullNameError = if (state.submittedOnce && value.isBlank()) {
                    "Full name is required"
                } else {
                    null
                },
            )
        }
    }

    fun updateEmail(value: String) {
        _uiState.update { state ->
            state.copy(
                email = value,
                emailError = if (state.submittedOnce && !value.contains("@")) {
                    "Enter a valid email address"
                } else {
                    null
                },
            )
        }
    }

    fun updatePhone(value: String) {
        _uiState.update { state -> state.copy(phone = value) }
    }

    fun updateCountry(value: String) {
        _uiState.update { state -> state.copy(country = value) }
    }

    fun updateCity(value: String) {
        _uiState.update { state -> state.copy(city = value) }
    }

    fun saveProfile() {
        _uiState.update { it.copy(submittedOnce = true) }
        if (!validate()) return

        val profile = currentProfile()
        viewModelScope.launch {
            AccountRepository.saveProfile(profile)
            // TODO: upsert to Supabase profiles table when backend is wired
            _snackbarMessages.emit(app.getString(R.string.account_saved_snackbar))
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        val nameBlank = state.fullName.isBlank()
        val emailInvalid = !state.email.contains("@")
        _uiState.update {
            it.copy(
                fullNameError = if (nameBlank) app.getString(R.string.account_error_full_name_required) else null,
                emailError = if (emailInvalid) app.getString(R.string.account_error_valid_email) else null,
            )
        }
        return !nameBlank && !emailInvalid
    }

    private fun currentProfile(): AccountProfile {
        val state = _uiState.value
        return AccountProfile(
            fullName = state.fullName,
            email = state.email,
            phone = state.phone,
            country = state.country,
            city = state.city,
        )
    }
}

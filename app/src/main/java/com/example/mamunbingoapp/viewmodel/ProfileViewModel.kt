package com.example.mamunbingoapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.auth.AuthRepository
import com.example.mamunbingoapp.data.auth.AuthState
import com.example.mamunbingoapp.data.profile.ProfileDto
import com.example.mamunbingoapp.data.profile.ProfileRepository
import com.example.mamunbingoapp.ui.components.AppAuthMessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileFormState(
    val fullName: String = "",
    val secondaryEmail: String = "",
    val phone: String = "",
    val country: String = "",
    val region: String = "",
    val city: String = "",
    val postalCode: String = "",
    val streetAddress: String = "",
    val apartmentOrHouseNo: String = "",
    val bio: String = "",
    val language: String = "",
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<Application>()

    private val _profileLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = combine(
        AuthRepository.authActionInProgress,
        _profileLoading,
    ) { authInProgress, profileInProgress ->
        authInProgress || profileInProgress
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _uiMessageType = MutableStateFlow(AppAuthMessageType.Error)
    val uiMessageType: StateFlow<AppAuthMessageType> = _uiMessageType.asStateFlow()

    private val _formResetKey = MutableStateFlow(0)
    val formResetKey: StateFlow<Int> = _formResetKey.asStateFlow()

    private val _authEmail = MutableStateFlow<String?>(null)
    val authEmail: StateFlow<String?> = _authEmail.asStateFlow()

    private val _authUserId = MutableStateFlow<String?>(null)
    val authUserId: StateFlow<String?> = _authUserId.asStateFlow()

    private val _displayNameInput = MutableStateFlow("")
    val displayNameInput: StateFlow<String> = _displayNameInput.asStateFlow()

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _profileForm = MutableStateFlow(ProfileFormState())
    val profileForm: StateFlow<ProfileFormState> = _profileForm.asStateFlow()

    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl.asStateFlow()

    private val _isProfileRefreshing = MutableStateFlow(false)
    val isProfileRefreshing: StateFlow<Boolean> = _isProfileRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            syncAuthUserFields()
            reloadProfileFromRemote()
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        _uiMessage.value = null
        when {
            currentPassword.isBlank() -> showError(app.getString(R.string.profile_error_enter_current_password))
            newPassword.length < MIN_PASSWORD_LENGTH ->
                showError(app.getString(R.string.change_password_error_min_length))
            confirmPassword.isBlank() -> showError(app.getString(R.string.change_password_error_confirm))
            newPassword != confirmPassword -> showError(app.getString(R.string.change_password_error_mismatch))
            else -> viewModelScope.launch {
                AuthRepository.clearAuthActionError()
                val success = AuthRepository.changePassword(currentPassword, newPassword)
                if (success) {
                    _uiMessageType.value = AppAuthMessageType.Success
                    _uiMessage.value = app.getString(R.string.profile_success_password_updated)
                    _formResetKey.value += 1
                } else {
                    showError(
                        AuthRepository.authActionError.value
                            ?: app.getString(R.string.profile_error_password_update_failed),
                    )
                }
            }
        }
    }

    fun updateDisplayNameInput(value: String) {
        _displayNameInput.value = value
    }

    fun saveDisplayName() {
        val name = _displayNameInput.value.trim()
        if (name.isBlank()) {
            showError(app.getString(R.string.profile_error_display_name_required))
            return
        }
        viewModelScope.launch {
            _profileLoading.value = true
            val userId = AuthRepository.currentSignedInUserId()
            if (userId.isNullOrBlank()) {
                showError(app.getString(R.string.profile_error_sign_in_required))
                _profileLoading.value = false
                return@launch
            }
            val profileResult = ProfileRepository.updateProfile(
                buildProfileDto(userId).copy(displayName = name),
            )
            if (profileResult.isFailure) {
                showError(profileResult.exceptionOrNull()?.message ?: app.getString(R.string.profile_error_update_profile_failed))
                _profileLoading.value = false
                return@launch
            }
            val authSuccess = AuthRepository.updateDisplayName(name)
            _profileLoading.value = false
            if (authSuccess) {
                _uiMessageType.value = AppAuthMessageType.Success
                _uiMessage.value = app.getString(R.string.profile_success_display_name_saved)
                applyProfileDto(profileResult.getOrThrow())
            } else {
                showError(AuthRepository.authActionError.value ?: app.getString(R.string.profile_error_update_display_name_failed))
            }
        }
    }

    fun updateEmailInput(value: String) {
        _emailInput.value = value
    }

    fun saveEmail() {
        val email = _emailInput.value.trim()
        if (email.isBlank() || !email.contains("@")) {
            showError(app.getString(R.string.profile_error_valid_email))
            return
        }
        viewModelScope.launch {
            val success = AuthRepository.updateEmail(email)
            if (success) {
                _uiMessageType.value = AppAuthMessageType.Info
                _uiMessage.value = app.getString(R.string.profile_success_check_email_confirm)
                refreshAuthProfile()
            } else {
                showError(AuthRepository.authActionError.value ?: app.getString(R.string.profile_error_update_email_failed))
            }
        }
    }

    fun updateFullName(value: String) {
        _profileForm.value = _profileForm.value.copy(fullName = value)
    }

    fun updateSecondaryEmail(value: String) {
        _profileForm.value = _profileForm.value.copy(secondaryEmail = value)
    }

    fun updatePhone(value: String) {
        _profileForm.value = _profileForm.value.copy(phone = value)
    }

    fun updateCountry(value: String) {
        _profileForm.value = _profileForm.value.copy(country = value)
    }

    fun updateRegion(value: String) {
        _profileForm.value = _profileForm.value.copy(region = value)
    }

    fun updateCity(value: String) {
        _profileForm.value = _profileForm.value.copy(city = value)
    }

    fun updatePostalCode(value: String) {
        _profileForm.value = _profileForm.value.copy(postalCode = value)
    }

    fun updateStreetAddress(value: String) {
        _profileForm.value = _profileForm.value.copy(streetAddress = value)
    }

    fun updateApartmentOrHouseNo(value: String) {
        _profileForm.value = _profileForm.value.copy(apartmentOrHouseNo = value)
    }

    fun updateBio(value: String) {
        _profileForm.value = _profileForm.value.copy(bio = value)
    }

    fun uploadAvatar(
        context: Context,
        imageUri: Uri,
        cachedUserId: String? = null,
    ) {
        viewModelScope.launch {
            _profileLoading.value = true
            refreshAuthFieldsForAvatarUpload(cachedUserId)
            val resolvedCachedUserId = cachedUserId?.trim().orEmpty().takeIf { it.isNotBlank() }
                ?: _authUserId.value?.trim().orEmpty().takeIf { it.isNotBlank() }
            ProfileRepository.uploadAvatarFromUri(
                context = context,
                imageUri = imageUri,
                cachedUserId = resolvedCachedUserId,
            )
                .onSuccess { dto ->
                    applyProfileDto(dto)
                    _uiMessageType.value = AppAuthMessageType.Success
                    _uiMessage.value = app.getString(R.string.profile_success_photo_updated)
                }
                .onFailure { error ->
                    showError(error.message ?: app.getString(R.string.profile_error_photo_upload_failed))
                }
            _profileLoading.value = false
        }
    }

    private fun refreshAuthFieldsForAvatarUpload(cachedUserId: String? = null) {
        val sessionUserId = AuthRepository.currentSignedInUserId()?.trim().orEmpty()
            .takeIf { it.isNotBlank() }
        val resolvedUserId = sessionUserId
            ?: cachedUserId?.trim().orEmpty()?.takeIf { it.isNotBlank() }
            ?: _authUserId.value?.trim().orEmpty()?.takeIf { it.isNotBlank() }
            ?: (AuthRepository.authState.value as? AuthState.SignedIn)?.userId?.trim().orEmpty()
                ?.takeIf { it.isNotBlank() }
        _authUserId.value = resolvedUserId
        if (_authEmail.value.isNullOrBlank()) {
            _authEmail.value = AuthRepository.currentSignedInEmail()
        }
    }

    fun deleteAvatar(context: Context) {
        viewModelScope.launch {
            _profileLoading.value = true
            val previousUrl = _avatarUrl.value
            ProfileRepository.deleteAvatar()
                .onSuccess { dto ->
                    Log.d(TAG, "avatar delete: oldAvatarUrl=$previousUrl")
                    clearLocalAvatarUrl()
                    Log.d(TAG, "avatar delete: local avatarUrl cleared")
                    applyProfileDto(dto.copy(avatarUrl = null))
                    if (!previousUrl.isNullOrBlank()) {
                        ProfileRepository.evictAvatarFromImageCache(context, previousUrl)
                    }
                    _profileLoading.value = false
                    ProfileRepository.loadOrCreateCurrentProfile()
                        .onSuccess { fresh -> applyProfileDto(fresh.copy(avatarUrl = null)) }
                    _uiMessageType.value = AppAuthMessageType.Success
                    _uiMessage.value = app.getString(R.string.profile_success_photo_removed)
                }
                .onFailure { error ->
                    showError(error.message ?: app.getString(R.string.profile_error_photo_remove_failed))
                    _profileLoading.value = false
                }
            if (_profileLoading.value) {
                _profileLoading.value = false
            }
        }
    }

    private fun clearLocalAvatarUrl() {
        _avatarUrl.value = null
    }

    fun saveProfileDetails() {
        viewModelScope.launch {
            _profileLoading.value = true
            val userId = AuthRepository.currentSignedInUserId()
            if (userId.isNullOrBlank()) {
                showError(app.getString(R.string.profile_error_sign_in_save_profile))
                _profileLoading.value = false
                return@launch
            }
            ProfileRepository.updateProfile(buildProfileDto(userId))
                .onSuccess { dto ->
                    applyProfileDto(dto)
                    _uiMessageType.value = AppAuthMessageType.Success
                    _uiMessage.value = app.getString(R.string.profile_success_profile_saved)
                }
                .onFailure { error ->
                    showError(error.message ?: app.getString(R.string.profile_error_save_profile_failed))
                }
            _profileLoading.value = false
        }
    }

    fun refreshAuthProfile() {
        viewModelScope.launch {
            syncAuthUserFields()
            reloadProfileFromRemote()
        }
    }

    fun refreshProfileFromRemote() {
        if (_profileLoading.value || _isProfileRefreshing.value) return
        viewModelScope.launch {
            _isProfileRefreshing.value = true
            syncAuthUserFields()
            reloadProfileFromRemote()
            _isProfileRefreshing.value = false
        }
    }

    private fun syncAuthUserFields() {
        _authEmail.value = AuthRepository.currentSignedInEmail()
        _authUserId.value = AuthRepository.currentSignedInUserId()
        AuthRepository.currentSignedInEmail()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { _emailInput.value = it }
    }

    private suspend fun reloadProfileFromRemote() {
        ProfileRepository.loadOrCreateCurrentProfile()
            .onSuccess { dto ->
                applyProfileDto(dto)
            }
            .onFailure { error ->
                if (_displayNameInput.value.isBlank()) {
                    _displayNameInput.value = AuthRepository.currentDisplayName().orEmpty()
                }
                showError(error.message ?: app.getString(R.string.profile_error_load_profile_failed))
            }
    }

    private fun applyProfileDto(dto: ProfileDto) {
        _profileForm.value = ProfileFormState(
            fullName = dto.fullName.orEmpty(),
            secondaryEmail = dto.secondaryEmail.orEmpty(),
            phone = dto.phone.orEmpty(),
            country = dto.country.orEmpty(),
            region = dto.region.orEmpty(),
            city = dto.city.orEmpty(),
            postalCode = dto.postalCode.orEmpty(),
            streetAddress = dto.streetAddress.orEmpty(),
            apartmentOrHouseNo = dto.apartmentOrHouseNo.orEmpty(),
            bio = dto.bio.orEmpty(),
            language = dto.language.orEmpty(),
        )
        val displayName = dto.displayName.orEmpty()
        if (displayName.isNotBlank() || _displayNameInput.value.isBlank()) {
            _displayNameInput.value = displayName
        }
        _avatarUrl.value = ProfileRepository.normalizeAvatarUrl(dto.avatarUrl)
    }

    private fun buildProfileDto(userId: String): ProfileDto {
        val form = _profileForm.value
        return ProfileDto(
            id = userId,
            displayName = _displayNameInput.value.trim().ifBlank { null },
            fullName = form.fullName.trim().ifBlank { null },
            phone = form.phone.trim().ifBlank { null },
            secondaryEmail = form.secondaryEmail.trim().ifBlank { null },
            country = form.country.trim().ifBlank { null },
            region = form.region.trim().ifBlank { null },
            city = form.city.trim().ifBlank { null },
            postalCode = form.postalCode.trim().ifBlank { null },
            streetAddress = form.streetAddress.trim().ifBlank { null },
            apartmentOrHouseNo = form.apartmentOrHouseNo.trim().ifBlank { null },
            bio = form.bio.trim().ifBlank { null },
            language = form.language.trim().ifBlank { null },
            avatarUrl = _avatarUrl.value?.trim()?.ifBlank { null },
        )
    }

    private fun showError(message: String) {
        _uiMessageType.value = AppAuthMessageType.Error
        _uiMessage.value = message
    }

    private companion object {
        private const val TAG = "ProfileViewModel"
        const val MIN_PASSWORD_LENGTH = 8
    }
}

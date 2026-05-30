package com.example.mamunbingoapp.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.ProfileAvatar
import com.example.mamunbingoapp.ui.components.profileAvatarInitials
import com.example.mamunbingoapp.ui.components.AppAuthMessage
import com.example.mamunbingoapp.ui.components.AppAuthMessageType
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppPullRefresh
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.appImeScrollable
import com.example.mamunbingoapp.ui.components.AppImeFormScrollBottomSpacer
import com.example.mamunbingoapp.viewmodel.ProfileFormState

@Composable
fun AccountFormScreen(
    onBack: () -> Unit,
    signedInEmail: String? = null,
    displayName: String = "",
    emailInput: String = "",
    profileForm: ProfileFormState = ProfileFormState(),
    profileMessage: String? = null,
    profileMessageType: AppAuthMessageType = AppAuthMessageType.Info,
    profileLoading: Boolean = false,
    isProfileRefreshing: Boolean = false,
    onProfileRefresh: () -> Unit = {},
    avatarUrl: String? = null,
    avatarInitials: String? = null,
    onAvatarPicked: (Uri) -> Unit = {},
    onAvatarDelete: () -> Unit = {},
    onDisplayNameChange: (String) -> Unit = {},
    onSaveDisplayName: () -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onUpdateEmail: () -> Unit = {},
    onFullNameChange: (String) -> Unit = {},
    onSecondaryEmailChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    onCountryChange: (String) -> Unit = {},
    onRegionChange: (String) -> Unit = {},
    onCityChange: (String) -> Unit = {},
    onPostalCodeChange: (String) -> Unit = {},
    onStreetAddressChange: (String) -> Unit = {},
    onApartmentOrHouseNoChange: (String) -> Unit = {},
    onBioChange: (String) -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onSaveProfileDetails: () -> Unit = {},
) {
    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
    ) { uri ->
        if (uri != null) onAvatarPicked(uri)
    }
    var showDeleteAvatarDialog by rememberSaveable { mutableStateOf(false) }
    val hasAvatar = !avatarUrl.isNullOrBlank()
    AppConfirmDialog(
        visible = showDeleteAvatarDialog,
        title = "Remove profile photo?",
        message = "Your profile photo will be removed from your account.",
        confirmText = "Remove",
        cancelText = "Cancel",
        onConfirm = {
            showDeleteAvatarDialog = false
            onAvatarDelete()
        },
        onCancel = { showDeleteAvatarDialog = false },
        onDismiss = { showDeleteAvatarDialog = false },
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.surface),
        ) {
            AppHeaderBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter),
            )
            Column(Modifier.fillMaxSize()) {
                AppTopBar(
                    title = "My Account",
                    showBack = true,
                    onBackClick = onBack,
                )
                AppPullRefresh(
                    isRefreshing = isProfileRefreshing,
                    onRefresh = onProfileRefresh,
                    modifier = Modifier.weight(1f),
                    enabled = !profileLoading,
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appImeScrollable(rememberScrollState())
                        .padding(top = Dimens.spacing8)
                        .padding(horizontal = Dimens.screenHorizontalPadding),
                ) {
                    Text(
                        text = "Your account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    Text(
                        text = "Login email is managed by sign-in. Personal details sync to your Supabase profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProfileAvatar(
                            avatarUrl = avatarUrl,
                            initials = avatarInitials
                                ?: profileAvatarInitials(displayName, profileForm.fullName),
                            showEditBadge = true,
                            loading = profileLoading,
                            onPickAvatar = {
                                pickAvatarLauncher.launch(
                                    PickVisualMediaRequest(PickVisualMedia.ImageOnly),
                                )
                            },
                            onDeleteAvatar = { showDeleteAvatarDialog = true },
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing8))
                        Text(
                            text = if (hasAvatar) "Tap remove to delete photo" else "Tap photo to add",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing24))

                    AccountSectionHeader(
                        title = "Account login details",
                        subtitle = "Display name and email for sign-in",
                        emphasized = true,
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    AppSectionSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimens.spacing12,
                                    vertical = Dimens.spacing16,
                                ),
                        ) {
                            AccountFormField(
                                label = "Display name",
                                value = displayName,
                                onValueChange = onDisplayNameChange,
                                placeholder = "Enter display name",
                                leadingIcon = Icons.Filled.Person,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing12))
                            AppPrimaryButton(
                                text = "Save display name",
                                onClick = onSaveDisplayName,
                                loading = profileLoading,
                                enabled = !profileLoading,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountCompactInfoRow(
                                label = "Signed-in email",
                                value = signedInEmail ?: "Not signed in",
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "New email",
                                value = emailInput,
                                onValueChange = onEmailChange,
                                placeholder = "Enter new email",
                                leadingIcon = Icons.Filled.Mail,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing12))
                            AppPrimaryButton(
                                text = "Update email",
                                onClick = onUpdateEmail,
                                loading = profileLoading,
                                enabled = !profileLoading,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacing24))

                    AccountSectionHeader(
                        title = "Personal profile details",
                        subtitle = "Saved to your Supabase profile",
                        emphasized = false,
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    AppSectionSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimens.spacing12,
                                    vertical = Dimens.spacing16,
                                ),
                        ) {
                            AccountFormField(
                                label = "Full name",
                                value = profileForm.fullName,
                                onValueChange = onFullNameChange,
                                placeholder = "Your full name",
                                leadingIcon = Icons.Filled.Person,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Secondary email",
                                value = profileForm.secondaryEmail,
                                onValueChange = onSecondaryEmailChange,
                                placeholder = "Contact email",
                                leadingIcon = Icons.Filled.Mail,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Phone number",
                                value = profileForm.phone,
                                onValueChange = onPhoneChange,
                                placeholder = "+1 555 000 0000",
                                leadingIcon = Icons.Filled.Phone,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Country",
                                value = profileForm.country,
                                onValueChange = onCountryChange,
                                placeholder = "Country",
                                leadingIcon = Icons.Filled.Public,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Region",
                                value = profileForm.region,
                                onValueChange = onRegionChange,
                                placeholder = "State or region",
                                leadingIcon = Icons.Filled.LocationOn,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "City",
                                value = profileForm.city,
                                onValueChange = onCityChange,
                                placeholder = "City",
                                leadingIcon = Icons.Filled.LocationCity,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Postal code",
                                value = profileForm.postalCode,
                                onValueChange = onPostalCodeChange,
                                placeholder = "Postal code",
                                leadingIcon = Icons.Filled.Place,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Street address",
                                value = profileForm.streetAddress,
                                onValueChange = onStreetAddressChange,
                                placeholder = "Street address",
                                leadingIcon = Icons.Filled.Place,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Apartment / house no.",
                                value = profileForm.apartmentOrHouseNo,
                                onValueChange = onApartmentOrHouseNoChange,
                                placeholder = "Apt or house number",
                                leadingIcon = Icons.Filled.Home,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Bio",
                                value = profileForm.bio,
                                onValueChange = onBioChange,
                                placeholder = "Short bio",
                                leadingIcon = Icons.Filled.Description,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            AccountFormField(
                                label = "Language",
                                value = profileForm.language,
                                onValueChange = onLanguageChange,
                                placeholder = "e.g. English",
                                leadingIcon = Icons.Filled.Language,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing12))
                            AppAuthMessage(
                                message = profileMessage,
                                type = profileMessageType,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (!profileMessage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(Dimens.spacing12))
                            }
                            AppPrimaryButton(
                                text = "Save profile",
                                onClick = onSaveProfileDetails,
                                loading = profileLoading,
                                enabled = !profileLoading,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing24))
                    AppImeFormScrollBottomSpacer()
                }
                }
            }
        }
    }
}

@Composable
private fun AccountSectionHeader(
    title: String,
    subtitle: String,
    emphasized: Boolean,
) {
    Text(
        text = title,
        style = if (emphasized) {
            MaterialTheme.typography.titleMedium
        } else {
            MaterialTheme.typography.titleSmall
        },
        color = if (emphasized) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
    Spacer(modifier = Modifier.height(Dimens.spacing4))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = if (emphasized) 0.9f else 0.75f,
        ),
    )
}

@Composable
private fun AccountCompactInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusSmall))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Mail,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = Dimens.spacing12),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AccountFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorText: String? = null,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(Dimens.spacing8))
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        keyboardOptions = keyboardOptions,
        isError = isError,
        errorText = errorText,
    )
}

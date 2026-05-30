package com.example.mamunbingoapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppAuthMessage
import com.example.mamunbingoapp.ui.components.AppAuthMessageType
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppImeFormScrollBottomSpacer
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AuthBottomWave
import com.example.mamunbingoapp.ui.components.AuthFooterPrompt
import com.example.mamunbingoapp.ui.components.PasswordStrengthMeter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onLogIn: () -> Unit = {},
    onSendResetLink: (String) -> Unit = {},
    onUpdatePassword: (String) -> Unit = {},
    recoveryEmail: String? = null,
    errorMessage: String? = null,
    infoMessage: String? = null,
    isLoading: Boolean = false,
    resetEmailSent: Boolean = false,
    recoveryPending: Boolean = false,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailSent by rememberSaveable { mutableStateOf(false) }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var localValidationError by rememberSaveable { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(resetEmailSent) {
        if (resetEmailSent) emailSent = true
    }

    androidx.compose.runtime.LaunchedEffect(recoveryEmail) {
        if (!recoveryEmail.isNullOrBlank()) email = recoveryEmail
    }

    val topBarTitleRes = when {
        recoveryPending -> R.string.change_password_title
        emailSent -> R.string.forgot_success_title
        else -> R.string.forgot_title
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthBottomWave(modifier = Modifier.align(Alignment.BottomCenter))
        AppHeaderPageLayout(
            scrollableContent = true,
            topBar = {
                AppTopBar(
                    title = stringResource(topBarTitleRes),
                    showBack = true,
                    onBackClick = if (emailSent && !recoveryPending) onLogIn else onBack,
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.spacing5)
                        .padding(horizontal = Dimens.screenHorizontalPadding),
                ) {
                    if (recoveryPending) {
                        Spacer(modifier = Modifier.height(Dimens.spacing16))
                        Text(
                            text = stringResource(R.string.change_password_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AppAuthMessage(
                            message = infoMessage,
                            type = AppAuthMessageType.Info,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (!infoMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.spacing12))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.change_password_new_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AppTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                localValidationError = null
                            },
                            placeholder = stringResource(R.string.change_password_new_placeholder),
                            visualTransformation = if (newPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = null,
                                    )
                                }
                            },
                        )
                        if (newPassword.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.spacing8))
                            PasswordStrengthMeter(password = newPassword)
                            if (newPassword.length < 8) {
                                Spacer(modifier = Modifier.height(Dimens.spacing4))
                                Text(
                                    text = stringResource(R.string.password_min_length_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacing16))
                        Text(
                            text = stringResource(R.string.change_password_confirm_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AppTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                localValidationError = null
                            },
                            placeholder = stringResource(R.string.change_password_confirm_placeholder),
                            visualTransformation = if (confirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = null,
                                    )
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing16))
                        val displayError = localValidationError ?: errorMessage
                        AppAuthMessage(
                            message = displayError,
                            type = AppAuthMessageType.Error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (!displayError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.spacing12))
                        }
                        val errorEnterNew = stringResource(R.string.change_password_error_enter_new)
                        val errorMinLength = stringResource(R.string.change_password_error_min_length)
                        val errorConfirm = stringResource(R.string.change_password_error_confirm)
                        val errorMismatch = stringResource(R.string.change_password_error_mismatch)
                        AppPrimaryButton(
                            text = stringResource(R.string.change_password_button),
                            onClick = {
                                localValidationError = when {
                                    newPassword.isBlank() -> errorEnterNew
                                    newPassword.length < 8 -> errorMinLength
                                    confirmPassword.isBlank() -> errorConfirm
                                    newPassword != confirmPassword -> errorMismatch
                                    else -> null
                                }
                                if (localValidationError == null) {
                                    onUpdatePassword(newPassword)
                                }
                            },
                            loading = isLoading,
                        )
                    } else if (emailSent) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Eco,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.forgot_success_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.forgot_success_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        AppAuthMessage(
                            message = stringResource(R.string.forgot_success_banner),
                            type = AppAuthMessageType.Success,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.login_email_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AppTextField(
                            value = email,
                            onValueChange = { },
                            placeholder = stringResource(R.string.forgot_email_placeholder),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing16))
                AppPrimaryButton(
                    text = stringResource(R.string.forgot_back_to_login),
                    onClick = onLogIn,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                )
                Spacer(modifier = Modifier.height(Dimens.spacing24))
                AuthFooterPrompt(
                    primaryText = stringResource(R.string.forgot_resend_prompt),
                    linkText = stringResource(R.string.forgot_resend_cta),
                    onClick = { onSendResetLink(email) }
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
            } else {
                        Spacer(modifier = Modifier.height(Dimens.spacing16))
                        Text(
                            text = stringResource(R.string.forgot_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.forgot_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.login_email_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AppTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = stringResource(R.string.forgot_email_placeholder),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing16))
                AppAuthMessage(
                    message = errorMessage,
                    type = AppAuthMessageType.Error,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.spacing12))
                }
                if (!infoMessage.isNullOrBlank() && (emailSent || recoveryPending)) {
                    AppAuthMessage(
                        message = infoMessage,
                        type = AppAuthMessageType.Info,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing12))
                }
                AppPrimaryButton(
                    text = stringResource(R.string.forgot_send_button),
                    onClick = { onSendResetLink(email) },
                    loading = isLoading,
                )
                Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.forgot_remember_prompt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = " " + stringResource(R.string.forgot_remember_cta),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable(onClick = onLogIn)
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.spacing16))
                            Text(
                                text = stringResource(R.string.forgot_inbox_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacing24))
                    }
                    AppImeFormScrollBottomSpacer()
                }
            },
        )
    }
}

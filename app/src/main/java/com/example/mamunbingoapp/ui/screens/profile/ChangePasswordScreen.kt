package com.example.mamunbingoapp.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppAuthMessage
import com.example.mamunbingoapp.ui.components.AppAuthMessageType
import com.example.mamunbingoapp.ui.components.PasswordStrengthMeter
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppImeFormScrollBottomSpacer
import com.example.mamunbingoapp.ui.components.AppTopBar

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    isLoading: Boolean = false,
    message: String? = null,
    messageType: AppAuthMessageType = AppAuthMessageType.Error,
    formResetKey: Int = 0,
    onChangePassword: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit = { _, _, _ -> },
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var currentVisible by rememberSaveable { mutableStateOf(false) }
    var newVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(formResetKey) {
        if (formResetKey > 0) {
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
        }
    }

    AppHeaderPageLayout(
        scrollableContent = true,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.change_password_title),
                showBack = true,
                onBackClick = onBack,
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing5)
                    .padding(horizontal = Dimens.screenHorizontalPadding),
            ) {
            Spacer(modifier = Modifier.height(Dimens.spacing16))
                Text(
                    text = stringResource(R.string.change_password_current_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    placeholder = stringResource(R.string.change_password_current_placeholder),
                    visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { currentVisible = !currentVisible }) {
                            Icon(
                                imageVector = if (currentVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.change_password_new_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = stringResource(R.string.change_password_new_placeholder),
                    visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                imageVector = if (newVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                if (newPassword.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    PasswordStrengthMeter(password = newPassword)
                    if (newPassword.length < 8) {
                        Spacer(modifier = Modifier.height(Dimens.spacing4))
                        Text(
                            text = "Use at least 8 characters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.change_password_confirm_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = stringResource(R.string.change_password_confirm_placeholder),
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
            AppAuthMessage(
                message = message,
                type = messageType,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Dimens.spacing12))
            }
            AppPrimaryButton(
                text = stringResource(R.string.change_password_button),
                onClick = { onChangePassword(currentPassword, newPassword, confirmPassword) },
                loading = isLoading,
                enabled = !isLoading,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing24))
            AppImeFormScrollBottomSpacer()
            }
        },
    )
}

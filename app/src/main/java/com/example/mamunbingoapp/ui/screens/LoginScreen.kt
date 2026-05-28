package com.example.mamunbingoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.automirrored.filled.Login
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppAuthMessage
import com.example.mamunbingoapp.ui.components.AppAuthMessageType
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AuthBottomWave
import com.example.mamunbingoapp.ui.components.AuthFooterPrompt

@Composable
fun LoginScreen(
    onForgotPassword: () -> Unit = {},
    onLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onRegister: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    AppHeaderPageLayout(
        topBar = {},
        content = {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AuthBottomWave(modifier = Modifier.align(Alignment.BottomCenter))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.screenHorizontalPadding)
            ) {
            Spacer(modifier = Modifier.height(96.dp))
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
                    text = stringResource(R.string.login_welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.login_email_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(R.string.login_email_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.login_password_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(R.string.login_password_placeholder),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onForgotPassword() }
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            AppAuthMessage(
                message = errorMessage,
                type = AppAuthMessageType.Error,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Dimens.spacing12))
            }
            AppPrimaryButton(
                text = stringResource(R.string.login_button),
                onClick = { onLogin(email, password) },
                loading = isLoading,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            AuthFooterPrompt(
                primaryText = stringResource(R.string.login_register_prompt),
                linkText = stringResource(R.string.login_register_cta),
                onClick = onRegister
            )
        }
        }
        }
    )
}

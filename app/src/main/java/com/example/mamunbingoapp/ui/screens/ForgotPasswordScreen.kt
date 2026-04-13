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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Success
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AuthBottomWave
import com.example.mamunbingoapp.ui.components.AuthFooterPrompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onLogIn: () -> Unit = {},
    onSendResetLink: (String) -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailSent by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AuthBottomWave(modifier = Modifier.align(Alignment.BottomCenter))
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AppHeaderBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter)
            )
            Column(Modifier.fillMaxSize()) {
                AppTopBar(
                    title = stringResource(if (emailSent) R.string.forgot_success_title else R.string.forgot_title),
                    showBack = true,
                    onBackClick = if (emailSent) onLogIn else onBack
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = Dimens.spacing5)
                        .padding(horizontal = Dimens.screenHorizontalPadding)
                ) {
                    if (emailSent) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Success.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacing16),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(R.string.forgot_success_banner),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Success
                    )
                }
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
                Spacer(modifier = Modifier.height(16.dp))
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
                Spacer(modifier = Modifier.weight(1f))
                AuthFooterPrompt(
                    primaryText = stringResource(R.string.forgot_resend_prompt),
                    linkText = stringResource(R.string.forgot_resend_cta),
                    onClick = { onSendResetLink(email) }
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
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
                Spacer(modifier = Modifier.height(16.dp))
                AppPrimaryButton(
                    text = stringResource(R.string.forgot_send_button),
                    onClick = {
                        onSendResetLink(email)
                        emailSent = true
                    }
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.forgot_inbox_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            }
            }
        }
    }
}

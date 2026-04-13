package com.example.mamunbingoapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Success
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppTopBar

@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var currentVisible by rememberSaveable { mutableStateOf(false) }
    var newVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    val strengthLabel = stringResource(R.string.change_password_strength_strong)
    val strengthProgress = 0.75f

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
                title = stringResource(R.string.change_password_title),
                showBack = true,
                onBackClick = onBack
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing5)
                    .padding(horizontal = Dimens.screenHorizontalPadding)
            ) {
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.change_password_strength, strengthLabel),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(strengthProgress)
                        .height(6.dp)
                        .background(Success, RoundedCornerShape(3.dp))
                )
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
            Spacer(modifier = Modifier.height(16.dp))
            AppPrimaryButton(
                text = stringResource(R.string.change_password_button),
                onClick = { }
            )
            }
        }
    }
}

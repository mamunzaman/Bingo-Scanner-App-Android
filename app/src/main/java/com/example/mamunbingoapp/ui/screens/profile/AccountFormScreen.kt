package com.example.mamunbingoapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.viewmodel.AccountViewModel

@Composable
fun AccountFormScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    .align(androidx.compose.ui.Alignment.TopCenter),
            )
            Column(Modifier.fillMaxSize()) {
                AppTopBar(
                    title = "My Account",
                    showBack = true,
                    onBackClick = onBack,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = Dimens.spacing5)
                        .padding(horizontal = Dimens.screenHorizontalPadding),
                ) {
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    Text(
                        text = "Profile details",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    Text(
                        text = "Add your contact information. Details are stored on this device until account sync is enabled.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    Text(
                        text = "PROFILE DETAILS",
                        style = AppTextStyles.sectionLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Dimens.spacing8),
                    )
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
                                value = uiState.fullName,
                                onValueChange = viewModel::updateFullName,
                                placeholder = "Your full name",
                                leadingIcon = Icons.Filled.Person,
                                isError = uiState.fullNameError != null,
                                errorText = uiState.fullNameError,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing20))
                            AccountFormField(
                                label = "Email",
                                value = uiState.email,
                                onValueChange = viewModel::updateEmail,
                                placeholder = "you@example.com",
                                leadingIcon = Icons.Filled.Mail,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                isError = uiState.emailError != null,
                                errorText = uiState.emailError,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing20))
                            AccountFormField(
                                label = "Phone number",
                                value = uiState.phone,
                                onValueChange = viewModel::updatePhone,
                                placeholder = "+1 555 000 0000",
                                leadingIcon = Icons.Filled.Phone,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing20))
                            AccountFormField(
                                label = "Country",
                                value = uiState.country,
                                onValueChange = viewModel::updateCountry,
                                placeholder = "Country",
                                leadingIcon = Icons.Filled.Public,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing20))
                            AccountFormField(
                                label = "City",
                                value = uiState.city,
                                onValueChange = viewModel::updateCity,
                                placeholder = "City",
                                leadingIcon = Icons.Filled.LocationCity,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing24))
                    AppPrimaryButton(
                        text = "Save",
                        onClick = viewModel::saveProfile,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(Dimens.spacing16),
                    )
                }
            }
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

package com.example.mamunbingoapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mamunbingoapp.BuildConfig
import com.example.mamunbingoapp.data.dev.DevReset
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Secondary
import com.example.mamunbingoapp.theme.SecondaryContainer
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppIconContainer
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.viewmodel.SettingsViewModel
import com.example.mamunbingoapp.viewmodel.ThemeMode
import com.example.mamunbingoapp.viewmodel.ThemeViewModel
import androidx.compose.material3.Scaffold
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel,
    onChangePassword: () -> Unit,
    onLocationServices: () -> Unit,
    onEnvironmentalImpact: () -> Unit,
    onTermsOfService: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onLogout: () -> Unit,
    onTabSelected: (AppTab) -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AppConfirmDialog(
        visible = showLogoutDialog,
        title = "Are you sure?",
        message = "Do you want to continue?",
        confirmText = "Confirm",
        cancelText = "Cancel",
        onConfirm = { showLogoutDialog = false; onLogout() },
        onCancel = { showLogoutDialog = false },
        onDismiss = { showLogoutDialog = false }
    )
    AppConfirmDialog(
        visible = showResetDialog,
        title = "Reset database?",
        message = "This will delete all local rooms, tickets, history, and settings. (Debug only)",
        confirmText = "Reset",
        cancelText = "Cancel",
        onConfirm = {
            showResetDialog = false
            scope.launch {
                withContext(Dispatchers.IO) { DevReset.resetAll(context) }
                (context as? Activity)?.recreate()
            }
        },
        onCancel = { showResetDialog = false },
        onDismiss = { showResetDialog = false }
    )
    val themeMode by themeViewModel.themeMode.collectAsState(ThemeMode.SYSTEM)
    val showDemoData by viewModel.showDemoData.collectAsState()
    val keepScreenOnDuringGame by viewModel.keepScreenOnDuringGame.collectAsState()
    val pushNotifications by viewModel.pushNotifications.collectAsState()
    val dailyReminders by viewModel.dailyReminders.collectAsState()
    val faceIdTouchId by viewModel.faceIdTouchId.collectAsState()
    val dataSharing by viewModel.dataSharing.collectAsState()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = { AppBottomBar(selectedTab = AppTab.Profile, onTabSelected = onTabSelected) }
    ) { paddingValues ->
        AppHeaderPageLayout(
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            topBar = {
                AppTopBar(
                    title = "Settings",
                    showBack = true,
                    onBackClick = onBack
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.screenHorizontalPadding)
                        .padding(top = Dimens.spacing8, bottom = Dimens.spacing16)
                ) {
                    SettingsSection(title = "DATA") {
                SettingsToggleRow(
                    icon = Icons.Default.Eco,
                    title = "Show demo data",
                    subtitle = "Include demo sessions in History and ticket picker",
                    checked = showDemoData,
                    onCheckedChange = { viewModel.setShowDemoData(it) }
                )
            }
            SettingsSection(title = "LIVE PLAY") {
                SettingsToggleRow(
                    icon = Icons.Filled.StayCurrentPortrait,
                    title = "Keep screen on during game",
                    subtitle = "Prevents your phone from locking while playing live Bingo.",
                    checked = keepScreenOnDuringGame,
                    onCheckedChange = { viewModel.setKeepScreenOnDuringGame(it) }
                )
            }
            SettingsSection(title = "APPEARANCE") {
                SettingsThemeRow(
                    icon = Icons.Default.Smartphone,
                    title = "System",
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.SYSTEM) }
                )
                SettingsThemeRow(
                    icon = Icons.Default.LightMode,
                    title = "Light",
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) }
                )
                SettingsThemeRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark",
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) }
                )
            }
            SettingsSection(title = "NOTIFICATIONS") {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Game updates and rewards",
                    checked = pushNotifications,
                    onCheckedChange = { viewModel.setPushNotifications(it) }
                )
                SettingsToggleRow(
                    icon = Icons.Default.Alarm,
                    title = "Daily Reminders",
                    subtitle = "Never miss your daily eco-task",
                    checked = dailyReminders,
                    onCheckedChange = { viewModel.setDailyReminders(it) }
                )
            }
            SettingsSection(title = "SECURITY") {
                SettingsToggleRow(
                    icon = Icons.Default.Fingerprint,
                    title = "FaceID / TouchID",
                    checked = faceIdTouchId,
                    onCheckedChange = { viewModel.setFaceIdTouchId(it) }
                )
                SettingsNavRow(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = onChangePassword
                )
            }
            SettingsSection(title = "PRIVACY") {
                SettingsNavRow(
                    icon = Icons.Default.LocationOn,
                    title = "Location Services",
                    onClick = onLocationServices
                )
                SettingsToggleRow(
                    icon = Icons.Default.Share,
                    title = "Data Sharing",
                    subtitle = "Help us improve anonymously",
                    checked = dataSharing,
                    onCheckedChange = { viewModel.setDataSharing(it) }
                )
            }
            SettingsSection(title = "ABOUT") {
                SettingsNavRow(
                    icon = Icons.Default.Eco,
                    title = "Environmental Impact",
                    subtitle = "See your cumulative CO2 savings",
                    onClick = onEnvironmentalImpact,
                    showChevron = false
                )
                SettingsNavRow(title = "Terms of Service", onClick = onTermsOfService)
                SettingsNavRow(title = "Privacy Policy", onClick = onPrivacyPolicy)
            }
            if (BuildConfig.DEBUG) {
                SettingsSection(title = "Developer") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.screenHorizontalPadding, 16.dp)
                    ) {
                        AppPrimaryButton(
                            text = "Reset local database",
                            onClick = { showResetDialog = true }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Version 1.0.0 (Build 242)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Made with ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Secondary
                    )
                    Text(
                        text = " for the Earth",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showLogoutDialog = true }
                        .background(SecondaryContainer, RoundedCornerShape(12.dp))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Secondary
                    )
                }
            }
            }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = title,
            style = AppTextStyles.sectionLabel,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimens.radiusSmall))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) }
            .padding(horizontal = Dimens.screenHorizontalPadding, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.screenHorizontalPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsThemeRow(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = Dimens.screenHorizontalPadding, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.screenHorizontalPadding)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SettingsNavRow(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = Dimens.screenHorizontalPadding, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (icon != null) Dimens.screenHorizontalPadding else 0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

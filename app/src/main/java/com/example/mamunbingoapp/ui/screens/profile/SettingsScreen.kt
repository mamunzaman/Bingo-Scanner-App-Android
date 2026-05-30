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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.alpha
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.BuildConfig
import com.example.mamunbingoapp.data.dev.DevReset
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Secondary
import com.example.mamunbingoapp.theme.SecondaryContainer
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppIconContainer
import com.example.mamunbingoapp.ui.components.ProfileComingSoonBadge
import com.example.mamunbingoapp.ui.components.AppIconTile
import com.example.mamunbingoapp.ui.components.AppInsetDivider
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppBottomBarScrollExtraPadding
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.data.localization.AppLanguage
import com.example.mamunbingoapp.viewmodel.AppLanguageViewModel
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
    viewModel: SettingsViewModel = viewModel(),
    appLanguageViewModel: AppLanguageViewModel = viewModel(),
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AppConfirmDialog(
        visible = showLogoutDialog,
        title = stringResource(R.string.settings_logout_title),
        message = stringResource(R.string.settings_logout_message),
        confirmText = stringResource(R.string.settings_confirm),
        cancelText = stringResource(R.string.settings_cancel),
        onConfirm = { showLogoutDialog = false; onLogout() },
        onCancel = { showLogoutDialog = false },
        onDismiss = { showLogoutDialog = false }
    )
    AppConfirmDialog(
        visible = showResetDialog,
        title = stringResource(R.string.settings_reset_title),
        message = stringResource(R.string.settings_reset_message),
        confirmText = stringResource(R.string.settings_reset_confirm),
        cancelText = stringResource(R.string.settings_cancel),
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
    val appLanguage by appLanguageViewModel.appLanguage.collectAsState()
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
                    title = stringResource(R.string.settings_title),
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
                        .padding(top = Dimens.spacing8, bottom = Dimens.spacing16 + AppBottomBarScrollExtraPadding)
                ) {
                    SettingsSection(title = stringResource(R.string.settings_section_data)) {
                SettingsToggleRow(
                    icon = Icons.Default.Eco,
                    title = stringResource(R.string.settings_show_demo_data),
                    subtitle = stringResource(R.string.settings_show_demo_data_subtitle),
                    checked = showDemoData,
                    onCheckedChange = { viewModel.setShowDemoData(it) }
                )
            }
            SettingsGroupedSection(title = stringResource(R.string.settings_section_live_play)) {
                SettingsToggleRow(
                    icon = Icons.Filled.StayCurrentPortrait,
                    title = stringResource(R.string.settings_keep_screen_on),
                    subtitle = stringResource(R.string.settings_keep_screen_on_subtitle),
                    checked = keepScreenOnDuringGame,
                    onCheckedChange = { viewModel.setKeepScreenOnDuringGame(it) },
                    groupedInCard = true,
                )
            }
            SettingsGroupedSection(title = stringResource(R.string.settings_section_appearance)) {
                SettingsThemeRow(
                    icon = Icons.Default.Smartphone,
                    title = stringResource(R.string.settings_theme_system),
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.SYSTEM) },
                    groupedInCard = true,
                )
                AppInsetDivider(
                    startInset = settingsInsetDividerStart,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                )
                SettingsThemeRow(
                    icon = Icons.Default.LightMode,
                    title = stringResource(R.string.settings_theme_light),
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) },
                    groupedInCard = true,
                )
                AppInsetDivider(
                    startInset = settingsInsetDividerStart,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                )
                SettingsThemeRow(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.settings_theme_dark),
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) },
                    groupedInCard = true,
                )
                AppInsetDivider(
                    startInset = settingsInsetDividerStart,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                )
                SettingsLanguageRow(
                    selectedLanguage = appLanguage,
                    onLanguageSelected = appLanguageViewModel::setAppLanguage,
                    groupedInCard = true,
                )
            }
            SettingsGroupedSection(title = stringResource(R.string.settings_section_notifications)) {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_push_notifications),
                    subtitle = stringResource(R.string.settings_push_notifications_subtitle),
                    checked = pushNotifications,
                    onCheckedChange = { viewModel.setPushNotifications(it) },
                    groupedInCard = true,
                    comingSoon = true,
                )
                AppInsetDivider(
                    startInset = settingsInsetDividerStart,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                )
                SettingsToggleRow(
                    icon = Icons.Default.Alarm,
                    title = stringResource(R.string.settings_daily_reminders),
                    subtitle = stringResource(R.string.settings_daily_reminders_subtitle),
                    checked = dailyReminders,
                    onCheckedChange = { viewModel.setDailyReminders(it) },
                    groupedInCard = true,
                    comingSoon = true,
                )
            }
            SettingsGroupedSection(title = stringResource(R.string.settings_section_security)) {
                SettingsToggleRow(
                    icon = Icons.Default.Fingerprint,
                    title = stringResource(R.string.settings_face_id),
                    checked = faceIdTouchId,
                    onCheckedChange = { viewModel.setFaceIdTouchId(it) },
                    groupedInCard = true,
                    comingSoon = true,
                )
                AppInsetDivider(
                    startInset = settingsInsetDividerStart,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                )
                SettingsNavRow(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.settings_change_password),
                    onClick = onChangePassword,
                    groupedInCard = true,
                )
            }
            SettingsSection(title = stringResource(R.string.settings_section_privacy)) {
                SettingsNavRow(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.settings_location_services),
                    onClick = onLocationServices
                )
                SettingsToggleRow(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.settings_data_sharing),
                    subtitle = stringResource(R.string.settings_data_sharing_subtitle),
                    checked = dataSharing,
                    onCheckedChange = { viewModel.setDataSharing(it) }
                )
            }
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                SettingsNavRow(
                    icon = Icons.Default.Eco,
                    title = stringResource(R.string.settings_environmental_impact),
                    subtitle = stringResource(R.string.settings_environmental_impact_subtitle),
                    onClick = onEnvironmentalImpact,
                    showChevron = false
                )
                SettingsNavRow(title = stringResource(R.string.settings_terms), onClick = onTermsOfService)
                SettingsNavRow(title = stringResource(R.string.settings_privacy), onClick = onPrivacyPolicy)
            }
            if (BuildConfig.DEBUG) {
                SettingsSection(title = stringResource(R.string.settings_section_developer)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.screenHorizontalPadding, 16.dp)
                    ) {
                        AppPrimaryButton(
                            text = stringResource(R.string.settings_reset_local_db),
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
                    text = stringResource(R.string.settings_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.settings_made_with),
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
                        text = stringResource(R.string.settings_for_the_earth),
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
                        text = stringResource(R.string.settings_log_out),
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

private val settingsGroupedCardShape = RoundedCornerShape(Dimens.radiusLarge)

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        AppSectionTitle(
            text = title,
            uppercase = false,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
        )
        AppSectionSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = settingsGroupedCardShape,
            content = content,
        )
    }
}

private val settingsIconTileSize = 42.dp
private val settingsInsetDividerStart =
    Dimens.spacing16 + settingsIconTileSize + Dimens.spacing12

@Composable
private fun SettingsGroupedSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(top = Dimens.spacing24)) {
        AppSectionTitle(
            text = title,
            uppercase = false,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            modifier = Modifier.padding(
                start = Dimens.spacing4,
                top = Dimens.spacing8,
                bottom = 6.dp,
            ),
        )
        AppSectionSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = settingsGroupedCardShape,
            content = content,
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    groupedInCard: Boolean = false,
    comingSoon: Boolean = false,
) {
    val horizontal = if (groupedInCard) Dimens.spacing16 else Dimens.screenHorizontalPadding
    val textStart = if (groupedInCard) Dimens.spacing12 else Dimens.screenHorizontalPadding
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = if (comingSoon) mutedColor else MaterialTheme.colorScheme.onSurface
  val iconContainerColor = if (comingSoon) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
    }
    val iconTint = if (comingSoon) mutedColor else MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (comingSoon) 0.55f else 1f)
            .then(if (groupedInCard) Modifier.heightIn(min = 72.dp) else Modifier)
            .then(
                if (comingSoon) {
                    Modifier
                } else {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onCheckedChange(!checked) },
                    )
                },
            )
            .padding(horizontal = horizontal, vertical = if (groupedInCard) Dimens.spacing8 else Dimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (groupedInCard) {
            AppIconTile(
                icon = icon,
                size = settingsIconTileSize,
                iconSize = 22.dp,
                containerColor = iconContainerColor,
                iconTint = iconTint,
            )
        } else {
            AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = textStart),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (comingSoon) {
            ProfileComingSoonBadge()
        } else {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun SettingsThemeRow(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    groupedInCard: Boolean = false,
) {
    val horizontal = if (groupedInCard) Dimens.spacing16 else Dimens.screenHorizontalPadding
    val textStart = if (groupedInCard) Dimens.spacing12 else Dimens.screenHorizontalPadding
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (groupedInCard) Modifier.heightIn(min = 72.dp) else Modifier)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = horizontal, vertical = if (groupedInCard) Dimens.spacing8 else Dimens.spacing16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (groupedInCard) {
            AppIconTile(
                icon = icon,
                size = settingsIconTileSize,
                iconSize = 22.dp,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f),
            )
        } else {
            AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(start = textStart)
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
private fun SettingsLanguageRow(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    groupedInCard: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val horizontal = if (groupedInCard) Dimens.spacing16 else Dimens.screenHorizontalPadding
    val textStart = if (groupedInCard) Dimens.spacing12 else Dimens.screenHorizontalPadding
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (groupedInCard) Modifier.heightIn(min = 72.dp) else Modifier)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { expanded = true }
                .padding(
                    horizontal = horizontal,
                    vertical = if (groupedInCard) Dimens.spacing8 else Dimens.spacing16,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (groupedInCard) {
                AppIconTile(
                    icon = Icons.Default.Language,
                    size = settingsIconTileSize,
                    iconSize = 22.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f),
                )
            } else {
                AppIconContainer(icon = Icons.Default.Language, size = 40.dp, iconSize = 24.dp)
            }
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = textStart),
            )
            Text(
                text = localizedLanguageName(selectedLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = mutedColor,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = mutedColor,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AppLanguage.supported.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(localizedLanguageName(language))
                            if (language.code == selectedLanguage.code) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(start = Dimens.spacing8)
                                        .size(18.dp),
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        if (language.code != selectedLanguage.code) {
                            onLanguageSelected(language)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun localizedLanguageName(language: AppLanguage): String = when (language.code) {
    AppLanguage.English.code -> stringResource(R.string.language_english)
    AppLanguage.Deutsch.code -> stringResource(R.string.language_german)
    else -> language.displayName
}

@Composable
private fun SettingsNavRow(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit,
    groupedInCard: Boolean = false,
    comingSoon: Boolean = false,
) {
    val horizontal = if (groupedInCard) Dimens.spacing16 else Dimens.screenHorizontalPadding
    val textStart =
        if (icon != null) (if (groupedInCard) Dimens.spacing12 else Dimens.screenHorizontalPadding) else 0.dp
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = if (comingSoon) mutedColor else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (comingSoon) 0.55f else 1f)
            .then(if (groupedInCard) Modifier.heightIn(min = 72.dp) else Modifier)
            .then(
                if (comingSoon) {
                    Modifier
                } else {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick,
                    )
                },
            )
            .padding(horizontal = horizontal, vertical = if (groupedInCard) Dimens.spacing8 else Dimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            if (groupedInCard) {
                AppIconTile(
                    icon = icon,
                    size = settingsIconTileSize,
                    iconSize = 22.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = if (comingSoon) 0.28f else 0.50f,
                    ),
                    iconTint = if (comingSoon) mutedColor else MaterialTheme.colorScheme.primary,
                )
            } else {
                AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = textStart),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (comingSoon) {
            ProfileComingSoonBadge()
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = mutedColor,
            )
        }
    }
}

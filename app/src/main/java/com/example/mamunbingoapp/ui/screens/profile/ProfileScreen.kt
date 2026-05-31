package com.example.mamunbingoapp.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mamunbingoapp.viewmodel.AccountViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppAuthMessage
import com.example.mamunbingoapp.ui.components.AppAuthMessageType
import com.example.mamunbingoapp.ui.components.ProfileAvatar
import com.example.mamunbingoapp.ui.components.profileAvatarInitials
import com.example.mamunbingoapp.ui.components.AppInsetDivider
import com.example.mamunbingoapp.ui.components.ProfileMenuItem
import com.example.mamunbingoapp.ui.components.appPremiumCardBorder
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import com.example.mamunbingoapp.theme.Secondary
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppPullRefresh
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import com.example.mamunbingoapp.ui.components.AppTopBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

private val profileSectionCardShape = RoundedCornerShape(12.dp)

private val profileMenuDividerStart =
    Dimens.spacing16 + 40.dp + Dimens.spacing16

@Composable
fun ProfileScreen(
    onTabSelected: (AppTab) -> Unit,
    onSettings: () -> Unit,
    onMyAccount: () -> Unit,
    onPaymentMethods: () -> Unit,
    onHistory: () -> Unit,
    onSupport: () -> Unit,
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit,
    showBottomBar: Boolean = true,
    accountViewModel: AccountViewModel,
    authEmail: String? = null,
    authUserId: String? = null,
    authDisplayName: String? = null,
    authAvatarUrl: String? = null,
    authAvatarInitials: String? = null,
    profileLoading: Boolean = false,
    isProfileRefreshing: Boolean = false,
    onProfileRefresh: () -> Unit = {},
    profileMessage: String? = null,
    profileMessageType: AppAuthMessageType = AppAuthMessageType.Info,
    onAvatarPicked: (Uri) -> Unit = {},
    onAvatarDelete: () -> Unit = {},
) {
    val accountProfile by accountViewModel.profile.collectAsStateWithLifecycle()
    val displayName = authDisplayName?.takeIf { it.isNotBlank() } ?: accountProfile.displayName()
    val displayEmail = authEmail ?: accountProfile.displayEmail()
    val avatarInitials = authAvatarInitials
        ?: accountProfile.avatarInitials()
        ?: profileAvatarInitials(displayName)
    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
    ) { uri ->
        if (uri != null) onAvatarPicked(uri)
    }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAvatarDialog by rememberSaveable { mutableStateOf(false) }
    val hasAvatar = !authAvatarUrl.isNullOrBlank()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val copiedMessage = stringResource(R.string.common_copied_to_clipboard)
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
        visible = showDeleteAvatarDialog,
        title = stringResource(R.string.profile_remove_photo_title),
        message = stringResource(R.string.profile_remove_photo_message),
        confirmText = stringResource(R.string.common_remove),
        cancelText = stringResource(R.string.settings_cancel),
        onConfirm = {
            showDeleteAvatarDialog = false
            onAvatarDelete()
        },
        onCancel = { showDeleteAvatarDialog = false },
        onDismiss = { showDeleteAvatarDialog = false },
    )
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(selectedTab = AppTab.Profile, onTabSelected = onTabSelected)
            }
        }
    ) { innerPadding ->
        AppHeaderPageLayout(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.profile_title),
                    actions = {
                        IconButton(onClick = onSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            content = {
        AppPullRefresh(
            isRefreshing = isProfileRefreshing,
            onRefresh = onProfileRefresh,
            modifier = Modifier.weight(1f),
            enabled = !profileLoading,
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(bottom = Dimens.spacing16)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileAvatar(
                    avatarUrl = authAvatarUrl,
                    initials = avatarInitials,
                    showEditBadge = true,
                    loading = profileLoading,
                    onPickAvatar = {
                        pickAvatarLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    },
                    onDeleteAvatar = { showDeleteAvatarDialog = true },
                )
                Text(
                    text = if (hasAvatar) {
                        stringResource(R.string.profile_tap_remove_photo)
                    } else {
                        stringResource(R.string.profile_tap_add_photo)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.spacing8),
                )
                if (!profileMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.spacing12))
                    AppAuthMessage(
                        message = profileMessage,
                        type = profileMessageType,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.spacing8),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = displayEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                authUserId?.takeIf { it.isNotBlank() }?.let { userId ->
                    Text(
                        text = stringResource(R.string.profile_id_format, userId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forest,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.profile_eco_warrior_level, 5),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.profile_impact_member_since, "June 2023"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .iosElevatedShadow(shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .appPremiumCardBorder(profileSectionCardShape)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.profile_total_wins_label),
                        style = AppTextStyles.sectionLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "12",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.profile_wins_this_week, 2),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(1.dp, 60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.profile_trees_planted_label),
                        style = AppTextStyles.sectionLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forest,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "48",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.profile_trees_this_month, 5),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            InviteParticipantsCard(
                inviteCode = "bingo.eco/live/j28k-92",
                onCopyClick = {
                    clipboardManager.setText(AnnotatedString("bingo.eco/live/j28k-92"))
                    scope.launch {
                        snackbarHostState.showSnackbar(copiedMessage)
                    }
                }
            )
            AppSectionTitle(
                text = stringResource(R.string.profile_settings_section),
                modifier = Modifier.padding(bottom = Dimens.spacing12),
            )
            val profileMenuDividerColor =
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
            Column(modifier = Modifier.fillMaxWidth()) {
                ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.profile_my_account),
                    onClick = onMyAccount
                )
                AppInsetDivider(startInset = profileMenuDividerStart, color = profileMenuDividerColor)
                ProfileMenuItem(
                    icon = Icons.Default.Payments,
                    title = stringResource(R.string.profile_payment_methods),
                    onClick = {},
                    comingSoon = true,
                )
                AppInsetDivider(startInset = profileMenuDividerStart, color = profileMenuDividerColor)
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.history_title),
                    onClick = onHistory
                )
                AppInsetDivider(startInset = profileMenuDividerStart, color = profileMenuDividerColor)
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = stringResource(R.string.profile_support),
                    onClick = {},
                    comingSoon = true,
                )
                AppInsetDivider(startInset = profileMenuDividerStart, color = profileMenuDividerColor)
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.settings_change_password),
                    onClick = onChangePassword
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showLogoutDialog = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.settings_log_out),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
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
private fun InviteParticipantsCard(
    inviteCode: String,
    onCopyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .clip(profileSectionCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .appPremiumCardBorder(profileSectionCardShape)
            .padding(24.dp)
    ) {
        AppSectionTitle(
            text = stringResource(R.string.profile_invite_section),
            modifier = Modifier.padding(bottom = Dimens.spacing16),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
                        .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacing12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inviteCode,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 0.dp)
                    )
                    IconButton(onClick = onCopyClick) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.common_copy_cd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.profile_invite_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}


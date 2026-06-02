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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.appPremiumCardBorder
import com.example.mamunbingoapp.theme.Secondary
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppBottomBarScrollExtraPadding
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
                .padding(bottom = Dimens.pageContentBottomPadding + AppBottomBarScrollExtraPadding)
        ) {
            ProfileSummaryCard(
                displayName = displayName,
                avatarUrl = authAvatarUrl,
                avatarInitials = avatarInitials,
                profileLoading = profileLoading,
                winsCount = "12",
                onPickAvatar = {
                    pickAvatarLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                },
                onDeleteAvatar = { showDeleteAvatarDialog = true },
            )
            if (!profileMessage.isNullOrBlank()) {
                AppAuthMessage(
                    message = profileMessage,
                    type = profileMessageType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.spacing12),
                )
            }
            ProfileAccountOverviewCard(
                email = displayEmail,
                memberSince = "June 2023",
                userId = authUserId,
                modifier = Modifier.padding(top = Dimens.spacing12, bottom = Dimens.spacing16),
            )
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
private fun ProfileSummaryCard(
    displayName: String,
    avatarUrl: String?,
    avatarInitials: String?,
    profileLoading: Boolean,
    winsCount: String,
    onPickAvatar: () -> Unit,
    onDeleteAvatar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    AppSectionSurface(
        modifier = modifier.fillMaxWidth(),
        shape = profileSectionCardShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(
                avatarUrl = avatarUrl,
                initials = avatarInitials,
                size = 64.dp,
                showEditBadge = true,
                loading = profileLoading,
                onPickAvatar = onPickAvatar,
                onDeleteAvatar = onDeleteAvatar,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spacing16, end = Dimens.spacing12),
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.profile_eco_warrior_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = cs.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Dimens.spacing4),
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(52.dp)
                    .background(cs.outlineVariant.copy(alpha = Dimens.outlineDividerAlpha)),
            )
            ProfileWinsHighlight(
                winsCount = winsCount,
                modifier = Modifier.padding(start = Dimens.spacing16),
            )
        }
    }
}

@Composable
private fun ProfileWinsHighlight(
    winsCount: String,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier.widthIn(min = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = cs.primary,
            )
            Text(
                text = winsCount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
        Text(
            text = stringResource(R.string.profile_stat_wins),
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant.copy(alpha = 0.72f),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(top = Dimens.spacing4),
        )
    }
}

@Composable
private fun ProfileAccountOverviewCard(
    email: String,
    memberSince: String,
    userId: String?,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val dividerColor = cs.outlineVariant.copy(alpha = 0.20f)
    AppSectionSurface(
        modifier = modifier.fillMaxWidth(),
        shape = profileSectionCardShape,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = Dimens.spacing20,
                vertical = Dimens.spacing20,
            ),
        ) {
            Text(
                text = stringResource(R.string.profile_account_overview),
                style = AppTextStyles.sectionLabel,
                color = cs.onSurfaceVariant.copy(alpha = 0.88f),
                modifier = Modifier.padding(bottom = Dimens.spacing12),
            )
            ProfileAccountOverviewRow(
                label = stringResource(R.string.profile_email_label),
            ) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AppInsetDivider(color = dividerColor)
            ProfileAccountOverviewRow(
                label = stringResource(R.string.profile_member_since_label),
            ) {
                Text(
                    text = memberSince,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            userId?.takeIf { it.isNotBlank() }?.let { id ->
                AppInsetDivider(color = dividerColor)
                ProfileAccountOverviewRow(
                    label = stringResource(R.string.profile_id_label),
                ) {
                    Text(
                        text = id,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant.copy(alpha = 0.68f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAccountOverviewRow(
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.spacing16),
            contentAlignment = Alignment.CenterEnd,
        ) {
            value()
        }
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


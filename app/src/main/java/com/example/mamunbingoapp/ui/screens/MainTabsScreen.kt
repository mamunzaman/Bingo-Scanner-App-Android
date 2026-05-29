package com.example.mamunbingoapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.screens.scan.ScanScreen
import com.example.mamunbingoapp.viewmodel.AccountViewModel

@Composable
fun MainTabsScreen(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onNavigateToLiveRoom: (String) -> Unit,
    onNavigateToLiveRooms: () -> Unit,
    /** Scan tab / camera: opens in-app CameraX with [BingoScanType] for OCR routing. */
    onNavigateToBingoLiveCamera: (BingoScanType) -> Unit,
    /** Jackpot “Scan Sheet”: opens camera without scan-type sheet (default target). */
    onJackpotScanSheet: () -> Unit,
    onNavigateToManualEntry: () -> Unit,
    onNavigateToManualEntryWithScannedNumbers: (List<Int>) -> Unit = {},
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMyAccount: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToTicketDetail: (String) -> Unit = {},
    onNavigateToHistoryDetail: (String) -> Unit = {},
    onCallNumber: (Int, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onLogout: () -> Unit,
    accountViewModel: AccountViewModel,
    authEmail: String? = null,
    authUserId: String? = null,
    authDisplayName: String? = null,
    authAvatarUrl: String? = null,
    authAvatarInitials: String? = null,
    profileLoading: Boolean = false,
    onAvatarPicked: (android.net.Uri) -> Unit = {},
    onAvatarDelete: () -> Unit = {},
    profileMessage: String? = null,
    profileMessageType: com.example.mamunbingoapp.ui.components.AppAuthMessageType =
        com.example.mamunbingoapp.ui.components.AppAuthMessageType.Info,
    isProfileRefreshing: Boolean = false,
    onProfileRefresh: () -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        bottomBar = { AppBottomBar(selectedTab = selectedTab, onTabSelected = onTabSelected) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (selectedTab) {
            AppTab.Home -> HomeScreen(
                onScanClick = onJackpotScanSheet,
                onQuickActionClick = { action ->
                    when (action) {
                        "tickets" -> onNavigateToHistory()
                        "results" -> onTabSelected(AppTab.Jackpot)
                        "help" -> onNavigateToSupport()
                        else -> onJackpotScanSheet()
                    }
                },
                onTicketClick = onNavigateToHistoryDetail,
                onViewAllTickets = onNavigateToHistory,
                onTabSelected = onTabSelected,
                showBottomBar = false,
                profileDisplayName = authDisplayName,
                profileAvatarUrl = authAvatarUrl,
                homeAvatarInitials = authAvatarInitials,
                isProfileRefreshing = isProfileRefreshing,
                onProfileRefresh = onProfileRefresh,
            )
            AppTab.Scan -> ScanScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { onTabSelected(AppTab.Home) },
                onLaunchCamera = onNavigateToBingoLiveCamera,
                onOpenNumberPad = onNavigateToManualEntry
            )
            AppTab.Jackpot -> com.example.mamunbingoapp.ui.screens.live.LiveRoomsScreen(
                onEnterRoom = onNavigateToLiveRoom,
                onCreateRoom = onNavigateToLiveRoom,
                onScanSheet = onJackpotScanSheet,
                onManualEntry = onNavigateToManualEntry,
                onHistory = onNavigateToHistory,
                onGoLivePlay = onNavigateToLiveRooms,
                onTabSelected = onTabSelected,
                showBottomBar = false
            )
            AppTab.Profile -> com.example.mamunbingoapp.ui.screens.profile.ProfileScreen(
                onTabSelected = onTabSelected,
                onSettings = onNavigateToSettings,
                onMyAccount = onNavigateToMyAccount,
                onPaymentMethods = onNavigateToPaymentMethods,
                onHistory = onNavigateToHistory,
                onSupport = onNavigateToSupport,
                onChangePassword = onNavigateToChangePassword,
                onLogout = onLogout,
                showBottomBar = false,
                accountViewModel = accountViewModel,
                authEmail = authEmail,
                authUserId = authUserId,
                authDisplayName = authDisplayName,
                authAvatarUrl = authAvatarUrl,
                authAvatarInitials = authAvatarInitials,
                profileLoading = profileLoading,
                isProfileRefreshing = isProfileRefreshing,
                onProfileRefresh = onProfileRefresh,
                onAvatarPicked = onAvatarPicked,
                onAvatarDelete = onAvatarDelete,
                profileMessage = profileMessage,
                profileMessageType = profileMessageType,
            )
            }
        }
    }
}

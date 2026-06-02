package com.example.mamunbingoapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mamunbingoapp.domain.model.BingoScanType
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
    /** Jackpot “Scan Sheet”: after type selection, opens camera with chosen [BingoScanType]. */
    onJackpotScanSheet: (BingoScanType) -> Unit,
    onNavigateToManualEntry: () -> Unit,
    onNavigateToManualEntryWithScannedNumbers: (List<Int>) -> Unit = {},
    onNavigateToHistory: () -> Unit,
    onNavigateToArchivedGames: () -> Unit = {},
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
    // Bottom navigation is provided by [MainShellScaffold] in NavGraph for all main-graph routes.
    Column(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            AppTab.Home -> HomeScreen(
                onLaunchCamera = onNavigateToBingoLiveCamera,
                onQuickActionClick = { action ->
                    when (action) {
                        "tickets" -> onNavigateToHistory()
                        "results" -> onTabSelected(AppTab.Jackpot)
                        "help" -> onNavigateToSupport()
                        else -> onJackpotScanSheet(BingoScanType.PLAY_PAPER)
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
                onLaunchCamera = onNavigateToBingoLiveCamera,
                onManualEntry = onNavigateToManualEntry,
                onHistory = onNavigateToHistory,
                onArchivedGames = onNavigateToArchivedGames,
                onGoLivePlay = onNavigateToLiveRooms,
                onTabSelected = onTabSelected,
                showBottomBar = false
            )
            AppTab.Projects -> com.example.mamunbingoapp.ui.projects.ProjectsScreen(
                modifier = Modifier.fillMaxSize(),
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

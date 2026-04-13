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
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.screens.scan.ScanScreen

@Composable
fun MainTabsScreen(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onNavigateToLiveRoom: (String) -> Unit,
    onNavigateToLiveRooms: () -> Unit,
    /** Scan tab Launch camera: same GMS Take Photo launcher as `main` → pending URI → `historyPhotoImport`. */
    onNavigateToHistoryPhotoImport: () -> Unit,
    /** Jackpot “Scan Sheet”: same launcher as Scan tab camera. */
    onJackpotScanSheet: () -> Unit,
    onNavigateToManualEntry: () -> Unit,
    onNavigateToManualEntryWithScannedNumbers: (List<Int>) -> Unit = {},
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMyAccount: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToTicketDetail: (String) -> Unit = {},
    onCallNumber: (Int, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onLogout: () -> Unit
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
                onScanClick = { },
                onQuickActionClick = { },
                onTicketClick = onNavigateToTicketDetail,
                onViewAllTickets = { },
                onTabSelected = onTabSelected,
                showBottomBar = false
            )
            AppTab.Scan -> ScanScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { onTabSelected(AppTab.Home) },
                onLaunchCamera = onNavigateToHistoryPhotoImport,
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
                onLogout = onLogout,
                showBottomBar = false
            )
            }
        }
    }
}

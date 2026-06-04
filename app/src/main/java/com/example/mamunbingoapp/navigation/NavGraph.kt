package com.example.mamunbingoapp.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.viewmodel.ImportTicketViewModel
import com.example.mamunbingoapp.viewmodel.MainTabsViewModel
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor
import com.example.mamunbingoapp.viewmodel.logMasterSheetManualPrefill
import com.example.mamunbingoapp.viewmodel.normalizeManualEntryGridPrefill
import com.example.mamunbingoapp.ui.screens.ForgotPasswordScreen
import com.example.mamunbingoapp.ui.screens.history.HistoryDetailScreen
import com.example.mamunbingoapp.ui.screens.history.HistoryListScreen
import com.example.mamunbingoapp.ui.screens.history.HistoryPhotoImportScreen
import com.example.mamunbingoapp.ui.screens.camera.BingoLiveCameraImportScreen
import com.example.mamunbingoapp.ui.screens.LoginScreen
import com.example.mamunbingoapp.ui.screens.manual.ManualEntryScreen
import com.example.mamunbingoapp.ui.screens.MainTabsScreen
import com.example.mamunbingoapp.ui.screens.PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY
import com.example.mamunbingoapp.ui.screens.PENDING_HISTORY_PHOTO_IMPORT_URI_KEY
import com.example.mamunbingoapp.ui.screens.REQUEST_SHOW_SCAN_TYPE_SHEET_KEY
import com.example.mamunbingoapp.ui.screens.SCAN_PIPELINE_BUSY_KEY
import com.example.mamunbingoapp.ui.screens.RegisterScreen
import com.example.mamunbingoapp.ui.screens.OnboardingScreen
import com.example.mamunbingoapp.ui.screens.SplashScreen
import com.example.mamunbingoapp.ui.screens.live.LivePlayScreen
import com.example.mamunbingoapp.ui.screens.live.LiveSheetDetailScreen
import com.example.mamunbingoapp.viewmodel.LivePlayUiEvent
import com.example.mamunbingoapp.viewmodel.LivePlayUiState
import com.example.mamunbingoapp.viewmodel.LivePlayViewModel
import com.example.mamunbingoapp.ui.screens.profile.ChangePasswordScreen
import com.example.mamunbingoapp.ui.screens.profile.AccountFormScreen
import com.example.mamunbingoapp.viewmodel.AccountViewModel
import com.example.mamunbingoapp.ui.screens.profile.PaymentMethodsScreen
import com.example.mamunbingoapp.ui.screens.legal.PrivacyPolicyScreen
import com.example.mamunbingoapp.ui.screens.legal.TermsOfServiceScreen
import com.example.mamunbingoapp.ui.screens.profile.EnvironmentalImpactScreen
import com.example.mamunbingoapp.ui.screens.profile.LocationServicesScreen
import com.example.mamunbingoapp.ui.screens.ticket.TicketDetailScreen
import com.example.mamunbingoapp.ui.screens.live.ArchivedGamesScreen
import com.example.mamunbingoapp.ui.screens.live.ArchivedGameDetailScreen
import com.example.mamunbingoapp.ui.screens.live.ArchivedGameTicketDetailScreen
import com.example.mamunbingoapp.viewmodel.ThemeViewModel
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.screens.profile.SettingsScreen
import com.example.mamunbingoapp.ui.screens.profile.SupportScreen
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import com.example.mamunbingoapp.viewmodel.ImportTicketDeepLinkViewModel
import com.example.mamunbingoapp.viewmodel.MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV
import com.example.mamunbingoapp.viewmodel.validFilledCellCount
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import com.example.mamunbingoapp.ui.components.AppBottomBarShellHeight
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.platform.LocalContext
import com.example.mamunbingoapp.data.SettingsRepository
import com.example.mamunbingoapp.data.auth.AuthPasswordRecoveryState
import com.example.mamunbingoapp.data.auth.AuthRepository
import com.example.mamunbingoapp.data.auth.AuthState
import com.example.mamunbingoapp.viewmodel.LoginViewModel
import com.example.mamunbingoapp.viewmodel.ProfileViewModel
import com.example.mamunbingoapp.ui.components.profileAvatarInitials
import com.example.mamunbingoapp.viewmodel.RegisterViewModel
import com.example.mamunbingoapp.history.HistoryOcrSource
/**
 * Scan / import navigation map (which composable owns which flow):
 *
 * 1) **Scan tab / Jackpot “Scan Sheet”** — `bingoLiveCameraImport` (CameraX + QR frames) or document scan: live Bingo QR **→** Manual Entry; otherwise GMS `historyPhotoImport` pending URI + `onPhotoTaken` + `analyzeTicketFromUri` (in-screen Take Photo matches).
 *
 * 2) **History take-photo import** — `HistoryListScreen` → `historyPhotoImport` → `HistoryPhotoImportScreen`; **Take photo** opens `bingoLiveCameraImport` first, same as (1).
 *
 * **Analysis:** `ImportTicketViewModel.analyzeTicketFromUri` runs ML Kit **QR** first (legacy `MAMUN_BINGO_TICKET:` or `mamunbingo://import-ticket?` + [QrTicketCodec]); on success it skips **OCR** and pre-fills like a strong scan. Otherwise the existing **OCR** path is unchanged.
 *
 * **Gallery** on that screen: **PickVisualMedia** → in-app preview → Apply → `onPhotoTaken` + `analyzeTicketFromUri`; Discard cancels pending pick. **Take photo** uses the in-app CameraX route via `bingoLiveCameraImport`.
 *
 * **NavGraph does not** compose `ImportTicketScreen`; `HistoryPhotoImportScreen` wires gallery to `rememberImportTicketGalleryImagePickLauncher`.
 *
 * Legacy `directScan` / `directDocumentScan` routes removed from the graph.
 */
private const val SCAN_ENTRY_HANDOFF_TAG = "scan-entry-handoff"
private const val SCAN_PIPELINE_LOG = "ScanPipelineBusy"
private const val MAIN_GRAPH_ROUTE = "main"
private const val MAIN_TABS_ROUTE = "tabs"

private fun stagePendingHistoryPhotoImportScanType(
    navController: NavHostController,
    scanType: BingoScanType,
) {
    runCatching {
        navController.getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle[PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY] =
            scanType.name
    }.onFailure {
        Log.w(SCAN_ENTRY_HANDOFF_TAG, "failed to stage scanType=${scanType.name}: ${it.message}")
    }
}

/** Same conditions as auto `navigate` from `historyPhotoImport`; used to hide hero image before transition. */
private fun qualifiesForHistoryPhotoAutoManualEntry(
    scanResult: com.example.mamunbingoapp.viewmodel.ScanResultUiState,
): Boolean {
    if (scanResult !is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success) return false
    val s = scanResult
    if (!s.numbers.any { it != 0 }) return false
    if (com.example.mamunbingoapp.viewmodel.validFilledCellCount(s.numbers) <
        com.example.mamunbingoapp.viewmodel.MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV
    ) {
        return false
    }
    return true
}

private fun decodeScannedNumbers(raw: String?): List<Int> =
    raw
        ?.let(Uri::decode)
        ?.split(",")
        ?.mapNotNull { it.toIntOrNull() }
        ?.take(25)
        ?.takeIf { it.isNotEmpty() }
        ?: emptyList()

private fun buildManualEntryRoute(
    scannedNumbers: List<Int>? = null,
    ocrSource: HistoryOcrSource? = null,
    ocrConfidence: Float? = null,
    prefillAsRowMajor: Boolean? = null,
    losNumber: String? = null,
    serialNumber: String? = null,
    sheetName: String? = null,
): String {
    val losQ = Uri.encode(losNumber ?: "")
    val serQ = Uri.encode(serialNumber ?: "")
    val sheetQ = Uri.encode(sheetName ?: "")
    val hasMeta = !losNumber.isNullOrBlank() || !serialNumber.isNullOrBlank() || !sheetName.isNullOrBlank()
    if (scannedNumbers.isNullOrEmpty()) {
        if (!hasMeta) return "manualEntry"
        return "manualEntry?scannedNumbers=&ocrSource=&ocrConfidence=&prefillOrder=columnMajor&losNumber=$losQ&serialNumber=$serQ&sheetName=$sheetQ"
    }
    val encoded = Uri.encode(scannedNumbers.joinToString(","))
    val sourceParam = "&ocrSource=${Uri.encode(ocrSource?.name ?: "")}"
    val confidenceParam = "&ocrConfidence=${Uri.encode(ocrConfidence?.toString() ?: "")}"
    val orderParam = "&prefillOrder=${Uri.encode(if (prefillAsRowMajor == true) "rowMajor" else "columnMajor")}"
    val metaParam = "&losNumber=$losQ&serialNumber=$serQ&sheetName=$sheetQ"
    return "manualEntry?scannedNumbers=$encoded$sourceParam$confidenceParam$orderParam$metaParam"
}

private fun buildManualEntryForRoomRoute(
    roomId: String,
    numbers: List<Int>,
    losNumber: String? = null,
    serialNumber: String? = null,
    sheetName: String? = null,
    prefillAsRowMajor: Boolean = false,
): String {
    val encoded = Uri.encode(numbers.joinToString(","))
    val orderParam = if (prefillAsRowMajor) "&prefillOrder=rowMajor" else ""
    val meta =
        if (losNumber.isNullOrBlank() && serialNumber.isNullOrBlank() && sheetName.isNullOrBlank()) ""
        else "&losNumber=${Uri.encode(losNumber ?: "")}&serialNumber=${Uri.encode(serialNumber ?: "")}&sheetName=${Uri.encode(sheetName ?: "")}"
    return "manualEntryForRoom/$roomId?scannedNumbers=$encoded$orderParam$meta"
}

private fun buildBingoLiveCameraImportRoute(
    scanType: BingoScanType = BingoScanType.PLAY_PAPER,
): String = "bingoLiveCameraImport?scanType=${Uri.encode(scanType.name)}"

private const val HISTORY_PHOTO_IMPORT_GRAPH_ROUTE =
    "historyPhotoImport?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&losNumber={losNumber}&serialNumber={serialNumber}&sheetName={sheetName}"

private const val MANUAL_ENTRY_DEEP_LINK_FALLBACK =
    "manualEntry?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&prefillOrder={prefillOrder}&losNumber={losNumber}&serialNumber={serialNumber}&sheetName={sheetName}"

private data class HistoryPhotoImportPrefill(
    val scannedNumbers: List<Int>,
    val ocrSource: HistoryOcrSource?,
    val ocrConfidence: Float?,
    val ticketMeta: ManualEntryTicketMeta,
)

private fun parseHistoryPhotoImportPrefill(arguments: android.os.Bundle?): HistoryPhotoImportPrefill {
    val scannedNumbers = decodeScannedNumbers(arguments?.getString("scannedNumbers"))
    val ocrSource = arguments?.getString("ocrSource")
        ?.takeIf { it.isNotBlank() }
        ?.let { raw -> runCatching { HistoryOcrSource.valueOf(raw) }.getOrNull() }
    val ocrConfidence = arguments?.getString("ocrConfidence")?.toFloatOrNull()
    return HistoryPhotoImportPrefill(scannedNumbers, ocrSource, ocrConfidence, arguments.parseManualEntryTicketMeta())
}

private data class ManualEntryFromNavArgs(
    val scannedNumbers: List<Int>,
    val prefillAsRowMajor: Boolean,
    val ocrSourceLabel: String?,
    val entryRouteForPopUpTo: String,
    val ticketMeta: ManualEntryTicketMeta,
)

private fun parseManualEntryFromNav(backStackEntry: NavBackStackEntry): ManualEntryFromNavArgs {
    val args = backStackEntry.arguments
    val scannedNumbers = decodeScannedNumbers(args?.getString("scannedNumbers"))
    val prefillOrder = args?.getString("prefillOrder")
    val prefillAsRowMajor = prefillOrder == "rowMajor"
    val ocrSourceLabel = args?.getString("ocrSource")?.takeIf { it.isNotBlank() }
    val entryRoute = backStackEntry.destination.route ?: MANUAL_ENTRY_DEEP_LINK_FALLBACK
    return ManualEntryFromNavArgs(
        scannedNumbers,
        prefillAsRowMajor,
        ocrSourceLabel,
        entryRoute,
        args.parseManualEntryTicketMeta(),
    )
}

private data class ManualEntryForRoomFromNavArgs(
    val roomId: String,
    val scannedNumbers: List<Int>,
    val prefillAsRowMajor: Boolean,
    val entryRouteForPopUpTo: String,
    val ticketMeta: ManualEntryTicketMeta,
)

private fun parseManualEntryForRoomFromNav(backStackEntry: NavBackStackEntry): ManualEntryForRoomFromNavArgs {
    val args = backStackEntry.arguments
    val roomId = args?.getString("roomId") ?: ""
    val scannedNumbers = decodeScannedNumbers(args?.getString("scannedNumbers"))
    val prefillAsRowMajor = args?.getString("prefillOrder") == "rowMajor"
    val entryRoute = backStackEntry.destination.route ?: "manualEntryForRoom/"
    return ManualEntryForRoomFromNavArgs(
        roomId,
        scannedNumbers,
        prefillAsRowMajor,
        entryRoute,
        args.parseManualEntryTicketMeta(),
    )
}

private fun isBlockingRouteForImportDeepLink(route: String?): Boolean {
    if (route == null) return true
    if (route == "splash" || route == "onboarding") return true
    if (route.startsWith("auth/")) return true
    return false
}

private fun isPublicAuthRoute(route: String?): Boolean = isBlockingRouteForImportDeepLink(route)

private suspend fun performLogout(navController: NavHostController) {
    AuthRepository.signOut()
    navController.navigateToLoginClearingBackStack()
}

private fun NavHostController.navigateToLoginClearingBackStack() {
    if (currentBackStackEntry?.destination?.route == "auth/login") return
    navigate("auth/login") {
        popUpTo(graph.id) {
            inclusive = true
            saveState = false
        }
        launchSingleTop = true
    }
}

@Suppress("UNUSED_PARAMETER")
private fun NavHostController.navigateToMainFromAuth(authRoute: String) {
    if (currentBackStackEntry?.destination?.route == "main") return
    navigate("main") {
        popUpTo(graph.id) {
            inclusive = true
            saveState = false
        }
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToLoginFromApp() {
    navigateToLoginClearingBackStack()
}

private fun navigateAfterOnboardingOrSplash(
    navController: NavHostController,
    authState: AuthState,
    recoveryPending: Boolean,
) {
    when (authState) {
        AuthState.Loading -> Unit
        is AuthState.SignedIn -> {
            if (recoveryPending) {
                if (navController.currentBackStackEntry?.destination?.route == "auth/forgot") return
                navController.navigate("auth/forgot") {
                    popUpTo("splash") { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        AuthState.SignedOut, is AuthState.Error -> {
            if (navController.currentBackStackEntry?.destination?.route == "auth/login") return
            navController.navigate("auth/login") {
                popUpTo("splash") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

private fun shouldRedirectToLogin(route: String?, authState: AuthState): Boolean {
    if (route == null || isPublicAuthRoute(route)) return false
    return authState is AuthState.SignedOut || authState is AuthState.Error
}

@Composable
private fun ImportTicketDeepLinkHandler(
    navController: NavHostController,
    importDeepLinkViewModel: ImportTicketDeepLinkViewModel,
) {
    val pending by importDeepLinkViewModel.pendingImportTicket.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    LaunchedEffect(pending, route) {
        val u = pending ?: return@LaunchedEffect
        if (isBlockingRouteForImportDeepLink(route)) return@LaunchedEffect
        val payload = QrTicketCodec.decode(u.toString()).getOrNull() ?: run {
            importDeepLinkViewModel.consume()
            return@LaunchedEffect
        }
        importDeepLinkViewModel.consume()
        val nums = QrTicketCodec.rowMajorFromQrGrid5x5(payload.grid)
        if (validFilledCellCount(nums) < MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV) {
            return@LaunchedEffect
        }
        val target = buildManualEntryRoute(
            nums,
            ocrSource = null,
            ocrConfidence = null,
            prefillAsRowMajor = true,
            losNumber = payload.los,
            serialNumber = payload.serial,
            sheetName = payload.sheetName.trim().takeIf { it.isNotEmpty() },
        )
        runCatching {
            navController.navigate(target) { launchSingleTop = true }
        }
    }
}

// --- Archived routes (inside [MAIN_GRAPH_ROUTE]; relative paths, encoded args) ---
private const val ARCHIVED_LIST_ROUTE = "archivedGames"
private const val ARCHIVED_DETAIL_ROUTE = "archivedGameDetail/{roomId}/{archivedAt}"
private const val ARCHIVED_TICKET_ROUTE = "archivedGameTicket/{roomId}/{archivedAt}/{ticketId}"

private data class ArchivedDetailNavArgs(val roomId: String, val archivedAt: Long)

private data class ArchivedTicketNavArgs(
    val roomId: String,
    val archivedAt: Long,
    val ticketId: String,
)

private fun decodeArchivedRouteArg(raw: String?): String? =
    raw?.let { Uri.decode(it).trim() }?.takeIf { it.isNotEmpty() }

private fun parseArchivedDetailArgs(entry: NavBackStackEntry): ArchivedDetailNavArgs? {
    val bundle = entry.arguments ?: return null
    val roomId = decodeArchivedRouteArg(bundle.getString("roomId")) ?: return null
    if (!bundle.containsKey("archivedAt")) return null
    val archivedAt = bundle.getLong("archivedAt")
    if (archivedAt <= 0L) return null
    return ArchivedDetailNavArgs(roomId, archivedAt)
}

private fun parseArchivedTicketArgs(entry: NavBackStackEntry): ArchivedTicketNavArgs? {
    val bundle = entry.arguments ?: return null
    val roomId = decodeArchivedRouteArg(bundle.getString("roomId")) ?: return null
    if (!bundle.containsKey("archivedAt")) return null
    val archivedAt = bundle.getLong("archivedAt")
    if (archivedAt <= 0L) return null
    val ticketId = decodeArchivedRouteArg(bundle.getString("ticketId")) ?: return null
    return ArchivedTicketNavArgs(roomId, archivedAt, ticketId)
}

private fun buildArchivedDetailRoute(roomId: String, archivedAt: Long): String =
    "archivedGameDetail/${Uri.encode(roomId.trim())}/$archivedAt"

private fun buildArchivedTicketRoute(roomId: String, archivedAt: Long, ticketId: String): String =
    "archivedGameTicket/${Uri.encode(roomId.trim())}/$archivedAt/${Uri.encode(ticketId.trim())}"

private fun NavHostController.navigateArchivedRoute(route: String) {
    runCatching { navigate(route) }.onFailure {
        Log.w("ArchivedNav", "navigate failed: $route", it)
    }
}

private fun NavHostController.popArchivedBackOrToTabs() {
    if (popBackStack()) return
    runCatching { popBackStack(MAIN_TABS_ROUTE, inclusive = false) }
}

/**
 * Fullscreen routes (no app bottom navigation):
 * - splash, onboarding
 * - auth/login, auth/register, auth/forgot
 * - bingoLiveCameraImport (CameraX live scanner)
 */
private fun shouldHideMainBottomBar(route: String?): Boolean {
    if (route == null) return true
    if (route == "splash" || route == "onboarding") return true
    if (route == "auth/login" || route == "auth/register" || route == "auth/forgot") return true
    if (route.startsWith("auth/")) return true
    if (route.startsWith("bingoLiveCameraImport")) return true
    return false
}

/** Live/manual only; archived/profile/history use [MainTabsViewModel] so bar taps update highlight. */
private fun appTabHighlightForRoute(route: String?): AppTab? = when {
    route == null || route == MAIN_TABS_ROUTE -> null
    route.startsWith("livePlay") || route.startsWith("liveSheet") -> AppTab.Jackpot
    route.startsWith("manualEntry") || route.startsWith("historyPhotoImport") -> AppTab.Scan
    else -> null
}

private fun stageMainShellTab(tabsViewModel: MainTabsViewModel?, tab: AppTab) {
    tabsViewModel?.setSelectedTab(tab)
}

private fun parseMainShellTabHint(raw: String?): AppTab? =
    raw?.takeIf { it.isNotBlank() }?.let { runCatching { AppTab.valueOf(it) }.getOrNull() }

/** Pop pushed routes above [MAIN_TABS_ROUTE], then show the selected tab root in [MainTabsScreen]. */
private fun NavHostController.navigateToMainTabRoot(
    tab: AppTab,
    tabsViewModel: MainTabsViewModel?,
) {
    stageMainShellTab(tabsViewModel, tab)
    runCatching {
        getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle["selectedTab"] = tab.name
    }
    if (currentDestination?.route == MAIN_TABS_ROUTE) return
    var pops = 0
    while (
        currentDestination?.route != MAIN_TABS_ROUTE &&
        currentDestination?.route != null &&
        pops < 48
    ) {
        if (!popBackStack()) break
        pops++
    }
    if (currentDestination?.route != MAIN_TABS_ROUTE) {
        runCatching {
            navigate(MAIN_TABS_ROUTE) {
                popUpTo(MAIN_GRAPH_ROUTE) { inclusive = false }
                launchSingleTop = true
                restoreState = true
            }
        }.onFailure {
            Log.w("MainShellNav", "tab root navigate failed: ${tab.name}", it)
        }
    }
}

private fun NavHostController.onMainBottomBarTabSelected(
    tab: AppTab,
    tabsViewModel: MainTabsViewModel?,
) {
    if (stageManualEntryPendingTabIfDirty(tab)) return
    if (isMainShellScanPipelineBusy()) return
    navigateToMainTabRoot(tab, tabsViewModel)
}

private fun NavHostController.isMainShellScanPipelineBusy(): Boolean {
    val mainHandle = runCatching { getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle }.getOrNull()
        ?: return false
    return mainHandle.get<Boolean>(SCAN_PIPELINE_BUSY_KEY) == true
}

/** When Manual Entry has unsaved edits, stash tab intent for the screen discard dialog. */
private fun NavHostController.stageManualEntryPendingTabIfDirty(tab: AppTab): Boolean {
    val entry = currentBackStackEntry ?: return false
    val route = entry.destination.route ?: return false
    if (!route.startsWith("manualEntry")) return false
    if (entry.savedStateHandle.get<Boolean>(
            com.example.mamunbingoapp.viewmodel.MANUAL_ENTRY_UNSAVED_DIRTY_KEY,
        ) != true
    ) {
        return false
    }
    entry.savedStateHandle[
        com.example.mamunbingoapp.viewmodel.MANUAL_ENTRY_PENDING_TAB_KEY,
    ] = tab.name
    return true
}

@Composable
private fun rememberShellTabsViewModel(navController: NavHostController): MainTabsViewModel? {
    val mainEntry = remember(navController) {
        runCatching { navController.getBackStackEntry(MAIN_GRAPH_ROUTE) }.getOrNull()
    }
    return mainEntry?.let { viewModel(it) }
}

@Composable
private fun MainShellScaffold(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = !shouldHideMainBottomBar(currentRoute)
    val mainGraphEntry = remember(navBackStackEntry?.id) {
        runCatching { navController.getBackStackEntry(MAIN_GRAPH_ROUTE) }.getOrNull()
    }
    val tabsViewModel: MainTabsViewModel? =
        if (showBottomBar) mainGraphEntry?.let { viewModel(it) } else null
    val vmSelectedTab by tabsViewModel?.selectedTab?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(AppTab.Home) }
    val tabHintFlow = remember(mainGraphEntry?.id) {
        mainGraphEntry?.savedStateHandle?.getStateFlow("selectedTab", "")
    }
    val tabHintRaw by tabHintFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf("") }
    val scanPipelineBusyFlow = remember(mainGraphEntry?.id) {
        mainGraphEntry?.savedStateHandle?.getStateFlow(SCAN_PIPELINE_BUSY_KEY, false)
    }
    val isGlobalScanBusy by scanPipelineBusyFlow?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(false) }
    val highlightedTab = appTabHighlightForRoute(currentRoute)
        ?: parseMainShellTabHint(tabHintRaw)
        ?: vmSelectedTab
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentBottomInset =
        if (showBottomBar) AppBottomBarShellHeight + navBarInset else 0.dp
    Box(modifier = Modifier.fillMaxSize()) {
        content(
            Modifier
                .fillMaxSize()
                .padding(bottom = contentBottomInset),
        )
        if (showBottomBar) {
            AppBottomBar(
                selectedTab = highlightedTab,
                tabsEnabled = !isGlobalScanBusy,
                onTabSelected = { tab ->
                    if (isGlobalScanBusy) {
                        Log.d(SCAN_PIPELINE_LOG, "MainShell bottom tab ignored: ${tab.name}")
                        return@AppBottomBar
                    }
                    navController.onMainBottomBarTabSelected(tab, tabsViewModel)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(100f),
            )
        }
    }
}

@Composable
private fun ArchivedNavInvalidArgs(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) { onDismiss() }
}

private fun SavedStateHandle.setScanPipelineBusy(busy: Boolean) {
    if (get<Boolean>(SCAN_PIPELINE_BUSY_KEY) == busy) return
    set(SCAN_PIPELINE_BUSY_KEY, busy)
    Log.d(SCAN_PIPELINE_LOG, "main savedStateHandle busy=$busy")
}

private fun NavHostController.setMainScanPipelineBusy(busy: Boolean) {
    runCatching {
        getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle.setScanPipelineBusy(busy)
    }
}

private fun clearPendingHistoryPhotoImportHandoff(navController: NavHostController) {
    runCatching {
        val mainHandle = navController.getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle
        mainHandle.remove<String>(PENDING_HISTORY_PHOTO_IMPORT_URI_KEY)
        mainHandle.remove<String>(PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY)
        mainHandle.setScanPipelineBusy(false)
    }
}

/** Duplicate-sheet Scan Another: clear import handoff, pop to tabs, show scan type sheet (Option A). */
private fun NavHostController.restartFreshScanAfterDuplicate(
    tabsViewModel: MainTabsViewModel?,
) {
    clearPendingHistoryPhotoImportHandoff(this)
    runCatching {
        getBackStackEntry(HISTORY_PHOTO_IMPORT_GRAPH_ROUTE).savedStateHandle["clearImportSession"] = true
    }
    runCatching {
        val mainHandle = getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle
        mainHandle[REQUEST_SHOW_SCAN_TYPE_SHEET_KEY] = true
        mainHandle["selectedTab"] = AppTab.Scan.name
    }
    navigateToMainTabRoot(AppTab.Scan, tabsViewModel)
}

@Composable
fun NavGraph(
    themeViewModel: ThemeViewModel,
    startDestination: String = "splash",
    importDeepLinkViewModel: ImportTicketDeepLinkViewModel,
) {
    val navController = rememberNavController()
    val authScope = rememberCoroutineScope()
    val authState by AuthRepository.authState.collectAsStateWithLifecycle()
    val passwordRecovery by AuthRepository.passwordRecovery.collectAsStateWithLifecycle()
    val recoveryPending = passwordRecovery is AuthPasswordRecoveryState.PendingSetNewPassword
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    ImportTicketDeepLinkHandler(
        navController = navController,
        importDeepLinkViewModel = importDeepLinkViewModel
    )
    var photoImportLeaveHandler by remember { mutableStateOf<((() -> Unit) -> Unit)?>(null) }
    LaunchedEffect(navController) {
        snapshotFlow { navController.currentBackStackEntry?.destination?.route }
            .collect { route ->
                if (route?.startsWith("historyPhotoImport") != true) photoImportLeaveHandler = null
            }
    }
    LaunchedEffect(authState, currentRoute, recoveryPending) {
        val route = currentRoute ?: return@LaunchedEffect
        when {
            recoveryPending -> {
                if (route != "auth/forgot") {
                    navController.navigate("auth/forgot") {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
            isPublicAuthRoute(route) -> {
                if (authState is AuthState.SignedIn && !recoveryPending &&
                    (route == "auth/login" || route == "auth/register")
                ) {
                    navController.navigateToMainFromAuth(route)
                }
            }
            shouldRedirectToLogin(route, authState) -> {
                navController.navigateToLoginFromApp()
            }
        }
    }
    MainShellScaffold(navController = navController) { shellModifier ->
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = shellModifier,
    ) {
        composable("splash") {
            var splashFinished by remember { mutableStateOf(false) }
            var skipOnboarding by remember { mutableStateOf(false) }
            SplashScreen(
                onFinished = { skip ->
                    splashFinished = true
                    skipOnboarding = skip
                },
            )
            LaunchedEffect(splashFinished, skipOnboarding, authState, recoveryPending) {
                if (!splashFinished) return@LaunchedEffect
                if (!skipOnboarding) {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                    return@LaunchedEffect
                }
                navigateAfterOnboardingOrSplash(navController, authState, recoveryPending)
            }
        }
        composable("onboarding") {
            val scope = rememberCoroutineScope()
            OnboardingScreen(
                onFinished = {
                    scope.launch {
                        SettingsRepository.setOnboardingCompleted(true)
                        if (authState is AuthState.SignedIn) {
                            val target = if (recoveryPending) "auth/forgot" else "main"
                            navController.navigate(target) {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        } else {
                            navController.navigate("auth/login") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    }
                },
            )
        }
        composable("auth/login") {
            val loginVm: LoginViewModel = viewModel()
            val isLoading by loginVm.isLoading.collectAsStateWithLifecycle()
            val validationError by loginVm.validationError.collectAsStateWithLifecycle()
            val authActionError by loginVm.authActionError.collectAsStateWithLifecycle()
            val errorMessage = validationError ?: authActionError
            LoginScreen(
                onForgotPassword = { navController.navigate("auth/forgot") },
                onLogin = loginVm::signIn,
                onRegister = { navController.navigate("auth/register") },
                isLoading = isLoading,
                errorMessage = errorMessage,
            )
        }
        composable("auth/forgot") {
            val authActionError by AuthRepository.authActionError.collectAsStateWithLifecycle()
            val authRecoveryHint by AuthRepository.authRecoveryHint.collectAsStateWithLifecycle()
            val isLoading by AuthRepository.authActionInProgress.collectAsStateWithLifecycle()
            val resetEmailSent = authRecoveryHint?.startsWith("Check your email") == true
            val recoveryEmail =
                (passwordRecovery as? AuthPasswordRecoveryState.PendingSetNewPassword)?.email
            LaunchedEffect(Unit) {
                if (!recoveryPending && !resetEmailSent) {
                    AuthRepository.clearRecoveryState(
                        clearError = true,
                        clearHint = true,
                        clearHandledDeepLink = false,
                        reason = "open_forgot_screen_normal",
                    )
                }
            }
            LaunchedEffect(authRecoveryHint, recoveryPending, authState) {
                if (recoveryPending) return@LaunchedEffect
                if (authRecoveryHint?.startsWith("Password updated") != true) return@LaunchedEffect
                AuthRepository.clearAuthRecoveryHint()
                if (authState is AuthState.SignedIn) {
                    navController.navigateToMainFromAuth("auth/forgot")
                } else {
                    navController.navigate("auth/login") {
                        popUpTo("auth/forgot") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onLogIn = { navController.popBackStack() },
                onSendResetLink = { email ->
                    authScope.launch { AuthRepository.requestPasswordResetEmail(email) }
                },
                onUpdatePassword = { password ->
                    authScope.launch { AuthRepository.updatePasswordAfterRecovery(password) }
                },
                recoveryEmail = recoveryEmail,
                errorMessage = authActionError,
                infoMessage = authRecoveryHint,
                isLoading = isLoading,
                resetEmailSent = resetEmailSent,
                recoveryPending = recoveryPending,
            )
        }
        composable("auth/register") {
            val registerVm: RegisterViewModel = viewModel()
            val isLoading by registerVm.isLoading.collectAsStateWithLifecycle()
            val validationError by registerVm.validationError.collectAsStateWithLifecycle()
            val authActionError by registerVm.authActionError.collectAsStateWithLifecycle()
            val errorMessage = validationError ?: authActionError
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onSignUp = registerVm::signUp,
                onLogin = { navController.popBackStack() },
                onForgotPassword = { navController.navigate("auth/forgot") },
                isLoading = isLoading,
                errorMessage = errorMessage,
            )
        }
        navigation(
            route = MAIN_GRAPH_ROUTE,
            startDestination = MAIN_TABS_ROUTE,
        ) {
            composable(route = MAIN_TABS_ROUTE) { backStackEntry ->
            val mainGraphEntry = remember(backStackEntry) {
                navController.getBackStackEntry(MAIN_GRAPH_ROUTE)
            }
            val tabsViewModel: MainTabsViewModel = viewModel(mainGraphEntry)
            val accountViewModel: AccountViewModel = viewModel(backStackEntry)
            val profileViewModel: ProfileViewModel = viewModel(backStackEntry)
            val selectedTab by tabsViewModel.selectedTab.collectAsState()
            val lastActiveRoomId by tabsViewModel.lastActiveRoomId.collectAsState()
            val authEmail by profileViewModel.authEmail.collectAsStateWithLifecycle()
            val authUserId by profileViewModel.authUserId.collectAsStateWithLifecycle()
            val authDisplayName by profileViewModel.displayNameInput.collectAsStateWithLifecycle()
            val authAvatarUrl by profileViewModel.avatarUrl.collectAsStateWithLifecycle()
            val profileForm by profileViewModel.profileForm.collectAsStateWithLifecycle()
            val isProfileLoading by profileViewModel.isLoading.collectAsStateWithLifecycle()
            val isProfileRefreshing by profileViewModel.isProfileRefreshing.collectAsStateWithLifecycle()
            val profileMessage by profileViewModel.uiMessage.collectAsStateWithLifecycle()
            val profileMessageType by profileViewModel.uiMessageType.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val authAvatarInitials = profileAvatarInitials(
                authDisplayName.orEmpty(),
                profileForm.fullName,
            )
            LaunchedEffect(backStackEntry) {
                profileViewModel.refreshAuthProfile()
            }
            LaunchedEffect(backStackEntry) {
                val mainHandle = navController.getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle
                mainHandle.get<String>("selectedTab")?.let { tabName ->
                    runCatching { AppTab.valueOf(tabName) }.getOrNull()?.let { tab ->
                        tabsViewModel.setSelectedTab(tab)
                        mainHandle.remove<String>("selectedTab")
                    }
                }
            }
            val requestShowScanTypeSheet by mainGraphEntry.savedStateHandle
                .getStateFlow(REQUEST_SHOW_SCAN_TYPE_SHEET_KEY, false)
                .collectAsStateWithLifecycle()
            MainTabsScreen(
                selectedTab = selectedTab,
                mainBackStackEntry = mainGraphEntry,
                requestShowScanTypeSheet = requestShowScanTypeSheet,
                onScanTypeSheetRequestConsumed = {
                    mainGraphEntry.savedStateHandle[REQUEST_SHOW_SCAN_TYPE_SHEET_KEY] = false
                },
                onTabSelected = { tabsViewModel.setSelectedTab(it) },
                onNavigateToLiveRoom = { roomId ->
                    stageMainShellTab(tabsViewModel, AppTab.Jackpot)
                    navController.navigate("livePlayRoom/$roomId")
                },
                onNavigateToLiveRooms = {},
                onNavigateToBingoLiveCamera = { scanType ->
                    stageMainShellTab(tabsViewModel, AppTab.Scan)
                    stagePendingHistoryPhotoImportScanType(navController, scanType)
                    navController.navigate(buildBingoLiveCameraImportRoute(scanType))
                },
                onJackpotScanSheet = { scanType ->
                    stageMainShellTab(tabsViewModel, AppTab.Jackpot)
                    stagePendingHistoryPhotoImportScanType(navController, scanType)
                    navController.navigate(buildBingoLiveCameraImportRoute(scanType))
                },
                onNavigateToManualEntry = {
                    stageMainShellTab(tabsViewModel, AppTab.Scan)
                    navController.navigate("manualEntry")
                },
                onNavigateToManualEntryWithScannedNumbers = { numbers ->
                    stageMainShellTab(tabsViewModel, AppTab.Scan)
                    val roomId = lastActiveRoomId
                    if (!roomId.isNullOrBlank()) {
                        navController.navigate(buildManualEntryForRoomRoute(roomId, numbers))
                    } else {
                        navController.navigate(buildManualEntryRoute(numbers))
                    }
                },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToArchivedGames = {
                    stageMainShellTab(tabsViewModel, AppTab.Jackpot)
                    navController.navigate(ARCHIVED_LIST_ROUTE) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    stageMainShellTab(tabsViewModel, AppTab.Profile)
                    navController.navigate("settings")
                },
                onNavigateToMyAccount = {
                    stageMainShellTab(tabsViewModel, AppTab.Profile)
                    navController.navigate("myAccount")
                },
                onNavigateToPaymentMethods = {
                    stageMainShellTab(tabsViewModel, AppTab.Profile)
                    navController.navigate("paymentMethods")
                },
                onNavigateToSupport = {
                    stageMainShellTab(tabsViewModel, AppTab.Profile)
                    navController.navigate("support")
                },
                onNavigateToChangePassword = {
                    stageMainShellTab(tabsViewModel, AppTab.Profile)
                    navController.navigate("changePassword")
                },
                onNavigateToTicketDetail = { id -> navController.navigate("ticket/$id") },
                onNavigateToHistoryDetail = { sessionId ->
                    val id = sessionId.trim()
                    if (id.isEmpty()) return@MainTabsScreen
                    if (com.example.mamunbingoapp.data.HistoryRepository.getAll().any { it.id == id }) {
                        navController.navigate("historyDetail/$id")
                    } else {
                        navController.navigate("ticket/$id")
                    }
                },
                onCallNumber = { n, onResult -> tabsViewModel.callNumber(n, onResult) },
                onLogout = {
                    authScope.launch { performLogout(navController) }
                },
                accountViewModel = accountViewModel,
                authEmail = authEmail,
                authUserId = authUserId,
                authDisplayName = authDisplayName,
                authAvatarUrl = authAvatarUrl,
                authAvatarInitials = authAvatarInitials,
                profileLoading = isProfileLoading,
                isProfileRefreshing = isProfileRefreshing,
                onProfileRefresh = profileViewModel::refreshProfileFromRemote,
                onAvatarPicked = { uri ->
                    profileViewModel.uploadAvatar(context, uri, cachedUserId = authUserId)
                },
                onAvatarDelete = { profileViewModel.deleteAvatar(context) },
                profileMessage = profileMessage,
                profileMessageType = profileMessageType,
            )
        }
        composable("livePlayRoom/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            if (roomId.isBlank()) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            val mainEntry = navController.getBackStackEntry("main")
            val tabsViewModel: MainTabsViewModel = viewModel(mainEntry)
            LaunchedEffect(roomId) { tabsViewModel.setLastActiveRoomId(roomId) }
            val vm: LivePlayViewModel = viewModel(backStackEntry)
            val uiState by vm.state.collectAsState()
            val showResetConfirm by vm.showResetConfirm.collectAsState()
            val showResetProtectionDialog by vm.showResetProtectionDialog.collectAsState()
            val roomConflict by vm.roomConflict.collectAsState()
            val pendingRoomId by vm.pendingNavigateToRoomId.collectAsState()
            val selectedTicketIdRequest by backStackEntry.savedStateHandle
                .getStateFlow("selectedTicketId", "")
                .collectAsState()
            var showCallCompleteDialog by remember { mutableStateOf(false) }
            LaunchedEffect(roomId) { vm.bind(roomId) }
            LaunchedEffect(selectedTicketIdRequest) {
                if (selectedTicketIdRequest.isNotBlank()) {
                    vm.selectTicket(selectedTicketIdRequest)
                    backStackEntry.savedStateHandle["selectedTicketId"] = ""
                }
            }
            LaunchedEffect(pendingRoomId) {
                pendingRoomId?.let { rid ->
                    navController.getBackStackEntry("main")?.savedStateHandle?.set("selectedTab", AppTab.Jackpot.name)
                    navController.navigate("livePlayRoom/$rid") {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(MAIN_TABS_ROUTE) { inclusive = false }
                    }
                    vm.clearPendingNavigate()
                }
            }
            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    if (event is LivePlayUiEvent.CallLimitReachedDialog) showCallCompleteDialog = true
                }
            }
            val shellTabsVm = rememberShellTabsViewModel(navController)
            LivePlayScreen(
                onBack = {
                    stageMainShellTab(shellTabsVm, AppTab.Jackpot)
                    navController.popBackStack()
                },
                showBottomBar = false,
                roomId = roomId,
                onTabSelected = { tab ->
                    navController.onMainBottomBarTabSelected(tab, shellTabsVm)
                },
                sheets = uiState.sheets,
                initialSelectedTicketId = uiState.sheets.getOrNull(uiState.selectedIndex)?.ticketId
                    ?: uiState.sheets.firstOrNull()?.ticketId
                    ?: "",
                sheetName = uiState.sheetName.ifEmpty { "Unnamed" },
                playedAtMillis = uiState.playedAtMillis,
                calledNumbers = uiState.calledNumbers,
                lastCalled = uiState.lastCalled,
                lastCalledAtMillis = uiState.lastCalledAtMillis,
                isCallLimitReached = uiState.isCallLimitReached,
                effectiveStatus = uiState.effectiveStatus,
                showCallCompleteDialog = showCallCompleteDialog,
                onCallCompleteDismiss = { showCallCompleteDialog = false },
                onOpenSheetDetail = { ticketId -> navController.navigate("liveSheetDetail/$roomId/$ticketId") },
                onNavigateToManualEntry = { navController.navigate("manualEntryForRoom/$roomId") },
                onCallNumber = { n, onResult -> vm.callNumber(n, onResult) },
                onCallRandomNumber = { vm.callRandomNumber() },
                onGoLive = { vm.addTicketToRoom(it) },
                onAddToRoom = { vm.addTicketToRoom(it) },
                showRoomConflictDialog = roomConflict.visible,
                conflictExistingRoomName = roomConflict.existingRoomName ?: "another room",
                conflictHasTargetRoom = roomConflict.targetRoomId != null,
                onDismissConflict = { vm.dismissConflict() },
                onOpenExistingRoom = { vm.openExistingRoom() },
                onMoveToTargetRoom = { vm.moveToTargetRoom() },
                onLeaveRoom = {
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", AppTab.Jackpot.name) }
                    navController.popBackStack()
                },
                showResetConfirm = showResetConfirm,
                showResetProtectionDialog = showResetProtectionDialog,
                onResetClick = { vm.onResetClick() },
                onResetConfirm = { vm.onResetConfirm() },
                onResetDismiss = { vm.onResetDismiss() },
                onStartNewRoomFromReset = { vm.onStartNewRoomFromReset() },
                onFinishClick = { vm.markRoomArchived() },
                onUndoLastCall = { vm.undoLastCalledNumber() }
            )
        }
        composable("liveSheetDetail/{roomId}/{ticketId}") { backStackEntry ->
            val routeRoomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            if (routeRoomId.isBlank() || ticketId.isBlank()) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            var repoData by remember { mutableStateOf<com.example.mamunbingoapp.data.TicketDetailData?>(null) }
            LaunchedEffect(ticketId) {
                repoData = com.example.mamunbingoapp.data.HistoryRepository.getTicketData(ticketId)
            }
            val livePlayEntry = try { navController.getBackStackEntry("livePlayRoom/$routeRoomId") } catch (_: IllegalArgumentException) { null }
            val vm: LivePlayViewModel? = livePlayEntry?.let { viewModel(it) }
            val fallbackState = remember { MutableStateFlow(LivePlayUiState()) }
            val uiState by (vm?.state ?: fallbackState).collectAsState()
            val sheet = if (vm != null) uiState.sheets.find { it.ticketId == ticketId } else null
            val calledNumbers = if (vm != null) uiState.calledNumbers else (repoData?.calledNumbers ?: emptyList())
            val lastCalled = if (vm != null) uiState.lastCalled else repoData?.calledNumbers?.lastOrNull()
            LiveSheetDetailScreen(
                ticketId = ticketId,
                roomId = routeRoomId,
                sheetName = sheet?.title ?: repoData?.sheetName?.ifEmpty { "Unnamed sheet" } ?: "Unnamed sheet",
                losNumber = sheet?.losNumber ?: repoData?.losNumber,
                serialNumber = sheet?.serialNumber ?: repoData?.serialNumber,
                playedAtMillis = sheet?.playedAtMillis ?: repoData?.playedAtMillis ?: System.currentTimeMillis(),
                cells = sheet?.cells ?: repoData?.cells,
                calledNumbers = calledNumbers,
                lastCalled = lastCalled,
                onBack = { navController.popBackStack() },
                onBackToRoom = {
                    if (!navController.popBackStack("livePlayRoom/$routeRoomId", inclusive = false)) {
                        navController.navigate("livePlayRoom/$routeRoomId") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onRemoveSheet = if (routeRoomId.isNotBlank()) {
                    {
                        com.example.mamunbingoapp.data.RoomRepository.removeTicketFromRoom(routeRoomId, ticketId)
                        vm?.refresh(null)
                        navController.popBackStack()
                    }
                } else null
            )
        }
        composable(
            route = "manualEntry?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&prefillOrder={prefillOrder}&losNumber={losNumber}&serialNumber={serialNumber}&sheetName={sheetName}",
            arguments = listOf(
                navArgument("scannedNumbers") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("ocrSource") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("ocrConfidence") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefillOrder") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("losNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("serialNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("sheetName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val me = parseManualEntryFromNav(backStackEntry)
            val shellTabsVm = rememberShellTabsViewModel(navController)
            val manualEntryVm: com.example.mamunbingoapp.viewmodel.ManualEntryViewModel = viewModel(backStackEntry)
            ManualEntryScreen(
                onBack = { navController.popBackStack() },
                scannedNumbers = me.scannedNumbers,
                prefillAsRowMajor = me.prefillAsRowMajor,
                ocrSourceLabel = me.ocrSourceLabel,
                losNumber = me.ticketMeta.losNumber,
                serialNumber = me.ticketMeta.serialNumber,
                initialSheetName = me.ticketMeta.sheetName,
                showBottomBar = false,
                onOpenExistingSheet = { ticketId ->
                    navController.navigate("historyDetail/$ticketId")
                },
                onScanAnother = {
                    manualEntryVm.dismissSheetDuplicate()
                    navController.restartFreshScanAfterDuplicate(shellTabsVm)
                },
                onSaveOnlySuccess = { _, _ ->
                    navController.popBackStack()
                },
                onTabSelected = { tab ->
                    navController.onMainBottomBarTabSelected(tab, shellTabsVm)
                },
                onNavigateToLivePlay = { roomId ->
                    navController.navigate("livePlayRoom/$roomId") {
                        popUpTo(me.entryRouteForPopUpTo) { inclusive = true }
                    }
                },
                viewModel = manualEntryVm,
            )
        }
        composable(
            route = "manualEntryForRoom/{roomId}?scannedNumbers={scannedNumbers}&prefillOrder={prefillOrder}&losNumber={losNumber}&serialNumber={serialNumber}&sheetName={sheetName}",
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("scannedNumbers") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("prefillOrder") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("losNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("serialNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("sheetName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val mer = parseManualEntryForRoomFromNav(backStackEntry)
            val shellTabsVm = rememberShellTabsViewModel(navController)
            val manualEntryRoomVm: com.example.mamunbingoapp.viewmodel.ManualEntryViewModel =
                viewModel(backStackEntry)
            ManualEntryScreen(
                onBack = { navController.popBackStack() },
                scannedNumbers = mer.scannedNumbers,
                prefillAsRowMajor = mer.prefillAsRowMajor,
                losNumber = mer.ticketMeta.losNumber,
                serialNumber = mer.ticketMeta.serialNumber,
                initialSheetName = mer.ticketMeta.sheetName,
                showBottomBar = false,
                onOpenExistingSheet = { ticketId ->
                    navController.navigate("historyDetail/$ticketId")
                },
                onScanAnother = {
                    manualEntryRoomVm.dismissSheetDuplicate()
                    navController.restartFreshScanAfterDuplicate(shellTabsVm)
                },
                onSaveOnlySuccess = { ticketId, savedRoomId ->
                    val targetRoomId = savedRoomId ?: mer.roomId
                    runCatching {
                        navController.getBackStackEntry("livePlayRoom/$targetRoomId")
                            .savedStateHandle["selectedTicketId"] = ticketId
                    }
                    if (!navController.popBackStack("livePlayRoom/$targetRoomId", inclusive = false)) {
                        navController.navigate("livePlayRoom/$targetRoomId") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onTabSelected = { tab ->
                    navController.onMainBottomBarTabSelected(tab, shellTabsVm)
                },
                onNavigateToLivePlay = { roomId ->
                    navController.navigate("livePlayRoom/$roomId") {
                        popUpTo(mer.entryRouteForPopUpTo) { inclusive = true }
                    }
                },
                viewModel = manualEntryRoomVm,
            )
        }
        composable("history") { backStackEntry ->
            val historyVm: com.example.mamunbingoapp.viewmodel.HistoryViewModel = viewModel(backStackEntry)
            val shellTabsVm = rememberShellTabsViewModel(navController)
            HistoryListScreen(
                onBack = { navController.popBackStack() },
                onSessionClick = { sessionId, _ ->
                    navController.navigate("historyDetail/$sessionId")
                },
                onJoinLiveRoom = { roomId ->
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", AppTab.Jackpot.name) }
                    navController.navigate("livePlayRoom/$roomId") {
                        popUpTo("history") { inclusive = false }
                    }
                },
                onTabSelected = { tab ->
                    navController.onMainBottomBarTabSelected(tab, shellTabsVm)
                },
                onDeleteSession = { sessionId ->
                    com.example.mamunbingoapp.data.HistoryRepository.deleteSession(sessionId)
                },
                onLeaveRoom = { sessionId ->
                    com.example.mamunbingoapp.data.RoomRepository.unassignTicket(sessionId)
                },
                onAddFromPhotoClick = {
                    runCatching {
                        navController.getBackStackEntry("historyPhotoImport")
                            .savedStateHandle["clearImportSession"] = true
                    }
                    clearPendingHistoryPhotoImportHandoff(navController)
                    navController.navigate("historyPhotoImport") {
                        launchSingleTop = true
                    }
                },
                onPlayClick = {
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", AppTab.Jackpot.name) }
                    navController.popBackStack(MAIN_TABS_ROUTE, inclusive = false)
                },
                onBulkAddToRoom = { roomId, sessionIds ->
                    historyVm.addSessionsToRoom(roomId, sessionIds)
                },
                showBottomBar = false,
                viewModel = historyVm
            )
        }
        composable(
            route = "historyPhotoImport?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&losNumber={losNumber}&serialNumber={serialNumber}&sheetName={sheetName}",
            arguments = listOf(
                navArgument("scannedNumbers") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("ocrSource") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("ocrConfidence") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("losNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("serialNumber") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("sheetName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val importVm: com.example.mamunbingoapp.viewmodel.ImportTicketViewModel =
                viewModel(backStackEntry)
            val scanPipelineBusy by importVm.scanPipelineBusy.collectAsStateWithLifecycle()
            LaunchedEffect(scanPipelineBusy) {
                navController.setMainScanPipelineBusy(scanPipelineBusy)
            }
            LaunchedEffect(backStackEntry) {
                if (backStackEntry.savedStateHandle.remove<Boolean>("clearImportSession") == true) {
                    importVm.clear()
                }
            }
            DisposableEffect(backStackEntry.lifecycle, importVm) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        importVm.clear()
                        clearPendingHistoryPhotoImportHandoff(navController)
                    }
                }
                backStackEntry.lifecycle.addObserver(observer)
                onDispose { backStackEntry.lifecycle.removeObserver(observer) }
            }
            val selectedUri by importVm.selectedImageUri.collectAsState(initial = null)
            val galleryPendingUri by importVm.galleryPendingEditUri.collectAsState(initial = null)
            val displayImportUri = galleryPendingUri ?: selectedUri
            var lastOcrSource by remember { mutableStateOf<HistoryOcrSource?>(null) }
            var lastOcrConfidence by remember { mutableStateOf<Float?>(null) }
                    var continueNavigated by remember { mutableStateOf(false) }
                    var importManualNavDone by rememberSaveable { mutableStateOf(false) }
                    val importPrefill = parseHistoryPhotoImportPrefill(backStackEntry.arguments)
            val prefilledScannedNumbers = importPrefill.scannedNumbers
            val prefilledOcrSource = importPrefill.ocrSource
            val prefilledOcrConfidence = importPrefill.ocrConfidence
            val mainEntryForHandoff = remember(navController) {
                runCatching { navController.getBackStackEntry("main") }.getOrNull()
            }
            LaunchedEffect(mainEntryForHandoff, importVm) {
                val mainEntry = mainEntryForHandoff ?: return@LaunchedEffect
                fun consumePendingImport() {
                    val pending = mainEntry.savedStateHandle
                        .get<String>(PENDING_HISTORY_PHOTO_IMPORT_URI_KEY)
                        ?.takeIf { it.isNotBlank() }
                        ?: return
                    navController.setMainScanPipelineBusy(true)
                    val pendingScanTypeName =
                        mainEntry.savedStateHandle.remove<String>(PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY)
                    mainEntry.savedStateHandle.remove<String>(PENDING_HISTORY_PHOTO_IMPORT_URI_KEY)
                    val uri = Uri.parse(pending)
                    val vmPending = importVm.peekPendingScanType()
                    val effectiveScanType = when {
                        pendingScanTypeName != null -> BingoScanType.fromRouteValue(pendingScanTypeName)
                        vmPending != null -> vmPending
                        else -> {
                            Log.w(
                                SCAN_ENTRY_HANDOFF_TAG,
                                "pending photo handoff missing scan type; defaulting PLAY_PAPER",
                            )
                            BingoScanType.PLAY_PAPER
                        }
                    }
                    importVm.setPendingScanType(effectiveScanType)
                    importVm.onPhotoTaken(uri, effectiveScanType)
                    importVm.analyzeTicketFromUri(context, uri, scanType = effectiveScanType)
                }
                consumePendingImport()
                mainEntry.savedStateHandle
                    .getStateFlow(PENDING_HISTORY_PHOTO_IMPORT_URI_KEY, "")
                    .collect { pending ->
                        if (pending.isBlank()) return@collect
                        consumePendingImport()
                    }
            }
            LaunchedEffect(prefilledScannedNumbers, selectedUri, galleryPendingUri) {
                if (selectedUri == null && galleryPendingUri == null && prefilledScannedNumbers.isNotEmpty()) {
                    val sourceLabel = when (prefilledOcrSource) {
                        HistoryOcrSource.GEMINI -> "Live Scan"
                        HistoryOcrSource.ML_KIT -> "Live Scan"
                        null -> "Live Scan"
                    }
                    importVm.prefillFromLiveScan(
                        numbers = prefilledScannedNumbers,
                        source = sourceLabel,
                        confidence = prefilledOcrConfidence,
                        losNumber = importPrefill.ticketMeta.losNumber,
                        serialNumber = importPrefill.ticketMeta.serialNumber,
                    )
                    lastOcrSource = prefilledOcrSource ?: HistoryOcrSource.ML_KIT
                    lastOcrConfidence = prefilledOcrConfidence
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                    val scanResult by importVm.scanResult.collectAsState()
                    val success = scanResult as? com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success
                    val error = scanResult as? com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error
                    val detectedCount = success?.numbers?.let { com.example.mamunbingoapp.viewmodel.validFilledCellCount(it) } ?: 0
                    val canContinue = success?.numbers?.any { it != 0 } == true
                    val mainTabsVm = runCatching {
                        viewModel<MainTabsViewModel>(navController.getBackStackEntry("main"))
                    }.getOrNull()
                    val fallbackRoomFlow = remember { MutableStateFlow<String?>(null) }
                    val lastActiveRoomId by (mainTabsVm?.lastActiveRoomId ?: fallbackRoomFlow).collectAsState()
                    val effectiveImportScanType = importVm.effectiveImportScanType()
                    fun navigateImportToManualEntry(
                        scan: com.example.mamunbingoapp.viewmodel.ScanResultUiState,
                        useActiveRoom: Boolean,
                        isMainSheet: Boolean,
                    ) {
                        if (continueNavigated || importManualNavDone) return
                        when (scan) {
                            is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success -> {
                                continueNavigated = true
                                importManualNavDone = true
                                val s = scan
                                val nums = s.numbers
                                val grid = normalizeManualEntryGridPrefill(nums)
                                if (isMainSheet) {
                                    logMasterSheetManualPrefill(grid, s.losNumber, s.serialNumber)
                                    importVm.releaseImportImageMemory()
                                }
                                val hasGrid = grid.any { it != 0 }
                                val route =
                                    if (hasGrid && useActiveRoom && !lastActiveRoomId.isNullOrBlank()) {
                                        buildManualEntryForRoomRoute(
                                            lastActiveRoomId!!,
                                            grid,
                                            losNumber = s.losNumber,
                                            serialNumber = s.serialNumber,
                                            sheetName = s.sheetName,
                                            prefillAsRowMajor = true,
                                        )
                                    } else if (hasGrid) {
                                        buildManualEntryRoute(
                                            grid,
                                            s.ocrSource ?: lastOcrSource,
                                            if (s.ocrSource != null) null else lastOcrConfidence,
                                            prefillAsRowMajor = true,
                                            losNumber = s.losNumber,
                                            serialNumber = s.serialNumber,
                                            sheetName = s.sheetName,
                                        )
                                    } else {
                                        buildManualEntryRoute(
                                            scannedNumbers = null,
                                            ocrSource = s.ocrSource ?: lastOcrSource,
                                            ocrConfidence = if (s.ocrSource != null) null else lastOcrConfidence,
                                            losNumber = s.losNumber,
                                            serialNumber = s.serialNumber,
                                            sheetName = s.sheetName,
                                        )
                                    }
                                navController.navigate(route) {
                                    popUpTo(HISTORY_PHOTO_IMPORT_GRAPH_ROUTE) { inclusive = true }
                                }
                            }
                            is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error -> {
                                continueNavigated = true
                                importManualNavDone = true
                                if (isMainSheet) {
                                    logMasterSheetManualPrefill(emptyList(), scan.losNumber, scan.serialNumber)
                                    importVm.releaseImportImageMemory()
                                }
                                val route = buildManualEntryRoute(
                                    losNumber = scan.losNumber,
                                    serialNumber = scan.serialNumber,
                                )
                                navController.navigate(route) {
                                    popUpTo(HISTORY_PHOTO_IMPORT_GRAPH_ROUTE) { inclusive = true }
                                }
                            }
                            else -> Unit
                        }
                    }
                    LaunchedEffect(scanResult, effectiveImportScanType) {
                        if (scanResult is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Loading ||
                            scanResult is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Idle
                        ) {
                            if (scanResult !is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success) {
                                continueNavigated = false
                                importManualNavDone = false
                            }
                            return@LaunchedEffect
                        }
                        if (effectiveImportScanType == BingoScanType.MAIN_SHEET) {
                            when (scanResult) {
                                is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success,
                                is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error -> {
                                    if (continueNavigated || importManualNavDone) return@LaunchedEffect
                                    navigateImportToManualEntry(
                                        scanResult,
                                        useActiveRoom = false,
                                        isMainSheet = true,
                                    )
                                }
                                else -> Unit
                            }
                            return@LaunchedEffect
                        }
                        if (!qualifiesForHistoryPhotoAutoManualEntry(scanResult)) {
                            if (scanResult !is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success) {
                                continueNavigated = false
                            }
                            return@LaunchedEffect
                        }
                        if (continueNavigated || importManualNavDone) return@LaunchedEffect
                        navigateImportToManualEntry(
                            scanResult,
                            useActiveRoom = false,
                            isMainSheet = false,
                        )
                    }
                    val suppressHeroForAutoManualNav =
                        qualifiesForHistoryPhotoAutoManualEntry(scanResult) ||
                        continueNavigated ||
                        importManualNavDone ||
                        (effectiveImportScanType == BingoScanType.MAIN_SHEET &&
                            (scanResult is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success ||
                                scanResult is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error))
                    HistoryPhotoImportScreen(
                        importViewModel = importVm,
                        onScanPipelineBusyChanged = { busy ->
                            navController.setMainScanPipelineBusy(busy)
                        },
                        onBackClick = {
                            if (importVm.isScanPipelineBusy()) return@HistoryPhotoImportScreen
                            val leave: () -> Unit = {
                                clearPendingHistoryPhotoImportHandoff(navController)
                                navController.popBackStack()
                            }
                            photoImportLeaveHandler?.invoke(leave) ?: leave()
                        },
                        onClearImageClick = {},
                        onLaunchCamera = { scanType ->
                            navController.navigate(buildBingoLiveCameraImportRoute(scanType))
                        },
                        onSaveClick = {},
                        onSaveAndRoomClick = {},
                        onScanAgainClick = {
                            importVm.clear()
                        },
                        onRetryAnalysisClick = {
                            importVm.setScanResult(com.example.mamunbingoapp.viewmodel.ScanResultUiState.Idle)
                        },
                        selectedImageUri = displayImportUri,
                        isAnalyzing = false,
                        analysisSummary = error?.message,
                        detectedCount = detectedCount,
                        canContinue = canContinue,
                        showIncompleteWarning = false,
                        showLowConfidenceWarning = false,
                        suppressHeroImage = suppressHeroForAutoManualNav,
                        onRegisterLeaveHandler = { register ->
                            photoImportLeaveHandler = register
                        },
                    )
            }
        }
        composable(
            route = "historyDetail/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            val shellTabsVm = rememberShellTabsViewModel(navController)
            val app = LocalContext.current.applicationContext as android.app.Application
            val scope = rememberCoroutineScope()
            val detailVm: com.example.mamunbingoapp.viewmodel.HistoryDetailViewModel = viewModel(
                factory = com.example.mamunbingoapp.viewmodel.HistoryDetailViewModelFactory(sessionId, app)
            )
            val detailState by detailVm.state.collectAsState()
            LaunchedEffect(detailState.pendingAddToLivePlay) {
                val handoff = detailState.pendingAddToLivePlay ?: return@LaunchedEffect
                if (handoff.roomId.isBlank() || handoff.sessionId.isBlank() || handoff.ticketId.isBlank()) {
                    detailVm.clearPendingNavigate()
                    return@LaunchedEffect
                }
                com.example.mamunbingoapp.data.HistoryRepository.addToLive(handoff.sessionId)
                runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", AppTab.Jackpot.name) }
                navController.navigate("livePlayRoom/${handoff.roomId}") {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo("history") { inclusive = false }
                }
                detailVm.clearPendingNavigate()
            }
            HistoryDetailScreen(
                sessionId = sessionId,
                session = detailState.session,
                calledNumbers = detailState.calledNumbers,
                cells = detailState.cells,
                playLogs = detailState.playLogs,
                testDateMillis = detailState.testDateMillis,
                testDrawDateLabel = detailState.testDrawDateLabel,
                testDateInfoMessage = detailState.testDateInfoMessage,
                isTestDateLoading = detailState.isTestDateLoading,
                onChangeTestDate = { millis -> detailVm.setTestDate(millis) },
                onClearTestDate = { detailVm.clearTestDate() },
                assignedRoomId = detailState.assignedRoomId,
                sheetStatus = detailState.sheetStatus,
                isLoading = detailState.isLoading,
                errorMessage = detailState.errorMessage,
                onBack = { navController.popBackStack() },
                onOpenTicketDetail = { ticketId -> navController.navigate("ticket/$ticketId") },
                onTabSelected = { tab ->
                    navController.onMainBottomBarTabSelected(tab, shellTabsVm)
                },
                onLeaveFromLive = { navController.popBackStack() },
                onAddToLivePlay = { roomId ->
                    if (roomId.isNotBlank()) detailVm.addToLivePlay(roomId)
                },
                onOpenRoom = { roomId ->
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", AppTab.Jackpot.name) }
                    navController.navigate("livePlayRoom/$roomId") {
                        popUpTo("history") { inclusive = false }
                    }
                },
                showRoomConflictDialog = detailState.showRoomConflictDialog,
                conflictExistingRoomId = detailState.conflictExistingRoomId,
                conflictExistingRoomName = detailState.conflictExistingRoomName,
                onDismissConflictDialog = { detailVm.dismissConflictDialog() },
                onResolveMoveConflict = { detailVm.resolveMoveConflict() },
                snackbarMessage = detailVm.snackbarMessage,
                actionLoading = detailState.actionLoading,
                onDuplicateSession = { sid ->
                    scope.launch {
                        val newId = com.example.mamunbingoapp.data.HistoryRepository.duplicateSession(sid)
                        if (newId != null) {
                            navController.navigate("historyDetail/$newId")
                        }
                    }
                },
                performSoftDelete = { detailVm.performSoftDelete() },
                onRestoreSession = { detailVm.restoreSession() },
                showBottomBar = false,
            )
        }
        composable("settings") {
            val shellTabsVm = rememberShellTabsViewModel(navController)
            SettingsScreen(
                onBack = { navController.popBackStack() },
                themeViewModel = themeViewModel,
                showBottomBar = false,
                onChangePassword = { navController.navigate("changePassword") },
                onLocationServices = { navController.navigate("locationServices") },
                onEnvironmentalImpact = { navController.navigate("environmentalImpact") },
                onTermsOfService = { navController.navigate("termsOfService") },
                onPrivacyPolicy = { navController.navigate("privacyPolicy") },
                onLogout = {
                    authScope.launch { performLogout(navController) }
                },
                onTabSelected = { tab ->
                    navController.onMainBottomBarTabSelected(tab, shellTabsVm)
                },
            )
        }
        composable("myAccount") {
            val mainEntry = runCatching { navController.getBackStackEntry("main") }.getOrNull()
            val profileViewModel: ProfileViewModel = if (mainEntry != null) {
                viewModel(mainEntry)
            } else {
                viewModel()
            }
            val authEmail by profileViewModel.authEmail.collectAsStateWithLifecycle()
            val authUserId by profileViewModel.authUserId.collectAsStateWithLifecycle()
            val displayNameInput by profileViewModel.displayNameInput.collectAsStateWithLifecycle()
            val emailInput by profileViewModel.emailInput.collectAsStateWithLifecycle()
            val profileForm by profileViewModel.profileForm.collectAsStateWithLifecycle()
            val profileMessage by profileViewModel.uiMessage.collectAsStateWithLifecycle()
            val profileMessageType by profileViewModel.uiMessageType.collectAsStateWithLifecycle()
            val isProfileSaving by profileViewModel.isLoading.collectAsStateWithLifecycle()
            val isProfileRefreshing by profileViewModel.isProfileRefreshing.collectAsStateWithLifecycle()
            val authAvatarUrl by profileViewModel.avatarUrl.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val avatarInitials = profileAvatarInitials(
                displayNameInput,
                profileForm.fullName,
            )
            LaunchedEffect(Unit) {
                profileViewModel.refreshAuthProfile()
            }
            AccountFormScreen(
                onBack = { navController.popBackStack() },
                signedInEmail = authEmail,
                displayName = displayNameInput,
                emailInput = emailInput,
                profileForm = profileForm,
                profileMessage = profileMessage,
                profileMessageType = profileMessageType,
                profileLoading = isProfileSaving,
                isProfileRefreshing = isProfileRefreshing,
                onProfileRefresh = profileViewModel::refreshProfileFromRemote,
                avatarUrl = authAvatarUrl,
                avatarInitials = avatarInitials,
                onAvatarPicked = { uri ->
                    profileViewModel.uploadAvatar(context, uri, cachedUserId = authUserId)
                },
                onAvatarDelete = { profileViewModel.deleteAvatar(context) },
                onDisplayNameChange = profileViewModel::updateDisplayNameInput,
                onSaveDisplayName = profileViewModel::saveDisplayName,
                onEmailChange = profileViewModel::updateEmailInput,
                onUpdateEmail = profileViewModel::saveEmail,
                onFullNameChange = profileViewModel::updateFullName,
                onSecondaryEmailChange = profileViewModel::updateSecondaryEmail,
                onPhoneChange = profileViewModel::updatePhone,
                onCountryChange = profileViewModel::updateCountry,
                onRegionChange = profileViewModel::updateRegion,
                onCityChange = profileViewModel::updateCity,
                onPostalCodeChange = profileViewModel::updatePostalCode,
                onStreetAddressChange = profileViewModel::updateStreetAddress,
                onApartmentOrHouseNoChange = profileViewModel::updateApartmentOrHouseNo,
                onBioChange = profileViewModel::updateBio,
                onSaveProfileDetails = profileViewModel::saveProfileDetails,
            )
        }
        composable("paymentMethods") {
            PaymentMethodsScreen(onBack = { navController.popBackStack() })
        }
        composable("support") {
            SupportScreen(onBack = { navController.popBackStack() })
        }
        composable("changePassword") {
            val profileVm: ProfileViewModel = viewModel()
            val isLoading by profileVm.isLoading.collectAsStateWithLifecycle()
            val message by profileVm.uiMessage.collectAsStateWithLifecycle()
            val messageType by profileVm.uiMessageType.collectAsStateWithLifecycle()
            val formResetKey by profileVm.formResetKey.collectAsStateWithLifecycle()
            ChangePasswordScreen(
                onBack = { navController.popBackStack() },
                isLoading = isLoading,
                message = message,
                messageType = messageType,
                formResetKey = formResetKey,
                onChangePassword = profileVm::changePassword,
            )
        }
        composable("locationServices") {
            LocationServicesScreen(onBack = { navController.popBackStack() })
        }
        composable("environmentalImpact") {
            EnvironmentalImpactScreen(onBack = { navController.popBackStack() })
        }
        composable("termsOfService") {
            TermsOfServiceScreen(onBack = { navController.popBackStack() })
        }
        composable("privacyPolicy") {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
            composable(ARCHIVED_LIST_ROUTE) {
                ArchivedGamesScreen(
                    onBack = { navController.popArchivedBackOrToTabs() },
                    onOpenSession = { session ->
                        navController.navigateArchivedRoute(
                            buildArchivedDetailRoute(session.roomId, session.archivedAt),
                        )
                    },
                )
            }
            composable(
                route = ARCHIVED_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.StringType },
                    navArgument("archivedAt") { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val navArgs = parseArchivedDetailArgs(backStackEntry)
                if (navArgs == null) {
                    ArchivedNavInvalidArgs(onDismiss = { navController.popBackStack() })
                    return@composable
                }
                ArchivedGameDetailScreen(
                    roomId = navArgs.roomId,
                    archivedAt = navArgs.archivedAt,
                    onBack = { navController.popBackStack() },
                    onOpenArchivedTicket = { rid, at, ticketId ->
                        navController.navigateArchivedRoute(
                            buildArchivedTicketRoute(rid, at, ticketId),
                        )
                    },
                )
            }
            composable(
                route = ARCHIVED_TICKET_ROUTE,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.StringType },
                    navArgument("archivedAt") { type = NavType.LongType },
                    navArgument("ticketId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val navArgs = parseArchivedTicketArgs(backStackEntry)
                if (navArgs == null) {
                    ArchivedNavInvalidArgs(onDismiss = { navController.popBackStack() })
                    return@composable
                }
                ArchivedGameTicketDetailScreen(
                    roomId = navArgs.roomId,
                    archivedAt = navArgs.archivedAt,
                    ticketId = navArgs.ticketId,
                    onBack = { navController.popBackStack() },
                )
            }
        composable("ticket/{ticketId}") { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            val vm: com.example.mamunbingoapp.viewmodel.TicketDetailViewModel = viewModel(backStackEntry)
            val ticketDetailScope = rememberCoroutineScope()
            val ticketData by vm.state.collectAsState()
            val pendingRoomId by vm.pendingNavigateToRoomId.collectAsState()
            LaunchedEffect(pendingRoomId) {
                pendingRoomId?.let { rid ->
                    navController.navigate("livePlayRoom/$rid")
                    vm.clearPendingNavigate()
                }
            }
            val fallbackCells = List(25) { BingoCellUi(null, false, false, false, false) }
            TicketDetailScreen(
                ticketId = ticketId,
                onBack = { navController.popBackStack() },
                onAddToLive = { _, _ -> },
                onOpenRoom = { roomId -> navController.navigate("livePlayRoom/$roomId") },
                viewModel = vm,
                onEdit = {
                    navController.popBackStack()
                    navController.navigate("manualEntry")
                },
                onDelete = {
                    ticketDetailScope.launch {
                        val sid = com.example.mamunbingoapp.data.HistoryRepository.getSessionIdForTicket(ticketId) ?: ticketId
                        com.example.mamunbingoapp.data.HistoryRepository.deleteSession(sid)
                        navController.popBackStack()
                    }
                },
                sheetName = ticketData?.sheetName?.takeIf { it.isNotBlank() },
                playedAtMillis = ticketData?.playedAtMillis ?: System.currentTimeMillis(),
                cells = ticketData?.cells ?: fallbackCells,
                calledNumbers = ticketData?.calledNumbers ?: emptyList(),
                playLogs = ticketData?.playLogs ?: emptyList(),
                serialNumber = ticketData?.serialNumber,
                losNumber = ticketData?.losNumber
            )
        }
        }
        composable(
            route = "bingoLiveCameraImport?scanType={scanType}",
            arguments = listOf(
                navArgument("scanType") {
                    type = NavType.StringType
                    defaultValue = BingoScanType.PLAY_PAPER.name
                },
            ),
        ) { backStackEntry ->
            val scanType = BingoScanType.fromRouteValue(backStackEntry.arguments?.getString("scanType"))
            val mainEntry = runCatching { navController.getBackStackEntry(MAIN_GRAPH_ROUTE) }.getOrNull()
            LaunchedEffect(scanType) {
                stagePendingHistoryPhotoImportScanType(navController, scanType)
            }
            val photoImportEntry = navController.previousBackStackEntry?.takeIf { entry ->
                entry.destination.route?.startsWith("historyPhotoImport") == true
            }
            val importVmForCameraCancel = photoImportEntry?.let { viewModel<ImportTicketViewModel>(it) }
            val tabsViewModel: MainTabsViewModel? = mainEntry?.let { viewModel(it) }
            val fallbackRoomFlow = remember { MutableStateFlow<String?>(null) }
            val lastActiveRoomId by (tabsViewModel?.lastActiveRoomId ?: fallbackRoomFlow).collectAsState()
            BingoLiveCameraImportScreen(
                scanType = scanType,
                onScanBusyChanged = { busy -> navController.setMainScanPipelineBusy(busy) },
                onBingoQrDecoded = { nums, serial, los, sheetName ->
                    navController.setMainScanPipelineBusy(true)
                    val route = if (!lastActiveRoomId.isNullOrBlank()) {
                        buildManualEntryForRoomRoute(
                            lastActiveRoomId!!,
                            nums,
                            serialNumber = serial,
                            losNumber = los,
                            sheetName = sheetName,
                            prefillAsRowMajor = true,
                        )
                    } else {
                        buildManualEntryRoute(
                            nums,
                            ocrSource = null,
                            ocrConfidence = null,
                            prefillAsRowMajor = true,
                            serialNumber = serial,
                            losNumber = los,
                            sheetName = sheetName,
                        )
                    }
                    navController.navigate(route) { popUpTo("bingoLiveCameraImport") { inclusive = true } }
                },
                onFullTicketPhotoCaptured = { uri ->
                    navController.setMainScanPipelineBusy(true)
                    runCatching {
                        val mainHandle = navController.getBackStackEntry(MAIN_GRAPH_ROUTE).savedStateHandle
                        mainHandle[PENDING_HISTORY_PHOTO_IMPORT_URI_KEY] = uri.toString()
                        mainHandle[PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY] = scanType.name
                    }.onFailure {
                        Log.w(
                            SCAN_ENTRY_HANDOFF_TAG,
                            "handoff failed to stage uri+scanType=${scanType.name}: ${it.message}",
                        )
                    }
                    navController.navigate("historyPhotoImport") {
                        popUpTo("bingoLiveCameraImport") { inclusive = true }
                    }
                },
                onScanFullTicket = {
                    stagePendingHistoryPhotoImportScanType(navController, scanType)
                    navController.navigate("historyPhotoImport") {
                        popUpTo("bingoLiveCameraImport") { inclusive = true }
                    }
                },
                onBack = {
                    importVmForCameraCancel?.clear()
                    clearPendingHistoryPhotoImportHandoff(navController)
                    navController.popBackStack()
                    Unit
                },
            )
        }
    }
    }
}

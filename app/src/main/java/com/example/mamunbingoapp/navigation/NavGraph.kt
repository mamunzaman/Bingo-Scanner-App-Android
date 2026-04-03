package com.example.mamunbingoapp.navigation

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.viewmodel.MainTabsViewModel
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor
import com.example.mamunbingoapp.ui.screens.ForgotPasswordScreen
import com.example.mamunbingoapp.ui.screens.history.HistoryDetailScreen
import com.example.mamunbingoapp.ui.screens.history.HistoryListScreen
import com.example.mamunbingoapp.ui.screens.history.HistoryPhotoImportScreen
import com.example.mamunbingoapp.ui.screens.LoginScreen
import com.example.mamunbingoapp.ui.screens.manual.ManualEntryScreen
import com.example.mamunbingoapp.ui.screens.MainTabsScreen
import com.example.mamunbingoapp.ui.screens.RegisterScreen
import com.example.mamunbingoapp.ui.screens.SplashScreen
import com.example.mamunbingoapp.ui.screens.scan.DirectScanScreen
import com.example.mamunbingoapp.ui.screens.live.LivePlayScreen
import com.example.mamunbingoapp.ui.screens.live.LiveSheetDetailScreen
import com.example.mamunbingoapp.data.preferences.LiveHeaderStyle
import com.example.mamunbingoapp.viewmodel.LivePlayUiEvent
import com.example.mamunbingoapp.viewmodel.LivePlayUiState
import com.example.mamunbingoapp.viewmodel.LivePlayViewModel
import com.example.mamunbingoapp.ui.screens.profile.ChangePasswordScreen
import com.example.mamunbingoapp.ui.screens.profile.MyAccountScreen
import com.example.mamunbingoapp.ui.screens.profile.PaymentMethodsScreen
import com.example.mamunbingoapp.ui.screens.legal.PrivacyPolicyScreen
import com.example.mamunbingoapp.ui.screens.legal.TermsOfServiceScreen
import com.example.mamunbingoapp.ui.screens.profile.EnvironmentalImpactScreen
import com.example.mamunbingoapp.ui.screens.profile.LocationServicesScreen
import com.example.mamunbingoapp.ui.screens.ticket.TicketDetailScreen
import com.example.mamunbingoapp.viewmodel.ThemeViewModel
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.screens.profile.SettingsScreen
import com.example.mamunbingoapp.ui.screens.profile.SupportScreen
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.platform.LocalContext
import com.example.mamunbingoapp.history.HistoryOcrSource
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * Scan / import navigation map (which composable owns which flow):
 *
 * 1) **Scan tab document scan** — `MainTabsScreen` → `directDocumentScan` (transient: GMS scanner only, no Import Ticket)
 *    → on success `historyPhotoImport` with URI via `main` savedStateHandle; cancel pops to Scan. Continue → `manualEntry`.
 *    Route `directScan` / `DirectScanScreen` remains in the graph; Scan tab does not use it.
 *
 * 2) **History take-photo import** — `HistoryListScreen` → `historyPhotoImport` → `HistoryPhotoImportScreen`
 *    (same route as Scan tab): document scanner → URI + preview; ticket read via Vision API TBD; prefill route args can still open Continue with numbers.
 *
 * Scan-tab camera and history import share `historyPhotoImport`; `directScan` is a separate CameraX screen still in the graph.
 */
private const val SCAN_ENTRY_HANDOFF_TAG = "scan-entry-handoff"

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
): String {
    val losQ = Uri.encode(losNumber ?: "")
    val serQ = Uri.encode(serialNumber ?: "")
    val hasMeta = !losNumber.isNullOrBlank() || !serialNumber.isNullOrBlank()
    if (scannedNumbers.isNullOrEmpty()) {
        if (!hasMeta) return "manualEntry"
        return "manualEntry?scannedNumbers=&ocrSource=&ocrConfidence=&prefillOrder=columnMajor&losNumber=$losQ&serialNumber=$serQ"
    }
    val encoded = Uri.encode(scannedNumbers.joinToString(","))
    val sourceParam = "&ocrSource=${Uri.encode(ocrSource?.name ?: "")}"
    val confidenceParam = "&ocrConfidence=${Uri.encode(ocrConfidence?.toString() ?: "")}"
    val orderParam = "&prefillOrder=${Uri.encode(if (prefillAsRowMajor == true) "rowMajor" else "columnMajor")}"
    val metaParam = "&losNumber=$losQ&serialNumber=$serQ"
    return "manualEntry?scannedNumbers=$encoded$sourceParam$confidenceParam$orderParam$metaParam"
}

private fun buildManualEntryForRoomRoute(
    roomId: String,
    numbers: List<Int>,
    losNumber: String? = null,
    serialNumber: String? = null,
): String {
    val encoded = Uri.encode(numbers.joinToString(","))
    val meta =
        if (losNumber.isNullOrBlank() && serialNumber.isNullOrBlank()) ""
        else "&losNumber=${Uri.encode(losNumber ?: "")}&serialNumber=${Uri.encode(serialNumber ?: "")}"
    return "manualEntryForRoom/$roomId?scannedNumbers=$encoded$meta"
}

private const val HISTORY_PHOTO_IMPORT_GRAPH_ROUTE =
    "historyPhotoImport?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&losNumber={losNumber}&serialNumber={serialNumber}"

private const val DIRECT_DOCUMENT_SCAN_ROUTE = "directDocumentScan"

private const val PENDING_HISTORY_PHOTO_IMPORT_URI_KEY = "pendingHistoryPhotoImportUri"

private const val MANUAL_ENTRY_DEEP_LINK_FALLBACK =
    "manualEntry?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&prefillOrder={prefillOrder}&losNumber={losNumber}&serialNumber={serialNumber}"

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
    val entryRouteForPopUpTo: String,
    val ticketMeta: ManualEntryTicketMeta,
)

private fun parseManualEntryForRoomFromNav(backStackEntry: NavBackStackEntry): ManualEntryForRoomFromNavArgs {
    val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
    val scannedNumbers = decodeScannedNumbers(backStackEntry.arguments?.getString("scannedNumbers"))
    val entryRoute = backStackEntry.destination.route ?: "manualEntryForRoom/"
    return ManualEntryForRoomFromNavArgs(
        roomId,
        scannedNumbers,
        entryRoute,
        backStackEntry.arguments.parseManualEntryTicketMeta(),
    )
}

@Composable
fun NavGraph(
    themeViewModel: ThemeViewModel,
    startDestination: String = "splash"
) {
    val navController = rememberNavController()
    var photoImportLeaveHandler by remember { mutableStateOf<((() -> Unit) -> Unit)?>(null) }
    LaunchedEffect(navController) {
        snapshotFlow { navController.currentBackStackEntry?.destination?.route }
            .collect { route ->
                if (route?.startsWith("historyPhotoImport") != true) photoImportLeaveHandler = null
            }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(2000)
                navController.navigate("auth/login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("auth/login") {
            LoginScreen(
                onForgotPassword = { navController.navigate("auth/forgot") },
                onLogin = {
                    navController.navigate("main") {
                        popUpTo("auth/login") { inclusive = true }
                    }
                },
                onRegister = { navController.navigate("auth/register") }
            )
        }
        composable("auth/forgot") {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onLogIn = { navController.popBackStack() }
            )
        }
        composable("auth/register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onSignUp = {
                    navController.navigate("main") {
                        popUpTo("auth/register") { inclusive = true }
                    }
                },
                onLogin = { navController.popBackStack() },
                onForgotPassword = { navController.navigate("auth/forgot") }
            )
        }
        composable(
            route = "main",
            exitTransition = {
                if (targetState.destination.route == "directScan") {
                    fadeOut(
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) + scaleOut(
                        targetScale = 0.99f,
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    )
                } else null
            },
            popEnterTransition = {
                if (initialState.destination.route == "directScan") {
                    fadeIn(
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) + scaleIn(
                        initialScale = 0.99f,
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    )
                } else null
            }
        ) { backStackEntry ->
            val tabsViewModel: MainTabsViewModel = viewModel(backStackEntry)
            val selectedTab by tabsViewModel.selectedTab.collectAsState()
            val lastActiveRoomId by tabsViewModel.lastActiveRoomId.collectAsState()
            LaunchedEffect(backStackEntry) {
                backStackEntry.savedStateHandle.get<String>("selectedTab")?.let { tabName ->
                    runCatching { AppTab.valueOf(tabName) }.getOrNull()?.let { tab ->
                        tabsViewModel.setSelectedTab(tab)
                        backStackEntry.savedStateHandle.remove<String>("selectedTab")
                    }
                }
            }
            MainTabsScreen(
                selectedTab = selectedTab,
                onTabSelected = { tabsViewModel.setSelectedTab(it) },
                onNavigateToLiveRoom = { roomId -> navController.navigate("livePlayRoom/$roomId") },
                onNavigateToLiveRooms = {},
                onNavigateToHistoryPhotoImport = {
                    Log.d(
                        SCAN_ENTRY_HANDOFF_TAG,
                        "handoff src=MainTabs/ScanScreen(launchCamera) dest=directDocumentScan (transient GMS scanner)"
                    )
                    navController.navigate(DIRECT_DOCUMENT_SCAN_ROUTE)
                },
                onNavigateToManualEntry = { navController.navigate("manualEntry") },
                onNavigateToManualEntryWithScannedNumbers = { numbers ->
                    val roomId = lastActiveRoomId
                    if (!roomId.isNullOrBlank()) {
                        navController.navigate(buildManualEntryForRoomRoute(roomId, numbers))
                    } else {
                        navController.navigate(buildManualEntryRoute(numbers))
                    }
                },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToMyAccount = { navController.navigate("myAccount") },
                onNavigateToPaymentMethods = { navController.navigate("paymentMethods") },
                onNavigateToSupport = { navController.navigate("support") },
                onNavigateToTicketDetail = { id -> navController.navigate("ticket/$id") },
                onCallNumber = { n, onResult -> tabsViewModel.callNumber(n, onResult) },
                onLogout = {
                    navController.navigate("auth/login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "directScan",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + slideInVertically(
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 12 }
                ) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                ) + scaleOut(
                    targetScale = 0.99f,
                    animationSpec = tween(240, easing = FastOutSlowInEasing)
                )
            }
        ) {
            DirectScanScreen(
                onBack = { navController.popBackStack() },
                onEnterNumbers = {
                    Log.d(
                        SCAN_ENTRY_HANDOFF_TAG,
                        "handoff src=directScan(DirectScanScreen) dest=manualEntry action=enterNumbers"
                    )
                    navController.navigate("manualEntry")
                }
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
            val liveHeaderStyle by vm.liveHeaderStyle.collectAsState(initial = LiveHeaderStyle.V1_CLEAN)
            val showResetConfirm by vm.showResetConfirm.collectAsState()
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
                        popUpTo("main") { inclusive = false }
                    }
                    vm.clearPendingNavigate()
                }
            }
            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    if (event is LivePlayUiEvent.CallLimitReachedDialog) showCallCompleteDialog = true
                }
            }
            LivePlayScreen(
                onBack = {
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", AppTab.Jackpot.name) }
                    navController.popBackStack()
                },
                showBottomBar = true,
                roomId = roomId,
                onTabSelected = { tab ->
                    if (tab == AppTab.Scan) Log.d("scan-flow", "FOUND entry -> ScanScreen from: NavGraph.kt")
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", tab.name) }
                    navController.popBackStack("main", inclusive = false)
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
                onResetClick = { vm.onResetClick() },
                onResetConfirm = { vm.onResetConfirm() },
                onResetDismiss = { vm.onResetDismiss() },
                onFinishClick = { vm.markRoomArchived() },
                onUndoLastCall = { vm.undoLastCalledNumber() },
                liveHeaderStyle = liveHeaderStyle
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
            route = "manualEntry?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&prefillOrder={prefillOrder}&losNumber={losNumber}&serialNumber={serialNumber}",
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
                }
            )
        ) { backStackEntry ->
            val me = parseManualEntryFromNav(backStackEntry)
            Log.d("scan-flow", "ScanScreen manual scanner path")
            Log.d(
                "HistoryPhotoImport",
                "manualEntry opened with scannedNumbers count = ${me.scannedNumbers.size}, prefillOrder = ${backStackEntry.arguments?.getString("prefillOrder")}"
            )
            ManualEntryScreen(
                onBack = { navController.popBackStack() },
                scannedNumbers = me.scannedNumbers,
                prefillAsRowMajor = me.prefillAsRowMajor,
                ocrSourceLabel = me.ocrSourceLabel,
                losNumber = me.ticketMeta.losNumber,
                serialNumber = me.ticketMeta.serialNumber,
                onSaveOnlySuccess = { _, _ ->
                    navController.popBackStack()
                },
                onTabSelected = { tab ->
                    if (tab == AppTab.Scan) Log.d("scan-flow", "FOUND entry -> ScanScreen from: NavGraph.kt")
                    runCatching { navController.getBackStackEntry("main")?.savedStateHandle?.set("selectedTab", tab.name) }
                    navController.popBackStack("main", inclusive = false)
                },
                onNavigateToLivePlay = { roomId ->
                    navController.navigate("livePlayRoom/$roomId") {
                        popUpTo(me.entryRouteForPopUpTo) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "manualEntryForRoom/{roomId}?scannedNumbers={scannedNumbers}&losNumber={losNumber}&serialNumber={serialNumber}",
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("scannedNumbers") {
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
                }
            )
        ) { backStackEntry ->
            val mer = parseManualEntryForRoomFromNav(backStackEntry)
            ManualEntryScreen(
                onBack = { navController.popBackStack() },
                scannedNumbers = mer.scannedNumbers,
                losNumber = mer.ticketMeta.losNumber,
                serialNumber = mer.ticketMeta.serialNumber,
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
                    if (tab == AppTab.Scan) Log.d("scan-flow", "FOUND entry -> ScanScreen from: NavGraph.kt")
                    runCatching { navController.getBackStackEntry("main")?.savedStateHandle?.set("selectedTab", tab.name) }
                    navController.popBackStack("main", inclusive = false)
                },
                onNavigateToLivePlay = { roomId ->
                    navController.navigate("livePlayRoom/$roomId") {
                        popUpTo(mer.entryRouteForPopUpTo) { inclusive = true }
                    }
                }
            )
        }
        composable("history") {
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
                    if (tab == AppTab.Scan) Log.d("scan-flow", "FOUND entry -> ScanScreen from: NavGraph.kt")
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", tab.name) }
                    navController.popBackStack("main", inclusive = false)
                },
                onDeleteSession = { sessionId ->
                    com.example.mamunbingoapp.data.HistoryRepository.deleteSession(sessionId)
                },
                onLeaveRoom = { sessionId ->
                    com.example.mamunbingoapp.data.RoomRepository.unassignTicket(sessionId)
                },
                onAddFromPhotoClick = {
                    Log.d(
                        SCAN_ENTRY_HANDOFF_TAG,
                        "handoff src=HistoryList(addFromPhoto) dest=historyPhotoImport screen=HistoryPhotoImportScreen"
                    )
                    navController.navigate("historyPhotoImport")
                },
                onPlayClick = {
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", AppTab.Jackpot.name) }
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }
        composable(
            route = DIRECT_DOCUMENT_SCAN_ROUTE,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            val context = LocalContext.current
            val scannerOptions = remember {
                GmsDocumentScannerOptions.Builder()
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                    .setPageLimit(1)
                    .build()
            }
            val scannerClient = remember { GmsDocumentScanning.getClient(scannerOptions) }
            val scanDocument = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                        val scannedPageUri = scanResult?.pages?.firstOrNull()?.imageUri
                        if (scannedPageUri != null) {
                            runCatching {
                                navController.getBackStackEntry("main").savedStateHandle[PENDING_HISTORY_PHOTO_IMPORT_URI_KEY] =
                                    scannedPageUri.toString()
                            }
                            Log.d(
                                SCAN_ENTRY_HANDOFF_TAG,
                                "handoff src=directDocumentScan dest=historyPhotoImport (pending URI)"
                            )
                            navController.navigate("historyPhotoImport") {
                                popUpTo(DIRECT_DOCUMENT_SCAN_ROUTE) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
                    else -> navController.popBackStack()
                }
            }
            LaunchedEffect(Unit) {
                Log.d(
                    SCAN_ENTRY_HANDOFF_TAG,
                    "handoff src=directDocumentScan dest=GmsDocumentScanner"
                )
                val activity = context.findActivity()
                if (activity == null) {
                    navController.popBackStack()
                    return@LaunchedEffect
                }
                scannerClient.getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        scanDocument.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                    .addOnFailureListener { e ->
                        Log.d("directDocumentScan", "document scanner launch failed: ${e.message}")
                        navController.popBackStack()
                    }
            }
            Box(Modifier.fillMaxSize())
        }
        composable(
            route = "historyPhotoImport?scannedNumbers={scannedNumbers}&ocrSource={ocrSource}&ocrConfidence={ocrConfidence}&losNumber={losNumber}&serialNumber={serialNumber}",
            enterTransition = {
                if (initialState.destination.route == DIRECT_DOCUMENT_SCAN_ROUTE) EnterTransition.None
                else null
            },
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
                }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val importVm: com.example.mamunbingoapp.viewmodel.ImportTicketViewModel = viewModel()
            val selectedUri by importVm.selectedImageUri.collectAsState(initial = null)
            var lastOcrSource by remember { mutableStateOf<HistoryOcrSource?>(null) }
            var lastOcrConfidence by remember { mutableStateOf<Float?>(null) }
            var continueNavigated by remember { mutableStateOf(false) }
            val importPrefill = parseHistoryPhotoImportPrefill(backStackEntry.arguments)
            val prefilledScannedNumbers = importPrefill.scannedNumbers
            val prefilledOcrSource = importPrefill.ocrSource
            val prefilledOcrConfidence = importPrefill.ocrConfidence
            val scannerOptions = remember {
                GmsDocumentScannerOptions.Builder()
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                    .setPageLimit(1)
                    .build()
            }
            val scannerClient = remember { GmsDocumentScanning.getClient(scannerOptions) }
            val scanDocument = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    val scannedPageUri = scanResult?.pages?.firstOrNull()?.imageUri
                    if (scannedPageUri != null) {
                        importVm.onPhotoTaken(scannedPageUri)
                        importVm.analyzeTicketFromUri(context, scannedPageUri)
                    } else {
                        importVm.setScanResult(com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error("Could not load scanned page"))
                    }
                }
            }
            val launchGmsDocumentScan: () -> Unit = {
                val activity = context.findActivity()
                if (activity == null) {
                    importVm.setScanResult(com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error("Scanner unavailable"))
                } else {
                    scannerClient.getStartScanIntent(activity)
                        .addOnSuccessListener { intentSender ->
                            scanDocument.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.d("HistoryPhotoImport", "document scanner launch failed: ${e.message}")
                            importVm.setScanResult(com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error("Could not start scanner"))
                        }
                }
            }
            LaunchedEffect(backStackEntry) {
                val mainEntry = runCatching { navController.getBackStackEntry("main") }.getOrNull()
                    ?: return@LaunchedEffect
                val pending = mainEntry.savedStateHandle.get<String>(PENDING_HISTORY_PHOTO_IMPORT_URI_KEY)
                    ?: return@LaunchedEffect
                mainEntry.savedStateHandle.remove<String>(PENDING_HISTORY_PHOTO_IMPORT_URI_KEY)
                if (pending.isNotBlank()) {
                    importVm.onPhotoTaken(Uri.parse(pending))
                }
            }
            LaunchedEffect(prefilledScannedNumbers, selectedUri) {
                if (selectedUri == null && prefilledScannedNumbers.isNotEmpty()) {
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
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    AppBottomBar(
                        selectedTab = AppTab.Jackpot,
                        onTabSelected = { tab ->
                            if (tab == AppTab.Scan) Log.d("scan-flow", "FOUND entry -> ScanScreen from: NavGraph.kt")
                            val action: () -> Unit = {
                                navController.getBackStackEntry("main")?.savedStateHandle?.set("selectedTab", tab.name)
                                navController.popBackStack("main", inclusive = false)
                                Unit
                            }
                            photoImportLeaveHandler?.invoke(action) ?: action()
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                ) {
                    val scanResult by importVm.scanResult.collectAsState()
                    val success = scanResult as? com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success
                    val error = scanResult as? com.example.mamunbingoapp.viewmodel.ScanResultUiState.Error
                    val detectedCount = success?.numbers?.let { finalUiGridRowMajor(it).count { n -> n != 0 } } ?: 0
                    val canContinue = success?.numbers?.any { it != 0 } == true
                    HistoryPhotoImportScreen(
                        onBackClick = {
                            importVm.clear()
                            navController.popBackStack()
                            Unit
                        },
                        onClearImageClick = {
                            importVm.clear()
                            Unit
                        },
                        onTakePhotoClick = {
                            Log.d(
                                SCAN_ENTRY_HANDOFF_TAG,
                                "handoff src=HistoryPhotoImportScreen(ui_take_photo) dest=GmsDocumentScanner"
                            )
                            launchGmsDocumentScan()
                        },
                        onContinueClick = {
                            success?.numbers?.let { nums ->
                                if (continueNavigated) {
                                    Log.d("HistoryPhotoImport", "duplicate continue ignored")
                                    return@let
                                }
                                continueNavigated = true
                                Log.d(
                                    SCAN_ENTRY_HANDOFF_TAG,
                                    "handoff src=HistoryPhotoImportScreen(continue_review) dest=manualEntry prefillCount=${nums.size}"
                                )
                                importVm.clear()
                                val route = buildManualEntryRoute(
                                    nums,
                                    success.ocrSource ?: lastOcrSource,
                                    if (success.ocrSource != null) null else lastOcrConfidence,
                                    prefillAsRowMajor = true,
                                    losNumber = success.losNumber,
                                    serialNumber = success.serialNumber,
                                )
                                Log.d("HistoryPhotoImport", "continue clicked from import review")
                                Log.d("HistoryPhotoImport", "built manual entry route = $route")
                                Log.d("HistoryPhotoImport", "navigating to manual entry")
                                navController.navigate(route) {
                                    popUpTo(HISTORY_PHOTO_IMPORT_GRAPH_ROUTE) { inclusive = true }
                                }
                            }
                        },
                        onScanAgainClick = {
                            importVm.clear()
                        },
                        onRetryAnalysisClick = {
                            importVm.setScanResult(com.example.mamunbingoapp.viewmodel.ScanResultUiState.Idle)
                        },
                        selectedImageUri = selectedUri,
                        isAnalyzing = false,
                        analysisSummary = error?.message,
                        detectedCount = detectedCount,
                        canContinue = canContinue,
                        showIncompleteWarning = false,
                        showLowConfidenceWarning = false,
                    )
                }
            }
        }
        composable(
            route = "historyDetail/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            val scope = rememberCoroutineScope()
            val detailVm: com.example.mamunbingoapp.viewmodel.HistoryDetailViewModel = viewModel(
                factory = com.example.mamunbingoapp.viewmodel.HistoryDetailViewModelFactory(sessionId)
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
                assignedRoomId = detailState.assignedRoomId,
                sheetStatus = detailState.sheetStatus,
                isLoading = detailState.isLoading,
                errorMessage = detailState.errorMessage,
                onBack = { navController.popBackStack() },
                onOpenTicketDetail = { ticketId -> navController.navigate("ticket/$ticketId") },
                onTabSelected = { tab ->
                    if (tab == AppTab.Scan) Log.d("scan-flow", "FOUND entry -> ScanScreen from: NavGraph.kt")
                    runCatching { navController.getBackStackEntry("main").savedStateHandle.set("selectedTab", tab.name) }
                    navController.popBackStack("main", inclusive = false)
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
                onRestoreSession = { detailVm.restoreSession() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                themeViewModel = themeViewModel,
                onChangePassword = { navController.navigate("changePassword") },
                onLocationServices = { navController.navigate("locationServices") },
                onEnvironmentalImpact = { navController.navigate("environmentalImpact") },
                onTermsOfService = { navController.navigate("termsOfService") },
                onPrivacyPolicy = { navController.navigate("privacyPolicy") },
                onLogout = {
                    navController.navigate("auth/login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onTabSelected = { tab ->
                    if (tab == AppTab.Scan) Log.d("scan-flow", "FOUND entry -> ScanScreen from: NavGraph.kt")
                    runCatching { navController.getBackStackEntry("main")?.savedStateHandle?.set("selectedTab", tab.name) }
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }
        composable("myAccount") {
            MyAccountScreen(onBack = { navController.popBackStack() })
        }
        composable("paymentMethods") {
            PaymentMethodsScreen(onBack = { navController.popBackStack() })
        }
        composable("support") {
            SupportScreen(onBack = { navController.popBackStack() })
        }
        composable("changePassword") {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
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
                sheetName = ticketData?.sheetName ?: "Unnamed sheet",
                playedAtMillis = ticketData?.playedAtMillis ?: System.currentTimeMillis(),
                cells = ticketData?.cells ?: fallbackCells,
                calledNumbers = ticketData?.calledNumbers ?: emptyList()
            )
        }
    }
}

package com.example.mamunbingoapp.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.viewmodel.ImportTicketViewModel
import com.example.mamunbingoapp.viewmodel.MainTabsViewModel
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

private fun stagePendingHistoryPhotoImportScanType(
    navController: NavHostController,
    scanType: BingoScanType,
) {
    runCatching {
        navController.getBackStackEntry("main").savedStateHandle[PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY] =
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

private fun clearPendingHistoryPhotoImportHandoff(navController: NavHostController) {
    runCatching {
        val mainHandle = navController.getBackStackEntry("main").savedStateHandle
        mainHandle.remove<String>(PENDING_HISTORY_PHOTO_IMPORT_URI_KEY)
        mainHandle.remove<String>(PENDING_HISTORY_PHOTO_IMPORT_SCAN_TYPE_KEY)
    }
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
    NavHost(
        navController = navController,
        startDestination = startDestination
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
        composable(route = "main") { backStackEntry ->
            val tabsViewModel: MainTabsViewModel = viewModel(backStackEntry)
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
                onNavigateToBingoLiveCamera = { scanType ->
                    stagePendingHistoryPhotoImportScanType(navController, scanType)
                    navController.navigate(buildBingoLiveCameraImportRoute(scanType))
                },
                onJackpotScanSheet = {
                    navController.navigate(buildBingoLiveCameraImportRoute(BingoScanType.PLAY_PAPER))
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
                onNavigateToChangePassword = { navController.navigate("changePassword") },
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
            val mainEntry = runCatching { navController.getBackStackEntry("main") }.getOrNull()
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
                onBingoQrDecoded = { nums, serial, los, sheetName ->
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
                    runCatching {
                        val mainHandle = navController.getBackStackEntry("main").savedStateHandle
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
            ManualEntryScreen(
                onBack = { navController.popBackStack() },
                scannedNumbers = me.scannedNumbers,
                prefillAsRowMajor = me.prefillAsRowMajor,
                ocrSourceLabel = me.ocrSourceLabel,
                losNumber = me.ticketMeta.losNumber,
                serialNumber = me.ticketMeta.serialNumber,
                initialSheetName = me.ticketMeta.sheetName,
                onSaveOnlySuccess = { _, _ ->
                    navController.popBackStack()
                },
                onTabSelected = { tab ->
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
            ManualEntryScreen(
                onBack = { navController.popBackStack() },
                scannedNumbers = mer.scannedNumbers,
                prefillAsRowMajor = mer.prefillAsRowMajor,
                losNumber = mer.ticketMeta.losNumber,
                serialNumber = mer.ticketMeta.serialNumber,
                initialSheetName = mer.ticketMeta.sheetName,
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
        composable("history") { backStackEntry ->
            val historyVm: com.example.mamunbingoapp.viewmodel.HistoryViewModel = viewModel(backStackEntry)
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
                    navController.popBackStack("main", inclusive = false)
                },
                onBulkAddToRoom = { roomId, sessionIds ->
                    historyVm.addSessionsToRoom(roomId, sessionIds)
                },
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
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    AppBottomBar(
                        selectedTab = AppTab.Jackpot,
                        onTabSelected = { tab ->
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
                    val detectedCount = success?.numbers?.let { com.example.mamunbingoapp.viewmodel.validFilledCellCount(it) } ?: 0
                    val canContinue = success?.numbers?.any { it != 0 } == true
                    val mainTabsVm = runCatching {
                        viewModel<MainTabsViewModel>(navController.getBackStackEntry("main"))
                    }.getOrNull()
                    val fallbackRoomFlow = remember { MutableStateFlow<String?>(null) }
                    val lastActiveRoomId by (mainTabsVm?.lastActiveRoomId ?: fallbackRoomFlow).collectAsState()
                    fun navigateImportToManualEntry(useActiveRoom: Boolean) {
                        val s = success ?: return
                        val nums = s.numbers
                        if (continueNavigated) {
                            return
                        }
                        continueNavigated = true
                        val route =
                            if (useActiveRoom && !lastActiveRoomId.isNullOrBlank()) {
                                buildManualEntryForRoomRoute(
                                    lastActiveRoomId!!,
                                    finalUiGridRowMajor(nums),
                                    losNumber = s.losNumber,
                                    serialNumber = s.serialNumber,
                                    sheetName = s.sheetName,
                                    prefillAsRowMajor = true,
                                )
                            } else {
                                buildManualEntryRoute(
                                    finalUiGridRowMajor(nums),
                                    s.ocrSource ?: lastOcrSource,
                                    if (s.ocrSource != null) null else lastOcrConfidence,
                                    prefillAsRowMajor = true,
                                    losNumber = s.losNumber,
                                    serialNumber = s.serialNumber,
                                    sheetName = s.sheetName,
                                )
                            }
                        navController.navigate(route) {
                            popUpTo(HISTORY_PHOTO_IMPORT_GRAPH_ROUTE) { inclusive = true }
                        }
                    }
                    LaunchedEffect(scanResult) {
                        if (!qualifiesForHistoryPhotoAutoManualEntry(scanResult)) {
                            if (scanResult !is com.example.mamunbingoapp.viewmodel.ScanResultUiState.Success) {
                                continueNavigated = false
                            }
                            return@LaunchedEffect
                        }
                        if (continueNavigated) return@LaunchedEffect
                        navigateImportToManualEntry(useActiveRoom = false)
                    }
                    val suppressHeroForAutoManualNav =
                        qualifiesForHistoryPhotoAutoManualEntry(scanResult) || continueNavigated
                    HistoryPhotoImportScreen(
                        importViewModel = importVm,
                        onBackClick = {
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
        }
        composable(
            route = "historyDetail/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
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
                    authScope.launch { performLogout(navController) }
                },
                onTabSelected = { tab ->
                    runCatching { navController.getBackStackEntry("main")?.savedStateHandle?.set("selectedTab", tab.name) }
                    navController.popBackStack("main", inclusive = false)
                }
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
}

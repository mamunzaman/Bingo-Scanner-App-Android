package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.LiveFonts
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.LiveRoomTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.BingoCardGrid
import com.example.mamunbingoapp.ui.components.BingoGridMode
import com.example.mamunbingoapp.ui.components.AlmostBingoAlertRowV2
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.ui.components.LiveCallInputBar
import com.example.mamunbingoapp.ui.components.common.bingoLetter
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.RoomSettings
import com.example.mamunbingoapp.data.preferences.LiveHeaderStyle
import com.example.mamunbingoapp.engine.RoomTimerManager
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.ui.model.RoomStatus
import com.example.mamunbingoapp.viewmodel.LivePlayUiState
import com.example.mamunbingoapp.viewmodel.LiveSheetUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatPlayDate(millis: Long): String {
    if (millis <= 0L) return "Today"
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
}

private suspend fun applyRoomCompletionEffects(
    snackbarHostState: SnackbarHostState,
    haptics: HapticFeedback,
    onShowConfetti: () -> Unit
) {
    snackbarHostState.showSnackbar(
        message = "Round complete • $MAX_LIVE_CALLS/$MAX_LIVE_CALLS",
        duration = SnackbarDuration.Short
    )
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    onShowConfetti()
}

private val LivePagerPagePeek = 24.dp
private val LivePagerPageSpacing = 12.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LivePlayScreen(
    onBack: () -> Unit,
    showBottomBar: Boolean = false,
    roomId: String = "",
    selectedTabForBottomBar: AppTab = AppTab.Jackpot,
    onTabSelected: (AppTab) -> Unit = {},
    sheets: List<LiveSheetUi> = emptyList(),
    initialSelectedTicketId: String = "",
    sheetName: String = "Today Session",
    playedAtMillis: Long = System.currentTimeMillis(),
    calledNumbers: List<Int> = emptyList(),
    lastCalled: Int? = null,
    lastCalledAtMillis: Long? = null,
    isCallLimitReached: Boolean = false,
    effectiveStatus: RoomStatus = RoomStatus.RUNNING,
    showCallCompleteDialog: Boolean = false,
    onCallCompleteDismiss: () -> Unit = {},
    onOpenSheetDetail: (String) -> Unit = {},
    onNavigateToManualEntry: () -> Unit = {},
    onCallNumber: (Int, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onCallRandomNumber: () -> Unit = {},
    onGoLive: (String) -> Unit = {},
    onAddToRoom: (String) -> Unit = {},
    onLeaveRoom: () -> Unit = {},
    showRoomConflictDialog: Boolean = false,
    conflictExistingRoomName: String = "another room",
    conflictHasTargetRoom: Boolean = false,
    onDismissConflict: () -> Unit = {},
    onOpenExistingRoom: () -> Unit = {},
    onMoveToTargetRoom: () -> Unit = {},
    showResetConfirm: Boolean = false,
    onResetClick: () -> Unit = {},
    onResetConfirm: () -> Unit = {},
    onResetDismiss: () -> Unit = {},
    onFinishClick: () -> Unit = {},
    onUndoLastCall: () -> Unit = {},
    liveHeaderStyle: LiveHeaderStyle = LiveHeaderStyle.V1_CLEAN
) {
    var selectedView by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isMyTicketsSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showLeaveRoomDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var showInfoSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteRoomConfirm by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var lastStatus by rememberSaveable { mutableStateOf<RoomStatus?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val roomStatus = effectiveStatus
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val displaySheets = sheets
    val liveInputFocusRequester = remember { FocusRequester() }
    val inputBarVisible = showBottomBar && !showSettingsSheet && !isMyTicketsSheetOpen && displaySheets.isNotEmpty()
    val canAddNumber = effectiveStatus == RoomStatus.RUNNING && !isCallLimitReached
    var didRequestFocusForBar by remember { mutableStateOf(false) }
    LaunchedEffect(inputBarVisible, canAddNumber) {
        if (!inputBarVisible || !canAddNumber) {
            didRequestFocusForBar = false
            return@LaunchedEffect
        }
        if (!didRequestFocusForBar) {
            didRequestFocusForBar = true
            liveInputFocusRequester.requestFocus()
        }
    }

    // Room completion & side effects (no central IME/focus; active input handles its own).
    LaunchedEffect(roomStatus) {
        val prev = lastStatus
        lastStatus = roomStatus
        if (prev == RoomStatus.RUNNING && roomStatus == RoomStatus.IDLE) {
            applyRoomCompletionEffects(snackbarHostState, haptics) { showConfetti = true }
        }
    }

    val roomSettings by (if (roomId.isNotBlank()) RoomRepository.roomSettingsFlow(roomId) else flowOf(null))
        .collectAsStateWithLifecycle(initialValue = null)

    val room by RoomRepository.roomFlow(roomId).collectAsState(initial = null)
    if (showInfoSheet) {
        RoomInfoBottomSheet(
            onDismiss = { showInfoSheet = false },
            roomName = room?.name ?: "—",
            roomId = roomId,
            createdAt = room?.createdAt,
            ticketsCount = displaySheets.size,
            calledCount = calledNumbers.size,
            lastCalled = lastCalled,
            isLive = roomSettings?.isRunning,
            onOpenMyTickets = {
                showInfoSheet = false
                isMyTicketsSheetOpen = true
            }
        )
    }
    if (showSettingsSheet && roomId.isNotBlank()) {
        RoomSettingsBottomSheet(
            roomId = roomId,
            settings = roomSettings,
            isCallLimitReached = isCallLimitReached,
            onDismiss = { showSettingsSheet = false },
            onDeleteRoomClick = { showDeleteRoomConfirm = true }
        )
    }

    RoomConflictDialog(
        visible = showRoomConflictDialog,
        existingRoomName = conflictExistingRoomName,
        hasTargetRoom = conflictHasTargetRoom,
        onCancel = onDismissConflict,
        onOpenExistingRoom = onOpenExistingRoom,
        onMoveToTargetRoom = onMoveToTargetRoom
    )
    if (showLeaveRoomDialog) {
        AppConfirmDialog(
            visible = true,
            title = "Leave room?",
            message = "You can return anytime. Sheets will stay in the room.",
            confirmText = "Leave",
            cancelText = "Cancel",
            showCancelButton = true,
            onConfirm = {
                showLeaveRoomDialog = false
                onLeaveRoom()
            },
            onCancel = { showLeaveRoomDialog = false },
            onDismiss = { showLeaveRoomDialog = false }
        )
    }
    if (showDeleteRoomConfirm) {
        AppConfirmDialog(
            visible = true,
            title = "Delete room?",
            message = "This will delete the room and its called numbers. Tickets will NOT be deleted.",
            confirmText = "Delete",
            cancelText = "Cancel",
            showCancelButton = true,
            onConfirm = {
                showDeleteRoomConfirm = false
                showSettingsSheet = false
                scope.launch {
                    RoomTimerManager.stopAutoCall(roomId)
                    RoomRepository.deleteRoomSync(roomId)
                    onLeaveRoom()
                }
            },
            onCancel = { showDeleteRoomConfirm = false },
            onDismiss = { showDeleteRoomConfirm = false }
        )
    }
    if (showCallCompleteDialog) {
        AlertDialog(
            onDismissRequest = onCallCompleteDismiss,
            title = { Text("Call complete") },
            text = { Text("$MAX_LIVE_CALLS numbers have been called. Calling has ended.") },
            confirmButton = {
                TextButton(onClick = onCallCompleteDismiss) { Text("OK") }
            }
        )
    }
    if (showResetConfirm) {
        AppConfirmDialog(
            visible = true,
            title = "Reset round?",
            message = "This clears called numbers. Tickets remain assigned.",
            confirmText = "Reset",
            cancelText = "Cancel",
            showCancelButton = true,
            onConfirm = {
                onResetConfirm()
                scope.launch {
                    snackbarHostState.showSnackbar("New round started", duration = SnackbarDuration.Short)
                }
            },
            onCancel = onResetDismiss,
            onDismiss = onResetDismiss
        )
    }

    val listState = rememberLazyListState()
    val showCompactBar by remember {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            val greenHeaderVisible = visible.any { it.key == "live_header" }
            !greenHeaderVisible
        }
    }
    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LiveRoomTopBar(
                title = room?.name ?: "Live Play",
                onBack = onBack,
                onAddTicket = { isMyTicketsSheetOpen = true },
                onOpenSettings = { showSettingsSheet = true },
                onOpenInfo = { showInfoSheet = true },
                onLeaveRoom = { showLeaveRoomDialog = true },
                showArchivedBadge = effectiveStatus == RoomStatus.FINISHED
            )
        },
        bottomBar = {
            if (showBottomBar && !showSettingsSheet && !isMyTicketsSheetOpen) {
                LivePlayBottomArea(
                    displaySheets = displaySheets,
                    calledNumbers = calledNumbers,
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    effectiveStatus = effectiveStatus,
                    isCallLimitReached = isCallLimitReached,
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    onCallRandomNumber = onCallRandomNumber,
                    onCallNumber = onCallNumber,
                    onUndoLastCall = onUndoLastCall,
                    selectedTabForBottomBar = selectedTabForBottomBar,
                    onTabSelected = onTabSelected,
                    showCompactBar = showCompactBar,
                    haptic = haptic,
                    focusRequester = liveInputFocusRequester
                )
            }
        }
    ) { innerPadding ->
    val cardsModeCompact = !selectedView && displaySheets.isNotEmpty()
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .then(if (cardsModeCompact) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
    ) {
    Column(
        modifier = Modifier
            .then(if (cardsModeCompact) Modifier.fillMaxWidth().wrapContentHeight().padding(bottom = Dimens.spacing8) else Modifier.fillMaxSize())
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
    ) {
        MyTicketsBottomSheet(
            visible = isMyTicketsSheetOpen,
            onDismiss = { isMyTicketsSheetOpen = false },
            roomId = roomId,
            onAddToRoom = { sessionId ->
                isMyTicketsSheetOpen = false
                onAddToRoom(sessionId)
            },
            onGoLive = { sessionId ->
                isMyTicketsSheetOpen = false
                onGoLive(sessionId)
            },
            onCreateTicket = {
                isMyTicketsSheetOpen = false
                onNavigateToManualEntry()
            }
        )
        Column(modifier = if (selectedView) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth().wrapContentHeight()) {
            if (displaySheets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.screenHorizontalPadding)
                        .padding(bottom = Dimens.inputBarHeight + Dimens.spacing16)
                ) {
                    AnimatedVisibility(
                        visible = roomSettings?.isRunning == true && !isCallLimitReached,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ResumeAutoCallBanner(roomId = roomId, onResume = { RoomTimerManager.setAutoCallRunning(roomId, true) })
                    }
                    LiveEmptyState(
                        onAddTicket = { isMyTicketsSheetOpen = true },
                        onManualEntry = onNavigateToManualEntry,
                        onScanSheet = { onTabSelected(AppTab.Scan) }
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (selectedView) Modifier.weight(1f) else Modifier.wrapContentHeight())
                        .animateContentSize(),
                    contentPadding = PaddingValues(
                        start = Dimens.screenHorizontalPadding,
                        end = Dimens.screenHorizontalPadding,
                        top = Dimens.spacing8,
                        bottom = if (selectedView) Dimens.inputBarHeight + Dimens.spacing16 else 0.dp
                    )
                ) {
                    item(key = "resume_banner") {
                        Column(modifier = Modifier.padding(bottom = Dimens.spacing8)) {
                            AnimatedVisibility(
                                visible = roomSettings?.isRunning == true && !isCallLimitReached,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                ResumeAutoCallBanner(roomId = roomId, onResume = { RoomTimerManager.setAutoCallRunning(roomId, true) })
                            }
                        }
                    }
                    item(key = "live_header") {
                        Box {
                            LiveRoomTopSection(
                                uiState = LivePlayUiState(
                                    sheetName = sheetName,
                                    calledNumbers = calledNumbers,
                                    lastCalled = lastCalled,
                                    lastCalledAtMillis = lastCalledAtMillis,
                                    effectiveStatus = effectiveStatus
                                ),
                                onFinish = {
                                    onFinishClick()
                                    scope.launch { snackbarHostState.showSnackbar("Room archived") }
                                },
                                onReset = onResetClick,
                                onTapToCallRandom = {
                                    if (calledNumbers.size >= MAX_LIVE_CALLS) scope.launch {
                                        snackbarHostState.showSnackbar("Round complete — Finish or Reset", duration = SnackbarDuration.Short)
                                    } else onCallRandomNumber()
                                },
                                liveHeaderStyle = liveHeaderStyle
                            )
                        }
                    }
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(1f)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = Dimens.spacing8)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                CalledHistoryPanel(
                                    calledNumbers = calledNumbers.takeLast(MAX_LIVE_CALLS),
                                    isCallLimitReached = isCallLimitReached
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = Dimens.spacing8),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = Dimens.cardBorderDefault
                                )
                                Box(modifier = Modifier.padding(top = Dimens.spacing8, bottom = Dimens.spacing16)) {
                                    ViewToggleChipRow(
                                        selectedCards = !selectedView,
                                        onCardsClick = { selectedView = false },
                                        onListClick = { selectedView = true }
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = Dimens.cardBorderDefault
                                )
                            }
                        }
                    }
                    item(key = "sticky_content_gap") {
                        Spacer(modifier = Modifier.height(Dimens.spacing12))
                    }
                    if (selectedView) {
                        itemsIndexed(displaySheets, key = { _, sheet -> sheet.ticketId }) { index, sheet ->
                            Box(
                                modifier = Modifier
                                    .padding(bottom = Dimens.spacing16)
                                    .then(if (index == 0) Modifier else Modifier.padding(top = Dimens.spacing16))
                            ) {
                                ListSheetRow(
                                    title = sheet.title,
                                    marked = "Marked: ${sheet.markedCount}/25",
                                    cells = sheet.cells,
                                    onClick = { onOpenSheetDetail(sheet.ticketId) }
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier.padding(bottom = if (selectedView) Dimens.spacing16 else 0.dp)
                            ) {
                                BingoSheetsCarousel(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                    sheets = displaySheets,
                                    initialSelectedTicketId = initialSelectedTicketId
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
    if (showConfetti) {
        ConfettiOverlay(
            modifier = Modifier.fillMaxSize(),
            onDone = { showConfetti = false }
        )
    }
    }
}

@Composable
private fun LivePlayBottomArea(
    displaySheets: List<LiveSheetUi>,
    calledNumbers: List<Int>,
    inputText: String,
    onInputChange: (String) -> Unit,
    effectiveStatus: RoomStatus,
    isCallLimitReached: Boolean,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onCallRandomNumber: () -> Unit,
    onCallNumber: (Int, (Boolean) -> Unit) -> Unit,
    onUndoLastCall: () -> Unit,
    selectedTabForBottomBar: AppTab,
    onTabSelected: (AppTab) -> Unit,
    showCompactBar: Boolean,
    haptic: HapticFeedback,
    focusRequester: FocusRequester
) {
    val actionGuard = remember { mutableStateOf(false) }
    val sizeWhenGuardSet = remember { mutableStateOf<Int?>(null) }
    fun releaseGuard() {
        actionGuard.value = false
        sizeWhenGuardSet.value = null
    }
    LaunchedEffect(calledNumbers.size, actionGuard.value) {
        if (!actionGuard.value) {
            sizeWhenGuardSet.value = null
            return@LaunchedEffect
        }
        val expected = sizeWhenGuardSet.value ?: return@LaunchedEffect
        if (calledNumbers.size != expected) {
            releaseGuard()
        }
    }

    fun handleCallClick() {
        val raw = inputText.trim()
        val n = raw.toIntOrNull()
        if (calledNumbers.size >= MAX_LIVE_CALLS) {
            scope.launch { snackbarHostState.showSnackbar("Round complete — Finish or Reset", duration = SnackbarDuration.Short) }
            releaseGuard()
            return
        }
        when (effectiveStatus) {
            RoomStatus.FINISHED -> {
                scope.launch { snackbarHostState.showSnackbar("Room archived — Reset to start again", duration = SnackbarDuration.Short) }
                releaseGuard()
                return
            }
            RoomStatus.IDLE -> {
                scope.launch { snackbarHostState.showSnackbar("Round complete — Finish or Reset", duration = SnackbarDuration.Short) }
                releaseGuard()
                return
            }
            RoomStatus.RUNNING -> {
                if (raw.isNotEmpty()) {
                    if (n == null || n !in 1..75) {
                        scope.launch { snackbarHostState.showSnackbar("Enter a number from 1 to 75", duration = SnackbarDuration.Short) }
                        releaseGuard()
                        return
                    }
                    onCallNumber(n) { added ->
                        if (added) {
                            onInputChange("")
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("${bingoLetter(n)}-$n already called") }
                            releaseGuard()
                        }
                    }
                } else {
                    onCallRandomNumber()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            LiveCallInputBar(
                progressText = "${calledNumbers.size}/$MAX_LIVE_CALLS",
                enterLabel = "1",
                enterRange = "–75",
                inputText = inputText,
                onInputChange = onInputChange,
                canAddNumber = effectiveStatus == RoomStatus.RUNNING && !isCallLimitReached,
                actionInProgress = actionGuard.value,
                onCallClick = {
                    if (actionGuard.value) return@LiveCallInputBar
                    actionGuard.value = true
                    sizeWhenGuardSet.value = calledNumbers.size
                    handleCallClick()
                },
                onBackClick = {
                    if (actionGuard.value) return@LiveCallInputBar
                    actionGuard.value = true
                    sizeWhenGuardSet.value = calledNumbers.size
                    onUndoLastCall()
                },
                focusRequester = focusRequester
            )
            AppBottomBar(
                selectedTab = selectedTabForBottomBar,
                onTabSelected = onTabSelected,
                showTopShadow = !showCompactBar
            )
        }
    }
}

@Composable
private fun LiveEmptyState(
    onAddTicket: () -> Unit = {},
    onManualEntry: () -> Unit,
    onScanSheet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
    ) {
        Box(
            modifier = Modifier
                .size(192.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBasket,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 16.dp)
            )
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Icon(
                imageVector = Icons.Default.EnergySavingsLeaf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = (-8).dp)
            )
            Icon(
                imageVector = Icons.Default.EnergySavingsLeaf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 16.dp, y = 16.dp)
                    .rotate(45f)
            )
        }
        Text(
            text = "No Bingo Sheets Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Add a ticket to start playing in this room.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Dimens.spacing8))
        AppPrimaryButton(
            text = "Add Ticket",
            onClick = onAddTicket,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconCompact),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        )
        OutlinedButton(
            onClick = onManualEntry,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight),
            shape = RoundedCornerShape(Dimens.radiusCard)
        ) {
            Text(
                text = "Manual Entry",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            text = "You can add multiple sheets and play live.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

private fun formatLastCalledAgo(lastCalledAtMillis: Long?, nowMillis: Long): String {
    if (lastCalledAtMillis == null) return "00"
    val elapsedSec = (nowMillis - lastCalledAtMillis) / 1000
    return when {
        elapsedSec < 60 -> "${elapsedSec} sec ago"
        elapsedSec < 3600 -> "${elapsedSec / 60} min ago"
        else -> "${elapsedSec / 3600} h ago"
    }
}

@Composable
private fun LivePill(
    label: String,
    dot: Boolean,
    dotAlpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.heightIn(max = Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.radiusSmall),
        color = colorScheme.surface.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.onPrimary.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
        ) {
            if (dot) {
                Box(
                    modifier = Modifier
                        .size(Dimens.spacing8)
                        .background(colorScheme.error.copy(alpha = dotAlpha), CircleShape)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun CalledBadge(
    calledCount: Int,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.radiusSmall),
        color = colorScheme.surface.copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.onPrimary.copy(alpha = 0.22f))
    ) {
        Text(
            text = "$calledCount / $maxCount",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = colorScheme.onPrimary.copy(alpha = 0.92f),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            modifier = Modifier.padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing8)
        )
    }
}

@Composable
private fun HostControlBar(
    calledCount: Int,
    roomStatus: RoomStatus,
    onResetClick: () -> Unit,
    onFinishClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isFinished = roomStatus == RoomStatus.FINISHED
    val finishInteractionSource = remember { MutableInteractionSource() }
    val resetInteractionSource = remember { MutableInteractionSource() }
    val finishPressed by finishInteractionSource.collectIsPressedAsState()
    val resetPressed by resetInteractionSource.collectIsPressedAsState()
    val finishScale by animateFloatAsState(if (finishPressed) 0.95f else 1f, label = "finishScale")
    val resetScale by animateFloatAsState(if (resetPressed) 0.95f else 1f, label = "resetScale")
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacing8),
        color = colorScheme.surface,
        shape = RoundedCornerShape(Dimens.radiusSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isFinished) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ROUND COMPLETE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Room archived",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurface
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ROUND COMPLETE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$calledCount",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = colorScheme.primary
                        )
                        Text(
                            text = "/$MAX_LIVE_CALLS",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onFinishClick,
                    interactionSource = finishInteractionSource,
                    modifier = Modifier
                        .height(Dimens.buttonHeight)
                        .graphicsLayer(scaleX = finishScale, scaleY = finishScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.errorContainer,
                        contentColor = colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = Dimens.spacing16),
                    shape = RoundedCornerShape(Dimens.radiusSmall),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                ) {
                    Text("Finish", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold))
                }
                Button(
                    onClick = onResetClick,
                    interactionSource = resetInteractionSource,
                    modifier = Modifier
                        .height(Dimens.buttonHeight)
                        .graphicsLayer(scaleX = resetScale, scaleY = resetScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = Dimens.spacing12),
                    shape = RoundedCornerShape(Dimens.radiusSmall),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(Dimens.iconCompact), tint = colorScheme.primary)
                    Spacer(Modifier.width(Dimens.spacing8))
                    Text("Reset", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(1200)
        onDone()
    }
    val colorScheme = MaterialTheme.colorScheme
    val particles: List<Pair<Float, Float>> = remember {
        List(16) {
            Pair(
                Random.nextFloat(),
                0.3f + Random.nextFloat() * 0.5f
            )
        }
    }
    val alpha = remember { Animatable(0.4f) }
    val offsetY = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.snapTo(0.4f)
        offsetY.snapTo(0f)
        launch { alpha.animateTo(0f, tween(1200)) }
        launch { offsetY.animateTo(0.15f, tween(1200)) }
    }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alpha.value
                    translationY = offsetY.value * 400f
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEachIndexed { i, p ->
                    val x = p.first
                    val sizeFrac = p.second
                    val radius = 8f * sizeFrac
                    val px = x * size.width
                    val py = size.height * 0.15f + (i % 4) * 24f
                    drawCircle(
                        color = colorScheme.primary.copy(alpha = 0.25f),
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(px, py)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.radiusSmall),
        color = colorScheme.surface.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.onPrimary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacing8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(Dimens.iconCompact), tint = colorScheme.onPrimary)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colorScheme.onPrimary
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun LiveCallerCard(
    sheetName: String,
    calledCount: Int,
    lastCalled: Int?,
    lastCalledAtMillis: Long?
) {
    val colorScheme = MaterialTheme.colorScheme
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(lastCalledAtMillis) {
        while (lastCalledAtMillis != null) {
            delay(1000)
            nowMillis = System.currentTimeMillis()
        }
    }
    val lastCalledAgoText = formatLastCalledAgo(lastCalledAtMillis, nowMillis)
    val numberPopScale = remember { Animatable(1f) }
    LaunchedEffect(lastCalled) {
        numberPopScale.snapTo(0.85f)
        numberPopScale.animateTo(1.05f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        numberPopScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    val infinite = rememberInfiniteTransition(label = "livePulse")
    val dotAlpha by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    val numberShowcaseBg = colorScheme.surface.copy(alpha = 0.14f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusLarge))
            .background(
                Brush.linearGradient(
                    listOf(colorScheme.primary, colorScheme.primary.copy(alpha = 0.85f))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-48).dp)
                .size(180.dp)
                .background(colorScheme.onPrimary.copy(alpha = 0.06f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-24).dp, y = 32.dp)
                .size(130.dp)
                .background(colorScheme.onPrimary.copy(alpha = 0.04f), CircleShape)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(Dimens.spacing16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                LivePill(label = "LIVE", dot = true, dotAlpha = dotAlpha)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    CalledBadge(
                        calledCount = calledCount,
                        maxCount = MAX_LIVE_CALLS,
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
            LinearProgressIndicator(
                progress = { calledCount.coerceAtMost(MAX_LIVE_CALLS).toFloat() / MAX_LIVE_CALLS },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = colorScheme.onPrimary.copy(alpha = 0.92f),
                trackColor = colorScheme.onPrimary.copy(alpha = 0.22f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.liveCardNumberMinHeight)
                    .clip(RoundedCornerShape(Dimens.radiusSmall))
                    .background(numberShowcaseBg)
                    .border(1.dp, colorScheme.onPrimary.copy(alpha = 0.20f), RoundedCornerShape(Dimens.radiusSmall))
                    .padding(Dimens.spacing16)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8, Alignment.CenterVertically)
                ) {
                    val letter = bingoLetter(lastCalled)
                    if (letter.isNotEmpty()) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = LiveFonts.DMMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = lastCalled?.toString() ?: "00",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 96.sp
                        ),
                        color = colorScheme.onPrimary,
                        modifier = Modifier.graphicsLayer(
                            scaleX = numberPopScale.value,
                            scaleY = numberPopScale.value
                        )
                    )
                    Text(
                        text = "Last called: $lastCalledAgoText",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.Normal
                        ),
                        color = colorScheme.onPrimary.copy(alpha = 0.68f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun CallANumberCard(
    inputText: String,
    onInputChange: (String) -> Unit,
    canAddNumber: Boolean,
    onEnterClick: () -> Unit,
    onScanClick: () -> Unit,
    onUndoClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var inputFocused by remember { mutableStateOf(false) }
    val scanInteractionSource = remember { MutableInteractionSource() }
    val undoInteractionSource = remember { MutableInteractionSource() }
    val enterInteractionSource = remember { MutableInteractionSource() }
    val scanPressed by scanInteractionSource.collectIsPressedAsState()
    val undoPressed by undoInteractionSource.collectIsPressedAsState()
    val enterPressed by enterInteractionSource.collectIsPressedAsState()
    val scanScale by animateFloatAsState(if (scanPressed) 0.96f else 1f, label = "scanScale")
    val undoScale by animateFloatAsState(if (undoPressed) 0.96f else 1f, label = "undoScale")
    val enterScale by animateFloatAsState(if (enterPressed) 0.98f else 1f, label = "enterScale")
    val inputBorderColor by animateColorAsState(
        if (inputFocused) colorScheme.primary else colorScheme.outlineVariant,
        label = "inputBorder"
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.surface,
        shape = RoundedCornerShape(Dimens.radiusLarge),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
        ) {
            Text(
                text = "CALL A NUMBER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onScanClick,
                    interactionSource = scanInteractionSource,
                    modifier = Modifier
                        .size(Dimens.buttonHeight)
                        .graphicsLayer(scaleX = scanScale, scaleY = scanScale)
                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                        .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(1.5.dp, colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(Dimens.radiusSmall))
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan ticket", tint = colorScheme.primary, modifier = Modifier.size(Dimens.iconDefault))
                }
                Row(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.5.dp, inputBorderColor, RoundedCornerShape(Dimens.radiusSmall))
                        .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 72.dp, max = 88.dp)
                            .heightIn(min = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { v ->
                                val filtered = v.filter { c -> c.isDigit() }.take(2)
                                if (canAddNumber) onInputChange(filtered)
                            },
                            enabled = canAddNumber,
                            modifier = Modifier.fillMaxWidth().onFocusEvent { inputFocused = it.isFocused },
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            maxLines = 1,
                            singleLine = true,
                            cursorBrush = SolidColor(colorScheme.primary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            "1 – 75",
                                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 24.sp,
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            )
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.spacing8))
                    Button(
                        onClick = onEnterClick,
                        enabled = canAddNumber,
                        interactionSource = enterInteractionSource,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .graphicsLayer(scaleX = enterScale, scaleY = enterScale),
                        contentPadding = PaddingValues(horizontal = Dimens.spacing16),
                        shape = RoundedCornerShape(Dimens.radiusSmall),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            contentColor = colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text("ENTER", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(
                    onClick = onUndoClick,
                    interactionSource = undoInteractionSource,
                    modifier = Modifier
                        .size(Dimens.buttonHeight)
                        .graphicsLayer(scaleX = undoScale, scaleY = undoScale)
                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                        .background(colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(1.5.dp, colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(Dimens.radiusSmall))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo last call", tint = colorScheme.primary, modifier = Modifier.size(Dimens.iconDefault))
                }
            }
        }
    }
}

@Composable
private fun ViewToggleChipRow(
    selectedCards: Boolean,
    onCardsClick: () -> Unit,
    onListClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val cardsColor by animateColorAsState(
        targetValue = if (selectedCards) cs.primary else cs.onSurfaceVariant,
        label = "cardsColor"
    )
    val listColor by animateColorAsState(
        targetValue = if (!selectedCards) cs.primary else cs.onSurfaceVariant,
        label = "listColor"
    )
    val indicatorHeight = 2.dp
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.spacing32 + Dimens.spacing4)
    ) {
        val halfWidth = (maxWidth.value / 2).dp
        val indicatorFraction by animateFloatAsState(
            targetValue = if (selectedCards) 0f else 1f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            label = "indicator"
        )
        val indicatorOffset = (indicatorFraction * maxWidth.value / 2).dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { this[SemanticsProperties.Selected] = selectedCards }
                .clickable(onClick = onCardsClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spacing8, horizontal = Dimens.spacing8),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.GridView, contentDescription = "Cards view", modifier = Modifier.size(Dimens.iconCompact), tint = cardsColor)
                Spacer(modifier = Modifier.size(Dimens.spacing8))
                Text("Cards", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selectedCards) FontWeight.Bold else FontWeight.SemiBold), color = cardsColor)
            }
            Spacer(modifier = Modifier.height(indicatorHeight))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { this[SemanticsProperties.Selected] = !selectedCards }
                .clickable(onClick = onListClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.spacing8, horizontal = Dimens.spacing8),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "List view", modifier = Modifier.size(Dimens.iconCompact), tint = listColor)
                Spacer(modifier = Modifier.size(Dimens.spacing8))
                Text("List", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (!selectedCards) FontWeight.Bold else FontWeight.SemiBold), color = listColor)
            }
            Spacer(modifier = Modifier.height(indicatorHeight))
        }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = indicatorOffset)
                .width(halfWidth)
                .height(indicatorHeight)
                .background(cs.primary)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BingoSheetsCarousel(
    modifier: Modifier = Modifier,
    sheets: List<LiveSheetUi>,
    initialSelectedTicketId: String = ""
) {
    val initialIndex = sheets.indexOfFirst { it.ticketId == initialSelectedTicketId }.coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val currentIndex by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
                .minByOrNull { kotlin.math.abs(it.offset) }?.index ?: 0
        }
    }
    Column(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            val horizontalPadding = Dimens.screenHorizontalPadding
            val cardWidth = maxWidth - horizontalPadding * 2
            LazyRow(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(LivePagerPageSpacing)
            ) {
                itemsIndexed(sheets, key = { _, s -> s.ticketId }) { index, sheet ->
                    val isCentered = index == currentIndex
                    Box(
                        modifier = Modifier
                            .width(cardWidth)
                            .wrapContentHeight()
                            .graphicsLayer {
                                alpha = if (isCentered) 1f else 0.92f
                                scaleX = if (isCentered) 1f else 0.98f
                                scaleY = if (isCentered) 1f else 0.98f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SheetCard(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            sheet = sheet
                        )
                    }
                }
            }
        }
        if (sheets.size > 1) {
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(sheets.size) { idx ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (idx == currentIndex) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (idx == currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetCard(modifier: Modifier = Modifier, sheet: LiveSheetUi) {
    val gridCells = when {
        sheet.cells.size == 25 && sheet.cells.any { !it.number.isNullOrBlank() } -> sheet.cells
        sheet.cells.size == 25 -> BingoCellUi.placeholderCells25()
        sheet.cells.isNotEmpty() -> sheet.cells + List(25 - sheet.cells.size) { BingoCellUi(null, false, false, false, false) }
        else -> BingoCellUi.placeholderCells25()
    }
    val markedSet = gridCells.take(25).mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet()
    val winResult = BingoWinChecker.check(markedSet)
    val winningCells = winResult.winningCells
    val markedList = gridCells.take(25).map { it.isMarked }
    val almostBingo = BingoWinChecker.bestAlmostBingo(markedList)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(Dimens.radiusCard))
            .padding(Dimens.spacing16)
    ) {
        if (winResult.isWin) {
            BingoWinBanner(lineCount = winResult.winningLines.size)
            Spacer(modifier = Modifier.height(Dimens.spacing12))
        }
        if (almostBingo != null) {
            AlmostBingoAlertRowV2(
                lineType = almostBingo.lineLabel,
                filled = almostBingo.marked,
                total = almostBingo.total,
                markedCells = markedSet,
                nearCells = emptySet(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimens.spacing12))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sheet.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
            Text(
                text = "Marked: ${sheet.markedCount}/25",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                modifier = Modifier
                    .wrapContentWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        BingoCardGrid(
            cells = gridCells,
            modifier = Modifier.fillMaxWidth(),
            mode = BingoGridMode.PLAY,
            winningCells = winningCells,
            onCellClick = {}
        )
    }
}

@Composable
private fun ListSheetRow(title: String, marked: String, cells: List<BingoCellUi>, onClick: () -> Unit = {}) {
    val miniGrid = if (cells.size == 25) cells.map { it.isMarked } else List(25) { false }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.buttonHeight)
            .semantics(mergeDescendants = true) { contentDescription = "$title, $marked. Double tap to open." }
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.radiusCard))
            .clickable(
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(Dimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16)
    ) {
        MiniBingoGrid(cells = miniGrid)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Text(text = marked, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconDefault),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResumeAutoCallBanner(roomId: String, onResume: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = Dimens.spacing8)
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Resume Auto-Call?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Button(onClick = onResume, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("Resume")
        }
    }
}

@Composable
private fun RoomSettingsBottomSheet(
    roomId: String,
    settings: RoomSettings?,
    isCallLimitReached: Boolean = false,
    onDismiss: () -> Unit,
    onDeleteRoomClick: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Room settings", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Auto-call numbers")
                Switch(
                    checked = settings?.isRunning == true,
                    onCheckedChange = { if (!isCallLimitReached) RoomTimerManager.setAutoCallRunning(roomId, it) },
                    enabled = !isCallLimitReached
                )
            }
            Text("Interval (seconds)", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 8, 10).forEach { sec ->
                    OutlinedButton(onClick = { RoomTimerManager.setInterval(roomId, sec) }) {
                        Text("${sec}s")
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Danger zone", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            OutlinedButton(
                onClick = onDeleteRoomClick,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(Dimens.radiusCard)
            ) {
                Text("Delete Room", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

@Composable
private fun MiniBingoGrid(cells: List<Boolean>) {
    val shape = RoundedCornerShape(2.dp)
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        cells.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { matched ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (matched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LiveCallPanelLightPreview() {
    MamunBingoTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)) {
            LiveCallerCard(
                sheetName = "Today Session",
                calledCount = 7,
                lastCalled = 42,
                lastCalledAtMillis = System.currentTimeMillis() - 5_000
            )
            CallANumberCard(
                inputText = "",
                onInputChange = { },
                canAddNumber = true,
                onEnterClick = { },
                onScanClick = { },
                onUndoClick = { }
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LiveCallPanelDarkPreview() {
    MamunBingoTheme(darkTheme = true) {
        LiveCallerCard(
            sheetName = "Friday Night",
            calledCount = 0,
            lastCalled = null,
            lastCalledAtMillis = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LivePlayScreenPreview() {
    val sheet1 = LiveSheetUi("1", "Sheet 1", System.currentTimeMillis(), BingoCellUi.placeholderCells25(), 0)
    val sheet2 = LiveSheetUi("2", "Sheet 2", System.currentTimeMillis(), BingoCellUi.placeholderCells25().mapIndexed { i, c -> if (i in listOf(0, 6, 12, 18, 24)) c.copy(isMarked = true) else c }, 5)
    val called = listOf(5, 12, 23, 31, 44, 52, 61, 3, 19, 28, 42, 55)
    MamunBingoTheme {
        LivePlayScreen(
            onBack = {},
            sheetName = "Today Session",
            sheets = listOf(sheet1, sheet2),
            initialSelectedTicketId = "1",
            calledNumbers = called,
            lastCalled = 55
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalledHistoryPanelLightPreview() {
    MamunBingoTheme {
        Column(Modifier.padding(16.dp)) {
            CalledHistoryPanel(
                calledNumbers = listOf(4, 8, 12, 18, 26, 31, 42, 55, 64, 73),
                isCallLimitReached = false
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalledHistoryPanelDarkPreview() {
    MamunBingoTheme(darkTheme = true) {
        Column(Modifier.padding(16.dp)) {
            CalledHistoryPanel(
                calledNumbers = listOf(4, 8, 12, 18, 26, 31, 42, 55, 64, 73),
                isCallLimitReached = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoSheetsCarouselLightPreview() {
    val sheets = listOf(
        LiveSheetUi("1", "Manual 33", System.currentTimeMillis(), BingoCellUi.placeholderCells25(), 9),
        LiveSheetUi("2", "Manual 34", System.currentTimeMillis(), BingoCellUi.placeholderCells25(), 0)
    )
    MamunBingoTheme {
        Column(Modifier.padding(16.dp)) {
            BingoSheetsCarousel(
                modifier = Modifier.fillMaxWidth(),
                sheets = sheets,
                initialSelectedTicketId = "1"
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BingoSheetsCarouselDarkPreview() {
    val sheets = listOf(
        LiveSheetUi("1", "Manual 33", System.currentTimeMillis(), BingoCellUi.placeholderCells25(), 9),
        LiveSheetUi("2", "Manual 34", System.currentTimeMillis(), BingoCellUi.placeholderCells25(), 0)
    )
    MamunBingoTheme(darkTheme = true) {
        Column(Modifier.padding(16.dp)) {
            BingoSheetsCarousel(
                modifier = Modifier.fillMaxWidth(),
                sheets = sheets,
                initialSelectedTicketId = "1"
            )
        }
    }
}

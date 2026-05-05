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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.QrCode2
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
import android.app.Activity
import android.view.WindowManager
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import com.example.mamunbingoapp.ui.components.AppInsetDivider
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.mamunbingoapp.domain.model.QrTicketPayload
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import com.example.mamunbingoapp.domain.qr.QrTicketImageGenerator
import com.example.mamunbingoapp.ui.components.qr.TicketQrDialog
import com.example.mamunbingoapp.ui.components.qr.cellsToQrGrid5x5
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Warning
import com.example.mamunbingoapp.theme.WarningBorder
import com.example.mamunbingoapp.theme.WarningContainer
import com.example.mamunbingoapp.theme.WarningText
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
import com.example.mamunbingoapp.ui.components.LivePlayCallKeypad
import com.example.mamunbingoapp.ui.components.BulkSelectionActionBar
import com.example.mamunbingoapp.ui.components.DeleteFromHistoryBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.LeaveRoomBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.MiniBingoGrid
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.SettingsRepository
import com.example.mamunbingoapp.ui.screens.manual.ManualEntryKeypadDockMetrics
import com.example.mamunbingoapp.ui.components.common.bingoLetter
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.RoomSettings
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
    onUndoLastCall: () -> Unit = {}
) {
    var selectedView by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isMyTicketsSheetOpen by rememberSaveable { mutableStateOf(false) }
    var showLeaveRoomDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var showInfoSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteRoomConfirm by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var detailSheet by remember { mutableStateOf<LiveSheetUi?>(null) }
    var listSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedTicketIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showLiveListBulkLeaveConfirm by remember { mutableStateOf(false) }
    var showLiveListBulkDeleteConfirm by remember { mutableStateOf(false) }
    var lastStatus by rememberSaveable { mutableStateOf<RoomStatus?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val roomStatus = effectiveStatus
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = context as? Activity
    val keepScreenOnDuringGame by SettingsRepository.keepScreenOnDuringGameFlow
        .collectAsStateWithLifecycle(initialValue = true)
    DisposableEffect(keepScreenOnDuringGame) {
        if (keepScreenOnDuringGame) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val displaySheets = sheets
    val canAddNumber = effectiveStatus == RoomStatus.RUNNING && !isCallLimitReached
    val liveContentBottomPad = ManualEntryKeypadDockMetrics.estimatedDockHeight + Dimens.spacing8
    val liveListLazyContentBottomPad = Dimens.spacing8

    LaunchedEffect(selectedView) {
        if (!selectedView) {
            listSelectionMode = false
            selectedTicketIds = emptySet()
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
    LeaveRoomBulkConfirmDialog(
        visible = showLiveListBulkLeaveConfirm,
        count = selectedTicketIds.size,
        onDismiss = { showLiveListBulkLeaveConfirm = false },
        onConfirm = {
            RoomRepository.unassignTickets(selectedTicketIds)
            listSelectionMode = false
            selectedTicketIds = emptySet()
            showLiveListBulkLeaveConfirm = false
            scope.launch {
                snackbarHostState.showSnackbar("Removed from room")
            }
        }
    )
    DeleteFromHistoryBulkConfirmDialog(
        visible = showLiveListBulkDeleteConfirm,
        count = selectedTicketIds.size,
        onDismiss = { showLiveListBulkDeleteConfirm = false },
        onConfirm = {
            HistoryRepository.deleteSessions(selectedTicketIds)
            listSelectionMode = false
            selectedTicketIds = emptySet()
            showLiveListBulkDeleteConfirm = false
            scope.launch {
                snackbarHostState.showSnackbar("Deleted from history")
            }
        }
    )
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
                onBack = {
                    if (listSelectionMode) {
                        listSelectionMode = false
                        selectedTicketIds = emptySet()
                    } else {
                        onBack()
                    }
                },
                onAddTicket = { isMyTicketsSheetOpen = true },
                onOpenSettings = { showSettingsSheet = true },
                onOpenInfo = { showInfoSheet = true },
                onResetGame = onResetClick,
                onLeaveRoom = { showLeaveRoomDialog = true },
                showArchivedBadge = effectiveStatus == RoomStatus.FINISHED,
                showSheetViewModeMenu = displaySheets.isNotEmpty(),
                listViewSelected = selectedView,
                onSelectCardsView = { selectedView = false },
                onSelectListView = { selectedView = true },
                showListBulkSelect = selectedView && displaySheets.isNotEmpty(),
                listSelectionMode = listSelectionMode,
                listSelectedCount = selectedTicketIds.size,
                listSelectAllEnabled = selectedTicketIds.size < displaySheets.size,
                onEnterListSelection = {
                    listSelectionMode = true
                    selectedTicketIds = emptySet()
                },
                onListSelectAll = {
                    selectedTicketIds = displaySheets.map { it.ticketId }.toSet()
                },
                onListClearSelection = { selectedTicketIds = emptySet() }
            )
        },
        bottomBar = {
            if (listSelectionMode && selectedView && displaySheets.isNotEmpty()) {
                BulkSelectionActionBar(
                    modifier = Modifier.navigationBarsPadding(),
                    showJoinLive = false,
                    showRemoveFromRoom = true,
                    removeFromRoomEnabled = selectedTicketIds.isNotEmpty(),
                    removeCount = selectedTicketIds.size,
                    deleteEnabled = selectedTicketIds.isNotEmpty(),
                    deleteCount = selectedTicketIds.size,
                    onRemoveFromRoomClick = { showLiveListBulkLeaveConfirm = true },
                    onDeleteFromHistoryClick = { showLiveListBulkDeleteConfirm = true }
                )
            } else if (showBottomBar && !isMyTicketsSheetOpen) {
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
                    haptic = haptic
                )
            }
        }
    ) { innerPadding ->
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
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
            },
            onBulkDeleteTickets = { ids ->
                com.example.mamunbingoapp.data.HistoryRepository.deleteSessions(ids)
            },
            onBulkLeaveTickets = { ids ->
                com.example.mamunbingoapp.data.RoomRepository.unassignTickets(ids)
            },
            onBulkAddToRoom = { ids ->
                ids.forEach { sessionId -> onAddToRoom(sessionId) }
            }
        )
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (displaySheets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.screenHorizontalPadding)
                        .padding(bottom = liveContentBottomPad)
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
                    )
                }
            } else if (selectedView) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = Dimens.screenHorizontalPadding,
                        end = Dimens.screenHorizontalPadding,
                        top = Dimens.spacing8,
                        bottom = liveListLazyContentBottomPad
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
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(1f)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = Dimens.spacing4)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LiveRoomWithHistoryCard(
                                    uiState = LivePlayUiState(
                                        sheetName = sheetName,
                                        calledNumbers = calledNumbers,
                                        lastCalled = lastCalled,
                                        lastCalledAtMillis = lastCalledAtMillis,
                                        effectiveStatus = effectiveStatus
                                    ),
                                    isCallLimitReached = isCallLimitReached
                                )
                                AppInsetDivider(
                                    modifier = Modifier.padding(vertical = Dimens.spacing8),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    thickness = 1.dp,
                                )
                            }
                        }
                    }
                    item(key = "sticky_content_gap") {
                        Spacer(modifier = Modifier.height(Dimens.spacing5))
                    }
                    itemsIndexed(displaySheets, key = { _, sheet -> sheet.ticketId }) { index, sheet ->
                        Box(
                            modifier = Modifier
                                .padding(bottom = Dimens.spacing5)
                                .then(if (index == 0) Modifier else Modifier.padding(top = Dimens.spacing5))
                        ) {
                            ListSheetRow(
                                title = sheet.title,
                                marked = "${sheet.markedCount}/25",
                                serialNumber = sheet.serialNumber,
                                losNumber = sheet.losNumber,
                                scannedDate = formatPlayDate(sheet.playedAtMillis),
                                cells = sheet.cells,
                                onClick = { detailSheet = sheet },
                                selectionMode = listSelectionMode,
                                selected = sheet.ticketId in selectedTicketIds,
                                onSelectionToggle = {
                                    val id = sheet.ticketId
                                    selectedTicketIds =
                                        if (id in selectedTicketIds) selectedTicketIds - id
                                        else selectedTicketIds + id
                                }
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screenHorizontalPadding)
                        .padding(top = Dimens.spacing8)
                ) {
                    AnimatedVisibility(
                        visible = roomSettings?.isRunning == true && !isCallLimitReached,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ResumeAutoCallBanner(roomId = roomId, onResume = { RoomTimerManager.setAutoCallRunning(roomId, true) })
                    }
                    LiveRoomWithHistoryCard(
                        uiState = LivePlayUiState(
                            sheetName = sheetName,
                            calledNumbers = calledNumbers,
                            lastCalled = lastCalled,
                            lastCalledAtMillis = lastCalledAtMillis,
                            effectiveStatus = effectiveStatus
                        ),
                        isCallLimitReached = isCallLimitReached
                    )
                }
                BingoSheetsCarousel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = Dimens.spacing12, bottom = Dimens.spacing8),
                    sheets = displaySheets,
                    initialSelectedTicketId = initialSelectedTicketId,
                    onSheetClick = { detailSheet = it }
                )
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
    detailSheet?.let { sheet ->
        SheetDetailBottomSheet(
            sheet = sheet,
            onDismiss = { detailSheet = null },
            onOpenFullDetail = {
                detailSheet = null
                onOpenSheetDetail(sheet.ticketId)
            }
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
    haptic: HapticFeedback
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
                    if (n == null) {
                        scope.launch { snackbarHostState.showSnackbar("Invalid Bingo number", duration = SnackbarDuration.Short) }
                        releaseGuard()
                        return
                    }
                    if (n !in 1..75) {
                        scope.launch { snackbarHostState.showSnackbar("Enter a number between 1 and 75", duration = SnackbarDuration.Short) }
                        releaseGuard()
                        return
                    }
                    onCallNumber(n) { added ->
                        if (added) {
                            onInputChange("")
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "${bingoLetter(n)}-$n already called",
                                    duration = SnackbarDuration.Short
                                )
                            }
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
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            LivePlayCallKeypad(
                latestCalled = calledNumbers.lastOrNull(),
                draft = inputText,
                onDraftChange = onInputChange,
                canAddNumber = effectiveStatus == RoomStatus.RUNNING && !isCallLimitReached,
                actionInProgress = actionGuard.value,
                onCallClick = {
                    if (!actionGuard.value) {
                        actionGuard.value = true
                        sizeWhenGuardSet.value = calledNumbers.size
                        handleCallClick()
                    }
                },
                onUndoClick = {
                    if (!actionGuard.value) {
                        actionGuard.value = true
                        sizeWhenGuardSet.value = calledNumbers.size
                        onUndoLastCall()
                    }
                }
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.liveCardNumberMinHeight)
                    .clip(RoundedCornerShape(Dimens.radiusSmall))
                    .background(numberShowcaseBg)
                    .border(1.dp, colorScheme.onPrimary.copy(alpha = 0.20f), RoundedCornerShape(Dimens.radiusSmall))
                    .padding(Dimens.spacing16)
            ) {
                val dynamicNumberSize = (this@BoxWithConstraints.maxHeight.value * 0.22f)
                    .coerceIn(48f, 120f)
                    .sp
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
                                fontWeight = FontWeight.Bold,
                                fontSize = dynamicNumberSize * 0.45f
                            ),
                            color = colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = lastCalled?.toString() ?: "00",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = dynamicNumberSize
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
                        val inputFontSize = 28f.coerceIn(22f, 36f).sp
                        BasicTextField(
                            value = inputText,
                            onValueChange = { v ->
                                val filtered = v.filter { c -> c.isDigit() }.take(2)
                                if (canAddNumber) onInputChange(filtered)
                            },
                            enabled = canAddNumber,
                            modifier = Modifier.fillMaxWidth().onFocusEvent { inputFocused = it.isFocused },
                            textStyle = TextStyle(
                                fontSize = inputFontSize,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                lineHeight = inputFontSize,
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
                                                fontSize = inputFontSize,
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = inputFontSize,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BingoSheetsCarousel(
    modifier: Modifier = Modifier,
    sheets: List<LiveSheetUi>,
    initialSelectedTicketId: String = "",
    onSheetClick: (LiveSheetUi) -> Unit = {}
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
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val horizontalPadding = Dimens.screenHorizontalPadding
            val naturalCardWidth = maxWidth - horizontalPadding * 2
            val cardNonGridOverhead = 100.dp
            val maxCardWidthFromHeight = maxHeight - cardNonGridOverhead
            val cardWidth = minOf(naturalCardWidth, maxCardWidthFromHeight).coerceAtLeast(180.dp)
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(horizontal = (maxWidth - cardWidth) / 2),
                horizontalArrangement = Arrangement.spacedBy(LivePagerPageSpacing),
                verticalAlignment = Alignment.CenterVertically
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
                            sheet = sheet,
                            onClick = { onSheetClick(sheet) }
                        )
                    }
                }
            }
        }
        if (sheets.size > 1) {
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Dimens.spacing8),
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
private fun SheetCard(modifier: Modifier = Modifier, sheet: LiveSheetUi, onClick: () -> Unit = {}) {
    val gridCells = when {
        sheet.cells.size == 25 && sheet.cells.any { !it.number.isNullOrBlank() } -> sheet.cells
        sheet.cells.size == 25 -> BingoCellUi.placeholderCells25()
        sheet.cells.isNotEmpty() -> sheet.cells + List(25 - sheet.cells.size) { BingoCellUi(null, false, false, false, false) }
        else -> BingoCellUi.placeholderCells25()
    }
    val markedSet = gridCells.take(25).mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet()
    val winResult = BingoWinChecker.check(markedSet)
    val winningCells = winResult.winningCells
    val isWin = winResult.isWin
    val cardBg = if (isWin) WarningContainer else MaterialTheme.colorScheme.surfaceVariant
    val cardBorder = if (isWin) WarningBorder else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    BoxWithConstraints(
        modifier = modifier
            .clip(cardShape)
            .background(cardBg)
            .border(1.dp, cardBorder, cardShape)
            .clickable(
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
    ) {
        val referenceWidth = 300.dp
        val scale = (maxWidth / referenceWidth).coerceIn(0.65f, 1.15f)
        val outerDensity = LocalDensity.current
        val scaledDensity = remember(outerDensity, scale) {
            Density(density = outerDensity.density * scale, fontScale = outerDensity.fontScale * scale)
        }
        CompositionLocalProvider(LocalDensity provides scaledDensity) {
            Column(modifier = Modifier.padding(Dimens.spacing12)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sheet.title,
                        style = MaterialTheme.typography.titleLarge,
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
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isWin) WarningText else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(
                                if (isWin) Warning.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                BingoCardGrid(
                    cells = gridCells,
                    modifier = Modifier.fillMaxWidth(),
                    mode = BingoGridMode.PLAY,
                    winningCells = winningCells,
                    onCellClick = {}
                )
            }
        }
    }
}

/** Live room sheet list row: header row + full-bleed footer strip (matches History list cards). */
@Composable
private fun ListSheetRow(
    title: String,
    marked: String,
    serialNumber: String?,
    losNumber: String?,
    scannedDate: String,
    cells: List<BingoCellUi>,
    onClick: () -> Unit = {},
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onSelectionToggle: () -> Unit = {}
) {
    val miniGrid = if (cells.size == 25) cells.map { it.isMarked } else List(25) { false }
    val safeSerial = serialNumber?.takeIf { it.isNotBlank() } ?: "--"
    val safeLos = losNumber?.takeIf { it.isNotBlank() } ?: "--"
    val rowShape = RoundedCornerShape(Dimens.radiusSmall)
    val borderWidth = if (selectionMode && selected) 2.dp else Dimens.cardBorderDefault
    val borderColor = if (selectionMode && selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }
    val rowGo = {
        if (selectionMode) onSelectionToggle() else onClick()
    }
    val headerClick = Modifier.clickable(
        indication = rememberRipple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = rowGo
    )
    val footerClick = Modifier.clickable(
        indication = rememberRipple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = rowGo
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = if (selectionMode) {
                    "$title, ${if (selected) "selected" else "not selected"}. Double tap to toggle."
                } else {
                    "$title, Marked $marked. Double tap to open."
                }
            }
            .clip(rowShape)
            .border(width = borderWidth, color = borderColor, shape = rowShape)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onSelectionToggle() },
                    modifier = Modifier.padding(start = Dimens.spacing4),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing5)
                    .then(headerClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16)
            ) {
                MiniBingoGrid(cells = miniGrid)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Scanned: $scannedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = Dimens.outlineDividerAlpha))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing4)
                .then(footerClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListSheetInfoCell(
                label = "SERIAL",
                value = safeSerial,
                modifier = Modifier.weight(1f)
            )
            ListSheetVerticalDivider()
            ListSheetInfoCell(
                label = "LOS",
                value = safeLos,
                modifier = Modifier.weight(1f)
            )
            ListSheetVerticalDivider()
            ListSheetInfoCell(
                label = "MARKED",
                value = marked,
                modifier = Modifier.weight(1f),
                valueColor = MaterialTheme.colorScheme.primary,
                valueFontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ListSheetInfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueFontWeight: FontWeight = FontWeight.Bold
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = valueFontWeight,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ListSheetVerticalDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetDetailBottomSheet(
    sheet: LiveSheetUi,
    onDismiss: () -> Unit,
    onOpenFullDetail: () -> Unit
) {
    val gridCells = when {
        sheet.cells.size == 25 && sheet.cells.any { !it.number.isNullOrBlank() } -> sheet.cells
        sheet.cells.size == 25 -> BingoCellUi.placeholderCells25()
        sheet.cells.isNotEmpty() -> sheet.cells + List(25 - sheet.cells.size) { BingoCellUi(null, false, false, false, false) }
        else -> BingoCellUi.placeholderCells25()
    }
    val markedSet = gridCells.take(25).mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet()
    val winResult = BingoWinChecker.check(markedSet)
    val scannedDate = formatPlayDate(sheet.playedAtMillis)
    val metaSerial = sheet.serialNumber?.takeIf { it.isNotBlank() } ?: "--"
    val metaLos = sheet.losNumber?.takeIf { it.isNotBlank() } ?: "--"
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showTicketQr by remember { mutableStateOf(false) }
    var ticketQrLoading by remember { mutableStateOf(false) }
    var ticketQrBitmap: Bitmap? by remember { mutableStateOf(null) }
    var ticketQrError: String? by remember { mutableStateOf(null) }

    LaunchedEffect(sheetState) {
        runCatching { sheetState.expand() }
    }

    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(bottom = Dimens.spacing16)
        ) {
            // Expanded presentation is state-driven; content remains constraint-driven.
            val sectionSpacing = (maxHeight * 0.012f).coerceIn(4.dp, 8.dp)
            val handleBlockHeight = (maxHeight * 0.01f).coerceIn(4.dp, 8.dp)
            val infoBlockHeightEstimate = (maxHeight * 0.28f).coerceIn(88.dp, 132.dp)
            val actionButtonHeight = Dimens.buttonHeight
            val reservedHeight = handleBlockHeight + infoBlockHeightEstimate + actionButtonHeight + (sectionSpacing * 3)
            val remainingGridHeight = (maxHeight - reservedHeight).coerceAtLeast(150.dp)
            // Grid fit is responsive: use whichever constraint (width or height) is tighter.
            val gridTargetFromWidth = maxWidth * 0.86f
            val gridTargetFromHeight = remainingGridHeight * 0.82f
            val compactGridWidth = minOf(gridTargetFromWidth, gridTargetFromHeight).coerceAtLeast(150.dp)
            val unifiedShape = RoundedCornerShape(Dimens.radiusCard)
            val unifiedBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = Dimens.spacing4),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                )
                Surface(
                    modifier = Modifier
                        .width(compactGridWidth)
                        .align(Alignment.CenterHorizontally),
                    shape = unifiedShape,
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        Dimens.cardBorderDefault,
                        unifiedBorder
                    ),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing8),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sheet.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = Dimens.spacing4),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        showTicketQr = true
                                        ticketQrLoading = true
                                        ticketQrBitmap = null
                                        ticketQrError = null
                                        if (gridCells.isEmpty() || gridCells.size < 25) {
                                            ticketQrError = "This sheet is not ready to share as a QR code yet."
                                            ticketQrLoading = false
                                            return@launch
                                        }
                                        val grid = withContext(Dispatchers.Default) {
                                            cellsToQrGrid5x5(gridCells)
                                        }
                                        val serial = sheet.serialNumber?.trim()?.takeIf { it.isNotBlank() }
                                        val los = sheet.losNumber?.trim()?.takeIf { it.isNotBlank() }
                                        val encoded = runCatching {
                                            QrTicketCodec.encodeDeepLink(
                                                QrTicketPayload(
                                                    grid = grid,
                                                    serial = serial,
                                                    los = los,
                                                )
                                            )
                                        }
                                        if (encoded.isFailure) {
                                            ticketQrBitmap = null
                                            ticketQrError = encoded.exceptionOrNull()?.message
                                                ?: "Could not encode this sheet for QR."
                                            ticketQrLoading = false
                                            return@launch
                                        }
                                        val bmp = withContext(Dispatchers.Default) {
                                            QrTicketImageGenerator.generateBitmap(encoded.getOrThrow())
                                        }
                                        ticketQrLoading = false
                                        bmp.fold(
                                            onSuccess = {
                                                ticketQrError = null
                                                ticketQrBitmap = it
                                            },
                                            onFailure = { e ->
                                                ticketQrBitmap = null
                                                ticketQrError = e.message
                                                    ?: "Could not build the QR image."
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                enabled = !ticketQrLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QrCode2,
                                    contentDescription = "Show ticket QR",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Marked ${sheet.markedCount}/25",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(100.dp)
                                    )
                                    .padding(horizontal = Dimens.spacing8, vertical = 3.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SheetPreviewInfoCell(
                                label = "SERIAL",
                                value = metaSerial,
                                modifier = Modifier.weight(1f)
                            )
                            ListSheetVerticalDivider()
                            SheetPreviewInfoCell(
                                label = "LOS",
                                value = metaLos,
                                modifier = Modifier.weight(1f)
                            )
                            ListSheetVerticalDivider()
                            SheetPreviewInfoCell(
                                label = "SCANNED",
                                value = scannedDate,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.cardBorderDefault)
                                .background(unifiedBorder.copy(alpha = 0.9f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.spacing8, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BingoCardGrid(
                                cells = gridCells,
                                modifier = Modifier.fillMaxWidth(),
                                mode = BingoGridMode.PLAY,
                                winningCells = winResult.winningCells,
                                onCellClick = {}
                            )
                        }
                    }
                }

                Button(
                    onClick = onOpenFullDetail,
                    modifier = Modifier
                        .width(compactGridWidth)
                        .align(Alignment.CenterHorizontally),
                    shape = unifiedShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "View full detail",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
    if (showTicketQr) {
        TicketQrDialog(
            isLoading = ticketQrLoading,
            bitmap = ticketQrBitmap,
            errorMessage = ticketQrError,
            onDismiss = {
                showTicketQr = false
                ticketQrLoading = false
                ticketQrBitmap = null
                ticketQrError = null
            }
        )
    }
}

@Composable
private fun SheetPreviewInfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AppSectionTitle(
            text = label,
            uppercase = false,
            usePrimaryColor = false,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.02f
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SheetMetaField(
    label: String,
    value: String
) {
    Text(
        text = label,
        style = sheetMetaLabelTextStyle(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = value,
        style = sheetMetaValueTextStyle(),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun sheetMetaLabelTextStyle(): TextStyle =
    MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.15f,
        letterSpacing = 0.sp
    )

@Composable
private fun sheetMetaValueTextStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.5f,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
    )

@Composable
private fun RoomSettingsBottomSheet(
    roomId: String,
    settings: RoomSettings?,
    isCallLimitReached: Boolean = false,
    onDismiss: () -> Unit,
    onDeleteRoomClick: () -> Unit = {}
) {
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        windowInsets = WindowInsets(0, 0, 0, 0),
        shape = BottomSheetDefaults.ExpandedShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(Dimens.spacing24),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Room settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            AppInsetDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Auto-call numbers")
                Switch(
                    checked = settings?.isRunning == true,
                    onCheckedChange = { if (!isCallLimitReached) RoomTimerManager.setAutoCallRunning(roomId, it) },
                    enabled = !isCallLimitReached
                )
            }
            AppSectionTitle(text = "Interval (seconds)", uppercase = false, usePrimaryColor = false)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 8, 10).forEach { sec ->
                    OutlinedButton(onClick = { RoomTimerManager.setInterval(roomId, sec) }) {
                        Text("${sec}s")
                    }
                }
            }
            AppInsetDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )
            AppSectionTitle(
                text = "Danger zone",
                uppercase = false,
                usePrimaryColor = false,
                color = MaterialTheme.colorScheme.error,
            )
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

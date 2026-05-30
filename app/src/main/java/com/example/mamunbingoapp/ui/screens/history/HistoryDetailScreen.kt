package com.example.mamunbingoapp.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.mamunbingoapp.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.LiveRoom
import com.example.mamunbingoapp.data.TicketPlayLog
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.ui.components.CalledHistoryPanelContext
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.core.BingoPlayableNumbers
import com.example.mamunbingoapp.ui.components.BingoDetailGridCard
import com.example.mamunbingoapp.ui.components.CompactAlmostBingoRow
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.ui.components.TicketPlayLogSection
import com.example.mamunbingoapp.domain.model.QrTicketPayload
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import com.example.mamunbingoapp.domain.qr.QrTicketImageGenerator
import com.example.mamunbingoapp.ui.components.qr.TicketQrDialog
import com.example.mamunbingoapp.ui.components.qr.cellsToQrGrid5x5
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.model.SheetStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.viewmodel.LiveRoomsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatHistoryDetailDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))

private fun formatHistoryDetailTimestamp(millis: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault()).format(Date(millis))

private val HistoryDetailCardShape = RoundedCornerShape(Dimens.radiusLarge)
private val HistoryDetailCardPadding = Dimens.spacing16
private val HistoryDetailStatCardPadding = Dimens.spacing12
private val HistoryDetailTicketSectionTopPadding = Dimens.spacing10
private val HistoryDetailTicketSectionBottomPadding = Dimens.spacing16
private val HistoryDetailTicketDividerToBingoGap = Dimens.spacing12
private val HistoryDetailTicketGridToDividerGap = Dimens.spacing10
private val HistoryDetailTicketDividerToFooterGap = Dimens.spacing8
private const val HistoryDetailTicketDividerAlpha = 0.22f
private val HistoryDetailBottomContentPadding = Dimens.spacing32
private val HistoryDetailSectionSpacing = Dimens.spacing12
private val HistoryDetailHeroVerticalSpacing = Dimens.spacing8
private val HistoryDetailHeroActionHeight = 28.dp
private val HistoryDetailLatestCallSize = 68.dp
private val HistoryDetailRecentChipSize = 38.dp

private enum class HistoryDetailRoomAction {
    JoinRoom,
    ChangeRoom,
}

private fun resolveHistoryDetailRoomAction(assignedRoomId: String?): HistoryDetailRoomAction =
    if (assignedRoomId.isNullOrBlank()) HistoryDetailRoomAction.JoinRoom
    else HistoryDetailRoomAction.ChangeRoom

@Composable
private fun HistoryDetailSectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(HistoryDetailCardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    AppSectionSurface(
        modifier = modifier.fillMaxWidth(),
        shape = HistoryDetailCardShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
private fun HistoryDetailSectionTitle(
    title: String,
    trailing: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
fun HistoryDetailScreen(
    sessionId: String,
    session: HistorySession?,
    calledNumbers: List<Int> = emptyList(),
    cells: List<BingoCellUi>? = null,
    playLogs: List<TicketPlayLog> = emptyList(),
    testDateMillis: Long? = null,
    testDrawDateLabel: String? = null,
    testDateInfoMessage: String? = null,
    isTestDateLoading: Boolean = false,
    onChangeTestDate: (Long) -> Unit = {},
    onClearTestDate: () -> Unit = {},
    assignedRoomId: String? = null,
    sheetStatus: SheetStatus = SheetStatus.IDLE,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit,
    onOpenTicketDetail: (String) -> Unit = {},
    onTabSelected: (AppTab) -> Unit,
    onAddToLivePlay: (roomId: String) -> Unit = {},
    onOpenRoom: (roomId: String) -> Unit = {},
    showRoomConflictDialog: Boolean = false,
    conflictExistingRoomId: String? = null,
    conflictExistingRoomName: String? = null,
    onDismissConflictDialog: () -> Unit = {},
    onResolveMoveConflict: () -> Unit = {},
    snackbarMessage: Flow<String>? = null,
    actionLoading: Boolean = false,
    onDuplicateSession: (sessionId: String) -> Unit = {},
    onLeaveFromLive: () -> Unit = { onBack() },
    performSoftDelete: suspend () -> Unit = {},
    onRestoreSession: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.collect { snackbarHostState.showSnackbar(it) }
    }
    val scope = rememberCoroutineScope()
    var showLeaveLiveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTestDatePicker by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showRoomPicker by remember { mutableStateOf(false) }
    var qrErrorMessage by remember { mutableStateOf<String?>(null) }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var deletedAwaitingSnackbar by remember { mutableStateOf(false) }
    var cachedSession by remember { mutableStateOf<HistorySession?>(null) }
    var cachedAssignedRoomId by remember { mutableStateOf<String?>(null) }
    var cachedCells by remember { mutableStateOf<List<BingoCellUi>?>(null) }
    var cachedCalledNumbers by remember { mutableStateOf<List<Int>>(emptyList()) }
    var cachedSheetStatus by remember { mutableStateOf(SheetStatus.IDLE) }
    if (session != null && !deletedAwaitingSnackbar) {
        cachedSession = session
        cachedAssignedRoomId = assignedRoomId
        cachedCells = cells
        cachedCalledNumbers = calledNumbers
        cachedSheetStatus = sheetStatus
    }
    val s = if (deletedAwaitingSnackbar) cachedSession else session
    val displayAssignedRoomId = if (deletedAwaitingSnackbar) cachedAssignedRoomId else assignedRoomId
    val displayCells = if (deletedAwaitingSnackbar) cachedCells else cells
    val displayCalledNumbers = if (deletedAwaitingSnackbar) cachedCalledNumbers else calledNumbers
    val markedSetForAlert = remember(displayCells) { displayCells?.take(25)?.mapIndexed { i, c -> i.takeIf { c.isMarked } }?.filterNotNull()?.toSet() ?: emptySet() }
    val winResult = remember(displayCells) {
        displayCells?.take(25)?.let { list ->
            BingoWinChecker.check(list.mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet())
        }
    }
    val almostBingoInfo = remember(displayCells) {
        displayCells?.take(25)?.let { BingoWinChecker.bestAlmostBingo(it.map { it.isMarked }) }
    }
    val playableMarkedProgress = remember(displayCells) {
        displayCells?.let { BingoPlayableNumbers.formatMarkedProgress(BingoPlayableNumbers.countMarkedPlayableCells(it)) }
            ?: BingoPlayableNumbers.formatMarkedProgress(0)
    }
    val displaySheetStatus = if (deletedAwaitingSnackbar) cachedSheetStatus else sheetStatus

    if (s == null && !deletedAwaitingSnackbar) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppHeaderBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter)
            )
            Column(Modifier.fillMaxSize()) {
                AppTopBar(title = stringResource(R.string.history_detail_title), showBack = true, onBackClick = onBack)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = Dimens.spacing5)
                        .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacing16),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    Text(
                        text = stringResource(R.string.common_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.history_detail_ticket_unavailable),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    Text(
                        text = errorMessage?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.history_detail_ticket_removed_default),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }
            }
            }
        }
        return
    }
    val sessionForDisplay = s!!
    val ticketId = sessionForDisplay.sheetsPlayed.firstOrNull()?.ticketId ?: sessionForDisplay.id
    val roomsViewModel: LiveRoomsViewModel = viewModel()
    val liveRooms by roomsViewModel.rooms.collectAsState()
    val roomAction = resolveHistoryDetailRoomAction(displayAssignedRoomId)
    val showLiveBadge = displaySheetStatus == SheetStatus.ACTIVE
    val defaultRoomName = stringResource(R.string.live_room_default_name, 1)
    val qrNoGridMessage = stringResource(R.string.history_detail_qr_no_grid)
    val qrEncodeFailedMessage = stringResource(R.string.history_detail_qr_encode_failed)
    val qrImageFailedMessage = stringResource(R.string.history_detail_qr_image_failed)
    val deletedSnackbarMessage = stringResource(R.string.history_detail_deleted_snackbar)
    val undoActionLabel = stringResource(R.string.common_undo)
    val copiedToClipboardMessage = stringResource(R.string.common_copied_to_clipboard)
    val unnamedSheetLabel = stringResource(R.string.history_unnamed_sheet)

    if (showRoomPicker) {
        HistoryDetailRoomPickerSheet(
            rooms = liveRooms,
            changingRoom = roomAction == HistoryDetailRoomAction.ChangeRoom,
            currentRoomId = displayAssignedRoomId,
            onRoomSelected = { roomId ->
                showRoomPicker = false
                onAddToLivePlay(roomId)
            },
            onDismiss = { showRoomPicker = false },
            onCreateRoom = {
                showRoomPicker = false
                if (liveRooms.isEmpty()) {
                    scope.launch { roomsViewModel.createRoom(defaultRoomName) }
                }
                onTabSelected(AppTab.Jackpot)
            },
        )
    }

    if (showLeaveLiveDialog) {
        AppConfirmDialog(
            visible = true,
            title = stringResource(R.string.history_detail_leave_live_title),
            message = stringResource(R.string.history_detail_leave_live_message),
            confirmText = stringResource(R.string.common_remove),
            cancelText = stringResource(R.string.settings_cancel),
            showCancelButton = true,
            onConfirm = {
                RoomRepository.unassignTicket(ticketId)
                showLeaveLiveDialog = false
                onLeaveFromLive()
            },
            onCancel = { showLeaveLiveDialog = false },
            onDismiss = { showLeaveLiveDialog = false }
        )
    }
    RoomConflictDialog(
        visible = showRoomConflictDialog,
        existingRoomName = conflictExistingRoomName
            ?: stringResource(R.string.history_detail_another_room_fallback),
        hasTargetRoom = showRoomConflictDialog,
        onCancel = onDismissConflictDialog,
        onOpenExistingRoom = {
            onDismissConflictDialog()
            conflictExistingRoomId?.let { onOpenRoom(it) }
        },
        onMoveToTargetRoom = onResolveMoveConflict,
        moveButtonEnabled = !actionLoading
    )
    if (showQrDialog) {
        TicketQrDialog(
            bitmap = qrBitmap,
            errorMessage = qrErrorMessage,
            onDismiss = {
                showQrDialog = false
                qrBitmap = null
                qrErrorMessage = null
            }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.history_detail_delete_title)) },
            text = { Text(stringResource(R.string.history_detail_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            performSoftDelete()
                            showDeleteDialog = false
                            cachedSession = sessionForDisplay
                            deletedAwaitingSnackbar = true
                            val result = snackbarHostState.showSnackbar(
                                message = deletedSnackbarMessage,
                                actionLabel = undoActionLabel,
                                withDismissAction = true,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onRestoreSession()
                            } else {
                                onBack()
                            }
                            deletedAwaitingSnackbar = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }
    if (showTestDatePicker) {
        val initialMillis = testDateMillis ?: sessionForDisplay.effectivePlayedAtMillis()
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis,
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showTestDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onChangeTestDate)
                        showTestDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.history_detail_test_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestDatePicker = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            },
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppHeaderBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter)
            )
            Column(Modifier.fillMaxSize()) {
                val clipboard = LocalClipboardManager.current
                val ticketMeta by TicketRepository.observeTicket(ticketId)
                    .collectAsStateWithLifecycle(initialValue = null)
                AppTopBar(
                    title = stringResource(R.string.history_detail_title),
                    showBack = true,
                    onBackClick = onBack,
                    actions = {
                        HistoryDetailHeaderActions(
                            showLeaveLive = displaySheetStatus == SheetStatus.ACTIVE,
                            onLeaveLiveClick = { showLeaveLiveDialog = true },
                            onShowQrClick = {
                                scope.launch {
                                    val cellsList = displayCells
                                    if (cellsList == null) {
                                        qrBitmap = null
                                        qrErrorMessage = qrNoGridMessage
                                        showQrDialog = true
                                        return@launch
                                    }
                                    val grid = withContext(Dispatchers.Default) {
                                        cellsToQrGrid5x5(cellsList)
                                    }
                                    val encoded = runCatching {
                                        QrTicketCodec.encodeDeepLink(
                                            QrTicketPayload(
                                                grid = grid,
                                                sheetName = sessionForDisplay.effectiveSheetName(),
                                                serial = sessionForDisplay.serialNumber,
                                                los = sessionForDisplay.losNumber,
                                            )
                                        )
                                    }
                                    if (encoded.isFailure) {
                                        qrBitmap = null
                                        qrErrorMessage = qrEncodeFailedMessage
                                        showQrDialog = true
                                        return@launch
                                    }
                                    val bmp = withContext(Dispatchers.Default) {
                                        QrTicketImageGenerator.generateBitmap(encoded.getOrThrow())
                                    }
                                    bmp.fold(
                                        onSuccess = {
                                            qrErrorMessage = null
                                            qrBitmap = it
                                            showQrDialog = true
                                        },
                                        onFailure = { e ->
                                            qrBitmap = null
                                            qrErrorMessage = qrImageFailedMessage
                                            showQrDialog = true
                                        }
                                    )
                                }
                            },
                            onDeleteClick = { showDeleteDialog = true },
                        )
                    }
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = Dimens.screenHorizontalPadding,
                        top = Dimens.spacing8,
                        end = Dimens.screenHorizontalPadding,
                        bottom = HistoryDetailBottomContentPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(HistoryDetailSectionSpacing),
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(HistoryDetailHeroVerticalSpacing),
                        ) {
                            HistoryDetailHeroSection(
                                sheetName = sessionForDisplay.effectiveSheetName().ifEmpty { unnamedSheetLabel },
                                ticketId = ticketId,
                                sheetStatus = displaySheetStatus,
                                roomAction = roomAction,
                                showLiveBadge = showLiveBadge,
                                onRoomActionClick = {
                                    scope.launch {
                                        if (liveRooms.isEmpty()) {
                                            roomsViewModel.createRoom(defaultRoomName)
                                        }
                                        showRoomPicker = true
                                    }
                                },
                                onCopyTicketId = {
                                    clipboard.setText(AnnotatedString(ticketId))
                                    scope.launch {
                                        snackbarHostState.showSnackbar(copiedToClipboardMessage)
                                    }
                                },
                            )
                            HistoryDetailStatsRow(
                                drawDate = formatHistoryDetailDate(
                                    testDateMillis ?: sessionForDisplay.effectivePlayedAtMillis()
                                ),
                                sheetsCount = sessionForDisplay.sheetsCount,
                                calledCount = displayCalledNumbers.size,
                                markedProgress = playableMarkedProgress,
                            )
                            if (displayAssignedRoomId.isNullOrBlank()) {
                                HistoryDetailTestDateDebugSection(
                                    testDateMillis = testDateMillis,
                                    testDrawDateLabel = testDrawDateLabel,
                                    testDateInfoMessage = testDateInfoMessage,
                                    isTestDateLoading = isTestDateLoading,
                                    onChangeTestDateClick = { showTestDatePicker = true },
                                    onClearTestDateClick = onClearTestDate,
                                )
                            }
                        }
                    }
                    item {
                        HistoryDetailNumbersCalledCard(
                            calledNumbers = displayCalledNumbers.takeLast(MAX_LIVE_CALLS),
                            isCallLimitReached = displayCalledNumbers.size >= MAX_LIVE_CALLS,
                        )
                    }
                    if (almostBingoInfo != null && displayCells != null && displayCells.size >= 25) {
                        item {
                            CompactAlmostBingoRow(
                                lineType = almostBingoInfo.lineLabel,
                                filled = almostBingoInfo.marked,
                                total = almostBingoInfo.total,
                                markedCells = markedSetForAlert,
                            )
                        }
                    }
                    if (winResult != null && winResult.isWin && displayCells != null && displayCells.size >= 25) {
                        item {
                            BingoWinBanner(
                                lineCount = winResult.winningLines.size,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    item {
                        HistoryDetailSectionCard(
                            contentPadding = PaddingValues(
                                start = HistoryDetailCardPadding,
                                end = HistoryDetailCardPadding,
                                top = HistoryDetailTicketSectionTopPadding,
                                bottom = HistoryDetailTicketSectionBottomPadding,
                            ),
                        ) {
                            HistoryDetailGridWithMetaStrip(
                                losNumber = sessionForDisplay.losNumber,
                                serialNumber = sessionForDisplay.serialNumber,
                                cells = displayCells,
                                winningCells = if (winResult?.isWin == true) {
                                    winResult.winningCells
                                } else {
                                    emptySet()
                                },
                                createdAtMillis = ticketMeta?.createdAt
                                    ?: sessionForDisplay.effectivePlayedAtMillis(),
                                updatedAtMillis = sessionForDisplay.effectivePlayedAtMillis(),
                            )
                        }
                    }
                    if (playLogs.isNotEmpty()) {
                        item {
                            TicketPlayLogSection(
                                playLogs = playLogs,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                AppBottomBar(selectedTab = AppTab.Jackpot, onTabSelected = onTabSelected)
            }
        }
    }
}

@Composable
private fun HistoryDetailHeroSection(
    sheetName: String,
    ticketId: String,
    sheetStatus: SheetStatus,
    roomAction: HistoryDetailRoomAction,
    showLiveBadge: Boolean,
    onRoomActionClick: () -> Unit,
    onCopyTicketId: () -> Unit,
) {
    val truncatedId = if (ticketId.length > 18) ticketId.take(16) + "…" else ticketId
    val isLiveTicket = sheetStatus == SheetStatus.ACTIVE
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HistoryDetailHeroVerticalSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HistoryDetailHeroVerticalSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sheetName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (!isLiveTicket) {
                when (sheetStatus) {
                    SheetStatus.COMPLETED,
                    SheetStatus.IN_PROGRESS -> HistoryDetailStatusChip(status = sheetStatus)
                    else -> Unit
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HistoryDetailHeroVerticalSpacing),
        ) {
            Text(
                text = truncatedId,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = onCopyTicketId, modifier = Modifier.size(HistoryDetailHeroActionHeight)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.history_detail_copy_ticket_cd),
                    modifier = Modifier.size(Dimens.iconCompact),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            HistoryDetailRoomActionButton(
                action = roomAction,
                onClick = onRoomActionClick,
            )
            if (showLiveBadge) {
                HistoryDetailLiveBadge()
            }
        }
    }
}

@Composable
private fun HistoryDetailRoomActionButton(
    action: HistoryDetailRoomAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (label, icon) = when (action) {
        HistoryDetailRoomAction.JoinRoom ->
            stringResource(R.string.history_detail_join_room) to Icons.Filled.PlayArrow
        HistoryDetailRoomAction.ChangeRoom ->
            stringResource(R.string.history_detail_change_room) to Icons.Filled.SwapHoriz
    }
    val shape = RoundedCornerShape(Dimens.radiusPill)
    Row(
        modifier = modifier
            .height(HistoryDetailHeroActionHeight)
            .clip(shape)
            .background(Primary.copy(alpha = 0.12f))
            .border(BorderStroke(Dimens.cardBorderDefault, Primary.copy(alpha = 0.28f)), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacing10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Primary,
        )
    }
}

@Composable
private fun HistoryDetailLiveBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "historyDetailLiveDot")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "historyDetailLiveDotScale",
    )
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "historyDetailLiveDotAlpha",
    )
    val badgeShape = RoundedCornerShape(Dimens.radiusPill)
    Row(
        modifier = modifier
            .height(HistoryDetailHeroActionHeight)
            .clip(badgeShape)
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = Dimens.spacing10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
    ) {
        Box(
            modifier = Modifier.size(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .scale(dotScale)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
        Text(
            text = stringResource(R.string.common_live_badge),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun HistoryDetailStatusChip(status: SheetStatus) {
    if (status == SheetStatus.IDLE || status == SheetStatus.ACTIVE) return
    val label = when (status) {
        SheetStatus.COMPLETED -> stringResource(R.string.history_detail_status_finished)
        SheetStatus.IN_PROGRESS -> stringResource(R.string.history_detail_status_active)
        else -> return
    }
    val bg = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha)
    val chipShape = RoundedCornerShape(Dimens.radiusPill)
    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(bg)
            .border(BorderStroke(Dimens.cardBorderDefault, borderColor), chipShape)
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing5),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun HistoryDetailTestDateDebugSection(
    testDateMillis: Long?,
    testDrawDateLabel: String?,
    testDateInfoMessage: String?,
    isTestDateLoading: Boolean,
    onChangeTestDateClick: () -> Unit,
    onClearTestDateClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.history_detail_test_date_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
        )
        if (testDateMillis != null) {
            Text(
                text = stringResource(
                    R.string.history_detail_test_date_active,
                    formatHistoryDetailDate(testDateMillis),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            testDrawDateLabel?.takeIf { it.isNotBlank() }?.let { drawDate ->
                Text(
                    text = stringResource(R.string.history_detail_test_date_draw, drawDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (isTestDateLoading) {
            Text(
                text = stringResource(R.string.history_detail_test_date_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
            )
        }
        testDateInfoMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onChangeTestDateClick) {
                Text(stringResource(R.string.history_detail_change_test_date))
            }
            if (testDateMillis != null) {
                TextButton(onClick = onClearTestDateClick) {
                    Text(stringResource(R.string.history_detail_test_date_clear))
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailGridWithMetaStrip(
    losNumber: String?,
    serialNumber: String?,
    cells: List<BingoCellUi>?,
    winningCells: Set<Int>,
    createdAtMillis: Long,
    updatedAtMillis: Long,
) {
    val gap = Dimens.spacing8
    val maxCellSize = 52.dp
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val rawFromWidth = (maxWidth - gap * 4) / 5f
        val compactCellSize = rawFromWidth.coerceIn(22.dp, maxCellSize)
        val bingoGridWidth = gap * 4 + compactCellSize * 5
        Column(
            modifier = Modifier.width(bingoGridWidth),
            horizontalAlignment = Alignment.Start,
        ) {
            HistoryDetailTicketMetaBlock(
                losNumber = losNumber,
                serialNumber = serialNumber,
                gridWidth = bingoGridWidth,
            )
            HistoryDetailTicketSectionDivider(gridWidth = bingoGridWidth)
            Spacer(modifier = Modifier.height(HistoryDetailTicketDividerToBingoGap))
            BingoDetailGridCard(
                cells = cells,
                winningCells = winningCells,
                historyDetailOuterMaxWidth = bingoGridWidth,
                historyDetailOuterMaxHeight = null,
                emphasized = true,
                historyDetailPlainGrid = true,
            )
            Spacer(modifier = Modifier.height(HistoryDetailTicketGridToDividerGap))
            HistoryDetailTicketSectionDivider(gridWidth = bingoGridWidth)
            Spacer(modifier = Modifier.height(HistoryDetailTicketDividerToFooterGap))
            HistoryDetailTicketDatesFooter(
                createdAtMillis = createdAtMillis,
                updatedAtMillis = updatedAtMillis,
                gridWidth = bingoGridWidth,
            )
        }
    }
}

@Composable
private fun HistoryDetailTicketSectionDivider(gridWidth: Dp) {
    HorizontalDivider(
        modifier = Modifier.width(gridWidth),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = HistoryDetailTicketDividerAlpha),
    )
}

@Composable
private fun HistoryDetailTicketDatesFooter(
    createdAtMillis: Long,
    updatedAtMillis: Long,
    gridWidth: Dp,
) {
    val scheme = MaterialTheme.colorScheme
    val labelColor = scheme.onSurfaceVariant.copy(alpha = 0.52f)
    val valueColor = scheme.onSurfaceVariant.copy(alpha = 0.82f)
    Row(
        modifier = Modifier
            .width(gridWidth)
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.Bottom,
    ) {
        HistoryDetailTicketMetaColumn(
            label = stringResource(R.string.history_detail_label_created).uppercase(Locale.getDefault()),
            value = formatHistoryDetailTimestamp(createdAtMillis),
            labelColor = labelColor,
            valueColor = valueColor,
            horizontalAlignment = Alignment.Start,
            labelFontSize = 10.sp,
            valueFontSize = 13.sp,
            valueFontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        HistoryDetailTicketMetaColumn(
            label = stringResource(R.string.history_detail_label_updated).uppercase(Locale.getDefault()),
            value = formatHistoryDetailTimestamp(updatedAtMillis),
            labelColor = labelColor,
            valueColor = valueColor,
            horizontalAlignment = Alignment.End,
            labelFontSize = 10.sp,
            valueFontSize = 13.sp,
            valueFontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HistoryDetailTicketMetaBlock(
    losNumber: String?,
    serialNumber: String?,
    gridWidth: Dp,
) {
    val placeholderDash = stringResource(R.string.common_placeholder_dash)
    val losValue = losNumber?.takeIf { it.isNotBlank() } ?: placeholderDash
    val serialValue = serialNumber?.takeIf { it.isNotBlank() } ?: placeholderDash
    val scheme = MaterialTheme.colorScheme
    val labelColor = scheme.onSurfaceVariant.copy(alpha = 0.62f)
    val valueColor = scheme.onSurfaceVariant.copy(alpha = 0.88f)
    Row(
        modifier = Modifier
            .width(gridWidth)
            .height(IntrinsicSize.Max)
            .padding(vertical = Dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HistoryDetailTicketMetaColumn(
            label = stringResource(R.string.ocr_stat_los_nr),
            value = losValue,
            labelColor = labelColor,
            valueColor = valueColor,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f),
        )
        HistoryDetailTicketMetaColumn(
            label = stringResource(R.string.ocr_stat_serie),
            value = serialValue,
            labelColor = labelColor,
            valueColor = valueColor,
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HistoryDetailTicketMetaColumn(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
    labelFontSize: TextUnit = 11.sp,
    valueFontSize: TextUnit = 19.sp,
    valueFontWeight: FontWeight = FontWeight.Bold,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = labelFontSize,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
            ),
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = valueFontWeight,
                fontSize = valueFontSize,
                lineHeight = (valueFontSize.value + 3).sp,
            ),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HistoryDetailStatsRow(
    drawDate: String,
    sheetsCount: Int,
    calledCount: Int,
    markedProgress: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing10),
        verticalAlignment = Alignment.Top,
    ) {
        HistoryDetailStatCard(
            label = stringResource(R.string.history_detail_stat_draw_date),
            value = drawDate,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        HistoryDetailStatCard(
            label = stringResource(R.string.history_detail_stat_sheets),
            value = sheetsCount.toString(),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        HistoryDetailStatCard(
            label = stringResource(R.string.history_detail_stat_called),
            value = calledCount.toString(),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        HistoryDetailStatCard(
            label = stringResource(R.string.history_detail_stat_marked),
            value = markedProgress,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun HistoryDetailStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    HistoryDetailSectionCard(
        modifier = modifier,
        contentPadding = PaddingValues(HistoryDetailStatCardPadding),
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(Dimens.spacing5))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HistoryDetailNumbersCalledCard(
    calledNumbers: List<Int>,
    isCallLimitReached: Boolean,
) {
    HistoryDetailSectionCard {
        HistoryDetailSectionTitle(
            title = stringResource(R.string.history_detail_numbers_called),
            trailing = stringResource(R.string.history_detail_live_feed),
        )
        Spacer(modifier = Modifier.height(Dimens.spacing8))
        CalledHistoryPanel(
            modifier = Modifier.fillMaxWidth(),
            calledNumbers = calledNumbers,
            isCallLimitReached = isCallLimitReached,
            showLimitMessage = isCallLimitReached,
            showTvBoard = false,
            applyOuterPadding = false,
            premiumLatestRow = false,
            latestCircleSize = HistoryDetailLatestCallSize,
            recentChipSize = HistoryDetailRecentChipSize,
            panelContext = CalledHistoryPanelContext.HistoryDetail,
        )
    }
}

@Composable
private fun HistoryDetailHeaderActions(
    showLeaveLive: Boolean,
    onLeaveLiveClick: () -> Unit,
    onShowQrClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(onClick = onShowQrClick) {
            Icon(
                imageVector = Icons.Filled.QrCode2,
                contentDescription = stringResource(R.string.history_detail_show_qr_cd),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (showLeaveLive) {
            IconButton(onClick = onLeaveLiveClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = stringResource(R.string.history_detail_leave_live_cd),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.history_detail_delete_cd),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDetailRoomPickerSheet(
    rooms: List<LiveRoom>,
    changingRoom: Boolean,
    currentRoomId: String?,
    onRoomSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreateRoom: () -> Unit,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val actionLabel = if (changingRoom) {
        stringResource(R.string.history_detail_move)
    } else {
        stringResource(R.string.common_add)
    }
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing24, vertical = Dimens.spacing8),
        ) {
            Text(
                text = if (changingRoom) {
                    stringResource(R.string.history_detail_change_room_title)
                } else {
                    stringResource(R.string.history_choose_live_room)
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = Dimens.spacing16),
            )
            if (rooms.isEmpty()) {
                Text(
                    text = stringResource(R.string.history_no_active_rooms),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.spacing12),
                )
                TextButton(
                    onClick = {
                        onDismiss()
                        onCreateRoom()
                    },
                ) {
                    Text(stringResource(R.string.history_create_live_room))
                }
            } else {
                rooms.forEachIndexed { index, room ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        )
                    }
                    val isCurrent = !currentRoomId.isNullOrBlank() && room.roomId == currentRoomId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isCurrent) { onRoomSelected(room.roomId) }
                            .padding(vertical = Dimens.spacing12),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (isCurrent) {
                                stringResource(R.string.common_current)
                            } else {
                                actionLabel
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.settings_cancel))
            }
            Spacer(modifier = Modifier.height(Dimens.spacing8))
        }
    }
}

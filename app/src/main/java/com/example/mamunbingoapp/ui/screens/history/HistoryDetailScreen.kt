package com.example.mamunbingoapp.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.AppInsetDivider
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState
import androidx.compose.material3.TextField
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.OnSurface
import com.example.mamunbingoapp.theme.Outline
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.SurfaceContainer
import com.example.mamunbingoapp.theme.WarningBorder
import com.example.mamunbingoapp.theme.WarningContainer
import com.example.mamunbingoapp.theme.WarningIcon
import com.example.mamunbingoapp.theme.WarningSubText
import com.example.mamunbingoapp.theme.WarningText
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.ui.components.CalledHistoryPanelContext
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.LabelValueInfoRow
import com.example.mamunbingoapp.ui.components.LabelValueInfoRowVariant
import com.example.mamunbingoapp.ui.components.BingoDetailGridCard
import com.example.mamunbingoapp.ui.components.CompactAlmostBingoRow
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.ui.components.StatusPill
import com.example.mamunbingoapp.data.LiveRoom
import com.example.mamunbingoapp.viewmodel.LiveRoomsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.mamunbingoapp.domain.model.QrTicketPayload
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import com.example.mamunbingoapp.domain.qr.QrTicketImageGenerator
import com.example.mamunbingoapp.ui.components.qr.TicketQrDialog
import com.example.mamunbingoapp.ui.components.qr.cellsToQrGrid5x5
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.model.SheetStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryDetailScreen(
    sessionId: String,
    session: HistorySession?,
    calledNumbers: List<Int> = emptyList(),
    cells: List<BingoCellUi>? = null,
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
    val roomsViewModel: LiveRoomsViewModel = viewModel()
    val rooms by roomsViewModel.rooms.collectAsState()
    val roomsWithStats by roomsViewModel.roomsWithStats.collectAsState()
    val scope = rememberCoroutineScope()
    var showLeaveLiveDialog by remember { mutableStateOf(false) }
    var showAlreadyInRoomDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showRoomPicker by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
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
                AppTopBar(title = "History Detail", showBack = true, onBackClick = onBack)
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
                        text = "Loading…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                } else {
                    Text(
                        text = "Ticket not available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    Text(
                        text = errorMessage?.takeIf { it.isNotBlank() } ?: "This ticket was removed from live session.",
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

    if (showLeaveLiveDialog) {
        AppConfirmDialog(
            visible = true,
            title = "Remove sheet from Live?",
            message = "This will remove this sheet from the current live session. You can still find it in History.",
            confirmText = "Remove",
            cancelText = "Cancel",
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
        existingRoomName = conflictExistingRoomName ?: "another room",
        hasTargetRoom = showRoomConflictDialog,
        onCancel = onDismissConflictDialog,
        onOpenExistingRoom = {
            onDismissConflictDialog()
            conflictExistingRoomId?.let { onOpenRoom(it) }
        },
        onMoveToTargetRoom = onResolveMoveConflict,
        moveButtonEnabled = !actionLoading
    )
    showAlreadyInRoomDialog?.let { (roomId, roomName) ->
        AppConfirmDialog(
            visible = true,
            title = "Already in room",
            message = "This sheet is already in Room $roomName.",
            confirmText = "Open Room",
            cancelText = "OK",
            showCancelButton = true,
            onConfirm = {
                showAlreadyInRoomDialog = null
                onOpenRoom(roomId)
            },
            onCancel = { showAlreadyInRoomDialog = null },
            onDismiss = { showAlreadyInRoomDialog = null }
        )
    }
    if (showRoomPicker) {
        RoomPickerBottomSheet(
            roomsWithStats = roomsWithStats,
            onRoomSelected = { room ->
                showRoomPicker = false
                onAddToLivePlay(room.roomId)
            },
            onCreateRoom = { showCreateRoomDialog = true },
            onDismiss = { showRoomPicker = false }
        )
    }
    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onConfirm = { name ->
                scope.launch {
                    val roomId = RoomRepository.createRoom(name.ifBlank { "New Room" })
                    showCreateRoomDialog = false
                    showRoomPicker = false
                    onAddToLivePlay(roomId)
                }
            },
            onDismiss = { showCreateRoomDialog = false }
        )
    }
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
            title = { Text("Delete Bingo Sheet?") },
            text = { Text("This will delete this sheet from history. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            performSoftDelete()
                            showDeleteDialog = false
                            cachedSession = sessionForDisplay
                            deletedAwaitingSnackbar = true
                            val result = snackbarHostState.showSnackbar(
                                message = "Bingo sheet deleted",
                                actionLabel = "Undo",
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
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                val configuration = LocalConfiguration.current
                val historyDetailIconOnlyActions = configuration.screenWidthDp < 360
                val historyDetailLiveActionLabel =
                    if (displayAssignedRoomId != null) "Go to Live Room" else "Add to Live Play"
                AppTopBar(
                    title = "History Detail",
                    showBack = true,
                    onBackClick = onBack,
                    actions = {
                        HistoryDetailHeaderActions(
                            iconOnlyActions = historyDetailIconOnlyActions,
                            liveActionLabel = historyDetailLiveActionLabel,
                            onLiveAction = {
                                if (displayAssignedRoomId != null) {
                                    displayAssignedRoomId?.let { onOpenRoom(it) }
                                } else {
                                    scope.launch {
                                        val assigned = RoomRepository.findAssignedRoomId(ticketId)
                                        if (assigned != null) {
                                            val name = RoomRepository.getRoom(assigned)?.name ?: "another room"
                                            showAlreadyInRoomDialog = Pair(assigned, name)
                                        } else {
                                            showRoomPicker = true
                                        }
                                    }
                                }
                            },
                            onShowQrClick = {
                                scope.launch {
                                    val cellsList = displayCells
                                    if (cellsList == null) {
                                        qrBitmap = null
                                        qrErrorMessage = "No saved grid to encode for this sheet."
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
                                        qrErrorMessage = encoded.exceptionOrNull()?.message
                                            ?: "Could not encode ticket for QR"
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
                                            qrErrorMessage = e.message ?: "Could not generate QR image"
                                            showQrDialog = true
                                        }
                                    )
                                }
                            },
                            onDeleteClick = { showDeleteDialog = true },
                        )
                    }
                )
                val historyDetailSectionGap = Dimens.spacing8
                val historyDetailStatusPadV = Dimens.spacing8
                val historyDetailWinSpacer = Dimens.spacing4
                val historyDetailBottomInset = Dimens.spacing8
                val historyDetailCalledPanelPadH = Dimens.spacing8
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.screenHorizontalPadding)
                            .padding(bottom = historyDetailBottomInset),
                        verticalArrangement = Arrangement.spacedBy(historyDetailSectionGap),
                    ) {
                            when (displaySheetStatus) {
                                SheetStatus.COMPLETED -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = Dimens.spacing12,
                                                vertical = historyDetailStatusPadV,
                                            ),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusPill(status = SheetStatus.COMPLETED)
                                    }
                                }
                                SheetStatus.IN_PROGRESS -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = Dimens.spacing12,
                                                vertical = historyDetailStatusPadV,
                                            ),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusPill(status = SheetStatus.IN_PROGRESS)
                                    }
                                }
                                else -> { }
                            }
                            val clipboard = LocalClipboardManager.current
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (displaySheetStatus == SheetStatus.ACTIVE) {
                                    AppSectionSurface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shadowElevation = 0.dp,
                                    ) {
                                        HistoryDetailCompactActiveStatusRow(
                                            onLeaveClick = { showLeaveLiveDialog = true },
                                        )
                                    }
                                }
                                HistoryDetailCompactTicketSection(
                                    session = sessionForDisplay,
                                    ticketId = ticketId,
                                    displaySheetStatus = displaySheetStatus,
                                    displayAssignedRoomId = displayAssignedRoomId,
                                    displayCalledNumbers = displayCalledNumbers,
                                    onCopyTicketId = {
                                        clipboard.setText(AnnotatedString(ticketId))
                                        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                                    },
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (almostBingoInfo != null && displayCells != null && displayCells.size >= 25) {
                                    CompactAlmostBingoRow(
                                        lineType = almostBingoInfo.lineLabel,
                                        filled = almostBingoInfo.marked,
                                        total = almostBingoInfo.total,
                                        markedCells = markedSetForAlert,
                                    )
                                }
                                CalledHistoryPanel(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = historyDetailCalledPanelPadH),
                                    calledNumbers = displayCalledNumbers.takeLast(MAX_LIVE_CALLS),
                                    isCallLimitReached = displayCalledNumbers.size >= MAX_LIVE_CALLS,
                                    showLimitMessage = displayCalledNumbers.size >= MAX_LIVE_CALLS,
                                    panelContext = CalledHistoryPanelContext.HistoryDetail,
                                )
                            }
                            if (winResult != null && winResult.isWin && displayCells != null && displayCells.size >= 25) {
                                BingoWinBanner(
                                    lineCount = winResult.winningLines.size,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (winResult != null && winResult.isWin && displayCells != null && displayCells.size >= 25) {
                                Spacer(modifier = Modifier.height(historyDetailWinSpacer))
                            }
                            AppSectionSurface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                shadowElevation = 0.dp,
                            ) {
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    BingoDetailGridCard(
                                        cells = displayCells,
                                        winningCells = if (winResult?.isWin == true) winResult.winningCells else emptySet(),
                                        historyDetailOuterMaxWidth = maxWidth,
                                        historyDetailOuterMaxHeight = maxHeight,
                                    )
                                }
                            }
                    }
                }
                AppBottomBar(selectedTab = AppTab.Jackpot, onTabSelected = onTabSelected)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomPickerBottomSheet(
    roomsWithStats: List<com.example.mamunbingoapp.viewmodel.RoomWithStats>,
    onRoomSelected: (LiveRoom) -> Unit,
    onCreateRoom: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacing16)
        ) {
            Text(
                text = "Add to Live Play",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            if (roomsWithStats.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(roomsWithStats, key = { it.room.roomId }) { rws ->
                        val room = rws.room
                        val ticketCount = rws.ticketCount
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = Dimens.buttonHeight)
                                .clickable { onRoomSelected(rws.room) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconDefault),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$ticketCount ticket(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spacing24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No rooms yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Create a room to add this ticket to live play.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                    AppPrimaryButton(
                        text = "Create Room",
                        onClick = onCreateRoom,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateRoomDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.radiusCard))
                .background(MaterialTheme.colorScheme.surface)
                .padding(Dimens.spacing24)
        ) {
            Text(
                text = "Create Room",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Room name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Dimens.spacing24))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(onClick = { onConfirm(name) }) { Text("Create") }
            }
        }
    }
}

@Composable
private fun HistoryDetailCompactActiveStatusRow(
    onLeaveClick: () -> Unit,
    compactVertical: Boolean = false,
) {
    val padV = if (compactVertical) Dimens.spacing4 else Dimens.spacing5
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacing12, vertical = padV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.radiusPill))
                .background(IconContainerBg)
                .padding(horizontal = Dimens.spacing12, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Active",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.radiusPill))
                    .background(SurfaceContainer)
                    .border(BorderStroke(1.dp, Outline), RoundedCornerShape(Dimens.radiusPill))
                    .clickable(onClick = onLeaveClick)
                    .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing5),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Leave",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface,
                )
            }
        }
    }
}

@Composable
private fun HistoryDetailCompactTicketSection(
    session: HistorySession,
    ticketId: String,
    displaySheetStatus: SheetStatus,
    displayAssignedRoomId: String?,
    displayCalledNumbers: List<Int>,
    onCopyTicketId: () -> Unit,
    compactVertical: Boolean = false,
) {
    val dateStr = remember(session.id, session.playedAtMillis) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(session.effectivePlayedAtMillis()))
    }
    val sheetLabel = session.effectiveSheetName().ifEmpty { "Unnamed sheet" }
    val truncatedId = if (ticketId.length > 16) ticketId.take(14) + "…" else ticketId
    val n = session.sheetsCount
    val sheetWord = if (n == 1) "Sheet" else "Sheets"
    val summaryMiddle = "Numbers called"
    val summaryTail = when {
        displayAssignedRoomId != null && displayCalledNumbers.isNotEmpty() ->
            "${displayCalledNumbers.size} called"
        displayAssignedRoomId != null ->
            "No calls yet"
        else ->
            "${displayCalledNumbers.size} called"
    }
    val statsAnnotated = buildAnnotatedString {
        val baseStyle = SpanStyle(
            color = OnSurface.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
        )
        val boldStyle = SpanStyle(
            color = OnSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
        withStyle(boldStyle) {
            append("$n $sheetWord")
        }
        withStyle(baseStyle) {
            append(" · $summaryMiddle · ")
        }
        withStyle(boldStyle) {
            append(summaryTail)
        }
    }
    val statusText = historyDetailStatusLabel(displaySheetStatus)
    val ticketLabelTop = if (compactVertical) 2.dp else 4.dp
    val ticketLabelBottom = if (compactVertical) 1.dp else 2.dp
    AppSectionSurface(modifier = Modifier.fillMaxWidth(), shadowElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.spacing4)
        ) {
            AppSectionTitle(
                text = "TICKET INFORMATION",
                uppercase = false,
                usePrimaryColor = false,
                color = Outline.copy(alpha = 0.9f),
                modifier = Modifier.padding(
                    top = ticketLabelTop,
                    start = Dimens.spacing12,
                    end = Dimens.spacing12,
                    bottom = ticketLabelBottom,
                ),
            )
            LabelValueInfoRow(
                label = "Sheet name",
                variant = if (compactVertical) LabelValueInfoRowVariant.Compact else LabelValueInfoRowVariant.Default,
                showDivider = false,
            ) {
                Text(
                    text = sheetLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AppInsetDivider(
                startInset = Dimens.spacing12,
                endInset = Dimens.spacing12,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
            )
            LabelValueInfoRow(
                label = "Draw date",
                variant = if (compactVertical) LabelValueInfoRowVariant.Compact else LabelValueInfoRowVariant.Default,
                showDivider = false,
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                )
            }
            AppInsetDivider(
                startInset = Dimens.spacing12,
                endInset = Dimens.spacing12,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
            )
            LabelValueInfoRow(
                label = "Ticket ID",
                variant = if (compactVertical) LabelValueInfoRowVariant.Compact else LabelValueInfoRowVariant.Default,
                showDivider = false,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = truncatedId,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    IconButton(onClick = onCopyTicketId, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy ticket id",
                            modifier = Modifier.size(Dimens.iconCompact),
                            tint = Primary,
                        )
                    }
                }
            }
            AppInsetDivider(
                startInset = Dimens.spacing12,
                endInset = Dimens.spacing12,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
            )
            LabelValueInfoRow(
                label = "Status",
                variant = if (compactVertical) LabelValueInfoRowVariant.Compact else LabelValueInfoRowVariant.Default,
                showDivider = false,
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (displaySheetStatus == SheetStatus.ACTIVE) {
                        Primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.End,
                )
            }
            AppInsetDivider(
                startInset = Dimens.spacing12,
                endInset = Dimens.spacing12,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
            )
            val statsRowPadV = if (compactVertical) 2.dp else 4.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = statsRowPadV, horizontal = Dimens.spacing12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = statsAnnotated, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            session.ocrSource?.takeIf { it in listOf("GEMINI", "ML_KIT") }?.let { src ->
                val via = if (src == "GEMINI") "Gemini OCR" else "ML Kit OCR"
                val conf = session.ocrConfidence?.let { " · ${(it * 100).toInt()}%" } ?: ""
                Text(
                    text = "$via$conf",
                    modifier = Modifier.padding(
                        start = Dimens.spacing12,
                        end = Dimens.spacing12,
                        top = Dimens.spacing4,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun historyDetailStatusLabel(status: SheetStatus): String = when (status) {
    SheetStatus.ACTIVE -> "Live"
    SheetStatus.COMPLETED -> "Completed"
    SheetStatus.IN_PROGRESS -> "In progress"
    SheetStatus.IDLE -> "—"
}

@Composable
private fun HistoryDetailHeaderActions(
    iconOnlyActions: Boolean,
    liveActionLabel: String,
    onLiveAction: () -> Unit,
    onShowQrClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        if (iconOnlyActions) {
            IconButton(onClick = onLiveAction) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = liveActionLabel,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            TextButton(
                onClick = onLiveAction,
                contentPadding = PaddingValues(
                    horizontal = Dimens.spacing8,
                    vertical = Dimens.spacing8,
                ),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconCompact),
                )
                Spacer(modifier = Modifier.width(Dimens.spacing4))
                Text(
                    text = liveActionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onShowQrClick) {
            Icon(
                imageVector = Icons.Filled.QrCode2,
                contentDescription = "Show QR",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}


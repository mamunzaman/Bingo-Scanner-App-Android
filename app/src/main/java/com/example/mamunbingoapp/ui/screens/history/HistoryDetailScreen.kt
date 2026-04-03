package com.example.mamunbingoapp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Slate200
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.AlmostBingoAlertRowV2
import com.example.mamunbingoapp.ui.components.BingoCardGrid
import com.example.mamunbingoapp.ui.components.BingoGridMode
import com.example.mamunbingoapp.ui.components.BingoSheetSection
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.ui.components.SectionHeader
import com.example.mamunbingoapp.ui.components.TicketInfoCard
import com.example.mamunbingoapp.ui.components.TicketInfoItem
import com.example.mamunbingoapp.ui.components.StatusPill
import com.example.mamunbingoapp.ui.components.TicketInfoStatusChip
import com.example.mamunbingoapp.data.LiveRoom
import com.example.mamunbingoapp.viewmodel.LiveRoomsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.model.SheetStatus
import androidx.compose.foundation.clickable
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
        containerColor = Color.Transparent,
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
                AppTopBar(
                    title = "History Detail",
                    showBack = true,
                    onBackClick = onBack,
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = Dimens.screenHorizontalPadding,
                top = Dimens.spacing5,
                end = Dimens.screenHorizontalPadding,
                bottom = Dimens.spacing16
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing24)
        ) {
            when (displaySheetStatus) {
                SheetStatus.ACTIVE -> item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(status = SheetStatus.ACTIVE)
                        Row(
                            modifier = Modifier
                                .heightIn(min = Dimens.buttonHeight)
                                .clip(RoundedCornerShape(Dimens.radiusSmall))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(Dimens.spacing4),
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.radiusSmall))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { }
                                    .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Live",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.radiusSmall))
                                    .clickable { showLeaveLiveDialog = true }
                                    .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Leave",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                SheetStatus.COMPLETED -> item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(status = SheetStatus.COMPLETED)
                    }
                }
                SheetStatus.IN_PROGRESS -> item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(status = SheetStatus.IN_PROGRESS)
                    }
                }
                SheetStatus.IDLE -> { }
            }
            item {
                val clipboard = LocalClipboardManager.current
                val infoItems = buildList {
                    add(TicketInfoItem("Sheet Name", sessionForDisplay.effectiveSheetName().ifEmpty { "Unnamed sheet" }))
                    add(
                        TicketInfoItem(
                            "Draw Date",
                            SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(sessionForDisplay.effectivePlayedAtMillis()))
                        )
                    )
                    sessionForDisplay.ocrSource?.takeIf { it in listOf("GEMINI", "ML_KIT") }?.let { src ->
                        add(TicketInfoItem("Imported via", if (src == "GEMINI") "Gemini OCR" else "ML Kit OCR"))
                    }
                    if (sessionForDisplay.ocrSource == "GEMINI" && sessionForDisplay.ocrConfidence != null) {
                        add(TicketInfoItem("OCR confidence", "${(sessionForDisplay.ocrConfidence!! * 100).toInt()}%"))
                    }
                    sessionForDisplay.originalOcrNumbers?.takeIf { it.isNotBlank() }?.takeIf {
                        sessionForDisplay.ocrSource in listOf("GEMINI", "ML_KIT")
                    }?.let { nums ->
                        add(TicketInfoItem("Original OCR numbers", nums))
                    }
                    if (sessionForDisplay.ocrSource in listOf("GEMINI", "ML_KIT") && sessionForDisplay.originalOcrNumbers?.isNotBlank() == true && displayCells != null) {
                        val originalList = sessionForDisplay.originalOcrNumbers!!.split(",").mapNotNull { it.trim().toIntOrNull() }.take(25)
                        val savedRowMajor = displayCells!!.take(25).map { it.number?.toIntOrNull() ?: 0 }
                        val savedForCompare = if (sessionForDisplay.ocrSource == "ML_KIT") rowMajorToColumnMajor(savedRowMajor) else savedRowMajor
                        val n = minOf(originalList.size, savedForCompare.size)
                        val edited = n > 0 && (0 until n).any { originalList[it] != savedForCompare[it] }
                        val correctionCount = (0 until n).count { originalList[it] != savedForCompare[it] }
                        add(TicketInfoItem("OCR review", if (edited) "Edited after import" else "Saved without changes"))
                        add(TicketInfoItem("OCR corrections", when (correctionCount) { 0 -> "0 changes"; 1 -> "1 change"; else -> "$correctionCount changes" }))
                    }
                    add(
                        TicketInfoItem(
                            "Ticket ID",
                            ticketId,
                            trailing = {
                                IconButton(onClick = {
                                    clipboard.setText(AnnotatedString(ticketId))
                                    scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy ticket id")
                                }
                            }
                        )
                    )
                    if (displaySheetStatus == SheetStatus.ACTIVE) {
                        add(TicketInfoItem("Status", "", trailing = { TicketInfoStatusChip("Live") }))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                    TicketInfoCard(title = "TICKET INFORMATION", items = infoItems)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconCompact),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${sessionForDisplay.sheetsCount} Sheets",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                            Icon(
                                imageVector = Icons.Default.Numbers,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconCompact),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            if (displayAssignedRoomId != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Numbers Called (Live)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = if (displayCalledNumbers.isNotEmpty()) "${displayCalledNumbers.size} Called" else "Live (loading…)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            } else {
                                Text(
                                    text = "${displayCalledNumbers.size} Numbers Called",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
            item {
                CalledHistoryPanel(
                    modifier = Modifier.fillMaxWidth(),
                    calledNumbers = displayCalledNumbers,
                    isCallLimitReached = true
                )
            }
            if (winResult != null && winResult.isWin && displayCells != null && displayCells.size >= 25) {
                item {
                    BingoWinBanner(lineCount = winResult.winningLines.size, modifier = Modifier.fillMaxWidth())
                }
                item { Spacer(modifier = Modifier.height(Dimens.spacing12)) }
            }
            if (almostBingoInfo != null && displayCells != null && displayCells.size >= 25) {
                item {
                    AlmostBingoAlertRowV2(
                        lineType = almostBingoInfo.lineLabel,
                        filled = almostBingoInfo.marked,
                        total = almostBingoInfo.total,
                        markedCells = markedSetForAlert,
                        nearCells = emptySet(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Spacer(modifier = Modifier.height(Dimens.spacing12)) }
            }
            item {
                SectionHeader(title = "Bingo Sheet")
            }
            item {
                BingoGridCard(cells = displayCells, winningCells = if (winResult?.isWin == true) winResult.winningCells else emptySet())
            }
            if (displayAssignedRoomId != null && displayCalledNumbers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.radiusCard))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(Dimens.radiusCard))
                            .padding(Dimens.spacing16)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)) {
                            Text(
                                text = "Waiting for live data…",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Open Live Room to see real-time called numbers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
                ) {
                    if (displayAssignedRoomId != null) {
                        AppPrimaryButton(
                            text = "Go to Live Room",
                            onClick = { displayAssignedRoomId?.let { onOpenRoom(it) } },
                            modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.buttonHeight),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimens.iconCompact),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        )
                    } else {
                        AppPrimaryButton(
                            text = "Add to Live Play",
                            onClick = {
                                scope.launch {
                                    val assigned = RoomRepository.findAssignedRoomId(ticketId)
                                    if (assigned != null) {
                                        val name = RoomRepository.getRoom(assigned)?.name ?: "another room"
                                        showAlreadyInRoomDialog = Pair(assigned, name)
                                    } else {
                                        showRoomPicker = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.buttonHeight),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimens.iconCompact),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = { onDuplicateSession(sessionForDisplay.id) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.buttonHeight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(Dimens.radiusCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconCompact),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Duplicate Session",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = Dimens.spacing8)
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

@Composable
private fun RoomPickerBottomSheet(
    roomsWithStats: List<com.example.mamunbingoapp.viewmodel.RoomWithStats>,
    onRoomSelected: (LiveRoom) -> Unit,
    onCreateRoom: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
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
private fun BingoGridCard(cells: List<BingoCellUi>?, winningCells: Set<Int> = emptySet()) {
    BingoSheetSection {
        if (cells == null || cells.size != 25) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No grid saved for this session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }
        } else {
            BingoCardGrid(
                cells = cells,
                modifier = Modifier.fillMaxWidth(),
                mode = BingoGridMode.PREVIEW,
                winningCells = winningCells,
                onCellClick = {}
            )
        }
    }
}

private fun rowMajorToColumnMajor(rowMajor: List<Int>): List<Int> =
    if (rowMajor.size < 25) rowMajor else (0..24).map { i -> rowMajor[(i % 5) * 5 + i / 5] }

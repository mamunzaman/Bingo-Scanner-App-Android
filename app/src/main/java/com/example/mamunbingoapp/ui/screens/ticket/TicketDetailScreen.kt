package com.example.mamunbingoapp.ui.screens.ticket

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.ui.components.TicketPlayLogSection
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.AlmostBingoAlertRowV2
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.ui.components.BingoCardGrid
import com.example.mamunbingoapp.ui.components.BingoGridMode
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.LiveRoom
import com.example.mamunbingoapp.viewmodel.LiveRoomsViewModel
import com.example.mamunbingoapp.viewmodel.TicketDetailViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Error
import com.example.mamunbingoapp.domain.model.QrTicketPayload
import com.example.mamunbingoapp.domain.qr.QrTicketCodec
import com.example.mamunbingoapp.domain.qr.QrTicketImageGenerator
import com.example.mamunbingoapp.ui.components.qr.TicketQrDialog
import com.example.mamunbingoapp.ui.components.qr.cellsToQrGrid5x5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TicketDetailScreen(
    ticketId: String = "",
    onBack: () -> Unit,
    onAddToLive: (ticketId: String, roomId: String) -> Unit = { _, _ -> },
    onOpenRoom: (roomId: String) -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    sheetName: String? = null,
    playedAtMillis: Long = System.currentTimeMillis(),
    cells: List<BingoCellUi> = emptyList(),
    calledNumbers: List<Int> = emptyList(),
    playLogs: List<com.example.mamunbingoapp.data.TicketPlayLog> = emptyList(),
    serialNumber: String? = null,
    losNumber: String? = null,
    viewModel: TicketDetailViewModel? = null
) {
    val resolvedSheetName = sheetName ?: stringResource(R.string.history_unnamed_sheet)
    val anotherRoomFallback = stringResource(R.string.history_detail_another_room_fallback)
    val defaultRoomName = stringResource(R.string.live_room_default_name, 1)
    val qrEncodeFailedMessage = stringResource(R.string.history_detail_qr_encode_failed)
    val qrImageFailedMessage = stringResource(R.string.history_detail_qr_image_failed)
    val roomsViewModel: LiveRoomsViewModel = viewModel()
    val rooms by roomsViewModel.rooms.collectAsState()
    val roomTicketCounts by com.example.mamunbingoapp.data.RoomRepository.roomTicketCountsFlow()
        .collectAsState(initial = emptyMap())
    val pickerRooms = remember(rooms, roomTicketCounts) {
        com.example.mamunbingoapp.data.RoomRepository.roomsVisibleInRoomPicker(rooms, roomTicketCounts)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoadingFlow = viewModel?.isLoading ?: remember { MutableStateFlow(false).asStateFlow() }
    val isLoading by isLoadingFlow.collectAsState(initial = false)

    LaunchedEffect(viewModel) {
        viewModel?.snackbarMessage?.collect { snackbarHostState.showSnackbar(it) }
    }
    val conflictFlow = viewModel?.roomConflict ?: remember { MutableStateFlow(com.example.mamunbingoapp.ui.components.RoomConflictUi()).asStateFlow() }
    val roomConflict by conflictFlow.collectAsState(initial = com.example.mamunbingoapp.ui.components.RoomConflictUi())
    var sessionIdResolved by remember(ticketId) { mutableStateOf<String?>(null) }
    LaunchedEffect(ticketId) {
        sessionIdResolved = HistoryRepository.getSessionIdForTicket(ticketId) ?: ticketId
    }
    val sessionId = sessionIdResolved ?: ticketId
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAlreadyInRoomDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showRoomPicker by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var qrErrorMessage by remember { mutableStateOf<String?>(null) }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    AppConfirmDialog(
        visible = showDeleteDialog,
        title = stringResource(R.string.ticket_detail_delete_title),
        message = stringResource(R.string.ticket_detail_delete_message),
        confirmText = stringResource(R.string.common_delete),
        cancelText = stringResource(R.string.settings_cancel),
        showCancelButton = true,
        onConfirm = {
            showDeleteDialog = false
            onDelete()
        },
        onCancel = { showDeleteDialog = false },
        onDismiss = { showDeleteDialog = false }
    )
    showAlreadyInRoomDialog?.let { (roomId, roomName) ->
        AppConfirmDialog(
            visible = true,
            title = stringResource(R.string.ticket_detail_already_in_room_title),
            message = stringResource(R.string.ticket_detail_already_in_room_message, roomName),
            confirmText = stringResource(R.string.ticket_detail_open_room),
            cancelText = stringResource(R.string.import_ticket_scan_tips_dialog_ok),
            showCancelButton = true,
            onConfirm = {
                showAlreadyInRoomDialog = null
                onOpenRoom(roomId)
            },
            onCancel = { showAlreadyInRoomDialog = null },
            onDismiss = { showAlreadyInRoomDialog = null }
        )
    }
    if (viewModel != null) {
        RoomConflictDialog(
            visible = roomConflict.visible,
            existingRoomName = roomConflict.existingRoomName ?: anotherRoomFallback,
            hasTargetRoom = roomConflict.targetRoomId != null,
            onCancel = { viewModel.dismissConflict() },
            onOpenExistingRoom = { viewModel.openExistingRoom() },
            onMoveToTargetRoom = { viewModel.moveToTargetRoom() },
            moveButtonEnabled = !isLoading
        )
    }
    if (showRoomPicker) {
        TicketRoomPickerDialog(
            rooms = pickerRooms,
            onRoomSelected = { room ->
                showRoomPicker = false
                if (viewModel != null) viewModel.addToRoom(room.roomId)
                else onAddToLive(ticketId, room.roomId)
            },
            onDismiss = { showRoomPicker = false }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppHeaderBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter)
            )
            Column(Modifier.fillMaxSize()) {
                AppTopBar(
                    title = stringResource(R.string.ticket_detail_title),
                    showBack = true,
                    onBackClick = onBack,
                    actions = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val grid = withContext(Dispatchers.Default) {
                                        cellsToQrGrid5x5(cells)
                                    }
                                    val encoded = runCatching {
                                        QrTicketCodec.encodeDeepLink(
                                            QrTicketPayload(
                                                grid = grid,
                                                sheetName = resolvedSheetName,
                                                serial = serialNumber,
                                                los = losNumber,
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
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QrCode2,
                                contentDescription = stringResource(R.string.history_detail_show_qr_cd),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.pageContentTopPadding, bottom = Dimens.spacing24)
                ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.radiusSmall),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacing16)
                ) {
                    Text(
                        text = resolvedSheetName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(playedAtMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.ticket_detail_preview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Dimens.spacing8)
            )
            val list25 = if (cells.size >= 25) cells.take(25) else cells + List(25 - cells.size) { BingoCellUi(null, false, false, false, false) }
            val markedSet = list25.mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet()
            val winResult = BingoWinChecker.check(markedSet)
            val markedList = list25.map { it.isMarked }
            val almostBingo = BingoWinChecker.bestAlmostBingo(markedList)
            if (winResult.isWin) {
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                BingoWinBanner(lineCount = winResult.winningLines.size, modifier = Modifier.fillMaxWidth())
            }
            if (almostBingo != null) {
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                AlmostBingoAlertRowV2(
                    lineType = almostBingo.lineLabel,
                    filled = almostBingo.marked,
                    total = almostBingo.total,
                    markedCells = markedSet,
                    nearCells = emptySet(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.radiusCard),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(Dimens.spacing16)) {
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    BingoCardGrid(
                        cells = cells,
                        modifier = Modifier.fillMaxWidth(),
                        mode = BingoGridMode.PREVIEW,
                        winningCells = winResult.winningCells,
                        onCellClick = { }
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing24))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing24))
                    CalledHistoryPanel(calledNumbers = calledNumbers)
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                }
            }

            if (playLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.spacing16))
                TicketPlayLogSection(playLogs = playLogs)
            }

            Spacer(modifier = Modifier.height(Dimens.spacing24))
            val assignedRoomId by RoomRepository.assignedRoomIdFlow(sessionId).collectAsState(initial = null)
            var removedFromRoom by remember { mutableStateOf(false) }
            if (assignedRoomId != null && !removedFromRoom) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.radiusCard),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing16),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconDefault),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.ticket_detail_in_live_room),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.ticket_detail_remove_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        RoomRepository.unassignTicket(sessionId)
                        removedFromRoom = true
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.buttonHeight),
                    shape = RoundedCornerShape(Dimens.radiusCard),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.ticket_detail_remove_from_live),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Dimens.spacing8)
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing16))
            }
            AppPrimaryButton(
                text = stringResource(R.string.ticket_detail_play),
                onClick = {
                    scope.launch {
                        val assigned = RoomRepository.findAssignedRoomId(sessionId)
                        if (assigned != null) {
                            val name = RoomRepository.getRoom(assigned)?.name ?: anotherRoomFallback
                            showAlreadyInRoomDialog = Pair(assigned, name)
                        } else {
                            if (rooms.isEmpty()) roomsViewModel.createRoom(defaultRoomName)
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
            Spacer(modifier = Modifier.height(Dimens.spacing24))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).heightIn(min = Dimens.buttonHeight),
                    shape = RoundedCornerShape(Dimens.radiusCard),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.ticket_detail_edit_cd),
                        modifier = Modifier.size(Dimens.iconCompact),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.ticket_detail_edit),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Dimens.spacing8)
                    )
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f).heightIn(min = Dimens.buttonHeight),
                    shape = RoundedCornerShape(Dimens.radiusCard),
                    border = BorderStroke(1.dp, Error),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.ticket_detail_delete_cd),
                        modifier = Modifier.size(Dimens.iconCompact),
                        tint = Error
                    )
                    Text(
                        text = stringResource(R.string.common_delete),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Dimens.spacing8)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacing12))
        }
    }
    }
    }
}

@Composable
private fun TicketRoomPickerDialog(
    rooms: List<LiveRoom>,
    onRoomSelected: (LiveRoom) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing24)
                .clip(RoundedCornerShape(Dimens.radiusCard))
                .background(MaterialTheme.colorScheme.surface)
                .padding(Dimens.spacing16)
        ) {
            Text(
                text = stringResource(R.string.ticket_detail_select_room),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            if (rooms.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
                ) {
                    rooms.forEach { room ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = Dimens.buttonHeight)
                                .clickable { onRoomSelected(room) }
                                .padding(12.dp)
                        ) {
                            Text(room.name, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}


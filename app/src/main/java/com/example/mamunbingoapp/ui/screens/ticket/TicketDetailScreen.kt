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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
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
    sheetName: String = "Unnamed sheet",
    playedAtMillis: Long = System.currentTimeMillis(),
    cells: List<BingoCellUi> = emptyList(),
    calledNumbers: List<Int> = emptyList(),
    viewModel: TicketDetailViewModel? = null
) {
    val roomsViewModel: LiveRoomsViewModel = viewModel()
    val rooms by roomsViewModel.rooms.collectAsState()
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

    AppConfirmDialog(
        visible = showDeleteDialog,
        title = "Delete Ticket",
        message = "Are you sure you want to delete this ticket?",
        confirmText = "Delete",
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
    if (viewModel != null) {
        RoomConflictDialog(
            visible = roomConflict.visible,
            existingRoomName = roomConflict.existingRoomName ?: "another room",
            hasTargetRoom = roomConflict.targetRoomId != null,
            onCancel = { viewModel.dismissConflict() },
            onOpenExistingRoom = { viewModel.openExistingRoom() },
            onMoveToTargetRoom = { viewModel.moveToTargetRoom() },
            moveButtonEnabled = !isLoading
        )
    }
    if (showRoomPicker) {
        TicketRoomPickerDialog(
            rooms = rooms,
            onRoomSelected = { room ->
                showRoomPicker = false
                if (viewModel != null) viewModel.addToRoom(room.roomId)
                else onAddToLive(ticketId, room.roomId)
            },
            onDismiss = { showRoomPicker = false }
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
                    title = "Ticket Detail",
                    showBack = true,
                    onBackClick = onBack
                )
                Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing5, bottom = Dimens.spacing24)
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
                        text = sheetName,
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
                text = "Preview",
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
                                text = "In live room",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Remove below to unassign",
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
                        text = "Remove from Live Room",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Dimens.spacing8)
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing16))
            }
            AppPrimaryButton(
                text = "Play",
                onClick = {
                    scope.launch {
                        val assigned = RoomRepository.findAssignedRoomId(sessionId)
                        if (assigned != null) {
                            val name = RoomRepository.getRoom(assigned)?.name ?: "another room"
                            showAlreadyInRoomDialog = Pair(assigned, name)
                        } else {
                            if (rooms.isEmpty()) roomsViewModel.createRoom("Room 1")
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
                        contentDescription = "Edit",
                        modifier = Modifier.size(Dimens.iconCompact),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Edit",
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
                        contentDescription = "Delete",
                        modifier = Modifier.size(Dimens.iconCompact),
                        tint = Error
                    )
                    Text(
                        text = "Delete",
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
                text = "Select Room",
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


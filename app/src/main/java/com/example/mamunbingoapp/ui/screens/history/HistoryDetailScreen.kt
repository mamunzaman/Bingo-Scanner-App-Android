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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.RoomConflictDialog
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.ui.components.CalledHistoryPanelContext
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.BingoDetailGridCard
import com.example.mamunbingoapp.ui.components.CompactAlmostBingoRow
import com.example.mamunbingoapp.ui.components.BingoWinBanner
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
private val HistoryDetailGridVerticalPadding = Dimens.spacing12
private val HistoryDetailBottomContentPadding = Dimens.spacing32
private val HistoryDetailSectionSpacing = Dimens.spacing12
private val HistoryDetailHeroVerticalSpacing = Dimens.spacing8
private val HistoryDetailHeroActionHeight = 28.dp
private val HistoryDetailLatestCallSize = 68.dp
private val HistoryDetailRecentChipSize = 38.dp

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
                val clipboard = LocalClipboardManager.current
                val ticketMeta by TicketRepository.observeTicket(ticketId)
                    .collectAsStateWithLifecycle(initialValue = null)
                AppTopBar(
                    title = "History Detail",
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
                                sheetName = sessionForDisplay.effectiveSheetName().ifEmpty { "Unnamed sheet" },
                                ticketId = ticketId,
                                sheetStatus = displaySheetStatus,
                                showJoinLive = displaySheetStatus == SheetStatus.ACTIVE &&
                                    !displayAssignedRoomId.isNullOrBlank(),
                                onJoinLive = {
                                    displayAssignedRoomId?.let { onOpenRoom(it) }
                                },
                                onCopyTicketId = {
                                    clipboard.setText(AnnotatedString(ticketId))
                                    scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                                },
                            )
                            HistoryDetailStatsRow(
                                drawDate = formatHistoryDetailDate(sessionForDisplay.effectivePlayedAtMillis()),
                                sheetsCount = sessionForDisplay.sheetsCount,
                                calledCount = displayCalledNumbers.size,
                            )
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
                                horizontal = HistoryDetailCardPadding,
                                vertical = HistoryDetailGridVerticalPadding,
                            ),
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.spacing4),
                                contentAlignment = Alignment.Center,
                            ) {
                                BingoDetailGridCard(
                                    cells = displayCells,
                                    winningCells = if (winResult?.isWin == true) {
                                        winResult.winningCells
                                    } else {
                                        emptySet()
                                    },
                                    historyDetailOuterMaxWidth = maxWidth,
                                    historyDetailOuterMaxHeight = null,
                                    emphasized = true,
                                    historyDetailPlainGrid = true,
                                )
                            }
                        }
                    }
                    item {
                        HistoryDetailMetaFooter(
                            createdAtMillis = ticketMeta?.createdAt
                                ?: sessionForDisplay.effectivePlayedAtMillis(),
                            updatedAtMillis = sessionForDisplay.effectivePlayedAtMillis(),
                        )
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
    showJoinLive: Boolean,
    onJoinLive: () -> Unit,
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
                    contentDescription = "Copy ticket id",
                    modifier = Modifier.size(Dimens.iconCompact),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (isLiveTicket) {
                if (showJoinLive) {
                    HistoryDetailJoinButton(onClick = onJoinLive)
                }
                HistoryDetailLiveBadge()
            }
        }
    }
}

@Composable
private fun HistoryDetailJoinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Join Room",
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
            text = "LIVE",
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
        SheetStatus.COMPLETED -> "Finished"
        SheetStatus.IN_PROGRESS -> "Active"
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
private fun HistoryDetailStatsRow(
    drawDate: String,
    sheetsCount: Int,
    calledCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing10),
        verticalAlignment = Alignment.Top,
    ) {
        HistoryDetailStatCard(
            label = "Draw Date",
            value = drawDate,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        HistoryDetailStatCard(
            label = "Sheets",
            value = sheetsCount.toString(),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        HistoryDetailStatCard(
            label = "Called",
            value = calledCount.toString(),
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
        HistoryDetailSectionTitle(title = "Numbers Called", trailing = "Live Feed")
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
private fun HistoryDetailMetaFooter(
    createdAtMillis: Long,
    updatedAtMillis: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacing4),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Created ${formatHistoryDetailTimestamp(createdAtMillis)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(Dimens.spacing8))
        Text(
            text = "Updated ${formatHistoryDetailTimestamp(updatedAtMillis)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
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
                contentDescription = "Show QR",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (showLeaveLive) {
            IconButton(onClick = onLeaveLiveClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = "Leave live",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
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


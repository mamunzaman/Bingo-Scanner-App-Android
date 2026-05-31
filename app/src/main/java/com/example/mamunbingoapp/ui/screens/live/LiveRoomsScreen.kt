package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.ui.components.TicketGridThumbnailPreview
import kotlinx.coroutines.flow.flowOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ButtonDefaults
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.core.SundayBingoSchedule
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.GreenImpactBg
import com.example.mamunbingoapp.theme.DarkPrimary
import com.example.mamunbingoapp.theme.OnDarkPrimaryContainer
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryBorder
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.theme.WarningIcon
import com.example.mamunbingoapp.viewmodel.RoomWithStats
import java.time.Duration
import java.time.Instant
import com.example.mamunbingoapp.R
import androidx.compose.ui.platform.LocalContext
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.viewmodel.LiveRoomsViewModel
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppIconContainer
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState
import com.example.mamunbingoapp.data.AssignTicketResult
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.ui.screens.scan.ScanTypeSelectionSheet
import androidx.compose.ui.unit.dp

private val berlinZone: ZoneId = SundayBingoSchedule.berlinZone

private fun resolveSundayFeaturedRoom(
    rooms: List<RoomWithStats>,
    sundayTitle: String,
): RoomWithStats? = rooms.find { it.room.name.equals(sundayTitle, ignoreCase = true) }

@Composable
fun LiveRoomsScreen(
    onEnterRoom: (String) -> Unit,
    onCreateRoom: (String) -> Unit,
    onScanSheet: () -> Unit,
    onLaunchCamera: (BingoScanType) -> Unit = { _ -> onScanSheet() },
    onManualEntry: () -> Unit,
    onHistory: () -> Unit,
    onGoLivePlay: () -> Unit,
    onTabSelected: (AppTab) -> Unit = {},
    showBottomBar: Boolean = true,
) {
    val viewModel: LiveRoomsViewModel = viewModel()
    val rooms by viewModel.rooms.collectAsState()
    val roomsWithStats by viewModel.roomsWithStats.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val lastCreatedRoomId by viewModel.lastCreatedRoomId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var showAddedSheetsSheet by remember { mutableStateOf(false) }
    var showScanTypeSheet by remember { mutableStateOf(false) }
    var showExistingTicketsSheet by remember { mutableStateOf(false) }
    var sundayPickerRoomId by remember { mutableStateOf<String?>(null) }
    var newRoomName by remember { mutableStateOf("") }
    var pendingRoomIdToOpen by remember { mutableStateOf<String?>(null) }
    var suppressCreateNavigation by remember { mutableStateOf(false) }
    var pendingSundayRoomAction by remember { mutableStateOf<((String) -> Unit)?>(null) }
    val sundayTitle = stringResource(R.string.live_nav_sunday_featured_title)
    val sundayReservedMessage = stringResource(R.string.live_nav_sunday_room_reserved)
    val sundayFeaturedRoom = remember(roomsWithStats, sundayTitle) {
        resolveSundayFeaturedRoom(roomsWithStats, sundayTitle)
    }

    fun ensureSundayRoom(action: (String) -> Unit) {
        val existingId = sundayFeaturedRoom?.room?.roomId
        if (existingId != null) {
            action(existingId)
        } else {
            pendingSundayRoomAction = action
            suppressCreateNavigation = true
            viewModel.createRoom(sundayTitle)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        RoomRepository.scheduleSundayFeaturedRoomMaintenance()
    }
    LaunchedEffect(lastCreatedRoomId) {
        lastCreatedRoomId?.let { id ->
            showCreateDialog = false
            newRoomName = ""
            if (suppressCreateNavigation) {
                suppressCreateNavigation = false
                pendingSundayRoomAction?.invoke(id)
                pendingSundayRoomAction = null
                viewModel.clearLastCreatedRoomId()
                return@let
            }
            pendingRoomIdToOpen = id
            viewModel.clearLastCreatedRoomId()
        }
    }
    LaunchedEffect(pendingRoomIdToOpen) {
        pendingRoomIdToOpen?.let { rid ->
            pendingRoomIdToOpen = null
            onCreateRoom(rid)
        }
    }

    if (showCreateDialog) {
        val defaultRoomName = stringResource(R.string.live_room_default_name, rooms.size + 1)
        CreateRoomDialog(
            roomName = newRoomName,
            onRoomNameChange = { newRoomName = it },
            onDismiss = { showCreateDialog = false },
            onCreate = {
                val name = newRoomName.trim().ifBlank { defaultRoomName }
                if (name.equals(sundayTitle, ignoreCase = true)) {
                    showCreateDialog = false
                    newRoomName = ""
                    scope.launch { snackbarHostState.showSnackbar(sundayReservedMessage) }
                } else {
                    viewModel.createRoom(name)
                }
            },
            createEnabled = newRoomName.trim().isNotEmpty() && !isCreating,
            isLoading = isCreating
        )
    }

    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
    AppHeaderPageLayout(
        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        topBar = {
        AppTopBar(
            title = stringResource(R.string.live_nav_title),
            actions = {
                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.live_nav_create_room_cd),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        )
        },
        content = {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing24, bottom = Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
        ) {
            val sundayFeaturedRoomId = sundayFeaturedRoom?.room?.roomId
            SundayFeaturedRoomHero(
                sundayTitle = sundayTitle,
                roomWithStats = sundayFeaturedRoom,
                onAddSheet = { showAddOptionsSheet = true },
                onOpenRoom = { ensureSundayRoom(onEnterRoom) },
            )
            QuickActionsSection(
                onScanSheet = onScanSheet,
                onHistory = onHistory,
                onManualEntry = onManualEntry
            )
            RecentRoomsSection(
                roomsWithStats = roomsWithStats,
                excludeRoomId = sundayFeaturedRoomId,
                onJoinRoom = onEnterRoom,
                onCreateRoomClick = { showCreateDialog = true },
            )
        }
        }
    )
    }

    if (showAddOptionsSheet) {
        SundayAddOptionsSheet(
            onDismiss = { showAddOptionsSheet = false },
            onViewAdded = {
                showAddOptionsSheet = false
                ensureSundayRoom {
                    sundayPickerRoomId = it
                    showAddedSheetsSheet = true
                }
            },
            onScanNew = {
                showAddOptionsSheet = false
                ensureSundayRoom { showScanTypeSheet = true }
            },
            onAddExisting = {
                showAddOptionsSheet = false
                ensureSundayRoom { roomId ->
                    sundayPickerRoomId = roomId
                    showExistingTicketsSheet = true
                }
            },
        )
    }

    if (showAddedSheetsSheet) {
        SundayAddedSheetsSheet(
            sundayRoomId = sundayPickerRoomId ?: sundayFeaturedRoom?.room?.roomId,
            onDismiss = { showAddedSheetsSheet = false },
        )
    }

    if (showScanTypeSheet) {
        ScanTypeSelectionSheet(
            onDismiss = { showScanTypeSheet = false },
            onScanTypeSelected = { type ->
                showScanTypeSheet = false
                onLaunchCamera(type)
            },
        )
    }

    MyTicketsBottomSheet(
        visible = showExistingTicketsSheet,
        onDismiss = { showExistingTicketsSheet = false },
        roomId = sundayPickerRoomId.orEmpty(),
        emptyTitleRes = R.string.live_nav_no_saved_sheets,
        emptySubtitleRes = R.string.live_nav_no_saved_sheets_subtitle,
        emptyCtaRes = R.string.live_nav_scan_new_bingo_card,
        onAddToRoom = { ticketId ->
            val roomId = sundayPickerRoomId ?: return@MyTicketsBottomSheet
            scope.launch {
                when (val result = RoomRepository.assignTicketToRoom(roomId, ticketId)) {
                    is AssignTicketResult.Success -> {
                        showExistingTicketsSheet = false
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.live_play_snackbar_added_to_room),
                        )
                    }
                    is AssignTicketResult.AlreadyInRoom -> {
                        val roomName = RoomRepository.getRoom(result.existingRoomId)?.name
                            ?: context.getString(R.string.history_detail_another_room_fallback)
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.ticket_detail_already_in_room_message, roomName),
                        )
                    }
                    is AssignTicketResult.Error -> { }
                }
            }
        },
        onGoLive = { _ ->
            showExistingTicketsSheet = false
            sundayPickerRoomId?.let(onEnterRoom)
        },
        onCreateTicket = {
            showExistingTicketsSheet = false
            onScanSheet()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SundayAddOptionsSheet(
    onDismiss: () -> Unit,
    onViewAdded: () -> Unit,
    onScanNew: () -> Unit,
    onAddExisting: () -> Unit,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing8, bottom = Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Text(
                text = stringResource(R.string.live_nav_add_bingo_card),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier.padding(bottom = Dimens.spacing4),
            )
            SundayAddOptionRow(
                title = stringResource(R.string.live_nav_view_added_bingo_sheets),
                subtitle = stringResource(R.string.live_nav_view_added_bingo_sheets_subtitle),
                icon = Icons.Default.List,
                onClick = onViewAdded,
            )
            SundayAddOptionRow(
                title = stringResource(R.string.live_nav_add_options_scan_new),
                subtitle = stringResource(R.string.live_nav_add_options_scan_new_subtitle),
                icon = Icons.Default.QrCodeScanner,
                onClick = onScanNew,
            )
            SundayAddOptionRow(
                title = stringResource(R.string.live_nav_add_existing_bingo_sheet),
                subtitle = stringResource(R.string.live_nav_add_existing_bingo_sheet_subtitle),
                icon = Icons.Default.ConfirmationNumber,
                onClick = onAddExisting,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SundayAddedSheetsSheet(
    sundayRoomId: String?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val sheetHeight = (config.screenHeightDp * 0.55f).dp
    val addedSheets = rememberSundayAddedSheets(sundayRoomId)
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = sheetHeight)
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing8, bottom = Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Text(
                text = stringResource(R.string.live_nav_added_bingo_sheets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            if (addedSheets.isEmpty()) {
                Text(
                    text = stringResource(R.string.live_nav_no_sheets_added_yet),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Dimens.spacing8),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                ) {
                    items(addedSheets, key = { it.ticketId }) { sheet ->
                        SundayAddedSheetRow(sheet = sheet)
                    }
                }
            }
        }
    }
}

private data class SundayAddedSheetUi(
    val ticketId: String,
    val title: String,
    val serial: String?,
    val los: String?,
    val markedCount: Int,
)

@Composable
private fun rememberSundayAddedSheets(roomId: String?): List<SundayAddedSheetUi> {
    val sessions by HistoryRepository.sessionsFlow.collectAsState()
    val ticketIdsFlow = remember(roomId) {
        if (roomId.isNullOrBlank()) flowOf(emptyList()) else RoomRepository.roomTicketsFlow(roomId)
    }
    val ticketIds by ticketIdsFlow.collectAsState(initial = emptyList())
    return remember(sessions, ticketIds) {
        ticketIds.map { resolveSundayAddedSheet(sessions, it) }
    }
}

private fun resolveSundayAddedSheet(
    sessions: List<HistorySession>,
    ticketId: String,
): SundayAddedSheetUi {
    val session = sessions.find { it.id == ticketId || it.ticketId == ticketId }
        ?: sessions.find { it.sheetsPlayed.any { sheet -> sheet.ticketId == ticketId } }
    return if (session != null) {
        SundayAddedSheetUi(
            ticketId = ticketId,
            title = session.effectiveSheetName().ifBlank { ticketId.takeLast(6).uppercase() },
            serial = session.serialNumber?.takeIf { it.isNotBlank() },
            los = session.losNumber?.takeIf { it.isNotBlank() },
            markedCount = session.sheetsPlayed.firstOrNull()?.markedCount ?: 0,
        )
    } else {
        SundayAddedSheetUi(
            ticketId = ticketId,
            title = ticketId.takeLast(8).uppercase(),
            serial = null,
            los = null,
            markedCount = 0,
        )
    }
}

@Composable
private fun SundayAddedSheetRow(
    sheet: SundayAddedSheetUi,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val metaText = when {
        sheet.serial != null && sheet.los != null -> stringResource(
            R.string.live_nav_sheet_meta_serial_los,
            sheet.serial,
            sheet.los,
        )
        sheet.serial != null -> stringResource(R.string.live_nav_sheet_meta_serial, sheet.serial)
        sheet.los != null -> stringResource(R.string.live_nav_sheet_meta_los, sheet.los)
        else -> null
    }
    val shape = RoundedCornerShape(Dimens.radiusMedium)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.surfaceContainerLow)
            .border(
                Dimens.cardBorderDefault,
                scheme.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha),
                shape,
            )
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        TicketGridThumbnailPreview(
            matchedCells = sheet.markedCount,
            modifier = Modifier.size(34.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = sheet.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (metaText != null) {
                Text(
                    text = metaText,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(Dimens.radiusPill),
            color = Primary.copy(alpha = 0.10f),
        ) {
            Text(
                text = stringResource(R.string.live_nav_sheet_added_badge),
                modifier = Modifier.padding(horizontal = Dimens.spacing8, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
            )
        }
    }
}

@Composable
private fun SundayAddOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusMedium),
        color = scheme.surfaceContainerLow,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            scheme.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha),
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
        ) {
            Surface(
                shape = RoundedCornerShape(Dimens.radiusSmall),
                color = scheme.primary.copy(alpha = 0.12f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier
                        .padding(Dimens.spacing8)
                        .size(Dimens.iconCompact),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CreateRoomDialog(
    roomName: String,
    onRoomNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    createEnabled: Boolean,
    isLoading: Boolean = false
) {
    AppConfirmDialog(
        visible = true,
        title = stringResource(R.string.live_nav_create_room_title),
        confirmText = stringResource(R.string.common_create),
        cancelText = stringResource(R.string.settings_cancel),
        showCancelButton = true,
        confirmEnabled = createEnabled,
        onConfirm = onCreate,
        onCancel = onDismiss,
        onDismiss = onDismiss,
        content = {
            AppTextField(
                value = roomName,
                onValueChange = onRoomNameChange,
                placeholder = stringResource(R.string.live_nav_room_name_placeholder),
                singleLine = true
            )
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            }
        }
    )
}

private data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionsSection(
    onScanSheet: () -> Unit,
    onHistory: () -> Unit,
    onManualEntry: () -> Unit
) {
    val actions = listOf(
        QuickActionItem(
            stringResource(R.string.live_nav_scan_sheet),
            stringResource(R.string.live_nav_scan_sheet_subtitle),
            Icons.Default.QrCodeScanner,
            onScanSheet
        ),
        QuickActionItem(
            stringResource(R.string.live_nav_history),
            stringResource(R.string.live_nav_history_subtitle),
            Icons.Default.History,
            onHistory
        ),
        QuickActionItem(
            stringResource(R.string.live_nav_manual_entry),
            stringResource(R.string.live_nav_manual_entry_subtitle),
            Icons.Default.Edit,
            onManualEntry
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
        Text(
            text = stringResource(R.string.live_nav_quick_actions),
            style = AppTextStyles.sectionTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
            contentPadding = PaddingValues(end = Dimens.spacing8)
        ) {
            items(actions) { item ->
                QuickActionTile(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    modifier = Modifier.width(132.dp),
                    onClick = item.onClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusMedium)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    Column(
        modifier = modifier
            .wrapContentHeight()
            .defaultMinSize(minHeight = 92.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.cardBorderDefault, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(Dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
    ) {
        AppIconContainer(icon = icon, size = 32.dp, iconSize = 18.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private enum class LiveRoomSortOption {
    NEWEST, OLDEST, NAME_AZ, NAME_ZA
}

@Composable
private fun liveRoomSortLabel(option: LiveRoomSortOption): String = when (option) {
    LiveRoomSortOption.NEWEST -> stringResource(R.string.live_nav_sort_newest)
    LiveRoomSortOption.OLDEST -> stringResource(R.string.live_nav_sort_oldest)
    LiveRoomSortOption.NAME_AZ -> stringResource(R.string.live_nav_sort_name_az)
    LiveRoomSortOption.NAME_ZA -> stringResource(R.string.live_nav_sort_name_za)
}

@Composable
private fun RecentRoomsSection(
    roomsWithStats: List<RoomWithStats>,
    excludeRoomId: String?,
    onJoinRoom: (String) -> Unit,
    onCreateRoomClick: () -> Unit = {},
) {
    var sortExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(LiveRoomSortOption.NEWEST) }
    val sortLabel = stringResource(R.string.live_nav_sort_by, liveRoomSortLabel(selectedSort))
    val sortOptions = LiveRoomSortOption.entries
    val sortedRooms = remember(roomsWithStats, selectedSort, excludeRoomId) {
        val filtered = if (excludeRoomId != null) {
            roomsWithStats.filter { it.room.roomId != excludeRoomId }
        } else {
            roomsWithStats
        }
        when (selectedSort) {
            LiveRoomSortOption.OLDEST -> filtered.sortedBy { it.room.createdAt }
            LiveRoomSortOption.NAME_AZ -> filtered.sortedBy { it.room.name.lowercase() }
            LiveRoomSortOption.NAME_ZA -> filtered.sortedByDescending { it.room.name.lowercase() }
            LiveRoomSortOption.NEWEST -> filtered.sortedByDescending { it.room.createdAt }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.live_nav_other_rooms),
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (roomsWithStats.isNotEmpty()) {
                Box(modifier = Modifier.wrapContentWidth()) {
                    Box(
                        modifier = Modifier
                            .height(Dimens.spacing32)
                            .clip(RoundedCornerShape(Dimens.radiusPill))
                            .background(Color.Transparent)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.radiusPill))
                            .clickable { sortExpanded = true }
                            .padding(horizontal = Dimens.spacing12),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                sortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimens.iconDefault)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false },
                        modifier = Modifier
                            .wrapContentWidth(Alignment.End)
                            .widthIn(min = 140.dp)
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(liveRoomSortLabel(option)) },
                                onClick = {
                                    sortExpanded = false
                                    selectedSort = option
                                }
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = sortedRooms.isEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(Dimens.spacing24),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = stringResource(R.string.live_nav_empty_rooms_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.live_nav_empty_rooms_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                AppPrimaryButton(
                    text = stringResource(R.string.live_nav_create_room_button),
                    onClick = onCreateRoomClick,
                )
            }
        }
        AnimatedVisibility(
            visible = sortedRooms.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)) {
                sortedRooms.forEach { rws ->
                    LiveRoomListCard(
                        title = rws.room.name,
                        ticketsInRoom = rws.ticketCount.coerceAtLeast(0),
                        calledCount = rws.calledCount.coerceAtLeast(0),
                        onJoin = { onJoinRoom(rws.room.roomId) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun nextSundayDrawBerlin(from: ZonedDateTime = ZonedDateTime.now(berlinZone)): ZonedDateTime {
    var target = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        .withHour(17)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
    if (!target.isAfter(from)) {
        target = target.plusWeeks(1)
    }
    return target
}

private fun formatSundaySessionCountdown(remaining: Duration): String {
    val totalSeconds = remaining.seconds.coerceAtLeast(0)
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes.coerceAtLeast(1)}m"
    }
}

private sealed interface SundayHeroCountdownState {
    data object LiveNow : SundayHeroCountdownState
    data class StartsIn(val hours: Int, val minutes: Int, val seconds: Int) : SundayHeroCountdownState
}

private fun resolveSundayHeroCountdown(now: ZonedDateTime): SundayHeroCountdownState {
    if (SundayBingoSchedule.activeSessionStart(now) != null) {
        return SundayHeroCountdownState.LiveNow
    }
    val remaining = Duration.between(now, nextSundayDrawBerlin(now)).coerceAtLeast(Duration.ZERO)
    val totalSeconds = remaining.seconds
    val hours = (totalSeconds / 3_600).toInt().coerceIn(0, 99)
    val minutes = ((totalSeconds % 3_600) / 60).toInt().coerceIn(0, 59)
    val seconds = (totalSeconds % 60).toInt().coerceIn(0, 59)
    return SundayHeroCountdownState.StartsIn(hours, minutes, seconds)
}

@Composable
private fun rememberSundayHeroCountdownState(): SundayHeroCountdownState {
    var state by remember {
        mutableStateOf(resolveSundayHeroCountdown(ZonedDateTime.now(berlinZone)))
    }
    LaunchedEffect(Unit) {
        while (true) {
            val now = ZonedDateTime.now(berlinZone)
            state = resolveSundayHeroCountdown(now)
            delay(1_000L)
        }
    }
    return state
}

@Composable
private fun SundayHeroCountdownFocus(
    state: SundayHeroCountdownState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SundayHeroCountdownState.LiveNow -> SundayHeroLiveNowBadge(modifier = modifier)
        is SundayHeroCountdownState.StartsIn -> SundayHeroCountdownBoxes(
            hours = state.hours,
            minutes = state.minutes,
            seconds = state.seconds,
            modifier = modifier,
        )
    }
}

@Composable
private fun SundayHeroCountdownBoxes(
    hours: Int,
    minutes: Int,
    seconds: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.live_nav_countdown_starts_in_label),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.45f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            SundayHeroCountdownSegment(
                value = "%02d".format(hours),
                label = stringResource(R.string.live_nav_countdown_hr),
                modifier = Modifier.weight(1f),
            )
            SundayHeroCountdownSeparator()
            SundayHeroCountdownSegment(
                value = "%02d".format(minutes),
                label = stringResource(R.string.live_nav_countdown_min),
                modifier = Modifier.weight(1f),
            )
            SundayHeroCountdownSeparator()
            SundayHeroCountdownSegment(
                value = "%02d".format(seconds),
                label = stringResource(R.string.live_nav_countdown_sec),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SundayHeroCountdownSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 18.sp,
        ),
        fontWeight = FontWeight.Normal,
        color = Color.White.copy(alpha = 0.28f),
    )
}

@Composable
private fun SundayHeroCountdownSegment(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.radiusSearchField)
    val fillColor = PrimaryDark.copy(alpha = 0.2f)
    val borderColor = PrimaryBorder.copy(alpha = 0.72f)
    val labelColor = OnDarkPrimaryContainer
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(shape)
            .background(fillColor)
            .border(Dimens.cardBorderDefault, borderColor, shape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing10),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 31.sp,
                    lineHeight = 32.sp,
                ),
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 1.4.sp,
                ),
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
            )
        }
    }
}

@Composable
private fun SundayHeroLiveNowBadge(modifier: Modifier = Modifier) {
    val heroOnDark = Color.White
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(Dimens.radiusMedium),
        color = Primary.copy(alpha = 0.22f),
        border = BorderStroke(Dimens.cardBorderDefault, PrimaryBorder.copy(alpha = 0.55f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(DarkPrimary),
                )
                Text(
                    text = stringResource(R.string.live_nav_sunday_live_now).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = heroOnDark,
                    letterSpacing = 1.2.sp,
                )
            }
        }
    }
}

@Composable
private fun formatStartedAgo(createdAtMillis: Long): String {
    val minutes = Duration.between(
        Instant.ofEpochMilli(createdAtMillis),
        Instant.now(),
    ).toMinutes().coerceAtLeast(1)
    val label = when {
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> "${minutes / 60}h"
        else -> "${minutes / 1440}d"
    }
    return stringResource(R.string.live_nav_started_ago, label)
}

@Composable
private fun SundayFeaturedRoomHero(
    sundayTitle: String,
    roomWithStats: RoomWithStats?,
    onAddSheet: () -> Unit,
    onOpenRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ticketCount = roomWithStats?.ticketCount?.coerceAtLeast(0) ?: 0
    val called = roomWithStats?.calledCount?.coerceIn(0, MAX_LIVE_CALLS) ?: 0
    val progress = if (MAX_LIVE_CALLS > 0) called.toFloat() / MAX_LIVE_CALLS else 0f
    val percent = (progress * 100).toInt()
    val sessionInProgress = called > 0
    val countdownState = rememberSundayHeroCountdownState()
    val startedAgoLabel = if (sessionInProgress && roomWithStats != null) {
        formatStartedAgo(roomWithStats.room.createdAt)
    } else {
        null
    }
    val shape = RoundedCornerShape(Dimens.radiusLarge)
    val heroOnDark = Color.White
    val heroMuted = Color.White.copy(alpha = 0.72f)
    val deco = Color.White.copy(alpha = 0.08f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onOpenRoom)
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryDark, GreenImpactBg),
                ),
            )
            .drawBehind {
                drawCircle(deco, radius = 72.dp.toPx(), center = Offset(size.width - 24.dp.toPx(), 36.dp.toPx()))
                drawCircle(deco, radius = 48.dp.toPx(), center = Offset(28.dp.toPx(), size.height - 20.dp.toPx()))
            }
            .padding(Dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = sundayTitle,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                lineHeight = 24.sp,
            ),
            fontWeight = FontWeight.SemiBold,
            color = heroOnDark.copy(alpha = 0.88f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(Dimens.spacing10))
        SundayHeroCountdownFocus(state = countdownState)
        Spacer(modifier = Modifier.height(Dimens.spacing10))
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = heroMuted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.live_nav_hero_tickets, ticketCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = heroMuted,
                )
            }
            if (startedAgoLabel != null) {
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.22f)),
                )
                Text(
                    text = startedAgoLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = heroMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spacing12))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.live_nav_numbers_called_progress, called, MAX_LIVE_CALLS),
                style = MaterialTheme.typography.labelMedium,
                color = heroOnDark,
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = WarningIcon,
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(Dimens.radiusPill)),
            color = WarningIcon,
            trackColor = Color.White.copy(alpha = 0.18f),
        )
        Spacer(modifier = Modifier.height(Dimens.spacing12))
        Button(
            onClick = onOpenRoom,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(Dimens.radiusButtonPill),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.16f),
                contentColor = heroOnDark,
            ),
        ) {
            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Dimens.spacing8))
            Text(
                text = stringResource(R.string.live_nav_open_sunday_room),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(Dimens.spacing8))
        Button(
            onClick = onAddSheet,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight),
            shape = RoundedCornerShape(Dimens.radiusButtonPill),
            colors = ButtonDefaults.buttonColors(
                containerColor = WarningIcon,
                contentColor = Color(0xFF1A1A1A),
            ),
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Dimens.spacing8))
            Text(
                text = stringResource(R.string.live_nav_add_bingo_card),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LiveRoomListCard(
    title: String,
    ticketsInRoom: Int,
    calledCount: Int,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val clampedCalled = calledCount.coerceIn(0, MAX_LIVE_CALLS)
    val progress = if (MAX_LIVE_CALLS > 0) clampedCalled.toFloat() / MAX_LIVE_CALLS else 0f
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onJoin),
        shape = shape,
        color = cs.surface,
        border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Primary),
            )
            AppIconContainer(
                icon = Icons.Default.Groups,
                size = 40.dp,
                iconSize = 22.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(Dimens.radiusPill),
                        color = Primary.copy(alpha = 0.10f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Dimens.spacing8, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                            )
                            Text(
                                text = stringResource(R.string.bingo_session_active),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                            )
                        }
                    }
                    Text(
                        text = if (ticketsInRoom == 1) {
                            stringResource(R.string.bingo_session_tickets_one)
                        } else {
                            stringResource(R.string.bingo_session_tickets_other, ticketsInRoom)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = stringResource(R.string.bingo_session_called_count, clampedCalled, MAX_LIVE_CALLS),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(Dimens.radiusPill)),
                    color = Primary,
                    trackColor = cs.outlineVariant.copy(alpha = 0.35f),
                )
            }
            Button(
                onClick = onJoin,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(Dimens.radiusPill),
                contentPadding = PaddingValues(horizontal = Dimens.spacing14),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = cs.onPrimary,
                ),
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.bingo_session_join),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

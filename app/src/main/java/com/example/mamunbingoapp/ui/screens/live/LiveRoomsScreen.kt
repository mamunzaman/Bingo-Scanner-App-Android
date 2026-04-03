package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.viewmodel.LiveRoomsViewModel
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CreateNewRoomCardUC2
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppIconContainer
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTextField
import com.example.mamunbingoapp.ui.components.BingoSessionCard_V3
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.core.RoomStatusResolver
import com.example.mamunbingoapp.ui.model.RoomStatus

@Composable
fun LiveRoomsScreen(
    onEnterRoom: (String) -> Unit,
    onCreateRoom: (String) -> Unit,
    onScanSheet: () -> Unit,
    onManualEntry: () -> Unit,
    onHistory: () -> Unit,
    onGoLivePlay: () -> Unit,
    onTabSelected: (AppTab) -> Unit,
    showBottomBar: Boolean = true
) {
    val viewModel: LiveRoomsViewModel = viewModel()
    val rooms by viewModel.rooms.collectAsState()
    val roomsWithStats by viewModel.roomsWithStats.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val lastCreatedRoomId by viewModel.lastCreatedRoomId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newRoomName by remember { mutableStateOf("") }
    var pendingRoomIdToOpen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(lastCreatedRoomId) {
        lastCreatedRoomId?.let { id ->
            showCreateDialog = false
            newRoomName = ""
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
        CreateRoomDialog(
            roomName = newRoomName,
            onRoomNameChange = { newRoomName = it },
            onDismiss = { showCreateDialog = false },
            onCreate = {
                val name = newRoomName.trim().ifBlank { "Room ${rooms.size + 1}" }
                viewModel.createRoom(name)
            },
            createEnabled = newRoomName.trim().isNotEmpty() && !isCreating,
            isLoading = isCreating
        )
    }

    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        AppTopBar(
            title = stringResource(R.string.live_nav_title)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing24, bottom = Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CreateNewRoomCardUC2(
                roomsCount = roomsWithStats.size,
                activeCount = roomsWithStats.count { it.calledCount > 0 },
                onClick = { showCreateDialog = true }
            )
            QuickActionsSection(
                onScanSheet = onScanSheet,
                onHistory = onHistory,
                onManualEntry = onManualEntry
            )
            RecentRoomsSection(
                roomsWithStats = roomsWithStats,
                onJoinRoom = onEnterRoom,
                onCreateRoomClick = { showCreateDialog = true }
            )
        }
        if (showBottomBar) {
            AppBottomBar(selectedTab = AppTab.Jackpot, onTabSelected = onTabSelected)
        }
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
        title = "Create Room",
        confirmText = "Create",
        cancelText = "Cancel",
        showCancelButton = true,
        confirmEnabled = createEnabled,
        onConfirm = onCreate,
        onCancel = onDismiss,
        onDismiss = onDismiss,
        content = {
            AppTextField(
                value = roomName,
                onValueChange = onRoomNameChange,
                placeholder = "Room name",
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.live_nav_quick_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16),
            contentPadding = PaddingValues(end = Dimens.spacing16)
        ) {
            items(actions) { item ->
                QuickActionTile(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    modifier = Modifier.width(160.dp),
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
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val borderColor = MaterialTheme.colorScheme.primaryContainer
    Column(
        modifier = modifier
            .wrapContentHeight()
            .defaultMinSize(minHeight = 120.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(Dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppIconContainer(icon = icon, size = 40.dp, iconSize = Dimens.iconDefault)
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val RECENT_ROOMS_SORT_OPTIONS = listOf("Newest", "Oldest", "Name A–Z", "Name Z–A")

@Composable
private fun RecentRoomsSection(
    roomsWithStats: List<com.example.mamunbingoapp.viewmodel.RoomWithStats>,
    onJoinRoom: (String) -> Unit,
    onCreateRoomClick: () -> Unit = {}
) {
    var sortExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Newest") }
    val sortLabel = "Sort by $selectedSort"
    val sortedRooms = remember(roomsWithStats, selectedSort) {
        when (selectedSort) {
            "Oldest" -> roomsWithStats.sortedBy { it.room.createdAt }
            "Name A–Z" -> roomsWithStats.sortedBy { it.room.name.lowercase() }
            "Name Z–A" -> roomsWithStats.sortedByDescending { it.room.name.lowercase() }
            else -> roomsWithStats.sortedByDescending { it.room.createdAt }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.live_nav_recent_rooms),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
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
                        RECENT_ROOMS_SORT_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
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
            visible = roomsWithStats.isEmpty(),
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
                    text = "No rooms yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Create a room to get started.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                AppPrimaryButton(text = "Create room", onClick = onCreateRoomClick)
            }
        }
        AnimatedVisibility(
            visible = roomsWithStats.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)) {
                sortedRooms.forEach { rws ->
                    val tickets = rws.ticketCount.coerceAtLeast(0)
                    BingoSessionCard_V3(
                        title = rws.room.name,
                        ticketsInRoom = tickets,
                        calledCount = rws.calledCount.coerceAtLeast(0),
                        totalCalledCount = MAX_LIVE_CALLS,
                        onJoin = { onJoinRoom(rws.room.roomId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

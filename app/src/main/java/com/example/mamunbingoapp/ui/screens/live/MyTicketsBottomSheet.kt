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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.BulkSelectionActionBar
import com.example.mamunbingoapp.ui.components.DeleteFromHistoryBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.LeaveRoomBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.EmptyStateBlock
import com.example.mamunbingoapp.ui.components.common.SearchFilterSortHeader
import com.example.mamunbingoapp.ui.components.common.SearchHeaderVariant
import com.example.mamunbingoapp.ui.model.TicketUiModel
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.viewmodel.MyTicketsViewModel
import com.example.mamunbingoapp.viewmodel.TicketFilter
import com.example.mamunbingoapp.viewmodel.TicketSort
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatTicketDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy • HH:mm", Locale.getDefault()).format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    roomId: String = "",
    onAddToRoom: (String) -> Unit = {},
    onGoLive: (String) -> Unit,
    onCreateTicket: () -> Unit,
    onBulkDeleteTickets: (Collection<String>) -> Unit = {},
    onBulkLeaveTickets: (Collection<String>) -> Unit = {},
    onBulkAddToRoom: (Collection<String>) -> Unit = {}
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val sheetHeight = (config.screenHeightDp * 0.8f).dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            TicketsBottomSheetContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .padding(horizontal = Dimens.screenHorizontalPadding),
                roomId = roomId,
                onDismiss = onDismiss,
                onAddToRoom = {
                scope.launch { sheetState.hide() }
                onDismiss()
                onAddToRoom(it)
            },
            onGoLive = {
                scope.launch { sheetState.hide() }
                onDismiss()
                onGoLive(it)
            },
            onCreateTicket = {
                scope.launch { sheetState.hide() }
                onDismiss()
                onCreateTicket()
            },
            onBulkDeleteTickets = onBulkDeleteTickets,
            onBulkLeaveTickets = onBulkLeaveTickets,
            onBulkAddToRoom = onBulkAddToRoom
            )
        }
    }
}

@Composable
private fun TicketsBottomSheetContent(
    modifier: Modifier = Modifier,
    roomId: String = "",
    onDismiss: () -> Unit = {},
    onAddToRoom: (String) -> Unit = {},
    onGoLive: (String) -> Unit,
    onCreateTicket: () -> Unit,
    onBulkDeleteTickets: (Collection<String>) -> Unit = {},
    onBulkLeaveTickets: (Collection<String>) -> Unit = {},
    onBulkAddToRoom: (Collection<String>) -> Unit = {}
) {
    var selectionMode by remember { mutableStateOf(false) }
    var selectedTicketIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showBulkLeaveConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val vm: MyTicketsViewModel = viewModel()
    LaunchedEffect(roomId) { vm.setRoomId(roomId) }
    val query by vm.query.collectAsState()
    val selectedFilter by vm.selectedFilter.collectAsState()
    val selectedSort by vm.selectedSort.collectAsState()
    val filtered by vm.filteredTickets.collectAsState()
    val filterCounts by vm.filterCounts.collectAsState()
    val selectedInThisRoom = remember(selectedTicketIds, filtered, roomId) {
        if (roomId.isBlank()) emptySet()
        else selectedTicketIds.filter { tid ->
            filtered.find { it.id == tid }?.assignedRoomId == roomId
        }.toSet()
    }
    val selectedAddableToRoom = remember(selectedTicketIds, filtered, roomId) {
        if (roomId.isBlank()) emptySet()
        else selectedTicketIds.filter { tid ->
            filtered.find { it.id == tid }?.assignedRoomId == null
        }.toSet()
    }

    Column(modifier = modifier.animateContentSize()) {
        TicketsHeader(
            roomId = roomId,
            onDismiss = onDismiss,
            selectionMode = selectionMode,
            selectedCount = selectedTicketIds.size,
            onDoneSelection = {
                selectionMode = false
                selectedTicketIds = emptySet()
            }
        )
        if (!selectionMode && filtered.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing8),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    selectionMode = true
                    selectedTicketIds = emptySet()
                }) {
                    Text("Select")
                }
            }
        }
        LeaveRoomBulkConfirmDialog(
            visible = showBulkLeaveConfirm,
            count = selectedInThisRoom.size,
            onDismiss = { showBulkLeaveConfirm = false },
            onConfirm = {
                onBulkLeaveTickets(selectedInThisRoom)
                selectionMode = false
                selectedTicketIds = emptySet()
                showBulkLeaveConfirm = false
                scope.launch { snackbarHostState.showSnackbar("Removed from room") }
            }
        )
        DeleteFromHistoryBulkConfirmDialog(
            visible = showBulkDeleteConfirm,
            count = selectedTicketIds.size,
            onDismiss = { showBulkDeleteConfirm = false },
            onConfirm = {
                onBulkDeleteTickets(selectedTicketIds)
                selectionMode = false
                selectedTicketIds = emptySet()
                showBulkDeleteConfirm = false
                scope.launch { snackbarHostState.showSnackbar("Deleted from history") }
            }
        )
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        SearchFilterSortHeader(
            variant = SearchHeaderVariant.SearchFilterSort,
            query = query,
            onQueryChange = vm::setQuery,
            placeholder = "Search tickets",
            filterOptions = TicketFilter.entries.map { it.displayName },
            selectedFilter = selectedFilter.displayName,
            onFilterSelect = { label -> vm.setFilter(TicketFilter.entries.first { it.displayName == label }) },
            filterCounts = filterCounts,
            showFilterCounts = true,
            sortOptions = listOf("Newest", "Oldest", "Name (A–Z)", "Date"),
            selectedSort = selectedSort.displayName,
            onSortSelect = { label ->
                vm.setSort(TicketSort.entries.firstOrNull { it.displayName == label } ?: TicketSort.Newest)
            },
            contentPadding = PaddingValues(top = Dimens.spacing12, bottom = Dimens.spacing8),
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.weight(1f)) {
            androidx.compose.animation.AnimatedVisibility(
                visible = filtered.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.fillMaxSize()
            ) {
                TicketsEmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onCreateTicket = onCreateTicket
                )
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = filtered.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = Dimens.spacing12,
                        bottom = Dimens.spacing24
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)
                ) {
                    items(filtered, key = { it.id }) { ticket ->
                        TicketRowCard(
                            ticket = ticket,
                            roomId = roomId,
                            selectionMode = selectionMode,
                            isSelected = ticket.id in selectedTicketIds,
                            onToggleSelect = {
                                val id = ticket.id
                                selectedTicketIds =
                                    if (id in selectedTicketIds) selectedTicketIds - id else selectedTicketIds + id
                            },
                            onAddToRoom = { onAddToRoom(ticket.id) },
                            onGoLive = { onGoLive(ticket.id) }
                        )
                    }
                }
            }
        }
        if (selectionMode && filtered.isNotEmpty()) {
            BulkSelectionActionBar(
                showAddToRoom = roomId.isNotBlank() && filtered.any { it.assignedRoomId == null },
                addToRoomEnabled = selectedAddableToRoom.isNotEmpty(),
                addCount = selectedAddableToRoom.size,
                onAddToRoomClick = {
                    onBulkAddToRoom(selectedAddableToRoom)
                    selectionMode = false
                    selectedTicketIds = emptySet()
                    scope.launch { snackbarHostState.showSnackbar("Added to room") }
                },
                showRemoveFromRoom = roomId.isNotBlank(),
                removeFromRoomEnabled = selectedInThisRoom.isNotEmpty(),
                removeCount = selectedInThisRoom.size,
                deleteEnabled = selectedTicketIds.isNotEmpty(),
                deleteCount = selectedTicketIds.size,
                onRemoveFromRoomClick = { showBulkLeaveConfirm = true },
                onDeleteFromHistoryClick = { showBulkDeleteConfirm = true }
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TicketsHeader(
    roomId: String = "",
    onDismiss: () -> Unit = {},
    selectionMode: Boolean = false,
    selectedCount: Int = 0,
    onDoneSelection: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (selectionMode) {
                    if (selectedCount == 0) "Select items" else "$selectedCount selected"
                } else {
                    "My Tickets"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Text(
                text = when {
                    selectionMode -> "Tap rows to select"
                    roomId.isNotBlank() -> "Add tickets to this room or go live"
                    else -> "Select a ticket to go live"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectionMode) {
                TextButton(onClick = onDoneSelection) {
                    Text("Done")
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TicketRowCard(
    ticket: TicketUiModel,
    roomId: String,
    selectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onAddToRoom: () -> Unit,
    onGoLive: () -> Unit
) {
    val assignedRoomId = ticket.assignedRoomId
    val isInThisRoom = roomId.isNotBlank() && assignedRoomId == roomId
    val isInOtherRoom = assignedRoomId != null && assignedRoomId != roomId
    val actionLabel = when {
        isInThisRoom -> "Go Live"
        isInOtherRoom -> "Already in Room"
        else -> "Add to Room"
    }
    val canAct = !isInOtherRoom
    var otherRoomName by remember(assignedRoomId) { mutableStateOf<String?>(null) }
    LaunchedEffect(assignedRoomId) {
        otherRoomName = if (assignedRoomId != null) RoomRepository.getRoom(assignedRoomId)?.name else null
    }
    val summary = buildString {
        append(ticket.title)
        append(", ")
        append(formatTicketDate(ticket.createdAt))
        if (isInOtherRoom) append(", in ${otherRoomName ?: "another room"}")
        append(". $actionLabel button.")
    }
    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.buttonHeight)
            .semantics(mergeDescendants = true) { contentDescription = summary }
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, cardShape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
                }
            )
            .clickable(
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ) { if (selectionMode) onToggleSelect() }
            .padding(Dimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16)
    ) {
        if (selectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                modifier = Modifier.padding(end = Dimens.spacing4)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconDefault),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)) {
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatTicketDate(ticket.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
            if (isInOtherRoom) {
                Text(
                    text = "Room: ${otherRoomName ?: "another room"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (!selectionMode) {
            Button(
                onClick = { if (canAct) if (isInThisRoom) onGoLive() else onAddToRoom() },
                modifier = Modifier.height(36.dp),
                enabled = canAct,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Dimens.screenHorizontalPadding),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(100.dp)
            ) {
                Icon(
                    imageVector = if (isInThisRoom) Icons.Default.PlayArrow else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconCompact),
                    tint = if (canAct) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Dimens.spacing8))
                Text(actionLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

@Composable
private fun TicketsEmptyState(
    modifier: Modifier = Modifier,
    onCreateTicket: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing16, Alignment.CenterVertically)
    ) {
        EmptyStateBlock(
            icon = Icons.Default.ConfirmationNumber,
            title = "No tickets found",
            subtitle = "Create a ticket to add to this room or go live.",
            modifier = Modifier.fillMaxWidth()
        )
        AppPrimaryButton(
            text = "Create New Ticket",
            onClick = onCreateTicket,
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
    }
}

@Preview(name = "Light")
@Composable
private fun MyTicketsBottomSheetPreviewLight() {
    MamunBingoTheme(darkTheme = false) {
        MyTicketsBottomSheet(
            visible = true,
            onDismiss = {},
            onGoLive = {},
            onCreateTicket = {}
        )
    }
}

@Preview(name = "Dark")
@Composable
private fun MyTicketsBottomSheetPreviewDark() {
    MamunBingoTheme(darkTheme = true) {
        MyTicketsBottomSheet(
            visible = true,
            onDismiss = {},
            onGoLive = {},
            onCreateTicket = {}
        )
    }
}

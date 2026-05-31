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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppFieldLabel
import com.example.mamunbingoapp.ui.components.BulkSelectionActionBar
import com.example.mamunbingoapp.ui.components.DeleteFromHistoryBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.LeaveRoomBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.EmptyStateBlock
import com.example.mamunbingoapp.ui.components.TicketGridThumbnailPreview
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
    onBulkAddToRoom: (Collection<String>) -> Unit = {},
    emptyTitleRes: Int = R.string.live_play_no_tickets_title,
    emptySubtitleRes: Int = R.string.live_play_no_tickets_subtitle,
    emptyCtaRes: Int = R.string.live_play_create_new_ticket,
) {
    if (!visible) return
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val sheetHeight = (config.screenHeightDp * 0.8f).dp

    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0),
        shape = BottomSheetDefaults.ExpandedShape,
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
                    .padding(horizontal = Dimens.spacing24)
                    .padding(top = Dimens.spacing24),
                roomId = roomId,
                emptyTitleRes = emptyTitleRes,
                emptySubtitleRes = emptySubtitleRes,
                emptyCtaRes = emptyCtaRes,
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
    emptyTitleRes: Int = R.string.live_play_no_tickets_title,
    emptySubtitleRes: Int = R.string.live_play_no_tickets_subtitle,
    emptyCtaRes: Int = R.string.live_play_create_new_ticket,
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
    val removedSnackbarMessage = stringResource(R.string.history_snackbar_removed_from_room)
    val deletedSnackbarMessage = stringResource(R.string.history_snackbar_deleted)
    val addedSnackbarMessage = stringResource(R.string.live_play_snackbar_added_to_room)
    val vm: MyTicketsViewModel = viewModel()
    LaunchedEffect(roomId) { vm.setRoomId(roomId) }
    val query by vm.query.collectAsState()
    val selectedFilter by vm.selectedFilter.collectAsState()
    val selectedSort by vm.selectedSort.collectAsState()
    val filtered by vm.filteredTickets.collectAsState()
    val filterCounts by vm.filterCounts.collectAsState()
    val sortNewestLabel = stringResource(R.string.history_sort_newest)
    val sortOldestLabel = stringResource(R.string.history_sort_oldest)
    val sortNameAzLabel = stringResource(R.string.history_sort_name_az)
    val sortDateLabel = stringResource(R.string.history_sort_date)
    val sortOptions = listOf(sortNewestLabel, sortOldestLabel, sortNameAzLabel, sortDateLabel)
    val selectedSortLabel = when (selectedSort) {
        TicketSort.Newest -> sortNewestLabel
        TicketSort.Oldest -> sortOldestLabel
        TicketSort.NameAZ -> sortNameAzLabel
        TicketSort.Date -> sortDateLabel
    }
    val filterLabels = TicketFilter.entries.associateWith { stringResource(it.labelResId) }
    val filterOptions = TicketFilter.entries.map { filterLabels.getValue(it) }
    val selectedFilterLabel = filterLabels.getValue(selectedFilter)
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
                    Text(stringResource(R.string.history_select))
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
                scope.launch { snackbarHostState.showSnackbar(removedSnackbarMessage) }
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
                scope.launch { snackbarHostState.showSnackbar(deletedSnackbarMessage) }
            }
        )
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        SearchFilterSortHeader(
            variant = SearchHeaderVariant.SearchFilterSort,
            query = query,
            onQueryChange = vm::setQuery,
            placeholder = stringResource(R.string.live_play_search_tickets),
            filterOptions = filterOptions,
            selectedFilter = selectedFilterLabel,
            onFilterSelect = { label ->
                vm.setFilter(filterLabels.entries.first { it.value == label }.key)
            },
            filterCounts = filterCounts,
            showFilterCounts = true,
            sortOptions = sortOptions,
            selectedSort = selectedSortLabel,
            onSortSelect = { label ->
                val sort = when (label) {
                    sortNewestLabel -> TicketSort.Newest
                    sortOldestLabel -> TicketSort.Oldest
                    sortNameAzLabel -> TicketSort.NameAZ
                    sortDateLabel -> TicketSort.Date
                    else -> TicketSort.Newest
                }
                vm.setSort(sort)
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
                    titleRes = emptyTitleRes,
                    subtitleRes = emptySubtitleRes,
                    ctaRes = emptyCtaRes,
                    onCreateTicket = onCreateTicket,
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
                    scope.launch { snackbarHostState.showSnackbar(addedSnackbarMessage) }
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
    val headerTitle = if (selectionMode) {
        if (selectedCount == 0) {
            stringResource(R.string.history_select_items)
        } else {
            pluralStringResource(R.plurals.history_selected_count, selectedCount, selectedCount)
        }
    } else {
        stringResource(R.string.home_my_tickets)
    }
    val headerSubtitle = when {
        selectionMode -> stringResource(R.string.live_play_tickets_tap_to_select)
        roomId.isNotBlank() -> stringResource(R.string.live_play_tickets_add_or_live)
        else -> stringResource(R.string.live_play_tickets_select_to_live)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Text(
                text = headerSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectionMode) {
                TextButton(onClick = onDoneSelection) {
                    Text(stringResource(R.string.common_done))
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.common_close),
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
        isInThisRoom -> stringResource(R.string.live_play_go_live)
        isInOtherRoom -> stringResource(R.string.live_play_already_in_room)
        else -> stringResource(R.string.live_play_add_to_room)
    }
    val canAct = !isInOtherRoom
    var otherRoomName by remember(assignedRoomId) { mutableStateOf<String?>(null) }
    val anotherRoomFallback = stringResource(R.string.history_detail_another_room_fallback)
    LaunchedEffect(assignedRoomId) {
        otherRoomName = if (assignedRoomId != null) RoomRepository.getRoom(assignedRoomId)?.name else null
    }
    val inRoomSuffix = if (isInOtherRoom) {
        stringResource(
            R.string.live_play_a11y_ticket_in_room,
            otherRoomName ?: anotherRoomFallback
        )
    } else {
        ""
    }
    val summary = stringResource(
        R.string.live_play_a11y_ticket_summary,
        ticket.title,
        formatTicketDate(ticket.createdAt),
        inRoomSuffix,
        actionLabel
    )
    val placeholderDash = stringResource(R.string.common_placeholder_dash)
    val serialValue = ticket.serialNumber?.takeIf { it.isNotBlank() } ?: placeholderDash
    val losValue = ticket.losNumber?.takeIf { it.isNotBlank() } ?: placeholderDash
    val markedValue = ticket.status?.takeIf { it.isNotBlank() } ?: "--"
    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = summary }
            .clickable(
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                when {
                    selectionMode -> onToggleSelect()
                    canAct -> if (isInThisRoom) onGoLive() else onAddToRoom()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
    ) {
        if (selectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                modifier = Modifier.padding(end = Dimens.spacing4)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, cardShape)
                    } else {
                        Modifier.border(
                            Dimens.cardBorderDefault,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            cardShape
                        )
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing10),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12)
            ) {
                TicketGridThumbnailPreview(
                    matchedCells = ticket.status?.toIntOrNull() ?: 0,
                    modifier = Modifier.size(34.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatTicketDate(ticket.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                    if (isInOtherRoom) {
                        Text(
                            text = stringResource(
                                R.string.live_play_room_prefix,
                                otherRoomName ?: anotherRoomFallback
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconDefault),
                    tint = if (canAct) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TicketInfoCell(
                    label = stringResource(R.string.live_play_label_serial),
                    value = serialValue,
                    modifier = Modifier.weight(1f)
                )
                VerticalInfoDivider()
                TicketInfoCell(
                    label = stringResource(R.string.live_play_label_los),
                    value = losValue,
                    modifier = Modifier.weight(1f)
                )
                VerticalInfoDivider()
                TicketInfoCell(
                    label = stringResource(R.string.live_play_label_marked),
                    value = markedValue,
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TicketInfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AppFieldLabel(text = label)
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun VerticalInfoDivider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    )
}

@Composable
private fun TicketsEmptyState(
    modifier: Modifier = Modifier,
    titleRes: Int = R.string.live_play_no_tickets_title,
    subtitleRes: Int = R.string.live_play_no_tickets_subtitle,
    ctaRes: Int = R.string.live_play_create_new_ticket,
    onCreateTicket: () -> Unit,
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
            title = stringResource(titleRes),
            subtitle = stringResource(subtitleRes),
            modifier = Modifier.fillMaxWidth()
        )
        AppPrimaryButton(
            text = stringResource(ctaRes),
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

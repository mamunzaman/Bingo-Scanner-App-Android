package com.example.mamunbingoapp.ui.screens.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mamunbingoapp.data.LiveRoom
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.mamunbingoapp.ui.components.EmptyHistoryActionCards
import com.example.mamunbingoapp.ui.components.EmptyHistoryState
import com.example.mamunbingoapp.ui.screens.history.components.HistorySheetCard
import com.example.mamunbingoapp.ui.components.common.SearchFilterSortHeader
import com.example.mamunbingoapp.ui.components.common.SearchHeaderVariant
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.viewmodel.HistoryFilter
import com.example.mamunbingoapp.viewmodel.HistoryViewModel
import com.example.mamunbingoapp.viewmodel.HistorySortOption
import com.example.mamunbingoapp.viewmodel.HistorySourceFilter
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.BulkSelectionActionBar
import com.example.mamunbingoapp.ui.components.DeleteFromHistoryBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.LeaveRoomBulkConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppIconTile
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
@Composable
fun HistoryListScreen(
    onBack: () -> Unit,
    onSessionClick: (sessionId: String, roomId: String?) -> Unit,
    onJoinLiveRoom: (roomId: String) -> Unit = {},
    onTabSelected: (AppTab) -> Unit,
    onDeleteSession: (sessionId: String) -> Unit = {},
    onLeaveRoom: (sessionId: String) -> Unit = {},
    onAddFromPhotoClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onBulkDeleteSessions: (Collection<String>) -> Unit = { ids -> ids.forEach { onDeleteSession(it) } },
    onBulkLeaveSessions: (Collection<String>) -> Unit = { ids -> ids.forEach { onLeaveRoom(it) } },
    onBulkAddToRoom: (roomId: String, sessionIds: Collection<String>) -> Unit = { _, _ -> },
    viewModel: HistoryViewModel = viewModel()
) {
    var selectionMode by remember { mutableStateOf(false) }
    var selectedSessionIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showBulkLeaveConfirm by remember { mutableStateOf(false) }
    var showRoomPicker by remember { mutableStateOf(false) }
    val liveRooms by viewModel.liveRooms.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val filteredSessions by viewModel.filteredSessions.collectAsState()
    val sessionsWithLive by viewModel.sessionsWithLiveStatus.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val counts by viewModel.filterCounts.collectAsState()
    val selectedSourceFilter by viewModel.selectedSourceFilter.collectAsState()
    val screenHPad = Dimens.screenHorizontalPadding
    val selectedSessionIdsInRoom = remember(selectedSessionIds, filteredSessions) {
        selectedSessionIds.filter { sid ->
            filteredSessions.any { it.session.id == sid && it.roomId != null }
        }.toSet()
    }
    val selectedSessionsInLive = remember(selectedSessionIds, filteredSessions) {
        filteredSessions.filter { it.session.id in selectedSessionIds && it.roomId != null }
    }
    val selectedSessionsNotInRoom = remember(selectedSessionIds, filteredSessions) {
        filteredSessions.filter { it.session.id in selectedSessionIds && it.roomId == null }
    }
    val allSelectedInRoom = remember(selectedSessionIds, selectedSessionsNotInRoom) {
        selectedSessionIds.isNotEmpty() && selectedSessionsNotInRoom.isEmpty()
    }
    val filterAllLabel = stringResource(R.string.history_filter_all)
    val filterSavedLabel = stringResource(R.string.history_filter_saved)
    val filterCompletedLabel = stringResource(R.string.history_filter_completed)
    val filterArchivedLabel = stringResource(R.string.history_filter_archived)
    val sourceOcrLabel = stringResource(R.string.history_source_ocr)
    val sourceManualLabel = stringResource(R.string.history_source_manual)
    val sortNewestLabel = stringResource(R.string.history_sort_newest)
    val sortOldestLabel = stringResource(R.string.history_sort_oldest)
    val sortNameAzLabel = stringResource(R.string.history_sort_name_az)
    val sortDateLabel = stringResource(R.string.history_sort_date)
    val searchPlaceholder = stringResource(R.string.history_search_placeholder)
    val removedSnackbarMessage = stringResource(R.string.history_snackbar_removed_from_room)
    val deletedSnackbarMessage = stringResource(R.string.history_snackbar_deleted)
    val bulkInRoomInfoText = stringResource(R.string.history_bulk_already_in_room)
    val unnamedSheetLabel = stringResource(R.string.history_unnamed_sheet)
    val dateSavedLabel = stringResource(R.string.history_date_saved)
    val dateScannedLabel = stringResource(R.string.history_date_scanned)
    val combinedFilterOptions = listOf(
        filterAllLabel,
        sourceOcrLabel,
        sourceManualLabel,
        filterSavedLabel,
        filterCompletedLabel,
        filterArchivedLabel,
    )
    val selectedCombinedFilter = if (selectedSourceFilter == HistorySourceFilter.ALL) {
        when (selectedFilter) {
            HistoryFilter.ALL -> filterAllLabel
            HistoryFilter.SAVED -> filterSavedLabel
            HistoryFilter.COMPLETED -> filterCompletedLabel
            HistoryFilter.ARCHIVED -> filterArchivedLabel
        }
    } else {
        when (selectedSourceFilter) {
            HistorySourceFilter.OCR_IMPORTS -> sourceOcrLabel
            HistorySourceFilter.MANUAL -> sourceManualLabel
            else -> filterAllLabel
        }
    }
    val onCombinedFilterSelect: (String) -> Unit = { label ->
        when (label) {
            sourceOcrLabel -> {
                viewModel.setSourceFilter(HistorySourceFilter.OCR_IMPORTS)
                viewModel.setFilter(HistoryFilter.ALL)
            }
            sourceManualLabel -> {
                viewModel.setSourceFilter(HistorySourceFilter.MANUAL)
                viewModel.setFilter(HistoryFilter.ALL)
            }
            filterSavedLabel -> {
                viewModel.setSourceFilter(HistorySourceFilter.ALL)
                viewModel.setFilter(HistoryFilter.SAVED)
            }
            filterCompletedLabel -> {
                viewModel.setSourceFilter(HistorySourceFilter.ALL)
                viewModel.setFilter(HistoryFilter.COMPLETED)
            }
            filterArchivedLabel -> {
                viewModel.setSourceFilter(HistorySourceFilter.ALL)
                viewModel.setFilter(HistoryFilter.ARCHIVED)
            }
            else -> {
                viewModel.setSourceFilter(HistorySourceFilter.ALL)
                viewModel.setFilter(HistoryFilter.ALL)
            }
        }
    }
    val sortOptions = listOf(sortNewestLabel, sortOldestLabel, sortNameAzLabel, sortDateLabel)
    val sortLabelByOption = mapOf(
        HistorySortOption.NEWEST to sortNewestLabel,
        HistorySortOption.OLDEST to sortOldestLabel,
        HistorySortOption.NAME_AZ to sortNameAzLabel,
        HistorySortOption.DATE to sortDateLabel,
    )
    val selectedSortLabel = sortLabelByOption[selectedSort] ?: sortNewestLabel
    val onSortSelect: (String) -> Unit = { label ->
        HistorySortOption.entries.first { sortLabelByOption[it] == label }.let { viewModel.setSort(it) }
    }
    val filterCounts = mapOf(
        filterAllLabel to counts.all,
        filterSavedLabel to counts.saved,
        filterCompletedLabel to counts.completed,
        filterArchivedLabel to counts.archived,
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectionMode && filteredSessions.isNotEmpty()) {
                BulkSelectionActionBar(
                    modifier = Modifier.navigationBarsPadding(),
                    showJoinLive = selectedSessionsInLive.isNotEmpty(),
                    joinLiveEnabled = selectedSessionsInLive.isNotEmpty(),
                    joinLiveCount = selectedSessionsInLive.size,
                    onJoinLiveClick = {
                        selectedSessionsInLive.firstOrNull()?.roomId?.let { roomId ->
                            onJoinLiveRoom(roomId)
                            selectionMode = false
                            selectedSessionIds = emptySet()
                        }
                    },
                    showAddToRoom = selectedSessionsNotInRoom.isNotEmpty(),
                    addToRoomEnabled = selectedSessionsNotInRoom.isNotEmpty(),
                    addCount = selectedSessionsNotInRoom.size,
                    onAddToRoomClick = {
                        if (selectedSessionsNotInRoom.isNotEmpty()) showRoomPicker = true
                    },
                    inRoomInfoText = if (allSelectedInRoom) bulkInRoomInfoText else null,
                    showRemoveFromRoom = filteredSessions.any { it.roomId != null },
                    removeFromRoomEnabled = selectedSessionIdsInRoom.isNotEmpty(),
                    removeCount = selectedSessionIdsInRoom.size,
                    deleteEnabled = selectedSessionIds.isNotEmpty(),
                    deleteCount = selectedSessionIds.size,
                    onRemoveFromRoomClick = { showBulkLeaveConfirm = true },
                    onDeleteFromHistoryClick = { showBulkDeleteConfirm = true }
                )
            } else {
                AppBottomBar(selectedTab = AppTab.Jackpot, onTabSelected = onTabSelected)
            }
        }
    ) { paddingValues ->
        AppHeaderPageLayout(
            modifier = Modifier.padding(paddingValues),
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.history_title),
                    titleContent = if (selectionMode) {
                        {
                            Text(
                                text = if (selectedSessionIds.isEmpty()) {
                                    stringResource(R.string.history_select_items)
                                } else {
                                    pluralStringResource(
                                        R.plurals.history_selected_count,
                                        selectedSessionIds.size,
                                        selectedSessionIds.size,
                                    )
                                },
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { heading() }
                            )
                        }
                    } else {
                        null
                    },
                    showBack = true,
                    onBackClick = {
                        if (selectionMode) {
                            selectionMode = false
                            selectedSessionIds = emptySet()
                        } else {
                            onBack()
                        }
                    },
                    actions = {
                        when {
                            selectionMode -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4)
                                ) {
                                    TextButton(
                                        onClick = {
                                            selectedSessionIds =
                                                filteredSessions.map { it.session.id }.toSet()
                                        },
                                        enabled = filteredSessions.isNotEmpty() &&
                                            selectedSessionIds.size < filteredSessions.size
                                    ) {
                                        Text(stringResource(R.string.history_select_all))
                                    }
                                    TextButton(
                                        onClick = { selectedSessionIds = emptySet() },
                                        enabled = selectedSessionIds.isNotEmpty()
                                    ) {
                                        Text(stringResource(R.string.history_clear_selection))
                                    }
                                }
                            }
                            else -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (filteredSessions.isNotEmpty()) {
                                        TextButton(onClick = {
                                            selectionMode = true
                                            selectedSessionIds = emptySet()
                                        }) {
                                            Text(stringResource(R.string.history_select))
                                        }
                                    }
                                    IconButton(
                                        onClick = onAddFromPhotoClick,
                                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                    ) {
                                        AppIconTile(
                                            icon = Icons.Default.Add,
                                            size = 40.dp,
                                            iconSize = 24.dp,
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            iconTint = MaterialTheme.colorScheme.onPrimary,
                                            shape = RoundedCornerShape(50),
                                            contentDescription = stringResource(R.string.history_add_from_photo_cd),
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            content = {
                LeaveRoomBulkConfirmDialog(
                    visible = showBulkLeaveConfirm,
                    count = selectedSessionIdsInRoom.size,
                    onDismiss = { showBulkLeaveConfirm = false },
                    onConfirm = {
                        onBulkLeaveSessions(selectedSessionIdsInRoom)
                        selectionMode = false
                        selectedSessionIds = emptySet()
                        showBulkLeaveConfirm = false
                        scope.launch {
                            snackbarHostState.showSnackbar(removedSnackbarMessage)
                        }
                    }
                )
                DeleteFromHistoryBulkConfirmDialog(
                    visible = showBulkDeleteConfirm,
                    count = selectedSessionIds.size,
                    onDismiss = { showBulkDeleteConfirm = false },
                    onConfirm = {
                        onBulkDeleteSessions(selectedSessionIds)
                        selectionMode = false
                        selectedSessionIds = emptySet()
                        showBulkDeleteConfirm = false
                        scope.launch {
                            snackbarHostState.showSnackbar(deletedSnackbarMessage)
                        }
                    }
                )
                if (showRoomPicker) {
                    val pendingIds = selectedSessionsNotInRoom.map { it.session.id }
                    AddToRoomPickerSheet(
                        rooms = liveRooms,
                        onRoomSelected = { roomId ->
                            onBulkAddToRoom(roomId, pendingIds)
                            showRoomPicker = false
                            selectionMode = false
                            selectedSessionIds = emptySet()
                        },
                        onDismiss = { showRoomPicker = false },
                        onCreateRoom = onPlayClick
                    )
                }
                if (filteredSessions.isEmpty() && sessionsWithLive.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                                                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                                                .fillMaxWidth()
                                .padding(top = Dimens.spacing8)
                        ) {
                            SearchFilterSortHeader(
                                variant = SearchHeaderVariant.SearchFilterSort,
                                query = query,
                                onQueryChange = viewModel::setQuery,
                                placeholder = searchPlaceholder,
                                filterOptions = combinedFilterOptions,
                                selectedFilter = selectedCombinedFilter,
                                onFilterSelect = onCombinedFilterSelect,
                                sortOptions = sortOptions,
                                selectedSort = selectedSortLabel,
                                onSortSelect = onSortSelect,
                                showFilterCounts = true,
                                filterCounts = filterCounts,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = screenHPad),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                EmptyHistoryState(
                                    title = stringResource(R.string.history_empty_title),
                                    subtitle = stringResource(R.string.history_empty_subtitle),
                                    icon = Icons.Default.History,
                                    actions = {
                                        EmptyHistoryActionCards(
                                            onPlayClick = onPlayClick,
                                            onScanClick = onAddFromPhotoClick,
                                            modifier = Modifier.fillMaxWidth(),
                                            showTitleAndSubtitle = false
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                                                        .fillMaxWidth()
                ) {
                    val listAreaHeight = maxHeight
                    val historyHeaderHeight = 148.dp
                    val listState = rememberLazyListState()
                    val isScrolled = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                    val separatorMaskAlpha by animateFloatAsState(
                        targetValue = if (isScrolled) 1f else 0f,
                        label = "historyHeaderSeparatorMaskAlpha"
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = historyHeaderHeight, bottom = Dimens.spacing24),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
                        ) {
            if (filteredSessions.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier.height(listAreaHeight),
                            contentAlignment = if (sessionsWithLive.isEmpty()) Alignment.Center else Alignment.TopCenter
                        ) {
                            if (sessionsWithLive.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                        .padding(horizontal = screenHPad),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    EmptyHistoryState(
                                        title = stringResource(R.string.history_empty_title),
                                        subtitle = stringResource(R.string.history_empty_subtitle),
                                        icon = Icons.Default.History,
                                        actions = {
                                            EmptyHistoryActionCards(
                                                onPlayClick = onPlayClick,
                                                onScanClick = onAddFromPhotoClick,
                                                modifier = Modifier.fillMaxWidth(),
                                                showTitleAndSubtitle = false
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                        .padding(horizontal = screenHPad)
                                        .padding(top = Dimens.spacing8)
                                ) {
                                    EmptyHistoryState(
                                        title = stringResource(R.string.history_no_results_title),
                                        subtitle = stringResource(R.string.history_no_results_subtitle),
                                        icon = Icons.Default.History
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredSessions) { item ->
                        val markedForCard = maxOf(
                            item.resolvedMarkedCount,
                            item.session.sheetsPlayed.firstOrNull()?.markedCount ?: 0
                        )
                        val playedAt = item.session.effectivePlayedAtMillis()
                        val dateLabelPrefix =
                            if (item.session.ocrSource.isNullOrBlank()) dateSavedLabel else dateScannedLabel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = screenHPad)
                        ) {
                            HistorySheetCard(
                                title = item.session.effectiveSheetName().ifEmpty { unnamedSheetLabel },
                                playedAtMillis = playedAt,
                                dateLabelPrefix = dateLabelPrefix,
                                serialNumber = item.session.serialNumber,
                                losNumber = item.session.losNumber,
                                markedCount = markedForCard,
                                markedCells = item.resolvedMarkedCells.takeIf { it.size == 25 },
                                onViewClick = { onSessionClick(item.session.id, item.roomId) },
                                onJoinClick = if (item.isLive && item.roomId != null) ({ onJoinLiveRoom(item.roomId!!) }) else ({ onSessionClick(item.session.id, item.roomId) }),
                                onDelete = { onDeleteSession(item.session.id) },
                                onLeaveRoom = if (item.roomId != null) ({ onLeaveRoom(item.session.id) }) else null,
                                selectionMode = selectionMode,
                                selected = item.session.id in selectedSessionIds,
                                inRoom = item.roomId != null,
                                roomName = item.roomName,
                                onSelectionToggle = {
                                    val id = item.session.id
                                    selectedSessionIds =
                                        if (id in selectedSessionIds) selectedSessionIds - id
                                        else selectedSessionIds + id
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                }
            }
            }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                                                .fillMaxWidth()
                                .zIndex(2f)
                        ) {
                            Column(
                                modifier = Modifier
                                                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(top = Dimens.spacing8)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    SearchFilterSortHeader(
                                        variant = SearchHeaderVariant.SearchFilterSort,
                                        query = query,
                                        onQueryChange = viewModel::setQuery,
                                        placeholder = searchPlaceholder,
                                        filterOptions = combinedFilterOptions,
                                        selectedFilter = selectedCombinedFilter,
                                        onFilterSelect = onCombinedFilterSelect,
                                        sortOptions = sortOptions,
                                        selectedSort = selectedSortLabel,
                                        onSortSelect = onSortSelect,
                                        showFilterCounts = true,
                                        filterCounts = filterCounts,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(
                                                start = Dimens.screenHorizontalPadding + Dimens.spacing16,
                                                end = Dimens.screenHorizontalPadding + Dimens.spacing16,
                                                top = Dimens.spacing12 + Dimens.textFieldHeight
                                            )
                                                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = separatorMaskAlpha)
                                            )
                                    )
                                }
                            }
                        }
                }
        }
        }
        }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToRoomPickerSheet(
    rooms: List<LiveRoom>,
    onRoomSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreateRoom: () -> Unit = {}
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
                .padding(horizontal = Dimens.spacing24, vertical = Dimens.spacing8)
        ) {
            Text(
                text = stringResource(R.string.history_choose_live_room),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = Dimens.spacing16)
            )
            if (rooms.isEmpty()) {
                Text(
                    text = stringResource(R.string.history_no_active_rooms),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Dimens.spacing12)
                )
                TextButton(
                    onClick = {
                        onDismiss()
                        onCreateRoom()
                    }
                ) {
                    Text(stringResource(R.string.history_create_live_room))
                }
            } else {
                rooms.forEachIndexed { index, room ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoomSelected(room.roomId) }
                            .padding(vertical = Dimens.spacing12),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.common_add),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.settings_cancel))
            }
            Spacer(modifier = Modifier.height(Dimens.spacing8))
        }
    }
}










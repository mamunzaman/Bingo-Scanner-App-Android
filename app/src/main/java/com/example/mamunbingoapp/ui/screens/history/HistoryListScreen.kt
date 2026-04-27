package com.example.mamunbingoapp.ui.screens.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
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
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
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
    viewModel: HistoryViewModel = viewModel()
) {
    var selectionMode by remember { mutableStateOf(false) }
    var selectedSessionIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showBulkLeaveConfirm by remember { mutableStateOf(false) }
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
    val combinedFilterOptions = buildList {
        add(HistoryFilter.ALL.displayName)
        add(HistorySourceFilter.OCR_IMPORTS.displayName)
        add(HistorySourceFilter.MANUAL.displayName)
        addAll(
            HistoryFilter.entries
                .filter { it != HistoryFilter.ALL }
                .map { it.displayName }
        )
    }
    val selectedCombinedFilter = if (selectedSourceFilter == HistorySourceFilter.ALL) {
        selectedFilter.displayName
    } else {
        selectedSourceFilter.displayName
    }
    val onCombinedFilterSelect: (String) -> Unit = { label ->
        val sourceOption = HistorySourceFilter.entries.firstOrNull { it.displayName == label }
        if (sourceOption != null && sourceOption != HistorySourceFilter.ALL) {
            viewModel.setSourceFilter(sourceOption)
            viewModel.setFilter(HistoryFilter.ALL)
        } else {
            viewModel.setSourceFilter(HistorySourceFilter.ALL)
            viewModel.setFilter(HistoryFilter.entries.first { it.displayName == label })
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (selectionMode && filteredSessions.isNotEmpty()) {
                BulkSelectionActionBar(
                    modifier = Modifier.navigationBarsPadding(),
                    showJoinLive = true,
                    joinLiveEnabled = selectedSessionsInLive.isNotEmpty(),
                    joinLiveCount = selectedSessionsInLive.size,
                    onJoinLiveClick = {
                        selectedSessionsInLive.firstOrNull()?.roomId?.let { roomId ->
                            onJoinLiveRoom(roomId)
                            selectionMode = false
                            selectedSessionIds = emptySet()
                        }
                    },
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
                    title = "History",
                    titleContent = if (selectionMode) {
                        {
                            Text(
                                text = if (selectedSessionIds.isEmpty()) {
                                    "Select items"
                                } else {
                                    "${selectedSessionIds.size} selected"
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
                                        Text("Select all")
                                    }
                                    TextButton(
                                        onClick = { selectedSessionIds = emptySet() },
                                        enabled = selectedSessionIds.isNotEmpty()
                                    ) {
                                        Text("Clear")
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
                                            Text("Select")
                                        }
                                    }
                                    IconButton(
                                        onClick = onAddFromPhotoClick,
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
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add from photo",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
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
                            snackbarHostState.showSnackbar("Removed from room")
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
                            snackbarHostState.showSnackbar("Deleted from history")
                        }
                    }
                )
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
                                placeholder = "Search sessions",
                                filterOptions = combinedFilterOptions,
                                selectedFilter = selectedCombinedFilter,
                                onFilterSelect = onCombinedFilterSelect,
                                sortOptions = HistorySortOption.entries.map { it.displayName },
                                selectedSort = selectedSort.displayName,
                                onSortSelect = { label ->
                                    viewModel.setSort(HistorySortOption.entries.first { it.displayName == label })
                                },
                                showFilterCounts = true,
                                filterCounts = mapOf(
                                    "All" to counts.all,
                                    "Saved" to counts.saved,
                                    "Completed" to counts.completed,
                                    "Archived" to counts.archived
                                ),
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
                                    title = "No sessions yet",
                                    subtitle = "Your played sessions will appear here.",
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
                    val historyHeaderHeight = 136.dp
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
                            contentPadding = PaddingValues(top = historyHeaderHeight, bottom = Dimens.spacing8),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)
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
                                        title = "No sessions yet",
                                        subtitle = "Your played sessions will appear here.",
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
                                        title = "No results",
                                        subtitle = "Try a different search or filter.",
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
                            if (item.session.ocrSource.isNullOrBlank()) "Saved" else "Scanned"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = screenHPad)
                        ) {
                            HistorySheetCard(
                                title = item.session.effectiveSheetName().ifEmpty { "Unnamed sheet" },
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
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                                    .padding(top = Dimens.spacing8)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    SearchFilterSortHeader(
                                        variant = SearchHeaderVariant.SearchFilterSort,
                                        query = query,
                                        onQueryChange = viewModel::setQuery,
                                        placeholder = "Search sessions",
                                        filterOptions = combinedFilterOptions,
                                        selectedFilter = selectedCombinedFilter,
                                        onFilterSelect = onCombinedFilterSelect,
                                        sortOptions = HistorySortOption.entries.map { it.displayName },
                                        selectedSort = selectedSort.displayName,
                                        onSortSelect = { label ->
                                            viewModel.setSort(HistorySortOption.entries.first { it.displayName == label })
                                        },
                                        showFilterCounts = true,
                                        filterCounts = mapOf(
                                            "All" to counts.all,
                                            "Saved" to counts.saved,
                                            "Completed" to counts.completed,
                                            "Archived" to counts.archived
                                        ),
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










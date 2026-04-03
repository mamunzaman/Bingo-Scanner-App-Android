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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.mamunbingoapp.ui.components.EmptyHistoryActionCards
import com.example.mamunbingoapp.ui.components.EmptyHistoryState
import com.example.mamunbingoapp.ui.components.RoomSessionCard
import com.example.mamunbingoapp.ui.components.common.SearchFilterSortHeader
import com.example.mamunbingoapp.ui.components.common.SearchHeaderVariant
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.viewmodel.HistoryFilter
import com.example.mamunbingoapp.viewmodel.HistoryViewModel
import com.example.mamunbingoapp.viewmodel.HistorySortOption
import com.example.mamunbingoapp.viewmodel.HistorySourceFilter
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.core.SheetStatusResolver
import com.example.mamunbingoapp.ui.model.SheetStatus
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
    viewModel: HistoryViewModel = viewModel()
) {
    val filteredSessions by viewModel.filteredSessions.collectAsState()
    val sessionsWithLive by viewModel.sessionsWithLiveStatus.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val counts by viewModel.filterCounts.collectAsState()
    val selectedSourceFilter by viewModel.selectedSourceFilter.collectAsState()
    val screenHPad = Dimens.screenHorizontalPadding
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
        bottomBar = { AppBottomBar(selectedTab = AppTab.Jackpot, onTabSelected = onTabSelected) }
    ) { paddingValues ->
        Surface(color = MaterialTheme.colorScheme.surface) {
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
            Column(modifier = Modifier.fillMaxSize()) {
                AppTopBar(
                    title = "History",
                    showBack = true,
                    onBackClick = onBack,
                    actions = {
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
                            contentPadding = PaddingValues(top = historyHeaderHeight, bottom = Dimens.spacing16),
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
                        val ticketsCount = item.session.sheetsCount.coerceAtLeast(1)
                        val calledCount = item.resolvedCalledCount.coerceAtLeast(0)
                        val sheetStatus = SheetStatusResolver.resolve(
                            assignedRoomId = item.roomId,
                            calledCount = calledCount
                        )
                        val (statusText, statusDotColor) = when (sheetStatus) {
                            SheetStatus.ACTIVE -> "Active" to MaterialTheme.colorScheme.primary
                            SheetStatus.COMPLETED -> "Completed" to MaterialTheme.colorScheme.onSurfaceVariant
                            SheetStatus.IN_PROGRESS -> "In Progress" to MaterialTheme.colorScheme.onSurfaceVariant
                            SheetStatus.IDLE -> "Saved" to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val (actionText, onAction) = if (item.isLive && item.roomId != null) {
                            "Join" to { onJoinLiveRoom(item.roomId!!) }
                        } else {
                            "View" to { onSessionClick(item.session.id, item.roomId) }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = screenHPad)
                        ) {
                            RoomSessionCard(
                                title = item.session.effectiveSheetName().ifEmpty { "Unnamed sheet" },
                                statusText = statusText,
                                statusDotColor = statusDotColor,
                                ticketsCount = ticketsCount,
                                calledCount = calledCount,
                                actionText = actionText,
                                onActionClick = onAction,
                                modifier = Modifier.fillMaxWidth(),
                                onCardClick = { onSessionClick(item.session.id, item.roomId) },
                                sheetStatus = sheetStatus,
                                addedToRoomName = item.roomName,
                                addedAtMillis = item.session.effectivePlayedAtMillis(),
                                markedCount = maxOf(item.resolvedMarkedCount, item.resolvedCalledCount),
                                markedCells = item.resolvedMarkedCells.takeIf { it.size == 25 },
                                almostBingo = item.almostBingo,
                                bingoWinLineCount = item.bingoWinLineCount,
                                actionIcon = if (actionText == "Join") Icons.Default.PlayArrow else Icons.AutoMirrored.Filled.OpenInNew,
                                onViewClick = if (item.isLive && item.roomId != null) {{ onSessionClick(item.session.id, item.roomId) }} else null,
                                onDeleteClick = { onDeleteSession(item.session.id) },
                                onLeaveRoomClick = if (item.roomId != null) { { onLeaveRoom(item.session.id) } } else null,
                                ocrSource = item.session.ocrSource,
                                editedAfterOcr = item.editedAfterOcr,
                                ocrCorrectionCount = item.ocrCorrectionCount
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
    }
    }
    }
}



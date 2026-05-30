package com.example.mamunbingoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mamunbingoapp.data.HistoryRepository
import com.example.mamunbingoapp.data.HistorySession
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.data.db.TicketCellEntity
import com.example.mamunbingoapp.data.remote.BingoDrawDto
import com.example.mamunbingoapp.viewmodel.HomeViewModel
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.GreenImpactBg
import com.example.mamunbingoapp.ui.components.CalledNumbersDetailSheet
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppPullRefresh
import com.example.mamunbingoapp.ui.components.ProfileAvatar
import com.example.mamunbingoapp.ui.components.profileAvatarInitials
import com.example.mamunbingoapp.ui.components.home.HomeQuickScanFab
import com.example.mamunbingoapp.ui.components.home.QuickActionItem
import com.example.mamunbingoapp.ui.components.home.QuickActionsScrollRow
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.ui.components.AppIconTile
import com.example.mamunbingoapp.ui.components.AppSectionHeader
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCard
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCardModel
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCellState
import com.example.mamunbingoapp.ui.components.home.CurrentJackpotCard
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.ui.screens.scan.ScanTypeSelectionSheet
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val berlinZone: ZoneId = ZoneId.of("Europe/Berlin")

private fun dispatchHomeQuickAction(
    action: String,
    onQuickActionClick: (String) -> Unit,
) {
    runCatching { onQuickActionClick(action) }
}

private val homeContentBottomMargin = Dimens.spacing24

@Composable
fun HomeScreen(
    onLaunchCamera: (BingoScanType) -> Unit = {},
    onQuickActionClick: (String) -> Unit = {},
    /** History session id → [HistoryDetailScreen] route (`historyDetail/{sessionId}`). */
    onTicketClick: (String) -> Unit = {},
    onViewAllTickets: () -> Unit = {},
    onTabSelected: (AppTab) -> Unit = {},
    showBottomBar: Boolean = true,
    profileDisplayName: String? = null,
    profileAvatarUrl: String? = null,
    homeAvatarInitials: String? = null,
    isProfileRefreshing: Boolean = false,
    onProfileRefresh: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
) {
    val latestDraw by homeViewModel.latestDraw.collectAsStateWithLifecycle()
    val isRemoteLoading by homeViewModel.isRemoteLoading.collectAsStateWithLifecycle()
    val remoteError by homeViewModel.remoteError.collectAsStateWithLifecycle()
    val tickets by HistoryRepository.sessionsFlow.collectAsStateWithLifecycle()
    val cellsByTicket by TicketRepository.cellsByTicketFlow()
        .collectAsStateWithLifecycle(initialValue = emptyMap())
    val ticketToRoom by RoomRepository.ticketToRoomFlow()
        .collectAsStateWithLifecycle(initialValue = emptyMap())
    val calledNumbersByRoom by RoomRepository.allRoomsCalledNumbersFlow()
        .collectAsStateWithLifecycle(initialValue = emptyMap())
    val archivedByRoom by RoomRepository.roomsArchivedMapFlow()
        .collectAsStateWithLifecycle(initialValue = emptyMap())
    val defaultPlayerName = stringResource(R.string.home_default_player_name)
    val welcomeName = profileDisplayName?.takeIf { it.isNotBlank() } ?: defaultPlayerName
    val showProfileSummary = profileDisplayName != null
    var showScanTypeSheet by remember { mutableStateOf(false) }
    val requestScan = { showScanTypeSheet = true }
    AppHeaderPageLayout(
        topBar = {
        AppTopBar(
            title = "",
            titleContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showProfileSummary) {
                        ProfileAvatar(
                            avatarUrl = profileAvatarUrl,
                            initials = homeAvatarInitials
                                ?: profileAvatarInitials(welcomeName),
                            size = 40.dp,
                            showEditBadge = false,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape,
                                ),
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = timeAwareGreeting(welcomeName),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing4))
                        Text(
                            text = stringResource(R.string.home_ready_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.home_notifications_cd),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        },
        content = {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val scrollModifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing8)
            if (showProfileSummary) {
                AppPullRefresh(
                    isRefreshing = isProfileRefreshing,
                    onRefresh = onProfileRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(modifier = scrollModifier) {
                        HomeScrollBody(
                            onScanClick = requestScan,
                            onQuickActionClick = onQuickActionClick,
                            onTicketClick = onTicketClick,
                            onViewAllTickets = onViewAllTickets,
                            latestDraw = latestDraw,
                            isRemoteLoading = isRemoteLoading,
                            remoteError = remoteError,
                            tickets = tickets,
                            cellsByTicket = cellsByTicket,
                            ticketToRoom = ticketToRoom,
                            calledNumbersByRoom = calledNumbersByRoom,
                            archivedByRoom = archivedByRoom,
                        )
                    }
                }
            } else {
                Column(modifier = scrollModifier) {
                    HomeScrollBody(
                        onScanClick = requestScan,
                        onQuickActionClick = onQuickActionClick,
                        onTicketClick = onTicketClick,
                        onViewAllTickets = onViewAllTickets,
                        latestDraw = latestDraw,
                        isRemoteLoading = isRemoteLoading,
                        remoteError = remoteError,
                        tickets = tickets,
                        cellsByTicket = cellsByTicket,
                        ticketToRoom = ticketToRoom,
                        calledNumbersByRoom = calledNumbersByRoom,
                        archivedByRoom = archivedByRoom,
                    )
                }
            }
            HomeQuickScanFab(
                onClick = requestScan,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = Dimens.screenHorizontalPadding,
                        bottom = homeContentBottomMargin,
                    ),
            )
        }
        if (showBottomBar) {
            AppBottomBar(selectedTab = AppTab.Home, onTabSelected = onTabSelected)
        }
        }
    )
    if (showScanTypeSheet) {
        ScanTypeSelectionSheet(
            onDismiss = { showScanTypeSheet = false },
            onScanTypeSelected = { type ->
                showScanTypeSheet = false
                onLaunchCamera(type)
            },
        )
    }
}

@Composable
private fun HomeScrollBody(
    onScanClick: () -> Unit,
    onQuickActionClick: (String) -> Unit,
    onTicketClick: (String) -> Unit,
    onViewAllTickets: () -> Unit,
    latestDraw: BingoDrawDto?,
    isRemoteLoading: Boolean,
    remoteError: String?,
    tickets: List<HistorySession>,
    cellsByTicket: Map<String, List<TicketCellEntity>>,
    ticketToRoom: Map<String, String>,
    calledNumbersByRoom: Map<String, List<Int>>,
    archivedByRoom: Map<String, Boolean>,
) {
        val activeLiveRoomId = remember(calledNumbersByRoom, archivedByRoom, ticketToRoom) {
            resolveActiveLiveRoomId(calledNumbersByRoom, archivedByRoom, ticketToRoom)
        }
        val ticketSummary = remember(
            tickets,
            cellsByTicket,
            ticketToRoom,
            calledNumbersByRoom,
            activeLiveRoomId,
        ) {
            buildHomeActiveTicketsSummary(
                tickets = tickets,
                cellsByTicket = cellsByTicket,
                ticketToRoom = ticketToRoom,
                calledNumbersByRoom = calledNumbersByRoom,
                activeLiveRoomId = activeLiveRoomId,
            )
        }
        val latestNumbers = latestDraw?.winningNumbers.orEmpty()
        var showLatestNumbersSheet by remember { mutableStateOf(false) }
        Column {
            CurrentJackpotCard(
                latestDraw = latestDraw,
                isRemoteLoading = isRemoteLoading,
                remoteError = remoteError,
                onScanClick = onScanClick,
                onLatestNumbersClick = {
                    if (latestNumbers.isNotEmpty()) showLatestNumbersSheet = true
                },
            )
            Spacer(modifier = Modifier.height(Dimens.spacing24))
            Text(
                text = stringResource(R.string.home_quick_actions),
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            QuickActionsScrollRow(
                modifier = Modifier.fillMaxWidth(),
                items = listOf(
                    QuickActionItem(
                        label = stringResource(R.string.home_scan_ticket),
                        icon = Icons.Filled.QrCodeScanner,
                        onClick = { runCatching { onScanClick() } },
                        emphasized = true,
                    ),
                    QuickActionItem(
                        label = stringResource(R.string.home_my_tickets),
                        icon = Icons.Filled.ConfirmationNumber,
                        onClick = { dispatchHomeQuickAction("tickets", onQuickActionClick) },
                    ),
                    QuickActionItem(
                        label = stringResource(R.string.home_results),
                        icon = Icons.Filled.EmojiEvents,
                        onClick = { dispatchHomeQuickAction("results", onQuickActionClick) },
                    ),
                    QuickActionItem(
                        label = stringResource(R.string.home_help),
                        icon = Icons.AutoMirrored.Filled.Help,
                        onClick = { dispatchHomeQuickAction("help", onQuickActionClick) },
                    ),
                ),
            )
            Spacer(modifier = Modifier.height(Dimens.spacing24))
            AppSectionHeader(
                title = stringResource(R.string.home_active_tickets),
                actionText = stringResource(R.string.home_view_all),
                onActionClick = onViewAllTickets,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            HomeActiveTicketsSummaryRow(summary = ticketSummary)
            if (tickets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.spacing12))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = Dimens.spacing4),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing14),
                ) {
                    tickets
                        .sortedByDescending { it.effectivePlayedAtMillis() }
                        .take(5)
                        .forEach { session ->
                            val preview = ticketSummary.previews.find { it.sessionId == session.id }
                            if (preview != null) {
                                val calledLabel = preview.liveCalledCount?.let { count ->
                                    stringResource(R.string.home_ticket_called_count, count)
                                }
                                ActiveTicketCard(
                                    model = ActiveTicketCardModel(
                                        ticketLabel = sessionDisplayLabel(session),
                                        drawDate = formatHomeTicketDate(session.effectivePlayedAtMillis()),
                                        isInLiveRoom = preview.isInLiveRoom,
                                        calledCountLabel = calledLabel,
                                        calledProgress = preview.calledProgress,
                                        showCalledProgress = preview.showCalledProgress,
                                        neutralGrid = preview.neutralGrid,
                                        cellStates = preview.cellStates,
                                    ),
                                    onClick = { onTicketClick(session.id) },
                                )
                            }
                        }
                    Spacer(modifier = Modifier.width(Dimens.spacing8))
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacing32))
            GreenImpactCard(treesPlanted = 120, treesToMilestone = 30)
            Spacer(modifier = Modifier.height(homeContentBottomMargin))
        }
        if (showLatestNumbersSheet && latestNumbers.isNotEmpty()) {
            CalledNumbersDetailSheet(
                onDismiss = { showLatestNumbersSheet = false },
                calledNumbers = latestNumbers,
                title = stringResource(R.string.home_latest_numbers_title),
                countPillText = pluralStringResource(
                    R.plurals.home_latest_numbers_count,
                    latestNumbers.size,
                    latestNumbers.size,
                ),
                footerText = pluralStringResource(
                    R.plurals.home_latest_numbers_footer,
                    latestNumbers.distinct().size,
                    latestNumbers.distinct().size,
                ),
            )
        }
}

@Composable
private fun HomeActiveTicketsSummaryRow(summary: HomeActiveTicketsSummary) {
    val shape = RoundedCornerShape(Dimens.radiusLarge)
    val cs = MaterialTheme.colorScheme
    val glassSurface = Color.White.copy(alpha = 0.94f)
    val borderColor = cs.outlineVariant.copy(alpha = 0.45f)
    val emDash = stringResource(R.string.common_em_dash)
    val calledValue = if (summary.hasActiveLiveRoom) summary.calledCount.toString() else emDash
    val calledLabel = if (summary.hasActiveLiveRoom) {
        pluralStringResource(R.plurals.home_numbers_called, summary.calledCount)
    } else {
        stringResource(R.string.home_no_live_session)
    }
    val activeTicketsLabel = pluralStringResource(R.plurals.home_active_tickets, summary.activeCount)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .iosElevatedShadow(elevation = 4.dp, shape = shape)
                .clip(shape)
                .background(glassSurface)
                .border(Dimens.cardBorderDefault, borderColor, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing14, vertical = Dimens.spacing12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeActiveTicketsStatItem(
                    icon = Icons.Filled.ConfirmationNumber,
                    valueText = summary.activeCount.toString(),
                    labelText = activeTicketsLabel,
                    iconContainerColor = Primary.copy(alpha = 0.09f),
                    iconTint = Primary.copy(alpha = 0.88f),
                    valueColor = cs.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = Dimens.spacing10)
                        .width(1.dp)
                        .height(34.dp)
                        .background(cs.outlineVariant.copy(alpha = 0.28f)),
                )
                HomeActiveTicketsStatItem(
                    icon = Icons.Filled.LiveTv,
                    valueText = calledValue,
                    labelText = calledLabel,
                    iconContainerColor = if (summary.hasActiveLiveRoom) {
                        Primary.copy(alpha = 0.10f)
                    } else {
                        cs.surfaceVariant.copy(alpha = 0.72f)
                    },
                    iconTint = if (summary.hasActiveLiveRoom) Primary.copy(alpha = 0.88f) else cs.onSurfaceVariant,
                    valueColor = if (summary.hasActiveLiveRoom) Primary else cs.onSurfaceVariant,
                    showLiveDot = summary.hasActiveLiveRoom,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (summary.activeCount == 0) {
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            Text(
                text = stringResource(R.string.home_scan_to_track_hint),
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeActiveTicketsStatItem(
    icon: ImageVector,
    valueText: String,
    labelText: String,
    iconContainerColor: Color,
    iconTint: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
    showLiveDot: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        Box(contentAlignment = Alignment.Center) {
            AppIconTile(
                icon = icon,
                size = 32.dp,
                iconSize = 18.dp,
                containerColor = iconContainerColor,
                iconTint = iconTint,
            )
            if (showLiveDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(1.dp, Color.White, CircleShape),
                )
            }
        }
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                )
                Text(
                    text = " $labelText",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

private data class HomeTicketPreview(
    val sessionId: String,
    val isInLiveRoom: Boolean,
    val liveCalledCount: Int?,
    val calledProgress: Float,
    val showCalledProgress: Boolean,
    val neutralGrid: Boolean,
    val cellStates: List<ActiveTicketCellState>,
)

private data class HomeActiveTicketsSummary(
    val activeCount: Int,
    val calledCount: Int,
    val hasActiveLiveRoom: Boolean,
    val previews: List<HomeTicketPreview>,
)

private fun resolveActiveLiveRoomId(
    calledNumbersByRoom: Map<String, List<Int>>,
    archivedByRoom: Map<String, Boolean>,
    ticketToRoom: Map<String, String>,
): String? {
    val liveRoomIds = ticketToRoom.values.toSet()
    return calledNumbersByRoom
        .filter { (roomId, numbers) ->
            numbers.isNotEmpty() &&
                !archivedByRoom.getOrDefault(roomId, false) &&
                (liveRoomIds.isEmpty() || roomId in liveRoomIds)
        }
        .maxByOrNull { it.value.size }
        ?.key
}

private fun buildHomeActiveTicketsSummary(
    tickets: List<HistorySession>,
    cellsByTicket: Map<String, List<TicketCellEntity>>,
    ticketToRoom: Map<String, String>,
    calledNumbersByRoom: Map<String, List<Int>>,
    activeLiveRoomId: String?,
): HomeActiveTicketsSummary {
    val activeCalledNumbers = activeLiveRoomId
        ?.let { calledNumbersByRoom[it] }
        .orEmpty()
    val activeCalledSet = activeCalledNumbers.toSet()
    val activeCalledCount = activeCalledNumbers.size

    val previews = tickets.map { session ->
        val roomId = ticketToRoom[session.id]
        val isInLiveRoom = activeLiveRoomId != null && roomId == activeLiveRoomId
        val cells = cellsByTicket[session.id].orEmpty().sortedBy { it.cellIndex }
        val cellStates = (0 until 25).map { index ->
            val cell = cells.getOrNull(index)
            val number = cell?.value?.trim()?.takeIf { it.uppercase() != "FREE" }?.toIntOrNull()
            val isCalled = isInLiveRoom && number != null && number in activeCalledSet
            ActiveTicketCellState(
                display = cell?.value?.trim().orEmpty(),
                isCalled = isCalled,
            )
        }
        HomeTicketPreview(
            sessionId = session.id,
            isInLiveRoom = isInLiveRoom,
            liveCalledCount = if (isInLiveRoom && activeCalledCount > 0) activeCalledCount else null,
            calledProgress = activeCalledCount / 75f,
            showCalledProgress = isInLiveRoom,
            neutralGrid = !isInLiveRoom,
            cellStates = cellStates,
        )
    }
    return HomeActiveTicketsSummary(
        activeCount = tickets.size,
        calledCount = activeCalledCount,
        hasActiveLiveRoom = activeLiveRoomId != null,
        previews = previews,
    )
}

@Composable
private fun timeAwareGreeting(name: String): String {
    val hour = ZonedDateTime.now(berlinZone).hour
    val salutation = when (hour) {
        in 5..11 -> stringResource(R.string.home_greeting_morning)
        in 12..17 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_evening)
    }
    return stringResource(R.string.home_greeting_format, salutation, name)
}

private fun sessionDisplayLabel(session: HistorySession): String {
    session.serialNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { return "#$it" }
    session.losNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { return "#$it" }
    val sheet = session.effectiveSheetName()
    if (sheet.isNotBlank() && !sheet.equals("Unnamed", ignoreCase = true)) {
        return sheet.take(14)
    }
    return "#${session.id.takeLast(4).uppercase(Locale.getDefault())}"
}

private fun formatHomeTicketDate(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    return ZonedDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(millis),
        berlinZone,
    ).format(formatter)
}

@Composable
private fun GreenImpactCard(
    treesPlanted: Int,
    treesToMilestone: Int,
    modifier: Modifier = Modifier
) {
    val progress = 0.75f
    AppSectionSurface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(GreenImpactBg),
        color = GreenImpactBg,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Filled.Forest,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(180.dp)
                    .offset(x = 40.dp, y = 40.dp),
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            )
            Column(
                modifier = Modifier.padding(Dimens.spacing16)
            ) {
                Text(
                    text = stringResource(R.string.home_green_impact),
                    style = AppTextStyles.sectionTitle,
                    color = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_trees_planted, treesPlanted),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.home_milestone, treesToMilestone),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

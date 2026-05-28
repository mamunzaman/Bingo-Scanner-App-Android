package com.example.mamunbingoapp.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.GreenImpactBg
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppPullRefresh
import com.example.mamunbingoapp.ui.components.ProfileAvatar
import com.example.mamunbingoapp.ui.components.profileAvatarInitials
import com.example.mamunbingoapp.ui.components.AppCard
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppSectionHeader
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCard
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCardModel
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCellState
import com.example.mamunbingoapp.ui.components.home.CurrentJackpotCard
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.delay

private val berlinZone: ZoneId = ZoneId.of("Europe/Berlin")

@Composable
fun HomeScreen(
    onScanClick: () -> Unit = {},
    onQuickActionClick: (String) -> Unit = {},
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
    val welcomeName = profileDisplayName?.takeIf { it.isNotBlank() } ?: "Player"
    val showProfileSummary = profileDisplayName != null
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
                            text = "Ready for the next draw?",
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
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        },
        content = {
        val homeScrollModifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .padding(top = Dimens.spacing8, bottom = Dimens.spacing16)
        if (showProfileSummary) {
            AppPullRefresh(
                isRefreshing = isProfileRefreshing,
                onRefresh = onProfileRefresh,
                modifier = Modifier.weight(1f),
            ) {
                Column(modifier = homeScrollModifier) {
                    HomeScrollBody(
                        onScanClick = onScanClick,
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(homeScrollModifier),
            ) {
                HomeScrollBody(
                    onScanClick = onScanClick,
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
        if (showBottomBar) {
            AppBottomBar(selectedTab = AppTab.Home, onTabSelected = onTabSelected)
        }
        }
    )
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
        Column {
            CurrentJackpotCard(
                latestDraw = latestDraw,
                isRemoteLoading = isRemoteLoading,
                remoteError = remoteError,
                onScanClick = onScanClick,
            )
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            HomeDrawStatusStrip(isRemoteLoading = isRemoteLoading)
            Spacer(modifier = Modifier.height(Dimens.spacing24))
            Text(
                text = stringResource(R.string.home_quick_actions),
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton("Scan", Icons.Filled.CenterFocusWeak) { onQuickActionClick("scan") }
                QuickActionButton("Tickets", Icons.Filled.ConfirmationNumber) { onQuickActionClick("tickets") }
                QuickActionButton("Results", Icons.Filled.EmojiEvents) { onQuickActionClick("results") }
                QuickActionButton("Help", Icons.AutoMirrored.Filled.Help) { onQuickActionClick("help") }
            }
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
                    Spacer(modifier = Modifier.width(Dimens.spacing8))
                    tickets
                        .sortedByDescending { it.effectivePlayedAtMillis() }
                        .take(5)
                        .forEach { session ->
                            val preview = ticketSummary.previews.find { it.sessionId == session.id }
                            if (preview != null) {
                                ActiveTicketCard(
                                    model = ActiveTicketCardModel(
                                        ticketLabel = sessionDisplayLabel(session),
                                        drawDate = formatHomeTicketDate(session.effectivePlayedAtMillis()),
                                        isInLiveRoom = preview.isInLiveRoom,
                                        calledCountLabel = preview.calledCountLabel,
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
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.home_eco_news),
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            EcoNewsItem(
                title = stringResource(R.string.home_news_1),
                meta = stringResource(R.string.home_news_1_meta)
            )
            Spacer(modifier = Modifier.height(16.dp))
            EcoNewsItem(
                title = stringResource(R.string.home_news_2),
                meta = stringResource(R.string.home_news_2_meta)
            )
        }
}

@Composable
private fun HomeDrawStatusStrip(isRemoteLoading: Boolean) {
    val statusText = rememberDrawStatusText()
    val pulseTransition = rememberInfiniteTransition(label = "drawStatusPulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drawStatusDotAlpha",
    )
    AppSectionSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusLarge),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing14, vertical = Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing10),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha)),
            )
            Text(
                text = "Upcoming",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (isRemoteLoading) "Loading draw schedule…" else statusText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HomeActiveTicketsSummaryRow(summary: HomeActiveTicketsSummary) {
    AppSectionSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusLarge),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing14),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${summary.activeCount} active ${if (summary.activeCount == 1) "ticket" else "tickets"}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                summary.calledNumbersLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (summary.activeCount == 0) {
                Text(
                    text = "Scan a ticket to start tracking progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class HomeTicketPreview(
    val sessionId: String,
    val isInLiveRoom: Boolean,
    val calledCountLabel: String?,
    val calledProgress: Float,
    val showCalledProgress: Boolean,
    val neutralGrid: Boolean,
    val cellStates: List<ActiveTicketCellState>,
)

private data class HomeActiveTicketsSummary(
    val activeCount: Int,
    val calledNumbersLabel: String?,
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
            calledCountLabel = if (isInLiveRoom && activeCalledCount > 0) {
                "$activeCalledCount called"
            } else {
                null
            },
            calledProgress = activeCalledCount / 75f,
            showCalledProgress = isInLiveRoom,
            neutralGrid = !isInLiveRoom,
            cellStates = cellStates,
        )
    }
    val calledNumbersLabel = if (activeLiveRoomId != null && activeCalledCount > 0) {
        "$activeCalledCount numbers called"
    } else {
        null
    }
    return HomeActiveTicketsSummary(
        activeCount = tickets.size,
        calledNumbersLabel = calledNumbersLabel,
        previews = previews,
    )
}

private fun timeAwareGreeting(name: String): String {
    val hour = ZonedDateTime.now(berlinZone).hour
    val salutation = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    return "$salutation, $name"
}

@Composable
private fun rememberDrawStatusText(): String {
    var status by remember { mutableStateOf(formatDrawStatusText()) }
    LaunchedEffect(Unit) {
        while (true) {
            status = formatDrawStatusText()
            delay(60_000L)
        }
    }
    return status
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

private fun formatDrawStatusText(from: ZonedDateTime = ZonedDateTime.now(berlinZone)): String {
    val nextDraw = nextSundayDrawBerlin(from)
    if (from.toLocalDate() == nextDraw.toLocalDate()) {
        return "Draw today at 17:00"
    }
    val remaining = Duration.between(from, nextDraw)
    val days = remaining.toDays()
    val hours = remaining.toHours() % 24
    return when {
        days > 0 -> "Next draw in ${days}d ${hours}h"
        hours > 0 -> "Next draw in ${hours}h ${remaining.toMinutes() % 60}m"
        else -> "Next draw in ${remaining.toMinutes()}m"
    }
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
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
    ) {
        AppCard(
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clip(CircleShape),
            onClick = onClick,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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

@Composable
private fun EcoNewsItem(
    title: String,
    meta: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    AppSectionSurface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

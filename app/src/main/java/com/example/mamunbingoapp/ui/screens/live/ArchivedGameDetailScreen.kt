package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.ArchivedGameSession
import com.example.mamunbingoapp.data.TicketPlayLogRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.data.isArchivedCallsOnlyPlaceholderTicketId
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppInsetDivider
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchivedGameDetailScreen(
    roomId: String,
    archivedAt: Long,
    onBack: () -> Unit,
    onOpenArchivedTicket: (roomId: String, archivedAt: Long, ticketId: String) -> Unit,
) {
    var session by remember(roomId, archivedAt) { mutableStateOf<ArchivedGameSession?>(null) }
    var loading by remember(roomId, archivedAt) { mutableStateOf(true) }

    LaunchedEffect(roomId, archivedAt) {
        loading = true
        session = TicketPlayLogRepository.getArchivedSession(roomId, archivedAt)
        loading = false
    }

    val mainDateFormat = remember {
        SimpleDateFormat("EEE, d MMM yyyy • HH:mm", Locale.getDefault())
    }
    val archivedTimestampFormat = remember {
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        AppHeaderPageLayout(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.archived_game_detail_title),
                    showBack = true,
                    onBackClick = onBack,
                )
            },
            content = {
                when {
                    loading -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    session == null -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.screenHorizontalPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.archived_game_detail_not_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    else -> {
                        val resolved = session!!
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = Dimens.screenHorizontalPadding)
                                .padding(top = Dimens.spacing8, bottom = Dimens.spacing24),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spacing16),
                        ) {
                            ArchivedGameDetailHero(
                                session = resolved,
                                roomId = roomId,
                                archivedAt = archivedAt,
                                mainDateFormat = mainDateFormat,
                                archivedTimestampFormat = archivedTimestampFormat,
                            )
                            CalledHistoryPanel(
                                calledNumbers = resolved.calledNumbers,
                                applyOuterPadding = false,
                            )
                            AppSectionSurface(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Dimens.spacing8),
                                ) {
                                    AppSectionTitle(
                                        text = stringResource(R.string.archived_game_detail_tickets_title),
                                        modifier = Modifier.padding(horizontal = Dimens.spacing16),
                                    )
                                    Text(
                                        text = stringResource(R.string.archived_game_detail_tickets_read_only_hint),
                                        modifier = Modifier.padding(
                                            horizontal = Dimens.spacing16,
                                            vertical = Dimens.spacing4,
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (resolved.ticketIds.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.archived_game_detail_no_tickets),
                                            modifier = Modifier.padding(
                                                horizontal = Dimens.spacing16,
                                                vertical = Dimens.spacing12,
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    } else {
                                        resolved.ticketIds.forEachIndexed { index, ticketId ->
                                            if (index > 0) {
                                                AppInsetDivider(
                                                    startInset = Dimens.spacing16,
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                )
                                            }
                                            ArchivedGameTicketRow(
                                                index = index + 1,
                                                ticketId = ticketId,
                                                onViewClick = {
                                                    onOpenArchivedTicket(roomId, archivedAt, ticketId)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ArchivedGameDetailHero(
    session: ArchivedGameSession,
    roomId: String,
    archivedAt: Long,
    mainDateFormat: SimpleDateFormat,
    archivedTimestampFormat: SimpleDateFormat,
) {
    val archiveId = remember(roomId, archivedAt) { formatArchiveId(roomId, archivedAt) }
    val mainDateLabel = remember(session.archivedAt, mainDateFormat) {
        mainDateFormat.format(Date(session.archivedAt))
    }
    val archivedAtFormatted = remember(session.archivedAt, archivedTimestampFormat) {
        archivedTimestampFormat.format(Date(session.archivedAt))
    }
    val archivedAtLabel = stringResource(
        R.string.archived_game_detail_archived_at,
        archivedAtFormatted,
    )

    AppSectionSurface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArchivedGameDetailArchivedPill()
                Text(
                    text = stringResource(R.string.archived_game_detail_archive_id, archiveId),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = session.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = mainDateLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Primary,
            )
            Text(
                text = archivedAtLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = Dimens.spacing4),
                color = Primary.copy(alpha = 0.35f),
                thickness = Dimens.cardBorderDefault,
            )
        }
    }
}

@Composable
private fun ArchivedGameDetailArchivedPill() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.archived_game_detail_archived_pill),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
        )
    }
}

@Composable
private fun ArchivedGameTicketRow(
    index: Int,
    ticketId: String,
    onViewClick: () -> Unit,
) {
    if (ticketId.isArchivedCallsOnlyPlaceholderTicketId()) return
    val ticket by TicketRepository.observeTicket(ticketId).collectAsState(initial = null)
    val title = ticket?.sheetName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.archived_game_detail_unnamed_ticket)
    val los = ticket?.losNumber?.trim()?.takeIf { it.isNotEmpty() }
    val serial = ticket?.serialNumber?.trim()?.takeIf { it.isNotEmpty() }
    val subtitle = when {
        los != null && serial != null ->
            stringResource(R.string.archived_game_detail_ticket_meta, los, serial)
        los != null -> stringResource(R.string.archived_game_detail_ticket_los, los)
        serial != null -> stringResource(R.string.archived_game_detail_ticket_serial, serial)
        else -> null
    }
    val indexLabel = remember(index) { index.toString().padStart(2, '0') }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick)
            .semantics { role = Role.Button }
            .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        Text(
            text = indexLabel,
            modifier = Modifier.widthIn(min = Dimens.spacing32),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Primary.copy(alpha = 0.85f),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onViewClick) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = stringResource(R.string.archived_game_detail_view_ticket_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatArchiveId(roomId: String, archivedAt: Long): String {
    val roomShort = roomId.replace("-", "").takeLast(8).uppercase(Locale.getDefault())
    val timeShort = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date(archivedAt))
    return "$roomShort-$timeShort"
}

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.ArchivedGameSession
import com.example.mamunbingoapp.data.TicketPlayLogRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchivedGamesScreen(
    onBack: () -> Unit,
    onOpenSession: (ArchivedGameSession) -> Unit,
) {
    val sessions by TicketPlayLogRepository.observeArchivedSessions()
        .collectAsState(initial = emptyList())
    val archivedDateFormat = remember {
        SimpleDateFormat("EEE, d MMM yyyy • HH:mm", Locale.getDefault())
    }
    val sundayFeaturedTitle = stringResource(R.string.live_nav_sunday_featured_title)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        AppHeaderPageLayout(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.archived_games_title),
                    showBack = true,
                    onBackClick = onBack,
                )
            },
            content = {
                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.screenHorizontalPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.archived_games_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.screenHorizontalPadding)
                            .padding(bottom = Dimens.pageContentBottomPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                    ) {
                        items(sessions, key = { "${it.roomId}_${it.archivedAt}" }) { session ->
                            ArchivedGameSessionCard(
                                session = session,
                                archivedDateFormat = archivedDateFormat,
                                sundayFeaturedTitle = sundayFeaturedTitle,
                                onClick = { onOpenSession(session) },
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ArchivedGameSessionCard(
    session: ArchivedGameSession,
    archivedDateFormat: SimpleDateFormat,
    sundayFeaturedTitle: String,
    onClick: () -> Unit,
) {
    val showSundayChip = remember(session.title, sundayFeaturedTitle) {
        isSundayJackpotArchivedSession(session.title, sundayFeaturedTitle)
    }
    val archivedAtLabel = remember(session.archivedAt, archivedDateFormat) {
        archivedDateFormat.format(Date(session.archivedAt))
    }
    val callsLabel = stringResource(R.string.archived_games_stat_calls, session.calledNumberCount)
    val ticketsLabel = stringResource(R.string.archived_games_stat_tickets, session.ticketCount)
    val metaColor = MaterialTheme.colorScheme.onSurfaceVariant
    val statIconSize = Dimens.spacing16

    AppSectionSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing10),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                ) {
                    Text(
                        text = session.title,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (showSundayChip) {
                        ArchivedGameSundayJackpotChip()
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        modifier = Modifier.size(statIconSize),
                        tint = metaColor,
                    )
                    Text(
                        text = archivedAtLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = metaColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                ) {
                    ArchivedGameStatLabel(
                        icon = Icons.Default.Call,
                        text = callsLabel,
                        iconSize = statIconSize,
                        color = metaColor,
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = metaColor,
                    )
                    ArchivedGameStatLabel(
                        icon = Icons.Default.ConfirmationNumber,
                        text = ticketsLabel,
                        iconSize = statIconSize,
                        color = metaColor,
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(Dimens.spacing24),
                tint = metaColor,
            )
        }
    }
}

@Composable
private fun ArchivedGameStatLabel(
    icon: ImageVector,
    text: String,
    iconSize: Dp,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = color,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun ArchivedGameSundayJackpotChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.archived_games_sunday_jackpot_chip),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
}

private fun isSundayJackpotArchivedSession(title: String, sundayFeaturedTitle: String): Boolean {
    if (title.startsWith("Sonntag Bingo", ignoreCase = true)) return true
    if (title.contains(sundayFeaturedTitle, ignoreCase = true)) return true
    return title.contains("Sunday", ignoreCase = true) && title.contains("Bingo", ignoreCase = true)
}

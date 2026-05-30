package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.TicketPlayLog
import com.example.mamunbingoapp.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TicketPlayLogSection(
    playLogs: List<TicketPlayLog>,
    modifier: Modifier = Modifier,
) {
    if (playLogs.isEmpty()) return
    AppSectionSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing12),
        ) {
            AppSectionTitle(
                text = stringResource(R.string.ticket_play_log_title),
                uppercase = false,
                usePrimaryColor = false,
            )
            playLogs.forEachIndexed { index, log ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
                    )
                }
                TicketPlayLogEntry(log = log)
            }
        }
    }
}

@Composable
private fun TicketPlayLogEntry(log: TicketPlayLog) {
    val timestampFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.ticket_play_log_room, log.roomName),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(
                R.string.ticket_play_log_added,
                timestampFormat.format(Date(log.addedAt)),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
        )
        Text(
            text = stringResource(
                R.string.ticket_play_log_played,
                timestampFormat.format(Date(log.archivedAt)),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
        )
        Text(
            text = pluralStringResource(
                R.plurals.ticket_play_log_called_count,
                log.calledNumbers.size,
                log.calledNumbers.size,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
        )
        val resultText = when {
            log.bingoLineCount > 0 -> pluralStringResource(
                R.plurals.ticket_play_log_result_bingo,
                log.bingoLineCount,
                log.bingoLineCount,
            )
            log.markedCount > 0 -> stringResource(
                R.string.ticket_play_log_result_marked,
                log.markedCount,
            )
            else -> null
        }
        if (resultText != null) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

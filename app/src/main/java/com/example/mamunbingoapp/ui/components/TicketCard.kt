package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun TicketCard(
    ticketId: String,
    amount: String,
    drawDate: String,
    status: TicketStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val cardModifier = modifier
        .width(160.dp)
        .iosElevatedShadow(shape = shape)
        .background(MaterialTheme.colorScheme.surface, shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(Dimens.spacing16)
    Column(modifier = cardModifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                StatusChip(status = status)
                Text(
                    text = ticketId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Text(
                text = stringResource(R.string.ticket_card_draw, drawDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
    }
}

@Composable
fun TicketGridThumbnailPreview(
    matchedCells: Int,
    modifier: Modifier = Modifier
) {
    val corner = RoundedCornerShape(2.dp)
    val totalCells = 25
    val safeMatched = matchedCells.coerceIn(0, totalCells)
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                RoundedCornerShape(8.dp)
            )
            .border(
                Dimens.cardBorderDefault,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f),
                RoundedCornerShape(8.dp)
            )
            .padding(3.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        (0 until totalCells).chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { idx ->
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                if (idx < safeMatched) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                corner
                            )
                    )
                }
            }
        }
    }
}

package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens

private val TicketInfoCardBg = Color(0xFFF7F8F6)
private val TicketInfoCardBorder = Color.Black.copy(alpha = 0.05f)
private val LiveStatusPillBg = Color(0xFFEAF6EA)
private val LiveStatusPillText = Color(0xFF4A8F2A)

/** Green-tint pill aligned with live “recent” styling. */
@Composable
fun TicketInfoStatusChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
        ),
        color = LiveStatusPillText,
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusButtonPill))
            .background(LiveStatusPillBg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

data class TicketInfoItem(
    val label: String,
    val value: String,
    val trailing: (@Composable () -> Unit)? = null
)

/** Light neutral fill, soft border, spaced rows (no dividers). */
@Composable
fun TicketInfoCard(
    title: String,
    items: List<TicketInfoItem>,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.radiusMedium)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = TicketInfoCardBg,
        border = BorderStroke(1.dp, TicketInfoCardBorder),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing16)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(Dimens.spacing10))
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(Dimens.spacing12))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spacing4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (item.value.isNotEmpty()) {
                            Text(
                                text = item.value,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        item.trailing?.invoke()
                    }
                }
            }
        }
    }
}

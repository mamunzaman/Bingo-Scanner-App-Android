package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.Slate200
import com.example.mamunbingoapp.theme.Warning
import com.example.mamunbingoapp.theme.WarningBorder
import com.example.mamunbingoapp.theme.WarningContainer
import com.example.mamunbingoapp.theme.WarningIcon
import com.example.mamunbingoapp.theme.WarningSubText
import com.example.mamunbingoapp.theme.WarningText

enum class AlmostBingoAlertVariant {
    /** Alarm icon left, mini grid stacked under score on the right. */
    Default,
    /** Mini grid left, title center, orange score pill right (History Detail compact banner). */
    HistoryDetailCompact,
}

@Composable
fun AlmostBingoAlertRowV2(
    lineType: String,
    filled: Int,
    total: Int,
    markedCells: Set<Int>,
    nearCells: Set<Int> = emptySet(),
    modifier: Modifier = Modifier,
    variant: AlmostBingoAlertVariant = AlmostBingoAlertVariant.Default,
) {
    val shell = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(Dimens.radiusCard))
        .background(WarningContainer)
        .border(Dimens.cardBorderDefault, WarningBorder, RoundedCornerShape(Dimens.radiusCard))
    val subtitle = "$lineType · need ${total - filled} more number${if (total - filled == 1) "" else "s"}"
    when (variant) {
        AlmostBingoAlertVariant.HistoryDetailCompact -> {
            Row(
                modifier = modifier
                    .then(shell)
                    .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniBingoPreview(
                    markedCells = markedCells,
                    nearCells = nearCells,
                    modifier = Modifier.size(44.dp),
                    cellRadius = 2.dp,
                    gap = 2.dp,
                )
                Spacer(modifier = Modifier.width(Dimens.spacing10))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Almost Bingo!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarningText
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningSubText
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spacing8))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimens.radiusPill))
                        .background(Warning)
                        .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing4),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$filled/$total",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarningText
                    )
                }
            }
        }
        AlmostBingoAlertVariant.Default -> {
            Row(
                modifier = modifier
                    .then(shell)
                    .padding(horizontal = Dimens.spacing14, vertical = Dimens.spacing10),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.iconAlertBox)
                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                        .background(WarningIcon),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = WarningText,
                        modifier = Modifier.size(Dimens.iconAlert)
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spacing12))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
                ) {
                    Text(
                        text = "Almost Bingo!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarningText
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningSubText
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spacing12))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.radiusPill))
                            .background(Warning)
                            .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing4),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$filled/$total",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarningText
                        )
                    }
                    MiniBingoPreview(
                        markedCells = markedCells,
                        nearCells = nearCells,
                        modifier = Modifier.size(Dimens.miniBingoPreviewSize),
                        cellRadius = Dimens.radiusXSmall,
                        gap = Dimens.spacing4
                    )
                }
            }
        }
    }
}

@Composable
fun MiniBingoPreview(
    markedCells: Set<Int>,
    nearCells: Set<Int> = emptySet(),
    modifier: Modifier = Modifier,
    cellRadius: Dp = Dimens.radiusXSmall,
    gap: Dp = Dimens.spacing4
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(gap),
        userScrollEnabled = false
    ) {
        items((0 until 25).toList()) { index ->
            val isFree = index == 12
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(cellRadius))
                    .background(
                        when {
                            isFree -> PrimaryContainer
                            index in nearCells -> WarningIcon
                            index in markedCells -> Primary
                            else -> Slate200
                        }
                    )
            )
        }
    }
}

package com.example.mamunbingoapp.ui.screens.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun HistorySheetCard(
    title: String,
    isActive: Boolean,
    calledCount: Int,
    totalCount: Int = 75,
    markedCount: Int = 0,
    markedCells: List<Boolean>? = null,
    onViewClick: () -> Unit,
    onJoinClick: () -> Unit,
    onDelete: () -> Unit,
    onLeaveRoom: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val safeTotal = totalCount.coerceAtLeast(1)
    val safeCalled = calledCount.coerceIn(0, safeTotal)
    val previewCells = when {
        markedCells != null && markedCells.size == 25 -> markedCells
        else -> List(25) { it < markedCount.coerceIn(0, 25) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusCard),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            Dimens.cardBorderDefault,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing5)
        ) {
            HistorySheetMiniPreview(cells = previewCells)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing5)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Text(
                        text = if (isActive) "ACTIVE" else "SAVED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = "$safeCalled/$safeTotal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing5)
            ) {
                Text(
                    text = "View",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.clickable(onClick = onViewClick)
                )
                Button(
                    onClick = onJoinClick,
                    shape = RoundedCornerShape(Dimens.radiusSearchField),
                    contentPadding = PaddingValues(horizontal = Dimens.spacing5, vertical = Dimens.spacing4),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = "Join",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f),
                        modifier = Modifier.size(Dimens.iconCompact)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySheetMiniPreview(cells: List<Boolean>) {
    val list = if (cells.size >= 25) cells.take(25) else cells + List(25 - cells.size) { false }
    val outerShape = RoundedCornerShape(Dimens.radiusMedium)
    val cellShape = RoundedCornerShape(Dimens.radiusXSmall)

    Box(
        modifier = Modifier
            .width(46.dp)
            .size(46.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, outerShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            list.chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEach { marked ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (marked) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                                    cellShape
                                )
                        )
                    }
                }
            }
        }
    }
}




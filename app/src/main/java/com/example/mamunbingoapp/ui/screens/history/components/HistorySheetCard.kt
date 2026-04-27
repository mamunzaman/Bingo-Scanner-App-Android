package com.example.mamunbingoapp.ui.screens.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.MiniBingoGrid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorySheetCard(
    title: String,
    playedAtMillis: Long,
    dateLabelPrefix: String,
    serialNumber: String?,
    losNumber: String?,
    markedCount: Int,
    markedCells: List<Boolean>?,
    onViewClick: () -> Unit,
    onJoinClick: () -> Unit,
    onDelete: () -> Unit,
    onLeaveRoom: (() -> Unit)? = null,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onSelectionToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateStr = remember(playedAtMillis) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(playedAtMillis))
    }
    val subtitle = "$dateLabelPrefix: $dateStr"
    val safeSerial = serialNumber?.takeIf { it.isNotBlank() } ?: "--"
    val safeLos = losNumber?.takeIf { it.isNotBlank() } ?: "--"
    val markedNumerator = markedCount.coerceIn(0, 25)
    val marked = "$markedNumerator/25"
    val miniGrid = when {
        markedCells != null && markedCells.size == 25 -> markedCells
        else -> List(25) { it < markedNumerator }
    }
    val rowShape = RoundedCornerShape(Dimens.radiusCard)
    var menuExpanded by remember { mutableStateOf(false) }
    val borderWidth = when {
        selectionMode && selected -> 2.dp
        else -> Dimens.cardBorderDefault
    }
    val borderColor = when {
        selectionMode && selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha)
    }
    val rowClick = {
        if (selectionMode) onSelectionToggle() else onViewClick()
    }

    val headerClick = Modifier.clickable(
        indication = rememberRipple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = rowClick
    )
    val footerClick = Modifier.clickable(
        indication = rememberRipple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = rowClick
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = if (selectionMode) {
                    "$title, ${if (selected) "selected" else "not selected"}. Double tap to toggle."
                } else {
                    "$title, Marked $marked. Double tap to open."
                }
            }
            .clip(rowShape)
            .border(width = borderWidth, color = borderColor, shape = rowShape)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onSelectionToggle() },
                    modifier = Modifier.padding(start = Dimens.spacing4),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing4)
                    .then(headerClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12)
            ) {
                MiniBingoGrid(cells = miniGrid)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!selectionMode) {
                Box(
                    modifier = Modifier.padding(end = Dimens.spacing4)
                ) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions",
                            modifier = Modifier.size(Dimens.iconCompact),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Join") },
                            onClick = {
                                menuExpanded = false
                                onJoinClick()
                            }
                        )
                        if (onLeaveRoom != null) {
                            DropdownMenuItem(
                                text = { Text("Leave") },
                                onClick = {
                                    menuExpanded = false
                                    onLeaveRoom()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = Dimens.outlineDividerAlpha))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = Dimens.spacing12, vertical = 3.dp)
                .then(footerClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistorySheetInfoCell(
                label = "SERIAL",
                value = safeSerial,
                modifier = Modifier.weight(1f)
            )
            HistorySheetVerticalDivider()
            HistorySheetInfoCell(
                label = "LOS",
                value = safeLos,
                modifier = Modifier.weight(1f)
            )
            HistorySheetVerticalDivider()
            HistorySheetInfoCell(
                label = "MARKED",
                value = marked,
                modifier = Modifier.weight(1f),
                valueColor = MaterialTheme.colorScheme.primary,
                valueFontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HistorySheetInfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueFontWeight: FontWeight = FontWeight.Bold
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = valueFontWeight,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistorySheetVerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = Dimens.outlineDividerAlpha))
    )
}


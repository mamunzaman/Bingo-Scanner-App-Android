package com.example.mamunbingoapp.ui.screens.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.core.BingoPlayableNumbers
import com.example.mamunbingoapp.theme.AppAlpha
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryPressed
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCellState
import com.example.mamunbingoapp.ui.components.home.ActiveTicketCompactSheetPreview
import com.example.mamunbingoapp.ui.components.home.ActiveTicketListSheetPreview
import com.example.mamunbingoapp.ui.components.home.ActiveTicketLosSerieRow
import com.example.mamunbingoapp.viewmodel.HistoryMiniGridCell
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorySheetCard(
    title: String,
    playedAtMillis: Long,
    serialNumber: String?,
    losNumber: String?,
    miniGridCells: List<HistoryMiniGridCell>,
    markedCount: Int,
    onViewClick: () -> Unit,
    onJoinClick: () -> Unit,
    onDelete: () -> Unit,
    onLeaveRoom: (() -> Unit)? = null,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    inRoom: Boolean = false,
    roomName: String? = null,
    onSelectionToggle: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val dateTimeStr = remember(playedAtMillis) {
        SimpleDateFormat("d MMM yyyy • HH:mm", Locale.getDefault()).format(Date(playedAtMillis))
    }
    val markedNumerator = BingoPlayableNumbers.coercePlayableMarkedCount(markedCount)
    val selectedA11y = stringResource(R.string.live_play_a11y_selected)
    val notSelectedA11y = stringResource(R.string.live_play_a11y_not_selected)
    val selectionA11y = stringResource(
        R.string.live_play_a11y_sheet_select,
        title,
        if (selected) selectedA11y else notSelectedA11y,
    )
    val openA11y = stringResource(R.string.live_play_a11y_sheet_open, title, markedNumerator)
    val gridCellStates = remember(miniGridCells) {
        miniGridCells.take(BingoPlayableNumbers.GRID_CELL_COUNT).map {
            ActiveTicketCellState(display = it.display, isCalled = it.isCalled)
        }
    }
    val rowShape = RoundedCornerShape(Dimens.radiusLarge)
    val leadingSlotWidth = 42.dp
    var menuExpanded by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val borderColor = when {
        selectionMode && selected -> cs.primary.copy(alpha = 0.55f)
        inRoom -> cs.primary.copy(alpha = AppAlpha.AlphaBorderStrong)
        else -> cs.outlineVariant.copy(alpha = AppAlpha.AlphaBorder)
    }
    val selectedTintBg: Modifier = if (selectionMode && selected) {
        Modifier.background(cs.primaryContainer.copy(alpha = 0.10f))
    } else {
        Modifier
    }
    val rowClick = {
        if (selectionMode) onSelectionToggle() else onViewClick()
    }
    val contentClick = Modifier.clickable(
        indication = rememberRipple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = rowClick,
    )

    AppSectionSurface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = if (selectionMode) selectionA11y else openA11y
            }
            .clip(rowShape),
        shape = rowShape,
        borderColor = borderColor,
        shadowElevation = if (inRoom) 5.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surface)
                .then(selectedTintBg),
            verticalAlignment = Alignment.Top,
        ) {
            if (selectionMode) {
                Box(
                    modifier = Modifier
                        .width(leadingSlotWidth)
                        .padding(top = Dimens.spacing12),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = { onSelectionToggle() },
                        colors = CheckboxDefaults.colors(checkedColor = cs.primary),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (selectionMode) Dimens.spacing4 else Dimens.spacing12,
                        end = if (selectionMode) Dimens.spacing12 else Dimens.spacing4,
                        top = Dimens.spacing12,
                        bottom = Dimens.spacing12,
                    )
                    .then(contentClick),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
                verticalAlignment = Alignment.Top,
            ) {
                ActiveTicketCompactSheetPreview(
                    cellStates = gridCellStates,
                    neutralGrid = !inRoom,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .heightIn(min = ActiveTicketListSheetPreview.Height),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                        ),
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ActiveTicketLosSerieRow(
                        losNumber = losNumber,
                        serieNumber = serialNumber,
                        valueStyle = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    if (inRoom) {
                        HistorySheetRoomRow(
                            roomLabel = roomName?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.history_in_room_pill),
                        )
                    }
                    HistorySheetMetaIconRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconCompact),
                                tint = Primary.copy(alpha = 0.62f),
                            )
                        },
                        text = dateTimeStr,
                        muted = true,
                    )
                    if (selectionMode && inRoom) {
                        Text(
                            text = stringResource(R.string.history_already_in_room_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant.copy(alpha = 0.55f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (!selectionMode) {
                Box(modifier = Modifier.padding(top = Dimens.spacing4, end = Dimens.spacing4)) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.history_more_actions_cd),
                            modifier = Modifier.size(Dimens.iconCompact),
                            tint = cs.onSurfaceVariant.copy(alpha = 0.9f),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.history_join)) },
                            onClick = {
                                menuExpanded = false
                                onJoinClick()
                            },
                        )
                        if (onLeaveRoom != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.history_leave)) },
                                onClick = {
                                    menuExpanded = false
                                    onLeaveRoom()
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.common_delete)) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySheetRoomRow(
    roomLabel: String,
    modifier: Modifier = Modifier,
) {
    val chipShape = RoundedCornerShape(Dimens.radiusPill)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(chipShape)
            .background(IconContainerBg, chipShape)
            .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
    ) {
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconCompact),
            tint = Primary,
        )
        Text(
            text = roomLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 16.sp,
            ),
            color = PrimaryPressed,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun HistorySheetMetaIconRow(
    icon: @Composable () -> Unit,
    text: String,
    muted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
    ) {
        icon()
        Text(
            text = text,
            style = if (muted) {
                MaterialTheme.typography.labelSmall
            } else {
                MaterialTheme.typography.labelMedium
            },
            color = cs.onSurfaceVariant.copy(alpha = if (muted) 0.55f else 0.92f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

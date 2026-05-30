package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.core.AlmostBingoInfo
import com.example.mamunbingoapp.core.MAX_CALLED_NUMBERS
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.PrimaryPressed
import com.example.mamunbingoapp.theme.Secondary
import com.example.mamunbingoapp.ui.model.RoomStatus
import com.example.mamunbingoapp.ui.model.SheetStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoomSessionCard(
    title: String,
    statusText: String,
    statusDotColor: Color,
    ticketsCount: Int,
    calledCount: Int,
    totalCount: Int = MAX_CALLED_NUMBERS,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
    roomStatus: RoomStatus? = null,
    sheetStatus: SheetStatus? = null,
    addedToRoomName: String? = null,
    addedAtMillis: Long? = null,
    markedCount: Int = 0,
    markedCells: List<Boolean>? = null,
    almostBingo: AlmostBingoInfo? = null,
    bingoWinLineCount: Int? = null,
    actionIcon: ImageVector? = null,
    onViewClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onLeaveRoomClick: (() -> Unit)? = null,
    ocrSource: String? = null,
    editedAfterOcr: Boolean = false,
    ocrCorrectionCount: Int = 0,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionToggle: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    val dateUnavailable = stringResource(R.string.room_session_date_unavailable)
    val notInRoomLabel = stringResource(R.string.room_session_not_in_room)

    val dateLabel = remember(addedAtMillis, dateUnavailable) {
        addedAtMillis?.let { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it)) }
            ?: dateUnavailable
    }
    val safeTotal = totalCount.coerceAtLeast(1)
    val safeCalled = calledCount.coerceIn(0, safeTotal)
    val progressValue = (safeCalled.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
    val roomLabel = addedToRoomName?.takeIf { it.isNotBlank() } ?: notInRoomLabel
    val isActive = sheetStatus == SheetStatus.ACTIVE || statusText.equals("Active", ignoreCase = true)
    val isJoinAction = actionText.equals("Join", ignoreCase = true)

    val summary = "$title, $safeCalled of $safeTotal called."
    val cardOnClick: (() -> Unit)? = when {
        selectionMode && onSelectionToggle != null -> onSelectionToggle
        onCardClick != null -> onCardClick
        else -> null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = summary },
        onClick = cardOnClick ?: {},
        enabled = cardOnClick != null,
        shape = RoundedCornerShape(Dimens.radiusCard),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.cardBorderDefault, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                    ) {
                        if (selectionMode && onSelectionToggle != null) {
                            Checkbox(checked = isSelected, onCheckedChange = { onSelectionToggle() })
                        }
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isActive) {
                            Text(
                                text = stringResource(R.string.bingo_session_active),
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryPressed,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .background(IconContainerBg, RoundedCornerShape(100.dp))
                                    .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
                            )
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                    ) {
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimens.spacing5),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                        Text(
                            text = "$safeCalled/$safeTotal",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Text(
                            text = "? $roomLabel",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                if (!selectionMode) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                    ) {
                        OutlinedButton(
                            onClick = onViewClick ?: onCardClick ?: onActionClick,
                            contentPadding = PaddingValues(horizontal = Dimens.spacing12, vertical = Dimens.spacing8),
                            border = BorderStroke(Dimens.cardBorderDefault, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(stringResource(R.string.common_view), maxLines = 1)
                        }
                        Button(
                            onClick = if (isJoinAction) onActionClick else ({ /* no-op */ }), 
                            enabled = isJoinAction,
                            contentPadding = PaddingValues(horizontal = Dimens.spacing12, vertical = Dimens.spacing8),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconCompact)
                            )
                            Spacer(modifier = Modifier.size(Dimens.spacing4))
                            Text(stringResource(R.string.bingo_session_join), maxLines = 1)
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!selectionMode && onLeaveRoomClick != null) {
                    TextButton(onClick = { showLeaveConfirm = true }) {
                        Text(stringResource(R.string.room_session_leave_button))
                    }
                }
                if (!selectionMode && onDeleteClick != null) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.room_session_delete_cd),
                            tint = Secondary
                        )
                    }
                }
            }
        }
    }

    AppConfirmDialog(
        visible = showDeleteConfirm,
        title = stringResource(R.string.history_detail_delete_title),
        message = stringResource(R.string.history_detail_delete_message),
        confirmText = stringResource(R.string.common_delete),
        cancelText = stringResource(R.string.common_cancel),
        showCancelButton = true,
        onConfirm = {
            onDeleteClick?.invoke()
            showDeleteConfirm = false
        },
        onCancel = { showDeleteConfirm = false },
        onDismiss = { showDeleteConfirm = false }
    )

    AppConfirmDialog(
        visible = showLeaveConfirm,
        title = stringResource(R.string.room_session_leave_title),
        message = stringResource(R.string.room_session_leave_message),
        confirmText = stringResource(R.string.common_leave),
        cancelText = stringResource(R.string.common_cancel),
        showCancelButton = true,
        onConfirm = {
            onLeaveRoomClick?.invoke()
            showLeaveConfirm = false
        },
        onCancel = { showLeaveConfirm = false },
        onDismiss = { showLeaveConfirm = false }
    )
}

package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun BulkSelectionActionBar(
    modifier: Modifier = Modifier,
    showJoinLive: Boolean = false,
    joinLiveEnabled: Boolean = false,
    joinLiveCount: Int = 0,
    onJoinLiveClick: () -> Unit = {},
    showAddToRoom: Boolean = false,
    addToRoomEnabled: Boolean = false,
    addCount: Int = 0,
    onAddToRoomClick: () -> Unit = {},
    inRoomInfoText: String? = null,
    showRemoveFromRoom: Boolean,
    removeFromRoomEnabled: Boolean,
    removeCount: Int,
    deleteEnabled: Boolean,
    deleteCount: Int,
    onRemoveFromRoomClick: () -> Unit,
    onDeleteFromHistoryClick: () -> Unit
) {
    val dockShape = RoundedCornerShape(
        topStart = Dimens.radiusCard,
        topEnd = Dimens.radiusCard
    )
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dimens.cardBorderDefault,
                color = cs.outlineVariant.copy(alpha = Dimens.outlineDividerAlpha),
                shape = dockShape
            ),
        shape = dockShape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = cs.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.spacing16,
                    end = Dimens.spacing16,
                    top = Dimens.spacing12,
                    bottom = Dimens.spacing16
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing10)
        ) {
            if (showJoinLive) {
                FilledTonalButton(
                    onClick = onJoinLiveClick,
                    enabled = joinLiveEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.radiusMedium),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = cs.primaryContainer,
                        contentColor = cs.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = if (joinLiveCount > 0) "Join live ($joinLiveCount)"
                        else "Join live"
                    )
                }
            }
            if (!inRoomInfoText.isNullOrBlank()) {
                Text(
                    text = inRoomInfoText,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (showAddToRoom) {
                FilledTonalButton(
                    onClick = onAddToRoomClick,
                    enabled = addToRoomEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.radiusMedium)
                ) {
                    Text(
                        text = if (addCount > 0) "Add eligible ($addCount)"
                        else "Add eligible"
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showRemoveFromRoom) {
                    OutlinedButton(
                        onClick = onRemoveFromRoomClick,
                        enabled = removeFromRoomEnabled,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.radiusMedium),
                        colors = ButtonDefaults.outlinedButtonColors(
                            disabledContainerColor = cs.surfaceContainerHighest,
                            disabledContentColor = cs.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = if (removeCount > 0) "Remove from room ($removeCount)"
                            else "Remove from room"
                        )
                    }
                }
                Button(
                    onClick = onDeleteFromHistoryClick,
                    enabled = deleteEnabled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Dimens.radiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.error,
                        contentColor = cs.onError,
                        disabledContainerColor = cs.surfaceContainerHighest,
                        disabledContentColor = cs.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Text(
                        text = if (deleteCount > 0) "Delete ($deleteCount)"
                        else "Delete"
                    )
                }
            }
        }
    }
}

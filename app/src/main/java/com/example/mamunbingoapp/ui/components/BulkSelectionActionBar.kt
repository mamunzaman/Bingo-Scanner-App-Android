package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

/**
 * Bottom docked actions for bulk selection in History / My Tickets.
 * Optional: [showAddToRoom] add unassigned tickets to the current live room (tonal).
 * [showRemoveFromRoom] / [onRemoveFromRoomClick]: leave room only.
 * Delete: permanent history removal.
 */
@Composable
fun BulkSelectionActionBar(
    modifier: Modifier = Modifier,
    showAddToRoom: Boolean = false,
    addToRoomEnabled: Boolean = false,
    addCount: Int = 0,
    onAddToRoomClick: () -> Unit = {},
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
                color = cs.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha),
                shape = dockShape
            ),
        shape = dockShape,
        tonalElevation = Dimens.cardElevationSubtle,
        shadowElevation = Dimens.cardElevationSubtle,
        color = cs.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = Dimens.screenHorizontalPadding,
                vertical = Dimens.spacing12
            )
        ) {
            if (showAddToRoom) {
                FilledTonalButton(
                    onClick = onAddToRoomClick,
                    enabled = addToRoomEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.spacing12)
                ) {
                    Text(
                        text = if (addCount > 0) "Add to room ($addCount)"
                        else "Add to room"
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing8))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (showAddToRoom) 0.dp else Dimens.spacing12),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showRemoveFromRoom) {
                    OutlinedButton(
                        onClick = onRemoveFromRoomClick,
                        enabled = removeFromRoomEnabled,
                        modifier = Modifier.weight(1f)
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
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

package com.example.mamunbingoapp.ui.components

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.example.mamunbingoapp.theme.Dimens

data class RoomConflictUi(
    val visible: Boolean = false,
    val existingRoomId: String? = null,
    val existingRoomName: String? = null,
    val targetRoomId: String? = null
)

@Composable
fun RoomConflictDialog(
    visible: Boolean,
    existingRoomName: String,
    hasTargetRoom: Boolean,
    onCancel: () -> Unit,
    onOpenExistingRoom: () -> Unit,
    onMoveToTargetRoom: () -> Unit,
    moveButtonEnabled: Boolean = true
) {
    if (!visible) return
    BasicAlertDialog(
        onDismissRequest = onCancel,
        content = {
            Column {
                Text(
                    "Ticket already in another room",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                Text(
                    "This ticket is already assigned to $existingRoomName.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    TextButton(onClick = onOpenExistingRoom) { Text("Go to existing room") }
                    if (hasTargetRoom) {
                        TextButton(
                            onClick = onMoveToTargetRoom,
                            enabled = moveButtonEnabled
                        ) {
                            Text("Move to selected room")
                        }
                    }
                }
            }
        }
    )
}

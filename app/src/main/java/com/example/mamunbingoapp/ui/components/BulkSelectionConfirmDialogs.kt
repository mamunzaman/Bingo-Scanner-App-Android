package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun LeaveRoomBulkConfirmDialog(
    visible: Boolean,
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Dimens.radiusXL),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                "Remove from room?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                "Remove $count item(s) from the live room. They stay in History and can be added again later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Remove")
            }
        }
    )
}

@Composable
fun DeleteFromHistoryBulkConfirmDialog(
    visible: Boolean,
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Dimens.radiusXL),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                "Delete from history?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                "Permanently delete $count ticket(s) from this device. This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    )
}

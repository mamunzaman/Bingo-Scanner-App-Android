package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.model.RoomStatus
import com.example.mamunbingoapp.ui.model.SheetStatus

@Composable
fun StatusPill(status: SheetStatus, modifier: Modifier = Modifier) {
    if (status == SheetStatus.IDLE) return
    val (bg, textColor) = when (status) {
        SheetStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        SheetStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        SheetStatus.IN_PROGRESS -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        SheetStatus.IDLE -> return
    }
    val label = when (status) {
        SheetStatus.ACTIVE -> "Active"
        SheetStatus.COMPLETED -> "Completed"
        SheetStatus.IN_PROGRESS -> "In Progress"
        SheetStatus.IDLE -> ""
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(bg)
            .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun RoomStatusPill(status: RoomStatus, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (status) {
        RoomStatus.RUNNING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        RoomStatus.IDLE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        RoomStatus.FINISHED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        RoomStatus.RUNNING -> "Running"
        RoomStatus.IDLE -> "Round complete"
        RoomStatus.FINISHED -> "Finished"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(bg)
            .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

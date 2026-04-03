package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Error
import com.example.mamunbingoapp.theme.Success
import com.example.mamunbingoapp.theme.WarningDark

enum class TicketStatus {
    Pending,
    Won,
    Lost,
    Confirmed
}

@Composable
fun StatusChip(
    status: TicketStatus,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, labelColor) = when (status) {
        TicketStatus.Pending -> Triple("Pending", WarningDark.copy(alpha = 0.2f), WarningDark)
        TicketStatus.Won -> Triple("Won", MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary)
        TicketStatus.Lost -> Triple("Lost", Error.copy(alpha = 0.25f), Error)
        TicketStatus.Confirmed -> Triple("Confirmed", Success.copy(alpha = 0.2f), Success)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(containerColor)
            .padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = labelColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    com.example.mamunbingoapp.theme.MamunBingoTheme {
        StatusChip(status = TicketStatus.Won)
    }
}

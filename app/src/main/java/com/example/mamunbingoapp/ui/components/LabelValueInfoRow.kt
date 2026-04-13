package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

/**
 * Shared label / value row with hairline divider — e.g. Ticket Information blocks.
 * [LabelValueInfoRowVariant.Compact] tightens vertical padding for dense layouts.
 */
enum class LabelValueInfoRowVariant {
    Default,
    Compact,
}

@Composable
fun LabelValueInfoRow(
    label: String,
    modifier: Modifier = Modifier,
    variant: LabelValueInfoRowVariant = LabelValueInfoRowVariant.Default,
    showDivider: Boolean = true,
    content: @Composable () -> Unit,
) {
    val verticalPad = when (variant) {
        LabelValueInfoRowVariant.Default -> Dimens.spacing5
        LabelValueInfoRowVariant.Compact -> Dimens.spacing4
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPad, horizontal = Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                content()
            }
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
            )
        }
    }
}

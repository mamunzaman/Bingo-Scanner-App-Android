package com.example.mamunbingoapp.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

/**
 * Full-width divider with optional leading/trailing inset (e.g. align with row text past an icon tile).
 */
@Composable
fun AppInsetDivider(
    modifier: Modifier = Modifier,
    startInset: Dp = 0.dp,
    endInset: Dp = 0.dp,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
) {
    HorizontalDivider(
        modifier = modifier.padding(start = startInset, end = endInset),
        thickness = thickness,
        color = color,
    )
}

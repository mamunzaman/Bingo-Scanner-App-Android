package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

/**
 * Surface wrapper for a bingo grid: same radius, border, and padding as History Detail bingo sheet card.
 *
 * [premiumLayered]: soft tonal layering (no shadow); inner nested plate slightly off-surface; same insets as flat.
 */
@Composable
fun BingoSheetSection(
    modifier: Modifier = Modifier,
    premiumLayered: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val innerShape = RoundedCornerShape(Dimens.radiusSmall)
    val outerBorder = BorderStroke(
        Dimens.cardBorderDefault,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f),
    )
    val innerBorder = BorderStroke(
        0.5.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f),
    )
    if (premiumLayered) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            border = outerBorder,
        ) {
            Box(Modifier.padding(Dimens.spacing16)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = innerShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = innerBorder,
                ) {
                    content()
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    Dimens.cardBorderDefault,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape
                )
                .padding(Dimens.spacing16)
        ) {
            content()
        }
    }
}

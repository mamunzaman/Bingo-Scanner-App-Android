package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

/**
 * Leading icon tile for list rows and settings-style rows.
 * Part of the shared UI kit with [AppListRow], [AppInsetDivider], [AppSectionSurface].
 */
@Composable
fun AppIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = Dimens.iconDefault,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(Dimens.radiusSmall),
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize),
        )
    }
}

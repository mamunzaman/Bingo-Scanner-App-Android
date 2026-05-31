package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

/** Default alpha for the primary-tinted section border ([AppSectionSurface], [appPremiumCardBorder]). */
const val APP_SECTION_BORDER_ALPHA = 0.14f

/**
 * Premium section shell: [surface] fill, primary-tinted border ([APP_SECTION_BORDER_ALPHA]), flat shadow.
 * UI kit peers: [AppCard] (standalone M3 card), [AppListRow], [AppIconTile], [AppInsetDivider],
 * [AppSectionTitle] (screen section headings), [AppFieldLabel], [AppBottomSheetSurface].
 *
 * For Row/Column shells that cannot use [Surface], use [appPremiumCardBorder] with the same [shape].
 */
@Composable
fun AppSectionSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.radiusLarge),
    color: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = APP_SECTION_BORDER_ALPHA),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = BorderStroke(Dimens.cardBorderDefault, borderColor),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/** Border stroke matching [AppSectionSurface] defaults (non-[Surface] shells, e.g. Profile stats row). */
fun Modifier.appPremiumCardBorder(shape: Shape): Modifier = composed {
    border(
        width = Dimens.cardBorderDefault,
        color = MaterialTheme.colorScheme.primary.copy(alpha = APP_SECTION_BORDER_ALPHA),
        shape = shape,
    )
}

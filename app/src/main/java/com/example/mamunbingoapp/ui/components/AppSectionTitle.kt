package com.example.mamunbingoapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.example.mamunbingoapp.theme.AppTextStyles
import java.util.Locale

/**
 * Uppercase section label using [AppTextStyles.sectionLabel].
 * [usePrimaryColor] `true` matches Settings grouped headers (primary); `false` uses onSurfaceVariant.
 */
@Composable
fun AppSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    usePrimaryColor: Boolean = true,
    uppercase: Boolean = true,
    color: Color? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val resolvedColor = color ?: if (usePrimaryColor) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = if (uppercase) text.uppercase(Locale.getDefault()) else text
    Text(
        text = label,
        style = AppTextStyles.sectionLabel,
        color = resolvedColor,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier,
    )
}

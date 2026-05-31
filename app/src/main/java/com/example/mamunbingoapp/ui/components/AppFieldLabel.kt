package com.example.mamunbingoapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.example.mamunbingoapp.theme.AppTextStyles
import java.util.Locale

/** Compact label for fields/meta rows — not a screen section heading (see [AppSectionTitle]). */
@Composable
fun AppFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    uppercase: Boolean = false,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val label = if (uppercase) text.uppercase(Locale.getDefault()) else text
    Text(
        text = label,
        style = AppTextStyles.sectionLabel,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier,
    )
}

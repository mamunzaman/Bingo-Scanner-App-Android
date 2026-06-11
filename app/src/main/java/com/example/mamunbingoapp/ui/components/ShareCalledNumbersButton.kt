package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens

/** Circular share action for called-number board export (live room header + sheet cards). */
@Composable
fun ShareCalledNumbersButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = colorScheme.primary.copy(alpha = 0.10f)
    val borderC = colorScheme.primary.copy(alpha = 0.20f)
    val shareContentDescription = stringResource(R.string.live_play_share_called_numbers_cd)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(36.dp)
            .semantics { contentDescription = shareContentDescription },
        shape = CircleShape,
        color = bg,
        border = BorderStroke(Dimens.cardBorderDefault, borderC),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconCompact),
                tint = if (enabled) {
                    colorScheme.primary.copy(alpha = 0.82f)
                } else {
                    colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
            )
        }
    }
}

package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.appPremiumCardBorder

private val comingSoonInactiveAlpha = 0.55f

@Composable
fun ProfileComingSoonBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.profile_coming_soon),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusSmall))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
            .padding(horizontal = Dimens.spacing8, vertical = 4.dp),
    )
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    comingSoon: Boolean = false,
    cardShape: Shape = RoundedCornerShape(12.dp),
    showChevron: Boolean = true,
    usePremiumCardBorder: Boolean = true,
) {
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = if (comingSoon) mutedColor else MaterialTheme.colorScheme.onSurface
    val iconContainerColor = if (comingSoon) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val iconTint = if (comingSoon) mutedColor else MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (comingSoon) comingSoonInactiveAlpha else 1f)
            .padding(vertical = 8.dp)
            .then(
                if (comingSoon) {
                    Modifier
                } else {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClick,
                    )
                },
            )
            .background(MaterialTheme.colorScheme.surface, cardShape)
            .then(
                if (usePremiumCardBorder) {
                    Modifier.appPremiumCardBorder(cardShape)
                } else {
                    Modifier
                },
            )
            .padding(Dimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIconTile(
            icon = icon,
            size = 40.dp,
            iconSize = 24.dp,
            containerColor = iconContainerColor,
            iconTint = iconTint,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            modifier = Modifier.weight(1f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (comingSoon) {
                ProfileComingSoonBadge()
            }
            if (showChevron && !comingSoon) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = mutedColor,
                )
            }
        }
    }
}

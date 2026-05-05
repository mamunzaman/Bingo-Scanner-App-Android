package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

enum class AppListRowDensity {
    Compact,
    Comfortable,
}

private val listRowMinHeightCompact = 48.dp
private val listRowMinHeightComfortable = 56.dp

/**
 * Generic list row: optional [leading], title + optional [subtitle], optional [trailing].
 * Use with [AppIconTile] and [AppInsetDivider] when migrating screens.
 */
@Composable
fun AppListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    density: AppListRowDensity = AppListRowDensity.Comfortable,
    onClick: (() -> Unit)? = null,
    horizontalPadding: Dp = Dimens.spacing16,
    verticalPadding: Dp = Dimens.spacing8,
) {
    val minH = when (density) {
        AppListRowDensity.Compact -> listRowMinHeightCompact
        AppListRowDensity.Comfortable -> listRowMinHeightComfortable
    }
    val rowModifier = modifier
        .fillMaxWidth()
        .heightIn(min = minH)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    onClick = onClick,
                    role = Role.Button,
                )
            } else {
                Modifier
            },
        )
        .padding(horizontal = horizontalPadding, vertical = verticalPadding)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        if (leading != null) {
            leading()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

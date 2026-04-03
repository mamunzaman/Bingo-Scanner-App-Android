package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.PrimaryPressed
import com.example.mamunbingoapp.theme.Slate600

@Composable
fun EmptyHistoryState(
    title: String,
    subtitle: String,
    icon: ImageVector? = Icons.Default.History,
    actions: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconEmptyState),
                    tint = PrimaryPressed.copy(alpha = 0.6f)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryPressed,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                textAlign = TextAlign.Center
            )
        }
        if (actions != null) {
            actions()
        }
    }
}

@Composable
fun EmptyHistoryStateActions(
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        actions()
    }
}

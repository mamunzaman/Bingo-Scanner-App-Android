package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.MamunBingoTheme

/**
 * Standalone M3 card with subtle elevation and configurable inner padding.
 * Prefer [AppSectionSurface] for premium grouped sections (border + shadow spec).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(Dimens.spacing16),
    content: @Composable () -> Unit,
) {
    val colors = CardDefaults.cardColors(
        containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationSubtle)
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            shape = shape,
            colors = colors,
            elevation = elevation,
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppCardPreview() {
    MamunBingoTheme {
        AppCard {
            androidx.compose.material3.Text(
                "Ticket #4829",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

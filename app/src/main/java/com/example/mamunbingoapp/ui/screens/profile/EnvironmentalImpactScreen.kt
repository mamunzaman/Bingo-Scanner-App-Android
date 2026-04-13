package com.example.mamunbingoapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppIconContainer
import com.example.mamunbingoapp.ui.components.AppTopBar

@Composable
fun EnvironmentalImpactScreen(onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AppHeaderBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Environmental Impact",
                showBack = true,
                onBackClick = onBack
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = Dimens.spacing5)
                    .padding(horizontal = Dimens.screenHorizontalPadding)
                    .padding(bottom = Dimens.spacing16)
            ) {
                Text(
                    text = "Track how your play choices can relate to environmental awareness. Figures shown here are illustrative until full analytics are connected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
                SettingsSubpageSection(title = "INSIGHTS") {
                    InfoRow(
                        icon = Icons.Default.Eco,
                        title = "Eco focus",
                        body = "The app highlights eco-themed tasks and education alongside bingo play."
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    InfoRow(
                        icon = Icons.Default.EnergySavingsLeaf,
                        title = "CO₂ awareness",
                        body = "Future updates may estimate cumulative CO₂ savings from digital actions and eco challenges."
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    InfoRow(
                        icon = Icons.Default.Park,
                        title = "Green goals",
                        body = "Reminders and rewards can nudge daily habits that support sustainability."
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    InfoRow(
                        icon = Icons.Default.Recycling,
                        title = "Transparency",
                        body = "When metrics go live, you will see clear explanations of how numbers are calculated."
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSubpageSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = AppTextStyles.sectionLabel,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = Dimens.spacing8)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Dimens.radiusSmall))
                .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacing16)
        ) {
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AppIconContainer(icon = icon, size = 40.dp, iconSize = 24.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.screenHorizontalPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

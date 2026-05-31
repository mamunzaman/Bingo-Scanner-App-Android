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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppIconContainer
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import com.example.mamunbingoapp.ui.components.AppTopBar

@Composable
fun LocationServicesScreen(onBack: () -> Unit) {
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
                title = stringResource(R.string.settings_location_services),
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
                    text = stringResource(R.string.profile_location_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
                SettingsSubpageSection(title = stringResource(R.string.profile_location_overview_section)) {
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        title = stringResource(R.string.profile_location_purpose_title),
                        body = stringResource(R.string.profile_location_purpose_body)
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    InfoRow(
                        icon = Icons.Default.MyLocation,
                        title = stringResource(R.string.profile_location_precision_title),
                        body = stringResource(R.string.profile_location_precision_body)
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    InfoRow(
                        icon = Icons.Default.Map,
                        title = stringResource(R.string.profile_location_control_title),
                        body = stringResource(R.string.profile_location_control_body)
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    InfoRow(
                        icon = Icons.Default.Security,
                        title = stringResource(R.string.profile_location_privacy_title),
                        body = stringResource(R.string.profile_location_privacy_body)
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
        AppSectionTitle(
            text = title,
            modifier = Modifier.padding(bottom = Dimens.spacing12),
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

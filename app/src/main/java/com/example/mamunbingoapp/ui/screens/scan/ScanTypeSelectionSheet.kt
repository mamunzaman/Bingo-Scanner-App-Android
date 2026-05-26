package com.example.mamunbingoapp.ui.screens.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTypeSelectionSheet(
    onDismiss: () -> Unit,
    onScanTypeSelected: (BingoScanType) -> Unit,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing8, bottom = Dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Text(
                text = "Choose scan type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier.padding(bottom = Dimens.spacing4),
            )
            Text(
                text = "Select the sheet format you are scanning.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Dimens.spacing4),
            )
            BingoScanType.entries.forEach { type ->
                ScanTypeOptionRow(
                    title = type.title,
                    subtitle = type.subtitle,
                    icon = scanTypeIcon(type),
                    onClick = { onScanTypeSelected(type) },
                )
            }
        }
    }
}

@Composable
private fun ScanTypeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusMedium),
        color = scheme.surfaceContainerLow,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            scheme.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha),
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
        ) {
            Surface(
                shape = RoundedCornerShape(Dimens.radiusSmall),
                color = scheme.primary.copy(alpha = 0.12f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier
                        .padding(Dimens.spacing8)
                        .size(Dimens.iconCompact),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun scanTypeIcon(type: BingoScanType): ImageVector = when (type) {
    BingoScanType.PLAY_PAPER -> Icons.Outlined.Description
    BingoScanType.ONLINE -> Icons.Outlined.Smartphone
    BingoScanType.MAIN_SHEET -> Icons.Outlined.GridOn
}

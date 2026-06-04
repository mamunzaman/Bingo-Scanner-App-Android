package com.example.mamunbingoapp.ui.screens.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.APP_SECTION_BORDER_ALPHA
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.AppIconTile
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
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing4, bottom = Dimens.spacing24),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing8),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            ) {
                Text(
                    text = stringResource(R.string.scan_type_choose_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.scan_type_choose_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.78f),
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing12),
            ) {
                BingoScanType.entries.forEach { type ->
                    ScanTypeOptionRow(
                        title = scanTypeTitle(type),
                        subtitle = scanTypeSubtitle(type),
                        icon = scanTypeIcon(type),
                        onClick = { onScanTypeSelected(type) },
                    )
                }
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
    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        color = scheme.surface,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            scheme.primary.copy(alpha = APP_SECTION_BORDER_ALPHA),
        ),
        shadowElevation = 0.5.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16),
        ) {
            AppIconTile(
                icon = icon,
                size = 44.dp,
                iconSize = Dimens.iconDefault,
                containerColor = scheme.primaryContainer.copy(alpha = 0.45f),
                iconTint = scheme.primary,
                shape = RoundedCornerShape(Dimens.radiusMedium),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.82f),
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

@Composable
fun scanTypeTitle(type: BingoScanType): String = stringResource(scanTypeTitleRes(type))

@Composable
fun scanTypeSubtitle(type: BingoScanType): String = stringResource(scanTypeSubtitleRes(type))

fun scanTypeTitleRes(type: BingoScanType): Int = when (type) {
    BingoScanType.PLAY_PAPER -> R.string.scan_type_play_paper_title
    BingoScanType.ONLINE -> R.string.scan_type_online_title
    BingoScanType.MAIN_SHEET -> R.string.scan_type_main_sheet_title
}

fun scanTypeSubtitleRes(type: BingoScanType): Int = when (type) {
    BingoScanType.PLAY_PAPER -> R.string.scan_type_play_paper_subtitle
    BingoScanType.ONLINE -> R.string.scan_type_online_subtitle
    BingoScanType.MAIN_SHEET -> R.string.scan_type_main_sheet_subtitle
}

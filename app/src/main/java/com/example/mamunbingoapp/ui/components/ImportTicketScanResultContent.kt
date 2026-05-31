package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.theme.WarningBorder
import com.example.mamunbingoapp.theme.WarningContainer
import com.example.mamunbingoapp.theme.WarningText

/**
 * Failed / weak-scan import layout aligned with [AppSectionSurface] card rhythm.
 * [primaryMessage] comes from the scan pipeline (e.g. [com.example.mamunbingoapp.viewmodel.weakScanFailureMessage]).
 */
@Composable
fun ImportTicketFailedScanContent(
    primaryMessage: String,
    helpText: String,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    detectedValidCount: Int? = null,
    losNumber: String? = null,
    serialNumber: String? = null,
    onRecrop: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    val iconBadgeShape = RoundedCornerShape(Dimens.radiusSmall)
    val errorCardBg =
        if (dark) lerp(cs.surface, WarningContainer, 0.22f)
        else WarningContainer.copy(alpha = 0.55f)
    val errorBorderColor = WarningBorder.copy(alpha = if (dark) 0.22f else 0.18f)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            color = errorCardBg,
            border = BorderStroke(Dimens.cardBorderDefault, errorBorderColor),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing12),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.iconAlertBox)
                        .clip(iconBadgeShape)
                        .background(WarningBorder.copy(alpha = if (dark) 0.35f else 0.28f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconCompact),
                        tint = WarningText.copy(alpha = 0.9f),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
                ) {
                    Text(
                        text = stringResource(R.string.import_ticket_failed_scan_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = cs.onSurface.copy(alpha = 0.94f),
                    )
                    Text(
                        text = primaryMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant.copy(alpha = 0.82f),
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                    )
                }
            }
        }
        AppSectionSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
        ) {
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = Primary.copy(alpha = if (dark) 0.82f else 0.88f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing10),
            )
        }
        ImportTicketFailedScanInfoRow(
            detectedValidCount = detectedValidCount,
            losNumber = losNumber,
            serialNumber = serialNumber,
            modifier = Modifier.fillMaxWidth(),
        )
        if (onRecrop != null) {
            TextButton(
                onClick = onRecrop,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_ticket_recrop))
            }
        }
        ImportTicketPhotoActionRow(
            onTakePhoto = onTakePhoto,
            onPickFromGallery = onPickFromGallery,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ImportTicketFailedScanInfoRow(
    detectedValidCount: Int?,
    losNumber: String?,
    serialNumber: String?,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val emDash = stringResource(R.string.common_em_dash)
    val countText = detectedValidCount?.toString() ?: emDash
    val losDisp = losNumber?.takeIf { it.isNotBlank() } ?: emDash
    val serDisp = serialNumber?.takeIf { it.isNotBlank() } ?: emDash
    val countIsZero = detectedValidCount == 0
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        letterSpacing = 0.4.sp,
        fontWeight = FontWeight.Medium,
    )
    val valueStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    val countColor = when {
        countIsZero -> WarningText.copy(alpha = 0.88f)
        else -> cs.onSurface.copy(alpha = 0.94f)
    }
    AppSectionSurface(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
            ) {
                Text(
                    text = stringResource(R.string.import_ticket_numbers_detected_label),
                    style = labelStyle,
                    color = cs.onSurfaceVariant.copy(alpha = 0.62f),
                )
                Text(
                    text = countText,
                    style = valueStyle,
                    color = countColor,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)) {
                    Text(
                        text = stringResource(R.string.import_ticket_losnummer_label),
                        style = labelStyle,
                        color = cs.onSurfaceVariant.copy(alpha = 0.62f),
                    )
                    Text(
                        text = losDisp,
                        style = valueStyle,
                        color = cs.onSurface.copy(alpha = 0.94f),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)) {
                    Text(
                        text = stringResource(R.string.import_ticket_seriennummer_label),
                        style = labelStyle,
                        color = cs.onSurfaceVariant.copy(alpha = 0.62f),
                    )
                    Text(
                        text = serDisp,
                        style = valueStyle,
                        color = cs.onSurface.copy(alpha = 0.94f),
                    )
                }
            }
        }
    }
}

@Composable
fun ImportTicketPhotoActionRow(
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val actionHeight = Dimens.buttonHeight
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Row(
        modifier = modifier.heightIn(min = actionHeight),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppPrimaryButton(
            text = stringResource(R.string.import_ticket_take_photo),
            onClick = onTakePhoto,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconCompact),
                    tint = OnPrimary,
                )
            },
        )
        Surface(
            onClick = onPickFromGallery,
            enabled = enabled,
            shape = shape,
            color = cs.surface,
            border = BorderStroke(Dimens.cardBorderDefault, cs.primary.copy(alpha = APP_SECTION_BORDER_ALPHA)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .size(actionHeight)
                .alpha(if (enabled) 1f else 0.38f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PhotoLibrary,
                    contentDescription = stringResource(R.string.import_ticket_gallery_cd),
                    modifier = Modifier.size(Dimens.iconDefault),
                    tint = PrimaryDark,
                )
            }
        }
    }
}

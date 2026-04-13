package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.CardBorderGreen
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.EmptyHistoryCardBg
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.theme.Slate600
import com.example.mamunbingoapp.theme.WarningBorder
import com.example.mamunbingoapp.theme.WarningContainer
import com.example.mamunbingoapp.theme.WarningText

/**
 * Failed / weak-scan import layout: error card, helper card, optional scan info row, photo actions.
 * Surfaces use soft green-tint fills + low-α **outline** borders (no drop shadow), aligned with live import header polish.
 * Analyzing / in-progress feedback on the ticket image is handled only in `ImportTicketMainContent` (hero overlay), not here.
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
    val iconBadgeShape = RoundedCornerShape(Dimens.radiusMedium)
    val errorCardBg =
        if (dark) lerp(cs.surface, cs.surfaceVariant, 0.1f)
        else lerp(Color(0xFFF6F8F4), cs.surfaceVariant, 0.05f)
    val helpCardBg =
        if (dark) lerp(cs.surfaceVariant, cs.surface, 0.35f).copy(alpha = 0.88f)
        else lerp(Color(0xFFF0F5EC), cs.surface, 0.25f).copy(alpha = 0.92f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(Dimens.spacing20))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            color = errorCardBg,
            border = BorderStroke(1.dp, cs.outline.copy(alpha = if (dark) 0.09f else 0.065f)),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing20, vertical = Dimens.spacing20),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.iconAlertBox)
                        .clip(iconBadgeShape)
                        .background(WarningContainer.copy(alpha = 0.72f))
                        .border(Dimens.cardBorderDefault, WarningBorder.copy(alpha = 0.28f), iconBadgeShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconCompact),
                        tint = WarningText.copy(alpha = 0.92f),
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacing12))
                Text(
                    text = stringResource(R.string.import_ticket_failed_scan_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface.copy(alpha = 0.96f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                Text(
                    text = primaryMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant.copy(alpha = 0.76f),
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spacing12))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            color = helpCardBg,
            border = BorderStroke(1.dp, cs.outline.copy(alpha = if (dark) 0.07f else 0.05f)),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
            )
        }
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        ImportTicketFailedScanInfoRow(
            detectedValidCount = detectedValidCount,
            losNumber = losNumber,
            serialNumber = serialNumber,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(Dimens.spacing20))
        if (onRecrop != null) {
            TextButton(
                onClick = onRecrop,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_ticket_recrop))
            }
            Spacer(modifier = Modifier.height(Dimens.spacing12))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
        ) {
            ImportTicketFailedScanPhotoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PhotoCamera,
                title = "Take Photo",
                subtitle = "Use your camera",
                background = PrimaryDark,
                iconBackground = PrimaryContainer.copy(alpha = 0.45f),
                iconTint = PrimaryContainer,
                titleColor = OnPrimary,
                subtitleColor = PrimaryContainer,
                enabled = enabled,
                onClick = onTakePhoto,
            )
            ImportTicketFailedScanPhotoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PhotoLibrary,
                title = "Gallery",
                subtitle = stringResource(R.string.import_ticket_gallery_subtitle),
                background = EmptyHistoryCardBg,
                iconBackground = PrimaryContainer.copy(alpha = 0.55f),
                iconTint = PrimaryDark,
                titleColor = PrimaryDark,
                subtitleColor = Slate600,
                border = BorderStroke(Dimens.cardBorderDefault, CardBorderGreen.copy(alpha = 0.45f)),
                enabled = enabled,
                onClick = onPickFromGallery,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
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
    val dark = isSystemInDarkTheme()
    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    val countText = detectedValidCount?.toString() ?: "—"
    val losDisp = losNumber?.takeIf { it.isNotBlank() } ?: "—"
    val serDisp = serialNumber?.takeIf { it.isNotBlank() } ?: "—"
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        letterSpacing = 0.45.sp,
        fontWeight = FontWeight.Medium,
    )
    val valueColor = cs.onSurface.copy(alpha = 0.94f)
    val metaValueStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
    Surface(
        modifier = modifier,
        shape = cardShape,
        color = if (dark) {
            cs.surfaceVariant.copy(alpha = 0.38f)
        } else {
            lerp(Color(0xFFF6F8F4), cs.surfaceVariant, 0.08f)
        },
        border = BorderStroke(1.dp, cs.outline.copy(alpha = if (dark) 0.08f else 0.06f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing20, vertical = Dimens.spacing20),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing16),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            ) {
                Text(
                    text = stringResource(R.string.import_ticket_numbers_detected_label),
                    style = labelStyle,
                    color = cs.onSurfaceVariant.copy(alpha = 0.58f),
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing12),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                    Text(
                        text = stringResource(R.string.import_ticket_losnummer_label),
                        style = labelStyle,
                        color = cs.onSurfaceVariant.copy(alpha = 0.58f),
                    )
                    Text(
                        text = losDisp,
                        style = metaValueStyle,
                        color = valueColor,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                    Text(
                        text = stringResource(R.string.import_ticket_seriennummer_label),
                        style = labelStyle,
                        color = cs.onSurfaceVariant.copy(alpha = 0.58f),
                    )
                    Text(
                        text = serDisp,
                        style = metaValueStyle,
                        color = valueColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportTicketFailedScanPhotoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    background: Color,
    iconBackground: Color,
    iconTint: Color,
    titleColor: Color,
    subtitleColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val pad = Dimens.spacing12
    val minH = 76.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minH)
            .clip(shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.38f)
            .padding(horizontal = pad, vertical = pad),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(Dimens.radiusSmall))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = iconTint,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = titleColor,
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = subtitleColor.copy(alpha = 0.88f),
        )
    }
}

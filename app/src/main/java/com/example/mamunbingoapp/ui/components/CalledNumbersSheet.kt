package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import com.example.mamunbingoapp.domain.qr.CalledNumbersQrCodec
import com.example.mamunbingoapp.domain.qr.QrTicketImageGenerator
import com.example.mamunbingoapp.theme.Error
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.viewmodel.CalledNumbersViewModel

/**
 * Bottom sheet: called numbers with tappable B/I/N/G/O board circles for in-place edit during live manual entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalledNumbersSheet(
    onDismiss: () -> Unit,
    calledNumbers: List<Int>,
    viewModel: CalledNumbersViewModel,
    onStartReplace: () -> Unit,
    maxCalls: Int = MAX_LIVE_CALLS,
    title: String? = null,
    countPillText: String? = null,
    footerText: String? = null,
    onOverflowMenuClick: (() -> Unit)? = null,
    onShareCalledNumbers: (() -> Unit)? = null,
    onShowQrCode: (() -> Unit)? = null,
    onScanQr: (() -> Unit)? = null,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val resolvedTitle = title ?: stringResource(R.string.live_play_called_numbers_label)
    val scheme = MaterialTheme.colorScheme
    val orderedCalls = remember(calledNumbers) { calledNumbers.distinct() }
    val numbersByColumn = remember(calledNumbers) { groupCalledNumbersByColumn(calledNumbers) }
    val boardLatest = calledNumbers.lastOrNull()
    val distinctCallCount = orderedCalls.size
    val selectedNumber = viewModel.selectedCalledNumber
    val clearSelectionInteraction = remember { MutableInteractionSource() }
    val clearSelectionModifier = Modifier.clickable(
        interactionSource = clearSelectionInteraction,
        indication = null,
        onClick = viewModel::clearSelection,
    )

    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing20, bottom = Dimens.spacing16)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing16),
        ) {
            val hasHeaderActions = onShareCalledNumbers != null || onOverflowMenuClick != null ||
                onShowQrCode != null || onScanQr != null
            if (hasHeaderActions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimens.spacing8),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onShowQrCode != null) {
                        val canShowQr = calledNumbers.isNotEmpty()
                        IconButton(
                            onClick = onShowQrCode,
                            enabled = canShowQr,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = stringResource(
                                    R.string.live_play_show_called_numbers_qr_cd,
                                ),
                                tint = if (canShowQr) {
                                    scheme.primary.copy(alpha = 0.82f)
                                } else {
                                    scheme.onSurfaceVariant.copy(alpha = 0.38f)
                                },
                            )
                        }
                    }
                    if (onScanQr != null) {
                        IconButton(
                            onClick = onScanQr,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = stringResource(
                                    R.string.live_play_scan_called_numbers_qr_cd,
                                ),
                                tint = scheme.onSurfaceVariant.copy(alpha = 0.88f),
                            )
                        }
                    }
                    if (onShareCalledNumbers != null) {
                        val canShare = calledNumbers.isNotEmpty()
                        IconButton(
                            onClick = onShareCalledNumbers,
                            enabled = canShare,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = stringResource(
                                    R.string.live_play_share_called_numbers_cd,
                                ),
                                tint = if (canShare) {
                                    scheme.primary.copy(alpha = 0.82f)
                                } else {
                                    scheme.onSurfaceVariant.copy(alpha = 0.38f)
                                },
                            )
                        }
                    }
                    if (onOverflowMenuClick != null) {
                        IconButton(
                            onClick = onOverflowMenuClick,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            ) {
                Text(
                    text = resolvedTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Surface(
                    shape = RoundedCornerShape(Dimens.radiusPill),
                    color = scheme.primary.copy(alpha = 0.14f),
                    border = BorderStroke(Dimens.cardBorderDefault, scheme.primary.copy(alpha = 0.28f)),
                ) {
                    Text(
                        text = countPillText ?: "${calledNumbers.size} / $maxCalls",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.primary,
                        modifier = Modifier.padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing5),
                    )
                }
            }
            TvBingoBoard(
                numbersByColumn = numbersByColumn,
                latest = boardLatest,
                boardGreen = scheme.primary,
                letterRed = scheme.secondary,
                lineColor = Color.Black.copy(alpha = 0.38f),
                callSequence = calledNumbers,
                selectedNumber = selectedNumber,
                onNumberClick = viewModel::onNumberTapped,
                selectedNumberOverlay = { chipSize ->
                    CalledNumberEditPill(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = chipSize * 0.08f, y = -chipSize * 0.22f)
                            .zIndex(1f),
                        onEdit = onStartReplace,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = footerText ?: "$distinctCallCount called numbers",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant.copy(alpha = 0.68f),
                modifier = Modifier
                    .padding(top = Dimens.spacing4)
                    .then(clearSelectionModifier),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalledNumbersQrDisplaySheet(
    calledNumbers: List<Int>,
    onDismiss: () -> Unit,
    onShareQrImage: (android.graphics.Bitmap) -> Unit,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    val qrContent = remember(calledNumbers) { CalledNumbersQrCodec.encode(calledNumbers) }
    var qrBitmap by remember(calledNumbers) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var qrError by remember(calledNumbers) { mutableStateOf<String?>(null) }
    androidx.compose.runtime.LaunchedEffect(qrContent) {
        qrBitmap = null
        qrError = null
        QrTicketImageGenerator.generateBitmap(qrContent, sizePx = 1024)
            .onSuccess { qrBitmap = it }
            .onFailure { qrError = it.message }
    }
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing16, bottom = Dimens.spacing24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing12),
        ) {
            Text(
                text = stringResource(R.string.called_numbers_qr_display_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.called_numbers_qr_display_help),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            when {
                qrError != null -> {
                    Text(
                        text = stringResource(R.string.called_numbers_qr_display_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Error,
                        textAlign = TextAlign.Center,
                    )
                }
                qrBitmap == null -> {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                    )
                }
                else -> {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.called_numbers_qr_code_cd),
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(Dimens.spacing8)),
                    )
                }
            }
            if (qrBitmap != null) {
                OutlinedButton(
                    onClick = { qrBitmap?.let(onShareQrImage) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.iconCompact),
                        )
                        Text(stringResource(R.string.called_numbers_qr_share))
                    }
                }
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    }
}

@Composable
private fun CalledNumberEditPill(
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.radiusPill),
        color = scheme.surfaceContainerHigh,
        shadowElevation = Dimens.cardElevationSubtle,
        border = BorderStroke(Dimens.cardBorderDefault, scheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(30.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.ticket_detail_edit_cd),
                modifier = Modifier.size(Dimens.iconCompact),
                tint = scheme.primary,
            )
        }
    }
}

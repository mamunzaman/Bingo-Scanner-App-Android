package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens

enum class PendingSheetSave {
    SAVE_ONLY,
    SAVE_AND_PLAY,
}

data class SheetDuplicateUi(
    val visible: Boolean = false,
    val existingTicketId: String? = null,
    val losNumber: String = "",
    val serialNumber: String = "",
    val pendingSave: PendingSheetSave? = null,
)

private val SheetDialogShape = RoundedCornerShape(
    topStart = Dimens.radiusXL,
    topEnd = Dimens.radiusXL,
    bottomStart = Dimens.radiusLarge,
    bottomEnd = Dimens.radiusLarge,
)

@Composable
fun DuplicateSheetDialog(
    visible: Boolean,
    losNumber: String?,
    serialNumber: String?,
    onOpenExistingSheet: () -> Unit,
    onScanAnother: () -> Unit,
    onSaveAnyway: () -> Unit,
) {
    if (!visible) return
    val los = losNumber?.trim()?.takeIf { it.isNotEmpty() }
    val serial = serialNumber?.trim()?.takeIf { it.isNotEmpty() }
    val scheme = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onScanAnother,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onScanAnother,
                    )
                    .background(scheme.scrim),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenHorizontalPadding)
                    .padding(bottom = Dimens.spacing12)
                    .navigationBarsPadding()
                    .shadow(Dimens.cardElevationDefault, SheetDialogShape),
                shape = SheetDialogShape,
                color = scheme.surface,
                tonalElevation = Dimens.cardElevationSubtle,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = Dimens.spacing20,
                            vertical = Dimens.spacing12,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .width(Dimens.spacing24)
                            .height(Dimens.spacing4)
                            .background(
                                scheme.onSurfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(Dimens.radiusPill),
                            ),
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing10))
                    AppIconTile(
                        icon = Icons.Outlined.ContentCopy,
                        size = 40.dp,
                        iconSize = Dimens.iconCompact,
                        containerColor = scheme.primaryContainer.copy(alpha = 0.28f),
                        iconTint = scheme.primary,
                        shape = RoundedCornerShape(Dimens.radiusSmall),
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing10))
                    Text(
                        text = stringResource(R.string.manual_entry_duplicate_sheet_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing5))
                    Text(
                        text = stringResource(R.string.manual_entry_duplicate_sheet_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (los != null || serial != null) {
                        Spacer(modifier = Modifier.height(Dimens.spacing12))
                        AppSectionSurface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimens.radiusCard),
                            color = scheme.surfaceContainer,
                            borderColor = scheme.primary.copy(alpha = APP_SECTION_BORDER_ALPHA),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.spacing12),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                            ) {
                                if (los != null) {
                                    DuplicateSheetMetaChip(
                                        label = stringResource(R.string.home_active_ticket_los_label),
                                        value = los,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (serial != null) {
                                    DuplicateSheetMetaChip(
                                        label = stringResource(R.string.home_active_ticket_serie_label),
                                        value = serial,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing16))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                    ) {
                        Button(
                            onClick = onOpenExistingSheet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.buttonHeight),
                            shape = RoundedCornerShape(Dimens.radiusButtonPill),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = scheme.primary,
                                contentColor = scheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.manual_entry_duplicate_open_existing),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        OutlinedButton(
                            onClick = onScanAnother,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.buttonHeight),
                            shape = RoundedCornerShape(Dimens.radiusButtonPill),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = scheme.surfaceContainer,
                                contentColor = scheme.primary,
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                scheme.primary.copy(alpha = 0.72f),
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.manual_entry_duplicate_scan_another),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        TextButton(
                            onClick = onSaveAnyway,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.manual_entry_duplicate_save_anyway),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateSheetMetaChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.radiusSmall),
        color = scheme.surfaceContainerHigh,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            scheme.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha),
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = Dimens.spacing12,
                vertical = Dimens.spacing10,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

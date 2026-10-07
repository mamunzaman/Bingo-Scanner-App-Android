package com.example.mamunbingoapp.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.ui.components.AppBottomSheetSurface
import com.example.mamunbingoapp.ui.components.AppInsetDivider
import com.example.mamunbingoapp.ui.components.rememberAppBottomSheetState
import com.example.mamunbingoapp.ui.core.interaction.appRipple

private val ScanTypeSheetCorner = 24.dp
private val ScanTypeThumbnailSize = 76.dp
private val ScanTypeRowMinHeight = 48.dp
val ScanTypeSheetFormats: List<BingoScanType> = BingoScanType.entries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTypeSelectionSheet(
    onDismiss: () -> Unit,
    onScanTypeSelected: (BingoScanType) -> Unit,
    onAddFromGallery: () -> Unit,
) {
    val sheetState = rememberAppBottomSheetState(skipPartiallyExpanded = true)
    AppBottomSheetSurface(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = ScanTypeSheetCorner, topEnd = ScanTypeSheetCorner),
    ) {
        ScanTypeSheetContent(
            onScanTypeSelected = onScanTypeSelected,
            onAddFromGallery = onAddFromGallery,
        )
    }
}

@Composable
fun ScanTypeSheetContent(
    onScanTypeSelected: (BingoScanType) -> Unit,
    onAddFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .padding(bottom = Dimens.spacing16),
    ) {
        Text(
            text = stringResource(R.string.scan_type_choose_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            modifier = Modifier.padding(top = Dimens.spacing4),
        )
        Spacer(Modifier.height(Dimens.spacing8))
        Text(
            text = stringResource(R.string.scan_type_choose_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Dimens.spacing16))
        ScanTypeSheetFormats.forEachIndexed { index, type ->
            val title = scanTypeTitle(type)
            val subtitle = scanTypeSubtitle(type)
            ScanTypeFormatRow(
                title = title,
                subtitle = subtitle,
                onClick = { onScanTypeSelected(type) },
                thumbnail = {
                    ScanTypeThumbnail(
                        type = type,
                        modifier = Modifier
                            .size(ScanTypeThumbnailSize)
                            .clearAndSetSemantics { },
                    )
                },
            )
            if (index != ScanTypeSheetFormats.lastIndex) {
                AppInsetDivider(startInset = ScanTypeThumbnailSize + Dimens.spacing12)
            }
        }
        Spacer(Modifier.height(Dimens.spacing20))
        Text(
            text = stringResource(R.string.scan_type_gallery_section),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Dimens.spacing8))
        ScanTypeGalleryRow(onClick = onAddFromGallery)
    }
}

@Composable
private fun ScanTypeFormatRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    thumbnail: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val description = stringResource(R.string.scan_type_option_a11y, title, subtitle)
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ScanTypeRowMinHeight)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clickable(
                interactionSource = interaction,
                indication = appRipple(),
                onClick = onClick,
            )
            .padding(vertical = Dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        thumbnail()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.iconDefault),
        )
    }
}

@Composable
private fun ScanTypeGalleryRow(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val title = stringResource(R.string.scan_type_gallery_title)
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(Dimens.radiusSearchField)
    val galleryBg = if (isSystemInDarkTheme()) {
        scheme.primaryContainer.copy(alpha = 0.32f)
    } else {
        IconContainerBg
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ScanTypeRowMinHeight)
            .clip(shape)
            .background(galleryBg)
            .semantics(mergeDescendants = true) { contentDescription = title }
            .clickable(
                interactionSource = interaction,
                indication = appRipple(),
                onClick = onClick,
            )
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoLibrary,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(Dimens.iconDefault),
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.iconDefault),
        )
    }
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

@Preview(showBackground = true)
@Composable
private fun ScanTypeSheetLightPreview() {
    MamunBingoTheme {
        ScanTypeSheetContent(onScanTypeSelected = {}, onAddFromGallery = {})
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScanTypeSheetDarkPreview() {
    MamunBingoTheme(darkTheme = true) {
        ScanTypeSheetContent(onScanTypeSelected = {}, onAddFromGallery = {})
    }
}

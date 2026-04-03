package com.example.mamunbingoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mamunbingoapp.theme.CardBorderGreen
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.Scrim
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.Slate200
import com.example.mamunbingoapp.theme.Slate400
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.theme.Success
import com.example.mamunbingoapp.theme.WarningText
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.QualityLevel
import com.example.mamunbingoapp.ui.components.ScanQualityData
import com.example.mamunbingoapp.ui.components.ScanResultData
import com.example.mamunbingoapp.ui.components.ScanResultQualityCard
import com.example.mamunbingoapp.viewmodel.ImportTicketViewModel
import com.example.mamunbingoapp.viewmodel.ScanResultUiState
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor

@Composable
fun ImportTicketScreen(
    onBack: () -> Unit,
    onDiscardAndBack: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onReviewClicked: () -> Unit,
    showBottomBar: Boolean = true,
    imageSource: String? = null,
    viewModel: ImportTicketViewModel = viewModel()
) {
    val selectedUri by viewModel.selectedImageUri.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val successNumbers = (scanResult as? ScanResultUiState.Success)?.numbers
    val ctaEnabled = !successNumbers.isNullOrEmpty()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingReplaceAction by remember { mutableStateOf<PendingReplace?>(null) }
    var reviewClickedOnce by remember(selectedUri, scanResult) { mutableStateOf(false) }
    val isDirty = selectedUri != null ||
        scanResult is ScanResultUiState.Loading ||
        scanResult is ScanResultUiState.Success ||
        scanResult is ScanResultUiState.Error
    val isAnalyzing = scanResult is ScanResultUiState.Loading
    val scrollState = rememberScrollState()
    var previousScanResult by remember { mutableStateOf<ScanResultUiState?>(null) }
    var userTouchedScroll by remember { mutableStateOf(false) }
    var programmaticScrollInProgress by remember { mutableStateOf(false) }
    var scrollValueObservedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        if (!scrollValueObservedOnce) {
            scrollValueObservedOnce = true
            return@LaunchedEffect
        }
        if (!programmaticScrollInProgress) userTouchedScroll = true
    }
    LaunchedEffect(scanResult) {
        val prev = previousScanResult
        if ((scanResult is ScanResultUiState.Success || scanResult is ScanResultUiState.Error) && prev is ScanResultUiState.Loading && !userTouchedScroll) {
            programmaticScrollInProgress = true
            scrollState.animateScrollTo(scrollState.maxValue)
            programmaticScrollInProgress = false
        }
        previousScanResult = scanResult
    }
    LaunchedEffect(selectedUri) {
        if (selectedUri != null) {
            userTouchedScroll = false
            if (!userTouchedScroll) {
                programmaticScrollInProgress = true
                scrollState.animateScrollTo(0)
                programmaticScrollInProgress = false
            }
        }
    }

    BackHandler(enabled = true) {
        if (isDirty) showDiscardDialog = true else onBack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.statusBarsPadding()) {
            AppTopBar(
                title = "Import Ticket",
                showBack = true,
                onBackClick = { if (isDirty) showDiscardDialog = true else onBack() }
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing24, bottom = Dimens.spacing16)
        ) {
            HeroBannerCard(
                selectedImageUri = selectedUri,
                isAnalyzing = scanResult is ScanResultUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            when {
                scanResult is ScanResultUiState.Success || scanResult is ScanResultUiState.Error -> ScanResultQualityCard(
                    scanResult = scanResultToCardData(scanResult),
                    qualityData = scanResultToQualityData(scanResult),
                    showQualitySection = true,
                    modifier = Modifier.fillMaxWidth()
                )
                else -> ScanResultPlaceholder(
                    isAnalyzing = scanResult is ScanResultUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                TakePhotoCard(
                    modifier = Modifier.weight(1f),
                    onClick = { if (isDirty) pendingReplaceAction = PendingReplace.TakePhoto else onTakePhoto() }
                )
                GalleryCard(
                    modifier = Modifier.weight(1f),
                    onClick = { if (isDirty) pendingReplaceAction = PendingReplace.PickGallery else onPickFromGallery() }
                )
            }
            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(Dimens.spacing16))
                Text(
                    text = "Image actions are unavailable while analysis is running.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
            }
            if ((scanResult is ScanResultUiState.Success || scanResult is ScanResultUiState.Error) && imageSource != null) {
                Text(
                    text = "Source: $imageSource",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
            }
            ctaHelperLine(scanResult)?.let { (text, useWarningColor) ->
                Spacer(modifier = Modifier.height(Dimens.spacing16))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (useWarningColor) WarningText else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.spacing16))
            }
            if ((scanResult as? ScanResultUiState.Success)?.let { finalUiGridRowMajor(it.numbers).count { n -> n != 0 } } == FULL_GRID_COUNT) {
                Text(
                    text = "Perfect scan. All numbers detected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Success,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                Spacer(modifier = Modifier.height(Dimens.spacing24))
            }
            AppPrimaryButton(
                text = primaryCtaLabel(scanResult),
                onClick = {
                    if (!reviewClickedOnce) {
                        reviewClickedOnce = true
                        onReviewClicked()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ctaEnabled && !reviewClickedOnce,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconDefault),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
            if (!isAnalyzing) {
                secondaryActionLabel(scanResult)?.let { label ->
                    Spacer(modifier = Modifier.height(Dimens.spacing12))
                    OutlinedButton(
                        onClick = { if (isDirty) pendingReplaceAction = PendingReplace.PickGallery else onPickFromGallery() },
                        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
                        border = BorderStroke(1.dp, CardBorderGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(text = label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        if (showBottomBar) {
            AppBottomBar(selectedTab = AppTab.Scan, onTabSelected = {})
        }
    }
    AppConfirmDialog(
        visible = showDiscardDialog,
        title = "Discard scan?",
        message = "Your uploaded image and scan result will be lost.",
        confirmText = "Discard",
        cancelText = "Stay",
        onConfirm = { showDiscardDialog = false; onDiscardAndBack() },
        onCancel = { showDiscardDialog = false },
        onDismiss = { showDiscardDialog = false },
    )
    AppConfirmDialog(
        visible = pendingReplaceAction != null,
        title = "Replace current scan?",
        message = "Your current uploaded image and scan result will be replaced.",
        confirmText = "Replace",
        cancelText = "Cancel",
        onConfirm = {
            when (pendingReplaceAction) {
                PendingReplace.TakePhoto -> onTakePhoto()
                PendingReplace.PickGallery -> onPickFromGallery()
                null -> { }
            }
            pendingReplaceAction = null
        },
        onCancel = { pendingReplaceAction = null },
        onDismiss = { pendingReplaceAction = null },
    )
}

private enum class PendingReplace { TakePhoto, PickGallery }

private const val FULL_GRID_COUNT = 25

private fun primaryCtaLabel(scanResult: ScanResultUiState): String = when (scanResult) {
    is ScanResultUiState.Idle,
    is ScanResultUiState.Loading,
    is ScanResultUiState.Error -> "Review Detected Ticket"
    is ScanResultUiState.Success -> when (finalUiGridRowMajor(scanResult.numbers).count { it != 0 }) {
        0 -> "Review Detected Ticket"
        FULL_GRID_COUNT -> "Review Ticket"
        else -> "Review Detected Numbers"
    }
}

private fun secondaryActionLabel(scanResult: ScanResultUiState): String? = when (scanResult) {
    is ScanResultUiState.Error -> "Retry Scan"
    is ScanResultUiState.Success -> when (finalUiGridRowMajor(scanResult.numbers).count { it != 0 }) {
        0 -> "Try Another Photo"
        else -> "Scan Again"
    }
    else -> null
}

private fun ctaHelperLine(scanResult: ScanResultUiState): Pair<String, Boolean>? = when (scanResult) {
    is ScanResultUiState.Loading -> Pair("Please wait while your ticket is being analyzed.", false)
    is ScanResultUiState.Error -> Pair("Scan failed. Try another photo or enter numbers manually.", false)
    is ScanResultUiState.Success -> when (val n = finalUiGridRowMajor(scanResult.numbers).count { it != 0 }) {
        0 -> Pair("No readable numbers were found. Try another photo or enter numbers manually.", false)
        in 1..(FULL_GRID_COUNT - 1) -> Pair("Only some numbers were detected. Please review before continuing.", true)
        else -> null
    }
    else -> Pair("Upload a ticket image to start scanning.", false)
}

private fun scanResultToCardData(scanResult: ScanResultUiState): ScanResultData = when (scanResult) {
    is ScanResultUiState.Success -> {
        val count = finalUiGridRowMajor(scanResult.numbers).count { it != 0 }
        val statusText = when {
            count == FULL_GRID_COUNT -> "Full ticket detected"
            count in 1..(FULL_GRID_COUNT - 1) -> "Partial detection"
            else -> "No readable numbers"
        }
        ScanResultData(
            numbers = count.toString(),
            grid = if (count > 0) "5×5" else "—",
            card = statusText
        )
    }
    is ScanResultUiState.Error -> ScanResultData(
        numbers = "—",
        grid = "—",
        card = "Could not read ticket",
        helperLine = "Try another photo with better lighting and focus."
    )
    is ScanResultUiState.Loading -> ScanResultData(numbers = "—", grid = "—", card = "Scanning…")
    else -> ScanResultData()
}

private fun qualityFromCount(count: Int): QualityLevel = when {
    count <= 0 -> QualityLevel.LOW
    count >= FULL_GRID_COUNT -> QualityLevel.HIGH
    else -> QualityLevel.MEDIUM
}

private fun scanResultToQualityData(scanResult: ScanResultUiState): ScanQualityData = when (scanResult) {
    is ScanResultUiState.Success -> {
        val level = qualityFromCount(finalUiGridRowMajor(scanResult.numbers).count { it != 0 })
        val ok = level == QualityLevel.HIGH || level == QualityLevel.MEDIUM
        ScanQualityData(
            level = level,
            lightingOk = ok,
            focusOk = ok,
            angleOk = level == QualityLevel.HIGH
        )
    }
    is ScanResultUiState.Error -> ScanQualityData(level = QualityLevel.LOW, lightingOk = false, focusOk = false, angleOk = false)
    else -> ScanQualityData()
}

@Composable
private fun ScanResultPlaceholder(
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.cardBorderDefault, CardBorderGreen),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.spacing24),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                Text(
                    text = "Analyzing…",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Scan result will appear after you upload and analyze a ticket image.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HeroBannerCard(
    selectedImageUri: Uri?,
    isAnalyzing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val imageShape = RoundedCornerShape(Dimens.radiusBingoCell)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.cardBorderDefault, CardBorderGreen, shape)
            .padding(Dimens.spacing12)
    ) {
        if (selectedImageUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(imageShape),
                    contentScale = ContentScale.Crop
                )
                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(imageShape)
                            .background(Scrim),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.spacing24),
                                color = MaterialTheme.colorScheme.onSurface,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Analyzing…",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(Dimens.radiusSmall))
                        .background(IconContainerBg)
                        .border(Dimens.cardBorderDefault, CardBorderGreen, RoundedCornerShape(Dimens.radiusSmall)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconDefault),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "No photo selected",
                    style = AppTextStyles.actionCardTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Take or choose a photo to scan your bingo ticket",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TakePhotoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = Dimens.buttonHeight * 2)
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.cardBorderDefault, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(Dimens.radiusCard))
            .clickable(onClick = onClick)
            .padding(Dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(Dimens.radiusBingoCell))
                .background(IconContainerBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconCompact),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Take Photo",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Use your camera",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GalleryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier
            .heightIn(min = Dimens.buttonHeight * 2)
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.cardBorderDefault, CardBorderGreen, RoundedCornerShape(Dimens.radiusCard))
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(Dimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(Dimens.radiusBingoCell))
                .background(IconContainerBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconCompact),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Gallery",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Pick from photos",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun ImportTicketScreenPreview() {
    MamunBingoTheme {
        ImportTicketScreen(
            onBack = {},
            onDiscardAndBack = {},
            onTakePhoto = {},
            onPickFromGallery = {},
            onReviewClicked = {},
            showBottomBar = true
        )
    }
}

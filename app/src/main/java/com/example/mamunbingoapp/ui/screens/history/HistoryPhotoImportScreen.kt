package com.example.mamunbingoapp.ui.screens.history

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.font.FontWeight
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Error
import com.example.mamunbingoapp.theme.Success
import com.example.mamunbingoapp.theme.Warning
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.viewmodel.ImportTicketViewModel
import com.example.mamunbingoapp.viewmodel.ScanResultUiState
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor

private const val PREVIEW_TAG = "HistoryPhotoImport"

/** Minimum filled cells before we allow continuing (encourages close-up capture for OCR). */
private const val MinDetectedCellsToContinue = 15

@Composable
private fun AnalysisSummaryBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(Dimens.radiusSmall),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(Modifier.padding(Dimens.spacing12)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private data class ScanQualityUiModel(
    val lightingScore: Int,
    val focusScore: Int,
    val angleScore: Int,
    val overallScore: Int,
    val overallLabel: String
)

private val FALLBACK_QUALITY = ScanQualityUiModel(50, 50, 50, 50, "Medium")

private fun scoreToSuffix(score: Int): String = when {
    score >= 75 -> " ✓"
    score in 50..74 -> " ~"
    else -> " !"
}

private fun scoreToColor(score: Int): Color = when {
    score >= 75 -> Success
    score in 50..74 -> Warning
    else -> Error
}

private fun scanQualityHint(model: ScanQualityUiModel): Pair<String, Boolean>? {
    if (model.overallScore >= 75) return null
    val (score, hint) = when {
        model.lightingScore <= model.focusScore && model.lightingScore <= model.angleScore ->
            model.lightingScore to "Improve lighting"
        model.focusScore <= model.angleScore ->
            model.focusScore to "Hold camera steady"
        else ->
            model.angleScore to "Take photo more straight"
    }
    if (score >= 75) return null
    return hint to (score < 50)
}

private suspend fun analyzeImageQuality(context: Context, uri: Uri): ScanQualityUiModel =
    withContext(Dispatchers.IO) {
        try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            val w = opts.outWidth
            val h = opts.outHeight
            if (w <= 0 || h <= 0) return@withContext FALLBACK_QUALITY
            val sample = maxOf(1, maxOf(w, h) / 200)
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return@withContext FALLBACK_QUALITY
            val lighting = computeLightingScore(bitmap).coerceIn(0, 100)
            val focus = computeFocusScore(bitmap).coerceIn(0, 100)
            val angle = computeAngleScore(bitmap).coerceIn(0, 100)
            val overall = (lighting * 0.4f + focus * 0.4f + angle * 0.2f).toInt().coerceIn(0, 100)
            val label = when {
                overall >= 75 -> "Good"
                overall in 50..74 -> "Medium"
                else -> "Poor"
            }
            ScanQualityUiModel(lighting, focus, angle, overall, label)
        } catch (e: Exception) {
            Log.e(PREVIEW_TAG, "scan quality analysis failed", e)
            FALLBACK_QUALITY
        }
    }

private fun computeLightingScore(bmp: Bitmap): Int {
    val w = bmp.width
    val h = bmp.height
    var sum = 0L
    var n = 0
    val step = maxOf(1, minOf(w, h) / 50)
    for (y in 0 until h step step) {
        for (x in 0 until w step step) {
            val p = bmp.getPixel(x, y)
            val g = (AndroidColor.red(p) * 0.299f + AndroidColor.green(p) * 0.587f + AndroidColor.blue(p) * 0.114f).toInt()
            sum += g
            n++
        }
    }
    if (n == 0) return 50
    val avg = (sum / n).toInt()
    return (100 - minOf(100, kotlin.math.abs(avg - 128) * 100 / 128)).coerceIn(0, 100)
}

private fun computeFocusScore(bmp: Bitmap): Int {
    val w = bmp.width
    val h = bmp.height
    if (w < 3 || h < 3) return 50
    var sum = 0.0
    var n = 0
    val step = maxOf(1, minOf(w, h) / 40)
    for (y in 1 until h - 1 step step) {
        for (x in 1 until w - 1 step step) {
            val c = gray(bmp, x, y)
            val dx = 2 * c - gray(bmp, x - 1, y) - gray(bmp, x + 1, y)
            val dy = 2 * c - gray(bmp, x, y - 1) - gray(bmp, x, y + 1)
            sum += kotlin.math.abs(dx) + kotlin.math.abs(dy)
            n++
        }
    }
    if (n == 0) return 50
    val v = (sum / n).toFloat()
    return minOf(100, (v * 0.4f).toInt()).coerceIn(0, 100)
}

private fun gray(bmp: Bitmap, x: Int, y: Int): Int {
    val p = bmp.getPixel(x, y)
    return (AndroidColor.red(p) * 0.299f + AndroidColor.green(p) * 0.587f + AndroidColor.blue(p) * 0.114f).toInt()
}

private fun computeAngleScore(bmp: Bitmap): Int {
    val w = bmp.width
    val h = bmp.height
    if (w <= 0 || h <= 0) return 50
    val ratio = minOf(w, h).toFloat() / maxOf(w, h)
    return if (ratio >= 0.7f) 100 else (50 + (ratio * 70)).toInt().coerceIn(0, 100)
}

@Composable
private fun ScanQualityCard(
    modifier: Modifier = Modifier,
    title: String = "SCAN QUALITY",
    status: String = "Good",
    statusColor: Color = Success,
    progress: Float = 0.72f,
    lightingLabel: String = "Lighting ✓",
    focusLabel: String = "Focus ✓",
    angleLabel: String = "Angle ~",
    lightingColor: Color = MaterialTheme.colorScheme.onSurface,
    focusColor: Color = MaterialTheme.colorScheme.onSurface,
    angleColor: Color = Warning
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusCard),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing12)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.progressBarHeight)
                    .clip(RoundedCornerShape(Dimens.progressBarRadius)),
                color = Success,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = lightingLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = lightingColor
                )
                Text(
                    text = focusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = focusColor
                )
                Text(
                    text = angleLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = angleColor
                )
            }
        }
    }
}

@Composable
private fun ImportTicketContent(
    selectedImageUri: Uri?,
    isAnalyzing: Boolean,
    onTakePhotoClick: () -> Unit,
    onClearImageClick: () -> Unit,
    onScanAgainClick: () -> Unit,
    onRetryAnalysisClick: () -> Unit,
    analysisSummary: String?,
    detectedCount: Int,
    canContinue: Boolean,
    showIncompleteWarning: Boolean,
    showLowConfidenceWarning: Boolean,
    showTooFewNumbersWarning: Boolean,
    isScanError: Boolean,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scanQuality by remember(selectedImageUri) { mutableStateOf(FALLBACK_QUALITY) }
    var analysisJob by remember { mutableStateOf<Job?>(null) }
    var showImagePreview by remember { mutableStateOf(false) }
    val currentUri = rememberUpdatedState(selectedImageUri)
    val scrollState = rememberScrollState()
    var resultSectionScrollOffset by remember(selectedImageUri) { mutableStateOf(0) }
    var didAutoScrollToResult by remember(selectedImageUri) { mutableStateOf(false) }
    var userTouchedScroll by remember(selectedImageUri) { mutableStateOf(false) }
    var programmaticScrollInProgress by remember { mutableStateOf(false) }
    var continueUsed by remember(selectedImageUri) { mutableStateOf(false) }
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress && !programmaticScrollInProgress) userTouchedScroll = true
    }
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            resultSectionScrollOffset = 0
            didAutoScrollToResult = false
            userTouchedScroll = false
        }
    }
    LaunchedEffect(selectedImageUri) {
        analysisJob?.cancel()
        if (selectedImageUri != null) {
            analysisJob = launch {
                val uriAtStart = currentUri.value
                if (uriAtStart == null) return@launch
                Log.d(PREVIEW_TAG, "selected uri=$uriAtStart preview rendered from uri manual rotation disabled")
                val result = analyzeImageQuality(context, uriAtStart)
                if (currentUri.value == uriAtStart) scanQuality = result
            }
        }
    }
    LaunchedEffect(isAnalyzing, detectedCount, resultSectionScrollOffset) {
        if (!isAnalyzing && detectedCount > 0 && resultSectionScrollOffset > 0 && !didAutoScrollToResult && !userTouchedScroll) {
            val delta = resultSectionScrollOffset - scrollState.value
            if (delta != 0) {
                programmaticScrollInProgress = true
                scrollState.animateScrollBy(delta.toFloat())
                programmaticScrollInProgress = false
            }
            didAutoScrollToResult = true
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = Dimens.spacing16)
            .verticalScroll(scrollState)
    ) {
        Card(
            onClick = {
                if (selectedImageUri != null && !isAnalyzing) showImagePreview = true
            },
            shape = RoundedCornerShape(Dimens.radiusCard),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .then(
                    if (selectedImageUri != null) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(Dimens.radiusCard))
                    else Modifier
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationSubtle)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (selectedImageUri == null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.68f)
                                .aspectRatio(5f / 4f)
                                .border(
                                    width = Dimens.cardBorderDefault * 2,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(Dimens.radiusSmall)
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing8),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(Dimens.iconEmptyState), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(Dimens.spacing8))
                            Text("No photo selected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(Dimens.spacing8))
                            Text(
                                "Move closer – fill the frame with the Bingo grid",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    SubcomposeAsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                Text("Loading…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        success = { SubcomposeAsyncImageContent() }
                    )
                    if (isAnalyzing) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                                Text("Analyzing ticket...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    if (analysisSummary != null && detectedCount > 0 && !isAnalyzing) {
                        Surface(
                            shape = RoundedCornerShape(Dimens.radiusSmall),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(Dimens.spacing8)
                        ) {
                            Text(
                                text = if (canContinue) "Ready" else "$detectedCount numbers",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
                            )
                        }
                    }
                }
            }
        }
        if (showImagePreview && selectedImageUri != null) {
            Dialog(
                onDismissRequest = { showImagePreview = false },
                properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f)).clickable { showImagePreview = false }
                ) {
                    SubcomposeAsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().align(Alignment.Center))
                    IconButton(onClick = { showImagePreview = false }, modifier = Modifier.align(Alignment.TopEnd).padding(Dimens.spacing8)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        Card(
            shape = RoundedCornerShape(Dimens.radiusCard),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (analysisSummary != null) Modifier.onGloballyPositioned { resultSectionScrollOffset = it.boundsInParent().top.toInt() } else Modifier)
        ) {
            Column(modifier = Modifier.padding(Dimens.spacing12)) {
                if (isAnalyzing) {
                    Text(
                        "Reading ticket…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SCAN RESULT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(Dimens.radiusPill),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = when {
                                    analysisSummary == null && selectedImageUri != null -> "Pending"
                                    analysisSummary == null -> "—"
                                    canContinue -> "Ready"
                                    showTooFewNumbersWarning -> "Too few"
                                    else -> "Incomplete"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing8))
                    if (analysisSummary == null) {
                        Text(
                            if (selectedImageUri == null) {
                                "Take or choose a photo for your ticket."
                            } else {
                                "Your photo is ready. The app reads the ticket from the image automatically."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                        ) {
                            AnalysisSummaryBox(
                                label = "Numbers",
                                value = "$detectedCount",
                                modifier = Modifier.weight(1f)
                            )
                            AnalysisSummaryBox(
                                label = "Grid",
                                value = if (detectedCount > 0) "5×5" else "—",
                                modifier = Modifier.weight(1f)
                            )
                            AnalysisSummaryBox(
                                label = "Card",
                                value = if (canContinue) "Ready" else "—",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacing8))
                        if (isScanError) {
                            Text(
                                analysisSummary.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "Ticket scanned automatically from the image.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacing8))
                            Text(
                                "Review the detected numbers and continue if correct.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        if (selectedImageUri != null && !isAnalyzing) {
            ScanQualityCard(
                title = "SCAN QUALITY",
                status = scanQuality.overallLabel,
                statusColor = when (scanQuality.overallLabel) { "Good" -> Success; "Medium" -> Warning; else -> Error },
                progress = scanQuality.overallScore / 100f,
                lightingLabel = "Lighting${scoreToSuffix(scanQuality.lightingScore)}",
                focusLabel = "Focus${scoreToSuffix(scanQuality.focusScore)}",
                angleLabel = "Angle${scoreToSuffix(scanQuality.angleScore)}",
                lightingColor = scoreToColor(scanQuality.lightingScore),
                focusColor = scoreToColor(scanQuality.focusScore),
                angleColor = scoreToColor(scanQuality.angleScore)
            )
            Spacer(modifier = Modifier.height(Dimens.spacing16))
        }
        Card(
            onClick = onTakePhotoClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
            shape = RoundedCornerShape(Dimens.radiusCard),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationSubtle)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(Dimens.spacing12),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
            ) {
                Surface(
                    shape = RoundedCornerShape(Dimens.radiusSmall),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(Modifier.padding(Dimens.spacing4)) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(Dimens.iconCompact), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("Take Photo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text("Use your camera", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Move closer – fill the frame with the Bingo grid",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.spacing4)
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        if (showTooFewNumbersWarning && selectedImageUri != null && !isAnalyzing) {
            Text(
                text = "Move closer and retake photo",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(Dimens.spacing8))
        }
        if (canContinue && (showIncompleteWarning || showLowConfidenceWarning)) {
            Text(
                text = if (showIncompleteWarning) "Incomplete result. Please review numbers carefully." else "Low confidence. Please review before saving.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(Dimens.spacing8))
        }
        if (!canContinue) {
            if (!showTooFewNumbersWarning) {
                Text(
                    text = if (selectedImageUri == null) {
                        "Take a photo. Continue is available once enough numbers are detected from the image (or from live-scan prefill)."
                    } else {
                        "Continue is available once enough numbers are detected from the image."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacing8))
            }
        }
        AppPrimaryButton(
            text = "Review Detected Ticket",
            onClick = {
                if (!canContinue || continueUsed) return@AppPrimaryButton
                continueUsed = true
                onContinueClick()
            },
            enabled = canContinue && !continueUsed,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(Dimens.iconCompact), tint = MaterialTheme.colorScheme.onPrimary) }
        )
        if (selectedImageUri != null || (!canContinue && analysisSummary != null)) {
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            if (selectedImageUri != null) {
                OutlinedButton(onClick = { analysisJob?.cancel(); onScanAgainClick() }, modifier = Modifier.fillMaxWidth(), enabled = !isAnalyzing) { Text("Scan Again") }
            }
            if (!canContinue && !isAnalyzing && analysisSummary != null) {
                if (selectedImageUri != null) Spacer(modifier = Modifier.height(Dimens.spacing8))
                OutlinedButton(onClick = onRetryAnalysisClick, modifier = Modifier.fillMaxWidth()) { Text("Dismiss error") }
            }
        }
    }
}

/**
 * **Route:** `historyPhotoImport` — document scanner, preview. Continue → `manualEntry` when numbers exist (e.g. live-scan prefill).
 */
@Composable
fun HistoryPhotoImportScreen(
    onBackClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onClearImageClick: () -> Unit = {},
    onScanAgainClick: () -> Unit = {},
    onRetryAnalysisClick: () -> Unit = {},
    onRegisterLeaveHandler: (((() -> Unit) -> Unit)?) -> Unit = {},
    /** Deep-link ticket meta; nav host merges into scan success when present. */
    handoffLosNumber: String? = null,
    handoffSerialNumber: String? = null,
    selectedImageUri: Uri? = null,
    @Suppress("UNUSED_PARAMETER") isAnalyzing: Boolean = false,
    @Suppress("UNUSED_PARAMETER") analysisSummary: String? = null,
    @Suppress("UNUSED_PARAMETER") detectedCount: Int = 0,
    @Suppress("UNUSED_PARAMETER") canContinue: Boolean = false,
    showIncompleteWarning: Boolean = false,
    showLowConfidenceWarning: Boolean = false,
    onContinueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val importVm: ImportTicketViewModel = viewModel()
    val scanResult by importVm.scanResult.collectAsState()
    val isAnalyzingUi = scanResult is ScanResultUiState.Loading
    val successState = scanResult as? ScanResultUiState.Success
    val errorState = scanResult as? ScanResultUiState.Error
    val finalUiGrid = successState?.let { finalUiGridRowMajor(it.numbers) }
    val detectedCountUi = finalUiGrid?.count { it != 0 } ?: 0
    val hasAnyNumber = finalUiGrid?.any { it != 0 } == true
    val tooFewForContinue = successState != null && hasAnyNumber && detectedCountUi < MinDetectedCellsToContinue
    val canContinueUi = hasAnyNumber && !tooFewForContinue
    val analysisSummaryUi = errorState?.message
        ?: successState?.let { "${finalUiGridRowMajor(it.numbers).count { n -> n != 0 }} numbers detected" }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingLeaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val isLocked = isAnalyzingUi || (detectedCountUi > 0) || (selectedImageUri != null)
    fun handleExitRequest(onExit: () -> Unit) {
        if (isLocked) {
            showDiscardDialog = true
            pendingLeaveAction = onExit
        } else {
            onExit()
        }
    }
    DisposableEffect(Unit) {
        onRegisterLeaveHandler { action -> handleExitRequest(action) }
        onDispose { onRegisterLeaveHandler(null) }
    }
    BackHandler {
        if (showDiscardDialog) showDiscardDialog = false else handleExitRequest(onBackClick)
    }
    AppConfirmDialog(
        visible = showDiscardDialog,
        title = "Discard photo and scan?",
        message = "Leaving will discard the selected image and any scan or import progress and results.",
        cancelText = "Continue Editing",
        confirmText = "Discard",
        onCancel = { showDiscardDialog = false; pendingLeaveAction = null },
        onConfirm = {
            onClearImageClick()
            pendingLeaveAction?.invoke()
            pendingLeaveAction = null
            showDiscardDialog = false
        },
        onDismiss = { showDiscardDialog = false; pendingLeaveAction = null }
    )
    Box(modifier = modifier.fillMaxSize()) {
        AppHeaderBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar(title = "Import Ticket", showBack = true, onBackClick = { handleExitRequest(onBackClick) })
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = Dimens.spacing5)
                    .padding(horizontal = Dimens.screenHorizontalPadding)
            ) {
            ImportTicketContent(
                selectedImageUri = selectedImageUri,
                isAnalyzing = isAnalyzingUi,
                onTakePhotoClick = onTakePhotoClick,
                onClearImageClick = onClearImageClick,
                onScanAgainClick = onScanAgainClick,
                onRetryAnalysisClick = onRetryAnalysisClick,
                analysisSummary = analysisSummaryUi,
                detectedCount = detectedCountUi,
                canContinue = canContinueUi,
                showIncompleteWarning = showIncompleteWarning,
                showLowConfidenceWarning = showLowConfidenceWarning,
                showTooFewNumbersWarning = tooFewForContinue,
                isScanError = errorState != null,
                onContinueClick = onContinueClick
            )
            }
        }
    }
}

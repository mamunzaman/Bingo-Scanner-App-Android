package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Success
import com.example.mamunbingoapp.viewmodel.DetectionStatus
import com.example.mamunbingoapp.viewmodel.ImportOcrProgressUiState
import kotlinx.coroutines.delay
import kotlin.math.PI

// Medium green — visible on both light photo backgrounds and dark overlays.
private val CrosshairGreen = Success

/** Smooth scan sweep — gentle ease at turn-around points. */
private val ScanSweepEasing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

/** Grid/corner waypoints — spread across ticket preview (xFrac, yFrac). */
private val ScanFocusWaypoints = arrayOf(
    0.14f to 0.16f,
    0.50f to 0.14f,
    0.86f to 0.18f,
    0.74f to 0.36f,
    0.26f to 0.38f,
    0.58f to 0.54f,
    0.16f to 0.60f,
    0.84f to 0.64f,
)

private data class ScanFocusMarkerSpec(
    val phaseOffset: Float,
    val scale: Float,
    val alphaScale: Float,
    val showRing: Boolean,
)

private fun markerPosition(
    phase: Float,
    phaseOffset: Float,
    width: Float,
    height: Float,
    maxYFrac: Float = 0.72f,
): Offset {
    val p = (phase + phaseOffset) % 1f
    val segCount = ScanFocusWaypoints.size
    val seg = (p * segCount).toInt().coerceIn(0, segCount - 1)
    val segT = p * segCount - seg
    val next = (seg + 1) % segCount
    val xFrac = lerp(ScanFocusWaypoints[seg].first, ScanFocusWaypoints[next].first, segT)
    val yFrac = lerp(ScanFocusWaypoints[seg].second, ScanFocusWaypoints[next].second, segT)
        .coerceAtMost(maxYFrac)
    return Offset(xFrac * width, yFrac * height)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScanFocusMarker(
    center: Offset,
    halfBox: Float,
    arm: Float,
    strokeWidth: Float,
    color: Color,
    showRing: Boolean,
    ringPhase: Float,
) {
    val cc = color
    // L-brackets (corner focus)
    drawLine(cc, Offset(center.x - halfBox, center.y - halfBox), Offset(center.x - halfBox + arm, center.y - halfBox), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x - halfBox, center.y - halfBox), Offset(center.x - halfBox, center.y - halfBox + arm), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x + halfBox - arm, center.y - halfBox), Offset(center.x + halfBox, center.y - halfBox), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x + halfBox, center.y - halfBox), Offset(center.x + halfBox, center.y - halfBox + arm), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x + halfBox, center.y + halfBox - arm), Offset(center.x + halfBox, center.y + halfBox), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x + halfBox, center.y + halfBox), Offset(center.x + halfBox - arm, center.y + halfBox), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x - halfBox + arm, center.y + halfBox), Offset(center.x - halfBox, center.y + halfBox), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x - halfBox, center.y + halfBox), Offset(center.x - halfBox, center.y + halfBox - arm), strokeWidth, cap = StrokeCap.Round)

    val crossLen = halfBox * 0.38f
    drawLine(cc, Offset(center.x - crossLen, center.y), Offset(center.x + crossLen, center.y), strokeWidth, cap = StrokeCap.Round)
    drawLine(cc, Offset(center.x, center.y - crossLen), Offset(center.x, center.y + crossLen), strokeWidth, cap = StrokeCap.Round)

    if (showRing) {
        val ringR = halfBox * 0.78f
        val dashPhase = ringPhase * (2f * PI.toFloat() * ringR)
        drawCircle(
            color = cc.copy(alpha = cc.alpha * 0.30f),
            radius = ringR,
            center = center,
            style = Stroke(
                width = strokeWidth * 0.65f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(6.dp.toPx(), 9.dp.toPx()),
                    dashPhase,
                ),
            ),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCleanScanSweep(
    scanProgress: Float,
    primary: Color,
) {
    val w = size.width
    val h = size.height
    val insetX = w * 0.08f
    val lineY = scanProgress * h
    val beamStroke = 1.75.dp.toPx()
    val softBandH = 20.dp.toPx()

    // One soft faded band behind the beam (no stacked trail lines)
    val bandTop = (lineY - softBandH).coerceAtLeast(0f)
    val bandBottom = lineY.coerceAtMost(h)
    if (bandBottom > bandTop) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    primary.copy(alpha = 0.06f),
                    primary.copy(alpha = 0.03f),
                ),
                startY = bandTop,
                endY = bandBottom,
            ),
            topLeft = Offset(insetX * 0.6f, bandTop),
            size = Size(w - insetX * 1.2f, bandBottom - bandTop),
        )
    }

    // Single clean rounded scan beam
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                CrosshairGreen.copy(alpha = 0.38f),
                primary.copy(alpha = 0.58f),
                CrosshairGreen.copy(alpha = 0.38f),
                Color.Transparent,
            ),
            startX = insetX,
            endX = w - insetX,
        ),
        start = Offset(insetX, lineY),
        end = Offset(w - insetX, lineY),
        strokeWidth = beamStroke,
        cap = StrokeCap.Round,
    )
}

/**
 * Scanner overlay: premium vertical sweep + multi focus markers + scanning rectangle.
 * No text — caller handles labels.
 */
@Composable
fun ScanningAnalysisAnimation(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val inf = rememberInfiniteTransition(label = "scan")

    val scanLineProgress by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4400, easing = ScanSweepEasing),
            RepeatMode.Reverse,
        ),
        label = "scanLine",
    )
    val crosshairPhase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(11_000, easing = ScanSweepEasing),
            RepeatMode.Restart,
        ),
        label = "chPhase",
    )
    val pulseGlow by inf.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val ringPhase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring",
    )

    val focusMarkers = remember {
        listOf(
            ScanFocusMarkerSpec(phaseOffset = 0.80f, scale = 0.46f, alphaScale = 0.20f, showRing = false),
            ScanFocusMarkerSpec(phaseOffset = 0.60f, scale = 0.50f, alphaScale = 0.26f, showRing = false),
            ScanFocusMarkerSpec(phaseOffset = 0.40f, scale = 0.54f, alphaScale = 0.30f, showRing = false),
            ScanFocusMarkerSpec(phaseOffset = 0.20f, scale = 0.58f, alphaScale = 0.36f, showRing = false),
            ScanFocusMarkerSpec(phaseOffset = 0f, scale = 1f, alphaScale = 0.82f, showRing = true),
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawCleanScanSweep(scanLineProgress, primary)

        // Scanning rectangle (top-left zone — ticket grid hint)
        val rectL = w * 0.10f
        val rectT = h * 0.08f
        val rectW = w * 0.48f
        val rectH = h * 0.28f
        val rectCr = CornerRadius(8.dp.toPx())

        drawRoundRect(
            color = primary.copy(alpha = 0.04f),
            topLeft = Offset(rectL, rectT),
            size = Size(rectW, rectH),
            cornerRadius = rectCr,
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.14f),
            topLeft = Offset(rectL, rectT),
            size = Size(rectW, rectH),
            cornerRadius = rectCr,
            style = Stroke(1.dp.toPx()),
        )

        // 5 focus markers — secondaries drawn first, main marker on top
        val baseHalfBox = 44.dp.toPx().coerceAtMost(w * 0.14f)
        val baseArm = 12.dp.toPx()
        val baseSw = 2.dp.toPx()
        focusMarkers.forEach { spec ->
            val center = markerPosition(crosshairPhase, spec.phaseOffset, w, h)
            val halfBox = baseHalfBox * spec.scale
            val arm = baseArm * spec.scale
            val sw = (baseSw * spec.scale).coerceAtLeast(1.dp.toPx())
            val markerAlpha = pulseGlow * spec.alphaScale
            drawScanFocusMarker(
                center = center,
                halfBox = halfBox,
                arm = arm,
                strokeWidth = sw,
                color = CrosshairGreen.copy(alpha = markerAlpha),
                showRing = spec.showRing,
                ringPhase = ringPhase,
            )
        }
    }
}

/**
 * Bottom "Processing Data..." card — matches the code.html scanner design.
 * Place at [Alignment.BottomCenter] inside the overlay.
 */
@Composable
fun ProcessingDataCard(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme

    var progressTarget by remember { mutableFloatStateOf(0.35f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "progressBar",
    )
    LaunchedEffect(Unit) {
        while (progressTarget < 0.95f) {
            delay(700)
            progressTarget = (progressTarget + 0.01f + (Math.random() * 0.025f).toFloat())
                .coerceAtMost(0.95f)
        }
    }

    val pingInf = rememberInfiniteTransition(label = "ping")
    val pingAlpha by pingInf.animateFloat(
        initialValue = 0.95f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "pingAlpha",
    )

    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.93f), RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
            .padding(horizontal = Dimens.spacing24, vertical = Dimens.spacing20),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(pingAlpha)
                        .background(cs.primary, CircleShape),
                )
                Text(
                    text = stringResource(R.string.ocr_processing_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.primary,
                )
            }

            Text(
                text = stringResource(R.string.ocr_processing_body),
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
            )

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(cs.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .background(cs.primary),
                )
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing4),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatItem(label = stringResource(R.string.ocr_stat_nodes), value = "12 / 25")
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(cs.outlineVariant.copy(alpha = 0.45f)),
                )
                StatItem(label = stringResource(R.string.ocr_stat_confidence), value = "98.4%")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontSize = 9.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = cs.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = cs.onSurface,
        )
    }
}

/**
 * Three-dot progress indicator. Kept for API compatibility.
 */
@Composable
fun AnalyzingProgressDots(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val inf = rememberInfiniteTransition(label = "dots")
    val phase by inf.animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "dotsPhase",
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            val slot = phase - i
            val raw = when {
                slot < 0f -> 0f
                slot < 0.5f -> slot / 0.5f
                slot < 1.0f -> (1f - slot) / 0.5f
                else -> 0f
            }
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .alpha(0.30f + raw.coerceIn(0f, 1f) * 0.70f)
                    .background(color, CircleShape),
            )
        }
    }
}

/**
 * Bingo-specific OCR status card.
 * When [progress] is null falls back to animated defaults.
 * When [progress] has real data from the OCR pipeline, shows live stage label,
 * actual detected cell count and LOS / serial detection status.
 */
private enum class MarkerPhase { Queued, Waiting, Scanning, Checking }

private fun resolveMarkerPhase(stage: String): MarkerPhase = when {
    stage.contains("QR", ignoreCase = true) -> MarkerPhase.Queued
    stage.contains("grid", ignoreCase = true) -> MarkerPhase.Waiting
    stage.contains("numbers", ignoreCase = true) ||
        stage.contains("markers", ignoreCase = true) -> MarkerPhase.Scanning
    else -> MarkerPhase.Checking
}

@Composable
private fun markerPhaseLabel(phase: MarkerPhase): String = stringResource(
    when (phase) {
        MarkerPhase.Queued -> R.string.ocr_marker_queued
        MarkerPhase.Waiting -> R.string.ocr_marker_waiting
        MarkerPhase.Scanning -> R.string.ocr_marker_scanning
        MarkerPhase.Checking -> R.string.ocr_marker_checking
    },
)

@Composable
fun BingoOcrStatusCard(
    progress: ImportOcrProgressUiState? = null,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    // Stage label — real when available, else rotates through defaults
    val defaultTitleIds = listOf(
        R.string.ocr_stage_reading_numbers,
        R.string.ocr_stage_detecting_grid,
        R.string.ocr_stage_checking_markers,
    )
    var titleIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            titleIndex = (titleIndex + 1) % defaultTitleIds.size
        }
    }
    val stageLabel = progress?.stageLabel ?: stringResource(defaultTitleIds[titleIndex])

    // Progress bar — real ratio when cells detected, else animated placeholder
    val hasRealCells = (progress?.detectedGridCells ?: 0) > 0
    var fakeProgressTarget by remember { mutableFloatStateOf(0.22f) }
    LaunchedEffect(hasRealCells) {
        if (!hasRealCells) {
            while (fakeProgressTarget < 0.88f) {
                delay(900)
                fakeProgressTarget = (fakeProgressTarget + 0.01f + (Math.random() * 0.03f).toFloat())
                    .coerceAtMost(0.88f)
            }
        }
    }
    val rawTarget = if (hasRealCells) {
        (progress!!.detectedGridCells.toFloat() / progress.totalGridCells.toFloat()).coerceIn(0f, 1f)
    } else fakeProgressTarget
    val animatedProgress by animateFloatAsState(
        targetValue = rawTarget,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "binoProgress",
    )

    // Ping dot
    val pingInf = rememberInfiniteTransition(label = "pingB")
    val pingAlpha by pingInf.animateFloat(
        initialValue = 0.95f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "pingBAlpha",
    )

    // Animated placeholder grid counter: cycles 6→12→18→24 while real count is unknown
    val animGridSteps = remember { listOf(6, 12, 18, 24) }
    var animGridIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(700)
            animGridIndex = (animGridIndex + 1) % animGridSteps.size
        }
    }
    val gridCellsText = if (hasRealCells) "${progress!!.detectedGridCells} / ${progress.totalGridCells}"
        else "${animGridSteps[animGridIndex]} / 25"

    // Stage-aware marker status — advances text/colour with each OCR stage until real values arrive
    val currentStage = progress?.stageLabel ?: ""
    val markerPhase = resolveMarkerPhase(currentStage)
    val pendingMarkerLabel = markerPhaseLabel(markerPhase)
    val foundLabel = stringResource(R.string.ocr_marker_found)
    val notFoundLabel = stringResource(R.string.ocr_marker_not_found)
    val losText = when (progress?.losStatus) {
        DetectionStatus.Found -> foundLabel
        DetectionStatus.NotFound -> notFoundLabel
        else -> pendingMarkerLabel
    }
    val serialText = when (progress?.serialStatus) {
        DetectionStatus.Found -> foundLabel
        DetectionStatus.NotFound -> notFoundLabel
        else -> pendingMarkerLabel
    }
    val losColor = when {
        progress?.losStatus == DetectionStatus.Found -> cs.primary
        progress?.losStatus == DetectionStatus.NotFound -> cs.onSurfaceVariant
        markerPhase == MarkerPhase.Scanning -> cs.primary
        markerPhase == MarkerPhase.Waiting -> cs.onSurfaceVariant
        else -> cs.outline
    }
    val serialColor = when {
        progress?.serialStatus == DetectionStatus.Found -> cs.primary
        progress?.serialStatus == DetectionStatus.NotFound -> cs.onSurfaceVariant
        markerPhase == MarkerPhase.Scanning -> cs.primary
        markerPhase == MarkerPhase.Waiting -> cs.onSurfaceVariant
        else -> cs.outline
    }
    val knownStatuses = listOf(DetectionStatus.Found, DetectionStatus.NotFound)
    val losShowPulse = markerPhase == MarkerPhase.Scanning && progress?.losStatus !in knownStatuses
    val serialShowPulse = markerPhase == MarkerPhase.Scanning && progress?.serialStatus !in knownStatuses

    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.96f), RoundedCornerShape(28.dp))
            .border(1.dp, cs.outline.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
            .padding(horizontal = Dimens.spacing24, vertical = Dimens.spacing20),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(pingAlpha)
                        .background(cs.primary, CircleShape),
                )
                Text(
                    text = stageLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.primary,
                )
            }
            Text(
                text = stringResource(R.string.ocr_bingo_status_body),
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
            )
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(cs.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(cs.primary, cs.primary.copy(alpha = 0.72f)),
                            )
                        ),
                )
            }
            // Stats row: GRID CELLS | LOS | SERIAL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing4),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BingoStatItem(label = stringResource(R.string.ocr_stat_grid_cells), value = gridCellsText, valueColor = cs.primary)
                Box(Modifier.height(28.dp).width(1.dp).background(cs.outlineVariant.copy(alpha = 0.45f)))
                BingoStatItem(label = stringResource(R.string.ocr_stat_los_nr), value = losText, valueColor = losColor, showPulse = losShowPulse)
                Box(Modifier.height(28.dp).width(1.dp).background(cs.outlineVariant.copy(alpha = 0.45f)))
                BingoStatItem(label = stringResource(R.string.ocr_stat_serie), value = serialText, valueColor = serialColor, showPulse = serialShowPulse)
            }
        }
    }
}

@Composable
private fun BingoStatItem(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    showPulse: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    val pulseInf = rememberInfiniteTransition(label = "pulse_$label")
    val pulseAlpha by pulseInf.animateFloat(
        initialValue = 1f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha_$label",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontSize = 8.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = cs.outline,
        )
        if (showPulse) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .alpha(pulseAlpha)
                        .background(valueColor, CircleShape),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
        }
    }
}

/** Kept for API compatibility — delegates to [ScanningAnalysisAnimation]. */
@Composable
fun RadarSweepScanner(
    containerWidth: Dp,
    containerHeight: Dp,
    modifier: Modifier = Modifier,
) {
    ScanningAnalysisAnimation(modifier = modifier)
}

package com.example.mamunbingoapp.ui.components

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.viewmodel.DetectionStatus
import com.example.mamunbingoapp.viewmodel.ImportOcrProgressUiState
import kotlinx.coroutines.delay
import kotlin.math.PI

// Medium green — visible on both light photo backgrounds and dark overlays.
private val CrosshairGreen = Color(0xFF2E9B5E)

/**
 * Scanner overlay: vertical scan-line sweep + floating crosshair + scanning rectangle.
 * No text — caller handles labels.
 */
@Composable
fun ScanningAnalysisAnimation(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val inf = rememberInfiniteTransition(label = "scan")

    val scanLineProgress by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "scanLine",
    )
    val crosshairPhase by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10_000, easing = LinearEasing), RepeatMode.Restart),
        label = "chPhase",
    )
    val pulseGlow by inf.animateFloat(
        initialValue = 0.3f, targetValue = 0.88f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val ringPhase by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring",
    )
    val rectLinePulse by inf.animateFloat(
        initialValue = 0.15f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rectLine",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // ── 1. Vertical scan line sweep ────────────────────────────────────────
        val scanBarH = 4.dp.toPx()
        val glowH = 22.dp.toPx()
        val scanY = scanLineProgress * (h + glowH * 2f) - glowH
        val scanAlpha = when {
            scanLineProgress < 0.08f -> (scanLineProgress / 0.08f)
            scanLineProgress > 0.90f -> ((1f - scanLineProgress) / 0.10f)
            else -> 1f
        }.coerceIn(0f, 1f)

        val glowTop = (scanY - glowH).coerceAtLeast(0f)
        val glowBottom = (scanY + glowH * 0.3f).coerceIn(glowTop, h)
        if (glowBottom > glowTop) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, primary.copy(alpha = 0.16f * scanAlpha)),
                    startY = glowTop,
                    endY = glowBottom,
                ),
                topLeft = Offset(0f, glowTop),
                size = Size(w, glowBottom - glowTop),
            )
        }
        val barTop = (scanY - scanBarH / 2f).coerceIn(0f, h - scanBarH)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    primary.copy(alpha = 0.68f * scanAlpha),
                    primary.copy(alpha = 0.68f * scanAlpha),
                    Color.Transparent,
                ),
                startX = 0f, endX = w,
            ),
            topLeft = Offset(0f, barTop),
            size = Size(w, scanBarH),
        )

        // ── 2. Scanning rectangle (top-left zone) ──────────────────────────────
        val rectL = w * 0.10f
        val rectT = h * 0.08f
        val rectW = w * 0.48f
        val rectH = h * 0.28f
        val rectCr = CornerRadius(8.dp.toPx())

        drawRoundRect(
            color = primary.copy(alpha = 0.06f),
            topLeft = Offset(rectL, rectT),
            size = Size(rectW, rectH),
            cornerRadius = rectCr,
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.24f),
            topLeft = Offset(rectL, rectT),
            size = Size(rectW, rectH),
            cornerRadius = rectCr,
            style = Stroke(1.dp.toPx()),
        )
        drawLine(
            color = primary.copy(alpha = rectLinePulse),
            start = Offset(rectL + 14.dp.toPx(), rectT + rectH / 2f),
            end = Offset(rectL + rectW - 14.dp.toPx(), rectT + rectH / 2f),
            strokeWidth = 1.dp.toPx(),
        )

        // ── 3. Floating crosshair ──────────────────────────────────────────────
        // Centre-positions as (xFrac, yFrac) of container — matches HTML float-crosshair keyframes.
        val wps = arrayOf(
            0.25f to 0.22f,
            0.72f to 0.30f,
            0.42f to 0.65f,
            0.80f to 0.78f,
        )
        val seg = (crosshairPhase * 4).toInt().coerceIn(0, 3)
        val segT = crosshairPhase * 4f - seg
        val next = (seg + 1) % 4
        val chX = lerp(wps[seg].first, wps[next].first, segT) * w
        val chY = lerp(wps[seg].second, wps[next].second, segT) * h

        val halfBox = 56.dp.toPx()
        val arm = 14.dp.toPx()
        val sw = 2.dp.toPx()
        val cc = CrosshairGreen.copy(alpha = pulseGlow)

        // L-brackets
        drawLine(cc, Offset(chX - halfBox, chY - halfBox), Offset(chX - halfBox + arm, chY - halfBox), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX - halfBox, chY - halfBox), Offset(chX - halfBox, chY - halfBox + arm), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX + halfBox - arm, chY - halfBox), Offset(chX + halfBox, chY - halfBox), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX + halfBox, chY - halfBox), Offset(chX + halfBox, chY - halfBox + arm), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX + halfBox, chY + halfBox - arm), Offset(chX + halfBox, chY + halfBox), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX + halfBox, chY + halfBox), Offset(chX + halfBox - arm, chY + halfBox), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX - halfBox + arm, chY + halfBox), Offset(chX - halfBox, chY + halfBox), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX - halfBox, chY + halfBox), Offset(chX - halfBox, chY + halfBox - arm), sw, cap = StrokeCap.Round)

        // Centre cross
        val crossLen = 22.dp.toPx()
        drawLine(cc, Offset(chX - crossLen, chY), Offset(chX + crossLen, chY), sw, cap = StrokeCap.Round)
        drawLine(cc, Offset(chX, chY - crossLen), Offset(chX, chY + crossLen), sw, cap = StrokeCap.Round)

        // Rotating dashed ring
        val ringR = halfBox * 0.80f
        val dashPhase = ringPhase * (2f * PI.toFloat() * ringR)
        drawCircle(
            color = CrosshairGreen.copy(alpha = pulseGlow * 0.42f),
            radius = ringR,
            center = Offset(chX, chY),
            style = Stroke(
                width = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(8.dp.toPx(), 10.dp.toPx()),
                    dashPhase,
                ),
            ),
        )
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
                    text = "Processing Data...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.primary,
                )
            }

            Text(
                text = "Our advanced OCR system is identifying your numbers and serial markers. Please keep the camera steady.",
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
                StatItem(label = "NODES", value = "12 / 25")
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(cs.outlineVariant.copy(alpha = 0.45f)),
                )
                StatItem(label = "CONFIDENCE", value = "98.4%")
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
@Composable
fun BingoOcrStatusCard(
    progress: ImportOcrProgressUiState? = null,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    // Stage label — real when available, else rotates through defaults
    val defaultTitles = listOf(
        "Reading bingo numbers…",
        "Detecting ticket grid…",
        "Checking serial and LOS…",
    )
    var titleIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            titleIndex = (titleIndex + 1) % defaultTitles.size
        }
    }
    val stageLabel = progress?.stageLabel ?: defaultTitles[titleIndex]

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
    val markerStatusText = when {
        currentStage.contains("QR", ignoreCase = true) -> "Queued"
        currentStage.contains("grid", ignoreCase = true) -> "Waiting"
        currentStage.contains("numbers", ignoreCase = true) ||
            currentStage.contains("markers", ignoreCase = true) -> "Scanning"
        else -> "Checking"
    }
    val losText = when (progress?.losStatus) {
        DetectionStatus.Found -> "Found"
        DetectionStatus.NotFound -> "Not found"
        else -> markerStatusText
    }
    val serialText = when (progress?.serialStatus) {
        DetectionStatus.Found -> "Found"
        DetectionStatus.NotFound -> "Not found"
        else -> markerStatusText
    }
    val losColor = when {
        progress?.losStatus == DetectionStatus.Found -> cs.primary
        progress?.losStatus == DetectionStatus.NotFound -> cs.onSurfaceVariant
        markerStatusText == "Scanning" -> cs.primary
        markerStatusText == "Waiting" -> cs.onSurfaceVariant
        else -> cs.outline  // Queued / Checking
    }
    val serialColor = when {
        progress?.serialStatus == DetectionStatus.Found -> cs.primary
        progress?.serialStatus == DetectionStatus.NotFound -> cs.onSurfaceVariant
        markerStatusText == "Scanning" -> cs.primary
        markerStatusText == "Waiting" -> cs.onSurfaceVariant
        else -> cs.outline
    }
    val knownStatuses = listOf(DetectionStatus.Found, DetectionStatus.NotFound)
    val losShowPulse = markerStatusText == "Scanning" && progress?.losStatus !in knownStatuses
    val serialShowPulse = markerStatusText == "Scanning" && progress?.serialStatus !in knownStatuses

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
                text = "We're scanning the 5×5 bingo grid, ticket series, LOS number and serial markers. Please keep this screen open.",
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
                BingoStatItem(label = "GRID CELLS", value = gridCellsText, valueColor = cs.primary)
                Box(Modifier.height(28.dp).width(1.dp).background(cs.outlineVariant.copy(alpha = 0.45f)))
                BingoStatItem(label = "LOS-NR", value = losText, valueColor = losColor, showPulse = losShowPulse)
                Box(Modifier.height(28.dp).width(1.dp).background(cs.outlineVariant.copy(alpha = 0.45f)))
                BingoStatItem(label = "SERIE", value = serialText, valueColor = serialColor, showPulse = serialShowPulse)
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

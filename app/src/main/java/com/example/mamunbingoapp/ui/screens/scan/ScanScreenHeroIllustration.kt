package com.example.mamunbingoapp.ui.screens.scan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.core.interaction.AppMotion
import com.example.mamunbingoapp.ui.core.interaction.rememberAppAnimationsEnabled

private val ScanHeroTicketWidth = 108.dp
private val ScanHeroTicketHeight = 134.dp
private val ScanHeroFrameSize = 168.dp
private val ScanHeroSymbolSize = 34.dp
private val ScanHeroBracketArm = 22.dp
private val ScanHeroBracketStroke = 3.5.dp
val ScanHeroBottomCurveHeight = 28.dp

@Composable
fun rememberScanAnimationsEnabled(): Boolean = rememberAppAnimationsEnabled()

@Composable
private fun rememberScanLineProgress(): androidx.compose.runtime.State<Float> {
    val infinite = rememberInfiniteTransition(label = "ticketScanHero")
    return infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(AppMotion.ScanLineMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scanLine",
    )
}

@Composable
fun ScanHeroBottomCurve(
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.surface,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(ScanHeroBottomCurveHeight),
    ) {
        val bulge = size.height * 0.9f
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, 0f)
            quadraticBezierTo(
                size.width * 0.5f,
                -bulge,
                0f,
                0f,
            )
            close()
        }
        drawPath(path, fillColor)
    }
}

@Composable
fun TicketScanHero(
    modifier: Modifier = Modifier,
    contentScale: Float = 1f,
    maxIllustrationHeight: Dp = Dp.Unspecified,
    animationsEnabled: Boolean = true,
) {
    ScanScreenHeroIllustration(
        modifier = modifier,
        contentScale = contentScale,
        maxIllustrationHeight = maxIllustrationHeight,
        animationsEnabled = animationsEnabled,
    )
}

@Composable
fun ScanScreenHeroIllustration(
    modifier: Modifier = Modifier,
    contentScale: Float = 1f,
    maxIllustrationHeight: Dp = Dp.Unspecified,
    animationsEnabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val ticketFill = if (isDark) colors.surfaceVariant else colors.surface
    val notchFill = colors.primaryContainer
    val scale = contentScale.coerceIn(0.78f, 1f)
    val heightModifier = if (maxIllustrationHeight != Dp.Unspecified) {
        Modifier.heightIn(max = maxIllustrationHeight)
    } else {
        Modifier
    }

    key(animationsEnabled) {
        val resolvedScanProgress = if (animationsEnabled) {
            val scanProgress by rememberScanLineProgress()
            scanProgress
        } else {
            AppMotion.ScanLineFrozenProgress
        }
        TicketScanHeroContent(
            modifier = modifier.then(heightModifier).clearAndSetSemantics { },
            primary = colors.primary,
            ticketFill = ticketFill,
            notchFill = notchFill,
            outline = colors.primary.copy(alpha = 0.42f),
            scale = scale,
            resolvedScanProgress = resolvedScanProgress,
            showScanBeam = animationsEnabled,
        )
    }
}

@Composable
private fun TicketScanHeroContent(
    modifier: Modifier,
    primary: Color,
    ticketFill: Color,
    notchFill: Color,
    outline: Color,
    scale: Float,
    resolvedScanProgress: Float,
    showScanBeam: Boolean,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val frame = minOf(maxWidth * 0.52f, maxHeight * 0.92f, ScanHeroFrameSize * scale)
            .coerceAtLeast(150.dp * scale)
        val ticketW = (ScanHeroTicketWidth * scale).coerceIn(96.dp, 116.dp)
        val ticketH = (ScanHeroTicketHeight * scale).coerceIn(124.dp, 144.dp)

        Box(
            modifier = Modifier.size(frame),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.matchParentSize()) {
                val ticketWidthPx = ticketW.toPx()
                val ticketHeightPx = ticketH.toPx()
                val left = (size.width - ticketWidthPx) / 2f
                val top = (size.height - ticketHeightPx) / 2f
                val right = left + ticketWidthPx
                val bottom = top + ticketHeightPx
                val corner = Dimens.radiusSmall.toPx()
                val stroke = ScanHeroBracketStroke.toPx()
                val bracketGap = 8.dp.toPx()

                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width / 2f, bottom),
                        radius = ticketWidthPx * 0.95f,
                    ),
                    topLeft = Offset(left - 8.dp.toPx(), top + ticketHeightPx * 0.35f),
                    size = Size(ticketWidthPx + 16.dp.toPx(), ticketHeightPx * 0.72f),
                    cornerRadius = CornerRadius(corner, corner),
                )

                drawRoundRect(
                    color = ticketFill,
                    topLeft = Offset(left, top),
                    size = Size(ticketWidthPx, ticketHeightPx),
                    cornerRadius = CornerRadius(corner, corner),
                )
                drawRoundRect(
                    color = outline,
                    topLeft = Offset(left, top),
                    size = Size(ticketWidthPx, ticketHeightPx),
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = 2.dp.toPx()),
                )

                val headerH = ticketHeightPx * 0.16f
                drawRoundRect(
                    color = primary.copy(alpha = 0.16f),
                    topLeft = Offset(left, top),
                    size = Size(ticketWidthPx, headerH + corner),
                    cornerRadius = CornerRadius(corner, corner),
                )
                drawRect(
                    color = ticketFill,
                    topLeft = Offset(left, top + headerH),
                    size = Size(ticketWidthPx, corner),
                )

                val midY = top + ticketHeightPx / 2f
                val notchR = 7.dp.toPx()
                drawCircle(notchFill, notchR, Offset(left, midY))
                drawCircle(notchFill, notchR, Offset(right, midY))

                val lineLeft = left + ticketWidthPx * 0.18f
                val lineRight = right - ticketWidthPx * 0.18f
                val lineY = top + ticketHeightPx * 0.78f
                drawLine(primary.copy(alpha = 0.28f), Offset(lineLeft, lineY), Offset(lineRight, lineY), 2.dp.toPx(), StrokeCap.Round)
                drawLine(
                    primary.copy(alpha = 0.16f),
                    Offset(lineLeft + 8.dp.toPx(), lineY + 7.dp.toPx()),
                    Offset(lineRight - 8.dp.toPx(), lineY + 7.dp.toPx()),
                    2.dp.toPx(),
                    StrokeCap.Round,
                )

                val symbol = ScanHeroSymbolSize.toPx()
                val cx = size.width / 2f
                val cy = top + ticketHeightPx * 0.46f
                val half = symbol / 2f
                val arm = symbol * 0.28f
                drawCornerBrackets(
                    left = cx - half,
                    top = cy - half,
                    right = cx + half,
                    bottom = cy + half,
                    arm = arm,
                    stroke = stroke,
                    color = primary,
                )
                val barW = symbol * 0.42f
                drawLine(primary, Offset(cx - barW, cy - 4.dp.toPx()), Offset(cx + barW, cy - 4.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
                drawLine(primary, Offset(cx - barW * 0.7f, cy + 4.dp.toPx()), Offset(cx + barW * 0.7f, cy + 4.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)

                if (showScanBeam) {
                    val edgeFade = when {
                        resolvedScanProgress < 0.08f -> resolvedScanProgress / 0.08f
                        resolvedScanProgress > 0.92f -> (1f - resolvedScanProgress) / 0.08f
                        else -> 1f
                    }
                    val scanY = top + ticketHeightPx * 0.12f +
                        ticketHeightPx * 0.76f * resolvedScanProgress
                    drawScanBeam(
                        y = scanY,
                        left = left + 10.dp.toPx(),
                        right = right - 10.dp.toPx(),
                        primary = primary,
                        fade = edgeFade,
                    )
                } else {
                    drawRoundRect(
                        color = primary.copy(alpha = 0.08f),
                        topLeft = Offset(left + 10.dp.toPx(), top + ticketHeightPx * 0.38f),
                        size = Size(ticketWidthPx - 20.dp.toPx(), 10.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    )
                }

                drawCornerBrackets(
                    left = left - bracketGap,
                    top = top - bracketGap,
                    right = right + bracketGap,
                    bottom = bottom + bracketGap,
                    arm = ScanHeroBracketArm.toPx(),
                    stroke = stroke,
                    color = primary,
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScanBeam(
    y: Float,
    left: Float,
    right: Float,
    primary: Color,
    fade: Float,
) {
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                primary.copy(alpha = 0.18f * fade),
                primary.copy(alpha = 0.55f * fade),
                primary.copy(alpha = 0.18f * fade),
                Color.Transparent,
            ),
            startX = left,
            endX = right,
        ),
        start = Offset(left, y),
        end = Offset(right, y),
        strokeWidth = 10.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = primary.copy(alpha = 0.92f * fade),
        start = Offset(left + 6.dp.toPx(), y),
        end = Offset(right - 6.dp.toPx(), y),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerBrackets(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    arm: Float,
    stroke: Float,
    color: Color,
) {
    val cap = StrokeCap.Round
    drawLine(color, Offset(left, top + arm), Offset(left, top), stroke, cap)
    drawLine(color, Offset(left, top), Offset(left + arm, top), stroke, cap)
    drawLine(color, Offset(right - arm, top), Offset(right, top), stroke, cap)
    drawLine(color, Offset(right, top), Offset(right, top + arm), stroke, cap)
    drawLine(color, Offset(right, bottom - arm), Offset(right, bottom), stroke, cap)
    drawLine(color, Offset(right, bottom), Offset(right - arm, bottom), stroke, cap)
    drawLine(color, Offset(left + arm, bottom), Offset(left, bottom), stroke, cap)
    drawLine(color, Offset(left, bottom), Offset(left, bottom - arm), stroke, cap)
}

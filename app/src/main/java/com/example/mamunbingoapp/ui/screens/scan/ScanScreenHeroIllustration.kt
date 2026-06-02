package com.example.mamunbingoapp.ui.screens.scan

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens

private val ScanHeroCardMaxSize = 176.dp
private val ScanHeroOuterBracketInset = 18.dp
private val ScanHeroInnerBracketInset = 16.dp
private val ScanHeroBracketArm = 22.dp
private val ScanHeroBracketStroke = 2.5.dp
private val ScanHeroOuterBracketStroke = 3.dp
val ScanHeroBottomCurveHeight = 28.dp
private val ScanLineSweepMs = 4800
private const val ScanLineFrozenProgress = 0.42f

@Composable
private fun rememberScanLineProgress(): androidx.compose.runtime.State<Float> {
    val infinite = rememberInfiniteTransition(label = "scanHero")
    return infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(ScanLineSweepMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanLine",
    )
}

@Composable
fun rememberScanAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }
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
fun ScanScreenHeroIllustration(
    modifier: Modifier = Modifier,
    contentScale: Float = 1f,
    maxIllustrationHeight: Dp = Dp.Unspecified,
    animationsEnabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val primary = colors.primary
    val gridColor = primary.copy(alpha = 0.035f)
    val bracketColor = primary.copy(alpha = 0.72f)
    val cardShape = RoundedCornerShape(Dimens.radiusLarge)
    val scale = contentScale.coerceIn(0.52f, 1f)

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
            ScanLineFrozenProgress
        }

        ScanScreenHeroIllustrationContent(
            modifier = modifier.then(heightModifier),
            primary = primary,
            gridColor = gridColor,
            bracketColor = bracketColor,
            cardShape = cardShape,
            scale = scale,
            resolvedScanProgress = resolvedScanProgress,
        )
    }
}

@Composable
private fun ScanScreenHeroIllustrationContent(
    modifier: Modifier,
    primary: Color,
    gridColor: Color,
    bracketColor: Color,
    cardShape: RoundedCornerShape,
    scale: Float,
    resolvedScanProgress: Float,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val cardSize = minOf(
            maxWidth * 0.42f * scale,
            maxHeight * 0.82f,
            ScanHeroCardMaxSize * scale,
        ).coerceAtLeast(96.dp * scale)
        val bracketInset = ScanHeroOuterBracketInset * scale
        val innerInset = ScanHeroInnerBracketInset * scale
        val bracketArm = ScanHeroBracketArm * scale
        val bracketStroke = ScanHeroBracketStroke * scale
        val outerStroke = ScanHeroOuterBracketStroke * scale
        val iconSize = cardSize * 0.22f
        val canvasPad = 14.dp * scale

        Canvas(
            modifier = Modifier.size(cardSize + bracketInset * 2 + canvasPad),
        ) {
            val cardSizePx = cardSize.toPx()
            val cardLeft = (size.width - cardSizePx) / 2f
            val cardTop = (size.height - cardSizePx) / 2f
            val cardRight = cardLeft + cardSizePx
            val cardBottom = cardTop + cardSizePx
            val cornerPx = Dimens.radiusLarge.toPx()
            val outerInset = bracketInset.toPx()
            val outerArm = bracketArm.toPx() + 6.dp.toPx() * scale

            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.14f),
                        primary.copy(alpha = 0.05f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width / 2f, cardBottom + 4.dp.toPx()),
                    radius = cardSizePx * 0.88f,
                ),
                topLeft = Offset(cardLeft - 16.dp.toPx(), cardTop + cardSizePx * 0.40f),
                size = Size(cardSizePx + 32.dp.toPx(), cardSizePx * 0.55f),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
            )

            drawCornerBrackets(
                left = cardLeft - outerInset,
                top = cardTop - outerInset,
                right = cardRight + outerInset,
                bottom = cardBottom + outerInset,
                arm = outerArm,
                stroke = outerStroke.toPx(),
                color = bracketColor,
            )
        }

        Box(
            modifier = Modifier
                .size(cardSize)
                .shadow(Dimens.cardElevationSubtle, cardShape, clip = false)
                .clip(cardShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val gridStep = size.width / 6f
                val edge = 10.dp.toPx() * scale
                for (i in 1..5) {
                    val x = gridStep * i
                    drawLine(
                        color = gridColor,
                        start = Offset(x, edge),
                        end = Offset(x, size.height - edge),
                        strokeWidth = 0.55f,
                    )
                }
                for (i in 1..5) {
                    val y = gridStep * i
                    drawLine(
                        color = gridColor,
                        start = Offset(edge, y),
                        end = Offset(size.width - edge, y),
                        strokeWidth = 0.55f,
                    )
                }

                val innerInsetPx = innerInset.toPx()
                val innerArmPx = bracketArm.toPx()
                drawCornerBrackets(
                    left = innerInsetPx,
                    top = innerInsetPx,
                    right = size.width - innerInsetPx,
                    bottom = size.height - innerInsetPx,
                    arm = innerArmPx,
                    stroke = bracketStroke.toPx(),
                    color = bracketColor.copy(alpha = 0.62f),
                )

                val scanInset = innerInsetPx + 8.dp.toPx() * scale
                val scanTop = scanInset
                val scanBottom = size.height - scanInset
                val scanY = scanTop + (scanBottom - scanTop) * resolvedScanProgress
                val scanLeft = scanInset
                val scanRight = size.width - scanInset

                drawSoftScanLine(
                    y = scanY,
                    left = scanLeft,
                    right = scanRight,
                    stroke = (1.25.dp * scale).toPx(),
                    primary = primary,
                )
            }
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSoftScanLine(
    y: Float,
    left: Float,
    right: Float,
    stroke: Float,
    primary: Color,
) {
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                primary.copy(alpha = 0.16f),
                primary.copy(alpha = 0.34f),
                primary.copy(alpha = 0.16f),
                Color.Transparent,
            ),
            startX = left,
            endX = right,
        ),
        start = Offset(left, y),
        end = Offset(right, y),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
}

private fun Dp.coerceAtLeast(minimumValue: Dp): Dp =
    if (this >= minimumValue) this else minimumValue

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

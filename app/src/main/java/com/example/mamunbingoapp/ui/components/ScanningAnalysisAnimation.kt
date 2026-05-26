package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp

private val ScanGreen = Color(0xFF4ADE80)
private val ScanGreenDark = Color(0xFF2F7F33)
private val ScanGreenMid = Color(0xFF22C55E)

/**
 * Premium scanner animation overlay. Displays:
 * - Radially-faded green grid mesh
 * - Patrolling crosshair with rotating dashed ring and corner brackets
 * - Pulsing acquisition nodes (small squares)
 * - Drifting data-stream particles
 * - Centered text label below
 */
/**
 * Pure canvas scanner animation. Contains no text — the caller is responsible for status labels.
 */
@Composable
fun ScanningAnalysisAnimation(
    modifier: Modifier = Modifier,
) {
    val inf = rememberInfiniteTransition(label = "scan")

    // Crosshair patrol: 0→1 around a rectangle path
    val patrol by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6_000, easing = LinearEasing)),
        label = "patrol",
    )

    // Rotating dashed ring
    val ringRotation by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10_000, easing = LinearEasing)),
        label = "ring",
    )

    // Node A
    val nodeA by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2_000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "nodeA",
    )
    // Node B offset
    val nodeB by inf.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 2_000
                0f at 700
                1f at 1_400
                0f at 2_000
            },
        ),
        label = "nodeB",
    )
    // Node C offset
    val nodeC by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 2_500
                0f at 300
                1f at 1_300
                0f at 2_500
            },
        ),
        label = "nodeC",
    )

    // Particle 1: drifts downward
    val particle1Y by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3_000, easing = LinearEasing)),
        label = "p1y",
    )
    val particle1Alpha by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = LinearEasing), RepeatMode.Reverse,
        ),
        label = "p1a",
    )

    // Particle 2: drifts upward
    val particle2Y by inf.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3_500, easing = LinearEasing)),
        label = "p2y",
    )
    val particle2Alpha by inf.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1_000, easing = LinearEasing), RepeatMode.Reverse,
        ),
        label = "p2a",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height

                val gridL = w * 0.17f
                val gridR = w * 0.83f
                val gridT = h * 0.20f
                val gridB = h * 0.80f
                val gridW = gridR - gridL
                val gridH = gridB - gridT

                val cx0 = gridL + gridW * 0.08f
                val cx1 = gridR - gridW * 0.08f
                val cy0 = gridT + gridH * 0.10f
                val cy1 = gridB - gridH * 0.10f
                val perim = 2 * ((cx1 - cx0) + (cy1 - cy0))

                onDrawBehind {
                    drawScanGrid(gridL, gridR, gridT, gridB)
                    val chPos = crosshairPosition(patrol, cx0, cy0, cx1, cy1, perim)
                    drawCrosshair(chPos, ringRotation)
                    drawAcquisitionNodes(gridL, gridT, gridW, gridH, nodeA, nodeB, nodeC)
                    drawParticles(
                        gridL, gridR, gridT, gridB,
                        particle1Y, particle1Alpha * 0.8f,
                        particle2Y, particle2Alpha * 0.8f,
                    )
                }
            },
    )
}

/** Returns the (x, y) crosshair centre for normalised patrol fraction [t] (0..1) around rectangle. */
private fun crosshairPosition(
    t: Float,
    x0: Float, y0: Float,
    x1: Float, y1: Float,
    perimeter: Float,
): Offset {
    val dist = (t * perimeter).coerceIn(0f, perimeter)
    val top = x1 - x0
    val right = top + (y1 - y0)
    val bottom = right + (x1 - x0)
    return when {
        dist <= top -> Offset(x0 + dist, y0)
        dist <= right -> Offset(x1, y0 + (dist - top))
        dist <= bottom -> Offset(x1 - (dist - right), y1)
        else -> Offset(x0, y1 - (dist - bottom))
    }
}

private fun DrawScope.drawScanGrid(
    gridL: Float, gridR: Float, gridT: Float, gridB: Float,
) {
    val gridW = gridR - gridL
    val gridH = gridB - gridT
    val strokeColor = ScanGreenDark.copy(alpha = 0.18f)
    val stroke = Stroke(0.6.dp.toPx())

    // Horizontal lines (3 interior)
    for (i in 1..3) {
        val y = gridT + gridH * (i / 4f)
        drawLine(strokeColor, Offset(gridL, y), Offset(gridR, y), strokeWidth = stroke.width)
    }
    // Vertical lines (3 interior)
    for (i in 1..3) {
        val x = gridL + gridW * (i / 4f)
        drawLine(strokeColor, Offset(x, gridT), Offset(x, gridB), strokeWidth = stroke.width)
    }
}

private fun DrawScope.drawCrosshair(center: Offset, ringRotDeg: Float) {
    val arm = 13.dp.toPx()
    val bracketReach = 20.dp.toPx()
    val bracketInset = 12.dp.toPx()
    val ringRadius = 20.dp.toPx()

    // Center cross
    val crossStroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
    val crossColor = ScanGreen.copy(alpha = 0.90f)
    drawLine(crossColor, center.copy(x = center.x - arm), center.copy(x = center.x + arm), crossStroke.width)
    drawLine(crossColor, center.copy(y = center.y - arm * 0.8f), center.copy(y = center.y + arm * 0.8f), crossStroke.width)

    // Rotating dashed ring
    withTransform({ rotate(ringRotDeg, center) }) {
        drawCircle(
            color = ScanGreenDark.copy(alpha = 0.55f),
            radius = ringRadius,
            center = center,
            style = Stroke(
                width = 0.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx()), 0f),
            ),
        )
    }

    // Corner brackets
    val bStroke = 1.4.dp.toPx()
    val bColor = ScanGreen.copy(alpha = 0.85f)
    val br = bracketReach
    val bi = bracketInset
    // TL
    drawLine(bColor, Offset(center.x - br, center.y - bi), Offset(center.x - br, center.y - br), bStroke)
    drawLine(bColor, Offset(center.x - br, center.y - br), Offset(center.x - bi, center.y - br), bStroke)
    // TR
    drawLine(bColor, Offset(center.x + bi, center.y - br), Offset(center.x + br, center.y - br), bStroke)
    drawLine(bColor, Offset(center.x + br, center.y - br), Offset(center.x + br, center.y - bi), bStroke)
    // BR
    drawLine(bColor, Offset(center.x + br, center.y + bi), Offset(center.x + br, center.y + br), bStroke)
    drawLine(bColor, Offset(center.x + br, center.y + br), Offset(center.x + bi, center.y + br), bStroke)
    // BL
    drawLine(bColor, Offset(center.x - bi, center.y + br), Offset(center.x - br, center.y + br), bStroke)
    drawLine(bColor, Offset(center.x - br, center.y + br), Offset(center.x - br, center.y + bi), bStroke)
}

private fun DrawScope.drawAcquisitionNodes(
    gridL: Float, gridT: Float, gridW: Float, gridH: Float,
    alphaA: Float, alphaB: Float, alphaC: Float,
) {
    val nodeSize = 7.dp.toPx()
    val strokeW = 0.9.dp.toPx()

    data class Node(val xFrac: Float, val yFrac: Float, val alpha: Float)

    val nodes = listOf(
        Node(0.09f, 0.12f, alphaA),
        Node(0.83f, 0.78f, alphaB),
        Node(0.47f, 0.47f, alphaC),
    )
    for (n in nodes) {
        val x = gridL + gridW * n.xFrac
        val y = gridT + gridH * n.yFrac
        drawRect(
            color = ScanGreen.copy(alpha = n.alpha * 0.82f),
            topLeft = Offset(x, y),
            size = androidx.compose.ui.geometry.Size(nodeSize, nodeSize),
            style = Stroke(width = strokeW),
        )
    }
}

private fun DrawScope.drawParticles(
    gridL: Float, gridR: Float, gridT: Float, gridB: Float,
    p1YFrac: Float, p1Alpha: Float,
    p2YFrac: Float, p2Alpha: Float,
) {
    val r = 1.2.dp.toPx()
    // Particle 1 – drifts from gridT to gridB on left third
    val p1x = gridL + (gridR - gridL) * 0.12f
    val p1y = gridT + (gridB - gridT) * p1YFrac
    drawCircle(ScanGreen.copy(alpha = p1Alpha), r, Offset(p1x, p1y))

    // Particle 2 – drifts from gridB to gridT on right third
    val p2x = gridL + (gridR - gridL) * 0.88f
    val p2y = gridT + (gridB - gridT) * p2YFrac
    drawCircle(ScanGreen.copy(alpha = p2Alpha), r, Offset(p2x, p2y))
}

package com.example.mamunbingoapp.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val WinningMarkerStrokeWidth = 2.5.dp
private const val WinningMarkerSteps = 40

/**
 * Hand-drawn red ellipse used for bingo win cells ([BingoCell], Live carousel sheet cells).
 */
fun DrawScope.drawBingoWinningEllipseMarker(
    strokeColor: Color,
    size: Size = this.size,
) {
    val strokeWidth = WinningMarkerStrokeWidth.toPx()
    val margin = maxOf(strokeWidth * 2f, minOf(size.width, size.height) * 0.12f)
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rx = (size.width / 2f - margin).coerceAtLeast(2f)
    val ry = (size.height / 2f - margin).coerceAtLeast(2f)
    val path = Path()
    for (i in 0..WinningMarkerSteps) {
        val t = i.toFloat() / WinningMarkerSteps * 2f * PI.toFloat()
        val jitter = (sin(t * 7f) * 0.02f + sin(t * 11f) * 0.015f)
        val r = 1f + jitter
        val x = cx + rx * cos(t) * r
        val y = cy + ry * sin(t) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(
        path,
        strokeColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}

/** Draws the shared winning ellipse on top of cell content (same visual as detail [BingoCell]). */
fun Modifier.bingoWinningMarker(
    enabled: Boolean,
    strokeColor: Color,
): Modifier = if (!enabled) {
    this
} else {
    drawWithContent {
        drawContent()
        drawBingoWinningEllipseMarker(strokeColor)
    }
}

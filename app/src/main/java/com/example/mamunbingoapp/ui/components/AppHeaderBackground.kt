package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.mamunbingoapp.theme.Background
import com.example.mamunbingoapp.theme.HeaderGradientEnd

/** Soft vertical gradient (live-card greens in light); decorative strokes kept low-contrast. */
@Composable
fun AppHeaderBackground(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gradientColors = if (dark) {
                listOf(
                    cs.surface,
                    cs.primaryContainer.copy(alpha = 0.18f),
                    cs.surface
                )
            } else {
                listOf(
                    Background,
                    HeaderGradientEnd,
                    cs.surface
                )
            }
            drawRect(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = h
                )
            )
            val strokeWidth = 1.dp.toPx()
            val strokeColor = cs.primary.copy(alpha = if (dark) 0.09f else 0.06f)
            val spiralColor = cs.onPrimaryContainer.copy(alpha = if (dark) 0.06f else 0.04f)

            val wave1 = Path().apply {
                moveTo(0f, h * 0.35f)
                cubicTo(w * 0.2f, h * 0.1f, w * 0.5f, h * 0.5f, w * 0.6f, h * 0.4f)
                cubicTo(w * 0.75f, h * 0.55f, w * 0.95f, h * 0.2f, w + 20f, h * 0.3f)
            }
            drawPath(wave1, strokeColor, style = Stroke(width = strokeWidth))

            val wave2 = Path().apply {
                moveTo(-20f, h * 0.7f)
                cubicTo(w * 0.25f, h * 0.55f, w * 0.55f, h * 0.65f, w * 0.75f, h * 0.75f)
                cubicTo(w * 0.9f, h * 0.88f, w * 1.02f, h * 0.7f, w + 20f, h * 0.6f)
            }
            drawPath(wave2, cs.primary.copy(alpha = if (dark) 0.07f else 0.05f), style = Stroke(width = strokeWidth))

            val spiralRadius = 0.6f * kotlin.math.min(w, h)
            val cx = w * 0.88f
            val cy = h * 0.12f
            val spiralPath = Path().apply {
                moveTo(cx + spiralRadius, cy)
                for (i in 1..12) {
                    val t = i / 12f
                    val r = spiralRadius * (1f - t * 0.85f)
                    val angle = t * 2f * kotlin.math.PI.toFloat()
                    lineTo(
                        cx + r * kotlin.math.cos(angle),
                        cy + r * kotlin.math.sin(angle)
                    )
                }
            }
            drawPath(spiralPath, spiralColor, style = Stroke(width = strokeWidth))
        }
    }
}

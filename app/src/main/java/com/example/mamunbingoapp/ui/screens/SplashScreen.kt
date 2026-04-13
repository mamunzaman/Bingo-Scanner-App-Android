package com.example.mamunbingoapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.SettingsRepository
import com.example.mamunbingoapp.theme.PrimaryPressed
import com.example.mamunbingoapp.theme.Slate400
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_MS = 3450
private const val BUBBLE_COUNT = 28

/** Material motion–style easing for the loading bar (ease-in-out, premium feel). */
private val SplashProgressEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
/** Soft overshoot for hero pop-in (aligned with reference HTML). */
private val SplashHeroPopEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

private val BingoLabels = listOf(
    "B1", "B5", "B9", "B14", "I16", "I20", "I25", "I29", "N31", "N38", "N41", "N45",
    "G46", "G52", "G57", "O63", "O70", "O73", "O75", "B3", "I19", "N44", "G50", "O68", "B12", "I24", "N33",
)

private data class BubbleSpec(
    val xFrac: Float,
    val speed: Float,
    val phase: Float,
    val label: String,
    val isCircle: Boolean,
    val size: Float,
    val opacity: Float,
    val wobble: Float,
)

@Composable
fun SplashScreen(onFinished: (skipOnboarding: Boolean) -> Unit) {
    val progress = remember { Animatable(0f) }
    val heroAlpha = remember { Animatable(0f) }
    val heroScale = remember { Animatable(0.88f) }
    LaunchedEffect(Unit) {
        coroutineScope {
            launch { heroAlpha.animateTo(1f, tween(480, easing = FastOutSlowInEasing)) }
            launch { heroScale.animateTo(1f, tween(780, easing = SplashHeroPopEasing)) }
            launch { progress.animateTo(1f, tween(SPLASH_MS, easing = SplashProgressEasing)) }
            delay(SPLASH_MS.toLong())
            val skipOnboarding = SettingsRepository.getOnboardingCompleted()
            onFinished(skipOnboarding)
        }
    }
    val bg = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val pc = MaterialTheme.colorScheme.primaryContainer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .navigationBarsPadding(),
    ) {
        BingoBubbleRainBackground(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = bg,
            primary = primary,
            primaryContainer = pc,
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = heroAlpha.value
                    scaleX = heroScale.value
                    scaleY = heroScale.value
                    transformOrigin = TransformOrigin.Center
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SplashLogoMark(primary = primary, primaryContainer = pc)
            val title = stringResource(R.string.splash_title)
            SplashBingoTitle(
                title = title,
                primary = primary,
                modifier = Modifier.padding(top = 20.dp),
            )
            SplashTaglineAnimated(
                text = stringResource(R.string.splash_tagline),
                letterCount = title.length,
                modifier = Modifier.padding(top = 8.dp),
            )
            Box(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .width(60.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(pc),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.value)
                        .background(primary, RoundedCornerShape(10.dp)),
                )
            }
        }
    }
}

@Composable
private fun SplashLogoMark(
    primary: androidx.compose.ui.graphics.Color,
    primaryContainer: androidx.compose.ui.graphics.Color,
) {
    val infinite = rememberInfiniteTransition(label = "splashLogo")
    val breathe by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "iconBreathe",
    )
    val sway by infinite.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ecoSway",
    )
    val halo by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo",
    )
    Box(modifier = Modifier.size(108.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(102.dp)
                .graphicsLayer {
                    scaleX = halo
                    scaleY = halo
                    alpha = 0.35f
                }
                .border(BorderStroke(1.5.dp, primary.copy(alpha = 0.22f)), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .shadow(5.dp, CircleShape, spotColor = primary.copy(alpha = 0.35f))
                .border(BorderStroke(1.dp, primaryContainer.copy(alpha = 0.88f)), CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Eco,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = breathe
                        scaleY = breathe
                        rotationZ = sway
                    },
                tint = primary,
            )
        }
    }
}

@Composable
private fun SplashBingoTitle(
    title: String,
    primary: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val gradientEnd = with(density) { 42.sp.toPx() }
    val brush = Brush.verticalGradient(
        colors = listOf(primary, PrimaryPressed),
        startY = 0f,
        endY = gradientEnd,
    )
    val chars = remember(title) { title.toList() }
    val letterAlphas = remember(title) { List(chars.size) { Animatable(0f) } }
    val letterOffsets = remember(title) { List(chars.size) { Animatable(22f) } }
    val baseStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 6.sp,
    )
    LaunchedEffect(title) {
        chars.forEachIndexed { i, ch ->
            if (ch.isWhitespace()) {
                letterAlphas[i].snapTo(1f)
                letterOffsets[i].snapTo(0f)
            }
        }
        val animIndices = chars.mapIndexedNotNull { i, c -> if (c.isWhitespace()) null else i }
        coroutineScope {
            animIndices.forEachIndexed { stagger, charIndex ->
                launch {
                    delay(200 + stagger * 62L)
                    coroutineScope {
                        launch { letterOffsets[charIndex].animateTo(0f, tween(420, easing = FastOutSlowInEasing)) }
                        launch { letterAlphas[charIndex].animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
                    }
                }
            }
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chars.forEachIndexed { i, ch ->
            when {
                ch.isWhitespace() -> Spacer(Modifier.width(10.dp))
                else -> {
                    Text(
                        text = ch.toString(),
                        style = TextStyle(
                            brush = brush,
                            fontSize = baseStyle.fontSize,
                            fontWeight = baseStyle.fontWeight,
                            letterSpacing = baseStyle.letterSpacing,
                            fontFamily = baseStyle.fontFamily,
                        ),
                        modifier = Modifier.graphicsLayer {
                            alpha = letterAlphas[i].value
                            translationY = letterOffsets[i].value
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashTaglineAnimated(
    text: String,
    letterCount: Int,
    modifier: Modifier = Modifier,
) {
    val tagAlpha = remember { Animatable(0f) }
    val tagY = remember { Animatable(14f) }
    LaunchedEffect(text, letterCount) {
        delay(180 + letterCount.coerceAtLeast(1) * 62L + 120L)
        launch {
            tagY.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
        }
        tagAlpha.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            letterSpacing = 2.5.sp,
        ),
        color = Slate400,
        modifier = modifier.graphicsLayer {
            alpha = tagAlpha.value
            translationY = tagY.value
        },
    )
}

@Composable
private fun BingoBubbleRainBackground(
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color,
    primary: androidx.compose.ui.graphics.Color,
    primaryContainer: androidx.compose.ui.graphics.Color,
) {
    val bubbles = remember {
        List(BUBBLE_COUNT) {
            BubbleSpec(
                xFrac = Random.nextFloat(),
                speed = 0.65f + Random.nextFloat() * 1.15f,
                phase = Random.nextFloat(),
                label = BingoLabels.random(),
                isCircle = Random.nextFloat() > 0.35f,
                size = 16f + Random.nextFloat() * 30f,
                opacity = 0.08f + Random.nextFloat() * 0.47f,
                wobble = Random.nextFloat() * 6.28f,
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "bubbleRain")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(42_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rainT",
    )
    val iconGreen = primary
    val lightGreen = primaryContainer
    val paleGreen = com.example.mamunbingoapp.theme.IconContainerBg
    Canvas(modifier) {
        drawRect(color = backgroundColor)
        val w = size.width
        val h = size.height
        val scale = (w / 300f).coerceIn(0.75f, 3f)
        bubbles.forEach { b ->
            val cycle = (t * b.speed * 4.4f + b.phase) % 1f
            val y = cycle * (h + 100f) - 50f
            val wx = sin(t * 6.28f * 1.2f + b.wobble).toFloat() * (w * 0.012f)
            val x = b.xFrac * w + wx
            val s = (b.size * scale).coerceIn(12f, minOf(w, h) * 0.14f)
            val alpha = b.opacity.coerceIn(0.06f, 0.48f)
            if (b.isCircle) {
                val fill = if (b.size > 26f) lightGreen else paleGreen
                drawCircle(
                    color = fill.copy(alpha = alpha),
                    radius = s * 0.5f,
                    center = Offset(x, y),
                )
                drawCircle(
                    color = iconGreen.copy(alpha = alpha * 1.05f),
                    radius = s * 0.5f,
                    center = Offset(x, y),
                    style = Stroke(width = 1.dp.toPx()),
                )
            } else {
                val rw = s * 1.4f
                val rh = s * 0.85f
                val fill = if (b.size > 28f) iconGreen else lightGreen
                drawRoundRect(
                    color = fill.copy(alpha = alpha),
                    topLeft = Offset(x - rw / 2f, y - rh / 2f),
                    size = Size(rw, rh),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )
            }
            val textColor = if (b.isCircle) iconGreen else androidx.compose.ui.graphics.Color.White
            val fontPx = (s * 0.36f).coerceAtLeast(7f)
            drawIntoCanvas { c ->
                val p = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = fontPx
                    color = textColor.copy(alpha = (alpha * 1.4f).coerceIn(0.08f, 0.55f)).toArgb()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val fm = p.fontMetrics
                val textY = y - (fm.ascent + fm.descent) / 2f
                c.nativeCanvas.drawText(b.label, x, textY, p)
            }
        }
    }
}

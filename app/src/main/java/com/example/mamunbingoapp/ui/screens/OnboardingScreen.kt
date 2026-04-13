package com.example.mamunbingoapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Outline
import com.example.mamunbingoapp.theme.Slate600
import com.example.mamunbingoapp.theme.SurfaceContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 3
private const val SLIDE_MS = 2400

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val segmentProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        segmentProgress.snapTo(0f)
        segmentProgress.animateTo(1f, tween(SLIDE_MS, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(pagerState.currentPage) {
        delay(SLIDE_MS.toLong())
        when (val p = pagerState.currentPage) {
            in 0 until PAGE_COUNT - 1 -> pagerState.scrollToPage(p + 1)
            else -> onFinished()
        }
    }

    val footerFill = (pagerState.currentPage + segmentProgress.value) / PAGE_COUNT

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch {
                            when (val p = pagerState.currentPage) {
                                in 0 until PAGE_COUNT - 1 -> pagerState.scrollToPage(p + 1)
                                else -> onFinished()
                            }
                        }
                    },
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                ) { page ->
                    OnboardingSlide(page = page)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = Dimens.cardElevationSubtle,
                shadowElevation = 0.dp,
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SurfaceContainer),
                )
                Column(
                    modifier = Modifier.padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 26.dp),
                ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = footerFill.coerceIn(0f, 1f))
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp),
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(PAGE_COUNT) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.5.dp)
                                .then(
                                    if (selected) {
                                        Modifier
                                            .width(20.dp)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    } else {
                                        Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    },
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_skip_hint),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = Outline,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                }
            }
            }
        }
    }
}

@Composable
private fun OnboardingSlide(page: Int) {
    val label = stringResource(
        when (page) {
            0 -> R.string.onboarding_slide1_label
            1 -> R.string.onboarding_slide2_label
            else -> R.string.onboarding_slide3_label
        },
    )
    val title = stringResource(
        when (page) {
            0 -> R.string.onboarding_slide1_title
            1 -> R.string.onboarding_slide2_title
            else -> R.string.onboarding_slide3_title
        },
    )
    val body = stringResource(
        when (page) {
            0 -> R.string.onboarding_slide1_body
            1 -> R.string.onboarding_slide2_body
            else -> R.string.onboarding_slide3_body
        },
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (page) {
            0 -> OnboardingArtSlide1()
            1 -> OnboardingArtSlide2()
            else -> OnboardingArtSlide3()
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 25.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 21.sp,
            ),
            color = Slate600,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OnboardingArtSlide1() {
    val bg = com.example.mamunbingoapp.theme.IconContainerBg
    val p = MaterialTheme.colorScheme.primary
    val pc = MaterialTheme.colorScheme.primaryContainer
    Box(modifier = Modifier.size(180.dp, 155.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rx = 16.dp.toPx()
            drawRoundRect(
                color = bg,
                cornerRadius = CornerRadius(rx, rx),
                size = size,
            )
            val cell = 34.dp.toPx()
            val gap = 7.dp.toPx()
            val padX = 17.dp.toPx()
            val padY = 17.dp.toPx()
            val colors = listOf(p, pc, p, pc, p, pc, p, pc, p)
            var i = 0
            for (row in 0..2) {
                for (col in 0..2) {
                    val ox = padX + col * (cell + gap)
                    val oy = padY + row * (cell + gap)
                    drawRoundRect(
                        color = colors[i++],
                        topLeft = Offset(ox, oy),
                        size = Size(cell, cell),
                        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
                    )
                }
            }
            val barY = padY + 3 * (cell + gap) + 8.dp.toPx()
            drawRoundRect(
                color = p.copy(alpha = 0.15f),
                topLeft = Offset(padX, barY),
                size = Size(size.width - 2 * padX, 16.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
            )
        }
    }
}

@Composable
private fun OnboardingArtSlide2() {
    val bg = com.example.mamunbingoapp.theme.IconContainerBg
    val p = MaterialTheme.colorScheme.primary
    val pc = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surface
    Box(modifier = Modifier.size(180.dp, 155.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.42f
            val r = 52.dp.toPx()
            drawCircle(color = bg, radius = r, center = Offset(cx, cy))
            val sweep = 60f
            var start = -90f
            val fills = listOf(
                p,
                pc,
                p.copy(alpha = 0.45f),
                pc.copy(alpha = 0.6f),
                p.copy(alpha = 0.28f),
                pc.copy(alpha = 0.4f),
            )
            repeat(6) { k ->
                drawArc(
                    color = fills[k],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                )
                start += sweep
            }
            drawCircle(color = surface, radius = 15.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

@Composable
private fun OnboardingArtSlide3() {
    val bg = com.example.mamunbingoapp.theme.IconContainerBg
    val p = MaterialTheme.colorScheme.primary
    val pc = MaterialTheme.colorScheme.primaryContainer
    Box(modifier = Modifier.size(180.dp, 155.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = 62.dp.toPx()
            drawCircle(color = bg, radius = 30.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))
            drawCircle(color = p, radius = 9.dp.toPx(), center = Offset(cx, cy - 5.dp.toPx()))
            drawCircle(color = bg, radius = 22.dp.toPx(), center = Offset(44.dp.toPx(), 98.dp.toPx()), style = Stroke(1.dp.toPx()))
            drawCircle(color = pc, radius = 7.dp.toPx(), center = Offset(44.dp.toPx(), 94.dp.toPx()))
            drawCircle(color = bg, radius = 22.dp.toPx(), center = Offset(136.dp.toPx(), 98.dp.toPx()), style = Stroke(1.dp.toPx()))
            drawCircle(color = pc, radius = 7.dp.toPx(), center = Offset(136.dp.toPx(), 94.dp.toPx()))
            drawLine(
                color = p,
                start = Offset(67.dp.toPx(), 76.dp.toPx()),
                end = Offset(55.dp.toPx(), 88.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 2f), 0f),
            )
            drawLine(
                color = p,
                start = Offset(113.dp.toPx(), 76.dp.toPx()),
                end = Offset(125.dp.toPx(), 88.dp.toPx()),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 2f), 0f),
            )
            drawRoundRect(
                color = p,
                topLeft = Offset(42.dp.toPx(), 128.dp.toPx()),
                size = Size(96.dp.toPx(), 20.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            )
        }
    }
}

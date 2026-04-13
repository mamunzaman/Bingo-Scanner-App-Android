package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.common.bingoLetter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Where the panel sits horizontally: **[HistoryDetail]** matches live card (no duplicate horizontal padding; parent has screen padding).
 * Use **[Default]** with [applyOuterPadding] for other hosts.
 */
enum class CalledHistoryPanelContext {
    Default,
    HistoryDetail,
}

/**
 * Recent called numbers: **latest** as a large primary circle; prior calls in a [LazyRow] with
 * horizontal **contentPadding** (no edge overlay), **snap** fling, and **animateScrollToItem** so the active (last) call stays visible with a small inward offset. Card chrome and "Called History" header removed.
 * Light theme: main circle uses softened greens + brighter white rim; inactive small circles use warm **#F5F3F0** fill and slightly stronger hairline border.
 *
 * @param applyOuterPadding When false, parent supplies horizontal/insets (e.g. shared live card). Ignored when [panelContext] is [CalledHistoryPanelContext.HistoryDetail].
 */
@Composable
fun CalledHistoryPanel(
    calledNumbers: List<Int>,
    isCallLimitReached: Boolean = false,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") maxPerColumn: Int = 4,
    @Suppress("UNUSED_PARAMETER") showTitle: Boolean = true,
    showLimitMessage: Boolean = true,
    applyOuterPadding: Boolean = true,
    panelContext: CalledHistoryPanelContext = CalledHistoryPanelContext.Default,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useOuterPadding = when (panelContext) {
        CalledHistoryPanelContext.HistoryDetail -> false
        CalledHistoryPanelContext.Default -> applyOuterPadding
    }
    val lastCalled = calledNumbers.lastOrNull()
    val displayList = calledNumbers
    val isEmpty = displayList.isEmpty()
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    var hasDoneInitialScroll by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val scrollBreathingPx = remember(density) { with(density) { (-10).dp.toPx().toInt() } }
    LaunchedEffect(lastCalled, displayList.size) {
        if (isEmpty) {
            hasDoneInitialScroll = false
            return@LaunchedEffect
        }
        val index = lastCalled?.let { n -> displayList.indexOfLast { it == n } } ?: displayList.lastIndex
        if (index < 0) return@LaunchedEffect
        if (!hasDoneInitialScroll) {
            listState.scrollToItem(index, scrollOffset = scrollBreathingPx)
            hasDoneInitialScroll = true
        } else {
            listState.animateScrollToItem(index, scrollOffset = scrollBreathingPx)
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (useOuterPadding) {
                    Modifier.padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8)
                } else {
                    Modifier.padding(vertical = Dimens.spacing4)
                }
            ),
    ) {
        if (isEmpty) {
            EmptyStateBlock(
                icon = Icons.Default.Numbers,
                title = "No numbers called yet",
                subtitle = "Called numbers will appear here during the game.",
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LatestCallCircle(
                    number = lastCalled,
                    colorScheme = colorScheme,
                    isFinished = isCallLimitReached,
                )
                Spacer(modifier = Modifier.width(20.dp))
                LazyRow(
                    state = listState,
                    flingBehavior = flingBehavior,
                    contentPadding = PaddingValues(horizontal = Dimens.spacing12),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    itemsIndexed(displayList, key = { _, num -> num }) { _, num ->
                        HistoryCallCircle(
                            number = num,
                            isLatest = num == lastCalled,
                            colorScheme = colorScheme,
                        )
                    }
                }
            }
        
        }
    }
}

@Composable
private fun LatestCallCircle(
    number: Int?,
    colorScheme: ColorScheme,
    isFinished: Boolean = false,
) {
    val scaleAnim = remember { Animatable(1f) }
    val alphaAnim = remember { Animatable(1f) }
    var previousNumber by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(number) {
        when {
            number == null -> {
                scaleAnim.snapTo(1f)
                alphaAnim.snapTo(1f)
                previousNumber = null
            }
            previousNumber == null -> {
                scaleAnim.snapTo(1f)
                alphaAnim.snapTo(1f)
                previousNumber = number
            }
            number != previousNumber -> {
                scaleAnim.snapTo(0.94f)
                alphaAnim.snapTo(0.88f)
                coroutineScope {
                    launch {
                        scaleAnim.animateTo(
                            1f,
                            tween(260, easing = FastOutSlowInEasing),
                        )
                    }
                    launch {
                        alphaAnim.animateTo(
                            1f,
                            tween(260, easing = FastOutSlowInEasing),
                        )
                    }
                }
                previousNumber = number
            }
        }
    }
    val radialColors = if (isFinished) {
        if (isSystemInDarkTheme()) {
            listOf(colorScheme.error.copy(alpha = 0.82f), colorScheme.error.copy(alpha = 0.48f))
        } else {
            listOf(Color(0xFFD84040), Color(0xFFB02828))
        }
    } else if (isSystemInDarkTheme()) {
        listOf(colorScheme.primary.copy(alpha = 0.82f), colorScheme.primary.copy(alpha = 0.48f))
    } else {
        listOf(Color(0xFF7ABF3F), Color(0xFF529628))
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scaleAnim.value)
            .alpha(alphaAnim.value),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Brush.radialGradient(colors = radialColors))
                .border(1.dp, Color.White.copy(alpha = 0.38f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.09f), Color.Transparent),
                            center = Offset(28f, 26f),
                            radius = 38f
                        )
                    )
            )
            Text(
                text = number?.let { formatBingoNumber(it) } ?: "—",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.6.sp,
                ),
                color = Color.White.copy(alpha = 0.98f),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun HistoryCallCircle(
    number: Int,
    isLatest: Boolean,
    colorScheme: ColorScheme,
) {
    val rowAlpha = remember { Animatable(1f) }
    var wasLatest by remember { mutableStateOf(false) }
    LaunchedEffect(isLatest) {
        if (isLatest) {
            if (!wasLatest) {
                rowAlpha.snapTo(0.92f)
                rowAlpha.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
            }
            wasLatest = true
        } else {
            rowAlpha.snapTo(1f)
            wasLatest = false
        }
    }
    val latestGradient = if (isSystemInDarkTheme()) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.primaryContainer.copy(alpha = 0.9f),
                colorScheme.primaryContainer.copy(alpha = 0.78f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFEAF6EA), Color(0xFFDFF1DF))
        )
    }
    val recentTextGreen = Color(0xFF4A8F2A)
    val inactiveBorderLight = Color.Black.copy(alpha = 0.056f)
    val inactiveBorder = if (isSystemInDarkTheme()) {
        Color.White.copy(alpha = 0.07f)
    } else {
        inactiveBorderLight
    }
    val latestRim = colorScheme.outline.copy(alpha = if (isSystemInDarkTheme()) 0.16f else 0.1f)
    Box(
        modifier = Modifier
            .size(48.dp)
            .alpha(rowAlpha.value)
            .clip(CircleShape)
            .then(
                if (isLatest) {
                    Modifier
                        .background(latestGradient)
                        .border(1.dp, latestRim, CircleShape)
                } else {
                    Modifier
                        .background(
                            if (isSystemInDarkTheme()) {
                                colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            } else {
                                Color(0xFFF5F3F0)
                            }
                        )
                        .border(1.dp, inactiveBorder, CircleShape)
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatBingoNumber(number),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = if (isLatest) 0.35.sp else 0.25.sp,
            ),
            color = if (isLatest) {
                if (isSystemInDarkTheme()) {
                    colorScheme.onPrimaryContainer.copy(alpha = 0.95f)
                } else {
                    recentTextGreen
                }
            } else {
                if (isSystemInDarkTheme()) {
                    colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    Color.Black.copy(alpha = 0.5f)
                }
            },
        )
    }
}

private fun formatBingoNumber(num: Int): String {
    val letter = bingoLetter(num)
    return if (letter.isEmpty()) "—" else "$letter$num"
}

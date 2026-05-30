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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.common.bingoLetter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val CalledHistoryMainRowHeight = 72.dp
private val TvNumberCyanShadow = Color(0xFF80DEEA)

/**
 * Where the panel sits horizontally: **[HistoryDetail]** matches live card (no duplicate horizontal padding; parent has screen padding).
 * Use **[Default]** with [applyOuterPadding] for other hosts.
 */
enum class CalledHistoryPanelContext {
    Default,
    HistoryDetail,
}

/**
 * Recent called numbers: latest + prior calls row, then a full-width TV-style **B I N G O** track board (no chips on the board).
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
    showTvBoard: Boolean = true,
    premiumLatestRow: Boolean = false,
    latestCircleSize: Dp? = null,
    recentChipSize: Dp? = null,
    panelContext: CalledHistoryPanelContext = CalledHistoryPanelContext.Default,
) {
    val colorScheme = MaterialTheme.colorScheme
    val mainRowHeight = latestCircleSize ?: if (premiumLatestRow) 88.dp else CalledHistoryMainRowHeight
    val resolvedRecentChipSize = recentChipSize ?: if (premiumLatestRow) 48.dp else 44.dp
    val recentChipSpacing = if (premiumLatestRow) Dimens.spacing10 else Dimens.spacing8
    val recentOverlapOffset = if (premiumLatestRow) 10.dp else 8.dp
    val useOuterPadding = when (panelContext) {
        CalledHistoryPanelContext.HistoryDetail -> false
        CalledHistoryPanelContext.Default -> applyOuterPadding
    }
    val lastCalled = calledNumbers.lastOrNull()
    val displayList = calledNumbers
    val isEmpty = displayList.isEmpty()
    val numbersByColumn = remember(calledNumbers) { groupCalledNumbersByColumn(calledNumbers) }
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    var hasDoneInitialScroll by remember { mutableStateOf(false) }
    LaunchedEffect(displayList.size) {
        if (isEmpty) {
            hasDoneInitialScroll = false
            return@LaunchedEffect
        }
        val index = displayList.lastIndex
        if (!hasDoneInitialScroll) {
            listState.scrollToItem(index)
            hasDoneInitialScroll = true
        } else {
            listState.animateScrollToItem(index)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (useOuterPadding) {
                    Modifier.padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8)
                } else if (panelContext == CalledHistoryPanelContext.HistoryDetail) {
                    Modifier
                } else {
                    Modifier.padding(vertical = Dimens.spacing4)
                },
            ),
    ) {
        if (isEmpty) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mainRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LatestCallCircle(
                    number = null,
                    colorScheme = colorScheme,
                    isFinished = isCallLimitReached,
                    modifier = Modifier.zIndex(2f),
                    circleSize = mainRowHeight,
                    showGlowRing = premiumLatestRow,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = Dimens.spacing12),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.live_play_no_numbers_called),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                        maxLines = if (panelContext == CalledHistoryPanelContext.HistoryDetail) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (panelContext != CalledHistoryPanelContext.HistoryDetail) {
                        Text(
                            text = stringResource(R.string.live_play_called_numbers_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mainRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LatestCallCircle(
                    number = lastCalled,
                    colorScheme = colorScheme,
                    isFinished = isCallLimitReached,
                    modifier = Modifier.zIndex(2f),
                    circleSize = mainRowHeight,
                    showGlowRing = premiumLatestRow,
                )
                val overlapOffset = recentOverlapOffset
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .zIndex(1f),
                ) {
                    val chipSize = resolvedRecentChipSize
                    val chipSpacing = recentChipSpacing
                    val chipCount = displayList.size
                    val totalSpacing = if (chipCount > 1) chipSpacing * (chipCount - 1) else 0.dp
                    val estimatedContentWidth = (chipSize * chipCount) + totalSpacing
                    val fitsWithoutScroll = estimatedContentWidth <= maxWidth

                    if (fitsWithoutScroll) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .wrapContentWidth()
                                    .offset(x = -overlapOffset),
                                horizontalArrangement = Arrangement.spacedBy(chipSpacing),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                displayList.forEach { num ->
                                    HistoryCallCircle(
                                        number = num,
                                        isLatest = num == lastCalled,
                                        colorScheme = colorScheme,
                                        chipSize = chipSize,
                                    )
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            LazyRow(
                                state = listState,
                                flingBehavior = flingBehavior,
                                contentPadding = PaddingValues(),
                                horizontalArrangement = Arrangement.spacedBy(chipSpacing),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(x = -overlapOffset),
                            ) {
                                itemsIndexed(displayList, key = { _, num -> num }) { _, num ->
                                    HistoryCallCircle(
                                        number = num,
                                        isLatest = num == lastCalled,
                                        colorScheme = colorScheme,
                                        chipSize = chipSize,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showTvBoard) {
                Spacer(modifier = Modifier.height(Dimens.spacing8))
                TvBingoBoard(
                    numbersByColumn = numbersByColumn,
                    latest = lastCalled,
                    boardGreen = colorScheme.primary,
                    letterRed = colorScheme.secondary,
                    lineColor = Color.Black.copy(alpha = 0.38f),
                    callSequence = calledNumbers,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (showLimitMessage && isCallLimitReached) {
            Text(
                text = stringResource(R.string.live_play_round_complete_all_calls),
                modifier = Modifier.padding(top = Dimens.spacing4),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LatestCallCircle(
    number: Int?,
    colorScheme: ColorScheme,
    isFinished: Boolean = false,
    modifier: Modifier = Modifier,
    circleSize: Dp = CalledHistoryMainRowHeight,
    showGlowRing: Boolean = false,
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
                        scaleAnim.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
                    }
                    launch {
                        alphaAnim.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
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
        modifier = modifier.size(circleSize),
        contentAlignment = Alignment.Center,
    ) {
        if (showGlowRing) {
            Box(
                modifier = Modifier
                    .size(circleSize + 10.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorScheme.primary.copy(alpha = 0.24f), CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.06f)),
            )
        }
        Box(
            modifier = Modifier
                .size(circleSize)
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
                                radius = 38f,
                            ),
                        ),
                )
                Text(
                    text = number?.let { formatBingoNumber(it) } ?: "—",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = if (circleSize >= 84.dp) 26.sp else 22.sp,
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
}

@Composable
private fun HistoryCallCircle(
    number: Int,
    isLatest: Boolean,
    colorScheme: ColorScheme,
    chipSize: Dp = 44.dp,
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
    val chipBg = if (isLatest) {
        if (isSystemInDarkTheme()) {
            colorScheme.primaryContainer.copy(alpha = 0.38f)
        } else {
            Color(0xFFEAF6EA)
        }
    } else if (isSystemInDarkTheme()) {
        colorScheme.surfaceVariant.copy(alpha = 0.42f)
    } else {
        Color(0xFFF3F4F2)
    }
    val chipBorder = if (isLatest) {
        if (isSystemInDarkTheme()) {
            colorScheme.primary.copy(alpha = 0.46f)
        } else {
            Color(0xFF7FB063).copy(alpha = 0.75f)
        }
    } else {
        colorScheme.outlineVariant.copy(alpha = 0.52f)
    }
    val chipText = if (isLatest) {
        if (isSystemInDarkTheme()) {
            colorScheme.onPrimaryContainer.copy(alpha = 0.96f)
        } else {
            Color(0xFF2F6D19)
        }
    } else if (isSystemInDarkTheme()) {
        colorScheme.onSurface.copy(alpha = 0.74f)
    } else {
        Color.Black.copy(alpha = 0.66f)
    }
    Box(
        modifier = Modifier
            .size(chipSize)
            .alpha(rowAlpha.value)
            .clip(CircleShape)
            .background(chipBg)
            .border(1.dp, chipBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatBingoNumber(number),
            style = (if (chipSize <= 40.dp) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.labelLarge
            }).copy(
                fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = 0.2.sp,
            ),
            color = chipText,
        )
    }
}

private fun formatBingoNumber(num: Int): String {
    val letter = bingoLetter(num)
    return if (letter.isEmpty()) "—" else "$letter$num"
}

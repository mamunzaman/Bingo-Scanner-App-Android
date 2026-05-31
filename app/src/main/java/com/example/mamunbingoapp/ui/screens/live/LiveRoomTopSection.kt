package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.core.MAX_LIVE_CALLS
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.LiveFonts
import com.example.mamunbingoapp.ui.components.common.bingoLetter
import com.example.mamunbingoapp.ui.model.RoomStatus
import com.example.mamunbingoapp.viewmodel.LivePlayUiState

private object NewVariantSpacing {
    val topRowPaddingH = Dimens.spacing16
    val topRowPaddingV = Dimens.spacing8
    val centerZonePaddingV = Dimens.spacing12
    val footerPaddingH = Dimens.spacing16
    val footerPaddingTop = Dimens.spacing12
    val footerPaddingBottom = Dimens.spacing12
    val dividerThickness = Dimens.cardBorderDefault
    val dividerAlpha = 0.45f
    val compactThreshold = 140.dp
    val compactTopPaddingV = Dimens.spacing4
    val compactCenterPaddingV = Dimens.spacing4
    val compactFooterPaddingTop = Dimens.spacing4
    val compactFooterPaddingBottom = Dimens.spacing8
}

private fun progressFraction(calledCount: Int): Float =
    (calledCount.coerceAtMost(MAX_LIVE_CALLS).toFloat() / MAX_LIVE_CALLS)

private fun liveBadgePillBackground(colorScheme: ColorScheme, darkTheme: Boolean): Color =
    if (darkTheme) {
        lerp(colorScheme.surface, colorScheme.primaryContainer, 0.28f).copy(alpha = 0.93f)
    } else {
        lerp(Color(0xFFE2F0DC), Color(0xFFD4E8D0), 0.35f).copy(alpha = 0.98f)
    }

private fun liveBadgePillBorder(colorScheme: ColorScheme, darkTheme: Boolean): Color =
    if (darkTheme) {
        colorScheme.primary.copy(alpha = 0.2f)
    } else {
        Color(0xFF4A8F2A).copy(alpha = 0.16f)
    }

private fun liveCountPillBackground(colorScheme: ColorScheme, darkTheme: Boolean): Color =
    if (darkTheme) {
        lerp(colorScheme.surface, colorScheme.surfaceVariant, 0.14f).copy(alpha = 0.88f)
    } else {
        lerp(Color(0xFFF6F8F4), colorScheme.surfaceVariant, 0.06f).copy(alpha = 0.9f)
    }

private fun liveCountPillBorder(colorScheme: ColorScheme, darkTheme: Boolean): Color =
    if (darkTheme) {
        colorScheme.outline.copy(alpha = 0.055f)
    } else {
        Color.Black.copy(alpha = 0.036f)
    }

private val LiveStatusPillShape = RoundedCornerShape(Dimens.radiusButtonPill)
private val LiveCompactTopBarHeight = 44.dp
private val LiveCompactTopBarHPad = Dimens.spacing12

private data class LiveDisplayValues(
    val displayLetter: String,
    val displayNumberText: String,
    val displayCallCountText: String,
    val displayLastCalledText: String
)

private fun liveDisplayValues(
    letter: String,
    number: Int?,
    callCount: Int,
    totalCount: Int,
    lastCalledAgoText: String
) = LiveDisplayValues(
    displayLetter = letter,
    displayNumberText = number?.toString() ?: "00",
    displayCallCountText = "$callCount / $totalCount",
    displayLastCalledText = lastCalledAgoText
)

@Composable
private fun LiveBadge(
    dotAlpha: Float,
    colorScheme: ColorScheme
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liveDotPulse")
    val dotPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotScale"
    )
    val dotPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotAlpha"
    )
    val dotAlphaCombined = dotPulseAlpha * dotAlpha.coerceIn(0.75f, 1f)
    Surface(
        shape = LiveStatusPillShape,
        color = colorScheme.primary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.22f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .scale(dotPulseScale)
                    .alpha(dotAlphaCombined)
                    .background(Color.Red, CircleShape)
            )
            Text(
                text = stringResource(R.string.common_live_badge),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.12.sp,
                    lineHeight = 12.sp
                ),
                color = colorScheme.primary
            )
        }
    }
}

@Composable
private fun LiveCallCountCapsule(
    calledCount: Int,
    colorScheme: ColorScheme
) {
    val darkTheme = isSystemInDarkTheme()
    val countStyle = MaterialTheme.typography.labelMedium.copy(
        fontFamily = LiveFonts.DMMono,
        fontSize = 12.sp,
        lineHeight = 14.sp
    )
    Surface(
        shape = LiveStatusPillShape,
        color = liveCountPillBackground(colorScheme, darkTheme),
        border = BorderStroke(1.dp, liveCountPillBorder(colorScheme, darkTheme)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacing12, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = calledCount.toString(),
                style = countStyle.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.02).sp
                ),
                color = colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 1
            )
            Text(
                text = " / ",
                style = countStyle.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.1.sp
                ),
                color = colorScheme.onSurface.copy(alpha = 0.3f),
                maxLines = 1
            )
            Text(
                text = MAX_LIVE_CALLS.toString(),
                style = countStyle.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp
                ),
                color = colorScheme.onSurface.copy(alpha = 0.52f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LiveCountText(
    text: String,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = LiveFonts.DMMono,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
        ),
        color = colorScheme.onSurface,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible
    )
}

@Composable
private fun LastCalledText(
    agoText: String,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Text(
        text = stringResource(R.string.live_play_last_called, agoText),
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = LiveFonts.DMMono,
            fontWeight = FontWeight.Normal
        ),
        color = colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AutoCallHint(
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Casino,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            text = stringResource(R.string.live_play_tap_to_auto_call),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.06.sp
            ),
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun BingoStrip(
    letter: String,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    if (letter.isEmpty()) return
    Text(
        text = letter,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = LiveFonts.DMMono,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        ),
        color = colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LiveNumberTextRaw(
    displayNumberText: String,
    colorScheme: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 104.sp
) {
    Text(
        text = displayNumberText,
        style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = LiveFonts.DMMono,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            lineHeight = fontSize
        ),
        color = colorScheme.primary,
        modifier = modifier.semantics { heading(); traversalIndex = 2f }
    )
}

@Composable
private fun LiveNumberText(
    displayNumberText: String,
    numberPopScale: Float,
    colorScheme: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 104.sp
) {
    LiveNumberTextRaw(
        displayNumberText = displayNumberText,
        colorScheme = colorScheme,
        modifier = modifier.graphicsLayer(scaleX = numberPopScale, scaleY = numberPopScale),
        fontSize = fontSize
    )
}

@Composable
private fun LastCalledNumberStack(
    outgoingText: String,
    outgoingVisible: Boolean,
    currentNumberText: String,
    numberPopScale: Float,
    modifier: Modifier = Modifier,
    numberContent: @Composable (text: String, modifier: Modifier) -> Unit
) {
    Box(
        modifier = modifier.semantics(mergeDescendants = true) { heading(); traversalIndex = 2f }
    ) {
        SubcomposeLayout(modifier = Modifier) { constraints ->
            val currentMeasurables = subcompose("current") {
                AnimatedContent(
                    targetState = currentNumberText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                    },
                    label = "lastCalledNumber"
                ) { text ->
                    numberContent(
                        text,
                        Modifier.graphicsLayer(scaleX = numberPopScale, scaleY = numberPopScale)
                    )
                }
            }
            val outgoingMeasurables = if (outgoingText.isNotEmpty()) {
                subcompose("outgoing") { numberContent(outgoingText, Modifier) }
            } else null
            val currentPlaceable = currentMeasurables.first().measure(constraints)
            val outgoingPlaceable = outgoingMeasurables?.first()?.measure(constraints)
            val slotW = maxOf(currentPlaceable.width, outgoingPlaceable?.width ?: 0)
            val slotH = maxOf(currentPlaceable.height, outgoingPlaceable?.height ?: 0)
            layout(slotW, slotH) {
                currentPlaceable.place(
                    (slotW - currentPlaceable.width) / 2,
                    (slotH - currentPlaceable.height) / 2
                )
            }
        }
        if (outgoingText.isNotEmpty()) {
            AnimatedVisibility(
                visible = outgoingVisible,
                modifier = Modifier.matchParentSize(),
                exit = fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) + slideOutVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetOffsetY = { -it / 4 }
                )
            ) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.alpha(0.55f)) {
                        numberContent(outgoingText, Modifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveLetterText(
    letter: String,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    if (letter.isEmpty()) return
    Text(
        text = letter,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = LiveFonts.DMMono,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        ),
        color = colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LiveCallCardV1(
    values: LiveDisplayValues,
    numberPopScale: Float,
    dotAlpha: Float,
    previousOutgoingText: String,
    previousOutgoingVisible: Boolean,
    onAutoCall: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: androidx.compose.material3.ColorScheme = MaterialTheme.colorScheme
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(Dimens.radiusLarge))
            .clickable(onClick = onAutoCall, indication = null, interactionSource = remember { MutableInteractionSource() }),
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(Dimens.spacing12)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveBadge(dotAlpha, colorScheme)
                    LiveCountText(values.displayCallCountText, colorScheme)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    LiveLetterText(values.displayLetter, colorScheme)
                    Spacer(Modifier.weight(1f))
                    LastCalledNumberStack(
                        outgoingText = previousOutgoingText,
                        outgoingVisible = previousOutgoingVisible,
                        currentNumberText = values.displayNumberText,
                        numberPopScale = numberPopScale,
                        modifier = Modifier,
                        numberContent = { text, mod -> LiveNumberTextRaw(text, colorScheme, mod, 104.sp) }
                    )
                }
                LastCalledText(values.displayLastCalledText, colorScheme)
                Box(modifier = Modifier.padding(top = Dimens.spacing4)) {
                    AutoCallHint(colorScheme)
                }
            }
        }
    }
}

@Composable
private fun LiveCallCardV2(
    values: LiveDisplayValues,
    numberPopScale: Float,
    dotAlpha: Float,
    previousOutgoingText: String,
    previousOutgoingVisible: Boolean,
    onAutoCall: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: androidx.compose.material3.ColorScheme = MaterialTheme.colorScheme
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(Dimens.radiusLarge))
            .clickable(onClick = onAutoCall, indication = null, interactionSource = remember { MutableInteractionSource() }),
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing12),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.widthIn(min = 48.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                LiveLetterText(values.displayLetter, colorScheme)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveBadge(dotAlpha, colorScheme)
                    LiveCountText(values.displayCallCountText, colorScheme)
                }
                LastCalledNumberStack(
                    outgoingText = previousOutgoingText,
                    outgoingVisible = previousOutgoingVisible,
                    currentNumberText = values.displayNumberText,
                    numberPopScale = numberPopScale,
                    modifier = Modifier.fillMaxWidth(),
                    numberContent = { text, mod -> LiveNumberTextRaw(text, colorScheme, mod, 104.sp) }
                )
                LastCalledText(values.displayLastCalledText, colorScheme)
                Box(modifier = Modifier.padding(top = Dimens.spacing4)) {
                    AutoCallHint(colorScheme)
                }
            }
        }
    }
}

@Composable
private fun LiveCallCardV3(
    values: LiveDisplayValues,
    numberPopScale: Float,
    dotAlpha: Float,
    previousOutgoingText: String,
    previousOutgoingVisible: Boolean,
    onAutoCall: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: androidx.compose.material3.ColorScheme = MaterialTheme.colorScheme
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(Dimens.radiusLarge))
            .clickable(onClick = onAutoCall, indication = null, interactionSource = remember { MutableInteractionSource() }),
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveBadge(dotAlpha, colorScheme)
                LiveCountText(values.displayCallCountText, colorScheme)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                BingoStrip(values.displayLetter, colorScheme)
                Spacer(Modifier.weight(1f))
                LastCalledNumberStack(
                    outgoingText = previousOutgoingText,
                    outgoingVisible = previousOutgoingVisible,
                    currentNumberText = values.displayNumberText,
                    numberPopScale = numberPopScale,
                    modifier = Modifier,
                    numberContent = { text, mod -> LiveNumberTextRaw(text, colorScheme, mod, 32.sp) }
                )
            }
            LastCalledText(values.displayLastCalledText, colorScheme)
            Box(modifier = Modifier.padding(top = Dimens.spacing4)) {
                AutoCallHint(colorScheme)
            }
        }
    }
}

@Composable
private fun LiveCallCardV4(
    values: LiveDisplayValues,
    numberPopScale: Float,
    dotAlpha: Float,
    previousOutgoingText: String,
    previousOutgoingVisible: Boolean,
    onAutoCall: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: androidx.compose.material3.ColorScheme = MaterialTheme.colorScheme
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .border(3.dp, colorScheme.primary, shape)
            .clip(shape)
            .clickable(onClick = onAutoCall, indication = null, interactionSource = remember { MutableInteractionSource() }),
        shape = shape,
        color = colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxHeight < NewVariantSpacing.compactThreshold
            val topPaddingV = if (compact) NewVariantSpacing.compactTopPaddingV else NewVariantSpacing.topRowPaddingV
            val centerPaddingV = if (compact) NewVariantSpacing.compactCenterPaddingV else NewVariantSpacing.centerZonePaddingV
            val footerTop = if (compact) NewVariantSpacing.compactFooterPaddingTop else NewVariantSpacing.footerPaddingTop
            val footerBottom = if (compact) NewVariantSpacing.compactFooterPaddingBottom else NewVariantSpacing.footerPaddingBottom
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NewVariantSpacing.topRowPaddingH)
                        .padding(top = topPaddingV, bottom = topPaddingV)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiveBadge(dotAlpha, colorScheme)
                        LiveCountText(values.displayCallCountText, colorScheme)
                    }
                }
                if (!compact) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = NewVariantSpacing.dividerThickness,
                        color = colorScheme.outlineVariant.copy(alpha = NewVariantSpacing.dividerAlpha)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = centerPaddingV, bottom = centerPaddingV),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (values.displayLetter.isNotEmpty()) {
                            Text(
                                text = values.displayLetter,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = LiveFonts.DMMono,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                ),
                                color = colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .offset(y = 6.dp)
                            )
                        }
                        val numSp = if (compact) 22.sp else 57.sp
                        LastCalledNumberStack(
                            outgoingText = previousOutgoingText,
                            outgoingVisible = previousOutgoingVisible,
                            currentNumberText = values.displayNumberText,
                            numberPopScale = numberPopScale,
                            modifier = Modifier,
                            numberContent = { text, mod ->
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = LiveFonts.DMMono,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = numSp,
                                        lineHeight = numSp
                                    ),
                                    color = colorScheme.primary,
                                    modifier = mod.semantics { heading(); traversalIndex = 2f }
                                )
                            }
                        )
                    }
                }
                if (!compact) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = NewVariantSpacing.dividerThickness,
                        color = colorScheme.outlineVariant.copy(alpha = NewVariantSpacing.dividerAlpha)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NewVariantSpacing.footerPaddingH)
                        .padding(top = footerTop, bottom = footerBottom),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (compact) 0.dp else 6.dp)
                ) {
                    if (!compact) {
                        Text(
                            text = stringResource(R.string.live_play_last_called, values.displayLastCalledText),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = LiveFonts.DMMono,
                                fontWeight = FontWeight.Normal
                            ),
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    if (!compact) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = stringResource(R.string.live_play_tap_to_auto_call),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.06.sp
                                ),
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCallCardV5(
    values: LiveDisplayValues,
    numberPopScale: Float,
    dotAlpha: Float,
    previousOutgoingText: String,
    previousOutgoingVisible: Boolean,
    onAutoCall: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: androidx.compose.material3.ColorScheme = MaterialTheme.colorScheme
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .border(3.dp, colorScheme.primary, shape)
            .clip(shape)
            .clickable(onClick = onAutoCall, indication = null, interactionSource = remember { MutableInteractionSource() }),
        shape = shape,
        color = colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxHeight < NewVariantSpacing.compactThreshold
            val topPaddingV = if (compact) NewVariantSpacing.compactTopPaddingV else NewVariantSpacing.topRowPaddingV
            val bodyPaddingV = if (compact) NewVariantSpacing.compactCenterPaddingV else NewVariantSpacing.centerZonePaddingV
            val footerTop = if (compact) NewVariantSpacing.compactFooterPaddingTop else NewVariantSpacing.footerPaddingTop
            val footerBottom = if (compact) NewVariantSpacing.compactFooterPaddingBottom else NewVariantSpacing.footerPaddingBottom
            val numberSp = if (compact) 56.sp else 88.sp
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.primary)
                        .padding(horizontal = NewVariantSpacing.topRowPaddingH, vertical = topPaddingV),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colorScheme.error.copy(alpha = dotAlpha), CircleShape)
                        )
                        Text(
                            text = stringResource(R.string.common_live_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.12.sp
                            ),
                            color = colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = values.displayCallCountText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NewVariantSpacing.footerPaddingH, vertical = bodyPaddingV),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(Dimens.radiusSmall))
                            .background(colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(Dimens.radiusSmall))
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (values.displayLetter.isNotEmpty()) {
                            Text(
                                text = values.displayLetter,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = LiveFonts.DMMono,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp
                                ),
                                color = colorScheme.primary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(colorScheme.primary.copy(alpha = 0.25f))
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        LastCalledNumberStack(
                            outgoingText = previousOutgoingText,
                            outgoingVisible = previousOutgoingVisible,
                            currentNumberText = values.displayNumberText,
                            numberPopScale = numberPopScale,
                            modifier = Modifier,
                            numberContent = { text, mod ->
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = LiveFonts.DMMono,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = numberSp,
                                        lineHeight = numberSp
                                    ),
                                    color = colorScheme.primary,
                                    modifier = mod.semantics { heading(); traversalIndex = 2f }
                                )
                            }
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = NewVariantSpacing.dividerThickness,
                    color = colorScheme.outlineVariant.copy(alpha = NewVariantSpacing.dividerAlpha)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = NewVariantSpacing.footerPaddingH)
                        .padding(top = footerTop, bottom = footerBottom),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!compact) {
                        Text(
                            text = stringResource(R.string.live_play_last_called, values.displayLastCalledText),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = LiveFonts.DMMono,
                                fontWeight = FontWeight.Normal
                            ),
                            color = colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(Modifier)
                    }
                    if (!compact) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Dimens.radiusPill))
                                .background(colorScheme.primary)
                                .clickable(onClick = onAutoCall, indication = null, interactionSource = remember { MutableInteractionSource() })
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = colorScheme.onPrimary
                                )
                                Text(
                                    text = stringResource(R.string.live_play_auto_call),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val BINGO_LETTERS = listOf("B", "I", "N", "G", "O")

@Composable
private fun LiveCallCardV6(
    values: LiveDisplayValues,
    numberPopScale: Float,
    dotAlpha: Float,
    previousOutgoingText: String,
    previousOutgoingVisible: Boolean,
    onAutoCall: () -> Unit,
    modifier: Modifier = Modifier,
    colorScheme: androidx.compose.material3.ColorScheme = MaterialTheme.colorScheme
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .border(3.dp, colorScheme.primary, shape)
            .clip(shape)
            .clickable(onClick = onAutoCall, indication = null, interactionSource = remember { MutableInteractionSource() }),
        shape = shape,
        color = colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxHeight < NewVariantSpacing.compactThreshold
            val topPaddingV = if (compact) NewVariantSpacing.compactTopPaddingV else NewVariantSpacing.topRowPaddingV
            val centerPaddingV = if (compact) NewVariantSpacing.compactCenterPaddingV else NewVariantSpacing.centerZonePaddingV
            val numberSp = if (compact) 48.sp else 68.sp
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 26.dp else 30.dp)
                        .background(colorScheme.primary)
                        .padding(horizontal = NewVariantSpacing.topRowPaddingH, vertical = topPaddingV),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(colorScheme.error.copy(alpha = dotAlpha), CircleShape)
                        )
                        Text(
                            text = stringResource(R.string.common_live_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.12.sp
                            ),
                            color = colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = values.displayCallCountText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = centerPaddingV),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (values.displayLetter.isNotEmpty()) {
                                Text(
                                    text = values.displayLetter,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = LiveFonts.DMMono,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp
                                    ),
                                    color = colorScheme.primary,
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .offset(y = 4.dp)
                                )
                            }
                            LastCalledNumberStack(
                                outgoingText = previousOutgoingText,
                                outgoingVisible = previousOutgoingVisible,
                                currentNumberText = values.displayNumberText,
                                numberPopScale = numberPopScale,
                                modifier = Modifier,
                                numberContent = { text, mod ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = LiveFonts.DMMono,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = numberSp,
                                            lineHeight = numberSp
                                        ),
                                        color = colorScheme.primary,
                                        modifier = mod.semantics { heading(); traversalIndex = 2f }
                                    )
                                }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.primary.copy(alpha = 0.06f))
                        .padding(vertical = if (compact) 4.dp else 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BINGO_LETTERS.forEach { letter ->
                        val active = letter.equals(values.displayLetter, ignoreCase = true)
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = LiveFonts.DMMono,
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = if (compact) 12.sp else 14.sp
                            ),
                            color = if (active) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                if (!compact) {
                    Text(
                        text = stringResource(R.string.live_play_last_called, values.displayLastCalledText),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = NewVariantSpacing.footerPaddingH)
                            .padding(top = NewVariantSpacing.footerPaddingTop, bottom = NewVariantSpacing.footerPaddingBottom),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStatusHeaderRow(
    calledCount: Int,
    dotAlpha: Float,
    colorScheme: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier,
    horizontalInset: Dp = 0.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset, vertical = Dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LiveBadge(dotAlpha = dotAlpha, colorScheme = colorScheme)
        LiveCallCountCapsule(calledCount = calledCount, colorScheme = colorScheme)
    }
}

@Composable
private fun LiveLastCalledPremiumChip(
    number: Int,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val letter = bingoLetter(number)
    val numberText = if (letter.isEmpty()) number.toString() else "$letter$number"
    Surface(
        modifier = modifier,
        shape = LiveStatusPillShape,
        color = colorScheme.surface,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            colorScheme.outlineVariant.copy(alpha = 0.22f),
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        ) {
            Text(
                text = stringResource(R.string.live_play_last_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp,
                ),
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            )
            Text(
                text = numberText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = LiveFonts.DMMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                ),
                color = colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LiveShareCalledNumbersButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = colorScheme.primary.copy(alpha = 0.10f)
    val borderC = colorScheme.primary.copy(alpha = 0.20f)
    val shareContentDescription = stringResource(R.string.live_play_share_called_numbers_cd)
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .semantics { contentDescription = shareContentDescription },
        shape = CircleShape,
        color = bg,
        border = BorderStroke(Dimens.cardBorderDefault, borderC),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconCompact),
                tint = colorScheme.primary.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun LiveHistoryToggleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = colorScheme.primary.copy(alpha = 0.10f)
    val borderC = colorScheme.primary.copy(alpha = 0.20f)
    val historyContentDescription = stringResource(R.string.live_play_open_called_numbers_cd)
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .semantics { contentDescription = historyContentDescription },
        shape = CircleShape,
        color = bg,
        border = BorderStroke(Dimens.cardBorderDefault, borderC),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconCompact),
                tint = colorScheme.primary.copy(alpha = 0.82f),
            )
        }
    }
}

/** LIVE + count + history action in one surface card (live play). */
@Composable
fun LiveRoomWithHistoryCard(
    uiState: LivePlayUiState,
    isCallLimitReached: Boolean,
    modifier: Modifier = Modifier,
    onOpenCalledNumbers: () -> Unit = {},
    onShareCalledNumbers: (() -> Unit)? = null,
    lastCalled: Int? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isSessionActive = uiState.effectiveStatus == RoomStatus.RUNNING || uiState.lastCalledAtMillis != null
    val infinite = rememberInfiniteTransition(label = "livePulse")
    val dotAlphaPulse by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    val dotAlpha = if (isSessionActive) dotAlphaPulse else 0.75f

    val cardShape = RoundedCornerShape(Dimens.radiusCard)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        color = colorScheme.surface,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            colorScheme.outlineVariant.copy(alpha = 0.28f)
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(colorScheme.surfaceContainerLowest)
        ) {
            Column(
                Modifier.padding(
                    horizontal = LiveCompactTopBarHPad,
                    vertical = Dimens.spacing4,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LiveCompactTopBarHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiveBadge(dotAlpha = dotAlpha, colorScheme = colorScheme)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = Dimens.spacing8),
                        contentAlignment = Alignment.Center,
                    ) {
                        val n = lastCalled ?: uiState.lastCalled
                        if (n != null) {
                            LiveLastCalledPremiumChip(number = n)
                        }
                    }
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                    ) {
                        LiveCallCountCapsule(
                            calledCount = uiState.calledNumbers.size,
                            colorScheme = colorScheme,
                        )
                        if (onShareCalledNumbers != null) {
                            LiveShareCalledNumbersButton(onClick = onShareCalledNumbers)
                        }
                        LiveHistoryToggleButton(onClick = onOpenCalledNumbers)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveRoomTopSection(
    uiState: LivePlayUiState,
    onFinish: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val isSessionActive = uiState.effectiveStatus == RoomStatus.RUNNING || uiState.lastCalledAtMillis != null
    val infinite = rememberInfiniteTransition(label = "livePulse")
    val dotAlphaPulse by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    val dotAlpha = if (isSessionActive) dotAlphaPulse else 0.75f

    if (compact) {
        GreenCardCompact(
            calledCount = uiState.calledNumbers.size,
            dotAlpha = dotAlpha,
            colorScheme = colorScheme,
            modifier = modifier
        )
    } else {
            GreenCard(
                calledCount = uiState.calledNumbers.size,
                dotAlpha = dotAlpha,
            colorScheme = colorScheme,
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RoundProgressCard(
    calledCount: Int,
    isBehindGreen: Boolean = false,
    onFinish: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    StatusCard(calledCount, isBehindGreen, onFinish, onReset, modifier)
}

@Composable
private fun StatusCard(
    calledCount: Int,
    isBehindGreen: Boolean,
    onFinish: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.surface else colorScheme.surface,
        animationSpec = tween(200),
        label = "container"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.onSurface.copy(alpha = 0.2f) else colorScheme.outline.copy(alpha = 0f),
        animationSpec = tween(200),
        label = "border"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "label"
    )
    val trackColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.onPrimary.copy(alpha = 0.25f) else colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "track"
    )
    val fillColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.onPrimary else colorScheme.primary,
        animationSpec = tween(200),
        label = "fill"
    )
    val countColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.onPrimary else colorScheme.primary,
        animationSpec = tween(200),
        label = "count"
    )
    val finishContainerColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.onPrimary else colorScheme.primary,
        animationSpec = tween(200),
        label = "finishContainer"
    )
    val finishContentColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.primary else colorScheme.onPrimary,
        animationSpec = tween(200),
        label = "finishContent"
    )
    val resetContainerColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.surface else colorScheme.primaryContainer,
        animationSpec = tween(200),
        label = "resetContainer"
    )
    val resetContentColor by animateColorAsState(
        targetValue = if (isBehindGreen) colorScheme.onPrimary else colorScheme.primary,
        animationSpec = tween(200),
        label = "resetContent"
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isBehindGreen) Modifier.border(1.dp, borderColor, RoundedCornerShape(Dimens.radiusLarge)) else Modifier),
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = containerColor,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = stringResource(R.string.live_play_round_progress_title),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.12.sp
                    ),
                    color = labelColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(Dimens.progressBarHeight)
                            .clip(RoundedCornerShape(Dimens.progressBarRadius))
                            .background(trackColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(Dimens.progressBarHeight)
                                .fillMaxWidth(progressFraction(calledCount))
                                .clip(RoundedCornerShape(Dimens.progressBarRadius))
                                .background(fillColor)
                        )
                    }
                    Text(
                        text = "$calledCount / $MAX_LIVE_CALLS",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = LiveFonts.DMMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = countColor
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.heightIn(min = Dimens.buttonHeight),
                    shape = RoundedCornerShape(Dimens.radiusButtonPill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = finishContainerColor,
                        contentColor = finishContentColor
                    ),
                    contentPadding = PaddingValues(horizontal = Dimens.spacing12, vertical = Dimens.spacing8),
                    elevation = ButtonDefaults.buttonElevation(Dimens.cardElevationSubtle, Dimens.cardElevationSubtle, 0.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(Dimens.iconCompact), tint = finishContentColor)
                    Spacer(Modifier.width(Dimens.spacing4))
                    Text(stringResource(R.string.live_play_finish), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = finishContentColor)
                }
                FilledTonalButton(
                    onClick = onReset,
                    modifier = Modifier.heightIn(min = Dimens.buttonHeight),
                    shape = RoundedCornerShape(Dimens.radiusButtonPill),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = resetContainerColor,
                        contentColor = resetContentColor
                    ),
                    contentPadding = PaddingValues(horizontal = Dimens.spacing12, vertical = Dimens.spacing8)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(Dimens.iconCompact), tint = resetContentColor)
                }
            }
        }
    }
}

@Composable
private fun GreenCardCompact(
    calledCount: Int,
    dotAlpha: Float,
    colorScheme: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surface,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(Dimens.cardBorderDefault, colorScheme.outlineVariant.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        LiveStatusHeaderRow(
            calledCount = calledCount,
            dotAlpha = dotAlpha,
                colorScheme = colorScheme,
            horizontalInset = Dimens.spacing16,
            )
    }
}

@Composable
private fun GreenCard(
    calledCount: Int,
    dotAlpha: Float,
    colorScheme: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    val innerInset = Dimens.screenHorizontalPadding
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.radiusXL),
        color = colorScheme.surface,
        border = BorderStroke(Dimens.cardBorderDefault, colorScheme.outlineVariant.copy(alpha = 0.45f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        LiveStatusHeaderRow(
            calledCount = calledCount,
            dotAlpha = dotAlpha,
            colorScheme = colorScheme,
            horizontalInset = innerInset,
        )
    }
}

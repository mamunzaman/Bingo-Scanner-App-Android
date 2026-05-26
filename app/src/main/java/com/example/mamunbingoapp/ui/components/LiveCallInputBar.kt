package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens

private const val LiveKeypadVisibilityAnimMs = 200
private val liveKeypadVisibilitySpec = tween<Float>(
    durationMillis = LiveKeypadVisibilityAnimMs,
    easing = FastOutSlowInEasing,
)

/** Live play bottom dock heights (input row + optional digit block) for list/scroll padding. */
object LivePlayCallKeypadMetrics {
    val topRowHeight: Dp get() = Dimens.inputBarHeight - Dimens.spacing8
    val keyHeight: Dp get() = Dimens.spacing32 + Dimens.spacing8
    val digitBlockTopGap: Dp get() = Dimens.spacing4
    val digitRowGap: Dp get() = 3.dp
    private val shellVerticalPad: Dp get() = Dimens.spacing8 + Dimens.spacing12
    val digitBlockHeight: Dp get() = digitBlockTopGap + keyHeight + digitRowGap + keyHeight
    val collapsedDockHeight: Dp get() = shellVerticalPad + topRowHeight
    val expandedDockHeight: Dp get() = collapsedDockHeight + digitBlockHeight
    val collapsedListScrollPadding: Dp get() = Dimens.spacing4
    val expandedListScrollPadding: Dp get() = digitBlockHeight + Dimens.spacing12
}

/**
 * Manual-entry-style numeric keypad for live rooms: progress, draft, clear, undo, call, digit rows.
 * Sits above [AppBottomBar]; shell/padding aligned with manual-entry numeric keypad.
 */
@Composable
fun LivePlayCallKeypad(
    @Suppress("UNUSED_PARAMETER") latestCalled: Int?,
    draft: String,
    onDraftChange: (String) -> Unit,
    canAddNumber: Boolean,
    actionInProgress: Boolean,
    showNumberKeypad: Boolean = true,
    onToggleNumberKeypad: () -> Unit = {},
    onCallClick: () -> Unit,
    onUndoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val keyHeight = LivePlayCallKeypadMetrics.keyHeight
    val keyShape = RoundedCornerShape(10.dp)
    val keyBg = scheme.surfaceContainerHighest.copy(alpha = 0.55f)
    val pillShape = RoundedCornerShape(Dimens.radiusMedium)
    val confirmDiameter = Dimens.buttonHeight
    val haptic = LocalHapticFeedback.current
    val actionsEnabled = !actionInProgress
    // Allow Add when there is text so invalid/edge values can be submitted; empty stays off (no feedback on type-only).
    val hasNonBlankInput = draft.trim().isNotEmpty()
    val callEnabled = canAddNumber && actionsEnabled && hasNonBlankInput
    val topRowHeight = Dimens.inputBarHeight - Dimens.spacing8
    val compactActionSize = topRowHeight
    val hasDraft = draft.isNotEmpty()
    val inputBg by animateColorAsState(
        targetValue = if (hasDraft) {
            scheme.surfaceContainerHighest.copy(alpha = 0.98f)
        } else {
            scheme.surfaceContainerHighest.copy(alpha = 0.9f)
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveInputBg"
    )
    val inputBorder by animateColorAsState(
        targetValue = if (hasDraft) {
            scheme.outlineVariant.copy(alpha = 0.72f)
        } else {
            scheme.outlineVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveInputBorder"
    )
    val clearInteraction = remember { MutableInteractionSource() }
    val clearPressed by clearInteraction.collectIsPressedAsState()
    val clearScale by animateFloatAsState(
        targetValue = if (clearPressed) 0.95f else 1f,
        animationSpec = tween(75, easing = FastOutSlowInEasing),
        label = "liveClearScale"
    )
    val undoInteraction = remember { MutableInteractionSource() }
    val undoPressed by undoInteraction.collectIsPressedAsState()
    val undoScale by animateFloatAsState(
        targetValue = if (undoPressed) 0.96f else 1f,
        animationSpec = tween(80, easing = FastOutSlowInEasing),
        label = "liveUndoScale"
    )
    val callInteraction = remember { MutableInteractionSource() }
    val callPressed by callInteraction.collectIsPressedAsState()
    val callScale by animateFloatAsState(
        targetValue = if (callPressed && callEnabled) 0.965f else 1f,
        animationSpec = tween(90, easing = FastOutSlowInEasing),
        label = "liveCallScale"
    )
    val callContainerColor by animateColorAsState(
        targetValue = if (callEnabled) scheme.primaryContainer else scheme.primaryContainer.copy(alpha = 0.55f),
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveCallContainer"
    )
    val callIconTint by animateColorAsState(
        targetValue = if (callEnabled) scheme.primary else scheme.primary.copy(alpha = 0.38f),
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveCallIconTint"
    )

    fun appendDigit(d: Int) {
        if (!canAddNumber || !actionsEnabled) return
        if (draft.length >= 2) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onDraftChange(draft + d)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = Dimens.radiusMedium,
            topEnd = Dimens.radiusMedium
        ),
        color = scheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.screenHorizontalPadding,
                    end = Dimens.screenHorizontalPadding,
                    top = Dimens.spacing8,
                    bottom = Dimens.spacing12
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(topRowHeight)
                        .clip(pillShape)
                        .background(inputBg)
                        .border(
                            width = Dimens.cardBorderDefault,
                            color = inputBorder,
                            shape = pillShape
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = Dimens.spacing12, end = Dimens.spacing8),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val inputFontSize = (topRowHeight.value * 0.42f)
                            .coerceIn(20f, 34f)
                            .sp
                        Crossfade(
                            targetState = draft.isEmpty(),
                            modifier = Modifier.weight(1f),
                            animationSpec = tween(85, easing = FastOutSlowInEasing),
                            label = "liveDraftEmpty"
                        ) { isEmpty ->
                            Text(
                                text = if (isEmpty) "1-75" else draft,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = if (isEmpty) FontWeight.Medium else FontWeight.Bold,
                                    fontSize = inputFontSize,
                                    lineHeight = inputFontSize,
                                    letterSpacing = 0.5.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = if (isEmpty) {
                                    scheme.onSurfaceVariant.copy(alpha = 0.65f)
                                } else {
                                    scheme.onSurface
                                },
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(Dimens.cardBorderDefault)
                                .height(Dimens.spacing20)
                                .background(scheme.outlineVariant.copy(alpha = 0.55f))
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = clearScale
                                    scaleY = clearScale
                                }
                                .clip(CircleShape)
                                .clickable(
                                    enabled = draft.isNotEmpty() && actionsEnabled,
                                    interactionSource = clearInteraction,
                                    indication = null
                                ) {
                                    if (draft.isNotEmpty()) onDraftChange("")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                modifier = Modifier.graphicsLayer(alpha = 0.7f),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp
                                ),
                                color = scheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                IconButton(
                    onClick = {
                        if (!actionsEnabled) return@IconButton
                        onUndoClick()
                    },
                    enabled = actionsEnabled,
                    interactionSource = undoInteraction,
                    modifier = Modifier
                        .size(compactActionSize)
                        .graphicsLayer {
                            scaleX = undoScale
                            scaleY = undoScale
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo last call",
                        tint = scheme.primary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(compactActionSize)
                        .graphicsLayer {
                            scaleX = callScale
                            scaleY = callScale
                        }
                        .clip(CircleShape)
                        .border(
                            width = Dimens.cardBorderDefault,
                            color = scheme.primary,
                            shape = CircleShape
                        )
                        .background(callContainerColor)
                        .clickable(
                            enabled = callEnabled,
                            interactionSource = callInteraction,
                            indication = null
                        ) { onCallClick() }
                        .semantics { contentDescription = "Call number" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconDefault + Dimens.spacing4),
                        tint = callIconTint
                    )
                }
                LiveKeypadToggleButton(
                    keypadOpen = showNumberKeypad,
                    onClick = onToggleNumberKeypad,
                    size = compactActionSize,
                )
            }
            AnimatedVisibility(
                visible = showNumberKeypad,
                enter = fadeIn(animationSpec = liveKeypadVisibilitySpec) +
                    expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(
                            durationMillis = LiveKeypadVisibilityAnimMs,
                            easing = FastOutSlowInEasing,
                        ),
                    ),
                exit = fadeOut(animationSpec = liveKeypadVisibilitySpec) +
                    shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(
                            durationMillis = LiveKeypadVisibilityAnimMs,
                            easing = FastOutSlowInEasing,
                        ),
                    ),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(LivePlayCallKeypadMetrics.digitBlockTopGap))
                    val rowA = listOf(1, 2, 3, 4, 5)
                    val rowB = listOf(6, 7, 8, 9, 0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (d in rowA) {
                            LivePlayKeypadDigitKey(
                                digit = d,
                                keyHeight = keyHeight,
                                keyShape = keyShape,
                                keyBgIdle = keyBg,
                                enabled = canAddNumber && actionsEnabled,
                                onDigit = { appendDigit(d) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(LivePlayCallKeypadMetrics.digitRowGap))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (d in rowB) {
                            LivePlayKeypadDigitKey(
                                digit = d,
                                keyHeight = keyHeight,
                                keyShape = keyShape,
                                keyBgIdle = keyBg,
                                enabled = canAddNumber && actionsEnabled,
                                onDigit = { appendDigit(d) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveKeypadToggleButton(
    keypadOpen: Boolean,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.radiusSmall)
    val toggleContentDescription = if (keypadOpen) {
        "Hide number keypad"
    } else {
        "Show number keypad"
    }
    val containerColor by animateColorAsState(
        targetValue = if (keypadOpen) {
            scheme.primary.copy(alpha = 0.14f)
        } else {
            scheme.surfaceContainerHighest.copy(alpha = 0.55f)
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveKeypadToggleBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (keypadOpen) {
            scheme.primary.copy(alpha = 0.36f)
        } else {
            scheme.outlineVariant.copy(alpha = 0.45f)
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveKeypadToggleBorder",
    )
    val iconTint by animateColorAsState(
        targetValue = if (keypadOpen) {
            scheme.primary
        } else {
            scheme.onSurfaceVariant.copy(alpha = 0.78f)
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveKeypadToggleIcon",
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = toggleContentDescription },
        shape = shape,
        color = containerColor,
        border = BorderStroke(Dimens.cardBorderDefault, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Dialpad,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconDefault),
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun RowScope.LivePlayKeypadDigitKey(
    digit: Int,
    keyHeight: Dp,
    keyShape: RoundedCornerShape,
    keyBgIdle: Color,
    enabled: Boolean,
    onDigit: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val digitFontSize = (keyHeight.value * 0.48f).coerceIn(20f, 34f).sp
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(75, easing = FastOutSlowInEasing),
        label = "liveKeypadDigit"
    )
    val keyBg = if (pressed) {
        scheme.surfaceContainerHighest.copy(alpha = 0.62f)
    } else {
        keyBgIdle
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .height(keyHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .clip(keyShape)
            .background(keyBg)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null
            ) { onDigit() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$digit",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = digitFontSize,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            color = scheme.onSurface
        )
    }
}

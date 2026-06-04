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
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.AppAlpha
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.APP_SECTION_BORDER_ALPHA

private const val LiveKeypadVisibilityAnimMs = 200
private val liveKeypadVisibilitySpec = tween<Float>(
    durationMillis = LiveKeypadVisibilityAnimMs,
    easing = FastOutSlowInEasing,
)

/** Live play bottom dock heights (input row + optional digit block) for list/scroll padding. */
object LivePlayCallKeypadMetrics {
    val topRowHeight: Dp get() = Dimens.inputBarHeight - Dimens.spacing8
    val keyHeight: Dp get() = AppNumberKeypadMetrics.keyHeight
    val digitBlockTopGap: Dp get() = Dimens.spacing8 + Dimens.spacing10
    val digitRowGap: Dp get() = AppNumberKeypadMetrics.digitRowGap
    val fabSafeBottomInset: Dp get() = AppNumberKeypadMetrics.fabSafeBottomInset
    private val shellVerticalPad: Dp get() = Dimens.spacing16 + Dimens.spacing16
    val digitBlockHeight: Dp get() =
        digitBlockTopGap + AppNumberKeypadMetrics.digitGridHeight + fabSafeBottomInset
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
    undoEnabled: Boolean = false,
    actionInProgress: Boolean,
    showNumberKeypad: Boolean = true,
    onToggleNumberKeypad: () -> Unit = {},
    onCallClick: () -> Unit,
    onUndoClick: () -> Unit,
    modifier: Modifier = Modifier,
    lockedOverlay: @Composable BoxScope.() -> Unit = {},
    contentAlpha: Float = 1f,
) {
    val scheme = MaterialTheme.colorScheme
    val callNumberA11y = stringResource(R.string.live_play_a11y_call_number)
    val pillShape = RoundedCornerShape(Dimens.radiusButtonPill)
    val consoleShape = RoundedCornerShape(
        topStart = Dimens.radiusXL,
        topEnd = Dimens.radiusXL,
    )
    val haptic = LocalHapticFeedback.current
    val actionsEnabled = !actionInProgress
    val inputVisuallyActive = canAddNumber
    // Allow Add when there is text so invalid/edge values can be submitted; empty stays off (no feedback on type-only).
    val hasNonBlankInput = draft.trim().isNotEmpty()
    val callEnabled = canAddNumber && actionsEnabled && hasNonBlankInput
    val topRowHeight = Dimens.inputBarHeight - Dimens.spacing8
    val compactActionSize = topRowHeight
    val hasDraft = draft.isNotEmpty()
    val inputBg by animateColorAsState(
        targetValue = when {
            !inputVisuallyActive -> scheme.surface
            hasDraft -> scheme.primaryContainer.copy(alpha = 0.38f)
            else -> scheme.surface
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveInputBg"
    )
    val inputBorder by animateColorAsState(
        targetValue = when {
            !inputVisuallyActive -> scheme.outlineVariant.copy(alpha = 0.24f)
            hasDraft -> scheme.primary.copy(alpha = 0.5f)
            else -> scheme.outlineVariant.copy(alpha = AppAlpha.AlphaBorder)
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
        targetValue = if (callEnabled) scheme.primary else scheme.surfaceContainerHigh,
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveCallContainer"
    )
    val callIconTint by animateColorAsState(
        targetValue = if (callEnabled) scheme.onPrimary else scheme.onSurfaceVariant.copy(alpha = 0.45f),
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
        modifier = modifier.fillMaxWidth(),
        shape = consoleShape,
        color = scheme.surfaceContainer,
        tonalElevation = Dimens.cardElevationSubtle,
        shadowElevation = Dimens.cardElevationDefault,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            scheme.primary.copy(alpha = APP_SECTION_BORDER_ALPHA),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.screenHorizontalPadding,
                    end = Dimens.screenHorizontalPadding,
                    top = Dimens.spacing16,
                    bottom = Dimens.spacing16,
                ),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = if (contentAlpha < 1f) {
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = contentAlpha }
                    } else {
                        Modifier.fillMaxWidth()
                    },
                ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.spacing4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
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
                            .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing4),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val inputFontSize = (topRowHeight.value * 0.46f)
                            .coerceIn(22f, 36f)
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
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = if (isEmpty) FontWeight.Medium else FontWeight.Bold,
                                    fontSize = inputFontSize,
                                    lineHeight = inputFontSize,
                                    letterSpacing = if (isEmpty) 0.5.sp else 1.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                ),
                                color = when {
                                    !inputVisuallyActive -> scheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    isEmpty -> scheme.onSurfaceVariant.copy(alpha = 0.82f)
                                    else -> scheme.onSurface
                                },
                                maxLines = 1,
                                textAlign = TextAlign.Center,
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
                                color = if (inputVisuallyActive) {
                                    scheme.primary
                                } else {
                                    scheme.onSurfaceVariant.copy(alpha = 0.34f)
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                LivePlayConsoleOutlinedAction(
                    onClick = {
                        if (undoEnabled && actionsEnabled) {
                            onUndoClick()
                        }
                    },
                    enabled = actionsEnabled && undoEnabled,
                    interactionSource = undoInteraction,
                    scale = undoScale,
                    size = compactActionSize,
                    contentDescription = stringResource(R.string.live_play_undo_cd),
                    inactive = !undoEnabled,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconDefault),
                        tint = if (actionsEnabled && undoEnabled) {
                            scheme.primary
                        } else {
                            scheme.onSurfaceVariant.copy(alpha = 0.34f)
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = Dimens.spacing4)
                        .size(compactActionSize)
                        .then(
                            if (callEnabled) {
                                Modifier.shadow(
                                    Dimens.cardElevationDefault,
                                    CircleShape,
                                    clip = false,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .graphicsLayer {
                            scaleX = callScale
                            scaleY = callScale
                        }
                        .clip(CircleShape)
                        .background(callContainerColor)
                        .clickable(
                            enabled = callEnabled,
                            interactionSource = callInteraction,
                            indication = null,
                        ) { onCallClick() }
                        .semantics { contentDescription = callNumberA11y },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconDefault + Dimens.spacing4),
                        tint = callIconTint,
                    )
                }
                LiveKeypadToggleButton(
                    keypadOpen = showNumberKeypad,
                    onClick = onToggleNumberKeypad,
                    size = compactActionSize,
                    inactive = !inputVisuallyActive,
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
                    HorizontalDivider(
                        modifier = Modifier.padding(top = Dimens.spacing8),
                        thickness = Dimens.cardBorderDefault,
                        color = scheme.outlineVariant.copy(alpha = Dimens.outlineDividerAlpha),
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacing10))
                    AppNumberKeypad(
                        onDigit = { appendDigit(it) },
                        enabled = canAddNumber && actionsEnabled,
                        bottomSafeSpacing = LivePlayCallKeypadMetrics.fabSafeBottomInset,
                    )
                }
            }
                }
                lockedOverlay()
            }
        }
    }
}

@Composable
private fun LivePlayConsoleOutlinedAction(
    onClick: () -> Unit,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    scale: Float,
    size: Dp,
    contentDescription: String,
    modifier: Modifier = Modifier,
    inactive: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.radiusButtonPill)
    val isActive = enabled && !inactive
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { this.contentDescription = contentDescription },
        shape = shape,
        color = if (isActive) {
            scheme.surfaceContainerHigh
        } else {
            scheme.surface
        },
        border = BorderStroke(
            Dimens.cardBorderDefault,
            if (isActive) {
                scheme.primary.copy(alpha = 0.45f)
            } else {
                scheme.outlineVariant.copy(alpha = 0.24f)
            },
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun LiveKeypadToggleButton(
    keypadOpen: Boolean,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
    inactive: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.radiusButtonPill)
    val toggleContentDescription = if (keypadOpen) {
        stringResource(R.string.live_play_a11y_hide_keypad)
    } else {
        stringResource(R.string.live_play_a11y_show_keypad)
    }
    val containerColor by animateColorAsState(
        targetValue = when {
            keypadOpen -> scheme.primaryContainer.copy(alpha = 0.55f)
            inactive -> scheme.surface
            else -> scheme.surfaceContainerHigh
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveKeypadToggleBg",
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            keypadOpen -> scheme.primary.copy(alpha = 0.55f)
            inactive -> scheme.outlineVariant.copy(alpha = 0.24f)
            else -> scheme.outlineVariant.copy(alpha = Dimens.outlineBorderAlpha)
        },
        animationSpec = tween(140, easing = FastOutSlowInEasing),
        label = "liveKeypadToggleBorder",
    )
    val iconTint by animateColorAsState(
        targetValue = when {
            keypadOpen -> scheme.primary
            inactive -> scheme.onSurfaceVariant.copy(alpha = 0.34f)
            else -> scheme.onSurfaceVariant.copy(alpha = 0.78f)
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


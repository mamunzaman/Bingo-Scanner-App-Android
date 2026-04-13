package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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

/**
 * Manual-entry-style numeric keypad for live rooms: progress, draft, clear, undo, call, digit rows.
 * Sits above [AppBottomBar]; shell/padding aligned with manual-entry numeric keypad.
 */
@Composable
fun LivePlayCallKeypad(
    progressText: String,
    draft: String,
    onDraftChange: (String) -> Unit,
    canAddNumber: Boolean,
    actionInProgress: Boolean,
    onCallClick: () -> Unit,
    onUndoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val keyHeight = Dimens.spacing32 + Dimens.spacing12
    val keyShape = RoundedCornerShape(Dimens.radiusSmall)
    val keyBg = scheme.surfaceContainerHighest.copy(alpha = 0.55f)
    val pillShape = RoundedCornerShape(Dimens.radiusPill)
    val confirmDiameter = Dimens.buttonHeight
    val haptic = LocalHapticFeedback.current
    val parsed = draft.trim().toIntOrNull()
    val isValidNumber = parsed != null && parsed in 1..75
    val actionsEnabled = !actionInProgress
    val callEnabled = canAddNumber && isValidNumber && actionsEnabled

    fun appendDigit(d: Int) {
        if (!canAddNumber || !actionsEnabled) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (draft.length < 2) onDraftChange(draft + d)
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
                    top = Dimens.spacing12,
                    bottom = Dimens.spacing16
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
                        .height(Dimens.inputBarHeight)
                        .clip(pillShape)
                        .border(
                            width = Dimens.cardBorderDefault,
                            color = scheme.primary,
                            shape = pillShape
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(scheme.primary)
                            .padding(horizontal = Dimens.spacing12),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4)
                    ) {
                        Column {
                            Text(
                                text = progressText.replace(" ", ""),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = scheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "called",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onPrimary.copy(alpha = 0.9f),
                                maxLines = 1
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(scheme.primaryContainer)
                            .padding(start = Dimens.spacing12, end = Dimens.spacing8),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                    ) {
                        Crossfade(
                            targetState = draft.isEmpty(),
                            modifier = Modifier.weight(1f),
                            animationSpec = tween(85, easing = FastOutSlowInEasing),
                            label = "liveDraftEmpty"
                        ) { isEmpty ->
                            Text(
                                text = if (isEmpty) "—" else draft,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = if (isEmpty) FontWeight.Medium else FontWeight.Bold,
                                    lineHeight = 40.sp,
                                    letterSpacing = if (isEmpty) 0.sp else 0.5.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                color = if (isEmpty) {
                                    scheme.onPrimaryContainer.copy(alpha = 0.5f)
                                } else {
                                    scheme.primary
                                },
                                maxLines = 1,
                                textAlign = TextAlign.Start
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(Dimens.cardBorderDefault)
                                .height(Dimens.spacing24)
                                .background(scheme.primary.copy(alpha = 0.35f))
                        )
                        Box(
                            modifier = Modifier
                                .size(Dimens.spacing32)
                                .clip(CircleShape)
                                .clickable(
                                    enabled = draft.isNotEmpty() && actionsEnabled,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (draft.isNotEmpty()) onDraftChange("")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×",
                                modifier = Modifier.padding(bottom = 2.dp),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 26.sp
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
                    modifier = Modifier.size(confirmDiameter)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo last call",
                        tint = scheme.primary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(confirmDiameter)
                        .clip(CircleShape)
                        .border(
                            width = Dimens.cardBorderDefault,
                            color = scheme.primary,
                            shape = CircleShape
                        )
                        .background(
                            if (callEnabled) scheme.primaryContainer
                            else scheme.primaryContainer.copy(alpha = 0.55f)
                        )
                        .clickable(
                            enabled = callEnabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCallClick()
                        }
                        .semantics { contentDescription = "Call number" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconDefault + Dimens.spacing4),
                        tint = if (callEnabled) scheme.primary else scheme.primary.copy(alpha = 0.38f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.spacing12))
            val rowA = listOf(1, 2, 3, 4, 5)
            val rowB = listOf(6, 7, 8, 9, 0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
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
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
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
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface
        )
    }
}

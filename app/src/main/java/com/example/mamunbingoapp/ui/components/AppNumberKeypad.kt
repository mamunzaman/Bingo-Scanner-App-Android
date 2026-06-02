package com.example.mamunbingoapp.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.AppAlpha
import com.example.mamunbingoapp.theme.Dimens

/** Shared numeric keypad metrics (Live Play is the visual source of truth). */
object AppNumberKeypadMetrics {
    val keyHeight: Dp get() = Dimens.spacing32 + Dimens.spacing8
    val digitRowGap: Dp get() = Dimens.spacing8
    val digitGridHeight: Dp get() = keyHeight + digitRowGap + keyHeight
    val fabSafeBottomInset: Dp get() = Dimens.spacing12
    val horizontalPadding: Dp get() = Dimens.spacing4
}

/**
 * Global 0–9 numeric keypad (two rows: 1–5, 6–0). Visual style matches Live Play.
 * Callers supply [onDigit] and optional [enabled]; screen-specific chrome stays outside.
 */
@Composable
fun AppNumberKeypad(
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyHeight: Dp = AppNumberKeypadMetrics.keyHeight,
    digitRowGap: Dp = AppNumberKeypadMetrics.digitRowGap,
    keyBackgroundIdle: Color? = null,
    bottomSafeSpacing: Dp = 0.dp,
    firstRow: List<Int> = listOf(1, 2, 3, 4, 5),
    secondRow: List<Int> = listOf(6, 7, 8, 9, 0),
) {
    val scheme = MaterialTheme.colorScheme
    val keyShape = RoundedCornerShape(Dimens.radiusMedium)
    val keyBgIdle = keyBackgroundIdle ?: scheme.surface
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppNumberKeypadMetrics.horizontalPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(digitRowGap),
        ) {
            for (d in firstRow) {
                AppNumberKeypadDigitKey(
                    digit = d,
                    keyHeight = keyHeight,
                    keyShape = keyShape,
                    keyBgIdle = keyBgIdle,
                    enabled = enabled,
                    onDigit = { onDigit(d) },
                )
            }
        }
        Spacer(modifier = Modifier.height(digitRowGap))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(digitRowGap),
        ) {
            for (d in secondRow) {
                AppNumberKeypadDigitKey(
                    digit = d,
                    keyHeight = keyHeight,
                    keyShape = keyShape,
                    keyBgIdle = keyBgIdle,
                    enabled = enabled,
                    onDigit = { onDigit(d) },
                )
            }
        }
        if (bottomSafeSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(bottomSafeSpacing))
        }
    }
}

@Composable
private fun RowScope.AppNumberKeypadDigitKey(
    digit: Int,
    keyHeight: Dp,
    keyShape: RoundedCornerShape,
    keyBgIdle: Color,
    enabled: Boolean,
    onDigit: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val digitFontSize = (keyHeight.value * 0.48f).coerceIn(20f, 34f).sp
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(75, easing = FastOutSlowInEasing),
        label = "appKeypadDigitScale",
    )
    val keyBg = when {
        pressed -> scheme.primaryContainer.copy(alpha = 0.42f)
        !enabled -> scheme.surfaceContainer.copy(alpha = 0.65f)
        else -> keyBgIdle
    }
    val keyBorderColor = when {
        pressed -> scheme.primary.copy(alpha = 0.4f)
        else -> scheme.outlineVariant.copy(alpha = AppAlpha.AlphaBorderStrong)
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .height(keyHeight)
            .then(
                if (!pressed && enabled) {
                    Modifier.shadow(Dimens.cardElevationSubtle, keyShape, clip = false)
                } else {
                    Modifier
                },
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .clip(keyShape)
            .background(keyBg)
            .border(1.5.dp, keyBorderColor, keyShape)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
            ) { onDigit() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$digit",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = digitFontSize,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            color = if (enabled) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

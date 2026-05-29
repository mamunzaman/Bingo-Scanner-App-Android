package com.example.mamunbingoapp.ui.components.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.CardBorderGreen
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.EmptyHistoryCardBg
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.ui.components.iosElevatedShadow

data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val emphasized: Boolean = false,
)

private val pillShape = RoundedCornerShape(Dimens.radiusMedium)
private val pillHeight = 40.dp
private val pillMinWidth = 132.dp
private val fabSize = 56.dp
private val fabHostSize = 72.dp
private const val pulseCycleMs = 3000
private const val ringStaggerMs = 200
private const val burstMs = 900
private const val burstEndMs = ringStaggerMs + burstMs

private val ringOneStrokeScaleSpec = infiniteRepeatable(
    animation = keyframes {
        durationMillis = pulseCycleMs
        1f at 0
        1.35f at 450 using FastOutSlowInEasing
        1f at burstMs
        1f at pulseCycleMs
    },
    repeatMode = RepeatMode.Restart,
)

private val ringOneStrokeAlphaSpec = infiniteRepeatable(
    animation = keyframes {
        durationMillis = pulseCycleMs
        0.5f at 0
        0.24f at 360
        0f at burstMs
        0f at pulseCycleMs
    },
    repeatMode = RepeatMode.Restart,
)

private val ringTwoGlowScaleSpec = infiniteRepeatable(
    animation = keyframes {
        durationMillis = pulseCycleMs
        1f at 0
        1f at ringStaggerMs
        1.22f at ringStaggerMs + 450 using FastOutSlowInEasing
        1f at burstEndMs
        1f at pulseCycleMs
    },
    repeatMode = RepeatMode.Restart,
)

private val ringTwoGlowAlphaSpec = infiniteRepeatable(
    animation = keyframes {
        durationMillis = pulseCycleMs
        0f at 0
        0.28f at ringStaggerMs
        0.12f at ringStaggerMs + 380
        0f at burstEndMs
        0f at pulseCycleMs
    },
    repeatMode = RepeatMode.Restart,
)

private val buttonBurstGlowSpec = infiniteRepeatable(
    animation = keyframes {
        durationMillis = pulseCycleMs
        0f at 0
        0.2f at 180
        0.38f at 480
        0.22f at burstMs
        0f at burstEndMs
        0f at pulseCycleMs
    },
    repeatMode = RepeatMode.Restart,
)

@Composable
fun QuickActionsScrollRow(
    items: List<QuickActionItem>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(end = Dimens.spacing16),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        items(items, key = { it.label }) { item ->
            QuickActionPillCard(
                label = item.label,
                icon = item.icon,
                onClick = item.onClick,
                emphasized = item.emphasized,
            )
        }
    }
}

@Composable
fun QuickActionPillCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val surfaceColor = if (emphasized) PrimaryContainer.copy(alpha = 0.88f) else EmptyHistoryCardBg
    val borderColor = if (emphasized) {
        Primary.copy(alpha = 0.42f)
    } else {
        CardBorderGreen.copy(alpha = 0.62f)
    }
    val iconBg = if (emphasized) Primary.copy(alpha = 0.18f) else IconContainerBg
    Row(
        modifier = modifier
            .defaultMinSize(minWidth = pillMinWidth, minHeight = pillHeight)
            .height(pillHeight)
            .iosElevatedShadow(elevation = if (emphasized) 5.dp else 3.dp, shape = pillShape)
            .clip(pillShape)
            .background(surfaceColor)
            .border(Dimens.cardBorderDefault, borderColor, pillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(Dimens.radiusSmall))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spacing10))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
            color = PrimaryDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun HomeQuickScanFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    val pulseTransition = rememberInfiniteTransition(label = "homeScanFabPulse")
    val ringOneStrokeScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = ringOneStrokeScaleSpec,
        label = "ringOneStrokeScale",
    )
    val ringOneStrokeAlpha by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = ringOneStrokeAlphaSpec,
        label = "ringOneStrokeAlpha",
    )
    val ringTwoGlowScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = ringTwoGlowScaleSpec,
        label = "ringTwoGlowScale",
    )
    val ringTwoGlowAlpha by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = ringTwoGlowAlphaSpec,
        label = "ringTwoGlowAlpha",
    )
    val buttonBurstGlow by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = buttonBurstGlowSpec,
        label = "buttonBurstGlow",
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "homeScanFabPressScale",
    )
    val fabGradient = Brush.verticalGradient(
        colors = listOf(Primary, PrimaryDark, PrimaryDark),
    )
    Box(
        modifier = modifier.size(fabHostSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(fabSize)
                .scale(ringOneStrokeScale)
                .alpha(ringOneStrokeAlpha)
                .border(width = 2.5.dp, color = PrimaryDark, shape = shape),
        )
        Box(
            modifier = Modifier
                .size(fabSize)
                .scale(ringTwoGlowScale)
                .alpha(ringTwoGlowAlpha)
                .clip(shape)
                .background(Primary.copy(alpha = 0.24f))
                .border(width = 1.5.dp, color = Primary.copy(alpha = 0.65f), shape = shape),
        )
        Box(
            modifier = Modifier
                .size(fabSize)
                .scale(pressScale)
                .alpha(buttonBurstGlow)
                .drawBehind {
                    drawCircle(
                        color = PrimaryDark.copy(alpha = 0.62f),
                        radius = size.minDimension / 2f,
                    )
                },
        )
        Box(
            modifier = Modifier
                .size(fabSize)
                .scale(pressScale)
                .iosElevatedShadow(elevation = 14.dp, shape = shape)
                .clip(shape)
                .background(fabGradient)
                .border(width = 1.5.dp, color = OnPrimary.copy(alpha = 0.7f), shape = shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Scan ticket",
                tint = OnPrimary,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

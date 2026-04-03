package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.common.bingoLetter

@Composable
fun CalledNumberChip(
    number: Int,
    isActive: Boolean,
    isNewlyAdded: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.iconAlertBox
) {
    val colorScheme = MaterialTheme.colorScheme
    val newItemSpec = tween<Float>(durationMillis = 280, easing = FastOutSlowInEasing)
    val scale by animateFloatAsState(
        targetValue = if (isNewlyAdded) 1.05f else 1f,
        animationSpec = newItemSpec
    )
    val alphaAnim = remember { Animatable(1f) }
    LaunchedEffect(isNewlyAdded) {
        if (isNewlyAdded) {
            alphaAnim.snapTo(0.92f)
            alphaAnim.animateTo(1f, animationSpec = newItemSpec)
        } else {
            alphaAnim.snapTo(1f)
        }
    }
    val shape = CircleShape
    val bgColor = if (isActive) colorScheme.primary else colorScheme.surfaceVariant
    val textColor = if (isActive) colorScheme.onPrimary else colorScheme.onSurface
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 18.sp
    )
    val numberText = "%02d".format(number)
    val mod = modifier
        .semantics(mergeDescendants = true) {
            contentDescription = if (isActive) "$numberText, latest called number" else numberText
        }
        .alpha(alphaAnim.value)
        .scale(scale)
        .size(size)
        .clip(shape)
        .then(
            if (isActive) Modifier.shadow(2.dp, shape)
            else Modifier
        )
        .background(bgColor, shape)
    Box(modifier = mod, contentAlignment = Alignment.Center) {
        Text(
            text = numberText,
            style = textStyle,
            color = textColor
        )
    }
}

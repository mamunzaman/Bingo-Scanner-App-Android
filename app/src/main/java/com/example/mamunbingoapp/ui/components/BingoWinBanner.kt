package com.example.mamunbingoapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Success

@Composable
fun BingoWinBanner(
    lineCount: Int,
    modifier: Modifier = Modifier,
    playEnterAnimation: Boolean = false,
    visible: Boolean = true,
    onExitComplete: (() -> Unit)? = null
) {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    if (playEnterAnimation && visible) {
        LaunchedEffect(Unit) {
            scale.snapTo(0.85f)
            alpha.snapTo(0f)
            scale.animateTo(1f, tween<Float>(220, easing = FastOutSlowInEasing))
            alpha.animateTo(1f, tween<Float>(180, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(visible) {
        if (!visible) {
            scale.animateTo(0.95f, tween<Float>(180, easing = FastOutSlowInEasing))
            alpha.animateTo(0f, tween<Float>(220, easing = FastOutSlowInEasing))
            onExitComplete?.invoke()
        }
    }
    val (title, subText) = when {
        lineCount >= 3 -> stringResource(R.string.live_play_win_jackpot) to stringResource(R.string.live_play_win_jackpot_sub)
        lineCount == 2 -> stringResource(R.string.live_play_win_two_lines) to stringResource(R.string.live_play_win_two_lines_sub)
        else -> stringResource(R.string.live_play_win_one_line) to stringResource(R.string.live_play_win_one_line_sub)
    }
    Row(
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .widthIn(min = 140.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(Dimens.radiusCard))
            .background(Success.copy(alpha = 0.15f))
            .padding(horizontal = Dimens.spacing14, vertical = Dimens.spacing10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.size(Dimens.iconAlertBox)
        )
        Spacer(modifier = Modifier.width(Dimens.spacing12))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Success,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

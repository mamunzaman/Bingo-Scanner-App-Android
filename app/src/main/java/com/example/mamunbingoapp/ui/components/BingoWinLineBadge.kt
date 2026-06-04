package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import kotlin.math.cos
import kotlin.math.sin

private val BingoWinLineBadgeTwoLineColor = Color(0xFF0D9488)
private val BingoWinLineBadgeJackpotColor = Color(0xFFE6A817)

private data class BingoWinLineBadgeStyle(
    val label: String,
    val background: Color,
    val icon: ImageVector,
)

private fun bingoWinLineBadgeStyle(lineCount: Int): BingoWinLineBadgeStyle? = when {
    lineCount <= 0 -> null
    lineCount >= 3 -> BingoWinLineBadgeStyle("JACKPOT", BingoWinLineBadgeJackpotColor, Icons.Filled.EmojiEvents)
    lineCount == 2 -> BingoWinLineBadgeStyle("2 BINGO", BingoWinLineBadgeTwoLineColor, Icons.Filled.Star)
    else -> BingoWinLineBadgeStyle("1 BINGO", Primary, Icons.Filled.Star)
}

@Composable
private fun BingoWinLineBadgeBurst(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 14.dp, height = 10.dp)) {
        val origin = Offset(size.width * 0.72f, size.height * 0.85f)
        val strokePx = 1.2.dp.toPx()
        val length = size.minDimension * 0.9f
        listOf(-145f, -110f, -75f).forEach { degrees ->
            val radians = Math.toRadians(degrees.toDouble()).toFloat()
            val end = Offset(
                x = origin.x + cos(radians) * length,
                y = origin.y + sin(radians) * length,
            )
            drawLine(
                color = color,
                start = origin,
                end = end,
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
            )
        }
    }
}

/**
 * Compact floating win sticker: 1 BINGO / 2 BINGO / JACKPOT by completed line count.
 * Used on Live carousel cards and Live Sheet Detail grid.
 */
@Composable
fun BingoWinLineBadge(
    lineCount: Int,
    modifier: Modifier = Modifier,
) {
    val style = bingoWinLineBadgeStyle(lineCount) ?: return
    val pillShape = RoundedCornerShape(100.dp)
    Box(modifier = modifier.wrapContentWidth()) {
        BingoWinLineBadgeBurst(
            color = style.background.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 2.dp),
        )
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .heightIn(min = 20.dp)
                .iosElevatedShadow(elevation = 3.dp, shape = pillShape)
                .clip(pillShape)
                .background(style.background)
                .padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = OnPrimary,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = style.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    letterSpacing = 0.2.sp,
                ),
                color = OnPrimary,
                maxLines = 1,
            )
        }
    }
}

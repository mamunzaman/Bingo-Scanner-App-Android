package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.CardBorderGreen
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.IconContainerBg
import com.example.mamunbingoapp.theme.Outline
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.theme.Slate600
import java.util.Locale

/** Target visible cards in viewport (~2.3–2.6); card width derived with [StatRowGap] between items. */
private const val VisibleCardsTarget = 2.45f

private val StatRowGap = Dimens.spacing8

private val MetricsEdgeFadeWidth = 28.dp

private data class StatEntry(
    val label: String,
    val value: String,
    val style: SummaryStatCardStyle,
)

/**
 * Import Ticket metrics: [LazyRow] with fixed card width; edge fades only when the list can scroll further
 * left/right (hidden at rest / end / when content does not overflow).
 */
@Composable
fun ScanResultSummaryCard(
    numbersDetected: String,
    gridLabel: String,
    confidenceFraction: Float,
    losNumber: String?,
    serialNumber: String?,
    modifier: Modifier = Modifier,
    numbersLabel: String,
    gridColumnLabel: String,
    confidenceLabel: String,
    losLabel: String,
    serialLabel: String,
) {
    val frac = confidenceFraction.coerceIn(0f, 1f)
    val pctText = "${(frac * 1000f).toInt() / 10f}%".let { s ->
        if (s.endsWith(".0%")) s.replace(".0%", "%") else s
    }
    val miniShape = RoundedCornerShape(Dimens.radiusSmall)
    val losDisp = losNumber?.takeIf { it.isNotBlank() } ?: "—"
    val serDisp = serialNumber?.takeIf { it.isNotBlank() } ?: "—"
    val fadePx = with(LocalDensity.current) { MetricsEdgeFadeWidth.toPx() }
    val fadeEdgeColor = IconContainerBg

    val stats = remember(
        numbersLabel,
        numbersDetected,
        gridColumnLabel,
        gridLabel,
        confidenceLabel,
        pctText,
        losLabel,
        losDisp,
        serialLabel,
        serDisp,
    ) {
        listOf(
            StatEntry(numbersLabel, numbersDetected, SummaryStatCardStyle.Neutral),
            StatEntry(gridColumnLabel, gridLabel, SummaryStatCardStyle.Neutral),
            StatEntry(confidenceLabel, pctText, SummaryStatCardStyle.Match),
            StatEntry(losLabel, losDisp, SummaryStatCardStyle.Neutral),
            StatEntry(serialLabel, serDisp, SummaryStatCardStyle.Neutral),
        )
    }

    val listState = rememberLazyListState()
    val showLeftFade = listState.canScrollBackward
    val showRightFade = listState.canScrollForward

    Box(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.spacing8),
        ) {
            val cardWidth: Dp = run {
                val w = maxWidth
                val twoGaps = StatRowGap * 2
                val raw = (w - twoGaps) / VisibleCardsTarget
                raw.coerceAtLeast(84.dp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .drawWithContent {
                        drawContent()
                        if (fadePx > 0f && size.width > fadePx * 2.5f) {
                            if (showLeftFade) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(fadeEdgeColor, fadeEdgeColor.copy(alpha = 0f)),
                                        startX = 0f,
                                        endX = fadePx,
                                    ),
                                    size = Size(fadePx, size.height),
                                )
                            }
                            if (showRightFade) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(fadeEdgeColor.copy(alpha = 0f), fadeEdgeColor),
                                        startX = size.width - fadePx,
                                        endX = size.width,
                                    ),
                                    topLeft = Offset(size.width - fadePx, 0f),
                                    size = Size(fadePx, size.height),
                                )
                            }
                        }
                    },
            ) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(StatRowGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(
                        items = stats,
                        key = { "${it.label}_${it.value}" },
                    ) { stat ->
                        SummaryStatMiniCard(
                            label = stat.label,
                            value = stat.value,
                            style = stat.style,
                            shape = miniShape,
                            modifier = Modifier.width(cardWidth),
                        )
                    }
                }
            }
        }
    }
}

private enum class SummaryStatCardStyle {
    Neutral,
    Match,
}

@Composable
private fun SummaryStatMiniCard(
    label: String,
    value: String,
    style: SummaryStatCardStyle,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val labelText = label.uppercase(Locale.getDefault())
    val neutralBorder = BorderStroke(Dimens.cardBorderDefault, Outline.copy(alpha = 0.28f))
    when (style) {
        SummaryStatCardStyle.Neutral -> Surface(
            modifier = modifier,
            shape = shape,
            color = cs.surface,
            border = neutralBorder,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            SummaryStatMiniCardContent(
                labelText = labelText,
                value = value,
                labelColor = Slate600,
                valueColor = cs.onSurface,
            )
        }
        SummaryStatCardStyle.Match -> Surface(
            modifier = modifier,
            shape = shape,
            color = IconContainerBg,
            border = BorderStroke(Dimens.cardBorderDefault, CardBorderGreen),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            SummaryStatMiniCardContent(
                labelText = labelText,
                value = value,
                labelColor = PrimaryDark,
                valueColor = PrimaryDark,
            )
        }
    }
}

@Composable
private fun SummaryStatMiniCardContent(
    labelText: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.spacing8,
                vertical = Dimens.spacing10,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Dimens.spacing8))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

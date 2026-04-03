package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.EmptyHistoryCardBg
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.PrimaryPressed
import com.example.mamunbingoapp.theme.Slate600
import com.example.mamunbingoapp.theme.MamunBingoTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun EmptyHistoryActionCards(
    onPlayClick: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** When false, omits the title/subtitle inside the card (use with [EmptyHistoryState] above). */
    showTitleAndSubtitle: Boolean = true,
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRoundRect(color = EmptyHistoryCardBg, cornerRadius = androidx.compose.ui.geometry.CornerRadius(Dimens.radiusCard.toPx()))
                val stroke = Stroke(
                    width = Dimens.borderBingoUnmarked.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )
                drawRoundRect(
                    color = Primary,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(Dimens.radiusCard.toPx())
                )
            }
            .padding(Dimens.spacing16)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing12)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MiniBingoGrid()
            }
            if (showTitleAndSubtitle) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)
                ) {
                    Text(
                        text = "No sessions yet",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryPressed,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Your played sessions will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8)
            ) {
                PlayButton(onClick = onPlayClick, modifier = Modifier.weight(1f))
                ScanButton(onClick = onScanClick, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniBingoGrid(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.alpha(0.3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.borderBingoMarked)
        ) {
            for (row in 0..4) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.borderBingoMarked)) {
                    for (col in 0..4) {
                        val isHeader = row == 0
                        val isCenter = row == 2 && col == 2
                        val bg = when {
                            isHeader -> Primary
                            isCenter -> Primary
                            else -> PrimaryContainer
                        }
                        Box(
                            modifier = Modifier
                                .size(Dimens.iconDefault)
                                .clip(RoundedCornerShape(Dimens.borderBingoMarked))
                                .background(bg)
                        )
                    }
                }
            }
        }
}

@Composable
private fun PlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = Dimens.buttonHeight)
            .clip(shape)
            .background(Primary)
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.spacing12, horizontal = Dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconCompact),
            tint = OnPrimary
        )
        Spacer(modifier = Modifier.size(Dimens.spacing8))
        Text(
            text = "Play",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = OnPrimary
        )
    }
}

@Composable
private fun ScanButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = Dimens.buttonHeight)
            .clip(shape)
            .background(Color.White)
            .border(Dimens.borderBingoUnmarked, Primary, shape)
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.spacing12, horizontal = Dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconCompact),
            tint = Primary
        )
        Spacer(modifier = Modifier.size(Dimens.spacing8))
        Text(
            text = "Scan",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F5F4)
@Composable
private fun EmptyHistoryActionCardsPreview() {
    MamunBingoTheme {
        EmptyHistoryActionCards(
            onPlayClick = {},
            onScanClick = {},
            modifier = Modifier.padding(Dimens.screenHorizontalPadding)
        )
    }
}

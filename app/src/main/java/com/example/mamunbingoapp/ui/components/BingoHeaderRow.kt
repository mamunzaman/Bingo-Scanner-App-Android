package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.DarkBackground
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.theme.OnPrimary

private val BINGO_LETTERS = listOf("B", "I", "N", "G", "O")

@Composable
fun BingoHeaderRow(
    modifier: Modifier = Modifier,
    cellSpacing: Dp = Dimens.spacing8,
    cellShape: Shape = RoundedCornerShape(BingoBoxTokens.Radius),
    cellSize: Dp? = null,
    letterTextStyle: TextStyle? = null,
    useDarkStyle: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val rowModifier = if (cellSize != null) modifier else modifier.fillMaxWidth()
    val bg = if (useDarkStyle) DarkBackground else colorScheme.surface
    val textColor = if (useDarkStyle) OnPrimary else colorScheme.onSurface
    val boxModifier = if (useDarkStyle) {
        Modifier.clip(cellShape).background(bg)
    } else {
        Modifier.clip(cellShape).background(bg).border(BingoBoxTokens.BorderWidth, colorScheme.outlineVariant, cellShape)
    }
    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(cellSpacing)
    ) {
        BINGO_LETTERS.forEach { letter ->
            val cellModifier = if (cellSize != null) {
                Modifier.size(cellSize)
            } else {
                Modifier.weight(1f).aspectRatio(1f)
            }
            Box(
                modifier = cellModifier.then(boxModifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    style = letterTextStyle ?: MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoHeaderRowPreview() {
    MamunBingoTheme {
        BingoHeaderRow(modifier = Modifier.fillMaxWidth())
    }
}

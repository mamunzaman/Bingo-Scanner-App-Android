package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.LocalPrimaryBorder
import com.example.mamunbingoapp.theme.MamunBingoTheme

object BingoBoxTokens {
    val SizeDefault: Dp = 56.dp
    val SizeCompact: Dp = 40.dp
    val Radius: Dp = 16.dp
    val BorderWidth: Dp = 1.5.dp
    val SpacingBetweenBoxes: Dp = 10.dp
}

@Composable
fun BingoNumberBox(
    numberText: String,
    isMarked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp? = BingoBoxTokens.SizeDefault,
    shape: Shape = RoundedCornerShape(BingoBoxTokens.Radius),
    enabled: Boolean = true,
    isSelected: Boolean = false,
    showBorderWhenMarked: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val bg = when {
        isMarked -> colorScheme.primary
        isSelected -> colorScheme.primary.copy(alpha = 0.05f)
        else -> colorScheme.surface
    }
    val primaryBorder = LocalPrimaryBorder.current
    val borderColor = when {
        isMarked -> if (showBorderWhenMarked) primaryBorder else Color.Transparent
        isSelected -> colorScheme.primary
        else -> colorScheme.outlineVariant
    }
    val textColor = when {
        isMarked -> colorScheme.onPrimary
        isSelected -> colorScheme.primary
        else -> colorScheme.onSurface
    }
    val mod = modifier
        .then(if (size != null) Modifier.size(size) else Modifier)
        .clip(shape)
        .background(bg)
        .then(
            if (enabled) Modifier.border(BingoBoxTokens.BorderWidth, borderColor, shape)
            else Modifier
        )
    Box(modifier = mod, contentAlignment = Alignment.Center) {
        Text(
            text = numberText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

@Composable
fun BingoNumberBox(
    number: Int,
    isMarked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp? = BingoBoxTokens.SizeDefault,
    shape: Shape = RoundedCornerShape(BingoBoxTokens.Radius),
    enabled: Boolean = true,
    showBorderWhenMarked: Boolean = true
) {
    BingoNumberBox(
        numberText = "%02d".format(number),
        isMarked = isMarked,
        modifier = modifier,
        size = size,
        shape = shape,
        enabled = enabled,
        isSelected = false,
        showBorderWhenMarked = showBorderWhenMarked
    )
}

@Preview(showBackground = true)
@Composable
private fun BingoNumberBoxUnmarkedPreview() {
    MamunBingoTheme {
        BingoNumberBox(numberText = "21", isMarked = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoNumberBoxMarkedPreview() {
    MamunBingoTheme {
        BingoNumberBox(numberText = "42", isMarked = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoNumberBoxCompactPreview() {
    MamunBingoTheme {
        BingoNumberBox(numberText = "07", isMarked = true, size = BingoBoxTokens.SizeCompact)
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoNumberBoxRowPreview() {
    MamunBingoTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(BingoBoxTokens.SpacingBetweenBoxes)
        ) {
            BingoNumberBox(numberText = "08", isMarked = true)
            BingoNumberBox(numberText = "21", isMarked = false)
            BingoNumberBox(numberText = "42", isMarked = true)
            BingoNumberBox(numberText = "51", isMarked = false)
            BingoNumberBox(numberText = "73", isMarked = true)
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BingoNumberBoxRowDarkPreview() {
    MamunBingoTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(BingoBoxTokens.SpacingBetweenBoxes)
        ) {
            BingoNumberBox(numberText = "04", isMarked = false)
            BingoNumberBox(numberText = "26", isMarked = false)
            BingoNumberBox(numberText = "47", isMarked = true)
            BingoNumberBox(numberText = "74", isMarked = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoNumberBoxGrid5x5Preview() {
    val cells = listOf(
        "04" to true, "26" to false, "39" to false, "47" to true, "74" to true,
        "08" to false, "21" to false, "42" to true, "51" to false, "61" to false,
        "11" to false, "23" to false, "31" to true, "49" to false, "60" to false,
        "02" to true, "17" to false, "28" to false, "55" to false, "70" to false,
        "05" to false, "12" to true, "24" to false, "32" to false, "45" to false
    )
    MamunBingoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cells.chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (num, marked) ->
                        BingoNumberBox(numberText = num, isMarked = marked, size = BingoBoxTokens.SizeCompact)
                    }
                }
            }
        }
    }
}

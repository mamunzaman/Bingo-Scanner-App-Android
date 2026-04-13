package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.DarkBackground
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryContainer
import com.example.mamunbingoapp.theme.PrimaryDark

private val BINGO_LETTERS = listOf("B", "I", "N", "G", "O")

/** Same 5-column gap as the weighted grid row — use with [BingoHeaderRow] PrimaryGreen chips when matching a 5×5 grid below. */
internal fun importTicketPremiumFiveColumnGap(cellSpacing: Dp): Dp =
    cellSpacing.coerceAtLeast(Dimens.spacing5)

/** How header letter cells are filled: dark tiles (live play), outline surface (light), solid primary green **circle chips** (shared with Import premium + future Live), pale-strip premium, or Import alias for the same green chips. */
enum class BingoHeaderStyle {
    DarkLetters,
    LightOutlined,
    /** Primary green circular chips, white bold letters — reusable for Live play and Import sheet/grid. */
    PrimaryGreen,
    /** Import Ticket result: stronger letters on a soft tinted strip — clearer than plain text on pale green, lighter than solid black header. */
    ImportPreviewPremium,
    /** Same rendering as [PrimaryGreen] (green circle chips); kept as a named Import variant. */
    ImportTicketPremium,
}

@Composable
fun BingoHeaderRow(
    modifier: Modifier = Modifier,
    cellSpacing: Dp = Dimens.spacing8,
    cellShape: Shape = RoundedCornerShape(BingoBoxTokens.Radius),
    cellSize: Dp? = null,
    letterTextStyle: TextStyle? = null,
    style: BingoHeaderStyle = BingoHeaderStyle.DarkLetters,
    /** PrimaryGreen / ImportTicketPremium: must match the 5×5 grid gap (e.g. [importTicketPremiumFiveColumnGap]). */
    fiveColumnGap: Dp? = null,
) {
    val rowModifier = if (cellSize != null) modifier else modifier.fillMaxWidth()
    if (style == BingoHeaderStyle.ImportPreviewPremium) {
        ImportPreviewPremiumBingoHeader(
            modifier = rowModifier,
            cellSpacing = cellSpacing,
            letterTextStyle = letterTextStyle,
        )
    } else if (style == BingoHeaderStyle.PrimaryGreen || style == BingoHeaderStyle.ImportTicketPremium) {
        PrimaryGreenCircleChipBingoHeader(
            modifier = rowModifier,
            cellSpacing = cellSpacing,
            letterTextStyle = letterTextStyle,
            fiveColumnGap = fiveColumnGap,
        )
    } else {
        val colorScheme = MaterialTheme.colorScheme
        val headerPair = when (style) {
            BingoHeaderStyle.DarkLetters ->
                OnPrimary to Modifier.clip(cellShape).background(DarkBackground)
            BingoHeaderStyle.LightOutlined ->
                colorScheme.onSurface to Modifier
                    .clip(cellShape)
                    .background(colorScheme.surface)
                    .border(BingoBoxTokens.BorderWidth, colorScheme.outlineVariant, cellShape)
            BingoHeaderStyle.PrimaryGreen,
            BingoHeaderStyle.ImportTicketPremium,
            ->
                error("PrimaryGreen/ImportTicketPremium use PrimaryGreenCircleChipBingoHeader")
            BingoHeaderStyle.ImportPreviewPremium ->
                error("ImportPreviewPremium uses ImportPreviewPremiumBingoHeader")
        }
        val textColor = headerPair.first
        val boxModifier = headerPair.second
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(cellSpacing),
        ) {
            BINGO_LETTERS.forEach { letter ->
                val cellModifier = if (cellSize != null) {
                    Modifier.size(cellSize)
                } else {
                    Modifier.weight(1f).aspectRatio(1f)
                }
                Box(
                    modifier = cellModifier.then(boxModifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter,
                        style = letterTextStyle ?: MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportPreviewPremiumBingoHeader(
    modifier: Modifier,
    cellSpacing: Dp,
    letterTextStyle: TextStyle?,
) {
    val stripShape = RoundedCornerShape(Dimens.radiusLarge)
    val letterStyle = letterTextStyle ?: MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.55.sp,
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(stripShape)
                .background(PrimaryContainer.copy(alpha = 0.72f))
                .border(Dimens.cardBorderDefault, Primary.copy(alpha = 0.22f), stripShape)
                .padding(horizontal = Dimens.spacing14, vertical = Dimens.spacing12),
            horizontalArrangement = Arrangement.spacedBy(cellSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BINGO_LETTERS.forEach { letter ->
                Text(
                    text = letter,
                    modifier = Modifier.weight(1f),
                    style = letterStyle,
                    color = PrimaryDark,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Shared premium BINGO strip: same [Row] + [weight] + [aspectRatio] column system as the 5×5 grid. */
@Composable
private fun PrimaryGreenCircleChipBingoHeader(
    modifier: Modifier,
    cellSpacing: Dp,
    letterTextStyle: TextStyle?,
    fiveColumnGap: Dp? = null,
) {
    val letterStyle = letterTextStyle ?: MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.25.sp,
    )
    val gap = fiveColumnGap ?: minOf(cellSpacing, GreenChipMaxSpacing)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = BINGO_LETTERS[index],
                    style = letterStyle,
                    color = OnPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

private val GreenChipMaxSpacing = Dimens.spacing4

@Composable
private fun ImportTicketGreenChipBingoHeader(
    modifier: Modifier,
    cellSpacing: Dp,
    letterTextStyle: TextStyle?,
) = PrimaryGreenCircleChipBingoHeader(
    modifier,
    cellSpacing,
    letterTextStyle,
    fiveColumnGap = null,
)

@Preview(showBackground = true)
@Composable
private fun BingoHeaderRowPreview() {
    MamunBingoTheme {
        BingoHeaderRow(modifier = Modifier.fillMaxWidth())
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoHeaderRowImportPreviewPreview() {
    MamunBingoTheme {
        BingoHeaderRow(
            modifier = Modifier.fillMaxWidth(),
            style = BingoHeaderStyle.ImportPreviewPremium,
            cellSpacing = Dimens.spacing4,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoHeaderRowImportTicketPremiumPreview() {
    MamunBingoTheme {
        BingoHeaderRow(
            modifier = Modifier.fillMaxWidth(),
            style = BingoHeaderStyle.ImportTicketPremium,
            cellSpacing = Dimens.spacing4,
        )
    }
}

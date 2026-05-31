package com.example.mamunbingoapp.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens

private val BingoColumnLetters = listOf("B", "I", "N", "G", "O")

private const val ShareImageWidthPx = 1080
private const val ShareBoardGreen: Int = 0xFF4C8C00.toInt()
private const val ShareBoardFill: Int = 0xFFD9E9B4.toInt()
private const val MatrixColumnCount = 5
private val ChipMinSize = 30.dp
private val ChipMaxSize = 44.dp
private val MatrixRowGap = Dimens.spacing5

/** Visual tier for a called number (board + recent chips). */
enum class TvCallVisualTier {
    Latest,
    Recent,
    Older,
}

/** Distance from latest call in [callSequence] (distinct, call order). */
fun tvCallVisualTier(number: Int, callSequence: List<Int>): TvCallVisualTier {
    if (callSequence.isEmpty()) return TvCallVisualTier.Older
    val ordered = callSequence.distinct()
    val index = ordered.indexOf(number)
    if (index < 0) return TvCallVisualTier.Older
    return when (ordered.lastIndex - index) {
        0 -> TvCallVisualTier.Latest
        1, 2 -> TvCallVisualTier.Recent
        else -> TvCallVisualTier.Older
    }
}

/** Groups [calledNumbers] by B/I/N/G/O column, preserving call order within each column. */
fun groupCalledNumbersByColumn(calledNumbers: List<Int>): Map<String, List<Int>> {
    val buckets = BingoColumnLetters.associateWith { mutableListOf<Int>() }.toMutableMap()
    for (number in calledNumbers) {
        buckets[columnKeyForNumber(number)]?.add(number)
    }
    return buckets
}

/**
 * Called-number board: row-grid dot matrix with circular B/I/N/G/O header chips
 * and aligned number rows across all five columns.
 */
@Composable
fun TvBingoBoard(
    numbersByColumn: Map<String, List<Int>>,
    latest: Int?,
    boardGreen: Color,
    letterRed: Color,
    lineColor: Color,
    modifier: Modifier = Modifier,
    callSequence: List<Int> = emptyList(),
) {
    val rowCount = remember(numbersByColumn) {
        numbersByColumn.values.maxOfOrNull { it.size } ?: 0
    }
    val matrixRows = remember(numbersByColumn, rowCount) {
        (0 until rowCount).map { rowIndex ->
            BingoColumnLetters.map { letter ->
                numbersByColumn[letter]?.getOrNull(rowIndex)
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val chipSize = matrixChipSize(maxWidth)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.spacing8),
            verticalArrangement = Arrangement.spacedBy(MatrixRowGap),
        ) {
            BingoMatrixRow(chipSize = chipSize) { columnIndex ->
                BingoColumnHeaderChip(
                    letter = BingoColumnLetters[columnIndex],
                    size = chipSize,
                )
            }
            matrixRows.forEach { rowNumbers ->
                BingoMatrixRow(chipSize = chipSize) { columnIndex ->
                    val number = rowNumbers[columnIndex]
                    if (number != null) {
                        BingoNumberChip(
                            number = number,
                            tier = numberTier(number, callSequence, latest),
                            size = chipSize,
                        )
                    } else {
                        MatrixBlankCell(size = chipSize)
                    }
                }
            }
        }
    }
}

@Composable
private fun BingoMatrixRow(
    chipSize: Dp,
    cellContent: @Composable (columnIndex: Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(MatrixColumnCount) { columnIndex ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                cellContent(columnIndex)
            }
        }
    }
}

@Composable
private fun MatrixBlankCell(size: Dp) {
    Box(modifier = Modifier.size(size))
}

@Composable
private fun BingoColumnHeaderChip(
    letter: String,
    size: Dp,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = scheme.primary,
        border = BorderStroke(
            Dimens.cardBorderDefault,
            scheme.primary.copy(alpha = 0.45f),
        ),
        shadowElevation = Dimens.cardElevationSubtle,
        tonalElevation = 1.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = headerLabelFont(size),
                    lineHeight = headerLabelFont(size),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color = scheme.onPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BingoNumberChip(
    number: Int,
    tier: TvCallVisualTier,
    size: Dp,
) {
    val scheme = MaterialTheme.colorScheme
    val style = numberChipStyle(tier, scheme)

    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = style.background,
        border = style.border,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = style.fontWeight,
                    fontSize = numberLabelFont(size),
                    lineHeight = numberLabelFont(size),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color = style.foreground,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

private data class NumberChipStyle(
    val background: Color,
    val foreground: Color,
    val border: BorderStroke,
    val fontWeight: FontWeight,
)

private fun numberChipStyle(
    tier: TvCallVisualTier,
    scheme: ColorScheme,
): NumberChipStyle {
    val fill = scheme.primaryContainer.copy(alpha = 0.84f)
    val text = scheme.onSurface
    val softBorder = BorderStroke(
        Dimens.cardBorderDefault,
        scheme.outlineVariant.copy(alpha = 0.22f),
    )
    return when (tier) {
        TvCallVisualTier.Latest -> NumberChipStyle(
            background = fill,
            foreground = text,
            border = BorderStroke(
                Dimens.borderBingoUnmarked,
                scheme.primary.copy(alpha = 0.32f),
            ),
            fontWeight = FontWeight.Bold,
        )
        TvCallVisualTier.Recent,
        TvCallVisualTier.Older,
        -> NumberChipStyle(
            background = fill,
            foreground = text,
            border = softBorder,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun matrixChipSize(availableWidth: Dp): Dp {
    val horizontalGaps = Dimens.spacing4 * (MatrixColumnCount - 1)
    val cellWidth = (availableWidth - horizontalGaps) / MatrixColumnCount
    return minOf(cellWidth, ChipMaxSize).coerceAtLeast(ChipMinSize)
}

private fun headerLabelFont(chipSize: Dp): TextUnit {
    val sp = (chipSize.value * 0.46f).coerceIn(14f, 20f)
    return sp.sp
}

private fun numberLabelFont(chipSize: Dp): TextUnit {
    val sp = (chipSize.value * 0.40f).coerceIn(12f, 18f)
    return sp.sp
}

private fun numberTier(number: Int, callSequence: List<Int>, latest: Int?): TvCallVisualTier {
    if (callSequence.isNotEmpty()) return tvCallVisualTier(number, callSequence)
    return if (number == latest) TvCallVisualTier.Latest else TvCallVisualTier.Older
}

private fun columnKeyForNumber(number: Int): String =
    when (number) {
        in 1..15 -> "B"
        in 16..30 -> "I"
        in 31..45 -> "N"
        in 46..60 -> "G"
        else -> "O"
    }

/**
 * Renders a shareable PNG of the current called-number board (B/I/N/G/O matrix).
 */
fun createCalledNumbersShareBitmap(
    calledNumbers: List<Int>,
    title: String,
    timestamp: String,
): Bitmap {
    val numbersByColumn = groupCalledNumbersByColumn(calledNumbers)
    val rowCount = numbersByColumn.values.maxOfOrNull { it.size } ?: 0
    val padding = 56f
    val titleSize = 52f
    val subtitleSize = 30f
    val headerGap = 36f
    val chipSize = 76f
    val rowGap = 18f
    val colGap = 14f
    val boardTop = padding + titleSize + subtitleSize + headerGap
    val boardHeight = chipSize + rowGap + (chipSize + rowGap) * rowCount
    val height = (boardTop + boardHeight + padding).toInt().coerceAtLeast(720)

    val bitmap = Bitmap.createBitmap(ShareImageWidthPx, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        textSize = titleSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF616161.toInt()
        textSize = subtitleSize
    }
    canvas.drawText(title, padding, padding + titleSize * 0.82f, titlePaint)
    canvas.drawText(timestamp, padding, padding + titleSize + subtitleSize * 0.9f, subtitlePaint)

    val columnWidth = (ShareImageWidthPx - padding * 2 - colGap * (MatrixColumnCount - 1)) / MatrixColumnCount
    var y = boardTop

    BingoColumnLetters.forEachIndexed { columnIndex, letter ->
        val cx = padding + columnIndex * (columnWidth + colGap) + columnWidth / 2f
        drawShareLetterChip(canvas, letter, cx, y + chipSize / 2f, chipSize / 2f)
    }
    y += chipSize + rowGap

    for (row in 0 until rowCount) {
        BingoColumnLetters.forEachIndexed { columnIndex, letter ->
            val number = numbersByColumn[letter]?.getOrNull(row) ?: return@forEachIndexed
            val cx = padding + columnIndex * (columnWidth + colGap) + columnWidth / 2f
            val isLatest = tvCallVisualTier(number, calledNumbers) == TvCallVisualTier.Latest
            drawShareNumberChip(canvas, number, cx, y + chipSize / 2f, chipSize / 2f, isLatest)
        }
        y += chipSize + rowGap
    }

    return bitmap
}

private fun drawShareLetterChip(canvas: AndroidCanvas, letter: String, cx: Float, cy: Float, radius: Float) {
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ShareBoardGreen }
    canvas.drawCircle(cx, cy, radius, fillPaint)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textSize = radius * 0.95f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(letter, cx, textY, textPaint)
}

private fun drawShareNumberChip(
    canvas: AndroidCanvas,
    number: Int,
    cx: Float,
    cy: Float,
    radius: Float,
    isLatest: Boolean,
) {
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ShareBoardFill }
    canvas.drawCircle(cx, cy, radius, fillPaint)
    if (isLatest) {
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ShareBoardGreen
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.14f
        }
        canvas.drawCircle(cx, cy, radius - ringPaint.strokeWidth / 2f, ringPaint)
    } else {
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33000000.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawCircle(cx, cy, radius - 1f, ringPaint)
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.BLACK
        textSize = radius * 0.72f
        typeface = Typeface.create(
            Typeface.DEFAULT,
            if (isLatest) Typeface.BOLD else Typeface.NORMAL,
        )
        textAlign = Paint.Align.CENTER
    }
    val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(number.toString(), cx, textY, textPaint)
}

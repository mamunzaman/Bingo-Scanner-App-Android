package com.example.mamunbingoapp.ui.components.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.EmptyHistoryCardBg
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.theme.TicketPaperBorder
import com.example.mamunbingoapp.theme.TicketPaperCell
import com.example.mamunbingoapp.theme.TicketPaperTop
import com.example.mamunbingoapp.ui.components.BingoWinLineBadge
import com.example.mamunbingoapp.ui.components.bingoWinningMarker
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import com.example.mamunbingoapp.ui.model.BingoCellUi

private val CARD_WIDTH = 200.dp
private val CARD_HEIGHT = 296.dp
private val PROGRESS_SLOT_HEIGHT = 16.dp
private val PROGRESS_BAR_HEIGHT = 5.dp
private val STATUS_CHIP_MIN_WIDTH = 72.dp
private val BINGO_LETTERS = listOf("B", "I", "N", "G", "O")
private val CENTER_CELL_INDEX = 12
private val PAPER_TOP = TicketPaperTop
private val PAPER_BOTTOM = EmptyHistoryCardBg
private val MINI_GRID_GAP = 3.dp
private val MINI_CELL_RADIUS = 9.dp
private val MINI_HEADER_RADIUS = 10.dp
private val MINI_BORDER = 1.dp
private val LiveWinBadgeCardOffsetX = 8.dp
private val LiveWinBadgeCardOffsetY = (-10).dp
private val MINI_CELL_PAPER = TicketPaperCell
private val MINI_CELL_BORDER_COLOR = TicketPaperBorder
private val SHEET_PADDING = 7.dp
private val HEADER_ROW_HEIGHT = 30.dp

/** Optional B–O grid overrides (Live carousel only; Home passes null). */
data class ActiveTicketGridStyle(
    val sheetPadding: Dp,
    val gridGap: Dp,
    val numberFontExtraSp: Float = 0f,
    val letterBoxHeight: Dp? = null,
    val letterFontSize: TextUnit? = null,
    val numberFontSize: TextUnit? = null,
)

// Live grid typography when keypad is OPEN
val LiveSheetTicketGridStyleOpen = ActiveTicketGridStyle(
    sheetPadding = 10.dp,
    gridGap = 5.dp,
    letterBoxHeight = 24.dp,
    letterFontSize = 14.sp,
    numberFontSize = 13.sp,
)

// Live grid typography when keypad is CLOSED
val LiveSheetTicketGridStyleClosed = ActiveTicketGridStyle(
    sheetPadding = 10.dp,
    gridGap = 5.dp,
    letterBoxHeight = 28.dp,
    letterFontSize = 16.sp,
    numberFontSize = 18.sp,
)

/**
 * Default size for [ActiveTicketCompactSheetPreview] on sheet list rows (History, Live, etc.).
 * Full-size B–O preview remains on [ActiveTicketCard] via [ActiveTicketSheetPreview] (`compact = false`).
 */
object ActiveTicketListSheetPreview {
    val Width = 110.dp
    val Height = 138.dp

    fun sizeModifier(): Modifier = Modifier
        .width(Width)
        .height(Height)
}

/** Compact list/history preview — separate proportions from full Active Ticket grid. */
private val COMPACT_SHEET_PADDING = 4.dp
private val COMPACT_GRID_GAP = 4.dp
private val COMPACT_HEADER_HEIGHT = 17.dp
private val COMPACT_HEADER_RADIUS = 6.dp
private val COMPACT_CELL_RADIUS = 5.dp
private val COMPACT_SHEET_CORNER = 10.dp

data class ActiveTicketCellState(
    val display: String,
    val isCalled: Boolean,
)

/** Maps live/history grid cells to Active Ticket preview cell states (marked = called highlight). */
fun bingoGridCellsToActiveTicketCellStates(cells: List<BingoCellUi>): List<ActiveTicketCellState> {
    val normalized = when {
        cells.size >= 25 -> cells.take(25)
        else -> cells + List(25 - cells.size.coerceAtMost(25)) {
            BingoCellUi(null, false, false, false, false)
        }
    }
    return normalized.map { cell ->
        val display = cell.number?.trim().orEmpty().let { raw ->
            when {
                raw.isEmpty() -> ""
                raw.equals("FREE", ignoreCase = true) -> "FREE"
                else -> raw
            }
        }
        ActiveTicketCellState(display = display, isCalled = cell.isMarked)
    }
}

data class ActiveTicketCardModel(
    val sheetName: String,
    val losNumber: String? = null,
    val serieNumber: String? = null,
    val isInLiveRoom: Boolean,
    val calledCountLabel: String?,
    val calledProgress: Float,
    val showCalledProgress: Boolean,
    val neutralGrid: Boolean,
    val resultSourceLabel: String? = null,
    val cellStates: List<ActiveTicketCellState>,
)

@Composable
fun ActiveTicketCard(
    model: ActiveTicketCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.radiusLarge)
    val showGlow = model.isInLiveRoom
    val glowTransition = rememberInfiniteTransition(label = "activeTicketGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "activeTicketGlowAlpha",
    )
    val cs = MaterialTheme.colorScheme
    val borderColor = if (model.isInLiveRoom) {
        cs.primary.copy(alpha = if (showGlow) glowAlpha else 0.45f)
    } else {
        cs.outlineVariant.copy(alpha = 0.62f)
    }
    val cardElevation = if (model.isInLiveRoom) 8.dp else 5.dp

    BingoSheetTicketCard(
        sheetName = model.sheetName,
        losNumber = model.losNumber,
        serieNumber = model.serieNumber,
        cellStates = model.cellStates,
        neutralGrid = model.neutralGrid,
        onClick = onClick,
        modifier = modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT),
        shape = shape,
        borderColor = borderColor,
        shadowElevation = cardElevation,
        trailingHeader = {
            ActiveTicketStatusChip(isInLiveRoom = model.isInLiveRoom)
        },
        headerContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
            ) {
                model.calledCountLabel?.takeIf { it.isNotBlank() }?.let { countLabel ->
                    Text(
                        text = countLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (model.isInLiveRoom) cs.primary else cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ActiveTicketLosSerieRow(
                    losNumber = model.losNumber,
                    serieNumber = model.serieNumber,
                )
                model.resultSourceLabel?.takeIf { it.isNotBlank() }?.let { label ->
                    Text(
                        text = label,
                        style = activeTicketMetaSecondaryStyle(),
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        bottomSlot = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PROGRESS_SLOT_HEIGHT),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (model.showCalledProgress) {
                    LinearProgressIndicator(
                        progress = { model.calledProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PROGRESS_BAR_HEIGHT)
                            .clip(RoundedCornerShape(Dimens.radiusPill)),
                        color = cs.primary,
                        trackColor = cs.surfaceVariant.copy(alpha = 0.55f),
                    )
                }
            }
        },
    )
}

/**
 * Shared ticket paper card: title row, optional meta, LOS/SERIE, and flexible B–O grid
 * ([ActiveTicketSheetPreview]). Used by Home Active Ticket and Live room carousel cards.
 */
@Composable
fun BingoSheetTicketCard(
    sheetName: String,
    losNumber: String?,
    serieNumber: String?,
    cellStates: List<ActiveTicketCellState>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    neutralGrid: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(Dimens.radiusLarge),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f),
    shadowElevation: Dp = 5.dp,
    contentPadding: PaddingValues = PaddingValues(Dimens.spacing12),
    /** Pushes the B–O grid to the card bottom (square Live carousel cards). */
    anchorGridToBottom: Boolean = false,
    gridStyle: ActiveTicketGridStyle? = null,
    headerContent: @Composable () -> Unit = {},
    trailingHeader: @Composable () -> Unit = {},
    bottomSlot: @Composable () -> Unit = {},
    winningCells: Set<Int> = emptySet(),
    liveWinStyling: Boolean = false,
    winLineCount: Int = 0,
) {
    val titleInteraction = remember { MutableInteractionSource() }
    val showWinBadge = liveWinStyling && winLineCount > 0
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .iosElevatedShadow(elevation = shadowElevation, shape = shape)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PAPER_TOP, PAPER_BOTTOM),
                    ),
                )
                .border(Dimens.cardBorderDefault, borderColor, shape)
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = titleInteraction,
                    onClick = onClick,
                )
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sheetName,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = Dimens.spacing8),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                trailingHeader()
            }
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            headerContent()
            Spacer(modifier = Modifier.height(Dimens.spacing8))
            if (anchorGridToBottom) {
                Spacer(modifier = Modifier.weight(1f))
            }
            val gridModifier = if (anchorGridToBottom) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
                    .weight(1f)
                    .fillMaxSize()
            }
            ActiveTicketSheetPreview(
                cellStates = cellStates,
                neutralGrid = neutralGrid,
                layoutFromWidth = anchorGridToBottom,
                gridStyle = gridStyle,
                winningCells = winningCells,
                liveWinStyling = liveWinStyling,
                modifier = gridModifier,
            )
            bottomSlot()
            }
        }
        if (showWinBadge) {
            BingoWinLineBadge(
                lineCount = winLineCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(2f)
                    .offset(x = LiveWinBadgeCardOffsetX, y = LiveWinBadgeCardOffsetY),
            )
        }
    }
}

@Composable
private fun activeTicketMetaSecondaryStyle() =
    MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        lineHeight = 12.sp,
    )

@Composable
private fun activeTicketMetaLabelStyle() =
    MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        lineHeight = 11.sp,
        fontWeight = FontWeight.Medium,
    )

@Composable
private fun activeTicketMetaValueStyle() =
    MaterialTheme.typography.labelLarge.copy(
        fontSize = 15.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
    )

/** Shared LOS / SERIE row (Active Ticket + History list cards). */
@Composable
fun ActiveTicketLosSerieRow(
    losNumber: String?,
    serieNumber: String?,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle? = null,
    valueStyle: TextStyle? = null,
) {
    ActiveTicketLosSerieMeta(
        losNumber = losNumber,
        serieNumber = serieNumber,
        modifier = modifier,
        labelStyle = labelStyle,
        valueStyle = valueStyle,
    )
}

@Composable
private fun ActiveTicketLosSerieMeta(
    losNumber: String?,
    serieNumber: String?,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle? = null,
    valueStyle: TextStyle? = null,
) {
    val los = losNumber?.trim()?.takeIf { it.isNotEmpty() }
    val serie = serieNumber?.trim()?.takeIf { it.isNotEmpty() }
    if (los == null && serie == null) return

    val cs = MaterialTheme.colorScheme
    val resolvedLabelStyle = labelStyle ?: activeTicketMetaLabelStyle()
    val resolvedValueStyle = valueStyle ?: activeTicketMetaValueStyle()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        if (los != null) {
            ActiveTicketMetaField(
                label = stringResource(R.string.home_active_ticket_los_label),
                value = los,
                labelStyle = resolvedLabelStyle,
                valueStyle = resolvedValueStyle,
                labelColor = cs.onSurfaceVariant,
                valueColor = cs.onSurface,
                alignEnd = false,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (serie != null) {
            ActiveTicketMetaField(
                label = stringResource(R.string.home_active_ticket_serie_label),
                value = serie,
                labelStyle = resolvedLabelStyle,
                valueStyle = resolvedValueStyle,
                labelColor = cs.onSurfaceVariant,
                valueColor = cs.onSurface,
                alignEnd = true,
            )
        }
    }
}

@Composable
private fun ActiveTicketMetaField(
    label: String,
    value: String,
    labelStyle: TextStyle,
    valueStyle: TextStyle,
    labelColor: Color,
    valueColor: Color,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val horizontalAlign = if (alignEnd) Alignment.End else Alignment.Start
    val textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
    Column(
        modifier = modifier.wrapContentWidth(horizontalAlign),
        horizontalAlignment = horizontalAlign,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActiveTicketStatusChip(
    isInLiveRoom: Boolean,
    modifier: Modifier = Modifier,
) {
    val label = if (isInLiveRoom) {
        stringResource(R.string.home_active_ticket_status_live_room)
    } else {
        stringResource(R.string.home_active_ticket_status_saved)
    }
    val container = if (isInLiveRoom) {
        Primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    }
    val content = if (isInLiveRoom) Primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .width(STATUS_CHIP_MIN_WIDTH)
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(container)
            .padding(vertical = Dimens.spacing4),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp,
                fontSize = 10.sp,
            ),
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * Compact B–O sheet preview for list-style cards ([ActiveTicketListSheetPreview] size).
 * Use on History, Live room lists, and similar rows — not on the home [ActiveTicketCard] carousel.
 *
 * @param modifier Defaults to [ActiveTicketListSheetPreview.sizeModifier]; override only for one-off layouts.
 */
@Composable
fun ActiveTicketCompactSheetPreview(
    cellStates: List<ActiveTicketCellState>,
    neutralGrid: Boolean,
    modifier: Modifier = ActiveTicketListSheetPreview.sizeModifier(),
    liveWinStyling: Boolean = false,
) {
    ActiveTicketSheetPreview(
        cellStates = cellStates,
        neutralGrid = neutralGrid,
        modifier = modifier,
        compact = true,
        liveWinStyling = liveWinStyling,
    )
}

@Composable
private fun ActiveTicketSheetPreview(
    cellStates: List<ActiveTicketCellState>,
    neutralGrid: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    layoutFromWidth: Boolean = false,
    gridStyle: ActiveTicketGridStyle? = null,
    winningCells: Set<Int> = emptySet(),
    liveWinStyling: Boolean = false,
) {
    val sheetShape = RoundedCornerShape(if (compact) COMPACT_SHEET_CORNER else Dimens.spacing12)
    val gridGap = when {
        compact -> COMPACT_GRID_GAP
        gridStyle != null -> gridStyle.gridGap
        else -> MINI_GRID_GAP
    }
    val sheetPadding = when {
        compact -> COMPACT_SHEET_PADDING
        gridStyle != null -> gridStyle.sheetPadding
        else -> SHEET_PADDING
    }
    val numberFontExtraSp = if (compact) 0f else gridStyle?.numberFontExtraSp ?: 0f
    val letterBoxHeight = if (compact) null else gridStyle?.letterBoxHeight
    val letterFontSize = if (compact) null else gridStyle?.letterFontSize
    val numberFontSize = if (compact) null else gridStyle?.numberFontSize
    val sheetModifier = modifier
        .then(
            if (layoutFromWidth || compact) Modifier.wrapContentHeight() else Modifier.fillMaxSize()
        )
        .iosElevatedShadow(
            elevation = if (compact) 3.dp else 5.dp,
            shape = sheetShape,
        )
        .clip(sheetShape)
        .background(Color.White)
        .border(MINI_BORDER, MINI_CELL_BORDER_COLOR, sheetShape)
        .padding(sheetPadding)

    if (layoutFromWidth && !compact) {
        BoxWithConstraints(modifier = sheetModifier.fillMaxWidth()) {
            val contentWidth = maxWidth
            val cellSize = ((contentWidth - gridGap * 4) / 5f).coerceAtLeast(24.dp)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ActiveTicketBingoHeaderRow(
                    compact = compact,
                    gridGap = gridGap,
                    letterBoxHeight = letterBoxHeight,
                    letterFontSize = letterFontSize,
                )
                Spacer(modifier = Modifier.height(gridGap))
                cellStates.take(25).chunked(5).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cellSize),
                        horizontalArrangement = Arrangement.spacedBy(gridGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        row.forEachIndexed { colIndex, cell ->
                            val cellIndex = rowIndex * 5 + colIndex
                            ActiveTicketSheetCell(
                                cell = cell,
                                cellIndex = cellIndex,
                                neutralGrid = neutralGrid,
                                compact = compact,
                                isWinningCell = cellIndex in winningCells,
                                liveWinStyling = liveWinStyling,
                                numberFontExtraSp = numberFontExtraSp,
                                numberFontSize = numberFontSize,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(cellSize),
                            )
                        }
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = sheetModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActiveTicketBingoHeaderRow(
            compact = compact,
            gridGap = gridGap,
            letterBoxHeight = letterBoxHeight,
            letterFontSize = letterFontSize,
        )
        Spacer(modifier = Modifier.height(gridGap))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(gridGap),
        ) {
            cellStates.take(25).chunked(5).forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(gridGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        val cellIndex = rowIndex * 5 + colIndex
                        val cellModifier = if (compact) {
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        } else {
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                        }
                        ActiveTicketSheetCell(
                            cell = cell,
                            cellIndex = cellIndex,
                            neutralGrid = neutralGrid,
                            compact = compact,
                            isWinningCell = cellIndex in winningCells,
                            liveWinStyling = liveWinStyling,
                            numberFontExtraSp = numberFontExtraSp,
                            numberFontSize = numberFontSize,
                            modifier = cellModifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveTicketBingoHeaderRow(
    compact: Boolean = false,
    gridGap: Dp = if (compact) COMPACT_GRID_GAP else MINI_GRID_GAP,
    letterBoxHeight: Dp? = null,
    letterFontSize: TextUnit? = null,
) {
    val headerShape = RoundedCornerShape(
        if (compact) COMPACT_HEADER_RADIUS else MINI_HEADER_RADIUS,
    )
    val rowHeight = when {
        compact -> COMPACT_HEADER_HEIGHT
        letterBoxHeight != null -> letterBoxHeight
        else -> HEADER_ROW_HEIGHT
    }
    val letterFont = when {
        compact -> 9.sp
        letterFontSize != null -> letterFontSize
        else -> 14.sp
    }
    val letterLineHeight = when {
        compact -> 10.sp
        letterFontSize != null -> (letterFontSize.value * 1.07f).sp
        else -> 15.sp
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),
    ) {
        val letterWidth = (maxWidth - gridGap * 4) / 5
        Row(
            horizontalArrangement = Arrangement.spacedBy(gridGap),
        ) {
            BINGO_LETTERS.forEach { letter ->
                Box(
                    modifier = Modifier
                        .width(letterWidth)
                        .height(rowHeight)
                        .clip(headerShape)
                        .background(MaterialTheme.colorScheme.onSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = letterFont,
                            lineHeight = letterLineHeight,
                        ),
                        color = OnPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTicketSheetCell(
    cell: ActiveTicketCellState,
    cellIndex: Int,
    neutralGrid: Boolean,
    compact: Boolean = false,
    isWinningCell: Boolean = false,
    liveWinStyling: Boolean = false,
    numberFontExtraSp: Float = 0f,
    numberFontSize: TextUnit? = null,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val showWinMarker = liveWinStyling && isWinningCell && !neutralGrid
    val cellShape = RoundedCornerShape(
        if (compact) COMPACT_CELL_RADIUS else MINI_CELL_RADIUS,
    )
    val isCenterFree = cellIndex == CENTER_CELL_INDEX &&
        (cell.display.isBlank() || cell.display.equals("FREE", ignoreCase = true))
    val highlight = !neutralGrid && cell.isCalled
    val detailMarked = highlight && liveWinStyling
    val bg = when {
        detailMarked -> cs.primary
        highlight -> PrimaryDark
        else -> MINI_CELL_PAPER
    }
    val borderColor = MINI_CELL_BORDER_COLOR
    Box(
        modifier = modifier
            .clip(cellShape)
            .background(bg)
            .border(MINI_BORDER, borderColor, cellShape)
            .bingoWinningMarker(showWinMarker, cs.error),
        contentAlignment = Alignment.Center,
    ) {
        val label = when {
            isCenterFree -> "FREE"
            cell.display.isNotBlank() -> cell.display.trim()
            else -> ""
        }
        if (label.isNotEmpty()) {
            val cellFontSize = when {
                numberFontSize != null && !compact -> numberFontSize
                else -> (
                    when {
                        compact && isCenterFree -> 6f
                        compact && label.length >= 2 -> 9f
                        compact -> 10f
                        isCenterFree -> 7f
                        label.length >= 2 -> 10f
                        else -> 11f
                    } + numberFontExtraSp
                ).sp
            }
            val cellLineHeight = when {
                numberFontSize != null && !compact -> (numberFontSize.value * 1.1f).sp
                else -> (
                    when {
                        compact -> 10f
                        else -> 11f
                    } + numberFontExtraSp
                ).sp
            }
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = cellFontSize,
                    lineHeight = cellLineHeight,
                    fontWeight = FontWeight.Bold,
                ),
                color = when {
                    detailMarked -> cs.onPrimary
                    highlight -> Color.White
                    else -> cs.onSurface
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

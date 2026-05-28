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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.EmptyHistoryCardBg
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.ui.components.iosElevatedShadow

private val CARD_WIDTH = 200.dp
private val CARD_HEIGHT = 296.dp
private val META_BLOCK_HEIGHT = 36.dp
private val PROGRESS_SLOT_HEIGHT = 16.dp
private val PROGRESS_BAR_HEIGHT = 5.dp
private val STATUS_CHIP_MIN_WIDTH = 72.dp
private val BINGO_LETTERS = listOf("B", "I", "N", "G", "O")
private val CENTER_CELL_INDEX = 12
private val PAPER_TOP = Color(0xFFFFFEF9)
private val PAPER_BOTTOM = EmptyHistoryCardBg
private val MINI_GRID_GAP = 3.dp
private val MINI_CELL_RADIUS = 9.dp
private val MINI_HEADER_RADIUS = 10.dp
private val MINI_BORDER = 1.dp
private val MINI_CELL_PAPER = Color(0xFFFCFCFC)
private val MINI_CELL_BORDER_COLOR = Color(0xFFE2E2E2)
private val SHEET_PADDING = 7.dp
private val HEADER_ROW_HEIGHT = 30.dp

data class ActiveTicketCellState(
    val display: String,
    val isCalled: Boolean,
)

data class ActiveTicketCardModel(
    val ticketLabel: String,
    val drawDate: String,
    val isInLiveRoom: Boolean,
    val calledCountLabel: String?,
    val calledProgress: Float,
    val showCalledProgress: Boolean,
    val neutralGrid: Boolean,
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

    Box(
        modifier = modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT)
            .iosElevatedShadow(elevation = cardElevation, shape = shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(PAPER_TOP, PAPER_BOTTOM),
                ),
            )
            .border(Dimens.cardBorderDefault, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(
                start = Dimens.spacing12,
                end = Dimens.spacing12,
                top = Dimens.spacing16,
                bottom = Dimens.spacing12,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
            ) {
                Text(
                    text = model.ticketLabel,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = STATUS_CHIP_MIN_WIDTH + Dimens.spacing8),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                ActiveTicketStatusChip(
                    isInLiveRoom = model.isInLiveRoom,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(META_BLOCK_HEIGHT),
            ) {
                Text(
                    text = model.calledCountLabel.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (model.isInLiveRoom) cs.primary else cs.onSurfaceVariant,
                    minLines = 1,
                )
                Text(
                    text = "Draw ${model.drawDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ActiveTicketSheetPreview(
                cellStates = model.cellStates,
                neutralGrid = model.neutralGrid,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
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
        }
    }
}

@Composable
private fun ActiveTicketStatusChip(
    isInLiveRoom: Boolean,
    modifier: Modifier = Modifier,
) {
    val label = if (isInLiveRoom) "Live room" else "Saved ticket"
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

@Composable
private fun ActiveTicketSheetPreview(
    cellStates: List<ActiveTicketCellState>,
    neutralGrid: Boolean,
    modifier: Modifier = Modifier,
) {
    val sheetShape = RoundedCornerShape(Dimens.spacing12)
    Column(
        modifier = modifier
            .fillMaxSize()
            .iosElevatedShadow(elevation = 5.dp, shape = sheetShape)
            .clip(sheetShape)
            .background(Color.White)
            .border(MINI_BORDER, MINI_CELL_BORDER_COLOR, sheetShape)
            .padding(SHEET_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActiveTicketBingoHeaderRow()
        Spacer(modifier = Modifier.height(MINI_GRID_GAP))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(MINI_GRID_GAP),
        ) {
            cellStates.take(25).chunked(5).forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(MINI_GRID_GAP),
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        val cellIndex = rowIndex * 5 + colIndex
                        ActiveTicketSheetCell(
                            cell = cell,
                            cellIndex = cellIndex,
                            neutralGrid = neutralGrid,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveTicketBingoHeaderRow() {
    val headerShape = RoundedCornerShape(MINI_HEADER_RADIUS)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HEADER_ROW_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(MINI_GRID_GAP),
    ) {
        BINGO_LETTERS.forEach { letter ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(headerShape)
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 15.sp,
                    ),
                    color = OnPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ActiveTicketSheetCell(
    cell: ActiveTicketCellState,
    cellIndex: Int,
    neutralGrid: Boolean,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val cellShape = RoundedCornerShape(MINI_CELL_RADIUS)
    val isCenterFree = cellIndex == CENTER_CELL_INDEX &&
        (cell.display.isBlank() || cell.display.equals("FREE", ignoreCase = true))
    val highlight = !neutralGrid && cell.isCalled
    val bg = if (highlight) PrimaryDark else MINI_CELL_PAPER
    val borderColor = MINI_CELL_BORDER_COLOR
    Box(
        modifier = modifier
            .clip(cellShape)
            .background(bg)
            .border(MINI_BORDER, borderColor, cellShape),
        contentAlignment = Alignment.Center,
    ) {
        val label = when {
            isCenterFree -> "FREE"
            cell.display.isNotBlank() -> cell.display.trim()
            else -> ""
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = when {
                        isCenterFree -> 7.sp
                        label.length >= 2 -> 10.sp
                        else -> 11.sp
                    },
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = if (highlight) Color.White else cs.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

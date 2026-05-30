package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.model.BingoCellUi

/**
 * History-detail-style bingo grid: empty state or [BingoCardGrid] in
 * [BingoGridMode.PREVIEW] with [BingoCardGrid] history compact sizing.
 */
@Composable
fun BingoDetailGridCard(
    cells: List<BingoCellUi>?,
    winningCells: Set<Int> = emptySet(),
    historyDetailOuterMaxWidth: Dp? = null,
    historyDetailOuterMaxHeight: Dp? = null,
    emphasized: Boolean = false,
    historyDetailPlainGrid: Boolean = false,
) {
    val maxCellSize = if (emphasized) 52.dp else 44.dp
    val cellGap = Dimens.spacing8
    val innerContentMaxWidth = historyDetailOuterMaxWidth
    val innerContentMaxHeight = historyDetailOuterMaxHeight
    if (cells == null || cells.size != 25) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.bingo_detail_no_grid),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
            )
        }
    } else {
        BingoCardGrid(
            cells = cells,
            modifier = Modifier.fillMaxWidth(),
            mode = BingoGridMode.PREVIEW,
            winningCells = winningCells,
            onCellClick = {},
            historyDetailCompact = true,
            historyDetailContentMaxWidth = innerContentMaxWidth,
            historyDetailContentMaxHeight = innerContentMaxHeight,
            historyDetailMaxCellSize = maxCellSize,
            historyDetailCellGap = cellGap,
            historyDetailUseSheetSection = !historyDetailPlainGrid,
        )
    }
}

package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.model.BingoCellUi

/**
 * History-detail-style bingo grid: [BingoSheetSection] empty state or [BingoCardGrid] in
 * [BingoGridMode.PREVIEW] with [BingoCardGrid] history compact sizing.
 */
@Composable
fun BingoDetailGridCard(
    cells: List<BingoCellUi>?,
    winningCells: Set<Int> = emptySet(),
    historyDetailOuterMaxWidth: Dp? = null,
    historyDetailOuterMaxHeight: Dp? = null,
) {
    val sheetInset = Dimens.spacing16 * 2
    val innerContentMaxWidth = historyDetailOuterMaxWidth?.let { (it - sheetInset).coerceAtLeast(1.dp) }
    val innerContentMaxHeight = historyDetailOuterMaxHeight?.let { (it - sheetInset).coerceAtLeast(1.dp) }
    if (cells == null || cells.size != 25) {
        BingoSheetSection(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No grid saved for this session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }
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
        )
    }
}

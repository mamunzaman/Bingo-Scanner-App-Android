package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.model.BingoCellUi

@Composable
fun BingoCardGrid(
    cells: List<BingoCellUi>,
    modifier: Modifier = Modifier,
    mode: BingoGridMode = BingoGridMode.PLAY,
    cellSpacing: Dp = Dimens.spacing8,
    editUseFixedCellSize: Boolean = true,
    editCellSize: Dp = Dimens.bingoCellSize,
    winningCells: Set<Int> = emptySet(),
    onCellClick: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(cellSpacing)
    ) {
        BingoHeaderRow(
            modifier = Modifier.fillMaxWidth(),
            cellSpacing = cellSpacing,
            cellSize = if (mode == BingoGridMode.EDIT && editUseFixedCellSize) editCellSize else null
        )
        BingoGrid5x5(
            cells = cells,
            modifier = Modifier.fillMaxWidth(),
            mode = mode,
            cellSpacing = cellSpacing,
            editUseFixedCellSize = editUseFixedCellSize,
            editCellSize = editCellSize,
            winningCells = winningCells,
            onCellClick = onCellClick
        )
    }
}

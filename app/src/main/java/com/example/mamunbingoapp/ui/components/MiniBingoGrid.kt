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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.core.BingoPlayableNumbers

@Composable
fun MiniBingoGrid(cells: List<Boolean>) {
    val normalizedCells = remember(cells) {
        when {
            cells.size >= BingoPlayableNumbers.GRID_CELL_COUNT ->
                cells.take(BingoPlayableNumbers.GRID_CELL_COUNT)
            else -> cells + List(BingoPlayableNumbers.GRID_CELL_COUNT - cells.size) { false }
        }
    }
    val shape = RoundedCornerShape(2.dp)
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .background(scheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(4.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        normalizedCells.chunked(5).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEachIndexed { colIndex, matched ->
                    val cellIndex = rowIndex * 5 + colIndex
                    val isFreeCenter = cellIndex == BingoPlayableNumbers.FREE_CENTER_CELL_INDEX
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                when {
                                    isFreeCenter -> scheme.primaryContainer
                                    matched -> scheme.primary
                                    else -> scheme.outlineVariant
                                },
                                shape,
                            )
                    )
                }
            }
        }
    }
}

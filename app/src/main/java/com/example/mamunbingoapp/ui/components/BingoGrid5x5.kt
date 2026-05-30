package com.example.mamunbingoapp.ui.components

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.mamunbingoapp.core.BingoWinChecker
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.model.BingoCellUi

enum class BingoGridMode { EDIT, PLAY, PREVIEW }

private fun bingoNumberFontScaleForCellSize(cellSize: Dp, emphasize: Boolean = false): Float {
    val sizeFactor = if (emphasize) 0.52f else 0.48f
    val maxScale = if (emphasize) 1.58f else 1.45f
    val numberFontSp = (cellSize.value * sizeFactor).coerceIn(14f, if (emphasize) 30f else 32f)
    val baseNumberSp = 22f
    return (numberFontSp / baseNumberSp).coerceIn(0.7f, maxScale)
}

@Composable
fun BingoGrid5x5(
    cells: List<BingoCellUi>,
    modifier: Modifier = Modifier,
    mode: BingoGridMode = BingoGridMode.PLAY,
    cellSpacing: Dp = Dimens.spacing8,
    editUseFixedCellSize: Boolean = true,
    editCellSize: Dp = Dimens.bingoCellSize,
    winningCells: Set<Int> = emptySet(),
    onCellClick: (Int) -> Unit = {},
    /** When set (e.g. History detail compact), lays out 5×5 at this cell size without a square aspect-ratio block. */
    fixedLayoutCellSize: Dp? = null,
) {
    val normalized = when {
        cells.size >= 25 -> cells.take(25)
        else -> cells + List(25 - cells.size) { BingoCellUi(null, false, false, false, false) }
    }
    val markedSet = remember(normalized) {
        normalized.mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet()
    }
    val winResult = remember(markedSet) { BingoWinChecker.check(markedSet) }
    val effectiveWinningCells = winResult.winningCells
    val nearWinningCells = remember(markedSet, effectiveWinningCells) {
        val rows = (0..4).map { r -> (0..4).map { c -> r * 5 + c } }
        val cols = (0..4).map { c -> (0..4).map { r -> r * 5 + c } }
        val diagMain = listOf(0, 6, 12, 18, 24)
        val diagAnti = listOf(4, 8, 12, 16, 20)
        val allLines = rows + cols + listOf(diagMain, diagAnti)
        val oneMissing = allLines
            .filter { line -> line.count { it in markedSet } == 4 }
            .flatMap { line -> line.filter { it !in markedSet } }
            .toSet()
        oneMissing - effectiveWinningCells
    }
    val previousWinningCells = remember { mutableStateOf(emptySet<Int>()) }
    val previousNearWinningCells = remember { mutableStateOf(emptySet<Int>()) }
    val previousWinLineCount = remember { mutableStateOf(0) }
    val newWinningCells = effectiveWinningCells - previousWinningCells.value
    val newNearWinningCells = nearWinningCells - previousNearWinningCells.value
    LaunchedEffect(nearWinningCells) { previousNearWinningCells.value = nearWinningCells }
    val showReactionBanner = remember { mutableStateOf(false) }
    val bannerExiting = remember { mutableStateOf(false) }
    val reactionLineCount = remember { mutableStateOf(0) }
    val currentWinLineCount = winResult.winningLines.size
    LaunchedEffect(effectiveWinningCells) {
        previousWinningCells.value = effectiveWinningCells
        previousNearWinningCells.value = previousNearWinningCells.value - effectiveWinningCells
    }
    LaunchedEffect(currentWinLineCount) {
        if (currentWinLineCount <= previousWinLineCount.value) previousWinLineCount.value = currentWinLineCount
    }
    LaunchedEffect(mode, currentWinLineCount, previousWinLineCount.value) {
        if (mode == BingoGridMode.PLAY && currentWinLineCount > previousWinLineCount.value) {
            reactionLineCount.value = currentWinLineCount
            bannerExiting.value = false
            showReactionBanner.value = true
            previousWinLineCount.value = currentWinLineCount
        }
    }
    LaunchedEffect(showReactionBanner.value, reactionLineCount.value) {
        if (showReactionBanner.value) {
            kotlinx.coroutines.delay(1800)
            bannerExiting.value = true
        }
    }
    when (mode) {
        BingoGridMode.EDIT -> EditModeGrid(
            cells = normalized,
            modifier = modifier,
            winningCells = emptySet(),
            onCellClick = onCellClick,
            cellSpacing = cellSpacing,
            useFixedCellSize = editUseFixedCellSize,
            cellSize = editCellSize
        )
        BingoGridMode.PLAY, BingoGridMode.PREVIEW -> {
            val fixed = fixedLayoutCellSize
            Box(modifier = modifier) {
                if (fixed != null) {
                    FixedPlayModeGrid(
                        cells = normalized,
                        modifier = Modifier.fillMaxWidth(),
                        cellSize = fixed,
                        winningCells = effectiveWinningCells,
                        newWinningCells = newWinningCells,
                        nearWinningCells = nearWinningCells,
                        newNearWinningCells = newNearWinningCells,
                        onCellClick = onCellClick,
                        cellSpacing = cellSpacing,
                    )
                } else {
                    PlayModeGrid(
                        cells = normalized,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        winningCells = effectiveWinningCells,
                        newWinningCells = newWinningCells,
                        nearWinningCells = nearWinningCells,
                        newNearWinningCells = newNearWinningCells,
                        onCellClick = onCellClick,
                        cellSpacing = cellSpacing
                    )
                }
                if (mode == BingoGridMode.PLAY && (showReactionBanner.value || bannerExiting.value)) {
                    BingoWinBanner(
                        lineCount = reactionLineCount.value,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(Dimens.spacing12),
                        playEnterAnimation = true,
                        visible = !bannerExiting.value,
                        onExitComplete = {
                            showReactionBanner.value = false
                            bannerExiting.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditModeGrid(
    cells: List<BingoCellUi>,
    modifier: Modifier,
    winningCells: Set<Int>,
    onCellClick: (Int) -> Unit,
    cellSpacing: Dp,
    useFixedCellSize: Boolean,
    cellSize: Dp
) {
    if (useFixedCellSize) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            cells.chunked(5).forEachIndexed { rowIndex, row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(cellSpacing)
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        val index = rowIndex * 5 + colIndex
                        Box(modifier = Modifier.size(cellSize)) {
                            BingoCell(
                                cell = cell,
                                modifier = Modifier.fillMaxSize(),
                                onClick = { onCellClick(index) }.takeIf { cell.isEditable && !cell.isDisabled },
                                isWinning = index in winningCells
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = modifier.fillMaxWidth().aspectRatio(1f)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                cells.chunked(5).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(cellSpacing)
                    ) {
                        row.forEachIndexed { colIndex, cell ->
                            val index = rowIndex * 5 + colIndex
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                BingoCell(
                                    cell = cell,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = { onCellClick(index) }.takeIf { cell.isEditable && !cell.isDisabled },
                                    isWinning = index in winningCells
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedPlayModeGrid(
    cells: List<BingoCellUi>,
    modifier: Modifier,
    cellSize: Dp,
    winningCells: Set<Int>,
    newWinningCells: Set<Int>,
    nearWinningCells: Set<Int>,
    newNearWinningCells: Set<Int>,
    onCellClick: (Int) -> Unit,
    cellSpacing: Dp,
) {
    val outerDensity = LocalDensity.current
    val numberFontScale = remember(cellSize) { bingoNumberFontScaleForCellSize(cellSize, emphasize = true) }
    val numberScaledDensity = remember(outerDensity, numberFontScale) {
        Density(
            density = outerDensity.density,
            fontScale = outerDensity.fontScale * numberFontScale
        )
    }
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(cellSpacing),
        ) {
            cells.chunked(5).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
                    row.forEachIndexed { colIndex, cell ->
                        val index = rowIndex * 5 + colIndex
                        Box(modifier = Modifier.size(cellSize)) {
                            CompositionLocalProvider(LocalDensity provides numberScaledDensity) {
                                BingoCell(
                                    cell = cell,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = { onCellClick(index) }.takeIf { cell.isEditable && !cell.isDisabled },
                                    isWinning = index in winningCells,
                                    animateWinningPulse = index in newWinningCells,
                                    isNearWinning = index in nearWinningCells,
                                    animateNearWin = index in newNearWinningCells,
                                    numberFontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayModeGrid(
    cells: List<BingoCellUi>,
    modifier: Modifier,
    winningCells: Set<Int>,
    newWinningCells: Set<Int>,
    nearWinningCells: Set<Int>,
    newNearWinningCells: Set<Int>,
    onCellClick: (Int) -> Unit,
    cellSpacing: Dp
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth().aspectRatio(1f)) {
        val estimatedCellSize = ((maxWidth - (cellSpacing * 4)) / 5f).coerceAtLeast(20.dp)
        val outerDensity = LocalDensity.current
        val numberFontScale = remember(estimatedCellSize) { bingoNumberFontScaleForCellSize(estimatedCellSize) }
        val numberScaledDensity = remember(outerDensity, numberFontScale) {
            Density(
                density = outerDensity.density,
                fontScale = outerDensity.fontScale * numberFontScale
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            cells.chunked(5).forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(cellSpacing)
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        val index = rowIndex * 5 + colIndex
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                            CompositionLocalProvider(LocalDensity provides numberScaledDensity) {
                                BingoCell(
                                    cell = cell,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = { onCellClick(index) }.takeIf { cell.isEditable && !cell.isDisabled },
                                    isWinning = index in winningCells,
                                    animateWinningPulse = index in newWinningCells,
                                    isNearWinning = index in nearWinningCells,
                                    animateNearWin = index in newNearWinningCells
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BingoGridRowWinPreview() {
    val cells = List(25) { i ->
        val row = i / 5
        BingoCellUi(
            number = (i + 1).toString().padStart(2, '0'),
            isMarked = row == 0,
            isCalled = false,
            isEditable = false,
            isDisabled = false
        )
    }
    com.example.mamunbingoapp.theme.MamunBingoTheme {
        BingoGrid5x5(
            cells = cells,
            mode = BingoGridMode.PLAY,
            winningCells = setOf(0, 1, 2, 3, 4),
            onCellClick = {}
        )
    }
}

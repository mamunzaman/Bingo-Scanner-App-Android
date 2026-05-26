package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.OnPrimary
import com.example.mamunbingoapp.theme.Outline
import com.example.mamunbingoapp.theme.Primary
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
    onCellClick: (Int) -> Unit = {},
    bingoHeaderStyle: BingoHeaderStyle = BingoHeaderStyle.DarkLetters,
    /** History detail: width-based cell size, no square aspect-ratio grid block; tighter gaps. */
    historyDetailCompact: Boolean = false,
    /** History detail: inner content area after sheet padding; both set to size from width and height. */
    historyDetailContentMaxWidth: Dp? = null,
    historyDetailContentMaxHeight: Dp? = null,
) {
    if (historyDetailCompact) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val gap = Dimens.spacing4
            val headerGap = Dimens.spacing4
            val contentW = historyDetailContentMaxWidth ?: maxWidth
            val contentH = historyDetailContentMaxHeight
            val rawFromWidth = (contentW - gap * 4) / 5f
            val compactCellSize = if (contentH != null) {
                val rawFromHeight = (contentH - headerGap - gap * 4) / 6f
                minOf(rawFromWidth, rawFromHeight).coerceIn(22.dp, 44.dp)
            } else {
                rawFromWidth.coerceIn(22.dp, 44.dp)
            }
            val bingoGridWidth = gap * 4 + compactCellSize * 5
            val sheetOuterWidth = bingoGridWidth + Dimens.spacing16 * 2
            val letterStyle = when {
                compactCellSize < 28.dp ->
                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                compactCellSize < 36.dp ->
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                else ->
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = if (contentH != null) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.fillMaxWidth()
                },
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(sheetOuterWidth)) {
                    BingoSheetSection(premiumLayered = true) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(headerGap),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier.width(bingoGridWidth),
                                contentAlignment = Alignment.Center,
                            ) {
                                BingoHeaderRow(
                                    modifier = Modifier,
                                    cellSpacing = gap,
                                    cellSize = compactCellSize,
                                    letterTextStyle = letterStyle,
                                    style = bingoHeaderStyle,
                                )
                            }
                            BingoGrid5x5(
                                cells = cells,
                                modifier = Modifier.width(bingoGridWidth),
                                mode = mode,
                                cellSpacing = gap,
                                editUseFixedCellSize = editUseFixedCellSize,
                                editCellSize = editCellSize,
                                winningCells = winningCells,
                                onCellClick = onCellClick,
                                fixedLayoutCellSize = compactCellSize,
                            )
                        }
                    }
                }
            }
        }
    } else {
        val headerToGridSpacing = when (bingoHeaderStyle) {
            BingoHeaderStyle.ImportPreviewPremium -> Dimens.spacing8
            BingoHeaderStyle.ImportTicketPremium,
            BingoHeaderStyle.PrimaryGreen,
            -> Dimens.spacing16
            else -> cellSpacing
        }
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(headerToGridSpacing),
        ) {
            BingoHeaderRow(
                modifier = Modifier.fillMaxWidth(),
                cellSpacing = cellSpacing,
                cellSize = if (mode == BingoGridMode.EDIT && editUseFixedCellSize) editCellSize else null,
                style = bingoHeaderStyle,
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
}

/**
 * Read-only green bingo sheet for Import Ticket: title row + [BingoCardGrid] with primary-green BINGO letters;
 * LOS/serial belong in [ImportTicketLosSerialCard] above the sheet, not here.
 */
@Composable
fun ImportTicketBingoSheetCard(
    cells: List<BingoCellUi>,
    sheetTitle: String,
    dateText: String,
    modifier: Modifier = Modifier,
    cellSpacing: Dp = Dimens.spacing4,
) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val greenHeaderLabelColor = OnPrimary.copy(alpha = 0.78f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .clip(shape)
            .border(1.dp, Outline.copy(alpha = 0.42f), shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing8)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BINGO!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnPrimary
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = greenHeaderLabelColor
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing4))
            Text(
                text = sheetTitle,
                style = MaterialTheme.typography.bodySmall,
                color = greenHeaderLabelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(Dimens.spacing8)
        ) {
            BingoCardGrid(
                cells = cells,
                modifier = Modifier.fillMaxWidth(),
                mode = BingoGridMode.PLAY,
                cellSpacing = cellSpacing,
                bingoHeaderStyle = BingoHeaderStyle.PrimaryGreen,
            )
        }
    }
}

private fun manualEntryCommittedNumberDisplay(raw: String?): String? {
    val s = raw?.takeIf { it.isNotBlank() } ?: return null
    val n = s.toIntOrNull() ?: return s
    return n.toString()
}

/**
 * Same green bingo sheet card as Manual Entry; set [readOnly] for Import (or other) scan review — static meta + non-editable grid.
 */
@Composable
fun ManualEntryBingoCard(
    sheetName: String,
    dateText: String,
    isEditingSheetName: Boolean,
    sheetTitleStyle: TextStyle,
    onSheetNameChange: (String) -> Unit,
    onSheetNameDone: () -> Unit,
    onRequestEditTitle: () -> Unit,
    onToggleEditSheet: () -> Unit,
    onEditDate: () -> Unit,
    onDismissSheetTitleEdit: () -> Unit,
    losNummerText: String,
    onLosNummerChange: (String) -> Unit,
    serienNummerText: String,
    onSerienNummerChange: (String) -> Unit,
    cells: List<BingoCellUi>,
    selectedIndex: Int,
    draftPerCell: List<String>,
    onCellSelected: (Int) -> Unit,
    sheetTitleFocusRequester: FocusRequester,
    sheetNameFieldLabel: String? = null,
    sheetNameRenameHelper: String? = null,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
    compactGreenHeader: Boolean = false
) {
    val sheetTitleOutsideTapInteraction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val dividerLine = OnPrimary.copy(alpha = 0.12f)
    val metaHeadlineStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
        color = OnPrimary
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .clip(shape)
            .border(1.dp, Outline.copy(alpha = 0.42f), shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)
                .padding(
                    horizontal = Dimens.spacing12,
                    vertical = if (compactGreenHeader) Dimens.spacing5 else Dimens.spacing8
                )
        ) {
            val greenHeaderLabelColor = OnPrimary.copy(alpha = 0.78f)
            if (readOnly) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "BINGO!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = OnPrimary,
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = greenHeaderLabelColor,
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing4))
                    Text(
                        text = sheetName.ifBlank { "Untitled sheet" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = greenHeaderLabelColor,
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimens.spacing5),
                    thickness = 1.dp,
                    color = dividerLine,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LOSNUMMER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.6.sp
                            ),
                            color = greenHeaderLabelColor
                        )
                        Text(
                            text = losNummerText.ifBlank { "—" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.spacing8),
                            style = metaHeadlineStyle,
                            color = OnPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = Dimens.spacing8)
                            .width(1.dp)
                            .height(Dimens.spacing24)
                            .background(dividerLine)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "SERIENNUMMER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.6.sp
                            ),
                            color = greenHeaderLabelColor,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = serienNummerText.ifBlank { "—" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.spacing8),
                            style = metaHeadlineStyle.copy(textAlign = TextAlign.End),
                            color = OnPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "BINGO!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = OnPrimary,
                            modifier = Modifier.then(
                                if (isEditingSheetName) {
                                    Modifier.clickable(
                                        indication = null,
                                        interactionSource = sheetTitleOutsideTapInteraction
                                    ) { onDismissSheetTitleEdit() }
                                } else {
                                    Modifier
                                }
                            )
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = greenHeaderLabelColor,
                            modifier = Modifier.clickable {
                                if (isEditingSheetName) onDismissSheetTitleEdit()
                                onEditDate()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacing4))
                    if (!sheetNameFieldLabel.isNullOrBlank()) {
                        Text(
                            text = sheetNameFieldLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = OnPrimary,
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacing4))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { if (!isEditingSheetName) onRequestEditTitle() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (isEditingSheetName) {
                                BasicTextField(
                                    value = sheetName,
                                    onValueChange = onSheetNameChange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(sheetTitleFocusRequester),
                                    textStyle = sheetTitleStyle.copy(
                                        color = OnPrimary,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(OnPrimary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = { onSheetNameDone() }
                                    ),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (sheetName.isEmpty()) {
                                                Text(
                                                    text = "Untitled sheet",
                                                    style = sheetTitleStyle.copy(
                                                        fontWeight = FontWeight.Normal
                                                    ),
                                                    color = greenHeaderLabelColor
                                                )
                                            }
                                            inner()
                                        }
                                    }
                                )
                            } else {
                                Text(
                                    text = sheetName.ifBlank { "Untitled sheet" },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = greenHeaderLabelColor,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Rename sheet",
                            modifier = Modifier
                                .size(Dimens.iconAlert)
                                .clickable { onToggleEditSheet() },
                            tint = greenHeaderLabelColor
                        )
                    }
                    if (!sheetNameRenameHelper.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(Dimens.spacing4))
                        Text(
                            text = sheetNameRenameHelper,
                            style = MaterialTheme.typography.labelSmall,
                            color = greenHeaderLabelColor.copy(alpha = 0.92f),
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = Dimens.spacing5)
                        .then(
                            if (isEditingSheetName) {
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = sheetTitleOutsideTapInteraction
                                ) { onDismissSheetTitleEdit() }
                            } else {
                                Modifier
                            }
                        ),
                    thickness = 1.dp,
                    color = dividerLine
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isEditingSheetName) {
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = sheetTitleOutsideTapInteraction
                                ) { onDismissSheetTitleEdit() }
                            } else {
                                Modifier
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LOSNUMMER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.6.sp
                            ),
                            color = greenHeaderLabelColor
                        )
                        BasicTextField(
                            value = losNummerText,
                            onValueChange = onLosNummerChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.spacing8),
                            textStyle = metaHeadlineStyle,
                            singleLine = true,
                            cursorBrush = SolidColor(OnPrimary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (losNummerText.isEmpty()) {
                                        Text(
                                            text = "—",
                                            style = metaHeadlineStyle,
                                            color = greenHeaderLabelColor
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = Dimens.spacing8)
                            .width(1.dp)
                            .height(Dimens.spacing24)
                            .background(dividerLine)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "SERIENNUMMER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.6.sp
                            ),
                            color = greenHeaderLabelColor,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        BasicTextField(
                            value = serienNummerText,
                            onValueChange = onSerienNummerChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.spacing8),
                            textStyle = metaHeadlineStyle.copy(textAlign = TextAlign.End),
                            singleLine = true,
                            cursorBrush = SolidColor(OnPrimary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (serienNummerText.isEmpty()) {
                                        Text(
                                            text = "—",
                                            style = metaHeadlineStyle,
                                            color = greenHeaderLabelColor,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    start = Dimens.spacing12,
                    top = Dimens.spacing12,
                    end = Dimens.spacing12,
                    bottom = Dimens.spacing12
                )
                .clickable(
                    enabled = isEditingSheetName && !readOnly,
                    indication = null,
                    interactionSource = sheetTitleOutsideTapInteraction
                ) { onDismissSheetTitleEdit() }
        ) {
            val displayCells = remember(cells, selectedIndex, draftPerCell, readOnly) {
                val normalized =
                    if (cells.size >= 25) cells.take(25) else cells + List(25 - cells.size) {
                        BingoCellUi(null, false, false, true, false)
                    }
                if (readOnly) {
                    normalized.map { cell ->
                        val committed = manualEntryCommittedNumberDisplay(cell.number).orEmpty()
                        cell.copy(
                            number = committed.takeIf { it.isNotEmpty() },
                            isSelected = false,
                            isEditable = false,
                        )
                    }
                } else {
                    normalized.mapIndexed { i, cell ->
                        val draft = draftPerCell.getOrNull(i).orEmpty()
                        val committed =
                            manualEntryCommittedNumberDisplay(cell.number).orEmpty()
                        val displayNumber = if (i == selectedIndex) {
                            if (draft.isNotEmpty()) draft else committed
                        } else {
                            committed
                        }
                        cell.copy(
                            number = displayNumber.takeIf { it.isNotEmpty() },
                            isSelected = i == selectedIndex,
                            isEditable = true
                        )
                    }
                }
            }
            BingoCardGrid(
                cells = displayCells,
                modifier = Modifier.fillMaxWidth(),
                mode = BingoGridMode.PREVIEW,
                winningCells = emptySet(),
                onCellClick = if (readOnly) { {} } else onCellSelected,
            )
        }
    }
}

package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.BingoDetailGridCard
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.ui.components.CompactAlmostBingoRow
import com.example.mamunbingoapp.ui.components.TicketInfoCard
import com.example.mamunbingoapp.ui.components.TicketInfoItem
import com.example.mamunbingoapp.ui.components.TicketInfoStatusChip
import com.example.mamunbingoapp.ui.components.SectionHeader
import com.example.mamunbingoapp.ui.model.BingoCellUi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatPlayDate(millis: Long): String {
    if (millis <= 0L) return "Today"
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
}

@Composable
fun LiveSheetDetailScreen(
    ticketId: String,
    roomId: String = "",
    sheetName: String = "Unnamed sheet",
    playedAtMillis: Long = System.currentTimeMillis(),
    cells: List<BingoCellUi>? = null,
    calledNumbers: List<Int> = emptyList(),
    lastCalled: Int? = null,
    onBack: () -> Unit,
    onBackToRoom: () -> Unit = {},
    onRemoveSheet: (() -> Unit)? = null
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    val room by RoomRepository.roomFlow(roomId).collectAsState(initial = null)
    val roomName = room?.name ?: "this room"
    if (showRemoveDialog) {
        AppConfirmDialog(
            visible = true,
            title = "Remove sheet?",
            message = "This will remove this sheet from $roomName.",
            confirmText = "Remove Sheet",
            cancelText = "Cancel",
            showCancelButton = true,
            onConfirm = {
                showRemoveDialog = false
                onRemoveSheet?.invoke()
            },
            onCancel = { showRemoveDialog = false },
            onDismiss = { showRemoveDialog = false }
        )
    }

    AppHeaderPageLayout(
        topBar = {
            AppTopBar(
                title = "Sheet Detail",
                showBack = true,
                onBackClick = onBack,
                actions = {
                    TextButton(onClick = onBackToRoom) {
                        Text("Back to Room")
                    }
                    if (onRemoveSheet != null && roomId.isNotBlank()) {
                        TextButton(onClick = { showRemoveDialog = true }) {
                            Text("Remove Sheet")
                        }
                    }
                }
            )
        },
        content = {
            LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = Dimens.screenHorizontalPadding,
                top = Dimens.spacing8,
                end = Dimens.screenHorizontalPadding,
                bottom = Dimens.spacing16
            ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing24)
            ) {
            item {
                val clipboard = LocalClipboardManager.current
                val context = LocalContext.current
                val infoItems = buildList {
                    add(TicketInfoItem("Sheet Name", sheetName))
                    add(TicketInfoItem("Draw Date", formatPlayDate(playedAtMillis)))
                    add(
                        TicketInfoItem(
                            "Ticket ID",
                            ticketId,
                            trailing = {
                                IconButton(onClick = {
                                    clipboard.setText(AnnotatedString(ticketId))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy ticket id")
                                }
                            }
                        )
                    )
                    if (roomId.isNotBlank()) {
                        add(TicketInfoItem("Status", "", trailing = { TicketInfoStatusChip("In Room") }))
                    }
                }
                TicketInfoCard(title = "TICKET INFORMATION", items = infoItems)
            }
            item {
                CalledHistoryPanel(
                    modifier = Modifier.fillMaxWidth(),
                    calledNumbers = calledNumbers
                )
            }
            item {
                SectionHeader(title = "Bingo Sheet")
            }
            item {
                val gridCells = liveSheetDetailGridCells(cells)
                val markedSet = gridCells?.take(25)?.mapIndexed { i, c -> i.takeIf { c.isMarked } }?.filterNotNull()?.toSet() ?: emptySet()
                val winResult = gridCells?.take(25)?.let { list ->
                    BingoWinChecker.check(list.mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet())
                }
                val almostBingo = gridCells?.take(25)?.let { BingoWinChecker.bestAlmostBingo(it.map { it.isMarked }) }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8)
                ) {
                    if (almostBingo != null) {
                        CompactAlmostBingoRow(
                            lineType = almostBingo.lineLabel,
                            filled = almostBingo.marked,
                            total = almostBingo.total,
                            markedCells = markedSet,
                        )
                    }
                    if (winResult != null && winResult.isWin) {
                        BingoWinBanner(
                            lineCount = winResult.winningLines.size,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (winResult != null && winResult.isWin) {
                        Spacer(modifier = Modifier.height(Dimens.spacing8))
                    }
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        BingoDetailGridCard(
                            cells = gridCells,
                            winningCells = if (winResult?.isWin == true) winResult.winningCells else emptySet(),
                            historyDetailOuterMaxWidth = maxWidth,
                            historyDetailOuterMaxHeight = null,
                        )
                    }
                }
            }
        }
        }
    )
}

/** Same 25-cell display rules as the previous live detail grid (preview-style grid via [BingoDetailGridCard]). */
private fun liveSheetDetailGridCells(cells: List<BingoCellUi>?): List<BingoCellUi>? {
    return when {
        cells != null && cells.size == 25 && cells.any { !it.number.isNullOrBlank() } -> cells
        cells != null && cells.size == 25 -> BingoCellUi.placeholderCells25()
        cells != null && cells.isNotEmpty() ->
            cells + List(25 - cells.size) { BingoCellUi(null, false, false, false, false) }
        else -> null
    }
}


package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.AlmostBingoAlertRowV2
import com.example.mamunbingoapp.ui.components.BingoCardGrid
import com.example.mamunbingoapp.ui.components.BingoWinBanner
import com.example.mamunbingoapp.ui.components.BingoGridMode
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
    val displayCells = cells?.takeIf { it.size == 25 }
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
                LiveSheetDetailGridCard(cells = displayCells, sheetName = sheetName)
            }
        }
        }
    )
}

@Composable
private fun LiveSheetDetailGridCard(cells: List<BingoCellUi>?, sheetName: String) {
    val shape = RoundedCornerShape(Dimens.radiusCard)
    val gridCells = when {
        cells != null && cells.size == 25 && cells.any { !it.number.isNullOrBlank() } -> cells
        cells != null && cells.size == 25 -> BingoCellUi.placeholderCells25()
        cells != null && cells.isNotEmpty() -> cells + List(25 - cells.size) { BingoCellUi(null, false, false, false, false) }
        else -> null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(Dimens.cardBorderDefault, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f), shape)
            .padding(Dimens.spacing16)
    ) {
        if (gridCells == null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No grid saved for this sheet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }
        } else {
            val markedSet = gridCells.mapIndexed { i, c -> i.takeIf { c.isMarked } }.filterNotNull().toSet()
            val winResult = BingoWinChecker.check(markedSet)
            val winningCells = winResult.winningCells
            val markedList = gridCells.map { it.isMarked }
            val almostBingo = BingoWinChecker.bestAlmostBingo(markedList)
            if (winResult.isWin) {
                BingoWinBanner(lineCount = winResult.winningLines.size, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(Dimens.spacing12))
            }
            if (almostBingo != null) {
                AlmostBingoAlertRowV2(
                    lineType = almostBingo.lineLabel,
                    filled = almostBingo.marked,
                    total = almostBingo.total,
                    markedCells = markedSet,
                    nearCells = emptySet(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimens.spacing12))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sheetName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Marked: ${gridCells.count { it.isMarked }}/25",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(Dimens.radiusPill)).padding(horizontal = Dimens.spacing8, vertical = Dimens.spacing4)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacing16))
            BingoCardGrid(
                cells = gridCells,
                modifier = Modifier.fillMaxWidth(),
                mode = BingoGridMode.PLAY,
                winningCells = winningCells,
                onCellClick = {}
            )
        }
    }
}

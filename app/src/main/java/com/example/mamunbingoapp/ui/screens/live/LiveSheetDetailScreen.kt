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
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.data.RoomRepository
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppConfirmDialog
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.CalledHistoryPanel
import com.example.mamunbingoapp.core.BingoWinChecker
import com.example.mamunbingoapp.ui.components.BingoDetailGridCard
import com.example.mamunbingoapp.ui.components.BingoWinLineBadge
import com.example.mamunbingoapp.ui.components.CompactAlmostBingoRow
import com.example.mamunbingoapp.ui.components.TicketInfoCard
import com.example.mamunbingoapp.ui.components.TicketInfoItem
import com.example.mamunbingoapp.ui.components.TicketInfoStatusChip
import com.example.mamunbingoapp.ui.components.SectionHeader
import com.example.mamunbingoapp.ui.components.home.ActiveTicketLosSerieRow
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
import androidx.compose.ui.res.stringResource
import com.example.mamunbingoapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun formatPlayDate(millis: Long): String {
    if (millis <= 0L) return stringResource(R.string.common_today)
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
}

@Composable
fun LiveSheetDetailScreen(
    ticketId: String,
    roomId: String = "",
    sheetName: String = "",
    losNumber: String? = null,
    serialNumber: String? = null,
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
    val roomName = room?.name ?: stringResource(R.string.live_play_this_room)
    val displaySheetName = sheetName.ifBlank { stringResource(R.string.history_unnamed_sheet) }
    if (showRemoveDialog) {
        AppConfirmDialog(
            visible = true,
            title = stringResource(R.string.live_play_remove_sheet_title),
            message = stringResource(R.string.live_play_remove_sheet_message, roomName),
            confirmText = stringResource(R.string.live_play_remove_sheet_confirm),
            cancelText = stringResource(R.string.settings_cancel),
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
                title = stringResource(R.string.live_play_sheet_detail_title),
                showBack = true,
                onBackClick = onBack,
                actions = {
                    TextButton(onClick = onBackToRoom) {
                        Text(stringResource(R.string.live_play_back_to_room))
                    }
                    if (onRemoveSheet != null && roomId.isNotBlank()) {
                        TextButton(onClick = { showRemoveDialog = true }) {
                            Text(stringResource(R.string.live_play_remove_sheet_confirm))
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
                top = 0.dp,
                end = Dimens.screenHorizontalPadding,
                bottom = Dimens.pageContentBottomPadding,
            ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing24)
            ) {
            item {
                val clipboard = LocalClipboardManager.current
                val context = LocalContext.current
                val copiedMessage = stringResource(R.string.common_copied_to_clipboard)
                val infoItems = buildList {
                    add(TicketInfoItem(stringResource(R.string.live_play_sheet_name_label), displaySheetName))
                    add(TicketInfoItem(stringResource(R.string.history_detail_stat_draw_date), formatPlayDate(playedAtMillis)))
                    add(
                        TicketInfoItem(
                            stringResource(R.string.live_play_ticket_id_label),
                            ticketId,
                            trailing = {
                                IconButton(onClick = {
                                    clipboard.setText(AnnotatedString(ticketId))
                                    Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.history_detail_copy_ticket_cd)
                                    )
                                }
                            }
                        )
                    )
                    if (roomId.isNotBlank()) {
                        add(
                            TicketInfoItem(
                                stringResource(R.string.live_play_status_label),
                                "",
                                trailing = { TicketInfoStatusChip(stringResource(R.string.live_play_status_in_room)) }
                            )
                        )
                    }
                }
                TicketInfoCard(title = stringResource(R.string.live_play_ticket_info_title), items = infoItems)
            }
            item {
                CalledHistoryPanel(
                    modifier = Modifier.fillMaxWidth(),
                    calledNumbers = calledNumbers
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                ) {
                    SectionHeader(title = stringResource(R.string.live_play_bingo_sheet_section))
                    ActiveTicketLosSerieRow(
                        losNumber = losNumber,
                        serieNumber = serialNumber,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val gridMaxWidth = maxWidth
                        Box(modifier = Modifier.fillMaxWidth()) {
                            BingoDetailGridCard(
                                cells = gridCells,
                                winningCells = if (winResult?.isWin == true) {
                                    winResult.winningCells
                                } else {
                                    emptySet()
                                },
                                historyDetailOuterMaxWidth = gridMaxWidth,
                                historyDetailOuterMaxHeight = null,
                            )
                            if (winResult != null && winResult.isWin) {
                                BingoWinLineBadge(
                                    lineCount = winResult.winningLines.size,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .zIndex(1f)
                                        .offset(x = 8.dp, y = (-6).dp),
                                )
                            }
                        }
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


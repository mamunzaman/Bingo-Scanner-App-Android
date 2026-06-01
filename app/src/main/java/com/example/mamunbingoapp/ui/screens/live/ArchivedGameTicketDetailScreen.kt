package com.example.mamunbingoapp.ui.screens.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.data.TicketPlayLog
import com.example.mamunbingoapp.data.TicketPlayLogRepository
import com.example.mamunbingoapp.data.TicketRepository
import com.example.mamunbingoapp.data.mergeTicketCellsWithArchivedCalledNumbers
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.model.BingoCellUi
import com.example.mamunbingoapp.ui.screens.history.HistoryReadOnlyTicketDetailContent

@Composable
fun ArchivedGameTicketDetailScreen(
    roomId: String,
    archivedAt: Long,
    ticketId: String,
    onBack: () -> Unit,
) {
    var playLog by remember(roomId, archivedAt, ticketId) { mutableStateOf<TicketPlayLog?>(null) }
    var loading by remember(roomId, archivedAt, ticketId) { mutableStateOf(true) }
    var notFound by remember(roomId, archivedAt, ticketId) { mutableStateOf(false) }

    LaunchedEffect(roomId, archivedAt, ticketId) {
        loading = true
        notFound = false
        playLog = TicketPlayLogRepository.getArchivedTicketPlayLog(roomId, archivedAt, ticketId)
        notFound = playLog == null
        loading = false
    }

    val ticket by TicketRepository.observeTicket(ticketId).collectAsState(initial = null)
    val baseCells by TicketRepository.ticketCellsFlow(ticketId).collectAsState(initial = emptyList())
    val log = playLog
    val calledNumbers = log?.calledNumbers.orEmpty()
    val displayCells: List<BingoCellUi>? = remember(baseCells, calledNumbers, log) {
        if (log == null) null
        else mergeTicketCellsWithArchivedCalledNumbers(baseCells, calledNumbers).takeIf { it.isNotEmpty() }
    }
    val sheetName = ticket?.sheetName?.takeIf { it.isNotBlank() }
        ?: log?.roomName
        ?: ""
    val sessionRoomLabel = log?.drawDate?.takeIf { it.isNotBlank() } ?: log?.roomName.orEmpty()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        AppHeaderPageLayout(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.archived_game_ticket_detail_title),
                    showBack = true,
                    onBackClick = onBack,
                )
            },
            content = {
                when {
                    loading -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    notFound -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.screenHorizontalPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.archived_game_ticket_detail_not_found),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    else -> {
                        HistoryReadOnlyTicketDetailContent(
                            sheetName = sheetName,
                            sessionRoomLabel = sessionRoomLabel,
                            archivedAtMillis = log?.archivedAt ?: archivedAt,
                            losNumber = ticket?.losNumber,
                            serialNumber = ticket?.serialNumber,
                            cells = displayCells,
                            calledNumbers = calledNumbers,
                            createdAtMillis = ticket?.createdAt ?: log?.addedAt ?: archivedAt,
                            updatedAtMillis = log?.archivedAt ?: archivedAt,
                            modifier = Modifier.weight(1f),
                            listTopPadding = 0.dp,
                        )
                    }
                }
            },
        )
    }
}

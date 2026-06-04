package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.core.SundayBingoSchedule
import com.example.mamunbingoapp.data.db.DatabaseProvider
import com.example.mamunbingoapp.data.db.TicketCellEntity
import com.example.mamunbingoapp.data.db.TicketEntity
import com.example.mamunbingoapp.ui.model.BingoCellUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

object TicketRepository {
    private fun ticketDao() = DatabaseProvider.db.ticketDao()

    fun ticketsFlow(): Flow<List<HistorySession>> =
        ticketDao().observeTickets().map { list ->
            list.map { it.toHistorySession() }
        }

    fun markedCountsByTicketFlow(): Flow<Map<String, Int>> =
        ticketDao().observeMarkedCounts().map { list ->
            list.associate { it.ticketId to it.markedCount.toInt() }
        }

    fun cellsByTicketFlow(): Flow<Map<String, List<TicketCellEntity>>> =
        ticketDao().observeAllCells().map { list ->
            list.groupBy { it.ticketId }.mapValues { (_, cells) -> cells.sortedBy { it.cellIndex } }
        }

    suspend fun findDuplicateTicketForWeeklyPlay(
        losNumber: String,
        serialNumber: String,
        playedAtMillis: Long,
    ): String? = withContext(Dispatchers.IO) {
        val los = losNumber.trim().takeIf { it.isNotEmpty() } ?: return@withContext null
        val serial = serialNumber.trim().takeIf { it.isNotEmpty() } ?: return@withContext null
        val window = SundayBingoSchedule.playWindowForPlayedAtMillis(playedAtMillis)
        ticketDao().findActiveTicketIdByLosSerialInWindow(
            losNumber = los,
            serialNumber = serial,
            windowStartMillis = window.startInclusive,
            windowEndExclusiveMillis = window.endExclusive,
        )
    }

    suspend fun saveManualTicket(
        sheetName: String,
        playedAtMillis: Long,
        cells: List<BingoCellUi>,
        ocrSource: String? = null,
        ocrConfidence: Float? = null,
        originalOcrNumbers: String? = null,
        losNumber: String? = null,
        serialNumber: String? = null,
    ): String {
        val ticketId = "manual-${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val source = ocrSource?.takeIf { it in listOf("GEMINI", "ML_KIT") } ?: "manual"
        ticketDao().upsertTicket(
            TicketEntity(
                ticketId = ticketId,
                sheetName = sheetName,
                playedAtMillis = playedAtMillis,
                createdAt = now,
                source = source,
                ocrConfidence = ocrConfidence,
                originalOcrNumbers = if (source != "manual") originalOcrNumbers?.takeIf { it.isNotBlank() } else null,
                losNumber = losNumber?.trim()?.takeIf { it.isNotEmpty() },
                serialNumber = serialNumber?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
        val cellEntities = cells.take(25).mapIndexed { index, cell ->
            TicketCellEntity(
                ticketId = ticketId,
                cellIndex = index,
                value = cell.number,
                isMarked = cell.isMarked
            )
        }
        ticketDao().upsertCells(cellEntities)
        return ticketId
    }

    fun observeTicket(ticketId: String): Flow<TicketEntity?> =
        ticketDao().observeTicket(ticketId)

    fun ticketCellsFlow(ticketId: String): Flow<List<BingoCellUi>> =
        ticketDao().observeTicketCells(ticketId).map { list ->
            list.sortedBy { it.cellIndex }.map { cell ->
                BingoCellUi(
                    number = cell.value,
                    isMarked = cell.isMarked,
                    isCalled = false,
                    isEditable = false,
                    isDisabled = false
                )
            }.let { if (it.size == 25) it else (it + List(25 - it.size) { BingoCellUi(null, false, false, false, false) }) }
        }

    suspend fun getTicketData(ticketId: String): TicketDetailData? {
        val ticket = ticketDao().observeTickets().first().find { it.ticketId == ticketId } ?: return null
        val cells = ticketDao().observeTicketCells(ticketId).first()
            .sortedBy { it.cellIndex }
            .map { BingoCellUi(it.value, it.isMarked, false, false, false) }
        val cells25 = if (cells.size == 25) cells else cells + List(25 - cells.size.coerceAtMost(25)) { BingoCellUi(null, false, false, false, false) }
        return TicketDetailData(
            sheetName = ticket.sheetName,
            playedAtMillis = ticket.playedAtMillis,
            cells = cells25,
            calledNumbers = emptyList(),
            losNumber = ticket.losNumber,
            serialNumber = ticket.serialNumber,
        )
    }

    suspend fun getSessionById(ticketId: String): HistorySession? {
        val ticket = ticketDao().observeTickets().first().find { it.ticketId == ticketId } ?: return null
        return ticket.toHistorySession()
    }

    suspend fun deleteTicket(ticketId: String) {
        ticketDao().deleteTicketCells(ticketId)
        ticketDao().deleteTicket(ticketId)
    }

    suspend fun softDeleteTicket(ticketId: String) {
        ticketDao().softDeleteTicket(ticketId)
    }

    suspend fun restoreTicket(ticketId: String) {
        ticketDao().restoreTicket(ticketId)
    }

    suspend fun duplicateTicket(sessionId: String): String? = withContext(Dispatchers.IO) {
        val ticket = ticketDao().observeTickets().first().find { it.ticketId == sessionId } ?: return@withContext null
        val cells = ticketDao().observeTicketCells(sessionId).first()
        val newTicketId = "manual-${UUID.randomUUID()}"
        val newSheetName = ticket.sheetName.removeSuffix(" (Copy)") + " (Copy)"
        val now = System.currentTimeMillis()
        ticketDao().upsertTicket(
            TicketEntity(
                ticketId = newTicketId,
                sheetName = newSheetName,
                playedAtMillis = ticket.playedAtMillis,
                createdAt = now,
                source = "manual",
                isDeleted = false,
                ocrConfidence = ticket.ocrConfidence,
                originalOcrNumbers = ticket.originalOcrNumbers,
                losNumber = ticket.losNumber,
                serialNumber = ticket.serialNumber,
            )
        )
        val newCells = cells.map { cell ->
            TicketCellEntity(
                ticketId = newTicketId,
                cellIndex = cell.cellIndex,
                value = cell.value,
                isMarked = cell.isMarked
            )
        }
        ticketDao().upsertCells(newCells)
        newTicketId
    }

    /** Upserts demo room tickets + cells with sample LOS/SERIE when missing (debug `DEMO_MODE` only). */
    suspend fun seedDemoTickets() = withContext(Dispatchers.IO) {
        val cellsByTicket = DemoDataFactory.createDemoTicketCells()
        val now = System.currentTimeMillis()
        for (seed in DemoDataFactory.demoTicketSeeds()) {
            val existing = ticketDao().observeTicket(seed.ticketId).first()
            val cells = cellsByTicket[seed.ticketId].orEmpty().take(25)
            if (existing == null) {
                ticketDao().upsertTicket(
                    TicketEntity(
                        ticketId = seed.ticketId,
                        sheetName = seed.sheetName,
                        playedAtMillis = seed.playedAtMillis,
                        createdAt = now,
                        source = "demo",
                        losNumber = seed.losNumber,
                        serialNumber = seed.serialNumber,
                    )
                )
                if (cells.isNotEmpty()) {
                    ticketDao().upsertCells(
                        cells.mapIndexed { index, cell ->
                            TicketCellEntity(
                                ticketId = seed.ticketId,
                                cellIndex = index,
                                value = cell.number,
                                isMarked = cell.isMarked,
                            )
                        }
                    )
                }
            } else {
                val losBlank = existing.losNumber.isNullOrBlank()
                val serialBlank = existing.serialNumber.isNullOrBlank()
                if (losBlank && serialBlank) {
                    ticketDao().upsertTicket(
                        existing.copy(
                            losNumber = seed.losNumber,
                            serialNumber = seed.serialNumber,
                        )
                    )
                }
            }
        }
    }
}

private fun TicketEntity.toHistorySession(): HistorySession {
    val sheet = SheetPlayed(
        ticketId = ticketId,
        title = sheetName,
        subtitle = "Manual entry",
        markedCount = 0,
        totalCount = 25
    )
    val ocrSource = source.takeIf { it in listOf("GEMINI", "ML_KIT") }
    return HistorySession(
        id = ticketId,
        title = sheetName,
        isCompleted = false,
        sheetsCount = 1,
        calledCount = 0,
        calledNumbersPreview = emptyList(),
        calledNumbersFull = emptyList(),
        sheetsPlayed = listOf(sheet),
        sheetName = sheetName,
        playedAtMillis = playedAtMillis,
        ocrSource = ocrSource,
        ocrConfidence = ocrConfidence,
        originalOcrNumbers = originalOcrNumbers,
        losNumber = losNumber,
        serialNumber = serialNumber,
    )
}

package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.data.db.DatabaseProvider
import com.example.mamunbingoapp.data.db.TicketPlayLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object TicketPlayLogRepository {
    private fun dao() = DatabaseProvider.db.ticketPlayLogDao()

    fun observeForTicket(ticketId: String): Flow<List<TicketPlayLog>> =
        dao().observeForTicket(ticketId).map { list -> list.map { it.toTicketPlayLog() } }

    fun observeLatestCalledNumbersByTicket(): Flow<Map<String, List<Int>>> =
        dao().observeAll().map { logs ->
            logs
                .filter { !it.ticketId.isArchivedCallsOnlyPlaceholderTicketId() }
                .groupBy { it.ticketId }
                .mapValues { (_, entries) ->
                    entries.maxByOrNull { it.archivedAt }?.toTicketPlayLog()?.calledNumbers.orEmpty()
                }
        }

    fun observeArchivedSessions(): Flow<List<ArchivedGameSession>> =
        dao().observeAll().map { logs ->
            logs.map { it.toTicketPlayLog() }.toArchivedGameSessions()
        }

    suspend fun getArchivedSession(roomId: String, archivedAt: Long): ArchivedGameSession? =
        dao().observeAll().first().let { entities ->
            val logs = entities
                .filter { it.roomId == roomId && it.archivedAt == archivedAt }
                .map { it.toTicketPlayLog() }
            logs.toArchivedGameSessions().firstOrNull()
        }

    suspend fun getArchivedTicketPlayLog(
        roomId: String,
        archivedAt: Long,
        ticketId: String,
    ): TicketPlayLog? =
        dao().observeAll().first()
            .map { it.toTicketPlayLog() }
            .firstOrNull { log ->
                log.roomId == roomId &&
                    log.archivedAt == archivedAt &&
                    log.ticketId == ticketId &&
                    !log.ticketId.isArchivedCallsOnlyPlaceholderTicketId()
            }

    suspend fun insertAll(entries: List<TicketPlayLogEntity>) {
        if (entries.isEmpty()) return
        dao().insertAll(entries)
    }
}

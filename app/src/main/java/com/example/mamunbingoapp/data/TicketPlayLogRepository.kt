package com.example.mamunbingoapp.data

import com.example.mamunbingoapp.data.db.DatabaseProvider
import com.example.mamunbingoapp.data.db.TicketPlayLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object TicketPlayLogRepository {
    private fun dao() = DatabaseProvider.db.ticketPlayLogDao()

    fun observeForTicket(ticketId: String): Flow<List<TicketPlayLog>> =
        dao().observeForTicket(ticketId).map { list -> list.map { it.toTicketPlayLog() } }

    fun observeLatestCalledNumbersByTicket(): Flow<Map<String, List<Int>>> =
        dao().observeAll().map { logs ->
            logs.groupBy { it.ticketId }.mapValues { (_, entries) ->
                entries.maxByOrNull { it.archivedAt }?.toTicketPlayLog()?.calledNumbers.orEmpty()
            }
        }

    suspend fun insertAll(entries: List<TicketPlayLogEntity>) {
        if (entries.isEmpty()) return
        dao().insertAll(entries)
    }
}

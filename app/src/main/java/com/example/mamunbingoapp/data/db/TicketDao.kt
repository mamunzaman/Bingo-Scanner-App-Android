package com.example.mamunbingoapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTicket(ticket: TicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCells(cells: List<TicketCellEntity>)

    @Query("SELECT * FROM tickets WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun observeTickets(): kotlinx.coroutines.flow.Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE ticketId = :ticketId AND isDeleted = 0 LIMIT 1")
    fun observeTicket(ticketId: String): kotlinx.coroutines.flow.Flow<TicketEntity?>

    @Query("SELECT * FROM ticket_cells WHERE ticketId = :ticketId ORDER BY cellIndex ASC")
    fun observeTicketCells(ticketId: String): kotlinx.coroutines.flow.Flow<List<TicketCellEntity>>

    @Query("DELETE FROM tickets WHERE ticketId = :ticketId")
    suspend fun deleteTicket(ticketId: String)

    @Query("DELETE FROM ticket_cells WHERE ticketId = :ticketId")
    suspend fun deleteTicketCells(ticketId: String)

    @Query("UPDATE tickets SET isDeleted = 1 WHERE ticketId = :ticketId")
    suspend fun softDeleteTicket(ticketId: String)

    @Query("UPDATE tickets SET isDeleted = 0 WHERE ticketId = :ticketId")
    suspend fun restoreTicket(ticketId: String)

    @Query("SELECT ticketId, SUM(CASE WHEN isMarked = 1 THEN 1 ELSE 0 END) AS markedCount FROM ticket_cells GROUP BY ticketId")
    fun observeMarkedCounts(): kotlinx.coroutines.flow.Flow<List<TicketMarkedCount>>

    @Query("SELECT * FROM ticket_cells ORDER BY ticketId, cellIndex ASC")
    fun observeAllCells(): kotlinx.coroutines.flow.Flow<List<TicketCellEntity>>

    @Query(
        """
        SELECT ticketId FROM tickets
        WHERE isDeleted = 0
        AND losNumber = :losNumber
        AND serialNumber = :serialNumber
        AND playedAtMillis >= :windowStartMillis
        AND playedAtMillis < :windowEndExclusiveMillis
        LIMIT 1
        """
    )
    suspend fun findActiveTicketIdByLosSerialInWindow(
        losNumber: String,
        serialNumber: String,
        windowStartMillis: Long,
        windowEndExclusiveMillis: Long,
    ): String?
}

data class TicketMarkedCount(val ticketId: String, val markedCount: Long)

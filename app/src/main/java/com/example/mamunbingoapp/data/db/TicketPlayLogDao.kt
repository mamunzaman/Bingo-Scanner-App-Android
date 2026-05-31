package com.example.mamunbingoapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketPlayLogDao {
    @Query("SELECT * FROM ticket_play_logs WHERE ticketId = :ticketId ORDER BY archivedAt DESC")
    fun observeForTicket(ticketId: String): Flow<List<TicketPlayLogEntity>>

    @Query("SELECT * FROM ticket_play_logs ORDER BY archivedAt DESC")
    fun observeAll(): Flow<List<TicketPlayLogEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entries: List<TicketPlayLogEntity>)
}

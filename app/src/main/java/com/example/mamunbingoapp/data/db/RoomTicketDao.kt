package com.example.mamunbingoapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RoomTicketDao {
    @Query("SELECT * FROM room_tickets WHERE roomId = :roomId ORDER BY addedAt ASC")
    fun observeTickets(roomId: String): kotlinx.coroutines.flow.Flow<List<RoomTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTicket(entity: RoomTicketEntity)

    @Query("DELETE FROM room_tickets WHERE roomId = :roomId AND ticketId = :ticketId")
    suspend fun removeTicket(roomId: String, ticketId: String)

    @Transaction
    suspend fun moveTicket(ticketId: String, fromRoomId: String, toRoomId: String) {
        removeTicket(fromRoomId, ticketId)
        addTicket(RoomTicketEntity(roomId = toRoomId, ticketId = ticketId))
    }

    @Query("DELETE FROM room_tickets WHERE roomId = :roomId")
    suspend fun clearTickets(roomId: String)

    @Query("SELECT * FROM room_tickets")
    fun observeAllTickets(): kotlinx.coroutines.flow.Flow<List<RoomTicketEntity>>

    @Query("SELECT roomId FROM room_tickets WHERE ticketId = :ticketId LIMIT 1")
    suspend fun getRoomIdForTicket(ticketId: String): String?

    @Query("SELECT rt.roomId AS roomId, COUNT(rt.ticketId) AS count FROM room_tickets rt INNER JOIN tickets t ON rt.ticketId = t.ticketId WHERE t.isDeleted = 0 GROUP BY rt.roomId")
    fun observeValidTicketCountsByRoom(): kotlinx.coroutines.flow.Flow<List<RoomTicketCountResult>>
}

data class RoomTicketCountResult(val roomId: String, val count: Int)

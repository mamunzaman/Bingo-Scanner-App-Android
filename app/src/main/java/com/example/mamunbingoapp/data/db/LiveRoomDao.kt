package com.example.mamunbingoapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LiveRoomDao {
    @Query("SELECT * FROM live_rooms ORDER BY createdAt ASC")
    fun observeRooms(): kotlinx.coroutines.flow.Flow<List<LiveRoomEntity>>

    @Query("SELECT * FROM live_rooms WHERE roomId = :roomId LIMIT 1")
    fun observeRoom(roomId: String): kotlinx.coroutines.flow.Flow<LiveRoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoom(room: LiveRoomEntity)

    @Query("DELETE FROM live_rooms WHERE roomId = :roomId")
    suspend fun deleteRoom(roomId: String)
}

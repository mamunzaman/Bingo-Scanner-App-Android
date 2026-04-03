package com.example.mamunbingoapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomCalledNumberDao {
    @Query("SELECT * FROM room_called_numbers WHERE roomId = :roomId ORDER BY calledAt ASC")
    fun observeCalled(roomId: String): kotlinx.coroutines.flow.Flow<List<RoomCalledNumberEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCalled(entity: RoomCalledNumberEntity)

    @Query("DELETE FROM room_called_numbers WHERE roomId = :roomId")
    suspend fun clearCalled(roomId: String)

    @Query("DELETE FROM room_called_numbers WHERE roomId = :roomId AND number = :number")
    suspend fun deleteCalledNumber(roomId: String, number: Int)
}

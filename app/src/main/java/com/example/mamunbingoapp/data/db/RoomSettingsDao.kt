package com.example.mamunbingoapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomSettingsDao {
    @Query("SELECT * FROM room_settings")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<RoomSettingsEntity>>

    @Query("SELECT * FROM room_settings WHERE roomId = :roomId LIMIT 1")
    fun observeSettings(roomId: String): kotlinx.coroutines.flow.Flow<RoomSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: RoomSettingsEntity)

    @Query("DELETE FROM room_settings WHERE roomId = :roomId")
    suspend fun deleteSettings(roomId: String)

    @Query("UPDATE room_settings SET isArchived = :archived WHERE roomId = :roomId")
    suspend fun updateArchived(roomId: String, archived: Boolean): Int
}

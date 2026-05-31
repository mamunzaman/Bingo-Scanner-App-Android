package com.example.mamunbingoapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LiveRoomEntity::class,
        RoomTicketEntity::class,
        RoomCalledNumberEntity::class,
        RoomSettingsEntity::class,
        TicketEntity::class,
        TicketCellEntity::class,
        TicketPlayLogEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun liveRoomDao(): LiveRoomDao
    abstract fun roomTicketDao(): RoomTicketDao
    abstract fun roomCalledNumberDao(): RoomCalledNumberDao
    abstract fun roomSettingsDao(): RoomSettingsDao
    abstract fun ticketDao(): TicketDao
    abstract fun ticketPlayLogDao(): TicketPlayLogDao
}

package com.example.mamunbingoapp.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    private var _db: AppDatabase? = null
    val db: AppDatabase
        get() = checkNotNull(_db) { "Database not initialized. Call init(context) from MainActivity." }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tickets ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE room_settings ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tickets ADD COLUMN ocrConfidence REAL")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tickets ADD COLUMN originalOcrNumbers TEXT")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tickets ADD COLUMN losNumber TEXT")
            db.execSQL("ALTER TABLE tickets ADD COLUMN serialNumber TEXT")
        }
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS tickets (
                    ticketId TEXT PRIMARY KEY NOT NULL,
                    sheetName TEXT NOT NULL,
                    playedAtMillis INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    source TEXT NOT NULL DEFAULT 'manual'
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS ticket_cells (
                    ticketId TEXT NOT NULL,
                    cellIndex INTEGER NOT NULL,
                    value TEXT,
                    isMarked INTEGER NOT NULL,
                    PRIMARY KEY (ticketId, cellIndex)
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_ticket_cells_ticketId ON ticket_cells(ticketId)")
        }
    }

    fun init(context: Context) {
        if (_db != null) return
        _db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mamun_bingo_db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    fun closeAndClear(context: Context) {
        _db?.clearAllTables()
        _db?.close()
        _db = null
        context.applicationContext.deleteDatabase("mamun_bingo_db")
    }
}

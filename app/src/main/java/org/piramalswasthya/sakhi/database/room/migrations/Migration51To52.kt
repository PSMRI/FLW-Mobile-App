package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration51To52 : Migration(51, 52) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS MAA_MEETING (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                meetingDate TEXT,
                place TEXT,
                villageName TEXT,
                mitaninActivityCheckList TEXT,
                noOfPragnentWomen TEXT,
                noOfLactingMother TEXT,
                participants INTEGER,
                ashaId INTEGER,
                meetingImages TEXT,
                createdAt INTEGER NOT NULL DEFAULT 0,
                updatedAt INTEGER NOT NULL DEFAULT 0,
                syncState TEXT NOT NULL DEFAULT 'UNSYNCED'
            )
            """.trimIndent()
        )

        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_MAA_MEETING_id ON MAA_MEETING(id)"
        )
    }
}

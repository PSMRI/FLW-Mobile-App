package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration52To53 : Migration(52, 53) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_DewormingMeeting_dewormingDate
            ON DewormingMeeting(dewormingDate)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS FILARIA_MDA_CAMPAIGN_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                visitDate TEXT NOT NULL,
                visitYear TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER,
                createdAt INTEGER NOT NULL,
                syncedAt TEXT,
                syncState TEXT NOT NULL DEFAULT 'UNSYNCED'
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            index_FILARIA_MDA_CAMPAIGN_HISTORY_formId_visitYear
            ON FILARIA_MDA_CAMPAIGN_HISTORY(formId, visitYear)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            index_FILARIA_MDA_CAMPAIGN_HISTORY_visitDate
            ON FILARIA_MDA_CAMPAIGN_HISTORY(visitDate)
            """.trimIndent()
        )
    }


}

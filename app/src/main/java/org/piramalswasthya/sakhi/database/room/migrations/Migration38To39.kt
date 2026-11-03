package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration38To39 : Migration(38, 39) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS FILARIA_MDA_VISIT_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                hhId INTEGER NOT NULL,
                visitDate TEXT NOT NULL,
                visitMonth TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                syncedAt TEXT
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_FILARIA_MDA_VISIT_HISTORY_hhId_formId_visitMonth
            ON FILARIA_MDA_VISIT_HISTORY (hhId, formId, visitMonth)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_FILARIA_MDA_VISIT_HISTORY_hhId_visitDate
            ON FILARIA_MDA_VISIT_HISTORY (hhId, visitDate)
            """.trimIndent()
        )
    }
}

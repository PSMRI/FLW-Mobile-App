package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration36To37 : Migration(36, 37) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS mosquito_net_visit (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                hhId INTEGER NOT NULL,
                visitDate TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                syncedAt TEXT
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_mosquito_net_visit_unique
            ON mosquito_net_visit (hhId, visitDate, formId)
            """.trimIndent()
        )
    }
}

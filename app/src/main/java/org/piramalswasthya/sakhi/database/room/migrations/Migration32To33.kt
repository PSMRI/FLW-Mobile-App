package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration32To33 : Migration(32, 33) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ALL_VISIT_HISTORY_HBYC (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                hhId INTEGER NOT NULL,
                visitDay TEXT NOT NULL,
                visitDate TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                syncedAt INTEGER
            )
        """.trimIndent()
        )
        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_all_visit_history_hbyc_unique 
            ON ALL_VISIT_HISTORY_HBYC (benId, hhId, visitDay, visitDate, formId)
        """.trimIndent()
        )
    }
}

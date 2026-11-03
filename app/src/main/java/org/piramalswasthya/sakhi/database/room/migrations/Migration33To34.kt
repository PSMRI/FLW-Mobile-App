package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration33To34 : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS form_schema (
                formId TEXT NOT NULL PRIMARY KEY,
                formName TEXT NOT NULL,
                version INTEGER NOT NULL DEFAULT 1,
                schemaJson TEXT NOT NULL
            )
        """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS all_visit_history (
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
            CREATE UNIQUE INDEX IF NOT EXISTS index_all_visit_history_unique 
            ON all_visit_history (benId, hhId, visitDay, visitDate, formId)
        """.trimIndent()
        )
    }
}

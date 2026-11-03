package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration30To31 : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE CBAC ADD COLUMN isReffered INTEGER DEFAULT 0")
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
        database.execSQL("ALTER TABLE IMMUNIZATION ADD COLUMN mcpCardSummary1 TEXT")
        database.execSQL("ALTER TABLE IMMUNIZATION ADD COLUMN mcpCardSummary2 TEXT")
        database.execSQL("DROP TABLE IF EXISTS `UWIN_SESSION`")
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `UWIN_SESSION` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionDate` INTEGER NOT NULL,
                    `place` TEXT,
                    `participantsCount` INTEGER NOT NULL,
                    `uploadedFiles1` TEXT,
                    `uploadedFiles2` TEXT,
                    `processed` TEXT,
                    `createdBy` TEXT NOT NULL,
                    `createdDate` INTEGER NOT NULL,
                    `updatedBy` TEXT NOT NULL,
                    `updatedDate` INTEGER NOT NULL,
                    `syncState` INTEGER NOT NULL
                    )
                    """.trimIndent()
        )
    }
}

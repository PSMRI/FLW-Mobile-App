package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration34To35 : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
           CREATE TABLE ALL_EYE_SURGERY_VISIT_HISTORY (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    benId INTEGER NOT NULL,
                    hhId INTEGER NOT NULL,
                    visitDate TEXT NOT NULL,
                    formId TEXT NOT NULL,
                    version INTEGER NOT NULL,
                    formDataJson TEXT NOT NULL,
                    isSynced INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    syncedAt TEXT,
                    visitMonth TEXT NOT NULL DEFAULT ''
                )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS 
            index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_hhId_visitDate_formId
            ON ALL_EYE_SURGERY_VISIT_HISTORY (benId, hhId, visitDate, formId)
            """.trimIndent()
        )

        if (tableExists(database, "MALARIA_SCREENING")) {

            if (!columnExists(database, "MALARIA_SCREENING", "visitId")) {
                database.execSQL(
                    "ALTER TABLE MALARIA_SCREENING ADD COLUMN visitId INTEGER NOT NULL DEFAULT 1"
                )
            }

            if (!columnExists(database, "MALARIA_SCREENING", "malariaTestType")) {
                database.execSQL(
                    "ALTER TABLE MALARIA_SCREENING ADD COLUMN malariaTestType INTEGER DEFAULT 0"
                )
            }

            if (!columnExists(database, "MALARIA_SCREENING", "malariaSlideTestType")) {
                database.execSQL(
                    "ALTER TABLE MALARIA_SCREENING ADD COLUMN malariaSlideTestType INTEGER DEFAULT 0"
                )
            }

            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS ind_malariasn ON MALARIA_SCREENING(benId, visitId)"
            )
        }
    }
}

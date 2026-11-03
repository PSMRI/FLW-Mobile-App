package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration35To36 : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {


        if (tableExists(db, "ALL_EYE_SURGERY_VISIT_HISTORY")) {

            db.execSQL("ALTER TABLE ALL_EYE_SURGERY_VISIT_HISTORY RENAME TO temp_eye_history")

            db.execSQL(
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

            db.execSQL(
                """
                INSERT INTO ALL_EYE_SURGERY_VISIT_HISTORY (
                    id, benId, hhId, visitDate, formId, version, formDataJson, isSynced, createdAt, syncedAt, visitMonth
                )
                SELECT id, benId, hhId, visitDate, formId, version, formDataJson, isSynced, createdAt, syncedAt, visitMonth
                FROM temp_eye_history
            """.trimIndent()
            )

            db.execSQL("DROP TABLE temp_eye_history")

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_formId_visitMonth
                ON ALL_EYE_SURGERY_VISIT_HISTORY(benId, formId, visitMonth)
            """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_visitDate
                ON ALL_EYE_SURGERY_VISIT_HISTORY(benId, visitDate)
            """.trimIndent()
            )
        }

        if (tableExists(db, "MALARIA_SCREENING")) {

            db.execSQL("DROP INDEX IF EXISTS ind_malariasn")

            if (!columnExists(db, "MALARIA_SCREENING", "visitId")) {
                db.execSQL(
                    "ALTER TABLE MALARIA_SCREENING ADD COLUMN visitId INTEGER NOT NULL DEFAULT 1"
                )
            }

            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS ind_malariasn ON MALARIA_SCREENING(benId, visitId)"
            )
        }
    }
}

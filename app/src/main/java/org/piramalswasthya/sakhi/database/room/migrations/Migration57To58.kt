package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration57To58 : Migration(57, 58) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN verifiedByUserName TEXT NOT NULL DEFAULT ''"
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN reason TEXT NOT NULL DEFAULT ''"
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN otherReason TEXT NOT NULL DEFAULT ''"
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN approvalStatus INTEGER NOT NULL DEFAULT 0"
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN verifiedByUserId INTEGER NOT NULL DEFAULT 0"
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN isClaimed INTEGER NOT NULL DEFAULT 0"
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN approvalDate TEXT NOT NULL DEFAULT '' "
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN calimedDate TEXT  NOT NULL DEFAULT '' "
        )

        database.execSQL(
            "ALTER TABLE INCENTIVE_RECORD ADD COLUMN supervisorRole TEXT NOT NULL DEFAULT '' "
        )

        database.execSQL(
            "ALTER TABLE ALL_EYE_SURGERY_VISIT_HISTORY ADD COLUMN eyeSide TEXT"
        )

        database.execSQL(
            "DROP INDEX IF EXISTS index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_formId_visitMonth"
        )

        database.execSQL("""
            CREATE TABLE ALL_EYE_SURGERY_VISIT_HISTORY_NEW (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                hhId INTEGER NOT NULL,
                visitDate TEXT NOT NULL,
                visitMonth TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                syncedAt TEXT,
                eyeSide TEXT NOT NULL DEFAULT ''
            )
        """)

        // 🔥 Important: remove duplicates before inserting
        database.execSQL("""
            INSERT INTO ALL_EYE_SURGERY_VISIT_HISTORY_NEW (
                id, benId, hhId, visitDate, visitMonth, formId,
                version, formDataJson, isSynced, createdAt, syncedAt, eyeSide
            )
            SELECT 
                MIN(id), benId, hhId, visitDate, visitMonth, formId,
                version, formDataJson, isSynced, createdAt, syncedAt,
                IFNULL(eyeSide, '')
            FROM ALL_EYE_SURGERY_VISIT_HISTORY
            GROUP BY benId, formId, IFNULL(eyeSide, '')
        """)

        database.execSQL("DROP TABLE ALL_EYE_SURGERY_VISIT_HISTORY")

        database.execSQL("""
            ALTER TABLE ALL_EYE_SURGERY_VISIT_HISTORY_NEW 
            RENAME TO ALL_EYE_SURGERY_VISIT_HISTORY
        """)

        // Recreate indexes
        database.execSQL("""
            CREATE UNIQUE INDEX index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_formId_eyeSide 
            ON ALL_EYE_SURGERY_VISIT_HISTORY(benId, formId, eyeSide)
        """)

        database.execSQL("""
            CREATE INDEX index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_visitDate 
            ON ALL_EYE_SURGERY_VISIT_HISTORY(benId, visitDate)
        """)
    }
}

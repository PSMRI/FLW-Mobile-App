package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration55To56 : Migration(55, 56) {
    override fun migrate(database: SupportSQLiteDatabase) {

        if (!columnExists(database, "INCENTIVE_RECORD", "isEligible")) {
            database.execSQL(
                """
            ALTER TABLE INCENTIVE_RECORD
            ADD COLUMN isEligible INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
            )
        }
    }
}

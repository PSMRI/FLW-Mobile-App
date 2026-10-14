package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration59To60 : Migration(59, 60) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            database.execSQL(
                "ALTER TABLE BENEFICIARY ADD COLUMN abha_familyId TEXT"
            )
        } catch (_: Exception) {
        }
    }
}

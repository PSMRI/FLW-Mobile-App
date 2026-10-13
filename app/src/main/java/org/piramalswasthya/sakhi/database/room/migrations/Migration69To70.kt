package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration69To70 : Migration(69, 70) {
    override fun migrate(database: SupportSQLiteDatabase) {
        if (!columnExists(database, "BENEFICIARY", "livingPlace")) {
            database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN livingPlace TEXT")
        }
        if (!columnExists(database, "BENEFICIARY", "otherLivingPlace")) {
            database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN otherLivingPlace TEXT")
        }
        if (!columnExists(database, "BENEFICIARY", "institutionName")) {
            database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN institutionName TEXT")
        }
    }
}

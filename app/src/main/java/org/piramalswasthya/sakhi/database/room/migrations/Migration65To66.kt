package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.addTBScreeningRiskFactorColumns
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration65To66 : Migration(65, 66) {
    override fun migrate(database: SupportSQLiteDatabase) {
        addTBScreeningRiskFactorColumns(database)

        if (!columnExists(database, "CBAC", "cbac_occupational_exposure_other")) {
            database.execSQL("ALTER TABLE CBAC ADD COLUMN cbac_occupational_exposure_other TEXT")
        }
    }
}

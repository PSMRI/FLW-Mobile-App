package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration27To28 : Migration(27, 28) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val cursor = database.query("PRAGMA table_info(PNC_VISIT)")
        val existingColumns = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            existingColumns.add(cursor.getString(1))
        }
        cursor.close()

        if (!existingColumns.contains("deliveryDischargeSummary1")) {
            database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary1 TEXT")
        }
        if (!existingColumns.contains("deliveryDischargeSummary2")) {
            database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary2 TEXT")
        }
        if (!existingColumns.contains("deliveryDischargeSummary3")) {
            database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary3 TEXT")
        }
        if (!existingColumns.contains("deliveryDischargeSummary4")) {
            database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary4 TEXT")
        }
        if (!existingColumns.contains("sterilisationDate")) {
            database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN sterilisationDate INTEGER ")
        }
        if (!existingColumns.contains("anyDangerSign")) {
            database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN anyDangerSign TEXT ")
        }
    }

}

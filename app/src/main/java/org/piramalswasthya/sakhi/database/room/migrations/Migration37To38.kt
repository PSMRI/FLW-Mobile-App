package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration37To38 : Migration(37, 38) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN isSNCU TEXT")
        database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary1 TEXT")
        database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary2 TEXT")
        database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary3 TEXT")
        database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary4 TEXT")
    }
}

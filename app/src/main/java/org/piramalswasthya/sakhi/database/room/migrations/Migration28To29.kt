package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration28To29 : Migration(28, 29) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PMSMA ADD COLUMN visitDate INTEGER")
        database.execSQL("ALTER TABLE PMSMA ADD COLUMN visitNumber INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE PMSMA ADD COLUMN anyOtherHighRiskCondition TEXT")
    }
}

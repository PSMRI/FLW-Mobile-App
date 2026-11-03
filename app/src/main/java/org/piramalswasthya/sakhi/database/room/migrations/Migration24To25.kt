package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration24To25 : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN dateOfAntraInjection TEXT")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN dueDateOfAntraInjection TEXT")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN mpaFile TEXT")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN antraDose TEXT")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN lmp_date INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN lmpDate INTEGER NOT NULL DEFAULT 0")


    }
}

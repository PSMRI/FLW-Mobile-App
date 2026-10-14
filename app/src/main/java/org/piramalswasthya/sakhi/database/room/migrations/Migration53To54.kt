package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration53To54 : Migration(53, 54) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN visitLabel TEXT")
        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN typeOfTBCase TEXT")
        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN reasonForSuspicion TEXT")

        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN hasSymptoms INTEGER NOT NULL DEFAULT 0")

        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN isChestXRayDone INTEGER")
        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN chestXRayResult TEXT")
        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN referralFacility TEXT")

        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN isTBConfirmed INTEGER")
        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN isDRTBConfirmed INTEGER")

        database.execSQL("ALTER TABLE TB_SUSPECTED ADD COLUMN isConfirmed INTEGER NOT NULL DEFAULT 0")
    }
}

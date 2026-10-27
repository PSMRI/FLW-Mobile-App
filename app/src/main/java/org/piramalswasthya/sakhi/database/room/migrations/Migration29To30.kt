package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration29To30 : Migration(29, 30) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary1 TEXT")
        database.execSQL("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary2 TEXT")
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `MAA_MEETING` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`meetingDate` TEXT, `place` TEXT, `participants` INTEGER, `ashaId` INTEGER, " +
                    "`meetingImages` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL)"
        )
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN isKitHandedOver INTEGER ")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitHandedOverDate INTEGER")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitPhoto1 TEXT")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitPhoto2 TEXT")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN lmpDate INTEGER NOT NULL DEFAULT 0 ")
        database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN lmp_date INTEGER NOT NULL DEFAULT 0")

    }
}

package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// General Check-up referral follow-up table (FLW-1229). Same as release-2.13 v74.
object Migration73To74 : Migration(73, 74) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `gc_referral_follow_up` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`benId` INTEGER NOT NULL, " +
                    "`houseHoldId` INTEGER NOT NULL, " +
                    "`referredOnDate` TEXT NOT NULL, " +
                    "`followUpDate` TEXT NOT NULL, " +
                    "`followUpStatus` TEXT, " +
                    "`fieldsJson` TEXT NOT NULL, " +
                    "`isSynced` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_gc_referral_follow_up_benId_followUpDate` " +
                    "ON `gc_referral_follow_up` (`benId`, `followUpDate`)"
        )
    }
}

package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration77To78 : Migration(77, 78) {
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
                    "`updatedAt` INTEGER NOT NULL, " +
                    "`referralReason` TEXT NOT NULL DEFAULT '', " +
                    "`referralMonth` INTEGER NOT NULL DEFAULT 0)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_gc_referral_follow_up_benId_referralReason_referralMonth` " +
                    "ON `gc_referral_follow_up` (`benId`, `referralReason`, `referralMonth`)"
        )
    }
}

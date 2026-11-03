package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration67To68 : Migration(67, 68) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `tb_tpt_follow_up` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`benId` INTEGER NOT NULL, " +
                    "`houseHoldId` INTEGER NOT NULL, " +
                    "`visitNo` INTEGER NOT NULL, " +
                    "`followUpNo` INTEGER NOT NULL, " +
                    "`treatmentType` TEXT, " +
                    "`treatmentStartDate` TEXT, " +
                    "`followUpDate` TEXT, " +
                    "`formId` TEXT NOT NULL, " +
                    "`version` INTEGER NOT NULL, " +
                    "`fieldsJson` TEXT NOT NULL, " +
                    "`isSynced` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_tb_tpt_follow_up_benId_visitNo_followUpNo` " +
                    "ON `tb_tpt_follow_up` (`benId`, `visitNo`, `followUpNo`)"
        )
    }
}

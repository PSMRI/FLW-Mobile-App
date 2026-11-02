package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

object Migration71To72 : Migration(71, 72) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `GENERAL_CHECKUP` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `checkupId` TEXT NOT NULL, " +
                    "`benId` INTEGER NOT NULL, `hhId` INTEGER NOT NULL, `ashaId` INTEGER NOT NULL, " +
                    "`checkupDate` INTEGER NOT NULL, `problemTypeId` TEXT NOT NULL, " +
                    "`problemTypeLabel` TEXT NOT NULL, `problemIds` TEXT NOT NULL, " +
                    "`problemLabels` TEXT NOT NULL, `otherText` TEXT, `priority` TEXT NOT NULL, " +
                    "`masterVersion` INTEGER NOT NULL, `referralReason` TEXT NOT NULL, " +
                    "`referralDate` INTEGER NOT NULL, `referralMonth` INTEGER NOT NULL, " +
                    "`formData` TEXT, `createdBy` TEXT NOT NULL, `createdDate` INTEGER NOT NULL, " +
                    "`updatedDate` INTEGER NOT NULL, `syncState` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`benId`) REFERENCES `BENEFICIARY`(`beneficiaryId`) " +
                    "ON UPDATE CASCADE ON DELETE CASCADE )"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `ind_gc_checkup_id` ON `GENERAL_CHECKUP` (`checkupId`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `ind_gc_ben` ON `GENERAL_CHECKUP` (`benId`)"
        )
        if (!columnExists(database, "NCD_REFER", "priority")) {
            database.execSQL("ALTER TABLE NCD_REFER ADD COLUMN priority TEXT")
        }
        if (!columnExists(database, "NCD_REFER", "revisitMonth")) {
            database.execSQL("ALTER TABLE NCD_REFER ADD COLUMN revisitMonth INTEGER NOT NULL DEFAULT 0")
        }
        database.execSQL(
            "UPDATE NCD_REFER SET revisitMonth = " +
                    "CAST(strftime('%Y%m', revisitDate / 1000, 'unixepoch', 'localtime') AS INTEGER)"
        )
        database.execSQL("DROP INDEX IF EXISTS `ind_refcache`")
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `ind_refcache` ON `NCD_REFER` " +
                    "(`benId`, `referralReason`, `revisitMonth`)"
        )
    }
}

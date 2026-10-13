package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.addTBScreeningRiskFactorColumns
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.recreateBenBasicCacheView

object Migration66To67 : Migration(66, 67) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // The gamification build still had BEN_BASIC_CACHE in its pre-65 form.
        recreateBenBasicCacheView(database)

        if (!columnExists(database, "Adolescent_Health_Form_Data", "isSanitaryNapkinUsed")) {
            database.execSQL("ALTER TABLE Adolescent_Health_Form_Data ADD COLUMN isSanitaryNapkinUsed INTEGER")
        }

        addTBScreeningRiskFactorColumns(database)

        if (!columnExists(database, "CBAC", "cbac_occupational_exposure_other")) {
            database.execSQL("ALTER TABLE CBAC ADD COLUMN cbac_occupational_exposure_other TEXT")
        }

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `tb_referral_follow_up` (" +
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
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_tb_referral_follow_up_benId_followUpDate` " +
                    "ON `tb_referral_follow_up` (`benId`, `followUpDate`)"
        )
    }
}

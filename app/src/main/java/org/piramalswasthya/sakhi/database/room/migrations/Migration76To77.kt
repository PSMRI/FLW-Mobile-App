package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists

// TB Suspected: rebuild with isRecommendedForTpt (from release-2.14, originally numbered 74 -> 75).
object Migration76To77 : Migration(76, 77) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val tptSource = when {
            columnExists(db, "TB_SUSPECTED", "recommendedForTpt") -> "`recommendedForTpt`"
            columnExists(db, "TB_SUSPECTED", "isRecommendedForTpt") -> "`isRecommendedForTpt`"
            else -> "NULL"
        }
        db.execSQL("DROP TABLE IF EXISTS `TB_SUSPECTED_new`")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `TB_SUSPECTED_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `benId` INTEGER NOT NULL,
                `visitDate` INTEGER NOT NULL,
                `visitLabel` TEXT,
                `typeOfTBCase` TEXT,
                `reasonForSuspicion` TEXT,
                `hasSymptoms` INTEGER NOT NULL,
                `isSputumCollected` INTEGER,
                `sputumSubmittedAt` TEXT,
                `nikshayId` TEXT,
                `sputumTestResult` TEXT,
                `isChestXRayDone` INTEGER,
                `chestXRayResult` TEXT,
                `referralFacility` TEXT,
                `isTBConfirmed` INTEGER,
                `isDRTBConfirmed` INTEGER,
                `isRecommendedForTpt` INTEGER,
                `isConfirmed` INTEGER NOT NULL,
                `referred` INTEGER,
                `followUps` TEXT,
                `syncState` INTEGER NOT NULL,
                FOREIGN KEY(`benId`) REFERENCES `BENEFICIARY`(`beneficiaryId`)
                    ON UPDATE CASCADE ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `TB_SUSPECTED_new` (
                `id`, `benId`, `visitDate`, `visitLabel`, `typeOfTBCase`, `reasonForSuspicion`,
                `hasSymptoms`, `isSputumCollected`, `sputumSubmittedAt`, `nikshayId`,
                `sputumTestResult`, `isChestXRayDone`, `chestXRayResult`, `referralFacility`,
                `isTBConfirmed`, `isDRTBConfirmed`, `isRecommendedForTpt`, `isConfirmed`,
                `referred`, `followUps`, `syncState`
            )
            SELECT
                `id`, `benId`, `visitDate`, `visitLabel`, `typeOfTBCase`, `reasonForSuspicion`,
                `hasSymptoms`, `isSputumCollected`, `sputumSubmittedAt`, `nikshayId`,
                `sputumTestResult`, `isChestXRayDone`, `chestXRayResult`, `referralFacility`,
                `isTBConfirmed`, `isDRTBConfirmed`, $tptSource, `isConfirmed`,
                `referred`, `followUps`, `syncState`
            FROM `TB_SUSPECTED`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `TB_SUSPECTED`")
        db.execSQL("ALTER TABLE `TB_SUSPECTED_new` RENAME TO `TB_SUSPECTED`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `ind_tbs` ON `TB_SUSPECTED` (`benId`)")
    }
}

package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration74To75 : Migration(74, 75) {
    override fun migrate(db: SupportSQLiteDatabase) {
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
                `isTBConfirmed`, `isDRTBConfirmed`, `recommendedForTpt`, `isConfirmed`,
                `referred`, `followUps`, `syncState`
            FROM `TB_SUSPECTED`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `TB_SUSPECTED`")
        db.execSQL("ALTER TABLE `TB_SUSPECTED_new` RENAME TO `TB_SUSPECTED`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `ind_tbs` ON `TB_SUSPECTED` (`benId`)")
    }
}

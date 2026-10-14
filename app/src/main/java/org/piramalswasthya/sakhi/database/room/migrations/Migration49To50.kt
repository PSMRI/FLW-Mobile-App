package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration49To50 : Migration(49, 50) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN vhndPlaceId INTEGER DEFAULT 0"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN pregnantWomenAnc TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN lactatingMothersPnc TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN childrenImmunization TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN knowledgeBalancedDiet TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN careDuringPregnancy TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN importanceBreastfeeding TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN complementaryFeeding TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN hygieneSanitation TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN familyPlanningHealthcare TEXT"
        )

        database.execSQL(
            "ALTER TABLE VHND ADD COLUMN selectAllEducation INTEGER DEFAULT 0"
        )

        // ncd_refer

        database.execSQL("DROP TABLE IF EXISTS ncd_referal_all_visit")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ncd_referal_all_visit` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `benId` INTEGER NOT NULL,
                `hhId` INTEGER NOT NULL,
                `visitNo` INTEGER NOT NULL,
                `followUpNo` INTEGER NOT NULL,
                `treatmentStartDate` TEXT NOT NULL,
                `followUpDate` TEXT,
                `diagnosisCodes` TEXT,
                `formId` TEXT NOT NULL,
                `version` INTEGER NOT NULL,
                `formDataJson` TEXT NOT NULL,
                `isSynced` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `syncedAt` INTEGER
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_ncd_visit_ben_hh`
            ON `ncd_referal_all_visit` (`benId`, `hhId`)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_ncd_visit_followup`
            ON `ncd_referal_all_visit` (`benId`, `hhId`, `visitNo`, `followUpNo`)
            """.trimIndent()
        )
    }
}

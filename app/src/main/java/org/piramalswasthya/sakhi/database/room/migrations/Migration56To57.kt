package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration56To57 : Migration(56, 57) {
 override fun migrate(database: SupportSQLiteDatabase) {
     val householdLocColumns = listOf(
         "loc_country_id INTEGER NOT NULL DEFAULT 0",
         "loc_country_name TEXT NOT NULL DEFAULT ''",
         "loc_country_nameHindi TEXT",
         "loc_country_nameAssamese TEXT",
         "loc_state_id INTEGER NOT NULL DEFAULT 0",
         "loc_state_name TEXT NOT NULL DEFAULT ''",
         "loc_state_nameHindi TEXT",
         "loc_state_nameAssamese TEXT",
         "loc_district_id INTEGER NOT NULL DEFAULT 0",
         "loc_district_name TEXT NOT NULL DEFAULT ''",
         "loc_district_nameHindi TEXT",
         "loc_district_nameAssamese TEXT",
         "loc_block_id INTEGER NOT NULL DEFAULT 0",
         "loc_block_name TEXT NOT NULL DEFAULT ''",
         "loc_block_nameHindi TEXT",
         "loc_block_nameAssamese TEXT",
         "loc_village_id INTEGER NOT NULL DEFAULT 0",
         "loc_village_name TEXT NOT NULL DEFAULT ''",
         "loc_village_nameHindi TEXT",
         "loc_village_nameAssamese TEXT"
     )
     for (column in householdLocColumns) {
         val columnName = column.split(" ")[0]
              if (!columnExists(database, "HOUSEHOLD", columnName)) {
             database.execSQL("ALTER TABLE HOUSEHOLD ADD COLUMN $column")
         }
     }
     if (!columnExists(database, "HOUSEHOLD", "isDeactivate")) {
         database.execSQL("ALTER TABLE HOUSEHOLD ADD COLUMN isDeactivate INTEGER NOT NULL DEFAULT 0")
     }

     if (!columnExists(database, "BENEFICIARY", "isSpouseAdded")) {
         database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isSpouseAdded INTEGER NOT NULL DEFAULT 0")
     }
     if (!columnExists(database, "BENEFICIARY", "isChildrenAdded")) {
         database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isChildrenAdded INTEGER NOT NULL DEFAULT 0")
     }
     if (!columnExists(database, "BENEFICIARY", "isMarried")) {
         database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isMarried INTEGER NOT NULL DEFAULT 0")
     }
     if (!columnExists(database, "BENEFICIARY", "noOfChildren")) {
         database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN noOfChildren INTEGER NOT NULL DEFAULT 0")
     }
     if (!columnExists(database, "BENEFICIARY", "noOfAliveChildren")) {
         database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN noOfAliveChildren INTEGER NOT NULL DEFAULT 0")
     }
     if (!columnExists(database, "BENEFICIARY", "doYouHavechildren")) {
         database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN doYouHavechildren INTEGER NOT NULL DEFAULT 0")
     }
     if (!columnExists(database, "BENEFICIARY", "isDeactivate")) {
         database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isDeactivate INTEGER NOT NULL DEFAULT 0")
     }

     // Update isMarried based on existing gen_maritalStatusId data.
     database.execSQL("""
                        UPDATE BENEFICIARY
                        SET isMarried = CASE
                            WHEN gen_maritalStatusId = 2 THEN 1
                            ELSE 0
                        END
                        WHERE isMarried = 0
                    """.trimIndent())

     // ========================================================
     // 2b. PREGNANCY_ANC: add placeOfAnc columns
     //     (added to MIGRATION_45_46 after release-2.7,
     //      so users upgrading from v46 never got them)
     // ========================================================
     if (!columnExists(database, "PREGNANCY_ANC", "placeOfAnc")) {
         database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfAnc TEXT")
     }
     if (!columnExists(database, "PREGNANCY_ANC", "placeOfAncId")) {
         database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfAncId INTEGER")
     }

     // ========================================================
     // 3. CREATE new tables
     // ========================================================
     database.execSQL(
         """
                        CREATE TABLE IF NOT EXISTS `PulsePolioCampaign` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `formDataJson` TEXT,
                            `syncState` INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent()
     )

     database.execSQL(
         """
                        CREATE TABLE IF NOT EXISTS `ORSCampaign` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `formDataJson` TEXT,
                            `syncState` INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent()
     )

     database.execSQL(
         """
                        CREATE TABLE IF NOT EXISTS `TB_CONFIRMED_TREATMENT` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `benId` INTEGER NOT NULL,
                            `regimenType` TEXT,
                            `treatmentStartDate` INTEGER NOT NULL,
                            `expectedTreatmentCompletionDate` INTEGER,
                            `followUpDate` INTEGER,
                            `monthlyFollowUpDone` TEXT,
                            `adherenceToMedicines` TEXT,
                            `anyDiscomfort` INTEGER,
                            `treatmentCompleted` INTEGER,
                            `actualTreatmentCompletionDate` INTEGER,
                            `treatmentOutcome` TEXT,
                            `dateOfDeath` INTEGER,
                            `placeOfDeath` TEXT,
                            `reasonForDeath` TEXT NOT NULL DEFAULT 'Tuberculosis',
                            `reasonForNotCompleting` TEXT,
                            `syncState` INTEGER NOT NULL DEFAULT 0,
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL,
                            FOREIGN KEY(`benId`) REFERENCES `BENEFICIARY`(`beneficiaryId`) ON UPDATE CASCADE ON DELETE CASCADE
                        )
                        """.trimIndent()
     )
     database.execSQL("CREATE INDEX IF NOT EXISTS `ind_tb_confirmed` ON `TB_CONFIRMED_TREATMENT` (`benId`)")

     database.execSQL(
         """
                        CREATE TABLE IF NOT EXISTS `ALL_VISIT_HISTORY_ANC` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `benId` INTEGER NOT NULL,
                            `visitDay` TEXT NOT NULL,
                            `visitDate` TEXT NOT NULL,
                            `formId` TEXT NOT NULL,
                            `version` INTEGER NOT NULL,
                            `formDataJson` TEXT NOT NULL,
                            `isSynced` INTEGER NOT NULL DEFAULT 0,
                            `createdAt` INTEGER NOT NULL,
                            `syncedAt` INTEGER
                        )
                        """.trimIndent()
     )
     database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ALL_VISIT_HISTORY_ANC_benId_visitDay_visitDate_formId` ON `ALL_VISIT_HISTORY_ANC` (`benId`, `visitDay`, `visitDate`, `formId`)")

     // ========================================================
     // 4. TB_SCREENING: add 11 new columns
     // ========================================================
     val tbScreeningColumns = listOf(
         "riseOfFever INTEGER",
         "lossOfAppetite INTEGER",
         "age INTEGER",
         "diabetic INTEGER",
         "tobaccoUser INTEGER",
         "bmi INTEGER",
         "contactWithTBPatient INTEGER",
         "historyOfTBInLastFiveYrs INTEGER",
         "sympotomatic TEXT",
         "asymptomatic TEXT",
         "recommandateTest TEXT"
     )
     for (column in tbScreeningColumns) {
         val columnName = column.split(" ")[0]
         if (!columnExists(database, "TB_SCREENING", columnName)) {
             database.execSQL("ALTER TABLE TB_SCREENING ADD COLUMN $column")
         }
     }

     // ========================================================
     // 5. VHNC: recreate table to fix wrong-name columns
     //    MIGRATION_48_49 added noOfPregnantWomen,
     //    noOfLactatingMother, followupPrevious (wrong names).
     //    Entity expects noOfPragnentWoment, noOfLactingMother,
     //    followupPrevius. Room requires exact column match,
     //    so extra columns cause failure. Recreate the table.
     // ========================================================
     if (tableExists(database, "VHNC")) {
         database.execSQL(
             """
                            CREATE TABLE IF NOT EXISTS `VHNC_new` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `vhncDate` TEXT NOT NULL,
                                `place` TEXT,
                                `noOfBeneficiariesAttended` INTEGER,
                                `image1` TEXT,
                                `image2` TEXT,
                                `villageName` TEXT,
                                `anm` INTEGER,
                                `aww` INTEGER,
                                `noOfPragnentWoment` INTEGER DEFAULT 0,
                                `noOfLactingMother` INTEGER DEFAULT 0,
                                `noOfCommittee` INTEGER DEFAULT 0,
                                `followupPrevius` INTEGER,
                                `syncState` INTEGER NOT NULL DEFAULT 0
                            )
                            """.trimIndent()
         )
         // Copy data, preferring correctly-named columns if they exist,
         // falling back to wrong-named columns from MIGRATION_48_49
         val hasCorrectPragnent = columnExists(database, "VHNC", "noOfPragnentWoment")
         val hasWrongPregnant = columnExists(database, "VHNC", "noOfPregnantWomen")
         val hasCorrectLacting = columnExists(database, "VHNC", "noOfLactingMother")
         val hasWrongLactating = columnExists(database, "VHNC", "noOfLactatingMother")
         val hasCorrectFollowup = columnExists(database, "VHNC", "followupPrevius")
         val hasWrongFollowup = columnExists(database, "VHNC", "followupPrevious")

         val pragnentSrc = when {
             hasCorrectPragnent -> "`noOfPragnentWoment`"
             hasWrongPregnant -> "`noOfPregnantWomen`"
             else -> "0"
         }
         val lactingSrc = when {
             hasCorrectLacting -> "`noOfLactingMother`"
             hasWrongLactating -> "`noOfLactatingMother`"
             else -> "0"
         }
         val followupSrc = when {
             hasCorrectFollowup -> "`followupPrevius`"
             hasWrongFollowup -> "`followupPrevious`"
             else -> "NULL"
         }

         database.execSQL(
             """
                            INSERT INTO `VHNC_new`
                                (`id`, `vhncDate`, `place`, `noOfBeneficiariesAttended`,
                                 `image1`, `image2`, `villageName`, `anm`, `aww`,
                                 `noOfPragnentWoment`, `noOfLactingMother`, `noOfCommittee`,
                                 `followupPrevius`, `syncState`)
                            SELECT `id`, `vhncDate`, `place`, `noOfBeneficiariesAttended`,
                                `image1`, `image2`,
                                ${if (columnExists(database, "VHNC", "villageName")) "`villageName`" else "NULL"},
                                ${if (columnExists(database, "VHNC", "anm")) "`anm`" else "NULL"},
                                ${if (columnExists(database, "VHNC", "aww")) "`aww`" else "NULL"},
                                $pragnentSrc,
                                $lactingSrc,
                                ${if (columnExists(database, "VHNC", "noOfCommittee")) "`noOfCommittee`" else "0"},
                                $followupSrc,
                                `syncState`
                            FROM `VHNC`
                            """.trimIndent()
         )
         database.execSQL("DROP TABLE `VHNC`")
         database.execSQL("ALTER TABLE `VHNC_new` RENAME TO `VHNC`")
     } else {
         database.execSQL(
             """
                            CREATE TABLE IF NOT EXISTS `VHNC` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `vhncDate` TEXT NOT NULL,
                                `place` TEXT,
                                `noOfBeneficiariesAttended` INTEGER,
                                `image1` TEXT,
                                `image2` TEXT,
                                `villageName` TEXT,
                                `anm` INTEGER,
                                `aww` INTEGER,
                                `noOfPragnentWoment` INTEGER DEFAULT 0,
                                `noOfLactingMother` INTEGER DEFAULT 0,
                                `noOfCommittee` INTEGER DEFAULT 0,
                                `followupPrevius` INTEGER,
                                `syncState` INTEGER NOT NULL DEFAULT 0
                            )
                            """.trimIndent()
         )
     }

     // ========================================================
     // 6. NCD_REFER: fix unique index (benId) -> (benId, referralReason)
     // ========================================================
     database.execSQL("DROP INDEX IF EXISTS `ind_refcache`")
     database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `ind_refcache` ON `NCD_REFER` (`benId`, `referralReason`)")

     // ========================================================
     // 7. ncd_referal_all_visit: fix index names + uniqueness
     //    (MIGRATION_49_50 used wrong names and missed UNIQUE)
     // ========================================================
     if (tableExists(database, "ncd_referal_all_visit")) {
         database.execSQL("DROP INDEX IF EXISTS `index_ncd_visit_ben_hh`")
         database.execSQL("DROP INDEX IF EXISTS `index_ncd_visit_followup`")
         database.execSQL("DROP INDEX IF EXISTS `index_ncd_referal_all_visit_benId_hhId`")
         database.execSQL("DROP INDEX IF EXISTS `index_ncd_referal_all_visit_benId_hhId_visitNo_followUpNo`")
         database.execSQL("CREATE INDEX IF NOT EXISTS `index_ncd_referal_all_visit_benId_hhId` ON `ncd_referal_all_visit` (`benId`, `hhId`)")
         database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ncd_referal_all_visit_benId_hhId_visitNo_followUpNo` ON `ncd_referal_all_visit` (`benId`, `hhId`, `visitNo`, `followUpNo`)")
     }

     // ========================================================
     // 8. MAA_MEETING: add 4 missing columns
     //    (MIGRATION_51_52 used CREATE TABLE IF NOT EXISTS which
     //     is a no-op since the table already existed from v29)
     // ========================================================
     if (tableExists(database, "MAA_MEETING")) {
         if (!columnExists(database, "MAA_MEETING", "villageName")) {
             database.execSQL("ALTER TABLE MAA_MEETING ADD COLUMN villageName TEXT")
         }
         if (!columnExists(database, "MAA_MEETING", "mitaninActivityCheckList")) {
             database.execSQL("ALTER TABLE MAA_MEETING ADD COLUMN mitaninActivityCheckList TEXT")
         }
         if (!columnExists(database, "MAA_MEETING", "noOfPragnentWomen")) {
             database.execSQL("ALTER TABLE MAA_MEETING ADD COLUMN noOfPragnentWomen TEXT")
         }
         if (!columnExists(database, "MAA_MEETING", "noOfLactingMother")) {
             database.execSQL("ALTER TABLE MAA_MEETING ADD COLUMN noOfLactingMother TEXT")
         }
     }

     // ========================================================
     // 9. FILARIA_MDA_CAMPAIGN_HISTORY: fix column types
     //    (MIGRATION_52_53 used syncState TEXT instead of INTEGER,
     //     and isSynced nullable instead of NOT NULL)
     // ========================================================
     if (tableExists(database, "FILARIA_MDA_CAMPAIGN_HISTORY")) {
         database.execSQL(
             """
                            CREATE TABLE IF NOT EXISTS `FILARIA_MDA_CAMPAIGN_HISTORY_new` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `visitDate` TEXT NOT NULL,
                                `visitYear` TEXT NOT NULL,
                                `formId` TEXT NOT NULL,
                                `version` INTEGER NOT NULL,
                                `formDataJson` TEXT NOT NULL,
                                `isSynced` INTEGER NOT NULL DEFAULT 0,
                                `syncState` INTEGER NOT NULL DEFAULT 0,
                                `createdAt` INTEGER NOT NULL,
                                `syncedAt` TEXT
                            )
                            """.trimIndent()
         )
         database.execSQL(
             """
                            INSERT OR IGNORE INTO `FILARIA_MDA_CAMPAIGN_HISTORY_new`
                                (`id`, `visitDate`, `visitYear`, `formId`, `version`, `formDataJson`,
                                 `isSynced`, `syncState`, `createdAt`, `syncedAt`)
                            SELECT `id`, `visitDate`, `visitYear`, `formId`, `version`, `formDataJson`,
                                COALESCE(`isSynced`, 0),
                                CASE WHEN typeof(`syncState`) = 'text' THEN
                                    CASE WHEN `syncState` = 'SYNCED' THEN 2
                                         WHEN `syncState` = 'SYNCING' THEN 1
                                         ELSE 0 END
                                ELSE COALESCE(`syncState`, 0) END,
                                `createdAt`, `syncedAt`
                            FROM `FILARIA_MDA_CAMPAIGN_HISTORY`
                            """.trimIndent()
         )
         database.execSQL("DROP TABLE `FILARIA_MDA_CAMPAIGN_HISTORY`")
         database.execSQL("ALTER TABLE `FILARIA_MDA_CAMPAIGN_HISTORY_new` RENAME TO `FILARIA_MDA_CAMPAIGN_HISTORY`")
     } else {
         database.execSQL(
             """
                            CREATE TABLE IF NOT EXISTS `FILARIA_MDA_CAMPAIGN_HISTORY` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `visitDate` TEXT NOT NULL,
                                `visitYear` TEXT NOT NULL,
                                `formId` TEXT NOT NULL,
                                `version` INTEGER NOT NULL,
                                `formDataJson` TEXT NOT NULL,
                                `isSynced` INTEGER NOT NULL DEFAULT 0,
                                `syncState` INTEGER NOT NULL DEFAULT 0,
                                `createdAt` INTEGER NOT NULL,
                                `syncedAt` TEXT
                            )
                            """.trimIndent()
         )
     }
     database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_FILARIA_MDA_CAMPAIGN_HISTORY_formId_visitYear` ON `FILARIA_MDA_CAMPAIGN_HISTORY` (`formId`, `visitYear`)")
     database.execSQL("CREATE INDEX IF NOT EXISTS `index_FILARIA_MDA_CAMPAIGN_HISTORY_visitDate` ON `FILARIA_MDA_CAMPAIGN_HISTORY` (`visitDate`)")

     // ========================================================
     // 10. BEN_BASIC_CACHE: drop and recreate view
     //     (MIGRATION_45_46 reverted 6 columns from 44_45,
     //      and isDeactivate was never added to the view)
     // ========================================================
     database.execSQL("DROP VIEW IF EXISTS `BEN_BASIC_CACHE`")
     database.execSQL(
         """CREATE VIEW `BEN_BASIC_CACHE` AS SELECT b.beneficiaryId as benId,b.isMarried,b.noOfAliveChildren, b.noOfChildren, b.doYouHavechildren ,b.isConsent as isConsent, b.motherName as motherName, b.householdId as hhId, b.regDate, b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob,b.isDeactivate, b.isDeath,b.isDeathValue,b.dateOfDeath,b.timeOfDeath,b.reasonOfDeath,b.reasonOfDeathId,b.placeOfDeath,b.placeOfDeathId,b.otherPlaceOfDeath,b.isSpouseAdded,b.isChildrenAdded, b.familyHeadRelationPosition as relToHeadId, b.contactNumber as mobileNo, b.fatherName,h.fam_familyHeadName as familyHeadName, b.gen_spouseName as spouseName, b.rchId, b.gen_lastMenstrualPeriod as lastMenstrualPeriod, b.isHrpStatus as hrpStatus, b.syncState, b.gen_reproductiveStatusId as reproductiveStatusId, b.isKid, b.immunizationStatus, b.loc_village_id as villageId, b.abha_healthIdNumber as abhaId, b.isNewAbha, IFNULL(cbac.benId IS NOT NULL, 0) as cbacFilled, cbac.syncState as cbacSyncState, IFNULL(cdr.benId IS NOT NULL, 0) as cdrFilled, cdr.syncState as cdrSyncState, IFNULL(mdsr.benId IS NOT NULL, 0) as mdsrFilled, mdsr.syncState as mdsrSyncState, IFNULL(pmsma.benId IS NOT NULL, 0) as pmsmaFilled, pmsma.syncState as pmsmaSyncState, IFNULL(hbnc.benId IS NOT NULL, 0) as hbncFilled, IFNULL(hbyc.benId IS NOT NULL, 0) as hbycFilled, IFNULL(pwr.benId IS NOT NULL, 0) as pwrFilled, pwr.syncState as pwrSyncState, IFNULL(pwa.pregnantWomanDelivered, 0) as isDelivered, IFNULL(pwa.hrpConfirmed, 0) as pwHrp, IFNULL(ecr.benId IS NOT NULL, 0) as ecrFilled, IFNULL(ect.benId IS NOT NULL, 0) as ectFilled, IFNULL((pwa.maternalDeath OR do.complication = 'DEATH' OR pnc.motherDeath), 0) as isMdsr, IFNULL(tbsn.benId IS NOT NULL, 0) as tbsnFilled, tbsn.syncState as tbsnSyncState, IFNULL(tbsp.benId IS NOT NULL, 0) as tbspFilled, tbsp.syncState as tbspSyncState, IFNULL(ir.motherBenId IS NOT NULL, 0) as irFilled, ir.syncState as irSyncState, IFNULL(cr.motherBenId IS NOT NULL, 0) as crFilled, cr.syncState as crSyncState, IFNULL(do.benId IS NOT NULL, 0) as doFilled, do.syncState as doSyncState, IFNULL((hrppa.benId IS NOT NULL AND hrppa.noOfDeliveries IS NOT NULL AND hrppa.timeLessThan18m IS NOT NULL AND hrppa.heightShort IS NOT NULL AND hrppa.age IS NOT NULL AND hrppa.rhNegative IS NOT NULL AND hrppa.homeDelivery IS NOT NULL AND hrppa.badObstetric IS NOT NULL AND hrppa.multiplePregnancy IS NOT NULL), 0) as hrppaFilled, hrppa.syncState as hrppaSyncState, IFNULL((hrpnpa.benId IS NOT NULL AND hrpnpa.noOfDeliveries IS NOT NULL AND hrpnpa.timeLessThan18m IS NOT NULL AND hrpnpa.heightShort IS NOT NULL AND hrpnpa.age IS NOT NULL AND hrpnpa.misCarriage IS NOT NULL AND hrpnpa.homeDelivery IS NOT NULL AND hrpnpa.medicalIssues IS NOT NULL AND hrpnpa.pastCSection IS NOT NULL), 0) as hrpnpaFilled, hrpnpa.syncState as hrpnpaSyncState, IFNULL(hrpmbp.benId IS NOT NULL, 0) as hrpmbpFilled, hrpmbp.syncState as hrpmbpSyncState, IFNULL(hrpt.benId IS NOT NULL, 0) as hrptFilled, IFNULL(((count(distinct hrpt.id) > 3) OR (((JulianDay('now')) - JulianDay(date(max(hrpt.visitDate)/1000,'unixepoch','localtime'))) < 1)), 0) as hrptrackingDone, hrpt.syncState as hrptSyncState, IFNULL(hrnpt.benId IS NOT NULL, 0) as hrnptFilled, IFNULL(((JulianDay('now') - JulianDay(date(max(hrnpt.visitDate)/1000,'unixepoch','localtime'))) < 1), 0) as hrnptrackingDone, hrnpt.syncState as hrnptSyncState FROM BENEFICIARY b JOIN HOUSEHOLD h ON b.householdId = h.householdId LEFT OUTER JOIN CBAC cbac ON b.beneficiaryId = cbac.benId LEFT OUTER JOIN CDR cdr ON b.beneficiaryId = cdr.benId LEFT OUTER JOIN MDSR mdsr ON b.beneficiaryId = mdsr.benId LEFT OUTER JOIN PMSMA pmsma ON b.beneficiaryId = pmsma.benId LEFT OUTER JOIN HBNC hbnc ON b.beneficiaryId = hbnc.benId LEFT OUTER JOIN HBYC hbyc ON b.beneficiaryId = hbyc.benId LEFT OUTER JOIN PREGNANCY_REGISTER pwr ON b.beneficiaryId = pwr.benId LEFT OUTER JOIN PREGNANCY_ANC pwa ON b.beneficiaryId = pwa.benId LEFT OUTER JOIN pnc_visit pnc ON b.beneficiaryId = pnc.benId LEFT OUTER JOIN ELIGIBLE_COUPLE_REG ecr ON b.beneficiaryId = ecr.benId LEFT OUTER JOIN ELIGIBLE_COUPLE_TRACKING ect ON (b.beneficiaryId = ect.benId AND CAST((strftime('%s','now') - ect.visitDate/1000)/60/60/24 AS INTEGER) < 30) LEFT OUTER JOIN TB_SCREENING tbsn ON b.beneficiaryId = tbsn.benId LEFT OUTER JOIN TB_SUSPECTED tbsp ON b.beneficiaryId = tbsp.benId LEFT OUTER JOIN MALARIA_SCREENING masp on b.beneficiaryId = masp.benId LEFT OUTER JOIN MALARIA_CONFIRMED macp on b.beneficiaryId = macp.benId LEFT OUTER JOIN HRP_PREGNANT_ASSESS hrppa ON b.beneficiaryId = hrppa.benId LEFT OUTER JOIN HRP_NON_PREGNANT_ASSESS hrpnpa ON b.beneficiaryId = hrpnpa.benId LEFT OUTER JOIN HRP_MICRO_BIRTH_PLAN hrpmbp ON b.beneficiaryId = hrpmbp.benId LEFT OUTER JOIN HRP_NON_PREGNANT_TRACK hrnpt ON b.beneficiaryId = hrnpt.benId LEFT OUTER JOIN HRP_PREGNANT_TRACK hrpt ON b.beneficiaryId = hrpt.benId LEFT OUTER JOIN DELIVERY_OUTCOME do ON b.beneficiaryId = do.benId LEFT OUTER JOIN INFANT_REG ir ON b.beneficiaryId = ir.motherBenId LEFT OUTER JOIN CHILD_REG cr ON b.beneficiaryId = cr.motherBenId WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC"""
     )
 }
}

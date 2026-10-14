package org.piramalswasthya.sakhi.database.room.migrations

import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationHelpers {

    const val MIGRATION_60_61_NORMALIZE_ISDEATH_SQL =
        "UPDATE BENEFICIARY SET isDeath = 0 " +
                "WHERE isDeath IS NULL OR (isDeath <> 0 AND isDeath <> 1)"

    fun tableExists(db: SupportSQLiteDatabase, tableName: String): Boolean {
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun columnExists(
        db: SupportSQLiteDatabase,
        tableName: String,
        columnName: String
    ): Boolean {
        val cursor = db.query("PRAGMA table_info($tableName)")
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == columnName) {
                cursor.close()
                return true
            }
        }
        cursor.close()
        return false
    }

     fun addTBScreeningRiskFactorColumns(database: SupportSQLiteDatabase) {
        val columns = listOf(
            "keyPopulationRiskFactorIds TEXT DEFAULT NULL",
            "keyPopulationRiskFactors TEXT DEFAULT NULL",
            "hivStatusId INTEGER DEFAULT NULL",
            "hivStatus TEXT DEFAULT NULL",
            "fatigue INTEGER DEFAULT NULL",
            "shortBreath INTEGER DEFAULT NULL",
            "chestPain INTEGER DEFAULT NULL"

        )
        columns.forEach { columnDefinition ->
            val columnName = columnDefinition.substringBefore(" ")
            if (!columnExists(database, "TB_SCREENING", columnName)) {
                database.execSQL("ALTER TABLE TB_SCREENING ADD COLUMN $columnDefinition")
            }
        }
    }

     fun recreateBenBasicCacheView(database: SupportSQLiteDatabase) {
        database.execSQL("DROP VIEW IF EXISTS `BEN_BASIC_CACHE`")
        database.execSQL(
            "CREATE VIEW `BEN_BASIC_CACHE` AS " +
                    "SELECT b.beneficiaryId as benId,b.isMarried,b.noOfAliveChildren, b.noOfChildren, b.doYouHavechildren ,b.isConsent as isConsent, b.motherName as motherName, b.householdId as hhId, b.regDate, b.createdDate as createdDate, b.updatedDate as updatedDate, b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob,b.isDeactivate, b.isDeath,b.isDeathValue,b.dateOfDeath,b.timeOfDeath,b.reasonOfDeath,b.reasonOfDeathId,b.placeOfDeath,b.placeOfDeathId,b.otherPlaceOfDeath,b.isSpouseAdded,b.isChildrenAdded, b.familyHeadRelationPosition as relToHeadId" +
                    ", b.contactNumber as mobileNo, b.fatherName,h.fam_familyHeadName as familyHeadName, b.gen_spouseName as spouseName, b.rchId, b.gen_lastMenstrualPeriod as lastMenstrualPeriod" +
                    ", b.isHrpStatus as hrpStatus, b.syncState, b.gen_reproductiveStatusId as reproductiveStatusId, b.isKid, b.immunizationStatus" +
                    ", b.loc_village_id as villageId, b.abha_healthIdNumber as abhaId" +
                    ", b.isNewAbha" +
                    ", IFNULL(cbac.benId IS NOT NULL, 0) as cbacFilled, cbac.syncState as cbacSyncState" +
                    ", IFNULL(cdr.benId IS NOT NULL, 0) as cdrFilled, cdr.syncState as cdrSyncState" +
                    ", IFNULL(mdsr.benId IS NOT NULL, 0) as mdsrFilled, mdsr.syncState as mdsrSyncState" +
                    ", IFNULL(pmsma.benId IS NOT NULL, 0) as pmsmaFilled, pmsma.syncState as pmsmaSyncState" +
                    ", IFNULL(hbnc.benId IS NOT NULL, 0) as hbncFilled" +
                    ", IFNULL(hbyc.benId IS NOT NULL, 0) as hbycFilled" +
                    ", IFNULL(pwr.benId IS NOT NULL, 0) as pwrFilled, pwr.syncState as pwrSyncState" +
                    ", IFNULL(pwa.pregnantWomanDelivered, 0) as isDelivered, IFNULL(pwa.hrpConfirmed, 0) as pwHrp" +
                    ", IFNULL(ecr.benId IS NOT NULL, 0) as ecrFilled" +
                    ", IFNULL(ect.benId IS NOT NULL, 0) as ectFilled" +
                    ", IFNULL((pwa.maternalDeath OR do.complication = 'DEATH' OR pnc.motherDeath), 0) as isMdsr" +
                    ", IFNULL(tbsn.benId IS NOT NULL, 0) as tbsnFilled, tbsn.syncState as tbsnSyncState" +
                    ", IFNULL(tbsp.benId IS NOT NULL, 0) as tbspFilled, tbsp.syncState as tbspSyncState" +
                    ", IFNULL(ir.motherBenId IS NOT NULL, 0) as irFilled, ir.syncState as irSyncState" +
                    ", IFNULL(cr.motherBenId IS NOT NULL, 0) as crFilled, cr.syncState as crSyncState" +
                    ", IFNULL(do.benId IS NOT NULL, 0) as doFilled, do.syncState as doSyncState" +
                    ", IFNULL((hrppa.benId IS NOT NULL AND hrppa.noOfDeliveries IS NOT NULL AND hrppa.timeLessThan18m IS NOT NULL AND hrppa.heightShort IS NOT NULL AND hrppa.age IS NOT NULL AND hrppa.rhNegative IS NOT NULL AND hrppa.homeDelivery IS NOT NULL AND hrppa.badObstetric IS NOT NULL AND hrppa.multiplePregnancy IS NOT NULL), 0) as hrppaFilled, hrppa.syncState as hrppaSyncState" +
                    ", IFNULL((hrpnpa.benId IS NOT NULL AND hrpnpa.noOfDeliveries IS NOT NULL AND hrpnpa.timeLessThan18m IS NOT NULL AND hrpnpa.heightShort IS NOT NULL AND hrpnpa.age IS NOT NULL AND hrpnpa.misCarriage IS NOT NULL AND hrpnpa.homeDelivery IS NOT NULL AND hrpnpa.medicalIssues IS NOT NULL AND hrpnpa.pastCSection IS NOT NULL), 0) as hrpnpaFilled, hrpnpa.syncState as hrpnpaSyncState" +
                    ", IFNULL(hrpmbp.benId IS NOT NULL, 0) as hrpmbpFilled, hrpmbp.syncState as hrpmbpSyncState" +
                    ", IFNULL(hrpt.benId IS NOT NULL, 0) as hrptFilled, IFNULL(((count(distinct hrpt.id) > 3) OR (((JulianDay('now')) - JulianDay(date(max(hrpt.visitDate)/1000,'unixepoch','localtime'))) < 1)), 0) as hrptrackingDone, hrpt.syncState as hrptSyncState" +
                    ", IFNULL(hrnpt.benId IS NOT NULL, 0) as hrnptFilled, IFNULL(((JulianDay('now') - JulianDay(date(max(hrnpt.visitDate)/1000,'unixepoch','localtime'))) < 1), 0) as hrnptrackingDone, hrnpt.syncState as hrnptSyncState " +
                    "FROM BENEFICIARY b " +
                    "JOIN HOUSEHOLD h ON b.householdId = h.householdId " +
                    "LEFT OUTER JOIN CBAC cbac ON b.beneficiaryId = cbac.benId " +
                    "LEFT OUTER JOIN CDR cdr ON b.beneficiaryId = cdr.benId " +
                    "LEFT OUTER JOIN MDSR mdsr ON b.beneficiaryId = mdsr.benId " +
                    "LEFT OUTER JOIN PMSMA pmsma ON b.beneficiaryId = pmsma.benId " +
                    "LEFT OUTER JOIN HBNC hbnc ON b.beneficiaryId = hbnc.benId " +
                    "LEFT OUTER JOIN HBYC hbyc ON b.beneficiaryId = hbyc.benId " +
                    "LEFT OUTER JOIN PREGNANCY_REGISTER pwr ON b.beneficiaryId = pwr.benId " +
                    "LEFT OUTER JOIN PREGNANCY_ANC pwa ON b.beneficiaryId = pwa.benId " +
                    "LEFT OUTER JOIN pnc_visit pnc ON b.beneficiaryId = pnc.benId " +
                    "LEFT OUTER JOIN ELIGIBLE_COUPLE_REG ecr ON b.beneficiaryId = ecr.benId " +
                    "LEFT OUTER JOIN ELIGIBLE_COUPLE_TRACKING ect ON (b.beneficiaryId = ect.benId AND CAST((strftime('%s','now') - ect.visitDate/1000)/60/60/24 AS INTEGER) < 30) " +
                    "LEFT OUTER JOIN TB_SCREENING tbsn ON b.beneficiaryId = tbsn.benId " +
                    "LEFT OUTER JOIN TB_SUSPECTED tbsp ON b.beneficiaryId = tbsp.benId " +
                    "LEFT OUTER JOIN MALARIA_SCREENING masp on b.beneficiaryId = masp.benId " +
                    "LEFT OUTER JOIN MALARIA_CONFIRMED macp on b.beneficiaryId = macp.benId " +
                    "LEFT OUTER JOIN HRP_PREGNANT_ASSESS hrppa ON b.beneficiaryId = hrppa.benId " +
                    "LEFT OUTER JOIN HRP_NON_PREGNANT_ASSESS hrpnpa ON b.beneficiaryId = hrpnpa.benId " +
                    "LEFT OUTER JOIN HRP_MICRO_BIRTH_PLAN hrpmbp ON b.beneficiaryId = hrpmbp.benId " +
                    "LEFT OUTER JOIN HRP_NON_PREGNANT_TRACK hrnpt ON b.beneficiaryId = hrnpt.benId " +
                    "LEFT OUTER JOIN HRP_PREGNANT_TRACK hrpt ON b.beneficiaryId = hrpt.benId " +
                    "LEFT OUTER JOIN DELIVERY_OUTCOME do ON b.beneficiaryId = do.benId " +
                    "LEFT OUTER JOIN INFANT_REG ir ON b.beneficiaryId = ir.motherBenId " +
                    "LEFT OUTER JOIN CHILD_REG cr ON b.beneficiaryId = cr.motherBenId " +
                    "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY MAX(IFNULL(b.updatedDate, 0), IFNULL(b.createdDate, 0), IFNULL(b.regDate, 0)) DESC, b.beneficiaryId DESC"
        )
    }
}

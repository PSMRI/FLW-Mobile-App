package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.columnExists
import org.piramalswasthya.sakhi.database.room.migrations.MigrationHelpers.tableExists

object Migration19To20 : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        db.execSQL(
            "CREATE VIEW `BEN_BASIC_CACHE` AS " +
                    "SELECT b.beneficiaryId as benId, b.motherName as motherName, b.householdId as hhId, b.regDate, b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob, b.familyHeadRelationPosition as relToHeadId" +
                    ", b.contactNumber as mobileNo, b.fatherName, h.fam_familyHeadName as familyHeadName, b.gen_spouseName as spouseName, b.rchId, b.gen_lastMenstrualPeriod as lastMenstrualPeriod" +
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
                    "LEFT OUTER JOIN HRP_PREGNANT_ASSESS hrppa ON b.beneficiaryId = hrppa.benId " +
                    "LEFT OUTER JOIN HRP_NON_PREGNANT_ASSESS hrpnpa ON b.beneficiaryId = hrpnpa.benId " +
                    "LEFT OUTER JOIN HRP_MICRO_BIRTH_PLAN hrpmbp ON b.beneficiaryId = hrpmbp.benId " +
                    "LEFT OUTER JOIN HRP_NON_PREGNANT_TRACK hrnpt ON b.beneficiaryId = hrnpt.benId " +
                    "LEFT OUTER JOIN HRP_PREGNANT_TRACK hrpt ON b.beneficiaryId = hrpt.benId " +
                    "LEFT OUTER JOIN DELIVERY_OUTCOME do ON b.beneficiaryId = do.benId " +
                    "LEFT OUTER JOIN INFANT_REG ir ON b.beneficiaryId = ir.motherBenId " +
                    "LEFT OUTER JOIN CHILD_REG cr ON b.beneficiaryId = cr.motherBenId " +
                    "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC"
        )

        if (tableExists(db, "MALARIA_SCREENING")
            && !columnExists(db, "MALARIA_SCREENING", "slideTestName")
        ) {
            db.execSQL(
                "ALTER TABLE MALARIA_SCREENING ADD COLUMN slideTestName TEXT"
            )
        }
    }
}

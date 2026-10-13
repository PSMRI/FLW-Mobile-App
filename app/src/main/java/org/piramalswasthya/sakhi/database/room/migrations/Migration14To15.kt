package org.piramalswasthya.sakhi.database.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration14To15 : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.let {
            it.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isNewAbha INTEGER NOT NULL DEFAULT 0")
            it.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
            //  it.execSQL("CREATE VIEW BEN_BASIC_CACHE AS " +
            //         "SELECT  benId,hhId,regDate,benName,benSurname,gender,dob,relToHeadId,mobileNo,fatherName,familyHeadName,spouseName,rchId,hrpStatus,syncState,reproductiveStatusId, lastMenstrualPeriod,isKid,immunizationStatus,villageId,abhaId,isNewAbha,cbacFilled,cbacSyncState,cdrFilled,cdrSyncState,mdsrFilled,mdsrSyncState,pmsmaSyncState,pmsmaFilled,hbncFilled,hbycFilled,pwrFilled,pwrSyncState,doSyncState,irSyncState,crSyncState,ecrFilled,ectFilled,tbsnFilled,tbsnSyncState,tbspFilled,tbspSyncState,hrppaFilled,hrpnpaFilled,hrpmbpFilled,hrptFilled,hrptrackingDone,hrnptrackingDone,hrnptFilled,hrppaSyncState,hrpnpaSyncState,hrpmbpSyncState,hrptSyncState,hrnptSyncState,isDelivered,pwHrp,irFilled,isMdsr,crFilled,doFilled FROM BENEFICIARY");
            it.execSQL(
                "CREATE VIEW BEN_BASIC_CACHE AS " +
                        "SELECT b.beneficiaryId as benId, b.householdId as hhId, b.regDate, " +
                        "b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob, " +
                        "b.familyHeadRelationPosition as relToHeadId, b.contactNumber as mobileNo, " +
                        "b.fatherName, h.fam_familyHeadName as familyHeadName, b.gen_spouseName as spouseName, " +
                        "b.rchId, b.gen_lastMenstrualPeriod as lastMenstrualPeriod, b.isHrpStatus as hrpStatus, " +
                        "b.syncState, b.gen_reproductiveStatusId as reproductiveStatusId, b.isKid, b.immunizationStatus, " +
                        "b.loc_village_id as villageId, b.abha_healthIdNumber as abhaId, " +
                        "b.isNewAbha, " + // Added the new column here
                        "cbac.benId is not null as cbacFilled, cbac.syncState as cbacSyncState " +
                        "FROM BENEFICIARY b " +
                        "JOIN HOUSEHOLD h ON b.householdId = h.householdId " +
                        "LEFT OUTER JOIN CBAC cbac on b.beneficiaryId = cbac.benId " +
                        "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC"
            )

        }
    }
}

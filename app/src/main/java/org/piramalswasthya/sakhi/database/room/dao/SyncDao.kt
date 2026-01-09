package org.piramalswasthya.sakhi.database.room.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.model.SyncStatusCache

@Dao
interface SyncDao {

    @Query(
        "SELECT id, name, syncState, COUNT(*) as count " +
                "FROM ( " +
                "    SELECT 1 as id, 'Beneficiary' as name, b1.syncState as syncState " +
                "    FROM beneficiary b1 " +
//                "    WHERE b1.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 2 as id, 'EC Registration' as name, ecr.syncState as syncState " +
                "    FROM eligible_couple_reg ecr " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = ecr.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 3 as id, 'EC Tracking' as name, ect.syncState as syncState " +
                "    FROM eligible_couple_tracking ect " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = ect.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 4 as id, 'PW Registration' as name, pwr.syncState as syncState " +
                "    FROM pregnancy_register pwr " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = pwr.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 5 as id, 'PW ANC' as name, pwanc.syncState as syncState " +
                "    FROM pregnancy_anc pwanc " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = pwanc.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 6 as id, 'PMSMA' as name, pmsma.syncState as syncState " +
                "    FROM pmsma pmsma " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = pmsma.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 7 as id, 'Delivery Outcome' as name, do.syncState as syncState " +
                "    FROM delivery_outcome do " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = do.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 8 as id, 'PNC' as name, pnc.syncState as syncState " +
                "    FROM pnc_visit pnc " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = pnc.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 9 as id, 'Infant Reg' as name, ir.syncState as syncState " +
                "    FROM infant_reg ir " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = ir.motherBenId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 10 as id, 'CBAC' as name, c1.syncState as syncState " +
                "    FROM cbac c1 " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = c1.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 11 as id, 'TB Screening' as name, tbsn.syncState as syncState " +
                "    FROM TB_SCREENING tbsn " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = tbsn.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 12 as id, 'TB Suspected' as name, tbsp.syncState as syncState " +
                "    FROM TB_SUSPECTED tbsp " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = tbsp.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 13 as id, 'HRP Assess' as name, hrpa.syncState as syncState " +
                "    FROM HRP_PREGNANT_ASSESS hrpa " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = hrpa.benId " +

                "    UNION ALL " +
                "    SELECT 20 as id, 'Micro Birth Plan' as name, hrpa.syncState as syncState " +
                "    FROM HRP_MICRO_BIRTH_PLAN hrpa " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = hrpa.benId " +


//                "    WHERE b.loc_village_id = :villageId " +
                 "    UNION ALL " +
                "    SELECT 21 as id, 'VHND' as name, vhnd.syncState as syncState " +
                "    FROM VHND vhnd " +

                "    UNION ALL " +
                "    SELECT 22 as id, 'VHSNC' as name, vhnc.syncState as syncState " +
                "    FROM VHNC vhnc " +

                "    UNION ALL " +
                "    SELECT 23 as id, 'PHC' as name, phc.syncState as syncState " +
                "    FROM PHCReviewMeeting phc " +

                "    UNION ALL " +
                "    SELECT 24 as id, 'AHD' as name, ahd.syncState as syncState " +
                "    FROM AHDMeeting ahd " +

                "    UNION ALL " +
                "    SELECT 25 as id, 'Deworming' as name, deworming.syncState as syncState " +
                "    FROM DewormingMeeting deworming " +

//                "    INNER JOIN beneficiary b ON b.beneficiaryId = hrpa.benId " +
//                "    WHERE b.loc_village_id = :villageId " +



                "    UNION ALL " +
                "    SELECT 14 as id, 'HRP Track' as name, hrpt.syncState as syncState " +
                "    FROM HRP_PREGNANT_TRACK hrpt " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = hrpt.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 15 as id, 'HR NonPreg Assess' as name, hrnpa.syncState as syncState " +
                "    FROM HRP_NON_PREGNANT_ASSESS hrnpa " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = hrnpa.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 16 as id, 'HR NonPreg Track' as name, hrnpt.syncState as syncState " +
                "    FROM HRP_NON_PREGNANT_TRACK hrnpt " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = hrnpt.benId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 17 as id, 'Immunization' as name, imm.syncState as syncState " +
                "    FROM IMMUNIZATION imm " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = imm.beneficiaryId " +
//                "    WHERE b.loc_village_id = :villageId " +
                "    UNION ALL " +
                "    SELECT 18 as id, 'HBYC' as name,  CASE \n" +
                "   WHEN hbyc.isSynced = 1 THEN 2 ELSE hbyc.isSynced  END AS syncState " +
                "    FROM all_visit_history_hbyc hbyc " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = hbyc.benId " +
                "    UNION ALL " +
                "    SELECT 19 as id, 'HBNC' as name, CASE \n" +
                "   WHEN hbnc.isSynced = 1 THEN 2 ELSE hbnc.isSynced  END AS syncState " +
                "    FROM all_visit_history hbnc " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = hbnc.benId " +
                " UNION ALL"+
                " SELECT 26 as id, 'NCD Follow Up' as name, CASE \n" +
                "   WHEN ncd.isSynced = 1 THEN 2 ELSE ncd.isSynced  END AS syncState " +
                "    FROM ncd_referal_all_visit ncd " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = ncd.benId " +
                ") AS combined_data " +
                "GROUP BY id, name, syncState " +
                "ORDER BY id; "
    )
    fun getSyncStatus(): Flow<List<SyncStatusCache>>


//    fun getUnsyncedCount(): Flow<Int>
}
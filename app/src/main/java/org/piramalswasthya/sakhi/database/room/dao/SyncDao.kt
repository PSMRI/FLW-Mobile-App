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
                "    FROM beneficiary b1 where isDeactivate = 0 " +
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
                "    UNION ALL " +
                "    SELECT 27 as id, 'Malaria Screening' as name, masn.syncState as syncState " +
                "    FROM MALARIA_SCREENING masn " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = masn.benId " +
                "    UNION ALL " +
                "    SELECT 28 as id, 'Malaria Confirmed' as name, macf.syncState as syncState " +
                "    FROM MALARIA_CONFIRMED macf " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = macf.benId " +



                "    UNION ALL " +
                "    SELECT 29 as id, 'CDR' as name, cdr.syncState as syncState " +
                "    FROM CDR cdr " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = cdr.benId " +
                "    UNION ALL " +
                "    SELECT 30 as id, 'MDSR' as name, mdsr.syncState as syncState " +
                "    FROM MDSR mdsr " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = mdsr.benId " +
                "    UNION ALL " +
                "    SELECT 31 as id, 'Child Reg' as name, chr.syncState as syncState " +
                "    FROM CHILD_REG chr " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = chr.motherBenId " +
                "    UNION ALL " +
                "    SELECT 32 as id, 'AES Screening' as name, aes.syncState as syncState " +
                "    FROM AES_SCREENING aes " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = aes.benId " +
                "    UNION ALL " +
                "    SELECT 33 as id, 'Kala Azar Screening' as name, kzs.syncState as syncState " +
                "    FROM KALAZAR_SCREENING kzs " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = kzs.benId " +
                "    UNION ALL " +
                "    SELECT 34 as id, 'Filaria Screening' as name, fls.syncState as syncState " +
                "    FROM FILARIA_SCREENING fls " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = fls.benId " +
                "    UNION ALL " +
                "    SELECT 35 as id, 'Leprosy Screening' as name, lps.syncState as syncState " +
                "    FROM LEPROSY_SCREENING lps " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = lps.benId " +
                "    UNION ALL " +
                "    SELECT 36 as id, 'Leprosy Follow Up' as name, lpf.syncState as syncState " +
                "    FROM LEPROSY_FOLLOW_UP lpf " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = lpf.benId " +
                "    UNION ALL " +
                "    SELECT 37 as id, 'Adolescent Health' as name, adh.syncState as syncState " +
                "    FROM Adolescent_Health_Form_Data adh " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = adh.benId " +
                "    UNION ALL " +
                "    SELECT 38 as id, 'NCD Refer' as name, ncdr.syncState as syncState " +
                "    FROM NCD_REFER ncdr " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = ncdr.benId " +
                "    UNION ALL " +
                "    SELECT 39 as id, 'TB Confirmed Treatment' as name, tbct.syncState as syncState " +
                "    FROM TB_CONFIRMED_TREATMENT tbct " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = tbct.benId " +
                "    UNION ALL " +
                "    SELECT 40 as id, 'Pulse Polio Campaign' as name, ppc.syncState as syncState " +
                "    FROM PulsePolioCampaign ppc " +
                "    UNION ALL " +
                "    SELECT 41 as id, 'ORS Campaign' as name, orsc.syncState as syncState " +
                "    FROM ORSCampaign orsc " +
                "    UNION ALL " +
                "    SELECT 42 as id, 'Saas Bahu Sammelan' as name, sbs.syncState as syncState " +
                "    FROM SAAS_BAHU_ACTIVITY sbs " +
                "    UNION ALL " +
                "    SELECT 43 as id, 'MAA Meeting' as name, maam.syncState as syncState " +
                "    FROM MAA_MEETING maam " +
                "    UNION ALL " +
                "    SELECT 44 as id, 'UWIN Session' as name, uwin.syncState as syncState " +
                "    FROM UWIN_SESSION uwin " +
                "    UNION ALL " +
                "    SELECT 45 as id, 'Children Under Five' as name, CASE \n" +
                "   WHEN cufy.isSynced = 1 THEN 2 ELSE cufy.isSynced  END AS syncState " +
                "    FROM children_under_five_all_visit cufy " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = cufy.benId " +
                "    UNION ALL " +
                "    SELECT 46 as id, 'ANC Visit History' as name, CASE \n" +
                "   WHEN ancv.isSynced = 1 THEN 2 ELSE ancv.isSynced  END AS syncState " +
                "    FROM ALL_VISIT_HISTORY_ANC ancv " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = ancv.benId " +
                "    UNION ALL " +
                "    SELECT 47 as id, 'Eye Surgery' as name, CASE \n" +
                "   WHEN eys.isSynced = 1 THEN 2 ELSE eys.isSynced  END AS syncState " +
                "    FROM ALL_EYE_SURGERY_VISIT_HISTORY eys " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = eys.benId " +
                "    UNION ALL " +
                "    SELECT 48 as id, 'Ben IFA' as name, CASE \n" +
                "   WHEN bif.isSynced = 1 THEN 2 ELSE bif.isSynced  END AS syncState " +
                "    FROM ALL_BEN_IFA_VISIT_HISTORY bif " +
                "    INNER JOIN beneficiary b ON b.beneficiaryId = bif.benId " +
                "    UNION ALL " +
                "    SELECT 49 as id, 'Mosquito Net' as name, CASE \n" +
                "   WHEN mnv.isSynced = 1 THEN 2 ELSE mnv.isSynced  END AS syncState " +
                "    FROM mosquito_net_visit mnv " +
                "    UNION ALL " +
                "    SELECT 50 as id, 'Filaria MDA' as name, CASE \n" +
                "   WHEN fmv.isSynced = 1 THEN 2 ELSE fmv.isSynced  END AS syncState " +
                "    FROM FILARIA_MDA_VISIT_HISTORY fmv " +
                "    UNION ALL " +
                "    SELECT 51 as id, 'Filaria MDA Campaign' as name, CASE \n" +
                "   WHEN fmc.isSynced = 1 THEN 2 ELSE fmc.isSynced  END AS syncState " +
                "    FROM FILARIA_MDA_CAMPAIGN_HISTORY fmc " +
                ") AS combined_data " +
                "GROUP BY id, name, syncState " +
                "ORDER BY id; "
    )
    fun getSyncStatus(): Flow<List<SyncStatusCache>>


//    fun getUnsyncedCount(): Flow<Int>
}
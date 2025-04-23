package org.piramalswasthya.sakhi.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import org.piramalswasthya.sakhi.database.converters.LocationEntityListConverter
import org.piramalswasthya.sakhi.database.converters.SyncStateConverter
import org.piramalswasthya.sakhi.database.room.dao.AesDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.room.dao.EcrDao
import org.piramalswasthya.sakhi.database.room.dao.FilariaDao
import org.piramalswasthya.sakhi.database.room.dao.FpotDao
import org.piramalswasthya.sakhi.database.room.dao.HbncDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.HrpDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.database.room.dao.KalaAzarDao
import org.piramalswasthya.sakhi.database.room.dao.LeprosyDao
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.room.dao.PmjayDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.ChildRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.FPOTCache
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.IncentiveActivityCache
import org.piramalswasthya.sakhi.model.IncentiveRecordCache
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.model.MDSRCache
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.PMJAYCache
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.model.Vaccine

@Database(
    entities = [
        HouseholdCache::class,
        BenRegCache::class,
        BeneficiaryIdsAvail::class,
        CbacCache::class,
        CDRCache::class,
        MDSRCache::class,
        PNCVisitCache::class,
        PMSMACache::class,
        PMJAYCache::class,
        FPOTCache::class,
        HBNCCache::class,
        HBYCCache::class,
        EligibleCoupleRegCache::class,
        Vaccine::class,
        ImmunizationCache::class,
        PregnantWomanRegistrationCache::class,
        EligibleCoupleTrackingCache::class,
        TBScreeningCache::class,
        TBSuspectedCache::class,
        PregnantWomanAncCache::class,
        DeliveryOutcomeCache::class,
        InfantRegCache::class,
        ChildRegCache::class,
        HRPPregnantAssessCache::class,
        HRPNonPregnantAssessCache::class,
        HRPPregnantTrackCache::class,
        HRPNonPregnantTrackCache::class,
        HRPMicroBirthPlanCache::class,
        //INCENTIVES
        IncentiveActivityCache::class,
        IncentiveRecordCache::class,
        MalariaScreeningCache::class,
        AESScreeningCache::class,
        KalaAzarScreeningCache::class,
        FilariaScreeningCache::class,
        LeprosyScreeningCache::class,
        MalariaConfirmedCasesCache::class,
        IRSRoundScreening::class,

    ],
    views = [BenBasicCache::class],
    version = 15, exportSchema = false
)

@TypeConverters(LocationEntityListConverter::class, SyncStateConverter::class)

abstract class InAppDb : RoomDatabase() {

    abstract val benIdGenDao: BeneficiaryIdsAvailDao
    abstract val householdDao: HouseholdDao
    abstract val benDao: BenDao
    abstract val cbacDao: CbacDao
    abstract val cdrDao: CdrDao
    abstract val mdsrDao: MdsrDao
    abstract val pmsmaDao: PmsmaDao
    abstract val pmjayDao: PmjayDao
    abstract val fpotDao: FpotDao
    abstract val hbncDao: HbncDao
    abstract val hbycDao: HbycDao
    abstract val ecrDao: EcrDao
    abstract val vaccineDao: ImmunizationDao
    abstract val maternalHealthDao: MaternalHealthDao
    abstract val pncDao: PncDao
    abstract val tbDao: TBDao
    abstract val hrpDao: HrpDao
    abstract val deliveryOutcomeDao: DeliveryOutcomeDao
    abstract val infantRegDao: InfantRegDao
    abstract val childRegistrationDao: ChildRegistrationDao
    abstract val incentiveDao: IncentiveDao
    abstract val malariaDao: MalariaDao
    abstract val aesDao: AesDao
    abstract val kalaAzarDao: KalaAzarDao
    abstract val leprosyDao: LeprosyDao
    abstract val filariaDao: FilariaDao
    abstract val syncDao: SyncDao

    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null

        fun getInstance(appContext: Context): InAppDb {

            val MIGRATION_1_2 = Migration(1, 2, migrate = {
//                it.execSQL("select count(*) from beneficiary")
            })

            val MIGRATION_14_15 = Migration(14, 15, migrate = {
                it.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isNewAbha INTEGER NOT NULL DEFAULT 0")
                it.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE");
              //  it.execSQL("CREATE VIEW BEN_BASIC_CACHE AS " +
               //         "SELECT  benId,hhId,regDate,benName,benSurname,gender,dob,relToHeadId,mobileNo,fatherName,familyHeadName,spouseName,rchId,hrpStatus,syncState,reproductiveStatusId, lastMenstrualPeriod,isKid,immunizationStatus,villageId,abhaId,isNewAbha,cbacFilled,cbacSyncState,cdrFilled,cdrSyncState,mdsrFilled,mdsrSyncState,pmsmaSyncState,pmsmaFilled,hbncFilled,hbycFilled,pwrFilled,pwrSyncState,doSyncState,irSyncState,crSyncState,ecrFilled,ectFilled,tbsnFilled,tbsnSyncState,tbspFilled,tbspSyncState,hrppaFilled,hrpnpaFilled,hrpmbpFilled,hrptFilled,hrptrackingDone,hrnptrackingDone,hrnptFilled,hrppaSyncState,hrpnpaSyncState,hrpmbpSyncState,hrptSyncState,hrnptSyncState,isDelivered,pwHrp,irFilled,isMdsr,crFilled,doFilled FROM BENEFICIARY");
                it.execSQL("CREATE VIEW BEN_BASIC_CACHE AS " +
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
                        "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC")

            })

            val MIGRATION_13_14 = Migration(13, 14, migrate = {
                it.execSQL("alter table INCENTIVE_ACTIVITY add column fmrCode TEXT")
                it.execSQL("alter table INCENTIVE_ACTIVITY add column fmrCodeOld TEXT")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column systolic INTEGER")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column diastolic INTEGER")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column bloodGlucoseTest TEXT")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column fbg INTEGER")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column rbg INTEGER")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column ppbg INTEGER")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column hemoglobinTest TEXT")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column ifaGiven TEXT")
                it.execSQL("alter table HRP_NON_PREGNANT_TRACK add column ifaQuantity INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column systolic INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column diastolic INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column bloodGlucoseTest TEXT")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column fbg INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column rbg INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column ppbg INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column hemoglobinTest TEXT")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column ifaGiven TEXT")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column ifaQuantity INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column fastingOgtt INTEGER")
                it.execSQL("alter table HRP_PREGNANT_TRACK add column after2hrsOgtt INTEGER")
            })
//        _db.execSQL("CREATE TABLE IF NOT EXISTS `HRP_PREGNANT_TRACK` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `benId` INTEGER NOT NULL, `visitDate` INTEGER, `rdPmsa` TEXT, `rdDengue` TEXT, `rdFilaria` TEXT, `severeAnemia` TEXT, `hemoglobinTest` TEXT, `ifaGiven` TEXT, `ifaQuantity` INTEGER, `pregInducedHypertension` TEXT, `systolic` INTEGER, `diastolic` INTEGER, `gestDiabetesMellitus` TEXT, `bloodGlucoseTest` TEXT, `fbg` INTEGER, `rbg` INTEGER, `ppbg` INTEGER, `fastingOgtt` INTEGER, `after2hrsOgtt` INTEGER, `hypothyrodism` TEXT, `polyhydromnios` TEXT, `oligohydromnios` TEXT, `antepartumHem` TEXT, `malPresentation` TEXT, `hivsyph` TEXT, `visit` TEXT, `syncState` INTEGER NOT NULL, FOREIGN KEY(`benId`) REFERENCES `BENEFICIARY`(`beneficiaryId`) ON UPDATE CASCADE ON DELETE CASCADE )");
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        appContext,
                        InAppDb::class.java,
                        "Sakhi-2.0-In-app-database"
                    )
                        .addMigrations(
                            MIGRATION_14_15
                        )
                        .build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}
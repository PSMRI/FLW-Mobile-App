package org.piramalswasthya.sakhi.database.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.piramalswasthya.sakhi.database.converters.LocationEntityListConverter
import org.piramalswasthya.sakhi.database.converters.SyncStateConverter
import org.piramalswasthya.sakhi.database.room.dao.AdolescentHealthDao
import org.piramalswasthya.sakhi.database.room.dao.AesDao
import org.piramalswasthya.sakhi.database.converters.StringListConverter
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.room.dao.EcrDao
import org.piramalswasthya.sakhi.database.room.dao.FilariaDao
import org.piramalswasthya.sakhi.database.room.dao.FpotDao
import org.piramalswasthya.sakhi.database.room.dao.GeneralOpdDao
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
import org.piramalswasthya.sakhi.database.room.dao.MaaMeetingDao
import org.piramalswasthya.sakhi.database.room.dao.MosquitoNetFormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.room.dao.UwinDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDaoHBYC
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.InfantDao
import org.piramalswasthya.sakhi.database.room.dao.VLFDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.BenIfaFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.EyeSurgeryFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMDAFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.NCDReferalFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.ChildRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.FPOTCache
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.GeneralOPEDBeneficiary
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
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.model.MDSRCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.PMJAYCache
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.InfantEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity

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
        VHNDCache::class,
        VHNCCache::class,
        PHCReviewMeetingCache::class,
        AHDCache::class,
        DewormingCache::class,
        MalariaScreeningCache::class,
        AESScreeningCache::class,
        KalaAzarScreeningCache::class,
        FilariaScreeningCache::class,
        LeprosyScreeningCache::class,
        LeprosyFollowUpCache::class,
        MalariaConfirmedCasesCache::class,
        IRSRoundScreening::class,
        ProfileActivityCache::class,
        AdolescentHealthCache::class,
        ABHAModel::class,
        //Dynamic Data
        InfantEntity::class,
        FormSchemaEntity::class,
        SaasBahuSammelanCache::class,
        MaaMeetingEntity::class,
        FormResponseJsonEntity::class,
        FormResponseJsonEntityHBYC::class,
        CUFYFormResponseJsonEntity::class,
        NCDReferalFormResponseJsonEntity::class,
        GeneralOPEDBeneficiary::class,
        ReferalCache::class,
        UwinCache::class,
        EyeSurgeryFormResponseJsonEntity::class,
        BenIfaFormResponseJsonEntity::class,
        MosquitoNetFormResponseJsonEntity::class,
        FilariaMDAFormResponseJsonEntity::class,
        ANCFormResponseJsonEntity::class,
        TBConfirmedTreatmentCache::class
    ],
    views = [BenBasicCache::class],
    version = 54, exportSchema = false
)

@TypeConverters(
    LocationEntityListConverter::class,
    SyncStateConverter::class,
    StringListConverter::class
)

abstract class InAppDb : RoomDatabase() {

    abstract val benIdGenDao: BeneficiaryIdsAvailDao
    abstract val householdDao: HouseholdDao
    abstract val benDao: BenDao
    abstract val adolescentHealthDao: AdolescentHealthDao
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
    abstract val vlfDao: VLFDao
    abstract val malariaDao: MalariaDao
    abstract val aesDao: AesDao
    abstract val kalaAzarDao: KalaAzarDao
    abstract val leprosyDao: LeprosyDao
    abstract val filariaDao: FilariaDao
    abstract val profileDao: ProfileDao
    abstract val abhaGenratedDao: ABHAGenratedDao
    abstract val saasBahuSammelanDao: SaasBahuSammelanDao
    abstract val generalOpdDao: GeneralOpdDao
    abstract val maaMeetingDao: MaaMeetingDao
    abstract val uwinDao: UwinDao

    abstract val referalDao: NcdReferalDao

    abstract fun infantDao(): InfantDao
    abstract fun formSchemaDao(): FormSchemaDao
    abstract fun formResponseDao(): FormResponseDao
    abstract fun CUFYFormResponseDao(): CUFYFormResponseDao
    abstract fun CUFYFormResponseJsonDao(): CUFYFormResponseJsonDao
    abstract fun NCDReferalFormResponseJsonDao(): NCDReferalFormResponseJsonDao
    abstract fun formResponseJsonDao(): FormResponseJsonDao
    abstract fun formResponseJsonDaoHBYC(): FormResponseJsonDaoHBYC

    abstract fun formResponseJsonDaoANC() : FormResponseANCJsonDao
    abstract fun formResponseJsonDaoEyeSurgery(): EyeSurgeryFormResponseJsonDao
    abstract fun formResponseJsonDaoBenIfa(): BenIfaFormResponseJsonDao
    abstract fun formResponseMosquitoNetJsonDao(): MosquitoNetFormResponseDao
    abstract fun formResponseFilariaMDAJsonDao(): FilariaMDAFormResponseJsonDao

    abstract val syncDao: SyncDao

    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null

        fun getInstance(appContext: Context): InAppDb {

            val MIGRATION_1_2 = Migration(18, 19, migrate = {
                it.execSQL("alter table BEN_BASIC_CACHE add column isConsent BOOL")
                it.execSQL("alter table BENEFICIARY add column isConsent BOOL")

            })

            val MIGRATION_53_54 = object : Migration(53, 54) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN visitLabel TEXT")
                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN typeOfTBCase TEXT")
                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN reasonForSuspicion TEXT")

                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN hasSymptoms INTEGER NOT NULL DEFAULT 0")

                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN isChestXRayDone INTEGER")
                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN chestXRayResult TEXT")
                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN referralFacility TEXT")

                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN isTBConfirmed INTEGER")
                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN isDRTBConfirmed INTEGER")

                    database.execSQL("ALTER TABLE TBSuspectedCache ADD COLUMN isConfirmed INTEGER NOT NULL DEFAULT 0")
                }
            }

            val MIGRATION_52_53 = object : Migration(52, 53) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
            CREATE UNIQUE INDEX IF NOT EXISTS index_DewormingMeeting_dewormingDate
            ON DewormingMeeting(dewormingDate)
            """.trimIndent()
                    )
                }
            }



            val MIGRATION_51_52 = object : Migration(51, 52) {
                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS MAA_MEETING (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                meetingDate TEXT,
                place TEXT,
                villageName TEXT,
                mitaninActivityCheckList TEXT,
                noOfPragnentWomen TEXT,
                noOfLactingMother TEXT,
                participants INTEGER,
                ashaId INTEGER,
                meetingImages TEXT,
                createdAt INTEGER NOT NULL DEFAULT 0,
                updatedAt INTEGER NOT NULL DEFAULT 0,
                syncState TEXT NOT NULL DEFAULT 'UNSYNCED'
            )
            """.trimIndent()
                    )

                    database.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS index_MAA_MEETING_id ON MAA_MEETING(id)"
                    )
                }
            }


            val MIGRATION_50_51 = Migration(50, 51, migrate = {
                it.execSQL("alter table PHCReviewMeeting add column villageName TEXT")
                it.execSQL("alter table PHCReviewMeeting add column mitaninHistory TEXT")
                it.execSQL("alter table PHCReviewMeeting add column mitaninActivityCheckList TEXT")
                it.execSQL("alter table PHCReviewMeeting add column placeId INTEGER DEFAULT 0")
                it.execSQL(
                    """
            CREATE UNIQUE INDEX IF NOT EXISTS index_PHCReviewMeeting_id
            ON PHCReviewMeeting(id)
            """.trimIndent()
                )

            })


            val MIGRATION_49_50 = object : Migration(49, 50) {
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


            val MIGRATION_48_49 = object : Migration(48, 49) {
                override fun migrate(db: SupportSQLiteDatabase) {

                    val columns = listOf(
                        "villageName TEXT",
                        "anm INTEGER DEFAULT 0",
                        "aww INTEGER DEFAULT 0",
                        "noOfPregnantWomen INTEGER DEFAULT 0",
                        "noOfLactatingMother INTEGER DEFAULT 0",
                        "noOfCommittee INTEGER DEFAULT 0",
                        "followupPrevious INTEGER"
                    )

                    columns.forEach { columnDef ->
                        db.execSQL("ALTER TABLE VHNC ADD COLUMN $columnDef")
                    }
                }
            }



            val MIGRATION_47_48 = Migration(47, 48) {
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentUlcerationId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentTinglingId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN hypopigmentedPatchId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN thickenedSkinId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinNodulesId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinPatchDiscolorationId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentNumbnessId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN clawingFingersId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN tinglingNumbnessExtremitiesId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN inabilityCloseEyelidId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN difficultyHoldingObjectsId INTEGER DEFAULT 1")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN weaknessFeetId INTEGER DEFAULT 1")

                // ===== Symptom String fields =====
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentUlceration TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentTingling TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN hypopigmentedPatch TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN thickenedSkin TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinNodules TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN skinPatchDiscoloration TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentNumbness TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN clawingFingers TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN tinglingNumbnessExtremities TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN inabilityCloseEyelid TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN difficultyHoldingObjects TEXT")
                it.execSQL("ALTER TABLE LEPROSY_SCREENING ADD COLUMN weaknessFeet TEXT")

            }
            val MIGRATION_46_47 = Migration(46, 47) {
                it.execSQL(
                    """ALTER TABLE NCD_REFER 
                    ADD COLUMN type TEXT
                    """.trimIndent()
                )

            }

            val MIGRATION_45_46 = Migration(45, 46) {
                it.execSQL("ALTER TABLE PREGNANCY_ANC  ADD COLUMN placeOfAnc TEXT")
                it.execSQL("ALTER TABLE PREGNANCY_ANC  ADD COLUMN placeOfAncId INTEGER")
            }

            val MIGRATION_44_45 = object : Migration(44, 45) { // replace oldVersion and newVersion
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isSpouseAdded INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isChildrenAdded INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isMarried INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN noOfChildren INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN noOfAliveChildren INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN doYouHavechildren INTEGER NOT NULL DEFAULT 0")

                    database.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
                    database.execSQL(
                        "CREATE VIEW `BEN_BASIC_CACHE` AS " +
                                "SELECT b.beneficiaryId as benId,b.isMarried, b.noOfAliveChildren, b.noOfChildren, b.doYouHavechildren, b.isConsent as isConsent, b.motherName as motherName, b.householdId as hhId, b.regDate, b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob, b.isDeath,b.isDeathValue,b.dateOfDeath,b.timeOfDeath,b.reasonOfDeath,b.reasonOfDeathId,b.placeOfDeath,b.placeOfDeathId,b.otherPlaceOfDeath,b.isSpouseAdded,b.isChildrenAdded, b.familyHeadRelationPosition as relToHeadId" +
                                ", b.contactNumber as mobileNo, b.fatherName, h.fam_familyHeadName as familyHeadName, b.gen_spouseName as spouseName, b.rchId, b.gen_lastMenstrualPeriod as lastMenstrualPeriod" +
                                ", b.isHrpStatus as hrpStatus, b.syncState, b.gen_reproductiveStatusId as reproductiveStatusId, b.isKid, b.immunizationStatus" +
                                ", b.loc_village_id as villageId, b.abha_healthIdNumber as abhaId" +
                                ", b.isNewAbha" + // FIX: Using only one, correct source for isNewAbha.
                                ", IFNULL(cbac.benId IS NOT NULL, 0) as cbacFilled, cbac.syncState as cbacSyncState" +
                                ", IFNULL(cdr.benId IS NOT NULL, 0) as cdrFilled, cdr.syncState as cdrSyncState" +
                                ", IFNULL(mdsr.benId IS NOT NULL, 0) as mdsrFilled, mdsr.syncState as mdsrSyncState" +
                                ", IFNULL(pmsma.benId IS NOT NULL, 0) as pmsmaFilled, pmsma.syncState as pmsmaSyncState" +
                                ", IFNULL(hbnc.benId IS NOT NULL, 0) as hbncFilled" +
                                ", IFNULL(hbyc.benId IS NOT NULL, 0) as hbycFilled" +
                                ", IFNULL(pwr.benId IS NOT NULL, 0) as pwrFilled, pwr.syncState as pwrSyncState" +
                                ", IFNULL(pwa.pregnantWomanDelivered, 0) as isDelivered, IFNULL(pwa.hrpConfirmed, 0) as pwHrp" +
                                ", IFNULL(ecr.benId IS NOT NULL, 0) as ecrFilled" +
                                ", IFNULL(ect.benId IS NOT NULL, 0) as ectFilled" + // FIX: Removed duplicate ectFilled and used a safe version.
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
                                "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC"
                    )

                    database.execSQL("""
            UPDATE BENEFICIARY 
            SET isMarried = CASE 
                WHEN gen_maritalStatusId = 2 THEN 1 
                ELSE 0 
            END
        """.trimIndent())
                }


            }

            val MIGRATION_43_44 = object : Migration(43, 44) {
                override fun migrate(database: SupportSQLiteDatabase) {

                    fun addColumnIfNotExists(
                        db: SupportSQLiteDatabase,
                        table: String,
                        column: String,
                        type: String
                    ) {
                        val cursor = db.query("PRAGMA table_info($table)")
                        var exists = false

                        while (cursor.moveToNext()) {
                            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                            if (name.equals(column, ignoreCase = true)) {
                                exists = true
                                break
                            }
                        }
                        cursor.close()

                        if (!exists) {
                            db.execSQL("ALTER TABLE $table ADD COLUMN $column $type")
                        }
                    }


                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS LEPROSY_FOLLOW_UP (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                visitNumber INTEGER NOT NULL,
                followUpDate INTEGER NOT NULL,
                treatmentStatus TEXT,
                mdtBlisterPackReceived TEXT,
                treatmentCompleteDate INTEGER NOT NULL DEFAULT 0,
                remarks TEXT,
                homeVisitDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                leprosySymptoms TEXT,
                typeOfLeprosy TEXT,
                leprosySymptomsPosition INTEGER DEFAULT 1,
                visitLabel TEXT DEFAULT 'Visit -1',
                leprosyStatus TEXT DEFAULT '',
                referredTo INTEGER DEFAULT 0,
                referToName TEXT,
                treatmentEndDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                mdtBlisterPackRecived TEXT,
                treatmentStartDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                syncState INTEGER NOT NULL DEFAULT 0,
                createdBy TEXT DEFAULT '',
                createdDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                modifiedBy TEXT DEFAULT '',
                lastModDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
            )
        """)

                    database.execSQL("CREATE INDEX IF NOT EXISTS ind_leprosy_followup_ben ON LEPROSY_FOLLOW_UP (benId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS ind_leprosy_followup_visit ON LEPROSY_FOLLOW_UP (benId, visitNumber)")


                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "currentVisitNumber", "INTEGER NOT NULL DEFAULT 1")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "totalFollowUpMonthsRequired", "INTEGER NOT NULL DEFAULT 0")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "createdBy", "TEXT DEFAULT ''")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "createdDate", "INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "modifiedBy", "TEXT DEFAULT ''")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "lastModDate", "INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")



                    database.execSQL("""
            INSERT INTO LEPROSY_FOLLOW_UP (
                benId, visitNumber, followUpDate, treatmentStatus, mdtBlisterPackReceived,
                treatmentCompleteDate, remarks, syncState,
                homeVisitDate, leprosySymptoms, typeOfLeprosy, leprosySymptomsPosition,
                visitLabel, leprosyStatus, referredTo, referToName, treatmentEndDate,
                mdtBlisterPackRecived, treatmentStartDate, createdBy, createdDate, modifiedBy, lastModDate
            )
            SELECT 
                benId,
                1,
                homeVisitDate AS followUpDate,
                treatmentStatus,
                mdtBlisterPackRecived,
                treatmentEndDate,
                '',
                0,
                homeVisitDate,
                leprosySymptoms,
                typeOfLeprosy,
                leprosySymptomsPosition,
                visitLabel,
                leprosyStatus,
                referredTo,
                referToName,
                treatmentEndDate,
                mdtBlisterPackRecived,
                treatmentStartDate,
                '',
                ${System.currentTimeMillis()},
                '',
                ${System.currentTimeMillis()}
            FROM LEPROSY_SCREENING
            WHERE treatmentStatus IS NOT NULL
               OR mdtBlisterPackRecived IS NOT NULL
               OR leprosySymptoms IS NOT NULL
        """)
                }
            }




            val MIGRATION_42_43 = object : Migration(42, 43) {
                override fun migrate(database: SupportSQLiteDatabase) {

                    fun addColumnIfNotExists(
                        db: SupportSQLiteDatabase,
                        table: String,
                        column: String,
                        type: String
                    ) {
                        val cursor = db.query("PRAGMA table_info($table)")
                        var exists = false

                        while (cursor.moveToNext()) {
                            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                            if (name.equals(column, ignoreCase = true)) {
                                exists = true
                                break
                            }
                        }
                        cursor.close()

                        if (!exists) {
                            db.execSQL("ALTER TABLE $table ADD COLUMN $column $type")
                        }
                    }

                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "leprosySymptoms", "TEXT")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "visitLabel", "TEXT")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "visitNumber", "INTEGER")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "leprosySymptomsPosition", "INTEGER DEFAULT 1")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "isConfirmed", "INTEGER NOT NULL DEFAULT 0")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "treatmentStartDate", "INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "treatmentEndDate", "INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "mdtBlisterPackRecived", "TEXT")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "treatmentStatus", "TEXT")
                    addColumnIfNotExists(database, "LEPROSY_SCREENING", "leprosyState", "TEXT")
                }
            }

            val MIGRATION_40_41 = object : Migration(40, 41) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN isYesOrNo INTEGER DEFAULT 0")
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN dateSterilisation INTEGER")
                    database.execSQL("UPDATE PREGNANCY_ANC SET isPaiucdId = CASE WHEN isPaiucdId = 1 THEN 1 ELSE 0 END")
                }
            }
            val MIGRATION_41_42 = object : Migration(41, 42) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE form_schema ADD COLUMN language TEXT NOT NULL DEFAULT 'en'")
                }
            }

            val MIGRATION_39_40 = object : Migration(39, 40) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS `ALL_BEN_IFA_VISIT_HISTORY` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `benId` INTEGER NOT NULL,
                `hhId` INTEGER NOT NULL,
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
                    database.execSQL(
                        """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_ALL_BEN_IFA_VISIT_HISTORY_benId_hhId_visitDate_formId`
            ON `ALL_BEN_IFA_VISIT_HISTORY` (`benId`, `hhId`, `visitDate`, `formId`)
            """.trimIndent()
                    )
                }
            }

            val MIGRATION_38_39 = object : Migration(38, 39) {
                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS FILARIA_MDA_VISIT_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                hhId INTEGER NOT NULL,
                visitDate TEXT NOT NULL,
                visitMonth TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                syncedAt TEXT
            )
            """.trimIndent()
                    )
                    database.execSQL(
                        """
            CREATE UNIQUE INDEX IF NOT EXISTS index_FILARIA_MDA_VISIT_HISTORY_hhId_formId_visitMonth
            ON FILARIA_MDA_VISIT_HISTORY (hhId, formId, visitMonth)
            """.trimIndent()
                    )

                    database.execSQL(
                        """
            CREATE INDEX IF NOT EXISTS index_FILARIA_MDA_VISIT_HISTORY_hhId_visitDate
            ON FILARIA_MDA_VISIT_HISTORY (hhId, visitDate)
            """.trimIndent()
                    )
                }
            }


            val MIGRATION_37_38 = object : Migration(37, 38) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN isSNCU TEXT")
                    database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary1 TEXT")
                    database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary2 TEXT")
                    database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary3 TEXT")
                    database.execSQL("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary4 TEXT")
                }
            }

            val MIGRATION_36_37 = object : Migration(36, 37) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS mosquito_net_visit (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                hhId INTEGER NOT NULL,
                visitDate TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                syncedAt TEXT,
                createdAt INTEGER NOT NULL DEFAULT (strftime('%s','now'))
            )
            """.trimIndent()
                    )

                    database.execSQL(
                        """
            CREATE UNIQUE INDEX IF NOT EXISTS index_mosquito_net_visit_unique
            ON mosquito_net_visit (hhId, visitDate, formId)
            """.trimIndent()
                    )
                }
            }

            val MIGRATION_35_36 = object : Migration(35, 36) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
            ALTER TABLE ALL_EYE_SURGERY_VISIT_HISTORY
            ADD COLUMN visitMonth TEXT NOT NULL DEFAULT ''
        """.trimIndent())

                    db.execSQL("""
            UPDATE ALL_EYE_SURGERY_VISIT_HISTORY
            SET visitMonth = 
                CASE
                    WHEN visitDate GLOB '__-__-____'
                    THEN substr(visitDate, 7, 4) || '-' || substr(visitDate, 4, 2)
                    ELSE ''
                END
        """.trimIndent())

                    db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_eye_unique_month
            ON ALL_EYE_SURGERY_VISIT_HISTORY(benId, formId, visitMonth)
        """.trimIndent())
//                    database.execSQL("ALTER TABLE MALARIA_SCREENING ADD COLUMN visitId INTEGER NOT NULL DEFAULT 1")
                    db.execSQL("DROP INDEX IF EXISTS ind_malariasn")
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ind_malariasn ON MALARIA_SCREENING(benId, visitId)")
                }
            }





            val MIGRATION_34_35 = object : Migration(34, 35) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS `ALL_EYE_SURGERY_VISIT_HISTORY` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `benId` INTEGER NOT NULL,
                `hhId` INTEGER NOT NULL,
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
                    database.execSQL(
                        """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_hhId_visitDate_formId`
            ON `ALL_EYE_SURGERY_VISIT_HISTORY` (`benId`, `hhId`, `visitDate`, `formId`)
            """.trimIndent()
                    )
                    database.execSQL("ALTER TABLE MALARIA_SCREENING ADD COLUMN visitId INTEGER NOT NULL DEFAULT 1")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS ind_malariasn ON MALARIA_SCREENING(benId, visitId)")

                    database.execSQL("ALTER TABLE MALARIA_SCREENING ADD COLUMN malariaTestType INTEGER DEFAULT 0")
                    database.execSQL("ALTER TABLE MALARIA_SCREENING ADD COLUMN malariaSlideTestType INTEGER DEFAULT 0")
                }
            }


            val MIGRATION_33_34 = object : Migration(31, 32) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS form_schema (
                formId TEXT NOT NULL PRIMARY KEY,
                formName TEXT NOT NULL,
                version INTEGER NOT NULL DEFAULT 1,
                schemaJson TEXT NOT NULL
            )
        """.trimIndent())

                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS all_visit_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                hhId INTEGER NOT NULL,
                visitDay TEXT NOT NULL,
                visitDate TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                syncedAt INTEGER
            )
        """.trimIndent())

                    database.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS index_all_visit_history_unique 
            ON all_visit_history (benId, hhId, visitDay, visitDate, formId)
        """.trimIndent())
                }
            }


            val MIGRATION_32_33 = object : Migration(32, 33) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS ALL_VISIT_HISTORY_HBYC (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                benId INTEGER NOT NULL,
                hhId INTEGER NOT NULL,
                visitDay TEXT NOT NULL,
                visitDate TEXT NOT NULL,
                formId TEXT NOT NULL,
                version INTEGER NOT NULL,
                formDataJson TEXT NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                syncedAt INTEGER
            )
        """.trimIndent())
                    database.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS index_all_visit_history_hbyc_unique 
            ON ALL_VISIT_HISTORY_HBYC (benId, hhId, visitDay, visitDate, formId)
        """.trimIndent())
                }
            }

            val MIGRATION_31_32 = object : Migration(31, 32) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS `children_under_five_all_visit` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `benId` INTEGER NOT NULL,
                `hhId` INTEGER NOT NULL,
                `visitDate` TEXT NOT NULL,
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
            CREATE UNIQUE INDEX IF NOT EXISTS `index_children_under_five_all_visit_benId_hhId_visitDate_formId`
            ON `children_under_five_all_visit` (`benId`, `hhId`, `visitDate`, `formId`)
            """.trimIndent()
                    )
                }
            }


            val MIGRATION_30_31 = object : Migration(30, 31) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE CBAC ADD COLUMN isReffered INTEGER DEFAULT 0")

                    // Create the new table
                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS form_schema (
                formId TEXT NOT NULL PRIMARY KEY,
                formName TEXT NOT NULL,
                version INTEGER NOT NULL DEFAULT 1,
                schemaJson TEXT NOT NULL
            )
        """.trimIndent())
                    database.execSQL("ALTER TABLE IMMUNIZATION ADD COLUMN mcpCardSummary1 TEXT")
                    database.execSQL("ALTER TABLE IMMUNIZATION ADD COLUMN mcpCardSummary2 TEXT")
                    database.execSQL("DROP TABLE IF EXISTS `UWIN_SESSION`")
                    database.execSQL("""CREATE TABLE IF NOT EXISTS `UWIN_SESSION` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionDate` INTEGER NOT NULL,
                    `place` TEXT,
                    `participantsCount` INTEGER NOT NULL,
                    `uploadedFiles1` TEXT,
                    `uploadedFiles2` TEXT,
                    `processed` TEXT,
                    `createdBy` TEXT NOT NULL,
                    `createdDate` INTEGER NOT NULL,
                    `updatedBy` TEXT NOT NULL,
                    `updatedDate` INTEGER NOT NULL,
                    `syncState` INTEGER NOT NULL
                    )
                    """.trimIndent())
                }
            }


            val MIGRATION_29_30 = object : Migration(29, 30) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary1 TEXT")
                    database.execSQL("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary2 TEXT")
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS `MAA_MEETING` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "`meetingDate` TEXT, `place` TEXT, `participants` INTEGER, `ashaId` INTEGER, " +
                                "`meetingImages` TEXT, " +
                                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `syncState` INTEGER NOT NULL)"
                    )
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN isKitHandedOver INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitHandedOverDate INTEGER")
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitPhoto1 TEXT")
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitPhoto2 TEXT")
                }
            }




//            val MIGRATION_22_23 = object : Migration(22, 23) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    database.execSQL("ALTER TABLE GENERAL_OPD_ACTIVITY ADD COLUMN village TEXT")
//                }
//            }


            val MIGRATION_28_29 = object : Migration(28, 29) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE PMSMA ADD COLUMN visitDate INTEGER")
                    database.execSQL("ALTER TABLE PMSMA ADD COLUMN visitNumber INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE PMSMA ADD COLUMN anyOtherHighRiskCondition TEXT")
                }
            }

            val MIGRATION_27_28 = object : Migration(27, 28) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    val cursor = database.query("PRAGMA table_info(PNC_VISIT)")
                    val existingColumns = mutableSetOf<String>()
                    while (cursor.moveToNext()) {
                        existingColumns.add(cursor.getString(1))
                    }
                    cursor.close()

                    if (!existingColumns.contains("deliveryDischargeSummary1")) {
                        database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary1 TEXT")
                    }
                    if (!existingColumns.contains("deliveryDischargeSummary2")) {
                        database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary2 TEXT")
                    }
                    if (!existingColumns.contains("deliveryDischargeSummary3")) {
                        database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary3 TEXT")
                    }
                    if (!existingColumns.contains("deliveryDischargeSummary4")) {
                        database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary4 TEXT")
                    }
                    if (!existingColumns.contains("sterilisationDate")) {
                        database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN sterilisationDate INTEGER ")
                    }
                }

            }

            val MIGRATION_26_27 = object : Migration(26, 27) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE INCENTIVE_ACTIVITY ADD COLUMN groupName TEXT")

                }
            }


            val MIGRATION_25_26 = object : Migration(25, 26) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN lmpDate INTEGER")
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN visitDate INTEGER")
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN weekOfPregnancy INTEGER")
                }
            }

//            val MIGRATION_20_21 = object : Migration(20, 21) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    database.execSQL("""
//            CREATE TABLE IF NOT EXISTS `GENERAL_OPD_ACTIVITY` (
//                `benFlowID` INTEGER,
//                `beneficiaryRegID` INTEGER,
//                `benVisitID` INTEGER,
//                `visitCode` INTEGER,
//                `benVisitNo` INTEGER,
//                `nurseFlag` INTEGER,
//                `doctorFlag` INTEGER,
//                `pharmacist_flag` INTEGER,
//                `lab_technician_flag` INTEGER,
//                `radiologist_flag` INTEGER,
//                `oncologist_flag` INTEGER,
//                `specialist_flag` INTEGER,
//                `agentId` TEXT,
//                `visitDate` TEXT,
//                `modified_by` TEXT,
//                `modified_date` TEXT,
//                `benName` TEXT,
//                `deleted` INTEGER,
//                `firstName` TEXT,
//                `lastName` TEXT,
//                `age` TEXT,
//                `ben_age_val` INTEGER,
//                `genderID` INTEGER,
//                `genderName` TEXT,
//                `preferredPhoneNum` TEXT,
//                `fatherName` TEXT,
//                `spouseName` TEXT,
//                `districtName` TEXT,
//                `servicePointName` TEXT,
//                `registrationDate` TEXT,
//                `benVisitDate` TEXT,
//                `consultationDate` TEXT,
//                `consultantID` INTEGER,
//                `consultantName` TEXT,
//                `visitSession` TEXT,
//                `servicePointID` INTEGER,
//                `districtID` INTEGER,
//                `villageID` INTEGER,
//                `vanID` INTEGER,
//                `beneficiaryId` INTEGER NOT NULL,
//                `dob` TEXT,
//                `tc_SpecialistLabFlag` INTEGER,
//                `visitReason` TEXT,
//                `visitCategory` TEXT,
//                PRIMARY KEY(`beneficiaryId`)
//            )
//        """)
//                }
//            }

//            val MIGRATION_19_20 = object : Migration(19, 20) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    database.execSQL("ALTER TABLE MALARIA_SCREENING ADD COLUMN slideTestName TEXT")
//                }
//            }

            val MIGRATION_24_25 = object : Migration(24, 25) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN dateOfAntraInjection TEXT")
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN dueDateOfAntraInjection TEXT")
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN mpaFile TEXT")
                    database.execSQL("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN antraDose TEXT")
                }
            }

            val MIGRATION_23_24 = object : Migration(23, 24) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE MDSR ADD COLUMN mdsr1File TEXT")
                    database.execSQL("ALTER TABLE MDSR ADD COLUMN mdsr2File TEXT")
                    database.execSQL("ALTER TABLE MDSR ADD COLUMN mdsrDeathCertFile TEXT")
                }
            }
            val MIGRATION_22_23 = object : Migration(22, 23) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        "ALTER TABLE PNC_VISIT ADD COLUMN otherPlaceOfDeath TEXT"
                    )
                    database.execSQL("ALTER TABLE GENERAL_OPD_ACTIVITY ADD COLUMN village TEXT")
                }
            }


//            val MIGRATION_21_22 = object : Migration(21, 22) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeath INTEGER")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeathValue TEXT")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN dateOfDeath TEXT")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeath TEXT")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeathId INTEGER DEFAULT 0")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN otherPlaceOfDeath TEXT")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp1File TEXT")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp2File TEXT")
//                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN jsyFile TEXT")
//                }
//            }


            val MIGRATION_20_21 = object : Migration(20, 21) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE CDR ADD COLUMN cdr1File TEXT")
                    database.execSQL("ALTER TABLE CDR ADD COLUMN cdr2File TEXT")
                    database.execSQL("ALTER TABLE CDR ADD COLUMN cdrDeathCertFile TEXT")
                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS `GENERAL_OPD_ACTIVITY` (
                `benFlowID` INTEGER,
                `beneficiaryRegID` INTEGER,
                `benVisitID` INTEGER,
                `visitCode` INTEGER,
                `benVisitNo` INTEGER,
                `nurseFlag` INTEGER,
                `doctorFlag` INTEGER,
                `pharmacist_flag` INTEGER,
                `lab_technician_flag` INTEGER,
                `radiologist_flag` INTEGER,
                `oncologist_flag` INTEGER,
                `specialist_flag` INTEGER,
                `agentId` TEXT,
                `visitDate` TEXT,
                `modified_by` TEXT,
                `modified_date` TEXT,
                `benName` TEXT,
                `deleted` INTEGER,
                `firstName` TEXT,
                `lastName` TEXT,
                `age` TEXT,
                `ben_age_val` INTEGER,
                `genderID` INTEGER,
                `genderName` TEXT,
                `preferredPhoneNum` TEXT,
                `fatherName` TEXT,
                `spouseName` TEXT,
                `districtName` TEXT,
                `servicePointName` TEXT,
                `registrationDate` TEXT,
                `benVisitDate` TEXT,
                `consultationDate` TEXT,
                `consultantID` INTEGER,
                `consultantName` TEXT,
                `visitSession` TEXT,
                `servicePointID` INTEGER,
                `districtID` INTEGER,
                `villageID` INTEGER,
                `vanID` INTEGER,
                `beneficiaryId` INTEGER NOT NULL,
                `dob` TEXT,
                `tc_SpecialistLabFlag` INTEGER,
                `visitReason` TEXT,
                `visitCategory` TEXT,
                PRIMARY KEY(`beneficiaryId`)
            )
        """
                    )
                }
            }
            val MIGRATION_19_20 = object : Migration(19, 20) {
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
                    db.execSQL("ALTER TABLE MALARIA_SCREENING ADD COLUMN slideTestName TEXT")
                }
            }
//            val MIGRATION_18_19 = object : Migration(18, 19) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    val columns = listOf(
//                        "isDeath INTEGER",
//                        "isDeathValue TEXT",
//                        "dateOfDeath TEXT",
//                        "timeOfDeath TEXT",
//                        "reasonOfDeath TEXT",
//                        "reasonOfDeathId INTEGER",
//                        "placeOfDeath TEXT",
//                        "placeOfDeathId INTEGER",
//                        "otherPlaceOfDeath TEXT"
//                    )
//
//                    for (column in columns) {
//                        database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN $column")
//                    }
//
//
//
//                    // 🔹 Columns for PREGNANCY_ANC table
//                    val pregnancyAncColumns = listOf(
//                        "serialNo TEXT",
//                        "methodOfTermination TEXT",
//                        "methodOfTerminationId INTEGER DEFAULT 0 NOT NULL",
//                        "terminationDoneBy TEXT",
//                        "terminationDoneById INTEGER DEFAULT 0 NOT NULL",
//                        "isPaiucdId INTEGER DEFAULT 0 NOT NULL",
//                        "isPaiucd TEXT",
//                        "remarks TEXT",
//                        "abortionImg1 TEXT",
//                        "abortionImg2 TEXT",
//                        "placeOfDeath TEXT",
//                        "placeOfDeathId INTEGER DEFAULT 0 NOT NULL",
//                        "otherPlaceOfDeath TEXT"
//                    )
//
//                    for (column in pregnancyAncColumns) {
//                        database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN $column")
//                    }
//
//                }
//            }

            val MIGRATION_17_18 = object : Migration(17, 18) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // 1. Create new table with updated schema
                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS `ABHA_GENERATED_NEW` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `beneficiaryID` INTEGER NOT NULL,
                `beneficiaryRegID` INTEGER NOT NULL,
                `benName` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `message` TEXT NOT NULL,
                `txnId` TEXT NOT NULL,
                `benSurname` TEXT,
                `healthId` TEXT NOT NULL,
                `healthIdNumber` TEXT NOT NULL,
                `abhaProfileJson` TEXT NOT NULL,
                `isNewAbha` INTEGER NOT NULL,
                `providerServiceMapId` INTEGER NOT NULL,
                `syncState` INTEGER NOT NULL,
                FOREIGN KEY(`beneficiaryID`) REFERENCES `BENEFICIARY`(`beneficiaryId`) ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent()
                    )
                    // 2. Copy existing data into new table (with default/placeholder values for new fields)
                    try {
                        database.execSQL(
                            """
                INSERT INTO ABHA_GENERATED_NEW (
                    id,
                    beneficiaryID,
                    beneficiaryRegID,
                    benName,
                    createdBy,
                    message,
                    txnId,
                    benSurname,
                    healthId,
                    healthIdNumber,
                    abhaProfileJson,
                    isNewAbha,
                    providerServiceMapId,
                    syncState
                )
                SELECT
                    id,
                    benId AS beneficiaryID,
                    hhId AS beneficiaryRegID,
                    benName,
                    '' AS createdBy,
                    '' AS message,
                    '' AS txnId,
                    benSurname,
                    healthId,
                    healthIdNumber,
                    '' AS abhaProfileJson,
                    isNewAbha,
                    0,
                    0
                FROM ABHA_GENERATED
            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        // Table might not exist on some devices — log and continue
                        Log.w(
                            "RoomMigration",
                            "Skipping data copy: ABHA_GENERATED table not found",
                            e
                        )
                    }

                    // 3. Drop old table
                    try {
                        database.execSQL("DROP TABLE IF EXISTS ABHA_GENERATED")
                    } catch (_: Exception) {
                    }

                    // 4. Rename new table
                    database.execSQL("ALTER TABLE ABHA_GENERATED_NEW RENAME TO ABHA_GENERATED")

                    // 5. Recreate index
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ABHA_GENERATED_beneficiaryID` ON `ABHA_GENERATED` (`beneficiaryID`)")
                }
            }

            val MIGRATION_16_18 = object : Migration(16, 18) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // 1. Create new table with updated schema
                    database.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS `ABHA_GENERATED_NEW` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `beneficiaryID` INTEGER NOT NULL,
                `beneficiaryRegID` INTEGER NOT NULL,
                `benName` TEXT NOT NULL,
                `createdBy` TEXT NOT NULL,
                `message` TEXT NOT NULL,
                `txnId` TEXT NOT NULL,
                `benSurname` TEXT,
                `healthId` TEXT NOT NULL,
                `healthIdNumber` TEXT NOT NULL,
                `abhaProfileJson` TEXT NOT NULL,
                `isNewAbha` INTEGER NOT NULL,
                `providerServiceMapId` INTEGER NOT NULL,
                `syncState` INTEGER NOT NULL,
                FOREIGN KEY(`beneficiaryID`) REFERENCES `BENEFICIARY`(`beneficiaryId`) ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent()
                    )
                    // 2. Copy existing data into new table (with default/placeholder values for new fields)
                    try {
                        database.execSQL(
                            """
                INSERT INTO ABHA_GENERATED_NEW (
                    id,
                    beneficiaryID,
                    beneficiaryRegID,
                    benName,
                    createdBy,
                    message,
                    txnId,
                    benSurname,
                    healthId,
                    healthIdNumber,
                    abhaProfileJson,
                    isNewAbha,
                    providerServiceMapId,
                    syncState
                )
                SELECT
                    id,
                    benId AS beneficiaryID,
                    hhId AS beneficiaryRegID,
                    benName,
                    '' AS createdBy,
                    '' AS message,
                    '' AS txnId,
                    benSurname,
                    healthId,
                    healthIdNumber,
                    '' AS abhaProfileJson,
                    isNewAbha,
                    0,
                    0
                FROM ABHA_GENERATED
            """.trimIndent()
                        )
                    } catch (e: Exception) {
                        // Table might not exist on some devices — log and continue
                        Log.w(
                            "RoomMigration",
                            "Skipping data copy: ABHA_GENERATED table not found",
                            e
                        )
                    }

                    // 3. Drop old table
                    try {
                        database.execSQL("DROP TABLE IF EXISTS ABHA_GENERATED")
                    } catch (_: Exception) {
                    }

                    // 4. Rename new table
                    database.execSQL("ALTER TABLE ABHA_GENERATED_NEW RENAME TO ABHA_GENERATED")

                    // 5. Recreate index
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ABHA_GENERATED_beneficiaryID` ON `ABHA_GENERATED` (`beneficiaryID`)")
                }
            }

            val MIGRATION_21_22 = object : Migration(21, 22) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
            CREATE TABLE PREGNANCY_ANC_NEW (
                id INTEGER NOT NULL PRIMARY KEY,
                benId INTEGER NOT NULL,
                visitNumber INTEGER NOT NULL,
                isActive INTEGER NOT NULL,
                ancDate INTEGER NOT NULL,
                isAborted INTEGER NOT NULL,
                abortionType TEXT,
                abortionTypeId INTEGER NOT NULL,
                abortionFacility TEXT,
                abortionFacilityId INTEGER NOT NULL,
                abortionDate INTEGER,
                weight INTEGER,
                bpSystolic INTEGER,
                bpDiastolic INTEGER,
                pulseRate TEXT,
                hb REAL,
                fundalHeight INTEGER,
                urineAlbumin TEXT,
                urineAlbuminId INTEGER NOT NULL,
                randomBloodSugarTest TEXT,
                randomBloodSugarTestId INTEGER NOT NULL,
                numFolicAcidTabGiven INTEGER NOT NULL,
                numIfaAcidTabGiven INTEGER NOT NULL,
                anyHighRisk INTEGER,
                highRisk TEXT,
                highRiskId INTEGER NOT NULL,
                otherHighRisk TEXT,
                referralFacility TEXT,
                referralFacilityId INTEGER NOT NULL,
                hrpConfirmed INTEGER,
                hrpConfirmedBy TEXT,
                hrpConfirmedById INTEGER NOT NULL,
                maternalDeath INTEGER,
                maternalDeathProbableCause TEXT,
                maternalDeathProbableCauseId INTEGER NOT NULL,
                otherMaternalDeathProbableCause TEXT,
                deathDate INTEGER,
                pregnantWomanDelivered INTEGER,
                processed TEXT,
                createdBy TEXT NOT NULL,
                createdDate INTEGER NOT NULL,
                updatedBy TEXT NOT NULL,
                updatedDate INTEGER NOT NULL,
                syncState INTEGER NOT NULL,
                frontFilePath TEXT,   -- nullable now
                backFilePath TEXT,    -- nullable now
                FOREIGN KEY(benId) REFERENCES BENEFICIARY(beneficiaryId) 
                  ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent()
                    )

                    database.execSQL(
                        """
            INSERT INTO PREGNANCY_ANC_NEW (
                id, benId, visitNumber, isActive, ancDate, isAborted,
                abortionType, abortionTypeId, abortionFacility, abortionFacilityId, abortionDate,
                weight, bpSystolic, bpDiastolic, pulseRate, hb, fundalHeight,
                urineAlbumin, urineAlbuminId, randomBloodSugarTest, randomBloodSugarTestId,
                numFolicAcidTabGiven, numIfaAcidTabGiven, anyHighRisk, highRisk, highRiskId,
                otherHighRisk, referralFacility, referralFacilityId,
                hrpConfirmed, hrpConfirmedBy, hrpConfirmedById,
                maternalDeath, maternalDeathProbableCause, maternalDeathProbableCauseId,
                otherMaternalDeathProbableCause, deathDate, pregnantWomanDelivered,
                processed, createdBy, createdDate, updatedBy, updatedDate, syncState,
                frontFilePath, backFilePath
            )
            SELECT 
                id, benId, visitNumber, isActive, ancDate, isAborted,
                abortionType, abortionTypeId, abortionFacility, abortionFacilityId, abortionDate,
                weight, bpSystolic, bpDiastolic, pulseRate, hb, fundalHeight,
                urineAlbumin, urineAlbuminId, randomBloodSugarTest, randomBloodSugarTestId,
                numFolicAcidTabGiven, numIfaAcidTabGiven, anyHighRisk, highRisk, highRiskId,
                otherHighRisk, referralFacility, referralFacilityId,
                hrpConfirmed, hrpConfirmedBy, hrpConfirmedById,
                maternalDeath, maternalDeathProbableCause, maternalDeathProbableCauseId,
                otherMaternalDeathProbableCause, deathDate, pregnantWomanDelivered,
                processed, createdBy, createdDate, updatedBy, updatedDate, syncState,
                frontFilePath, backFilePath
            FROM PREGNANCY_ANC
        """.trimIndent()
                    )

                    database.execSQL("DROP TABLE PREGNANCY_ANC")

                    database.execSQL("ALTER TABLE PREGNANCY_ANC_NEW RENAME TO PREGNANCY_ANC")

                    database.execSQL("CREATE INDEX ind_mha ON PREGNANCY_ANC(benId)")

                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeath INTEGER")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeathValue TEXT")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN dateOfDeath TEXT")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeath TEXT")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeathId INTEGER DEFAULT 0")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN otherPlaceOfDeath TEXT")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp1File TEXT")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp2File TEXT")
                    database.execSQL("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN jsyFile TEXT")
                }
            }


            val MIGRATION_15_16 = object : Migration(15, 16) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Step 2: Drop the old view to ensure a clean slate.
                    db.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")

                    // Step 3: Create the new view with a clean, correct, and complete SQL query.
                    db.execSQL(
                        "CREATE VIEW `BEN_BASIC_CACHE` AS " +
                                "SELECT b.beneficiaryId as benId, b.householdId as hhId, b.regDate, b.firstName as benName, b.lastName as benSurname, b.gender, b.dob as dob, b.familyHeadRelationPosition as relToHeadId" +
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
                }
            }

            val MIGRATION_14_15 = Migration(14, 15, migrate = {
                it.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN isNewAbha INTEGER NOT NULL DEFAULT 0")
                it.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE");
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

            val MIGRATION_18_19 = object : Migration(18, 19) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    try {

                        // 1. Create new table with correct schema

                        database.execSQL(
                            """
            CREATE TABLE PROFILE_ACTIVITY_new (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT,  -- nullable (because String?)
                profileImage TEXT NOT NULL,
                village TEXT NOT NULL,
                employeeId INTEGER NOT NULL,
                dob TEXT NOT NULL,
                age INTEGER NOT NULL,
                mobileNumber TEXT NOT NULL,
                alternateMobileNumber TEXT NOT NULL,
                fatherOrSpouseName TEXT NOT NULL,
                dateOfJoining TEXT NOT NULL,
                bankAccount TEXT NOT NULL,
                ifsc TEXT NOT NULL,
                populationCovered INTEGER NOT NULL,
                choName TEXT NOT NULL,
                choMobile TEXT NOT NULL,
                awwName TEXT NOT NULL,
                awwMobile TEXT NOT NULL,
                anm1Name TEXT NOT NULL,
                anm1Mobile TEXT NOT NULL,
                anm2Name TEXT NOT NULL,
                anm2Mobile TEXT NOT NULL,
                abhaNumber TEXT NOT NULL,
                ashaHouseholdRegistration TEXT NOT NULL,
                ashaFamilyMember TEXT NOT NULL,
                providerServiceMapID TEXT NOT NULL,
                isFatherOrSpouse INTEGER NOT NULL DEFAULT 0,
                supervisorName TEXT NOT NULL,
                supervisorMobile TEXT NOT NULL
            )
        """
                        )

                        database.execSQL(
                            """
            INSERT INTO PROFILE_ACTIVITY_new (
                id, name, profileImage, village, employeeId, dob, age, mobileNumber,
                alternateMobileNumber, fatherOrSpouseName, dateOfJoining, bankAccount,
                ifsc, populationCovered, choName, choMobile, awwName, awwMobile,
                anm1Name, anm1Mobile, anm2Name, anm2Mobile, abhaNumber,
                ashaHouseholdRegistration, ashaFamilyMember, providerServiceMapID,
                isFatherOrSpouse, supervisorName, supervisorMobile
            )
            SELECT 
                id,
                name,
                profileImage,
                village,
                employeeId,
                dob,
                age,
                mobileNumber,
                alternateMobileNumber,
                fatherOrSpouseName,
                dateOfJoining,
                bankAccount,
                ifsc,
                populationCovered,
                choName,
                choMobile,
                awwName,
                awwMobile,
                anm1Name,
                anm1Mobile,
                anm2Name,
                anm2Mobile,
                abhaNumber,
                ashaHouseholdRegistration,
                ashaFamilyMember,
                providerServiceMapID,
                isFatherOrSpouse,
                supervisorName,
                supervisorMobile
            FROM PROFILE_ACTIVITY
        """
                        )

                        database.execSQL("DROP TABLE PROFILE_ACTIVITY")
                        database.execSQL("ALTER TABLE PROFILE_ACTIVITY_new RENAME TO PROFILE_ACTIVITY")

                        val columns = listOf(
                            "isDeath INTEGER",
                            "isDeathValue TEXT",
                            "dateOfDeath TEXT",
                            "timeOfDeath TEXT",
                            "reasonOfDeath TEXT",
                            "reasonOfDeathId INTEGER",
                            "placeOfDeath TEXT",
                            "placeOfDeathId INTEGER",
                            "otherPlaceOfDeath TEXT"
                        )

                        for (column in columns) {
                            database.execSQL("ALTER TABLE BENEFICIARY ADD COLUMN $column")
                        }


                        // 🔹 Columns for PREGNANCY_ANC table
                        val pregnancyAncColumns = listOf(
                            "serialNo TEXT",
                            "methodOfTermination TEXT",
                            "methodOfTerminationId INTEGER DEFAULT 0 NOT NULL",
                            "terminationDoneBy TEXT",
                            "terminationDoneById INTEGER DEFAULT 0 NOT NULL",
                            "isPaiucdId INTEGER DEFAULT 0 NOT NULL",
                            "isPaiucd TEXT",
                            "remarks TEXT",
                            "abortionImg1 TEXT",
                            "abortionImg2 TEXT",
                            "placeOfDeath TEXT",
                            "placeOfDeathId INTEGER DEFAULT 0 NOT NULL",
                            "otherPlaceOfDeath TEXT"
                        )

                        for (column in pregnancyAncColumns) {
                            database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN $column")
                        }

                    } catch (e: Exception) {

                        Log.e("DB_MIGRATION", "Migration 1->2 failed: ${e.message}", e)

                        throw e

                    }

                }
            }

//        _db.execSQL("CREATE TABLE IF NOT EXISTS `HRP_PREGNANT_TRACK` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `benId` INTEGER NOT NULL, `visitDate` INTEGER, `rdPmsa` TEXT, `rdDengue` TEXT, `rdFilaria` TEXT, `severeAnemia` TEXT, `hemoglobinTest` TEXT, `ifaGiven` TEXT, `ifaQuantity` INTEGER, `pregInducedHypertension` TEXT, `systolic` INTEGER, `diastolic` INTEGER, `gestDiabetesMellitus` TEXT, `bloodGlucoseTest` TEXT, `fbg` INTEGER, `rbg` INTEGER, `ppbg` INTEGER, `fastingOgtt` INTEGER, `after2hrsOgtt` INTEGER, `hypothyrodism` TEXT, `polyhydromnios` TEXT, `oligohydromnios` TEXT, `antepartumHem` TEXT, `malPresentation` TEXT, `hivsyph` TEXT, `visit` TEXT, `syncState` INTEGER NOT NULL, FOREIGN KEY(`benId`) REFERENCES `BENEFICIARY`(`beneficiaryId`) ON UPDATE CASCADE ON DELETE CASCADE )");
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        appContext,
                        InAppDb::class.java,
                        "Sakhi-2.0-In-app-database"
                    ).addMigrations(
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
//                        MIGRATION_16_18, \\2.5
//                        MIGRATION_18_19,
//                        MIGRATION_19_20,
//                        MIGRATION_20_21,
//                        MIGRATION_21_22,
//                        MIGRATION_22_23  \\2.5
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20,
                        MIGRATION_20_21,
                        MIGRATION_21_22,
                        MIGRATION_22_23,
                        MIGRATION_23_24,
                        MIGRATION_24_25,
                        MIGRATION_25_26,
                        MIGRATION_26_27,
                        MIGRATION_27_28,
                        MIGRATION_28_29,
                        MIGRATION_29_30,
                        MIGRATION_30_31,
                        MIGRATION_31_32,
                        MIGRATION_32_33,
                        MIGRATION_33_34,
                        MIGRATION_34_35,
                        MIGRATION_35_36,
                        MIGRATION_36_37,
                        MIGRATION_37_38,
                        MIGRATION_38_39,
                        MIGRATION_39_40,
                        MIGRATION_40_41,
                        MIGRATION_41_42,
                        MIGRATION_42_43,
                        MIGRATION_43_44,
                        MIGRATION_44_45,
                        MIGRATION_45_46,
                        MIGRATION_46_47,
                        MIGRATION_47_48,
                        MIGRATION_48_49,
                        MIGRATION_49_50,
                        MIGRATION_50_51,
                        MIGRATION_51_52,
                        MIGRATION_52_53,
                        MIGRATION_53_54

                    ).build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}
package org.piramalswasthya.sakhi.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.piramalswasthya.sakhi.database.converters.LocationEntityListConverter
import org.piramalswasthya.sakhi.database.converters.StringListConverter
import org.piramalswasthya.sakhi.database.converters.SyncStateConverter
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.database.room.dao.AdolescentHealthDao
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
import org.piramalswasthya.sakhi.database.room.dao.GeneralCheckupDao
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
import org.piramalswasthya.sakhi.database.room.dao.MaaMeetingDao
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.room.dao.MosquitoNetFormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.PmjayDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.NotificationDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.room.dao.UwinDao
import org.piramalswasthya.sakhi.database.room.dao.VLFDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.BenIfaFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.EyeSurgeryFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMDAFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDaoHBYC
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.InfantDao
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.helpers.DatabaseKeyManager
import org.piramalswasthya.sakhi.helpers.RoomDbEncryptionHelper
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMdaCampaignJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.NCDReferalFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.TBReferralFollowUpDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.TPTFollowUpDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.database.room.migrations.ALL_MIGRATIONS
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
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
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.PMJAYCache
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.model.NotificationEntity
import org.piramalswasthya.sakhi.model.GeneralCheckupCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.InfantEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.TBReferralFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
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
        PulsePolioCampaignCache::class,
        ORSCampaignCache::class,
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
        FilariaMDACampaignFormResponseJsonEntity::class,
        TBConfirmedTreatmentCache::class,
        TBReferralFollowUpEntity::class,
        TPTFollowUpEntity::class,
        NotificationEntity::class,
        GeneralCheckupCache::class
    ],
    views = [BenBasicCache::class],
    version = 73, exportSchema = true
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
    abstract val notificationDao: NotificationDao
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
    abstract val generalCheckupDao: GeneralCheckupDao

    abstract fun infantDao(): InfantDao
    abstract fun formSchemaDao(): FormSchemaDao
    abstract fun formResponseDao(): FormResponseDao
    abstract fun CUFYFormResponseDao(): CUFYFormResponseDao
    abstract fun CUFYFormResponseJsonDao(): CUFYFormResponseJsonDao
    abstract fun NCDReferalFormResponseJsonDao(): NCDReferalFormResponseJsonDao
    abstract fun tbReferralFollowUpDao(): TBReferralFollowUpDao
    abstract fun tptFollowUpDao(): TPTFollowUpDao
    abstract fun formResponseJsonDao(): FormResponseJsonDao
    abstract fun formResponseJsonDaoHBYC(): FormResponseJsonDaoHBYC

    abstract fun formResponseJsonDaoANC() : FormResponseANCJsonDao
    abstract fun formResponseJsonDaoEyeSurgery(): EyeSurgeryFormResponseJsonDao
    abstract fun formResponseJsonDaoBenIfa(): BenIfaFormResponseJsonDao
    abstract fun formResponseMosquitoNetJsonDao(): MosquitoNetFormResponseDao
    abstract fun formResponseFilariaMDAJsonDao(): FilariaMDAFormResponseJsonDao
    abstract fun formResponseFilariaMDACampaignJsonDao(): FilariaMdaCampaignJsonDao

    abstract val syncDao: SyncDao

    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null

        fun getInstance(appContext: Context): InAppDb {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    val isDebug = appContext.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0

                    val builder = Room.databaseBuilder(
                        appContext,
                        InAppDb::class.java,
                        "Sakhi-2.0-In-app-database"
                    )

                    if (!isDebug) {
                        val passphrase = DatabaseKeyManager.getDatabasePassphrase(appContext)

                        RoomDbEncryptionHelper.encryptIfNeeded(
                            context = appContext,
                            dbName = "Sakhi-2.0-In-app-database",
                            passphrase = passphrase
                        )

                        val factory = SupportOpenHelperFactory(String(passphrase).toByteArray(Charsets.UTF_8))
                        builder.openHelperFactory(factory)
                    }

                    instance = builder.addMigrations(*ALL_MIGRATIONS).build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}

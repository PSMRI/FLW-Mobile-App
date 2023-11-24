package org.piramalswasthya.sakhi.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import org.piramalswasthya.sakhi.database.converters.LocationEntityListConverter
import org.piramalswasthya.sakhi.database.converters.SyncStateConverter
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.room.dao.EcrDao
import org.piramalswasthya.sakhi.database.room.dao.FpotDao
import org.piramalswasthya.sakhi.database.room.dao.HbncDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.HrpDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.room.dao.PmjayDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.CbacCache
import org.piramalswasthya.sakhi.model.ChildRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.FPOTCache
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.IncentiveActivityCache
import org.piramalswasthya.sakhi.model.IncentiveRecordCache
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.MDSRCache
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
    ],
    views = [BenBasicCache::class],
    version = 13, exportSchema = false
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

    abstract val syncDao: SyncDao

    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null

        fun getInstance(appContext: Context): InAppDb {

            val MIGRATION_1_2 = Migration(1, 2, migrate = {
                it.execSQL("select count(*) from beneficiary")
            })

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        appContext,
                        InAppDb::class.java,
                        "Sakhi-2.0-In-app-database"
                    )
                        .addMigrations(
//                            MIGRATION_1_2
                        )
                        .build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}
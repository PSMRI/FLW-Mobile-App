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
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
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
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDaoHBYC
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormSchemaDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.InfantDao
import org.piramalswasthya.sakhi.model.ABHAModel
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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.InfantEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC

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
        ABHAModel::class,
        //Dynamic Data
        InfantEntity::class,
        FormSchemaEntity::class,
        FormResponseJsonEntity::class,
        FormResponseJsonEntityHBYC::class
    ],
    views = [BenBasicCache::class],

    version = 33, exportSchema = false
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
    abstract val abhaGenratedDao: ABHAGenratedDao

    abstract fun infantDao(): InfantDao
    abstract fun formSchemaDao(): FormSchemaDao
    abstract fun formResponseDao(): FormResponseDao
    abstract fun formResponseJsonDao(): FormResponseJsonDao
    abstract fun formResponseJsonDaoHBYC(): FormResponseJsonDaoHBYC

    abstract val syncDao: SyncDao

    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null

        fun getInstance(appContext: Context): InAppDb {


            val MIGRATION_1_2 = Migration(1, 2, migrate = {
//                it.execSQL("select count(*) from beneficiary")
            })
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


            val MIGRATION_30_31 = object : Migration(30, 31) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Create the new table
                    database.execSQL("""
            CREATE TABLE IF NOT EXISTS form_schema (
                formId TEXT NOT NULL PRIMARY KEY,
                formName TEXT NOT NULL,
                version INTEGER NOT NULL DEFAULT 1,
                schemaJson TEXT NOT NULL
            )
        """.trimIndent())
                }
            }


            val MIGRATION_29_30 = object : Migration(29, 30) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary1 TEXT")
                    database.execSQL("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary2 TEXT")
                }
            }


            val MIGRATION_28_29 = object : Migration(28, 29) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE PMSMA ADD COLUMN visitDate INTEGER")
                    database.execSQL("ALTER TABLE PMSMA ADD COLUMN visitNumber INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE PMSMA ADD COLUMN anyOtherHighRiskCondition TEXT")
                }
            }

            val MIGRATION_27_28 = object  : Migration(27,28)
            {
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
                        database.execSQL("ALTER TABLE PNC_VISIT ADD COLUMN sterilisationDate INTEGER ")                    }
                }

            }

            val MIGRATION_26_27 = object  : Migration(26,27)
            {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE INCENTIVE_ACTIVITY ADD COLUMN groupName TEXT  NOT NULL DEFAULT ''")                }
            }
            val MIGRATION_25_26 = object : Migration(25, 26) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN lmpDate INTEGER")
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN visitDate INTEGER")
                    database.execSQL("ALTER TABLE PREGNANCY_ANC ADD COLUMN weekOfPregnancy INTEGER")
                }
            }


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
                }
            }


            val MIGRATION_21_22 = object : Migration(21, 22) {
                override fun migrate(database: SupportSQLiteDatabase) {
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



            val MIGRATION_20_21 = object : Migration(20, 21) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE CDR ADD COLUMN cdr1File TEXT")
                    database.execSQL("ALTER TABLE CDR ADD COLUMN cdr2File TEXT")
                    database.execSQL("ALTER TABLE CDR ADD COLUMN cdrDeathCertFile TEXT")
                }
            }
            val MIGRATION_19_20 = object : Migration(19, 20) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
                    db.execSQL("CREATE VIEW `BEN_BASIC_CACHE` AS " +
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
                            "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC")
                }
            }
            val MIGRATION_18_19 = object : Migration(18, 19) {
                override fun migrate(database: SupportSQLiteDatabase) {
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

                }
            }

            val MIGRATION_17_18 = object : Migration(17, 18) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // 1. Create new table with updated schema
                    database.execSQL("""
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
        """.trimIndent())
                    // 2. Copy existing data into new table (with default/placeholder values for new fields)
                    try {
                        database.execSQL("""
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
            """.trimIndent())
                    } catch (e: Exception) {
                        // Table might not exist on some devices — log and continue
                        Log.w("RoomMigration", "Skipping data copy: ABHA_GENERATED table not found", e)
                    }

                    // 3. Drop old table
                    try {
                        database.execSQL("DROP TABLE IF EXISTS ABHA_GENERATED")
                    } catch (_: Exception) {}

                    // 4. Rename new table
                    database.execSQL("ALTER TABLE ABHA_GENERATED_NEW RENAME TO ABHA_GENERATED")

                    // 5. Recreate index
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ABHA_GENERATED_beneficiaryID` ON `ABHA_GENERATED` (`beneficiaryID`)")
                }
            }

            val MIGRATION_15_16 = object : Migration(15, 16) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Step 2: Drop the old view to ensure a clean slate.
                    db.execSQL("DROP VIEW IF EXISTS BEN_BASIC_CACHE")

                    // Step 3: Create the new view with a clean, correct, and complete SQL query.
                    db.execSQL("CREATE VIEW `BEN_BASIC_CACHE` AS " +
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
                            "WHERE b.isDraft = 0 GROUP BY b.beneficiaryId ORDER BY b.updatedDate DESC")
                }
            }

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
                    ).addMigrations(
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
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
                    ).build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}
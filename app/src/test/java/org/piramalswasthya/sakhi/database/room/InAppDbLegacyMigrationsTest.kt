package org.piramalswasthya.sakhi.database.room

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InAppDbLegacyMigrationsTest {

    private val executedSql = mutableListOf<String>()

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun invokeMigration(name: String, db: SupportSQLiteDatabase) {
        val clazz = Class.forName(
            "org.piramalswasthya.sakhi.database.room.InAppDb\$Companion\$getInstance\$$name\$1"
        )
        val constructor = clazz.declaredConstructors.first()
        constructor.isAccessible = true
        val instance = constructor.newInstance()
        val method = clazz.declaredMethods.first {
            (it.name == "migrate" || it.name == "invoke") &&
                    it.parameterTypes.size == 1 &&
                    it.parameterTypes[0] == SupportSQLiteDatabase::class.java
        }
        method.isAccessible = true
        method.invoke(instance, db)
    }

    private fun masterCursor(exists: Boolean): Cursor {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.count } returns if (exists) 1 else 0
        every { cursor.moveToFirst() } returns exists
        every { cursor.moveToNext() } returns false
        return cursor
    }

    private fun columnCursor(columns: List<String>): Cursor {
        val cursor = mockk<Cursor>(relaxed = true)
        var index = -1
        every { cursor.moveToNext() } answers {
            index += 1
            index < columns.size
        }
        every { cursor.getColumnIndexOrThrow("name") } returns 0
        every { cursor.getString(0) } answers { columns[index] }
        every { cursor.getString(1) } answers { columns[index] }
        return cursor
    }

    private fun mockDb(
        tableExists: Boolean = false,
        columns: List<String> = emptyList()
    ): SupportSQLiteDatabase {
        executedSql.clear()
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        every { db.execSQL(any<String>()) } answers {
            executedSql.add(firstArg<String>())
            Unit
        }
        every { db.query(any<String>()) } answers {
            val sql = firstArg<String>()
            if (sql.startsWith("PRAGMA")) columnCursor(columns) else masterCursor(tableExists)
        }
        every { db.query(any<String>(), any<Array<Any?>>()) } answers { masterCursor(tableExists) }
        return db
    }

    private fun mockAndroidLog() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.w(any<String>(), any<String>(), any<Throwable>()) } returns 0
    }

    private fun assertExecuted(fragment: String) {
        assertTrue(fragment, executedSql.any { it.contains(fragment) })
    }

    private fun assertNotExecuted(fragment: String) {
        assertTrue(fragment, executedSql.none { it.contains(fragment) })
    }

    @Test
    fun `MIGRATION_32_33 creates the hbyc visit history table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_32_33", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS ALL_VISIT_HISTORY_HBYC")
        assertExecuted("index_all_visit_history_hbyc_unique")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_31_32 creates the children under five visit table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_31_32", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS `children_under_five_all_visit`")
        assertExecuted("`index_children_under_five_all_visit_benId_hhId_visitDate_formId`")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_30_31 patches cbac immunization and recreates the uwin session table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_30_31", db)

        assertExecuted("ALTER TABLE CBAC ADD COLUMN isReffered INTEGER DEFAULT 0")
        assertExecuted("CREATE TABLE IF NOT EXISTS form_schema")
        assertExecuted("ALTER TABLE IMMUNIZATION ADD COLUMN mcpCardSummary1 TEXT")
        assertExecuted("ALTER TABLE IMMUNIZATION ADD COLUMN mcpCardSummary2 TEXT")
        assertExecuted("DROP TABLE IF EXISTS `UWIN_SESSION`")
        assertExecuted("CREATE TABLE IF NOT EXISTS `UWIN_SESSION`")
        assertEquals(6, executedSql.size)
    }

    @Test
    fun `MIGRATION_29_30 patches eligible couple tables and creates maa meeting`() {
        val db = mockDb()

        invokeMigration("MIGRATION_29_30", db)

        assertExecuted("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary1 TEXT")
        assertExecuted("ALTER TABLE eligible_couple_tracking ADD COLUMN dischargeSummary2 TEXT")
        assertExecuted("CREATE TABLE IF NOT EXISTS `MAA_MEETING`")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN isKitHandedOver INTEGER")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitHandedOverDate INTEGER")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitPhoto1 TEXT")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN kitPhoto2 TEXT")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN lmpDate INTEGER NOT NULL DEFAULT 0")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_REG ADD COLUMN lmp_date INTEGER NOT NULL DEFAULT 0")
        assertEquals(9, executedSql.size)
    }

    @Test
    fun `MIGRATION_28_29 adds the pmsma visit columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_28_29", db)

        assertExecuted("ALTER TABLE PMSMA ADD COLUMN visitDate INTEGER")
        assertExecuted("ALTER TABLE PMSMA ADD COLUMN visitNumber INTEGER NOT NULL DEFAULT 0")
        assertExecuted("ALTER TABLE PMSMA ADD COLUMN anyOtherHighRiskCondition TEXT")
        assertEquals(3, executedSql.size)
    }

    @Test
    fun `MIGRATION_27_28 adds the missing pnc visit columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_27_28", db)

        assertExecuted("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary1 TEXT")
        assertExecuted("ALTER TABLE PNC_VISIT ADD COLUMN deliveryDischargeSummary4 TEXT")
        assertExecuted("ALTER TABLE PNC_VISIT ADD COLUMN sterilisationDate INTEGER")
        assertExecuted("ALTER TABLE PNC_VISIT ADD COLUMN anyDangerSign TEXT")
        assertEquals(6, executedSql.size)
    }

    @Test
    fun `MIGRATION_27_28 skips pnc visit columns that already exist`() {
        val db = mockDb(
            columns = listOf(
                "deliveryDischargeSummary1",
                "deliveryDischargeSummary2",
                "deliveryDischargeSummary3",
                "deliveryDischargeSummary4",
                "sterilisationDate",
                "anyDangerSign"
            )
        )

        invokeMigration("MIGRATION_27_28", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_26_27 adds the incentive activity group name`() {
        val db = mockDb()

        invokeMigration("MIGRATION_26_27", db)

        assertExecuted("ALTER TABLE INCENTIVE_ACTIVITY ADD COLUMN groupName TEXT NOT NULL DEFAULT 'undefined'")
        assertEquals(1, executedSql.size)
    }

    @Test
    fun `MIGRATION_25_26 adds the pregnancy anc termination columns`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_25_26", db)

        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN lmpDate INTEGER")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN visitDate INTEGER")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN weekOfPregnancy INTEGER")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfDeath TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfDeathId INTEGER")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN otherPlaceOfDeath TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN methodOfTermination TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN methodOfTerminationId INTEGER")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN terminationDoneBy TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN terminationDoneById INTEGER")
        assertEquals(10, executedSql.size)
    }

    @Test
    fun `MIGRATION_25_26 does nothing when pregnancy anc is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_25_26", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_24_25 adds the antra injection columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_24_25", db)

        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN dateOfAntraInjection TEXT")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN dueDateOfAntraInjection TEXT")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN mpaFile TEXT")
        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_TRACKING ADD COLUMN antraDose TEXT")
        assertEquals(6, executedSql.size)
    }

    @Test
    fun `MIGRATION_23_24 adds the mdsr file columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_23_24", db)

        assertExecuted("ALTER TABLE MDSR ADD COLUMN mdsr1File TEXT")
        assertExecuted("ALTER TABLE MDSR ADD COLUMN mdsr2File TEXT")
        assertExecuted("ALTER TABLE MDSR ADD COLUMN mdsrDeathCertFile TEXT")
        assertEquals(3, executedSql.size)
    }

    @Test
    fun `MIGRATION_22_23 adds the pnc and general opd columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_22_23", db)

        assertExecuted("ALTER TABLE PNC_VISIT ADD COLUMN otherPlaceOfDeath TEXT")
        assertExecuted("ALTER TABLE GENERAL_OPD_ACTIVITY ADD COLUMN village TEXT")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_21_22 rebuilds pregnancy anc and patches delivery outcome`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_21_22", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS PREGNANCY_ANC_NEW")
        assertExecuted("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        assertExecuted("INSERT INTO PREGNANCY_ANC_NEW")
        assertExecuted("DROP TABLE PREGNANCY_ANC")
        assertExecuted("ALTER TABLE PREGNANCY_ANC_NEW RENAME TO PREGNANCY_ANC")
        assertExecuted("CREATE INDEX IF NOT EXISTS ind_mha ON PREGNANCY_ANC(benId)")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeath INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN isDeathValue TEXT")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN dateOfDeath TEXT")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeath TEXT")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN placeOfDeathId INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN otherPlaceOfDeath TEXT")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp1File TEXT")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN mcp2File TEXT")
        assertExecuted("ALTER TABLE DELIVERY_OUTCOME ADD COLUMN jsyFile TEXT")
        assertEquals(15, executedSql.size)
    }

    @Test
    fun `MIGRATION_21_22 skips the copy when pregnancy anc is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_21_22", db)

        assertNotExecuted("INSERT INTO PREGNANCY_ANC_NEW")
        assertExecuted("ALTER TABLE PREGNANCY_ANC_NEW RENAME TO PREGNANCY_ANC")
        assertEquals(4, executedSql.size)
    }

    @Test
    fun `MIGRATION_20_21 adds cdr files and creates the general opd table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_20_21", db)

        assertExecuted("ALTER TABLE CDR ADD COLUMN cdr1File TEXT")
        assertExecuted("ALTER TABLE CDR ADD COLUMN cdr2File TEXT")
        assertExecuted("ALTER TABLE CDR ADD COLUMN cdrDeathCertFile TEXT")
        assertExecuted("CREATE TABLE IF NOT EXISTS `GENERAL_OPD_ACTIVITY`")
        assertEquals(4, executedSql.size)
    }

    @Test
    fun `MIGRATION_19_20 recreates the basic cache view and patches malaria screening`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_19_20", db)

        assertExecuted("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        assertExecuted("CREATE VIEW `BEN_BASIC_CACHE`")
        assertExecuted("ALTER TABLE MALARIA_SCREENING ADD COLUMN slideTestName TEXT")
        assertEquals(3, executedSql.size)
    }

    @Test
    fun `MIGRATION_19_20 skips malaria screening when the table is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_19_20", db)

        assertNotExecuted("slideTestName")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_18_19 rebuilds the profile table and backfills beneficiary columns`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_18_19", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS PROFILE_ACTIVITY_new")
        assertExecuted("INSERT INTO PROFILE_ACTIVITY_new")
        assertExecuted("DROP TABLE IF EXISTS PROFILE_ACTIVITY")
        assertExecuted("ALTER TABLE PROFILE_ACTIVITY_new RENAME TO PROFILE_ACTIVITY")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isDeath INTEGER NOT NULL DEFAULT 0")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isConsent INTEGER NOT NULL DEFAULT 0")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN kid_isConsent INTEGER")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN tempMobileNoOfRelationId INTEGER NOT NULL DEFAULT 'undefined'")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN serialNo TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN abortionImg2 TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN otherPlaceOfDeath TEXT")
    }

    @Test
    fun `MIGRATION_18_19 only recreates the profile table when nothing else exists`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_18_19", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS PROFILE_ACTIVITY_new")
        assertNotExecuted("INSERT INTO PROFILE_ACTIVITY_new")
        assertNotExecuted("ALTER TABLE BENEFICIARY")
        assertNotExecuted("ALTER TABLE PREGNANCY_ANC")
        assertEquals(3, executedSql.size)
    }

    @Test
    fun `MIGRATION_18_19 skips beneficiary columns that already exist`() {
        val db = mockDb(tableExists = true, columns = listOf("isDeath", "isConsent"))

        invokeMigration("MIGRATION_18_19", db)

        assertNotExecuted("ADD COLUMN isDeath INTEGER")
        assertNotExecuted("ADD COLUMN isConsent INTEGER")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isDeathValue TEXT")
    }

    @Test
    fun `MIGRATION_17_18 rebuilds the abha generated table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_17_18", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS `ABHA_GENERATED_NEW`")
        assertExecuted("INSERT INTO ABHA_GENERATED_NEW")
        assertExecuted("DROP TABLE IF EXISTS ABHA_GENERATED")
        assertExecuted("ALTER TABLE ABHA_GENERATED_NEW RENAME TO ABHA_GENERATED")
        assertExecuted("`index_ABHA_GENERATED_beneficiaryID`")
        assertEquals(5, executedSql.size)
    }

    @Test
    fun `MIGRATION_17_18 logs and continues when the legacy abha table is missing`() {
        mockAndroidLog()
        val db = mockDb()
        every {
            db.execSQL(match<String> { it.contains("INSERT INTO ABHA_GENERATED_NEW") })
        } throws RuntimeException("no such table")

        invokeMigration("MIGRATION_17_18", db)

        verify { android.util.Log.w("RoomMigration", any<String>(), any<Throwable>()) }
        assertExecuted("ALTER TABLE ABHA_GENERATED_NEW RENAME TO ABHA_GENERATED")
        assertEquals(4, executedSql.size)
    }

    @Test
    fun `MIGRATION_16_18 rebuilds the abha generated table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_16_18", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS `ABHA_GENERATED_NEW`")
        assertExecuted("INSERT INTO ABHA_GENERATED_NEW")
        assertExecuted("DROP TABLE IF EXISTS ABHA_GENERATED")
        assertExecuted("ALTER TABLE ABHA_GENERATED_NEW RENAME TO ABHA_GENERATED")
        assertExecuted("`index_ABHA_GENERATED_beneficiaryID`")
        assertEquals(5, executedSql.size)
    }

    @Test
    fun `MIGRATION_16_18 logs and continues when the legacy abha table is missing`() {
        mockAndroidLog()
        val db = mockDb()
        every {
            db.execSQL(match<String> { it.contains("INSERT INTO ABHA_GENERATED_NEW") })
        } throws RuntimeException("no such table")

        invokeMigration("MIGRATION_16_18", db)

        verify { android.util.Log.w("RoomMigration", any<String>(), any<Throwable>()) }
        assertEquals(4, executedSql.size)
    }

    @Test
    fun `MIGRATION_15_16 recreates the basic cache view`() {
        val db = mockDb()

        invokeMigration("MIGRATION_15_16", db)

        assertExecuted("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        assertExecuted("CREATE VIEW `BEN_BASIC_CACHE`")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_14_15 adds the new abha flag and rebuilds the view`() {
        val db = mockDb()

        invokeMigration("MIGRATION_14_15", db)

        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isNewAbha INTEGER NOT NULL DEFAULT 0")
        assertExecuted("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        assertExecuted("CREATE VIEW BEN_BASIC_CACHE AS")
        assertEquals(3, executedSql.size)
    }

    @Test
    fun `MIGRATION_13_14 adds the incentive and hrp tracking columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_13_14", db)

        assertExecuted("alter table INCENTIVE_ACTIVITY add column fmrCode TEXT")
        assertExecuted("alter table INCENTIVE_ACTIVITY add column fmrCodeOld TEXT")
        assertExecuted("alter table HRP_NON_PREGNANT_TRACK add column systolic INTEGER")
        assertExecuted("alter table HRP_NON_PREGNANT_TRACK add column ifaQuantity INTEGER")
        assertExecuted("alter table HRP_PREGNANT_TRACK add column fastingOgtt INTEGER")
        assertExecuted("alter table HRP_PREGNANT_TRACK add column after2hrsOgtt INTEGER")
        assertEquals(22, executedSql.size)
    }

    @Test
    fun `MIGRATION_1_2 adds the consent columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_1_2", db)

        assertExecuted("alter table BEN_BASIC_CACHE add column isConsent BOOL")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN newColumn TEXT DEFAULT 'undefined'")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN kid_isConsent INTEGER DEFAULT 0")
        assertEquals(3, executedSql.size)
    }
}

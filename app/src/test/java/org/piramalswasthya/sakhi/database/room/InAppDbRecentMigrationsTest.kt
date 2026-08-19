package org.piramalswasthya.sakhi.database.room

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InAppDbRecentMigrationsTest {

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

    private fun assertExecuted(fragment: String) {
        assertTrue(fragment, executedSql.any { it.contains(fragment) })
    }

    private fun assertNotExecuted(fragment: String) {
        assertTrue(fragment, executedSql.none { it.contains(fragment) })
    }

    @Test
    fun `MIGRATION_63_64 adds sanitary napkin column to adolescent health`() {
        val db = mockDb()

        invokeMigration("MIGRATION_63_64", db)

        assertExecuted("ALTER TABLE Adolescent_Health_Form_Data")
        assertExecuted("isSanitaryNapkinUsed")
    }

    @Test
    fun `MIGRATION_62_63 adds notification columns when they are missing`() {
        val db = mockDb()

        invokeMigration("MIGRATION_62_63", db)

        assertExecuted("ALTER TABLE NOTIFICATION ADD COLUMN appType TEXT")
        assertExecuted("ALTER TABLE NOTIFICATION ADD COLUMN redirect TEXT")
        assertExecuted("ALTER TABLE NOTIFICATION ADD COLUMN readDate TEXT")
    }

    @Test
    fun `MIGRATION_62_63 skips notification columns that already exist`() {
        val db = mockDb(columns = listOf("appType", "redirect", "readDate"))

        invokeMigration("MIGRATION_62_63", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_62_63 swallows failures from sqlite`() {
        val db = mockDb()
        every { db.execSQL(any<String>()) } throws RuntimeException("boom")

        invokeMigration("MIGRATION_62_63", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_61_62 creates the notification table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_61_62", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS `NOTIFICATION`")
        assertExecuted("`notificationId` INTEGER NOT NULL")
        assertExecuted("PRIMARY KEY(`notificationId`)")
    }

    @Test
    fun `MIGRATION_61_62 swallows failures from sqlite`() {
        val db = mockDb()
        every { db.execSQL(any<String>()) } throws RuntimeException("boom")

        invokeMigration("MIGRATION_61_62", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_60_61 normalizes isDeath when the beneficiary table exists`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_60_61", db)

        assertEquals(1, executedSql.size)
        assertEquals(InAppDb.MIGRATION_60_61_NORMALIZE_ISDEATH_SQL, executedSql.first())
    }

    @Test
    fun `MIGRATION_60_61 does nothing when the beneficiary table is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_60_61", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_59_60 adds the abha family id column`() {
        val db = mockDb()

        invokeMigration("MIGRATION_59_60", db)

        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN abha_familyId TEXT")
    }

    @Test
    fun `MIGRATION_58_59 adds bangla location columns to household and beneficiary`() {
        val db = mockDb()

        invokeMigration("MIGRATION_58_59", db)

        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN loc_country_nameBangla TEXT")
        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN loc_state_nameBangla TEXT")
        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN loc_district_nameBangla TEXT")
        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN loc_block_nameBangla TEXT")
        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN loc_village_nameBangla TEXT")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN loc_district_nameBangla TEXT")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN loc_village_nameBangla TEXT")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN loc_country_nameBangla TEXT")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN loc_block_nameBangla TEXT")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN loc_state_nameBangla TEXT")
        assertEquals(10, executedSql.size)
    }

    @Test
    fun `MIGRATION_57_58 extends incentive record and rebuilds eye surgery history`() {
        val db = mockDb()

        invokeMigration("MIGRATION_57_58", db)

        assertExecuted("ALTER TABLE INCENTIVE_RECORD ADD COLUMN verifiedByUserName")
        assertExecuted("ALTER TABLE INCENTIVE_RECORD ADD COLUMN approvalStatus")
        assertExecuted("ALTER TABLE INCENTIVE_RECORD ADD COLUMN supervisorRole")
        assertExecuted("ALTER TABLE ALL_EYE_SURGERY_VISIT_HISTORY ADD COLUMN eyeSide TEXT")
        assertExecuted("DROP INDEX IF EXISTS index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_formId_visitMonth")
        assertExecuted("CREATE TABLE ALL_EYE_SURGERY_VISIT_HISTORY_NEW")
        assertExecuted("INSERT INTO ALL_EYE_SURGERY_VISIT_HISTORY_NEW")
        assertExecuted("DROP TABLE ALL_EYE_SURGERY_VISIT_HISTORY")
        assertExecuted("RENAME TO ALL_EYE_SURGERY_VISIT_HISTORY")
        assertExecuted("CREATE UNIQUE INDEX index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_formId_eyeSide")
        assertExecuted("CREATE INDEX index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_visitDate")
    }

    @Test
    fun `MIGRATION_56_57 creates fresh tables when nothing exists yet`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_56_57", db)

        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN loc_country_id INTEGER NOT NULL DEFAULT 0")
        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN loc_village_nameAssamese TEXT")
        assertExecuted("ALTER TABLE HOUSEHOLD ADD COLUMN isDeactivate")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isSpouseAdded")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isChildrenAdded")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isMarried")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN noOfChildren")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN noOfAliveChildren")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN doYouHavechildren")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isDeactivate")
        assertExecuted("UPDATE BENEFICIARY")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfAnc TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfAncId INTEGER")
        assertExecuted("CREATE TABLE IF NOT EXISTS `PulsePolioCampaign`")
        assertExecuted("CREATE TABLE IF NOT EXISTS `ORSCampaign`")
        assertExecuted("CREATE TABLE IF NOT EXISTS `TB_CONFIRMED_TREATMENT`")
        assertExecuted("`ind_tb_confirmed`")
        assertExecuted("CREATE TABLE IF NOT EXISTS `ALL_VISIT_HISTORY_ANC`")
        assertExecuted("ALTER TABLE TB_SCREENING ADD COLUMN riseOfFever INTEGER")
        assertExecuted("ALTER TABLE TB_SCREENING ADD COLUMN recommandateTest TEXT")
        assertExecuted("CREATE TABLE IF NOT EXISTS `VHNC`")
        assertExecuted("DROP INDEX IF EXISTS `ind_refcache`")
        assertExecuted("CREATE UNIQUE INDEX IF NOT EXISTS `ind_refcache`")
        assertExecuted("CREATE TABLE IF NOT EXISTS `FILARIA_MDA_CAMPAIGN_HISTORY`")
        assertExecuted("DROP VIEW IF EXISTS `BEN_BASIC_CACHE`")
        assertExecuted("CREATE VIEW `BEN_BASIC_CACHE`")
        assertNotExecuted("VHNC_new")
        assertNotExecuted("FILARIA_MDA_CAMPAIGN_HISTORY_new")
    }

    @Test
    fun `MIGRATION_56_57 rebuilds legacy tables when they already exist`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_56_57", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS `VHNC_new`")
        assertExecuted("INSERT INTO `VHNC_new`")
        assertExecuted("DROP TABLE `VHNC`")
        assertExecuted("ALTER TABLE `VHNC_new` RENAME TO `VHNC`")
        assertExecuted("DROP INDEX IF EXISTS `index_ncd_visit_ben_hh`")
        assertExecuted("CREATE UNIQUE INDEX IF NOT EXISTS `index_ncd_referal_all_visit_benId_hhId_visitNo_followUpNo`")
        assertExecuted("ALTER TABLE MAA_MEETING ADD COLUMN villageName TEXT")
        assertExecuted("ALTER TABLE MAA_MEETING ADD COLUMN mitaninActivityCheckList TEXT")
        assertExecuted("ALTER TABLE MAA_MEETING ADD COLUMN noOfPragnentWomen TEXT")
        assertExecuted("ALTER TABLE MAA_MEETING ADD COLUMN noOfLactingMother TEXT")
        assertExecuted("CREATE TABLE IF NOT EXISTS `FILARIA_MDA_CAMPAIGN_HISTORY_new`")
        assertExecuted("INSERT OR IGNORE INTO `FILARIA_MDA_CAMPAIGN_HISTORY_new`")
        assertExecuted("DROP TABLE `FILARIA_MDA_CAMPAIGN_HISTORY`")
        assertExecuted("ALTER TABLE `FILARIA_MDA_CAMPAIGN_HISTORY_new` RENAME TO `FILARIA_MDA_CAMPAIGN_HISTORY`")
    }

    @Test
    fun `MIGRATION_56_57 copies vhnc data from correctly named columns`() {
        val db = mockDb(
            tableExists = true,
            columns = listOf(
                "noOfPragnentWoment",
                "noOfLactingMother",
                "followupPrevius",
                "villageName",
                "anm",
                "aww",
                "noOfCommittee"
            )
        )

        invokeMigration("MIGRATION_56_57", db)

        val insert = executedSql.first { it.contains("INSERT INTO `VHNC_new`") }
        assertTrue(insert.contains("`noOfPragnentWoment`,"))
        assertTrue(insert.contains("`noOfLactingMother`,"))
        assertTrue(insert.contains("`followupPrevius`,"))
        assertTrue(insert.contains("`villageName`,"))
        assertTrue(insert.contains("`anm`,"))
        assertTrue(insert.contains("`aww`,"))
        assertNotExecuted("ALTER TABLE MAA_MEETING ADD COLUMN villageName TEXT")
    }

    @Test
    fun `MIGRATION_56_57 falls back to wrongly named vhnc columns`() {
        val db = mockDb(
            tableExists = true,
            columns = listOf("noOfPregnantWomen", "noOfLactatingMother", "followupPrevious")
        )

        invokeMigration("MIGRATION_56_57", db)

        val insert = executedSql.first { it.contains("INSERT INTO `VHNC_new`") }
        assertTrue(insert.contains("`noOfPregnantWomen`,"))
        assertTrue(insert.contains("`noOfLactatingMother`,"))
        assertTrue(insert.contains("`followupPrevious`,"))
    }

    @Test
    fun `MIGRATION_55_56 adds the incentive eligibility column when missing`() {
        val db = mockDb()

        invokeMigration("MIGRATION_55_56", db)

        assertExecuted("ALTER TABLE INCENTIVE_RECORD")
        assertExecuted("isEligible INTEGER NOT NULL DEFAULT 0")
    }

    @Test
    fun `MIGRATION_55_56 skips the incentive eligibility column when present`() {
        val db = mockDb(columns = listOf("isEligible"))

        invokeMigration("MIGRATION_55_56", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_54_55 adds the sterilisation date to eligible couple tracking`() {
        val db = mockDb()

        invokeMigration("MIGRATION_54_55", db)

        assertExecuted("ALTER TABLE ELIGIBLE_COUPLE_TRACKING")
        assertExecuted("dateOfSterilisation INTEGER NOT NULL DEFAULT 0")
    }

    @Test
    fun `MIGRATION_53_54 adds the tb suspected columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_53_54", db)

        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN visitLabel TEXT")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN typeOfTBCase TEXT")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN reasonForSuspicion TEXT")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN hasSymptoms INTEGER NOT NULL DEFAULT 0")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN isChestXRayDone INTEGER")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN chestXRayResult TEXT")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN referralFacility TEXT")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN isTBConfirmed INTEGER")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN isDRTBConfirmed INTEGER")
        assertExecuted("ALTER TABLE TB_SUSPECTED ADD COLUMN isConfirmed INTEGER NOT NULL DEFAULT 0")
        assertEquals(10, executedSql.size)
    }

    @Test
    fun `MIGRATION_52_53 creates the filaria campaign history table and indexes`() {
        val db = mockDb()

        invokeMigration("MIGRATION_52_53", db)

        assertExecuted("index_DewormingMeeting_dewormingDate")
        assertExecuted("CREATE TABLE IF NOT EXISTS FILARIA_MDA_CAMPAIGN_HISTORY")
        assertExecuted("index_FILARIA_MDA_CAMPAIGN_HISTORY_formId_visitYear")
        assertExecuted("index_FILARIA_MDA_CAMPAIGN_HISTORY_visitDate")
        assertEquals(4, executedSql.size)
    }

    @Test
    fun `MIGRATION_51_52 creates the maa meeting table and index`() {
        val db = mockDb()

        invokeMigration("MIGRATION_51_52", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS MAA_MEETING")
        assertExecuted("mitaninActivityCheckList TEXT")
        assertExecuted("CREATE UNIQUE INDEX IF NOT EXISTS index_MAA_MEETING_id ON MAA_MEETING(id)")
        assertEquals(2, executedSql.size)
    }
}

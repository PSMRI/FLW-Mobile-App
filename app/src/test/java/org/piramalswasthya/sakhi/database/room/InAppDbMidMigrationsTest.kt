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

class InAppDbMidMigrationsTest {

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
    fun `MIGRATION_50_51 extends the phc review meeting table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_50_51", db)

        assertExecuted("alter table PHCReviewMeeting add column villageName TEXT")
        assertExecuted("alter table PHCReviewMeeting add column mitaninHistory TEXT")
        assertExecuted("alter table PHCReviewMeeting add column mitaninActivityCheckList TEXT")
        assertExecuted("alter table PHCReviewMeeting add column placeId INTEGER DEFAULT 0")
        assertExecuted("index_PHCReviewMeeting_id")
        assertEquals(5, executedSql.size)
    }

    @Test
    fun `MIGRATION_49_50 extends vhnd and recreates the ncd referral visit table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_49_50", db)

        assertExecuted("ALTER TABLE VHND ADD COLUMN vhndPlaceId INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE VHND ADD COLUMN pregnantWomenAnc TEXT")
        assertExecuted("ALTER TABLE VHND ADD COLUMN selectAllEducation INTEGER DEFAULT 0")
        assertExecuted("DROP TABLE IF EXISTS ncd_referal_all_visit")
        assertExecuted("CREATE TABLE IF NOT EXISTS `ncd_referal_all_visit`")
        assertExecuted("`index_ncd_visit_ben_hh`")
        assertExecuted("`index_ncd_visit_followup`")
    }

    @Test
    fun `MIGRATION_48_49 adds the vhnc meeting columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_48_49", db)

        assertExecuted("ALTER TABLE VHNC ADD COLUMN villageName TEXT")
        assertExecuted("ALTER TABLE VHNC ADD COLUMN anm INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE VHNC ADD COLUMN aww INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE VHNC ADD COLUMN noOfPregnantWomen INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE VHNC ADD COLUMN noOfLactatingMother INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE VHNC ADD COLUMN noOfCommittee INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE VHNC ADD COLUMN followupPrevious INTEGER")
        assertEquals(7, executedSql.size)
    }

    @Test
    fun `MIGRATION_47_48 adds the leprosy symptom columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_47_48", db)

        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentUlcerationId INTEGER DEFAULT 1")
        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN weaknessFeetId INTEGER DEFAULT 1")
        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN recurrentUlceration TEXT")
        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN weaknessFeet TEXT")
        assertEquals(24, executedSql.size)
    }

    @Test
    fun `MIGRATION_46_47 adds the referral type column`() {
        val db = mockDb()

        invokeMigration("MIGRATION_46_47", db)

        assertExecuted("ALTER TABLE NCD_REFER")
        assertExecuted("ADD COLUMN type TEXT")
        assertEquals(1, executedSql.size)
    }

    @Test
    fun `MIGRATION_45_46 recreates the basic cache view and patches pregnancy anc`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_45_46", db)

        assertExecuted("DROP VIEW IF EXISTS `BEN_BASIC_CACHE`")
        assertExecuted("CREATE VIEW `BEN_BASIC_CACHE`")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfAnc TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN placeOfAncId INTEGER")
    }

    @Test
    fun `MIGRATION_45_46 skips pregnancy anc when the table is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_45_46", db)

        assertExecuted("CREATE VIEW `BEN_BASIC_CACHE`")
        assertNotExecuted("placeOfAnc")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_45_46 skips pregnancy anc columns that already exist`() {
        val db = mockDb(tableExists = true, columns = listOf("placeOfAnc", "placeOfAncId"))

        invokeMigration("MIGRATION_45_46", db)

        assertNotExecuted("ADD COLUMN placeOfAnc")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_44_45 creates the village activity tables when none exist`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_44_45", db)

        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN isSpouseAdded")
        assertExecuted("ALTER TABLE BENEFICIARY ADD COLUMN doYouHavechildren")
        assertExecuted("DROP VIEW IF EXISTS BEN_BASIC_CACHE")
        assertExecuted("CREATE VIEW `BEN_BASIC_CACHE`")
        assertExecuted("UPDATE BENEFICIARY")
        assertExecuted("CREATE TABLE IF NOT EXISTS PHCReviewMeeting (")
        assertExecuted("CREATE TABLE IF NOT EXISTS VHND (")
        assertExecuted("CREATE TABLE IF NOT EXISTS VHNC (")
        assertExecuted("CREATE TABLE IF NOT EXISTS AHDMeeting (")
        assertExecuted("CREATE TABLE IF NOT EXISTS DewormingMeeting (")
        assertExecuted("CREATE TABLE IF NOT EXISTS NCD_REFER (")
        assertExecuted("ind_refcache")
        assertExecuted("CREATE TABLE IF NOT EXISTS MALARIA_SCREENING")
        assertExecuted("CREATE TABLE IF NOT EXISTS AES_SCREENING")
        assertExecuted("CREATE TABLE IF NOT EXISTS KALAZAR_SCREENING")
        assertExecuted("CREATE TABLE IF NOT EXISTS FILARIA_SCREENING")
        assertExecuted("CREATE TABLE IF NOT EXISTS MALARIA_CONFIRMED")
        assertExecuted("CREATE TABLE IF NOT EXISTS IRS_ROUND")
        assertExecuted("CREATE TABLE IF NOT EXISTS Adolescent_Health_Form_Data")
        assertExecuted("CREATE TABLE IF NOT EXISTS infant")
        assertExecuted("CREATE TABLE IF NOT EXISTS SAAS_BAHU_ACTIVITY")
        assertNotExecuted("_temp")
    }

    @Test
    fun `MIGRATION_44_45 rebuilds existing tables that miss critical columns`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_44_45", db)

        assertExecuted("ALTER TABLE PHCReviewMeeting ADD COLUMN image2 TEXT")
        assertExecuted("ALTER TABLE PHCReviewMeeting ADD COLUMN syncState INTEGER NOT NULL DEFAULT 0")
        assertExecuted("CREATE TABLE IF NOT EXISTS PHCReviewMeeting_temp")
        assertExecuted("DROP TABLE PHCReviewMeeting")
        assertExecuted("ALTER TABLE PHCReviewMeeting_temp RENAME TO PHCReviewMeeting")
        assertExecuted("ALTER TABLE VHND_temp RENAME TO VHND")
        assertExecuted("ALTER TABLE AHDMeeting_temp RENAME TO AHDMeeting")
        assertExecuted("ALTER TABLE DewormingMeeting_temp RENAME TO DewormingMeeting")
        assertExecuted("ALTER TABLE NCD_REFER_temp RENAME TO NCD_REFER")
        assertNotExecuted("INSERT INTO PHCReviewMeeting_temp")
    }

    @Test
    fun `MIGRATION_44_45 copies shared columns into the rebuilt table`() {
        val db = mockDb(
            tableExists = true,
            columns = listOf(
                "id",
                "phcReviewDate",
                "place",
                "noOfBeneficiariesAttended",
                "image1",
                "image2",
                "syncState"
            )
        )

        invokeMigration("MIGRATION_44_45", db)

        assertExecuted("INSERT INTO VHND_temp")
        assertNotExecuted("ALTER TABLE PHCReviewMeeting ADD COLUMN image2 TEXT")
        assertNotExecuted("CREATE TABLE IF NOT EXISTS PHCReviewMeeting_temp")
    }

    @Test
    fun `MIGRATION_43_44 creates the leprosy screening and follow up tables`() {
        val db = mockDb()

        invokeMigration("MIGRATION_43_44", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS LEPROSY_SCREENING")
        assertExecuted("ind_leprosysn")
        assertExecuted("CREATE TABLE IF NOT EXISTS LEPROSY_FOLLOW_UP")
        assertExecuted("ind_leprosy_followup_ben")
        assertExecuted("ind_leprosy_followup_visit")
        assertEquals(5, executedSql.size)
    }

    @Test
    fun `MIGRATION_42_43 adds leprosy columns when the table exists`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_42_43", db)

        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN leprosySymptoms TEXT")
        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN visitLabel TEXT")
        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN isConfirmed INTEGER NOT NULL DEFAULT 0")
        assertExecuted("ALTER TABLE LEPROSY_SCREENING ADD COLUMN leprosyState TEXT")
        assertEquals(10, executedSql.size)
    }

    @Test
    fun `MIGRATION_42_43 returns early when the leprosy table is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_42_43", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_42_43 skips leprosy columns that already exist`() {
        val db = mockDb(tableExists = true, columns = listOf("leprosySymptoms", "visitLabel"))

        invokeMigration("MIGRATION_42_43", db)

        assertNotExecuted("ADD COLUMN leprosySymptoms TEXT")
        assertNotExecuted("ADD COLUMN visitLabel")
        assertExecuted("ADD COLUMN leprosySymptomsPosition")
        assertEquals(8, executedSql.size)
    }

    @Test
    fun `MIGRATION_41_42 adds the schema language and micro birth plan columns`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_41_42", db)

        assertExecuted("ALTER TABLE form_schema ADD COLUMN language TEXT NOT NULL DEFAULT 'en'")
        assertExecuted("ALTER TABLE HRP_MICRO_BIRTH_PLAN ADD COLUMN processed TEXT")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_41_42 only adds the schema language when the plan table is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_41_42", db)

        assertExecuted("ALTER TABLE form_schema ADD COLUMN language TEXT NOT NULL DEFAULT 'en'")
        assertEquals(1, executedSql.size)
    }

    @Test
    fun `MIGRATION_40_41 patches pregnancy anc when the table exists`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_40_41", db)

        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN isYesOrNo INTEGER")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN dateSterilisation INTEGER")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN isPaiucd TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN remarks TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN serialNo TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN abortionImg1 TEXT")
        assertExecuted("ALTER TABLE PREGNANCY_ANC ADD COLUMN abortionImg2 TEXT")
        assertNotExecuted("UPDATE PREGNANCY_ANC")
    }

    @Test
    fun `MIGRATION_40_41 normalizes the paiucd flag when the column exists`() {
        val db = mockDb(tableExists = true, columns = listOf("isPaiucdId"))

        invokeMigration("MIGRATION_40_41", db)

        assertExecuted("UPDATE PREGNANCY_ANC")
        assertExecuted("WHEN isPaiucdId = 1 THEN 1")
    }

    @Test
    fun `MIGRATION_40_41 does nothing when pregnancy anc is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_40_41", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_39_40 creates the ben ifa visit history table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_39_40", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS `ALL_BEN_IFA_VISIT_HISTORY`")
        assertExecuted("`index_ALL_BEN_IFA_VISIT_HISTORY_benId_hhId_visitDate_formId`")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_38_39 creates the filaria mda visit history table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_38_39", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS FILARIA_MDA_VISIT_HISTORY")
        assertExecuted("index_FILARIA_MDA_VISIT_HISTORY_hhId_formId_visitMonth")
        assertExecuted("index_FILARIA_MDA_VISIT_HISTORY_hhId_visitDate")
        assertEquals(3, executedSql.size)
    }

    @Test
    fun `MIGRATION_37_38 adds the infant discharge summary columns`() {
        val db = mockDb()

        invokeMigration("MIGRATION_37_38", db)

        assertExecuted("ALTER TABLE INFANT_REG ADD COLUMN isSNCU TEXT")
        assertExecuted("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary1 TEXT")
        assertExecuted("ALTER TABLE INFANT_REG ADD COLUMN deliveryDischargeSummary4 TEXT")
        assertEquals(5, executedSql.size)
    }

    @Test
    fun `MIGRATION_36_37 creates the mosquito net visit table`() {
        val db = mockDb()

        invokeMigration("MIGRATION_36_37", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS mosquito_net_visit")
        assertExecuted("index_mosquito_net_visit_unique")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_35_36 rebuilds eye surgery history and patches malaria screening`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_35_36", db)

        assertExecuted("ALTER TABLE ALL_EYE_SURGERY_VISIT_HISTORY RENAME TO temp_eye_history")
        assertExecuted("CREATE TABLE ALL_EYE_SURGERY_VISIT_HISTORY")
        assertExecuted("INSERT INTO ALL_EYE_SURGERY_VISIT_HISTORY")
        assertExecuted("DROP TABLE temp_eye_history")
        assertExecuted("index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_formId_visitMonth")
        assertExecuted("index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_visitDate")
        assertExecuted("DROP INDEX IF EXISTS ind_malariasn")
        assertExecuted("ALTER TABLE MALARIA_SCREENING ADD COLUMN visitId INTEGER NOT NULL DEFAULT 1")
        assertExecuted("CREATE UNIQUE INDEX IF NOT EXISTS ind_malariasn ON MALARIA_SCREENING(benId, visitId)")
    }

    @Test
    fun `MIGRATION_35_36 does nothing when neither table exists`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_35_36", db)

        assertEquals(0, executedSql.size)
    }

    @Test
    fun `MIGRATION_34_35 creates eye surgery history and patches malaria screening`() {
        val db = mockDb(tableExists = true)

        invokeMigration("MIGRATION_34_35", db)

        assertExecuted("CREATE TABLE ALL_EYE_SURGERY_VISIT_HISTORY")
        assertExecuted("index_ALL_EYE_SURGERY_VISIT_HISTORY_benId_hhId_visitDate_formId")
        assertExecuted("ALTER TABLE MALARIA_SCREENING ADD COLUMN visitId INTEGER NOT NULL DEFAULT 1")
        assertExecuted("ALTER TABLE MALARIA_SCREENING ADD COLUMN malariaTestType INTEGER DEFAULT 0")
        assertExecuted("ALTER TABLE MALARIA_SCREENING ADD COLUMN malariaSlideTestType INTEGER DEFAULT 0")
        assertExecuted("CREATE UNIQUE INDEX IF NOT EXISTS ind_malariasn ON MALARIA_SCREENING(benId, visitId)")
    }

    @Test
    fun `MIGRATION_34_35 only creates eye surgery history when malaria screening is absent`() {
        val db = mockDb(tableExists = false)

        invokeMigration("MIGRATION_34_35", db)

        assertExecuted("CREATE TABLE ALL_EYE_SURGERY_VISIT_HISTORY")
        assertNotExecuted("MALARIA_SCREENING")
        assertEquals(2, executedSql.size)
    }

    @Test
    fun `MIGRATION_33_34 creates the form schema and visit history tables`() {
        val db = mockDb()

        invokeMigration("MIGRATION_33_34", db)

        assertExecuted("CREATE TABLE IF NOT EXISTS form_schema")
        assertExecuted("CREATE TABLE IF NOT EXISTS all_visit_history")
        assertExecuted("index_all_visit_history_unique")
        assertEquals(3, executedSql.size)
    }
}

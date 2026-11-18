package org.piramalswasthya.sakhi.notifications

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FormSaveLogCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.testutil.JdbcSupportDb
import java.util.concurrent.TimeUnit

class FormSaveInterceptorTest {

    private lateinit var jdbc: JdbcSupportDb
    private val dao = mockk<EveningNotifDao>(relaxed = true)
    private val pref = mockk<PreferenceDao>()
    private lateinit var interceptor: FormSaveInterceptor

    private val now = System.currentTimeMillis()
    private val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)

    @Before
    fun setUp() {
        jdbc = JdbcSupportDb()
        val db = mockk<InAppDb>()
        every { db.openHelper.readableDatabase } returns jdbc.db
        val user = mockk<User>()
        every { user.userId } returns 42
        every { pref.getLoggedInUser() } returns user
        interceptor = FormSaveInterceptor(db, dao, pref)
    }

    @After
    fun tearDown() = jdbc.close()

    @Test
    fun `logs today's new non-draft records from included tables`() = runTest {
        jdbc.exec(
            // id + createdDate + draft flag
            "CREATE TABLE IMMUNIZATION (beneficiaryId INTEGER, createdDate INTEGER, isDraft INTEGER)",
            "INSERT INTO IMMUNIZATION VALUES (1, $now, 0)",
            "INSERT INTO IMMUNIZATION VALUES (1, $now, 0)", // same ben twice → one row
            "INSERT INTO IMMUNIZATION VALUES (2, $now, 1)", // draft
            "INSERT INTO IMMUNIZATION VALUES (3, $twoDaysAgo, 0)", // edit of an old record
            // householdId + visitDate, no draft column
            "CREATE TABLE HOUSEHOLD (householdId INTEGER, visitDate INTEGER)",
            "INSERT INTO HOUSEHOLD VALUES (9, $now)",
            // only an updated date → skipped (no creation date)
            "CREATE TABLE HBNC (benId INTEGER, updatedDate INTEGER)",
            "INSERT INTO HBNC VALUES (5, $now)",
            // TEXT date column → skipped
            "CREATE TABLE CBAC (benId INTEGER, createdDate TEXT)",
            "INSERT INTO CBAC VALUES (6, '$now')",
            // no id column → skipped
            "CREATE TABLE PMSMA (other INTEGER, createdDate INTEGER)",
            "INSERT INTO PMSMA VALUES (7, $now)"
        )
        val saves = slot<List<FormSaveLogCache>>()

        interceptor.captureToday()

        coVerify { dao.logSaves(capture(saves)) }
        val rows = saves.captured.sortedBy { it.beneficiaryId }
        assertEquals(listOf(1L, 9L), rows.map { it.beneficiaryId })
        assertEquals(listOf("child_immunization", "household_registration"), rows.map { it.formType })
        val today = ActivityClassifier.dateKey(now)
        rows.forEach {
            assertEquals(42, it.ashaWorkerId)
            assertEquals(today, it.dateKey)
        }
    }

    @Test
    fun `nothing new today logs nothing`() = runTest {
        jdbc.exec(
            "CREATE TABLE IMMUNIZATION (beneficiaryId INTEGER, createdDate INTEGER)",
            "INSERT INTO IMMUNIZATION VALUES (3, $twoDaysAgo)"
        )
        interceptor.captureToday()
        coVerify(exactly = 0) { dao.logSaves(any()) }
    }

    @Test
    fun `logged out device is skipped`() = runTest {
        every { pref.getLoggedInUser() } returns null
        interceptor.captureToday()
        coVerify(exactly = 0) { dao.logSaves(any()) }
    }

    @Test
    fun `preference failure is treated as logged out`() = runTest {
        every { pref.getLoggedInUser() } throws IllegalStateException("prefs")
        interceptor.captureToday()
        coVerify(exactly = 0) { dao.logSaves(any()) }
    }

    @Test
    fun `a failing table query does not block the others`() = runTest {
        jdbc.exec(
            // a view whose underlying table is gone: inspecting or reading it fails
            "CREATE TABLE gone (id INTEGER, createdDate INTEGER)",
            "CREATE VIEW CHILD_REG AS SELECT id, createdDate FROM gone",
            "DROP TABLE gone",
            "CREATE TABLE INFANT_REG (benId INTEGER, createdAt INTEGER)",
            "INSERT INTO INFANT_REG VALUES (11, $now)"
        )
        val saves = slot<List<FormSaveLogCache>>()

        interceptor.captureToday()

        coVerify { dao.logSaves(capture(saves)) }
        assertEquals(listOf(11L), saves.captured.map { it.beneficiaryId })
    }
}

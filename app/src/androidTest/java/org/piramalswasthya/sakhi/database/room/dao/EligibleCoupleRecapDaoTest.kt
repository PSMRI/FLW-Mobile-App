package org.piramalswasthya.sakhi.database.room.dao

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.piramalswasthya.sakhi.database.room.InAppDb

/**
 * Phase 6 — REAL Room instrumented test for the Eligible Couple recap count
 * (`EcrDao.countCurrentAshaEligibleCouples`, Option B: one DISTINCT-couple number
 * across registration + tracking). Runs the ACTUAL generated UNION SQL against
 * real rows in an in-memory instance of the production [InAppDb].
 *
 * Proves: `createdBy = :userName` ownership, the `[start, end)` windows over
 * `dateOfReg`/`visitDate`, and that the UNION/DISTINCT-benId dedup counts a couple
 * ONCE even when it appears in BOTH the registration and tracking tables. FKs stay
 * enforced (EC → BENEFICIARY → HOUSEHOLD parents inserted honestly). Fresh DB per
 * test; framework SQLite (not SQLCipher).
 */
@RunWith(AndroidJUnit4::class)
class EligibleCoupleRecapDaoTest {

    private lateinit var db: InAppDb
    private lateinit var support: SupportSQLiteDatabase

    private val userName = "test_asha"
    private val otherUserName = "other_asha"
    private val windowStart = 1_000_000L
    private val windowEnd = 2_000_000L
    private val inWindow = 1_500_000L
    private val hhId = 7_000L // shared parent household for all EC beneficiaries

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, InAppDb::class.java).allowMainThreadQueries().build()
        support = db.openHelper.writableDatabase
        insertMinimalRow("HOUSEHOLD", mapOf("householdId" to hhId, "ashaId" to 1, "isDraft" to 0))
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun ownRegistration_inWindow_isCounted() {
        insertEcReg(benId = 101, createdBy = userName, dateOfReg = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun ownTracking_inWindow_isCounted() {
        insertEcTracking(benId = 102, createdBy = userName, visitDate = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun sameCouple_registeredAndTracked_countedOnce() {
        insertEcReg(benId = 103, createdBy = userName, dateOfReg = inWindow)
        insertEcTracking(benId = 103, createdBy = userName, visitDate = inWindow + 10)
        assertEquals(1, count()) // DISTINCT benId across both tables
    }

    @Test
    fun twoDistinctCouples_oneRegOneTracking_countsTwo() {
        insertEcReg(benId = 104, createdBy = userName, dateOfReg = inWindow)
        insertEcTracking(benId = 105, createdBy = userName, visitDate = inWindow)
        assertEquals(2, count())
    }

    @Test
    fun otherAsha_isExcluded() {
        insertEcReg(benId = 106, createdBy = otherUserName, dateOfReg = inWindow)
        insertEcTracking(benId = 107, createdBy = otherUserName, visitDate = inWindow)
        assertEquals(0, count())
    }

    @Test
    fun beforeWindow_isExcluded() {
        insertEcReg(benId = 108, createdBy = userName, dateOfReg = windowStart - 1)
        assertEquals(0, count())
    }

    @Test
    fun startInclusive_endExclusive_boundaries() {
        insertEcReg(benId = 109, createdBy = userName, dateOfReg = windowStart)      // included
        insertEcTracking(benId = 110, createdBy = userName, visitDate = windowEnd)   // excluded
        assertEquals(1, count())
    }

    // ============================ helpers ============================

    private fun count(): Int = runBlocking {
        db.ecrDao.countCurrentAshaEligibleCouples(userName, windowStart, windowEnd)
    }

    private fun ensureBeneficiary(benId: Long) {
        insertMinimalRow(
            "BENEFICIARY",
            mapOf("beneficiaryId" to benId, "householdId" to hhId, "ashaId" to 1, "isDraft" to 0),
        )
    }

    private fun insertEcReg(benId: Long, createdBy: String, dateOfReg: Long) {
        ensureBeneficiary(benId)
        insertMinimalRow(
            "ELIGIBLE_COUPLE_REG",
            mapOf("benId" to benId, "createdBy" to createdBy, "dateOfReg" to dateOfReg),
        )
    }

    private fun insertEcTracking(benId: Long, createdBy: String, visitDate: Long) {
        ensureBeneficiary(benId)
        insertMinimalRow(
            "ELIGIBLE_COUPLE_TRACKING",
            mapOf("benId" to benId, "createdBy" to createdBy, "visitDate" to visitDate),
        )
    }

    /** PRAGMA-driven minimal INSERT (NOT NULL/no-default cols + overrides); FKs never disabled. */
    private fun insertMinimalRow(table: String, overrides: Map<String, Any?>) {
        val cols = ArrayList<String>()
        val args = ArrayList<Any?>()
        support.query("PRAGMA table_info(`$table`)").use { c ->
            val nameIdx = c.getColumnIndexOrThrow("name")
            val typeIdx = c.getColumnIndexOrThrow("type")
            val notNullIdx = c.getColumnIndexOrThrow("notnull")
            val defaultIdx = c.getColumnIndexOrThrow("dflt_value")
            while (c.moveToNext()) {
                val name = c.getString(nameIdx)
                val required = c.getInt(notNullIdx) == 1 && c.isNull(defaultIdx)
                when {
                    overrides.containsKey(name) -> {
                        cols += name; args += overrides[name]
                    }

                    required -> {
                        cols += name; args += dummyForType(c.getString(typeIdx))
                    }
                }
            }
        }
        val columnList = cols.joinToString(", ") { "`$it`" }
        val placeholders = cols.joinToString(", ") { "?" }
        support.execSQL(
            "INSERT OR IGNORE INTO `$table` ($columnList) VALUES ($placeholders)",
            args.toTypedArray(),
        )
    }

    private fun dummyForType(declaredType: String?): Any {
        val t = (declaredType ?: "").uppercase()
        return when {
            t.contains("INT") -> 0L
            t.contains("REAL") || t.contains("FLOA") || t.contains("DOUB") -> 0.0
            t.contains("BLOB") -> ByteArray(0)
            else -> ""
        }
    }
}

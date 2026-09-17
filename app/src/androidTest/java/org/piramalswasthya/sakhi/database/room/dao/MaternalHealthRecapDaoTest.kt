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
 * Phase 6 — REAL Room instrumented test for the Maternal Health recap count
 * (`MaternalHealthDao.countCurrentAshaMothersSupported`: DISTINCT mothers the ASHA
 * supported via ANY of pregnancy registration / ANC / PMSMA / delivery / PNC). Runs
 * the ACTUAL generated UNION SQL against real rows in an in-memory instance of the
 * production [InAppDb].
 *
 * Proves: `createdBy = :userName` ownership (no ashaId on these tables), the
 * `[start, end)` windows over each table's own activity date, and that UNION /
 * DISTINCT-benId counts a woman ONCE even when she appears across several maternal
 * forms — and that PNC's download duplicate-row quirk collapses harmlessly. The
 * `active`/`isActive` flag is deliberately NOT part of the query, so activities
 * done in the window count regardless of later closure. FKs stay enforced
 * (maternal → BENEFICIARY → HOUSEHOLD parents inserted honestly). Fresh DB per
 * test; framework SQLite (not SQLCipher).
 */
@RunWith(AndroidJUnit4::class)
class MaternalHealthRecapDaoTest {

    private lateinit var db: InAppDb
    private lateinit var support: SupportSQLiteDatabase

    private val userName = "test_asha"
    private val otherUserName = "other_asha"
    private val windowStart = 1_000_000L
    private val windowEnd = 2_000_000L
    private val inWindow = 1_500_000L
    private val hhId = 7_000L // shared parent household for all mothers

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
    fun ownPregnancyRegistration_inWindow_isCounted() {
        insertPwr(benId = 101, createdBy = userName, dateOfRegistration = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun ownAnc_inWindow_isCounted() {
        insertAnc(benId = 102, createdBy = userName, ancDate = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun ownPmsma_inWindow_isCounted() {
        insertPmsma(benId = 103, createdBy = userName, visitDate = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun ownDelivery_inWindow_isCounted() {
        insertDelivery(benId = 104, createdBy = userName, dateOfDelivery = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun ownPnc_inWindow_isCounted() {
        insertPnc(benId = 105, createdBy = userName, pncDate = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun sameWoman_acrossMultipleForms_countedOnce() {
        insertPwr(benId = 106, createdBy = userName, dateOfRegistration = inWindow)
        insertAnc(benId = 106, createdBy = userName, ancDate = inWindow + 10)
        insertPmsma(benId = 106, createdBy = userName, visitDate = inWindow + 20)
        assertEquals(1, count()) // DISTINCT benId across tables
    }

    @Test
    fun pncDuplicateRows_sameWoman_countedOnce() {
        // Simulates PNC's download id-drop: two rows for the same beneficiary.
        insertPnc(benId = 107, createdBy = userName, pncDate = inWindow)
        insertPnc(benId = 107, createdBy = userName, pncDate = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun otherAsha_isExcluded() {
        insertPwr(benId = 108, createdBy = otherUserName, dateOfRegistration = inWindow)
        insertAnc(benId = 109, createdBy = otherUserName, ancDate = inWindow)
        assertEquals(0, count())
    }

    @Test
    fun beforeWindow_isExcluded() {
        insertPwr(benId = 110, createdBy = userName, dateOfRegistration = windowStart - 1)
        assertEquals(0, count())
    }

    @Test
    fun startInclusive_endExclusive_boundaries() {
        insertPwr(benId = 111, createdBy = userName, dateOfRegistration = windowStart) // included
        insertAnc(benId = 112, createdBy = userName, ancDate = windowEnd)              // excluded
        assertEquals(1, count())
    }

    @Test
    fun twoDistinctWomen_countTwo() {
        insertPwr(benId = 113, createdBy = userName, dateOfRegistration = inWindow)
        insertDelivery(benId = 114, createdBy = userName, dateOfDelivery = inWindow)
        assertEquals(2, count())
    }

    // ============================ helpers ============================

    private fun count(): Int = runBlocking {
        db.maternalHealthDao.countCurrentAshaMothersSupported(userName, windowStart, windowEnd)
    }

    private fun ensureBeneficiary(benId: Long) {
        insertMinimalRow(
            "BENEFICIARY",
            mapOf("beneficiaryId" to benId, "householdId" to hhId, "ashaId" to 1, "isDraft" to 0),
        )
    }

    private fun insertPwr(benId: Long, createdBy: String, dateOfRegistration: Long) {
        ensureBeneficiary(benId)
        insertMinimalRow(
            "PREGNANCY_REGISTER",
            mapOf("benId" to benId, "createdBy" to createdBy, "dateOfRegistration" to dateOfRegistration),
        )
    }

    private fun insertAnc(benId: Long, createdBy: String, ancDate: Long) {
        ensureBeneficiary(benId)
        insertMinimalRow(
            "PREGNANCY_ANC",
            mapOf("benId" to benId, "createdBy" to createdBy, "ancDate" to ancDate),
        )
    }

    private fun insertPmsma(benId: Long, createdBy: String, visitDate: Long) {
        ensureBeneficiary(benId)
        insertMinimalRow(
            "PMSMA",
            mapOf("benId" to benId, "createdBy" to createdBy, "visitDate" to visitDate),
        )
    }

    private fun insertDelivery(benId: Long, createdBy: String, dateOfDelivery: Long) {
        ensureBeneficiary(benId)
        insertMinimalRow(
            "DELIVERY_OUTCOME",
            mapOf("benId" to benId, "createdBy" to createdBy, "dateOfDelivery" to dateOfDelivery),
        )
    }

    private fun insertPnc(benId: Long, createdBy: String, pncDate: Long) {
        ensureBeneficiary(benId)
        insertMinimalRow(
            "PNC_VISIT",
            mapOf("benId" to benId, "createdBy" to createdBy, "pncDate" to pncDate),
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

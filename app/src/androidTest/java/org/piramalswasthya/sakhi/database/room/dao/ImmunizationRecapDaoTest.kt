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
 * Phase 6 — REAL Room instrumented test for the Immunization recap count
 * (`ImmunizationDao.countCurrentAshaDosesAdministered`: vaccine DOSES the ASHA
 * administered). Runs the ACTUAL generated SQL against real rows in an in-memory
 * instance of the production [InAppDb].
 *
 * Proves: `createdBy = :userName` ownership (no ashaId on this table), the
 * `[start, end)` window over the user-entered vaccination `date`, per-DOSE counting
 * (multiple doses for one child each count), NULL-date exclusion, and that the
 * composite PK `(beneficiaryId, vaccineId)` makes the count re-sync-proof (inserting
 * the same dose twice counts once). FKs stay enforced (IMMUNIZATION → BENEFICIARY and
 * → VACCINE parents inserted honestly). Fresh DB per test; framework SQLite (not
 * SQLCipher).
 */
@RunWith(AndroidJUnit4::class)
class ImmunizationRecapDaoTest {

    private lateinit var db: InAppDb
    private lateinit var support: SupportSQLiteDatabase

    private val userName = "test_asha"
    private val otherUserName = "other_asha"
    private val windowStart = 1_000_000L
    private val windowEnd = 2_000_000L
    private val inWindow = 1_500_000L
    private val hhId = 7_000L // shared parent household for all beneficiaries

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
    fun ownDose_inWindow_isCounted() {
        insertDose(benId = 101, vaccineId = 1, createdBy = userName, date = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun multipleDosesSameChild_eachCounted() {
        // Three distinct vaccines for one child in the window -> 3 doses (NOT 1 child).
        insertDose(benId = 102, vaccineId = 1, createdBy = userName, date = inWindow)
        insertDose(benId = 102, vaccineId = 2, createdBy = userName, date = inWindow + 10)
        insertDose(benId = 102, vaccineId = 3, createdBy = userName, date = inWindow + 20)
        assertEquals(3, count())
    }

    @Test
    fun dosesForTwoChildren_countTwo() {
        insertDose(benId = 103, vaccineId = 1, createdBy = userName, date = inWindow)
        insertDose(benId = 104, vaccineId = 1, createdBy = userName, date = inWindow)
        assertEquals(2, count())
    }

    @Test
    fun reDownloadSameDose_countedOnce() {
        // Same (beneficiaryId, vaccineId) inserted twice -> composite PK keeps one row.
        insertDose(benId = 105, vaccineId = 1, createdBy = userName, date = inWindow)
        insertDose(benId = 105, vaccineId = 1, createdBy = userName, date = inWindow)
        assertEquals(1, count())
    }

    @Test
    fun otherAsha_isExcluded() {
        insertDose(benId = 106, vaccineId = 1, createdBy = otherUserName, date = inWindow)
        insertDose(benId = 107, vaccineId = 2, createdBy = otherUserName, date = inWindow)
        assertEquals(0, count())
    }

    @Test
    fun nullDate_isExcluded() {
        insertDose(benId = 108, vaccineId = 1, createdBy = userName, date = null)
        assertEquals(0, count())
    }

    @Test
    fun beforeWindow_isExcluded() {
        insertDose(benId = 109, vaccineId = 1, createdBy = userName, date = windowStart - 1)
        assertEquals(0, count())
    }

    @Test
    fun startInclusive_endExclusive_boundaries() {
        insertDose(benId = 110, vaccineId = 1, createdBy = userName, date = windowStart) // included
        insertDose(benId = 111, vaccineId = 1, createdBy = userName, date = windowEnd)   // excluded
        assertEquals(1, count())
    }

    // ============================ helpers ============================

    private fun count(): Int = runBlocking {
        db.vaccineDao.countCurrentAshaDosesAdministered(userName, windowStart, windowEnd)
    }

    private fun ensureBeneficiary(benId: Long) {
        insertMinimalRow(
            "BENEFICIARY",
            mapOf("beneficiaryId" to benId, "householdId" to hhId, "ashaId" to 1, "isDraft" to 0),
        )
    }

    private fun ensureVaccine(vaccineId: Int) {
        insertMinimalRow("VACCINE", mapOf("vaccineId" to vaccineId))
    }

    private fun insertDose(benId: Long, vaccineId: Int, createdBy: String, date: Long?) {
        ensureBeneficiary(benId)
        ensureVaccine(vaccineId)
        insertMinimalRow(
            "IMMUNIZATION",
            mapOf("beneficiaryId" to benId, "vaccineId" to vaccineId, "createdBy" to createdBy, "date" to date),
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

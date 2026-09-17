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
 * Phase 6 — REAL Room instrumented test for the Beneficiary + Household recap
 * registration counts (`BenDao.countCurrentAshaRegistrations` /
 * `HouseholdDao.countCurrentAshaRegistrations`). Runs the ACTUAL generated DAO SQL
 * against real rows in an in-memory instance of the production [InAppDb].
 *
 * Proves the SQL semantics: `ashaId = :userId` ownership, `isDraft = 0` filter,
 * the `[start, end)` window (over `regDate` / `createdTimeStamp`), that a
 * deactivated-later beneficiary is still counted, and that a household with a null
 * timestamp is excluded. FKs stay enforced; HOUSEHOLD (FK root) parents are
 * inserted honestly for BENEFICIARY rows. Fresh DB per test; framework SQLite
 * (not SQLCipher), so the real encrypted app DB is never touched.
 */
@RunWith(AndroidJUnit4::class)
class RegistrationRecapDaoTest {

    private lateinit var db: InAppDb
    private lateinit var support: SupportSQLiteDatabase

    private val userId = 42
    private val otherUserId = 77
    private val windowStart = 1_000_000L
    private val windowEnd = 2_000_000L
    private val inWindow = 1_500_000L
    private val hhForBen = 8_000L // parent household for beneficiary rows

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, InAppDb::class.java).allowMainThreadQueries().build()
        support = db.openHelper.writableDatabase
    }

    @After
    fun tearDown() = db.close()

    // ============================ BENEFICIARY ============================

    @Test
    fun beneficiary_ownRegistration_inWindow_isCounted() {
        insertBeneficiary(benId = 101, ashaId = userId, isDraft = 0, regDate = inWindow)
        assertEquals(1, benCount())
    }

    @Test
    fun beneficiary_otherAsha_isExcluded() {
        insertBeneficiary(benId = 102, ashaId = otherUserId, isDraft = 0, regDate = inWindow)
        assertEquals(0, benCount())
    }

    @Test
    fun beneficiary_draft_isExcluded() {
        insertBeneficiary(benId = 103, ashaId = userId, isDraft = 1, regDate = inWindow)
        assertEquals(0, benCount())
    }

    @Test
    fun beneficiary_deactivatedLater_isStillCounted() {
        insertBeneficiary(benId = 104, ashaId = userId, isDraft = 0, regDate = inWindow, isDeactivate = 1)
        assertEquals(1, benCount())
    }

    @Test
    fun beneficiary_beforeWindow_isExcluded() {
        insertBeneficiary(benId = 105, ashaId = userId, isDraft = 0, regDate = windowStart - 1)
        assertEquals(0, benCount())
    }

    @Test
    fun beneficiary_startInclusive_endExclusive_boundaries() {
        insertBeneficiary(benId = 106, ashaId = userId, isDraft = 0, regDate = windowStart) // included
        insertBeneficiary(benId = 107, ashaId = userId, isDraft = 0, regDate = windowEnd)   // excluded
        assertEquals(1, benCount())
    }

    @Test
    fun beneficiary_twoOwnRegistrations_countsTwo() {
        insertBeneficiary(benId = 108, ashaId = userId, isDraft = 0, regDate = inWindow)
        insertBeneficiary(benId = 109, ashaId = userId, isDraft = 0, regDate = inWindow + 1)
        assertEquals(2, benCount())
    }

    // ============================ HOUSEHOLD ============================

    @Test
    fun household_ownRegistration_inWindow_isCounted() {
        insertHousehold(hhId = 201, ashaId = userId, isDraft = 0, createdTimeStamp = inWindow)
        assertEquals(1, householdCount())
    }

    @Test
    fun household_otherAsha_isExcluded() {
        insertHousehold(hhId = 202, ashaId = otherUserId, isDraft = 0, createdTimeStamp = inWindow)
        assertEquals(0, householdCount())
    }

    @Test
    fun household_draft_isExcluded() {
        insertHousehold(hhId = 203, ashaId = userId, isDraft = 1, createdTimeStamp = inWindow)
        assertEquals(0, householdCount())
    }

    @Test
    fun household_nullTimestamp_isExcluded() {
        insertHousehold(hhId = 204, ashaId = userId, isDraft = 0, createdTimeStamp = null)
        assertEquals(0, householdCount())
    }

    @Test
    fun household_endExclusiveBoundary_isExcluded() {
        insertHousehold(hhId = 205, ashaId = userId, isDraft = 0, createdTimeStamp = windowEnd)
        assertEquals(0, householdCount())
    }

    // ============================ helpers ============================

    private fun benCount(): Int = runBlocking {
        db.benDao.countCurrentAshaRegistrations(userId, windowStart, windowEnd)
    }

    private fun householdCount(): Int = runBlocking {
        db.householdDao.countCurrentAshaRegistrations(userId, windowStart, windowEnd)
    }

    private fun insertHousehold(hhId: Long, ashaId: Int, isDraft: Int, createdTimeStamp: Long?) {
        val overrides = mutableMapOf<String, Any?>(
            "householdId" to hhId,
            "ashaId" to ashaId,
            "isDraft" to isDraft,
        )
        if (createdTimeStamp != null) overrides["createdTimeStamp"] = createdTimeStamp
        insertMinimalRow("HOUSEHOLD", overrides)
    }

    private fun insertBeneficiary(
        benId: Long,
        ashaId: Int,
        isDraft: Int,
        regDate: Long,
        isDeactivate: Int = 0,
    ) {
        // FK parent household (any values; not counted in the beneficiary query).
        insertMinimalRow("HOUSEHOLD", mapOf("householdId" to hhForBen, "ashaId" to ashaId, "isDraft" to 0))
        insertMinimalRow(
            "BENEFICIARY",
            mapOf(
                "beneficiaryId" to benId,
                "householdId" to hhForBen,
                "ashaId" to ashaId,
                "isDraft" to isDraft,
                "isDeactivate" to isDeactivate,
                "regDate" to regDate,
            ),
        )
    }

    /**
     * Inserts a schema-valid row into [table] supplying only the columns SQLite
     * requires (NOT NULL, no default) plus [overrides]; column list/types come from
     * PRAGMA table_info at runtime. FKs are never disabled; INSERT OR IGNORE keeps
     * shared parents idempotent.
     */
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

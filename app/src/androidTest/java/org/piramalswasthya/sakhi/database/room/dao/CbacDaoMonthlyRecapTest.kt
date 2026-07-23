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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.CbacCache

/**
 * Phase 4.4 — REAL Room instrumented test for the Monthly Recap CBAC metric query.
 *
 * Unlike the JVM unit test [org.piramalswasthya.sakhi.repositories.CbacRecapDataSourceTest]
 * (which mocks the DAO and therefore never executes any SQL), this test runs the
 * ACTUAL generated `CbacDao_Impl.countCurrentAshaScreenings(...)` against real rows
 * in a real (in-memory) instance of the production [InAppDb]. It proves the SQL
 * row semantics: the ownership predicate, the [start, end) window, COUNT(DISTINCT
 * benId), and sync-state independence.
 *
 * Production query under test (READ-ONLY, unchanged by this test):
 *   SELECT COUNT(DISTINCT benId) FROM CBAC
 *   WHERE fillDate >= :startInclusive AND fillDate < :endExclusive
 *     AND (ashaId = :userId OR (ashaId = 0 AND createdBy = :userName))
 *
 * Isolation: a fresh in-memory database per test (@Before/@After). The in-memory
 * builder uses the framework SQLite open helper, NOT the app's SQLCipher factory,
 * so encryption config is irrelevant to query semantics and the real app database
 * file is never touched. Room enables `PRAGMA foreign_keys = ON` (the schema has
 * FKs), so CBAC rows require real BENEFICIARY parents, which require a HOUSEHOLD
 * parent — those are inserted honestly (foreign keys are never disabled).
 */
@RunWith(AndroidJUnit4::class)
class CbacDaoMonthlyRecapTest {

    private lateinit var db: InAppDb
    private lateinit var cbacDao: CbacDao
    private lateinit var support: SupportSQLiteDatabase

    // Test-only identities. NEVER production user id 299.
    private val userId = 42
    private val userName = "test_asha"
    private val otherUserId = 77
    private val otherUserName = "other_asha"

    // Frozen recap window [start, end). Synthetic millis keep boundaries crisp.
    private val windowStart = 1_000_000L
    private val windowEnd = 2_000_000L
    private val inWindow = 1_500_000L

    // Single parent household (FK root) shared by all test beneficiaries.
    private val hhId = 9_001L

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, InAppDb::class.java)
            .allowMainThreadQueries()
            .build()
        cbacDao = db.cbacDao
        // Opening the writable handle runs Room's onOpen -> PRAGMA foreign_keys = ON.
        support = db.openHelper.writableDatabase
        insertMinimalRow("HOUSEHOLD", mapOf("householdId" to hhId))
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---------------------------------------------------------------------
    // 1. Current ASHA local row -> counted
    // ---------------------------------------------------------------------
    @Test
    fun case01_currentAsha_localRow_isCounted() {
        insertCbac(benId = 101, ashaId = userId, createdBy = userName, fillDate = inWindow)
        assertEquals(1, count())
    }

    // ---------------------------------------------------------------------
    // 2. Current ASHA downloaded row (ashaId = 0, createdBy = me) -> counted
    // ---------------------------------------------------------------------
    @Test
    fun case02_currentAsha_downloadedRow_isCounted() {
        insertCbac(benId = 102, ashaId = 0, createdBy = userName, fillDate = inWindow)
        assertEquals(1, count())
    }

    // ---------------------------------------------------------------------
    // 3. Downloaded row belonging to another username -> excluded
    // ---------------------------------------------------------------------
    @Test
    fun case03_downloaded_otherUsername_isExcluded() {
        insertCbac(benId = 103, ashaId = 0, createdBy = otherUserName, fillDate = inWindow)
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 4. Downloaded row with NULL createdBy -> excluded
    // ---------------------------------------------------------------------
    @Test
    fun case04_downloaded_nullCreatedBy_isExcluded() {
        insertCbac(benId = 104, ashaId = 0, createdBy = null, fillDate = inWindow)
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 5. Downloaded row with blank createdBy -> excluded (exact-equality SQL:
    //    '' never equals 'test_asha'). Blank ownership is NOT supported, which
    //    is the correct behaviour; the production query is left unchanged.
    // ---------------------------------------------------------------------
    @Test
    fun case05_downloaded_blankCreatedBy_isExcluded() {
        insertCbac(benId = 105, ashaId = 0, createdBy = "", fillDate = inWindow)
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 6. Another ASHA's local row (ashaId = 77) -> excluded
    // ---------------------------------------------------------------------
    @Test
    fun case06_otherAsha_localRow_isExcluded() {
        insertCbac(benId = 106, ashaId = otherUserId, createdBy = otherUserName, fillDate = inWindow)
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 7. Local + downloaded copy of the SAME beneficiary -> COUNT(DISTINCT benId) = 1
    // ---------------------------------------------------------------------
    @Test
    fun case07_localAndDownloadedDuplicate_distinctBenId_countsOne() {
        insertCbac(benId = 107, ashaId = userId, createdBy = userName, fillDate = inWindow)
        insertCbac(benId = 107, ashaId = 0, createdBy = userName, fillDate = inWindow + 1)
        assertEquals(1, count())
    }

    // ---------------------------------------------------------------------
    // 8. Two different beneficiaries (one local, one downloaded) -> 2
    // ---------------------------------------------------------------------
    @Test
    fun case08_twoDifferentBeneficiaries_countsTwo() {
        insertCbac(benId = 108, ashaId = userId, createdBy = userName, fillDate = inWindow)
        insertCbac(benId = 109, ashaId = 0, createdBy = userName, fillDate = inWindow)
        assertEquals(2, count())
    }

    // ---------------------------------------------------------------------
    // 9. fillDate == startInclusive -> included (>=)
    // ---------------------------------------------------------------------
    @Test
    fun case09_startInclusiveBoundary_isIncluded() {
        insertCbac(benId = 110, ashaId = userId, createdBy = userName, fillDate = windowStart)
        assertEquals(1, count())
    }

    // ---------------------------------------------------------------------
    // 10. fillDate == endExclusive -> excluded (< end)
    // ---------------------------------------------------------------------
    @Test
    fun case10_endExclusiveBoundary_isExcluded() {
        insertCbac(benId = 111, ashaId = userId, createdBy = userName, fillDate = windowEnd)
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 11. fillDate < startInclusive -> excluded
    // ---------------------------------------------------------------------
    @Test
    fun case11_beforeWindow_isExcluded() {
        insertCbac(benId = 112, ashaId = userId, createdBy = userName, fillDate = windowStart - 1)
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 12. fillDate > endExclusive -> excluded
    // ---------------------------------------------------------------------
    @Test
    fun case12_afterWindow_isExcluded() {
        insertCbac(benId = 113, ashaId = userId, createdBy = userName, fillDate = windowEnd + 1)
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 13. Sync state must NOT filter: UNSYNCED, SYNCING, SYNCED all count.
    //     Three distinct beneficiaries -> 3 proves every row was counted.
    // ---------------------------------------------------------------------
    @Test
    fun case13_syncStates_doNotMatter_allCount() {
        insertCbac(benId = 114, ashaId = userId, createdBy = userName, fillDate = inWindow, syncState = SyncState.UNSYNCED)
        insertCbac(benId = 115, ashaId = userId, createdBy = userName, fillDate = inWindow, syncState = SyncState.SYNCING)
        insertCbac(benId = 116, ashaId = userId, createdBy = userName, fillDate = inWindow, syncState = SyncState.SYNCED)
        assertEquals(3, count())
    }

    // ---------------------------------------------------------------------
    // 14. No matching rows -> 0
    // ---------------------------------------------------------------------
    @Test
    fun case14_emptyResult_isZero() {
        assertEquals(0, count())
    }

    // ---------------------------------------------------------------------
    // 15. Same beneficiary inside AND outside the window contributes exactly 1
    //     to the target-month count.
    // ---------------------------------------------------------------------
    @Test
    fun case15_sameBeneficiaryInsideAndOutside_countsOneForMonth() {
        insertCbac(benId = 117, ashaId = userId, createdBy = userName, fillDate = inWindow)
        insertCbac(benId = 117, ashaId = userId, createdBy = userName, fillDate = windowEnd + 5_000)
        assertEquals(1, count())
    }

    // ---------------------------------------------------------------------
    // 16. Cross-month local/download date drift (accepted upstream limitation):
    //     a local copy dated in month A and a downloaded copy of the SAME
    //     beneficiary dated in month B each contribute 1 to their own month.
    //     COUNT(DISTINCT benId) does NOT de-duplicate across different months.
    // ---------------------------------------------------------------------
    @Test
    fun case16_crossMonth_localAndDownload_eachMonthCountsOne() {
        val monthAStart = windowStart          // [1_000_000, 2_000_000)
        val monthAEnd = windowEnd
        val monthBStart = windowEnd            // [2_000_000, 3_000_000)
        val monthBEnd = 3_000_000L

        // Local copy dated in month A, downloaded copy (same benId) dated in month B.
        insertCbac(benId = 118, ashaId = userId, createdBy = userName, fillDate = 1_500_000L)
        insertCbac(benId = 118, ashaId = 0, createdBy = userName, fillDate = 2_500_000L)

        assertEquals(1, count(start = monthAStart, end = monthAEnd))
        assertEquals(1, count(start = monthBStart, end = monthBEnd))
    }

    // ====================== fixtures / helpers ======================

    /** Runs the REAL DAO query. Defaults target the logged-in test ASHA + window. */
    private fun count(
        uId: Int = userId,
        uName: String = userName,
        start: Long = windowStart,
        end: Long = windowEnd,
    ): Int = runBlocking {
        cbacDao.countCurrentAshaScreenings(uId, uName, start, end)
    }

    /** Inserts one CBAC row via the real DAO after ensuring its FK parent exists. */
    private fun insertCbac(
        benId: Long,
        ashaId: Int,
        createdBy: String?,
        fillDate: Long,
        syncState: SyncState = SyncState.SYNCED,
    ) = runBlocking {
        ensureBeneficiary(benId)
        cbacDao.upsert(
            CbacCache(
                benId = benId,
                ashaId = ashaId,
                fillDate = fillDate,
                createdBy = createdBy,
                syncState = syncState,
            )
        )
    }

    /**
     * Guarantees a BENEFICIARY parent row for [benId] so the CBAC foreign key is
     * satisfiable. Idempotent (INSERT OR IGNORE) so the same beneficiary can back
     * several CBAC rows. The beneficiary's own columns are irrelevant to the
     * CBAC-only query, so they get schema-valid dummies.
     */
    private fun ensureBeneficiary(benId: Long) {
        insertMinimalRow(
            "BENEFICIARY",
            mapOf("beneficiaryId" to benId, "householdId" to hhId),
        )
    }

    /**
     * Inserts a schema-valid row into [table], supplying ONLY the columns SQLite
     * actually requires (NOT NULL with no default) plus [overrides]. The column
     * list and declared types are read from PRAGMA table_info at runtime, so this
     * needs no knowledge of the (large) entity internals and stays correct if the
     * schema grows. Foreign keys are NOT disabled — parents are inserted for real,
     * in FK order (HOUSEHOLD before BENEFICIARY). INSERT OR IGNORE keeps it
     * idempotent for shared parents. Dummy values: INTEGER->0, REAL->0.0,
     * BLOB->empty, TEXT/other->"".
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
                        cols += name
                        args += overrides[name]
                    }
                    required -> {
                        cols += name
                        args += dummyForType(c.getString(typeIdx))
                    }
                    // otherwise: nullable or has a default -> omit
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
            else -> "" // TEXT / CHAR / CLOB / untyped
        }
    }
}

package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.MonthlyRecapDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.MonthlyRecapMetricsCodec
import org.piramalswasthya.sakhi.helpers.RecapClock
import org.piramalswasthya.sakhi.model.MonthlyRecapCache
import org.piramalswasthya.sakhi.model.MonthlyRecapLanguage
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsContract
import org.piramalswasthya.sakhi.model.RecapStatus
import org.piramalswasthya.sakhi.model.User
import java.util.Calendar

/**
 * In-memory DAO honouring the unique (userId, recapYearMonth) constraint via
 * insert-IGNORE semantics, mirroring the Room contract so repository
 * idempotency is testable on the JVM.
 */
private class FakeMonthlyRecapDao : MonthlyRecapDao {
    val rows = mutableListOf<MonthlyRecapCache>()
    var insertCalls = 0
    private var nextId = 1L

    private fun find(userId: Int, yearMonth: Int) =
        rows.firstOrNull { it.userId == userId && it.recapYearMonth == yearMonth }

    override suspend fun insert(recap: MonthlyRecapCache): Long {
        insertCalls++
        if (find(recap.userId, recap.recapYearMonth) != null) return -1L // IGNORE
        val stored = recap.copy(id = nextId++)
        rows.add(stored)
        return stored.id
    }

    override suspend fun get(userId: Int, yearMonth: Int) = find(userId, yearMonth)

    override fun observe(userId: Int, yearMonth: Int): Flow<MonthlyRecapCache?> =
        flowOf(find(userId, yearMonth))

    override suspend fun setLanguage(userId: Int, yearMonth: Int, languageToken: String, now: Long) {
        val i = rows.indexOfFirst { it.userId == userId && it.recapYearMonth == yearMonth }
        if (i >= 0) rows[i] = rows[i].copy(language = languageToken, updatedAt = now)
    }

    override suspend fun markStarted(userId: Int, yearMonth: Int, now: Long) {
        val i = rows.indexOfFirst { it.userId == userId && it.recapYearMonth == yearMonth }
        if (i >= 0 && rows[i].status != RecapStatus.COMPLETED.name) {
            rows[i] = rows[i].copy(
                status = RecapStatus.IN_PROGRESS.name,
                startedAt = rows[i].startedAt ?: now,
                updatedAt = now,
            )
        }
    }

    override suspend fun updateProgress(userId: Int, yearMonth: Int, scene: Int, now: Long) {
        val i = rows.indexOfFirst { it.userId == userId && it.recapYearMonth == yearMonth }
        if (i >= 0 && rows[i].status == RecapStatus.IN_PROGRESS.name) {
            rows[i] = rows[i].copy(progressScene = scene, updatedAt = now)
        }
    }

    override suspend fun setTotalScenes(userId: Int, yearMonth: Int, totalScenes: Int, now: Long) {
        val i = rows.indexOfFirst { it.userId == userId && it.recapYearMonth == yearMonth }
        if (i >= 0) rows[i] = rows[i].copy(totalScenes = totalScenes, updatedAt = now)
    }

    override suspend fun markCompleted(userId: Int, yearMonth: Int, now: Long) {
        val i = rows.indexOfFirst { it.userId == userId && it.recapYearMonth == yearMonth }
        if (i >= 0) {
            rows[i] = rows[i].copy(
                status = RecapStatus.COMPLETED.name,
                completedAt = rows[i].completedAt ?: now,
                updatedAt = now,
            )
        }
    }

    override suspend fun setMetricsIfAbsent(
        userId: Int,
        yearMonth: Int,
        metricsJson: String,
        now: Long,
    ): Int {
        // Mirrors the SQL "... AND metricsJson IS NULL": write (and report 1) only
        // when nothing is stored yet; otherwise report 0 (someone already froze it).
        val i = rows.indexOfFirst {
            it.userId == userId && it.recapYearMonth == yearMonth && it.metricsJson == null
        }
        return if (i >= 0) {
            rows[i] = rows[i].copy(metricsJson = metricsJson, updatedAt = now); 1
        } else {
            0
        }
    }
}

private class FixedClock(private val fixed: Calendar) : RecapClock {
    override fun now(): Calendar = fixed.clone() as Calendar
}

@OptIn(ExperimentalCoroutinesApi::class)
class MonthlyRecapRepoTest {

    private lateinit var dao: FakeMonthlyRecapDao
    private lateinit var pref: PreferenceDao
    private lateinit var cbacDataSource: CbacRecapDataSource
    private lateinit var householdDataSource: HouseholdRecapDataSource
    private lateinit var beneficiaryDataSource: BeneficiaryRecapDataSource
    private lateinit var eligibleCoupleDataSource: EligibleCoupleRecapDataSource
    private lateinit var maternalHealthDataSource: MaternalHealthRecapDataSource
    private lateinit var immunizationDataSource: ImmunizationRecapDataSource
    private lateinit var repo: MonthlyRecapRepo

    // Fixed "today": 20 July 2026 -> recap month June 2026 (202606).
    private val clock = FixedClock(
        Calendar.getInstance().apply {
            clear(); set(2026, Calendar.JULY, 20, 10, 30, 0)
        }
    )

    @Before
    fun setUp() {
        dao = FakeMonthlyRecapDao()
        pref = mockk()
        val user = mockk<User> {
            every { userId } returns 7
            every { userName } returns "meena"
        }
        every { pref.getLoggedInUser() } returns user
        cbacDataSource = mockk()
        householdDataSource = mockk()
        beneficiaryDataSource = mockk()
        eligibleCoupleDataSource = mockk()
        maternalHealthDataSource = mockk()
        immunizationDataSource = mockk()
        // Default: 5 CBAC screening events in the window (overridable per test).
        coEvery { cbacDataSource.countScreeningEvents(any(), any(), any(), any()) } returns 5
        coEvery { householdDataSource.countRegistrations(any(), any(), any()) } returns 0
        coEvery { beneficiaryDataSource.countRegistrations(any(), any(), any()) } returns 0
        coEvery { eligibleCoupleDataSource.countCouples(any(), any(), any()) } returns 0
        coEvery { maternalHealthDataSource.countMothersSupported(any(), any(), any()) } returns 0
        coEvery { immunizationDataSource.countDosesAdministered(any(), any(), any()) } returns 0
        repo = MonthlyRecapRepo(
            dao, pref, clock,
            MonthlyRecapMetricsCalculator(
                cbacDataSource,
                householdDataSource,
                beneficiaryDataSource,
                eligibleCoupleDataSource,
                maternalHealthDataSource,
                immunizationDataSource,
            ),
        )
    }

    @Test
    fun `getOrCreate creates exactly one stable snapshot and reuses it`() = runTest {
        val first = repo.getOrCreateCurrentRecap()
        val second = repo.getOrCreateCurrentRecap()
        assertNotNull(first)
        assertEquals(first!!.id, second!!.id)
        assertEquals(202606, first.recapYearMonth)
        assertEquals(1, dao.rows.size)
        assertEquals(1, dao.insertCalls) // second call short-circuits on read
    }

    @Test
    fun `getOrCreate returns the winner when another call inserted first`() = runTest {
        // Simulate a concurrent writer landing before this call's insert.
        val winner = repo.getOrCreateCurrentRecap()!!
        val loser = repo.getOrCreateCurrentRecap()!!
        assertEquals(winner.id, loser.id)
        assertEquals(winner.variantSeed, loser.variantSeed)
        assertEquals(1, dao.rows.size)
    }

    @Test
    fun `no logged-in user yields null and writes nothing`() = runTest {
        every { pref.getLoggedInUser() } returns null
        assertNull(repo.getOrCreateCurrentRecap())
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun `language persists to the current snapshot`() = runTest {
        assertTrue(repo.setRecapLanguage(MonthlyRecapLanguage.HINDI))
        assertEquals("HI", dao.rows.single().language)
        assertEquals(202606, dao.rows.single().recapYearMonth)
    }

    @Test
    fun `language change preserves snapshot identity and variant seed`() = runTest {
        val created = repo.getOrCreateCurrentRecap()!!
        repo.setRecapLanguage(MonthlyRecapLanguage.HINDI)
        repo.setRecapLanguage(MonthlyRecapLanguage.ASSAMESE)
        val row = dao.rows.single()
        assertEquals(created.id, row.id)
        assertEquals(created.variantSeed, row.variantSeed)
        assertEquals(created.recapYearMonth, row.recapYearMonth)
        assertEquals("AS", row.language)
    }

    @Test
    fun `markStarted moves snapshot to IN_PROGRESS`() = runTest {
        repo.markStarted()
        assertEquals(RecapStatus.IN_PROGRESS.name, dao.rows.single().status)
        assertNotNull(dao.rows.single().startedAt)
    }

    @Test
    fun `progress updates are clamped and require IN_PROGRESS`() = runTest {
        repo.markStarted()
        repo.updateSafeProgress(-5)
        assertEquals(0, dao.rows.single().progressScene)
        repo.updateSafeProgress(3)
        assertEquals(3, dao.rows.single().progressScene)
        repo.markCompleted()
        repo.updateSafeProgress(9) // ignored after completion
        assertEquals(3, dao.rows.single().progressScene)
    }

    @Test
    fun `markCompleted moves snapshot to COMPLETED and started state cannot regress`() = runTest {
        repo.markStarted()
        repo.markCompleted()
        assertEquals(RecapStatus.COMPLETED.name, dao.rows.single().status)
        repo.markStarted() // completed rows are protected
        assertEquals(RecapStatus.COMPLETED.name, dao.rows.single().status)
    }

    // ---- metrics generation / freeze (Phase 4.3) ----

    @Test
    fun `ensureMetrics calculates, persists and returns the CBAC activity metric`() = runTest {
        val payload = repo.ensureCurrentRecapMetrics()!!
        assertEquals(202606, payload.recapYearMonth)
        val category = payload.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_NCD }
        assertEquals(MonthlyRecapMetricsContract.CATEGORY_NCD, category.categoryId)
        assertEquals(5, category.categoryTotal)
        val activity = category.activities.single()
        assertEquals(MonthlyRecapMetricsContract.ACTIVITY_CBAC_SCREENINGS, activity.activityId)
        assertEquals("EVENT", activity.unit)
        assertEquals(5, activity.count)
        // Persisted into the snapshot's metricsJson.
        assertNotNull(dao.rows.single().metricsJson)
    }

    @Test
    fun `ensureMetrics emits the Maternal Health distinct-mothers category`() = runTest {
        coEvery { maternalHealthDataSource.countMothersSupported(any(), any(), any()) } returns 4
        val payload = repo.ensureCurrentRecapMetrics()!!
        val category =
            payload.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_MATERNAL_HEALTH }
        assertEquals(4, category.categoryTotal)
        val activity = category.activities.single()
        assertEquals(MonthlyRecapMetricsContract.ACTIVITY_MATERNAL_HEALTH_MOTHERS, activity.activityId)
        assertEquals("UNIQUE_BENEFICIARY", activity.unit)
        assertEquals(4, activity.count)
    }

    @Test
    fun `ensureMetrics emits the Immunization doses-administered category`() = runTest {
        coEvery { immunizationDataSource.countDosesAdministered(any(), any(), any()) } returns 12
        val payload = repo.ensureCurrentRecapMetrics()!!
        val category =
            payload.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_IMMUNIZATION }
        assertEquals(12, category.categoryTotal)
        val activity = category.activities.single()
        assertEquals(MonthlyRecapMetricsContract.ACTIVITY_IMMUNIZATION_DOSES, activity.activityId)
        assertEquals("DOSE", activity.unit)
        assertEquals(12, activity.count)
    }

    @Test
    fun `existing valid metrics are reused without recalculating`() = runTest {
        repo.ensureCurrentRecapMetrics()
        repo.ensureCurrentRecapMetrics()
        coVerify(exactly = 1) { cbacDataSource.countScreeningEvents(any(), any(), any(), any()) }
    }

    @Test
    fun `frozen metric is unchanged after later clinical data changes`() = runTest {
        val first = repo.ensureCurrentRecapMetrics()!!
        assertEquals(5, first.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_NCD }.categoryTotal)
        // Simulate new/edited CBAC rows after generation.
        coEvery { cbacDataSource.countScreeningEvents(any(), any(), any(), any()) } returns 99
        val second = repo.ensureCurrentRecapMetrics()!!
        assertEquals(5, second.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_NCD }.categoryTotal) // frozen value, not 99
        coVerify(exactly = 1) { cbacDataSource.countScreeningEvents(any(), any(), any(), any()) }
    }

    @Test
    fun `concurrent generation freezes one payload and both callers get it`() = runTest {
        val results = awaitAll(
            async { repo.ensureCurrentRecapMetrics() },
            async { repo.ensureCurrentRecapMetrics() },
        )
        assertEquals(5, results[0]!!.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_NCD }.categoryTotal)
        assertEquals(5, results[1]!!.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_NCD }.categoryTotal)
        assertEquals(1, dao.rows.size)
        coVerify(exactly = 1) { cbacDataSource.countScreeningEvents(any(), any(), any(), any()) }
    }

    @Test
    fun `language change does not alter a frozen metric`() = runTest {
        repo.ensureCurrentRecapMetrics()
        repo.setRecapLanguage(MonthlyRecapLanguage.HINDI)
        repo.setRecapLanguage(MonthlyRecapLanguage.ASSAMESE)
        val row = dao.rows.single()
        assertEquals("AS", row.language)
        assertEquals(5, MonthlyRecapMetricsCodec.decodeOrNull(row.metricsJson)!!.categories.first { it.categoryId == MonthlyRecapMetricsContract.CATEGORY_NCD }.categoryTotal)
    }

    @Test
    fun `metric generation does not change status or progress`() = runTest {
        repo.ensureCurrentRecapMetrics()
        val row = dao.rows.single()
        assertEquals(RecapStatus.NOT_STARTED.name, row.status)
        assertEquals(0, row.progressScene)
    }

    @Test
    fun `ensureMetrics returns null and writes nothing when no user is logged in`() = runTest {
        every { pref.getLoggedInUser() } returns null
        assertNull(repo.ensureCurrentRecapMetrics())
        assertTrue(dao.rows.isEmpty())
    }
}

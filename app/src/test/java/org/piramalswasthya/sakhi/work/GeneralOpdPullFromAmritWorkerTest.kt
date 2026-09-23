package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class GeneralOpdPullFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val benRepo: BenRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): GeneralOpdPullFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return GeneralOpdPullFromAmritWorker(context, params, benRepo, preferenceDao)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    @Test
    fun doWork_returnsSuccess_whenNoPagesToSync() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(any()) } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenInitialFetchReturnsMinusOne() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(any()) } returns -1

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("GeneralOpdPullFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("Initial page fetch failed", result.outputData.getString("error"))
    }

    @Test
    fun doWork_usesFirstSyncLastSyncedPage_whenLastSyncedTimestampIsDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        every { preferenceDao.getFirstSyncLastSyncedPage() } returns 2
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(2) } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(2) }
    }

    @Test
    fun doWork_returnsSuccess_whenInitialFetchRetriesThenReturnsNoPages() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(0) } returnsMany listOf(-2, 0)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 2) { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(0) }
    }

    @Test
    fun doWork_returnsSuccess_whenAllPagesSyncSuccessfully() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(any()) } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenBenRepoReturnsMinusOneDuringPageSync() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(any()) } returns 5
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(1) } returns -1

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("GeneralOpdPullFromAmritWorker", result.outputData.getString("worker_name"))
        assertTrue(result.outputData.getString("error")!!.contains("-1"))
    }

    @Test
    fun doWork_returnsSuccess_whenSQLiteConstraintExceptionCaughtInternally() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(any()) } returns 1
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(1) } throws SQLiteConstraintException("constraint violation")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenSQLiteConstraintExceptionThrownDuringInitialFetch() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { benRepo.getGeneralOPDBeneficiariesFromServertoWorker(0) } throws SQLiteConstraintException("initial fetch constraint failure")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("GeneralOpdPullFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "SQLite constraint: null",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsFailure_whenUnexpectedExceptionOccursBeforeSync() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } throws RuntimeException("boom")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("GeneralOpdPullFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("boom", result.outputData.getString("error"))
    }
}

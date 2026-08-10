package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.piramalswasthya.sakhi.repositories.MalariaRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullMalariaFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val malariaRepo: MalariaRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullMalariaFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullMalariaFromAmritWorker(context, params, malariaRepo, preferenceDao)
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
    fun doWork_returnsSuccess_whenAllRepoCallsReturnZero() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        coEvery { malariaRepo.getMalariaScreeningDetailsFromServer() } returns 0
        coEvery { malariaRepo.getIRSScreeningDetailsFromServer() } returns 0
        coEvery { malariaRepo.getMalariaConfiremedDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenAllRepoCallsReturnOne() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        coEvery { malariaRepo.getMalariaScreeningDetailsFromServer() } returns 1
        coEvery { malariaRepo.getIRSScreeningDetailsFromServer() } returns 1
        coEvery { malariaRepo.getMalariaConfiremedDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenARepoCallReturnsUnexpectedValue() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        coEvery { malariaRepo.getMalariaScreeningDetailsFromServer() } returns 2
        coEvery { malariaRepo.getIRSScreeningDetailsFromServer() } returns 0
        coEvery { malariaRepo.getMalariaConfiremedDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "Pull operation returned incomplete results",
            (result as ListenableWorker.Result.Failure).outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenARepoCallThrowsException() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        coEvery { malariaRepo.getMalariaScreeningDetailsFromServer() } throws RuntimeException("boom")
        coEvery { malariaRepo.getIRSScreeningDetailsFromServer() } returns 1
        coEvery { malariaRepo.getMalariaConfiremedDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenPreferenceDaoThrowsException() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } throws RuntimeException("pref error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "pref error",
            (result as ListenableWorker.Result.Failure).outputData.getString("error")
        )
    }

    @Test
    fun doWork_callsFirstSyncLastSyncedPage_whenLastSyncedTimestampIsDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        every { preferenceDao.getFirstSyncLastSyncedPage() } returns 3
        coEvery { malariaRepo.getMalariaScreeningDetailsFromServer() } returns 1
        coEvery { malariaRepo.getIRSScreeningDetailsFromServer() } returns 1
        coEvery { malariaRepo.getMalariaConfiremedDetailsFromServer() } returns 1

        worker().doWork()

        verify { preferenceDao.getFirstSyncLastSyncedPage() }
    }

    @Test
    fun doWork_doesNotCallFirstSyncLastSyncedPage_whenLastSyncedTimestampIsNotDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp + 1
        coEvery { malariaRepo.getMalariaScreeningDetailsFromServer() } returns 1
        coEvery { malariaRepo.getIRSScreeningDetailsFromServer() } returns 1
        coEvery { malariaRepo.getMalariaConfiremedDetailsFromServer() } returns 1

        worker().doWork()

        verify(exactly = 0) { preferenceDao.getFirstSyncLastSyncedPage() }
    }
}

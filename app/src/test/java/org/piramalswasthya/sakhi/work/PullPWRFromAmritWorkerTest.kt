package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
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
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullPWRFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val maternalHealthRepo: MaternalHealthRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullPWRFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullPWRFromAmritWorker(context, params, maternalHealthRepo, preferenceDao)
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
        coEvery { maternalHealthRepo.getPwrDetailsFromServer() } returns 0
        coEvery { maternalHealthRepo.getAncVisitDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenAllRepoCallsReturnOne() = runTest {
        coEvery { maternalHealthRepo.getPwrDetailsFromServer() } returns 1
        coEvery { maternalHealthRepo.getAncVisitDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenARepoCallReturnsUnexpectedValue() = runTest {
        coEvery { maternalHealthRepo.getPwrDetailsFromServer() } returns 2
        coEvery { maternalHealthRepo.getAncVisitDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "Pull operation returned incomplete results",
            (result as ListenableWorker.Result.Failure).outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenARepoCallThrowsException() = runTest {
        coEvery { maternalHealthRepo.getPwrDetailsFromServer() } throws RuntimeException("boom")
        coEvery { maternalHealthRepo.getAncVisitDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenBothRepoCallsReturnUnexpectedValues() = runTest {
        coEvery { maternalHealthRepo.getPwrDetailsFromServer() } returns 5
        coEvery { maternalHealthRepo.getAncVisitDetailsFromServer() } returns 7

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "PullPWRFromAmritWorker",
            (result as ListenableWorker.Result.Failure).outputData.getString("worker_name")
        )
    }
}

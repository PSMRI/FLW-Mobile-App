package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.coEvery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.HbncRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PushChildHBNCFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val hbncRepo: HbncRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PushChildHBNCFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PushChildHBNCFromAmritWorker(context, params, hbncRepo, preferenceDao)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun workerName_isNotBlank() {
        assertTrue(worker().workerName.isNotBlank())
    }

    @Test
    fun doWork_failsWithoutRunning_whenMaxRetriesExceeded() = runTest {
        val result = worker(attempt = 9).doWork()
        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            worker().workerName,
            (result as ListenableWorker.Result.Failure)
                .outputData.getString("worker_name")
        )
    }

    @Test
    fun doWork_reportsMaxRetryReason_whenMaxRetriesExceeded() = runTest {
        val result = worker(attempt = 5).doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Max retries (5) exceeded",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    @Test
    fun doSyncWork_returnsSuccess_whenRepoReturnsOne() = runTest {
        coEvery { hbncRepo.pushHBNCDetails() } returns 1
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_stampsLastSyncedTime_whenRepoReturnsOne() = runTest {
        coEvery { hbncRepo.pushHBNCDetails() } returns 1
        worker().doWork()
        verify { preferenceDao.setLastSyncedTimeStamp(any()) }
    }

    @Test
    fun doSyncWork_reportsSyncFalse_whenRepoReturnsZero() = runTest {
        coEvery { hbncRepo.pushHBNCDetails() } returns 0
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Sync operation returned false", result.outputData.getString("error"))
    }

    @Test
    fun doSyncWork_reportsWorkerName_whenRepoReturnsZero() = runTest {
        coEvery { hbncRepo.pushHBNCDetails() } returns 0
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(worker().workerName, result.outputData.getString("worker_name"))
    }

    @Test
    fun doSyncWork_returnsSuccess_whenRepoThrows_becauseInnerFailureIsSwallowed() = runTest {
        coEvery { hbncRepo.pushHBNCDetails() } throws RuntimeException("network down")
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }
}

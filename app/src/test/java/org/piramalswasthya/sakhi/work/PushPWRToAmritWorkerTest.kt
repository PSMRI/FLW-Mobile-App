package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
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
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PushPWRToAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val maternalHealthRepo: MaternalHealthRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PushPWRToAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PushPWRToAmritWorker(context, params, maternalHealthRepo, preferenceDao)
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
    fun doSyncWork_returnsSuccess_whenBothStepsSucceed() = runTest {
        coEvery { maternalHealthRepo.processNewPwr() } returns true
        coEvery { maternalHealthRepo.processNewAncVisit() } returns true
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_returnsFailure_whenFirstStepFails() = runTest {
        coEvery { maternalHealthRepo.processNewPwr() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Sync operation returned false", result.outputData.getString("error"))
    }

    @Test
    fun doSyncWork_skipsSecondStep_whenFirstStepFails() = runTest {
        coEvery { maternalHealthRepo.processNewPwr() } returns false
        worker().doWork()
        coVerify(exactly = 0) { maternalHealthRepo.processNewAncVisit() }
    }

    @Test
    fun doSyncWork_returnsFailure_whenSecondStepFails() = runTest {
        coEvery { maternalHealthRepo.processNewPwr() } returns true
        coEvery { maternalHealthRepo.processNewAncVisit() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(worker().workerName, result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { maternalHealthRepo.processNewPwr() } throws java.net.SocketTimeoutException("timeout")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenIOExceptionThrown() = runTest {
        coEvery { maternalHealthRepo.processNewPwr() } throws java.io.IOException("offline")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailureWithMessage_whenGenericExceptionThrown() = runTest {
        coEvery { maternalHealthRepo.processNewPwr() } throws IllegalStateException("boom")
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("boom", result.outputData.getString("error"))
    }
}

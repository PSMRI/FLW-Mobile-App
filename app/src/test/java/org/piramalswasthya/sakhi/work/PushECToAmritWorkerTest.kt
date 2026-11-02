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
import org.piramalswasthya.sakhi.repositories.EcrRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PushECToAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val ecrRepo: EcrRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PushECToAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PushECToAmritWorker(context, params, ecrRepo, preferenceDao)
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
        coEvery { ecrRepo.pushAndUpdateEcrRecord() } returns true
        coEvery { ecrRepo.pushAndUpdateEctRecord() } returns true
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_returnsFailure_whenFirstStepFails() = runTest {
        coEvery { ecrRepo.pushAndUpdateEcrRecord() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Sync operation returned false", result.outputData.getString("error"))
    }

    @Test
    fun doSyncWork_skipsSecondStep_whenFirstStepFails() = runTest {
        coEvery { ecrRepo.pushAndUpdateEcrRecord() } returns false
        worker().doWork()
        coVerify(exactly = 0) { ecrRepo.pushAndUpdateEctRecord() }
    }

    @Test
    fun doSyncWork_returnsFailure_whenSecondStepFails() = runTest {
        coEvery { ecrRepo.pushAndUpdateEcrRecord() } returns true
        coEvery { ecrRepo.pushAndUpdateEctRecord() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(worker().workerName, result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { ecrRepo.pushAndUpdateEcrRecord() } throws java.net.SocketTimeoutException("timeout")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenIOExceptionThrown() = runTest {
        coEvery { ecrRepo.pushAndUpdateEcrRecord() } throws java.io.IOException("offline")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailureWithMessage_whenGenericExceptionThrown() = runTest {
        coEvery { ecrRepo.pushAndUpdateEcrRecord() } throws IllegalStateException("boom")
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("boom", result.outputData.getString("error"))
    }
}

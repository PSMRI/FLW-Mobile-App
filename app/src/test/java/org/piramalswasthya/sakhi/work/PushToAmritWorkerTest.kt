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
import org.piramalswasthya.sakhi.repositories.BenRepo
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class PushToAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val benRepo: BenRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PushToAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PushToAmritWorker(context, params, benRepo, preferenceDao)
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
    fun doWork_returnsSuccess_whenProcessNewBenSucceeds() = runTest {
        coEvery { benRepo.processNewBen() } returns true
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailureWithSyncFalseMessage_whenProcessNewBenFails() = runTest {
        coEvery { benRepo.processNewBen() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("PushToAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("Sync operation returned false", result.outputData.getString("error"))
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { benRepo.processNewBen() } throws SocketTimeoutException("timeout")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenIOExceptionThrown() = runTest {
        coEvery { benRepo.processNewBen() } throws IOException("network down")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailureWithExceptionMessage_whenGenericExceptionThrown() = runTest {
        coEvery { benRepo.processNewBen() } throws RuntimeException("boom")
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("PushToAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("boom", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsFailureWithUnknownError_whenExceptionMessageIsNull() = runTest {
        coEvery { benRepo.processNewBen() } throws RuntimeException()
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Unknown error", result.outputData.getString("error"))
    }
}

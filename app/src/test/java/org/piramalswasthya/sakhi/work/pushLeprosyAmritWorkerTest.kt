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
import org.piramalswasthya.sakhi.repositories.LeprosyRepo
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class pushLeprosyAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val leprosyRepo: LeprosyRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): pushLeprosyAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return pushLeprosyAmritWorker(context, params, leprosyRepo, preferenceDao)
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
    fun doSyncWork_returnsSuccess_whenPushUnSyncedRecordsReturnsTrue() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } returns true

        val result = worker().doSyncWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_returnsFailureWithErrorData_whenPushUnSyncedRecordsReturnsFalse() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } returns false

        val result = worker().doSyncWork() as ListenableWorker.Result.Failure

        assertEquals("pushLeprosyAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("Sync operation returned false", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsSuccess_whenSyncSucceeds() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_retries_whenSyncThrowsSocketTimeoutException() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } throws SocketTimeoutException("timed out")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenSyncThrowsIOException() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } throws IOException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailureWithMessage_whenSyncThrowsGenericException() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } throws RuntimeException("unexpected failure")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("pushLeprosyAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("unexpected failure", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsFailureWithUnknownErrorMessage_whenSyncThrowsExceptionWithNoMessage() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } throws RuntimeException()

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("Unknown error", result.outputData.getString("error"))
    }

    @Test
    fun doSyncWork_propagatesException_whenPushUnSyncedRecordsThrows() = runTest {
        coEvery { leprosyRepo.pushUnSyncedRecords() } throws RuntimeException("boom")

        var thrown: Throwable? = null
        try {
            worker().doSyncWork()
        } catch (e: Throwable) {
            thrown = e
        }

        assertTrue(thrown is RuntimeException)
        assertEquals("boom", thrown?.message)
    }
}

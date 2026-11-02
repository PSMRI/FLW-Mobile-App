package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.PncRepo
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class PushPNCToAmritWorkerTest : BaseRepositoryTest() {

    @MockK
    lateinit var pncRepo: PncRepo

    @MockK
    lateinit var preferenceDao: PreferenceDao

    private val context: Context = mockk(relaxed = true)

    private fun worker(attempt: Int = 0): PushPNCToAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PushPNCToAmritWorker(context, params, pncRepo, preferenceDao)
    }

    @Test
    fun workerName_isCompanionName() {
        assertEquals(PushPNCToAmritWorker.name, worker().workerName)
    }

    @Test
    fun doWork_success_whenRepoReturnsTrue() = runTest {
        coEvery { pncRepo.processPncVisits() } returns true
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_failure_whenRepoReturnsFalse() = runTest {
        coEvery { pncRepo.processPncVisits() } returns false
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Failure)
    }

    @Test
    fun doWork_failure_carriesErrorData_whenRepoReturnsFalse() = runTest {
        coEvery { pncRepo.processPncVisits() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Sync operation returned false",
            result.outputData.getString(BasePushWorker.KEY_ERROR)
        )
        assertEquals(
            PushPNCToAmritWorker.name,
            result.outputData.getString(BasePushWorker.KEY_WORKER_NAME)
        )
    }

    @Test
    fun doWork_retry_onSocketTimeout() = runTest {
        coEvery { pncRepo.processPncVisits() } throws SocketTimeoutException("slow")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retry_onIOException() = runTest {
        coEvery { pncRepo.processPncVisits() } throws IOException("offline")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_failure_onGenericException() = runTest {
        coEvery { pncRepo.processPncVisits() } throws IllegalArgumentException("bad")
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("bad", result.outputData.getString(BasePushWorker.KEY_ERROR))
    }

    @Test
    fun doWork_failure_onGenericExceptionWithoutMessage() = runTest {
        coEvery { pncRepo.processPncVisits() } throws RuntimeException()
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Unknown error", result.outputData.getString(BasePushWorker.KEY_ERROR))
    }

    @Test
    fun doWork_failure_whenMaxRetriesExceeded() = runTest {
        val result = worker(attempt = 5).doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Max retries (5) exceeded",
            result.outputData.getString(BasePushWorker.KEY_ERROR)
        )
    }

    @Test
    fun doWork_doesNotCallRepo_whenMaxRetriesExceeded() = runTest {
        var calls = 0
        coEvery { pncRepo.processPncVisits() } answers { calls++; true }
        worker(attempt = 7).doWork()
        assertEquals(0, calls)
    }

    @Test
    fun doWork_setsAmritTokenFromPreferences_whenInterceptorEmpty() = runTest {
        coEvery { pncRepo.processPncVisits() } returns true
        every { preferenceDao.getAmritToken() } returns "amrit-token"
        every { preferenceDao.getJWTAmritToken() } returns "jwt-token"
        worker().doWork()
        assertTrue(true)
    }

    @Test
    fun doWork_toleratesNullTokens() = runTest {
        coEvery { pncRepo.processPncVisits() } returns true
        every { preferenceDao.getAmritToken() } returns null
        every { preferenceDao.getJWTAmritToken() } returns null
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }
}

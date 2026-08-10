package org.piramalswasthya.sakhi.work

import android.content.Context
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
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class NCDReferPushtoAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val referalRepo: NcdReferalRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
        TokenInsertTmcInterceptor.setToken("")
        TokenInsertTmcInterceptor.setJwt("")
    }

    private fun worker(attempt: Int = 0): NCDReferPushtoAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return NCDReferPushtoAmritWorker(context, params, referalRepo, preferenceDao)
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
    fun doWork_returnsSuccess_whenPushSucceeds() = runTest {
        coEvery { referalRepo.pushAndUpdateNCDReferRecord() } returns Unit
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsRetry_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { referalRepo.pushAndUpdateNCDReferRecord() } throws SocketTimeoutException("timed out")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsRetry_whenIOExceptionThrown() = runTest {
        coEvery { referalRepo.pushAndUpdateNCDReferRecord() } throws IOException("network error")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailureWithMessage_whenGenericExceptionThrown() = runTest {
        coEvery { referalRepo.pushAndUpdateNCDReferRecord() } throws RuntimeException("push failed")
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("push failed", result.outputData.getString("error"))
        assertEquals(
            worker().workerName,
            result.outputData.getString("worker_name")
        )
    }

    @Test
    fun doWork_returnsFailureWithUnknownError_whenExceptionHasNoMessage() = runTest {
        coEvery { referalRepo.pushAndUpdateNCDReferRecord() } throws RuntimeException()
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Unknown error", result.outputData.getString("error"))
    }

    @Test
    fun doWork_initializesAmritToken_whenTokenNotYetSet() = runTest {
        every { preferenceDao.getAmritToken() } returns "amrit-token"
        every { preferenceDao.getJWTAmritToken() } returns "jwt-token"
        coEvery { referalRepo.pushAndUpdateNCDReferRecord() } returns Unit
        worker().doWork()
        assertEquals("amrit-token", TokenInsertTmcInterceptor.getToken())
        assertEquals("jwt-token", TokenInsertTmcInterceptor.getJwt())
    }

    @Test
    fun doWork_invokesPushAndUpdateNCDReferRecord_exactlyOnce() = runTest {
        coEvery { referalRepo.pushAndUpdateNCDReferRecord() } returns Unit
        worker().doWork()
        coVerify(exactly = 1) { referalRepo.pushAndUpdateNCDReferRecord() }
    }
}

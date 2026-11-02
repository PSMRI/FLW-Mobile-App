package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
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
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class BasePushWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private var syncAction: suspend () -> ListenableWorker.Result = { ListenableWorker.Result.success() }

    private inner class TestWorker(
        appContext: Context,
        params: WorkerParameters,
        override val preferenceDao: PreferenceDao
    ) : BasePushWorker(appContext, params) {
        override val workerName = "TestPushWorker"
        override suspend fun doSyncWork(): Result = syncAction()
    }

    @Before
    fun stubTokens() {
        TokenInsertTmcInterceptor.setToken("")
        TokenInsertTmcInterceptor.setJwt("")
        syncAction = { ListenableWorker.Result.success() }
    }

    private fun worker(attempt: Int = 0): TestWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return TestWorker(context, params, preferenceDao)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_failsWithoutRunning_whenMaxRetriesExceeded() = runTest {
        val result = worker(attempt = 5).doWork()
        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "TestPushWorker",
            (result as ListenableWorker.Result.Failure).outputData.getString("worker_name")
        )
        assertEquals(
            "Max retries (5) exceeded",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_runsSyncWork_whenBelowMaxRetries() = runTest {
        val result = worker(attempt = 4).doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenDoSyncWorkSucceeds() = runTest {
        syncAction = { ListenableWorker.Result.success() }
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        syncAction = { throw SocketTimeoutException("timeout") }
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenIOExceptionThrown() = runTest {
        syncAction = { throw IOException("network down") }
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailureWithExceptionMessage_whenGenericExceptionThrown() = runTest {
        syncAction = { throw RuntimeException("boom") }
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("TestPushWorker", result.outputData.getString("worker_name"))
        assertEquals("boom", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsFailureWithUnknownError_whenExceptionMessageIsNull() = runTest {
        syncAction = { throw RuntimeException() }
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Unknown error", result.outputData.getString("error"))
    }

    @Test
    fun doWork_initializesAmritToken_whenTokenNotYetSet() = runTest {
        every { preferenceDao.getAmritToken() } returns "amrit-token"
        every { preferenceDao.getJWTAmritToken() } returns "jwt-token"
        worker().doWork()
        assertEquals("amrit-token", TokenInsertTmcInterceptor.getToken())
        assertEquals("jwt-token", TokenInsertTmcInterceptor.getJwt())
    }

    @Test
    fun doWork_doesNotOverwriteToken_whenAlreadySet() = runTest {
        TokenInsertTmcInterceptor.setToken("existing-token")
        TokenInsertTmcInterceptor.setJwt("existing-jwt")
        worker().doWork()
        assertEquals("existing-token", TokenInsertTmcInterceptor.getToken())
        assertEquals("existing-jwt", TokenInsertTmcInterceptor.getJwt())
    }

    @Test
    fun getForegroundInfo_returnsForegroundInfo() = runTest {
        mockkConstructor(NotificationCompat.Builder::class)
        every { anyConstructed<NotificationCompat.Builder>().setContentTitle(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setContentText(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any<Int>()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setProgress(any(), any(), any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().setOngoing(any()) } answers { self as NotificationCompat.Builder }
        every { anyConstructed<NotificationCompat.Builder>().build() } returns mockk(relaxed = true)

        val result = worker().getForegroundInfo()

        assertNotNull(result)
    }
}

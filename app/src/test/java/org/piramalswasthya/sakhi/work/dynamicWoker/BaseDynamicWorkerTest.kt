package org.piramalswasthya.sakhi.work.dynamicWoker

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
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class BaseDynamicWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private var syncAction: suspend () -> ListenableWorker.Result = { ListenableWorker.Result.success() }

    private inner class TestWorker(
        appContext: Context,
        params: WorkerParameters,
        override val preferenceDao: PreferenceDao
    ) : BaseDynamicWorker(appContext, params) {
        override val workerName = "TestDynamicWorker"
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
            "TestDynamicWorker",
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
    fun doWork_returnsFailure_whenIllegalStateExceptionThrown() = runTest {
        syncAction = { throw IllegalStateException("No user logged in") }
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("TestDynamicWorker", result.outputData.getString("worker_name"))
        assertEquals("No user logged in", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsFailureWithDefaultMessage_whenIllegalStateExceptionHasNoMessage() = runTest {
        syncAction = { throw IllegalStateException() }
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("IllegalStateException", result.outputData.getString("error"))
    }

    @Test
    fun doWork_retries_whenUnknownHostExceptionThrown() = runTest {
        syncAction = { throw UnknownHostException() }
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        syncAction = { throw SocketTimeoutException("timeout") }
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenUnexpectedExceptionThrown() = runTest {
        syncAction = { throw RuntimeException("boom") }
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Retry)
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

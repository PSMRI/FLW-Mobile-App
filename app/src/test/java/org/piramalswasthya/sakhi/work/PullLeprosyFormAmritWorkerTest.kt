package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.core.app.NotificationCompat
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.repositories.LeprosyRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullLeprosyFormAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val leprosyRepo: LeprosyRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullLeprosyFormAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullLeprosyFormAmritWorker(context, params, leprosyRepo, preferenceDao)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    private fun stubAllLeprosyCallsSuccess(value: Int = 1) {
        coEvery { leprosyRepo.getLeprosyScreeningDetailsFromServer() } returns value
        coEvery { leprosyRepo.getAllLeprosyDataFromServer() } returns value
        coEvery { leprosyRepo.getAllLeprosyFollowUpDataFromServer() } returns value
    }

    @Test
    fun doWork_returnsSuccess_whenAllLeprosyCallsReturnOne() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenAllLeprosyCallsReturnZero() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(0)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenLeprosyScreeningReturnsUnexpectedValue() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)
        coEvery { leprosyRepo.getLeprosyScreeningDetailsFromServer() } returns -1

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsFailure_whenLeprosyListReturnsUnexpectedValue() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)
        coEvery { leprosyRepo.getAllLeprosyDataFromServer() } returns 5

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_returnsFailure_whenLeprosyFollowUpReturnsUnexpectedValue() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)
        coEvery { leprosyRepo.getAllLeprosyFollowUpDataFromServer() } returns 5

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_returnsFailure_whenLeprosyScreeningThrowsException() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)
        coEvery { leprosyRepo.getLeprosyScreeningDetailsFromServer() } throws RuntimeException("network error")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsFailure_whenLeprosyListThrowsException() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)
        coEvery { leprosyRepo.getAllLeprosyDataFromServer() } throws RuntimeException("network error")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_returnsFailure_whenLeprosyFollowUpThrowsException() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)
        coEvery { leprosyRepo.getAllLeprosyFollowUpDataFromServer() } throws RuntimeException("network error")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_returnsFailure_whenSQLiteConstraintExceptionCaughtInternally() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        stubAllLeprosyCallsSuccess(1)
        coEvery { leprosyRepo.getAllLeprosyFollowUpDataFromServer() } throws SQLiteConstraintException("constraint violation")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_usesFirstSyncLastSyncedPage_whenLastSyncedTimestampIsDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        every { preferenceDao.getFirstSyncLastSyncedPage() } returns 2
        stubAllLeprosyCallsSuccess(1)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        verify(exactly = 1) { preferenceDao.getFirstSyncLastSyncedPage() }
    }

    @Test
    fun doWork_returnsFailure_whenPreferenceDaoThrowsExceptionBeforeSync() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } throws RuntimeException("boom")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("boom", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsFailure_withUnknownError_whenExceptionHasNoMessage() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } throws RuntimeException()

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullLeprosyFormAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("Unknown error", result.outputData.getString("error"))
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
        every { context.getString(any()) } returns "channel_id"

        val result = worker().getForegroundInfo()

        assertNotNull(result)
    }
}

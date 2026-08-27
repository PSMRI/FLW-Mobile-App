package org.piramalswasthya.sakhi.work

import android.content.Context
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
import org.piramalswasthya.sakhi.repositories.KalaAzarRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullKalaAzarFormAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val kalaAzarRepo: KalaAzarRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullKalaAzarFormAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullKalaAzarFormAmritWorker(context, params, kalaAzarRepo, preferenceDao)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    @Test
    fun doWork_returnsSuccess_whenServerReturnsOne() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { kalaAzarRepo.getKalaAzarScreeningDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenServerReturnsZero() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { kalaAzarRepo.getKalaAzarScreeningDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenServerReturnsUnexpectedValue() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { kalaAzarRepo.getKalaAzarScreeningDetailsFromServer() } returns 2

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals(
            "PullKalaAzarFormAmritWorker",
            result.outputData.getString("worker_name")
        )
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenRepoThrowsException_becauseExceptionIsSwallowedInternally() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
        coEvery { kalaAzarRepo.getKalaAzarScreeningDetailsFromServer() } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_usesFirstSyncLastSyncedPage_whenLastSyncedTimestampIsDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        every { preferenceDao.getFirstSyncLastSyncedPage() } returns 3
        coEvery { kalaAzarRepo.getKalaAzarScreeningDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        verify(exactly = 1) { preferenceDao.getFirstSyncLastSyncedPage() }
    }

    @Test
    fun doWork_doesNotUseFirstSyncLastSyncedPage_whenLastSyncedTimestampIsNotDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns 12345L
        coEvery { kalaAzarRepo.getKalaAzarScreeningDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        verify(exactly = 0) { preferenceDao.getFirstSyncLastSyncedPage() }
    }

    @Test
    fun doWork_returnsFailure_whenUnexpectedExceptionOccursBeforeSync() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } throws RuntimeException("boom")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullKalaAzarFormAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("boom", result.outputData.getString("error"))
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

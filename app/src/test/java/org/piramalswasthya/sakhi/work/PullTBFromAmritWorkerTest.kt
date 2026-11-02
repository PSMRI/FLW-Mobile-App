package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
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
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.repositories.TBRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullTBFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val tbRepo: TBRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullTBFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullTBFromAmritWorker(context, params, tbRepo, preferenceDao)
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
    fun doWork_returnsSuccess_whenAllServerCallsReturnZero() = runTest {
        coEvery { tbRepo.getTBScreeningDetailsFromServer() } returns 0
        coEvery { tbRepo.getTbSuspectedDetailsFromServer() } returns 0
        coEvery { tbRepo.getTbConfirmedDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenAllServerCallsReturnOne() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        coEvery { tbRepo.getTBScreeningDetailsFromServer() } returns 1
        coEvery { tbRepo.getTbSuspectedDetailsFromServer() } returns 1
        coEvery { tbRepo.getTbConfirmedDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenScreeningReturnsUnexpectedValue() = runTest {
        coEvery { tbRepo.getTBScreeningDetailsFromServer() } returns 5
        coEvery { tbRepo.getTbSuspectedDetailsFromServer() } returns 0
        coEvery { tbRepo.getTbConfirmedDetailsFromServer() } returns 0

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullTBFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsFailure_whenMultipleServerCallsReturnUnexpectedValues() = runTest {
        coEvery { tbRepo.getTBScreeningDetailsFromServer() } returns 0
        coEvery { tbRepo.getTbSuspectedDetailsFromServer() } returns 9
        coEvery { tbRepo.getTbConfirmedDetailsFromServer() } returns -2

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
    }

    @Test
    fun doWork_returnsSuccess_whenScreeningThrowsException() = runTest {
        coEvery { tbRepo.getTBScreeningDetailsFromServer() } throws RuntimeException("screening down")
        coEvery { tbRepo.getTbSuspectedDetailsFromServer() } returns 0
        coEvery { tbRepo.getTbConfirmedDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenSuspectedThrowsException() = runTest {
        coEvery { tbRepo.getTbSuspectedDetailsFromServer() } throws RuntimeException("suspected down")
        coEvery { tbRepo.getTBScreeningDetailsFromServer() } returns 0
        coEvery { tbRepo.getTbConfirmedDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenConfirmedThrowsException() = runTest {
        coEvery { tbRepo.getTbConfirmedDetailsFromServer() } throws RuntimeException("confirmed down")
        coEvery { tbRepo.getTBScreeningDetailsFromServer() } returns 0
        coEvery { tbRepo.getTbSuspectedDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
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

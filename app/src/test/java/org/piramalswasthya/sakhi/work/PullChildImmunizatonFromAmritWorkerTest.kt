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
import org.piramalswasthya.sakhi.repositories.ImmunizationRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullChildImmunizatonFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val immunizationRepo: ImmunizationRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullChildImmunizatonFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullChildImmunizatonFromAmritWorker(context, params, immunizationRepo, preferenceDao)
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
        coEvery { immunizationRepo.getImmunizationDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenServerReturnsZero() = runTest {
        coEvery { immunizationRepo.getImmunizationDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenServerReturnsUnexpectedValue() = runTest {
        coEvery { immunizationRepo.getImmunizationDetailsFromServer() } returns 2

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals(
            "PullChildImmunizatonFromAmritWorker",
            result.outputData.getString("worker_name")
        )
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenRepoThrowsException_becauseExceptionIsSwallowedInternally() = runTest {
        coEvery { immunizationRepo.getImmunizationDetailsFromServer() } throws RuntimeException("boom")

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

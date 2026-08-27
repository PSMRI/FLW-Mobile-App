package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.piramalswasthya.sakhi.repositories.IncentiveRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullIncentiveWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val incentiveRepo: IncentiveRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullIncentiveWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullIncentiveWorker(context, params, incentiveRepo, preferenceDao)
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
    fun doWork_returnsFailureWithUserNotFound_whenNoLoggedInUser() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("User not found", result.outputData.getString("result"))
    }

    @Test
    fun doWork_returnsFailure_whenPullIncentiveActivitiesFails() = runTest {
        coEvery { incentiveRepo.pullAndSaveAllIncentiveActivities(any()) } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Network Call failed act. Check in logcat",
            result.outputData.getString("result")
        )
    }

    @Test
    fun doWork_returnsFailure_whenPullIncentiveRecordsFails() = runTest {
        coEvery { incentiveRepo.pullAndSaveAllIncentiveActivities(any()) } returns true
        coEvery { incentiveRepo.pullAndSaveAllIncentiveRecords(any()) } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Network Call failed rec. Check in logcat",
            result.outputData.getString("result")
        )
    }

    @Test
    fun doWork_returnsSuccessAndUpdatesTimestamp_whenBothPullsSucceed() = runTest {
        coEvery { incentiveRepo.pullAndSaveAllIncentiveActivities(any()) } returns true
        coEvery { incentiveRepo.pullAndSaveAllIncentiveRecords(any()) } returns true
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
        verify { preferenceDao.lastIncentivePullTimestamp = any() }
    }
}

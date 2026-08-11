package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
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
import org.piramalswasthya.sakhi.repositories.HRPRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullHRPFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val hrpRepo: HRPRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullHRPFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullHRPFromAmritWorker(context, params, hrpRepo, preferenceDao)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    private fun stubAllHrpCallsSuccess(value: Int = 1) {
        coEvery { hrpRepo.getHighRiskAssessDetailsFromServer() } returns value
        coEvery { hrpRepo.getHRPTrackDetailsFromServer() } returns value
        coEvery { hrpRepo.getHRNonPTrackDetailsFromServer() } returns value
        coEvery { hrpRepo.getHighRiskAssessMicroBirthPlanDetailsFromServer() } returns value
    }

    @Test
    fun doWork_returnsSuccess_whenAllHrpCallsReturnOne() = runTest {
        stubAllHrpCallsSuccess(1)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenAllHrpCallsReturnZero() = runTest {
        stubAllHrpCallsSuccess(0)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenHighRiskAssessReturnsUnexpectedValue() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHighRiskAssessDetailsFromServer() } returns -1

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullHRPFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsFailure_whenHrpTrackReturnsUnexpectedValue() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHRPTrackDetailsFromServer() } returns 5

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullHRPFromAmritWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_returnsFailure_whenHrNonPTrackReturnsUnexpectedValue() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHRNonPTrackDetailsFromServer() } returns 5

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullHRPFromAmritWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_returnsFailure_whenMicroBirthPlanReturnsUnexpectedValue() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHighRiskAssessMicroBirthPlanDetailsFromServer() } returns 5

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullHRPFromAmritWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_returnsSuccess_whenHighRiskAssessThrowsExceptionCaughtInternally() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHighRiskAssessDetailsFromServer() } throws RuntimeException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenHrpTrackThrowsExceptionCaughtInternally() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHRPTrackDetailsFromServer() } throws RuntimeException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenHrNonPTrackThrowsExceptionCaughtInternally() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHRNonPTrackDetailsFromServer() } throws RuntimeException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenMicroBirthPlanThrowsExceptionCaughtInternally() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHighRiskAssessMicroBirthPlanDetailsFromServer() } throws RuntimeException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenSQLiteConstraintExceptionCaughtInternally() = runTest {
        stubAllHrpCallsSuccess(1)
        coEvery { hrpRepo.getHRPTrackDetailsFromServer() } throws SQLiteConstraintException("constraint violation")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }
}

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
import org.piramalswasthya.sakhi.repositories.PncRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullPNCFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val pncRepo: PncRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullPNCFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullPNCFromAmritWorker(context, params, pncRepo, preferenceDao)
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
    fun doWork_returnsSuccess_whenPncRepoReturnsOne() = runTest {
        coEvery { pncRepo.getPncVisitsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenPncRepoReturnsZero() = runTest {
        coEvery { pncRepo.getPncVisitsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenPncRepoReturnsUnexpectedValue() = runTest {
        coEvery { pncRepo.getPncVisitsFromServer() } returns -1

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullPNCFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenPncRepoThrowsExceptionCaughtInternally() = runTest {
        coEvery { pncRepo.getPncVisitsFromServer() } throws RuntimeException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenSQLiteConstraintExceptionCaughtInternally() = runTest {
        coEvery { pncRepo.getPncVisitsFromServer() } throws SQLiteConstraintException("constraint violation")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }
}

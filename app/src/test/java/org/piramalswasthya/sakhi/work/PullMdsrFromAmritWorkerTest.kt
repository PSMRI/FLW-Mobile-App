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
import org.piramalswasthya.sakhi.repositories.MdsrRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullMdsrFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val mdsrRepo: MdsrRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullMdsrFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullMdsrFromAmritWorker(context, params, mdsrRepo, preferenceDao)
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
    fun doWork_returnsSuccess_whenMdsrRepoReturnsOne() = runTest {
        coEvery { mdsrRepo.getMdsrFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenMdsrRepoReturnsZero() = runTest {
        coEvery { mdsrRepo.getMdsrFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenMdsrRepoReturnsUnexpectedValue() = runTest {
        coEvery { mdsrRepo.getMdsrFromServer() } returns -1

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullMdsrFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenMdsrRepoThrowsExceptionCaughtInternally() = runTest {
        coEvery { mdsrRepo.getMdsrFromServer() } throws RuntimeException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenSQLiteConstraintExceptionCaughtInternally() = runTest {
        coEvery { mdsrRepo.getMdsrFromServer() } throws SQLiteConstraintException("constraint violation")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }
}

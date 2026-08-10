package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
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
import org.piramalswasthya.sakhi.repositories.HbycRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullHBYCFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val hbycRepo: HbycRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PullHBYCFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullHBYCFromAmritWorker(context, params, hbycRepo, preferenceDao)
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
    fun doWork_returnsSuccess_whenHbycRepoReturnsOne() = runTest {
        coEvery { hbycRepo.getHBYCDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        verify { preferenceDao.setLastSyncedTimeStamp(any()) }
    }

    @Test
    fun doWork_returnsSuccess_whenHbycRepoReturnsZero() = runTest {
        coEvery { hbycRepo.getHBYCDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenHbycRepoReturnsUnexpectedValue() = runTest {
        coEvery { hbycRepo.getHBYCDetailsFromServer() } returns 2

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullHBYCFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenHbycRepoThrowsExceptionCaughtInternally() = runTest {
        coEvery { hbycRepo.getHBYCDetailsFromServer() } throws RuntimeException("network error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenTimestampPersistHitsSqliteConstraint() = runTest {
        coEvery { hbycRepo.getHBYCDetailsFromServer() } returns 1
        every {
            preferenceDao.setLastSyncedTimeStamp(any())
        } throws SQLiteConstraintException("unique constraint failed")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullHBYCFromAmritWorker", result.outputData.getString("worker_name"))
        assertTrue(result.outputData.getString("error")!!.contains("SQLite constraint"))
    }

    @Test
    fun doWork_returnsFailure_whenTimestampPersistThrowsGenericException() = runTest {
        coEvery { hbycRepo.getHBYCDetailsFromServer() } returns 1
        every {
            preferenceDao.setLastSyncedTimeStamp(any())
        } throws RuntimeException("disk full")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullHBYCFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("disk full", result.outputData.getString("error"))
    }
}

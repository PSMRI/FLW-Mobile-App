package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.repositories.UwinRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullUwinFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val uwinRepo: UwinRepo = mockk(relaxed = true)

    private fun worker(attempt: Int = 0): PullUwinFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullUwinFromAmritWorker(context, params, uwinRepo)
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
    fun doWork_returnsSuccess_whenDownSyncSucceeds() = runTest {
        coEvery { uwinRepo.downSyncAndPersist() } just Runs

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenDownSyncThrowsException() = runTest {
        coEvery { uwinRepo.downSyncAndPersist() } throws RuntimeException("uwin sync failed")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullUwinFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsFailure_whenDownSyncThrowsSQLiteConstraintException() = runTest {
        coEvery { uwinRepo.downSyncAndPersist() } throws SQLiteConstraintException("constraint violation")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PullUwinFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
    }
}

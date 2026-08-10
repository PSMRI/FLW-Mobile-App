package org.piramalswasthya.sakhi.work

import android.content.Context
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
import org.piramalswasthya.sakhi.repositories.PmsmaRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullPmsmaFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val pmsmaRepo: PmsmaRepo = mockk(relaxed = true)

    private fun worker(attempt: Int = 0): PullPmsmaFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullPmsmaFromAmritWorker(context, params, pmsmaRepo)
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
    fun doWork_returnsSuccess_whenRepoReturnsZero() = runTest {
        coEvery { pmsmaRepo.getPmsmaDetailsFromServer() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenRepoReturnsOne() = runTest {
        coEvery { pmsmaRepo.getPmsmaDetailsFromServer() } returns 1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenRepoReturnsUnexpectedValue() = runTest {
        coEvery { pmsmaRepo.getPmsmaDetailsFromServer() } returns 2

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "Pull operation returned incomplete results",
            (result as ListenableWorker.Result.Failure).outputData.getString("error")
        )
        assertEquals(
            "PullPmsmaFromAmritWorker",
            result.outputData.getString("worker_name")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenRepoThrowsException() = runTest {
        coEvery { pmsmaRepo.getPmsmaDetailsFromServer() } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }
}

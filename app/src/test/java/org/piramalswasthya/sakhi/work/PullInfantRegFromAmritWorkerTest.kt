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
import org.piramalswasthya.sakhi.repositories.InfantRegRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullInfantRegFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val infantRegRepo: InfantRegRepo = mockk(relaxed = true)

    private fun worker(attempt: Int = 0): PullInfantRegFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullInfantRegFromAmritWorker(context, params, infantRegRepo)
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
        coEvery { infantRegRepo.getInfantRegFromServer() } returns 1
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenServerReturnsZero() = runTest {
        coEvery { infantRegRepo.getInfantRegFromServer() } returns 0
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenServerReturnsUnexpectedValue() = runTest {
        coEvery { infantRegRepo.getInfantRegFromServer() } returns 2
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("PullInfantRegFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("Pull operation returned incomplete results", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsSuccess_whenServerThrowsExceptionSwallowedInternally() = runTest {
        coEvery { infantRegRepo.getInfantRegFromServer() } throws RuntimeException("network error")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }
}

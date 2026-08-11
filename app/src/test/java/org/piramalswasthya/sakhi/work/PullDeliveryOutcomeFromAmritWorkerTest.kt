package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PullDeliveryOutcomeFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo = mockk(relaxed = true)

    private fun worker(attempt: Int = 0): PullDeliveryOutcomeFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PullDeliveryOutcomeFromAmritWorker(context, params, deliveryOutcomeRepo)
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
    fun doWork_returnsSuccess_whenServerReturnsZero() = runTest {
        coEvery { deliveryOutcomeRepo.getDeliveryOutcomesFromServer() } returns 0
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenServerReturnsOne() = runTest {
        coEvery { deliveryOutcomeRepo.getDeliveryOutcomesFromServer() } returns 1
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenServerReturnsUnexpectedValue() = runTest {
        coEvery { deliveryOutcomeRepo.getDeliveryOutcomesFromServer() } returns 9
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
        assertEquals(
            "PullDeliveryOutcomeFromAmritWorker",
            result.outputData.getString("worker_name")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenRepoThrowsException_becauseInnerFailureIsSwallowed() = runTest {
        coEvery { deliveryOutcomeRepo.getDeliveryOutcomesFromServer() } throws RuntimeException("network down")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_invokesGetDeliveryOutcomesFromServer_exactlyOnce() = runTest {
        coEvery { deliveryOutcomeRepo.getDeliveryOutcomesFromServer() } returns 0
        worker().doWork()
        coVerify(exactly = 1) { deliveryOutcomeRepo.getDeliveryOutcomesFromServer() }
    }
}

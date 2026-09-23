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
import org.piramalswasthya.sakhi.repositories.CbacRepo

@OptIn(ExperimentalCoroutinesApi::class)
class CbacPullFromAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val cbacRepo: CbacRepo = mockk(relaxed = true)

    private fun worker(attempt: Int = 0): CbacPullFromAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return CbacPullFromAmritWorker(context, params, cbacRepo)
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
    fun doWork_returnsSuccess_andSkipsPagedPulls_whenNoAdditionalPagesExist() = runTest {
        coEvery { cbacRepo.pullAndPersistCbacRecord() } returns 0

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { cbacRepo.pullAndPersistCbacRecord(any()) }
        coVerify(exactly = 0) { cbacRepo.pullAndPersistCbacRecord(1) }
    }

    @Test
    fun doWork_returnsSuccess_andSkipsPagedPulls_whenPageCountIsNegative() = runTest {
        coEvery { cbacRepo.pullAndPersistCbacRecord() } returns -1

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { cbacRepo.pullAndPersistCbacRecord(any()) }
        coVerify(exactly = 0) { cbacRepo.pullAndPersistCbacRecord(1) }
    }

    @Test
    fun doWork_returnsSuccess_andPullsEveryPage_whenMultiplePagesExist() = runTest {
        coEvery { cbacRepo.pullAndPersistCbacRecord() } returns 3

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { cbacRepo.pullAndPersistCbacRecord(1) }
        coVerify(exactly = 1) { cbacRepo.pullAndPersistCbacRecord(2) }
        coVerify(exactly = 1) { cbacRepo.pullAndPersistCbacRecord(3) }
    }

    @Test
    fun doWork_returnsFailure_whenInitialPullThrows() = runTest {
        coEvery {
            cbacRepo.pullAndPersistCbacRecord()
        } throws RuntimeException("network down")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("CbacPullFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("network down", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsFailure_withUnknownError_whenExceptionMessageIsNull() = runTest {
        coEvery {
            cbacRepo.pullAndPersistCbacRecord()
        } throws RuntimeException(null as String?)

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("Unknown error", result.outputData.getString("error"))
    }

    @Test
    fun doWork_returnsFailure_whenAPagedPullThrows() = runTest {
        coEvery { cbacRepo.pullAndPersistCbacRecord() } returns 2
        coEvery { cbacRepo.pullAndPersistCbacRecord(1) } throws RuntimeException("page failed")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("CbacPullFromAmritWorker", result.outputData.getString("worker_name"))
        assertEquals("page failed", result.outputData.getString("error"))
        coVerify(exactly = 0) { cbacRepo.pullAndPersistCbacRecord(2) }
    }
}

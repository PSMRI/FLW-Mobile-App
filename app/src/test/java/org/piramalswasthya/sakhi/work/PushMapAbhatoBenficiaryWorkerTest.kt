package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
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
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.network.ABHAProfile
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class PushMapAbhatoBenficiaryWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val benRepo: BenRepo = mockk(relaxed = true)
    private val abhaGenratedDao: ABHAGenratedDao = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val abhaIdRepo: AbhaIdRepo = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PushMapAbhatoBenficiaryWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PushMapAbhatoBenficiaryWorker(context, params, benRepo, abhaGenratedDao, preferenceDao, abhaIdRepo)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun workerName_isNotBlank() {
        assertTrue(worker().workerName.isNotBlank())
    }

    @Test
    fun doWork_failsWithoutRunning_whenMaxRetriesExceeded() = runTest {
        val result = worker(attempt = 9).doWork()
        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            worker().workerName,
            (result as ListenableWorker.Result.Failure)
                .outputData.getString("worker_name")
        )
    }

    @Test
    fun doWork_reportsMaxRetryReason_whenMaxRetriesExceeded() = runTest {
        val result = worker(attempt = 5).doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Max retries (5) exceeded",
            result.outputData.getString("error")
        )
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    private fun abhaModel(benId: Long) = ABHAModel(
        beneficiaryID = benId,
        beneficiaryRegID = benId,
        benName = "Test",
        createdBy = "asha1",
        message = "",
        txnId = "txn-$benId",
        abhaProfileJson = Gson().toJson(ABHAProfile()),
        providerServiceMapId = 1
    )

    @Test
    fun doSyncWork_returnsSuccess_whenNoUnsyncedAbhaRecords() = runTest {
        every { abhaGenratedDao.getAllAbha() } returns emptyList()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_returnsSuccess_andDeletesRecord_whenMappingSucceeds() = runTest {
        val model = abhaModel(1L)
        every { abhaGenratedDao.getAllAbha() } returns listOf(model)
        coEvery { benRepo.getBenFromId(1L) } returns mockk<BenRegCache>(relaxed = true)
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.Success("{\"status\":\"ok\"}")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { abhaGenratedDao.deleteAbhaByBenId(1L) }
    }

    @Test
    fun doSyncWork_returnsRetry_whenMappingReturnsError() = runTest {
        val model = abhaModel(1L)
        every { abhaGenratedDao.getAllAbha() } returns listOf(model)
        coEvery { benRepo.getBenFromId(1L) } returns mockk<BenRegCache>(relaxed = true)
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.Error(500, "server error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
        coVerify(exactly = 0) { abhaGenratedDao.deleteAbhaByBenId(any()) }
    }

    @Test
    fun doSyncWork_returnsRetry_whenMappingReturnsNetworkError() = runTest {
        val model = abhaModel(1L)
        every { abhaGenratedDao.getAllAbha() } returns listOf(model)
        coEvery { benRepo.getBenFromId(1L) } returns mockk<BenRegCache>(relaxed = true)
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } returns NetworkResult.NetworkError

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doSyncWork_returnsRetry_whenSocketTimeoutExceptionThrown() = runTest {
        val model = abhaModel(1L)
        every { abhaGenratedDao.getAllAbha() } returns listOf(model)
        coEvery { benRepo.getBenFromId(1L) } returns mockk<BenRegCache>(relaxed = true)
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } throws SocketTimeoutException("timeout")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doSyncWork_returnsFailure_whenGenericExceptionThrownDuringMapping() = runTest {
        val model = abhaModel(1L)
        every { abhaGenratedDao.getAllAbha() } returns listOf(model)
        coEvery { benRepo.getBenFromId(1L) } returns mockk<BenRegCache>(relaxed = true)
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(any(), any()) } throws RuntimeException("network gone")

        val result = worker().doWork() as ListenableWorker.Result.Failure

        assertEquals("PushMapAbhatoBenficiaryWorker", result.outputData.getString("worker_name"))
        assertEquals("ABHA sync failed: network gone", result.outputData.getString("error"))
    }

    @Test
    fun doSyncWork_returnsRetry_whenOneOfMultipleRecordsFails() = runTest {
        val model1 = abhaModel(1L)
        val model2 = abhaModel(2L)
        every { abhaGenratedDao.getAllAbha() } returns listOf(model1, model2)
        coEvery { benRepo.getBenFromId(1L) } returns mockk<BenRegCache>(relaxed = true)
        coEvery { benRepo.getBenFromId(2L) } returns mockk<BenRegCache>(relaxed = true)
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(match { it.beneficiaryID == 1L }, any()) } returns NetworkResult.Success("{}")
        coEvery { abhaIdRepo.mapHealthIDToBeneficiary(match { it.beneficiaryID == 2L }, any()) } returns NetworkResult.Error(500, "fail")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
        coVerify { abhaGenratedDao.deleteAbhaByBenId(1L) }
        coVerify(exactly = 0) { abhaGenratedDao.deleteAbhaByBenId(2L) }
    }
}

package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class AncHomeVisitSyncWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: FormRepository = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
    }

    private fun worker(attempt: Int = 0): AncHomeVisitSyncWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return AncHomeVisitSyncWorker(context, params, preferenceDao, repository)
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

    @Test
    fun doWork_returnsFailure_whenNoUserLoggedIn() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "No user logged in",
            (result as ListenableWorker.Result.Failure).outputData.getString("error")
        )
    }

    @Test
    fun doSyncWork_savesVisits_whenResponseSuccessful() = runTest {
        val visit = mockk<HBNCVisitResponse>(relaxed = true)
        val visitList = listOf(visit)
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns HBNCVisitListResponse(data = visitList)
        coEvery { repository.getAllAncVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveDownloadedVisitListANC(visitList) }
    }

    @Test
    fun doSyncWork_savesEmptyList_whenResponseBodyNull() = runTest {
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery { repository.getAllAncVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveDownloadedVisitListANC(emptyList()) }
    }

    @Test
    fun doSyncWork_doesNotSave_whenResponseUnsuccessfulClientError() = runTest {
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 404
        every { response.message() } returns "Bad Request"
        coEvery { repository.getAllAncVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.saveDownloadedVisitListANC(any()) }
    }

    @Test
    fun doSyncWork_retries_whenResponseUnsuccessfulServerError() = runTest {
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 500
        every { response.message() } returns "Server Error"
        coEvery { repository.getAllAncVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenUnknownHostExceptionThrown() = runTest {
        coEvery { repository.getAllAncVisits(any()) } throws UnknownHostException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { repository.getAllAncVisits(any()) } throws SocketTimeoutException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }
}

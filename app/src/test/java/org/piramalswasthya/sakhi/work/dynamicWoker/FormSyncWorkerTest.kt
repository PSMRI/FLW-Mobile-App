package org.piramalswasthya.sakhi.work.dynamicWoker

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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class FormSyncWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: FormRepository = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): FormSyncWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return FormSyncWorker(context, params, preferenceDao, repository)
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
    fun doSyncWork_savesHbncVisits_whenResponseSuccessful() = runTest {
        val visit = mockk<HBNCVisitResponse>(relaxed = true)
        val visitList = listOf(visit)
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns HBNCVisitListResponse(data = visitList)
        coEvery { repository.getAllHbncVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveDownloadedVisitList(visitList) }
    }

    @Test
    fun doSyncWork_retries_whenHbncServerError() = runTest {
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 500
        every { response.message() } returns "Server Error"
        coEvery { repository.getAllHbncVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doSyncWork_doesNotThrow_whenHbncClientError() = runTest {
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 400
        every { response.message() } returns "Bad Request"
        coEvery { repository.getAllHbncVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_savesHbycVisits_whenResponseSuccessful() = runTest {
        val visit = mockk<HBNCVisitResponse>(relaxed = true)
        val visitList = listOf(visit)
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns HBNCVisitListResponse(data = visitList)
        coEvery { repository.getAllHbycVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveDownloadedVisitListHBYC(visitList) }
    }

    @Test
    fun doSyncWork_retries_whenHbycServerError() = runTest {
        val response = mockk<Response<HBNCVisitListResponse>>()
        every { response.isSuccessful } returns false
        every { response.code() } returns 503
        every { response.message() } returns "Server Error"
        coEvery { repository.getAllHbycVisits(any()) } returns response

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doSyncWork_skipsForm_whenBenIdNegative() = runTest {
        val form = FormResponseJsonEntity(
            id = 1, benId = -1L, hhId = 1L, visitDay = "1",
            visitDate = "2024-01-01", formId = "f1", version = 1, formDataJson = "{}"
        )
        coEvery { repository.getUnsyncedForms() } returns listOf(form)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.syncFormToServer(any()) }
    }

    @Test
    fun doSyncWork_marksFormSynced_whenSyncSucceeds() = runTest {
        val form = FormResponseJsonEntity(
            id = 7, benId = 5L, hhId = 1L, visitDay = "1",
            visitDate = "2024-01-01", formId = "f1", version = 1, formDataJson = "{}"
        )
        coEvery { repository.getUnsyncedForms() } returns listOf(form)
        coEvery { repository.syncFormToServer(form) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSynced(7) }
    }

    @Test
    fun doSyncWork_doesNotMarkSynced_whenSyncFails() = runTest {
        val form = FormResponseJsonEntity(
            id = 8, benId = 5L, hhId = 1L, visitDay = "1",
            visitDate = "2024-01-01", formId = "f1", version = 1, formDataJson = "{}"
        )
        coEvery { repository.getUnsyncedForms() } returns listOf(form)
        coEvery { repository.syncFormToServer(form) } returns false

        worker().doWork()

        coVerify(exactly = 0) { repository.markFormAsSynced(8) }
    }

    @Test
    fun doSyncWork_returnsSuccess_whenFormSyncThrows() = runTest {
        val form = FormResponseJsonEntity(
            id = 9, benId = 5L, hhId = 1L, visitDay = "1",
            visitDate = "2024-01-01", formId = "f1", version = 1, formDataJson = "{}"
        )
        coEvery { repository.getUnsyncedForms() } returns listOf(form)
        coEvery { repository.syncFormToServer(form) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSynced(any()) }
    }

    @Test
    fun doSyncWork_skipsFormHBYC_whenBenIdNegative() = runTest {
        val form = FormResponseJsonEntityHBYC(
            id = 1, benId = -1L, hhId = 1L, visitDay = "1",
            visitDate = "2024-01-01", formId = "f1", version = 1, formDataJson = "{}"
        )
        coEvery { repository.getUnsyncedFormsHBYC() } returns listOf(form)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.syncFormToServerHBYC(any()) }
    }

    @Test
    fun doSyncWork_marksFormSyncedHBYC_whenSyncSucceeds() = runTest {
        val form = FormResponseJsonEntityHBYC(
            id = 11, benId = 5L, hhId = 1L, visitDay = "1",
            visitDate = "2024-01-01", formId = "f1", version = 1, formDataJson = "{}"
        )
        coEvery { repository.getUnsyncedFormsHBYC() } returns listOf(form)
        coEvery { repository.syncFormToServerHBYC(form) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSyncedHBYC(11) }
    }

    @Test
    fun doSyncWork_continuesLoopHBYC_whenSyncThrows() = runTest {
        val form = FormResponseJsonEntityHBYC(
            id = 12, benId = 5L, hhId = 1L, visitDay = "1",
            visitDate = "2024-01-01", formId = "f1", version = 1, formDataJson = "{}"
        )
        coEvery { repository.getUnsyncedFormsHBYC() } returns listOf(form)
        coEvery { repository.syncFormToServerHBYC(form) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSyncedHBYC(any()) }
    }

    @Test
    fun doWork_retries_whenUnknownHostExceptionThrown() = runTest {
        coEvery { repository.getAllHbncVisits(any()) } throws UnknownHostException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { repository.getAllHbncVisits(any()) } throws SocketTimeoutException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }
}

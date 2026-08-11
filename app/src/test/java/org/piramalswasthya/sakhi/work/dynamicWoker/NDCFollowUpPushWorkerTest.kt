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
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.NCDFollowUpFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class NDCFollowUpPushWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: NCDFollowUpFormRepository = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): NDCFollowUpPushWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return NDCFollowUpPushWorker(context, params, preferenceDao, repository)
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

    private fun sampleForm(id: Int = 1, benId: Long = 5L) = NCDReferalFormResponseJsonEntity(
        id = id,
        benId = benId,
        hhId = 1L,
        visitNo = 1,
        followUpNo = 1,
        treatmentStartDate = "2024-01-01",
        followUpDate = null,
        diagnosisCodes = "A1",
        formId = FormConstants.CDTF_001,
        version = 1,
        formDataJson = "{}"
    )

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
    fun doSyncWork_marksFormSynced_whenSyncSucceeds() = runTest {
        val form = sampleForm(id = 7)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSynced(7) }
    }

    @Test
    fun doSyncWork_retries_whenSyncReturnsFalse() = runTest {
        val form = sampleForm(id = 8)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns false

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
        coVerify(exactly = 0) { repository.markFormAsSynced(any()) }
    }

    @Test
    fun doSyncWork_retries_whenSyncThrows() = runTest {
        val form = sampleForm(id = 9)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doSyncWork_retries_whenMarkAsSyncedThrows() = runTest {
        val form = sampleForm(id = 10)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns true
        coEvery { repository.markFormAsSynced(10) } throws RuntimeException("db error")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doSyncWork_returnsSuccess_whenAllFormsSyncSuccessfully() = runTest {
        val formA = sampleForm(id = 1, benId = 2L)
        val formB = sampleForm(id = 2, benId = 3L)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(formA, formB)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSynced(1) }
        coVerify { repository.markFormAsSynced(2) }
    }

    @Test
    fun doWork_retries_whenUnknownHostExceptionThrown() = runTest {
        coEvery { repository.getUnsyncedForms(any()) } throws UnknownHostException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { repository.getUnsyncedForms(any()) } throws SocketTimeoutException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }
}

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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NCDFollowUpSyncWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: NCDFollowUpFormRepository = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): NCDFollowUpSyncWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return NCDFollowUpSyncWorker(context, params, preferenceDao, repository)
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
    fun doSyncWork_retries_whenFetchThrowsIOException() = runTest {
        coEvery { repository.fetchFormsFromServer(any(), any()) } throws IOException("network down")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doSyncWork_continuesToPush_whenFetchThrowsGenericException() = runTest {
        coEvery { repository.fetchFormsFromServer(any(), any()) } throws RuntimeException("boom")
        val form = sampleForm(id = 3)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSynced(3) }
    }

    @Test
    fun doSyncWork_savesFetchedForms_whenFetchSucceeds() = runTest {
        val fetched = listOf(sampleForm(id = 1))
        coEvery { repository.fetchFormsFromServer(any(), any()) } returns fetched

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveDownloadedForms(fetched) }
    }

    @Test
    fun doSyncWork_skipsForm_whenBenIdNegative() = runTest {
        val form = sampleForm(id = 4, benId = -1L)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.syncFormToServer(any(), any(), any()) }
    }

    @Test
    fun doSyncWork_marksFormSynced_whenPushSucceeds() = runTest {
        val form = sampleForm(id = 5)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSynced(5) }
    }

    @Test
    fun doSyncWork_returnsSuccess_evenWhenPushFails() = runTest {
        val form = sampleForm(id = 6)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns false

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSynced(any()) }
    }

    @Test
    fun doSyncWork_returnsSuccess_evenWhenPushThrows() = runTest {
        val form = sampleForm(id = 7)
        coEvery { repository.getUnsyncedForms(FormConstants.CDTF_001) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSynced(any()) }
    }
}

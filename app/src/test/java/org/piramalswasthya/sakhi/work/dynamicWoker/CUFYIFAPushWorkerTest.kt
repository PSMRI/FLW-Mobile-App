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
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants

@OptIn(ExperimentalCoroutinesApi::class)
class CUFYIFAPushWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: CUFYFormRepository = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): CUFYIFAPushWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return CUFYIFAPushWorker(context, params, preferenceDao, repository)
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

    private fun sampleForm(id: Int = 1, benId: Long = 5L) = CUFYFormResponseJsonEntity(
        id = id,
        benId = benId,
        hhId = 1L,
        visitDate = "2024-01-01",
        formId = FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID,
        version = 1,
        formDataJson = "{}"
    )

    @Test
    fun doSyncWork_marksFormSynced_whenPushSucceeds() = runTest {
        val form = sampleForm(id = 9)
        coEvery { repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSynced(9) }
    }

    @Test
    fun doSyncWork_skipsForm_whenBenIdNegative() = runTest {
        val form = sampleForm(id = 10, benId = -1L)
        coEvery { repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID) } returns listOf(form)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.syncFormToServer(any(), any(), any()) }
    }

    @Test
    fun doSyncWork_doesNotMark_whenPushReturnsFalse() = runTest {
        val form = sampleForm(id = 11)
        coEvery { repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } returns false

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSynced(any()) }
    }

    @Test
    fun doSyncWork_continues_whenPushThrows() = runTest {
        val form = sampleForm(id = 12)
        coEvery { repository.getUnsyncedForms(FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID) } returns listOf(form)
        coEvery { repository.syncFormToServer(any(), any(), any()) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSynced(any()) }
    }
}

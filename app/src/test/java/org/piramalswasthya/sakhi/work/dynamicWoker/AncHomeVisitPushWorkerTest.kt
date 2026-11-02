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
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AncHomeVisitPushWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: FormRepository = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): AncHomeVisitPushWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return AncHomeVisitPushWorker(context, params, preferenceDao, repository)
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

    private fun sampleForm(id: Int = 1, benId: Long = 5L) = ANCFormResponseJsonEntity(
        id = id,
        benId = benId,
        visitDay = "Visit-1",
        visitDate = "2024-01-01",
        formId = "anc_form",
        version = 1,
        formDataJson = "{}"
    )

    @Test
    fun doSyncWork_marksFormSynced_whenPushSucceeds() = runTest {
        val form = sampleForm(id = 9)
        coEvery { repository.getUnsyncedFormsANC() } returns listOf(form)
        coEvery { repository.syncFormToServerANC(any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.markFormAsSyncedANC(9) }
    }

    @Test
    fun doSyncWork_skipsForm_whenBenIdNegative() = runTest {
        val form = sampleForm(id = 10, benId = -1L)
        coEvery { repository.getUnsyncedFormsANC() } returns listOf(form)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.syncFormToServerANC(any()) }
    }

    @Test
    fun doSyncWork_doesNotMark_whenPushReturnsFalse() = runTest {
        val form = sampleForm(id = 11)
        coEvery { repository.getUnsyncedFormsANC() } returns listOf(form)
        coEvery { repository.syncFormToServerANC(any()) } returns false

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSyncedANC(any()) }
    }

    @Test
    fun doSyncWork_continues_whenPushThrows() = runTest {
        val form = sampleForm(id = 12)
        coEvery { repository.getUnsyncedFormsANC() } returns listOf(form)
        coEvery { repository.syncFormToServerANC(any()) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.markFormAsSyncedANC(any()) }
    }
}

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
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.VLFRepo
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaMdaCampaignPushWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: VLFRepo = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): FilariaMdaCampaignPushWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return FilariaMdaCampaignPushWorker(context, params, preferenceDao, repository)
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

    private fun sampleForm(id: Int = 1) = FilariaMDACampaignFormResponseJsonEntity(
        id = id,
        visitDate = "2024-01-01",
        visitYear = "2024",
        formId = "LF_MDA_CAMPAIGN",
        version = 1,
        formDataJson = "{}"
    )

    @Test
    fun doSyncWork_savesEachForm_whenUnsyncedFormsExist() = runTest {
        val formA = sampleForm(id = 1)
        val formB = sampleForm(id = 2)
        coEvery { repository.getUnsyncedFilariaMdaCampaign() } returns listOf(formA, formB)
        coEvery { repository.saveMdaFilariaCampaignToServer(any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveMdaFilariaCampaignToServer(formA) }
        coVerify { repository.saveMdaFilariaCampaignToServer(formB) }
    }

    @Test
    fun doSyncWork_returnsSuccess_evenWhenSaveFails() = runTest {
        val form = sampleForm(id = 1)
        coEvery { repository.getUnsyncedFilariaMdaCampaign() } returns listOf(form)
        coEvery { repository.saveMdaFilariaCampaignToServer(any()) } returns false

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_returnsSuccess_evenWhenSaveThrows() = runTest {
        val form = sampleForm(id = 1)
        coEvery { repository.getUnsyncedFilariaMdaCampaign() } returns listOf(form)
        coEvery { repository.saveMdaFilariaCampaignToServer(any()) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_retries_whenUnknownHostExceptionThrown() = runTest {
        coEvery { repository.getUnsyncedFilariaMdaCampaign() } throws UnknownHostException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { repository.getUnsyncedFilariaMdaCampaign() } throws SocketTimeoutException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }
}

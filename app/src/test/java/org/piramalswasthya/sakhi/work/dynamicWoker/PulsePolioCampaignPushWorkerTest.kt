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
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class PulsePolioCampaignPushWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: VLFRepo = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PulsePolioCampaignPushWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PulsePolioCampaignPushWorker(context, params, preferenceDao, repository)
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
    fun doSyncWork_savesEachForm_whenUnsyncedFormsExist() = runTest {
        val formA = PulsePolioCampaignCache(id = 1, formDataJson = "{}")
        val formB = PulsePolioCampaignCache(id = 2, formDataJson = "{}")
        coEvery { repository.getUnsyncedPulsePolioCampaign() } returns listOf(formA, formB)
        coEvery { repository.savePulsePolioCampaignToServer(any()) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.savePulsePolioCampaignToServer(formA) }
        coVerify { repository.savePulsePolioCampaignToServer(formB) }
    }

    @Test
    fun doSyncWork_returnsSuccess_evenWhenSaveFails() = runTest {
        val form = PulsePolioCampaignCache(id = 1, formDataJson = "{}")
        coEvery { repository.getUnsyncedPulsePolioCampaign() } returns listOf(form)
        coEvery { repository.savePulsePolioCampaignToServer(any()) } returns false

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_returnsSuccess_evenWhenSaveThrows() = runTest {
        val form = PulsePolioCampaignCache(id = 1, formDataJson = "{}")
        coEvery { repository.getUnsyncedPulsePolioCampaign() } returns listOf(form)
        coEvery { repository.savePulsePolioCampaignToServer(any()) } throws RuntimeException("boom")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_retries_whenUnknownHostExceptionThrown() = runTest {
        coEvery { repository.getUnsyncedPulsePolioCampaign() } throws UnknownHostException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { repository.getUnsyncedPulsePolioCampaign() } throws SocketTimeoutException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }
}

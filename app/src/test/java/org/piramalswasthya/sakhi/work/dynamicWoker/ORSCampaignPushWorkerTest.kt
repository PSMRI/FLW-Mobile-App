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
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class ORSCampaignPushWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)
    private val repository: VLFRepo = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): ORSCampaignPushWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return ORSCampaignPushWorker(context, params, preferenceDao, repository)
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
    fun doSyncWork_doesNotPush_whenNoUnsyncedForms() = runTest {
        coEvery { repository.getUnsyncedORSCampaign() } returns emptyList()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { repository.saveORSCampaignToServer(any()) }
    }

    @Test
    fun doSyncWork_pushesAllForms_whenAllSucceed() = runTest {
        val form1 = ORSCampaignCache(id = 1)
        val form2 = ORSCampaignCache(id = 2)
        coEvery { repository.getUnsyncedORSCampaign() } returns listOf(form1, form2)
        coEvery { repository.saveORSCampaignToServer(form1) } returns true
        coEvery { repository.saveORSCampaignToServer(form2) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveORSCampaignToServer(form1) }
        coVerify { repository.saveORSCampaignToServer(form2) }
    }

    @Test
    fun doSyncWork_returnsSuccess_whenSomeFormsFail() = runTest {
        val form1 = ORSCampaignCache(id = 3)
        val form2 = ORSCampaignCache(id = 4)
        coEvery { repository.getUnsyncedORSCampaign() } returns listOf(form1, form2)
        coEvery { repository.saveORSCampaignToServer(form1) } returns false
        coEvery { repository.saveORSCampaignToServer(form2) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_continuesLoop_whenSyncThrowsException() = runTest {
        val form1 = ORSCampaignCache(id = 5)
        val form2 = ORSCampaignCache(id = 6)
        coEvery { repository.getUnsyncedORSCampaign() } returns listOf(form1, form2)
        coEvery { repository.saveORSCampaignToServer(form1) } throws RuntimeException("boom")
        coEvery { repository.saveORSCampaignToServer(form2) } returns true

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        coVerify { repository.saveORSCampaignToServer(form2) }
    }

    @Test
    fun doWork_retries_whenRepositoryThrowsUnknownHostException() = runTest {
        coEvery { repository.getUnsyncedORSCampaign() } throws UnknownHostException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenRepositoryThrowsSocketTimeoutException() = runTest {
        coEvery { repository.getUnsyncedORSCampaign() } throws SocketTimeoutException()

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailure_whenIllegalStateExceptionThrown() = runTest {
        coEvery { repository.getUnsyncedORSCampaign() } throws IllegalStateException("bad state")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Failure)
        assertEquals(
            "bad state",
            (result as ListenableWorker.Result.Failure).outputData.getString("error")
        )
    }
}

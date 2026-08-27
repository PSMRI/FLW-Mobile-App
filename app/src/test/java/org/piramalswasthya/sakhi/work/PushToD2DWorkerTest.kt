package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.coEvery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.CdrRepo
import org.piramalswasthya.sakhi.repositories.HbncRepo
import org.piramalswasthya.sakhi.repositories.HbycRepo
import org.piramalswasthya.sakhi.repositories.MdsrRepo
import org.piramalswasthya.sakhi.repositories.PmjayRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo

@OptIn(ExperimentalCoroutinesApi::class)
class PushToD2DWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val mdsrRepo: MdsrRepo = mockk(relaxed = true)
    private val cdrRepo: CdrRepo = mockk(relaxed = true)
    private val pmsmaRepo: PmsmaRepo = mockk(relaxed = true)
    private val pmjayRepo: PmjayRepo = mockk(relaxed = true)
    private val hbncRepo: HbncRepo = mockk(relaxed = true)
    private val hbycRepo: HbycRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
    }

    private fun worker(attempt: Int = 0): PushToD2DWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return PushToD2DWorker(context, params, mdsrRepo, cdrRepo, pmsmaRepo, pmjayRepo, hbncRepo, hbycRepo, preferenceDao)
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

    private fun stubAllRepos(value: Boolean) {
        coEvery { cdrRepo.processNewCdr() } returns value
        coEvery { mdsrRepo.processNewMdsr() } returns value
        coEvery { pmsmaRepo.processNewPmsma() } returns value
        coEvery { pmjayRepo.processNewPmjay() } returns value
        coEvery { hbncRepo.processNewHbnc() } returns value
        coEvery { hbycRepo.processNewHbyc() } returns value
    }

    @Test
    fun doSyncWork_returnsSuccess_whenEveryRepoSucceeds() = runTest {
        stubAllRepos(true)
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }

    @Test
    fun doSyncWork_returnsFailure_whenOneRepoFails() = runTest {
        stubAllRepos(true)
        coEvery { hbycRepo.processNewHbyc() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Sync operation returned false", result.outputData.getString("error"))
    }

    @Test
    fun doSyncWork_reportsWorkerName_whenOneRepoFails() = runTest {
        stubAllRepos(true)
        coEvery { cdrRepo.processNewCdr() } returns false
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("PushToD2DWorker", result.outputData.getString("worker_name"))
    }

    @Test
    fun doWork_retries_whenSocketTimeoutExceptionThrown() = runTest {
        coEvery { cdrRepo.processNewCdr() } throws java.net.SocketTimeoutException("timeout")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_retries_whenIOExceptionThrown() = runTest {
        coEvery { cdrRepo.processNewCdr() } throws java.io.IOException("offline")
        assertTrue(worker().doWork() is ListenableWorker.Result.Retry)
    }

    @Test
    fun doWork_returnsFailureWithMessage_whenGenericExceptionThrown() = runTest {
        coEvery { cdrRepo.processNewCdr() } throws IllegalStateException("boom")
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("boom", result.outputData.getString("error"))
    }

    @Test
    fun constraint_requiresNetworkConnection() {
        assertEquals(
            androidx.work.NetworkType.CONNECTED,
            PushToD2DWorker.constraint.requiredNetworkType
        )
    }
}

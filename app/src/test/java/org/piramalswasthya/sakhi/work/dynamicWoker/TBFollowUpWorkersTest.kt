package org.piramalswasthya.sakhi.work.dynamicWoker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TBReferralFollowUpRepository
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TPTStartRepository

class TBFollowUpWorkersTest {

    @Test
    fun tbReferralPushWorker_returnsSuccessWhenPendingRecordsPush() = runTest {
        val repository = mockk<TBReferralFollowUpRepository>()
        coEvery { repository.pushPending() } returns true
        val worker = TBReferralFollowUpPushWorker(context(), params(), mockk(), repository)

        assertTrue(worker.doSyncWork() is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { repository.pushPending() }
    }

    @Test
    fun tbReferralPullWorker_retriesWhenPullFails() = runTest {
        val repository = mockk<TBReferralFollowUpRepository>()
        coEvery { repository.pullFromServer() } returns false
        val worker = TBReferralFollowUpPullWorker(context(), params(), mockk(), repository)

        assertTrue(worker.doSyncWork() is ListenableWorker.Result.Retry)
        coVerify(exactly = 1) { repository.pullFromServer() }
    }

    @Test
    fun tptPushWorker_retriesWhenPendingRecordsFailToPush() = runTest {
        val repository = mockk<TPTStartRepository>()
        coEvery { repository.pushPending() } returns false
        val worker = TPTFollowUpPushWorker(context(), params(), mockk(), repository)

        assertTrue(worker.doSyncWork() is ListenableWorker.Result.Retry)
        coVerify(exactly = 1) { repository.pushPending() }
    }

    @Test
    fun tptPullWorker_returnsSuccessWhenPullCompletes() = runTest {
        val repository = mockk<TPTStartRepository>()
        coEvery { repository.pullFromServer() } returns true
        val worker = TPTFollowUpPullWorker(context(), params(), mockk(), repository)

        assertTrue(worker.doSyncWork() is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { repository.pullFromServer() }
    }

    private fun context() = mockk<Context>(relaxed = true)
    private fun params() = mockk<WorkerParameters>(relaxed = true)
}

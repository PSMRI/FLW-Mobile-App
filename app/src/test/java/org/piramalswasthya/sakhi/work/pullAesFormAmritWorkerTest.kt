package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.repositories.AESRepo

@OptIn(ExperimentalCoroutinesApi::class)
class pullAesFormAmritWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val aesRepo: AESRepo = mockk(relaxed = true)
    private val preferenceDao: PreferenceDao = mockk(relaxed = true)

    @Before
    fun stubLoggedInUser() {
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp + 1
    }

    private fun worker(attempt: Int = 0): pullAesFormAmritWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return pullAesFormAmritWorker(context, params, aesRepo, preferenceDao)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    @Test
    fun doWork_returnsSuccess_whenServerReturnsZero() = runTest {
        coEvery { aesRepo.getAESScreeningDetailsFromServer() } returns 0
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenServerReturnsOne() = runTest {
        coEvery { aesRepo.getAESScreeningDetailsFromServer() } returns 1
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsFailure_whenServerReturnsUnexpectedValue() = runTest {
        coEvery { aesRepo.getAESScreeningDetailsFromServer() } returns 5
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals(
            "Pull operation returned incomplete results",
            result.outputData.getString("error")
        )
        assertEquals(
            "pullAesFormAmritWorker",
            result.outputData.getString("worker_name")
        )
    }

    @Test
    fun doWork_returnsSuccess_whenRepoThrowsException_becauseInnerFailureIsSwallowed() = runTest {
        coEvery { aesRepo.getAESScreeningDetailsFromServer() } throws RuntimeException("network down")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_returnsSuccess_whenSQLiteConstraintExceptionCaughtInternally() = runTest {
        coEvery {
            aesRepo.getAESScreeningDetailsFromServer()
        } throws SQLiteConstraintException("constraint violation")
        val result = worker().doWork()
        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_usesFirstSyncLastSyncedPage_whenLastSyncedTimestampIsDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp
        every { preferenceDao.getFirstSyncLastSyncedPage() } returns 3
        coEvery { aesRepo.getAESScreeningDetailsFromServer() } returns 0

        worker().doWork()

        verify(exactly = 1) { preferenceDao.getFirstSyncLastSyncedPage() }
    }

    @Test
    fun doWork_skipsFirstSyncLastSyncedPage_whenLastSyncedTimestampIsNotDefault() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } returns Konstants.defaultTimeStamp + 100
        coEvery { aesRepo.getAESScreeningDetailsFromServer() } returns 0

        worker().doWork()

        verify(exactly = 0) { preferenceDao.getFirstSyncLastSyncedPage() }
    }

    @Test
    fun doWork_returnsFailure_withGenericErrorMessage_whenPreferenceDaoThrowsRuntimeException() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } throws RuntimeException("boom")
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("boom", result.outputData.getString("error"))
        assertEquals(
            "pullAesFormAmritWorker",
            result.outputData.getString("worker_name")
        )
    }

    @Test
    fun doWork_returnsFailure_withUnknownError_whenExceptionHasNoMessage() = runTest {
        every { preferenceDao.getLastSyncedTimeStamp() } throws RuntimeException()
        val result = worker().doWork() as ListenableWorker.Result.Failure
        assertEquals("Unknown error", result.outputData.getString("error"))
        assertEquals(
            "pullAesFormAmritWorker",
            result.outputData.getString("worker_name")
        )
    }
}

package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import org.piramalswasthya.sakhi.repositories.PncRepo

@OptIn(ExperimentalCoroutinesApi::class)
class UpdatePNCToECWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private val maternalHealthRepo: MaternalHealthRepo = mockk(relaxed = true)
    private val benRepo: BenRepo = mockk(relaxed = true)
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo = mockk(relaxed = true)
    private val pmsmaRepo: PmsmaRepo = mockk(relaxed = true)
    private val pncRepo: PncRepo = mockk(relaxed = true)
    private val infantRepo: InfantRegRepo = mockk(relaxed = true)

    private val workManagerMock: WorkManager = mockk(relaxed = true)

    @Before
    fun stubWorkManager() {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns workManagerMock
    }

    @After
    fun unstubWorkManager() {
        unmockkObject(WorkManager.Companion)
    }

    private fun worker(attempt: Int = 0): UpdatePNCToECWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return UpdatePNCToECWorker(context, params, maternalHealthRepo, benRepo, deliveryOutcomeRepo, pmsmaRepo, pncRepo, infantRepo)
    }

    @Test
    fun worker_isConstructedWithGivenContext() {
        assertEquals(context, worker().applicationContext)
    }

    @Test
    fun doWork_returnsAResult_whenDependenciesYieldNoData() = runTest {
        assertNotNull(worker().doWork())
    }

    private fun benWithProcessed(status: String?): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.processed } returns status
        return ben
    }

    @Test
    fun doWork_callsSetToInactive_onAllRepos_forExpiredIds() = runTest {
        val ids = setOf(10L, 20L)
        coEvery { deliveryOutcomeRepo.getExpiredRecords() } returns ids

        worker().doWork()

        coVerify { deliveryOutcomeRepo.setToInactive(ids) }
        coVerify { maternalHealthRepo.setToInactive(ids) }
        coVerify { pmsmaRepo.setToInactive(ids) }
        coVerify { pncRepo.setToInactive(ids) }
        coVerify { infantRepo.setToInactive(ids) }
    }

    @Test
    fun doWork_updatesBenReproductiveStatusAndSyncState_whenBenFound() = runTest {
        val ben = benWithProcessed("Y")
        coEvery { deliveryOutcomeRepo.getExpiredRecords() } returns setOf(1L)
        coEvery { benRepo.getBenFromId(1L) } returns ben

        worker().doWork()

        coVerify { benRepo.updateRecord(ben) }
        verify { ben.processed = "U" }
        verify { ben.syncState = SyncState.UNSYNCED }
    }

    @Test
    fun doWork_keepsProcessedAsN_whenAlreadyN() = runTest {
        val ben = benWithProcessed("N")
        coEvery { deliveryOutcomeRepo.getExpiredRecords() } returns setOf(1L)
        coEvery { benRepo.getBenFromId(1L) } returns ben

        worker().doWork()

        verify(exactly = 0) { ben.processed = "U" }
        coVerify { benRepo.updateRecord(ben) }
    }

    @Test
    fun doWork_skipsUpdateRecord_whenBenNotFound() = runTest {
        coEvery { deliveryOutcomeRepo.getExpiredRecords() } returns setOf(2L)
        coEvery { benRepo.getBenFromId(2L) } returns null

        worker().doWork()

        coVerify(exactly = 0) { benRepo.updateRecord(any()) }
    }

    @Test
    fun doWork_swallowsException_whenSetToInactiveThrows() = runTest {
        coEvery { deliveryOutcomeRepo.getExpiredRecords() } returns setOf(3L)
        coEvery { deliveryOutcomeRepo.setToInactive(any()) } throws RuntimeException("db fail")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_swallowsException_whenUpdateBenThrows() = runTest {
        coEvery { deliveryOutcomeRepo.getExpiredRecords() } returns setOf(4L)
        coEvery { benRepo.getBenFromId(4L) } throws RuntimeException("lookup failed")

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_triggersAmritPushWorker_viaWorkManager() = runTest {
        coEvery { deliveryOutcomeRepo.getExpiredRecords() } returns emptySet()

        worker().doWork()

        verify {
            workManagerMock.enqueueUniqueWork(
                WorkerUtils.syncGateUniqueName,
                any<ExistingWorkPolicy>(),
                any<OneTimeWorkRequest>()
            )
        }
    }
}

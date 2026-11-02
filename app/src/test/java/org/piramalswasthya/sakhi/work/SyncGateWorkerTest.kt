package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SyncGateWorkerTest : BaseRepositoryTest() {

    private val context: Context = mockk(relaxed = true)
    private lateinit var workManager: WorkManager

    private fun worker(attempt: Int = 0): SyncGateWorker {
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns attempt
        return SyncGateWorker(context, params)
    }

    private fun workerWithReArmCount(reArm: Int): SyncGateWorker {
        val data: Data = mockk(relaxed = true)
        every { data.getInt(any(), any()) } returns reArm
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.inputData } returns data
        return SyncGateWorker(context, params)
    }

    private fun mockWorkManagerReturning(vararg infos: WorkInfo) {
        mockkObject(WorkManager.Companion)
        workManager = mockk(relaxed = true)
        every { WorkManager.getInstance(any<Context>()) } returns workManager
        val future = mockk<ListenableFuture<List<WorkInfo>>>()
        every { future.get() } returns infos.toList()
        every { workManager.getWorkInfosForUniqueWork(WorkerUtils.pushWorkerUniqueName) } returns future
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
    fun doWork_startsPushChain_whenNoActiveCycleExists() = runTest {
        mockWorkManagerReturning()
        mockkObject(WorkerUtils)
        every { WorkerUtils.enqueuePushChain(any(), any()) } just Runs

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify { WorkerUtils.enqueuePushChain(context, skipRegistration = false) }
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any<String>(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun doWork_startsPushChain_whenAllExistingWorkIsFinished() = runTest {
        mockWorkManagerReturning(
            WorkInfo(UUID.randomUUID(), WorkInfo.State.SUCCEEDED, emptySet()),
            WorkInfo(UUID.randomUUID(), WorkInfo.State.FAILED, emptySet()),
            WorkInfo(UUID.randomUUID(), WorkInfo.State.CANCELLED, emptySet()),
        )
        mockkObject(WorkerUtils)
        every { WorkerUtils.enqueuePushChain(any(), any()) } just Runs

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify { WorkerUtils.enqueuePushChain(context, skipRegistration = false) }
    }

    @Test
    fun doWork_reArmsGate_whenPushCycleIsActive() = runTest {
        mockWorkManagerReturning(
            WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, emptySet())
        )

        val result = workerWithReArmCount(5).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        val requestSlot = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(
                WorkerUtils.syncGateUniqueName,
                ExistingWorkPolicy.REPLACE,
                capture(requestSlot)
            )
        }
        assertEquals(
            SyncGateWorker::class.java.name,
            requestSlot.captured.workSpec.workerClassName
        )
        assertEquals(
            6,
            requestSlot.captured.workSpec.input.getInt("sync_gate_rearm_count", -1)
        )
    }

    @Test
    fun doWork_treatsMixOfFinishedAndActive_asActive() = runTest {
        mockWorkManagerReturning(
            WorkInfo(UUID.randomUUID(), WorkInfo.State.SUCCEEDED, emptySet()),
            WorkInfo(UUID.randomUUID(), WorkInfo.State.BLOCKED, emptySet()),
        )

        val result = workerWithReArmCount(0).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify {
            workManager.enqueueUniqueWork(
                WorkerUtils.syncGateUniqueName,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun doWork_stopsPolling_whenReArmCeilingReached() = runTest {
        mockWorkManagerReturning(
            WorkInfo(UUID.randomUUID(), WorkInfo.State.RUNNING, emptySet())
        )

        val result = workerWithReArmCount(SyncGateWorker.MAX_REARM).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any<String>(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun doWork_returnsSuccess_whenWorkManagerLookupThrows() = runTest {
        mockkObject(WorkManager.Companion)
        workManager = mockk(relaxed = true)
        every { WorkManager.getInstance(any<Context>()) } returns workManager
        every {
            workManager.getWorkInfosForUniqueWork(WorkerUtils.pushWorkerUniqueName)
        } throws RuntimeException("boom")

        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}

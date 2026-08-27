package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkerUtilsTest {

    private val context: Context = mockk(relaxed = true)
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        mockkObject(WorkManager.Companion)
        workManager = mockk(relaxed = true)
        every { WorkManager.getInstance(any()) } returns workManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uniqueWorkNames_areStable() {
        assertEquals("PUSH-TO-AMRIT", WorkerUtils.pushWorkerUniqueName)
        assertEquals("PULL-FROM-AMRIT", WorkerUtils.pullWorkerUniqueName)
        assertEquals("SYNC-GATE", WorkerUtils.syncGateUniqueName)
    }

    @Test
    fun triggerAmritPushWorker_enqueuesTheGateAsUniqueReplaceWork() {
        WorkerUtils.triggerAmritPushWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                WorkerUtils.syncGateUniqueName,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerAmritPushWorker_gateRequestTargetsSyncGateWorker() {
        val request = slot<OneTimeWorkRequest>()
        every {
            workManager.enqueueUniqueWork(any<String>(), any(), capture(request))
        } returns mockk(relaxed = true)
        WorkerUtils.triggerAmritPushWorker(context)
        assertEquals(
            SyncGateWorker::class.java.name,
            request.captured.workSpec.workerClassName
        )
    }

    @Test
    fun triggerAmritPushWorker_skipsTheGate_whenSkippingRegistration() {
        WorkerUtils.triggerAmritPushWorker(context, skipRegistration = true)
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(
                WorkerUtils.syncGateUniqueName,
                any(),
                any<OneTimeWorkRequest>()
            )
        }
        verify { workManager.beginWith(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun enqueuePushChain_beginsUniqueRegistrationWork_byDefault() {
        WorkerUtils.enqueuePushChain(context)
        verify {
            workManager.beginUniqueWork(
                WorkerUtils.pushWorkerUniqueName,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun enqueuePushChain_startsWithBenIdGeneration_whenSkippingRegistration() {
        WorkerUtils.enqueuePushChain(context, skipRegistration = true)
        verify { workManager.beginWith(any<OneTimeWorkRequest>()) }
        verify(exactly = 0) {
            workManager.beginUniqueWork(
                any<String>(),
                any(),
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun enqueuePushChain_registrationRequestTargetsPushToAmritWorker() {
        val request = slot<OneTimeWorkRequest>()
        every {
            workManager.beginUniqueWork(any<String>(), any(), capture(request))
        } returns mockk(relaxed = true)
        WorkerUtils.enqueuePushChain(context)
        assertEquals(
            PushToAmritWorker::class.java.name,
            request.captured.workSpec.workerClassName
        )
    }

    @Test
    fun triggerAmritPullWorker_beginsUniquePullWork() {
        WorkerUtils.triggerAmritPullWorker(context)
        verify {
            workManager.beginUniqueWork(
                WorkerUtils.pullWorkerUniqueName,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerD2dSyncWorker_isCurrentlyANoOp() {
        WorkerUtils.triggerD2dSyncWorker(context)
        verify(exactly = 0) { WorkManager.getInstance(any()) }
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any<String>(), any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun triggerCbacPullWorker_enqueuesUniqueAppendOrReplaceWork() {
        WorkerUtils.triggerCbacPullWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                CbacPullFromAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerCbacPushWorker_enqueuesUniqueAppendOrReplaceWork() {
        WorkerUtils.triggerCbacPushWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                CbacPushToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerECPushWorker_enqueuesUniqueAppendOrReplaceWork() {
        WorkerUtils.triggerECPushWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                any<String>(),
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerPMSMAPushWorker_enqueuesUniqueAppendOrReplaceWork() {
        WorkerUtils.triggerPMSMAPushWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                any<String>(),
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerDeliveryOutcomePushWorker_usesWorkerCompanionName() {
        WorkerUtils.triggerDeliveryOutcomePushWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                PushDeliveryOutcomeToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerInfantRegPushWorker_usesWorkerCompanionName() {
        WorkerUtils.triggerInfantRegPushWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                PushInfantRegisterToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerGenBenIdWorker_keepsExistingWork() {
        WorkerUtils.triggerGenBenIdWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                GenerateBenIdsWorker.name,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerUwinWorker_keepsExistingWork() {
        WorkerUtils.triggerUwinWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                PullUwinFromAmritWorker.name,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun triggerPeriodicPncEcUpdateWorker_enqueuesDailyPeriodicWork() {
        val request = slot<PeriodicWorkRequest>()
        every {
            workManager.enqueueUniquePeriodicWork(any(), any(), capture(request))
        } returns mockk(relaxed = true)
        WorkerUtils.triggerPeriodicPncEcUpdateWorker(context)
        verify {
            workManager.enqueueUniquePeriodicWork(
                UpdatePNCToECWorker.periodicName,
                ExistingPeriodicWorkPolicy.KEEP,
                any<PeriodicWorkRequest>()
            )
        }
        assertEquals(
            UpdatePNCToECWorker::class.java.name,
            request.captured.workSpec.workerClassName
        )
        assertTrue(request.captured.workSpec.isPeriodic)
    }

    @Test
    fun triggerAdHocPncEcUpdateWorker_enqueuesOneShotWork() {
        val request = slot<OneTimeWorkRequest>()
        every {
            workManager.enqueueUniqueWork(any<String>(), any(), capture(request))
        } returns mockk(relaxed = true)
        WorkerUtils.triggerAdHocPncEcUpdateWorker(context)
        verify {
            workManager.enqueueUniqueWork(
                UpdatePNCToECWorker.oneShotName,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
        assertEquals(
            UpdatePNCToECWorker::class.java.name,
            request.captured.workSpec.workerClassName
        )
    }

    @Test
    fun triggerDownloadCardWorker_passesFileNameAsInputData() {
        val request = slot<OneTimeWorkRequest>()
        every {
            workManager.enqueueUniqueWork(any<String>(), any(), capture(request))
        } returns mockk(relaxed = true)
        val state = WorkerUtils.triggerDownloadCardWorker(
            context,
            "card.pdf",
            MutableLiveData(null)
        )
        assertNotNull(state)
        assertEquals(
            "card.pdf",
            request.captured.workSpec.input.getString(DownloadCardWorker.file_name)
        )
    }

    @Test
    fun triggerDownloadCardWorker_replacesExistingDownload() {
        WorkerUtils.triggerDownloadCardWorker(context, "x.pdf", MutableLiveData(null))
        verify {
            workManager.enqueueUniqueWork(
                DownloadCardWorker.name,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun cancelAllWork_delegatesToWorkManager() {
        WorkerUtils.cancelAllWork(context)
        verify { workManager.cancelAllWork() }
    }
}

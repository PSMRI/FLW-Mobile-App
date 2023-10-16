package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncWorkerUniqueName = "SYNC-WITH-AMRIT"

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerAmritSyncWorker(context: Context) {
        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullIncentiveActivityWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullHRPWorkRequest = OneTimeWorkRequestBuilder<PullHRPFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullTBWorkRequest = OneTimeWorkRequestBuilder<PullTBFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullECWorkRequest = OneTimeWorkRequestBuilder<PullECFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PullChildImmunizatonFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushWorkRequest = OneTimeWorkRequestBuilder<PushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PushChildImmunizationToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushHRPToAmritWorker = OneTimeWorkRequestBuilder<PushHRPToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushTBWorkRequest = OneTimeWorkRequestBuilder<PushTBToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushECWorkRequest = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pullWorkRequest
            )
            .then(pullIncentiveActivityWorkRequest)
            .then(pullCbacWorkRequest)
            .then(pullHRPWorkRequest)
            .then(pullTBWorkRequest)
            .then(pullECWorkRequest)
            .then(pullImmunizationWorkRequest)
            .then(pushWorkRequest)
            .then(pushCbacWorkRequest)
            .then(pushImmunizationWorkRequest)
            .then(pushHRPToAmritWorker)
            .then(pushTBWorkRequest)
            .then(pushECWorkRequest)
            .enqueue()
    }

    fun triggerAmritPushWorker(context: Context) {
        val pushWorkRequest = OneTimeWorkRequestBuilder<PushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushHRPToAmritWorker = OneTimeWorkRequestBuilder<PushHRPToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushTBWorkRequest = OneTimeWorkRequestBuilder<PushTBToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushECToAmritWorker = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushPWWorkRequest = OneTimeWorkRequestBuilder<PushPWRToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushPmsmaWorkRequest = OneTimeWorkRequestBuilder<PushPmsmaToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushDeliverOutcomeWorkRequest =
            OneTimeWorkRequestBuilder<PushDeliveryOutcomeToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushPNCWorkRequest = OneTimeWorkRequestBuilder<PushPNCToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushInfantRegisterWorkRequest =
            OneTimeWorkRequestBuilder<PushInfantRegisterToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PushChildImmunizationToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        //Always at last - INCENTIVES
        val pullIncentiveActivityWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pushWorkRequest
            )
            .then(pushCbacWorkRequest)
            .then(pushHRPToAmritWorker)
            .then(pushTBWorkRequest)
            .then(pushECToAmritWorker)
            .then(pushPWWorkRequest)
            .then(pushPmsmaWorkRequest)
            .then(pushDeliverOutcomeWorkRequest)
            .then(pushPNCWorkRequest)
            .then(pushInfantRegisterWorkRequest)
            .then(pushImmunizationWorkRequest)
            .then(pullIncentiveActivityWorkRequest)
            .enqueue()
    }

    fun triggerAmritPullWorker(context: Context) {
        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullIncentiveActivityWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullHRPWorkRequest = OneTimeWorkRequestBuilder<PullHRPFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullTBWorkRequest = OneTimeWorkRequestBuilder<PullTBFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullECWorkRequest = OneTimeWorkRequestBuilder<PullECFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPWWorkRequest = OneTimeWorkRequestBuilder<PullPWRFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPMSMAWorkRequest = OneTimeWorkRequestBuilder<PullPmsmaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPNCWorkRequest = OneTimeWorkRequestBuilder<PullPNCFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullDeliveryOutcomeWorkRequest =
            OneTimeWorkRequestBuilder<PullDeliveryOutcomeFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullInfantRegWorkRequest = OneTimeWorkRequestBuilder<PullInfantRegFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PullChildImmunizatonFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val setSyncCompleteWorker = OneTimeWorkRequestBuilder<UpdatePrefForPullCompleteWorker>()
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pullWorkRequest
            )
            .then(pullIncentiveActivityWorkRequest)
            .then(pullCbacWorkRequest)
            .then(pullHRPWorkRequest)
            .then(pullTBWorkRequest)
            .then(pullECWorkRequest)
            .then(pullPWWorkRequest)
            .then(pullPMSMAWorkRequest)
            .then(pullPNCWorkRequest)
            .then(pullDeliveryOutcomeWorkRequest)
            .then(pullInfantRegWorkRequest)
            .then(pullImmunizationWorkRequest)
            .then(setSyncCompleteWorker)
            .enqueue()
    }

    fun triggerD2dSyncWorker(context: Context) {
//        val workRequest = OneTimeWorkRequestBuilder<PushToD2DWorker>()
//            .setConstraints(PushToD2DWorker.constraint)
//            .build()
//        WorkManager.getInstance(context)
//            .enqueueUniqueWork(
//                PushToD2DWorker.name,
//                ExistingWorkPolicy.APPEND_OR_REPLACE,
//                workRequest
//            )
    }

    fun triggerCbacPullWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<CbacPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                CbacPullFromAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerCbacPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                CbacPushToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerECPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushECToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerPMSMAPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushPmsmaToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushPmsmaToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerDeliveryOutcomePushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushDeliveryOutcomeToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushDeliveryOutcomeToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerInfantRegPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushInfantRegisterToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushInfantRegisterToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerGenBenIdWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<GenerateBenIdsWorker>()
            .setConstraints(GenerateBenIdsWorker.constraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(GenerateBenIdsWorker.name, ExistingWorkPolicy.KEEP, workRequest)
    }

    fun triggerPeriodicPncEcUpdateWorker(context: Context) {
        val workRequest =
            PeriodicWorkRequest.Builder(UpdatePNCToECWorker::class.java, 1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
                .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UpdatePNCToECWorker.periodicName,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    fun triggerAdHocPncEcUpdateWorker(context: Context) {
        val workRequest = OneTimeWorkRequest.Builder(UpdatePNCToECWorker::class.java).build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UpdatePNCToECWorker.oneShotName,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
    }

    fun triggerDownloadCardWorker(
        context: Context,
        fileName: String,
        otpTxnID: MutableLiveData<String?>
    ): LiveData<Operation.State> {

        val workRequest = OneTimeWorkRequestBuilder<DownloadCardWorker>()
            .setConstraints(networkOnlyConstraint)
            .setInputData(Data.Builder().apply { putString(DownloadCardWorker.file_name, fileName) }
                .build())
            .build()

        return WorkManager.getInstance(context)
            .enqueueUniqueWork(
                DownloadCardWorker.name,
                ExistingWorkPolicy.REPLACE,
                workRequest
            ).state
    }

    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }
}
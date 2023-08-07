package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager

object WorkerUtils {

    const val syncWorkerUniqueName  = "SYNC-WITH-AMRIT"

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerAmritSyncWorker(context : Context){
        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
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
        val pushECWorkRequest = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncWorkerUniqueName, ExistingWorkPolicy.APPEND_OR_REPLACE, pullWorkRequest)
//            .then(pullCbacWorkRequest)
            .then(pullHRPWorkRequest)
//            .then(pullTBWorkRequest)
            .then(pullECWorkRequest)
            .then(pushWorkRequest)
//            .then(pushCbacWorkRequest)
            .then(pushHRPToAmritWorker)
//            .then(pushTBWorkRequest)
            .then(pushECWorkRequest)
            .enqueue()
    }
    fun triggerAmritPushWorker(context : Context){
        val pushWorkRequest = OneTimeWorkRequestBuilder<PushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushHRPToAmritWorker = OneTimeWorkRequestBuilder<PushHRPToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullHRPWorkRequest = OneTimeWorkRequestBuilder<PullHRPFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushTBWorkRequest = OneTimeWorkRequestBuilder<PushTBToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncWorkerUniqueName, ExistingWorkPolicy.APPEND_OR_REPLACE, pushWorkRequest)
//            .then(pushCbacWorkRequest)
            .then(pushHRPToAmritWorker)
//            .then(pushTBWorkRequest)
            .enqueue()
    }
    fun triggerAmritPullWorker(context : Context){
        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
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
        val setSyncCompleteWorker = OneTimeWorkRequestBuilder<UpdatePrefForPullCompleteWorker>()
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(syncWorkerUniqueName, ExistingWorkPolicy.APPEND_OR_REPLACE, pullWorkRequest)
//            .then(pullCbacWorkRequest)
            .then(pullHRPWorkRequest)
//            .then(pullTBWorkRequest)
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

    fun triggerCbacPullWorker(context: Context){
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
    fun triggerCbacPushWorker(context: Context){
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

    fun triggerGenBenIdWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<GenerateBenIdsWorker>()
            .setConstraints(GenerateBenIdsWorker.constraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(GenerateBenIdsWorker.name, ExistingWorkPolicy.KEEP, workRequest)
    }

    fun triggerDownloadCardWorker(
        context: Context,
        fileName: String,
        otpTxnID: MutableLiveData<String?>
    ): LiveData<Operation.State> {

        val workRequest = OneTimeWorkRequestBuilder<DownloadCardWorker>()
            .setConstraints(networkOnlyConstraint)
            .setInputData(Data.Builder().apply { putString(DownloadCardWorker.file_name, fileName) }.build())
            .build()

        return WorkManager.getInstance(context)
            .enqueueUniqueWork(DownloadCardWorker.name, ExistingWorkPolicy.REPLACE, workRequest).state
    }
    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }
}
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
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYIFAFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYORSFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYSAMFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.EyeSurgeryFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.FormSyncWorker
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncWorkerUniqueName = "SYNC-WITH-AMRIT"

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerAmritSyncWorker(context: Context) {
        val CUFYORSFormSyncWorker = OneTimeWorkRequestBuilder<CUFYORSFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYIFAFormSyncWorker = OneTimeWorkRequestBuilder<CUFYIFAFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullEyeSurgeryFormSyncWorker = OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYSAMFormSyncWorker = OneTimeWorkRequestBuilder<CUFYSAMFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val generalOpdPullFromAmritWorker = OneTimeWorkRequestBuilder<GeneralOpdPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAdolescentWorkRequest = OneTimeWorkRequestBuilder<PullAdolescentFromWorker>()
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

        val pullVLFWorkRequest = OneTimeWorkRequestBuilder<PullVLFFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullTBWorkRequest = OneTimeWorkRequestBuilder<PullTBFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullMalariaWorkRequest = OneTimeWorkRequestBuilder<PullMalariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullECWorkRequest = OneTimeWorkRequestBuilder<PullECFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PullChildImmunizatonFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBYCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullHBYCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBNCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullChildHBNCFromAmritWorker>()
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
           val pushVLFToAmritWorker = OneTimeWorkRequestBuilder<PushVLFToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()


        val pushTBWorkRequest = OneTimeWorkRequestBuilder<PushTBToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushECWorkRequest = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushChildHBYCToAmritWorker = OneTimeWorkRequestBuilder<PushChildHBYCToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushChildHBNCToAmritWorker = OneTimeWorkRequestBuilder<PushChildHBNCFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAESToAmritWorker = OneTimeWorkRequestBuilder<pullAesFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullkalaAzarToAmritWorker = OneTimeWorkRequestBuilder<PullKalaAzarFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullLeprosyToAmritWorker = OneTimeWorkRequestBuilder<PullLeprosyFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullFilariaToAmritWorker = OneTimeWorkRequestBuilder<PullFilariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushAbhaWorkRequest = OneTimeWorkRequestBuilder<PushMapAbhatoBenficiaryWorker>()
            .setConstraints(networkOnlyConstraint) // if you have constraints
            .build()

        val formSyncWorkerRequest = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val maaMeetingFormSyncWorkerRequest = OneTimeWorkRequestBuilder<MaaMeetingDownsyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pullWorkRequest
            )
            .then(CUFYORSFormSyncWorker)
            .then(CUFYIFAFormSyncWorker)
            .then(CUFYSAMFormSyncWorker)
            .then(pullEyeSurgeryFormSyncWorker)
            .then(maaMeetingFormSyncWorkerRequest)
            .then(pullIncentiveActivityWorkRequest)
            .then(pullCbacWorkRequest)
            .then(pullTBWorkRequest)
            .then(pullECWorkRequest)
            .then(pullImmunizationWorkRequest)
            .then(generalOpdPullFromAmritWorker)
//            .then(pullHBYCFromAmritWorker)
            .then(pullHBNCFromAmritWorker)
            .then(pullHRPWorkRequest)
            .then(pullVLFWorkRequest)
            .then(pullAdolescentWorkRequest)
            .then(pullMalariaWorkRequest)
            .then(pullAESToAmritWorker)
            .then(pullkalaAzarToAmritWorker)
            .then(pullFilariaToAmritWorker)
            .then(pullLeprosyToAmritWorker)
            .then(pushWorkRequest)
            .then(pushCbacWorkRequest)
            .then(pushImmunizationWorkRequest)
            .then(pushHRPToAmritWorker)
            .then(formSyncWorkerRequest)
            .then(pushTBWorkRequest)
            .then(pushECWorkRequest)
//            .then(pushChildHBYCToAmritWorker)
            .then(pushChildHBNCToAmritWorker)
            .then(pushAbhaWorkRequest)
            .then(pushVLFToAmritWorker)
            .enqueue()
    }

    fun triggerAmritPushWorker(context: Context) {
        val formSyncWorkerRequest  = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushWorkRequest = OneTimeWorkRequestBuilder<PushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushAdolescentWorkRequest = OneTimeWorkRequestBuilder<PushAdolescentAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushMaaMeetingsWorkRequest = OneTimeWorkRequestBuilder<MaaMeetingsPushWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushHRPToAmritWorker = OneTimeWorkRequestBuilder<PushHRPToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushVLFToAmritWorker = OneTimeWorkRequestBuilder<PushVLFToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushTBWorkRequest = OneTimeWorkRequestBuilder<PushTBToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushMalariaWorkRequest = OneTimeWorkRequestBuilder<PushMalariaAmritWorker>()
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
        val pushMdsrWorkRequest =
            OneTimeWorkRequestBuilder<PushMdsrToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushCdrWorkRequest =
            OneTimeWorkRequestBuilder<PushCdrToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PushChildImmunizationToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushChildHBYCToAmritWorker = OneTimeWorkRequestBuilder<PushChildHBYCToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushChildHBNCToAmritWorker = OneTimeWorkRequestBuilder<PushChildHBNCFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val CUFYORSFormSyncWorkerRequest  = OneTimeWorkRequestBuilder<CUFYORSFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYIFAFormSyncWorker = OneTimeWorkRequestBuilder<CUFYIFAFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushEyeSurgeryFormSyncWorker = OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYSAMFormSyncWorker = OneTimeWorkRequestBuilder<CUFYSAMFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        //Always at last - INCENTIVES
        val pullIncentiveActivityWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()

        val pullHBYCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullHBYCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBNCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullChildHBNCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushAESToAmritWorker = OneTimeWorkRequestBuilder<pushAesAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushkalaAzarToAmritWorker = OneTimeWorkRequestBuilder<pushKalaAzarAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushLeprosyToAmritWorker = OneTimeWorkRequestBuilder<pushLeprosyAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushFilariaToAmritWorker = OneTimeWorkRequestBuilder<PushFilariaAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushAbhaWorkRequest = OneTimeWorkRequestBuilder<PushMapAbhatoBenficiaryWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pushUwinWorkerRequest = OneTimeWorkRequestBuilder<PushUwinToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()


        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pushWorkRequest
            )
            .then(pushMaaMeetingsWorkRequest)
            .then(pushCbacWorkRequest)
            .then(pushAdolescentWorkRequest)
            .then(pushHRPToAmritWorker)
            .then(pushVLFToAmritWorker)
            .then(pushTBWorkRequest)
            .then(pushECToAmritWorker)
            .then(pushPWWorkRequest)
            .then(pushPmsmaWorkRequest)
            .then(pushUwinWorkerRequest)
            .then(pushDeliverOutcomeWorkRequest)
            .then(pushPNCWorkRequest)
            .then(pushInfantRegisterWorkRequest)
            .then(pushMdsrWorkRequest)
            .then(formSyncWorkerRequest)
            .then(pushCdrWorkRequest)
            .then(pushImmunizationWorkRequest)
//            .then(pushChildHBYCToAmritWorker)
            .then(pushChildHBNCToAmritWorker)
            .then(CUFYORSFormSyncWorkerRequest)
            .then(CUFYIFAFormSyncWorker)
            .then(pushEyeSurgeryFormSyncWorker)
            .then(CUFYSAMFormSyncWorker)
            .then(pullIncentiveActivityWorkRequest)
            .then(pushAbhaWorkRequest)
            .then(pushMalariaWorkRequest)
            .then(pushAESToAmritWorker)
            .then(pushkalaAzarToAmritWorker)
            .then(pushFilariaToAmritWorker)
            .then(pushLeprosyToAmritWorker)
            .enqueue()
    }

    fun triggerAmritPullWorker(context: Context) {
        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullEyeSurgeryFormSyncWorker = OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pulladolescentWorkRequest = OneTimeWorkRequestBuilder<PushAdolescentAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val generalOpdPullFromAmritWorker = OneTimeWorkRequestBuilder<GeneralOpdPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullIncentiveActivityWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullVaccineWorkRequest = OneTimeWorkRequestBuilder<PullVaccinesWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullHRPWorkRequest = OneTimeWorkRequestBuilder<PullHRPFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
         val pullVLFWorkRequest = OneTimeWorkRequestBuilder<PullVLFFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val maaMeetingFormSyncWorkerRequest = OneTimeWorkRequestBuilder<MaaMeetingDownsyncWorker>()
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
        val pullMdsrWorkRequest =
            OneTimeWorkRequestBuilder<PullMdsrFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullCdrWorkRequest =
            OneTimeWorkRequestBuilder<PullCdrFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBYCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullHBYCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBNCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullChildHBNCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullUwinWorkerRequest = OneTimeWorkRequestBuilder<PullUwinFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAshaWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val setSyncCompleteWorker = OneTimeWorkRequestBuilder<UpdatePrefForPullCompleteWorker>()
            .build()
        val pullMalariaWorkRequest = OneTimeWorkRequestBuilder<PullMalariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAESToAmritWorker = OneTimeWorkRequestBuilder<pullAesFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullkalaAzarToAmritWorker = OneTimeWorkRequestBuilder<PullKalaAzarFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullLeprosyToAmritWorker = OneTimeWorkRequestBuilder<PullLeprosyFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullFilariaToAmritWorker = OneTimeWorkRequestBuilder<PullFilariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pullWorkRequest
            )
            .then(maaMeetingFormSyncWorkerRequest)
            .then(pullEyeSurgeryFormSyncWorker)
            .then(pullIncentiveActivityWorkRequest)
            .then(pullUwinWorkerRequest)
            .then(pullCbacWorkRequest)
            .then(pullVaccineWorkRequest)
            .then(pullTBWorkRequest)
            .then(pullECWorkRequest)
            .then(pullPWWorkRequest)
            .then(pullPMSMAWorkRequest)
            .then(pullPNCWorkRequest)
            .then(pullDeliveryOutcomeWorkRequest)
            .then(pullInfantRegWorkRequest)
            .then(pullImmunizationWorkRequest)
            .then(generalOpdPullFromAmritWorker)
            .then(pullMdsrWorkRequest)
            .then(pullCdrWorkRequest)
//            .then(pullHBYCFromAmritWorker)
            .then(pullHBNCFromAmritWorker)
            .then(pullHRPWorkRequest)
            .then(pullVLFWorkRequest)
            .then(pulladolescentWorkRequest)
            .then(pullMalariaWorkRequest)
            .then(pullAESToAmritWorker)
            .then(pullkalaAzarToAmritWorker)
            .then(pullFilariaToAmritWorker)
            .then(pullLeprosyToAmritWorker)
            .then(setSyncCompleteWorker)
            .enqueue()
    }

    fun triggerD2dSyncWorker(context: Context) {
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


    fun triggerUwinWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PullUwinFromAmritWorker>()
            .setConstraints(PullUwinFromAmritWorker.constraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(PullUwinFromAmritWorker.name, ExistingWorkPolicy.KEEP, workRequest)
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
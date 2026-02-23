package org.piramalswasthya.sakhi.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import org.piramalswasthya.sakhi.repositories.PncRepo

@HiltWorker
class UpdatePNCToECWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val benRepo: BenRepo,
    private val deliveryOutcomeRepo: DeliveryOutcomeRepo,
    private val pmsmaRepo: PmsmaRepo,
    private val pncRepo: PncRepo,
    private val infantRepo: InfantRegRepo
) : CoroutineWorker(appContext, params) {

    companion object {
        const val oneShotName = "ad-hoc pnc ec update worker"
        const val periodicName = "scheduled pnc ec update worker"

    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result {
        try { setForeground(createForegroundInfo()) } catch (_: Throwable) {}
        val eligBenIds = deliveryOutcomeRepo.getExpiredRecords()
        setRecordsToInactive(eligBenIds)
        updateBen(eligBenIds)
        WorkerUtils.triggerAmritPushWorker(appContext)


        return Result.success()
    }

    private suspend fun updateBen(eligBenIds: Set<Long>) {
        try {
            val now = System.currentTimeMillis()
            eligBenIds.forEach {
                val ben = benRepo.getBenFromId(it)
                ben?.let {
                    it.updatedDate = now
                    it.genDetails?.reproductiveStatusId = 1
                    it.genDetails?.reproductiveStatus =
                        applicationContext.resources.getStringArray(R.array.nbr_reproductive_status_array2)[0]
                    if (it.processed != "N") it.processed = "U"
                    it.syncState = SyncState.UNSYNCED
                }
                if (ben != null) {
                    benRepo.updateRecord(ben)
                }
            }
        }catch (e:Exception){
//            HelperUtil.deliveryOutcomeUpdatePNCWorker.append("updateBen::$e")
//            HelperUtil.deliveryOutcomeUpdatePNCWorker.append("\n")
//            HelperUtil.deliveryOutcomeUpdatePNCWorkerMethod(appContext, "deliveryOutcomeUpdatePNCWorkerMethod.txt", HelperUtil.deliveryOutcomeRepo.toString())
        }

    }

    private suspend fun setRecordsToInactive(eligBenIds: Set<Long>) {
        try {
            deliveryOutcomeRepo.setToInactive(eligBenIds)
            maternalHealthRepo.setToInactive(eligBenIds)
            pmsmaRepo.setToInactive(eligBenIds)
            pncRepo.setToInactive(eligBenIds)
            infantRepo.setToInactive(eligBenIds)
        } catch (e: Exception) {
//            HelperUtil.deliveryOutcomeUpdatePNCWorker.append("setRecordsToInactive::$e")
//            HelperUtil.deliveryOutcomeUpdatePNCWorker.append("\n")
//            HelperUtil.deliveryOutcomeUpdatePNCWorkerMethod(appContext, "deliveryOutcomeUpdatePNCWorkerMethod.txt", HelperUtil.deliveryOutcomeRepo.toString())

        }

    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_sync_channel_id)
        ).setContentTitle("Data Sync").setContentText("Updating PNC to EC records")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true).setOngoing(true).build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(1003, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else ForegroundInfo(1003, notification)
    }
}
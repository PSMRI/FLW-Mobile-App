package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
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
import org.piramalswasthya.sakhi.utils.HelperUtil

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

    override suspend fun doWork(): Result {
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
}
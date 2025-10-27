package org.piramalswasthya.sakhi.repositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.NcdReferalDao
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.ReferralRequest
import org.piramalswasthya.sakhi.network.AmritApiService
import javax.inject.Inject

class NcdReferalRepo@Inject constructor(
    private val referalDao: NcdReferalDao,
    private val preferenceDao: PreferenceDao,
    private val tmcNetworkApiService: AmritApiService
)  {
    suspend fun getReferedNCD(benId: Long): ReferalCache? {
        return withContext(Dispatchers.IO) {
            referalDao.getReferalFromBenId(benId)
        }
    }

    suspend fun pushAndUpdateNCDReferRecord() {
        val unProcessedList = referalDao.getAllUnprocessedReferals()
        if (unProcessedList.isEmpty()) return

        for (ncdRefer in unProcessedList) {
            val request = ReferralRequest(
                refer = ncdRefer,
                beneficiaryRegID = ncdRefer.benId.toString(),
                providerServiceMapID = preferenceDao.getLoggedInUser()!!.serviceMapId,
                serviceID = 0,
                sessionID = 3,
                isSpecialist = false,
                beneficiaryID = ncdRefer.benId,
                nurseFlag = 0,
                doctorFlag = 0,
                visitCode = 0L,
                benVisitID = 0,
                createdBy = preferenceDao.getLoggedInUser()!!.userName,
                parkingPlaceID = preferenceDao.getLoggedInUser()!!.serviceMapId,
                pharmacist_flag = 0,
                vanID = preferenceDao.getLoggedInUser()!!.vanId,
                benFlowID = 0
                )

            val response = tmcNetworkApiService.postRefer(request)

            response?.body()?.string()?.let { body ->
                val jsonBody = JSONObject(body)
                val array = jsonBody.getJSONObject("data")
                val isSuccess = jsonBody.getString("status") == "Success"

                if (isSuccess) {
                    updateSyncStatusRefer(ncdRefer)
                }
                }
            }
        }


    private suspend fun updateSyncStatusRefer(refer: ReferalCache) {
        refer.syncState = SyncState.SYNCED
        referalDao.upsert(refer)
    }
    suspend fun saveReferedNCD(referCache: ReferalCache) {
        withContext(Dispatchers.IO) {
            referalDao.upsert(referCache)
        }
    }
}
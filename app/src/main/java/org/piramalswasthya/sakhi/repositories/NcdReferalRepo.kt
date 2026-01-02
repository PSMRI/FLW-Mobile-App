package org.piramalswasthya.sakhi.repositories
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.NcdReferalDao
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.ReferralRequest
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetCBACRequest
import org.piramalswasthya.sakhi.network.NCDReferalDTO
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class NcdReferalRepo@Inject constructor(
    private val referalDao: NcdReferalDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
)  {
    suspend fun getReferedNCD(benId: Long): ReferalCache? {
        return withContext(Dispatchers.IO) {
            referalDao.getReferalFromBenId(benId)
        }
    }

    fun Long.toApiDateFormat(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(this))
    }
    suspend fun pushAndUpdateNCDReferRecord() {
        val unProcessedList = referalDao.getAllUnprocessedReferals()
        if (unProcessedList.isEmpty()) return

        for (ncdRefer in unProcessedList) {
            val dto = ncdRefer.toDTO()

            val request = ReferralRequest(
                refer = dto,
                )


            val response = tmcNetworkApiService.postRefer(request)

            response?.body()?.string()?.let { body ->
                val jsonBody = JSONObject(body)
                val isSuccess = jsonBody.getString("status") == "Success"

                if (isSuccess) {
                    updateSyncStatusRefer(ncdRefer)
                }
                }
            }
        }

    suspend fun pullAndPersistReferRecord(page: Int = 0): Int {
        val userName = preferenceDao.getLoggedInUser()?.userName!!
        val cbacRequest = GetCBACRequest(userName)

        val response = tmcNetworkApiService.getCbacReferData(
            cbacRequest
        )
        val body = response.body()?.string()?.let { JSONObject(it) }
        body?.getInt("statusCode")?.takeIf { it == 5002 }?.let {
            val user = preferenceDao.getLoggedInUser()!!
            userRepo.refreshTokenTmc(user.userName, user.password)
            pullAndPersistReferRecord(page)
        }
        val dataArray = body?.optJSONArray("data")

        if (dataArray != null && dataArray.length() > 0) {
            val gson = Gson()
            val cbacEntities = mutableListOf<ReferalCache>()

            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val dto = gson.fromJson(item.toString(), NCDReferalDTO::class.java)
                cbacEntities.add(dto.toCache())
            }
            referalDao.insertAll(cbacEntities)


        }
        return 0
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
package org.piramalswasthya.sakhi.repositories
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.NcdReferalDao
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.ReferralRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetCBACRequest
import org.piramalswasthya.sakhi.network.NCDReferalDTO
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class NcdReferalRepo@Inject constructor(
    private val referalDao: NcdReferalDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService,
    private val database: InAppDb
)  {
    suspend fun getNcdFollowUpVisits(benId: Long): List<NCDReferalFormResponseJsonEntity> =
        database.NCDReferalFormResponseJsonDao().getAllVisitsByBeneficiary(benId)

    suspend fun getReferedNCD(benId: Long): ReferalCache? {
        return withContext(Dispatchers.IO) {
            referalDao.getReferalFromBenId(benId)
        }
    }

    fun Long.toApiDateFormat(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        return sdf.format(Date(this))
    }
    suspend fun pushAndUpdateNCDReferRecord() {
        val unProcessedList = referalDao.getAllUnprocessedReferals()
        if (unProcessedList.isEmpty()) return

        for (ncdRefer in unProcessedList) {
            // No HWC visit behind the referral (only CBAC creates one): 0 breaks the server's
            // BenVisitID foreign key, so send no value instead.
            val dto = ncdRefer.toDTO().let {
                it.copy(
                    benVisitID = it.benVisitID?.takeIf { id -> id > 0 },
                    visitCode = it.visitCode?.takeIf { code -> code > 0 }
                )
            }

            val request = ReferralRequest(
                refer = dto,
                )


            val response = tmcNetworkApiService.postRefer(request)

            val body = response?.body()?.string()
            if (body == null) {
                Timber.e("Referral push failed (type=${ncdRefer.type}, benId=${ncdRefer.benId}): HTTP ${response?.code()} ${response?.errorBody()?.string()}")
            }
            body?.let {
                val jsonBody = JSONObject(body)
                val isSuccess = jsonBody.getString("status") == "Success"

                if (isSuccess) {
                    updateSyncStatusRefer(ncdRefer)
                } else {
                    Timber.e("Referral push rejected (type=${ncdRefer.type}, benId=${ncdRefer.benId}): $body")
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
            val existingBenIds = database.benDao.getExistingBenIds(cbacEntities.map { it.benId }).toMutableSet()
            // The server returns its stored BeneficiaryRegID as benId. For a referral without an HWC visit
            // (e.g. General Check-up) that is the ben's real benRegId, so map it back to the beneficiaryId.
            cbacEntities.filter { it.benId !in existingBenIds }.forEach { refer ->
                database.benDao.getBeneficiaryIdByRegId(refer.benId)?.let { beneficiaryId ->
                    refer.benId = beneficiaryId
                    existingBenIds += beneficiaryId
                }
            }
            val validEntities = cbacEntities.filter { it.benId in existingBenIds }
            if (validEntities.size != cbacEntities.size) {
                Timber.w("Referral pull: ${cbacEntities.size - validEntities.size} of ${cbacEntities.size} referrals skipped, beneficiary not on device")
            }
            validEntities.filter { it.type.isNullOrBlank() }.forEach {
                // The server does not return type; keep the local one, else recognise General Check-up by its reason.
                it.type = referalDao.getReferralType(it.benId, it.referralReason)
                    ?: GeneralCheckupFormViewModel.REFERRAL_TYPE.takeIf { _ ->
                        it.referralReason?.startsWith(GeneralCheckupFormViewModel.REASON_PREFIX) == true
                    }
            }
            if (validEntities.isNotEmpty()) {
                referalDao.insertAll(validEntities)
            }


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

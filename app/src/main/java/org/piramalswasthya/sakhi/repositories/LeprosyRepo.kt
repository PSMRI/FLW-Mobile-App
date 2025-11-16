package org.piramalswasthya.sakhi.repositories

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.LeprosyDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.BenWithLeprosyScreeningCache
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyFollowUpRequestDTO
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequestForDisease
import org.piramalswasthya.sakhi.network.LeprosyFollowUpDTO
import org.piramalswasthya.sakhi.network.LeprosyScreeningDTO
import org.piramalswasthya.sakhi.network.LeprosyScreeningRequestDTO
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class LeprosyRepo @Inject constructor(
    private val leprosyDao: LeprosyDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService,
    @ApplicationContext private val context: Context

) {

    suspend fun getLeprosyScreening(benId: Long): LeprosyScreeningCache? {
        return withContext(Dispatchers.IO) {
            leprosyDao.getLeprosyScreening(benId)
        }
    }

    suspend fun saveLeprosyScreening(leprosyScreeningCache: LeprosyScreeningCache) {
        withContext(Dispatchers.IO) {
            leprosyDao.saveLeprosyScreening(leprosyScreeningCache)
        }
    }

    suspend fun updateLerosyScreening(leprosyScreeningCache: LeprosyScreeningCache) {
         withContext(Dispatchers.IO){
             leprosyDao.updateLeprosyScreening(leprosyScreeningCache)
         }
    }

    /* suspend fun getTBSuspected(benId: Long): TBSuspectedCache? {
         return withContext(Dispatchers.IO) {
             malariaDao.getTbSuspected(benId)
         }
     }
 
     suspend fun saveTBSuspected(tbSuspectedCache: TBSuspectedCache) {
         withContext(Dispatchers.IO) {
             malariaDao.saveTbSuspected(tbSuspectedCache)
         }
     }*/

    suspend fun getLeprosyScreeningDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
            try {
                val response = tmcNetworkApiService.getMalariaScreeningData(
                    GetDataPaginatedRequestForDisease(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
                        toDate = getCurrentDate(),
                        diseaseTypeID = 5,
                        userName = user.userName
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit tb screening data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveTBScreeningCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("TB Screening entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveTBScreeningCacheFromResponse(dataObj: String): MutableList<LeprosyScreeningCache> {
        val leprosyScreeningList = mutableListOf<LeprosyScreeningCache>()
        var requestDTO = Gson().fromJson(dataObj, LeprosyScreeningRequestDTO::class.java)
        requestDTO?.leprosyLists?.forEach { leprosyScreeningDTO ->
            leprosyScreeningDTO.homeVisitDate?.let {
                var tbScreeningCache: LeprosyScreeningCache? =
                    leprosyDao.getLeprosyScreening(
                        leprosyScreeningDTO.benId,
                        getLongFromDate(leprosyScreeningDTO.homeVisitDate),
                        getLongFromDate(leprosyScreeningDTO.homeVisitDate) - 19_800_000
                    )
                if (tbScreeningCache == null) {
                    benDao.getBen(leprosyScreeningDTO.benId)?.let {
                        leprosyDao.saveLeprosyScreening(leprosyScreeningDTO.toCache())
                    }
                }
            }
        }
        return leprosyScreeningList
    }


    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsLeprosyScreening()
        return (screeningResult == 1)
    }

    private suspend fun pushUnSyncedRecordsLeprosyScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val leprosyasnList: List<LeprosyScreeningCache> = leprosyDao.getLeprosyScreening(
                SyncState.UNSYNCED)

            val leprosysnDtos = mutableListOf<LeprosyScreeningDTO>()
            leprosyasnList.forEach { cache ->
                leprosysnDtos.add(cache.toDTO())
            }
            if (leprosysnDtos.isEmpty())
                return@withContext 1

            try {
                val response = tmcNetworkApiService.saveLeprosyScreeningData(
                    LeprosyScreeningRequestDTO(
                        userId = user.userId,
                        leprosyLists = leprosysnDtos
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to amrit tb screening data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    updateSyncStatusScreening(leprosyasnList)
                                    return@withContext 1
                                } catch (e: Exception) {
                                    Timber.d("Malaria Screening entries not synced $e")
                                }

                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }



    private suspend fun updateSyncStatusScreening(leprosysnList: List<LeprosyScreeningCache>) {
        leprosysnList.forEach {
            it.syncState = SyncState.SYNCED
            leprosyDao.saveLeprosyScreening(it)
        }
    }


//    /*suspend fun getCurrentFollowUp(benId: Long, visitNumber: Int): LeprosyFollowUpCache? {
//        return withContext(Dispatchers.IO) {
//            leprosyDao.getFollowUpByVisit(benId, visitNumber)
//        }
//    }*/

    suspend fun getAllFollowUpsForBeneficiary(benId: Long): List<LeprosyFollowUpCache> {
        return withContext(Dispatchers.IO) {
            leprosyDao.getAllFollowUpsForBeneficiary(benId)
        }
    }

    suspend fun getFollowUpsForCurrentVisit(benId: Long, visitNumber: Int): List<LeprosyFollowUpCache> {
        return withContext(Dispatchers.IO) {
            leprosyDao.getFollowUpsByVisit(benId, visitNumber)
        }
    }

    suspend fun saveFollowUp(followUp: LeprosyFollowUpCache) {
        withContext(Dispatchers.IO) {
            leprosyDao.insertFollowUp(followUp)
        }
    }

    suspend fun updateFollowUp(followUp: LeprosyFollowUpCache) {
        withContext(Dispatchers.IO) {
            leprosyDao.updateFollowUp(followUp)
        }
    }

    suspend fun getFollowUpsForVisit(benId: Long, visitNumber: Int): List<LeprosyFollowUpCache> {
        return leprosyDao.getFollowUpsForVisit(benId, visitNumber)
    }

    suspend fun completeVisitAndStartNext(benId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val screening = leprosyDao.getLeprosyScreening(benId) ?: return@withContext false

            screening.currentVisitNumber++
            screening.leprosyStatus = context.resources.getStringArray(R.array.leprosy_status)[0]
            screening.isConfirmed = false
            screening.leprosySymptomsPosition = 1
            screening.syncState = SyncState.UNSYNCED

            leprosyDao.updateLeprosyScreening(screening)
            true
        }
    }



    suspend fun getBenWithLeprosyData(benId: Long): BenWithLeprosyScreeningCache? {
        return withContext(Dispatchers.IO) {
            benDao.getBenWithLeprosyScreeningAndFollowUps(benId)
        }
    }


    suspend fun getAllLeprosyDataFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getAllLeprosyData(
                    GetDataPaginatedRequestForDisease(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
                        toDate = getCurrentDate(),
                        diseaseTypeID = 5,
                        userName = user.userName
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        Timber.d("Raw leprosy response: $responseString")

                        val jsonObj = JSONObject(responseString)

                        // Use the correct field names from the response
                        val statusCodeFromResponse = jsonObj.getInt("statusCode")
                        val status = jsonObj.getString("status")

                        Timber.d("Pull all leprosy data - Status: $statusCodeFromResponse, Status: $status")

                        when (statusCodeFromResponse) {
                            200 -> {
                                try {
                                    val dataArray = jsonObj.getJSONArray("data")
                                    saveAllLeprosyDataFromResponse(dataArray.toString())
                                    return@withContext 1
                                } catch (e: Exception) {
                                    Timber.d("Leprosy entries not synced $e")
                                    return@withContext 0
                                }
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(user.userName, user.password))
                                    throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            else -> {
                                throw IllegalStateException("$statusCodeFromResponse received, dont know what todo!?")
                            }
                        }
                    }
                } else {
                    Timber.d("HTTP error for leprosy data: $statusCode")
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("get_all_leprosy error : $e")
                return@withContext -2
            } catch (e: JSONException) {
                Timber.d("JSON parsing error for leprosy data: $e")
                return@withContext -1
            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_all_leprosy error : $e")
                return@withContext -1
            } catch (e: Exception) {
                Timber.d("get_all_leprosy unexpected error : $e")
                return@withContext -1
            }
            -1
        }
    }

    suspend fun getAllLeprosyFollowUpDataFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getAllLeprosyFollowUpData(
                    GetDataPaginatedRequestForDisease(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
                        toDate = getCurrentDate(),
                        diseaseTypeID = 5,
                        userName = user.userName
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        Timber.d("Raw leprosy followup response: $responseString")

                        val jsonObj = JSONObject(responseString)

                        // Use the correct field names from the response
                        val statusCodeFromResponse = jsonObj.getInt("statusCode")
                        val status = jsonObj.getString("status")

                        Timber.d("Pull all leprosy followup data - Status: $statusCodeFromResponse, Status: $status")

                        when (statusCodeFromResponse) {
                            200 -> {
                                try {
                                    val dataArray = jsonObj.getJSONArray("data")
                                    saveAllLeprosyFollowUpDataFromResponse(dataArray.toString())
                                    return@withContext 1
                                } catch (e: Exception) {
                                    Timber.d("Leprosy followup entries not synced $e")
                                    return@withContext 0
                                }
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(user.userName, user.password))
                                    throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            else -> {
                                throw IllegalStateException("$statusCodeFromResponse received, dont know what todo!?")
                            }
                        }
                    }
                } else {
                    Timber.d("HTTP error for leprosy followup data: $statusCode")
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("get_all_leprosy_followup error : $e")
                return@withContext -2
            } catch (e: JSONException) {
                Timber.d("JSON parsing error for leprosy followup data: $e")
                return@withContext -1
            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_all_leprosy_followup error : $e")
                return@withContext -1
            } catch (e: Exception) {
                Timber.d("get_all_leprosy_followup unexpected error : $e")
                return@withContext -1
            }
            -1
        }
    }

    // Upsync - Push Unsynced Leprosy FollowUp Data to Server
    suspend fun pushUnSyncedLeprosyFollowUpData(): Int {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

            val unsyncedFollowUps: List<LeprosyFollowUpCache> = leprosyDao.getAllFollowUpsByBenId().filter {
                it.syncState == SyncState.UNSYNCED
            }

            val followUpDtos = unsyncedFollowUps.map { it.toDTO() }

            if (followUpDtos.isEmpty()) return@withContext 1

            try {
                val response = tmcNetworkApiService.saveLeprosyFollowUpData(
                    followUpDtos
                    )

                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push leprosy followup data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                updateSyncStatusFollowUps(unsyncedFollowUps)
                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(user.userName, user.password))
                                    throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                val errorMessage = jsonObj.getString("errorMessage")
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
                            }
                        }
                    }
                }
            } catch (e: SocketTimeoutException) {
                Timber.d("push_leprosy_followup error : $e")
                return@withContext -2
            } catch (e: java.lang.IllegalStateException) {
                Timber.d("push_leprosy_followup error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveAllLeprosyDataFromResponse(dataObj: String) {


        try {

            val leprosyList = Gson().fromJson(dataObj, Array<LeprosyScreeningDTO>::class.java)


            leprosyList?.forEachIndexed { index, leprosyScreeningDTO ->


                leprosyScreeningDTO.homeVisitDate?.let { visitDate ->

                    val visitLong = getLongFromDate(visitDate)
                    val previousVisitWindow = visitLong - 19_800_000



                    val existingScreening = leprosyDao.getLeprosyScreening(
                        leprosyScreeningDTO.benId,
                        visitLong,
                        previousVisitWindow
                    )

                    if (existingScreening == null) {


                        val benExists = benDao.getBen(leprosyScreeningDTO.benId)

                        if (benExists != null) {
                            val cacheObj = leprosyScreeningDTO.toCache().copy(
                                syncState = SyncState.SYNCED
                            )
                            leprosyDao.saveLeprosyScreening(cacheObj)


                        } else {
                            Timber.w(" Ben does NOT exist locally → Skipping save for benId=${leprosyScreeningDTO.benId}")
                        }

                    } else {

                        Timber.d(
                            " Existing screening FOUND (id=${existingScreening.id}) → Updating record for benId=${leprosyScreeningDTO.benId}"
                        )

                        val updatedCache = leprosyScreeningDTO.toCache().copy(
                            id = existingScreening.id,
                            syncState = SyncState.SYNCED
                        )

                        leprosyDao.updateLeprosyScreening(updatedCache)

                    }
                } ?: run {
                    Timber.w(" homeVisitDate is NULL for benId=${leprosyScreeningDTO.benId} → Skipping this record")
                }
            }

            Timber.d(" Successfully saved ${leprosyList?.size ?: 0} leprosy screening records")

        } catch (e: Exception) {
            Timber.e(e, "Error saving leprosy data")
            throw e
        }
    }


    private suspend fun saveAllLeprosyFollowUpDataFromResponse(dataObj: String) {
        try {
            val followUpList = Gson().fromJson(dataObj, Array<LeprosyFollowUpDTO>::class.java)
            followUpList?.forEach { followUpDTO ->
                followUpDTO.followUpDate?.let { followUpDate ->
                    leprosyDao.insertFollowUp(followUpDTO.toCache().copy(
                        syncState = SyncState.SYNCED
                    ))
                }
            }
            Timber.d("Successfully upserted ${followUpList?.size ?: 0} leprosy followup records")
        } catch (e: Exception) {
            Timber.d("Error saving leprosy followup data: $e")
            throw e
        }
    }

    private suspend fun updateSyncStatusFollowUps(followUpList: List<LeprosyFollowUpCache>) {
        followUpList.forEach {
            it.syncState = SyncState.SYNCED
            leprosyDao.updateFollowUp(it)
        }
    }

    suspend fun pushAllUnSyncedRecords(): Boolean {
        val followUpResult = pushUnSyncedLeprosyFollowUpData()
        return ( followUpResult == 1)
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        private fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
            val dateString = dateFormat.format(millis)
            val timeString = timeFormat.format(millis)
            return "${dateString}T${timeString}.000Z"
        }

        private fun getLongFromDate(dateString: String): Long {
            val f = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
        }
    }


}
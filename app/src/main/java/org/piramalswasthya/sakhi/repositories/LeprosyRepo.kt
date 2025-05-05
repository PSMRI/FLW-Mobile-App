package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.LeprosyDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequestForDisease
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
    private val tmcNetworkApiService: AmritApiService
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
                        diseaseTypeID = 5
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
            if (leprosysnDtos.isEmpty()) return@withContext 1
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


    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        private fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
            val dateString = dateFormat.format(millis)
            val timeString = timeFormat.format(millis)
            return "${dateString}T${timeString}.000Z"
        }

        private fun getLongFromDate(dateString: String): Long {
            //Jul 22, 2023 8:17:23 AM"
            val f = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
        }
    }


}
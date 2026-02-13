package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.AesDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.network.AESScreeningDTO
import org.piramalswasthya.sakhi.network.AESScreeningRequestDTO
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequestForDisease
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AESRepo @Inject constructor(
    private val aesDao: AesDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {

    suspend fun getAESScreening(benId: Long): AESScreeningCache? {
        return withContext(Dispatchers.IO) {
            aesDao.getAESScreening(benId)
        }
    }

    suspend fun saveAESScreening(aesScreeningCache: AESScreeningCache) {
        withContext(Dispatchers.IO) {
            aesDao.saveAESScreening(aesScreeningCache)
        }
    }

    suspend fun getAESScreeningDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = tmcNetworkApiService.getMalariaScreeningData(
                    GetDataPaginatedRequestForDisease(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
                        toDate = getCurrentDate(),
                        diseaseTypeID = 3
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit AES screening data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveAESScreeningCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("AES Screening entries not synced $e")
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

    private suspend fun saveAESScreeningCacheFromResponse(dataObj: String): MutableList<AESScreeningCache> {
        val aesScreeningList = mutableListOf<AESScreeningCache>()
        var requestDTO = Gson().fromJson(dataObj, AESScreeningRequestDTO::class.java)
        requestDTO?.aesJeLists?.forEach { aesScreeningDTO ->
            aesScreeningDTO.visitDate.let {
                var aesScreeningCache: AESScreeningCache? =
                    aesDao.getAESScreening(
                        aesScreeningDTO.benId,
                        getLongFromDate(aesScreeningDTO.visitDate),
                        getLongFromDate(aesScreeningDTO.visitDate) - 19_800_000
                    )
                if (aesScreeningCache == null) {
                    benDao.getBen(aesScreeningDTO.benId)?.let {
                        aesDao.saveAESScreening(aesScreeningDTO.toCache())
                    }
                }
            }
        }
        return aesScreeningList
    }


    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsMalariaScreening()
        return (screeningResult == 1)
    }

    private suspend fun pushUnSyncedRecordsMalariaScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val aesSnList: List<AESScreeningCache> = aesDao.getAESScreening(
                SyncState.UNSYNCED)

            val aesSnDtos = mutableListOf<AESScreeningDTO>()
            aesSnList.forEach { cache ->
                aesSnDtos.add(cache.toDTO())
            }
            if (aesSnDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveAESScreeningData(
                    AESScreeningRequestDTO(
                        userId = user.userId,
                        aesJeLists = aesSnDtos
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
                                    updateSyncStatusScreening(aesSnList)
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


    private suspend fun updateSyncStatusScreening(aesAsList: List<AESScreeningCache>) {
        aesAsList.forEach {
            it.syncState = SyncState.SYNCED
            aesDao.saveAESScreening(it)
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
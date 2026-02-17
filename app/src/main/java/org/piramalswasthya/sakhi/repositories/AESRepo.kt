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
                Timber.e("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.e("get_tb error : $e")
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


    // RECORD-LEVEL ISOLATION: Coordinator always returns true so the
    // WorkManager worker succeeds. Failed records stay UNSYNCED for next cycle.
    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsAESScreening()
        Timber.d("AES push result: screening=$screeningResult")
        // Worker succeeds — failed records stay UNSYNCED for next cycle
        return true
    }

    // RECORD-LEVEL ISOLATION: AES Screening records are now sent in
    // chunks of 20 instead of one giant batch. Previously, if ANY record in
    // the batch was malformed, the ENTIRE batch failed and ALL records stayed
    // UNSYNCED. Now each chunk is independent — one bad chunk doesn't affect
    // the others. Failed chunks' records stay UNSYNCED for the next sync cycle.
    private suspend fun pushUnSyncedRecordsAESScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val aesSnList: List<AESScreeningCache> = aesDao.getAESScreening(SyncState.UNSYNCED)

            if (aesSnList.isEmpty()) return@withContext 1

            val CHUNK_SIZE = 20
            val chunks = aesSnList.chunked(CHUNK_SIZE)
            var successCount = 0
            var failCount = 0

            for (chunk in chunks) {
                try {
                    val chunkDtos = chunk.map { it.toDTO() }

                    val response = tmcNetworkApiService.saveAESScreeningData(
                        AESScreeningRequestDTO(
                            userId = user.userId,
                            aesJeLists = chunkDtos
                        )
                    )
                    val statusCode = response.code()
                    if (statusCode == 200) {
                        val responseString = response.body()?.string()
                        if (responseString != null) {
                            val jsonObj = JSONObject(responseString)
                            val responseStatusCode = jsonObj.getInt("statusCode")
                            Timber.d("Push to Amrit AES Screening chunk: $responseStatusCode")
                            when (responseStatusCode) {
                                200 -> {
                                    updateSyncStatusScreening(chunk)
                                    successCount += chunk.size
                                }

                                401, 5002 -> {
                                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                        Timber.d("Token refreshed, AES chunk will retry next cycle")
                                    }
                                    failCount += chunk.size
                                }

                                else -> {
                                    Timber.e("AES Screening chunk failed with statusCode: $responseStatusCode")
                                    failCount += chunk.size
                                }
                            }
                        }
                    } else {
                        Timber.e("AES Screening chunk HTTP error: $statusCode")
                        failCount += chunk.size
                    }
                } catch (e: Exception) {
                    Timber.e(e, "AES Screening chunk push failed: ${chunk.size} records")
                    failCount += chunk.size
                }
            }

            Timber.d("AES Screening push complete: $successCount succeeded, $failCount failed out of ${aesSnList.size}")
            return@withContext 1
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
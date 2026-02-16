package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.KalaAzarDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequestForDisease
import org.piramalswasthya.sakhi.network.KALAZARScreeningDTO
import org.piramalswasthya.sakhi.network.KalaAzarScreeningRequestDTO
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class KalaAzarRepo @Inject constructor(
    private val kalaAzarDao: KalaAzarDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {

    suspend fun getKalaAzarScreening(benId: Long): KalaAzarScreeningCache? {
        return withContext(Dispatchers.IO) {
            kalaAzarDao.getKalaAzarScreening(benId)
        }
    }

    suspend fun saveKalaAzarScreening(kalaAzarScreeningCache: KalaAzarScreeningCache) {
        withContext(Dispatchers.IO) {
            kalaAzarDao.saveKalaAzarScreening(kalaAzarScreeningCache)
        }
    }

    suspend fun getKalaAzarSuspected(benId: Long): KalaAzarScreeningCache? {
        return withContext(Dispatchers.IO) {
            kalaAzarDao.getKalaAzarSuspected(benId)
        }
    }

   /* suspend fun saveKalaAzarSuspected(tbSuspectedCache: TBSuspectedCache) {
        withContext(Dispatchers.IO) {
            kalaAzarDao.saveTbSuspected(tbSuspectedCache)
        }
    }*/

    suspend fun getKalaAzarScreeningDetailsFromServer(): Int {
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
                        diseaseTypeID = 2
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
                                    saveKalaAzarScreeningCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Kala Azar Screening entries not synced $e")
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

    private suspend fun saveKalaAzarScreeningCacheFromResponse(dataObj: String): MutableList<KalaAzarScreeningCache> {
        val kalaAzarScreeningList = mutableListOf<KalaAzarScreeningCache>()
        var requestDTO = Gson().fromJson(dataObj, KalaAzarScreeningRequestDTO::class.java)
        requestDTO?.kalaAzarLists?.forEach { kalaAzarScreeningDTO ->
            kalaAzarScreeningDTO.visitDate?.let {
                var kalaAzarScreeningCache: KalaAzarScreeningCache? =
                    kalaAzarDao.getKalaAzarScreening(
                        kalaAzarScreeningDTO.benId,
                        getLongFromDate(kalaAzarScreeningDTO.visitDate),
                        getLongFromDate(kalaAzarScreeningDTO.visitDate) - 19_800_000
                    )
                if (kalaAzarScreeningCache == null) {
                    benDao.getBen(kalaAzarScreeningDTO.benId)?.let {
                        kalaAzarDao.saveKalaAzarScreening(kalaAzarScreeningDTO.toCache())
                    }
                }
            }
        }
        return kalaAzarScreeningList
    }



    // RECORD-LEVEL ISOLATION: Coordinator always returns true so the
    // WorkManager worker succeeds. Failed records stay UNSYNCED for next cycle.
    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsKalaAzarScreening()
        Timber.d("Kala Azar push result: screening=$screeningResult")
        // Worker succeeds — failed records stay UNSYNCED for next cycle
        return true
    }

    // RECORD-LEVEL ISOLATION: Kala Azar Screening records are now sent in
    // chunks of 20 instead of one giant batch. Previously, if ANY record in
    // the batch was malformed, the ENTIRE batch failed and ALL records stayed
    // UNSYNCED. Now each chunk is independent — one bad chunk doesn't affect
    // the others. Failed chunks' records stay UNSYNCED for the next sync cycle.
    private suspend fun pushUnSyncedRecordsKalaAzarScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val tbsnList: List<KalaAzarScreeningCache> = kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED)

            if (tbsnList.isEmpty()) return@withContext 1

            val CHUNK_SIZE = 20
            val chunks = tbsnList.chunked(CHUNK_SIZE)
            var successCount = 0
            var failCount = 0

            for (chunk in chunks) {
                try {
                    val chunkDtos = chunk.map { it.toDTO() }

                    val response = tmcNetworkApiService.saveKalaAzarScreeningData(
                        KalaAzarScreeningRequestDTO(
                            userId = user.userId,
                            kalaAzarLists = chunkDtos
                        )
                    )
                    val statusCode = response.code()
                    if (statusCode == 200) {
                        val responseString = response.body()?.string()
                        if (responseString != null) {
                            val jsonObj = JSONObject(responseString)
                            val responseStatusCode = jsonObj.getInt("statusCode")
                            Timber.d("Push to Amrit Kala Azar Screening chunk: $responseStatusCode")
                            when (responseStatusCode) {
                                200 -> {
                                    updateSyncStatusScreening(chunk)
                                    successCount += chunk.size
                                }

                                401, 5002 -> {
                                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                        Timber.d("Token refreshed, Kala Azar chunk will retry next cycle")
                                    }
                                    failCount += chunk.size
                                }

                                else -> {
                                    Timber.e("Kala Azar Screening chunk failed with statusCode: $responseStatusCode")
                                    failCount += chunk.size
                                }
                            }
                        }
                    } else {
                        Timber.e("Kala Azar Screening chunk HTTP error: $statusCode")
                        failCount += chunk.size
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Kala Azar Screening chunk push failed: ${chunk.size} records")
                    failCount += chunk.size
                }
            }

            Timber.d("Kala Azar Screening push complete: $successCount succeeded, $failCount failed out of ${tbsnList.size}")
            return@withContext 1
        }
    }



    private suspend fun updateSyncStatusScreening(tbsnList: List<KalaAzarScreeningCache>) {
        tbsnList.forEach {
            it.syncState = SyncState.SYNCED
            kalaAzarDao.saveKalaAzarScreening(it)
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
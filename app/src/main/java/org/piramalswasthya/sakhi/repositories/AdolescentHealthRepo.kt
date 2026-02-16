package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.AdolescentHealthDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.network.AdolescentHealthRequestDTO
import org.piramalswasthya.sakhi.network.AdolscentHealthDTO
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AdolescentHealthRepo @Inject constructor(
    private val adolescentHealthDao: AdolescentHealthDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {

    suspend fun getAdolescentHealth(benId: Long): AdolescentHealthCache? {
        return withContext(Dispatchers.IO) {
            adolescentHealthDao.getAdolescentHealth(benId)
        }
    }

    suspend fun saveAdolescentHealth(adolescentHealthCache : AdolescentHealthCache) {
        withContext(Dispatchers.IO) {
            adolescentHealthDao.saveAdolescentHealth(adolescentHealthCache)
        }
    }


    suspend fun getadolescentHealthCacheFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
            try {
                val response = tmcNetworkApiService.getAdolescentHealthData(
                    GetDataPaginatedRequest(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
                        toDate = getCurrentDate()
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit adolescent screening data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveadolescentHealthCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Adolescent Screening entries not synced $e")
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

    private suspend fun saveadolescentHealthCacheFromResponse(dataObj: String): MutableList<TBScreeningCache> {
        val tbScreeningList = mutableListOf<TBScreeningCache>()
        var requestDTO = Gson().fromJson(dataObj, AdolescentHealthRequestDTO::class.java)
        requestDTO?.adolescentHealths?.forEach { tbScreeningDTO ->
            tbScreeningDTO.visitDate?.let {
                var tbScreeningCache: AdolescentHealthCache? =
                    adolescentHealthDao.getAdolescentHealth(
                        tbScreeningDTO.benId,
                        getLongFromDate(tbScreeningDTO.visitDate),
                        getLongFromDate(tbScreeningDTO.visitDate) - 19_800_000
                    )
                if (tbScreeningCache == null) {
                    benDao.getBen(tbScreeningDTO.benId)?.let {
                        adolescentHealthDao.saveAdolescentHealth(tbScreeningDTO.toCache())
                    }
                }
            }
        }
        return tbScreeningList
    }

    // RECORD-LEVEL ISOLATION: Coordinator always returns true so the
    // WorkManager worker succeeds. Failed records stay UNSYNCED for next cycle.
    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsAdolescentScreening()
        Timber.d("Adolescent Health push result: screening=$screeningResult")
        // Worker succeeds — failed records stay UNSYNCED for next cycle
        return true
    }

    // RECORD-LEVEL ISOLATION: Adolescent Health records are now sent in
    // chunks of 20 instead of one giant batch. Previously, if ANY record in
    // the batch was malformed, the ENTIRE batch failed and ALL records stayed
    // UNSYNCED. Now each chunk is independent — one bad chunk doesn't affect
    // the others. Failed chunks' records stay UNSYNCED for the next sync cycle.
    private suspend fun pushUnSyncedRecordsAdolescentScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val tbsnList: List<AdolescentHealthCache> = adolescentHealthDao.getAdolescentHealth(SyncState.UNSYNCED)

            if (tbsnList.isEmpty()) return@withContext 1

            val CHUNK_SIZE = 20
            val chunks = tbsnList.chunked(CHUNK_SIZE)
            var successCount = 0
            var failCount = 0

            for (chunk in chunks) {
                try {
                    val chunkDtos = chunk.map { it.toDTO() }

                    val response = tmcNetworkApiService.saveAdolescentHealthData(
                        AdolescentHealthRequestDTO(
                            userId = user.userId,
                            adolescentHealths = chunkDtos
                        )
                    )
                    val statusCode = response.code()
                    if (statusCode == 200) {
                        val responseString = response.body()?.string()
                        if (responseString != null) {
                            val jsonObj = JSONObject(responseString)
                            val responseStatusCode = jsonObj.getInt("statusCode")
                            Timber.d("Push to Amrit Adolescent Health chunk: $responseStatusCode")
                            when (responseStatusCode) {
                                200 -> {
                                    updateSyncStatusScreening(chunk)
                                    successCount += chunk.size
                                }

                                401, 5002 -> {
                                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                        Timber.d("Token refreshed, Adolescent Health chunk will retry next cycle")
                                    }
                                    failCount += chunk.size
                                }

                                else -> {
                                    Timber.e("Adolescent Health chunk failed with statusCode: $responseStatusCode")
                                    failCount += chunk.size
                                }
                            }
                        }
                    } else {
                        Timber.e("Adolescent Health chunk HTTP error: $statusCode")
                        failCount += chunk.size
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Adolescent Health chunk push failed: ${chunk.size} records")
                    failCount += chunk.size
                }
            }

            Timber.d("Adolescent Health push complete: $successCount succeeded, $failCount failed out of ${tbsnList.size}")
            return@withContext 1
        }
    }



    private suspend fun updateSyncStatusScreening(tbsnList: List<AdolescentHealthCache>) {
        tbsnList.forEach {
            it.syncState = SyncState.SYNCED
            adolescentHealthDao.saveAdolescentHealth(it)
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
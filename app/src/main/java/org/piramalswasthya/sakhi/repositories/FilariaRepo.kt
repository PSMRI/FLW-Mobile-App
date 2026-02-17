package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.FilariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.FilariaScreeningDTO
import org.piramalswasthya.sakhi.network.FilariaScreeningRequestDTO
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequestForDisease
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class FilariaRepo @Inject constructor(
    private val filariaDao: FilariaDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {

    suspend fun getFilariaScreening(benId: Long): FilariaScreeningCache? {
        return withContext(Dispatchers.IO) {
            filariaDao.getFilariaScreening(benId)
        }
    }

    suspend fun saveFilariaScreening(filariaScreeningCache: FilariaScreeningCache) {
        withContext(Dispatchers.IO) {
            filariaDao.saveFilariaScreening(filariaScreeningCache)
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

    suspend fun getFilariaScreeningDetailsFromServer(): Int {
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
                        diseaseTypeID = 4
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit Filaria screening data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveAESScreeningCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Filaria Screening entries not synced $e")
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

    private suspend fun saveAESScreeningCacheFromResponse(dataObj: String): MutableList<FilariaScreeningCache> {
        val filariaScreeningList = mutableListOf<FilariaScreeningCache>()
        var requestDTO = Gson().fromJson(dataObj, FilariaScreeningRequestDTO::class.java)
        requestDTO?.filariaLists?.forEach { filariaScreeningDTO ->
            filariaScreeningDTO.mdaHomeVisitDate?.let {
                var aesScreeningCache: FilariaScreeningCache? =
                    filariaDao.getFilariaScreening(
                        filariaScreeningDTO.benId,
                        getLongFromDate(filariaScreeningDTO.mdaHomeVisitDate),
                        getLongFromDate(filariaScreeningDTO.mdaHomeVisitDate) - 19_800_000
                    )
                if (aesScreeningCache == null) {
                    benDao.getBen(filariaScreeningDTO.benId)?.let {
                        filariaDao.saveFilariaScreening(filariaScreeningDTO.toCache())
                    }
                }
            }
        }
        return filariaScreeningList
    }

    /* suspend fun getTbSuspectedDetailsFromServer(): Int {
         return withContext(Dispatchers.IO) {
             val user =
                 preferenceDao.getLoggedInUser()
                     ?: throw IllegalStateException("No user logged in!!")
             val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
             try {
                 val response = tmcNetworkApiService.getTBSuspectedData(
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
                         Timber.d("Pull from amrit tb suspected data : $responseStatusCode")
                         when (responseStatusCode) {
                             200 -> {
                                 try {
                                     val dataObj = jsonObj.getString("data")
                                     saveTBSuspectedCacheFromResponse(dataObj)
                                 } catch (e: Exception) {
                                     Timber.d("TB Suspected entries not synced $e")
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
                                 throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
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
     }*/

    /*  private suspend fun saveTBSuspectedCacheFromResponse(dataObj: String): MutableList<TBSuspectedCache> {
          val tbSuspectedList = mutableListOf<TBSuspectedCache>()
          val requestDTO = Gson().fromJson(dataObj, TBSuspectedRequestDTO::class.java)
          requestDTO?.tbSuspectedList?.forEach { tbSuspectedDTO ->
              tbSuspectedDTO.visitDate?.let {
                  val tbSuspectedCache: TBSuspectedCache? =
                      malariaDao.getTbSuspected(
                          tbSuspectedDTO.benId,
                          getLongFromDate(tbSuspectedDTO.visitDate),
                          getLongFromDate(tbSuspectedDTO.visitDate) - 19_800_000
                      )
                  if (tbSuspectedCache == null) {
                      benDao.getBen(tbSuspectedDTO.benId)?.let {
                          malariaDao.saveTbSuspected(tbSuspectedDTO.toCache())
                      }
                  }
              }
          }
          return tbSuspectedList
      }*/

    // RECORD-LEVEL ISOLATION: Coordinator always returns true so the
    // WorkManager worker succeeds. Failed records stay UNSYNCED for next cycle.
    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsFilariaScreening()
        Timber.d("Filaria push result: screening=$screeningResult")
        // Worker succeeds — failed records stay UNSYNCED for next cycle
        return true
    }

    // RECORD-LEVEL ISOLATION: Filaria Screening records are now sent in
    // chunks of 20 instead of one giant batch. Previously, if ANY record in
    // the batch was malformed, the ENTIRE batch failed and ALL records stayed
    // UNSYNCED. Now each chunk is independent — one bad chunk doesn't affect
    // the others. Failed chunks' records stay UNSYNCED for the next sync cycle.
    private suspend fun pushUnSyncedRecordsFilariaScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val filariaSnList: List<FilariaScreeningCache> = filariaDao.getFilariaScreening(SyncState.UNSYNCED)

            if (filariaSnList.isEmpty()) return@withContext 1

            val CHUNK_SIZE = 20
            val chunks = filariaSnList.chunked(CHUNK_SIZE)
            var successCount = 0
            var failCount = 0

            for (chunk in chunks) {
                try {
                    val chunkDtos = chunk.map { it.toDTO() }

                    val response = tmcNetworkApiService.saveFilariaScreeningData(
                        FilariaScreeningRequestDTO(
                            userId = user.userId,
                            filariaLists = chunkDtos
                        )
                    )
                    val statusCode = response.code()
                    if (statusCode == 200) {
                        val responseString = response.body()?.string()
                        if (responseString != null) {
                            val jsonObj = JSONObject(responseString)
                            val responseStatusCode = jsonObj.getInt("statusCode")
                            Timber.d("Push to Amrit Filaria Screening chunk: $responseStatusCode")
                            when (responseStatusCode) {
                                200 -> {
                                    updateSyncStatusScreening(chunk)
                                    successCount += chunk.size
                                }

                                401, 5002 -> {
                                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                                        Timber.d("Token refreshed, Filaria chunk will retry next cycle")
                                    }
                                    failCount += chunk.size
                                }

                                else -> {
                                    Timber.e("Filaria Screening chunk failed with statusCode: $responseStatusCode")
                                    failCount += chunk.size
                                }
                            }
                        }
                    } else {
                        Timber.e("Filaria Screening chunk HTTP error: $statusCode")
                        failCount += chunk.size
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Filaria Screening chunk push failed: ${chunk.size} records")
                    failCount += chunk.size
                }
            }

            Timber.d("Filaria Screening push complete: $successCount succeeded, $failCount failed out of ${filariaSnList.size}")
            return@withContext 1
        }
    }

    /* private suspend fun pushUnSyncedRecordsTBSuspected(): Int {
         return withContext(Dispatchers.IO) {
             val user =
                 preferenceDao.getLoggedInUser()
                     ?: throw IllegalStateException("No user logged in!!")

             val tbspList: List<TBSuspectedCache> = tbDao.getTbSuspected(SyncState.UNSYNCED)

             val tbspDtos = mutableListOf<TBSuspectedDTO>()
             tbspList.forEach { cache ->
                 tbspDtos.add(cache.toDTO())
             }
             if (tbspDtos.isEmpty()) return@withContext 1
             try {
                 val response = tmcNetworkApiService.saveTBSuspectedData(
                     TBSuspectedRequestDTO(
                         userId = user.userId,
                         tbSuspectedList = tbspDtos
                     )
                 )
                 val statusCode = response.code()
                 if (statusCode == 200) {
                     val responseString = response.body()?.string()
                     if (responseString != null) {
                         val jsonObj = JSONObject(responseString)

                         val errorMessage = jsonObj.getString("errorMessage")
                         val responseStatusCode = jsonObj.getInt("statusCode")
                         Timber.d("Push to amrit tb screening data : $responseStatusCode")
                         when (responseStatusCode) {
                             200 -> {
                                 try {
                                     updateSyncStatusSuspected(tbspList)
                                     return@withContext 1
                                 } catch (e: Exception) {
                                     Timber.d("TB Screening entries not synced $e")
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
     }*/


    private suspend fun updateSyncStatusScreening(aesAsList: List<FilariaScreeningCache>) {
        aesAsList.forEach {
            it.syncState = SyncState.SYNCED
            filariaDao.saveFilariaScreening(it)
        }
    }

    /* private suspend fun updateSyncStatusSuspected(tbspList: List<TBSuspectedCache>) {
         tbspList.forEach {
             it.syncState = SyncState.SYNCED
             malariaDao.saveTbSuspected(it)
         }
     }*/

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
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

    suspend fun saveKalaAzarScreening(KalaAzarScreeningCache: KalaAzarScreeningCache) {
        withContext(Dispatchers.IO) {
            kalaAzarDao.saveKalaAzarScreening(KalaAzarScreeningCache)
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

/*
    suspend fun getTbSuspectedDetailsFromServer(): Int {
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
//                                    saveTBSuspectedCacheFromResponse(dataObj)
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
                Timber.d("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }
*/

    /*private suspend fun saveKalaAzarSuspectedCacheFromResponse(dataObj: String): MutableList<TBSuspectedCache> {
        val tbSuspectedList = mutableListOf<TBSuspectedCache>()
        val requestDTO = Gson().fromJson(dataObj, TBSuspectedRequestDTO::class.java)
        requestDTO?.tbSuspectedList?.forEach { tbSuspectedDTO ->
            tbSuspectedDTO.visitDate?.let {
                val tbSuspectedCache: TBSuspectedCache? =
                    kalaAzarDao.getTbSuspected(
                        tbSuspectedDTO.benId,
                        getLongFromDate(tbSuspectedDTO.visitDate),
                        getLongFromDate(tbSuspectedDTO.visitDate) - 19_800_000
                    )
                if (tbSuspectedCache == null) {
                    benDao.getBen(tbSuspectedDTO.benId)?.let {
                        kalaAzarDao.saveTbSuspected(tbSuspectedDTO.toCache())
                    }
                }
            }
        }
        return tbSuspectedList
    }*/

    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsKalaAzarScreening()
//        val suspectedResult = pushUnSyncedRecordsTBSuspected()
        return (screeningResult == 1)
//                && (suspectedResult == 1)
    }

    private suspend fun pushUnSyncedRecordsKalaAzarScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val tbsnList: List<KalaAzarScreeningCache> = kalaAzarDao.getKalaAzarScreening(SyncState.UNSYNCED)

            val kalaAzarsnDtos = mutableListOf<KALAZARScreeningDTO>()
            tbsnList.forEach { cache ->
                kalaAzarsnDtos.add(cache.toDTO())
            }
            if (kalaAzarsnDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveKalaAzarScreeningData(
                    KalaAzarScreeningRequestDTO(
                        userId = user.userId,
                        kalaAzarLists = kalaAzarsnDtos
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
                                    updateSyncStatusScreening(tbsnList)
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

//    private suspend fun pushUnSyncedRecordsKalaAxarSuspected(): Int {
//        return withContext(Dispatchers.IO) {
//            val user =
//                preferenceDao.getLoggedInUser()
//                    ?: throw IllegalStateException("No user logged in!!")
//
//            val tbspList: List<KalaAzarScreeningCache> = kalaAzarDao.getKalaAzarSuspected(SyncState.UNSYNCED)
//
//            val tbspDtos = mutableListOf<KALAZARScreeningDTO>()
//            tbspList.forEach { cache ->
//                tbspDtos.add(cache.toDTO())
//            }
//            if (tbspDtos.isEmpty()) return@withContext 1
//            try {
//                val response = tmcNetworkApiService.saveTBSuspectedData(
//                    TBSuspectedRequestDTO(
//                        userId = user.userId,
//                        tbSuspectedList = tbspDtos
//                    )
//                )
//                val statusCode = response.code()
//                if (statusCode == 200) {
//                    val responseString = response.body()?.string()
//                    if (responseString != null) {
//                        val jsonObj = JSONObject(responseString)
//
//                        val errorMessage = jsonObj.getString("errorMessage")
//                        val responseStatusCode = jsonObj.getInt("statusCode")
//                        Timber.d("Push to amrit tb screening data : $responseStatusCode")
//                        when (responseStatusCode) {
//                            200 -> {
//                                try {
//                                    updateSyncStatusSuspected(tbspList)
//                                    return@withContext 1
//                                } catch (e: Exception) {
//                                    Timber.d("TB Screening entries not synced $e")
//                                }
//
//                            }
//
//                            5002 -> {
//                                if (userRepo.refreshTokenTmc(
//                                        user.userName, user.password
//                                    )
//                                ) throw SocketTimeoutException("Refreshed Token!")
//                                else throw IllegalStateException("User Logged out!!")
//                            }
//
//                            5000 -> {
//                                if (errorMessage == "No record found") return@withContext 0
//                            }
//
//                            else -> {
//                                throw IllegalStateException("$responseStatusCode received, dont know what todo!?")
//                            }
//                        }
//                    }
//                }
//
//            } catch (e: SocketTimeoutException) {
//                Timber.d("get_tb error : $e")
//                return@withContext -2
//
//            } catch (e: java.lang.IllegalStateException) {
//                Timber.d("get_tb error : $e")
//                return@withContext -1
//            }
//            -1
//        }
//    }


    private suspend fun updateSyncStatusScreening(tbsnList: List<KalaAzarScreeningCache>) {
        tbsnList.forEach {
            it.syncState = SyncState.SYNCED
            kalaAzarDao.saveKalaAzarScreening(it)
        }
    }

//    private suspend fun updateSyncStatusSuspected(tbspList: List<TBSuspectedCache>) {
//        tbspList.forEach {
//            it.syncState = SyncState.SYNCED
//            kalaAzarDao.saveTbSuspected(it)
//        }
//    }

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
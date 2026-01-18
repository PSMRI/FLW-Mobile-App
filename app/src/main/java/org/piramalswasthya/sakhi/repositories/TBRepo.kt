package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.network.TBConfirmedRequestDTO
import org.piramalswasthya.sakhi.network.TBConfirmedTreatmentDTO
import org.piramalswasthya.sakhi.network.TBScreeningDTO
import org.piramalswasthya.sakhi.network.TBScreeningRequestDTO
import org.piramalswasthya.sakhi.network.TBSuspectedDTO
import org.piramalswasthya.sakhi.network.TBSuspectedRequestDTO
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class TBRepo @Inject constructor(
    private val tbDao: TBDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {

    suspend fun getTBScreening(benId: Long): TBScreeningCache? {
        return withContext(Dispatchers.IO) {
            tbDao.getTbScreening(benId)
        }
    }

    suspend fun saveTBScreening(tbScreeningCache: TBScreeningCache) {
        withContext(Dispatchers.IO) {
            tbDao.saveTbScreening(tbScreeningCache)
        }
    }

    suspend fun getTBSuspected(benId: Long): TBSuspectedCache? {
        return withContext(Dispatchers.IO) {
            tbDao.getTbSuspected(benId)
        }
    }

    suspend fun saveTBSuspected(tbSuspectedCache: TBSuspectedCache) {
        withContext(Dispatchers.IO) {
            tbDao.saveTbSuspected(tbSuspectedCache)
        }
    }

    suspend fun getTBConfirmed(benId: Long): TBConfirmedTreatmentCache?{
        return withContext(Dispatchers.IO) {
            tbDao.getTbConfirmed(benId)
        }
    }
    suspend fun saveTBConfirmed(tbConfirmedTreatmentCache: TBConfirmedTreatmentCache)
    {
        withContext(Dispatchers.IO)
        {
            tbDao.saveTbConfirmed(tbConfirmedTreatmentCache)
        }
    }

    suspend fun getAllFollowUpsForBeneficiary(benId: Long): List<TBConfirmedTreatmentCache> {
        return withContext(Dispatchers.IO) {
            tbDao.getAllFollowUpsForBeneficiary(benId)
        }
    }

    suspend fun getTBScreeningDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
            try {
                val response = tmcNetworkApiService.getTBScreeningData(
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

    private suspend fun saveTBScreeningCacheFromResponse(dataObj: String): MutableList<TBScreeningCache> {
        val tbScreeningList = mutableListOf<TBScreeningCache>()
        var requestDTO = Gson().fromJson(dataObj, TBScreeningRequestDTO::class.java)
        requestDTO?.tbScreeningList?.forEach { tbScreeningDTO ->
            tbScreeningDTO.visitDate?.let {
                var tbScreeningCache: TBScreeningCache? =
                    tbDao.getTbScreening(
                        tbScreeningDTO.benId,
                        getLongFromDate(tbScreeningDTO.visitDate),
                        getLongFromDate(tbScreeningDTO.visitDate) - 19_800_000
                    )
                if (tbScreeningCache == null) {
                    benDao.getBen(tbScreeningDTO.benId)?.let {
                        tbDao.saveTbScreening(tbScreeningDTO.toCache())
                    }
                }
            }
        }
        return tbScreeningList
    }

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
                Timber.d("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveTBSuspectedCacheFromResponse(dataObj: String): MutableList<TBSuspectedCache> {
        val tbSuspectedList = mutableListOf<TBSuspectedCache>()
        val requestDTO = Gson().fromJson(dataObj, TBSuspectedRequestDTO::class.java)
        requestDTO?.tbSuspectedList?.forEach { tbSuspectedDTO ->
            tbSuspectedDTO.visitDate?.let {
                val tbSuspectedCache: TBSuspectedCache? =
                    tbDao.getTbSuspected(
                        tbSuspectedDTO.benId,
                        getLongFromDate(tbSuspectedDTO.visitDate),
                        getLongFromDate(tbSuspectedDTO.visitDate) - 19_800_000
                    )
                if (tbSuspectedCache == null) {
                    benDao.getBen(tbSuspectedDTO.benId)?.let {
                        tbDao.saveTbSuspected(tbSuspectedDTO.toCache())
                    }
                }
            }
        }
        return tbSuspectedList
    }


    suspend fun getTbConfirmedDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {

            try {
                val user =
                    preferenceDao.getLoggedInUser()
                        ?: throw IllegalStateException("No user logged in!!")
                val response = tmcNetworkApiService.getTBConfirmedData()
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
                Timber.d("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }


    private suspend fun saveTBConfirmedCacheFromResponse(dataObj: String): MutableList<TBConfirmedTreatmentCache> {
        val tbConfirmedList = mutableListOf<TBConfirmedTreatmentCache>()
        val requestDTO = Gson().fromJson(dataObj, TBConfirmedRequestDTO::class.java)
        requestDTO?.tbConfirmedList?.forEach { tbConfirmedDTO ->

                val tbSuspectedCache: TBConfirmedTreatmentCache? =
                    tbDao.getALLTbConfirmed(
                        tbConfirmedDTO.benId,

                    )
                if (tbSuspectedCache == null) {
                    benDao.getBen(tbConfirmedDTO.benId)?.let {
                        tbDao.saveTbConfirmed(tbConfirmedDTO.toCache())
                    }
                }

        }
        return tbConfirmedList
    }


    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsTBScreening()
        val suspectedResult = pushUnSyncedRecordsTBSuspected()
        val confirmedResult = pushUnSyncedRecordsTBConfirmed()
        return (screeningResult == 1) && (suspectedResult == 1) && (confirmedResult == 1)
    }

    private suspend fun pushUnSyncedRecordsTBScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val tbsnList: List<TBScreeningCache> = tbDao.getTBScreening(SyncState.UNSYNCED)

            val tbsnDtos = mutableListOf<TBScreeningDTO>()
            tbsnList.forEach { cache ->
                tbsnDtos.add(cache.toDTO())
            }
            if (tbsnDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveTBScreeningData(
                    TBScreeningRequestDTO(
                        userId = user.userId,
                        tbScreeningList = tbsnDtos
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

    private suspend fun pushUnSyncedRecordsTBSuspected(): Int {
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
                Timber.d("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun pushUnSyncedRecordsTBConfirmed(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val tbspList: List<TBConfirmedTreatmentCache> = tbDao.getTbConfirmed(SyncState.UNSYNCED)

            val tbspDtos = mutableListOf<TBConfirmedTreatmentDTO>()
            tbspList.forEach { cache ->
                tbspDtos.add(cache.toDTO())
            }
            if (tbspDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveTBConfirmedData(
                    TBConfirmedRequestDTO(
                        userId = user.userId,
                        tbConfirmedList = tbspDtos
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
                                    updateSyncStatusConfirmed(tbspList)
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
                Timber.d("get_tb error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_tb error : $e")
                return@withContext -1
            }
            -1
        }

        }



    private suspend fun updateSyncStatusScreening(tbsnList: List<TBScreeningCache>) {
        tbsnList.forEach {
            it.syncState = SyncState.SYNCED
            tbDao.saveTbScreening(it)
        }
    }

    private suspend fun updateSyncStatusSuspected(tbspList: List<TBSuspectedCache>) {
        tbspList.forEach {
            it.syncState = SyncState.SYNCED
            tbDao.saveTbSuspected(it)
        }
    }

    private suspend fun updateSyncStatusConfirmed(tbspList: List<TBConfirmedTreatmentCache>) {
        tbspList.forEach {
            it.syncState = SyncState.SYNCED
            tbDao.saveTbConfirmed(it)
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
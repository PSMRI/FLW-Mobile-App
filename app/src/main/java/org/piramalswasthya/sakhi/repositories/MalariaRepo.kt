package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequestForDisease
import org.piramalswasthya.sakhi.network.IRSScreeningRequestDTO
import org.piramalswasthya.sakhi.network.MalariaConfirmedDTO
import org.piramalswasthya.sakhi.network.MalariaConfirmedRequestDTO
import org.piramalswasthya.sakhi.network.MalariaScreeningDTO
import org.piramalswasthya.sakhi.network.MalariaScreeningRequestDTO
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class MalariaRepo @Inject constructor(
    private val malariaDao: MalariaDao,
    private val benDao: BenDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    private val tmcNetworkApiService: AmritApiService
) {

    suspend fun getMalariaScreening(benId: Long): MalariaScreeningCache? {
        return withContext(Dispatchers.IO) {
            malariaDao.getMalariaScreening(benId)
        }
    }

    suspend fun saveMalariaScreening(malariaScreeningCache: MalariaScreeningCache) {
        withContext(Dispatchers.IO) {
            malariaDao.saveMalariaScreening(malariaScreeningCache)
        }
    }

    suspend fun getMalariaConfirmed(benId: Long): MalariaConfirmedCasesCache? {
        return withContext(Dispatchers.IO) {
            malariaDao.getMalariaConfirmed(benId)
        }
    }

    suspend fun saveMalariaConfirmed(tbSuspectedCache: MalariaConfirmedCasesCache) {
        withContext(Dispatchers.IO) {
            malariaDao.saveMalariaConfirmed(tbSuspectedCache)
        }
    }


    suspend fun getIRSScreening(benId: Long): IRSRoundScreening? {
        return withContext(Dispatchers.IO) {
            malariaDao.getIRSScreening(benId)
        }
    }

    suspend fun saveIRSScreening(irsRoundScreening: IRSRoundScreening) {
        withContext(Dispatchers.IO) {
            if (irsRoundScreening.id == 0) {
                malariaDao.saveIRSScreening(irsRoundScreening)

            } else {
                malariaDao.update(irsRoundScreening)

            }
        }
    }

    suspend fun updateIRSRecord(irsRoundScreening: Array<IRSRoundScreening>) {
        withContext(Dispatchers.IO) {
            malariaDao.updateIRS(*irsRoundScreening)
        }
    }

    suspend fun getAllActiveIRSRecords(hhId: Long): List<IRSRoundScreening> {
        return withContext(Dispatchers.IO) {
            malariaDao.getAllActiveIRSRecords(hhId)
        }
    }


    suspend fun getIRSScreeningDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
            try {
                val response = tmcNetworkApiService.getScreeningData(
                   1
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
                                    saveIRSScreeningCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("IRS Screening entries not synced $e")
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


    suspend fun getMalariaScreeningDetailsFromServer(): Int {
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
                        diseaseTypeID = 1
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
                                    saveMalariaScreeningCacheFromResponse(dataObj)
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

    private suspend fun saveMalariaScreeningCacheFromResponse(dataObj: String): MutableList<MalariaScreeningCache> {
        val malariaScreeningList = mutableListOf<MalariaScreeningCache>()
        var requestDTO = Gson().fromJson(dataObj, MalariaScreeningRequestDTO::class.java)
        requestDTO?.malariaLists?.forEach { malariaScreeningDTO ->
            malariaScreeningDTO.caseDate?.let {
                var tbScreeningCache: MalariaScreeningCache? =
                    malariaDao.getMalariaScreening(
                        malariaScreeningDTO.benId,
                        getLongFromDate(malariaScreeningDTO.caseDate),
                        getLongFromDate(malariaScreeningDTO.caseDate) - 19_800_000
                    )
                if (tbScreeningCache == null) {
                    benDao.getBen(malariaScreeningDTO.benId)?.let {
                        malariaDao.saveMalariaScreening(malariaScreeningDTO.toCache())
                    }
                }
            }
        }
        return malariaScreeningList
    }

    private suspend fun saveIRSScreeningCacheFromResponse(dataObj: String): MutableList<IRSRoundScreening> {
        val irsScreeningList = mutableListOf<IRSRoundScreening>()
        var requestDTO = Gson().fromJson(dataObj, IRSScreeningRequestDTO::class.java)
        requestDTO?.rounds?.forEach { irsScreeningDTO ->
            irsScreeningDTO.date?.let {
                var iRsScreeningCache: IRSRoundScreening? =
                    malariaDao.getIRSScreening(
                        irsScreeningDTO.householdId,
                    )
                if (iRsScreeningCache == null) {
                    benDao.getBen(irsScreeningDTO.householdId)?.let {
                        malariaDao.saveIRSScreening(irsScreeningDTO.toCache())
                    }
                }
            }
        }
        return irsScreeningList
    }

    suspend fun getMalariaConfiremedDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = preferenceDao.getLastSyncedTimeStamp()
            try {
                val response = tmcNetworkApiService.getMalariaConfirmedData(
                    GetDataPaginatedRequestForDisease(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = BenRepo.getCurrentDate(Konstants.defaultTimeStamp),
                        toDate = getCurrentDate(),
                        diseaseTypeID = 1
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit malaria confirmed data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveMalariaConfirmedCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Malaria Confirmed entries not synced $e")
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

    private suspend fun saveMalariaConfirmedCacheFromResponse(dataObj: String): MutableList<MalariaConfirmedCasesCache> {
        val malariaConfirmedList = mutableListOf<MalariaConfirmedCasesCache>()
        val requestDTO = Gson().fromJson(dataObj, MalariaConfirmedRequestDTO::class.java)
        requestDTO?.malariaFollowListUp?.forEach { malariaConfirmedDTO ->
            malariaConfirmedDTO.dateOfDiagnosis?.let {
                val malariaConfirmedCache: MalariaConfirmedCasesCache? =
                    malariaDao.getMalariaConfirmed(
                        malariaConfirmedDTO.benId,
                        getLongFromDate(malariaConfirmedDTO.dateOfDiagnosis),
                        getLongFromDate(malariaConfirmedDTO.dateOfDiagnosis) - 19_800_000
                    )
                if (malariaConfirmedCache == null) {
                    benDao.getBen(malariaConfirmedDTO.benId)?.let {
                        malariaDao.saveMalariaConfirmed(malariaConfirmedDTO.toCache())
                    }
                }
            }
        }
        return malariaConfirmedList
    }

    suspend fun pushUnSyncedRecords(): Boolean {
        val screeningResult = pushUnSyncedRecordsMalariaScreening()
        val suspectedResult = pushUnSyncedRecordsTBSuspected()
        return (screeningResult == 1) && (suspectedResult == 1)
    }

    private suspend fun pushUnSyncedRecordsMalariaScreening(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val malariasnList: List<MalariaScreeningCache> = malariaDao.getMalariaScreening(SyncState.UNSYNCED)

            val malariasnDtos = mutableListOf<MalariaScreeningDTO>()
            malariasnList.forEach { cache ->
                malariasnDtos.add(cache.toDTO())
            }
            if (malariasnDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveMalariaScreeningData(
                    MalariaScreeningRequestDTO(
                        userId = user.userId,
                        malariaLists = malariasnDtos
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
                                    updateSyncStatusScreening(malariasnList)
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

    private suspend fun pushUnSyncedRecordsTBSuspected(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val tbspList: List<MalariaConfirmedCasesCache> = malariaDao.getMalariaConfirmed(SyncState.UNSYNCED)

            val tbspDtos = mutableListOf<MalariaConfirmedDTO>()
            tbspList.forEach { cache ->
                tbspDtos.add(cache.toDTO())
            }
            if (tbspDtos.isEmpty()) return@withContext 1
            try {
                val response = tmcNetworkApiService.saveMalariaConfirmedData(
                    MalariaConfirmedRequestDTO(
                        userId = user.userId,
                        malariaFollowListUp = tbspDtos
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


    private suspend fun updateSyncStatusScreening(malariasnList: List<MalariaScreeningCache>) {
        malariasnList.forEach {
            it.syncState = SyncState.SYNCED
            malariaDao.saveMalariaScreening(it)
        }
    }

    private suspend fun updateSyncStatusSuspected(tbspList: List<MalariaConfirmedCasesCache>) {
        tbspList.forEach {
            it.syncState = SyncState.SYNCED
            malariaDao.saveMalariaConfirmed(it)
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


    suspend fun canSubmit(householdId: Long): Boolean {
        val (start, end) = HelperUtil.getYearRange()
        val count = malariaDao.countRoundsInYear(householdId, start, end)
        return count < 4
    }

    suspend fun getCount(householdId: Long) : Int {
        val (start, end) = HelperUtil.getYearRange()
        val count = malariaDao.countRoundsInYear(householdId, start, end)
        return count
    }

    suspend fun submitRound(round: IRSRoundScreening): Boolean {
        return if (canSubmit(round.householdId)) {
            malariaDao.saveIRSScreening(round)
            true
        } else {
            false // already reached limit
        }
    }



}
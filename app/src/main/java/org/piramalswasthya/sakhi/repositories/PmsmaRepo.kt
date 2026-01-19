package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PmsmaPost
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class PmsmaRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val benDao: BenDao,
    private val pmsmaDao: PmsmaDao,
) {
    suspend fun savePmsmaData(pmsmaCache: PMSMACache): Boolean {
        return withContext(Dispatchers.IO) {

            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                pmsmaCache.apply {
                    createdBy = user.userName
                    createdDate = System.currentTimeMillis()
                    updatedBy = user.userName
                    updatedDate = System.currentTimeMillis()
                }
                pmsmaDao.upsert(pmsmaCache)

                true
            } catch (e: Exception) {
                Timber.d("Error : $e raised at savePmsmaData")
                false
            }
        }
    }

    suspend fun processNewPmsma(): Boolean {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val pmsmaList = pmsmaDao.getAllUnprocessedPmsma()

            val pmsmaPostList = mutableSetOf<PmsmaPost>()

            pmsmaList.forEach {
                pmsmaPostList.clear()
                pmsmaPostList.add(it.asPostModel())
                val uploadDone = postDataToAmritServer(pmsmaPostList)
                if (uploadDone) {
                    it.processed = "P"
                    it.syncState = SyncState.SYNCED
                } else {
                    it.syncState = SyncState.UNSYNCED
                }
                pmsmaDao.updatePmsmaRecord(it)
                if (!uploadDone)
                    return@withContext false
            }

            return@withContext true
        }
    }

    private suspend fun postDataToAmritServer(pmsmaPostList: MutableSet<PmsmaPost>): Boolean {
        if (pmsmaPostList.isEmpty())
            return false

        try {
            val response = amritApiService.postPmsmaForm(pmsmaPostList.toList())
            val statusCode = response.code()

            if (statusCode == 200) {
                try {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        if (jsonObj.isNull("statusCode"))
                            throw IllegalStateException("Amrit server not responding properly, Contact Service Administrator!!")

                        when (jsonObj.getInt("statusCode")) {
                            200 -> {
                                Timber.d("Saved Successfully to server")
                                return true
                            }

                            401,5002 -> {
                                val user = preferenceDao.getLoggedInUser()
                                    ?: throw IllegalStateException("User seems to be logged out!!")
                                if (userRepo.refreshTokenTmc(user.userName, user.password))
                                    throw SocketTimeoutException()
                            }

                            else -> {
                                throw IOException("Throwing away IO eXcEpTiOn")
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                //server_resp5();
            }
            Timber.w("Bad Response from server, need to check $pmsmaPostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            return postDataToAmritServer(pmsmaPostList)
        } catch (e: Exception) {
            Timber.d("Caught exception $e here")
            return false
        }
    }

    suspend fun getPmsmaDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = Konstants.defaultTimeStamp
            try {
                val response = amritApiService.getPmsmaData(
                    GetDataPaginatedRequest(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = getCurrentDate(lastTimeStamp),
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
                        Timber.d("Pull from amrit PMSMA data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    savePmsmaCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("PMSMA entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            401,5002 -> {
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
                Timber.d("get_pmsma error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_pmsma error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun savePmsmaCacheFromResponse(dataObj: String): List<PmsmaPost> {
        val pmsmaList = Gson().fromJson(dataObj, Array<PmsmaPost>::class.java).toList()
        pmsmaList.forEach { pmsmaDTO ->
            pmsmaDTO.createdDate?.let {
                val hasBen = benDao.getBen(pmsmaDTO.benId) != null
                val pwrCache: PMSMACache? =
                    pmsmaDao.getPmsma(pmsmaDTO.benId)
                if (hasBen && pwrCache == null) {
                    pmsmaDao.upsert(pmsmaDTO.toPmsmaCache())
                }
            }
        }
        return pmsmaList
    }

    suspend fun getPmsmaByBenId(benId: Long): PMSMACache? {
        return withContext(Dispatchers.IO) {
            pmsmaDao.getPmsma(benId)
        }

    }

    suspend fun setToInactive(eligBenIds: Set<Long>) {
        withContext(Dispatchers.IO) {
            val records = pmsmaDao.getAllPmsma(eligBenIds)
            records.forEach {
                it.isActive = false
                if (it.processed != "N") it.processed = "U"
                it.syncState = SyncState.UNSYNCED
                it.updatedDate = System.currentTimeMillis()
                pmsmaDao.updatePmsmaRecord(it)
            }
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        fun getCurrentDate(millis: Long = System.currentTimeMillis()): String {
            val dateString = dateFormat.format(millis)
            val timeString = timeFormat.format(millis)
            return "${dateString}T${timeString}.000Z"
        }
    }
}
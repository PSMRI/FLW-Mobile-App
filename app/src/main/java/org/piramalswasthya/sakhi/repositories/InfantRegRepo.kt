package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.InfantRegPost
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class InfantRegRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val benDao: BenDao,
    private val infantRegDao: InfantRegDao
) {

    suspend fun getInfantReg(benId: Long, babyIndex: Int): InfantRegCache? {
        return withContext(Dispatchers.IO) {
            infantRegDao.getInfantReg(benId, babyIndex)
        }
    }

    suspend fun getInfantRegFromChildBenId(childBenId: Long): InfantRegCache? {
        return withContext(Dispatchers.IO) {
            infantRegDao.getInfantRegFromChildBenId(childBenId)
        }
    }

    suspend fun saveInfantReg(infantRegCache: InfantRegCache) {
        withContext(Dispatchers.IO) {
            infantRegDao.saveInfantReg(infantRegCache)
        }
    }

    suspend fun processNewInfantRegister(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

            val infantRegList = infantRegDao.getAllUnprocessedInfantReg()

            val infantRegPostList = mutableSetOf<InfantRegPost>()

            // RECORD-LEVEL ISOLATION: Each infant registration record is
            // processed independently. If one record fails, remaining records continue.
            // Failed records stay UNSYNCED and retry on next sync cycle.
            var successCount = 0
            var failCount = 0

            infantRegList.forEach {
                try {
                    infantRegPostList.clear()
                    val ben = benDao.getBen(it.motherBenId)
                        ?: throw IllegalStateException("No beneficiary exists for benId: ${it.motherBenId}!!")
                    infantRegPostList.add(it.asPostModel())
                    it.syncState = SyncState.SYNCING
                    infantRegDao.updateInfantReg(it)
                    val uploadDone = postDataToAmritServer(infantRegPostList)
                    if (uploadDone) {
                        it.processed = "P"
                        it.syncState = SyncState.SYNCED
                        successCount++
                    } else {
                        it.syncState = SyncState.UNSYNCED
                        failCount++
                    }
                    infantRegDao.updateInfantReg(it)
                } catch (e: Exception) {
                    Timber.e(e, "Infant registration push failed for motherBenId: ${it.motherBenId}")
                    it.syncState = SyncState.UNSYNCED
                    infantRegDao.updateInfantReg(it)
                    failCount++
                }
            }

            Timber.d("Infant registration push complete: $successCount succeeded, $failCount failed out of ${infantRegList.size}")
            return@withContext true
        }
    }

    private suspend fun postDataToAmritServer(infantRegPostList: MutableSet<InfantRegPost>, retryCount: Int = 3): Boolean {
        if (infantRegPostList.isEmpty()) return false
        val user =
            preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

        try {

            val response = amritApiService.postInfantRegForm(infantRegPostList.toList())
            val statusCode = response.code()

            if (statusCode == 200) {
                try {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errormessage = jsonObj.getString("errorMessage")
                        if (jsonObj.isNull("statusCode")) throw IllegalStateException("Amrit server not responding properly, Contact Service Administrator!!")
                        val responsestatuscode = jsonObj.getInt("statusCode")

                        when (responsestatuscode) {
                            200 -> {
                                Timber.d("Saved Successfully to server")
                                return true
                            }

                          401, 5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName,
                                        user.password
                                    )
                                ) throw SocketTimeoutException()
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
            Timber.w("Bad Response from server, need to check $infantRegPostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.e("Caught exception $e here")
            if (retryCount > 0) return postDataToAmritServer(infantRegPostList, retryCount - 1)
            Timber.e("postDataToAmritServer: max retries exhausted")
            return false
        } catch (e: JSONException) {
            Timber.e("Caught exception $e here")
            return false
        }
    }

    suspend fun getInfantRegFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = Konstants.defaultTimeStamp
            try {
                val response = amritApiService.getInfantRegData(
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
                        Timber.d("Pull from amrit Infant Reg data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveInfantRegCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Infant Reg entries not synced $e")
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
                Timber.e("get_infant_reg error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.e("get_infant_reg error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveInfantRegCacheFromResponse(dataObj: String): List<InfantRegPost> {
        var infantRegList = Gson().fromJson(dataObj, Array<InfantRegPost>::class.java).toList()
        infantRegList.forEach { infantReg ->
            infantReg.createdDate?.let {
                var infantRegCache: InfantRegCache? =
                    infantRegDao.getInfantReg(infantReg.benId, infantReg.babyIndex)
                if (infantRegCache == null) {
                    infantRegDao.saveInfantReg(infantReg.toCacheModel())
                }
            }
        }
        return infantRegList
    }

    suspend fun getNumBabyRegistered(benId: Long): Int {
        return withContext(Dispatchers.IO) {
            infantRegDao.getNumBabiesRegistered(benId)
        }
    }

    suspend fun setToInactive(eligBenIds: Set<Long>) {
        withContext(Dispatchers.IO) {
            val records = infantRegDao.getAllInfantRegs(eligBenIds)
            records.forEach {
                it.isActive = false
                if (it.processed != "N") it.processed = "U"
                it.syncState = SyncState.UNSYNCED
                it.updatedDate = System.currentTimeMillis()
                infantRegDao.updateInfantReg(it)
            }
        }
    }

    suspend fun update(infantRegCache: InfantRegCache) {
        withContext(Dispatchers.IO) {
            infantRegDao.updateInfantReg(infantRegCache)
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
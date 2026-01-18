package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.CDRPost
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class CdrRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val cdrDao: CdrDao,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao,
) {

    suspend fun saveCdrData(cdrCache: CDRCache): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                cdrDao.upsert(cdrCache)
                true
            } catch (e: Exception) {
                Timber.d("Error : $e raised at saveCdrData")
                false
            }
        }
    }

    suspend fun processNewCdr(): Boolean {
        return withContext(Dispatchers.IO) {
            val cdrList = cdrDao.getAllUnprocessedCdr()

            val cdrPostList = mutableSetOf<CDRPost>()

            cdrList.forEach {
                cdrPostList.clear()
                cdrPostList.add(it.asPostModel())
                it.syncState = SyncState.SYNCING
                cdrDao.update(it)
                val uploadDone = postCdrForm(cdrPostList.toList())
                if (uploadDone) {
                    it.processed = "P"
                    it.syncState = SyncState.SYNCED
                } else {
                    it.syncState = SyncState.UNSYNCED
                }
                cdrDao.update(it)
            }
            return@withContext true
        }
    }

    private suspend fun postCdrForm(cdrPostList: List<CDRPost>): Boolean {
        if (cdrPostList.isEmpty()) return false
        val user =
            preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
        try {
            val response = amritApiService.postCdrForm(cdrPostList.toList())
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
                                Timber.d("CDRs Sent Successfully to server")
                                return true
                            }

                         401,5002 -> {
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
                Timber.w("Bad Response from server $statusCode $response ")
            }
            Timber.w("Bad Response from server, need to check $cdrPostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            return postCdrForm(cdrPostList)
        } catch (e: JSONException) {
            Timber.d("Caught exception $e here")
            return false
        }

    }

    suspend fun getCdrFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = Konstants.defaultTimeStamp
            try {
                val response = amritApiService.getCdrData(
                    GetDataPaginatedRequest(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = MaternalHealthRepo.getCurrentDate(lastTimeStamp),
                        toDate = MaternalHealthRepo.getCurrentDate()
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit PNC Visit data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveCdrCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("CDR visit entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                           401, 5002 -> {
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
                Timber.d("get_pnc error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_pnc error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveCdrCacheFromResponse(dataObj: String) {

        val list =
            Gson().fromJson(dataObj, Array<CDRPost>::class.java).toList()
        list.forEach { pncDTO ->
            val ancCache =
                cdrDao.getCDR(pncDTO.benId)
            if (ancCache == null) {
                cdrDao.upsert(pncDTO.asCacheModel())
            }

        }
        return

    }
}
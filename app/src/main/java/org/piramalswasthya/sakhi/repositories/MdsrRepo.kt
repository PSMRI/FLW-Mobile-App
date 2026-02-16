package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.MDSRCache
import org.piramalswasthya.sakhi.model.MdsrPost
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class MdsrRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val mdsrDao: MdsrDao,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao
) {

    suspend fun saveMdsrData(mdsrCache: MDSRCache): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                mdsrDao.upsert(mdsrCache)
                true
            } catch (e: Exception) {
                Timber.d("Error : $e raised at saveMdsrData")
                false
            }
        }
    }

    suspend fun processNewMdsr(): Boolean {
        return withContext(Dispatchers.IO) {
            val mdsrList = mdsrDao.getAllUnprocessedMdsr()

            val mdsrPostList = mutableSetOf<MdsrPost>()

            // RECORD-LEVEL ISOLATION: Each MDSR record is processed
            // independently with try/catch. Previously this method silently
            // returned true even when records failed to upload (Pattern C).
            // Now failures are logged and tracked while still allowing the
            // worker to succeed (failed records retry on next sync cycle).
            var successCount = 0
            var failCount = 0

            mdsrList.forEach {
                try {
                    mdsrPostList.clear()
                    mdsrPostList.add(it.asPostModel())
                    it.syncState = SyncState.SYNCING
                    mdsrDao.updateMdsrRecord(it)
                    val uploadDone = postMdsrForm(mdsrPostList.toList())
                    if (uploadDone) {
                        it.processed = "P"
                        it.syncState = SyncState.SYNCED
                        successCount++
                    } else {
                        it.syncState = SyncState.UNSYNCED
                        failCount++
                    }
                    mdsrDao.updateMdsrRecord(it)
                } catch (e: Exception) {
                    Timber.e(e, "MDSR push failed for record")
                    it.syncState = SyncState.UNSYNCED
                    mdsrDao.updateMdsrRecord(it)
                    failCount++
                }
            }

            Timber.d("MDSR push complete: $successCount succeeded, $failCount failed out of ${mdsrList.size}")
            return@withContext true
        }
    }

    private suspend fun postMdsrForm(mdsrPostList: List<MdsrPost>): Boolean {
        if (mdsrPostList.isEmpty()) return false
        val user =
            preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")
        try {
            val response = amritApiService.postMdsrForm(mdsrPostList.toList())
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
                                Timber.d("MDSRs Sent Successfully to server")
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
            }
            Timber.w("Bad Response from server, need to check $mdsrPostList $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            return postMdsrForm(mdsrPostList)
        } catch (e: JSONException) {
            Timber.d("Caught exception $e here")
            return false
        }

    }

    suspend fun getMdsrFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = Konstants.defaultTimeStamp
            try {
                val response = amritApiService.getMdsrData(
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
                                    saveMdsrCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("PNC visit entries not synced $e")
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

    private suspend fun saveMdsrCacheFromResponse(dataObj: String) {

        val list =
            Gson().fromJson(dataObj, Array<MdsrPost>::class.java).toList()
        list.forEach { pncDTO ->
            val ancCache =
                mdsrDao.getMDSR(pncDTO.benId)
            if (ancCache == null) {
                mdsrDao.upsert(pncDTO.asCacheModel())
            }

        }
        return

    }

}
package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.HbycPost
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

class HbycRepo @Inject constructor(
    private val database: InAppDb,
    private val preferenceDao: PreferenceDao,
    private val hbycDao: HbycDao,
    private val benDao: BenDao,
    private val userRepo: UserRepo,
    private val amritApiService: AmritApiService
) {

    fun hbycList(benId: Long, hhId: Long) = database.hbycDao.getAllHbycEntries(hhId, benId)

    suspend fun saveHbycData(hbycCache: HBYCCache): Boolean {
        return withContext(Dispatchers.IO) {

            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                hbycCache.apply {
                    createdBy = user.userName
                    createdDate = System.currentTimeMillis()
                }

                database.hbycDao.upsert(hbycCache)

                true
            } catch (e: Exception) {
                Timber.e("Error : $e raised at saveHbycData")
                false
            }
        }
    }

    suspend fun processNewHbyc(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

            val hbycList = database.hbycDao.getAllUnprocessedHBYC()

            val hbycPostList = mutableSetOf<HbycPost>()

            hbycList.forEach {
                try {
                    hbycPostList.clear()
                    val household = database.householdDao.getHousehold(it.hhId)
                        ?: throw IllegalStateException("No household exists for hhId: ${it.hhId}!!")
                    val ben = database.benDao.getBen(it.hhId, it.benId)
                        ?: throw IllegalStateException("No beneficiary exists for benId: ${it.benId}!!")
                    val hbycCount = database.hbycDao.hbycCount()
                    hbycPostList.add(it.asPostModel(user, household, ben, hbycCount))
                    it.syncState = SyncState.SYNCING
                    database.hbycDao.setSynced(it)
                } catch (e: Exception) {
                    Timber.e(e, "HbycRepo: Error processing HBYC record benId=${it.benId}")
                }
            }

            return@withContext true
        }
    }

    /**
     * get all unprocessed data from local db and push it to amrit server
     */
    suspend fun pushHBYCDetails(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val hbycList = database.hbycDao.getAllUnprocessedHbyc()

            val hbycPostSet = mutableSetOf<HbycPost>()

            try {
                hbycList.forEach {
                    val household = database.householdDao.getHousehold(it.hhId)
                        ?: throw IllegalStateException("No household exists for hhId: ${it.hhId}!!")
                    val ben = database.benDao.getBen(it.hhId, it.benId)
                        ?: throw IllegalStateException("No beneficiary exists for benId: ${it.benId}!!")
                    val hbycCount = database.hbycDao.hbycCount()

                    hbycPostSet.add(it.asPostModel(user, household, ben, hbycCount))
                }
                val response = amritApiService.pushHBYCToServer(
                    hbycPostSet.toList()
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to Amrit HBYC data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    updateSyncStatus(hbycList)
                                } catch (e: Exception) {
                                    Timber.d("Child HByC entries sync status not updated $e")
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
                getHBYCDetailsFromServer()
                Timber.e("get hbyc data error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.e("get hbyc error : $e")
                return@withContext -1
            }
            -1
        }
    }

    /**
     * once data is pushed update the sync state to synced and processed to P
     */
    private suspend fun updateSyncStatus(hbycList: List<HBYCCache>) {
        hbycList.forEach {
            it.syncState = SyncState.SYNCED
            it.processed = "P"
            hbycDao.upsert(it)
        }
    }

    /**
     * get hbyc details from server and save it to local db
     */
    suspend fun getHBYCDetailsFromServer(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = Konstants.defaultTimeStamp
            try {
                val response = amritApiService.getHBYCFromServer(
                    GetDataPaginatedRequest(
                        ashaId = user.userId,
                        pageNo = 0,
                        fromDate = HelperUtil.getCurrentDate(lastTimeStamp),
                        toDate = HelperUtil.getCurrentDate()
                    )
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from Amrit HByC data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveChildHBYCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Child HByC entries not synced $e")
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
                getHBYCDetailsFromServer()
                Timber.e("get hbyc data error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.e("get hbyc error : $e")
                return@withContext -1
            }
            -1
        }
    }

    /**
     * extract hbyc objects from api response and save in in app db
     */
    private suspend fun saveChildHBYCacheFromResponse(dataObj: String) {
        val hbycList =
            Gson().fromJson(dataObj, Array<HbycPost>::class.java).toList()

        hbycList.forEach { hbycPost ->
            benDao.getBen(hbycPost.beneficiaryid)?.let {
                var cache =
                    hbycDao.getHbyc(it.householdId, it.beneficiaryId, hbycPost.month.toString())
                cache?.let { it1 ->
                    it1.processed = "P"
                    it1.syncState = SyncState.SYNCED
                    hbycDao.upsert(it1)
                } ?: run {
                    cache = hbycPost.toCache(it.householdId)
                    hbycDao.upsert(cache!!)
                }
            }
        }
    }

}
package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.HbncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HBNCPost
import org.piramalswasthya.sakhi.model.HbncHomeVisit
import org.piramalswasthya.sakhi.model.HbncVisitCard
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

class HbncRepo @Inject constructor(
    private val database: InAppDb,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val preferenceDao: PreferenceDao,
    private val hbncDao: HbncDao,
    private val benDao: BenDao
) {


    fun hbncList(benId: Long, hhId: Long) = database.hbncDao.getAllHbncEntries(hhId, benId)

    suspend fun getHbncCard(benId: Long, hhId: Long): HbncVisitCard? {
        return withContext(Dispatchers.IO) {
            database.hbncDao.getHbnc(hhId, benId, Konstants.hbncCardDay)?.visitCard
        }
    }

    suspend fun getHbncRecord(benId: Long, hhId: Long, day: Int): HBNCCache? {
        return withContext(Dispatchers.IO) {
            database.hbncDao.getHbnc(benId, hhId, day)
        }
    }


    suspend fun getFirstHomeVisit(hhId: Long, benId: Long): HbncHomeVisit? {
        return withContext(Dispatchers.IO) {
            database.hbncDao.getHbnc(hhId, benId, 1)?.homeVisitForm
        }
    }

    suspend fun saveHbncData(hbncCache: HBNCCache): Boolean {
        return withContext(Dispatchers.IO) {

            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
//                hbncCache.apply {
//                    createdBy = user.userName
//                    createdDate = System.currentTimeMillis()
//                }

                database.hbncDao.upsert(hbncCache)

                true
            } catch (e: Exception) {
                Timber.d("Error : $e raised at saveHbncData")
                false
            }
        }
    }

    suspend fun processNewHbnc(): Boolean {
        return withContext(Dispatchers.IO) {
            val user = preferenceDao.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in!!")

            val hbncList = database.hbncDao.getAllUnprocessedHbnc()

            val hbncPostSet = mutableSetOf<HBNCPost>()

            hbncList.forEach {
                hbncPostSet.clear()
                val household = database.householdDao.getHousehold(it.hhId)
                    ?: throw IllegalStateException("No household exists for hhId: ${it.hhId}!!")
                val ben = database.benDao.getBen(it.hhId, it.benId)
                    ?: throw IllegalStateException("No beneficiary exists for benId: ${it.benId}!!")
                val hbncCount = database.hbncDao.hbncCount()
//                hbncPostSet.add(it.asPostModel(user, household, hbncCount))
                it.syncState = SyncState.SYNCING
                database.hbncDao.update(it)
            }
            return@withContext true
        }
    }

    /**
     * get all unprocessed data from local db and push it to amrit server
     */
    suspend fun pushHBNCDetails(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")

            val hbncList = database.hbncDao.getAllUnprocessedHbnc()

            if (hbncList.isEmpty()) return@withContext 1
            val hbncPostSet = mutableSetOf<HBNCPost>()

            try {
                hbncList.forEach {
                    hbncPostSet.add(it.asPostModel(user))
                }
                val response = amritApiService.pushHBNCDetailsToServer(
                    hbncPostSet.toList()
                )
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Push to Amrit HBNC data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    updateSyncStatus(hbncList)
                                } catch (e: Exception) {
                                    Timber.d("Child HBNC entries sync status not updated $e")
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
                getHBNCDetailsFromServer()
                Timber.d("get hbnc data error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get hbnc error : $e")
                return@withContext -1
            }
            -1
        }
    }

    /**
     * once data is pushed update the sync state to synced and processed to P
     */
    private suspend fun updateSyncStatus(hbncList: List<HBNCCache>) {
        hbncList.forEach {
            it.syncState = SyncState.SYNCED
            it.processed = "P"
            hbncDao.update(it)
        }
    }

    /**
     * get hbnc details from server and save it to local db
     */
    suspend fun getHBNCDetailsFromServer(): Int {

        return withContext(Dispatchers.IO) {
            val user =
                preferenceDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            val lastTimeStamp = Konstants.defaultTimeStamp
            try {
                val response = amritApiService.getHBNCDetailsFromServer(
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
                        Timber.d("Pull from Amrit HBNC data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveChildHBNCacheFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Child HBNC entries not synced $e")
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
                getHBNCDetailsFromServer()
                Timber.d("get hbnc data error : $e")
                return@withContext -2

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get hbnc error : $e")
                return@withContext -1
            }
            -1
        }
    }

    /**
     * extract hbnc objects from api response and save in in app db
     */
    private suspend fun saveChildHBNCacheFromResponse(dataObj: String) {
        val hbncList =
            Gson().fromJson(dataObj, Array<HBNCPost>::class.java).toList()

        hbncList.forEach { hbncPost ->
            var cache = hbncDao.getHbnc(hbncPost.hhId, hbncPost.benId, hbncPost.homeVisitDate)
            cache?.let {
                if (it.visitCard == null) it.visitCard = hbncPost.hbncVisitCardDTO?.toCache()
                if (it.part1 == null) it.part1 = hbncPost.hbncPart1DTO?.toCache()
                if (it.part2 == null) it.part2 = hbncPost.hbncPart2DTO?.toCache()
                if (it.homeVisitForm == null) it.homeVisitForm = hbncPost.hbncVisitDTO?.toCache()
                it.processed = "P"
                it.syncState = SyncState.SYNCED
                hbncDao.upsert(it)
            } ?: run {
                benDao.getBen(hbncPost.benId)?.let {
                    cache = hbncPost.toCache(it.householdId)
                    hbncDao.upsert(cache!!)
                }
            }
        }
    }

}
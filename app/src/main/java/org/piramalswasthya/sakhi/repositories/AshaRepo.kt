package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.setToEndOfTheDay
import org.piramalswasthya.sakhi.model.IncentiveActivityListRequest
import org.piramalswasthya.sakhi.model.IncentiveActivityNetwork
import org.piramalswasthya.sakhi.model.IncentiveRecordListRequest
import org.piramalswasthya.sakhi.model.IncentiveRecordNetwork
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.getDateTimeStringFromLong
import org.piramalswasthya.sakhi.network.AmritApiService
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.Calendar
import javax.inject.Inject

class AshaRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val incentiveDao: IncentiveDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo

) {

    val list = incentiveDao.getAllRecords()


    suspend fun pullAndSaveAllAshaActivities(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = amritApiService.getAshaProfileData(user.userId)
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit incentives data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveAshaMasterData(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Incentive master data not synced $e")
                                    return@withContext false
                                }

                                return@withContext true
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                if (errorMessage == "No record found") return@withContext true
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.e("incentives error : $e")
                pullAndSaveAllAshaActivities(user)
                return@withContext true
            } catch (e: Exception) {
                Timber.d("Caught $e at incentives!")
                return@withContext false
            }
            true
        }
    }

    private suspend fun saveAshaMasterData(dataObj: String) {

        val activities =
            Gson().fromJson(dataObj, Array<IncentiveActivityNetwork>::class.java).toList()

        val activityList = activities.map { it.asCacheModel() }
        activityList.forEach { activity ->
            val activityCache = incentiveDao.getActivityById(activity.id)
            if (activityCache == null) {
                incentiveDao.insert(activity)
            }
        }
    }

}
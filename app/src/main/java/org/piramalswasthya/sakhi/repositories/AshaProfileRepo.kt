package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

class AshaProfileRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val profileDao: ProfileDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo
) {


    suspend fun postDataToAmritServer(
        benNetworkPostSet: ProfileActivityCache,
        retryCount: Int = 3
    ): Boolean {
        try {
            val response = amritApiService.submitAshaProfileData(benNetworkPostSet)
            val statusCode = response.code()

            if (statusCode == 200) {

                val responseString: String? = response.body()?.string()
                if (responseString != null) {
                    val jsonObj = JSONObject(responseString)
                    val responseStatusCode = jsonObj.getInt("statusCode")
                    if (responseStatusCode == 200) {
                        Timber.d("response : $jsonObj")
                        try {
                            val dataObj = jsonObj.getString("data")
                            saveProfileData(dataObj)
                        } catch (e: Exception) {
                            Timber.d("profile data not synced $e")
                            return false
                        }
                        return true
                    } else if (responseStatusCode == 5002) {
                        val user = preferenceDao.getLoggedInUser()
                            ?: throw IllegalStateException("User not logged in according to db")
                        if (userRepo.refreshTokenTmc(
                                user.userName, user.password
                            )
                        ) throw SocketTimeoutException("Refreshed Token!")
                        else throw IllegalStateException("User seems to be logged out and refresh token not working!!!!")
                    }
                }
            }
            Timber.w("Bad Response from server, need to check $response ")
            return false
        } catch (e: SocketTimeoutException) {
            Timber.d("Caught exception $e here")
            if (retryCount > 0) return postDataToAmritServer(
                benNetworkPostSet, retryCount - 1
            )
            Timber.e("postDataToAmritServer: max retries exhausted")
            return false
        } catch (e: JSONException) {
            Timber.d("Caught exception $e here")
            return false
        } catch (e: java.lang.Exception) {
            Timber.d("Caught exception $e here")
            return false
        }
    }


    suspend fun pullAndSaveAshaProfile(user: User, retryCount: Int = 3): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = amritApiService.getAshaProfileData(user.userId)
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit asha profile data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveProfileData(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("profile data not synced $e")
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
                                return@withContext true
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("profile error : $e")
                if (retryCount > 0) return@withContext pullAndSaveAshaProfile(user, retryCount - 1)
                Timber.e("pullAndSaveAshaProfile: max retries exhausted")
                return@withContext false
            } catch (e: Exception) {
                Timber.d("Caught $e at incentives!")
                return@withContext false
            }
            true
        }
    }


    suspend fun getSavedRecord(id: Long): ProfileActivityCache? {
        return withContext(Dispatchers.IO) {
            profileDao.getProfileActivityById(id)
        }
    }
    private suspend fun saveProfileData(dataObj: String) {

        val activitiesCache =
            Gson().fromJson(dataObj, ProfileActivityCache::class.java) as ProfileActivityCache
        if (activitiesCache != null) {
            profileDao.insert(activitiesCache)
        }


    }


}
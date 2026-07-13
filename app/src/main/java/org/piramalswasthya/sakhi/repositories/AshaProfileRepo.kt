package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.ProfileActivityNetwork
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

class AshaProfileRepo @Inject constructor(
    private val amritApiService: AmritApiService,
    private val profileDao: ProfileDao,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
    @ApplicationContext private val context: Context
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
                        Timber.d("responseAsha : $jsonObj")
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
            Timber.e("Caught exception $e here")
            if (retryCount > 0) return postDataToAmritServer(
                benNetworkPostSet, retryCount - 1
            )
            Timber.e("postDataToAmritServer: max retries exhausted")
            return false
        } catch (e: JSONException) {
            Timber.e("Caught exception $e here")
            return false
        } catch (e: java.lang.Exception) {
            Timber.e("Caught exception $e here")
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
                        val responseStatusCode = jsonObj.optInt("statusCode", 200)
                        Timber.d("Pull from amrit asha profile data : $responseStatusCode")
                        when (responseStatusCode) {
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
                                if (jsonObj.has("data") && !jsonObj.isNull("data")) {
                                    try {
                                        saveProfileData(jsonObj.getString("data"))
                                    } catch (e: Exception) {
                                        Timber.e("profile data not synced $e")
                                        return@withContext false
                                    }
                                }
                                return@withContext true
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.e("profile error : $e")
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

    suspend fun saveRecord(profileActivityCache: ProfileActivityCache) {
        withContext(Dispatchers.IO) {
            profileDao.insert(profileActivityCache)
        }
    }
    private suspend fun saveProfileData(dataObj: String) {
        val net = try {
            Gson().fromJson(dataObj, ProfileActivityNetwork::class.java)
        } catch (e: Exception) {
            Timber.e("saveProfileData parse failed $e")
            null
        } ?: return

        val employeeId = (net.employeeId ?: 0)
        if (employeeId == 0) {
            Timber.w("saveProfileData skipped: employeeId missing")
            return
        }
        val existingRecord = profileDao.getProfileActivityById(employeeId.toLong())

        val serverImage = net.profileImage ?: ""
        val resolvedImage = when {
            serverImage.isNotBlank() && !serverImage.startsWith("file://") -> {
                val localUri = ImageUtils.saveBenImageFromServerToStorage(
                    context, serverImage, employeeId.toLong()
                )
                localUri?.also { preferenceDao.saveProfilePicUri(Uri.parse(it)) }
                    ?: existingRecord?.profileImage ?: ""
            }

            serverImage.startsWith("file://") -> serverImage

            else -> existingRecord?.profileImage ?: ""
        }

        val record = ProfileActivityCache(
            id = employeeId.toLong(),
            name = net.name,
            profileImage = resolvedImage,
            village = net.village ?: "",
            employeeId = employeeId,
            dob = net.dob ?: "",
            age = net.age ?: 0,
            mobileNumber = net.mobileNumber ?: "",
            alternateMobileNumber = net.alternateMobileNumber ?: "",
            fatherOrSpouseName = net.fatherOrSpouseName ?: "",
            dateOfJoining = net.dateOfJoining ?: "",
            bankAccount = net.bankAccount ?: "",
            ifsc = net.ifsc ?: "",
            populationCovered = net.populationCovered ?: 0,
            choName = net.choName ?: "",
            choMobile = net.choMobile ?: "",
            awwName = net.awwName ?: "",
            awwMobile = net.awwMobile ?: "",
            anm1Name = net.anm1Name ?: "",
            anm1Mobile = net.anm1Mobile ?: "",
            anm2Name = net.anm2Name ?: "",
            anm2Mobile = net.anm2Mobile ?: "",
            abhaNumber = net.abhaNumber ?: "",
            ashaHouseholdRegistration = net.ashaHouseholdRegistration ?: "",
            ashaFamilyMember = net.ashaFamilyMember ?: "",
            providerServiceMapID = net.providerServiceMapID ?: " ",
            isFatherOrSpouse = net.isFatherOrSpouse ?: false,
            supervisorName = net.supervisorName ?: "",
            supervisorMobile = net.supervisorMobile ?: ""
        )
        profileDao.insert(record)
    }


}
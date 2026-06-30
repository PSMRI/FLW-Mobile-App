package org.piramalswasthya.sakhi.repositories

import android.widget.Toast
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.crypt.CryptoUtil
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.model.PeerAtFacility
import org.piramalswasthya.sakhi.model.SyncStatusCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.TmcAuthUserRequest
import org.piramalswasthya.sakhi.network.TmcRefreshTokenRequest
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject


class UserRepo @Inject constructor(
    benDao: BenDao,
    private val db: InAppDb,
    private val vaccineDao: ImmunizationDao,
    private val preferenceDao: PreferenceDao,
    private val syncDao: SyncDao,
    private val amritApiService: AmritApiService
) {

    val unProcessedRecordCount: Flow<List<SyncStatusCache>> = syncDao.getSyncStatus()



    suspend fun authenticateUser(userName: String, password: String): NetworkResponse<User?> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getTokenAmrit(userName, password)
                val user = setUserRole(userId, password)
                return@withContext NetworkResponse.Success(user)
            } catch (se: SocketTimeoutException) {
                return@withContext NetworkResponse.Error(message = "Server timed out !")
            } catch (se: HttpException) {
                return@withContext when (se.code()) {
                    401 -> NetworkResponse.Error(message = "Unauthorized: Invalid credentials")
                    else -> NetworkResponse.Error(message = "Unable to connect to server!")
                }
                // return@withContext NetworkResponse.Error(message = "Unable to connect to server !")
            } catch (ce: ConnectException) {
                return@withContext NetworkResponse.Error(message = "Server refused connection !")
            } catch (ue: UnknownHostException) {
                return@withContext NetworkResponse.Error(message = "Unable to connect to server !")
            } catch (ie: Exception) {
                if (ie.message == "Invalid username / password")
                    return@withContext NetworkResponse.Error(message = "Invalid Username/password")
                else
                    return@withContext NetworkResponse.Error(message = ie.message ?: "Something went wrong... Try again later")

            }
        }
    }

    suspend fun saveToken(userName: String, password: String): NetworkResponse<User?> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getTokenAmrit(userName, password)
                val user = setUserRole(userId, password)
                return@withContext NetworkResponse.Success(user)
            } catch (se: SocketTimeoutException) {
                return@withContext NetworkResponse.Error(message = "Server timed out !")
            } catch (se: HttpException) {
                return@withContext NetworkResponse.Error(message = "Unable to connect to server !")
            } catch (ce: ConnectException) {
                return@withContext NetworkResponse.Error(message = "Server refused connection !")
            } catch (ue: UnknownHostException) {
                return@withContext NetworkResponse.Error(message = "Unable to connect to server !")
            } catch (ie: Exception) {
                if (ie.message == "Invalid username / password")
                    return@withContext NetworkResponse.Error(message = "Invalid Username/password")
                else
                    return@withContext NetworkResponse.Error(message = ie.message ?: "Something went wrong... Try again later")

            }
        }
    }

     suspend fun setFacilityData(userId: Int) {
         try {
             val response = amritApiService.getUserDetailsById(userId = userId)
             val userData = response.data
             val facilityData = userData.facilityData
             facilityData?.location?.let { location ->

                 preferenceDao.saveLocationType(location.locationType ?: "")
                 preferenceDao.saveBlock(location.blockOrUlb ?: "")
                 preferenceDao.saveState(location.state ?: "")
                 preferenceDao.saveDistrict(location.district ?: "")

                 preferenceDao.saveSupervisorDistrict(location.district ?: "")
                 preferenceDao.saveSupervisorBlock(location.blockOrUlb ?: "")
                 preferenceDao.saveSupervisorState(location.state ?: "")
             }

             // ---------- FACILITY ----------
             facilityData?.facility?.let { facility ->

                 preferenceDao.saveSupervisorSubcenter(facility.facilityName ?: "")
                 preferenceDao.saveFacilityId(facility.facilityId ?: 0)
                 preferenceDao.saveSupervisorFacilityType(facility.facilityType ?: "")
             }

             // ---------- SUPERVISOR ----------
             facilityData?.supervisor?.let { supervisor ->

                 preferenceDao.saveSupervisorName(supervisor.fullName ?: "")
                 preferenceDao.saveSupervisorId(supervisor.userId ?: -1)
                 preferenceDao.saveSupervisorContact(supervisor.mobile ?: "")
             }

             val choList = mutableListOf<PeerAtFacility>()
             val anmList = mutableListOf<PeerAtFacility>()

             facilityData?.peersAtFacility?.forEach { peer ->

                 when (peer.role?.trim()?.uppercase()) {

                     "CHO" -> {
                         choList.add(peer)
                     }

                     "ANM" -> {
                         anmList.add(peer)
                     }
                 }
             }

             val moshi = Moshi.Builder().build()

             val choAdapter = moshi.adapter<List<PeerAtFacility>>(
                 Types.newParameterizedType(
                     List::class.java,
                     PeerAtFacility::class.java
                 )
             )

             val anmAdapter = moshi.adapter<List<PeerAtFacility>>(
                 Types.newParameterizedType(
                     List::class.java,
                     PeerAtFacility::class.java
                 )
             )

             preferenceDao.saveChoList(choAdapter.toJson(choList))
             preferenceDao.saveAnmList(anmAdapter.toJson(anmList))
         }
         catch (e: HttpException) {
             Timber.w("setFacilityData: HTTP ${e.code()}, skipping")
         } catch (e: Exception) {
             Timber.w("setFacilityData: failed, skipping")
         }
    }


    private suspend fun setUserRole(userId: Int, password: String): User {
        val response = amritApiService.getUserDetailsById(userId = userId)
        val user = response.data.toUser(password)
        preferenceDao.registerUser(user)
        preferenceDao.saveStateId(response.data.stateId)
        val userData = response.data
        val facilityData = userData.facilityData
        facilityData?.location?.let { location ->

            preferenceDao.saveLocationType(location.locationType ?: "")
            preferenceDao.saveBlock(location.blockOrUlb ?: "")
            preferenceDao.saveState(location.state ?: "")
            preferenceDao.saveDistrict(location.district ?: "")

            preferenceDao.saveSupervisorDistrict(location.district ?: "")
            preferenceDao.saveSupervisorBlock(location.blockOrUlb ?: "")
            preferenceDao.saveSupervisorState(location.state ?: "")
        }

        // ---------- FACILITY ----------
        facilityData?.facility?.let { facility ->

            preferenceDao.saveSupervisorSubcenter(facility.facilityName ?: "")
            preferenceDao.saveFacilityId(facility.facilityId ?: 0)
            preferenceDao.saveSupervisorFacilityType(facility.facilityType ?: "")
        }

        // ---------- SUPERVISOR ----------
        facilityData?.supervisor?.let { supervisor ->

            preferenceDao.saveSupervisorName(supervisor.fullName ?: "")
            preferenceDao.saveSupervisorId(supervisor.userId ?: -1)
            preferenceDao.saveSupervisorContact(supervisor.mobile ?: "")
        }
        return user
    }


    private fun offlineLogin(userName: String, password: String): Boolean {
        val loggedInUser = preferenceDao.getLoggedInUser()
        loggedInUser?.let {
            if (it.userName == userName && it.password == password) {
                val amritToken = preferenceDao.getAmritToken()
                TokenInsertTmcInterceptor.setToken(
                    amritToken
                        ?: throw IllegalStateException("User logging offline without pref saved token B!")
                )
                Timber.w("User Logged in!")

                return true
            } else if (it.userName == userName) {
                throw IllegalStateException("Invalid Username/password")
                Timber.w("Invalid Username/password")
                return false
            }
        }
        return false
    }

    private fun encrypt(password: String): String {
        val util = CryptoUtil()
        return util.encrypt(password)
    }

    suspend fun refreshTokenTmc(userName: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = preferenceDao.getRefreshToken()
                    ?: return@withContext false
                val response =     amritApiService.getRefreshToken(
                    json = TmcRefreshTokenRequest(refreshToken)
                )

                if (!response.isSuccessful) {
                    Timber.e("refreshTokenTmc: refresh failed HTTP ${response.code()}")
                    response.errorBody()?.close()
                    return@withContext false
                }

                val responseBody = JSONObject(
                    response.body()?.string()
                        ?: throw IllegalStateException("Response success but data missing @ $response")
                )
                val jwtToken = responseBody.optString("jwtToken", "")
                val newRefreshToken = responseBody.optString("refreshToken", "")

                if (jwtToken.isBlank()) {
                    Timber.e("refreshTokenTmc: refresh returned blank JWT")
                    return@withContext false
                }

                TokenInsertTmcInterceptor.setJwt(jwtToken)
                preferenceDao.registerJWTAmritToken(jwtToken)
                if (newRefreshToken.isNotBlank()) {
                    preferenceDao.registerRefreshToken(newRefreshToken)
                }
                return@withContext true

            } catch (se: SocketTimeoutException) {
                return@withContext refreshTokenTmc(userName, password)
            } catch (e: HttpException) {
                Timber.e("Auth Failed!")
                return@withContext false
            } catch (e: Exception) {
                Timber.e(e, "refreshTokenTmc: unexpected error during token refresh")
                return@withContext false
            }
        }
    }

    private suspend fun getTokenAmrit(userName: String, password: String): Int {
        return withContext(Dispatchers.IO) {
            val encryptedPassword = encrypt(password)
            val response =
                amritApiService.getJwtToken(
                    json = TmcAuthUserRequest(
                        userName,
                        encryptedPassword
                    )
                )
            Timber.d("JWT : $response")
            val responseBody = JSONObject(
                response.body()?.string()
                    ?: throw IllegalStateException("Response success but data missing @ $response")
            )
            val statusCode = responseBody.getInt("statusCode")
            val message = responseBody.getString("errorMessage")
            if (statusCode == 5002)
                throw IllegalStateException(message/*"Login failed"*/)
            if (statusCode == 401)
                throw IllegalStateException("Invalid username / password")
            val data = responseBody.getJSONObject("data")
            val token = data.getString("key")
            val userId = data.getInt("userID")
            val refreshToken = data.getString("refreshToken")
          //  db.clearAllTables()
            TokenInsertTmcInterceptor.setJwt(data.getString("jwtToken"))
            preferenceDao.registerJWTAmritToken(data.getString("jwtToken"))
            preferenceDao.registerRefreshToken(refreshToken)
            TokenInsertTmcInterceptor.setToken(token)
            preferenceDao.registerAmritToken(token)
            preferenceDao.lastAmritTokenFetchTimestamp = System.currentTimeMillis()
            val designationId = data.optInt("designationID", -1)
            preferenceDao.saveDesignationId(designationId)
            if (data.has("facilityData")) {

                val facilityData = data.getJSONObject("facilityData")

                if (facilityData.has("location")) {
                    val location = facilityData.getJSONObject("location")
                    val locationType = location.optString("locationType", "")
                    val district = location.optString("district", "")
                    val block = location.optString("blockOrUlb", "")
                    val state = location.optString("state", "")
                    preferenceDao.saveLocationType(locationType)
                    preferenceDao.saveBlock(block)
                    preferenceDao.saveState(state)
                    preferenceDao.saveDistrict(district)


                }

                if (facilityData.has("user")) {

                    val userObj = facilityData.getJSONObject("user")
                    val employeeId = userObj.optString("employeeId", "")
                    preferenceDao.saveEmployeeId(employeeId)
                    if (userObj.has("demographics")) {

                        val demographics = userObj.getJSONObject("demographics")
                        val gender = demographics.optString("gender", "")
                        val dob = demographics.optString("dob", "")
                        val mobile = demographics.optString("mobile", "")
                        val email = demographics.optString("email", "")
                        preferenceDao.saveUserGender(gender)
                        preferenceDao.saveUserDob(dob)
                        preferenceDao.saveUserMobile(mobile)
                        preferenceDao.saveUserEmail(email)

                    }
                }
                val supervisorName = data.optString("fullName", "")
                val supervisorId = data.optInt("userID", -1)
                preferenceDao.saveSupervisorName(supervisorName)
                preferenceDao.saveSupervisorId(supervisorId)
            }
            if (data.has("facilityData")) {

                val facilityData = data.getJSONObject("facilityData")

                if (facilityData.has("location")) {
                    val location = facilityData.getJSONObject("location")

                    val district = location.optString("district", "")
                    val block = location.optString("blockOrUlb", "")
                    val state = location.optString("state", "")

                    preferenceDao.saveSupervisorDistrict(district)
                    preferenceDao.saveSupervisorBlock(block)
                    preferenceDao.saveSupervisorState(state)
                }

                if (facilityData.has("facility")) {
                    val facilityObj = facilityData.getJSONObject("facility")


                        val subcenterName = facilityObj.optString("facilityName", "")
                        val facilityType = facilityObj.optString("facilityType", "")
                        val facilityId = facilityObj.optInt("facilityId", 0)

                        preferenceDao.saveSupervisorSubcenter(subcenterName)
                        preferenceDao.saveFacilityId(facilityId)
                        preferenceDao.saveSupervisorFacilityType(facilityType)

                }

                if (facilityData.has("facilities")) {
                    val facilitiesArray = facilityData.getJSONArray("facilities")

                    if (facilitiesArray.length() > 0) {
                        val facilityObj = facilitiesArray.getJSONObject(0)

                        val subcenterName = facilityObj.optString("facilityName", "")
                        val facilityType = facilityObj.optString("facilityType", "")
                        val facilityId = facilityObj.optInt("facilityId", 0)

                        preferenceDao.saveSupervisorSubcenter(subcenterName)
                        preferenceDao.saveFacilityId(facilityId)
                        preferenceDao.saveSupervisorFacilityType(facilityType)
                    }
                }

                if (facilityData.has("supervisor")) {
                    val facilityObj = facilityData.getJSONObject("supervisor")


                    val supervisorName = facilityObj.optString("fullName", "")
                    val supervisorEmpID = facilityObj.optString("employeeId", "")
                    val supervisorContact = facilityObj.optString("mobile", "")

                    preferenceDao.saveSupervisorName(supervisorName)
                    preferenceDao.saveSupervisorEmpID(supervisorEmpID)
                    preferenceDao.saveSupervisorContact(supervisorContact)

                }
            }
            return@withContext userId
        }
    }

    suspend fun saveFirebaseToken(userId: Int, token: String, updatedAt: String) {
        withContext(Dispatchers.IO) {
            try {
                val requestBody = mapOf(
                    "userId" to userId,
                    "token" to token,
                    "updatedAt" to updatedAt
                )

                val response = amritApiService.saveFirebaseToken(requestBody)

                if (response.isSuccessful) {
                    Timber.d("Firebase token saved successfully: ${response.body()?.string()}")
                } else {
                    Timber.e("Failed to save Firebase token: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception while saving Firebase token")
            }
        }
    }

}
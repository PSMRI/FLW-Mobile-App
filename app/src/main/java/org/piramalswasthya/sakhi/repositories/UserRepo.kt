package org.piramalswasthya.sakhi.repositories

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
import org.piramalswasthya.sakhi.model.SyncStatusCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.TmcAuthUserRequest
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
                return@withContext NetworkResponse.Error(message = "Unable to connect to server !")
            } catch (ce: ConnectException) {
                return@withContext NetworkResponse.Error(message = "Server refused connection !")
            } catch (ue: UnknownHostException) {
                return@withContext NetworkResponse.Error(message = "Unable to connect to server !")
            } catch (ie: Exception) {
                if (ie.message == "Invalid username / password")
                    return@withContext NetworkResponse.Error(message = "Invalid Username/password")
                else
                    return@withContext NetworkResponse.Error(message = "Something went wrong... Try again later")

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
                    return@withContext NetworkResponse.Error(message = "Something went wrong... Try again later")

            }
        }
    }

    private suspend fun setUserRole(userId: Int, password: String): User {
        val response = amritApiService.getUserDetailsById(userId = userId)
        val user = response.data.toUser(password)
        preferenceDao.registerUser(user)
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
                val response =
                    amritApiService.getJwtToken(
                        json = TmcAuthUserRequest(
                            userName,
                            encrypt(password)
                        )
                    )
                Timber.d("JWT : $response")
                if (!response.isSuccessful) {
                    return@withContext false
                }
                val responseBody = JSONObject(
                    response.body()?.string()
                        ?: throw IllegalStateException("Response success but data missing @ $response")
                )
                val responseStatusCode = responseBody.getInt("statusCode")
                if (responseStatusCode == 200) {
                    val data = responseBody.getJSONObject("data")
                    val token = data.getString("key")
                    TokenInsertTmcInterceptor.setToken(token)
                    preferenceDao.registerAmritToken(token)
                    return@withContext true
                } else {
                    val errorMessage = responseBody.getString("errorMessage")
                    Timber.d("Error Message $errorMessage")
                }
                return@withContext false
            } catch (se: SocketTimeoutException) {
                return@withContext refreshTokenTmc(userName, password)
            } catch (e: HttpException) {
                Timber.d("Auth Failed!")
                return@withContext false
            } catch (e: Exception) {
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
//                        password,
                        encryptedPassword
                    )
                )
            Timber.d("JWT : $response")
            val responseBody = JSONObject(
                response.body()?.string()
                    ?: throw IllegalStateException("Response success but data missing @ $response")
            )
            val statusCode = responseBody.getInt("statusCode")
            if (statusCode == 5002)
                throw IllegalStateException("Invalid username / password")
            val data = responseBody.getJSONObject("data")
            val token = data.getString("key")
            val userId = data.getInt("userID")
            db.clearAllTables()
            TokenInsertTmcInterceptor.setToken(token)
            preferenceDao.registerAmritToken(token)
            preferenceDao.lastAmritTokenFetchTimestamp = System.currentTimeMillis()
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
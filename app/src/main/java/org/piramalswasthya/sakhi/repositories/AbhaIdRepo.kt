package org.piramalswasthya.sakhi.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.network.*
import java.io.IOException
import javax.inject.Inject


class AbhaIdRepo @Inject constructor(
    private val abhaApiService: AbhaApiService
) {

    suspend fun getAccessToken(): NetworkResult<AbhaTokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.getToken()
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaTokenResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorJson = JSONObject(errorBody)
                    val errorMessage = errorJson.getString("message")
                    NetworkResult.Error(response.code(), errorMessage)
                }
            } catch (e: IOException) {
                NetworkResult.NetworkError
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-1, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun generateOtpForAadhaar(req: AbhaGenerateAadhaarOtpRequest): NetworkResult<AbhaGenerateAadhaarOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.generateAadhaarOtp(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaGenerateAadhaarOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorJson = JSONObject(errorBody)
                    val errorMessage = errorJson.getString("message")
                    NetworkResult.Error(response.code(), errorMessage)
                }
            } catch (e: IOException) {
                NetworkResult.NetworkError
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-1, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun verifyOtpForAadhaar(req: AbhaVerifyAadhaarOtpRequest): NetworkResult<AbhaVerifyAadhaarOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.verifyAadhaarOtp(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaVerifyAadhaarOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorJson = JSONObject(errorBody)
                    val errorMessage = errorJson.getString("message")
                    NetworkResult.Error(response.code(), errorMessage)
                }
            } catch (e: IOException) {
                NetworkResult.NetworkError
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-1, e.message ?: "Unknown Error")
            }
        }

    }

    suspend fun generateOtpForMobileNumber(req: AbhaGenerateMobileOtpRequest): NetworkResult<AbhaGenerateMobileOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.generateMobileOtp(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaGenerateMobileOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorJson = JSONObject(errorBody)
                    val errorMessage = errorJson.getString("message")
                    NetworkResult.Error(response.code(), errorMessage)
                }
            } catch (e: IOException) {
                NetworkResult.NetworkError
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-1, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun verifyOtpForMobileNumber(req: AbhaVerifyMobileOtpRequest): NetworkResult<AbhaVerifyMobileOtpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = abhaApiService.verifyMobileOtp(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result =
                        Gson().fromJson(responseBody, AbhaVerifyMobileOtpResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorJson = JSONObject(errorBody)
                    val errorMessage = errorJson.getString("message")
                    NetworkResult.Error(response.code(), errorMessage)
                }
            } catch (e: IOException) {
                NetworkResult.NetworkError
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-1, e.message ?: "Unknown Error")
            }
        }
    }

    suspend fun generateAbhaId(req: CreateAbhaIdRequest): NetworkResult<CreateAbhaIdResponse> {
        return withContext((Dispatchers.IO)) {
            try {
                val response = abhaApiService.createAbhaId(req)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val result = Gson().fromJson(responseBody, CreateAbhaIdResponse::class.java)
                    NetworkResult.Success(result)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorJson = JSONObject(errorBody)
                    val errorMessage = errorJson.getString("message")
                    NetworkResult.Error(response.code(), errorMessage)
                }
            } catch (e: IOException) {
                NetworkResult.NetworkError
            } catch (e: java.lang.Exception) {
                NetworkResult.Error(-1, e.message ?: "Unknown Error")
            }
        }
    }

}
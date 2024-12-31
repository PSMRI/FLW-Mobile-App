package org.piramalswasthya.sakhi.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AbhaApiService {

    // Staging (v1/v2/v3)
    @Headers("No-Auth: true")
    @POST
    suspend fun getToken(
        @Url url: String = "https://dev.abdm.gov.in/gateway/v0.5/sessions",
        @Body request: AbhaTokenRequest = AbhaTokenRequest()
    ): Response<ResponseBody>

    // Production (v1/v2/v3)
//    @Headers("No-Auth: true")
//    @POST
//    suspend fun getToken(
//        @Url url: String = "https://live.abdm.gov.in/gateway/v0.5/sessions",
//        @Body request: AbhaTokenRequest = AbhaTokenRequest()
//    ): Response<ResponseBody>

    // Generate OTP (v1)
    @POST("v1/registration/aadhaar/generateOtp")
    suspend fun generateAadhaarOtp(@Body aadhaar: AbhaGenerateAadhaarOtpRequest): Response<ResponseBody>

    // Generate OTP (v2)
    @POST("v2/registration/aadhaar/generateOtp")
    suspend fun generateAadhaarOtpV2(@Body aadhaar: AbhaGenerateAadhaarOtpRequest): Response<ResponseBody>

    // Generate OTP (v3)
    @POST("v3/enrollment/request/otp")
    suspend fun generateAadhaarOtpV3(@Body aadhaar: AbhaGenerateAadhaarOtpRequest, @Header("REQUEST-ID") requestId: String, @Header("TIMESTAMP") timestamp: String): Response<ResponseBody>

    @POST("v1/registration/aadhaar/resendAadhaarOtp")
    suspend fun resendAadhaarOtp(@Body aadhaar: AbhaResendAadhaarOtpRequest): Response<ResponseBody>

    // Verify OTP (v1/v2)
    @POST("v1/registration/aadhaar/verifyOTP")
    suspend fun verifyAadhaarOtp(@Body request: AbhaVerifyAadhaarOtpRequest): Response<ResponseBody>

    // Verify OTP (v3)
    @POST("v3/enrollment/enrol/byAadhaar")
    suspend fun verifyAadhaarOtp3(@Body request: AbhaVerifyAadhaarOtpRequest, @Header("REQUEST-ID") requestId: String, @Header("TIMESTAMP") timestamp: String): Response<ResponseBody>

    @GET("v3/profile/account/abha-card")
    suspend fun printAbhaCard(@Header("REQUEST-ID") requestId: String, @Header("TIMESTAMP") timestamp: String): Response<ResponseBody>

    @POST("v1/registration/aadhaar/generateMobileOTP")
    suspend fun generateMobileOtp(@Body mobile: AbhaGenerateMobileOtpRequest): Response<ResponseBody>

    @POST("v2/registration/aadhaar/checkAndGenerateMobileOTP")
    suspend fun checkAndGenerateMobileOtp(@Body request: AbhaGenerateMobileOtpRequest): Response<ResponseBody>

    @POST("v1/registration/aadhaar/verifyMobileOTP")
    suspend fun verifyMobileOtp(@Body request: AbhaVerifyMobileOtpRequest): Response<ResponseBody>

    // Verify Updated Mobile OTP (v3)
    @POST("v3/enrollment/auth/byAbdm")
    suspend fun verifyMobileOtp3(@Body request: AbhaVerifyMobileOtpRequest, @Header("REQUEST-ID") requestId: String, @Header("TIMESTAMP") timestamp: String): Response<ResponseBody>

    @POST("v1/registration/aadhaar/createHealthIdWithPreVerified")
    suspend fun createAbhaId(@Body request: CreateAbhaIdRequest): Response<ResponseBody>

    @POST("v1/hid/benefit/createHealthId/demo/auth")
    suspend fun createAbhaIdGov(@Body request: CreateAbhaIdGovRequest): Response<ResponseBody>

    @GET("v1/account/getCard")
    suspend fun getPdfCard(): Response<ResponseBody>

    @GET("v1/account/getPngCard")
    suspend fun getPngCard(): Response<ResponseBody>

    // Staging RES Encryption API (v1/v2)
//    @GET
//    suspend fun getAuthCert(
//        @Url url: String = "https://healthidsbx.abdm.gov.in/api/v2/auth/cert"
//    ): Response<ResponseBody>

    // Staging RES Encryption API (v3)
    @GET
    suspend fun getAuthCert(
        @Url url: String = "https://healthidsbx.abdm.gov.in/api/v1/auth/cert"
    ): Response<ResponseBody>

    // Production RES Encryption API (v3)
//    @GET
//    suspend fun getAuthCert(
//        @Url url: String = "https://healthid.abdm.gov.in/api/v1/auth/cert"
//    ): Response<ResponseBody>

    @GET("v2/ha/lgd/states")
    suspend fun getStateAndDistricts(): Response<ResponseBody>

    @POST("v1/registration/aadhaar/verifyBio")
    suspend fun verifyBio(@Body aadhaarVerifyBioRequest: AadhaarVerifyBioRequest): Response<ResponseBody>
}
package org.piramalswasthya.sakhi.network

import okhttp3.ResponseBody
import org.piramalswasthya.sakhi.model.BenCHOPost
import org.piramalswasthya.sakhi.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface AmritApiService {

    @Headers("No-Auth: true")
    @POST("commonapi-v1.0/user/userAuthenticate/")
    suspend fun getJwtToken(@Body json: TmcAuthUserRequest): Response<ResponseBody>

//    @Headers("No-Auth: true")
//    @POST
//    suspend fun getJwtToken(@Url url: String = "http://amritdemo.piramalswasthya.org:8080/commonapi-v1.0/user/userAuthenticate/",
//                            @Body json: TmcAuthUserRequest): Response<ResponseBody>

//    @Headers("No-Auth: true")
//    @POST
//    suspend fun getJwtToken(
//        @Body json: TmcAuthUserRequest, @Url url: String = "http://192.168.1.37:8085/user/userAuthenticate/"): Response<ResponseBody>
//
//    @POST
//    suspend fun getUserDetailsById(@Body userDetail: TmcUserDetailsRequest, @Url url: String = "http://192.168.1.37:8085/doortodoorapp/getUserDetails/") : Response<ResponseBody>

    @GET("flw-0.0.1/user/getUserDetail")
//    @GET("user/getUserRole")
    suspend fun getUserDetailsById(
        @Query("userId") userId: Int
    ): UserNetworkResponse

    @POST("tmapi-v1.0/registrar/registrarBeneficaryRegistrationNew")
    suspend fun getBenIdFromBeneficiarySending(@Body beneficiaryDataSending: BeneficiaryDataSending): Response<ResponseBody>

    @POST("hwc-facility-service/registrar/registrarBeneficaryRegistrationNew")
    suspend fun getBenIdFromBeneficiarySending(@Body benCHOPost: BenCHOPost): Response<ResponseBody>

    @POST("identity-0.0.1/rmnch/syncDataToAmrit")
    suspend fun submitRmnchDataAmrit(@Body sendingRMNCHData: SendingRMNCHData): Response<ResponseBody>

//    @POST("beneficiary/getBeneficiaryData")
    @POST("flw-0.0.1/beneficiary/getBeneficiaryData")
    suspend fun getBeneficiaries(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/cbac/getAll")
    suspend fun getCbacs( @Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/cbac/saveAll")
    suspend fun postCbacs(/*@Url url : String  ="http://192.168.1.94:8081/cbac/saveAll",*/@Body list : List<CbacPost>): Response<ResponseBody>

//    @POST("tb/screening/getAll")
    @POST("flw-0.0.1/tb/screening/getAll")
    suspend fun getTBScreeningData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/tb/suspected/getAll")
//    @POST("tb/suspected/getAll")
    suspend fun getTBSuspectedData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/tb/screening/saveAll")
//    @POST("tb/screening/saveAll")
    suspend fun saveTBScreeningData(@Body tbScreeningRequestDTO: TBScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/tb/suspected/saveAll")
//    @POST("tb/suspected/saveAll")
    suspend fun saveTBSuspectedData(@Body tbSuspectedRequestDTO: TBSuspectedRequestDTO): Response<ResponseBody>

    @POST("identity-0.0.1/id/getByBenId")
    suspend fun getBeneficiaryWithId(@Query("benId") benId: Long): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDWithUID/createHealthIDWithUID")
    suspend fun createHid(@Body createHealthIdRequest: CreateHealthIdRequest): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthID/getBenhealthID")
    suspend fun getBenHealthID(@Body getBenHealthIdRequest: GetBenHealthIdRequest): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthID/mapHealthIDToBeneficiary")
    suspend fun mapHealthIDToBeneficiary(@Body mapHIDtoBeneficiary: MapHIDtoBeneficiary): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDCard/generateOTP")
    suspend fun generateOtpHealthId(@Body generateOtpHid: GenerateOtpHid): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDCard/verifyOTPAndGenerateHealthCard")
    suspend fun verifyOtpAndGenerateHealthCard(@Body validateOtpHid: ValidateOtpHid): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/register/saveAll")
    suspend fun postEcrForm(@Body ecrPostList: List<EcrPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/tracking/saveAll")
    suspend fun postEctForm(@Body ecrPostList: List<EligibleCoupleTrackingCache>): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/register/getAll")
    suspend fun getEcrFormData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/tracking/getAll")
    suspend fun getEctFormData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/deliveryOutcome/saveAll")
    suspend fun postDeliveryOutcomeForm(@Body deliveryOutcomeList: List<DeliveryOutcomeCache>): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/deliveryOutcome/getAll")
    suspend fun getDeliverOutcomeData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/ancVisit/saveAll")
    suspend fun postAncForm(@Body ecrPostList: List<PregnantWomanAncCache>): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/ancVisit/getAll")
    suspend fun getAnvVisitsData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

}
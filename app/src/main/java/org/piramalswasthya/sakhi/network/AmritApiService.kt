package org.piramalswasthya.sakhi.network

import okhttp3.ResponseBody
import org.piramalswasthya.sakhi.model.*
import retrofit2.Response
import retrofit2.http.*

interface AmritApiService {

    @Headers("No-Auth: true")
    @POST("commonapi-v1.0/user/userAuthenticate")
    suspend fun getJwtToken(@Body json: TmcAuthUserRequest): Response<ResponseBody>

    @GET("flw-0.0.1/user/getUserDetail")
//    @GET("user/getUserRole")
    suspend fun getUserDetailsById(
        @Query("userId") userId: Int
    ): UserNetworkResponse

    @POST("tmapi-v1.0/registrar/registrarBeneficaryRegistrationNew")
    suspend fun getBenIdFromBeneficiarySending(@Body beneficiaryDataSending: BeneficiaryDataSending): Response<ResponseBody>

    @POST("hwc-facility-service/registrar/registrarBeneficaryRegistrationNew")
    suspend fun getBenIdFromBeneficiarySending(@Body benCHOPost: BenCHOPost): Response<ResponseBody>

    @POST("identity-v3.0.0/rmnch/syncDataToAmrit")
    suspend fun submitRmnchDataAmrit(@Body sendingRMNCHData: SendingRMNCHData): Response<ResponseBody>

    //    @POST("beneficiary/getBeneficiaryData")
    @POST("flw-0.0.1/beneficiary/getBeneficiaryData")
    suspend fun getBeneficiaries(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/cbac/getAll")
    suspend fun getCbacs(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/cbac/saveAll")
    suspend fun postCbacs(/*@Url url : String  ="http://192.168.1.94:8081/cbac/saveAll",*/@Body list: List<CbacPost>): Response<ResponseBody>

    //    @POST("tb/screening/getAll")
    @POST("flw-0.0.1/tb/screening/getAll")
    suspend fun getTBScreeningData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>


    @POST("flw-0.0.1/tb/suspected/getAll")
//    @POST("tb/suspected/getAll")
    suspend fun getTBSuspectedData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/tb/screening/saveAll")
//    @POST("tb/screening/saveAll")
    suspend fun saveTBScreeningData(@Body tbScreeningRequestDTO: TBScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/disease/KalaAzar/saveAll")
    suspend fun saveKalaAzarScreeningData(@Body kalaAzarScreenRequestDTO: KalaAzarScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/disease/Malaria/saveAll")
    suspend fun saveMalariaScreeningData(@Body malariaScreeningRequestDTO: MalariaScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/disease/Leprosy/saveAll")
    suspend fun saveLeprosyScreeningData(@Body leprosyScreeningRequestDTO: LeprosyScreeningRequestDTO): Response<ResponseBody>


    @POST("flw-0.0.1/disease/AesJe/saveAll")
    suspend fun saveAESScreeningData(@Body aesScreeningRequestDTO: AESScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/disease/Filaria/saveAll")
    suspend fun saveFilariaScreeningData(@Body filariaScreeningRequestDTO: FilariaScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/disease/getAll")
    suspend fun getMalariaScreeningData(@Body userDetail: GetDataPaginatedRequestForDisease): Response<ResponseBody>

    @GET("flw-0.0.1/irsRound/list")
    suspend fun getScreeningData(@Query("householdId") householdId:Int): Response<ResponseBody>
    @POST("flw-0.0.1/tb/suspected/saveAll")
    suspend fun saveTBSuspectedData(@Body tbSuspectedRequestDTO: TBSuspectedRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/api/follow-up/save")
    suspend fun saveMalariaConfirmedData(@Body malariaConfirmedRequestDTO: MalariaConfirmedRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/pregnant/assess/getAll")
//    @POST("highRisk/pregnant/assess/getAll")
    suspend fun getHRPAssessData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/pregnant/assess/saveAll")
//    @POST("highRisk/pregnant/assess/saveAll")
    suspend fun saveHRPAssessData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/assess/getAll")
//    @POST("highRisk/pregnant/assess/getAll")
    suspend fun getHighRiskAssessData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/assess/saveAll")
//    @POST("highRisk/pregnant/assess/saveAll")
    suspend fun saveHighRiskAssessData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>


    @POST("flw-0.0.1/highRisk/pregnant/track/getAll")
//    @POST("highRisk/pregnant/track/getAll")
    suspend fun getHRPTrackData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/pregnant/track/saveAll")
//    @POST("highRisk/pregnant/track/saveAll")
    suspend fun saveHRPTrackData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/nonPregnant/assess/getAll")
//    @POST("highRisk/nonPregnant/assess/getAll")
    suspend fun getHRNonPAssessData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/nonPregnant/assess/saveAll")
//    @POST("highRisk/nonPregnant/assess/saveAll")
    suspend fun saveHRNonPAssessData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>


    @POST("flw-0.0.1/highRisk/nonPregnant/track/getAll")
//    @POST("highRisk/nonPregnant/track/getAll")
    suspend fun getHRNonPTrackData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-0.0.1/highRisk/nonPregnant/track/saveAll")
//    @POST("highRisk/nonPregnant/track/saveAll")
    suspend fun saveHRNonPTrackData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("identity-0.0.1/id/getByBenId")
    suspend fun getBeneficiaryWithId(@Query("benId") benId: Long): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDWithUID/createHealthIDWithUID")
    suspend fun createHid(@Body createHealthIdRequest: CreateHealthIdRequest): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthID/getBenhealthID")
    suspend fun getBenHealthID(@Body getBenHealthIdRequest: GetBenHealthIdRequest): Response<ResponseBody>

    //@POST("fhirapi-v1.0/healthID/mapHealthIDToBeneficiary")
    @POST("fhirapi-v1.0/healthIDRecord/mapHealthIDToBeneficiary")
    suspend fun mapHealthIDToBeneficiary(@Body mapHIDtoBeneficiary: MapHIDtoBeneficiary): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDRecord/addHealthIdRecord")
    suspend fun addHealthIdRecord(@Body addHealthIdRecord: AddHealthIdRecord): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDCard/generateOTP")
    suspend fun generateOtpHealthId(@Body generateOtpHid: GenerateOtpHid): Response<ResponseBody>

    @POST("fhirapi-v1.0/healthIDCard/verifyOTPAndGenerateHealthCard")
    suspend fun verifyOtpAndGenerateHealthCard(@Body validateOtpHid: ValidateOtpHid): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/register/saveAll")
    suspend fun postEcrForm(@Body ecrPostList: List<EcrPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/tracking/saveAll")
    suspend fun postEctForm(@Body ectPostList: List<ECTNetwork>): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/register/getAll")
    suspend fun getEcrFormData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/couple/tracking/getAll")
    suspend fun getEctFormData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/deliveryOutcome/saveAll")
    suspend fun postDeliveryOutcomeForm(
        @Body deliveryOutcomeList: List<DeliveryOutcomePost>,
        /*@Url url : String  ="http://192.168.1.105:8081/maternalCare/deliveryOutcome/saveAll"*/
    ): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/deliveryOutcome/getAll")
    suspend fun getDeliveryOutcomeData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/ancVisit/saveAll")
    suspend fun postAncForm(@Body ancPostList: List<ANCPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/ancVisit/getAll")
    suspend fun getAncVisitsData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/pregnantWoman/saveAll")
    suspend fun postPwrForm(@Body pwrPostList: List<PwrPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/pregnantWoman/getAll")
    suspend fun getPwrData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/pmsma/saveAll")
    suspend fun postPmsmaForm(@Body pmsmaPostList: List<PmsmaPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/pmsma/getAll")
    suspend fun getPmsmaData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/infant/saveAll")
    suspend fun postInfantRegForm(@Body deliveryOutcomeList: List<InfantRegPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/infant/getAll")
    suspend fun getInfantRegData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/child-care/vaccination/saveAll")
    suspend fun postChildImmunizationDetails(@Body immunizationList: List<ImmunizationPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/child-care/vaccination/getAll")
    suspend fun getChildImmunizationDetails(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/pnc/saveAll")
    suspend fun postPncForm(@Body ancPostList: List<PNCNetwork>): Response<ResponseBody>

    @POST("/flw-0.0.1/maternalCare/pnc/getAll")
    suspend fun getPncVisitsData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @GET("/flw-0.0.1/child-care/vaccine/getAll")
    suspend fun getAllChildVaccines(@Query("category") category: String): Response<ResponseBody>

    @POST("/flw-0.0.1/death-reports/mdsr/saveAll")
    suspend fun postMdsrForm(@Body mdsrPostList: List<MdsrPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/death-reports/mdsr/getAll")
    suspend fun getMdsrData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/death-reports/cdr/saveAll")
    suspend fun postCdrForm(@Body cdrPostList: List<CDRPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/death-reports/cdr/getAll")
    suspend fun getCdrData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/incentive/masterData/getAll")
    suspend fun getAllIncentiveActivities(@Body requestBody: IncentiveActivityListRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/incentive/fetchUserData")
    suspend fun getAllIncentiveRecords(@Body requestBody: IncentiveRecordListRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/child-care/hbncVisit/getAll")
    suspend fun getHBNCDetailsFromServer(@Body getDataPaginatedRequest: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/child-care/hbncVisit/saveAll")
    suspend fun pushHBNCDetailsToServer(@Body hbncPostList: List<HBNCPost>): Response<ResponseBody>

    @POST("/flw-0.0.1/child-care/hbyc/getAll")
    suspend fun getHBYCFromServer(@Body getDataPaginatedRequest: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("/flw-0.0.1/child-care/hbyc/saveAll")
    suspend fun pushHBYCToServer(@Body hbncPostList: List<HbycPost>): Response<ResponseBody>

}
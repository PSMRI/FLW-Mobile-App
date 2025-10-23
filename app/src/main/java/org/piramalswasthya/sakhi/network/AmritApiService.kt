package org.piramalswasthya.sakhi.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.piramalswasthya.sakhi.model.*
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSubmitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.ApiResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import retrofit2.Response
import retrofit2.http.*

interface AmritApiService {
    @Multipart
    @POST("flw-api/maa-meetings/saveAll")
    suspend fun postMaaMeetingMultipart(
        @Part("meetingDate") meetingDate: RequestBody,
        @Part("place") place: RequestBody,
        @Part("participants") participants: RequestBody,
        @Part("ashaId") ashaId: RequestBody,
        @Part("createdBy") createdBy: RequestBody,
        @Part meetingImages: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @POST("flw-api/maa-meetings/getAll")
    suspend fun getMaaMeetings(@Body userDetail: GetDataRequest): Response<ResponseBody>

    @Headers("No-Auth: true", "User-Agent: okhttp")
    @POST("common-api/user/userAuthenticate")
    suspend fun getJwtToken(@Body json: TmcAuthUserRequest): Response<ResponseBody>

    @GET("flw-api/user/getUserDetail")
//    @GET("user/getUserRole")
    suspend fun getUserDetailsById(
        @Query("userId") userId: Int
    ): UserNetworkResponse

    @POST("common-api/firebaseNotification/userToken")
    suspend fun saveFirebaseToken(@Body json: Map<String, Any>): Response<ResponseBody>

    @POST("tm-api/registrar/registrarBeneficaryRegistrationNew")
    suspend fun getBenIdFromBeneficiarySending(@Body beneficiaryDataSending: BeneficiaryDataSending): Response<ResponseBody>

    @POST("hwc-facility-service/registrar/registrarBeneficaryRegistrationNew")
    suspend fun getBenIdFromBeneficiarySending(@Body benCHOPost: BenCHOPost): Response<ResponseBody>

    @POST("identity-api/rmnch/syncDataToAmrit")
    suspend fun submitRmnchDataAmrit(@Body sendingRMNCHData: SendingRMNCHData): Response<ResponseBody>

    @POST("flw-api/asha/editProfile")
    suspend fun submitAshaProfileData(@Body sendAshaPost: ProfileActivityCache): Response<ResponseBody>

    //    @POST("beneficiary/getBeneficiaryData")
    @POST("flw-api/beneficiary/getBeneficiaryData")
    suspend fun getBeneficiaries(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/generalOpd/getBeneficiaries")
    suspend fun getgeneralOPDBeneficiaries(@Body userDetail: GetDataPaginatedRequestForGeneralOPD): Response<ResponseBody>

    @POST("common-api/beneficiaryConsent/sendConsent")
    suspend fun sendOtp(@Body sendOtpRequest: sendOtpRequest): Response<ResponseBody>

    @POST("common-api/beneficiaryConsent/resendConsent")
    suspend fun resendOtp(@Body sendOtpRequest: sendOtpRequest): Response<ResponseBody>

    @POST("common-api/beneficiaryConsent/validateConsent")
    suspend fun validateOtp(@Body validateOtp: ValidateOtpRequest ): Response<ResponseBody>

    @POST("flw-api/cbac/getAll")
    suspend fun getCbacs(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/cbac/saveAll")
    suspend fun postCbacs(/*@Url url : String  ="http://192.168.1.94:8081/cbac/saveAll",*/@Body list: List<CbacPost>): Response<ResponseBody>

    //    @POST("tb/screening/getAll")
    @POST("flw-api/tb/screening/getAll")
    suspend fun getTBScreeningData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/tb/suspected/getAll")
//    @POST("tb/suspected/getAll")
    suspend fun getTBSuspectedData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/tb/screening/saveAll")
//    @POST("tb/screening/saveAll")
    suspend fun saveTBScreeningData(@Body tbScreeningRequestDTO: TBScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-api/disease/kalaAzar/saveAll")
    suspend fun saveKalaAzarScreeningData(@Body kalaAzarScreenRequestDTO: KalaAzarScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-api/disease/malaria/saveAll")
    suspend fun saveMalariaScreeningData(@Body malariaScreeningRequestDTO: MalariaScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-api/disease/leprosy/saveAll")
    suspend fun saveLeprosyScreeningData(@Body leprosyScreeningRequestDTO: LeprosyScreeningRequestDTO): Response<ResponseBody>


    @POST("flw-api/disease/aesJe/saveAll")
    suspend fun saveAESScreeningData(@Body aesScreeningRequestDTO: AESScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-api/disease/filaria/saveAll")
    suspend fun saveFilariaScreeningData(@Body filariaScreeningRequestDTO: FilariaScreeningRequestDTO): Response<ResponseBody>

    @POST("flw-api/disease/getAllDisease")
    suspend fun getMalariaScreeningData(@Body userDetail: GetDataPaginatedRequestForDisease): Response<ResponseBody>

    @GET("flw-api/irsRound/list")
    suspend fun getScreeningData(@Query("householdId") householdId:Int): Response<ResponseBody>

    @POST("flw-api/adolescentHealth/getAll")
    suspend fun getAdolescentHealthData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/adolescentHealth/saveAll")
    suspend fun saveAdolescentHealthData(@Body adolescentHealthRequestDTO: AdolescentHealthRequestDTO): Response<ResponseBody>

    @POST("flw-api/tb/suspected/saveAll")
    suspend fun saveTBSuspectedData(@Body tbSuspectedRequestDTO: TBSuspectedRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/api/follow-up/save")
    suspend fun saveMalariaConfirmedData(@Body malariaConfirmedRequestDTO: MalariaConfirmedRequestDTO): Response<ResponseBody>

    @POST("flw-0.0.1/api/follow-up/get")
    suspend fun getMalariaConfirmedData(@Body malariaConfirmedRequestDTO: GetDataPaginatedRequestForDisease): Response<ResponseBody>

    @POST("flw-api/highRisk/pregnant/assess/getAll")
//    @POST("highRisk/pregnant/assess/getAll")
    suspend fun getHRPAssessData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/forms/villageLevel/getAll")
    suspend fun getVLFData(@Body userDetail: GetVHNDRequest): Response<ResponseBody>

    @POST("flw-api/forms/villageLevel/vhnd/saveAll")
    suspend fun saveVHNDData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-api/forms/villageLevel/vhnc/saveAll")
    suspend fun saveVHNCData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-api/forms/villageLevel/phc/saveAll")
    suspend fun savePHCData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-api/forms/villageLevel/ahd/saveAll")
    suspend fun saveAHDData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-api/forms/villageLevel/deworming/saveAll")
    suspend fun saveDewormingData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-api/highRisk/pregnant/assess/saveAll")
//    @POST("highRisk/pregnant/assess/saveAll")
    suspend fun saveHRPAssessData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-api/highRisk/assess/getAll")
//    @POST("highRisk/pregnant/assess/getAll")
    suspend fun getHighRiskAssessData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @GET("flw-api/micro-birthPlan/getAll")
    suspend fun getMicroBirthPlanAssessData(@Query("userId") userId: Int): Response<ResponseBody>

    @POST("flw-api/micro-birthPlan/saveAll")
    suspend fun saveMicroBirthPlanAssessData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>


    @POST("flw-api/highRisk/assess/saveAll")
//    @POST("highRisk/pregnant/assess/saveAll")
    suspend fun saveHighRiskAssessData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>


    @POST("flw-api/highRisk/pregnant/track/getAll")
//    @POST("highRisk/pregnant/track/getAll")
    suspend fun getHRPTrackData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/highRisk/pregnant/track/saveAll")
//    @POST("highRisk/pregnant/track/saveAll")
    suspend fun saveHRPTrackData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("flw-api/highRisk/nonPregnant/assess/getAll")
//    @POST("highRisk/nonPregnant/assess/getAll")
    suspend fun getHRNonPAssessData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/highRisk/nonPregnant/assess/saveAll")
//    @POST("highRisk/nonPregnant/assess/saveAll")
    suspend fun saveHRNonPAssessData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>


    @POST("flw-api/highRisk/nonPregnant/track/getAll")
//    @POST("highRisk/nonPregnant/track/getAll")
    suspend fun getHRNonPTrackData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/highRisk/nonPregnant/track/saveAll")
//    @POST("highRisk/nonPregnant/track/saveAll")
    suspend fun saveHRNonPTrackData(@Body userDataDTO: UserDataDTO<Any?>): Response<ResponseBody>

    @POST("identity-api/id/getByBenId")
    suspend fun getBeneficiaryWithId(@Query("benId") benId: Long): Response<ResponseBody>

    @POST("fhir-api/healthIDWithUID/createHealthIDWithUID")
    suspend fun createHid(@Body createHealthIdRequest: CreateHealthIdRequest): Response<ResponseBody>

    @POST("fhir-api/healthID/getBenhealthID")
    suspend fun getBenHealthID(@Body getBenHealthIdRequest: GetBenHealthIdRequest): Response<ResponseBody>

    //@POST("fhir-api/healthID/mapHealthIDToBeneficiary")
    @POST("fhir-api/healthIDRecord/mapHealthIDToBeneficiary")
    suspend fun mapHealthIDToBeneficiary(@Body mapHIDtoBeneficiary: MapHIDtoBeneficiary): Response<ResponseBody>

    @POST("fhir-api/healthIDRecord/addHealthIdRecord")
    suspend fun addHealthIdRecord(@Body addHealthIdRecord: AddHealthIdRecord): Response<ResponseBody>

    @POST("fhir-api/healthIDCard/generateOTP")
    suspend fun generateOtpHealthId(@Body generateOtpHid: GenerateOtpHid): Response<ResponseBody>

    @POST("fhir-api/healthIDCard/verifyOTPAndGenerateHealthCard")
    suspend fun verifyOtpAndGenerateHealthCard(@Body validateOtpHid: ValidateOtpHid): Response<ResponseBody>

    @POST("flw-api/couple/register/saveAll")
    suspend fun postEcrForm(@Body ecrPostList: List<EcrPost>): Response<ResponseBody>

    @POST("flw-api/couple/tracking/saveAll")
    suspend fun postEctForm(@Body ectPostList: List<ECTNetwork>): Response<ResponseBody>

    @POST("flw-api/couple/register/getAll")
    suspend fun getEcrFormData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/couple/tracking/getAll")
    suspend fun getEctFormData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/maternalCare/deliveryOutcome/saveAll")
    suspend fun postDeliveryOutcomeForm(
        @Body deliveryOutcomeList: List<DeliveryOutcomePost>,
        /*@Url url : String  ="http://192.168.1.105:8081/maternalCare/deliveryOutcome/saveAll"*/
    ): Response<ResponseBody>

    @POST("flw-api/maternalCare/deliveryOutcome/getAll")
    suspend fun getDeliveryOutcomeData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/maternalCare/ancVisit/saveAll")
    suspend fun postAncForm(@Body ancPostList: List<ANCPost>): Response<ResponseBody>

    @POST("flw-api/maternalCare/ancVisit/getAll")
    suspend fun getAncVisitsData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/maternalCare/pregnantWoman/saveAll")
    suspend fun postPwrForm(@Body pwrPostList: List<PwrPost>): Response<ResponseBody>

    @POST("flw-api/maternalCare/pregnantWoman/getAll")
    suspend fun getPwrData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/maternalCare/pmsma/saveAll")
    suspend fun postPmsmaForm(@Body pmsmaPostList: List<PmsmaPost>): Response<ResponseBody>

    @POST("flw-api/maternalCare/pmsma/getAll")
    suspend fun getPmsmaData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/maternalCare/infant/saveAll")
    suspend fun postInfantRegForm(@Body deliveryOutcomeList: List<InfantRegPost>): Response<ResponseBody>

    @POST("flw-api/maternalCare/infant/getAll")
    suspend fun getInfantRegData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/child-care/vaccination/saveAll")
    suspend fun postChildImmunizationDetails(@Body immunizationList: List<ImmunizationPost>): Response<ResponseBody>

    @POST("flw-api/child-care/vaccination/getAll")
    suspend fun getChildImmunizationDetails(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/maternalCare/pnc/saveAll")
    suspend fun postPncForm(@Body ancPostList: List<PNCNetwork>): Response<ResponseBody>

    @POST("flw-api/maternalCare/pnc/getAll")
    suspend fun getPncVisitsData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @GET("flw-api/child-care/vaccine/getAll")
    suspend fun getAllChildVaccines(@Query("category") category: String): Response<ResponseBody>

    @POST("flw-api/death-reports/mdsr/saveAll")
    suspend fun postMdsrForm(@Body mdsrPostList: List<MdsrPost>): Response<ResponseBody>

    @POST("flw-api/death-reports/mdsr/getAll")
    suspend fun getMdsrData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/death-reports/cdr/saveAll")
    suspend fun postCdrForm(@Body cdrPostList: List<CDRPost>): Response<ResponseBody>

    @POST("flw-api/death-reports/cdr/getAll")
    suspend fun getCdrData(@Body userDetail: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/incentive/masterData/getAll")
    suspend fun getAllIncentiveActivities(@Body requestBody: IncentiveActivityListRequest): Response<ResponseBody>

    @GET("/flw-api/asha/getProfile")
    suspend fun getAshaProfileData(@Query("employeeId") userId: Int): Response<ResponseBody>

    @POST("flw-api/incentive/fetchUserData")
    suspend fun getAllIncentiveRecords(@Body requestBody: IncentiveRecordListRequest): Response<ResponseBody>

    @POST("flw-api/child-care/hbncVisit/getAll")
    suspend fun getHBNCDetailsFromServer(@Body getDataPaginatedRequest: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/child-care/hbncVisit/saveAll")
    suspend fun pushHBNCDetailsToServer(@Body hbncPostList: List<HBNCPost>): Response<ResponseBody>

    @POST("flw-api/child-care/hbyc/getAll")
    suspend fun getHBYCFromServer(@Body getDataPaginatedRequest: GetDataPaginatedRequest): Response<ResponseBody>

    @POST("flw-api/child-care/hbyc/saveAll")
    suspend fun pushHBYCToServer(@Body hbncPostList: List<HbycPost>): Response<ResponseBody>

    @GET("common-api/dynamicForm/form/{formId}/fields")
    suspend fun fetchFormSchema(
        @Path("formId") formId: String
    ): Response<ApiResponse<FormSchemaDto>>

    @POST("flw-api/child-care/hbncVisit/saveAll")
    suspend fun submitForm(
        @Body request: List<FormSubmitRequest>
    ): Response<Unit>

    @POST("flw-api/child-care/{formName}/saveAll")
    suspend fun submitChildCareForm(
        @Path("formName") formName: String,
        @Body request: List<FormSubmitRequest>
    ): Response<Unit>


    @POST("flw-api/child-care/hbncVisit/getAll")
    suspend fun getAllHbncVisits(
        @Body request: HBNCVisitRequest
    ): Response<HBNCVisitListResponse>

    @POST("flw-api/child-care/{formName}/getAll")
    suspend fun getAllFormVisits(
        @Path("formName") formName: String,
        @Body request: HBNCVisitRequest
    ): Response<HBNCVisitListResponse>

    @Multipart
    @POST("flw-api/uwin/session/saveAll")
    suspend fun saveUwinSession(
        @Part("meetingDate") meetingDate: RequestBody,
        @Part("place") place: RequestBody,
        @Part("participants") participants: RequestBody,
        @Part("ashaId") ashaId: RequestBody,
        @Part("createdBy") createdBy: RequestBody,
        @Part images: List<MultipartBody.Part>? = null
    ): Response<ResponseBody>

    @POST("flw-api/uwin/session/getAll")
    suspend fun getAllUwinSessions(
        @Body request: UwinGetAllRequest
    ): Response<ResponseBody>
}
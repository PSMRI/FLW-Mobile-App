package org.piramalswasthya.sakhi.network

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.utils.KeyUtils
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.parcelize.Parcelize
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.VHNDCache

import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.model.ReferalCache

@JsonClass(generateAdapter = true)
data class D2DAuthUserRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class D2DAuthUserResponse(
    val jwt: String
)

@JsonClass(generateAdapter = true)
data class D2DSaveUserRequest(
    val id: Int,
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class D2DSaveUserResponse(
    val jwt: String
)


////////////////////---------TMC------------//////////////////////

@JsonClass(generateAdapter = true)
data class TmcAuthUserRequest(
    val userName: String,
    val password: String,
    val authKey: String = "",
    val doLogout: Boolean = true
)

@JsonClass(generateAdapter = true)
data class TmcUserDetailsRequest(
    val userID: Int
)

@JsonClass(generateAdapter = true)
data class TmcUserVanSpDetailsRequest(
    val userID: Int,
    val providerServiceMapID: Int
)


@JsonClass(generateAdapter = true)
data class TmcLocationDetailsRequest(
    val spID: Int,
    val spPSMID: Int
)

@JsonClass(generateAdapter = true)
data class TmcGenerateBenIdsRequest(
    val benIDRequired: Int,
    val vanID: Int
)

@JsonClass(generateAdapter = true)
data class GetDataPaginatedRequest(
    val ashaId: Int,
    val pageNo: Int,
    val fromDate: String,
    val toDate: String
)

@JsonClass(generateAdapter = true)
data class GetCBACRequest(
    val createdBy: String,
)


@JsonClass(generateAdapter = true)
data class GetDataPaginatedRequestForGeneralOPD(
    val userId: Int,
    val villageID: Int,
    val userName: String,
    val ashaId: Int,
    val pageNo: Int,
    val fromDate: String,
    val toDate: String
)


@JsonClass(generateAdapter = true)
data class GetVHNDRequest(
    val formType: String,
    val userId: Int,

)

@JsonClass(generateAdapter = true)

data class GetDataPaginatedRequestForDisease(
    val ashaId: Int,
    val pageNo: Int,
    val fromDate: String,
    val toDate: String,
    val diseaseTypeID: Int
)

data class ValidateOtpRequest(
    val otp: Int,
    val mobNo: String,
)


data class sendOtpRequest(
    val mobNo: String,
)

@JsonClass(generateAdapter = true)
data class GetDataRequest(
    val villageID: Int,
    val fromDate: String,
    val toDate: String,
    val pageNo: Int,
    val userId: Long,
    val userName: String,
    val ashaId: Long
)

@JsonClass(generateAdapter = true)
data class BenResponse(
    val benId: String,
    val benRegId: Long,
    val abhaDetails: List<BenAbhaResponse>?,
    val toDate: String
)

@JsonClass(generateAdapter = true)
data class BenHealthDetails(
    val benHealthID: Int,
    val healthIdNumber: String,
    val beneficiaryRegID: Long,
    val healthId: String,
    val isNewAbha: Boolean
)

@JsonClass(generateAdapter = true)
data class BenAbhaResponse(
    val BeneficiaryRegID: Long,
    val HealthID: String,
    val HealthIDNumber: String,
    val AuthenticationMode: String?,
    val CreatedDate: String?
)
///////////////-------------Abha id-------------/////////////////

@JsonClass(generateAdapter = true)
data class AbhaTokenRequest(
    val clientId: String = KeyUtils.abhaClientID(),
    val clientSecret: String = KeyUtils.abhaClientSecret(),
    val grantType: String = "client_credentials"
)

@JsonClass(generateAdapter = true)
data class AbhaTokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val refreshExpiresIn: Int,
    val refreshToken: String,
    val tokenType: String
)

// ABHA v1/v2 request
//@JsonClass(generateAdapter = true)
//data class AbhaGenerateAadhaarOtpRequest(
//    var aadhaar: String
//)

// ABHA v3 request
@JsonClass(generateAdapter = true)
data class AbhaGenerateAadhaarOtpRequest(
//    var aadhaar: String
    val txnId: String,
    val scope: List<String>,
    val loginHint: String,
    var loginId: String,
    var otpSystem: String
)

@JsonClass(generateAdapter = true)
data class AadhaarVerifyBioRequest(
    var aadhaar: String,
    var bioType: String,
    var pid: String
)

@JsonClass(generateAdapter = true)
data class AbhaGenerateAadhaarOtpResponse(
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class AbhaGenerateAadhaarOtpResponseV2(
    val txnId: String,
    val mobileNumber: String,
    val message:String
)

@JsonClass(generateAdapter = true)
data class SendOtpResponse(
    val data: Data,
    val statusCode: Long,
    val errorMessage: String,
    val status: String,
)

data class Data(
    val response: String,
)

@JsonClass(generateAdapter = true)

data class ValidateOtpResponse(
    val data: ResponseOtp,
    val statusCode: Long,
    val errorMessage: String,
    val status: String,
)

data class ResponseOtp(
    val userName: String,
    val userId: String,
)

@JsonClass(generateAdapter = true)
data class AbhaResendAadhaarOtpRequest(
    val txnId: String
)

// ABHA v1/v2 request
//@JsonClass(generateAdapter = true)
//data class AbhaVerifyAadhaarOtpRequest(
//    val otp: String,
//    val txnId: String
//)

// ABHA v3 request
@JsonClass(generateAdapter = true)
data class AbhaVerifyAadhaarOtpRequest(
    val authData: AuthData,
    val consent: Consent
)

@JsonClass(generateAdapter = true)
data class SearchAbhaRequest(
    val scope: List<String>,
    var mobile: String
)

@JsonClass(generateAdapter = true)
data class SearchAbhaResponse(
    val txnId: String,
    val ABHA: List<Abha>
)

@JsonClass(generateAdapter = true)
data class Abha(
    val index: Int,
    val ABHANumber: String,
    val name: String,
    val gender: String
)

@JsonClass(generateAdapter = true)
data class LoginGenerateOtpRequest(
    val scope: List<String>,
    val loginHint: String,
    var loginId: String,
    val otpSystem: String,
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class LoginGenerateOtpResponse(
    val txnId: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class LoginVerifyOtpRequest(
    val scope: List<String>,
    val authData: AuthData3
)

@JsonClass(generateAdapter = true)
data class AuthData3(
    val authMethods: List<String>,
    val otp: Otp3
)

@JsonClass(generateAdapter = true)
data class Otp3(
    val txnId: String,
    var otpValue: String
)

@JsonClass(generateAdapter = true)
data class LoginVerifyOtpResponse(
    val txnId: String,
    val authResult: String,
    val message: String,
    val token: String,
    val expiresIn: Long,
    val refreshToken: String,
    val refreshExpiresIn: Long,
    val accounts: List<Accounts>
)

@JsonClass(generateAdapter = true)
data class Accounts(
    val ABHANumber: String,
    val preferredAbhaAddress: String,
    val name: String,
    val status: String,
    val profilePhoto: String,
    val mobileVerified: Boolean
)

@JsonClass(generateAdapter = true)
data class AuthData(
    val authMethods: List<String>,
    val otp: Otp
)

@JsonClass(generateAdapter = true)
data class Consent(
    val code: String,
    val version: String
)

@JsonClass(generateAdapter = true)
data class Otp(
    var timeStamp: String,
    val txnId: String,
    var otpValue: String,
    var mobile: String
)

// ABHA v1/v2 request
//@JsonClass(generateAdapter = true)
//data class AbhaVerifyAadhaarOtpResponse(
//    val txnId: String
//)

// ABHA v3 request
@Parcelize
@JsonClass(generateAdapter = true)
data class AbhaVerifyAadhaarOtpResponse(
    val message: String="",
    val txnId: String="",
    val tokens: Tokens = Tokens(),
    val ABHAProfile: ABHAProfile=ABHAProfile(),
    val isNew: Boolean=false
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Tokens(
    val token: String="",
    val expiresIn: Int=0,
    val refreshToken: String="",
    val refreshExpiresIn: Int=0
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ABHAProfile(
    val firstName: String="",
    val middleName: String="",
    val lastName: String="",
    val dob: String="",
    val gender: String="",
    val photo: String="",
    val mobile: String="",
    val email: String="",
    val phrAddress:List<String>?= listOf<String>(),
    val address: String="",
    val districtCode: String="",
    val stateCode: String="",
    val pinCode: String="",
    val abhaType: String="",
    val stateName: String="",
    val districtName: String="",
    val ABHANumber: String="",
    val abhaStatus: String=""
) : Parcelable

@JsonClass(generateAdapter = true)
data class AbhaGenerateMobileOtpRequest(
    val mobile: String,
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class AbhaGenerateMobileOtpResponse(
    val txnId: String
)

data class AbhaCheckAndGenerateMobileOtpResponse(
    val mobileLinked: Boolean,
    val txnId: String
)


// ABHA v1/v2 request
//@JsonClass(generateAdapter = true)
//data class AbhaVerifyMobileOtpRequest(
//    val otp: String,
//    val txnId: String
//)

// ABHA v3 request
@JsonClass(generateAdapter = true)
data class AbhaVerifyMobileOtpRequest(
    val scope: List<String>,
    val authData: AuthData2
)

@JsonClass(generateAdapter = true)
data class AuthData2(
    val authMethods: List<String>,
    val otp: Otp2
)

@JsonClass(generateAdapter = true)
data class Otp2(
    var timeStamp: String,
    val txnId: String,
    var otpValue: String
)


@JsonClass(generateAdapter = true)
data class AbhaVerifyMobileOtpResponse(
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class AbhaPublicCertificateResponse(
    val publicKey: String,
    val encryptionAlgorithm: String
)

@JsonClass(generateAdapter = true)
data class StateCodeResponse(
    val code: String,
    val name: String,
    val districts: List<DistrictCodeResponse>?
)

@JsonClass(generateAdapter = true)
data class DistrictCodeResponse(
    val code: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class CreateAbhaIdGovRequest(

    val aadharNumber: Long,
    val benefitName: String,
    val consentHealthId: Boolean,
    val dateOfBirth: String,
    val gender: String,
    val name: String,
    val stateCode: Int,
    val districtCode: Int
)

@JsonClass(generateAdapter = true)
data class CreateAbhaIdRequest(

    val email: String?,
    val firstName: String?,
    val healthId: String?,
    val lastName: String?,
    val middleName: String?,
    val password: String?,
    val profilePhoto: String?,
    val txnId: String
)

@JsonClass(generateAdapter = true)
data class CreateHIDResponse(
    val hID: Long,
    val healthIdNumber: String?,
    val name: String?,
    val gender: String?,
    val yearOfBirth: String?,
    val monthOfBirth: String?,
    val dayOfBirth: String?,
    val firstName: String?,
    val healthId: String?,
    val lastName: String?,
    val middleName: String?,
    val stateCode: String?,
    val districtCode: String?,
    val stateName: String?,
    val districtName: String?,
    val email: String?,
    val kycPhoto: String?,
    val mobile: String?,
    val authMethod: String?,
    val authMethods: Array<String>?,
    val deleted: Boolean,
    val processed: String?,
    val createdBy: String?,
    val txnId: String?,
)

@JsonClass(generateAdapter = true)
data class CreateAbhaIdResponse(

    val token: String,
    val refreshToken: String,
    val healthIdNumber: String,
    val name: String,
    val gender: String,
    val yearOfBirth: String,
    val monthOfBirth: String,
    val dayOfBirth: String,
    val firstName: String,
    val healthId: String?,
    val lastName: String,
    val middleName: String,
    val stateCode: String,
    val districtCode: String,
    val stateName: String,
    val districtName: String,
    val email: String?,
    val kycPhoto: String?,
    val profilePhoto: String,
    val mobile: String,
    val authMethods: Array<String>,
    val pincode: String?,
    val tags: Map<String, String>?,
    val alreadyExists: String,
    val new: Boolean,
    var txnId: String
)

@JsonClass(generateAdapter = true)
data class GenerateOtpHid(
    val authMethod: String?,
    val healthId: String?,
    val healthIdNumber: String?
)

@JsonClass(generateAdapter = true)
data class ValidateOtpHid(
    val otp: String?,
    val txnId: String?,
    val authMethod: String?
)

@JsonClass(generateAdapter = true)
data class GetBenHealthIdRequest(
    val beneficiaryRegID: Long?,
    val beneficiaryID: Long?,
)

@JsonClass(generateAdapter = true)
data class CreateHealthIdRequest(
    val otp: String?,
    val txnId: String?,
    val address: String?,
    val dayOfBirth: String?,
    val email: String?,
    val profilePhoto: String?,
    val password: String?,
    val healthId: String?,
    val healthIdNumber: String?,
    val firstName: String?,
    val gender: String?,
    val lastName: String?,
    val middleName: String?,
    val monthOfBirth: String?,
    val name: String?,
    val pincode: Int?,
    val yearOfBirth: String?,
    val providerServiceMapID: Int?,
    val createdBy: String?
)
@JsonClass(generateAdapter = true)
data class MapHIDtoBeneficiary(
    val beneficiaryRegID: Long?,
    val beneficiaryID: Long?,
    val healthId: String?,
    val healthIdNumber: String?,
    var providerServiceMapId: Int?,
    var createdBy: String?,
    var message: String?,
    var txnId: String?,
    var ABHAProfile: ABHAProfile?,
    var isNew: Boolean?
)

@JsonClass(generateAdapter = true)
data class AddHealthIdRecord(
    val healthId: String?,
    val healthIdNumber: String?,
    var providerServiceMapId: Int?,
    var createdBy: String?,
    var message: String?,
    var txnId: String?,
    var ABHAProfile: ABHAProfile?,
    var isNew: Boolean?
)

data class TBScreeningRequestDTO(
    val userId: Int,
    val tbScreeningList: List<TBScreeningDTO>
)

data class KalaAzarScreeningRequestDTO(
    val userId: Int,
    val kalaAzarLists: List<KALAZARScreeningDTO>
)

data class MalariaScreeningRequestDTO(
    val userId: Int,
    val malariaLists: List<MalariaScreeningDTO>
)

data class IRSScreeningRequestDTO(
    val rounds: List<ScreeningRoundDTO>
)

data class LeprosyScreeningRequestDTO(
    val userId: Int,
    val leprosyLists: List<LeprosyScreeningDTO>
)

data class AESScreeningRequestDTO(
    val userId: Int,
    val aesJeLists: List<AESScreeningDTO>
)

data class FilariaScreeningRequestDTO(
    val userId: Int,
    val filariaLists: List<FilariaScreeningDTO>
)

data class AdolescentHealthRequestDTO(
    val userId: Int,
    val adolescentHealths: List<AdolscentHealthDTO>
)

data class UserDataDTO<T>(
    val userId: Int,
    val entries: List<T>
)

data class HRPPregnantTrackDTO(
    var id: Int = 0,
    val benId: Long,
    var visitDate: String?,
    var rdPmsa: String? = null,
    var rdDengue: String? = null,
    var rdFilaria: String? = null,
    var severeAnemia: String? = null,
    var hemoglobinTest: String? = null,
    var ifaGiven: String? = null,
    var ifaQuantity: Int? = null,
    var pregInducedHypertension: String? = null,
    var systolic: Int? = null,
    var diastolic: Int? = null,
    var gestDiabetesMellitus: String? = null,
    var bloodGlucoseTest: String? = null,
    var fbg: Int? = null,
    var rbg: Int? = null,
    var ppbg: Int? = null,
    var fastingOgtt: Int? = null,
    var after2hrsOgtt: Int? = null,
    var hypothyrodism: String? = null,
    var polyhydromnios: String? = null,
    var oligohydromnios: String? = null,
    var antepartumHem: String? = null,
    var malPresentation: String? = null,
    var hivsyph: String? = null,
    var visit: String?
) {
    fun toCache(): HRPPregnantTrackCache {
        return HRPPregnantTrackCache(
            benId = benId,
            visitDate = getLongFromDate(visitDate),
            rdPmsa = rdPmsa,
            rdDengue = rdDengue,
            rdFilaria = rdFilaria,
            severeAnemia = severeAnemia,
            hemoglobinTest = hemoglobinTest,
            ifaGiven = ifaGiven,
            ifaQuantity = ifaQuantity,
            pregInducedHypertension = pregInducedHypertension,
            systolic = systolic,
            diastolic = diastolic,
            gestDiabetesMellitus = gestDiabetesMellitus,
            bloodGlucoseTest = bloodGlucoseTest,
            fbg = fbg,
            rbg = rbg,
            ppbg = ppbg,
            fastingOgtt = fastingOgtt,
            after2hrsOgtt = after2hrsOgtt,
            hypothyrodism = hypothyrodism,
            polyhydromnios = polyhydromnios,
            oligohydromnios = oligohydromnios,
            antepartumHem = antepartumHem,
            malPresentation = malPresentation,
            hivsyph = hivsyph,
            visit = visit,
            syncState = SyncState.SYNCED
        )
    }
}

data class HRPPregnantAssessDTO(
    var id: Int = 0,
    val benId: Long,
    var noOfDeliveries: String? = null,
    var timeLessThan18m: String? = null,
    var heightShort: String? = null,
    var age: String? = null,
    var rhNegative: String? = null,
    var homeDelivery: String? = null,
    var badObstetric: String? = null,
    var multiplePregnancy: String? = null,
    var lmpDate: String?,
    var edd: String?,
    var isHighRisk: Boolean = false,
    var visitDate: String?
) {
    fun toCache(): HRPPregnantAssessCache {
        return HRPPregnantAssessCache(
            benId = benId,
            noOfDeliveries = noOfDeliveries,
            timeLessThan18m = timeLessThan18m,
            heightShort = heightShort,
            age = age,
            rhNegative = rhNegative,
            homeDelivery = homeDelivery,
            badObstetric = badObstetric,
            multiplePregnancy = multiplePregnancy,
            lmpDate = getLongFromDate(lmpDate),
            edd = getLongFromDate(edd),
            isHighRisk = isHighRisk,
            visitDate = getLongFromDate(visitDate),
            syncState = SyncState.SYNCED
        )
    }
}

@JsonClass(generateAdapter = true)
data class HRPMicroBirthPlanDTO(
    val id: Int = 0,
    val benId: Long,
    var nearestSc: String? = null,
    var bloodGroup: String? = null,
    var contactNumber1: String? = null,
    var contactNumber2: String? = null,
    var scHosp: String? = null,
    var usg: String? = null,
    var block: String? = null,
    var nearestPhc: String? = null,
    var nearestFru: String? = null,
    var bloodDonors1: String? = null,
    var bloodDonors2: String? = null,
    var birthCompanion: String? = null,
    var careTaker: String? = null,
    var communityMember: String? = null,
    var communityMemberContact: String? = null,
    var modeOfTransportation: String? = null,
) {
    fun toCache(): HRPMicroBirthPlanCache {
        return HRPMicroBirthPlanCache(
            id = 0,
            benId = benId,
            nearestSc = nearestSc,
            bloodGroup = bloodGroup,
            contactNumber1 = contactNumber1,
            contactNumber2 = contactNumber2,
            scHosp = scHosp,
            usg = usg,
            block = block,
            nearestPhc = nearestPhc,
            nearestFru = nearestFru,
            bloodDonors1 = bloodDonors1,
            bloodDonors2 = bloodDonors2,
            birthCompanion = birthCompanion,
            careTaker = careTaker,
            communityMember = communityMember,
            communityMemberContact = communityMemberContact,
            modeOfTransportation = modeOfTransportation,
            processed = "P",
            syncState = SyncState.SYNCED
        )
    }
}




data class HRPNonPregnantTrackDTO(
    var id: Int = 0,
    val benId: Long,
    var visitDate: String?,
    var anemia: String? = null,
    var hypertension: String? = null,
    var systolic: Int? = null,
    var diastolic: Int? = null,
    var diabetes: String? = null,
    var bloodGlucoseTest: String? = null,
    var fbg: Int? = null,
    var rbg: Int? = null,
    var ppbg: Int? = null,
    var severeAnemia: String? = null,
    val hemoglobinTest: String? = null,
    val ifaGiven: String? = null,
    val ifaQuantity: Int? = null,
    var fp: String? = null,
    var lmp: String?,
    var missedPeriod: String? = null,
    var isPregnant: String? = null,
) {
    fun toCache(): HRPNonPregnantTrackCache {
        return HRPNonPregnantTrackCache(
            benId = benId,
            visitDate = getLongFromDate(visitDate),
            anemia = anemia,
            hypertension = hypertension,
            systolic = systolic,
            diastolic = diastolic,
            diabetes = diabetes,
            bloodGlucoseTest = bloodGlucoseTest,
            rbg = rbg,
            fbg = fbg,
            ppbg = ppbg,
            severeAnemia = severeAnemia,
            hemoglobinTest = hemoglobinTest,
            ifaGiven = ifaGiven,
            ifaQuantity = ifaQuantity,
            fp = fp,
            lmp = getLongFromDate(lmp),
            missedPeriod = missedPeriod,
            isPregnant = isPregnant,
            syncState = SyncState.SYNCED
        )
    }
}

data class HRPNonPregnantAssessDTO(
    val id: Int = 0,
    val benId: Long,
    var noOfDeliveries: String? = null,
    var timeLessThan18m: String? = null,
    var heightShort: String? = null,
    var age: String? = null,
    var misCarriage: String? = null,
    var homeDelivery: String? = null,
    var medicalIssues: String? = null,
    var pastCSection: String? = null,
    var isHighRisk: Boolean = false,
    var visitDate: String?
) {
    fun toCache(): HRPNonPregnantAssessCache {
        return HRPNonPregnantAssessCache(
            benId = benId,
            noOfDeliveries = noOfDeliveries,
            timeLessThan18m = timeLessThan18m,
            heightShort = heightShort,
            age = age,
            misCarriage = misCarriage,
            homeDelivery = homeDelivery,
            medicalIssues = medicalIssues,
            pastCSection = pastCSection,
            isHighRisk = isHighRisk,
            visitDate = getLongFromDate(visitDate),
            syncState = SyncState.SYNCED
        )
    }
}

data class VHNDDTO(
    val id: Int = 0,
    var vhndDate: String?,
    var place: String? = null,
    var noOfBeneficiariesAttended: Int? = null,
    var Image1: String? = null,
    var Image2: String? = null,
) {
    fun toCache(): VHNDCache {
        return VHNDCache(
            id = 0,
            vhndDate = vhndDate!!,
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            image1 = Image1,
            image2 = Image2,
            syncState = SyncState.SYNCED
        )
    }
}

data class VHNCDTO(
    val id: Int = 0,
    var vhncDate: String?,
    var place: String? = null,
    var noOfBeneficiariesAttended: Int? = null,
    var Image1: String? = null,
    var Image2: String? = null,
) {
    fun toCache(): VHNCCache {
        return VHNCCache(
            id = 0,
            vhncDate = vhncDate!!,
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            image1 = Image1,
            image2 = Image2,
            syncState = SyncState.SYNCED
        )
    }
}

data class PHCReviewDTO(
    val id: Int = 0,
    var phcReviewDate: String?,
    var place: String? = null,
    var noOfBeneficiariesAttended: Int? = null,
    var Image1: String? = null,
    var Image2: String? = null
) {
    fun toCache(): PHCReviewMeetingCache {
        return PHCReviewMeetingCache(
            id = 0,
            phcReviewDate = phcReviewDate!!,
            place = place,
            noOfBeneficiariesAttended = noOfBeneficiariesAttended,
            image1 = Image1,
            image2 = Image2,
            syncState = SyncState.SYNCED
        )
    }
}

data class AHDDTO(
    val id: Int = 0,
    var mobilizedForAHD: String?,
    var ahdPlace: String? = null,
    var ahdDate: String? = null,
    var image1: String? = null,
    var image2: String? = null,
) {
    fun toCache(): AHDCache {
        return AHDCache(
            id = 0,
            mobilizedForAHD = mobilizedForAHD!!,
            ahdPlace = ahdPlace,
            ahdDate = ahdDate,
            image1 = image1,
            image2 = image2,
            syncState = SyncState.SYNCED
        )
    }
}

data class DewormingDTO(
    var id: Int = 0,
    var dewormingDone: String? = null,
    var dewormingDate: String? = null,
    var dewormingLocation: String? = null,
    var ageGroup: Int? = null,
    var image1: String? = null,
    var image2: String? = null,
    var regDate: String? = null,
) {
    fun toCache(): DewormingCache {
        return DewormingCache(
            id = id,
            dewormingDone = dewormingDone,
            dewormingDate = dewormingDate,
            dewormingLocation = dewormingLocation,
            ageGroup = ageGroup,
            image1 = image1,
            image2 = image2,
            regDate = regDate,
            syncState = SyncState.SYNCED
        )
    }
}



data class TBScreeningDTO(
    val id: Long,
    val benId: Long,
    val visitDate: String?,
    var coughMoreThan2Weeks: Boolean?,
    var bloodInSputum: Boolean?,
    var feverMoreThan2Weeks: Boolean?,
    var lossOfWeight: Boolean?,
    var nightSweats: Boolean?,
    var historyOfTb: Boolean?,
    var takingAntiTBDrugs: Boolean?,
    var familySufferingFromTB: Boolean?
) {
    fun toCache(): TBScreeningCache {
        return TBScreeningCache(
            benId = benId,
            visitDate = getLongFromDate(visitDate),
            coughMoreThan2Weeks = coughMoreThan2Weeks,
            bloodInSputum = bloodInSputum,
            feverMoreThan2Weeks = feverMoreThan2Weeks,
            lossOfWeight = lossOfWeight,
            nightSweats = nightSweats,
            historyOfTb = historyOfTb,
            takingAntiTBDrugs = takingAntiTBDrugs,
            familySufferingFromTB = familySufferingFromTB,
            syncState = SyncState.SYNCED
        )
    }
}

data class AdolscentHealthDTO(
    var id :Int? = null,
    var userID :Int? =null,
    var benId:Long,
    var visitDate: String,
    var healthStatus: String? = null,
    var ifaTabletDistributed: Boolean? = null,
    var quantityOfIfaTablets: Int? = null,
    var menstrualHygieneAwarenessGiven: Boolean? = null,
    var sanitaryNapkinDistributed: Boolean? = null,
    var noOfPacketsDistributed: Int? = null,
    var place: String? = null,
    var distributionDate: String? = null,
    var referredToHealthFacility: String? = null,
    var counselingProvided: Boolean? = null,
    var counselingType: String? = null,
    var followUpDate: String? = null,
    var referralStatus: String? = null,
) {
    fun toCache(): AdolescentHealthCache {
        return AdolescentHealthCache(
            benId = benId,
            visitDate = getLongFromDate(visitDate),
            healthStatus = healthStatus,
            ifaTabletDistributed = ifaTabletDistributed,
            quantityOfIfaTablets = quantityOfIfaTablets,
            menstrualHygieneAwarenessGiven = menstrualHygieneAwarenessGiven,
            sanitaryNapkinDistributed = sanitaryNapkinDistributed,
            noOfPacketsDistributed = noOfPacketsDistributed,
            place = place,
            distributionDate = getLongFromDate(distributionDate),
            referredToHealthFacility = referredToHealthFacility,
            counselingProvided = counselingProvided,
            counselingType = counselingType,
            followUpDate = getLongFromDate(followUpDate),
            referralStatus = referralStatus,
            syncState = SyncState.SYNCED
        )
    }
}


data class ABHAGeneratedDTO(
    val id: Int = 0,
    val beneficiaryID: Long,
    val beneficiaryRegID: Long,
    val benName: String,
    val createdBy: String,
    val message: String,
    val txnId: String,
    val benSurname: String? = null,
    var healthId: String = "",
    var healthIdNumber: String = "",
    var isNewAbha: Boolean= false,
    val providerServiceMapId: Int,

) {
    fun toCache(): ABHAModel {
        return ABHAModel(
            beneficiaryID = beneficiaryID,
            beneficiaryRegID = beneficiaryRegID,
            benName = benName,
            benSurname = benSurname,
            healthId = healthId,
            txnId = txnId,
            message = message,
            createdBy = createdBy,
            healthIdNumber = healthIdNumber,
            isNewAbha = isNewAbha,
            providerServiceMapId = providerServiceMapId
        )
    }
}

data class TBSuspectedDTO(
    val id: Long,
    val benId: Long,
    val visitDate: String?,
    val isSputumCollected: Boolean?,
    val sputumSubmittedAt: String?,
    val nikshayId: String?,
    val sputumTestResult: String?,
    val referred: Boolean?,
    val followUps: String?
) {
    fun toCache(): TBSuspectedCache {
        return TBSuspectedCache(
            benId = benId,
            visitDate = getLongFromDate(visitDate),
            isSputumCollected = isSputumCollected,
            sputumSubmittedAt = sputumSubmittedAt,
            nikshayId = nikshayId,
            sputumTestResult = sputumTestResult,
            referred = referred,
            followUps = followUps,
            syncState = SyncState.SYNCED
        )
    }
}

data class TBSuspectedRequestDTO(
    val userId: Int,
    val tbSuspectedList: List<TBSuspectedDTO>
)

data class MalariaConfirmedRequestDTO(
    val userId: Int,
    val malariaFollowListUp: List<MalariaConfirmedDTO>
)


data class MalariaScreeningDTO(
    val id: Int = 0,
    val benId: Long,
    val caseDate: String,
    val houseHoldDetailsId: Long,
    val screeningDate: String,
    val beneficiaryStatus: String,
    val beneficiaryStatusId: Int = 0,
    val dateOfDeath: String,
    val placeOfDeath: String,
    val otherPlaceOfDeath: String,
    val reasonForDeath: String,
    val otherReasonForDeath: String,
    val rapidDiagnosticTest: String,
    val dateOfRdt: String,
    val slideTestName: String,
    val slideTestPf: String,
    val slideTestPv: String,
    val dateOfSlideTest: String,
    val dateOfVisitBySupervisor: String,
    var caseStatus: String ? = "",
    var referredTo: Int ? = 0,
    var referToName: String ? = null,
    var otherReferredFacility: String ? = null,
    var remarks: String ? = null,
    var diseaseTypeID: Int ? = 0,
    var followUpDate: String,
    var feverMoreThanTwoWeeks: Boolean ? = false,
    var fluLikeIllness: Boolean ? = false,
    var shakingChills: Boolean ? = false,
    var headache: Boolean ? = false,
    var muscleAches: Boolean ? = false,
    var tiredness: Boolean ? = false,
    var nausea: Boolean ? = false,
    var vomiting: Boolean ? = false,
    var diarrhea: Boolean ? = false,
    var createdBy: String ? = "",

) {
    fun toCache(): MalariaScreeningCache {
        return MalariaScreeningCache(
            benId = benId,
            caseDate = getLongFromDate(caseDate),
            caseStatus = caseStatus,
            houseHoldDetailsId = houseHoldDetailsId,
            referredTo = referredTo,
            referToName = referToName.toString(),
            otherReferredFacility = otherReferredFacility,
            remarks = remarks,
            followUpDate = getLongFromDate(followUpDate),
            syncState = SyncState.SYNCED,
            diseaseTypeID = diseaseTypeID,
            feverMoreThanTwoWeeks = feverMoreThanTwoWeeks,
            fluLikeIllness = fluLikeIllness,
            shakingChills = shakingChills,
            headache = headache,
            muscleAches = muscleAches,
            tiredness = tiredness,
            nausea = nausea,
            vomiting = vomiting,
            diarrhea = diarrhea,
            beneficiaryStatusId = beneficiaryStatusId,
            beneficiaryStatus = beneficiaryStatus,
            createdBy = createdBy,
            screeningDate = getLongFromDate(screeningDate),
            rapidDiagnosticTest = rapidDiagnosticTest,
            slideTestName = slideTestName,
            slideTestPf = slideTestPf,
            slideTestPv = slideTestPv,
            dateOfSlideTest = getLongFromDate(dateOfSlideTest),
            dateOfRdt = getLongFromDate(dateOfRdt),
            dateOfDeath = getLongFromDate(dateOfDeath),
            dateOfVisitBySupervisor = getLongFromDate(dateOfVisitBySupervisor),
            reasonForDeath = reasonForDeath,
            otherReasonForDeath = otherReasonForDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            placeOfDeath = placeOfDeath


        )
    }
}

data class MalariaConfirmedDTO(
    val id: Int = 0,
    val diseaseId: Int = 0,
    val benId: Long,
    val houseHoldDetailsId: Long,
    var dateOfDiagnosis: String,
    var treatmentStartDate: String,
    var treatmentCompletionDate: String,
    var treatmentGiven: String,
    var referralDate: String,
    var day: String,
) {
    fun toCache(): MalariaConfirmedCasesCache {
        return MalariaConfirmedCasesCache(
            benId = benId,
            dateOfDiagnosis = getLongFromDate(dateOfDiagnosis),
            treatmentStartDate = getLongFromDate(treatmentStartDate),
            treatmentCompletionDate = getLongFromDate(treatmentCompletionDate),
            referralDate = getLongFromDate(referralDate),
            treatmentGiven = treatmentGiven,
            houseHoldDetailsId = houseHoldDetailsId,
            diseaseId = diseaseId,
            day = day,
            )
    }
}

data class AESScreeningDTO(
    val id: Int = 0,
    val benId: Long,
    var visitDate: String,
    val houseHoldDetailsId: Long,
    var beneficiaryStatus: String ? = null,
    var beneficiaryStatusId: Int = 0,
    var dateOfDeath: String,
    var placeOfDeath: String ? = null,
    var otherPlaceOfDeath: String ? = null,
    var reasonForDeath: String ?  = null,
    var otherReasonForDeath: String ?  = null,
    var aesJeCaseStatus: String ? = "",
    var referredTo: Int ? = 0,
    var referToName: String ? = null,
    var otherReferredFacility: String ? = null,
    var diseaseTypeID: Int ? = 0,
    var createdDate: String,
    var createdBy: String ? = null,
    var followUpPoint: Int ? = 0,
    var syncState: SyncState = SyncState.UNSYNCED,

    ) {
    fun toCache(): AESScreeningCache {
        return AESScreeningCache(
            benId = benId,
            visitDate = getLongFromDate(visitDate),
            aesJeCaseStatus = aesJeCaseStatus,
            houseHoldDetailsId = houseHoldDetailsId,
            referredTo = referredTo,
            referToName = referToName.toString(),
            otherReferredFacility = otherReferredFacility,
            createdDate = getLongFromDate(createdDate),
            syncState = SyncState.SYNCED,
            diseaseTypeID = diseaseTypeID,
            beneficiaryStatusId = beneficiaryStatusId,
            beneficiaryStatus = beneficiaryStatus,
            createdBy = createdBy,
            dateOfDeath = getLongFromDate(dateOfDeath),
            reasonForDeath = reasonForDeath,
            otherReasonForDeath = otherReasonForDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            placeOfDeath = placeOfDeath,
            followUpPoint = followUpPoint


        )
    }
}


data class NCDReferalDTO(
    val id: Int = 0,
    val benId: Long,
    val referredToInstituteID: Int?,
    val refrredToAdditionalServiceList: List<String>?,
    val referredToInstituteName: String?,
    val referralReason: String?,
    val revisitDate: String,
    val vanID: Int?,
    val parkingPlaceID: Int?,
    val beneficiaryRegID: Long?,
    val benVisitID: Long?,
    val visitCode: Long?,
    val providerServiceMapID: Int?,
    val createdBy: String?,
    val isSpecialist: Boolean? = false,
    var syncState: SyncState = SyncState.UNSYNCED,

    ) {
    fun toCache(): ReferalCache {
        return ReferalCache(
            id = 0,
            benId = benId,
            revisitDate = getLongFromDate(revisitDate),
            referredToInstituteID = referredToInstituteID,
            refrredToAdditionalServiceList = refrredToAdditionalServiceList,
            referredToInstituteName = referredToInstituteName,
            visitCode = visitCode,
            benVisitID = benVisitID,
            createdBy = createdBy,
            isSpecialist = false,
            vanID = vanID,
            providerServiceMapID = providerServiceMapID,
            beneficiaryRegID =  beneficiaryRegID,
            referralReason = referralReason,
            parkingPlaceID = parkingPlaceID,
            syncState = SyncState.SYNCED




        )
    }
}


data class LeprosyScreeningDTO(
    val id: Int = 0,
    val benId: Long,
    val homeVisitDate: String,
    val leprosyStatusDate: String,
    val dateOfDeath: String,
    val houseHoldDetailsId: Long,
    var leprosyStatus: String ? = "",
    var referredTo: Int ? = 0,
    var referToName: String ? = null,
    var otherReferredTo: String ? = null,
    var typeOfLeprosy: String ? = null,
    var remarks: String ? = null,
    var beneficiaryStatus: String ? = null,
    var placeOfDeath: String ? = null,
    var otherPlaceOfDeath: String ? = null,
    var reasonForDeath: String ? = null,
    var otherReasonForDeath: String ? = null,
    var diseaseTypeID: Int ? = 0,
    var beneficiaryStatusId: Int ? = 0,
    var followUpDate: String,

    ) {
    fun toCache(): LeprosyScreeningCache {
        return LeprosyScreeningCache(
            benId = benId,
            homeVisitDate = getLongFromDate(homeVisitDate),
            leprosyStatusDate = getLongFromDate(leprosyStatusDate),
            dateOfDeath = getLongFromDate(dateOfDeath),
            leprosyStatus = leprosyStatus,
            houseHoldDetailsId = houseHoldDetailsId,
            referredTo = referredTo,
            referToName = referToName.toString(),
            otherReferredTo = otherReferredTo,
            remarks = remarks,
            followUpDate = getLongFromDate(followUpDate),
            syncState = SyncState.SYNCED,
            diseaseTypeID = diseaseTypeID,
            reasonForDeath = reasonForDeath,
            otherReasonForDeath = otherReasonForDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            placeOfDeath = placeOfDeath,
            beneficiaryStatusId = beneficiaryStatusId,
            beneficiaryStatus = beneficiaryStatus


        )
    }
}

data class FilariaScreeningDTO(
    val id: Int = 0,
    val benId: Long,
    val mdaHomeVisitDate: String,
    val houseHoldDetailsId: Long,
    var sufferingFromFilariasis: Boolean ? = false,
    var doseStatus: String ? = null,
    var affectedBodyPart: String ? = null,
    var otherDoseStatusDetails: String ? = null,
    var filariasisCaseCount: String ? = null,
    var medicineSideEffect: String ? = "",
    var otherSideEffectDetails: String ? = "",
    var createdBy: String ?  = "",
    var diseaseTypeID: Int ? = 0,
    var createdDate: String,
    var syncState: SyncState = SyncState.UNSYNCED,

    ) {
    fun toCache(): FilariaScreeningCache {
        return FilariaScreeningCache(
            benId = benId,
            syncState = SyncState.SYNCED,
            diseaseTypeID = diseaseTypeID,
            mdaHomeVisitDate = getLongFromDate(mdaHomeVisitDate),
            houseHoldDetailsId = houseHoldDetailsId,
            doseStatus = doseStatus.toString(),
            sufferingFromFilariasis = sufferingFromFilariasis!!,
            affectedBodyPart = affectedBodyPart.toString(),
            otherDoseStatusDetails = otherDoseStatusDetails.toString(),
            medicineSideEffect = medicineSideEffect.toString(),
            otherSideEffectDetails = otherSideEffectDetails.toString(),
            createdBy = createdBy.toString(),
            createdDate = getLongFromDate(createdDate),

        )
    }
}

data class ScreeningRoundDTO(
    val date: String,
    val rounds: Int,
    val householdId: Long
) {
    fun toCache(): IRSRoundScreening {
        return IRSRoundScreening(
            date = getLongFromDate(date),
            rounds = rounds,
            householdId = householdId,
        )
    }
}

data class KALAZARScreeningDTO(
    val id: Int = 0,
    val benId: Long,
    var visitDate: String,
    val houseHoldDetailsId: Long,
    var beneficiaryStatus: String,
    var beneficiaryStatusId: Int = 0,
    var dateOfDeath: String,
    var placeOfDeath: String,
    var otherPlaceOfDeath: String,
    var reasonForDeath: String,
    var otherReasonForDeath: String,
    var rapidDiagnosticTest: String,
    var dateOfRdt: String,
    var kalaAzarCaseStatus: String ? = "",
    var referredTo: Int ? = 0,
    var referToName: String,
    var otherReferredFacility: String,
    var diseaseTypeID: Int ? = 0,
    var createdDate: String,
    var createdBy: String ,
    var followUpPoint: Int ? = 0,
    var syncState: SyncState = SyncState.UNSYNCED,

    ) {
    fun toCache(): KalaAzarScreeningCache {
        return KalaAzarScreeningCache(
            benId = benId,
            visitDate = getLongFromDate(visitDate),
            kalaAzarCaseStatus = kalaAzarCaseStatus,
            houseHoldDetailsId = houseHoldDetailsId,
            referredTo = referredTo,
            referToName = referToName.toString(),
            otherReferredFacility = otherReferredFacility,
            createdDate = getLongFromDate(createdDate),
            syncState = SyncState.SYNCED,
            diseaseTypeID = diseaseTypeID,
            beneficiaryStatusId = beneficiaryStatusId,
            beneficiaryStatus = beneficiaryStatus,
            createdBy = createdBy,
            rapidDiagnosticTest = rapidDiagnosticTest,
            dateOfRdt = getLongFromDate(dateOfRdt),
            dateOfDeath = getLongFromDate(dateOfDeath),
            reasonForDeath = reasonForDeath,
            otherReasonForDeath = otherReasonForDeath,
            otherPlaceOfDeath = otherPlaceOfDeath,
            placeOfDeath = placeOfDeath,
            followUpPoint = followUpPoint


        )
    }
}

fun getLongFromDate(dateString: String?): Long {
    val f = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH)
    val date = dateString?.let { f.parse(it) }
    return date?.time ?: 0L
}

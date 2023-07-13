package org.piramalswasthya.sakhi.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BenCHOPost(
    @Json(name = "beneficiaryConsent")
    val beneficiaryConsent: Boolean = true,
    @Json(name = "firstName")
    val firstName: String,
    @Json(name = "lastName")
    val lastName: String?,
    @Json(name = "dOB")
    val dOB: String? = null,
    @Json(name = "fatherName")
    val fatherName: String? = null,
    @Json(name = "spouseName")
    val spouseName: String? = null,
    @Json(name = "motherName")
    val motherName: String? = null,
    @Json(name = "govtIdentityNo")
    val govtIdentityNo: String? = null,
    @Json(name = "govtIdentityTypeID")
    val govtIdentityTypeID: String? = null,
    @Json(name = "emergencyRegistration")
    val emergencyRegistration: Boolean,
    @Json(name = "titleId")
    val titleId: String? = null,
    @Json(name = "benImage")
    val benImage: String? = null,
    @Json(name = "bankName")
    val bankName: String? = null,
    @Json(name = "branchName")
    val branchName: String? = null,
    @Json(name = "ifscCode")
    val ifscCode: String? = null,
    @Json(name = "accountNo")
    val accountNo: String? = null,
    @Json(name = "maritalStatusID")
    val maritalStatusID: Int = 7,
    @Json(name = "maritalStatusName")
    val maritalStatusName: String = "Not Applicable",
    @Json(name = "ageAtMarriage")
    val ageAtMarriage: String? = null,
    @Json(name = "genderID")
    val genderID: Int,
    @Json(name = "genderName")
    val genderName: String,
    @Json(name = "literacyStatus")
    val literacyStatus: String?,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "email")
    val email: String? = null,
    @Json(name = "providerServiceMapId")
    val providerServiceMapId: String,
    @Json(name = "providerServiceMapID")
    val providerServiceMapID: String,
    @SerializedName("i_bendemographics")
    val benDemographics: BenDemographicsCHO,
    @Json(name = "benPhoneMaps")
    val benPhoneMaps: Array<BenPhoneMapCHO>,
    @Json(name = "beneficiaryIdentities")
    val beneficiaryIdentities: Array<BeneficiaryIdentitiesCHO>? = null,
    @Json(name = "vanID")
    val vanID: Int,
    @Json(name = "parkingPlaceID")
    val parkingPlaceID: Int,
    @Json(name = "createdBy")
    val createdBy: String
)

data class BenPhoneMapCHO(
    @Json(name = "parentBenRegID")
    val parentBenRegID: String? = null,
    @Json(name = "phoneNo")
    val phoneNo: String? = null,
    @Json(name = "alternateContactNumber")
    val alternateContactNumber: String? = null,
    @Json(name = "phoneTypeID")
    val phoneTypeID: Int? = null,
    @Json(name = "benRelationshipID")
    val benRelationshipID: String? = null,
    @Json(name = "vanID")
    val vanID: Int,
    @Json(name = "parkingPlaceID")
    val parkingPlaceID: Int,
    @Json(name = "createdBy")
    val createdBy: String
)

data class BeneficiaryIdentitiesCHO(
    @Json(name = "govtIdentityNo")
    var govtIdentityNo: Int = 0,

    @Json(name = "govtIdentityTypeID")
    var govtIdentityTypeID: Int = 0,

    @Json(name = "govtIdentityTypeName")
    var govtIdentityTypeName: String? = null,

    @Json(name = "identityType")
    var identityType: String,

    @Json(name = "createdBy")
    var createdBy: String,
)

data class BenDemographicsCHO(
    @Json(name = "incomeStatusID")
    val incomeStatusID: String? = null,
    @Json(name = "incomeStatusName")
    val incomeStatusName: String? = null,
    @Json(name = "occupationID")
    val occupationID: String? = null,
    @Json(name = "occupationName")
    val occupationName: String? = null,
    @Json(name = "educationID")
    val educationID: String? = null,
    @Json(name = "educationName")
    val educationName: String? = null,
    @Json(name = "communityID")
    val communityID: String? = null,
    @Json(name = "communityName")
    val communityName: String? = null,
    @Json(name = "religionID")
    val religionID: String? = null,
    @Json(name = "religionName")
    val religionName: String? = null,
    @Json(name = "countryID")
    val countryID: Int,
    @Json(name = "countryName")
    val countryName: String,
    @Json(name = "stateID")
    val stateID: Int,
    @Json(name = "stateName")
    val stateName: String,
    @Json(name = "districtID")
    val districtID: Int,
    @Json(name = "districtName")
    val districtName: String,
    @Json(name = "blockID")
    val blockID: Int,
    @Json(name = "blockName")
    val blockName: String,
    @Json(name = "districtBranchID")
    val districtBranchID: Int,
    @Json(name = "districtBranchName")
    val districtBranchName: String,
    @Json(name = "parkingPlaceID")
    val parkingPlaceID: Int,
    @Json(name = "servicePointID")
    val servicePointID: String,
    @Json(name = "servicePointName")
    val servicePointName: String,
    @Json(name = "habitation")
    val habitation: String? = null,
    @Json(name = "pinCode")
    val pinCode: String? = null,
    @Json(name = "addressLine1")
    val addressLine1: String? = null,
    @Json(name = "addressLine2")
    val addressLine2: String? = null,
    @Json(name = "addressLine3")
    val addressLine3: String? = null
)



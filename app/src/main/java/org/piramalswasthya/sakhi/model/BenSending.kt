package org.piramalswasthya.sakhi.model

import androidx.room.ColumnInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.piramalswasthya.sakhi.model.Gender.*

@JsonClass(generateAdapter = true)
data class BeneficiaryDataSending(
    @Json(name = "ageAtMarriage")
    val ageAtMarriage: String?,

    @Json(name = "benImage")
    val benImage: String,

    @Json(name = "benPhoneMaps")
    val benPhoneMaps: Array<BenPhoneMaps>,

    @Json(name = "beneficiaryIdentities")
    val beneficiaryIdentities: Array<BeneficiaryIdentities>,

    @Json(name = "createdBy")
    val createdBy: String,

    @Json(name = "dOB")
    private val dOB: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "emergencyRegistration")
    val isEmergencyRegistration: Boolean = false,

    @Json(name = "fatherName")
    val fatherName: String,

    @Json(name = "firstName")
    val firstName: String,


    @Json(name = "genderID")
    val genderID: Int = 0,

    @Json(name = "genderName")
    val genderName: String,

    @Json(name = "govtIdentityNo")
    val govtIdentityNo: String?,

    @Json(name = "govtIdentityTypeID")
    val govtIdentityTypeID: String?,

    @Json(name = "i_bendemographics")
    val benDemographics: BenDemographics,

    @Json(name = "lastName")
    val lastName: String,


    @ColumnInfo(name = "marriageDate")
    val marriageDate: String?,

    @Json(name = "spouseName")
    val spouseName: String?,

    @Json(name = "titleId")
    val titleId: String,


    @Json(name = "parkingPlaceID")
    val parkingPlaceID: Int = 0,

    @Json(name = "bankName")
    val bankName: String? = null,

    @Json(name = "providerServiceMapID")
    val providerServiceMapID: String,

    @Json(name = "maritalStatusID")
    val maritalStatusID: String = "",

    @Json(name = "vanID")
    val vanID: Int = 0,


    @Json(name = "accountNo")
    val accountNo: String? = null,


    @Json(name = "ifscCode")
    val ifscCode: String? = null,


    @Json(name = "motherName")
    val motherName: String,

    @Json(name = "branchName")
    val branchName: String? = null,

    @Json(name = "providerServiceMapId")
    val providerServiceMapId: String,


    @Json(name = "maritalStatusName")
    val maritalStatusName: String = "",

    )

data class BenDemographics(
    @Json(name = "addressLine1")
    var addressLine1: String,
    @Json(name = "addressLine2")
    var addressLine2: String,
    @Json(name = "addressLine3")
    var addressLine3: String,
    @Json(name = "blockID")
    var blockID: Int,

    @Json(name = "communityID")
    var communityID: String,
    @Json(name = "communityName")
    var communityName: String,

    @Json(name = "countryID")
    var countryID: Int,
    @Json(name = "countryName")
    var countryName: String,
    @Json(name = "districtBranchID")
    var districtBranchID: Int,
    @Json(name = "districtBranchName")
    var districtBranchName: String,


    @Json(name = "districtID")
    var districtID: Int,
    @Json(name = "parkingPlaceID")
    var parkingPlaceID: Int,
    @Json(name = "parkingPlaceName")
    var parkingPlaceName: String,

    @Json(name = "religionID")
    var religionID: String,

    @Json(name = "religionName")
    var religionName: String,

    @Json(name = "servicePointID")
    var servicePointID: String,
    @Json(name = "servicePointName")
    var servicePointName: String,

    @Json(name = "stateID")
    var stateID: Int,

    @Json(name = "stateName")
    var stateName: String,

    @Json(name = "zoneID")
    var zoneID: Int = 0,

    @Json(name = "zoneName")
    var zoneName: String,

//Nullable Fields, I think...
    @Json(name = "incomeStatusName")
    var incomeStatusName: String? = null,
    @Json(name = "blockName")
    var blockName: String? = null,
    @Json(name = "occupationName")
    var occupationName: String? = null,
    @Json(name = "incomeStatusID")
    var incomeStatusID: String? = null,
    @Json(name = "educationName")
    var educationName: String? = null,
    @Json(name = "districtName")
    var districtName: String? = null,
    @Json(name = "habitation")
    var habitation: String? = null,
    @Json(name = "educationID")
    var educationID: String? = null,
    @Json(name = "occupationID")
    var occupationID: String? = null,
    @Json(name = "pinCode")
    var pinCode: String? = null,


    )


data class BeneficiaryIdentities(
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


data class BenPhoneMaps(

    @Json(name = "createdBy")
    var createdBy: String,

    @Json(name = "phoneNo")
    var phoneNo: String

)

fun BenRegCache.asNetworkSendingModel(
    user: UserCache,
    locationRecord: LocationRecord
): BeneficiaryDataSending {

    return BeneficiaryDataSending(
        benImage = userImageBlob.toString(),
        firstName = firstName!!,
        lastName = lastName ?: "NA",
        dOB = getDateTimeStringFromLong(dob) ?: "",
        fatherName = fatherName!!,
        motherName = motherName!!,
        spouseName = genDetails?.spouseName,
        govtIdentityNo = null,
        govtIdentityTypeID = null,
        isEmergencyRegistration = false,
        titleId = "",
        //benImage = null,
        bankName = nameOfBank,
        branchName = nameOfBranch,
        ifscCode = ifscCode ?: "",
        accountNo = bankAccount,
        ageAtMarriage = genDetails?.ageAtMarriage?.toString() ?: "0",
        marriageDate = getDateTimeStringFromLong(genDetails?.marriageDate),
        genderID = genderId,
        genderName = when (gender) {
            MALE -> "Male"
            FEMALE -> "Female"
            TRANSGENDER -> "Transgender"
            null -> "NA"
        },
        maritalStatusID = genDetails?.maritalStatusId?.toString() ?: "",
        maritalStatusName = genDetails?.maritalStatus ?: "",
        email = "",
        providerServiceMapID = user.serviceMapId.toString(),
        providerServiceMapId = user.serviceMapId.toString(),
        benDemographics = BenDemographics(
            communityID = communityId.toString(),
            communityName = community ?: "",
            religionID = religionId.toString(),
            religionName = religion ?: "",
            countryID = 1,
            countryName = "India",
            stateID = locationRecord.stateId,
            stateName = locationRecord.state,
            districtID = locationRecord.districtId,
            districtName = locationRecord.district,
            blockID = locationRecord.blockId,
            districtBranchID = locationRecord.villageId,
            districtBranchName = locationRecord.village,
            zoneID = user.zoneId,
            zoneName = user.zoneName,
            parkingPlaceName = user.parkingPlaceName,
            parkingPlaceID = user.parkingPlaceId,
            servicePointID = user.servicePointId.toString(),
            servicePointName = user.servicePointName,
            addressLine1 = "D.No 3-160E",
            addressLine2 = "ARS Road",
            addressLine3 = "Neggipudi",
        ),
        benPhoneMaps = arrayOf(
            BenPhoneMaps(
                phoneNo = contactNumber.toString(),
                createdBy = user.userName,
            )
        ),
        beneficiaryIdentities = arrayOf(
            BeneficiaryIdentities(
                govtIdentityNo = 0,
                govtIdentityTypeName = "null",
                govtIdentityTypeID = 0,
                identityType = "National ID",
                createdBy = user.userName

            )
        ),
        vanID = user.vanId,
        parkingPlaceID = user.parkingPlaceId,
        createdBy = user.userName,


        )
}



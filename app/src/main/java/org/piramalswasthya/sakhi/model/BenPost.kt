package org.piramalswasthya.sakhi.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BenPost(
    @Json(name = "BenRegId")
    val benRegId: Long,
    @Json(name = "Countyid")
    val countyid: Int = 0,
    @Json(name = "Processed")
    val processed: String? = null,
    @Json(name = "isDeath")
    val isDeath: Boolean,

    @Json(name = "isDeathValue")
    val isDeathValue: String,

    @Json(name = "dateOfDeath")
    val dateOfDeath: String,

    @Json(name = "timeOfDeath")
    val timeOfDeath: String,

    @Json(name = "reasonOfDeath")
    val reasonOfDeath: String,

    @Json(name = "reasonOfDeathId")
    val reasonOfDeathId: Int,

    @Json(name = "placeOfDeath")
    val placeOfDeath: String,

    @Json(name = "placeOfDeathId")
    val placeOfDeathId: Int,

    @Json(name = "otherPlaceOfDeath")
    val otherPlaceOfDeath: String,

    @Json(name = "ProviderServiceMapID")
    val providerServiceMapID: Int = 0,
    @Json(name = "VanID")
    val vanID: Int = 4,
    @Json(name = "aadhaNo")
    val aadhaNo: String,
    @Json(name = "aadha_no")
    val aadha_no: String,
    @Json(name = "aadha_noId")
    val aadha_noId: Int,
    @Json(name = "age")
    val age: Int,
    @Json(name = "ageAtMarriage")
    val ageAtMarriage: Int = 0,
    @Json(name = "age_unit")
    val age_unit: String,
    @Json(name = "ageUnitId")
    val age_unitId: Int,
    @Json(name = "ashaid")
    val ashaId: Int,
    @Json(name = "benficieryid")
    val benId: Long,
    @Json(name = "childRegisteredAWCID")
    val childRegisteredAWCID: Int = 0,
    @Json(name = "childRegisteredSchoolID")
    val childRegisteredSchoolID: Int = 0,
    @Json(name = "createdBy")
    val createdBy: String,
    @Json(name = "createdDate")
    val createdDate: String,
    @Json(name = "currSubDistrictId")
    val currSubDistrictId: Int,
    @Json(name = "expectedDateOfDelivery")
    val expectedDateOfDelivery: String? = null,
    @Json(name = "facilitySelection")
    val facilitySelection: String = "",
    @Json(name = "familyHeadRelation")
    val familyHeadRelation: String,
    @Json(name = "familyHeadRelationPosition")
    val familyHeadRelationPosition: Int,
    val fatherName: String,
    val firstName: String,
    @Json(name = "guidelineId")
    val guidelineId: String,
    @Json(name = "houseoldId")
    val householdId: String,
    @Json(name = "hrpStatus")
    val isHrpStatus: Boolean,
    @Json(name = "id")
    val dummyIdMayBe: Int = 1,
    @Json(name = "immunizationStatus")
    val isImmunizationStatus: Boolean = false,
    @Json(name = "lastDeliveryConducted")
    val lastDeliveryConducted: String? = null,
    @Json(name = "lastDeliveryConductedID")
    val lastDeliveryConductedID: Int = 0,
    @Json(name = "lastMenstrualPeriod")
    val lastMenstrualPeriod: String? = null,
    val lastName: String,
    @Json(name = "latitude")
    val latitude: Double,
    @Json(name = "longitude")
    val longitude: Double,
    @Json(name = "marriageDate")
    val marriageDate: String?,
    @Json(name = "mobileOthers")
    val mobileOthers: String? = null,
    @Json(name = "mobilenoofRelation")
    val mobileNoOfRelation: String? = null,
    @Json(name = "mobilenoofRelationId")
    val mobileNoOfRelationId: Int = 0,
    val motherName: String,
    @Json(name = "nayiPahalDeliveryStatus")
    val nayiPahalDeliveryStatus: String = "Select",
    @Json(name = "nayiPahalDeliveryStatusPosition")
    val nayiPahalDeliveryStatusPosition: Int = 0,
    @Json(name = "ncd_priority")
    val ncdPriority: Int = 0,
    @Json(name = "need_opcare")
    val needOpCare: String,
    @Json(name = "need_opcareId")
    val needOpCareId: Int = 0,
    @Json(name = "nishchayPregnancyStatus")
    val nishchayPregnancyStatus: String = "",
    @Json(name = "nishchayPregnancyStatusPosition")
    val nishchayPregnancyStatusPosition: Int = 0,
    @Json(name = "nishchayDeliveryStatus")
    val nishchayDeliveryStatus: String = "",
    @Json(name = "nishchayDeliveryStatusPosition")
    val nishchayDeliveryStatusPosition: Int = 0,
    @Json(name = "noOfDaysForDelivery")
    val noOfDaysForDelivery: Int? = null,
    @Json(name = "previousLiveBirth")
    val previousLiveBirth: String = "",
    @Json(name = "rchid")
    val rchId: String,
    @Json(name = "registrationDate")
    val registrationDate: String,
    @Json(name = "registrationType")
    val registrationType: String,
    @Json(name = "religionOthers")
    val religionOthers: String,
    @Json(name = "reproductiveStatus")
    val reproductiveStatus: String,
    @Json(name = "reproductiveStatusId")
    val reproductiveStatusId: Int,
    @Json(name = "serverUpdatedStatus")
    val serverUpdatedStatus: Int,
    @Json(name = "spousename")
    val spouseName: String,
    @Json(name = "typeofSchoolID")
    val typeOfSchoolId: Int,
    @Json(name = "user_image")
    val userImage: String,
    @Json(name = "villageid")
    val villageId: Int,
    @Json(name = "villagename")
    val villageName: String? = null,
    @Json(name = "whoConductedDeliveryID")
    val whoConductedDeliveryID: Int = 0,
    @Json(name = "whoConductedDelivery")
    val whoConductedDelivery: String? = null,


    @Json(name = "literacy")
    val literacy: String? = null,

    @Json(name = "literacyId")
    val literacyId: Int = 0,


    //Bank details


    //Menstrual details
    @Json(name = "menstrualStatusId")
    val menstrualStatusId: Int = 0,

    @Json(name = "regularityofMenstrualCycleId")
    val regularityofMenstrualCycleId: Int = 0,

    @Json(name = "lengthofMenstrualCycleId")
    val lengthofMenstrualCycleId: Int = 0,

    @Json(name = "menstrualBFDId")
    val menstrualBFDId: Int = 0,

    @Json(name = "menstrualProblemId")
    val menstrualProblemId: Int = 0,


    @Json(name = "formStatus")
    val formStatus: String? = null,

    @Json(name = "formType")
    val formType: String? = null,

    //these are new fields of new born registration
    //strings

    @Json(name = "childRegisteredSchool")
    val childRegisteredSchool: String? = null,

    @Json(name = "TypeofSchool")
    val typeofSchool: String? = null,

    //these are new fields of registration for asha login


    @Json(name = "menstrualStatus")
    val menstrualStatus: String? = null,


    @Json(name = "dateMarriage")
    val dateMarriage: String? = null,

    @Json(name = "deliveryDate")
    val deliveryDate: String? = null,

    @Json(name = "suspected_hrp")
    val suspected_hrp: String? = null,

    @Json(name = "suspected_ncd")
    val suspected_ncd: String? = null,

    @Json(name = "suspected_tb")
    val suspected_tb: String? = null,

    @Json(name = "suspected_ncd_diseases")
    val suspected_ncd_diseases: String? = null,

    @Json(name = "confirmed_ncd")
    val confirmed_ncd: String? = null,

    @Json(name = "confirmed_hrp")
    val confirmed_hrp: String? = null,

    @Json(name = "confirmed_tb")
    val confirmed_tb: String? = null,

    @Json(name = "confirmed_ncd_diseases")
    val confirmed_ncd_diseases: String? = null,

    @Json(name = "diagnosis_status")
    val diagnosis_status: String? = null,

    @Json(name = "benPhoneMaps")
    val benPhoneMaps: Array<BenPhoneMaps>,

    @Json(name = "dob")
    val dob: String,

    @Json(name = "emergencyRegistration")
    val isEmergencyRegistration: Boolean = false,

    @Json(name = "genderId")
    val genderId: Int = 0,

    @Json(name = "gender")
    val gender: String,

    @Json(name = "maritalstatusId")
    val maritalStatusID: String? = null,

    @Json(name = "maritalstatus")
    val maritalStatusName: String? = null,

    @Json(name = "i_bendemographics")
    val benDemographics: BenDemographics,

    @Json(name = "isSpouseAdded")
    val isSpouseAdded: Boolean = false,

    @Json(name = "isChildrenAdded")
    val isChildrenAdded: Boolean = false,

    @Json(name = "isMarried")
    val isMarried: Boolean = false,

    @Json(name = "doYouHavechildren")
    val doYouHavechildren: Boolean = false,

    @Json(name = "noOfchildren")
    val noOfchildren: Int = 0,

    @Json(name = "noofAlivechildren")
    val noofAlivechildren: Int = 0,



    )

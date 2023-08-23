package org.piramalswasthya.sakhi.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BenPost(
    @Json(name = "BenRegId")
    @SerializedName("BenRegId")
    val benRegId: Long,
    @Json(name = "Countyid")
    @SerializedName("Countyid")
    val countyid: Int = 0,
    @Json(name = "Processed")
    @SerializedName("Processed")
    val processed: String? = null,
    @Json(name = "ProviderServiceMapID")
    @SerializedName("ProviderServiceMapID")
    val providerServiceMapID: Int = 0,
    @Json(name = "VanID")
    @SerializedName("VanID")
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
    @SerializedName("ashaid")
    val ashaId: Int,
    @Json(name = "benficieryid")
    @SerializedName("benficieryid")
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
    @SerializedName("houseoldId")
    val householdId: String,
    @Json(name = "hrpStatus")
    @SerializedName("hrpStatus")
    val isHrpStatus: Boolean,
    @Json(name = "id")
    @SerializedName("id")
    val dummyIdMayBe: Int = 1,
    @Json(name = "immunizationStatus")
    @SerializedName("immunizationStatus")
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
    @SerializedName("mobilenoofRelation")
    val mobileNoOfRelation: String? = null,
    @Json(name = "mobilenoofRelationId")
    @SerializedName("mobilenoofRelationId")
    val mobileNoOfRelationId: Int = 0,
    val motherName: String,
    @Json(name = "nayiPahalDeliveryStatus")
    val nayiPahalDeliveryStatus: String = "Select",
    @Json(name = "nayiPahalDeliveryStatusPosition")
    val nayiPahalDeliveryStatusPosition: Int = 0,
    @Json(name = "ncd_priority")
    @SerializedName("ncd_priority")
    val ncdPriority: Int = 0,
    @Json(name = "need_opcare")
    @SerializedName("need_opcare")
    val needOpCare: String,
    @Json(name = "need_opcareId")
    @SerializedName("need_opcareId")
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
    @SerializedName("rchid")
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
    @SerializedName("spousename")
    val spouseName: String,
    @Json(name = "typeofSchoolID")
    @SerializedName("typeofSchoolID")
    val typeOfSchoolId: Int,
    @Json(name = "user_image")
    @SerializedName("user_image")
    val userImage: String,
    @Json(name = "villageid")
    @SerializedName("villageid")
    val villageId: Int,
    @Json(name = "villagename")
    @SerializedName("villagename")
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
    @SerializedName("TypeofSchool")
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


    )

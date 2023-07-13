package org.piramalswasthya.sakhi.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendingRMNCHData(
    @Json(name = "houseHoldDetails")
    @SerializedName("houseHoldDetails")
    var houseHoldRegistrationData: List<HouseholdNetwork>? = null,

    @Json(name = "beneficiaryDetails")
    @SerializedName("beneficiaryDetails")
    var benficieryRegistrationData: List<BenPost>? = null,

    @Json(name = "cBACDetails")
    @SerializedName("cBACDetails")
    var cbacData: List<CbacPost>? = null,

    @Json(name = "bornBirthDeatils")
    @SerializedName("bornBirthDeatils")
    var birthDetails: List<BenRegKidNetwork>? = null,
)
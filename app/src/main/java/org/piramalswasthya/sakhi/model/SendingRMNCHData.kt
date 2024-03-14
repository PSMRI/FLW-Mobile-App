package org.piramalswasthya.sakhi.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendingRMNCHData(
    @Json(name = "houseHoldDetails")
    var houseHoldRegistrationData: List<HouseholdNetwork>? = null,

    @Json(name = "beneficiaryDetails")
    var benficieryRegistrationData: List<BenPost>? = null,

    @Json(name = "cBACDetails")
    var cbacData: List<CbacPost>? = null,

    @Json(name = "bornBirthDeatils")
    var birthDetails: List<BenRegKidNetwork>? = null,
)
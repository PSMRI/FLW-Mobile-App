package org.piramalswasthya.sakhi.model.dynamicModel

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class HBNCVisitResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("beneficiaryId")
    val beneficiaryId: Int,

    @SerializedName("visitDate")
    val visitDate: String,

    @SerializedName("fields")
    val fields: JsonObject
)

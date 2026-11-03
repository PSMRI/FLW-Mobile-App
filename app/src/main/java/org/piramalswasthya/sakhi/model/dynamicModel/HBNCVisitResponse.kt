package org.piramalswasthya.sakhi.model.dynamicModel

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HBNCVisitResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("houseHoldId")
    val houseHoldId: Long,

  @SerializedName("beneficiaryId")
    val beneficiaryId: Long,


    @SerializedName("visitDate")
    val visitDate: String,

    @SerializedName("eyeSide")
    val eyeSide: String,


    @SerializedName("fields")
    val fields: JsonObject
)

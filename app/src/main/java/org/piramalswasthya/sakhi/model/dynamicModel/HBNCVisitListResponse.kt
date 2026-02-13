package org.piramalswasthya.sakhi.model.dynamicModel

import com.google.gson.annotations.SerializedName

data class HBNCVisitListResponse(
    @SerializedName("data")
    val data: List<HBNCVisitResponse> = emptyList(),

    @SerializedName("statusCode")
    val statusCode: Int = 0,

    @SerializedName("errorMessage")
    val errorMessage: String? = null,

    @SerializedName("status")
    val status: String? = null
)

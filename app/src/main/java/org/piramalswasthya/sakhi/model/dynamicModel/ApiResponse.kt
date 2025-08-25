package org.piramalswasthya.sakhi.model.dynamicModel

import com.google.gson.annotations.SerializedName
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("statusCode")
    val statusCode: Int? = null,

    @SerializedName("data")
    val data: T? = null
)

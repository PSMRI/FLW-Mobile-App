package org.piramalswasthya.sakhi.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadResponse(
    val data: UploadData?,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class UploadData(
    val response: String
)
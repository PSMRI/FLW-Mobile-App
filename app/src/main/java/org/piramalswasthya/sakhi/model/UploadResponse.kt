package org.piramalswasthya.sakhi.model

data class UploadResponse(
    val data: UploadData?,
    val statusCode: Int,
    val errorMessage: String,
    val status: String
)

data class UploadData(
    val response: String
)
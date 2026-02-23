package org.piramalswasthya.sakhi.model

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val fileUrls: List<String>,
    val uploadId: String
)
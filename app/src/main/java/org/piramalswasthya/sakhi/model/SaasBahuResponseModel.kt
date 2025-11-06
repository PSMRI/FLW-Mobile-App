package org.piramalswasthya.sakhi.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SaasBahuSammelanGetAllResponse(
    @Json(name = "data") val data: List<SaasBahuSammelanServerItem>?,
    @Json(name = "statusCode") val statusCode: Int?,
    @Json(name = "status") val status: String?
)

@JsonClass(generateAdapter = true)
data class SaasBahuSammelanServerItem(
    @Json(name = "id") val id: Int?,
    @Json(name = "date") val meetingDate: Long? = 0L,
    @Json(name = "place") val place: String?,
    @Json(name = "participants") val participants: Int?,
    @Json(name = "ashaId") val ashaId: Int?,
    @Json(name = "imagePaths") val meetingImages: List<String>?
)
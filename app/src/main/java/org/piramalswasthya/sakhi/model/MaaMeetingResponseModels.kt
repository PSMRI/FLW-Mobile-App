package org.piramalswasthya.sakhi.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MaaMeetingGetAllResponse(
    @Json(name = "data") val data: List<MaaMeetingServerItem>?,
    @Json(name = "statusCode") val statusCode: Int?,
    @Json(name = "status") val status: String?
)

@JsonClass(generateAdapter = true)
data class MaaMeetingServerItem(
    @Json(name = "id") val id: Int?,
    @Json(name = "meetingDate") val meetingDate: String?,
    @Json(name = "place") val place: String?,
    @Json(name = "participants") val participants: Int?,
    @Json(name = "ashaId") val ashaId: Int?,
    @Json(name = "meetingImages") val meetingImages: List<String>?
)
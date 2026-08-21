package org.piramalswasthya.sakhi.network

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Badge endpoints (LLD §4.2) on the existing authenticated client.
 * The server owns policy; the device owns computation. No beneficiary
 * data is ever transmitted.
 */
interface BadgeApiService {

    @GET("flw-api/badges/config")
    suspend fun getConfig(): Response<BadgeConfigResponse>

    @GET("flw-api/badges/freezes")
    suspend fun getFreezes(): Response<BadgeFreezesResponse>

    @GET("flw-api/badges/earned")
    suspend fun getEarned(): Response<BadgeEarnedResponse>

    @POST("flw-api/badges/earned")
    suspend fun postEarned(@Body body: BadgeEarnedPush): Response<Unit>
}

@JsonClass(generateAdapter = true)
data class BadgeConfigResponse(
    /** key/value pairs, e.g. "milestones.steady_syncer" → "4,8,16,26", "feature_enabled" → "true" */
    val config: Map<String, String>?
)

@JsonClass(generateAdapter = true)
data class BadgeFreezesResponse(
    val freezes: List<BadgeFreezeDTO>?
)

@JsonClass(generateAdapter = true)
data class BadgeFreezeDTO(
    /** null/empty applies to all badges */
    val badgeId: String?,
    val startDate: Long,
    val endDate: Long
)

@JsonClass(generateAdapter = true)
data class BadgeEarnedResponse(
    val earned: List<BadgeEarnedDTO>?
)

@JsonClass(generateAdapter = true)
data class BadgeEarnedDTO(
    val badgeId: String,
    val level: Int,
    val earnedAt: Long
)

@JsonClass(generateAdapter = true)
data class BadgeEarnedPush(
    val userId: Int,
    val badges: List<BadgeEarnedDTO>
)

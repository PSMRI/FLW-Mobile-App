package org.piramalswasthya.sakhi.model.dynamicEntity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NCDFollowUpResponse(
    val statusCode: Int,
    val data: List<FormNCDFollowUpSubmitRequest>
)

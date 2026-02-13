package org.piramalswasthya.sakhi.model.dynamicEntity

data class NCDFollowUpResponse(
    val statusCode: Int,
    val data: List<FormNCDFollowUpSubmitRequest>
)

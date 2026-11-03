package org.piramalswasthya.sakhi.model.dynamicEntity

import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class FormNCDFollowUpSubmitRequest(
    val id: Int,
    val benId: Long,
    val hhId: Long,
    val visitNo: Int,
    val followUpNo: Int,
    val treatmentStartDate: String,
    val followUpDate: String?,
    val diagnosisCodes: String?,
    val formId: String,
    val version: Int,
    val formDataJson: String   // 🔥 STRING ONLY
)

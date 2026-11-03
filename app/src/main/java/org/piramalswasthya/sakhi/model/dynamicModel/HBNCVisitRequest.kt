package org.piramalswasthya.sakhi.model.dynamicModel

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HBNCVisitRequest(
    val fromDate: String,
    val toDate: String,
    val pageNo: Int,
    val ashaId: Int,
    val userName: String
)

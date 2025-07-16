package org.piramalswasthya.sakhi.model.dynamicModel

data class HBNCVisitRequest(
    val villageID: Int,
    val fromDate: String,
    val toDate: String,
    val pageNo: Int,
    val userId: Int,
    val userName: String,
    val ashaId: Int
)

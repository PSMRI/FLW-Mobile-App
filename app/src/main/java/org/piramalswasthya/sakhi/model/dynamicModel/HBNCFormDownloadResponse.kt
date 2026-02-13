package org.piramalswasthya.sakhi.model.dynamicModel

data class HBNCFormDownloadResponse(
    val id: Int,
    val beneficiaryId: Int,
    val visitDate: String,
    val fields: Map<String, Any?>
)

package org.piramalswasthya.sakhi.model.dynamicEntity

data class FormSubmitRequest(
    val userName: String,
    val formId: String,
    val beneficiaryId: Long,
    val houseHoldId: Long,
    val visitDate: String,
    val fields: Map<String, Any?>
)

package org.piramalswasthya.sakhi.model.dynamicEntity

data class FormSubmitRequest(
    val formId: String,
    val beneficiaryId: Int,
    val houseHoldId: Int,
    val visitDate: String,
    val fields: Map<String, Any?>
)

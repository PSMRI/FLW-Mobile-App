package org.piramalswasthya.sakhi.model

data class ChildOption(
    val formType: String,
    val title: String,
    val description: String,
    val isViewMode: Boolean = false,
    val visitDay: String? = null,
    val formDataJson: String? = null,
    val recordId: Int? = null
)
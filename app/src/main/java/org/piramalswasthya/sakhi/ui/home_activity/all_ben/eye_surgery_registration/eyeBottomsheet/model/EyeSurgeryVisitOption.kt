package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.model

data class EyeSurgeryVisitOption(
    val title: String,
    val visitDate: String,
    val eyeSide: String?,
    val isAddNew: Boolean,
    val formDataJson: String?,
    val recordId: Int?,

    val benName: String = "",
    val gender: String = "",
    val age: String = ""
)
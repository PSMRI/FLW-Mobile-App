package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model

data class ActivityDetail(
    val id: String,
    val name: String,
    val amount: Int,
    val claimCount: Int,
    val activityDate: String,
    val isDefaultActivity: Boolean,
    val groupName: String,
    val activityDec: String,
    val submittedOn: String,
    val status: ActivityStatus,
//    val statusMessage: String
)

enum class ActivityStatus {
    PENDING,
    VERIFIED,
    REJECTED
}

data class RejectionReason(
    val id: String,
    val reason: String,
    var isSelected: Boolean = false
)

data class WorkerDetailInfo(
    val workerName: String,
    val ashaId: String,
    val serviceCenter: String,
    val month: String,
    val supervisorId: String,
    val activities: List<ActivityDetail>
)

package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model

data class AshaWorker(
    val id: String,
    val name: String,
    val ashaId: String,
    val serviceCenter: String,
    val amount: Int,
    val totalIncentive: Int,
    val status: VerificationStatus
)

enum class VerificationStatus {
    VERIFIED,
    PENDING,
    REJECTED
}

data class MonthlyDetail(
    val month: String,
    val year: Int,
    val serviceCenter: String,
    val verifiedCount: Int,
    val pendingCount: Int,
    val rejectedCount: Int
)

data class Supervisor(
    val name: String,
    val id: String
)

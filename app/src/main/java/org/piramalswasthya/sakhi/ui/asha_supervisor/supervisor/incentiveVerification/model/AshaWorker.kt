package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model

import com.google.gson.annotations.SerializedName

data class AshaListResponse(
    @SerializedName("approvalStatus") val approvalStatus: ApprovalStatusSummary?,
    @SerializedName("data") val data: List<AshaWorkerResponse>?,
    @SerializedName("statusCode") val statusCode: Int?
)

data class ApprovalStatusSummary(
    @SerializedName("rejected") val rejected: Int = 0,
    @SerializedName("pending") val pending: Int = 0,
    @SerializedName("verified") val verified: Int = 0
)

data class AshaWorkerResponse(
    @SerializedName("approvalStatus") val approvalStatus: Int?,
    @SerializedName("facilityId") val facilityId: Int?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("facilityType") val facilityType: String?,
    @SerializedName("rejected") val rejected: Int?,
    @SerializedName("pending") val pending: Int?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("verified") val verified: Int?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("employeeId") val employeeId: String?,
    @SerializedName("userId") val userId: Int,
    @SerializedName("totalAmount") val totalAmount: Int?,
    @SerializedName("facilityName") val facilityName: String?,
    @SerializedName("activities") val activities: List<Activity>?


)
data class Activity(
    @SerializedName("approvalStatus") val approvalStatus: Int?,
    @SerializedName("reason") val reason: String?,
    @SerializedName("otherReason") val otherReason: String?,
    @SerializedName("claimedDate") val claimedDate: String?,
    @SerializedName("approvalDate") val approvalDate: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("isClaimed") val isClaimed: Boolean?
)

data class AshaWorker(
    val id: String,
    val name: String,
    val ashaId: String,
    val approvalDate: String,
    val OtherReason: String,
    val reason: String,
    val serviceCenter: String,
    val role: String,
    val amount: Int,
    val pending: Int,
    val verified: Int,
    val rejected: Int,
    val status: VerificationStatus
)

enum class VerificationStatus {
    VERIFIED,
    PENDING,
    REJECTED,
    OVERDUE,
    ALL
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
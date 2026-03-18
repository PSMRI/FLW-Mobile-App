package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("data") val data: DashboardData?,
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("errorMessage") val errorMessage: String?,
    @SerializedName("status") val status: String
)

data class DashboardData(
    @SerializedName("incentiveSummary") val incentiveSummary: IncentiveSummary,
    @SerializedName("location") val location: Location,
    @SerializedName("totalAshaCount") val totalAshaCount: Int,
    @SerializedName("facilities") val facilities: List<Facility>,
    @SerializedName("supervisor") val supervisor: Supervisor
)

data class IncentiveSummary(
    @SerializedName("overDue") val overDue: Int,
    @SerializedName("rejected") val rejected: Int,
    @SerializedName("pending") val pending: Int,
    @SerializedName("verified") val verified: Int
)

data class Location(
    @SerializedName("district") val district: String,
    @SerializedName("blockOrUlb") val blockOrUlb: String,
    @SerializedName("locationType") val locationType: String,
    @SerializedName("state") val state: String
)

data class Facility(
    @SerializedName("facilityId") val facilityId: Int,
    @SerializedName("ashaCount") val ashaCount: Int,
    @SerializedName("facilityType") val facilityType: String,
    @SerializedName("facilityName") val facilityName: String
)

data class Supervisor(
    @SerializedName("gender") val gender: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("userId") val userId: Int
)
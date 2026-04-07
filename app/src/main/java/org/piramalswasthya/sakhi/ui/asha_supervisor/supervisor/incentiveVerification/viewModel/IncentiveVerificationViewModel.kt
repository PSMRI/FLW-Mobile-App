package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.ApprovalStatusSummary
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.AshaListResponse
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.AshaWorker
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.VerificationStatus
import org.piramalswasthya.sakhi.utils.HelperUtil.formatDate
import javax.inject.Inject

@HiltViewModel
class IncentiveVerificationViewModel @Inject constructor(
    private val apiService: AmritApiService
) : ViewModel() {

    private val _uiState = MutableLiveData<VerificationUiState>()
    val uiState: LiveData<VerificationUiState> = _uiState

    private var currentStatus: VerificationStatus = VerificationStatus.ALL
    private var allWorkers: List<AshaWorker> = emptyList()
    private var summary: ApprovalStatusSummary = ApprovalStatusSummary(0, 0, 0)
    private var facilityId: Int = 0
    private var isInitialized = false
    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0

    fun init(status: String, facilityId: Int, month: Int, year: Int) {
        if (isInitialized) return
        isInitialized = true

        this.facilityId = facilityId
        this.selectedMonth = month
        this.selectedYear = year

        currentStatus = when (status.lowercase()) {
            "verified" -> VerificationStatus.VERIFIED
            "pending"  -> VerificationStatus.PENDING
            "rejected" -> VerificationStatus.REJECTED
            "overdue"  -> VerificationStatus.OVERDUE
            else       -> VerificationStatus.ALL
        }
        fetchAshaList()
    }

    fun refresh() {
        if (!isInitialized) return
        fetchAshaList()
    }

    private fun fetchAshaList() {
        val approvalStatusCode = when (currentStatus) {
            VerificationStatus.VERIFIED -> 101
            VerificationStatus.PENDING  -> 102
            VerificationStatus.REJECTED -> 103
            VerificationStatus.OVERDUE  -> 104
            VerificationStatus.ALL      -> 0
        }

        viewModelScope.launch {
            _uiState.value = VerificationUiState.Loading
            try {
                val response = apiService.getAshaListByFacility(
                    mapOf(
                        "facilityId"     to facilityId,
                        "month"          to selectedMonth,
                        "year"           to selectedYear,
                        "approvalStatus" to approvalStatusCode
                    )
                )

                if (response.isSuccessful) {
                    val json = response.body()?.string()

                    if (json.isNullOrEmpty()) {
                        _uiState.value = VerificationUiState.Error("Empty response from server")
                        return@launch
                    }

                    val parsed = try {
                        Gson().fromJson(json, AshaListResponse::class.java)
                    } catch (e: Exception) {
                        _uiState.value = VerificationUiState.Error("Parse error: ${e.message}")
                        return@launch
                    }

                    summary = parsed.approvalStatus ?: ApprovalStatusSummary(0, 0, 0)

                    allWorkers = parsed.data?.map { worker ->
                        val latestActivity = worker.activities
                            ?.filter { !it.approvalDate.isNullOrBlank() }
                            ?.maxByOrNull { it.approvalDate ?: "" }
                        AshaWorker(
                            id            = worker.userId.toString(),
                            name          = worker.fullName ?: "Unknown",
                            ashaId        = worker.employeeId ?: worker.userId.toString(),
                            serviceCenter = worker.facilityName ?: "",
                            amount        = worker.totalAmount ?: 0,
                            pending       = worker.pending ?: 0,
                            verified      = worker.verified ?: 0,
                            rejected      = worker.rejected ?: 0,
                            role          = latestActivity?.role?.takeIf { it.isNotBlank() } ?: "",
                            approvalDate  = formatDate(latestActivity?.approvalDate),
                            reason        = latestActivity?.reason ?: "",
                            OtherReason   = latestActivity?.otherReason ?: "",
                            status        = mapStatus(worker.approvalStatus)
                        )
                    } ?: emptyList()

                    _uiState.value = VerificationUiState.Success(
                        workers = allWorkers,
                        summary = summary
                    )
                } else {
                    _uiState.value = VerificationUiState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = VerificationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun search(query: String) {
        val filtered = if (query.isEmpty()) allWorkers
        else allWorkers.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.ashaId.contains(query, ignoreCase = true) ||
                    it.serviceCenter.contains(query, ignoreCase = true)
        }
        _uiState.value = VerificationUiState.Success(workers = filtered, summary = summary)
    }

    private fun mapStatus(code: Int?): VerificationStatus = when (code) {
        101  -> VerificationStatus.VERIFIED
        102  -> VerificationStatus.PENDING
        103  -> VerificationStatus.REJECTED
        104  -> VerificationStatus.OVERDUE
        else -> VerificationStatus.PENDING
    }
}

sealed class VerificationUiState {
    object Loading : VerificationUiState()
    data class Success(
        val workers: List<AshaWorker>,
        val summary: ApprovalStatusSummary
    ) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}
package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.IncentiveRecordListRequest
import org.piramalswasthya.sakhi.network.AmritApiService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class WorkerDetailViewModel @Inject constructor(
    private val apiService: AmritApiService,
    private val preferenceDao: PreferenceDao,
) : ViewModel() {

    private val _uiState = MutableLiveData<WorkerDetailUiState>()
    val uiState: LiveData<WorkerDetailUiState> = _uiState

    private val _actionState = MutableLiveData<ActionState>()
    val actionState: LiveData<ActionState> = _actionState

    private var filterMonth: Int = 0
    private var filterYear: Int = 0

    fun init(userId: Int, month: Int, year: Int) {
        filterMonth = month
        filterYear = year
        fetchClaimedIncentives(userId)
    }

    private fun fetchClaimedIncentives(userId: Int) {
        viewModelScope.launch {
            _uiState.value = WorkerDetailUiState.Loading
            try {
                val user = preferenceDao.getLoggedInUser()
                val userStateId = preferenceDao.getStateId()
                if (user == null) {
                    _uiState.value = WorkerDetailUiState.Error("User not logged in")
                    return@launch
                }

                android.util.Log.d(
                    "WorkerDetailVM",
                    "Fetching claimed incentives: userId=$userId month=$filterMonth year=$filterYear"
                )

                val requestBody = mapOf(
                    "userId" to userId,
                    "month" to filterMonth,
                    "year" to filterYear,
                    "villageID" to userStateId
                )

                val response = apiService.getClaimedIncentiveByUser(requestBody = requestBody)

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    if (json.isNullOrEmpty()) {
                        _uiState.value = WorkerDetailUiState.Error("Empty response")
                        return@launch
                    }

                    val jsonObj = JSONObject(json)
                    val statusCode = jsonObj.getInt("statusCode")

                    when (statusCode) {
                        200 -> {
                            val dataArray = jsonObj.getJSONArray("data")
                            val type = object : TypeToken<List<ClaimedIncentiveUI>>() {}.type
                            val records: List<ClaimedIncentiveUI> = Gson().fromJson(
                                dataArray.toString(), type
                            )
                            android.util.Log.d("WorkerDetailVM", "Records fetched: ${records.size}")
                            _uiState.value = WorkerDetailUiState.Success(records)
                        }
                        5000 -> {
                            _uiState.value = WorkerDetailUiState.Success(emptyList())
                        }
                        else -> {
                            _uiState.value = WorkerDetailUiState.Error(
                                jsonObj.optString("errorMessage", "Unknown error")
                            )
                        }
                    }
                } else {
                    _uiState.value = WorkerDetailUiState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("WorkerDetailVM", "Error: ${e.message}", e)
                _uiState.value = WorkerDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun verifyActivities(
        ashaId: Int,
        incentiveIds: List<Long>
    ) {

        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            try {
                val response = apiService.updateApprovalStatus(
                    mapOf(
                        "ashaId" to ashaId,
                        "month" to filterMonth,
                        "year" to filterYear,
                        "approvalStatus" to 101,
                        "incentiveIds" to incentiveIds.joinToString(","),
                        "reason" to "",
                        "otherReason" to ""
                    )
                )
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val jsonObj = JSONObject(json ?: "{}")
                    if (jsonObj.optInt("statusCode", 0) == 200) {
                        val updated = jsonObj.optInt("updatedRecords", 0)
                        _actionState.value = ActionState.Success("Successfully verified $updated records")
                    } else {
                        _actionState.value = ActionState.Error("Verification failed")
                    }
                } else {
                    _actionState.value = ActionState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("WorkerDetailVM", "Verify error: ${e.message}", e)
                _actionState.value = ActionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun rejectActivities(
        ashaId: Int,
        incentiveIds: List<Long>,
        reason: String,
        otherReason: String
    ) {

        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            try {
                val response = apiService.updateApprovalStatus(
                    mapOf(
                        "ashaId" to ashaId,
                        "month" to filterMonth,
                        "year" to filterYear,
                        "approvalStatus" to 103,
                        "incentiveIds" to incentiveIds.joinToString(","),
                        "reason" to reason,
                        "otherReason" to otherReason
                    )
                )
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val jsonObj = JSONObject(json ?: "{}")
                    if (jsonObj.optInt("statusCode", 0) == 200) {
                        val updated = jsonObj.optInt("updatedRecords", 0)
                        _actionState.value = ActionState.Success("Successfully rejected $updated records")
                    } else {
                        _actionState.value = ActionState.Error("Rejection failed")
                    }
                } else {
                    _actionState.value = ActionState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

data class ClaimedIncentiveUI(
    @SerializedName("activityId") val activityId: Int,
    @SerializedName("activityDec") val activityDec: String?,
    @SerializedName("groupName") val groupName: String?,
    @SerializedName("amount") val amount: Int,
    @SerializedName("claimCount") val claimCount: Int,
    @SerializedName("isDefaultActivity") val isDefaultActivity: Boolean,
    @SerializedName("totalAmount") val totalAmount: Int
)

sealed class WorkerDetailUiState {
    object Loading : WorkerDetailUiState()
    data class Success(val records: List<ClaimedIncentiveUI>) : WorkerDetailUiState()
    data class Error(val message: String) : WorkerDetailUiState()
}

sealed class ActionState {
    object Loading : ActionState()
    data class Success(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
}
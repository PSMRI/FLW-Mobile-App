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

    private val inputFormats = listOf(
        SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa", Locale.ENGLISH),
        SimpleDateFormat("MMM dd, yyyy h:mm:ss aa", Locale.ENGLISH),
        SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH)
    )

    fun init(userId: Int, month: Int, year: Int) {
        filterMonth = month
        filterYear = year
        fetchIncentiveRecords(userId)
    }

    private fun parseDate(dateStr: String?): Calendar? {
        if (dateStr.isNullOrEmpty()) return null
        for (format in inputFormats) {
            try {
                val date = format.parse(dateStr) ?: continue
                val cal = Calendar.getInstance()
                cal.time = date
                return cal
            } catch (_: Exception) {}
        }
        return null
    }

    private fun fetchIncentiveRecords(userId: Int) {
        viewModelScope.launch {
            _uiState.value = WorkerDetailUiState.Loading
            try {
                val user = preferenceDao.getLoggedInUser()
                if (user == null) {
                    _uiState.value = WorkerDetailUiState.Error("User not logged in")
                    return@launch
                }

                // ✅ UTC timezone set karo — false Z marker fix
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }

                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.set(Calendar.MONTH, filterMonth - 1)
                calendar.set(Calendar.YEAR, filterYear)

                // ✅ fromDate — month start, millisecond 0
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val fromDate = sdf.format(calendar.time)

                // ✅ toDate — month end, millisecond 999
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val toDate = sdf.format(calendar.time)

                android.util.Log.d("WorkerDetailVM", "userId=$userId month=$filterMonth year=$filterYear fromDate=$fromDate toDate=$toDate")

                val requestBody = IncentiveRecordListRequest(
                    userId = userId,
                    fromDate = fromDate,
                    toDate = toDate,
                    villageID = user.state.id
                )

                val response = apiService.getAllIncentiveRecords(requestBody = requestBody)

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
                            val type = object : TypeToken<List<IncentiveRecordUI>>() {}.type
                            val allRecords: List<IncentiveRecordUI> = Gson().fromJson(
                                dataArray.toString(), type
                            )

                            val filteredRecords = allRecords.filter { record ->
                                val cal = parseDate(record.startDate) ?: return@filter false
                                cal.get(Calendar.MONTH) + 1 == filterMonth &&
                                        cal.get(Calendar.YEAR) == filterYear
                            }

                            android.util.Log.d(
                                "WorkerDetailVM",
                                "Total: ${allRecords.size}, Filtered: ${filteredRecords.size}"
                            )

                            _uiState.value = WorkerDetailUiState.Success(filteredRecords)
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
        // ✅ empty guard
        if (incentiveIds.isEmpty()) {
            _actionState.value = ActionState.Error("No records to verify")
            return
        }
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
        // ✅ empty guard
        if (incentiveIds.isEmpty()) {
            _actionState.value = ActionState.Error("No records to reject")
            return
        }
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

data class IncentiveRecordUI(
    @SerializedName("id") val id: Long,
    @SerializedName("activityId") val activityId: Long,
    @SerializedName("ashaId") val ashaId: Int,
    @SerializedName("benId") val benId: Long,
    @SerializedName("amount") val amount: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("activityDec") val activityDec: String?,
    @SerializedName("groupName") val groupName: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("createdDate") val createdDate: String?,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("updatedDate") val updatedDate: String?,
    @SerializedName("updatedBy") val updatedBy: String?,
    @SerializedName("isEligible") val isEligible: Boolean?,
    @SerializedName("isDefaultActivity") val isDefaultActivity: Boolean?,
    @SerializedName("approvalStatus") val approvalStatus: Int?
)

sealed class WorkerDetailUiState {
    object Loading : WorkerDetailUiState()
    data class Success(val records: List<IncentiveRecordUI>) : WorkerDetailUiState()
    data class Error(val message: String) : WorkerDetailUiState()
}

sealed class ActionState {
    object Loading : ActionState()
    data class Success(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
}
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
import org.piramalswasthya.sakhi.network.AmritApiService
import javax.inject.Inject

@HiltViewModel
class BeneficiaryDetailViewModel @Inject constructor(
    private val apiService: AmritApiService,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    private val _uiState = MutableLiveData<BeneficiaryUiState>()
    val uiState: LiveData<BeneficiaryUiState> = _uiState

    private val _actionState = MutableLiveData<ActionState>()
    val actionState: LiveData<ActionState> = _actionState

    private var filterMonth: Int = 0
    private var filterYear: Int = 0

    fun fetchBeneficiaries(userId: Int, month: Int, year: Int, activityId: Int) {
        filterMonth = month
        filterYear = year
        viewModelScope.launch {
            _uiState.value = BeneficiaryUiState.Loading
            try {
                val user = preferenceDao.getLoggedInUser()
                if (user == null) {
                    _uiState.value = BeneficiaryUiState.Error("User not logged in")
                    return@launch
                }

                val response = apiService.getActivityDetailRecords(
                    requestBody = mapOf(
                        "userId"     to userId,
                        "month"      to month,
                        "year"       to year,
                        "villageID"  to user.state.id,
                        "activityId" to activityId
                    )
                )

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    if (json.isNullOrEmpty()) {
                        _uiState.value = BeneficiaryUiState.Error("Empty response")
                        return@launch
                    }

                    val jsonObj = JSONObject(json)
                    when (jsonObj.getInt("statusCode")) {
                        200 -> {
                            val type = object : TypeToken<List<BeneficiaryRecordUI>>() {}.type
                            val records: List<BeneficiaryRecordUI> = Gson().fromJson(
                                jsonObj.getJSONArray("data").toString(), type
                            )
                            _uiState.value = BeneficiaryUiState.Success(records)
                        }
                        5000 -> _uiState.value = BeneficiaryUiState.Success(emptyList())
                        else -> _uiState.value = BeneficiaryUiState.Error(
                            jsonObj.optString("errorMessage", "Unknown error")
                        )
                    }
                } else {
                    _uiState.value = BeneficiaryUiState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = BeneficiaryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun verifyBeneficiaries(ashaId: Int, incentiveIds: List<Long>) {
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
                _actionState.value = ActionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun rejectBeneficiaries(ashaId: Int, incentiveIds: List<Long>, reason: String, otherReason: String) {
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

data class BeneficiaryRecordUI(
    @SerializedName("id") val id: Long,
    @SerializedName("activityId") val activityId: Long,
    @SerializedName("ashaId") val ashaId: Int,
    @SerializedName("benId") val benId: Long,
    @SerializedName("amount") val amount: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("activityDec") val activityDec: String?,
    @SerializedName("groupName") val groupName: String?,
    @SerializedName("approvalStatus") val approvalStatus: Int?,
    @SerializedName("rchId") val rchId: String?,
    @SerializedName("abhaNumber") val abhaNumber: String?,
    @SerializedName("isClaimed") val isClaimed: Boolean?,
    @SerializedName("verifiedByUserName") val verifiedByUserName: String?
)

sealed class BeneficiaryUiState {
    object Loading : BeneficiaryUiState()
    data class Success(val records: List<BeneficiaryRecordUI>) : BeneficiaryUiState()
    data class Error(val message: String) : BeneficiaryUiState()
}
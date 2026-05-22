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

    fun fetchBeneficiaries(userId: Int, month: Int, year: Int, activityId: Int) {
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
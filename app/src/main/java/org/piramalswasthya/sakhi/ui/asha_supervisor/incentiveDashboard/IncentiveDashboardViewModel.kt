package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard.model.DashboardData
import org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard.model.DashboardResponse
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class IncentiveDashboardViewModel @Inject constructor(
    private val apiService: AmritApiService,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo
) : ViewModel() {

    private val _dashboardData = MutableLiveData<DashboardUiState>()
    val dashboardData: LiveData<DashboardUiState> = _dashboardData

    // ✅ sirf ek baar retry allowed
    fun fetchDashboard(month: Int, year: Int, isRetry: Boolean = false) {
        viewModelScope.launch {
            _dashboardData.value = DashboardUiState.Loading
            try {
                val user = preferenceDao.getLoggedInUser()
                if (user == null) {
                    _dashboardData.value = DashboardUiState.Error("User not logged in")
                    return@launch
                }

                val response = apiService.getAshaSupervisorDashboard(
                    mapOf("month" to month, "year" to year)
                )

                if (response.isSuccessful) {
                    val jsonString = response.body()?.string()
                    val parsed = Gson().fromJson(jsonString, DashboardResponse::class.java)

                    when {
                        parsed?.status == "Success" && parsed.data != null -> {
                            _dashboardData.value = DashboardUiState.Success(parsed.data)
                        }
                        parsed?.statusCode == 401 || parsed?.statusCode == 5002 -> {
                            // ✅ sirf ek baar retry
                            if (!isRetry && userRepo.refreshTokenTmc(user.userName, user.password)) {
                                fetchDashboard(month, year, isRetry = true)
                            } else {
                                _dashboardData.value = DashboardUiState.Error("Session expired, please login again")
                            }
                        }
                        else -> {
                            _dashboardData.value = DashboardUiState.Error(
                                parsed?.errorMessage ?: "Something went wrong"
                            )
                        }
                    }
                } else {
                    if (response.code() == 401 && !isRetry) {
                        if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                            fetchDashboard(month, year, isRetry = true)
                            return@launch
                        }
                    }
                    _dashboardData.value = DashboardUiState.Error("Server error: ${response.code()}")
                }
            } catch (e: SocketTimeoutException) {
                _dashboardData.value = DashboardUiState.Error("Timeout error, please try again")
            } catch (e: Exception) {
                _dashboardData.value = DashboardUiState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
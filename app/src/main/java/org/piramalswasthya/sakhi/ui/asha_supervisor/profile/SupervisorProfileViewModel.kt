package org.piramalswasthya.sakhi.ui.asha_supervisor.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import javax.inject.Inject

@HiltViewModel
class SupervisorProfileViewModel @Inject constructor(var preferenceDao: PreferenceDao) : ViewModel() {

    fun getUserGender(): String? = preferenceDao.getUserGender()

    fun getUserDob(): String? = preferenceDao.getUserDob()

    fun getUserMobile(): String? = preferenceDao.getUserMobile()

    fun getUserEmail(): String? = preferenceDao.getUserEmail()
    fun getsupervisorId(): Int? = preferenceDao.getSupervisorId()
    fun getSuperVisorname(): String? = preferenceDao.getSupervisorName()
    fun getSuperVisorSubname(): String = preferenceDao.getLoggedInUser()?.role ?: ""
    fun getDistrict() = preferenceDao.getSupervisorDistrict()
    fun getBlock() = preferenceDao.getSupervisorBlock()
    fun getState() = preferenceDao.getSupervisorState()
    fun getSubcenter() = preferenceDao.getSupervisorSubcenter()
    private val _navigateToLoginPage = MutableLiveData(false)
    val navigateToLoginPage: MutableLiveData<Boolean>
        get() = _navigateToLoginPage

    fun logout() {
        viewModelScope.launch {
            preferenceDao.deleteForLogout()
            preferenceDao.setLastSyncedTimeStamp(Konstants.defaultTimeStamp)
            _navigateToLoginPage.value = true
        }
    }
    fun navigateToLoginPageComplete() {
        _navigateToLoginPage.value = false
    }
}
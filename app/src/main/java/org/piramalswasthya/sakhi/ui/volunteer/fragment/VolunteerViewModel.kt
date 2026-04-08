package org.piramalswasthya.sakhi.ui.volunteer.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import javax.inject.Inject

@HiltViewModel
class VolunteerViewModel @Inject constructor(
    private val pref: PreferenceDao
) : ViewModel() {

    val currentUser = pref.getLoggedInUser()

    private val _navigateToLoginPage = MutableLiveData(false)
    val navigateToLoginPage: MutableLiveData<Boolean>
        get() = _navigateToLoginPage

    fun logout() {
        viewModelScope.launch {
            pref.deleteForLogout()
            pref.setLastSyncedTimeStamp(Konstants.defaultTimeStamp)
            _navigateToLoginPage.value = true
        }
    }

    fun navigateToLoginPageComplete() {
        _navigateToLoginPage.value = false
    }
}
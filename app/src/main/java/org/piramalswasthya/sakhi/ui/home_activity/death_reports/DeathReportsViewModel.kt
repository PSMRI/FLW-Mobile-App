package org.piramalswasthya.sakhi.ui.home_activity.death_reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class
DeathReportsViewModel @Inject constructor() : ViewModel() {

    private val _navigateToCdrList = MutableLiveData(false)
    val navigateToCdrList: LiveData<Boolean>
        get() = _navigateToCdrList

    private val _navigateToMdsrList = MutableLiveData(false)
    val navigateToMdsrList: LiveData<Boolean>
        get() = _navigateToMdsrList

    fun navigateToDeathReportList(isChild: Boolean) {
        viewModelScope.launch {
            if (isChild)
                _navigateToCdrList.value = true
            else
                _navigateToMdsrList.value = true
        }
    }

    fun navigateToCdrListCompleted() {
        _navigateToCdrList.value = false
    }

    fun navigateToMdsrListCompleted() {
        _navigateToMdsrList.value = false
    }
}


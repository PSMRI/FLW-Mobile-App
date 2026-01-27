package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.utils.CampaignDateUtil
import org.piramalswasthya.sakhi.utils.HelperUtil
import javax.inject.Inject
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class PulsePolioCampaignListViewModel @Inject constructor(
    private val vlfRepo: VLFRepo
) : ViewModel() {

    val allPulsePolioCampaignList = vlfRepo.pulsePolioCampaignList.map { list ->
        try {
            val currentYear = HelperUtil.getCurrentYear().toInt()
            list.filter {
                try {
                    val date = it.campaignDate ?: return@filter false
                    CampaignDateUtil.getYearFromDate(date) == currentYear
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private val _isCampaignAlreadyAdded = MutableLiveData(false)
    val isCampaignAlreadyAdded: LiveData<Boolean> = _isCampaignAlreadyAdded

    init {
        viewModelScope.launch {
            try {
                vlfRepo.getPulsePolioCampaignFromServer()
            } catch (e: Exception) {

            }
            checkCampaignEligibility()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkCampaignEligibility() {
        viewModelScope.launch {
            try {
                val currentYear = HelperUtil.getCurrentYear().toInt()
                val list = vlfRepo.getAllPulsePolioCampaigns()
                val currentYearRecords = list.filter {
                    val date = it.campaignDate ?: return@filter false
                    CampaignDateUtil.getYearFromDate(date) == currentYear
                }

                if (currentYearRecords.size >= 2) {
                    _isCampaignAlreadyAdded.postValue(true)
                    return@launch
                }

                val allCampaignDates = list.mapNotNull { it.campaignDate }
                val mostRecentCampaignDate = allCampaignDates.maxByOrNull { dateStr ->
                    CampaignDateUtil.parseDateToLocalDate(dateStr)?.toEpochDay() ?: Long.MIN_VALUE
                }

                if (mostRecentCampaignDate == null) {
                    _isCampaignAlreadyAdded.postValue(false)
                    return@launch
                }

                val canAdd = CampaignDateUtil.isMonthsCompleted(mostRecentCampaignDate, 6)
                _isCampaignAlreadyAdded.postValue(!canAdd)
            } catch (e: Exception) {
                _isCampaignAlreadyAdded.postValue(false)
            }
        }
    }

}

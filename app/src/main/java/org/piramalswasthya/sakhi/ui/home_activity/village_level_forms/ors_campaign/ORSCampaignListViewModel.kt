package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.utils.HelperUtil
import javax.inject.Inject

@HiltViewModel
class ORSCampaignListViewModel @Inject constructor(
    private val vlfRepo: VLFRepo
) : ViewModel() {
    val allORSCampaignList = vlfRepo.orsCampaignList.map { list ->
        val today = java.time.LocalDate.now()
        val threeMonthsAgo = today.minusMonths(3)
        val currentYear = HelperUtil.getCurrentYear().toInt()
        
        list.filter {
            val date = it.campaignDate ?: return@filter false
            val campaignDate = parseDateToLocalDate(date) ?: return@filter false
            
            campaignDate.year == currentYear &&
            (campaignDate.isAfter(threeMonthsAgo) || campaignDate.isEqual(threeMonthsAgo))
        }
    }

    private val _isCampaignAlreadyAdded = MutableLiveData(false)
    val isCampaignAlreadyAdded: LiveData<Boolean> = _isCampaignAlreadyAdded

    init {
        viewModelScope.launch {
            vlfRepo.getORSCampaignFromServer()
            checkCampaignEligibility()
        }
    }

    fun checkCampaignEligibility() {
        viewModelScope.launch {
            try {
                val list = vlfRepo.getAllORSCampaigns()

                val allCampaignDates = list.mapNotNull { it.campaignDate }
                val mostRecentCampaignDate = allCampaignDates.maxByOrNull { dateStr ->
                    parseDateToLocalDate(dateStr)?.toEpochDay() ?: Long.MIN_VALUE
                }

                if (mostRecentCampaignDate == null) {
                    _isCampaignAlreadyAdded.postValue(false)
                    return@launch
                }

                val canAdd = isOneMonthCompleted(mostRecentCampaignDate)
                _isCampaignAlreadyAdded.postValue(!canAdd)
            } catch (e: Exception) {
                _isCampaignAlreadyAdded.postValue(false)
            }
        }
    }

    private fun parseDateToLocalDate(dateStr: String): java.time.LocalDate? {
        return try {
            val dateFormats = listOf(
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd"
            )
            
            for (format in dateFormats) {
                try {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern(format)
                    return java.time.LocalDate.parse(dateStr, formatter)
                } catch (e: Exception) {
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun isOneMonthCompleted(lastDate: String): Boolean {
        return try {
            val last = parseDateToLocalDate(lastDate) ?: return false

            val nextAllowed = last.plusMonths(1)
            val today = java.time.LocalDate.now()

            today.isAfter(nextAllowed) || today.isEqual(nextAllowed)
        } catch (e: Exception) {
            false
        }
    }
}

package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

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
import org.piramalswasthya.sakhi.utils.HelperUtil
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ORSCampaignListViewModel @Inject constructor(
    private val vlfRepo: VLFRepo
) : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    val allORSCampaignList = vlfRepo.orsCampaignList.map { list ->
        try {
            val today = java.time.LocalDate.now()
            val threeMonthsAgo = today.minusMonths(3)
            val currentYear = HelperUtil.getCurrentYear().toInt()
            
            list.filter {
                try {
                    val date = it.campaignDate ?: return@filter false
                    val campaignDate = parseDateToLocalDate(date) ?: return@filter false
                    
                    campaignDate.year == currentYear &&
                    (campaignDate.isAfter(threeMonthsAgo) || campaignDate.isEqual(threeMonthsAgo))
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
                vlfRepo.getORSCampaignFromServer()
            } catch (e: Exception) {

            }
            checkCampaignEligibility()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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

package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio

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
class PulsePolioCampaignListViewModel @Inject constructor(
    private val vlfRepo: VLFRepo
) : ViewModel() {

    val allPulsePolioCampaignList = vlfRepo.pulsePolioCampaignList.map { list ->
        // Filter to show only current year data (1 year)
        val currentYear = HelperUtil.getCurrentYear().toInt()
        list.filter {
            val date = it.campaignDate ?: return@filter false
            getYearFromDate(date) == currentYear
        }
    }

    private val _isCampaignAlreadyAdded = MutableLiveData(false)
    val isCampaignAlreadyAdded: LiveData<Boolean> = _isCampaignAlreadyAdded

    init {
        viewModelScope.launch {
            vlfRepo.getPulsePolioCampaignFromServer()
            checkCampaignEligibility()
        }
    }

    fun checkCampaignEligibility() {
        viewModelScope.launch {
            try {
                val currentYear = HelperUtil.getCurrentYear().toInt()
                val list = vlfRepo.getAllPulsePolioCampaigns()
                val currentYearRecords = list.filter {
                    val date = it.campaignDate ?: return@filter false
                    getYearFromDate(date) == currentYear
                }

                if (currentYearRecords.size >= 2) {
                    _isCampaignAlreadyAdded.postValue(true)
                    return@launch
                }

                val allCampaignDates = list.mapNotNull { it.campaignDate }
                val mostRecentCampaignDate = allCampaignDates.maxByOrNull { dateStr ->
                    parseDateToLocalDate(dateStr)?.toEpochDay() ?: Long.MIN_VALUE
                }

                if (mostRecentCampaignDate == null) {
                    _isCampaignAlreadyAdded.postValue(false)
                    return@launch
                }

                val canAdd = isSixMonthsCompleted(mostRecentCampaignDate)
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

    private fun getYearFromDate(dateStr: String): Int {
        return try {
            val date = parseDateToLocalDate(dateStr)
            if (date != null) {
                return date.year
            }

            if (dateStr.length >= 4 && (dateStr[4] == '-' || dateStr[4] == '/')) {
                dateStr.take(4).toInt()
            } 
            else if (dateStr.length >= 10) {
                val lastSeparatorIndex = maxOf(
                    dateStr.lastIndexOf('-'),
                    dateStr.lastIndexOf('/')
                )
                if (lastSeparatorIndex > 0 && lastSeparatorIndex < dateStr.length - 4) {
                    dateStr.substring(lastSeparatorIndex + 1).toInt()
                } else {
                    0
                }
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun isSixMonthsCompleted(lastDate: String): Boolean {
        return try {
            val last = parseDateToLocalDate(lastDate) ?: return false

            val nextAllowed = last.plusMonths(6)
            val today = java.time.LocalDate.now()

            today.isAfter(nextAllowed) || today.isEqual(nextAllowed)
        } catch (e: Exception) {
            false
        }
    }
}

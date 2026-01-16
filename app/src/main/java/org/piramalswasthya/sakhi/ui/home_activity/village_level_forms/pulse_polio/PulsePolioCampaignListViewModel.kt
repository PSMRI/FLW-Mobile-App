package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.repositories.VLFRepo
import javax.inject.Inject

@HiltViewModel
class PulsePolioCampaignListViewModel @Inject constructor(
    private val vlfRepo: VLFRepo
) : ViewModel() {
    val allPulsePolioCampaignList = vlfRepo.pulsePolioCampaignList

    init {
        viewModelScope.launch {
            vlfRepo.getPulsePolioCampaignFromServer()
        }
    }
}

package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.repositories.VLFRepo
import javax.inject.Inject

@HiltViewModel
class ORSCampaignListViewModel @Inject constructor(
    private val vlfRepo: VLFRepo
) : ViewModel() {
    val allORSCampaignList = vlfRepo.orsCampaignList

    init {
        viewModelScope.launch {
            vlfRepo.getORSCampaignFromServer()
        }
    }
}

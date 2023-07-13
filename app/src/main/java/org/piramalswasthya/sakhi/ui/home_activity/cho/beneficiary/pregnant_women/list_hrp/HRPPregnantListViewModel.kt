package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.list_hrp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenFormList
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@HiltViewModel
class HRPPregnantListViewModel  @javax.inject.Inject
constructor(
    recordsRepo: RecordsRepo,
    val hrpRepo: HRPRepo
) : ViewModel() {
    private val allBenList = recordsRepo.hrpTrackingPregList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterBenFormList(list, filter)
    }

    var allHrpPregTrack : List<HRPPregnantTrackCache>? = null

    init {
        viewModelScope.launch {
            allHrpPregTrack = hrpRepo.getAllPregTrack()
        }
    }
    var benId : Long = 0L


    suspend fun getTrackDetails() : List<HRPPregnantTrackCache>? {
        return hrpRepo.getHrPregTrackList(benId)
    }
    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

}
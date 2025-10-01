package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterPwAncList
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.AllBenFragmentArgs
import javax.inject.Inject
@HiltViewModel
class PwAncVisitsListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recordsRepo: RecordsRepo,
    private val maternalHealthRepo: MaternalHealthRepo
) : ViewModel() {

    private var sourceFromArgs = PwAncVisitsListFragmentArgs.fromSavedStateHandle(savedStateHandle).source

    private val allBenList = when (sourceFromArgs) {
        1 -> recordsRepo.getRegisteredPregnantWomanNonFollowUpList()
        else -> recordsRepo.getRegisteredPregnantWomanList()
    }

    private val allBenListWithHighRisk = recordsRepo.getHighRiskPregnantWomanList()
    private val filter = MutableStateFlow("")
    private val showHighRisk = MutableStateFlow(false)
    val benList: Flow<List<BenWithAncListDomain>> =
        combine(allBenList, allBenListWithHighRisk, showHighRisk, filter) { normalList, highRiskList, isHighRisk, filterText ->
            val list = if (isHighRisk) highRiskList else normalList
            filterPwAncList(list, filterText)
        }

    private val benIdSelected = MutableStateFlow(0L)

    private val _bottomSheetList = benList.combine(benIdSelected) { list, benId ->
        if (benId != 0L)
            list.first { it.ben.benId == benId }.anc
        else
            emptyList()
    }
    val bottomSheetList: Flow<List<AncStatus>> get() = _bottomSheetList

    fun toggleHighRisk(show: Boolean) {
        viewModelScope.launch {
            showHighRisk.emit(show)
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            benIdSelected.emit(benId)
        }
    }
}


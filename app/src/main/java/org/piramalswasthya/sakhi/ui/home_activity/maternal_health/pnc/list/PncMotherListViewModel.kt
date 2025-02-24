package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterPncDomainList
import org.piramalswasthya.sakhi.model.PncDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.PwAncVisitsListFragmentArgs
import javax.inject.Inject

@HiltViewModel
class PncMotherListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recordsRepo: RecordsRepo
) : ViewModel() {

    private var sourceFromArgs = PncMotherListFragmentArgs.fromSavedStateHandle(savedStateHandle).source

    private val allBenList = when (sourceFromArgs) {
        1 -> recordsRepo.pncMotherNonFollowUpList
        else -> recordsRepo.pncMotherList
    }

    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterPncDomainList(list, filter)
    }

    private val benIdSelected = MutableStateFlow(0L)

    private val _bottomSheetList = benList.combine(benIdSelected) { list, benId ->
        if (benId != 0L)
            list.first { it.ben.benId == benId }.savedPncRecords.map { it.asDomainModel() }
        else
            emptyList()
    }

    val bottomSheetList: Flow<List<PncDomain>>
        get() = _bottomSheetList


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
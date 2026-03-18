package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.infant_reg.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterInfantDomainList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class InfantRegListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val onlyLowBirthWeight = savedStateHandle.get<Boolean>("onlyLowBirthWeight") ?: false
    private val allBenList = if (onlyLowBirthWeight)
        recordsRepo.getListForLowWeightInfantReg()
    else
        recordsRepo.getListForInfantReg()
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterInfantDomainList(list, filter)
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

}
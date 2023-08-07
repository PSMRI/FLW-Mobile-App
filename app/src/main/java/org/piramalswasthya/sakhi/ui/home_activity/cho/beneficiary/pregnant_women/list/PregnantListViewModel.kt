package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenFormList
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@HiltViewModel
class PregnantListViewModel @javax.inject.Inject
constructor(
    private val recordsRepo: RecordsRepo
) : ViewModel() {
    private val allBenList = recordsRepo.hrpPregnantWomenList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterBenFormList(list, filter)
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }
}
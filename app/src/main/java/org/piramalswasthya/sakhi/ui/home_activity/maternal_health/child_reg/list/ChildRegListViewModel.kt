package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.child_reg.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.helpers.filterBenFormList
import org.piramalswasthya.sakhi.helpers.sortChildRegList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class ChildRegListViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {
    private val allBenList =
        recordsRepo.getRegisteredInfants()
    private val filter = MutableStateFlow("")
    private val sortFilter = MutableStateFlow(EcFilterType.NEWEST_FIRST)

    val benList = allBenList
        .combine(filter) { list, f -> filterBenFormList(list, f) }
        .combine(sortFilter) { list, sort -> sortChildRegList(list, sort) }

    fun filterText(text: String) {
        viewModelScope.launch { filter.emit(text) }
    }

    fun setSortFilter(type: EcFilterType) {
        viewModelScope.launch { sortFilter.emit(type) }
    }

    fun getCurrentSort(): EcFilterType = sortFilter.value

}
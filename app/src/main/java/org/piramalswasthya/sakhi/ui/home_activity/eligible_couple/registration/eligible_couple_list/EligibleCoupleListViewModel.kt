package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.registration.eligible_couple_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.helpers.filterEcRegistrationList
import org.piramalswasthya.sakhi.helpers.sortEcRegistrationList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class EligibleCoupleListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recordsRepo: RecordsRepo
) : ViewModel() {

    companion object {
        const val SOURCE_DEFAULT = 1
        const val SOURCE_MISSED_PERIOD = 2
    }

    private var sourceFromArgs = EligibleCoupleListFragmentArgs.fromSavedStateHandle(savedStateHandle).source

    private val allBenList = when (sourceFromArgs) {
        SOURCE_MISSED_PERIOD -> recordsRepo.eligibleCoupleMissedPeriodList
        else -> recordsRepo.eligibleCoupleList
    }

    private val filter = MutableStateFlow("")
    private val sortFilter = MutableStateFlow(EcFilterType.NEWEST_FIRST)

    val benList = allBenList
        .combine(filter) { list, f -> filterEcRegistrationList(list, f) }
        .combine(sortFilter) { list, sort -> sortEcRegistrationList(list, sort) }

    fun filterText(text: String) {
        viewModelScope.launch { filter.emit(text) }
    }

    fun setSortFilter(type: EcFilterType) {
        viewModelScope.launch { sortFilter.emit(type) }
    }

    fun getCurrentSort(): EcFilterType = sortFilter.value

}
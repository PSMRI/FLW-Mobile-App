package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.registration.eligible_couple_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterEcRegistrationList
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
    val benList = allBenList.combine(filter) { list, filter ->
        filterEcRegistrationList(list, filter)
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

}
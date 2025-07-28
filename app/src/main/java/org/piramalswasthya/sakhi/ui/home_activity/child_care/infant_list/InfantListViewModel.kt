package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class InfantListViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {

    private val allBenList = recordsRepo.infantList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterBenList(list, filter)
    }

    fun getDobByBenIdAsync(benId: Long, onResult: (Long?) -> Unit) {
        viewModelScope.launch {
            allBenList.collect { list ->
                val dob = list.find { it.benId == benId }?.dob
                onResult(dob)
                return@collect
            }
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }
}
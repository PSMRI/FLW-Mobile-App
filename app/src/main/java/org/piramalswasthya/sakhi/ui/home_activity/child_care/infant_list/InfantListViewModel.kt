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
    private val kind = MutableStateFlow("false")

    val benList = allBenList.combine(kind) { list, kind ->
        if (kind.equals("false", true)) {
            filterBenList(list, false)
        } else {
            filterBenList(list, true)
        }
    }.combine(filter) { list, filter ->
        filterBenList(list, filter)
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    fun filterType(type: String) {
        viewModelScope.launch {
            kind.emit(type)
        }

    }

}
package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_women_registration.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterPwrRegistrationList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class PwRegistrationListViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {
    private val allBenList = recordsRepo.getPregnantWomenList()
    private val allBenWithRchList = recordsRepo.getPregnantWomenWithRchList()
    private val filter = MutableStateFlow("")
    private val kind = MutableStateFlow("false")

    val benList = allBenList.combine(kind) { list, kind ->
        if (kind.equals("false", true)) {
            filterPwrRegistrationList(list, false)
        } else {
            filterPwrRegistrationList(list, true)
        }
    }.combine(filter) { list, filter ->
        filterPwrRegistrationList(list, filter)
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
package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.confirmed.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterMalariaConfirmedList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject
@HiltViewModel
class ConfirmedMalariaListViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {

    val allBenList = recordsRepo.malariaConfirmedCasesList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterMalariaConfirmedList(list, filter)
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }
}
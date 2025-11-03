package org.piramalswasthya.sakhi.ui.home_activity.death_reports.gdr


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenFormList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class GdrListViewModel @Inject constructor( recordsRepo: RecordsRepo
) : ViewModel() {

    private val allBenList = recordsRepo.gdrList
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
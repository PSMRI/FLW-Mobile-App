package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterTbScreeningList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list.MalariaSuspectedListFragmentArgs
import javax.inject.Inject

@HiltViewModel
class TBScreeningListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,

    ) : ViewModel() {


    var hhId = TBScreeningListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = TBScreeningListFragmentArgs.fromSavedStateHandle(savedStateHandle).fromDisease

    val diseaseType = TBScreeningListFragmentArgs.fromSavedStateHandle(savedStateHandle).diseaseType

    private val allBenList = recordsRepo.tbScreeningList(hhId)


    private val filter = MutableStateFlow("")
   /* val benList = allBenList.combine(filter) { list, filter ->
        filterTbScreeningList(list, filter)
    }*/

    val benList = allBenList.combine(filter) { list, filter ->
        val filteredList = filterTbScreeningList(list, filter)
        filteredList
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }
}
package org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.repositories.GeneralCheckupRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class GeneralCheckupListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    generalCheckupRepo: GeneralCheckupRepo,
) : ViewModel() {

    private val filter = MutableStateFlow("")

    private val checkupCounts = generalCheckupRepo.getCheckupCountPerBen()

    val benList = combine(recordsRepo.allBenList, filter, checkupCounts) { list, text, counts ->
        filterBenList(list.filterNot { it.isDeath }, text).map {
            GeneralCheckupListAdapter.Item(it, counts[it.benId] ?: 0)
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch { filter.emit(text) }
    }
}

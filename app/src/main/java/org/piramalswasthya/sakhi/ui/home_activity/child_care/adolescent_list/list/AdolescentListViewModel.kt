package org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterAdolesenctList
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class AdolescentListViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {
    private val allBenList = recordsRepo.adolescentList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterAdolesenctList(list, filter)
    }


    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }
}
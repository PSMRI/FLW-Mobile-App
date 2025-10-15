package org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class ChildListViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {

    private val allBenList = recordsRepo.childList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, query ->
        filterBenList(list, query)
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }

    private fun collectBenList(action: (List<BenBasicDomain>) -> Boolean) {
        viewModelScope.launch {
            allBenList.collect { list ->
                if (action(list)) return@collect
            }
        }
    }

    fun getDobByBenIdAsync(benId: Long, onResult: (Long?) -> Unit) {
        collectBenList { list ->
            list.find { it.benId == benId }?.dob?.let {
                onResult(it)
                true
            } ?: false
        }
    }

    fun getBenById(benId: Long, onResult: (BenBasicDomain?) -> Unit) {
        collectBenList { list ->
            list.find { it.benId == benId }?.let {
                onResult(it)
                true
            } ?: false
        }
    }
}

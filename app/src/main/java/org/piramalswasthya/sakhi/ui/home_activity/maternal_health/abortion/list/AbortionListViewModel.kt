package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.helpers.filterAbortionList
import org.piramalswasthya.sakhi.helpers.sortAbortionList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AbortionListViewModel @Inject constructor(
    private val recordsRepo: RecordsRepo
) : ViewModel() {

    private val selectedYearMonth = MutableStateFlow(getCurrentYearMonth())
    private val searchQuery = MutableStateFlow("")
    private val sortFilter = MutableStateFlow(EcFilterType.NEWEST_FIRST)
    val allAbortionList = recordsRepo.getAbortionPregnantWomanList()
    private val benIdSelected = MutableStateFlow(0L)

    val abortionList = allAbortionList
        .combine(searchQuery) { list, filter -> filterAbortionList(list, filter) }
        .combine(sortFilter) { list, sort -> sortAbortionList(list, sort) }

    fun setYearMonth(year: Int, month: Int) {
        viewModelScope.launch {
            selectedYearMonth.emit(Pair(year, month))
        }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch { searchQuery.emit(query) }
    }

    fun setSortFilter(type: EcFilterType) {
        viewModelScope.launch { sortFilter.emit(type) }
    }

    fun getCurrentSort(): EcFilterType = sortFilter.value

    fun updateSelectedBenId(benId: Long) {
        viewModelScope.launch {
            benIdSelected.emit(benId)
        }
    }

    private fun getCurrentYearMonth(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return Pair(year, month)
    }
}

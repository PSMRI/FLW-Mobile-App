package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterAbortionList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AbortionListViewModel @Inject constructor(
    private val recordsRepo: RecordsRepo
) : ViewModel() {

    // 🔹 Filter state: Year and Month (we use Pair<Int, Int> to support API < 26)
    private val selectedYearMonth = MutableStateFlow(getCurrentYearMonth())
    private val searchQuery = MutableStateFlow("")

    // 🔹 All abortion records from ANC table
    val allAbortionList = recordsRepo.getAbortionPregnantWomanList()

    // 🔹 Filtered & searched abortion list
    // 🔹 Filtered & searched abortion list
//    val abortionList = combine(
//        allAbortionList,
//        selectedYearMonth,
//        searchQuery
//    ) { list, ym, query ->
//
//        val (year, month) = ym
//
//        val filtered = list.filter { item ->
//            item.savedAncRecords.any { anc ->
//                val date = anc.abortionDate
//                if (date != null) {
//                    val cal = Calendar.getInstance().apply { time = Date(date) }
//                    cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month
//                } else false
//            }
//        }
//
//        val searched = filterAbortionList(filtered, query)
//
//        searched.sortedByDescending {
//            it.savedAncRecords.maxOfOrNull { anc -> anc.abortionDate ?: 0L } ?: 0L
//        }
//    }


    private val benIdSelected = MutableStateFlow(0L)

    fun setYearMonth(year: Int, month: Int) {
        viewModelScope.launch {
            selectedYearMonth.emit(Pair(year, month))
        }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            searchQuery.emit(query)
        }
    }

    fun updateSelectedBenId(benId: Long) {
        viewModelScope.launch {
            benIdSelected.emit(benId)
        }
    }

    private fun getCurrentYearMonth(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        return Pair(year, month)
    }
}

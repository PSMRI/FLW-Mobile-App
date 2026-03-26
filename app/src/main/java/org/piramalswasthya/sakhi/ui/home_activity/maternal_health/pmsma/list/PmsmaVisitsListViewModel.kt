package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.PMSMAStatus
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class PmsmaVisitsListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    private val maternalHealthRepo: MaternalHealthRepo
) : ViewModel() {
    private val allBenList: Flow<List<BenWithAncListDomain>> =
        recordsRepo.getRegisteredPmsmaWomenList()
    private val filter = MutableStateFlow("")
    private val benIdSelected = MutableStateFlow<Long?>(null)
    val benList: Flow<List<BenWithAncListDomain>> =
        allBenList.combine(benIdSelected) { list, selectedId ->
            selectedId?.let { id ->
                list.filter { it.ben.benId == id }
            } ?: emptyList()
        }

    private val _bottomSheetList: Flow<List<PMSMAStatus>> =
        benList.map { list ->
            list.firstOrNull()?.pmsma.orEmpty()
        }


    val bottomSheetList: Flow<List<PMSMAStatus>>
        get() = _bottomSheetList

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            benIdSelected.emit(benId)
        }
    }
}

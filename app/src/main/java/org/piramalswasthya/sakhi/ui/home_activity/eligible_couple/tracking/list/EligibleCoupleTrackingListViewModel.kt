package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class EligibleCoupleTrackingListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
//    private var ecrRepo: EcrRepo
) : ViewModel() {

    val scope: CoroutineScope
        get() = viewModelScope

    private val allBenList = recordsRepo.eligibleCoupleTrackingList
    private val filter = MutableStateFlow("")
    private val selectedBenId = MutableStateFlow(0L)
    val benList = allBenList.combine(filter) { list, filter ->
        list.filter { domainList ->
            domainList.ben.benId in filterBenList(
                list.map { it.ben },
                filter
            ).map { it.benId }
        }
    }

    val bottomSheetList = allBenList.combineTransform(selectedBenId) { list, benId ->
        if (benId != 0L) {
            val emitList =
                list.firstOrNull { it.ben.benId == benId }?.savedECTRecords
            if (!emitList.isNullOrEmpty()) emit(emitList.reversed())
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }

    fun setClickedBenId(benId: Long) {
        viewModelScope.launch {
            selectedBenId.emit(benId)
        }

    }


}
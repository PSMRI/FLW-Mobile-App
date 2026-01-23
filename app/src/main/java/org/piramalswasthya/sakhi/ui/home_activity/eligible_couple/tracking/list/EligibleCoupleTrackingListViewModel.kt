package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterEcTrackingList
import org.piramalswasthya.sakhi.model.BenWithEctListDomain
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.list.PncMotherListFragmentArgs
import javax.inject.Inject

@HiltViewModel
class EligibleCoupleTrackingListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recordsRepo: RecordsRepo,
    benRepo: BenRepo
//    private var ecrRepo: EcrRepo
) : ViewModel() {

    private var sourceFromArgs = EligibleCoupleTrackingListFragmentArgs.fromSavedStateHandle(savedStateHandle).source

    val scope: CoroutineScope
        get() = viewModelScope

    private val allBenList = when (sourceFromArgs) {
        1 -> recordsRepo.eligibleCoupleTrackingNonFollowUpList
        2 -> recordsRepo.eligibleCoupleTrackingMissedPeriodList
        else -> recordsRepo.eligibleCoupleTrackingList
    }

    private val filter = MutableStateFlow("")
    private val selectedBenId = MutableStateFlow(0L)


    init {
        viewModelScope.launch {
            allBenList.collect { list ->
                val map = mutableMapOf<Long, String>()

                list.forEach { item ->
                    val ben = item.ben

                    val count = benRepo
                        .getChildBenListFromHousehold(
                            ben.hhId,
                            ben.benId,
                            ben.benName
                        ).size

                    map[ben.benId] = count.toString()
                }

                childrenCountMap.emit(map)
            }
        }
    }
    private val childrenCountMap =
        MutableStateFlow<Map<Long, String>>(emptyMap())
    val benList =
        allBenList
            .combine(filter) { list, filter ->
                list.filter { domainList ->
                    domainList.ben.benId in filterEcTrackingList(
                        list,
                        filter
                    ).map { it.ben.benId }
                }
            }
            .combine(
                childrenCountMap
            ) { list: List<BenWithEctListDomain>,
                countMap: Map<Long, String> ->

                list.map { item ->
                    item.copy(
                        numChildren = countMap[item.ben.benId] ?: "0"
                    )
                }
            }

    val bottomSheetList = allBenList.combineTransform(selectedBenId) { list, benId ->
        if (benId != 0L) {
            val emitList =
                list.firstOrNull { it.ben.benId == benId }?.savedECTRecords?.toMutableList()
                    ?.apply {
                        sortByDescending { it.visited }
                    }
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
package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterPwAncList
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.HomeVisitUiState
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.AllBenFragmentArgs
import javax.inject.Inject
@HiltViewModel
class PwAncVisitsListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recordsRepo: RecordsRepo,
    private val maternalHealthRepo: MaternalHealthRepo
) : ViewModel() {

    private var sourceFromArgs = PwAncVisitsListFragmentArgs.fromSavedStateHandle(savedStateHandle).source
    private val _repo = recordsRepo
    private val allBenList = when (sourceFromArgs) {
        1 -> recordsRepo.getRegisteredPregnantWomanNonFollowUpList()
        else -> recordsRepo.getRegisteredPregnantWomanList()
    }
    private val _homeVisitState = MutableLiveData<Map<Long, HomeVisitUiState>>()
    val homeVisitState: LiveData<Map<Long, HomeVisitUiState>> = _homeVisitState

    private val highRiskBenList = recordsRepo.getHighRiskPregnantWomanList()

    private val filter = MutableStateFlow("")
    private val showHighRisk = MutableStateFlow(false)

    enum class BottomSheetMode { NORMAL, PMSMA }
    private val bottomSheetMode = MutableStateFlow(BottomSheetMode.NORMAL)

    private val benIdSelected = MutableStateFlow(0L)
    private val pmsmaBenIdSelected = MutableStateFlow(0L)

    val benList: Flow<List<BenWithAncListDomain>> = combine(
        allBenList,
        highRiskBenList,
        showHighRisk,
        filter
    ) { normalList, highRiskList, isHighRisk, filterText ->
        val listToShow = if (isHighRisk) highRiskList else normalList
        filterPwAncList(listToShow, filterText)
    }

    val bottomSheetList: Flow<List<AncStatus>> = combine(
        benList,
        highRiskBenList,
        benIdSelected,
        pmsmaBenIdSelected,
        bottomSheetMode
    ) { normalList, highRiskList, benId, pmsmaId, mode ->

        when (mode) {

            BottomSheetMode.NORMAL ->
                normalList
                    .firstOrNull { it.ben.benId == benId }
                    ?.anc
                    .orEmpty()

            BottomSheetMode.PMSMA ->
                highRiskList
                    .firstOrNull { it.ben.benId == pmsmaId }
                    ?.anc
                    ?.filter { anc ->
                        anc.anyHighRisk == true || anc.placeOfAncId == 3
                    }
                    .orEmpty()
        }
    }




    fun loadHomeVisitState(benIds: List<Long>) {
        viewModelScope.launch {
            val map = mutableMapOf<Long, HomeVisitUiState>()

            benIds.forEach { benId ->
                map[benId] = _repo.getHomeVisitUiState(benId)
            }

            _homeVisitState.postValue(map)
        }
    }

    fun toggleHighRisk(show: Boolean) {
        viewModelScope.launch { showHighRisk.emit(show) }
    }

    fun filterText(text: String) {
        viewModelScope.launch { filter.emit(text) }
    }

    fun showAncBottomSheet(benId: Long, mode: BottomSheetMode) {
        viewModelScope.launch {
            bottomSheetMode.emit(mode)
            if (mode == BottomSheetMode.NORMAL) {
                benIdSelected.emit(benId)
            } else {
                pmsmaBenIdSelected.emit(benId)
            }
        }
    }
}

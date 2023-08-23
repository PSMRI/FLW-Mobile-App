package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenFormList
import org.piramalswasthya.sakhi.helpers.getAncStatusList
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PwAncVisitsListViewModel @Inject constructor(
    recordsRepo: RecordsRepo, private val maternalHealthRepo: MaternalHealthRepo
) : ViewModel() {
    private val allBenList = recordsRepo.getRegisteredPregnantWomanList()
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterBenFormList(list, filter)
    }

    private val _bottomSheetList = MutableStateFlow<List<AncStatus>>(emptyList())
    val bottomSheetList: Flow<List<AncStatus>>
        get() = _bottomSheetList


    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            val _list = mutableListOf<AncStatus>()
            val regis = maternalHealthRepo.getSavedRegistrationRecord(benId)!!
            val filledForms = maternalHealthRepo.getAllAncRecords(benId)
            val millisToday = Calendar.getInstance().setToStartOfTheDay().timeInMillis
            val list = getAncStatusList(filledForms, regis.lmpDate, benId, millisToday)
//                listOf(1, 2, 3, 4).map {
//                getAncStatus(filledForms, regis.lmpDate, it, benId, millisToday)
//            }
            Timber.d("list emitted $list")
            _bottomSheetList.emit(emptyList())
            _bottomSheetList.emit(list)
        }
    }


}
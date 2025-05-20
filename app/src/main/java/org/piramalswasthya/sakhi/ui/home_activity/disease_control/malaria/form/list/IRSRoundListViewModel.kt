package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class IRSRoundListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var hhId = MalariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId
    val allBenList = recordsRepo.iRSRoundList(hhId)
    private val benIdSelected = MutableStateFlow(0L)


    /*private val _irsRoundScreening = allBenList.combine(benIdSelected) { list, benId ->
        if (benId != 0L)
            list.first { it.round.id == benId }.round
        else
            emptyList()
    }
    val iRSRoundList: Flow<List<IRSRoundScreening?>>
        get() = _irsRoundScreening*/

}
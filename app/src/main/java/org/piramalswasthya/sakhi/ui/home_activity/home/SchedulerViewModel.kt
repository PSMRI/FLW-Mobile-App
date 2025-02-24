package org.piramalswasthya.sakhi.ui.home_activity.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    maternalHealthRepo: MaternalHealthRepo,
    recordsRepo: RecordsRepo
) : ViewModel() {
    enum class State {
        LOADING,
        LOADED,
    }

    private val _state = MutableLiveData(State.LOADING)
    val state: LiveData<State>
        get() = _state

    val ancDueCount: Flow<Int> = maternalHealthRepo.ancDueCount

    val ancNonFollowUpCount: Flow<Int> = recordsRepo.getRegisteredPregnantWomanNonFollowUpListCount()

    val pncNonFollowUpCount: Flow<Int> = recordsRepo.pncMotherNonFollowUpListCount

    val ecNonFollowUpCount: Flow<Int> = recordsRepo.eligibleCoupleTrackingNonFollowUpListCount

    val hrpDueCount: Flow<Int> = recordsRepo.hrpTrackingPregListCount

    val hrpCountEC: Flow<Int> = recordsRepo.hrpTrackingNonPregListCount

    val immunizationDue: Flow<Int> = recordsRepo.childrenImmunizationDueListCount

    val lowWeightBabiesCount: Flow<Int> = recordsRepo.lowWeightBabiesCount

    val abhaGeneratedCount: Flow<Int> = recordsRepo.benWithAbhaListCount

    val rchIdCount: Flow<Int> = recordsRepo.benWithRchListCount

    private val _date = MutableLiveData(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    )
    val date: LiveData<Long>
        get() = _date

    fun setDate(dateLong: Long) {
        _date.value = dateLong
        _state.value = State.LOADING
        viewModelScope.launch {
            delay(500)
            updateData()
        }
    }

    init {
        viewModelScope.launch {
            updateData()

        }
    }

    private suspend fun updateData() {
        _state.value = State.LOADED
    }


}
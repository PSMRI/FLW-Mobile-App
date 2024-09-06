package org.piramalswasthya.sakhi.ui.home_activity.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val maternalHealthRepo: MaternalHealthRepo,
    private val recordsRepo: RecordsRepo
) : ViewModel() {
    enum class State {
        LOADING,
        LOADED,
    }

    private val _state = MutableLiveData(State.LOADING)
    val state: LiveData<State>
        get() = _state

    private val _ancDueCount = MutableLiveData<Int>()
    val ancDueCount: LiveData<Int>
        get() = _ancDueCount

    private val _pncDueCount = MutableLiveData<Int>()
    val pncDueCount: LiveData<Int>
        get() = _pncDueCount

    private val _date = MutableLiveData<Long>()
    val date: LiveData<Long>
        get() = _date

    init {
        _date.value = Calendar.getInstance().timeInMillis
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {

            val ancCount: Flow<Int> = maternalHealthRepo.getAncDueCount(date.value!!)
            ancCount.collectLatest {
                _ancDueCount.value = it
            }

            val pncCount: Flow<Int> = recordsRepo.pncMotherListCount
            pncCount.collectLatest {
                _ancDueCount.value = it
            }
        }
    }

    val pwImmunizationDueCount: Flow<Int> = recordsRepo.motherImmunizationListCount

    val childImmunizationDueCount: Flow<Int> = recordsRepo.childrenImmunizationDueListCount

    val hrpDueCount: Flow<Int> = recordsRepo.hrpTrackingPregListCount

    val hrpCountEC: Flow<Int> = recordsRepo.hrpTrackingNonPregListCount

    val lowWeightBabiesCount: Flow<Int> = recordsRepo.lowWeightBabiesCount

//    private val _date = MutableLiveData(
//        Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//        }.timeInMillis
//    )

//    val date: LiveData<Long>
//        get() = _date

    fun setDate(dateLong: Long) {
        _date.value = dateLong
        _state.value = State.LOADING
        fetchData()
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
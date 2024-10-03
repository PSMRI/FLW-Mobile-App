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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val maternalHealthRepo: MaternalHealthRepo,
    private val recordsRepo: RecordsRepo,
    private val preferenceDao: PreferenceDao
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


    private val _ancOneCount = MutableLiveData<Int>()
    val ancOneCount: LiveData<Int>
        get() = _ancOneCount

    private val _ancTwoCount = MutableLiveData<Int>()
    val ancTwoCount: LiveData<Int>
        get() = _ancTwoCount


    private val _ancThreeCount = MutableLiveData<Int>()
    val ancThreeCount: LiveData<Int>
        get() = _ancThreeCount

    private val _ancFourCount = MutableLiveData<Int>()
    val ancFourCount: LiveData<Int>
        get() = _ancFourCount

    private val _pncDueCount = MutableLiveData<Int>()
    val pncDueCount: LiveData<Int>
        get() = _pncDueCount

    private val _date = MutableLiveData<Long>()
    val date: LiveData<Long>
        get() = _date

    init {
        _date.value = Calendar.getInstance().timeInMillis
        fetchData()
        fetchAncOne()
        fetchAncTwo()
        fetchAncThree()
        fetchAncFour()
    }

    val today = Calendar.getInstance()
    val thisYear = today.get(Calendar.YEAR)
    val thisMonth = today.get(Calendar.MONTH)
    val thisDay = today.get(Calendar.DAY_OF_MONTH)
    fun saveSelectedDay(day: Int) {
        preferenceDao.saveSelectedDay(day)
    }

     val getSelectedDate = preferenceDao.getSelectedDay()


    private fun fetchAncOne() {
        viewModelScope.launch {
            val ancOneCount: Flow<Int> = maternalHealthRepo.getAncOneDueCount(date.value!!)
            ancOneCount.collectLatest {
                _ancOneCount.value = it
            }
        }
    }
    private fun fetchAncTwo() {
        viewModelScope.launch {
            val ancTwoCount: Flow<Int> = maternalHealthRepo.getAncTwoDueCount(date.value!!)
            ancTwoCount.collectLatest {
                _ancTwoCount.value = it
            }
        }
    }

    private fun fetchAncThree() {
        viewModelScope.launch {
            val ancThreeCount: Flow<Int> = maternalHealthRepo.getAncThreeDueCount(date.value!!)
            ancThreeCount.collectLatest {
                _ancThreeCount.value = it
            }

        }
    }

    private fun fetchAncFour() {
        viewModelScope.launch {
            val ancfourCount: Flow<Int> = maternalHealthRepo.getAncFourDueCount(date.value!!,)
            ancfourCount.collectLatest {
                _ancFourCount.value = it
            }

        }
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

    val hrpPregnantWomenListCount: Flow<Int> = recordsRepo.hrpPregnantWomenListCount

    val eligibleCoupleListCount: Flow<Int> = recordsRepo.eligibleCoupleListCount

    val ncdEligiblelListCount: Flow<Int> = recordsRepo.getNcdEligibleListCount


    val tbScreeningListCount: Flow<Int> = recordsRepo.tbScreeningListCount

    val pregnentWomenListCount: Flow<Int> = recordsRepo.getPregnantWomenListCount()


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
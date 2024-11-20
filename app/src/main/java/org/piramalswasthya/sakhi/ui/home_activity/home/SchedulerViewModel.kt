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

    private val _pwImmunizationDueCount = MutableLiveData<Int>()
    val pwImmunizationDueCount: LiveData<Int>
        get() = _pwImmunizationDueCount

    private val _childImmunizationDueCount = MutableLiveData<Int>()
    val childImmunizationDueCount: LiveData<Int>
        get() = _childImmunizationDueCount

    private val _hbncDueCount = MutableLiveData<Int>()
    val hbncDueCount: LiveData<Int>
        get() = _hbncDueCount

    private val _hbycDueCount = MutableLiveData<Int>()
    val hbycDueCount: LiveData<Int>
        get() = _hbycDueCount

    private val _pncDueCount = MutableLiveData<Int>()
    val pncDueCount: LiveData<Int>
        get() = _pncDueCount

    private val _ecDueCount = MutableLiveData<Int>()
    val ecDueCount: LiveData<Int>
        get() = _ecDueCount

    private val _vhsndDueCount = MutableLiveData<Int>()
    val vhsndDueCount: LiveData<Int>
        get() = _vhsndDueCount

    private val _tdDueCount = MutableLiveData<Int>()
    val tdDueCount: LiveData<Int>
        get() = _tdDueCount

    private val _childrenImmunizationCount = MutableLiveData<Int>()
    val childrenImmunizationCount: LiveData<Int>
        get() = _childrenImmunizationCount

    private val _pwAncCount = MutableLiveData<Int>()
    val pwAncCount: LiveData<Int>
        get() = _pwAncCount

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
        }

        viewModelScope.launch {
            val pwImmunizationCount: Flow<Int> = recordsRepo.getPwImmunizationDueCount(date.value!!)
            pwImmunizationCount.collectLatest {
                _pwImmunizationDueCount.value = it
            }
        }

        viewModelScope.launch {
            val childImmunizationCount: Flow<Int> = recordsRepo.getChildrenImmunizationDueCount(date.value!!)
            childImmunizationCount.collectLatest {
                _childImmunizationDueCount.value = it
            }
        }

        viewModelScope.launch {
            val hbncCount: Flow<Int> = recordsRepo.getHbncDueCount(date.value!!)
            hbncCount.collectLatest {
                _hbncDueCount.value = it
            }
        }

        viewModelScope.launch {
            val hbycCount: Flow<Int> = recordsRepo.getHbycDueCount(date.value!!)
            hbycCount.collectLatest {
                _hbycDueCount.value = it
            }
        }

        viewModelScope.launch {
            val pncCount: Flow<Int> = recordsRepo.getPncDueCount(date.value!!)
            pncCount.collectLatest {
                _pncDueCount.value = it
            }
        }

        viewModelScope.launch {
            val ecCount: Flow<Int> = recordsRepo.getEcDueCount(date.value!!)
            ecCount.collectLatest {
                _ecDueCount.value = it
            }
        }

        val nextWed = getNextWednesdayInMillis(date.value!!)

        viewModelScope.launch {
            val tdCount: Flow<Int> = recordsRepo.getTdDueCount(nextWed)
            tdCount.collectLatest {
                _tdDueCount.value = it
            }
        }

        viewModelScope.launch {
            val childrenImmunizationCount: Flow<Int> = recordsRepo.getChildrenImmunizationDueCount(nextWed)
            childrenImmunizationCount.collectLatest {
                _childrenImmunizationCount.value = it
            }
        }

        viewModelScope.launch {
            val pwAncCount: Flow<Int> = recordsRepo.getPwAncDueCount(nextWed)
            pwAncCount.collectLatest {
                _pwAncCount.value = it
            }
        }

    }

    fun getNextWednesdayInMillis(currentMillis: Long): Long {
        // Create a Calendar instance and set it to the given timestamp
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentMillis
        }

        // Find the current day of the week
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Calculate how many days until the next Wednesday
        val daysUntilWednesday = if (currentDayOfWeek <= Calendar.WEDNESDAY) {
            Calendar.WEDNESDAY - currentDayOfWeek
        } else {
            7 - (currentDayOfWeek - Calendar.WEDNESDAY)
        }

        // Add the days to the current time
        calendar.add(Calendar.DAY_OF_MONTH, daysUntilWednesday)

        // Return the time in milliseconds
        return calendar.timeInMillis
    }

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
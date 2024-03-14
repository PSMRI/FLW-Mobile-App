package org.piramalswasthya.sakhi.ui.home_activity.get_ben_data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.repositories.BenRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GetBenViewModel @Inject constructor(private val benRepo: BenRepo) : ViewModel() {
    enum class State {
        IDLE,
        LOADING,
        ERROR_SERVER,
        ERROR_NETWORK,
        SUCCESS
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private var _numPages: Int = 0
    val numPages: Int
        get() = _numPages

    private lateinit var _benDataList: List<BenBasicDomain>
    val benDataList: List<BenBasicDomain>
        get() = _benDataList

    fun getBeneficiaries(pageNumber: Int) {
        viewModelScope.launch {
            _state.value = State.LOADING
            val paired = benRepo.getBeneficiariesFromServer(pageNumber)
            Timber.d("paired : $paired")
            val list = paired.second
            if (list.isNotEmpty()) {
                _benDataList = list
                _numPages = paired.first
                _state.value = State.SUCCESS
            } else
                _state.value = State.ERROR_SERVER

        }
    }

    init {
        getBeneficiaries(0)
    }
}
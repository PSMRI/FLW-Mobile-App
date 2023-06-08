package org.piramalswasthya.sakhi.ui.home_activity.all_ben

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class AllBenViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {


    private val allBenList = recordsRepo.getBenList()
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter){
       list, filter -> filterBenList(list, filter)
    }


    @Inject
    lateinit var benRepo:BenRepo

    @Inject
    lateinit var amritApiService: AmritApiService

    private val _abha = MutableLiveData<String?>()
    val abha: LiveData<String?>
        get() = _abha

    private val _benRegId = MutableLiveData<Long?>()
    val benRegId: LiveData<Long?>
        get() = _benRegId

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    fun fetchAbha(benId: Long) {
        _abha.value = null
        _benRegId.value = null
        viewModelScope.launch {
            val result = benRepo.getBeneficiaryWithId(benId)
            result?.let {
                if (it.abhaDetails != null) {
                    if (it.abhaDetails.isNotEmpty()) {
                        _abha.value = it.abhaDetails.first().HealthIDNumber
                    } else {
                        _benRegId.value = it.benRegId
                    }
                } else {
                    _benRegId.value = it.benRegId
                }
            }
        }
    }

    fun resetBenRegId() {
        _benRegId.value = null
    }
}
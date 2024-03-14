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
import org.piramalswasthya.sakhi.model.BenHealthIdDetails
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class AllBenViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    private val benRepo: BenRepo
) : ViewModel() {


    private val allBenList = recordsRepo.allBenList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterBenList(list, filter)
    }

    private val _abha = MutableLiveData<String?>()
    val abha: LiveData<String?>
        get() = _abha

    private val _benId = MutableLiveData<Long?>()
    val benId: LiveData<Long?>
        get() = _benId

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
        _benId.value = benId
        viewModelScope.launch {
            benRepo.getBenFromId(benId)?.let {
                val result = benRepo.getBeneficiaryWithId(it.benRegId)
                if (result != null) {
                    _abha.value = result.healthIdNumber
                    it.healthIdDetails = BenHealthIdDetails(result.healthId, result.healthIdNumber)
                    benRepo.updateRecord(it)
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
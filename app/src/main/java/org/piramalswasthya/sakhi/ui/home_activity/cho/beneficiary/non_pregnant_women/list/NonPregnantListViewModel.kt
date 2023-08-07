package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenFormList
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class NonPregnantListViewModel @Inject
constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {


    private val allBenList = recordsRepo.hrpNonPregnantWomenList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterBenFormList(list, filter)
    }


    @Inject
    lateinit var benRepo: BenRepo

    @Inject
    lateinit var amritApiService: AmritApiService

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

}
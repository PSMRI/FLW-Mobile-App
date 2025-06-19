package org.piramalswasthya.sakhi.ui.home_activity.general_op_care
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject
@HiltViewModel
class GeneralOpCareViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recordsRepo: RecordsRepo,
    private val benRepo: BenRepo
) : ViewModel() {



   var allBenList = recordsRepo.allBenList

    private val filter = MutableStateFlow("")
    private val kind = MutableStateFlow(0)

    val benList = allBenList.combine(kind) { list, kind ->
        filterBenList(list, kind)
    }.combine(filter) { list, filter ->
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

    fun filterType(type: Int) {
        viewModelScope.launch {
            kind.emit(type)
        }

    }

    suspend fun getBenFromId(benId: Long):Long{
        var benRegId = 0L
        val result = benRepo.getBenFromId(benId)
        if (result != null) {
            benRegId = result.benRegId
        }
        return benRegId
    }

}
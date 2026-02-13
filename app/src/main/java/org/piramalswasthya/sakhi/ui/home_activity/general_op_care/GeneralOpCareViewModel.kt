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
import org.piramalswasthya.sakhi.database.room.dao.GeneralOpdDao
import org.piramalswasthya.sakhi.helpers.filterOPDBenList
import org.piramalswasthya.sakhi.repositories.BenRepo
import javax.inject.Inject
@HiltViewModel
class GeneralOpCareViewModel @Inject constructor(
    private val benRepo: BenRepo,
    private val generalOpdDao: GeneralOpdDao,
) : ViewModel() {



   var allBenList = generalOpdDao.getAll()

    private val filter = MutableStateFlow("")
    private val kind = MutableStateFlow("")

    val benList = allBenList.combine(kind) { list, kind ->
        filterOPDBenList(list, kind)
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

    fun filterType(type: String) {
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
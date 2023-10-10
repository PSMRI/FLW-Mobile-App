package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.list_hrp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenFormList
import org.piramalswasthya.sakhi.helpers.filterBenHRNPTFormList
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HRPNonPregnantListViewModel @Inject
constructor(
    recordsRepo: RecordsRepo,
    private val hrpRepo: HRPRepo
) : ViewModel() {

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

    var allHRNonPregTrack: List<HRPNonPregnantTrackCache>? = null

    private val allBenList = recordsRepo.hrpTrackingNonPregList
    private val filter = MutableStateFlow("")
    val benList = allBenList.combine(filter) { list, filter ->
        filterBenHRNPTFormList(list, filter)
    }

    init {
        viewModelScope.launch {
            allHRNonPregTrack = hrpRepo.getAllNonPregTrack()
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }

    fun setBenId(benId: Long) {
        _benId.value = benId
    }

    suspend fun getTrackDetails(): List<HRPNonPregnantTrackCache>? {
        Timber.d("ben value " + _benId.value)
        return _benId.value?.let { hrpRepo.getHrNonPregTrackList(it) }
    }

    suspend fun updateBenWithForms(ben: BenBasicDomainForForm) {
        hrpRepo.getHrNonPregTrackList(ben.benId)?.let {
            val cal = Calendar.getInstance()
            if (it.isNotEmpty()) {
                it.first().visitDate?.let { date ->
                    cal.timeInMillis = date
                    ben.form1Enabled =
                        cal.get(Calendar.MONTH) != Calendar.getInstance().get(Calendar.MONTH)
                }
            }
        }
    }

}
package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.PMSMAFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.PmsmaRepo
import javax.inject.Inject

@HiltViewModel
class PmsmaViewModel @Inject constructor(
    state: SavedStateHandle,
    @ApplicationContext context: Context,
    private val pmsmaRepo: PmsmaRepo,
    private val benRepo: BenRepo,
    maternalHealthRepo: MaternalHealthRepo,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    enum class State { IDLE, LOADING, SUCCESS, FAIL }

    private val benId = PmsmaFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = PmsmaFragmentArgs.fromSavedStateHandle(state).hhId
    private val visitNumber = PmsmaFragmentArgs.fromSavedStateHandle(state).visitNumber
    val lastItemClick = PmsmaFragmentArgs.fromSavedStateHandle(state).lastItemClick

    private val _showHighRiskAlert = MutableLiveData<Boolean>()
    val showHighRiskAlert: LiveData<Boolean> get() = _showHighRiskAlert

    fun onHighRiskSelected(isHighRisk: Boolean) {
        if (isHighRisk) _showHighRiskAlert.value = true
    }
    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean> get() = _recordExists

    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }

    private val dataset = PMSMAFormDataset(context, preferenceDao.getCurrentLanguage(),this)
    val formList = dataset.listFlow

    private lateinit var ben: BenRegCache
    private lateinit var pwr: PregnantWomanRegistrationCache
    private var pmsma: PMSMACache? = null

    init {
        viewModelScope.launch {
            ben = benRepo.getBeneficiaryRecord(benId, hhId) ?: return@launch
            val household = benRepo.getHousehold(hhId) ?: return@launch
            pmsma = pmsmaRepo.getSavedRecord(benId, visitNumber)
            val lastPmsmaVisit = pmsmaRepo.getLastPmsmaVisit(benId)
            val countOfANC=pmsmaRepo.getActiveAncCountForBenIds(benId)
            pwr = maternalHealthRepo.getSavedRegistrationRecord(benId) ?: return@launch
            val lastAnc = maternalHealthRepo.getLatestAncRecord(benId)

            _benName.value = "${ben.firstName} ${ben.lastName ?: ""}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            _recordExists.value = pmsma != null

            dataset.setUpFirstPage(
                household,
                ben,
                pwr,
                lastAnc,
                pmsma,
                visitNumber,
                countOfANC,
                lastPmsmaVisit
            )
        }
    }

    fun submitForm() {
        _state.value = State.LOADING
        val user = preferenceDao.getLoggedInUser()!!

        val pmsmaCache = pmsma?.apply {
            updatedBy = user.name
            processed = "U"
            updatedDate = System.currentTimeMillis()
            syncState = SyncState.UNSYNCED
            dataset.mapValues(this)
        } ?: PMSMACache(
            benId = benId,
            processed = "N",
            createdBy = user.name,
            updatedBy = user.name,
            syncState = SyncState.UNSYNCED,
            isActive = true,
            visitNumber = visitNumber
        ).also { dataset.mapValues(it) }

        viewModelScope.launch {
            val saved = pmsmaRepo.savePmsmaData(pmsmaCache)
            _state.value = if (saved) State.SUCCESS else State.FAIL
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }
}

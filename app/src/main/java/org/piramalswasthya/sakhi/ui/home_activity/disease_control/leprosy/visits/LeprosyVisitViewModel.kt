package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.visits

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.LeprosyConfirmedDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.LeprosyRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.form.LeprosyFormFragmentArgs
import javax.inject.Inject

@HiltViewModel
class LeprosyVisitViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext var context: Context,
    private val leprosyRepo: LeprosyRepo,
    private val benRepo: BenRepo,
    var maternalHealthRepo: MaternalHealthRepo
) : ViewModel() {
    var isDeath = true
    val benId = LeprosyFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    val visitNumber = LeprosyVisitFragmentArgs.fromSavedStateHandle(savedStateHandle).visitNumber


    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED, VISIT_COMPLETED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
    get() = _state

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
    get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
    get() = _benAgeGender

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
    get() = _recordExists


    private val _visitInfo = MutableLiveData<String>()
    val visitInfo: LiveData<String>
    get() = _visitInfo

    private val _followUpDates = MutableLiveData<List<LeprosyFollowUpCache>>()
    val followUpDates: LiveData<List<LeprosyFollowUpCache>>
    get() = _followUpDates

    private val _lastFollowUp = MutableLiveData<LeprosyFollowUpCache?>()
    val lastFollowUp: LiveData<LeprosyFollowUpCache?>
    get() = _lastFollowUp

    private val username = preferenceDao.getLoggedInUser()?.userName ?: ""


    private val dataset = LeprosyConfirmedDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    var _isBeneficaryStatusDeath = MutableLiveData<Boolean>()

    val isBeneficaryStatusDeath: LiveData<Boolean>
    get() = _isBeneficaryStatusDeath
    private lateinit var leprosyScreenCache: LeprosyScreeningCache
    private lateinit var screeningData: LeprosyScreeningCache
    private lateinit var screening : LeprosyScreeningCache


    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                leprosyScreenCache = LeprosyScreeningCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId,
                    createdBy = username,
                    modifiedBy = username
                )
            }
            val screeningRecord = leprosyRepo.getLeprosyScreening(benId)
            if (screeningRecord == null) {
                _state.value = State.SAVE_FAILED
                return@launch
            }
            screening = screeningRecord
            screeningData = screeningRecord
            leprosyScreenCache = screening
            screeningData = screening
            _recordExists.value = true
            _isBeneficaryStatusDeath.value = screening.beneficiaryStatus.equals("Death", ignoreCase = true)

            val currentVisitNumber = screening.currentVisitNumber
            _visitInfo.value = "Visit - $currentVisitNumber"

            val followUpsForVisit = leprosyRepo.getFollowUpsForVisit(benId, visitNumber)
            _followUpDates.value = followUpsForVisit

            val latestFollowUp = followUpsForVisit.maxByOrNull { it.followUpDate }
            _lastFollowUp.value = latestFollowUp

            if (ben != null) {
                dataset.setUpPage(
                    ben = ben,
                    followUp = latestFollowUp
                )
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }
    private fun getLastFollowUpForCurrentVisit(
        followUps: List<LeprosyFollowUpCache>,
        currentVisitNumber: Int
    ): LeprosyFollowUpCache? {
        return followUps
            .filter { it.visitNumber == currentVisitNumber }
            .maxByOrNull { it.followUpDate }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun getIndexOfDate(): Int {
        return dataset.getIndexOfDate()
    }
    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }


}
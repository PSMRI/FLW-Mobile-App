package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.assess

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.HRPNonPregnantAssessDataset
import org.piramalswasthya.sakhi.configuration.HRPPregnantAssessDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.assess.HRPNonPregnantAssessFragmentArgs
import org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.form.EligibleCoupleTrackingFormFragmentArgs
import timber.log.Timber

@HiltViewModel
class HRPNonPregnantAssessViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val hrpReo: HRPRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        HRPNonPregnantAssessFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
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

    //    private lateinit var user: UserDomain
    private val dataset =
        HRPNonPregnantAssessDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isHighRisk: Boolean = false

    private lateinit var hrpNonPregnantAssess: HRPNonPregnantAssessCache

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                hrpNonPregnantAssess = HRPNonPregnantAssessCache(
                    benId = ben.beneficiaryId,
                )
            }

            hrpReo.getNonPregnantAssess(benId)?.let {
                hrpNonPregnantAssess = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) hrpNonPregnantAssess else null
            )

        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

//    fun getIndexOfEdd(): Int = dataset.getIndexOfEdd()
//    fun getIndexOfWeeksOfPregnancy(): Int = dataset.getIndexOfWeeksPregnancy()
//    fun getIndexOfPastIllness(): Int = dataset.getIndexOfPastIllness()

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(hrpNonPregnantAssess, 1)
                    hrpNonPregnantAssess.isHighRisk = isHighRiskStatus()
                    hrpReo.saveRecord(hrpNonPregnantAssess)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PWR data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun getIndexOfChildLabel() = dataset.getIndexOfChildLabel()

    fun getIndexOfPhysicalObservationLabel() = dataset.getIndexOfPhysicalObservationLabel()

    fun getIndexOfObstetricHistoryLabel() = dataset.getIndexOfObstetricHistoryLabel()

    private fun isHighRiskStatus(): Boolean {
        return dataset.isHighRisk()
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}
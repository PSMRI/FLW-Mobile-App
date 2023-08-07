package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.micro_birth_plan

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.HRPMicroBirthPlanDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import timber.log.Timber

@HiltViewModel
class HRPMicroBirthPlanViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val hrpReo: HRPRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId = HRPMicroBirthPlanFragmentArgs
        .fromSavedStateHandle(savedStateHandle).benId

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
        HRPMicroBirthPlanDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isHighRisk: Boolean = false

    private lateinit var microBirthPlanCache: HRPMicroBirthPlanCache

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                microBirthPlanCache = HRPMicroBirthPlanCache(
                    benId = ben.beneficiaryId,
                )
            }

            hrpReo.getMicroBirthPlan(benId)?.let {
                microBirthPlanCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) microBirthPlanCache else null
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
            try {
                _state.postValue(State.SAVING)

                dataset.mapValues(microBirthPlanCache, 1)
                hrpReo.saveRecord(microBirthPlanCache)
                isHighRisk = true
                if (isHighRisk) {
                    // save
                }

                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("saving PWR data failed!!")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}
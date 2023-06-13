package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.form

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.EligibleCoupleTrackingDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.EligibleCoupleTracking
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EligibleCoupleTrackingFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        EligibleCoupleTrackingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
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
        EligibleCoupleTrackingDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    var isPregnant: Boolean = false

    private lateinit var eligibleCoupleTracking: EligibleCoupleTracking

    init {
        viewModelScope.launch {
            val ben = maternalHealthRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                eligibleCoupleTracking = EligibleCoupleTracking(
                    benId = ben.beneficiaryId,
                )
            }

            maternalHealthRepo.getEct(benId)?.let {
                eligibleCoupleTracking = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) eligibleCoupleTracking else null
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

                    dataset.mapValues(eligibleCoupleTracking, 1)
                    maternalHealthRepo.saveEct(eligibleCoupleTracking)
                    isPregnant = (eligibleCoupleTracking.isPregnant == "Yes") ||
                        (eligibleCoupleTracking.pregnancyTestResult == "Positive")
                    if (isPregnant) {
                        maternalHealthRepo.getBenFromId(benId)?.let{
                            dataset.updateBen(it)
                            benRepo.persistRecord(it)
                        }
                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PWR data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}
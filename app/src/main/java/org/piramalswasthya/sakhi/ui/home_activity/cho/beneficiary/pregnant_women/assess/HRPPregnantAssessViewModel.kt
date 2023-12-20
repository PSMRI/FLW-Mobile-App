package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.assess

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.HRPPregnantAssessDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import timber.log.Timber

@HiltViewModel
class HRPPregnantAssessViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val hrpRepo: HRPRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        HRPPregnantAssessFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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

    private val _fullyFilled = MutableLiveData<Boolean>()
    val fullyFilled: LiveData<Boolean>
        get() = _fullyFilled

    //    private lateinit var user: UserDomain
    private val dataset =
        HRPPregnantAssessDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var hrpPregnantAssessCache: HRPPregnantAssessCache
    private var ben: BenRegCache? = null

    init {
        viewModelScope.launch {
            ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                hrpPregnantAssessCache = HRPPregnantAssessCache(
                    benId = ben.beneficiaryId,
                )
            }

            hrpRepo.getPregnantAssess(benId)?.let {
                hrpPregnantAssessCache = it
                _recordExists.value = true
                _fullyFilled.value = (
                        it.noOfDeliveries != null
                                && it.timeLessThan18m != null
                                && it.heightShort != null
                                && it.age != null
                                && it.rhNegative != null
                                && it.homeDelivery != null
                                && it.badObstetric != null
                                && it.multiplePregnancy != null)
            } ?: run {
                _recordExists.value = false
                _fullyFilled.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) hrpPregnantAssessCache else null
            )


        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(hrpPregnantAssessCache, 1)
                    hrpPregnantAssessCache.isHighRisk = isHighRisk()
                    hrpRepo.saveRecord(hrpPregnantAssessCache)
                    ben?.let {
                        dataset.updateBen(it)
                        benRepo.updateRecord(it)
                    }
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

    fun getIndexOfEdd() = dataset.getIndexOfEdd()

    fun isHighRisk(): Boolean {
        return dataset.isHighRisk()
    }

    fun resetState() {
        _state.value = State.IDLE
    }

}
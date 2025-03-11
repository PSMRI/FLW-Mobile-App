package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.pregnant_women.micro_birth_plan

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.HRPMicroBirthPlanDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
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

    private var _benDetails =BenRegGen()
    val benDetails: BenRegGen
        get() = _benDetails

    val currentUser = preferenceDao.getLoggedInUser()

    val currentLocation = preferenceDao.getLocationRecord()

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

    lateinit var _microBirthPlanCache: HRPMicroBirthPlanCache
    var _hRPPregnantAssessCache: HRPPregnantAssessCache?=null

//    private val _readRecord = MutableLiveData(benId > 0)
//    val readRecord: LiveData<Boolean>
//        get() = _readRecord
    fun setRecordExists(b: Boolean) {
        _recordExists.value = b


    }

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                _benDetails= ben.genDetails!!
                _microBirthPlanCache = HRPMicroBirthPlanCache(
                    benId = ben.beneficiaryId,
                )
            }
            _hRPPregnantAssessCache = HRPPregnantAssessCache(benId = benId)
            _hRPPregnantAssessCache= hrpReo.getPregnantAssess(benId)

            hrpReo.getMicroBirthPlan(benId)?.let {
                _microBirthPlanCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) _microBirthPlanCache else null
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
            try {
                _state.postValue(State.SAVING)

                dataset.mapValues(_microBirthPlanCache, 1)
                _microBirthPlanCache.processed = "U"
                _microBirthPlanCache.syncState = SyncState.UNSYNCED
                hrpReo.saveRecord(_microBirthPlanCache)
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
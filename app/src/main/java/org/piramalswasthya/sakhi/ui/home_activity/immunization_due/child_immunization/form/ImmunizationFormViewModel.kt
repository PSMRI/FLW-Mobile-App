package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form

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
import org.piramalswasthya.sakhi.configuration.ImmunizationDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ImmunizationCache
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImmunizationFormViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferenceDao: PreferenceDao,
    savedStateHandle: SavedStateHandle,
    private val vaccineDao: ImmunizationDao,
    benDao: BenDao,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val benId = ImmunizationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
    private val vaccineId =
        ImmunizationFormFragmentArgs.fromSavedStateHandle(savedStateHandle).vaccineId

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender

    private val dataset = ImmunizationDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    private lateinit var immCache: ImmunizationCache

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val asha = preferenceDao.getLoggedInUser()!!
                val savedRecord = vaccineDao.getImmunizationRecord(benId, vaccineId)
                immCache = savedRecord?.also { _recordExists.postValue(true) } ?: run {
                    ImmunizationCache(
                        beneficiaryId = benId,
                        vaccineId = vaccineId,
                        createdBy = asha.userName,
                        updatedBy = asha.userName,
                        syncState = SyncState.UNSYNCED
                    )
                }.also { _recordExists.postValue(false) }
                val ben = benDao.getBen(benId)!!
                _benName.postValue("${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}")
                _benAgeGender.postValue("${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}")
                val vaccine = vaccineDao.getVaccineById(vaccineId)
                    ?: throw IllegalStateException("Unknown Vaccine Injected, contact HAZMAT team!")
                dataset.setFirstPage(ben, vaccine, savedRecord)
            }
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(immCache, 1)
                    vaccineDao.addImmunizationRecord(immCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving PW-ANC data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    fun updateRecordExists(b: Boolean) {
        _recordExists.value = b

    }


}
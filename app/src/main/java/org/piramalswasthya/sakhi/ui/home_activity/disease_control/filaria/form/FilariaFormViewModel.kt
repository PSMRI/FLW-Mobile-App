package org.piramalswasthya.sakhi.ui.home_activity.disease_control.filaria.form

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
import org.piramalswasthya.sakhi.configuration.FilariaFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.FilariaRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.form.MalariaFormFragmentArgs
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel

class FilariaFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val filariaRepo: FilariaRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        MalariaFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED, DRAFT_SAVED
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

    private val dataset =
        FilariaFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    private lateinit var filariaScreeningCache: FilariaScreeningCache


    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                filariaScreeningCache = FilariaScreeningCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId,
                )
            }

            filariaRepo.getFilariaScreening(benId)?.let {
                filariaScreeningCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) filariaScreeningCache else null
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
                    dataset.mapValues(filariaScreeningCache, 1)
                    filariaScreeningCache.syncState = SyncState.UNSYNCED
                    filariaRepo.saveFilariaScreening(filariaScreeningCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving Filaria data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(filariaScreeningCache, 1)
                    filariaScreeningCache.syncState = SyncState.DRAFT
                    filariaRepo.saveFilariaScreening(filariaScreeningCache)
                    _state.postValue(State.DRAFT_SAVED)
                } catch (e: Exception) {
                    Timber.d("saving Filaria draft data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
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
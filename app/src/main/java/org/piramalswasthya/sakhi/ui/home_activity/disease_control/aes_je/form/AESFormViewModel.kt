package org.piramalswasthya.sakhi.ui.home_activity.disease_control.aes_je.form

import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.configuration.AESJEFormDataset
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.repositories.AESRepo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.form.MalariaFormFragmentArgs
import timber.log.Timber
import javax.inject.Inject
@HiltViewModel

class AESFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val aesRepo: AESRepo,
    private val benRepo: BenRepo
) : ViewModel() {
    val benId =
        MalariaFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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

    private val dataset =
        AESJEFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    private lateinit var aesScreeningCache: AESScreeningCache


    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                aesScreeningCache = AESScreeningCache(
                    benId = ben.beneficiaryId,
                    houseHoldDetailsId = ben.householdId,
                )
            }

            aesRepo.getAESScreening(benId)?.let {
                aesScreeningCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }

            dataset.setUpPage(
                ben,
                if (recordExists.value == true) aesScreeningCache else null
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
                    dataset.mapValues(aesScreeningCache, 1)
                    aesRepo.saveAESScreening(aesScreeningCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving AES data failed!!")
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
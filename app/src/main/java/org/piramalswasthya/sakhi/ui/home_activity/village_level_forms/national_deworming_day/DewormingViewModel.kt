package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.national_deworming_day

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.configuration.DewormingDataset
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DewormingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val vlfRepo: VLFRepo,
) : ViewModel() {
    
    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    val allDewormingList = vlfRepo.dewormingList
    private val dewormingId = DewormingFormFragmentArgs.fromSavedStateHandle(savedStateHandle).id

    @RequiresApi(Build.VERSION_CODES.O)
    val isCurrentMonthFormFilled : Flow<Map<String, Boolean>> = vlfRepo.isFormFilledForCurrentMonth()


    private var lastImageFormId: Int = 0
    fun setCurrentImageFormId(id: Int) {
        lastImageFormId = id
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastImageFormId, dpUri)
    }
    fun getCurrentFormList(): List<FormElement> {
        return dataset.getFormElementList()
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val dataset = DewormingDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    lateinit var _dewormingCache: DewormingCache

    init {
        viewModelScope.launch {
            _dewormingCache = DewormingCache(id = 0)
            val dewormingRecord = vlfRepo.getDeworming(dewormingId)
            dewormingRecord?.let {
                _dewormingCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
            }
            dataset.setUpPage(
                if (recordExists.value == true) dewormingRecord else null
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
                dataset.mapValues(_dewormingCache, 1)
                vlfRepo.saveDeworming(_dewormingCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("Saving Deworming data failed!!")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }
}
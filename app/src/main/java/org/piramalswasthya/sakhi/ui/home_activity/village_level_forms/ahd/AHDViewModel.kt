package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ahd

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
import org.piramalswasthya.sakhi.configuration.AHDDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.repositories.VLFRepo
import javax.inject.Inject

@HiltViewModel
class AHDViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val vlfRepo: VLFRepo,
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    fun getCurrentFormList(): List<FormElement> {
        return dataset.getFormElementList()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    val isCurrentMonthFormFilled : Flow<Map<String, Boolean>> = vlfRepo.isFormFilledForCurrentMonth()


    val allAHDList = vlfRepo.ahdList
    private val ahdId = AHDFormFragmentArgs.fromSavedStateHandle(savedStateHandle).id

    private var lastImageFormId: Int = 0
    fun setCurrentImageFormId(id: Int) {
        lastImageFormId = id
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastImageFormId, dpUri)
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val dataset = AHDDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    lateinit var _ahdCache: AHDCache

    init {
        viewModelScope.launch {
            _ahdCache = AHDCache(id = 0, mobilizedForAHD = "")
            vlfRepo.getAHD(ahdId)?.let {
                _ahdCache = it
                _recordExists.value = true
                dataset.setUpPage(it)
            } ?: run {
                _recordExists.value = false
                dataset.setUpPage(null)
            }
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
                dataset.mapValues(_ahdCache, 1)
                vlfRepo.saveAHDRecord(_ahdCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }
}
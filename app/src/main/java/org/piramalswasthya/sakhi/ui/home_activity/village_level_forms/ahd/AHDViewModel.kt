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
import org.piramalswasthya.sakhi.ui.common.DraftViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AHDViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val vlfRepo: VLFRepo,
) : ViewModel(), DraftViewModel<AHDCache> {

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

    private val _draftExists = MutableLiveData<AHDCache?>(null)
    val draftExists: LiveData<AHDCache?>
        get() = _draftExists

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
                checkDraft()
            }
        }
    }

    private suspend fun checkDraft() {
        vlfRepo.getDraftAHD()?.let {
            _draftExists.value = it
        }
    }

    override fun restoreDraft(draft: AHDCache) {
        viewModelScope.launch {
            _ahdCache = draft
            dataset.setUpPage(draft)
            _draftExists.value = null
        }
    }

    override fun ignoreDraft() {
        viewModelScope.launch {
            _draftExists.value?.let {
                vlfRepo.deleteAHDById(it.id)
            }
            _draftExists.value = null
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            try {
                dataset.mapValues(_ahdCache, 1)
                _ahdCache.isDraft = true
                vlfRepo.saveAHDRecord(_ahdCache)
            } catch (e: Exception) {
                Timber.e("saving AHD draft failed!! $e")
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
                _ahdCache.isDraft = false
                vlfRepo.saveAHDRecord(_ahdCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("saving AHD data failed!!")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }
}
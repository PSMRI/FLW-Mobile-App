package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.vhnd

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
import org.piramalswasthya.sakhi.configuration.VHNDDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.ui.common.DraftViewModel
import timber.log.Timber

@HiltViewModel
class VHNDViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val vlfReo: VLFRepo,
) : ViewModel(), DraftViewModel<VHNDCache> {
    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    val allVHNDList = vlfReo.vhndList
    private val vhndId = VHNDFormFragementArgs.fromSavedStateHandle(savedStateHandle).id

    @RequiresApi(Build.VERSION_CODES.O)
    val isCurrentMonthFormFilled : Flow<Map<String, Boolean>> = vlfReo.isFormFilledForCurrentMonth()

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

    private val _draftExists = MutableLiveData<VHNDCache?>(null)
    val draftExists: LiveData<VHNDCache?>
        get() = _draftExists

    private val dataset =
        VHNDDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    lateinit var _vhndCache: VHNDCache

    init {
        viewModelScope.launch {
            _vhndCache = VHNDCache(id = 0, vhndDate = "");
            vlfReo.getVHND(vhndId)?.let {
                _vhndCache = it
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
        vlfReo.getDraftVHND()?.let {
            _draftExists.value = it
        }
    }

    override fun restoreDraft(draft: VHNDCache) {
        viewModelScope.launch {
            _vhndCache = draft
            dataset.setUpPage(draft)
            _draftExists.value = null
        }
    }

    override fun ignoreDraft() {
        viewModelScope.launch {
            _draftExists.value?.let {
                vlfReo.deleteVHNDById(it.id)
            }
            _draftExists.value = null
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            try {
                dataset.mapValues(_vhndCache, 1)
                _vhndCache.isDraft = true
                vlfReo.saveRecord(_vhndCache)
            } catch (e: Exception) {
                Timber.e("saving VHND draft failed!! $e")
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
                dataset.mapValues(_vhndCache, 1)
                _vhndCache.isDraft = false
                vlfReo.saveRecord(_vhndCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("saving VHND data failed!! $e")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }


}

package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.phc_review_meeting

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
import org.piramalswasthya.sakhi.configuration.PHCReviewDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import timber.log.Timber

@HiltViewModel
class PHCReviewViewModel @javax.inject.Inject
constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val vlfReo: VLFRepo,
) : ViewModel() {
    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    val allPHCCList = vlfReo.phcList
    private val phcReviewId = PHCReviewFormFragementArgs.fromSavedStateHandle(savedStateHandle).id

    private var lastImageFormId: Int = 0
    fun setCurrentImageFormId(id: Int) {
        lastImageFormId = id
    }

    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastImageFormId, dpUri)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val isCurrentMonthFormFilled : Flow<Map<String, Boolean>> = vlfReo.isFormFilledForCurrentMonth()


    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _draftExists = MutableLiveData<PHCReviewMeetingCache?>(null)
    val draftExists: LiveData<PHCReviewMeetingCache?>
        get() = _draftExists

    private val dataset =
        PHCReviewDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    lateinit var _phcCache: PHCReviewMeetingCache

    init {
        viewModelScope.launch {
            _phcCache = PHCReviewMeetingCache(id = 0, phcReviewDate = "");
            vlfReo.getPHC(phcReviewId)?.let {
                _phcCache = it
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
        vlfReo.getDraftPHC()?.let {
            _draftExists.value = it
        }
    }

    fun restoreDraft(draft: PHCReviewMeetingCache) {
        viewModelScope.launch {
            _phcCache = draft
            dataset.setUpPage(draft)
            _draftExists.value = null
        }
    }

    fun ignoreDraft() {
        viewModelScope.launch {
            _draftExists.value?.let {
                vlfReo.deletePHCById(it.id)
            }
            _draftExists.value = null
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            try {
                dataset.mapValues(_phcCache, 1)
                _phcCache.isDraft = true
                vlfReo.saveRecord(_phcCache)
            } catch (e: Exception) {
                Timber.e("saving PHC draft failed!! $e")
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
                dataset.mapValues(_phcCache, 1)
                _phcCache.isDraft = false
                vlfReo.saveRecord(_phcCache)
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("saving PHC data failed!!")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }


}
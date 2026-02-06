package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.saas_bahu_sammelan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.SaasBahuSamelanDataset
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SaasBahuSamelanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    private val saasBahuDao: SaasBahuSammelanDao,
    private val saasBahuSammelanRepo: SaasBahuSammelanRepo,
    @ApplicationContext context: Context,
) : ViewModel() {


    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    var id = SaasBahuSamelanFragmentArgs.fromSavedStateHandle(savedStateHandle).id


    val allSammelanList : Flow<List<SaasBahuSammelanCache>> = saasBahuDao.getAllSammelan()


    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _recordExists = MutableLiveData<Boolean>()
    val recordExists: LiveData<Boolean>
        get() = _recordExists

    private val _draftExists = MutableLiveData<SaasBahuSammelanCache?>(null)
    val draftExists: LiveData<SaasBahuSammelanCache?>
        get() = _draftExists

    private val dataset =
        SaasBahuSamelanDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var saasBahuCache: SaasBahuSammelanCache


    init {
        viewModelScope.launch {
            val ashaId = preferenceDao.getLoggedInUser()!!.userId
            saasBahuCache = SaasBahuSammelanCache(0, ashaId = ashaId )

           saasBahuDao.getById(id)?.let {
                saasBahuCache = it
                _recordExists.value = true
               dataset.setUpPage(saasBahuCache, true)

           } ?: run {
                _recordExists.value = false
               dataset.setUpPage(null, false)
               checkDraft(ashaId)
           }
        }
    }

    private suspend fun checkDraft(ashaId: Int) {
        saasBahuDao.getDraftSammelan(ashaId)?.let {
            _draftExists.value = it
        }
    }

    fun restoreDraft(draft: SaasBahuSammelanCache) {
        viewModelScope.launch {
            saasBahuCache = draft
            dataset.setUpPage(draft, true)
            _draftExists.value = null
        }
    }

    fun ignoreDraft() {
        viewModelScope.launch {
            _draftExists.value?.let {
                saasBahuDao.deleteSammelan(it)
            }
            _draftExists.value = null
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    dataset.mapSaasBahuValues(saasBahuCache)
                    saasBahuCache.isDraft = true
                    saasBahuDao.insertSammelan(saasBahuCache)
                } catch (e: Exception) {
                    Timber.e("saving draft failed!! $e")
                }
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
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapSaasBahuValues(saasBahuCache)
                    saasBahuCache.isDraft = false
                    saasBahuSammelanRepo.saveSammelanForm(saasBahuCache)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.Forest.d("saving saas bahu sammelan data failed!! $e ${saasBahuCache.toString()}")
                    _state.postValue(State.SAVE_FAILED)
                }
            }

        }
    }



    fun setUploadUriFor(formId: Int, uri: Uri) {
        when (formId) {
            10 -> dataset.upload1.value = uri.toString()
            11 -> dataset.upload2.value = uri.toString()
            12 -> dataset.upload3.value = uri.toString()
            13 -> dataset.upload4.value = uri.toString()
            14 -> dataset.upload5.value = uri.toString()
        }
    }


    fun resetState() {
        _state.value = State.IDLE
    }


}
package org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list.form

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
import org.piramalswasthya.sakhi.configuration.AdolescentHealthFormDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.repositories.AdolescentHealthRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.ui.common.DraftViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AdolescentHealthFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
   val  preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val benRepo: BenRepo,
    private val adolescentHealthRepo: AdolescentHealthRepo
) : ViewModel(), DraftViewModel<AdolescentHealthCache> {

    val benId =
        AdolescentHealthFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId
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

    private val _draftExists = MutableLiveData<AdolescentHealthCache?>(null)
    val draftExists: LiveData<AdolescentHealthCache?>
        get() = _draftExists

    private val dataset = AdolescentHealthFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private var adolescentHealthCache: AdolescentHealthCache? = null

    init {
        viewModelScope.launch {
            val ben = benRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                adolescentHealthCache = AdolescentHealthCache(
                    benId = ben.beneficiaryId,
                )
            }

            adolescentHealthRepo.getAdolescentHealth(benId)?.let {
                adolescentHealthCache = it
                _recordExists.value = true
            } ?: run {
                _recordExists.value = false
                checkDraft()
            }

            dataset.setFirstPage(
                ben,
                if (recordExists.value == true) adolescentHealthCache else null
            )
        }
    }

    private suspend fun checkDraft() {
        adolescentHealthRepo.getDraftAdolescentHealth(benId)?.let {
            _draftExists.value = it
        }
    }

    override fun restoreDraft(draft: AdolescentHealthCache) {
        viewModelScope.launch {
            adolescentHealthCache = draft
            dataset.setFirstPage(benRepo.getBenFromId(benId), draft)
            _draftExists.value = null
        }
    }

    override fun ignoreDraft() {
        viewModelScope.launch {
            _draftExists.value?.let {
                it.id?.let { id -> adolescentHealthRepo.deleteAdolescentHealthById(id) }
            }
            _draftExists.value = null
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            try {
                adolescentHealthCache?.let {
                    dataset.mapValues(it, 1)
                    it.isDraft = true
                    adolescentHealthRepo.saveAdolescentHealth(it)
                }
            } catch (e: Exception) {
                Timber.e("saving adolescent draft failed!! $e")
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
                    adolescentHealthCache?.let {
                        dataset.mapValues(it, 1)
                        it.isDraft = false
                        adolescentHealthRepo.saveAdolescentHealth(it)
                    }
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving adolescent  data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }


    fun setRecordExist(b: Boolean) {
        _recordExists.value = b
    }

    fun resetState() {
        _state.value = State.IDLE
    }
}
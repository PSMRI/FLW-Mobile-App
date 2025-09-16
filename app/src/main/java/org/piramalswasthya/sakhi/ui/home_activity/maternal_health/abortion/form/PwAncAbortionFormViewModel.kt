package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.form

import android.content.Context
import android.net.Uri
import android.util.Log
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
import org.piramalswasthya.sakhi.configuration.PregnantWomanAncAbortionDataset
import org.piramalswasthya.sakhi.configuration.PregnantWomanAncVisitDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PwAncAbortionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val benRepo: BenRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private val benId =
        PwAncAbortionFormFragmentArgs.fromSavedStateHandle(savedStateHandle).benId

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
        PregnantWomanAncAbortionDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

    private lateinit var ancCache: PregnantWomanAncCache
    fun getIndexOfAbortionDischarge1() = dataset.getIndexOfAbortionDischarge1()
    fun getIndexOfAbortionDischarge2() = dataset.getIndexOfAbortionDischarge1()

    private var lastDocumentFormId: Int = 0
    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }
    fun getDocumentFormId():Int {
        return lastDocumentFormId
    }
    fun setImageUriToFormElement(dpUri: Uri) {
        dataset.setImageUriToFormElement(lastDocumentFormId, dpUri)

    }

    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            val ben = maternalHealthRepo.getBenFromId(benId)?.also { ben ->
                _benName.value =
                    "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
                _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
                ancCache = PregnantWomanAncCache(
                    benId = ben.beneficiaryId,
                    visitNumber = 0,
                    syncState = SyncState.UNSYNCED,
                    createdBy = asha.userName,
                    updatedBy = asha.userName
                )
            }
            maternalHealthRepo.getSavedRecordANC(benId)?.let {
                ancCache = it
                _recordExists.value =  it.terminationDoneBy != null
            } ?: run {
                _recordExists.value = false
            }
            val lastAnc = maternalHealthRepo.getSavedRecordANC(benId)

            dataset.setUpPage(
                ben,
                lastAnc,
                if (recordExists.value == true) ancCache else null
            )


        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    companion object {
        private const val TAG = "ANCFormViewModel"
    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)

                    dataset.mapValues(ancCache, 1)
                    maternalHealthRepo.persistAncRecord(ancCache)

                    if (ancCache.isAborted) {
                        maternalHealthRepo.getSavedRegistrationRecord(benId)?.let {
                            it.active = false
                            if (it.processed != "N") {
                                it.processed = "U"
                            }
                            it.syncState = SyncState.UNSYNCED
                            maternalHealthRepo.persistRegisterRecord(it)
                        }

                        maternalHealthRepo.getAllInActiveAncRecords(benId).apply {
                            forEach {
                                it.isActive = false
                                if (it.processed != "N") {
                                    it.processed = "U"
                                }
                                it.syncState = SyncState.UNSYNCED
                            }
                            maternalHealthRepo.updateAncRecord(toTypedArray())
                        }
                    }

                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }


    fun setRecordExist(b: Boolean) {
        _recordExists.value = b

    }

    fun getIndexOfWeeksOfPregnancy(): Int = dataset.getWeeksOfPregnancy()


}
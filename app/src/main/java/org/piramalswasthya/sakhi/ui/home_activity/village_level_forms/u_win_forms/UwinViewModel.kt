package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.u_win_forms

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.ImmunizationDataset
import org.piramalswasthya.sakhi.configuration.UWINDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.UwinDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.network.getLongFromDate
import org.piramalswasthya.sakhi.repositories.UwinRepo
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form.ImmunizationFormViewModel
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.form.PncFormViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UwinViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferenceDao: PreferenceDao,
    private val uwinRepo: UwinRepo
) : ViewModel() {

    enum class State { IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED }

    private val _recordExists = MutableLiveData(false)
    val recordExists: LiveData<Boolean> get() = _recordExists

    private val dataset = UWINDataset(context, preferenceDao.getCurrentLanguage())
    val formList: StateFlow<List<FormElement>> = dataset.listFlow

    val uwinList: LiveData<List<UwinCache>> = uwinRepo.getAllLocalRecords()

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    private val asha: User = preferenceDao.getLoggedInUser()
        ?: throw IllegalStateException("Logged-in user cannot be null")

    private var currentUwinId: Int = 0
    private var isFormPrepared = false
    private var lastDocumentFormId: Int = 0

    private var currentUwinCache: UwinCache? = null


    fun prepareForm(saved: Boolean, id: Int = 0) {
        if (isFormPrepared && currentUwinId == id) return
        currentUwinId = id
        isFormPrepared = true

        viewModelScope.launch(Dispatchers.IO) {
            val record = if (saved && id != 0) uwinRepo.getUwinById(id) else null
            dataset.setFirstPage(saved = saved && id != 0, cache = record)
        }
    }

    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }

    fun getDocumentFormId(): Int = lastDocumentFormId

    fun setImageUriToFormElement(uri: Uri) {
        dataset.setImageUriToFormElement(lastDocumentFormId, uri)
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataset.updateList(formId, index)
        }
    }

    fun getIndexUWINSummary1() = dataset.getUwinFileIndex1()
    fun getIndexUWINSummary2() = dataset.getUwinFileIndex2()

    fun saveForm() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.postValue(State.SAVING)
                val base = currentUwinCache?.copy(
                    updatedBy = asha.userName,
                    updatedDate = System.currentTimeMillis(),
                    syncState = SyncState.UNSYNCED
                ) ?: UwinCache(
                    id = 0,
                    sessionDate = 0L,
                    place = "",
                    participantsCount = 0,
                    uploadedFiles1 = "",
                    uploadedFiles2 = "",
                    createdBy = asha.userName,
                    updatedBy = asha.userName,
                    syncState = SyncState.UNSYNCED
                )
                dataset.mapValues(base, 1)
                if (base.id == 0) {
                    uwinRepo.insertLocalRecord(base)
                } else {
                    uwinRepo.updateLocalRecord(base) // add this helper in the repo/dao
                }
                currentUwinCache = base
                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.e(e, "Saving U-Win data failed")
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }
}

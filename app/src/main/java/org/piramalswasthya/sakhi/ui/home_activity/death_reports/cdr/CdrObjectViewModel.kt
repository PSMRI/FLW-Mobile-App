package org.piramalswasthya.sakhi.ui.home_activity.death_reports.cdr

<<<<<<< Updated upstream
import android.app.Application
=======
import android.content.Context
import android.net.Uri
>>>>>>> Stashed changes
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
import org.piramalswasthya.sakhi.configuration.CDRFormDataset
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.CdrRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CdrObjectViewModel @Inject constructor(
    state: SavedStateHandle,
    @ApplicationContext context: Context,
    private val database: InAppDb,
    private val cdrRepo: CdrRepo,
    private val benRepo: BenRepo,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        SUCCESS,
        FAIL,
        DRAFT_SAVED
    }

    private val benId = CdrObjectFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = CdrObjectFragmentArgs.fromSavedStateHandle(state).hhId
    private lateinit var ben: BenRegCache
    private lateinit var household: HouseholdCache
    private lateinit var user: User
    private var cdr: CDRCache? = null

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender
    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _exists = MutableLiveData<Boolean>()
    val exists: LiveData<Boolean>
        get() = _exists

<<<<<<< Updated upstream
    private val dataset =
        CDRFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow

=======
    private val _hasUnsavedChanges = MutableLiveData(false)
    val hasUnsavedChanges: LiveData<Boolean>
        get() = _hasUnsavedChanges

    private val dataset =
        CDRFormDataset(context, preferenceDao.getCurrentLanguage(), preferenceDao)
    val formList = dataset.listFlow

    private var lastDocumentFormId: Int = 0
    fun setCurrentDocumentFormId(id: Int) {
        lastDocumentFormId = id
    }
    fun getDocumentFormId():Int {
        return lastDocumentFormId
    }
    fun setImageUriToFormElement(dpUri: Uri) {
        _hasUnsavedChanges.value = true
        dataset.setImageUriToFormElement(lastDocumentFormId, dpUri)

    }
>>>>>>> Stashed changes

    fun submitForm() {
        _state.value = State.LOADING
        val cdrCache =
            CDRCache(benId = benId, processed = "N", syncState = SyncState.UNSYNCED)
        dataset.mapValues(cdrCache)
        viewModelScope.launch {
            val saved = cdrRepo.saveCdrData(cdrCache)
            if (saved) {
                _hasUnsavedChanges.postValue(false)
                _state.value = State.SUCCESS
            } else {
                _state.value = State.FAIL
            }
        }
    }

    fun saveDraft() {
        _state.value = State.LOADING
        val cdrCache =
            CDRCache(benId = benId, processed = "N", syncState = SyncState.DRAFT)
        dataset.mapValues(cdrCache)
        viewModelScope.launch {
            val saved = cdrRepo.saveCdrData(cdrCache)
            if (saved) {
                _hasUnsavedChanges.postValue(false)
                _state.value = State.DRAFT_SAVED
            } else {
                _state.value = State.FAIL
            }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ben = benRepo.getBeneficiaryRecord(benId, hhId)!!
                household = benRepo.getHousehold(hhId)!!
                user = preferenceDao.getLoggedInUser()!!
                cdr = database.cdrDao.getCDR(benId)
            }
            _benName.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            _exists.value = cdr != null
            dataset.setUpPage(
                ben = ben,
                currentAddress = null,
                currentHouseNumber = null,
                currentMohalla = null,
                saved = if (_exists.value == true) cdr else null
            )
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            _hasUnsavedChanges.postValue(true)
            dataset.updateList(formId, index)
        }
    }

    fun getIndexOfCDR1() = dataset.getIndexOfCDR1()
    fun getIndexOfCDR2() = dataset.getIndexOfCDR2()
    fun getIndexOfIsDeathCertificate() = dataset.getIndexOfIsDeathCertificate()

}
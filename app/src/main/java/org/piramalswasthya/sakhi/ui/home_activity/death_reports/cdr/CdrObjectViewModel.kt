package org.piramalswasthya.sakhi.ui.home_activity.death_reports.cdr

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class CdrObjectViewModel @Inject constructor(
    state: SavedStateHandle,
    context: Application,
    private val database: InAppDb,
    private val cdrRepo: CdrRepo,
    private val benRepo: BenRepo,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        SUCCESS,
        FAIL
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

    private val dataset = CDRFormDataset(context, preferenceDao.getCurrentLanguage(),preferenceDao)
    val formList = dataset.listFlow

    fun getIndexOfCDR1() = dataset.getIndexOfCDR1()
    fun getIndexOfCDR2() = dataset.getIndexOfCDR2()
    fun getIndexOfIsDeathCertificate() = dataset.getIndexOfIsDeathCertificate()

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
            withContext(Dispatchers.IO) {
                ben = benRepo.getBeneficiaryRecord(benId, hhId)!!
                household = benRepo.getHousehold(hhId)!!
                user = preferenceDao.getLoggedInUser()!!
                cdr = database.cdrDao.getCDR(benId)
            }


            _benName.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            val address = getAddress(household)
            dataset.setUpPage(
                ben,
                address,
                household.family?.houseNo,
                household.family?.mohallaName,
                cdr
            )
            _exists.value = cdr != null
        }
    }

    fun submitForm() {
        _state.value = State.LOADING
        val cdrCache = CDRCache(
            benId = benId,
            createdBy = user.userName,
            createdDate = System.currentTimeMillis(),
            updatedBy = user.userName,
            updatedDate = System.currentTimeMillis(),
            processed = "N",
            syncState = SyncState.UNSYNCED
        )
        dataset.mapValues(cdrCache)
        viewModelScope.launch {
            val saved = cdrRepo.saveCdrData(cdrCache)
            if (saved)
                _state.value = State.SUCCESS
            else
                _state.value = State.FAIL
        }
    }


    private fun getAddress(household: HouseholdCache): String {
        val houseNo = household.family?.houseNo
        val wardNo = household.family?.wardNo
        val name = household.family?.wardName
        val mohalla = household.family?.mohallaName
        val district = household.locationRecord.district.name
        val city = household.locationRecord.village.name
        val state = household.locationRecord.state.name

        var address = "$houseNo, $wardNo, $name, $mohalla, $city, $district, $state"
        address = address.replace(", ,", ",")
        address = address.replace(",,", ",")
        address = address.replace(" ,", "")
        address = address.replace("null, ", "")
        address = address.replace(", null", "")
        if (address.length > 50) address = address.substring(0, 50)
        return address
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }
}
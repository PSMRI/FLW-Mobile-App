package org.piramalswasthya.sakhi.ui.home_activity.death_reports.mdsr

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
import org.piramalswasthya.sakhi.configuration.MDSRFormDataset
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.MDSRCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MdsrRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MdsrObjectViewModel @Inject constructor(
    state: SavedStateHandle,
    context: Application,
    private val database: InAppDb,
    private val benRepo: BenRepo,
    private val mdsrRepo: MdsrRepo,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    enum class State { IDLE, LOADING, SUCCESS, FAIL }

    private val benId = MdsrObjectFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = MdsrObjectFragmentArgs.fromSavedStateHandle(state).hhId

    private lateinit var ben: BenRegCache
    private lateinit var household: HouseholdCache
    private lateinit var user: User
    private var mdsr: MDSRCache? = null

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String> get() = _benAgeGender

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State> get() = _state

    private val _exists = MutableLiveData<Boolean>()
    val exists: LiveData<Boolean> get() = _exists

    private lateinit var dataset: MDSRFormDataset
    val formList get() = dataset.listFlow


    fun getIndexOfMDSR1() = dataset.getIndexOfMDSR1()
    fun getIndexOfMDSR2() = dataset.getIndexOfMDSR2()
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
            // IO context me database + suspend calls
            val (pregnancyDeath, abortionDeath, deliveryDeath, pncDeath) = withContext(Dispatchers.IO) {
                val preg = benRepo.hasPregnancyDeath(benId)
                val abort = benRepo.hasAbortionDeath(benId)
                val deliv = benRepo.hasDeliveryDeath(benId)
                val pnc = benRepo.hasPncDeath(benId)
                val benRecord = benRepo.getBeneficiaryRecord(benId, hhId)!!
                val hhRecord = benRepo.getHousehold(hhId)!!
                val loggedUser = preferenceDao.getLoggedInUser()!!
                val mdsrRecord = database.mdsrDao.getMDSR(benId)

                ben = benRecord
                household = hhRecord
                user = loggedUser
                mdsr = mdsrRecord

                listOf(preg, abort, deliv, pnc)

            }


            // Dataset ko coroutine ke andar initialize karo
            dataset = MDSRFormDataset(
                context,
                preferenceDao.getCurrentLanguage(),
                preferenceDao,
                pregnancyDeath,
                abortionDeath,
                deliveryDeath,
                pncDeath
            )

            // UI updates
            _benName.value = "${ben.firstName} ${ben.lastName ?: ""}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            val address = getAddress(household)
            dataset.setUpPage(ben, address, mdsr)
            _exists.value = mdsr != null
        }
    }

    fun submitForm() {
        _state.value = State.LOADING
        val mdsrCache = MDSRCache(
            benId = benId,
            createdBy = user.userName,
            createdDate = System.currentTimeMillis(),
            updatedBy = user.userName,
            updatedDate = System.currentTimeMillis(),
            processed = "N",
            syncState = SyncState.UNSYNCED
        )
        dataset.mapValues(mdsrCache)

        viewModelScope.launch {
            val saved = mdsrRepo.saveMdsrData(mdsrCache)
            Timber.d("mdsr saved: $mdsrCache")
            _state.value = if (saved) State.SUCCESS else State.FAIL
        }
    }

    private fun getAddress(household: HouseholdCache): String {
        var address =
            "${household.family?.houseNo}, ${household.family?.wardNo}, ${household.family?.wardName}, ${household.family?.mohallaName}, ${household.locationRecord.village.name}, ${household.locationRecord.district.name}, ${household.locationRecord.state.name}"
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

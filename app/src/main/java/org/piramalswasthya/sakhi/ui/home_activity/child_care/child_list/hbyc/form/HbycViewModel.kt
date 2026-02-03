package org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.hbyc.form

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
import org.piramalswasthya.sakhi.configuration.HBYCFormDataset
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HbycRepo
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HbycViewModel @Inject constructor(
    state: SavedStateHandle,
    @ApplicationContext context: Context,
    private val database: InAppDb,
    private val hbycRepo: HbycRepo,
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

    private val benId = HbycFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = HbycFragmentArgs.fromSavedStateHandle(state).hhId
    private val month = HbycFragmentArgs.fromSavedStateHandle(state).month
    private lateinit var ben: BenRegCache
    private lateinit var household: HouseholdCache
    private lateinit var user: User
    private var hbyc: HBYCCache? = null

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String>
        get() = _benName
    private val _benAgeGender = MutableLiveData<String>()
    val benAgeGender: LiveData<String>
        get() = _benAgeGender
    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address
    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _exists = MutableLiveData<Boolean>()
    val exists: LiveData<Boolean>
        get() = _exists

    private val dataset =
        HBYCFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    fun submitForm() {
        _state.value = State.LOADING
        val hbycCache =
            HBYCCache(benId = benId, hhId = hhId, month = month.toString(), processed = "N", syncState = SyncState.UNSYNCED)
        dataset.mapValues(hbycCache)
        Timber.d("saving hbyc: $hbycCache")
        viewModelScope.launch {
            val saved = hbycRepo.saveHbycData(hbycCache)
            if (saved) {
                Timber.d("saved hbyc: $hbycCache")
                _state.value = State.SUCCESS
            } else {
                Timber.d("saving hbyc to local db failed!!")
                _state.value = State.FAIL
            }
        }
    }

    fun saveDraft() {
        _state.value = State.LOADING
        val hbycCache =
            HBYCCache(benId = benId, hhId = hhId, month = month.toString(), processed = "N", syncState = SyncState.DRAFT)
        dataset.mapValues(hbycCache)
        viewModelScope.launch {
            val saved = hbycRepo.saveHbycData(hbycCache)
            if (saved) {
                _state.value = State.DRAFT_SAVED
            } else {
                _state.value = State.FAIL
            }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Timber.d("benId : $benId hhId : $hhId")
                ben = benRepo.getBeneficiaryRecord(benId, hhId)!!
                household = benRepo.getHousehold(hhId)!!
                user = preferenceDao.getLoggedInUser()!!
                hbyc = database.hbycDao.getHbyc(hhId, benId, month.toString())
            }
            _benName.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            _address.value = getAddress(household)
            _exists.value = hbyc != null
            dataset.setUpPage(
                ben,
                if (_exists.value == true) hbyc else null,
                month.toString()
            )
        }
    }

    private fun getAddress(household: HouseholdCache): String {
        val houseNo = household.family?.houseNo
        val wardNo = household.family?.wardNo
        val name = household.family?.wardName
        val mohalla = household.family?.mohallaName
        val city = household.locationRecord.village.name
        val district = household.locationRecord.district.name
        val state = household.locationRecord.state.name

        var address = "$houseNo, $wardNo, $name, $mohalla, $city, $district, $state"
        address = address.replace(", ,", ",")
        address = address.replace(",,", ",")
        address = address.replace(" ,", "")
        address = address.replace("null, ", "")
        address = address.replace(", null", "")

        return address
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }
    }

    private fun getDateFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        dateLong?.let {
            return dateFormat.format(dateLong)
        } ?: run {
            return null
        }
    }

}

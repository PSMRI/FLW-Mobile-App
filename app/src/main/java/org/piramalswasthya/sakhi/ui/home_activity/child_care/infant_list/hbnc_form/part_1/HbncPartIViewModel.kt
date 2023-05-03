package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.hbnc_form.part_1

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.configuration.HBNCFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.UserDomain
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HbncRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HbncPartIViewModel @Inject constructor(
    @ApplicationContext context : Context,
    state: SavedStateHandle,
    preferenceDao: PreferenceDao,
    private val hbncRepo: HbncRepo,
    benRepo: BenRepo,
    userRepo : UserRepo
) : ViewModel() {

    enum class State {
        IDLE, LOADING, SUCCESS, FAIL
    }

    private val benId = HbncPartIFragmentArgs.fromSavedStateHandle(state).benId
    private val hhId = HbncPartIFragmentArgs.fromSavedStateHandle(state).hhId
    private lateinit var ben: BenRegCache
    private lateinit var household: HouseholdCache
    private lateinit var user: UserDomain
    private var hbnc: HBNCCache? = null

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

    private val dataset = HBNCFormDataset(context, preferenceDao.getCurrentLanguage(), Konstants.hbncPart1Day)
    val formList = dataset.listFlow
    val alertError = dataset.alertErrorMessageFlow

    fun submitForm() {
        _state.value = State.LOADING
        val hbncCache = HBNCCache(
            benId = benId,
            hhId = hhId,
            homeVisitDate = Konstants.hbncPart1Day,
            processed = "N",
            syncState = SyncState.UNSYNCED
        )
        dataset.mapValues(hbncCache)
        Timber.d("saving hbnc: $hbncCache")
        viewModelScope.launch {
            val saved = hbncRepo.saveHbncData(hbncCache)
            if (saved) {
                Timber.d("saved hbnc: $hbncCache")
                _state.value = State.SUCCESS
            } else {
                Timber.d("saving hbnc to local db failed!!")
                _state.value = State.FAIL
            }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Timber.d("benId : $benId hhId : $hhId")
                ben = benRepo.getBeneficiary(benId, hhId)!!
                household = benRepo.getHousehold(hhId)!!
                user = userRepo.getLoggedInUser()!!
                hbnc = hbncRepo.getHbncRecord(benId, hhId, Konstants.hbncPart1Day)
            }
            _benName.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            _benAgeGender.value = "${ben.age} ${ben.ageUnit?.name} | ${ben.gender?.name}"
            _exists.value = hbnc != null
            val hbncCard = hbncRepo.getHbncCard(benId, hhId)
            dataset.setPart1PageToList(hbncCard, hbnc?.part1)
        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList( formId, index)
        }

    }

    fun resetErrorMessage() {
        viewModelScope.launch {
            dataset.resetErrorMessageFlow()
        }
    }


}


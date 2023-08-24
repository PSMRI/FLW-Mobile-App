package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.register

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.configuration.BenRegCHODataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BenRegisterCHOViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val benRepo: BenRepo,
    private val householdRepo: HouseholdRepo,
    amritApiService: AmritApiService
) : ViewModel() {

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

    private lateinit var user: User

    private lateinit var locationRecord: LocationRecord

    private val dataset =
        BenRegCHODataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow


    private lateinit var benRegCache: BenRegCache

    init {
        viewModelScope.launch {
            dataset.setUpPage()
            user = preferenceDao.getLoggedInUser()!!
            locationRecord = preferenceDao.getLocationRecord()!!
            benRegCache = BenRegCache(
                ashaId = user.userId,
                beneficiaryId = -2,
                householdId = 0,
                isAdult = true,
                isKid = false,
                isDraft = true,
                gender = Gender.FEMALE,
                genderId = 2,
                genDetails = BenRegGen(reproductiveStatus = "OTHER", reproductiveStatusId = 5),
                syncState = SyncState.UNSYNCED,
                locationRecord = preferenceDao.getLocationRecord()!!
            )
//            dataset.mapValues(benRegCache, 0)
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
                if (householdRepo.getRecord(0) == null) {
                    var hh = HouseholdCache(
                        householdId = 0,
                        ashaId = user.userId,
                        processed = "P",
                        locationRecord = locationRecord,
                        isDraft = true
                    )
                    householdRepo.persistRecord(hh)
                    benRegCache.householdId = 0
                }
                _state.postValue(State.SAVING)
                dataset.mapValues(benRegCache)
                benRegCache.apply {
                    benRepo.substituteBenIdForDraft(benRegCache)
                    serverUpdatedStatus = 1
                    processed = "N"

                    if (createdDate == null) {
                        createdDate = System.currentTimeMillis()
                        createdBy = user.userName
                    }
                    updatedDate = System.currentTimeMillis()
                    updatedBy = user.userName
                }

                benRegCache.ageUnitId
                benRepo.persistRecord(benRegCache)

                _state.postValue(State.SAVE_SUCCESS)
            } catch (e: Exception) {
                Timber.d("saving ben data failed!! " + e.message)
                _state.postValue(State.SAVE_FAILED)
            }
        }
    }

    fun resetState() {
        _state.value = State.IDLE
    }

    fun getIndexOfAge() = dataset.getIndexOfAge()

    fun getIndexOfDob() = dataset.getIndexOfDOB()
}
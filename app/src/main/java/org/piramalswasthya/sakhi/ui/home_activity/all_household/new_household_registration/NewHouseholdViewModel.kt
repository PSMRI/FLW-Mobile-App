package org.piramalswasthya.sakhi.ui.home_activity.all_household.new_household_registration

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
import org.piramalswasthya.sakhi.configuration.HouseholdFormDataset
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NewHouseholdViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val benRepo: BenRepo,
    private val householdRepo: HouseholdRepo,
    userRepo: UserRepo
) : ViewModel() {

    enum class State {
        IDLE, SAVING, SAVE_SUCCESS, SAVE_FAILED
    }

    private var isConsentAgreed = false

    fun setConsentAgreed() {
        isConsentAgreed = true
    }

    fun getIsConsentAgreed() = isConsentAgreed

    private val hhIdFromArgs = NewHouseholdFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId
    private val isAshaFamily = NewHouseholdFragmentArgs.fromSavedStateHandle(savedStateHandle).isAshaFamily

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state

    private val _readRecord = MutableLiveData(hhIdFromArgs > 0)
    val readRecord: LiveData<Boolean>
        get() = _readRecord

    private lateinit var user: User
    private val dataset = HouseholdFormDataset(context, preferenceDao.getCurrentLanguage())
    val formList = dataset.listFlow
    private lateinit var household: HouseholdCache


    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                user = preferenceDao.getLoggedInUser()!!
                val locationRecord = preferenceDao.getLocationRecord()!!
                household = householdRepo.getRecord(hhIdFromArgs) ?: householdRepo.getDraftRecord()
                        ?: HouseholdCache(
                    householdId = 0,
                    ashaId = user.userId,
                    isDraft = true,
                    processed = "N",
                    registrationType = isAshaFamily,
                    locationRecord = locationRecord
                )
                dataset.setupPage(household)
            }
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapValues(household, 1)
                    dataset.mapValues(household, 2)
                    dataset.mapValues(household, 3)
                    household.apply {
                        if (householdId == 0L) {
                            dataset.freezeHouseholdId(household, user.userId)
                            householdRepo.substituteHouseholdIdForDraft(household)
                            serverUpdatedStatus = 1
                            processed = "N"
                        } else {
                            serverUpdatedStatus = 2
                            processed = "U"
                        }

                        if (createdTimeStamp == null) {
                            createdTimeStamp = System.currentTimeMillis()
                            createdBy = user.userName
                        }
                        updatedTimeStamp = System.currentTimeMillis()
                        updatedBy = user.userName
                    }
                    householdRepo.persistRecord(household)
                    benRepo.updateBenToSync(household.householdId, SyncState.UNSYNCED)
                    _state.postValue(State.SAVE_SUCCESS)
                } catch (e: Exception) {
                    Timber.d("saving HH data failed!!")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }

    /**
     * Household Id to be passed to Ben registration upon persisting household data to Room
     */
    fun getHHId() = household.householdId
    fun getHoFName() = "${household.family?.familyHeadName} ${household.family?.familyName ?: ""}"


    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    fun setRecordExists(b: Boolean) {
        _readRecord.value = b


    }

    fun updateValueByIdAndReturnListIndex(id: Int, value: String): Int {
        dataset.setValueById(id, value)
        return dataset.getIndexById(id)
    }

}
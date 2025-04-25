package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages.ASSAMESE
import org.piramalswasthya.sakhi.helpers.Languages.ENGLISH
import org.piramalswasthya.sakhi.helpers.Languages.HINDI
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.ui.service_location_activity.ServiceTypeViewModel.State
import javax.inject.Inject

@HiltViewModel
class SupervisorViewModel @Inject constructor(
    private val database: InAppDb,
    private val pref: PreferenceDao,
    private val userRepo: UserRepo,
) : ViewModel() {


    private val _devModeState: MutableLiveData<Boolean> = MutableLiveData(pref.isDevModeEnabled)
    val devModeEnabled: LiveData<Boolean>
        get() = _devModeState


    val currentUser = pref.getLoggedInUser()

    val numBenIdsAvail = database.benIdGenDao.liveCount()

    var profilePicUri: Uri?
        get() = pref.getProfilePicUri()
        set(value) = pref.saveProfilePicUri(value)

    val scope: CoroutineScope
        get() = viewModelScope
//    private var _unprocessedRecords: Int = 0
//    val unprocessedRecords: Int
//        get() = _unprocessedRecords

    private var _unprocessedRecordsCount: MutableLiveData<Int> = MutableLiveData(0)
    val unprocessedRecordsCount: LiveData<Int>
        get() = _unprocessedRecordsCount

    private lateinit var villageDropdownEntries: Array<String>
    val villageList: Array<String>
        get() = villageDropdownEntries

    val locationRecord: LocationRecord? = pref.getLocationRecord()
    val currentLanguage = pref.getCurrentLanguage()

    private lateinit var _userName: String
    val userName: String
        get() = _userName

    private var _selectedVillage: LocationEntity? = null
    val selectedVillage: LocationEntity?
        get() = _selectedVillage

    val selectedVillageName: String?
        get() = when (pref.getCurrentLanguage()) {
            ENGLISH -> selectedVillage?.name
            HINDI -> selectedVillage?.nameHindi ?: selectedVillage?.name
            ASSAMESE -> selectedVillage?.nameAssamese ?: selectedVillage?.name
        }

    private val _navigateToLoginPage = MutableLiveData(false)
    val navigateToLoginPage: MutableLiveData<Boolean>
        get() = _navigateToLoginPage

    private val _state = MutableLiveData(State.LOADING)
    val state: LiveData<State>
        get() = _state

    private var currentLocation: LocationRecord? = null
    private lateinit var user: User

    init {
        viewModelScope.launch {
//            _user = pref.getLoggedInUser()!!
            launch {
                userRepo.unProcessedRecordCount.collect { value ->
                    _unprocessedRecordsCount.value =
                        value.filter { it.syncState != SyncState.SYNCED }.sumOf { it.count }
                }
            }
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                user = pref.getLoggedInUser()!!
                _userName = user.name
                currentLocation = pref.getLocationRecord()
                _selectedVillage = currentLocation?.village
                when (pref.getCurrentLanguage()) {
                    ENGLISH -> {
                        villageDropdownEntries = user.villages.map { it.name }.toTypedArray()

                    }

                    HINDI -> {
                        villageDropdownEntries =
                            user.villages.map { it.nameHindi ?: it.name }.toTypedArray()
                    }

                    ASSAMESE -> {
                        villageDropdownEntries =
                            user.villages.map { it.nameAssamese ?: it.name }.toTypedArray()
                    }
                }

            }
            _state.value = State.SUCCESS
        }
    }

    fun setVillage(i: Int) {
        _selectedVillage = user.villages[i]

    }

    fun logout() {
        viewModelScope.launch {
            pref.deleteLoginCred()
            _navigateToLoginPage.value = true
        }
    }

    fun navigateToLoginPageComplete() {
        _navigateToLoginPage.value = false
    }


    fun setDevMode(boolean: Boolean) {
        pref.isDevModeEnabled = boolean
        _devModeState.value = boolean
    }

    fun getDebMode() = pref.isDevModeEnabled

}
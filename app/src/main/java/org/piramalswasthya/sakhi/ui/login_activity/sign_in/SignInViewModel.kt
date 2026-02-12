package org.piramalswasthya.sakhi.ui.login_activity.sign_in

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.UserRepo
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val database: InAppDb,
    private val pref: PreferenceDao
) : ViewModel() {

    private val _state: MutableLiveData<NetworkResponse<User?>> =
        MutableLiveData(NetworkResponse.Idle())
    val state: LiveData<NetworkResponse<User?>>
        get() = _state

    private val _loggedInUser: MutableLiveData<User?> =
        MutableLiveData(null)
    val loggedInUser: LiveData<User?>
        get() = _loggedInUser

    private val _logoutComplete: MutableLiveData<Boolean?> =
        MutableLiveData(null)
    val logoutComplete: LiveData<Boolean?>
        get() = _logoutComplete

    private var _unprocessedRecordsCount: MutableLiveData<Int> = MutableLiveData(0)
    val unprocessedRecordsCount: LiveData<Int>
        get() = _unprocessedRecordsCount

    init {
        _loggedInUser.value = pref.getLoggedInUser()
        viewModelScope.launch {
            launch {
                userRepo.unProcessedRecordCount.collect { value ->
                    _unprocessedRecordsCount.value =
                        value.filter { it.syncState != SyncState.SYNCED }.sumOf { it.count }
                }
            }
        }
    }

    fun loginInClicked() {
        _state.value = NetworkResponse.Loading()
    }

    /**
     * function to remove data of currently logged in uset
     * clear all in app db data
     * remove shared preferences data and reset last synced time
     * set logoutComplete live data to true to be observed in fragment
     */
    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
            pref.deleteForLogout()
            pref.setLastSyncedTimeStamp(Konstants.defaultTimeStamp)
            _loggedInUser.value = null
            Thread.sleep(2000)
            _logoutComplete.value = true
        }
    }

    fun getLoggedInUser(): User? {
        return pref.getLoggedInUser()
    }

    /**
     * authenticate a user with username and password
     */
    fun authUser(username: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = userRepo.authenticateUser(username, password)
            } catch (e: Exception) {
                _state.value =
                    NetworkResponse.Error("Network Call failed.\nUnknown error : ${e.message} stack-trace : ${e.stackTrace}")
                pref.deleteLoginCred()

            }
        }
    }

    fun fetchRememberedUserName(): String? =
        pref.getRememberedUserName()

    fun fetchRememberedPassword(): String? =
        pref.getRememberedPassword()

    fun fetchRememberedState(): String? =
        pref.getRememberedState()


    fun rememberUser(username: String, password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pref.registerLoginCred(username, password)
            }
        }
    }

    fun forgetUser() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                pref.deleteLoginCred()
            }
        }
    }

    fun updateState(state: NetworkResponse<User?>) {
        _state.value = state
    }

}
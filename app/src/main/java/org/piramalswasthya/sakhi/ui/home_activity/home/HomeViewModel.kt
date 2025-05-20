package org.piramalswasthya.sakhi.ui.home_activity.home

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.isInternetAvailable
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.time.Instant
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
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


    val locationRecord: LocationRecord? = pref.getLocationRecord()
    val currentLanguage = pref.getCurrentLanguage()

    private val _navigateToLoginPage = MutableLiveData(false)
    val navigateToLoginPage: MutableLiveData<Boolean>
        get() = _navigateToLoginPage

    init {
        viewModelScope.launch {
//            _user = pref.getLoggedInUser()!!
            launch {
                userRepo.unProcessedRecordCount.collect { value ->
                    _unprocessedRecordsCount.value =
                        value.filter { it.syncState != SyncState.SYNCED }.sumOf { it.count }
                }
            }
            val firebaseToken = FirebaseMessaging.getInstance().token.await()
            val userId = currentUser?.userId
            val updatedAt = Instant.now().toString()
            if (userId != null) {
                userRepo.saveFirebaseToken(userId, firebaseToken, updatedAt)
            }
        }
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
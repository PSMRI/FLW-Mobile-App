package org.piramalswasthya.sakhi.ui.home_activity.asha_profile

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
import org.piramalswasthya.sakhi.configuration.AshaProfileDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.repositories.AshaProfileRepo
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel

class AshaProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferenceDao: PreferenceDao,
    @ApplicationContext context: Context,
    private val ashaProfileRepo: AshaProfileRepo,
) : ViewModel() {

    val currentUser = preferenceDao.getLoggedInUser()
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

    private val dataset =
        AshaProfileDataset(context, preferenceDao.getCurrentLanguage(),ashaProfileRepo)
    val formList = dataset.listFlow

    var isPregnant: Boolean = false

    private lateinit var profileActivityCache: ProfileActivityCache

    private lateinit var locationRecord: LocationRecord



    init {
        viewModelScope.launch {
            val asha = preferenceDao.getLoggedInUser()!!
            locationRecord = preferenceDao.getLocationRecord()!!
            ashaProfileRepo.pullAndSaveAshaProfile(asha)
            ashaProfileRepo.getSavedRecord(asha.userId.toLong())?.let {
                Timber.d("Caught $it at profile!")
                profileActivityCache = it
                _recordExists.value = true
                currentUser?.let {
                    dataset.setUpPage(
                        it,
                        profileActivityCache
                    )
                }

            } ?: run {
                _recordExists.value = false

            }


        }
    }

    fun updateListOnValueChanged(formId: Int, index: Int) {
        viewModelScope.launch {
            dataset.updateList(formId, index)
        }

    }

    fun saveForm() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _state.postValue(State.SAVING)
                    dataset.mapProfileValues(profileActivityCache)
                    _state.postValue(State.SAVE_SUCCESS)

                } catch (e: IllegalAccessError) {
                    Timber.d("saving profile data failed!! $e")
                    _state.postValue(State.SAVE_FAILED)
                }
            }
        }
    }




    fun resetState() {
        _state.value = State.IDLE
    }
    fun setRecordExist(b: Boolean) {
        _recordExists.value = b

    }

}
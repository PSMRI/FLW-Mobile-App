package org.piramalswasthya.sakhi.ui.service_location_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.Languages.ASSAMESE
import org.piramalswasthya.sakhi.helpers.Languages.ENGLISH
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import javax.inject.Inject

@HiltViewModel
class ServiceTypeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pref: PreferenceDao,
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        SUCCESS
    }

    // Unsaved-changes tracking
    private val _hasUnsavedChanges = MutableLiveData(false)
    val hasUnsavedChanges: LiveData<Boolean>
        get() = _hasUnsavedChanges

    private val _draftSaved = MutableLiveData<Boolean>()
    val draftSaved: LiveData<Boolean>
        get() = _draftSaved

    private val KEY_DRAFT_VILLAGE_ID = "service_location_draft_village_id"
    private val KEY_DRAFT_BLOCK = "service_location_draft_block"
    private val KEY_DRAFT_DISTRICT = "service_location_draft_district"
    private val KEY_DRAFT_STATE = "service_location_draft_state"

    private lateinit var stateDropdownEntry: String
    val stateList: Array<String>
        get() = arrayOf(stateDropdownEntry)

    private lateinit var districtDropdownEntry: String
    val districtList: Array<String>
        get() = arrayOf(districtDropdownEntry)

    private lateinit var blockDropdownEntry: String
    val blockList: Array<String>
        get() = arrayOf(blockDropdownEntry)

    private lateinit var villageDropdownEntries: Array<String>
    val villageList: Array<String>
        get() = villageDropdownEntries

    private lateinit var _userName: String
    val userName: String
        get() = _userName

    private var _selectedVillage: LocationEntity? = null
    val selectedVillage: LocationEntity?
        get() = _selectedVillage
    val selectedVillageName: String?
        get() = when (pref.getCurrentLanguage()) {
            ENGLISH -> selectedVillage?.name
            Languages.HINDI -> selectedVillage?.nameHindi ?: selectedVillage?.name
            ASSAMESE -> selectedVillage?.nameAssamese ?: selectedVillage?.name
        }


    private val _state = MutableLiveData(State.LOADING)
    val state: LiveData<State>
        get() = _state


    private var currentLocation: LocationRecord? = null
    private lateinit var user: User

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                user = pref.getLoggedInUser()!!
                _userName = user.name
                currentLocation = pref.getLocationRecord()
                _selectedVillage = currentLocation?.village
                when (pref.getCurrentLanguage()) {
                    ENGLISH -> {
                        stateDropdownEntry = user.state.name
                        districtDropdownEntry = user.district.name
                        blockDropdownEntry = user.block.name
                        villageDropdownEntries = user.villages.map { it.name }.toTypedArray()

                    }

                   Languages.HINDI -> {
                        stateDropdownEntry =
                            user.state.let { it.nameHindi ?: it.name }
                        districtDropdownEntry =
                            user.district.let { it.nameHindi ?: it.name }
                        blockDropdownEntry =
                            user.block.let { it.nameHindi ?: it.name }
                        villageDropdownEntries =
                            user.villages.map { it.nameHindi ?: it.name }.toTypedArray()
                    }

                    ASSAMESE -> {
                        stateDropdownEntry =
                            user.state.let { it.nameAssamese ?: it.name }
                        districtDropdownEntry =
                            user.district.let { it.nameAssamese ?: it.name }
                        blockDropdownEntry =
                            user.block.let { it.nameAssamese ?: it.name }
                        villageDropdownEntries =
                            user.villages.map { it.nameAssamese ?: it.name }.toTypedArray()
                   }
                }

                // attempt to restore any saved draft after loading user data
                restoreDraftFromSavedState()

            }
            _state.value = State.SUCCESS
        }
    }

    fun isLocationSet(): Boolean {
        return if (state.value == State.LOADING)
            false
        else
            currentLocation != null
    }

    fun setVillage(i: Int) {
        _selectedVillage = user.villages[i]

    }

    // Mark that the user changed something on the form
    fun markLocationChanged() {
        _hasUnsavedChanges.value = true
    }

    fun clearUnsavedChanges() {
        _hasUnsavedChanges.value = false
    }

    fun saveDraftToSavedState() {
        // store minimal state needed to restore selection
        try {
            savedStateHandle.set(KEY_DRAFT_STATE, stateDropdownEntry)
            savedStateHandle.set(KEY_DRAFT_DISTRICT, districtDropdownEntry)
            savedStateHandle.set(KEY_DRAFT_BLOCK, blockDropdownEntry)
            savedStateHandle.set(KEY_DRAFT_VILLAGE_ID, selectedVillage?.id ?: -1)
            _draftSaved.value = true
        } catch (t: Throwable) {
            // swallow - best-effort
        }
    }

    fun restoreDraftFromSavedState() {
        try {
            val vid = savedStateHandle.get<Int>(KEY_DRAFT_VILLAGE_ID) ?: -1
            if (vid != -1) {
                // find the village by id from user's villages and set selection
                val found = user.villages.find { it.id == vid }
                if (found != null) {
                    _selectedVillage = found
                    _hasUnsavedChanges.value = true
                }
            }
        } catch (t: Throwable) {
            // ignore
        }
    }

    fun clearDraftFromSavedState() {
        try {
            savedStateHandle.set<Int?>(KEY_DRAFT_VILLAGE_ID, null)
            savedStateHandle.set<String?>(KEY_DRAFT_BLOCK, null)
            savedStateHandle.set<String?>(KEY_DRAFT_DISTRICT, null)
            savedStateHandle.set<String?>(KEY_DRAFT_STATE, null)
        } catch (t: Throwable) {
            // ignore
        }
    }

    fun saveCurrentLocation() {
        val locationRecord = LocationRecord(
            LocationEntity(1, "India"),
            user.state,
            user.district,
            user.block,
            selectedVillage!!
        )
        pref.saveLocationRecord(locationRecord)
    }


}
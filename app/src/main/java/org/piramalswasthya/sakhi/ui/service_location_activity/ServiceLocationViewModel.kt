package org.piramalswasthya.sakhi.ui.service_location_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val pref: PreferenceDao,
) : ViewModel() {

    enum class State {
        IDLE,
        LOADING,
        SUCCESS
    }

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
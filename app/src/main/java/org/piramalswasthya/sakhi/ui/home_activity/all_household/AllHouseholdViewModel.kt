package org.piramalswasthya.sakhi.ui.home_activity.all_household

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseHoldBasicDomain
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class AllHouseholdViewModel @Inject constructor(
    private val householdRepo: HouseholdRepo, recordsRepo: RecordsRepo, private val benRepo: BenRepo,
    private val preferenceDao: PreferenceDao,

    ) : ViewModel() {

    private val _hasDraft = MutableLiveData(false)
    val hasDraft: LiveData<Boolean>
        get() = _hasDraft

    private val filter = MutableStateFlow("")

    private val _navigateToNewHouseholdRegistration = MutableLiveData(false)
    val navigateToNewHouseholdRegistration: LiveData<Boolean>
        get() = _navigateToNewHouseholdRegistration

    val householdList = recordsRepo.hhList.combine(filter) { list, filter ->
        filterHH(list, filter)
    }
    private var _selectedHouseholdId: Long = 0

    private val _householdBenList = mutableListOf<BenRegCache>()
    val householdBenList: List<BenRegCache>
        get() = _householdBenList

    val selectedHouseholdId: Long
        get() = _selectedHouseholdId

    private var _selectedHousehold: HouseholdCache? = null
    val selectedHousehold: HouseholdCache?
        get() = _selectedHousehold


    fun checkDraft() {
        viewModelScope.launch {
            _hasDraft.value = householdRepo.getDraftRecord() != null
        }
    }

    fun navigateToNewHouseholdRegistration(delete: Boolean) {

        viewModelScope.launch {
            if (delete) householdRepo.deleteHouseholdDraft()
            _navigateToNewHouseholdRegistration.value = true
        }
    }

    fun navigateToNewHouseholdRegistrationCompleted() {
        _navigateToNewHouseholdRegistration.value = false
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }

    }

    private fun filterHH(
        list: List<HouseHoldBasicDomain>, filter: String
    ): List<HouseHoldBasicDomain> {
        return if (filter == "") list
        else {
            val filterText = filter.lowercase()
            list.filter {
                it.hhId.toString().contains(filterText) || it.headFullName.lowercase()
                    .contains(filterText) || it.contactNumber.lowercase().contains(filterText)
            }
        }
    }

    fun resetSelectedHouseholdId() {
        _selectedHouseholdId = 0
        _householdBenList.clear()
    }

    fun setSelectedHouseholdId(id: Long) {
        _selectedHouseholdId = id
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _selectedHousehold = householdRepo.getRecord(id)
            }
            _householdBenList.clear()
            _householdBenList.addAll(householdRepo.getAllBenOfHousehold(id))
        }
    }


    fun deActivateHouseHold(houseHoldBasicDomain: HouseHoldBasicDomain) {
        viewModelScope.launch {
            val user = preferenceDao.getLoggedInUser() ?: run {
                return@launch
            }
            val houseHoldCache = householdRepo.getRecord(houseHoldBasicDomain.hhId) ?: run {
                return@launch
            }
            houseHoldBasicDomain.apply {
                isDeactivate = !isDeactivate
            }.also {
                houseHoldCache.isDeactivate = houseHoldBasicDomain.isDeactivate
                houseHoldCache.processed = "U"
                houseHoldCache.serverUpdatedStatus = 2
            }
            householdRepo.persistRecord(houseHoldCache)
            val benList = benRepo.getBenListFromHousehold(houseHoldBasicDomain.hhId)
            try {
                benRepo.deactivateHouseHold(benList, houseHoldCache.asNetworkModel(user))
            } catch (e: Exception) {

            }
        }
    }
}
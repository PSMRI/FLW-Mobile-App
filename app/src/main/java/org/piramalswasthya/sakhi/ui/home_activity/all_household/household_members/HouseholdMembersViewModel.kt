package org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenHealthIdDetails
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.utils.HelperUtil.getDiffYears
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class HouseholdMembersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val benRepo: BenRepo,
    private val preferenceDao: PreferenceDao,
    ecrRepo: EcrRepo

    ) : ViewModel() {

    val hhId = HouseholdMembersFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = 0
    val diseaseType = "No"
    private val childCountMap =
        MutableStateFlow<Map<Long, Int>>(emptyMap())


    init {
        viewModelScope.launch {
            benRepo.getBenBasicListFromHousehold(hhId).collect { list ->
                val map = mutableMapOf<Long, Int>()

                list.forEach { ben ->
                    val count =
                        benRepo.getChildBenListFromHousehold(
                            ben.hhId,
                            ben.benId,
                            ben.benName
                        ).size

                    map[ben.benId] = count
                }

                childCountMap.emit(map)
            }
        }
    }
    val benListWithChildren =
        benRepo.getBenBasicListFromHousehold(hhId)
            .map { list ->
                list.sortedBy { ben ->
                    ben.relToHeadId != 19
                }
            }
            .map { list ->
                list.map { ben ->
                    val count =
                        benRepo.getChildBenListFromHousehold(
                            ben.hhId,
                            ben.benId,
                            ben.benName
                        ).size

                    ben.copy(noOfChildren = count)
                }
            }
    private val _abha = MutableLiveData<String?>()
    val abha: LiveData<String?>
        get() = _abha

    private val _benId = MutableLiveData<Long?>()
    val benId: LiveData<Long?>
        get() = _benId

    private val _benRegId = MutableLiveData<Long?>()
    val benRegId: LiveData<Long?>
        get() = _benRegId

    fun fetchAbha(benId: Long) {
        _abha.value = null
        _benRegId.value = null
        _benId.value = benId
        viewModelScope.launch {
            benRepo.getBenFromId(benId)?.let {
                val result = benRepo.getBeneficiaryWithId(it.benRegId)
                if (result != null) {
                    _abha.value = result.healthIdNumber
                    it.healthIdDetails = BenHealthIdDetails(result.healthId, result.healthIdNumber)
                    it.isNewAbha = result.isNewAbha
                    benRepo.updateRecord(it)
                } else {
                    _benRegId.value = it.benRegId
                }
            }
        }
    }

    fun resetBenRegId() {
        _benRegId.value = null
    }



    fun deActivateBeneficiary(benBasicDomain: BenBasicDomain) {
        viewModelScope.launch {
           var benRegCache =   benRepo.getBenFromId(benBasicDomain.benId)

            benBasicDomain.apply {
                isDeactivate = !isDeactivate
            }.also {
                benRegCache?.isDeactivate =  benBasicDomain.isDeactivate
                if (benRegCache?.processed != "N"){
                    benRegCache?.processed = "U"
                    benRegCache?.syncState = SyncState.UNSYNCED
                    benRegCache?.serverUpdatedStatus = 2
                }

            }

            if (benRegCache != null) {
                benRepo.updateRecord(benRegCache)
            }

               if (benRegCache != null) {
                   val result = benRepo.deactivateBeneficiary( listOf(benRegCache)/*,houseHoldCache.asNetworkModel(user)*/)
               }
        }
    }
    suspend fun isHOF(ben: BenBasicDomain): Boolean {
        val familyMemberList =
            benRepo.getBenListFromHousehold(ben.hhId)

        val hof = familyMemberList.firstOrNull {
            it.familyHeadRelationPosition == 19
        }

        return hof?.beneficiaryId == ben.benId
    }


    suspend fun canDeleteHoF(
        hhId:Long,
    ): Boolean {
        val householdMembers = benRepo.getBenListFromHousehold(hhId)
             val hof = householdMembers.firstOrNull { it.familyHeadRelationPosition == 19 }
        return householdMembers
                 .filter { it.beneficiaryId != hof?.beneficiaryId }
                 .isEmpty()
    }


}
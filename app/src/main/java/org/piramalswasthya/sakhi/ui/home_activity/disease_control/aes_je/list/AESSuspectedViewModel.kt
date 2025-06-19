package org.piramalswasthya.sakhi.ui.home_activity.disease_control.aes_je.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members.HouseholdMembersFragmentArgs
import javax.inject.Inject

@HiltViewModel
class AESSuspectedViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var hhId = AESSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = AESSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).fromDisease

    val diseaseType = AESSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).diseaseType

    val allBenList = recordsRepo.aesScreeningList(hhId)

}
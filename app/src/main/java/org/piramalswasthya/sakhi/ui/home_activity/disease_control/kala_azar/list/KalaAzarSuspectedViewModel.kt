package org.piramalswasthya.sakhi.ui.home_activity.disease_control.kala_azar.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members.HouseholdMembersFragmentArgs
import javax.inject.Inject

@HiltViewModel
class KalaAzarSuspectedViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var hhId = KalaAzarSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = KalaAzarSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).fromDisease

    val diseaseType = KalaAzarSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).diseaseType

    val allBenList = recordsRepo.KalazarScreeningList(hhId)

}
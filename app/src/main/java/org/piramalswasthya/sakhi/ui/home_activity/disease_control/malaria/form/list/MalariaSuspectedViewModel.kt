package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members.HouseholdMembersFragmentArgs
import javax.inject.Inject

@HiltViewModel
class MalariaSuspectedViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var hhId = MalariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = MalariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).fromDisease

    val diseaseType = MalariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).diseaseType

    val allBenList = recordsRepo.malariaScreeningList(hhId)

}
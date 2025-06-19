package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.kala_azar.list.KalaAzarSuspectedListFragmentArgs
import javax.inject.Inject

@HiltViewModel
class LeprosySuspectedViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var hhId = LeprosySuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = LeprosySuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).fromDisease

    val diseaseType = LeprosySuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).diseaseType

    val allBenList = recordsRepo.LeprosyScreeningList(hhId)

}
package org.piramalswasthya.sakhi.ui.home_activity.disease_control.filaria.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.kala_azar.list.KalaAzarSuspectedListFragmentArgs
import javax.inject.Inject

@HiltViewModel

class FilariaSuspectedViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var hhId = FilariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = FilariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).fromDisease

    val diseaseType = FilariaSuspectedListFragmentArgs.fromSavedStateHandle(savedStateHandle).diseaseType

    val allBenList = recordsRepo.filariaScreeningList(hhId)

}
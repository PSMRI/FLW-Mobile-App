package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.confirmed.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class LeprosyConfirmedViewModel  @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val allBenList = recordsRepo.LeprosyConfirmedList()

}
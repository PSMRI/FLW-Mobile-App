package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.suspected.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.list.LeprosySuspectedListFragmentArgs
import javax.inject.Inject


@HiltViewModel
class SuspectedLeprosyViewModel  @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val allBenList = recordsRepo.LeprosySuspectedList()

}
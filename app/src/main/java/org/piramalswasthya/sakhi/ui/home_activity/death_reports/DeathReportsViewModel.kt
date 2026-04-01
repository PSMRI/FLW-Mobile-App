package org.piramalswasthya.sakhi.ui.home_activity.death_reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

@HiltViewModel
class DeathReportsViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {

    val generalDeathCount = recordsRepo.getGeneralDeathCount()
    val maternalDeathCount = recordsRepo.getMaternalDeathCount()
    val nonMaternalDeathCount = recordsRepo.getNonMaternalDeathCount()
    val childDeathCount = recordsRepo.getChildDeathCount()

    val scope: CoroutineScope
        get() = viewModelScope

}

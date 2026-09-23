package org.piramalswasthya.sakhi.ui.home_activity.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject

data class DashboardStats(
    val totalAncWomen: Int = 0,
    val deliveryDue: Int = 0,
    val totalDeliveryWomen: Int = 0,
    val pncWomen: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {

    val stats: StateFlow<DashboardStats> = combine(
        recordsRepo.getRegisteredPregnantWomanListCount(),
        recordsRepo.getDeliveryDueWomenCount(),
        recordsRepo.getDeliveredWomenListCount(),
        recordsRepo.pncMotherListCount
    ) { totalAncWomen, deliveryDue, totalDeliveryWomen, pncWomen ->
        DashboardStats(
            totalAncWomen = totalAncWomen,
            deliveryDue = deliveryDue,
            totalDeliveryWomen = totalDeliveryWomen,
            pncWomen = pncWomen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )
}

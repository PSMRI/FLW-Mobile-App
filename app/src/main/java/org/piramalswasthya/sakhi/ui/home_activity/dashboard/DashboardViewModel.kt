package org.piramalswasthya.sakhi.ui.home_activity.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import javax.inject.Inject

data class DashboardStats(
    val totalBeneficiaries: Int = 0,
    val totalHouseholds: Int = 0,
    val pregnantWomen: Int = 0,
    val highRiskWomen: Int = 0,
    val deliveredWomen: Int = 0,
    val pendingSync: Int = 0,
    val maleBeneficiaries: Int = 0,
    val femaleBeneficiaries: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val database: InAppDb,
    private val pref: PreferenceDao,
) : ViewModel() {

    private val villageId: Int
        get() = pref.getLocationRecord()?.village?.id ?: 0

    val currentUser = pref.getLoggedInUser()

    val householdCount: StateFlow<Int> =
        database.householdDao.getAllHouseholdsCount(villageId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val stats: StateFlow<DashboardStats> = combine(
        database.benDao.getAllBenCount(villageId),
        database.benDao.getAllPregnancyWomenListCount(villageId),
        database.benDao.getHighRiskWomenCount(villageId),
        database.benDao.getAllDeliveredWomenListCount(villageId),
        database.benDao.getAllBenGenderCount(villageId, "MALE"),
        database.benDao.getAllBenGenderCount(villageId, "FEMALE"),
        database.benDao.getUnProcessedRecordCount(),
    ) { values ->
        val total    = values[0] as Int
        val pregnant = values[1] as Int
        val highRisk = values[2] as Int
        val delivered = values[3] as Int
        val male     = values[4] as Int
        val female   = values[5] as Int
        val unsynced = values[6] as Int

        DashboardStats(
            totalBeneficiaries  = total,
            pregnantWomen       = pregnant,
            highRiskWomen       = highRisk,
            deliveredWomen      = delivered,
            maleBeneficiaries   = male,
            femaleBeneficiaries = female,
            pendingSync         = unsynced,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardStats()
    )
}
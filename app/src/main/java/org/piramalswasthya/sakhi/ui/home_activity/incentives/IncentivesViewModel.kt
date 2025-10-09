package org.piramalswasthya.sakhi.ui.home_activity.incentives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ListenableWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.model.IncentiveDomainDTO
import org.piramalswasthya.sakhi.model.IncentiveGrouped
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.getDateStrFromLong
import org.piramalswasthya.sakhi.repositories.IncentiveRepo
import java.util.Calendar
import javax.inject.Inject
import org.piramalswasthya.sakhi.model.IncentiveActivityDomain


@HiltViewModel
class
IncentivesViewModel @Inject constructor(
    pref: PreferenceDao,
    incentiveRepo: IncentiveRepo
) : ViewModel() {

    private val _lastUpdated: Long = pref.lastIncentivePullTimestamp

    private val _incentiveRepo = incentiveRepo
    private val _pref = pref
    val lastUpdated: String
        get() = getDateStrFromLong(_lastUpdated)!!



    val items = incentiveRepo.activity_list.map { it ->
        it.map { it.asDomainModel() }
    }
    val sourceIncentiveList: Flow<List<IncentiveDomain>> =
        incentiveRepo.list.map { it -> it.map { it.asDomainModel() } }

    private val initStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        setToStartOfTheDay()
    }.timeInMillis

    private val initEnd = Calendar.getInstance().apply {
        setToStartOfTheDay()
    }.timeInMillis

    init {
        pullIncentives()
    }


    private val _from = MutableStateFlow(initStart)
    val from: Flow<Long>
        get() = _from

    private val _to = MutableStateFlow(initEnd)

    val to: Flow<Long>
        get() = _to


    private val range = MutableStateFlow(Pair(initStart, initEnd))

    val currentUser = pref.getLoggedInUser()

    val locationRecord: LocationRecord? = pref.getLocationRecord()

    val incentiveList: Flow<List<IncentiveDomain>> =
        sourceIncentiveList.combine(range) { list, range ->

            list.filter {
                it.record.createdDate in (range.first..range.second)
            }
        }

    val groupedIncentiveList: Flow<List<IncentiveGrouped>> =
        incentiveList.map { domainList ->
            groupIncentivesByActivity(domainList)
        }

    private fun pullIncentives() {
        viewModelScope.launch {
            val user = _pref.getLoggedInUser()
            if (user != null) {
                _incentiveRepo.pullAndSaveAllIncentiveActivities(user)
                _incentiveRepo.pullAndSaveAllIncentiveRecords(user)
            }
        }
    }

    fun setRange(from: Long, to: Long) {
        viewModelScope.launch {
            range.emit(Pair(from, to))
            _from.emit(from)
            _to.emit(to)

        }
    }


    fun mapToDomainDTO(list: List<IncentiveActivityDomain>): List<IncentiveDomainDTO> {
        val (from, to) = range.value

        return list.map { domain ->

            val filteredRecords = domain.records.filter { record ->
                record.createdDate in from..to
            }

            IncentiveDomainDTO(
                id = domain.activity.id,
                group = domain.activity.group,
                groupName = domain.activity.groupName,
                name = domain.activity.name,
                description = domain.activity.description,
                paymentParam = domain.activity.paymentParam,
                rate = domain.activity.rate.toLong(),
                noOfClaims = filteredRecords.size,
                amountClaimed = filteredRecords.sumOf { it.amount },
                fmrCode = domain.activity.fmrCode,
                documentsSubmitted = null
            )
        }.sortedBy { it.group }
    }


    fun groupIncentivesByActivity(incentiveDomainList: List<IncentiveDomain>): List<IncentiveGrouped> {
        return incentiveDomainList
            .groupBy { it.activity.id }
            .map { (activityId, incentives) ->
                IncentiveGrouped(
                    activityName = incentives.first().activity.name,
                    totalAmount = incentives.sumOf { it.record.amount },
                    count = incentives.size,
                    groupName = incentives.first().activity.groupName,
                    description = incentives.first().activity.description,
                    activity = incentives.first().activity
                )
            }
            .sortedBy { it.activityName }
    }

    fun getRecordsForActivity(activityId: Long): Flow<List<IncentiveDomain>> {
        return combine(_incentiveRepo.list, range) { cacheList, range ->
            val (from, to) = range
            cacheList
                .filter { it.activity.id == activityId && it.record.createdDate in from..to }
                .map { it.asDomainModel() }
                .sortedBy { it.record.createdDate }
        }
    }

}
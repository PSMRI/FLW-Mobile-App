package org.piramalswasthya.sakhi.ui.home_activity.incentives

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject
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
import org.piramalswasthya.sakhi.model.UploadResponse
import timber.log.Timber
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ActionState


@HiltViewModel
class
IncentivesViewModel @Inject constructor(
    pref: PreferenceDao,
    incentiveRepo: IncentiveRepo,
    var apiService: AmritApiService
) : ViewModel() {


    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val response: UploadResponse) : UploadState()
        data class Error(val message: String) : UploadState()
    }


    sealed class FileUpdateState {
        object Idle : FileUpdateState()
        object Loading : FileUpdateState()
        object Success : FileUpdateState()
        data class Error(val message: String) : FileUpdateState()
    }

    private val _lastUpdated: Long = pref.lastIncentivePullTimestamp

    private val _incentiveRepo = incentiveRepo
    private val _pref = pref

    private var _isStateChhattisgarh = MutableLiveData<Boolean>()
    val isStateChhattisgarh: LiveData<Boolean>
        get() = _isStateChhattisgarh

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

    private val _actionState = MutableLiveData<ActionState>()
    val actionState: LiveData<ActionState> = _actionState
    private val initEnd = Calendar.getInstance().apply {
        setToStartOfTheDay()
    }.timeInMillis

    init {
        val user = pref.getLoggedInUser()
        _isStateChhattisgarh.value = user?.state?.id == 8
        pullIncentives()
    }

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState


    private val _fileUpdateState = MutableLiveData<FileUpdateState>()
    val fileUpdateState: LiveData<FileUpdateState> = _fileUpdateState

    private val _incentivesList = MutableLiveData<List<IncentiveDomain>>()
    val incentivesList: LiveData<List<IncentiveDomain>> = _incentivesList



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

        val dtoList = list.map { domain ->
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
        }

        val groupOrder = dtoList.distinctBy { it.group }.map { it.group }

        return dtoList.sortedBy { groupOrder.indexOf(it.group) }
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
                    activity = incentives.first().activity,
                    hasZeroBen = incentives.any { it.record.benId == 0L },
                    defaultIncentive = incentives.any { it.activity.fmrCodeOld =="PER_MONTH" },
                    isEligible = incentives.all {it.record.isEligible}
                )
            }
            .sortedWith(
                compareByDescending<IncentiveGrouped> { it.defaultIncentive }
                    .thenByDescending { it.hasZeroBen }
                    .thenBy { it.activityName }


            )
    }

    fun getMonthNumber(monthName: String): Int {
        val months = mapOf(
            "January" to 1,
            "February" to 2,
            "March" to 3,
            "April" to 4,
            "May" to 5,
            "June" to 6,
            "July" to 7,
            "August" to 8,
            "September" to 9,
            "October" to 10,
            "November" to 11,
            "December" to 12
        )

        return months[monthName] ?: 0
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

    fun uploadIncentiveDocuments(item: IncentiveDomain) {
        viewModelScope.launch {

            _uploadState.value = UploadState.Loading
            val user = _pref.getLoggedInUser()

            if (user == null) {
                _uploadState.value = UploadState.Error("User not logged in")
                return@launch
            }
            val result = _incentiveRepo.uploadIncentiveFiles(
                id = item.record.id,
                userId = user.userId.toLong(),
                moduleName = item.activity.group,
                activityName = item.activity.name,
                fileUris = item.uploadedFiles
            )

            result.fold(
                onSuccess = { response ->

                   // item.serverFileUrls = response.fileUrls
                    item.isSubmitted = true
                    item.submittedAt = System.currentTimeMillis()

                    // saveToDatabase(item)
                    pullIncentivesAndWait()

                    _uploadState.value = UploadState.Success(response)



                },
                onFailure = { error ->
                    _uploadState.value = UploadState.Error(error.message ?: "Upload failed")
                }
            )
        }
    }


    private suspend fun pullIncentivesAndWait() {
        try {
            val user = _pref.getLoggedInUser()
            if (user != null) {
                _incentiveRepo.pullAndSaveAllIncentiveRecords(user)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error pulling incentives")
        }
    }
    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }











    fun claimIncentive(selectedMonth: String, selectedYear: String) {

        getMonthNumber(selectedMonth)

        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            try {

                val response = apiService.claimAshaIncentive(
                    mapOf(
                        "month" to getMonthNumber(selectedMonth),
                        "year" to selectedYear.toInt(),
                        "claimed" to true

                    )
                )

                if (response.isSuccessful) {

                    val json = response.body()?.string()
                    val jsonObj = JSONObject(json ?: "{}")

                    if (jsonObj.optInt("statusCode", 0) == 200) {

                        val updated = jsonObj.optInt("updatedRecords", 0)

                        _actionState.value =
                            ActionState.Success("Successfully claimed $updated records")
                        pullIncentives()
                    } else {
                        _actionState.value =
                            ActionState.Error(jsonObj.optString("errorMessage", "Claim failed"))
                    }

                } else {
                    _actionState.value =
                        ActionState.Error("Server error: ${response.code()}")
                }

            } catch (e: Exception) {

                _actionState.value =
                    ActionState.Error(e.message ?: "Unknown error")

            }
        }
    }
}
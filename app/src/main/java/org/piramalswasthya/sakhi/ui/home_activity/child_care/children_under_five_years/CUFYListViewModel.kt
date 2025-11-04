package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years


import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.ChildOption
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository

import javax.inject.Inject


@HiltViewModel
class CUFYListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    repository: CUFYFormRepository,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val allBenList = recordsRepo.childFilteredList

    private val _recordsRepo = repository
    private val filter = MutableStateFlow("")
    private val kind = MutableStateFlow("false")

    @SuppressLint("StaticFieldLeak")
    private val _context = context

    val benList = allBenList.combine(kind) { list, kind ->
        if (kind.equals("false", true)) {
            filterBenList(list, false)
        } else {
            filterBenList(list, true)
        }
    }.combine(filter) { list, filter ->
        filterBenList(list, filter)
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }

    private val _benListWithSamStatus = MutableStateFlow<List<BenWithSamStatus>>(emptyList())
    val benListWithSamStatus: StateFlow<List<BenWithSamStatus>> =
        _benListWithSamStatus.asStateFlow()

    private val _childOptionsList = MutableStateFlow<List<ChildOption>>(emptyList())
    val childOptionsList: StateFlow<List<ChildOption>> = _childOptionsList.asStateFlow()

    suspend fun getSavedVisits(formId: String, benId: Long): List<CUFYFormResponseJsonEntity> {
        return _recordsRepo.getSavedDataByFormId(formId, benId)
    }


    init {
        updateSamStatusesOnce()

    }


    fun startSamStatusUpdates() {
        viewModelScope.launch {
            benList.collect { list ->
                val updatedList = list.map { ben ->
                    val status = try {
                        _recordsRepo.getCurrentSamStatus(ben.benId)
                    } catch (e: Exception) {
                        "Check SAM"
                    }
                    BenWithSamStatus(ben, status)
                }
                _benListWithSamStatus.value = updatedList
            }
        }
    }


    fun updateSamStatusesOnce() {
        viewModelScope.launch {

            val currentList = benList.first()
            val updatedList = currentList.map { ben ->
                val status = try {
                    _recordsRepo.getCurrentSamStatus(ben.benId)
                } catch (e: Exception) {
                    "Check SAM"
                }
                BenWithSamStatus(ben, status)
            }
            _benListWithSamStatus.value = updatedList
        }
    }

    suspend fun getSamStatusForBeneficiary(benId: Long): String {
        return try {
            _recordsRepo.getCurrentSamStatus(benId)
        } catch (e: Exception) {
            "Check SAM"
        }
    }


    data class BenWithSamStatus(
        val ben: BenBasicDomain,
        val samStatus: String
    )

}

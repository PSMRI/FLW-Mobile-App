package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.ChildOption
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.IFA_FORM_NAME
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.ORS_FORM_NAME
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.SAM_FORM_NAME
import javax.inject.Inject


@HiltViewModel
class CUFYListViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    repository: CUFYFormRepository
) : ViewModel() {

    private val allBenList = recordsRepo.childFilteredList

    private val _recordsRepo = repository
    private val filter = MutableStateFlow("")
    private val kind = MutableStateFlow("false")

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
    val benListWithSamStatus: StateFlow<List<BenWithSamStatus>> = _benListWithSamStatus.asStateFlow()

    private val _childOptionsList = MutableStateFlow<List<ChildOption>>(emptyList())
    val childOptionsList: StateFlow<List<ChildOption>> = _childOptionsList.asStateFlow()

    suspend fun getSavedVisits(formId: String, benId: Long ): List<CUFYFormResponseJsonEntity> {
        return _recordsRepo.getSavedDataByFormId(formId, benId)
    }




    init {
        updateSamStatusesOnce()
        loadChildOptions()
    }

    private fun loadChildOptions() {
        val options = listOf(
            ChildOption(
                formType = SAM_FORM_NAME,
                title = "SAM Form",
                description = "Severe Acute Malnutrition",

                ),
            ChildOption(
                formType =  ORS_FORM_NAME,
                title = "ORS Form",
                description = "Oral Rehydration Solution",

                ),
            ChildOption(
                formType = IFA_FORM_NAME,
                title = "IFA Form",
                description = "Iron Folic Acid",

                ),

            ChildOption(
                formType = SAM_FORM_NAME,
                title = "SAM Form",
                description = "Severe Acute Malnutrition",

                ),
        )
        _childOptionsList.value = options
    }


    fun updateAllSamStatuses() {
        viewModelScope.launch {
            benList.collect { list ->
                val updatedList = list.map { ben ->
                    val status = _recordsRepo.getCurrentSamStatus(ben.benId)
                    BenWithSamStatus(ben, status)
                }

            }
        }
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


    fun getFilteredOptions(type: String?): List<ChildOption> {
        return when (type) {
            SAM_FORM_NAME -> childOptionsList.value.filter { it.formType == SAM_FORM_NAME }
            ORS_FORM_NAME -> childOptionsList.value.filter { it.formType == ORS_FORM_NAME }
            IFA_FORM_NAME -> childOptionsList.value.filter { it.formType == IFA_FORM_NAME }

            else -> childOptionsList.value // Show all options
        }
    }

    data class BenWithSamStatus(
        val ben: BenBasicDomain,
        val samStatus: String
    )

}

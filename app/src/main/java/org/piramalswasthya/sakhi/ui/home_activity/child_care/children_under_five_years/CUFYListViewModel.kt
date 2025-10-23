package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterBenList
import org.piramalswasthya.sakhi.model.ChildOption
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import javax.inject.Inject


@HiltViewModel
class CUFYListViewModel @Inject constructor(
    recordsRepo: RecordsRepo
) : ViewModel() {

    private val allBenList = recordsRepo.childFilteredList
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

    private val _childOptionsList = MutableStateFlow<List<ChildOption>>(emptyList())
    val childOptionsList: StateFlow<List<ChildOption>> = _childOptionsList.asStateFlow()

    init {
        loadChildOptions()
    }

    private fun loadChildOptions() {
        val options = listOf(
            ChildOption(
                formType = "Check SAM",
                title = "SAM Form",
                description = "Severe Acute Malnutrition",

                ),
            ChildOption(
                formType = "ORS",
                title = "ORS Form",
                description = "Oral Rehydration Solution",

                ),
            ChildOption(
                formType = "IFA",
                title = "IFA Form",
                description = "Iron Folic Acid",

                ),

            ChildOption(
                formType = "Check SAM",
                title = "SAM Form",
                description = "Severe Acute Malnutrition",

                ),
        )
        _childOptionsList.value = options
    }

    fun getFilteredOptions(type: String?): List<ChildOption> {
        return when (type) {
            "Check SAM" -> childOptionsList.value.filter { it.formType == "Check SAM" }
            "ORS" -> childOptionsList.value.filter { it.formType == "ORS" }
            "IFA" -> childOptionsList.value.filter { it.formType == "IFA" }
            "HIGH_RISK" -> childOptionsList.value.filter {
                it.formType in listOf("SAM", "ORS", "IFA")
            }
            else -> childOptionsList.value // Show all options
        }
    }
}

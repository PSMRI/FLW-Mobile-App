package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.list

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.helpers.filterTbScreeningList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TPTStartRepository
import org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain
import org.piramalswasthya.sakhi.R
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list.MalariaSuspectedListFragmentArgs
import javax.inject.Inject

@HiltViewModel
class TBScreeningListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    recordsRepo: RecordsRepo,
    private val tbRepo: TBRepo,
    private val tptRepo: TPTStartRepository,
    savedStateHandle: SavedStateHandle,

    ) : ViewModel() {


    var hhId = TBScreeningListFragmentArgs.fromSavedStateHandle(savedStateHandle).hhId

    val isFromDisease = TBScreeningListFragmentArgs.fromSavedStateHandle(savedStateHandle).fromDisease

    val diseaseType = TBScreeningListFragmentArgs.fromSavedStateHandle(savedStateHandle).diseaseType

    private val allBenList = recordsRepo.tbScreeningList(hhId)


    private val filter = MutableStateFlow("")
   /* val benList = allBenList.combine(filter) { list, filter ->
        filterTbScreeningList(list, filter)
    }*/

    val benList = allBenList.combine(filter) { list, filter ->
        val filteredList = filterTbScreeningList(list, filter)
        filteredList
    }

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text)
        }
    }

    suspend fun addScreeningBlockMessage(item: BenWithTbScreeningDomain): String? {
        val suspectedCase = item.tbSuspected
        val isStillPresumptive = suspectedCase != null &&
                suspectedCase.isRecommendedForTpt != true &&
                suspectedCase.isTBConfirmed != true &&
                !suspectedCase.isConfirmed
        if (isStillPresumptive) {
            return context.getString(R.string.tb_screening_presumptive_block)
        }

        val confirmedStatus = suspectedCase?.isTBConfirmed == true || suspectedCase?.isConfirmed == true
        val confirmedTreatment = tbRepo.getAllFollowUpsForBeneficiary(item.ben.benId)
        val latestConfirmed = confirmedTreatment.maxByOrNull { it.followUpDate ?: it.updatedAt }
        if (confirmedStatus && latestConfirmed?.treatmentCompleted != true) {
            val date = latestConfirmed?.expectedTreatmentCompletionDate?.let {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(java.util.Date(it))
            } ?: context.getString(R.string.not_available)
            return if (latestConfirmed == null) {
                context.getString(R.string.tb_screening_confirm_followup_not_started, date)
            } else {
                context.getString(R.string.tb_screening_confirm_treatment_ongoing, date)
            }
        }

        if (suspectedCase?.isRecommendedForTpt == true) {
            val history = tptRepo.getFollowUps(item.ben.benId)
            val isComplete = history.any { visit ->
                runCatching { JSONObject(visit.fieldsJson).optString("treatment_completed").equals("Yes", true) }
                    .getOrDefault(false)
            }
            if (!isComplete) {
                val expectedDate = history.asReversed().firstNotNullOfOrNull { visit ->
                    runCatching { JSONObject(visit.fieldsJson).optString("expected_treatment_completion_date") }
                        .getOrNull()?.takeIf(String::isNotBlank)
                }
                val date = expectedDate?.let(::formatTreatmentDate)
                    ?: context.getString(R.string.not_available)
                return if (history.isEmpty()) {
                    context.getString(R.string.tb_screening_tpt_followup_not_started, date)
                } else {
                    context.getString(R.string.tb_screening_tpt_treatment_ongoing, date)
                }
            }
        }

        return null
    }

    private fun formatTreatmentDate(value: String): String = runCatching {
        val input = listOf("dd-MM-yyyy", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
            .firstNotNullOfOrNull { pattern ->
                runCatching { SimpleDateFormat(pattern, Locale.ENGLISH).parse(value) }.getOrNull()
            } ?: return value
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(input)
    }.getOrDefault(value)
}

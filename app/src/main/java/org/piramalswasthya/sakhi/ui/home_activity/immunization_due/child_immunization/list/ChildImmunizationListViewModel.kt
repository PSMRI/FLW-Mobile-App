package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.filterImmunList
import org.piramalswasthya.sakhi.helpers.setToEndOfTheDay
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.ImmunizationCategory
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.model.VaccineState
import org.piramalswasthya.sakhi.model.VaccineType
import org.piramalswasthya.sakhi.model.toVaccineType
import org.piramalswasthya.sakhi.utils.HelperUtil.getLocalizedResources
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChildImmunizationListViewModel @Inject constructor(
    vaccineDao: ImmunizationDao,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val showDueOnly: Boolean =
        savedStateHandle.get<Boolean>("showDueOnly") ?: false

    private val resources get() = getLocalizedResources(context, preferenceDao.getCurrentLanguage())

    /** Quick worklist buckets shown as the two filter cards above the list (FLW-1144). */
    enum class QuickFilter { DUE_THIS_MONTH, NEXT_MONTH }

    /**
     * Single source of truth for the Dose Stage options: label and the category it filters on.
     * `null` category is the "All" entry.
     *
     * FLW-1144 dropped 10 YEARS / 16 YEARS, leaving 8 options. Those two could never match
     * anyway - this screen's query and the home tile count both clamp to dob >= now - 6 years.
     */
    private data class DoseStageOption(
        val category: ChildImmunizationCategory?,
        @StringRes val labelRes: Int,
    )

    private val doseStageOptions = listOf(
        DoseStageOption(null, R.string.all),
        DoseStageOption(ChildImmunizationCategory.BIRTH, R.string.imm_cat_birth_dose),
        DoseStageOption(ChildImmunizationCategory.WEEK_6, R.string.imm_cat_6_weeks),
        DoseStageOption(ChildImmunizationCategory.WEEK_10, R.string.imm_cat_10_weeks),
        DoseStageOption(ChildImmunizationCategory.WEEK_14, R.string.imm_cat_14_weeks),
        DoseStageOption(ChildImmunizationCategory.MONTH_9_12, R.string.imm_cat_9_12_months),
        DoseStageOption(ChildImmunizationCategory.MONTH_16_24, R.string.imm_cat_16_24_months),
        DoseStageOption(ChildImmunizationCategory.YEAR_5_6, R.string.imm_cat_5_6_years),
    )

    /** Localized Dose Stage label -> the category it filters on; `null` for "All" or unknown. */
    fun toCategory(localizedLabel: String): ChildImmunizationCategory? =
        doseStageOptions.firstOrNull { resources.getString(it.labelRes) == localizedLabel }
            ?.category
    private val pastRecords = vaccineDao.getBenWithImmunizationRecords(
        minDob = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.YEAR, -6)
        }.timeInMillis,
        maxDob = System.currentTimeMillis(),
    )

    /**
     * FLW-1144: search, Dose Stage and the quick bucket are three INDEPENDENT channels.
     * They previously shared one flow, so selecting a stage wiped the search term and vice
     * versa - which made "filters combine correctly" impossible.
     */
    private val searchText = MutableStateFlow("")
    private val doseStage = MutableStateFlow<ChildImmunizationCategory?>(null)
    private val quickFilter = MutableStateFlow<QuickFilter?>(null)

    val selectedFilter = MutableLiveData<String?>(resources.getString(R.string.all))
    var selectedPosition = 0

    private val vaccinesFlow = MutableStateFlow<List<Vaccine>>(emptyList())
//    val benWithVaccineDetails = pastRecords.combine(vaccinesFlow) { vaccineIdList, vaccines ->
//        vaccineIdList.map { cache ->
//            val ageMillis = System.currentTimeMillis() - cache.ben.dob
//            ImmunizationDetailsDomain(
//                ben = cache.ben.asBasicDomainModel(),
//                vaccineStateList = vaccines.filter { it.minAllowedAgeInMillis < ageMillis }.map { vaccine ->
//                    val state = when {
//                        cache.givenVaccines.any { it.vaccineId == vaccine.vaccineId } -> VaccineState.DONE
//                        ageMillis <= vaccine.minAllowedAgeInMillis -> VaccineState.PENDING
//                        ageMillis <= vaccine.maxAllowedAgeInMillis -> VaccineState.OVERDUE
//                        else -> VaccineState.MISSED
//                    }
//                    VaccineDomain(vaccine.vaccineId, vaccine.vaccineName, vaccine.immunizationService, state)
//                }
//            )
//        }
//    }
val benWithVaccineDetails = pastRecords.combine(vaccinesFlow) { vaccineIdList, vaccines ->
    // FLW-1144: upcoming vaccines have to be in scope for "Scheduled for next month" to
    // mean anything. The old gate (minAllowedAgeInMillis < ageMillis) admitted only doses
    // already due, so any not-yet-due dose was invisible and that bucket would always
    // count 0. Widened by exactly one month - additive, so nothing previously listed drops
    // out, and cards do not balloon with doses years away.
    val endOfNextMonth = Calendar.getInstance().apply {
        add(Calendar.MONTH, 1)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    }.setToEndOfTheDay().timeInMillis
    vaccineIdList.map { cache ->
        val ageMillis = System.currentTimeMillis() - cache.ben.dob
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.ENGLISH)
        ImmunizationDetailsDomain(
            ben = cache.ben.asBasicDomainModel(),
            vaccineStateList = vaccines.filter {
                it.minAllowedAgeInMillis < ageMillis ||
                    cache.ben.dob + it.minAllowedAgeInMillis <= endOfNextMonth
            }.map { vaccine ->
                val vaccineType = vaccine.vaccineName.toVaccineType()
                val buffersApplicableVaccines = setOf(
                    VaccineType.HEPB_BIRTH,
                    VaccineType.OPV_0,
                    VaccineType.VIT_K
                )
                val oneMonthBufferMillis = 30L * 24 * 60 * 60 * 1000
                val effectiveMaxAllowedAgeInMillis =
                    if (vaccineType in buffersApplicableVaccines) {
                        vaccine.maxAllowedAgeInMillis + oneMonthBufferMillis
                    } else {
                        vaccine.maxAllowedAgeInMillis
                    }

                val state = when {
                    cache.givenVaccines.any { it.vaccineId == vaccine.vaccineId } -> VaccineState.DONE
                    ageMillis <= vaccine.minAllowedAgeInMillis -> VaccineState.PENDING
                    ageMillis <= effectiveMaxAllowedAgeInMillis -> VaccineState.OVERDUE
                    else -> VaccineState.MISSED
                }
                // NEW - due date calculate karo
                val dueDateMillis = cache.ben.dob + vaccine.minAllowedAgeInMillis
                val dueDateStr = sdf.format(java.util.Date(dueDateMillis))

                VaccineDomain(
                    vaccineId = vaccine.vaccineId,
                    vaccineName = vaccine.vaccineName,
                    vaccineCategory = vaccine.immunizationService,
                    state = state,
                    dueDate = dueDateStr,
                    dueDateMillis = dueDateMillis
                )
            }
        )
    }
}
    // init: populate vaccinesFlow
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val vaccines = vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD)
            vaccinesFlow.emit(vaccines)
        }
    }

    /*    val benWithVaccineDetails = pastRecords.map { vaccineIdList ->
            vaccineIdList.map { cache ->
                val ageMillis = System.currentTimeMillis() - cache.ben.dob
                ImmunizationDetailsDomain(ben = cache.ben.asBasicDomainModel(),
                    vaccineStateList = vaccinesList.filter {
                        it.minAllowedAgeInMillis < ageMillis
                    }.map { vaccine ->
                        VaccineDomain(
                            vaccine.vaccineId,
                            vaccine.vaccineName,
                            vaccine.immunizationService,
                            if (cache.givenVaccines.any { it.vaccineId == vaccine.vaccineId }) VaccineState.DONE
                            else if (ageMillis <= (vaccine.minAllowedAgeInMillis)) {
                                VaccineState.PENDING
                            } else if (ageMillis <= (vaccine.maxAllowedAgeInMillis)) {
                                VaccineState.OVERDUE
                            } else VaccineState.MISSED
                        )
                    })
            }
        }*/

    // ── FLW-1144 filter predicates ────────────────────────────────────────────────────
    // Semantics taken from the ticket's prototype. Note the two buckets are deliberately
    // NOT symmetric: "due this month" is open-ended to the left (already-passed due dates
    // count as outstanding work), "next month" is a strict calendar month.

    /** Not administered, and due on or before the last day of the current month. */
    private fun isDueThisMonth(vaccine: VaccineDomain, now: Calendar): Boolean {
        if (vaccine.state == VaccineState.DONE) return false
        val monthEnd = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.setToEndOfTheDay().timeInMillis
        return vaccine.dueDateMillis <= monthEnd
    }

    /** Not administered, and due inside the next calendar month. */
    private fun isNextMonth(vaccine: VaccineDomain, now: Calendar): Boolean {
        if (vaccine.state == VaccineState.DONE) return false
        val next = (now.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        val due = Calendar.getInstance().apply { timeInMillis = vaccine.dueDateMillis }
        return due.get(Calendar.YEAR) == next.get(Calendar.YEAR) &&
            due.get(Calendar.MONTH) == next.get(Calendar.MONTH)
    }

    private fun inDose(vaccine: VaccineDomain, dose: ChildImmunizationCategory?) =
        dose == null || vaccine.vaccineCategory == dose

    /**
     * Dose Stage and the quick bucket must be satisfied by the SAME vaccine - a child with a
     * 6-week dose due next month and a 9-12-month dose due this month must not match
     * "9-12 Months" + "next month". Two independent passes would wrongly match them.
     */
    private fun matchesDoseAndBucket(
        ben: ImmunizationDetailsDomain,
        dose: ChildImmunizationCategory?,
        bucket: QuickFilter?,
        now: Calendar,
    ): Boolean = when (bucket) {
        QuickFilter.DUE_THIS_MONTH ->
            ben.vaccineStateList.any { inDose(it, dose) && isDueThisMonth(it, now) }

        QuickFilter.NEXT_MONTH ->
            ben.vaccineStateList.any { inDose(it, dose) && isNextMonth(it, now) }

        null -> dose == null || ben.vaccineStateList.any { inDose(it, dose) }
    }

    private fun applyFilters(
        list: List<ImmunizationDetailsDomain>,
        search: String,
        dose: ChildImmunizationCategory?,
        bucket: QuickFilter?,
    ): List<ImmunizationDetailsDomain> {
        val now = Calendar.getInstance()
        val dueOnly = if (showDueOnly) {
            list.filter { ben -> ben.vaccineStateList.any { it.state == VaccineState.OVERDUE } }
        } else {
            list
        }
        return filterImmunList(dueOnly, search)
            .filter { matchesDoseAndBucket(it, dose, bucket, now) }
    }

    val immunizationBenList = combine(
        benWithVaccineDetails, searchText, doseStage, quickFilter
    ) { list, search, dose, bucket ->
        applyFilters(list, search, dose, bucket)
    }

    /**
     * Card counts. Each honours the live search and Dose Stage but ignores the other card,
     * so selecting one bucket does not change the other's number.
     */
    val dueThisMonthCount = combine(
        benWithVaccineDetails, searchText, doseStage
    ) { list, search, dose ->
        applyFilters(list, search, dose, QuickFilter.DUE_THIS_MONTH).size
    }

    val nextMonthCount = combine(
        benWithVaccineDetails, searchText, doseStage
    ) { list, search, dose ->
        applyFilters(list, search, dose, QuickFilter.NEXT_MONTH).size
    }

    /** "12 beneficiaries · Due for this month · 9-12 Months" */
    val resultMeta = combine(
        immunizationBenList, doseStage, quickFilter
    ) { list, dose, bucket ->
        buildString {
            append(
                resources.getQuantityString(
                    R.plurals.imm_beneficiary_count, list.size, list.size
                )
            )
            when (bucket) {
                QuickFilter.DUE_THIS_MONTH ->
                    append(" · ").append(resources.getString(R.string.imm_due_this_month))

                QuickFilter.NEXT_MONTH ->
                    append(" · ").append(resources.getString(R.string.imm_scheduled_next_month))

                null -> Unit
            }
            dose?.let { selected ->
                doseStageOptions.firstOrNull { it.category == selected }?.let {
                    append(" · ").append(resources.getString(it.labelRes))
                }
            }
        }
    }

    /** True when a filter is narrowing the list, so the empty state can say so. */
    val isFiltered = combine(searchText, doseStage, quickFilter) { search, dose, bucket ->
        search.isNotBlank() || dose != null || bucket != null
    }

    fun setSearchText(text: String) {
        viewModelScope.launch {
            searchText.emit(text.trim().lowercase())
        }
    }

    fun setDoseStage(category: ChildImmunizationCategory?) {
        viewModelScope.launch {
            doseStage.emit(category)
        }
    }

    /** Single-select: re-selecting the active bucket clears it. */
    fun toggleQuickFilter(filter: QuickFilter) {
        viewModelScope.launch {
            quickFilter.emit(if (quickFilter.value == filter) null else filter)
        }
    }

    val selectedQuickFilter: QuickFilter? get() = quickFilter.value

    private val clickedBenId = MutableStateFlow(0L)

    val bottomSheetContent = clickedBenId.combine(benWithVaccineDetails) { a, b ->
        b.firstOrNull { it.ben.benId == a }

    }

    /* init {
         viewModelScope.launch {
             withContext(Dispatchers.IO) {
                 vaccinesList = vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD)
             }
         }
     }*/

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            clickedBenId.emit(benId)
        }
    }


    private val catList = ArrayList<String>()

    fun categoryData(): ArrayList<String> {
        catList.clear()
        doseStageOptions.mapTo(catList) { resources.getString(it.labelRes) }
        return catList
    }

    fun getSelectedBenId(): Long {
        return clickedBenId.value
    }

    val isSelectedBenDeathFlow = clickedBenId.combine(benWithVaccineDetails) { benId, list ->
        list.firstOrNull { it.ben.benId == benId }?.ben?.isDeath ?: false
    }
}
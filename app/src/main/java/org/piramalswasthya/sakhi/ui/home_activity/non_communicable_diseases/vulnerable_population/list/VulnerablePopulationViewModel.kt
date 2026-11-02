package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.vulnerable_population.list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.filterTbScreeningList
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.CommonConstants
import org.piramalswasthya.sakhi.utils.HelperUtil.getLocalizedResources
import javax.inject.Inject

@HiltViewModel
class VulnerablePopulationViewModel @Inject constructor(
    recordsRepo: RecordsRepo,
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    @ApplicationContext private val context: Context,

    ) : ViewModel() {

    private val resources get() = getLocalizedResources(context, preferenceDao.getCurrentLanguage())

    private val allBenList = recordsRepo.tbScreeningRiskfactorListforVulneravility

     data class CodedOption(val id: Int, val code: String, val label: String)

    private val riskCodeFilter = MutableStateFlow("")

    private val filter = MutableStateFlow("")

    val benList = combine(allBenList, filter, riskCodeFilter) { list, search, riskCode ->
        val searched = filterTbScreeningList(list, search)
        if (riskCode.isEmpty()) {
            searched
        } else {
            searched.filter { item ->
                item.tb?.keyPopulationRiskFactors.orEmpty()
                    .any { it.trim().equals(riskCode, ignoreCase = true) }
            }
        }
    }

    fun filterRiskCode(code: String?) {
        viewModelScope.launch {
            riskCodeFilter.emit(code.orEmpty().trim())
        }
    }
     fun masterRiskFactorOptions(): List<CodedOption> {

        val labels = resources.getStringArray(
            R.array.key_population_risk_factor_options
        )

        return labels.mapIndexed { index, label ->
            CodedOption(
                index + 1,
                CommonConstants.RISK_FACTOR_CODES.getOrElse(index) {
                    label.uppercase().replace(" ", "_")
                },
                label
            )
        }
    }

    val selectedFilter = MutableLiveData<String?>(resources.getString(R.string.all))




    private val englishCategories = listOf(
        "ALL",
        "Pregnancy",
        "Lactating mother",
        "Anti-TNF treatment",
        "Bronchial Asthma",
        "Cancer",
        "Cardiovascular Disorder",
        "Contact of Known TB Patients",
        "COPD",
        "COVID recovered patients",
        "Diabetes",
        "Dialysis",
        "Health Care Worker",
        "Hypertensive",
        "Liver Impairment",
        "Migrant",
        "Miner",
        "Palliative Care",
        "Patient on immunosuppressants",
        "Prison",
        "Illegal Immigrant",
        "Renal Impairment",
        "Transplantation",
        "Urban Slum",
        "H/o Adult BCG Vaccination",
        "Undernourished / Malnourished (BMI <18.5 kg/m2)",
        "Elderly (age >60 years)",
        "Workplace settings (coal/sandblasting/brick kiln)",
        "Tea garden worker",
        "Construction site worker",
        "Congregate settings",
        "Attendees of de-addiction centers",
        "Person exposed to indoor air pollution",
        "Marginalized populations at risk of HIV",
        "LGBTQAI++",
        "Substance abuse (alcoholic/intravenous drug users)",
        "Tobacco/smoker",
        "Silica exposure/silicosis",
        "Other",
        "Not applicable"
    )

    private fun localizedCategories(): List<String> =
        listOf(resources.getString(R.string.all)) +
                resources.getStringArray(R.array.key_population_risk_factor_options).toList()

    fun toEnglishCategory(localized: String): String {
        val idx = localizedCategories().indexOf(localized)
        return if (idx >= 0) englishCategories[idx] else ""
    }

    fun toFilterKey(localized: String): String {
        val idx = localizedCategories().indexOf(localized)
        if (idx <= 0) return ""
        return CommonConstants.RISK_FACTOR_CODES.getOrElse(idx - 1) { englishCategories[idx] }
    }

    var selectedPosition = 0

    fun filterText(text: String) {
        viewModelScope.launch {
            filter.emit(text.trim().lowercase())
        }

    }

    private val clickedBenId = MutableStateFlow(0L)

    fun updateBottomSheetData(benId: Long) {
        viewModelScope.launch {
            clickedBenId.emit(benId)
        }
    }
    private val catList = ArrayList<String>()



    fun categoryData(): ArrayList<String> {

        catList.clear()
        catList.add(resources.getString(R.string.all))
        catList.add(resources.getString(R.string.kp_risk_pregnancy))
        catList.add(resources.getString(R.string.kp_risk_lactating_mother))
        catList.add(resources.getString(R.string.kp_risk_anti_tnf_treatment))
        catList.add(resources.getString(R.string.kp_risk_bronchial_asthma))
        catList.add(resources.getString(R.string.kp_risk_cancer))
        catList.add(resources.getString(R.string.kp_risk_cardiovascular_disorder))
        catList.add(resources.getString(R.string.kp_risk_contact_of_known_tb_patients))
        catList.add(resources.getString(R.string.kp_risk_copd))
        catList.add(resources.getString(R.string.kp_risk_covid_recovered_patients))
        catList.add(resources.getString(R.string.kp_risk_diabetes))
        catList.add(resources.getString(R.string.kp_risk_dialysis))
        catList.add(resources.getString(R.string.kp_risk_health_care_worker))
        catList.add(resources.getString(R.string.kp_risk_hypertensive))
        catList.add(resources.getString(R.string.kp_risk_liver_impairment))
        catList.add(resources.getString(R.string.kp_risk_migrant))
        catList.add(resources.getString(R.string.kp_risk_miner))
        catList.add(resources.getString(R.string.kp_risk_palliative_care))
        catList.add(resources.getString(R.string.kp_risk_patient_on_immunosuppressants))
        catList.add(resources.getString(R.string.kp_risk_prison))
        catList.add(resources.getString(R.string.kp_risk_illegal_immigrant))
        catList.add(resources.getString(R.string.kp_risk_renal_impairment))
        catList.add(resources.getString(R.string.kp_risk_transplantation))
        catList.add(resources.getString(R.string.kp_risk_urban_slum))
        catList.add(resources.getString(R.string.kp_risk_ho_adult_bcg_vaccination))
        catList.add(resources.getString(R.string.kp_risk_undernourished_malnourished))
        catList.add(resources.getString(R.string.kp_risk_elderly))
        catList.add(resources.getString(R.string.kp_risk_workplace_settings))
        catList.add(resources.getString(R.string.kp_risk_tea_garden_worker))
        catList.add(resources.getString(R.string.kp_risk_construction_site_worker))
        catList.add(resources.getString(R.string.kp_risk_congregate_settings))
        catList.add(resources.getString(R.string.kp_risk_attendees_of_de_addiction_centers))
        catList.add(resources.getString(R.string.kp_risk_indoor_air_pollution))
        catList.add(resources.getString(R.string.kp_risk_marginalized_hiv))
        catList.add(resources.getString(R.string.kp_risk_lgbtqai))
        catList.add(resources.getString(R.string.kp_risk_substance_abuse))
        catList.add(resources.getString(R.string.kp_risk_tobacco_smoker))
        catList.add(resources.getString(R.string.kp_risk_silica_exposure_silicosis))
        catList.add(resources.getString(R.string.kp_risk_other))
        catList.add(resources.getString(R.string.not_applicable))

        return catList
    }


}
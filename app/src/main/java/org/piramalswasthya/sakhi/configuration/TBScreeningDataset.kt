package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.utils.CommonConstants

class TBScreeningDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var benAgeYears: Int = 0

    private val yesValue get() = resources.getStringArray(R.array.yes_no)[0]
    private val noValue  get() = resources.getStringArray(R.array.yes_no)[1]

    private val symptomaticLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.symptomatic_tb_screening),
        required = false
    )

    private val checkSymptomsLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.check_if_the_person_has_any_of_these_symptoms),
        required = false
    )
    private val dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val isCoughing = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_coughing),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var bloodInSputum = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_blsputum),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var isFever = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_feverwks),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var lossOfWeight = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_lsweight),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var nightSweats = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_ntswets),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var historyOfTB = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_histb),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var currentlyTakingDrugs = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_taking_tb_drug),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        doubleStar = true,
        hasDependants = true
    )

    private var familyHistoryTB = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_fh_tb),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = true
    )

    private var riseOfFever = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_rise_of_fever),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = true
    )


    private var lossOfAppetite = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_loss_of_appetite),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = true
    )

    private val headingTbHistory = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.cbac_histb),
        required = false
    )
    private val aSymptomaticLabel = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_asymptomatic_symptoms),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true,
        isEnabled = false
    )
    private val checkSymptomsLabel1 = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.check_if_the_person_has_any_of_these_symptoms),
        required = false
    )
    private var age = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_age_more_than_60),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var diabetic = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_diabetic),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var tobaccoUser = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_tobacco_user),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var bmi = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_bmi_less_than_18_5),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var contactWithTBPatient = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_contact_with_patient_on_treatment),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var historyOfTBInLastFiveYrs = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tb_history_last_5_years),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )

    private var shortageOfBreath = FormElement(
        id = 20,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_breath),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = false,
        required = true,
        hasDependants = true
    )

    private var fatigue = FormElement(
        id = 21,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.fatigue),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = false,
        required = true,
        hasDependants = true
    )

    private var chestPain = FormElement(
        id = 22,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.chestpain),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = false,
        required = true,
        hasDependants = true
    )


    private data class CodedOption(val id: Int, val code: String, val label: String)

    private val riskFactorCodes = CommonConstants.RISK_FACTOR_CODES

    private fun masterRiskFactorOptions(): List<CodedOption> {
        val labels = resources.getStringArray(R.array.key_population_risk_factor_options)
        return labels.mapIndexed { index, label ->
            CodedOption(index + 1, riskFactorCodes.getOrElse(index) { label.uppercase().replace(" ", "_") }, label)
        }
    }

    private var isMaleBen: Boolean = false
    private var isPregnantBen: Boolean = false
    private var riskFactorOptions: List<CodedOption> = emptyList()

    private val hivStatusOptions: List<CodedOption>
        get() = listOf(
            CodedOption(1, "POSITIVE", resources.getString(R.string.positive)),
            CodedOption(2, "REACTIVE", resources.getString(R.string.reactive)),
            CodedOption(3, "NEGATIVE", resources.getString(R.string.negative)),
            CodedOption(4, "UNKNOWN", resources.getString(R.string.unknown))
        )

    private val riskFactorsHeading = FormElement(
        id = 17,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.risk_factors),
        required = false
    )

    private val keyPopulationRiskFactors = FormElement(
        id = 18,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.key_population_risk_factors),
        entries = emptyArray(),
        required = true,
        showAsMultiSelectDialog = true,
        enableSearchInMultiSelect = true
    )

    private val hivStatus = FormElement(
        id = 19,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.hiv_status),
        entries = emptyArray(),
        required = true
    )


    suspend fun setUpPage(ben: BenRegCache?, saved: TBScreeningCache?) {
        val list = mutableListOf(
            dateOfVisit,
            isCoughing,
            bloodInSputum,
            isFever,
            riseOfFever,
            lossOfAppetite,
            lossOfWeight,
            nightSweats,
            chestPain,
            shortageOfBreath,
            fatigue,
            headingTbHistory,
            historyOfTB,
            currentlyTakingDrugs,
            familyHistoryTB,
            aSymptomaticLabel,
            riskFactorsHeading,
            keyPopulationRiskFactors,
            hivStatus
        )



        ben?.let {
            dateOfVisit.min = it.regDate
            benAgeYears = if (it.dob > 0L) BenBasicCache.getAgeFromDob(it.dob) else it.age
            isMaleBen = it.gender == Gender.MALE
            val reproductiveStatus = it.genDetails?.reproductiveStatus
            isPregnantBen = it.genDetails?.reproductiveStatusId == 1 ||
                    reproductiveStatus.equals("Yes", ignoreCase = true)
        }
        riskFactorOptions = masterRiskFactorOptions().let { all ->
            if (isMaleBen) all.filter { it.code != "PREGNANCY" && it.code != "LACTATING_MOTHER" } else all
        }
        keyPopulationRiskFactors.entries = riskFactorOptions.map { it.label }.toTypedArray()
        val notApplicableIndex = riskFactorOptions.indexOfFirst { it.code == "NOT_APPLICABLE" }
        keyPopulationRiskFactors.exclusiveOptionIndices =
            if (notApplicableIndex >= 0) setOf(notApplicableIndex) else null
        hivStatus.entries = hivStatusOptions.map { it.label }.toTypedArray()


        if (saved == null) {
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
            val pregnancyIndex = riskFactorOptions.indexOfFirst { it.code == "PREGNANCY" }

            keyPopulationRiskFactors.value = when {
                isPregnantBen && pregnancyIndex >= 0 -> pregnancyIndex.toString()
                else -> null
            }
            hivStatus.value = null
        } else {
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            isCoughing.value = boolToYesNo(saved.coughMoreThan2Weeks)
            bloodInSputum.value = boolToYesNo(saved.bloodInSputum)
            isFever.value = boolToYesNo(saved.feverMoreThan2Weeks)
            lossOfWeight.value = boolToYesNo(saved.lossOfWeight)
            nightSweats.value = boolToYesNo(saved.nightSweats)
            historyOfTB.value = boolToYesNo(saved.historyOfTb)
            currentlyTakingDrugs.value = boolToYesNo(saved.takingAntiTBDrugs)
            familyHistoryTB.value = boolToYesNo(saved.familySufferingFromTB)
            riseOfFever.value = boolToYesNo(saved.riseOfFever)
            lossOfAppetite.value = boolToYesNo(saved.lossOfAppetite)
            age.value =
                if (saved.age == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            diabetic.value =
                if (saved.diabetic == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            tobaccoUser.value =
                if (saved.tobaccoUser == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            bmi.value =
                if (saved.bmi == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            contactWithTBPatient.value =
                if (saved.contactWithTBPatient == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]

            fatigue.value = boolToYesNo(saved.fatigue)
            chestPain.value = boolToYesNo(saved.chestPain)
            shortageOfBreath.value = boolToYesNo(saved.shortBreath)

            historyOfTBInLastFiveYrs.value =
                if (saved.historyOfTBInLastFiveYrs == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]

            val savedIds = saved.keyPopulationRiskFactorIds.orEmpty()
            val savedCodes = saved.keyPopulationRiskFactors.orEmpty()
            val selectedIndexes = riskFactorOptions.mapIndexedNotNull { index, option ->
                val matches = savedIds.contains(option.id) ||
                        savedCodes.any { it.equals(option.code, true) || it.equals(option.label, true) }
                if (matches) index else null
            }
            keyPopulationRiskFactors.value =
                if (selectedIndexes.isEmpty()) null else selectedIndexes.sorted().joinToString("|")

            hivStatus.value = hivStatusOptions.firstOrNull {
                it.id == saved.hivStatusId ||
                        saved.hivStatus.equals(it.code, true) ||
                        saved.hivStatus.equals(it.label, true)
            }?.label
        }
        aSymptomaticLabel.value = computeAsymptomaticValue()
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        if (!isAsymptomaticDriver(formId)) return -1
        aSymptomaticLabel.value = computeAsymptomaticValue()
        return listFlow.value.indexOf(aSymptomaticLabel).takeIf { it >= 0 } ?: -1
    }

    fun isAsymptomaticDriver(formId: Int): Boolean = formId in setOf(
        isCoughing.id,
        bloodInSputum.id,
        isFever.id,
        riseOfFever.id,
        lossOfAppetite.id,
        lossOfWeight.id,
        nightSweats.id,
        chestPain.id,
        shortageOfBreath.id,
        fatigue.id,
        historyOfTB.id,
        currentlyTakingDrugs.id,
        familyHistoryTB.id
    )

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as TBScreeningCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.coughMoreThan2Weeks =
                isCoughing.value == resources.getStringArray(R.array.yes_no)[0]
            form.bloodInSputum = bloodInSputum.value == resources.getStringArray(R.array.yes_no)[0]
            form.feverMoreThan2Weeks = isFever.value == resources.getStringArray(R.array.yes_no)[0]
            form.nightSweats = nightSweats.value == resources.getStringArray(R.array.yes_no)[0]
            form.lossOfWeight = lossOfWeight.value == resources.getStringArray(R.array.yes_no)[0]
            form.historyOfTb = historyOfTB.value == resources.getStringArray(R.array.yes_no)[0]
            form.takingAntiTBDrugs =
                currentlyTakingDrugs.value == resources.getStringArray(R.array.yes_no)[0]
            form.familySufferingFromTB =
                familyHistoryTB.value == resources.getStringArray(R.array.yes_no)[0]
            form.riseOfFever = riseOfFever.value == resources.getStringArray(R.array.yes_no)[0]
            form.lossOfAppetite =
                lossOfAppetite.value == resources.getStringArray(R.array.yes_no)[0]
            // These questions are no longer part of the TB screening form.
            form.age = null
            form.diabetic = null
            form.tobaccoUser = null
            form.bmi = null
            form.contactWithTBPatient = null
            form.historyOfTBInLastFiveYrs = null
            form.sympotomatic = null
            form.asymptomatic = aSymptomaticLabel.value
            form.recommandateTest = null

            val selectedRiskFactors = keyPopulationRiskFactors.value
                ?.split("|")?.mapNotNull { it.toIntOrNull() }
                ?.mapNotNull { riskFactorOptions.getOrNull(it) }
                .orEmpty()
            form.keyPopulationRiskFactorIds = selectedRiskFactors.map { it.id }.takeIf { it.isNotEmpty() }
            form.keyPopulationRiskFactors = selectedRiskFactors.map { it.code }.takeIf { it.isNotEmpty() }

            val selectedHiv = hivStatusOptions.firstOrNull { it.label == hivStatus.value }
            form.fatigue = fatigue.value == resources.getStringArray(R.array.yes_no)[0]
            form.shortBreath = shortageOfBreath.value == resources.getStringArray(R.array.yes_no)[0]
            form.chestPain = chestPain.value == resources.getStringArray(R.array.yes_no)[0]
            form.hivStatusId = selectedHiv?.id
            form.hivStatus = selectedHiv?.code
        }
    }


    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array2)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
    }

    fun referHwcFacility():String?{
        val hasSingleStarSymptom = isCoughing.value == yesValue ||
            bloodInSputum.value == yesValue ||
            isFever.value == yesValue ||
            riseOfFever.value == yesValue ||
            lossOfAppetite.value == yesValue ||
            lossOfWeight.value == yesValue ||
            nightSweats.value == yesValue ||
            chestPain.value == yesValue || shortageOfBreath.value == yesValue ||
            fatigue.value == yesValue || historyOfTB.value == yesValue
        val hasDoubleStarSymptom = currentlyTakingDrugs.value == yesValue || familyHistoryTB.value == yesValue
        return if (hasSingleStarSymptom || hasDoubleStarSymptom
        )
            resources.getString(R.string.refer_to_hwc_facility_alert) else null

    }

    private fun computeAsymptomaticValue(): String? {
        val symptomAnswers = listOf(
            isCoughing, bloodInSputum, isFever, riseOfFever, lossOfAppetite,
            lossOfWeight, nightSweats, chestPain, shortageOfBreath, fatigue,
            historyOfTB, currentlyTakingDrugs, familyHistoryTB
        )
        return when {
            symptomAnswers.any { it.value == yesValue } -> noValue
            symptomAnswers.all { it.value == noValue } -> yesValue
            else -> null
        }
    }

    private fun boolToYesNo(value: Boolean?): String = when (value) {
        true -> yesValue
        false -> noValue
        null -> ""
    }

    fun isTbSuspected(): String? {
        return if (isCoughing.value == resources.getStringArray(R.array.yes_no)[0] ||
            bloodInSputum.value == resources.getStringArray(R.array.yes_no)[0] ||
            isFever.value == resources.getStringArray(R.array.yes_no)[0] ||
            nightSweats.value == resources.getStringArray(R.array.yes_no)[0] ||
            lossOfWeight.value == resources.getStringArray(R.array.yes_no)[0] ||
            historyOfTB.value == resources.getStringArray(R.array.yes_no)[0]||

            riseOfFever.value == resources.getStringArray(R.array.yes_no)[0] ||
            lossOfAppetite.value == resources.getStringArray(R.array.yes_no)[0] ||
            age.value == resources.getStringArray(R.array.yes_no)[0] ||
            diabetic.value == resources.getStringArray(R.array.yes_no)[0] ||
            tobaccoUser.value == resources.getStringArray(R.array.yes_no)[0] ||
            bmi.value == resources.getStringArray(R.array.yes_no)[0] ||
            contactWithTBPatient.value == resources.getStringArray(R.array.yes_no)[0] ||
            historyOfTBInLastFiveYrs.value == resources.getStringArray(R.array.yes_no)[0]
        )
            resources.getString(R.string.tb_suspected_alert) else null
    }

    fun isTbSuspectedFamily(): String? {
        return if (currentlyTakingDrugs.value == resources.getStringArray(R.array.yes_no)[0] || familyHistoryTB.value == resources.getStringArray(
                R.array.yes_no
            )[0]
        )
            resources.getString(R.string.tb_suspected_family_alert) else null
    }

    fun getIndexOfDate(): Int {
        return getIndexById(dateOfVisit.id)
    }
    fun getIndexOfAsymptomatic(): Int = listFlow.value.indexOf(aSymptomaticLabel)
}

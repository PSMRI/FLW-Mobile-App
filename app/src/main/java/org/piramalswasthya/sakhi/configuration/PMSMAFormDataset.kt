package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants.english
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.HEADLINE
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import org.piramalswasthya.sakhi.model.PMSMACache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class PMSMAFormDataset(
    context: Context,  currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private fun getLongFromDate(dateString: String): Long {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
        }
    }

    private val mctsNumberOrRchNumber = FormElement(
        id = 1,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.pmsma_mcts_rch_number),
        required = false
    )
    private val haveMCPCard = FormElement(
        id = 2,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_have_mcp_card),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        hasDependants = true,
        required = false
    )
    val givenMCPCard = FormElement(
        id = 3,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_given_mcp_card),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )

    private val husbandName = FormElement(
        id = 4,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.pmsma_husband_name),
        required = false
    )
    private val address = FormElement(
        id = 5,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_address),
        required = false
    )
    private val mobileNumber = FormElement(
        id = 6,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.pmsma_mobile_number),
        required = false
    )
    private val numANC = FormElement(
        id = 7,
        inputType = EDIT_TEXT,
        etMaxLength = 2,
        title = resources.getString(R.string.pmsma_num_anc),
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val motherStatus = FormElement(
        id = 8,
        inputType = HEADLINE,
        title = resources.getString(R.string.pmsma_mother_status),
        required = false
    )
    private val weight = FormElement(
        id = 9,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_weight),
        min = 30,
        max = 200,
        etMaxLength = 3,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val bp = FormElement(
        id = 10,
        inputType = EDIT_TEXT,
        title = "BP of PW – Systolic/ Diastolic (mm Hg) ",
//        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 7,
        required = false,
    )
    private val abdominalCheckUp = FormElement(
        id = 12,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.pmsma_abdominal_check_up),
        required = false
    )
    private val fetalStatus = FormElement(
        id = 13,
        inputType = HEADLINE,
        title = resources.getString(R.string.pmsma_fetal_status),
        required = false
    )
    private val fetalHRPM = FormElement(
        id = 14,
        inputType = EDIT_TEXT,
        etMaxLength = 3,
        title = resources.getString(R.string.pmsma_fetal_hrpm),
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val twinPregnancy = FormElement(
        id = 15,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_twin_pregnancy),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val investigations = FormElement(
        id = 16,
        inputType = HEADLINE,
        title = resources.getString(R.string.pmsma_investigation),
        required = false
    )
    private val urineAlbumin = FormElement(
        id = 17,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_urine_albumin),
        required = false
    )
    private val haemoglobinAndBloodGroup = FormElement(
        id = 18,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_haemoglobin_and_blood_group),
        required = false
    )
    private val hiv = FormElement(
        id = 19,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_hiv),
        required = false
    )
    private val vdrl = FormElement(
        id = 20,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_vdrl),
        required = false
    )
    private val hbsc = FormElement(
        id = 21,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_hbsc),
        required = false
    )
    private val malaria = FormElement(
        id = 22,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_malaria),
        required = false
    )
    private val hivTestDuringANC = FormElement(
        id = 23,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_hiv_test_during_anc),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val swollenCondtion = FormElement(
        id = 24,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_swollen_condition),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val bloodSugarTest = FormElement(
        id = 25,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_blood_sugar_test),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val ultraSound = FormElement(
        id = 26,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_ultrasound),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val interventionDetails = FormElement(
        id = 27,
        inputType = HEADLINE,
        title = resources.getString(R.string.pmsma_intervention_details),
        required = false
    )
    private val ironFolicAcid = FormElement(
        id = 28,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_iron_folic_acid),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val calciumSupplementation = FormElement(
        id = 29,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_calcium_supplementation),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val tetanusToxoid = FormElement(
        id = 30,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_tetanus_toxoid),
        entries = resources.getStringArray(R.array.pmsma_tetanus_toxoid_array),
        required = false
    )
    private val lmp = FormElement(
        id = 31,
        inputType = HEADLINE,
        title = resources.getString(R.string.pmsma_lmp),
        required = false
    )
    private val lastMenstrualPeriod = FormElement(
        id = 32,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.pmsma_last_mestrual_period),
        min = 0L,
        max = System.currentTimeMillis(),
        required = false
    )
    private val expectedDateOfDelivery = FormElement(
        id = 33,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.pmsma_expected_delivery_date),
        required = false
    )
    private val highRiskFactors = FormElement(
        id = 34,
        inputType = HEADLINE,
        title = resources.getString(R.string.pmsma_high_risk_factors),
        required = false
    )
    private val highriskSymbols = FormElement(
        id = 35,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_high_risk_symbols),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        hasDependants = true,
        required = false
    )
    private val highRiskReason = FormElement(
        id = 36,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_high_risk_reason),
        required = false
    )
    private val highRiskPregnant = FormElement(
        id = 37,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_high_risk_pregnant),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val highRiskPregnancyReferred = FormElement(
        id = 38,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_high_risk_pregnancy_referred),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val advice = FormElement(
        id = 39,
        inputType = HEADLINE,
        title = resources.getString(R.string.pmsma_advice),
        required = false
    )
    private val birthPrepAndNutritionAndFamilyPlanning = FormElement(
        id = 40,
        inputType = RADIO,
        title = resources.getString(R.string.pmsma_birth_complications_and_planning),
        entries = resources.getStringArray(R.array.pmsma_confirmation_array),
        required = false
    )
    private val medicalOfficerSign = FormElement(
        id = 41,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.pmsma_medical_officer_sign),
        required = false
    )

    suspend fun setUpFirstPage(
        household: HouseholdCache,
        ben: BenRegCache,
        pwr: PregnantWomanRegistrationCache,
        lastAnc: PregnantWomanAncCache?,
        pmsma: PMSMACache?
    ) {
        mctsNumberOrRchNumber.value = pwr.rchId?.toString()
        address.value = getAddress(household)
        mobileNumber.value = ben.contactNumber.toString()
        husbandName.value = ben.genDetails?.spouseName
        mctsNumberOrRchNumber.value = ben.rchId
        lastAnc?.let {
            "${(it.ancDate - pwr.lmpDate) / 7} Weeks"
        }
        lastMenstrualPeriod.value = getDateFromLong(pwr.lmpDate)
        expectedDateOfDelivery.value = getDateFromLong(getEddFromLmp(pwr.lmpDate))
        abdominalCheckUp.value = "${
            (TimeUnit.MILLISECONDS.toDays(
                Calendar.getInstance().setToStartOfTheDay().timeInMillis - pwr.lmpDate
            ) / 7)
        } Weeks"
        pmsma?.let { setExistingValues(it) }
        val list = firstPage
        setUpPage(list)
    }


    private val firstPage by lazy {
        listOf(
            mctsNumberOrRchNumber,
            haveMCPCard,
            husbandName,
            address,
            mobileNumber,
            numANC,
            motherStatus,
            weight,
            bp,
            abdominalCheckUp,
            fetalStatus,
            fetalHRPM,
            twinPregnancy,
            investigations,
            urineAlbumin,
            haemoglobinAndBloodGroup,
            hiv,
            vdrl,
            hbsc,
            malaria,
            hivTestDuringANC,
            swollenCondtion,
            bloodSugarTest,
            ultraSound,
            interventionDetails,
            ironFolicAcid,
            calciumSupplementation,
            tetanusToxoid,
            lmp,
            lastMenstrualPeriod,
            expectedDateOfDelivery,
            highRiskFactors,
            highriskSymbols,
            highRiskPregnant,
            highRiskPregnancyReferred,
            advice,
            birthPrepAndNutritionAndFamilyPlanning,
            medicalOfficerSign,

            )
    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PMSMACache).let { pmsmaCache ->
            pmsmaCache.mctsNumberOrRchNumber = mctsNumberOrRchNumber.value
            pmsmaCache.haveMCPCard = haveMCPCard.value == "Yes"
            pmsmaCache.husbandName = husbandName.value
            pmsmaCache.address = address.value
            pmsmaCache.mobileNumber = mobileNumber.value?.toLong() ?: 0L
            pmsmaCache.numANC = numANC.value?.toInt() ?: 0
            pmsmaCache.weight = weight.value?.toInt() ?: 0
            pmsmaCache.systolicBloodPressure =
                bp.value?.takeIf { it.isNotEmpty() }?.substringBefore("/")
            pmsmaCache.bloodPressure = bp.value?.takeIf { it.isNotEmpty() }?.substringAfter("/")
            pmsmaCache.abdominalCheckUp = abdominalCheckUp.value
            pmsmaCache.fetalHRPM = fetalHRPM.value?.toInt() ?: 0
            pmsmaCache.twinPregnancy = twinPregnancy.value == "Yes"
            pmsmaCache.urineAlbumin = urineAlbumin.value
            pmsmaCache.haemoglobinAndBloodGroup = haemoglobinAndBloodGroup.value
            pmsmaCache.hiv = hiv.value
            pmsmaCache.vdrl = vdrl.value
            pmsmaCache.hbsc = hbsc.value
            pmsmaCache.malaria = malaria.value
            pmsmaCache.hivTestDuringANC = hivTestDuringANC.value == "Yes"
            pmsmaCache.swollenCondtion = swollenCondtion.value == "Yes"
            pmsmaCache.bloodSugarTest = bloodSugarTest.value == "Yes"
            pmsmaCache.ultraSound = ultraSound.value == "Yes"
            pmsmaCache.ironFolicAcid = ironFolicAcid.value == "Yes"
            pmsmaCache.calciumSupplementation = calciumSupplementation.value == "Yes"
            pmsmaCache.tetanusToxoid = tetanusToxoid.value
            pmsmaCache.lastMenstrualPeriod =
                lastMenstrualPeriod.value?.let { getLongFromDate(it) } ?: 0L
            pmsmaCache.expectedDateOfDelivery = getLongFromDate(expectedDateOfDelivery.value!!)
            pmsmaCache.highriskSymbols = highriskSymbols.value == "Yes"
            pmsmaCache.highRiskReason = highRiskReason.value
            pmsmaCache.highRiskPregnant = highRiskPregnant.value == "Yes"
            pmsmaCache.highRiskPregnancyReferred = highRiskPregnancyReferred.value == "Yes"
            pmsmaCache.birthPrepAndNutritionAndFamilyPlanning =
                birthPrepAndNutritionAndFamilyPlanning.value == "Yes"
            pmsmaCache.medicalOfficerSign = medicalOfficerSign.value
        }

    }

    fun setExistingValues(pmsma: PMSMACache) {
        mctsNumberOrRchNumber.value = pmsma.mctsNumberOrRchNumber
        haveMCPCard.value = if (pmsma.haveMCPCard == true) "Yes" else "No"
        husbandName.value = pmsma.husbandName
        address.value = pmsma.address?.let { if (it.length > 100) it.substring(0, 100) else it }
        mobileNumber.value = pmsma.mobileNumber.toString()
        numANC.value = pmsma.numANC.toString()
        weight.value = pmsma.weight.toString()
        bp.value =
            if (pmsma.systolicBloodPressure == null || pmsma.bloodPressure == null) null else "${pmsma.systolicBloodPressure}/${pmsma.bloodPressure}"
        abdominalCheckUp.value = pmsma.abdominalCheckUp
        fetalHRPM.value = pmsma.fetalHRPM.toString()
        twinPregnancy.value = if (pmsma.twinPregnancy == true) "Yes" else "No"
        urineAlbumin.value = pmsma.urineAlbumin
        haemoglobinAndBloodGroup.value = pmsma.haemoglobinAndBloodGroup
        hiv.value = pmsma.hiv
        vdrl.value = pmsma.vdrl
        hbsc.value = pmsma.hbsc
        malaria.value = pmsma.malaria
        hivTestDuringANC.value = if (pmsma.hivTestDuringANC == true) "Yes" else "No"
        swollenCondtion.value = if (pmsma.swollenCondtion == true) "Yes" else "No"
        bloodSugarTest.value = if (pmsma.bloodSugarTest == true) "Yes" else "No"
        ultraSound.value = if (pmsma.ultraSound == true) "Yes" else "No"
        ironFolicAcid.value = if (pmsma.ironFolicAcid == true) "Yes" else "No"
        calciumSupplementation.value =
            if (pmsma.calciumSupplementation == true) "Yes" else "No"
        tetanusToxoid.value = pmsma.tetanusToxoid
        lastMenstrualPeriod.value = pmsma.lastMenstrualPeriod.let { getDateFromLong(it) }
        expectedDateOfDelivery.value = pmsma.expectedDateOfDelivery.let { getDateFromLong(it) }
        highriskSymbols.value = if (pmsma.highriskSymbols == true) "Yes" else "No"
        highRiskReason.value = pmsma.highRiskReason
        highRiskPregnant.value = if (pmsma.highRiskPregnant == true) "Yes" else "No"
        highRiskPregnancyReferred.value =
            if (pmsma.highRiskPregnancyReferred == true) "Yes" else "No"
        birthPrepAndNutritionAndFamilyPlanning.value =
            if (pmsma.birthPrepAndNutritionAndFamilyPlanning == true) "Yes" else "No"
        medicalOfficerSign.value = pmsma.medicalOfficerSign
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            haveMCPCard.id -> {
                triggerDependants(
                    source = haveMCPCard,
                    passedIndex = index,
                    triggerIndex = 1,
                    target = givenMCPCard
                )
            }

            highriskSymbols.id -> {
                triggerDependants(
                    source = highriskSymbols,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = highRiskReason
                )
            }

            mobileNumber.id -> validateMobileNumberOnEditText(mobileNumber)
            husbandName.id -> {
                if (currentLanguage.toString() == english) {
                   // validateAllCapsOrSpaceOnEditText(husbandName)
                    validateAllCapsOrSpaceOnEditTextWithHindiEnabled(husbandName)
                } else -1
            }
            bp.id -> validateForBp(bp)
            weight.id -> validateIntMinMax(weight)


            else -> -1
        }
    }


    private fun getAddress(household: HouseholdCache): String {
        val houseNo = household.family?.houseNo
        val wardNo = household.family?.wardNo
        val name = household.family?.wardName
        val mohalla = household.family?.mohallaName
        val district = household.locationRecord.district.name
        val city = household.locationRecord.village.name
        val state = household.locationRecord.state.name

        var address = "$houseNo, $wardNo, $name, $mohalla, $city, $district, $state"
        address = address.replace(", ,", ",")
        address = address.replace(",,", ",")
        address = address.replace(" ,", "")
        address = address.replace("null, ", "")
        address = address.replace(", null", "")

        return address
    }


}
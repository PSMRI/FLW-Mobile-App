package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.TBScreeningCache

class TBScreeningDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val symptomaticLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = "Symptomatic TB Screening",
        required = false
    )

    private val checkSymptomsLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = "Check if the person has any of these Symptoms:",
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
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_coughing),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var bloodInSputum = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_blsputum),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var isFever = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_feverwks),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var lossOfWeight = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_lsweight),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var nightSweats = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_ntswets),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var historyOfTB = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_histb),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var currentlyTakingDrugs = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_taking_tb_drug),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        doubleStar = true,
        hasDependants = false
    )

    private var familyHistoryTB = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_fh_tb),
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )

    private var riseOfFever = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Rise of fever in evening",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )


    private var lossOfAppetite = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Loss of appetite",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private val aSymptomaticLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = "Asymptomatic TB Screening",
        required = false
    )
    private val checkSymptomsLabel1 = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = "Check if the person has any of these Symptoms:",
        required = false
    )
    private var age = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Age more than 60 years",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var diabetic = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Diabetic",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var tobaccoUser = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Tobacco user",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var bmi = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "BMI < 18.5",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var contactWithTBPatient = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Contact with TB patient on treatment",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )
    private var historyOfTBInLastFiveYrs = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "History of TB in last 5 years",
        entries = resources.getStringArray(R.array.yes_no),
        doubleStar = true,
        required = true,
        hasDependants = false
    )


    suspend fun setUpPage(ben: BenRegCache?, saved: TBScreeningCache?) {
        val list = mutableListOf(
            symptomaticLabel,
            checkSymptomsLabel,
            dateOfVisit,
            isCoughing,
            bloodInSputum,
            isFever,
            lossOfWeight,
            nightSweats,
            historyOfTB,
            currentlyTakingDrugs,
            familyHistoryTB,
            riseOfFever,
            lossOfAppetite,
            aSymptomaticLabel,
            checkSymptomsLabel1,
            age,
            diabetic,
            tobaccoUser,
            bmi,
            contactWithTBPatient,
            historyOfTBInLastFiveYrs
        )
        if (saved == null) {
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
        } else {
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            isCoughing.value =
                if (saved.coughMoreThan2Weeks == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            bloodInSputum.value =
                if (saved.bloodInSputum == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isFever.value =
                if (saved.feverMoreThan2Weeks == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            lossOfWeight.value =
                if (saved.lossOfWeight == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            nightSweats.value =
                if (saved.nightSweats == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            historyOfTB.value =
                if (saved.historyOfTb == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            currentlyTakingDrugs.value =
                if (saved.takingAntiTBDrugs == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            familyHistoryTB.value =
                if (saved.familySufferingFromTB == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]

            riseOfFever.value =
                if (saved.riseOfFever == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]

            lossOfAppetite.value =
                if (saved.lossOfAppetite == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
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

            historyOfTBInLastFiveYrs.value =
                if (saved.historyOfTBInLastFiveYrs == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
        }


        ben?.let {
            dateOfVisit.min = it.regDate
        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return -1
//        return when (formId) {
//        }
    }

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
            form.age =
                age.value == resources.getStringArray(R.array.yes_no)[0]
            form.diabetic =
                diabetic.value == resources.getStringArray(R.array.yes_no)[0]
            form.tobaccoUser =
                tobaccoUser.value == resources.getStringArray(R.array.yes_no)[0]
            form.bmi =
                bmi.value == resources.getStringArray(R.array.yes_no)[0]
            form.contactWithTBPatient =
                contactWithTBPatient.value == resources.getStringArray(R.array.yes_no)[0]
            form.historyOfTBInLastFiveYrs =
                historyOfTBInLastFiveYrs.value == resources.getStringArray(R.array.yes_no)[0]
            form.symptomatic = isSymptomatic()
            form.asymptomatic = isAsymptomatic()

            if (isSymptomatic()=="Yes" && isAsymptomatic() =="No"){
                form.recommandedTest = "Sputum Test"
            }else if(isSymptomatic() == "No"&& isAsymptomatic() =="Yes"){
                form.recommandedTest = "Chest X-Ray"
            }else if(isSymptomatic() == "Yes"&& isAsymptomatic() =="Yes"){
                form.recommandedTest = "Both"
            }else{
                form.recommandedTest = "None"
            }
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
            resources.getString(R.string.refer_to_hwc_facility_alert) else null

    }

    fun isSymptomatic():String{
        return if (isCoughing.value == resources.getStringArray(R.array.yes_no)[0] ||
            bloodInSputum.value == resources.getStringArray(R.array.yes_no)[0] ||
            isFever.value == resources.getStringArray(R.array.yes_no)[0] ||
            nightSweats.value == resources.getStringArray(R.array.yes_no)[0] ||
            lossOfWeight.value == resources.getStringArray(R.array.yes_no)[0] ||
            historyOfTB.value == resources.getStringArray(R.array.yes_no)[0]||
            riseOfFever.value == resources.getStringArray(R.array.yes_no)[0] ||
            lossOfAppetite.value == resources.getStringArray(R.array.yes_no)[0]
        )
            "Yes" else "No"
    }
    fun isAsymptomatic():String{
        return if (
            age.value == resources.getStringArray(R.array.yes_no)[0] ||
            diabetic.value == resources.getStringArray(R.array.yes_no)[0] ||
            tobaccoUser.value == resources.getStringArray(R.array.yes_no)[0] ||
            bmi.value == resources.getStringArray(R.array.yes_no)[0] ||
            contactWithTBPatient.value == resources.getStringArray(R.array.yes_no)[0] ||
            historyOfTBInLastFiveYrs.value == resources.getStringArray(R.array.yes_no)[0]
        )
            "Yes" else "No"
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
}
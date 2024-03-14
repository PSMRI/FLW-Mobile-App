package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.getDateStrFromLong
import java.text.SimpleDateFormat
import java.util.Locale

class HBYCFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {
    companion object {
        private fun getLongFromDate(dateString: String): Long {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
        }
    }


    private val month = FormElement(
        id = 1,
        inputType = InputType.EDIT_TEXT,
        title = "Month",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val subCenterName = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = "Subcenter name ",
        required = false
    )
    private val year = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = "Year",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val primaryHealthCenterName = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = "Primary Health Center Name ",
        required = false
    )
    private val villagePopulation = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = "Village Population",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val infantPopulation = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = "Total number of children aged 3 to 15 months in villages",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val visitDate = FormElement(
        id = 7,
        inputType = InputType.DATE_PICKER,
        title = "Visit Date",
        required = false,
        min = 0L,
        max = System.currentTimeMillis()
    )
    private val hbycAgeCategory = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = "H.B.Y.C by age Tour",
        required = false,
        entries = arrayOf("3", "6", "9", "12", "15")
    )
    private val orsPacketDelivered = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = "O.R.S. Packet delivered ",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val ironFolicAcidGiven = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = "Iron folic acid syrup given (after six months of childbirth)",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val isVaccinatedByAge = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = "Child vaccinated by age",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val wasIll = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = "The child was ill",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val referred = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = "If yes refer hospital",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val supplementsGiven = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        title = "Supplements given to the child according to age",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val byHeightLength = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = "By height / length (color inscribed in mcp card)",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val childrenWeighingLessReferred = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        title = "Number of children weighing less than –3SD orange color indicated in MCCP card referred to VHSND/PHC/NRC",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val weightAccordingToAge = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        title = "Weight according to child’s age (color inscribed in MCP card)",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val delayInDevelopment = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title = "Delay / constraint found in the physical and mental development of the child according to age",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val referredToHealthInstitite = FormElement(
        id = 19,
        inputType = InputType.RADIO,
        title = "If yes then Health Institute R.B.S.K. Team / A.N.M. referred to",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val vitaminASupplementsGiven = FormElement(
        id = 20,
        inputType = InputType.RADIO,
        title = "Vitamin – A supplements given to the child according to age",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val deathAge = FormElement(
        id = 21,
        inputType = InputType.DROPDOWN,
        title = "Mark age (in month) at the time of death",
        required = false,
        entries = arrayOf(
            "3 Month",
            "4 Month",
            "5 Month",
            "6 Month",
            "7 Month",
            "8 Month",
            "9 Month",
            "10 Month",
            "11 Month",
            "12 Month",
            "13 Month",
            "14 Month",
            "15 Month",
        )
    )
    private val deathCause = FormElement(
        id = 22,
        inputType = InputType.EDIT_TEXT,
        title = "Cause of death",
        required = false
    )
    private val qmOrAnmInformed = FormElement(
        id = 23,
        inputType = InputType.RADIO,
        title = "Q.M/A.N.M was informed",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val deathPlace = FormElement(
        id = 24,
        inputType = InputType.DROPDOWN,
        title = "Place of death",
        required = false,
        entries = arrayOf("Home", "Health Center", "On the Way")
    )
    private val superVisorOn = FormElement(
        id = 25,
        inputType = InputType.RADIO,
        title = "Supervisor from block/district/state level in last on",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val orsShortage = FormElement(
        id = 26,
        inputType = InputType.RADIO,
        title = "ORS in last one month Packet shortage",
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val ifaDecreased = FormElement(
        id = 27,
        inputType = InputType.RADIO,
        title = "IFA in last one month Decreased syrup",
        required = false,
        entries = arrayOf("Yes", "No")
    )

    val firstPage by lazy {
        listOf(
            month,
            subCenterName,
            year,
            primaryHealthCenterName,
            villagePopulation,
            infantPopulation,
            visitDate,
            hbycAgeCategory,
            orsPacketDelivered,
            ironFolicAcidGiven,
            isVaccinatedByAge,
            wasIll,
            referred,
            supplementsGiven,
            byHeightLength,
            childrenWeighingLessReferred,
            weightAccordingToAge,
            delayInDevelopment,
            referredToHealthInstitite,
            vitaminASupplementsGiven,
            deathAge,
            deathCause,
            qmOrAnmInformed,
            deathPlace,
            superVisorOn,
            orsShortage,
            ifaDecreased
        )
    }

    suspend fun setUpPage(ben: BenRegCache?, saved: HBYCCache?, monthVal: String) {
        val list = listOf(
            month,
            subCenterName,
            year,
            primaryHealthCenterName,
            villagePopulation,
            infantPopulation,
            visitDate,
            hbycAgeCategory,
            orsPacketDelivered,
            ironFolicAcidGiven,
            isVaccinatedByAge,
            wasIll,
            referred,
            supplementsGiven,
            byHeightLength,
            childrenWeighingLessReferred,
            weightAccordingToAge,
            delayInDevelopment,
            referredToHealthInstitite,
            vitaminASupplementsGiven,
            deathAge,
            deathCause,
            qmOrAnmInformed,
            deathPlace,
            superVisorOn,
            orsShortage,
            ifaDecreased
        )

        month.value = monthVal

        saved?.let { hbycCache ->
            month.value = hbycCache.month
            subCenterName.value = hbycCache.subcenterName
            year.value = hbycCache.year
            primaryHealthCenterName.value = hbycCache.primaryHealthCenterName
            villagePopulation.value = hbycCache.villagePopulation
            infantPopulation.value = hbycCache.infantPopulation
            visitDate.value = getDateStrFromLong(hbycCache.visitdate)
            hbycAgeCategory.value = hbycCache.hbycAgeCategory
            orsPacketDelivered.value =
                if (hbycCache.orsPacketDelivered == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.orsPacketDelivered - 1]
            ironFolicAcidGiven.value =
                if (hbycCache.ironFolicAcidGiven == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.ironFolicAcidGiven - 1]
            isVaccinatedByAge.value =
                if (hbycCache.isVaccinatedByAge == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.isVaccinatedByAge - 1]
            wasIll.value =
                if (hbycCache.wasIll == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.wasIll - 1]
            referred.value =
                if (hbycCache.referred == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.referred - 1]
            supplementsGiven.value =
                if (hbycCache.supplementsGiven == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.supplementsGiven - 1]
            byHeightLength.value =
                if (hbycCache.byHeightLength == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.byHeightLength - 1]
            childrenWeighingLessReferred.value =
                if (hbycCache.childrenWeighingLessReferred == 0) null else resources.getStringArray(
                    R.array.yes_no
                )[hbycCache.childrenWeighingLessReferred - 1]
            weightAccordingToAge.value =
                if (hbycCache.weightAccordingToAge == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.weightAccordingToAge - 1]
            delayInDevelopment.value =
                if (hbycCache.delayInDevelopment == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.delayInDevelopment - 1]
            referredToHealthInstitite.value =
                if (hbycCache.referredToHealthInstitite == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.referredToHealthInstitite - 1]
            vitaminASupplementsGiven.value =
                if (hbycCache.vitaminASupplementsGiven == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.vitaminASupplementsGiven - 1]
            deathAge.value = hbycCache.deathAge
            deathCause.value = hbycCache.deathCause
            qmOrAnmInformed.value =
                if (hbycCache.qmOrAnmInformed == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.qmOrAnmInformed - 1]
            deathPlace.value =
                if (hbycCache.deathPlace != null) resources.getStringArray(R.array.do_cause_of_death_array)[hbycCache.deathPlace!!] else null
            superVisorOn.value =
                if (hbycCache.superVisorOn == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.superVisorOn - 1]
            orsShortage.value =
                if (hbycCache.orsShortage == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.orsShortage - 1]
            ifaDecreased.value =
                if (hbycCache.ifaDecreased == 0) null else resources.getStringArray(R.array.yes_no)[hbycCache.ifaDecreased - 1]
        }
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
//        return when (formId) {
//        }
        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HBYCCache).let { hbycCache ->

            hbycCache.month = month.value
            hbycCache.subcenterName = subCenterName.value
            hbycCache.year = year.value
            hbycCache.primaryHealthCenterName = primaryHealthCenterName.value
            hbycCache.villagePopulation = villagePopulation.value
            hbycCache.infantPopulation = infantPopulation.value
            hbycCache.visitdate = visitDate.value?.let { getLongFromDate(it) }
            hbycCache.hbycAgeCategory = hbycAgeCategory.value
            hbycCache.orsPacketDelivered = orsPacketDelivered.getPosition()
            hbycCache.ironFolicAcidGiven = ironFolicAcidGiven.getPosition()
            hbycCache.isVaccinatedByAge = isVaccinatedByAge.getPosition()
            hbycCache.wasIll = wasIll.getPosition()
            hbycCache.referred = referred.getPosition()
            hbycCache.supplementsGiven = supplementsGiven.getPosition()
            hbycCache.byHeightLength = byHeightLength.getPosition()
            hbycCache.childrenWeighingLessReferred = childrenWeighingLessReferred.getPosition()
            hbycCache.weightAccordingToAge = weightAccordingToAge.getPosition()
            hbycCache.delayInDevelopment = delayInDevelopment.getPosition()
            hbycCache.referredToHealthInstitite = referredToHealthInstitite.getPosition()
            hbycCache.vitaminASupplementsGiven = vitaminASupplementsGiven.getPosition()
            hbycCache.deathAge = deathAge.value
            hbycCache.deathCause = deathCause.value
            hbycCache.qmOrAnmInformed = qmOrAnmInformed.getPosition()
            hbycCache.deathPlace = deathPlace.getPosition()
            hbycCache.superVisorOn = superVisorOn.getPosition()
            hbycCache.orsShortage = orsShortage.getPosition()
            hbycCache.ifaDecreased = ifaDecreased.getPosition()
        }
    }

}
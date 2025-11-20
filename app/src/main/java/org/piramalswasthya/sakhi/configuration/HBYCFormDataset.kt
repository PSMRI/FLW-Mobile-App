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
        title = resources.getString(R.string.month),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val subCenterName = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_sub_center),
        required = false
    )
    private val year = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.mdsr_year),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val primaryHealthCenterName = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_primary_health_center_name),
        required = false
    )
    private val villagePopulation = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_village_population),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val infantPopulation = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_total_no_of_childrens),
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val visitDate = FormElement(
        id = 7,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_visit),
        required = false,
        min = 0L,
        max = System.currentTimeMillis()
    )
    private val hbycAgeCategory = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_hyyc_by_age_tour),
        required = false,
        entries = arrayOf("3", "6", "9", "12", "15")
    )
    private val orsPacketDelivered = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_ors_packet_delivered),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val ironFolicAcidGiven = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_Iron_folic_acid),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val isVaccinatedByAge = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_child_vaccinated_by_age),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val wasIll = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_child_was_ill),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val referred = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_if_yes_refer_hospital),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val supplementsGiven = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_suppliments_given_to_child),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val byHeightLength = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_by_height_length),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val childrenWeighingLessReferred = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_no_of_children_weighing_less),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val weightAccordingToAge = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_weight_acc_to_child),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val delayInDevelopment = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_delay_constraint_found),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val referredToHealthInstitite = FormElement(
        id = 19,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_if_yes_then_health_institute),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val vitaminASupplementsGiven = FormElement(
        id = 20,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_vit_a_suppliment),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val deathAge = FormElement(
        id = 21,
        inputType = InputType.DROPDOWN,
        title =  resources.getString(R.string.str_mark_age),
        required = false,
        entries =  resources.getStringArray(R.array.hbyc_month_array)
    )
    private val deathCause = FormElement(
        id = 22,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.mdsr_cause),
        required = false
    )
    private val qmOrAnmInformed = FormElement(
        id = 23,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_qm_informed),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val deathPlace = FormElement(
        id = 24,
        inputType = InputType.DROPDOWN,
        title =  resources.getString(R.string.str_place_of_death),
        required = false,
        entries = arrayOf("Home", "Health Center", "On the Way")
    )
    private val superVisorOn = FormElement(
        id = 25,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_supervisor_from_block),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val orsShortage = FormElement(
        id = 26,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_ors_in_last_one_month),
        required = false,
        entries = arrayOf("Yes", "No")
    )
    private val ifaDecreased = FormElement(
        id = 27,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_ifa_last_month),
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
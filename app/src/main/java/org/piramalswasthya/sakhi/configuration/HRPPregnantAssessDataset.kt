package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.InputType
import java.util.concurrent.TimeUnit

class HRPPregnantAssessDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val noOfDeliveries = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.no_of_deliveries_is_more_than_3),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val timeLessThan18m = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.time_from_last_delivery_is_less_than_18_months),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val heightShort = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.height_is_very_short_or_less_than_140_cms),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val age = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.age_is_less_than_18_or_more_than_35_years),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val rhNegative = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.rh_negative),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val homeDelivery = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.home_delivery_of_previous_pregnancy),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val badObstetric = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.bad_obstetric_history),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )


    private val multiplePregnancy = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.multiple_pregnancy),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var lmpDate = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.lmp_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private var edd = FormElement(
        id = 10,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.edd),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis(),
        max = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(280),
        hasDependants = false,
        isEnabled = false
    )

    private var childInfoLabel = FormElement(
        id = 11,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.information_on_children),
        arrayId = -1,
        required = false,
        hasDependants = false,
        showHighRisk = false
    )

    private var physicalObservationLabel = FormElement(
        id = 12,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.physical_observation),
        arrayId = -1,
        required = false,
        hasDependants = false,
        showHighRisk = false
    )

    private var obstetricHistoryLabel = FormElement(
        id = 13,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.obstetric_history),
        arrayId = -1,
        required = false,
        hasDependants = false,
        showHighRisk = false
    )

    private val assesLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.assess_for_high_risk_conditions_in_the_pregnant_women),
        required = false
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: HRPPregnantAssessCache?) {
        val list = mutableListOf(
            assesLabel,
            childInfoLabel,
            noOfDeliveries,
            timeLessThan18m,
            physicalObservationLabel,
            heightShort,
            age,
            obstetricHistoryLabel,
            rhNegative,
            homeDelivery,
            badObstetric,
            multiplePregnancy,
            lmpDate,
            edd
        )

        if (saved == null) {
            //
        } else {
            noOfDeliveries.value = getLocalValueInArray(R.array.yes_no, saved.noOfDeliveries)
            timeLessThan18m.value = getLocalValueInArray(R.array.yes_no, saved.timeLessThan18m)
            heightShort.value = getLocalValueInArray(R.array.yes_no, saved.heightShort)
            age.value = getLocalValueInArray(R.array.yes_no, saved.age)
            rhNegative.value = getLocalValueInArray(R.array.yes_no, saved.rhNegative)
            homeDelivery.value = getLocalValueInArray(R.array.yes_no, saved.homeDelivery)
            badObstetric.value = getLocalValueInArray(R.array.yes_no, saved.badObstetric)
            multiplePregnancy.value = getLocalValueInArray(R.array.yes_no, saved.multiplePregnancy)
            lmpDate.value = getDateFromLong(saved.lmpDate)
            edd.value = getDateFromLong(saved.edd)

            childInfoLabel.showHighRisk = (
                    noOfDeliveries.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                            timeLessThan18m.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
                    )

            physicalObservationLabel.showHighRisk = (
                    heightShort.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                            age.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
                    )

            obstetricHistoryLabel.showHighRisk = (
                    rhNegative.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                            homeDelivery.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                            badObstetric.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                            multiplePregnancy.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
                    )
        }

        ben?.genDetails?.lastMenstrualPeriod?.let {
            lmpDate.value = getDateFromLong(it)
            edd.value = getDateFromLong(it + TimeUnit.DAYS.toMillis(280))
            lmpDate.isEnabled = false
            edd.isEnabled = false
        }
        lmpDate.min = (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(280))
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            noOfDeliveries.id, timeLessThan18m.id -> {
                childInfoLabel.showHighRisk = noOfDeliveries.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                        timeLessThan18m.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
                -1
            }

            heightShort.id, age.id -> {
                physicalObservationLabel.showHighRisk = heightShort.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                        age.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
                -1
            }

            rhNegative.id, homeDelivery.id, badObstetric.id, multiplePregnancy.id -> {
                obstetricHistoryLabel.showHighRisk = rhNegative.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                        homeDelivery.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                        badObstetric.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                        multiplePregnancy.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
                -1
            }

            lmpDate.id -> {
                edd.value =
                    getDateFromLong(getLongFromDate(lmpDate.value) + TimeUnit.DAYS.toMillis(280))
                validateEmptyOnEditText(edd)
                -1
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPPregnantAssessCache).let { form ->
            form.noOfDeliveries = getEnglishValueInArray(R.array.yes_no, noOfDeliveries.value)
            form.timeLessThan18m = getEnglishValueInArray(R.array.yes_no, timeLessThan18m.value)
            form.heightShort = getEnglishValueInArray(R.array.yes_no, heightShort.value)
            form.age = getEnglishValueInArray(R.array.yes_no, age.value)
            form.rhNegative = getEnglishValueInArray(R.array.yes_no, rhNegative.value)
            form.homeDelivery = getEnglishValueInArray(R.array.yes_no, homeDelivery.value)
            form.badObstetric = getEnglishValueInArray(R.array.yes_no, badObstetric.value)
            form.multiplePregnancy = getEnglishValueInArray(R.array.yes_no, multiplePregnancy.value)
            form.lmpDate = getLongFromDate(lmpDate.value)
            form.edd = getLongFromDate(edd.value)
        }
    }

    fun getIndexOfChildLabel() = getIndexById(childInfoLabel.id)

    fun getIndexOfPhysicalObservationLabel() = getIndexById(physicalObservationLabel.id)

    fun getIndexOfObstetricHistoryLabel() = getIndexById(obstetricHistoryLabel.id)

    fun getIndexOfEdd() = getIndexById(edd.id)

    fun isHighRisk(): Boolean {
        return noOfDeliveries.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                timeLessThan18m.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                heightShort.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                age.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                rhNegative.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                homeDelivery.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                badObstetric.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                multiplePregnancy.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
    }

    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.lastMenstrualPeriod = getLongFromDate(lmpDate.value)
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
        benRegCache.syncState = SyncState.UNSYNCED
    }
}
package org.piramalswasthya.sakhi.configuration

import android.content.Context
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
        title = "No. of Deliveries is more than 3",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val timeLessThan18m = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = "Time from last delivery is less than 18 months",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val heightShort = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Height is very short or less than 140 cms",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val age = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Age is less than 18 or more than 35 years",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val rhNegative = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Rh Negative",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val homeDelivery = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Home delivery of previous pregnancy",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val badObstetric = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Bad obstetric history",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )


    private val multiplePregnancy = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Multiple Pregnancy",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private var lmpDate = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        title = "LMP Date",
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private var edd = FormElement(
        id = 10,
        inputType = InputType.DATE_PICKER,
        title = "EDD",
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
        title = "Information on children",
        arrayId = -1,
        required = false,
        hasDependants = false,
        showHighRisk = false
    )

    private var physicalObservationLabel = FormElement(
        id = 12,
        inputType = InputType.HEADLINE,
        title = "Physical Observation",
        arrayId = -1,
        required = false,
        hasDependants = false,
        showHighRisk = false
    )

    private var obstetricHistoryLabel = FormElement(
        id = 13,
        inputType = InputType.HEADLINE,
        title = "Obstetric History",
        arrayId = -1,
        required = false,
        hasDependants = false,
        showHighRisk = false
    )

    private val assesLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = "ASSESS FOR HIGH RISK CONDITIONS IN THE PREGNANT WOMEN",
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
            noOfDeliveries.value = saved.noOfDeliveries
            timeLessThan18m.value = saved.timeLessThan18m
            heightShort.value = saved.heightShort
            age.value = saved.age
            rhNegative.value = saved.rhNegative
            homeDelivery.value = saved.homeDelivery
            badObstetric.value = saved.badObstetric
            multiplePregnancy.value = saved.multiplePregnancy
            lmpDate.value = getDateFromLong(saved.lmpDate)
            edd.value = getDateFromLong(saved.edd)

            childInfoLabel.showHighRisk = (
                    noOfDeliveries.value.contentEquals("Yes") ||
                            timeLessThan18m.value.contentEquals("Yes")
                    )

            physicalObservationLabel.showHighRisk = (
                    heightShort.value.contentEquals("Yes") ||
                            age.value.contentEquals("Yes")
                    )

            obstetricHistoryLabel.showHighRisk = (
                    rhNegative.value.contentEquals("Yes") ||
                            homeDelivery.value.contentEquals("Yes") ||
                            badObstetric.value.contentEquals("Yes") ||
                            multiplePregnancy.value.contentEquals("Yes")
                    )
        }

        ben?.genDetails?.lastMenstrualPeriod?.let {
            lmpDate.value = getDateFromLong(it)
            edd.value = getDateFromLong(it + TimeUnit.DAYS.toMillis(280))
        }
        lmpDate.min = (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(280))
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            noOfDeliveries.id, timeLessThan18m.id -> {
                if (noOfDeliveries.value.contentEquals("Yes") ||
                    timeLessThan18m.value.contentEquals("Yes")
                ) {
                    childInfoLabel.showHighRisk = true
                    handleListOnValueChanged(childInfoLabel.id, 0)
                } else {
                    childInfoLabel.showHighRisk = false
                    handleListOnValueChanged(childInfoLabel.id, 0)
                }
            }

            heightShort.id, age.id -> {
                if (heightShort.value.contentEquals("Yes") ||
                    age.value.contentEquals("Yes")
                ) {
                    physicalObservationLabel.showHighRisk = true
                    handleListOnValueChanged(physicalObservationLabel.id, 0)
                } else {
                    physicalObservationLabel.showHighRisk = false
                    handleListOnValueChanged(physicalObservationLabel.id, 0)
                }
            }

            rhNegative.id, homeDelivery.id, badObstetric.id, multiplePregnancy.id -> {
                if (rhNegative.value.contentEquals("Yes") ||
                    homeDelivery.value.contentEquals("Yes") ||
                    badObstetric.value.contentEquals("Yes") ||
                    multiplePregnancy.value.contentEquals("Yes")
                ) {
                    obstetricHistoryLabel.showHighRisk = true
                    handleListOnValueChanged(obstetricHistoryLabel.id, 0)
                } else {
                    obstetricHistoryLabel.showHighRisk = false
                    handleListOnValueChanged(obstetricHistoryLabel.id, 0)
                }
            }

            lmpDate.id -> {
                edd.value =
                    getDateFromLong(getLongFromDate(lmpDate.value) + TimeUnit.DAYS.toMillis(280))
                -1
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPPregnantAssessCache).let { form ->
            form.noOfDeliveries = noOfDeliveries.value
            form.timeLessThan18m = timeLessThan18m.value
            form.heightShort = heightShort.value
            form.age = age.value
            form.rhNegative = rhNegative.value
            form.homeDelivery = homeDelivery.value
            form.badObstetric = badObstetric.value
            form.multiplePregnancy = multiplePregnancy.value
            form.lmpDate = getLongFromDate(lmpDate.value)
            form.edd = getLongFromDate(edd.value)
        }
    }

    fun getIndexOfChildLabel() = getIndexById(childInfoLabel.id)

    fun getIndexOfPhysicalObservationLabel() = getIndexById(physicalObservationLabel.id)

    fun getIndexOfObstetricHistoryLabel() = getIndexById(obstetricHistoryLabel.id)

    fun getIndexOfEdd() = getIndexById(edd.id)

    fun isHighRisk(): Boolean {
        return noOfDeliveries.value.contentEquals("Yes") ||
                timeLessThan18m.value.contentEquals("Yes") ||
                heightShort.value.contentEquals("Yes") ||
                age.value.contentEquals("Yes") ||
                rhNegative.value.contentEquals("Yes") ||
                homeDelivery.value.contentEquals("Yes") ||
                badObstetric.value.contentEquals("Yes") ||
                multiplePregnancy.value.contentEquals("Yes")
    }

}
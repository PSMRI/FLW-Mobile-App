package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.*

class HRPNonPregnantAssessDataset (context: Context, currentLanguage: Languages
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

    private val misCarriage = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Miscarriage/abortion",
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

    private val medicalIssues = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "During pregnancy or delivery you faced any medical issues",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )


    private val pastCSection = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Past C –section (CS)",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val infoChildLabel = FormElement(
        id = 9,
        inputType = InputType.HEADLINE,
        title = "Information on children",
        required = false
    )

    private val physicalObsLabel = FormElement(
        id = 10,
        inputType = InputType.HEADLINE,
        title = "Physical Observation",
        required = false
    )

    private val obsHistoryLabel = FormElement(
        id = 11,
        inputType = InputType.HEADLINE,
        title = "Obstetric History",
        required = false
    )

    private val assesLabel = FormElement(
        id = 12,
        inputType = InputType.HEADLINE,
        title = "ASSESS FOR HIGH RISK CONDITIONS IN THE NON-PREGNANT WOMEN",
        required = false
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: HRPNonPregnantAssessCache?) {
        val list = mutableListOf(
            assesLabel,
            infoChildLabel,
            noOfDeliveries,
            timeLessThan18m,
            physicalObsLabel,
            heightShort,
            age,
            obsHistoryLabel,
            misCarriage,
            homeDelivery,
            medicalIssues,
            pastCSection
        )

        saved?.let {
            noOfDeliveries.value = it.noOfDeliveries
            timeLessThan18m.value = it.timeLessThan18m
            heightShort.value = it.heightShort
            age.value = it.age
            misCarriage.value = it.misCarriage
            homeDelivery.value = it.homeDelivery
            medicalIssues.value = it.medicalIssues
            pastCSection.value = it.pastCSection
        }

        setUpPage(list)
    }
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when(formId) {
            noOfDeliveries.id, timeLessThan18m.id -> {
                infoChildLabel.showHighRisk =
                    (noOfDeliveries.value == "Yes" || timeLessThan18m.value == "Yes")
                -1
            }
            heightShort.id, age.id -> {
                physicalObsLabel.showHighRisk =
                    (heightShort.value == "Yes" || age.value == "Yes")
                -1
            }
            misCarriage.id, homeDelivery.id, medicalIssues.id, pastCSection.id -> {
                obsHistoryLabel.showHighRisk =
                    (misCarriage.value == "Yes" || homeDelivery.value == "Yes"
                            || medicalIssues.value == "Yes" || pastCSection.value == "Yes")
                -1
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPNonPregnantAssessCache).let { form ->
            form.noOfDeliveries = noOfDeliveries.value
            form.heightShort = heightShort.value
            form.age = age.value
            form.misCarriage = misCarriage.value
            form.homeDelivery = homeDelivery.value
            form.medicalIssues = medicalIssues.value
            form.pastCSection = pastCSection.value
        }
    }

    fun getIndexOfChildLabel() = getIndexById(infoChildLabel.id)

    fun getIndexOfPhysicalObservationLabel() = getIndexById(physicalObsLabel.id)

    fun getIndexOfObstetricHistoryLabel() = getIndexById(obsHistoryLabel.id)

    fun isHighRisk(): Boolean {
        return noOfDeliveries.value.contentEquals("Yes") ||
                timeLessThan18m.value.contentEquals("Yes") ||
                heightShort.value.contentEquals("Yes") ||
                age.value.contentEquals("Yes") ||
                misCarriage.value.contentEquals("Yes") ||
                homeDelivery.value.contentEquals("Yes") ||
                medicalIssues.value.contentEquals("Yes") ||
                pastCSection.value.contentEquals("Yes")
    }

}
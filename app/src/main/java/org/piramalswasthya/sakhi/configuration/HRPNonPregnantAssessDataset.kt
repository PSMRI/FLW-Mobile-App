package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.InputType

class HRPNonPregnantAssessDataset(
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

    private val misCarriage = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.miscarriage_abortion),
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

    private val medicalIssues = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.during_pregnancy_or_delivery_you_faced_any_medical_issues),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )


    private val pastCSection = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.past_c_section_cs),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val infoChildLabel = FormElement(
        id = 9,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.information_on_children),
        required = false
    )

    private val physicalObsLabel = FormElement(
        id = 10,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.physical_observation),
        required = false
    )

    private val obsHistoryLabel = FormElement(
        id = 11,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.obstetric_history),
        required = false
    )

    private val assesLabel = FormElement(
        id = 12,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.assess_for_high_risk_conditions_in_the_non_pregnant_women),
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
            noOfDeliveries.value = getLocalValueInArray(R.array.yes_no, it.noOfDeliveries)
            timeLessThan18m.value = getLocalValueInArray(R.array.yes_no,it.timeLessThan18m)
            heightShort.value = getLocalValueInArray(R.array.yes_no,it.heightShort)
            age.value = getLocalValueInArray(R.array.yes_no,it.age)
            misCarriage.value = getLocalValueInArray(R.array.yes_no,it.misCarriage)
            homeDelivery.value = getLocalValueInArray(R.array.yes_no,it.homeDelivery)
            medicalIssues.value = getLocalValueInArray(R.array.yes_no,it.medicalIssues)
            pastCSection.value = getLocalValueInArray(R.array.yes_no,it.pastCSection)

            infoChildLabel.showHighRisk =
                (noOfDeliveries.value == resources.getStringArray(R.array.yes_no)[0] || timeLessThan18m.value == resources.getStringArray(R.array.yes_no)[0])

            physicalObsLabel.showHighRisk =
                (heightShort.value == resources.getStringArray(R.array.yes_no)[0] || age.value == resources.getStringArray(R.array.yes_no)[0])

            obsHistoryLabel.showHighRisk =
                (misCarriage.value == resources.getStringArray(R.array.yes_no)[0] || homeDelivery.value == resources.getStringArray(R.array.yes_no)[0]
                        || medicalIssues.value == resources.getStringArray(R.array.yes_no)[0] || pastCSection.value == resources.getStringArray(R.array.yes_no)[0])
        }

        setUpPage(list)
    }
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            noOfDeliveries.id, timeLessThan18m.id -> {
                infoChildLabel.showHighRisk =
                    (noOfDeliveries.value == resources.getStringArray(R.array.yes_no)[0] || timeLessThan18m.value == resources.getStringArray(R.array.yes_no)[0])
                -1
            }

            heightShort.id, age.id -> {
                physicalObsLabel.showHighRisk =
                    (heightShort.value == resources.getStringArray(R.array.yes_no)[0] || age.value == resources.getStringArray(R.array.yes_no)[0])
                -1
            }

            misCarriage.id, homeDelivery.id, medicalIssues.id, pastCSection.id -> {
                obsHistoryLabel.showHighRisk =
                    (misCarriage.value == resources.getStringArray(R.array.yes_no)[0] || homeDelivery.value == resources.getStringArray(R.array.yes_no)[0]
                            || medicalIssues.value == resources.getStringArray(R.array.yes_no)[0] || pastCSection.value == resources.getStringArray(R.array.yes_no)[0])
                -1
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPNonPregnantAssessCache).let { form ->
            form.noOfDeliveries = getEnglishValueInArray(R.array.yes_no, noOfDeliveries.value)
            form.heightShort = getEnglishValueInArray(R.array.yes_no, heightShort.value)
            form.age = getEnglishValueInArray(R.array.yes_no, age.value)
            form.misCarriage = getEnglishValueInArray(R.array.yes_no, misCarriage.value)
            form.homeDelivery = getEnglishValueInArray(R.array.yes_no, homeDelivery.value)
            form.medicalIssues = getEnglishValueInArray(R.array.yes_no, medicalIssues.value)
            form.timeLessThan18m = getEnglishValueInArray(R.array.yes_no, timeLessThan18m.value)
            form.pastCSection = getEnglishValueInArray(R.array.yes_no, pastCSection.value)
        }
    }

    fun getIndexOfChildLabel() = getIndexById(infoChildLabel.id)

    fun getIndexOfPhysicalObservationLabel() = getIndexById(physicalObsLabel.id)

    fun getIndexOfObstetricHistoryLabel() = getIndexById(obsHistoryLabel.id)

    fun isHighRisk(): Boolean {
        return noOfDeliveries.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                timeLessThan18m.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                heightShort.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                age.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                misCarriage.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                homeDelivery.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                medicalIssues.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                pastCSection.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
    }

}
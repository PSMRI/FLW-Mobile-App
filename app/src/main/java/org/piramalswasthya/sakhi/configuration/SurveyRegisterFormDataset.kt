package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.model.FormInputOld
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.SurveyRegisterCache

class SurveyRegisterFormDataset(
    context: Context,
    private val surveyRegister: SurveyRegisterCache? = null
) {

    private val facilitatorSupervisor = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Name of Facilitator Supervisor",
        required = false
    )
    private val from = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "From",
        required = false
    )
    private val till = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Till",
        required = false
    )
    private val firstHouseLandlord = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Name of the landlord of First House",
        required = false
    )
    private val fisrtHomeIdentificationSymbol = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Home identification Symbol",
        required = false
    )
    private val middleHouseLandlord = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Name of the landlord of Middle House",
        required = false
    )
    private val middleHomeIdentificationSymbol = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Home Identification Symbol",
        required = false
    )
    private val lastHouseLandlord = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Name of the landlord of Last House",
        required = false
    )
    private val lastHomeIdentificationSymbol = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Home Identification Symbol",
        required = false
    )
    private val householdNumber = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Household number",
        required = false
    )
    private val headOfHouse = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Name of Head of House",
        required = false
    )
    private val numFamilyMembers = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Total members in the family",
        required = false
    )
    private val numEligibleCouples = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Total number of eligible couples in the family",
        required = false
    )
    private val numPregnantWomen = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Total number of pregnant women in the house",
        required = false
    )
    private val numChildrenLessThanOneMonth = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Total number of children (0 – 1 month)",
        required = false
    )
    private val numChildrenLessThanOneYear = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Total no. of children (1 month – 1 year)",
        required = false
    )
    private val numChildrenLessThanFiveYear = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Total no. of children (2 yr – 5 yr)",
        required = false
    )
}
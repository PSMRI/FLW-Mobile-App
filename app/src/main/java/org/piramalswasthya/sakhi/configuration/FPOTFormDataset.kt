package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.model.FPOTCache
import org.piramalswasthya.sakhi.model.FormInput
import org.piramalswasthya.sakhi.model.PMJAYCache
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class FPOTFormDataset(context: Context, private val fpot: FPOTCache? = null) {

    companion object {
        private fun getLongFromDate(dateString: String): Long {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
        }
    }

    fun mapValues(fpot: FPOTCache) {

    }
    private val monthlySerialNumber = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Monthly serial number",
        required = false
    )
    private val annualSerialNumber = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Annual serial number",
        required = false
    )
    val spouseName = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Husband/Wife’s Name",
        required = false
    )
    private val category = FormInput(
        inputType = FormInput.InputType.DROPDOWN,
        title = "Category",
        list = arrayOf(
            "Above Poverty Line (APL)",
            "Below Poverty Line (BPL)",
            "Anu Shuchit Jati (SC)",
            "Anu Shuchit Jan Jati (ST)"
        ),
        required = false
    )
    private val benAddress = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Beneficiary Address",
        required = false
    )
    val contactNumber = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Telephone Number/Mobile Number",
        required = false
    )
    private val educationalQualification = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Educational Qualification",
        required = false
    )
    private val numChildren = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Total number of surviving children",
        required = false
    )
    private val youngestChildAge = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Age of Youngest child",
        required = false
    )
    private val sterilization = FormInput(
        inputType = FormInput.InputType.RADIO,
        title = "Sterilization/ Sterilization Consent Form Filled",
        required = false,
        list = arrayOf("Yes", "No")
    )
    private val mrCheckListFilled = FormInput(
        inputType = FormInput.InputType.RADIO,
        title = "Medical Record Checklist Filled ",
        required = false,
        list = arrayOf("Yes", "No")
    )
    private val dateOfOperation = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Date of operation done ",
        required = false
    )
    private val femaleSterilization = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Female Sterilization ",
        required = false
    )
    private val secondFollowUpExpectedDate = FormInput(
        inputType = FormInput.InputType.DATE_PICKER,
        title = "Expected date of follow up",
        min = 0,
        max = System.currentTimeMillis(),
        required = false
    )
    private val followUpActualDate = FormInput(
        inputType = FormInput.InputType.DATE_PICKER,
        min = 0,
        max = System.currentTimeMillis(),
        title = "Actual date of follow up",
        required = false
    )
    private val followUpDetails = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Details of follow up ",
        required = false
    )
    private val secondPostFollowUpCounselling = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Counselling post follow up ",
        required = false
    )
    private val thirdFollowUpExpectedDate = FormInput(
        inputType = FormInput.InputType.DATE_PICKER,
        title = "Expected date of Follow-up",
        min = 0,
        max = System.currentTimeMillis(),
        required = false
    )
    private val menstruationStarted = FormInput(
        inputType = FormInput.InputType.RADIO,
        title = "Menstruation stared after female sterilization",
        required = false,
        list = arrayOf("Yes","No")
    )
    private val spermatozoaFoundInSemen = FormInput(
        inputType = FormInput.InputType.RADIO,
        title = "Spermatozoa found in semen",
        required = false,
        list = arrayOf("Yes", "No")
    )
    private val thirdPostFollowUpCounselling = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Counselling post follow up ",
        required = false
    )
    private val sterilizationOrVasectomyIssueDate = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Date of issue of female sterilization / vasectomy certificate",
        required = false
    )
    private val notIssuedReason = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "If not issued, please mention reasons",
        required = false
    )
    private val sterilizationOrVasectomyDocSubmitted = FormInput(
        inputType = FormInput.InputType.RADIO,
        title = "Photocopy of female sterilization / vasectomy certificate kept in Health Facility",
        required = false,
        list = arrayOf("Yes", "No")
    )
    private val remarks = FormInput(
        inputType = FormInput.InputType.EDIT_TEXT,
        title = "Remarks, if any ",
        required = false
    )

    val firstPage by lazy {
        listOf(monthlySerialNumber, annualSerialNumber, spouseName, category, benAddress, contactNumber, educationalQualification,
            numChildren, youngestChildAge, sterilization, mrCheckListFilled, dateOfOperation, femaleSterilization, secondFollowUpExpectedDate,
            followUpActualDate, followUpDetails, secondPostFollowUpCounselling, thirdFollowUpExpectedDate, menstruationStarted,
            spermatozoaFoundInSemen, thirdPostFollowUpCounselling, sterilizationOrVasectomyIssueDate, notIssuedReason,
            sterilizationOrVasectomyDocSubmitted, remarks)
    }
}
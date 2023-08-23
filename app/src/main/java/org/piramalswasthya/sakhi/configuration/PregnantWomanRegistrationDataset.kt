package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.getWeeksOfPregnancy
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import java.util.Calendar

class PregnantWomanRegistrationDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private fun getMinLmpMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1 * 280)
            return cal.timeInMillis
        }
    }

    private val dateOfReg = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.nbr_dor),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val rchId = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = "RCH ID",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12
    )
    private val mcpCardNumber = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = "MCP Card No",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12
    )
    private val name = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = "Name of Pregnant Women",
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val husbandName = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = "Name of Husband",
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val age = FormElement(
        id = 6,
        inputType = InputType.TEXT_VIEW,
        title = "Current Age of Woman",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForGenBen.toLong(),
        min = Konstants.minAgeForGenBen.toLong(),
    )

    private val lmp = FormElement(
        id = 7,
        inputType = InputType.DATE_PICKER,
        title = "LMP Date",
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = getMinLmpMillis(),
        hasDependants = true
    )
    private val weekOfPregnancy = FormElement(
        id = 8,
        inputType = InputType.TEXT_VIEW,
        title = "Weeks of Pregnancy at the time of Registration",
        required = false,
    )
    private val edd = FormElement(
        id = 9,
        inputType = InputType.TEXT_VIEW,
        title = "EDD",
        arrayId = -1,
        required = false,
    )
    private val bloodGroup = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = "Blood Group",
        arrayId = R.array.maternal_health_blood_group,
        entries = resources.getStringArray(R.array.maternal_health_blood_group),
        required = false
    )
    private val weight = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = "Weight of PW (Kg) at time Registration",
        arrayId = -1,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 30,
        max = 200
    )
    private val height = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = "Height of PW (Cm)",
        arrayId = -1,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 50,
        max = 200
    )
    private val vdrlrprTestResult = FormElement(
        id = 13,
        inputType = InputType.DROPDOWN,
        title = "VDRL/RPR Test result",
        arrayId = R.array.maternal_health_test_result,
        entries = resources.getStringArray(R.array.maternal_health_test_result),
        required = false,
        hasDependants = true
    )

    private val dateOfVdrlTestDone = FormElement(
        id = 14,
        inputType = InputType.DATE_PICKER,
        title = "Date of Test done",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
    )
    private val hivTestResult = FormElement(
        id = 15,
        inputType = InputType.DROPDOWN,
        title = "HIV Test result",
        arrayId = R.array.maternal_health_test_result,
        entries = resources.getStringArray(R.array.maternal_health_test_result),
        required = false,
        hasDependants = true
    )

    private val dateOfhivTestDone = FormElement(
        id = 16,
        inputType = InputType.DATE_PICKER,
        title = "Date of Test done",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
    )
    private val hbsAgTestResult = FormElement(
        id = 17,
        inputType = InputType.DROPDOWN,
        title = "HBsAg Test result",
        arrayId = R.array.maternal_health_test_result_2,
        entries = resources.getStringArray(R.array.maternal_health_test_result_2),
        required = false,
        hasDependants = true
    )

    private val dateOfhbsAgTestDone = FormElement(
        id = 18,
        inputType = InputType.DATE_PICKER,
        title = "Date of Test done",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
    )

    private val pastIllness = FormElement(
        id = 19,
        inputType = InputType.CHECKBOXES,
        title = "Past Illness",
        arrayId = R.array.maternal_health_past_illness,
        entries = resources.getStringArray(R.array.maternal_health_past_illness),
        required = true,
        value = resources.getStringArray(R.array.maternal_health_past_illness).first(),
        hasDependants = true
    )

    private val otherPastIllness = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = "other",
        required = true,
    )

    private val isFirstPregnancy = FormElement(
        id = 21,
        inputType = InputType.RADIO,
        title = "Is this your 1st pregnancy?",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val totalNumberOfPreviousPregnancy = FormElement(
        id = 22,
        inputType = InputType.EDIT_TEXT,
        title = "Total no. of previous Pregnancy",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        min = 0,
        max = 15
    )
    private val complicationsDuringLastPregnancy = FormElement(
        id = 23,
        inputType = InputType.DROPDOWN,
        title = "Any complications in Last Pregnancy",
        arrayId = R.array.maternal_health_past_del_complications,
        entries = resources.getStringArray(R.array.maternal_health_past_del_complications),
        required = false,
        hasDependants = true
    )

    private val otherComplicationsDuringLastPregnancy = FormElement(
        id = 24,
        inputType = InputType.EDIT_TEXT,
        title = "other",
        required = true,
    )

    private val isHrpCase = FormElement(
        id = 25,
        inputType = InputType.RADIO,
        title = "Is Identified as HRP cases?",
        entries = arrayOf("Yes", "No"),
        hasDependants = true,
        required = false
    )
    private val assignedAsHrpBy = FormElement(
        id = 26,
        inputType = InputType.DROPDOWN,
        title = "Who had identified as HRP?",
        arrayId = R.array.maternal_health_reg_hrp_confirm_by,
        entries = resources.getStringArray(R.array.maternal_health_reg_hrp_confirm_by),
        required = true
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: PregnantWomanRegistrationCache?) {
        val list = mutableListOf(
            dateOfReg,
            rchId,
            mcpCardNumber,
            name,
            husbandName,
            age,
            lmp,
            weekOfPregnancy,
            edd,
            bloodGroup,
            weight,
            height,
            vdrlrprTestResult,
//            dateOfVdrlTestDone,
            hivTestResult,
//            dateOfhivTestDone,
            hbsAgTestResult,
//            dateOfhbsAgTestDone,
            pastIllness,
            isFirstPregnancy,
            isHrpCase
        )
        dateOfReg.value = getDateFromLong(System.currentTimeMillis())
        dateOfReg.value?.let {
            val long = getLongFromDate(it)
            dateOfhivTestDone.min = long
            dateOfVdrlTestDone.min = long
            dateOfhbsAgTestDone.min = long
        }

        ben?.let {
            dateOfReg.min = it.regDate
            rchId.value = ben.rchId
            name.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            husbandName.value = ben.genDetails?.spouseName
            age.value = BenBasicCache.getAgeFromDob(ben.dob).toString()
        }
        saved?.let {
            dateOfReg.value = getDateFromLong(it.dateOfRegistration)
            rchId.value = it.rchId.toString()
            mcpCardNumber.value = it.mcpCardNumber.toString()
            lmp.value = getDateFromLong(it.lmpDate)
            weekOfPregnancy.value = getWeeksOfPregnancy(it.dateOfRegistration, it.lmpDate).toString()
            edd.value = getDateFromLong(getEddFromLmp(it.lmpDate))
            bloodGroup.value = bloodGroup.getStringFromPosition(it.bloodGroupId)
            weight.value = it.weight?.toString()
            height.value = it.height?.toString()
            vdrlrprTestResult.value = vdrlrprTestResult.getStringFromPosition(it.vdrlRprTestResultId)
            if(it.vdrlRprTestResultId == 1 ||it.vdrlRprTestResultId == 2)
                list.add(list.indexOf(vdrlrprTestResult)+1, dateOfVdrlTestDone)  
            if(it.hivTestResultId == 1 ||it.hivTestResultId == 2)
                list.add(list.indexOf(hivTestResult)+1, dateOfVdrlTestDone)
            if(it.hbsAgTestResultId == 1 ||it.hbsAgTestResultId == 2)
                list.add(list.indexOf(hbsAgTestResult)+1, dateOfVdrlTestDone)
            dateOfVdrlTestDone.value = it.dateOfVdrlRprTest?.let { it1 -> getDateFromLong(it1) }
            hivTestResult.value = hivTestResult.getStringFromPosition(it.hivTestResultId)
            dateOfhivTestDone.value = it.dateOfHivTest?.let { it1 -> getDateFromLong(it1) }
            hbsAgTestResult.value = hbsAgTestResult.getStringFromPosition(it.hbsAgTestResultId)
            dateOfhbsAgTestDone.value = it.dateOfHbsAgTest?.let { it1 -> getDateFromLong(it1) }
            pastIllness.value = it.pastIllness
            if(pastIllness.value==pastIllness.entries!!.last())
                list.add(list.indexOf(pastIllness)+1, otherPastIllness)
            otherPastIllness.value = it.otherPastIllness
            isFirstPregnancy.value = isFirstPregnancy.getStringFromPosition(if(it.is1st) 1 else 2)
            if(isFirstPregnancy.value==isFirstPregnancy.entries!!.last()) {
                totalNumberOfPreviousPregnancy.value = it.numPrevPregnancy?.toString()
                complicationsDuringLastPregnancy.value = it.complicationPrevPregnancyId?.let { it1 ->
                    complicationsDuringLastPregnancy.getStringFromPosition(
                        it1
                    )
                }
                list.addAll(
                    list.indexOf(isFirstPregnancy) + 1,
                    listOf(totalNumberOfPreviousPregnancy, complicationsDuringLastPregnancy)
                )
                otherComplicationsDuringLastPregnancy.value = it.otherComplication
                if(complicationsDuringLastPregnancy.value == complicationsDuringLastPregnancy.entries!!.last())
                    list.add(list.indexOf(complicationsDuringLastPregnancy)+1, otherComplicationsDuringLastPregnancy)
            }
            isHrpCase.value = isHrpCase.getStringFromPosition(if(it.isHrp) 1 else 2)
            if(it.isHrp) {
                assignedAsHrpBy.value = assignedAsHrpBy.getStringFromPosition(it.hrpIdById)
                list.add(assignedAsHrpBy)
            }

            



        }
        setUpPage(list)

    }


    fun getIndexOfEdd() = getIndexById(edd.id)
    fun getIndexOfWeeksPregnancy() = getIndexById(weekOfPregnancy.id)
    fun getIndexOfPastIllness() = getIndexById(pastIllness.id)


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            rchId.id -> validateRchIdOnEditText(rchId)
            mcpCardNumber.id -> validateMcpOnEditText(mcpCardNumber)
            dateOfReg.id ->{
                dateOfReg.value?.let {
                    lmp.max = getLongFromDate(it)
                    lmp.min = getMinFromMaxForLmp(lmp.max!!)
                }
                -1
            }
            lmp.id -> {
                val lmpLong = lmp.value?.let { getLongFromDate(it) }
                val regLong = dateOfReg.value?.let { getLongFromDate(it) }
                val eddLong = lmpLong?.let { getEddFromLmp(it) }
                lmpLong?.let {
                    regLong?.let {
                        val bracket =
                            getWeeksOfPregnancy(regLong, lmpLong)
                        weekOfPregnancy.value = bracket.toString()
                    }
                }
                edd.value = eddLong?.let { getDateFromLong(it) }
                dateOfhivTestDone.min = regLong
                dateOfVdrlTestDone.min = regLong
                dateOfhbsAgTestDone.min = regLong
                dateOfVdrlTestDone.max = eddLong
                dateOfhivTestDone.max = eddLong
                dateOfhbsAgTestDone.max = eddLong
                -1
            }

            weight.id -> validateIntMinMax(weight)
            height.id -> validateIntMinMax(height)

            vdrlrprTestResult.id -> {
                triggerDependantsReverse(
                    source = vdrlrprTestResult,
                    passedIndex = index,
                    triggerIndex = 2,
                    target = listOf(dateOfVdrlTestDone)
                )
            }

            hivTestResult.id -> {
                triggerDependantsReverse(
                    source = hivTestResult,
                    passedIndex = index,
                    triggerIndex = 2,
                    target = listOf(dateOfhivTestDone)
                )
            }

            hbsAgTestResult.id -> {
                triggerDependantsReverse(
                    source = hbsAgTestResult,
                    passedIndex = index,
                    triggerIndex = 2,
                    target = listOf(dateOfhbsAgTestDone)
                )
            }


            pastIllness.id -> {
                val entries = pastIllness.entries!!
                pastIllness.value?.takeIf { it.isNotEmpty() }?.let {
                    if (index == 0) pastIllness.value = pastIllness.entries!!.first()
                    else pastIllness.value = it.replace(entries.first(), "")
                } ?: run {
                    pastIllness.value = pastIllness.entries!!.first()
                }
                if (pastIllness.value?.contains(entries.last()) == true) {
                    triggerDependants(
                        source = pastIllness,
                        passedIndex = index,
                        triggerIndex = index,
                        target = otherPastIllness
                    )
                } else triggerDependants(
                    source = pastIllness,
                    passedIndex = index,
                    triggerIndex = -230,
                    target = otherPastIllness
                )
            }
            otherPastIllness.id ->{
                validateEmptyOnEditText(otherPastIllness)
                validateAllAlphabetsSpaceOnEditText(otherPastIllness)
            }
            isFirstPregnancy.id -> {
                complicationsDuringLastPregnancy.apply {
                    value = entries!!.first()
                }
                triggerDependants(
                    source = isFirstPregnancy,
                    passedIndex = index,
                    triggerIndex = 1,
                    target = listOf(
                        totalNumberOfPreviousPregnancy, complicationsDuringLastPregnancy
                    ),
                    targetSideEffect = listOf(otherComplicationsDuringLastPregnancy)
                )
            }

            totalNumberOfPreviousPregnancy.id -> validateIntMinMax(totalNumberOfPreviousPregnancy)
            complicationsDuringLastPregnancy.id -> {
                triggerDependants(
                    source = complicationsDuringLastPregnancy,
                    passedIndex = index,
                    triggerIndex = complicationsDuringLastPregnancy.entries!!.lastIndex,
                    target = otherComplicationsDuringLastPregnancy
                )
            }
            otherComplicationsDuringLastPregnancy.id -> {
                validateEmptyOnEditText(otherComplicationsDuringLastPregnancy)
                validateAllAlphabetsSpaceOnEditText(otherComplicationsDuringLastPregnancy)
            }

            isHrpCase.id -> {
                triggerDependants(
                    source = isHrpCase,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = assignedAsHrpBy
                )
            }

            else -> -1
        }

    }



    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PregnantWomanRegistrationCache).let {form ->
            form.dateOfRegistration = getLongFromDate(dateOfReg.value)
            form.mcpCardNumber = mcpCardNumber.value?.takeIf { it.isNotEmpty() }?.toLong()?:0
            form.rchId = rchId.value?.takeIf { it.isNotEmpty() }?.toLong()?:0
            form.lmpDate = getLongFromDate(lmp.value)
            form.bloodGroup = bloodGroup.value
            form.bloodGroupId= bloodGroup.getPosition()
            form.weight = weight.value?.toInt()
            form.height = height.value?.toInt()
            form.vdrlRprTestResult = vdrlrprTestResult.value
            form.vdrlRprTestResultId = vdrlrprTestResult.getPosition()
            form.dateOfVdrlRprTest = dateOfVdrlTestDone.value?.let { getLongFromDate(dateOfVdrlTestDone.value)}
            form.hivTestResult = hivTestResult.value
            form.hivTestResultId = hivTestResult.getPosition()
            form.dateOfHivTest = dateOfhivTestDone.value?.let { getLongFromDate(dateOfhivTestDone.value)}
            form.hbsAgTestResult = hbsAgTestResult.value
            form.hbsAgTestResultId = hbsAgTestResult.getPosition()
            form.dateOfHbsAgTest = dateOfhbsAgTestDone.value?.let {getLongFromDate(dateOfhbsAgTestDone.value)}
            form.pastIllness = pastIllness.value
            form.otherPastIllness = otherPastIllness.value
            form.is1st = isFirstPregnancy.value==isFirstPregnancy.entries!!.first()
            form.numPrevPregnancy= totalNumberOfPreviousPregnancy.value?.toInt()
            form.complicationPrevPregnancy = complicationsDuringLastPregnancy.value
            form.complicationPrevPregnancyId = complicationsDuringLastPregnancy.getPosition()
            form.otherComplication = otherComplicationsDuringLastPregnancy.value
            form.isHrp = isHrpCase.value == isHrpCase.entries!!.first()
            form.hrpIdBy = assignedAsHrpBy.value
            form.hrpIdById = assignedAsHrpBy.getPosition()
        }
    }

    fun mapValueToBenRegId( ben: BenRegCache?): Boolean {
        val rchIdFromBen = ben?.rchId?.takeIf{it.isNotEmpty()}?.toLong()
        rchId.value?.takeIf {
            it.isNotEmpty()
        }?.toLong()?.let {
            if(it!=rchIdFromBen){
                ben?.rchId = it.toString()
                return true
            }
        }
        return false
    }
}
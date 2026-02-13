package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FilariaScreeningCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.utils.HelperUtil.parseSelections
import java.util.Calendar

class FilariaFormDataset(
    context: Context,
    currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.home_visit_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private val isSuffering = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.suffering_from_filaries),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val whichPartOfBodyMale = FormElement(
        id = 3,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.body_part),
        arrayId = R.array.body_part_male,
        entries = resources.getStringArray(R.array.body_part_male),
        required = false,
        hasDependants = true
    )

    private val whichPartOfBodyFemale = FormElement(
        id = 4,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.body_part),
        arrayId = R.array.body_part_female,
        entries = resources.getStringArray(R.array.body_part_female),
        required = false,
        hasDependants = true
    )

    private val decAndAlbDoseStatus = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_medicine_distributed),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val medicineSideEffect = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.medicine_side_effect),
        arrayId = R.array.side_effect_of_medicine,
        entries = resources.getStringArray(R.array.side_effect_of_medicine),
        required = false,
        hasDependants = true
    )

    private val sideEffectOther = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )

    var isMale = false
    var benAge = 0L

    fun isMaleFemale(ben: BenRegCache?): Boolean {
        return when (ben?.gender) {
            Gender.MALE -> {
                isMale = true
                true
            }
            Gender.FEMALE -> {
                isMale = false
                false
            }
            else -> false
        }
    }

    private fun toCsv(rawValue: String?, entries: Array<String>): String {
        return parseSelections(rawValue, entries).joinToString(", ")
    }

    suspend fun setUpPage(ben: BenRegCache?, saved: FilariaScreeningCache?) {

        val list = mutableListOf(dateOfCase, isSuffering)

        isMaleFemale(ben)
        benAge = ben!!.dob
        decAndAllDoseFieldValidation(benAge)

        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
        } else {
            dateOfCase.value = getDateFromLong(saved.mdaHomeVisitDate)
            isSuffering.value =
                getLocalValueInArray(
                    R.array.yes_no,
                    if (saved.sufferingFromFilariasis == true) "Yes" else "No"
                )

            if (isSuffering.value == "Yes") {

                val bodyField = if (isMale) whichPartOfBodyMale else whichPartOfBodyFemale

                list.add(list.indexOf(isSuffering) + 1, bodyField)
                list.add(list.indexOf(isSuffering) + 2, decAndAlbDoseStatus)
                list.add(list.indexOf(isSuffering) + 3, medicineSideEffect)

                val parsedList = parseSelections(saved.affectedBodyPart, bodyField.entries!!)

                bodyField.value = if (parsedList.isNotEmpty()) {
                    parsedList.joinToString(", ")
                } else {
                    saved.affectedBodyPart ?: ""
                }

                bodyField.isEnabled = true

                decAndAlbDoseStatus.value =
                    getLocalValueInArray(decAndAlbDoseStatus.arrayId, saved.doseStatus)

                medicineSideEffect.value =
                    getLocalValueInArray(medicineSideEffect.arrayId, saved.medicineSideEffect)

                if (medicineSideEffect.value ==
                    medicineSideEffect.entries!![medicineSideEffect.entries!!.size - 2]
                ) {
                    list.add(list.indexOf(medicineSideEffect) + 1, sideEffectOther)
                    sideEffectOther.value = saved.otherSideEffectDetails
                    sideEffectOther.isEnabled = false
                }
            }
        }

        setUpPage(list)
    }

    private fun decAndAllDoseFieldValidation(age: Long) {
        if (isYoung(age)) {
            decAndAlbDoseStatus.isEnabled = false
            decAndAlbDoseStatus.value =
                getLocalValueInArray(decAndAlbDoseStatus.arrayId, "Yes")
        } else {
            decAndAlbDoseStatus.isEnabled = true
        }
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        when (formId) {

            isSuffering.id -> {
                isSuffering.isEnabled = true

                if (isSuffering.value == resources.getStringArray(R.array.yes_no)[0]) {

                    if (isMale) {
                        triggerDependants(
                            source = isSuffering,
                            passedIndex = index,
                            triggerIndex = 0,
                            target = listOf(whichPartOfBodyMale, decAndAlbDoseStatus, medicineSideEffect)
                        )
                    } else {
                        triggerDependants(
                            source = isSuffering,
                            passedIndex = index,
                            triggerIndex = 0,
                            target = listOf(whichPartOfBodyFemale, decAndAlbDoseStatus, medicineSideEffect)
                        )
                        whichPartOfBodyFemale.isEnabled = true
                        decAndAlbDoseStatus.isEnabled = true
                        decAndAllDoseFieldValidation(benAge)
                    }

                } else {

                    val targetField =
                        if (isMale) whichPartOfBodyMale else whichPartOfBodyFemale

                    triggerforHide(
                        source = isSuffering,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = targetField,
                        targetSideEffect = listOf(
                            targetField,
                            decAndAlbDoseStatus,
                            medicineSideEffect,
                            sideEffectOther
                        )
                    )
                }

                return 0
            }

            medicineSideEffect.id -> {
                if (medicineSideEffect.value ==
                    medicineSideEffect.entries!![medicineSideEffect.entries!!.size - 2]
                ) {
                    triggerDependants(
                        source = medicineSideEffect,
                        addItems = listOf(sideEffectOther),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = medicineSideEffect,
                        addItems = listOf(),
                        removeItems = listOf(sideEffectOther)
                    )
                }
                return 0
            }

            sideEffectOther.id -> {
                validateEmptyOnEditText(sideEffectOther)
                return 0
            }
        }
        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as FilariaScreeningCache).let { form ->

            form.mdaHomeVisitDate = getLongFromDate(dateOfCase.value)
            form.createdDate = getLongFromDate(dateOfCase.value)
            form.sufferingFromFilariasis = isSuffering()
            form.diseaseTypeID = 4

            val bodyField = if (isMale) whichPartOfBodyMale else whichPartOfBodyFemale

            form.affectedBodyPart = toCsv(bodyField.value, bodyField.entries!!)

            form.otherSideEffectDetails = sideEffectOther.value
            form.doseStatus = getEnglishValueInArray(R.array.yes_no, decAndAlbDoseStatus.value)
            form.medicineSideEffect =
                getEnglishValueInArray(R.array.side_effect_of_medicine, medicineSideEffect.value)
        }
    }

    fun isSuffering(): Boolean {
        return isSuffering.value == "Yes"
    }

    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
    }

    fun getIndexOfDate(): Int {
        return getIndexById(dateOfCase.id)
    }

    fun isYoung(age: Long): Boolean {
        val calDob = Calendar.getInstance().apply { timeInMillis = age }
        val calNow = Calendar.getInstance()

        val years = calNow.get(Calendar.YEAR) - calDob.get(Calendar.YEAR)
        val months = calNow.get(Calendar.MONTH) - calDob.get(Calendar.MONTH)
        val days = calNow.get(Calendar.DAY_OF_MONTH) - calDob.get(Calendar.DAY_OF_MONTH)

        var totalYears = years
        var totalMonths = months

        if (totalMonths < 0 || (totalMonths == 0 && days < 0)) {
            totalYears--
            totalMonths += 12
        }

        return totalYears < 2
    }
}

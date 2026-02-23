package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.util.Range
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.first

class TBConfirmedDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private fun getOneYearBeforeCurrentDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private var treatmentStartDateLong: Long = 0L
    private var lastFollowUpDateLong: Long = 0L
    private var nextFollowUpMonthStart: Long = 0L
    private var nextFollowUpMonthEnd: Long = 0L

    private val regimenType = FormElement(
        id = 1,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.regimen_type),
        entries = resources.getStringArray(R.array.tb_regimen_types),
        required = true,
        hasDependants = true,
        isEnabled = true
    )

    private val treatmentStartDate = FormElement(
        id = 2,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.treatment_start_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true,
        isEnabled = true

    )

    private val expectedTreatmentCompletionDate = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.expected_treatment_completion_date),
        arrayId = -1,
        required = false,
        hasDependants = true,
        isEnabled = true,

    )

    private val followUpDate = FormElement(
        id = 4,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.follow_up_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        hasDependants = true,
        isEnabled = false

    )

    private val monthlyFollowUpDone = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.monthly_follow_up_done),
        arrayId = -1,
        required = false,
        hasDependants = false,
        isEnabled = true

    )

    private val adherenceToMedicines = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.adherence_to_medicines),
        entries = resources.getStringArray(R.array.adherence_options),
        required = true,
        hasDependants = false,
        isEnabled = true

    )

    private val anyDiscomfort = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.any_discomfort),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false,
        isEnabled = true

    )

    private val treatmentCompleted = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.treatment_completed),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true,
        isEnabled = true

    )

    private val actualTreatmentCompletionDate = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.actual_treatment_completion_date),
        arrayId = -1,
        max = System.currentTimeMillis(),
        required = false,
        hasDependants = false,
        isEnabled = true

    )

    private val treatmentOutcome = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.treatment_outcome),
        entries = resources.getStringArray(R.array.tb_treatment_outcomes),
        required = false,
        hasDependants = true,
        isEnabled = true

    )

    private val dateOfDeath = FormElement(
        id = 11,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date_of_death),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = false,
        isEnabled = true

    )

    private val placeOfDeath = FormElement(
        id = 12,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.place_of_death),
        entries = resources.getStringArray(R.array.place_of_death),
        required = false,
        hasDependants = false,
        isEnabled = true

    )

    private val reasonForDeath = FormElement(
        id = 13,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.reason_for_death),
        required = false,
        hasDependants = false,
        isEnabled = true

    )

    private val reasonForNotCompleting = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.reason_for_not_completing),
        required = false,
        hasDependants = false,
        isEnabled = true

    )



    private var lastFollowUpDate: Long? = null
    private var followUpCount = 0
    private var treatmentStartDateValue: Long? = null
    private var regimenTypeValue: String? = null
    private var isNewRecord = true

    suspend fun setUpPage(
        ben: BenRegCache?,
        saved: TBConfirmedTreatmentCache?,
        suspectedTb: TBSuspectedCache?
    ) {
        val baseList = mutableListOf<FormElement>()

        if (saved == null) {
            isNewRecord = true
            treatmentStartDate.value = getDateFromLong(System.currentTimeMillis())
            reasonForDeath.value = resources.getString(R.string.tuberculosis)

            baseList.addAll(listOf(
                regimenType,
                treatmentStartDate,
                expectedTreatmentCompletionDate,
                followUpDate,
                monthlyFollowUpDone,
                adherenceToMedicines,
                anyDiscomfort,
               // treatmentCompleted,

            ))

            treatmentStartDate.max = System.currentTimeMillis()
            treatmentStartDate.min = suspectedTb?.visitDate
                ?.takeIf { it > 0 }
                ?: getOneYearBeforeCurrentDate()


        } else
        {
            isNewRecord = false
            regimenType.value = saved.regimenType
            followUpDate.isEnabled =true
            treatmentStartDateLong = saved?.treatmentStartDate ?: 0L
            lastFollowUpDateLong = saved?.followUpDate ?: 0L

            if (!saved.regimenType.isNullOrBlank()) {
                regimenType.isEnabled = false
            }

            saved.treatmentStartDate
                ?.takeIf { it > 0 }
                ?.let {
                    treatmentStartDate.value = getDateFromLong(it)
                    treatmentStartDate.isEnabled = false
                    updateFollowUpDateConstraints()
                }
            expectedTreatmentCompletionDate.value = saved.expectedTreatmentCompletionDate?.let {
                getDateFromLong(it)
            }
            followUpDate.value = saved.followUpDate?.let { getDateFromLong(it) }
            monthlyFollowUpDone.value = saved.monthlyFollowUpDone
            adherenceToMedicines.value = saved.adherenceToMedicines
            anyDiscomfort.value = saved.anyDiscomfort?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0]
                else resources.getStringArray(R.array.yes_no)[1]
            }
            treatmentCompleted.value = saved.treatmentCompleted?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0]
                else resources.getStringArray(R.array.yes_no)[1]
            }
            actualTreatmentCompletionDate.value = saved.actualTreatmentCompletionDate?.let {
                getDateFromLong(it)
            }
            treatmentOutcome.value = saved.treatmentOutcome
            dateOfDeath.value = saved.dateOfDeath?.let { getDateFromLong(it) }
            placeOfDeath.value = saved.placeOfDeath
            reasonForDeath.value = saved.reasonForDeath
            reasonForNotCompleting.value = saved.reasonForNotCompleting

            treatmentStartDateValue = saved.treatmentStartDate
            regimenTypeValue = saved.regimenType
            lastFollowUpDate = saved.followUpDate
            baseList.addAll(listOf(
                regimenType,
                treatmentStartDate,
                expectedTreatmentCompletionDate
            ))

            if (saved.followUpDate != null) {
                baseList.addAll(listOf(
                    followUpDate,
                    monthlyFollowUpDone,
                    adherenceToMedicines,
                    anyDiscomfort
                ))
            } else {
                baseList.add(followUpDate)
            }

            if (saved.treatmentCompleted != null) {
                baseList.add(treatmentCompleted)

                if (saved.treatmentCompleted == true) {
                    baseList.add(actualTreatmentCompletionDate)

                    if (saved.treatmentOutcome != null) {
                        baseList.add(treatmentOutcome)

                        if (saved.treatmentOutcome == "Death") {
                            baseList.addAll(listOf(
                                dateOfDeath,
                                placeOfDeath,
                                reasonForDeath
                            ))
                        }
                    }
                } else {
                    baseList.add(reasonForNotCompleting)
                }
            }

            val outcomes = resources.getStringArray(R.array.tb_treatment_outcomes)

            saved.treatmentOutcome?.let { outcome ->
                if (outcome == outcomes[0] || outcome == outcomes.last()) {
                    baseList.forEach { it.isEnabled = false }
                }
            }

        }

       /* ben?.let {
            treatmentStartDate.min = suspectedTb?.visitDate ?: it.regDate
        }*/

        setUpPage(baseList)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
         when (formId)
        {
            regimenType.id -> {
                regimenTypeValue = regimenType.value
                calculateExpectedCompletionDate()
                checkAndEnableTreatmentCompletion()

            }

            treatmentStartDate.id -> {
                val dateLong = getLongFromDate(treatmentStartDate.value)
                treatmentStartDateValue = dateLong
                followUpDate.min = dateLong
                followUpDate.isEnabled = true
                calculateExpectedCompletionDate()

            }

            followUpDate.id -> {
                val dateLong = getLongFromDate(followUpDate.value)
                lastFollowUpDate = dateLong
                updateMonthlyFollowUpCount()
                val error = validateFollowUpDate(dateLong)

                followUpDate.errorText = error
                checkAndEnableTreatmentCompletion()



            }

            treatmentCompleted.id -> {
                val yesNoArray = resources.getStringArray(R.array.yes_no)

                return if (index == 0) {

                    triggerDependants(
                        source = treatmentCompleted,
                        removeItems = listOf(reasonForNotCompleting),
                        addItems = listOf(actualTreatmentCompletionDate, treatmentOutcome),
                        position = -2
                    )
                } else {
                    triggerDependants(
                        source = treatmentCompleted,
                        removeItems = listOf(actualTreatmentCompletionDate, treatmentOutcome,
                            dateOfDeath, placeOfDeath, reasonForDeath),
                        addItems = listOf(reasonForNotCompleting),
                        position = -2
                    )
                }
            }

            treatmentOutcome.id -> {
                val treatmentOutcomes = resources.getStringArray(R.array.tb_treatment_outcomes)

                return if (treatmentOutcome.value == treatmentOutcomes[3]) {
                    triggerDependants(
                        source = treatmentOutcome,
                        removeItems = listOf(),
                        addItems = listOf(dateOfDeath, placeOfDeath, reasonForDeath),
                        position = -2
                    )
                } else {
                    triggerDependants(
                        source = treatmentOutcome,
                        removeItems = listOf(dateOfDeath, placeOfDeath, reasonForDeath),
                        addItems = listOf(),
                        position = -2
                    )
                }
            }


        }
        return  0
    }

    private fun getFirstDayOfNextMonth(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getLastDayOfNextMonth(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, 2)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun isSameMonth(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.timeInMillis = date1
        cal2.timeInMillis = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }



    private fun updateFollowUpDateConstraints() {
        if (treatmentStartDateLong == 0L) {
            followUpDate.isEnabled = false
            return
        }

        followUpDate.max = System.currentTimeMillis()

        if (lastFollowUpDateLong == 0L) {
            followUpDate.min = treatmentStartDateLong
            followUpDate.isEnabled = true
        } else {
            nextFollowUpMonthStart = getFirstDayOfNextMonth(lastFollowUpDateLong)
            nextFollowUpMonthEnd = getLastDayOfNextMonth(lastFollowUpDateLong)

            followUpDate.min = nextFollowUpMonthStart
            followUpDate.isEnabled = System.currentTimeMillis() >= nextFollowUpMonthStart
        }
    }

    fun validateAllFields(): Boolean {

        val followUpDateValid = validateCurrentFollowUpDate()
        if (regimenType.required && (regimenType.value == null || regimenType.value.isNullOrEmpty())) {
            regimenType.errorText = "Regimen type is required"
            return false
        }
        if (treatmentStartDate.required && (treatmentStartDate.value == null || treatmentStartDate.value.isNullOrEmpty())) {
            treatmentStartDate.errorText = "Treatment start date is required"
            return false
        }

        return followUpDateValid
    }
    fun validateCurrentFollowUpDate(): Boolean {
        val dateString = followUpDate.value
        if (dateString == null || dateString.isEmpty()) {
            followUpDate.errorText = "Follow-up date is required"
            return false
        }

        val dateLong = getLongFromDate(dateString)
        val error = validateFollowUpDate(dateLong)
        followUpDate.errorText = error
        return error == null
    }

    private fun validateFollowUpDate(selectedDate: Long): String? {

        if (selectedDate > System.currentTimeMillis()) {
            return "Follow-up date cannot be in the future"
        }

        if (selectedDate < treatmentStartDateLong) {
            return "Follow-up date cannot be before treatment start date"
        }

        if (lastFollowUpDateLong > 0) {
            if (selectedDate <= lastFollowUpDateLong) {
                return "Follow-up date must be after last follow-up date"
            }

            if (isSameMonth(selectedDate, lastFollowUpDateLong)) {
                return "Only one follow-up is allowed per month"
            }
        }

        return null
    }


    private fun calculateExpectedCompletionDate() {
        if (regimenTypeValue == null || treatmentStartDateValue == null) return

        val calendar = Calendar.getInstance().apply {
            timeInMillis = treatmentStartDateValue!!
        }

        when (regimenTypeValue) {
            resources.getStringArray(R.array.tb_regimen_types)[0],
            resources.getStringArray(R.array.tb_regimen_types)[3],
            resources.getStringArray(R.array.tb_regimen_types)[4]
                -> {
                calendar.add(Calendar.MONTH, 6)
                expectedTreatmentCompletionDate.value = getDateFromLong(calendar.timeInMillis)
            }
            resources.getStringArray(R.array.tb_regimen_types)[1] -> {
                val minDate = Calendar.getInstance().apply {
                    timeInMillis = treatmentStartDateValue!!
                    add(Calendar.MONTH, 9)
                }.timeInMillis
                val maxDate = Calendar.getInstance().apply {
                    timeInMillis = treatmentStartDateValue!!
                    add(Calendar.MONTH, 12)
                }.timeInMillis
                expectedTreatmentCompletionDate.value = "${getDateFromLong(minDate)} to ${getDateFromLong(maxDate)}"
            }
            resources.getStringArray(R.array.tb_regimen_types)[2] -> { // Longer Regimen (18–24 Months)
                val minDate = Calendar.getInstance().apply {
                    timeInMillis = treatmentStartDateValue!!
                    add(Calendar.MONTH, 18)
                }.timeInMillis
                val maxDate = Calendar.getInstance().apply {
                    timeInMillis = treatmentStartDateValue!!
                    add(Calendar.MONTH, 24)
                }.timeInMillis
                expectedTreatmentCompletionDate.value = "${getDateFromLong(minDate)} to ${getDateFromLong(maxDate)}"
            }
        }
    }

    private fun checkAndEnableTreatmentCompletion() {
        if (regimenTypeValue == null || followUpCount == 0) return

        val requiredFollowUps = when (regimenTypeValue) {
            resources.getStringArray(R.array.tb_regimen_types)[0],
            resources.getStringArray(R.array.tb_regimen_types)[3],
            resources.getStringArray(R.array.tb_regimen_types)[4]
                -> 5
            resources.getStringArray(R.array.tb_regimen_types)[1] -> 9
            resources.getStringArray(R.array.tb_regimen_types)[2] -> 18
            else -> 0
        }

        if (followUpCount >= requiredFollowUps) {
            treatmentCompleted.isEnabled = true
            triggerDependants(source = followUpDate, addItems = listOf(treatmentCompleted),removeItems = listOf(), position = -2)
        } else {
            treatmentCompleted.isEnabled = false
            treatmentCompleted.value = null

            triggerDependants(
                source = treatmentCompleted,
                removeItems = listOf(actualTreatmentCompletionDate, treatmentOutcome,
                    reasonForNotCompleting, dateOfDeath, placeOfDeath, reasonForDeath),
                addItems = listOf(),
                position = -2
            )
        }
    }

    private fun updateMonthlyFollowUpCount() {
        if (lastFollowUpDate == null || treatmentStartDateValue == null)
        { monthlyFollowUpDone.value = resources.getString(R.string.month_format, followUpCount)
            return}


        val calendarStart = Calendar.getInstance().apply {
            timeInMillis = treatmentStartDateValue!!
        }
        val calendarFollowUp = Calendar.getInstance().apply {
            timeInMillis = lastFollowUpDate!!
        }

        val yearDiff = calendarFollowUp.get(Calendar.YEAR) - calendarStart.get(Calendar.YEAR)
        val monthDiff = calendarFollowUp.get(Calendar.MONTH) - calendarStart.get(Calendar.MONTH)

        followUpCount = (yearDiff * 12) + monthDiff + 1

        monthlyFollowUpDone.value = resources.getString(R.string.month_format, followUpCount)

        checkAndEnableTreatmentCompletion()
    }

    private fun validateMonthlyFollowUp(newDate: Long): Boolean {
        val newCalendar = Calendar.getInstance().apply { timeInMillis = newDate }
        val newMonth = newCalendar.get(Calendar.MONTH)
        val newYear = newCalendar.get(Calendar.YEAR)


        return true
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as TBConfirmedTreatmentCache).let { form ->
            form.regimenType = regimenType.value
            form.treatmentStartDate = getLongFromDate(treatmentStartDate.value)
            form.expectedTreatmentCompletionDate = getLongFromDate(expectedTreatmentCompletionDate.value)
            form.followUpDate = getLongFromDate(followUpDate.value)
            form.monthlyFollowUpDone = monthlyFollowUpDone.value
            form.adherenceToMedicines = adherenceToMedicines.value
            form.anyDiscomfort = anyDiscomfort.value == resources.getStringArray(R.array.yes_no)[0]
            form.treatmentCompleted = treatmentCompleted.value?.let {
                it == resources.getStringArray(R.array.yes_no)[0]
            }
            form.actualTreatmentCompletionDate = getLongFromDate(actualTreatmentCompletionDate.value)
            form.treatmentOutcome = treatmentOutcome.value
            form.dateOfDeath = getLongFromDate(dateOfDeath.value)
            form.placeOfDeath = placeOfDeath.value
            form.reasonForDeath = reasonForDeath.value ?: resources.getString(R.string.tuberculosis)
            form.reasonForNotCompleting = reasonForNotCompleting.value
        }
    }
}
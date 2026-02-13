package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.widget.Toast
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import java.util.Calendar

class LeprosyConfirmedDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var homeVisitDateLong: Long = System.currentTimeMillis()
    private var treatmentStartDateLong: Long = 0L
    private var lastFollowUpDateLong: Long = 0L
    private var isNewFollowUp: Boolean = false
    private var nextFollowUpMonthStart: Long = 0L
    private var nextFollowUpMonthEnd: Long = 0L
    private var context = context

    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.home_visit_date),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        hasDependants = true,
        isEnabled = false
    )

    private val leprosyStatus = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_leprosy_status),
        arrayId = R.array.leprosy_status,
        entries = resources.getStringArray(R.array.leprosy_status),
        required = false,
        hasDependants = true,
        isEnabled = false
    )

    private var referredTo = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.refer_to),
        arrayId = R.array.dc_refer,
        entries = resources.getStringArray(R.array.dc_refer),
        required = false,
        hasDependants = true,
        isEnabled = false
    )
    private var other = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = false,
        hasDependants = false
    )

    private val followUpdate = FormElement(
        id = 12,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.follow_up_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        hasDependants = true
    )

    private val remarks = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_remarks),
        required = false,
        isEnabled = false
    )

    private val leprosySymptoms = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.any_leprosy_symptoms_present),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = false,
        isEnabled = false
    )

    private val visitLabel = FormElement(
        id = 15,
        inputType = InputType.TEXT_VIEW,
        title = "Visit",
        required = false,
        isEnabled = false
    )

    private val typeOfLeprosy = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        isEnabled = false,
        required = false,
        title = resources.getString(R.string.type_of_leprocy),
        arrayId = R.array.type_of_leprocy,
        entries = resources.getStringArray(R.array.type_of_leprocy),
        hasDependants = true
    )

    private val treatmentStartDate = FormElement(
        id = 17,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.treatment_start_date),
        required = true,
        isEnabled = true,
        hasDependants = true,
        max = System.currentTimeMillis(),
    )

    private val mdt_blister_pack_recived = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title = context.getString(R.string.mdt_blister_pack_received),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        isEnabled = true,
    )

    private val treatmentStatus = FormElement(
        id = 19,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.treatment_status),
        arrayId = R.array.leprosy_treatment_status_before_time,
        entries = resources.getStringArray(R.array.leprosy_treatment_status_before_time),
        required = true,
        isEnabled = true,
    )

    private val treatmentEndDate = FormElement(
        id = 20,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.actual_treatment_completion_date),
        required = true,
        isEnabled = true,
        max = System.currentTimeMillis(),
    )

    private val treatmentEndDateEstimation = FormElement(
        id = 21,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.expected_treatment_completion_date),
        required = false,
        isEnabled = false,
        hasDependants = true,
        max = System.currentTimeMillis(),
    )


    suspend fun setUpPage(
        ben: BenRegCache?,
        saved: LeprosyScreeningCache?,
        followUp: LeprosyFollowUpCache?
    ) {
        val list = mutableListOf(
            dateOfCase,
            leprosySymptoms,
            visitLabel,
            leprosyStatus,
            referredTo,
            typeOfLeprosy,
            treatmentStartDate,
            treatmentEndDateEstimation,
            followUpdate,
            mdt_blister_pack_recived,

            )

        homeVisitDateLong = saved?.homeVisitDate ?: System.currentTimeMillis()
        treatmentStartDateLong =
            saved?.treatmentStartDate?.takeIf { it > 0L } ?: followUp?.treatmentStartDate ?: 0L
        lastFollowUpDateLong = followUp?.followUpDate ?: 0L
        // isNewFollowUp = followUp == null

        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[0]
            visitLabel.value = "Visit -${saved?.currentVisitNumber ?: 1}"
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no)[1]

            updateDateConstraints()
            checkFollowUpDateAvailability()
            calculateAndSetExpectedCompletionDate()

        } else {
            dateOfCase.value = getDateFromLong(saved.homeVisitDate)
            leprosySymptoms.value =
                getLocalValueInArray(leprosySymptoms.arrayId, saved.leprosySymptoms)
            typeOfLeprosy.value = getLocalValueInArray(typeOfLeprosy.arrayId, saved.typeOfLeprosy)
            treatmentStartDate.value = getDateFromLong(treatmentStartDateLong)
            val symptomsPosition = saved.leprosySymptomsPosition ?: 1
            leprosySymptoms.value =
                resources.getStringArray(R.array.yes_no).getOrNull(symptomsPosition)
                    ?: resources.getStringArray(R.array.yes_no)[1]
            val visit_value = saved.visitLabel
            visitLabel.value = visit_value ?: "Visit -1"
            leprosyStatus.value = getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)

            if (leprosyStatus.value == leprosyStatus.entries!!.last()) {
                leprosyStatus.value = saved.leprosyStatus
                list.add(list.indexOf(leprosyStatus) + 1, other)
            } else {
                leprosyStatus.value =
                    getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)
            }

            remarks.value = saved.remarks
            referredTo.value = getLocalValueInArray(referredTo.arrayId, saved.referToName)
            if (referredTo.value == referredTo.entries!!.last()) {
                referredTo.value = saved.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value = getLocalValueInArray(referredTo.arrayId, saved.referToName)
            }
            treatmentStartDate.value = getDateFromLong(treatmentStartDateLong)

            other.value = saved.otherReferredTo

            updateDateConstraints()
            checkFollowUpDateAvailability()
            calculateAndSetExpectedCompletionDate()


        }

        if (followUp == null) {
            visitLabel.value = "Visit -${saved?.currentVisitNumber ?: 1}"
            // followUpdate.value = getDateFromLong(System.currentTimeMillis())
            isNewFollowUp = true
        } else {
            isNewFollowUp = false
            visitLabel.value = "Visit -${saved?.currentVisitNumber ?: 1}"
            // followUpdate.value = getDateFromLong(followUp.followUpDate)
            mdt_blister_pack_recived.value = getLocalValueInArray(
                mdt_blister_pack_recived.arrayId,
                followUp.mdtBlisterPackReceived
            )
            treatmentStatus.value = getLocalValueInArray(
                treatmentStatus.arrayId,
                followUp.treatmentStatus
            )

            if (followUp.treatmentCompleteDate > 0) {
                treatmentEndDate.value = getDateFromLong(followUp.treatmentCompleteDate)
                list.add(list.indexOf(treatmentStatus) + 1, treatmentEndDate)
            }

            remarks.value = followUp.remarks

            updateDateConstraints()
            checkFollowUpDateAvailability()
            calculateAndSetExpectedCompletionDate()
        }

        setUpPage(list)
    }

    suspend fun setUpPage(ben: BenRegCache?, followUp: LeprosyFollowUpCache?) {
        val list = mutableListOf(
            dateOfCase,
            leprosySymptoms,
            visitLabel,
            leprosyStatus,
            referredTo,
            typeOfLeprosy,
            treatmentStartDate,
            followUpdate,
            mdt_blister_pack_recived,
            treatmentStatus,
        )

        dateOfCase.isEnabled = false
        leprosyStatus.isEnabled = false
        visitLabel.isEnabled = false
        leprosySymptoms.isEnabled = false
        referredTo.isEnabled = false
        typeOfLeprosy.isEnabled = false
        treatmentStartDate.isEnabled = false
        followUpdate.isEnabled = false
        mdt_blister_pack_recived.isEnabled = false
        treatmentStatus.isEnabled = false

        homeVisitDateLong = followUp?.homeVisitDate ?: System.currentTimeMillis()
        treatmentStartDateLong = followUp?.treatmentStartDate ?: 0L
        lastFollowUpDateLong = followUp?.followUpDate ?: 0L

        treatmentStatus.arrayId = R.array.leprosy_treatment_status
        treatmentStatus.entries = resources.getStringArray(R.array.leprosy_treatment_status)
        if (followUp == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[0]
            visitLabel.value = "Visit -1"
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no)[1]
            isNewFollowUp = true
            updateDateConstraints()
        } else {
            isNewFollowUp = false
            dateOfCase.value = getDateFromLong(followUp.homeVisitDate)
            leprosySymptoms.value =
                getLocalValueInArray(leprosySymptoms.arrayId, followUp.leprosySymptoms)
            typeOfLeprosy.value =
                getLocalValueInArray(typeOfLeprosy.arrayId, followUp.typeOfLeprosy)
            treatmentStartDate.value = getDateFromLong(followUp.treatmentStartDate)
            val symptomsPosition = followUp.leprosySymptomsPosition ?: 1
            leprosySymptoms.value =
                resources.getStringArray(R.array.yes_no).getOrNull(symptomsPosition)
                    ?: resources.getStringArray(R.array.yes_no)[1]
            val visit_value = followUp.visitLabel
            visitLabel.value = visit_value ?: "Visit -1"
            leprosyStatus.value =
                getLocalValueInArray(leprosyStatus.arrayId, followUp.leprosyStatus)

            if (leprosyStatus.value == leprosyStatus.entries!!.last()) {
                leprosyStatus.value = followUp.leprosyStatus
                list.add(list.indexOf(leprosyStatus) + 1, other)
            } else {
                leprosyStatus.value =
                    getLocalValueInArray(leprosyStatus.arrayId, followUp.leprosyStatus)
            }

            remarks.value = followUp.remarks
            referredTo.value = getLocalValueInArray(referredTo.arrayId, followUp.referToName)
            if (referredTo.value == referredTo.entries!!.last()) {
                referredTo.value = followUp.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value = getLocalValueInArray(referredTo.arrayId, followUp.referToName)
            }
            treatmentStartDate.value = getDateFromLong(followUp.treatmentStartDate)
            followUpdate.value = getDateFromLong(followUp.followUpDate)
            mdt_blister_pack_recived.value = getLocalValueInArray(
                mdt_blister_pack_recived.arrayId,
                followUp.mdtBlisterPackReceived
            )

            treatmentStatus.value = getLocalValueInArray(
                treatmentStatus.arrayId,
                followUp.treatmentStatus
            )

            if (followUp.treatmentCompleteDate > 0) {
                treatmentEndDate.value = getDateFromLong(followUp.treatmentCompleteDate)
                list.add(list.indexOf(treatmentStatus) + 1, treatmentEndDate)
            }

            updateDateConstraints()
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        when (formId) {
            treatmentStatus.id -> {

                if (treatmentStatus.value == treatmentStatus.entries!!.last()) {
                    triggerDependants(
                        source = treatmentStatus,
                        addItems = listOf(treatmentEndDate),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = treatmentStatus,
                        addItems = listOf(),
                        removeItems = listOf(treatmentEndDate)
                    )
                }
            }

            treatmentStartDate.id -> {
                treatmentStartDateLong = getLongFromDate(treatmentStartDate.value)
                calculateAndSetExpectedCompletionDate()
                followUpdate.min = treatmentStartDateLong
                followUpdate.isEnabled = true

            }

            followUpdate.id -> {
                //   lastFollowUpDateLong = getLongFromDate(followUpdate.value)
                updateTreatmentStatusDropdown()
                triggerDependants(
                    source = followUpdate,
                    addItems = listOf(treatmentStatus),
                    removeItems = listOf()
                )


            }

        }
        return 0
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as LeprosyFollowUpCache).let { followUp ->
            followUp.followUpDate = getLongFromDate(followUpdate.value)
            followUp.mdtBlisterPackReceived = mdt_blister_pack_recived.value
            followUp.treatmentStatus = treatmentStatus.value
            followUp.treatmentStartDate = getLongFromDate(treatmentStartDate.value)

            if (treatmentStatus.value == treatmentStatus.entries?.last()) {
                followUp.treatmentCompleteDate = getLongFromDate(treatmentEndDate.value)
            } else {
                followUp.treatmentCompleteDate = 0
            }

            followUp.remarks = remarks.value
        }
    }


    private fun calculateAndSetExpectedCompletionDate() {
        if (treatmentStartDateLong == 0L) {
            treatmentEndDateEstimation.value = ""
            return
        }

        val typeOfLeprosyValue = typeOfLeprosy.value
        if (typeOfLeprosyValue.isNullOrBlank()) {
            treatmentEndDateEstimation.value = ""
            return
        }

        val leprosyTypes = resources.getStringArray(R.array.type_of_leprocy)
        val index = leprosyTypes.indexOf(typeOfLeprosyValue)

        val monthsToAdd = when (index) {
            0 -> 12
            1 -> 6
            else -> {
                treatmentEndDateEstimation.value = ""
                return
            }
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = treatmentStartDateLong
        calendar.add(Calendar.MONTH, monthsToAdd)

        treatmentEndDateEstimation.value = getDateFromLong(calendar.timeInMillis)
    }

    fun getIndexOfDate(): Int {
        return getIndexById(followUpdate.id)
    }

    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array2)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
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

    private fun isSameOrAfterMonth(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()

        cal1.timeInMillis = date1
        cal2.timeInMillis = date2

        val year1 = cal1.get(Calendar.YEAR)
        val year2 = cal2.get(Calendar.YEAR)

        val month1 = cal1.get(Calendar.MONTH)
        val month2 = cal2.get(Calendar.MONTH)

        return when {
            year1 > year2 -> true
            year1 == year2 && month1 >= month2 -> true
            else -> false
        }
    }

    private fun getFirstDayOfCurrentMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun updateTreatmentStatusDropdown() {
        val followUpDateLong = getLongFromDate(followUpdate.value)
        val expectedEndDateLong = getLongFromDate(treatmentEndDateEstimation.value)

        if (followUpDateLong > 0 && expectedEndDateLong > 0 &&
            isSameOrAfterMonth(followUpDateLong, expectedEndDateLong)
        ) {
            treatmentStatus.arrayId = R.array.leprosy_treatment_status
            treatmentStatus.entries =
                resources.getStringArray(R.array.leprosy_treatment_status)
        } else {
            treatmentStatus.arrayId = R.array.leprosy_treatment_status_before_time
            treatmentStatus.entries =
                resources.getStringArray(R.array.leprosy_treatment_status_before_time)
        }

        if (!treatmentStatus.entries!!.contains(treatmentStatus.value)) {
            treatmentStatus.value = ""
        }
    }


    private fun checkFollowUpDateAvailability() {
        val currentTime = System.currentTimeMillis()
        if (treatmentStartDateLong == 0L) {
            followUpdate.isEnabled = false
            return
        }
        if (lastFollowUpDateLong == 0L) {
            followUpdate.isEnabled = true
            return
        }

        nextFollowUpMonthStart = getFirstDayOfNextMonth(lastFollowUpDateLong)
        nextFollowUpMonthEnd = getLastDayOfNextMonth(lastFollowUpDateLong)

        if (currentTime < nextFollowUpMonthStart) {
            followUpdate.isEnabled = false
            Toast.makeText(
                context,
                "Next follow-up will be available from ${getDateFromLong(nextFollowUpMonthStart)}",
                Toast.LENGTH_LONG
            ).show()
        } else if (currentTime >= nextFollowUpMonthStart && currentTime <= nextFollowUpMonthEnd) {
            followUpdate.isEnabled = true
            // followUpdate.errorMessage = null
        } else {
            followUpdate.isEnabled = true
        }
    }

    fun validateFollowUpDate(selectedDate: Long): String? {
        if (lastFollowUpDateLong == 0L) {
            if (selectedDate < homeVisitDateLong) {
                return "Follow-up date cannot be before home visit date"
            }
            if (selectedDate > System.currentTimeMillis()) {
                return "Follow-up date cannot be in the future"
            }
            return null
        }

        val nextMonthStart = getFirstDayOfNextMonth(lastFollowUpDateLong)
        val currentTime = System.currentTimeMillis()

        /* if (selectedDate < nextMonthStart) {
             return "Next follow-up must be in the next month. Please select a date from ${getDateFromLong(nextMonthStart)}"
         }*/

        if (selectedDate > currentTime) {
            return "Follow-up date cannot be in the future"
        }

        /* if (!isSameOrAfterMonth(selectedDate, nextMonthStart)) {
             val cal = Calendar.getInstance()
             cal.timeInMillis = nextMonthStart
             val monthName = getMonthName(cal.get(Calendar.MONTH))
             return "Follow-up must be in $monthName. Please select a date between ${getDateFromLong(nextMonthStart)} and ${getDateFromLong(getLastDayOfNextMonth(lastFollowUpDateLong))}"
         }*/

        return null
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return monthNames.getOrElse(month) { "Unknown" }
    }


    fun validateForm(): String? {
        if (followUpdate.value!!.isBlank()) {
            return "Follow-up date is required"
        }

        val selectedDate = getLongFromDate(followUpdate.value)
        val dateValidation = validateFollowUpDate(selectedDate)
        if (dateValidation != null) {
            return dateValidation
        }

        if (treatmentStatus.value!!.isBlank()) {
            return "Treatment status is required"
        }

        if (treatmentStatus.value == treatmentStatus.entries?.last() && treatmentEndDate.value!!.isBlank()) {
            return "Treatment end date is required when treatment status is completed"
        }

        return null
    }


    private fun updateDateConstraints() {
        treatmentStartDate.min = homeVisitDateLong
        if (treatmentStartDateLong > 0) {
            treatmentStartDate.isEnabled = false
            treatmentStartDate.required = false

        } else {
            followUpdate.isEnabled = false
            return
        }

        if (lastFollowUpDateLong > 0) {
            val nextMonthStart = getFirstDayOfNextMonth(lastFollowUpDateLong)
            followUpdate.min = nextMonthStart
            followUpdate.max = System.currentTimeMillis()
        } else {
            followUpdate.min = treatmentStartDateLong
            followUpdate.max = System.currentTimeMillis()
        }

        val followUpDateLong = getLongFromDate(followUpdate.value)
        treatmentEndDate.min = if (followUpDateLong > 0) followUpDateLong else followUpdate.min
    }


    fun getNextFollowUpAvailabilityMessage(): String? {
        if (lastFollowUpDateLong == 0L) {
            return null
        }

        val nextMonthStart = getFirstDayOfNextMonth(lastFollowUpDateLong)
        val currentTime = System.currentTimeMillis()

        if (currentTime < nextMonthStart) {
            return "Next follow-up will be available from ${getDateFromLong(nextMonthStart)}"
        }

        return null
    }


}
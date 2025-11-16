package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache

class LeprosyConfirmedDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var homeVisitDateLong: Long = System.currentTimeMillis()
    private var treatmentStartDateLong: Long = 0L
    private var lastFollowUpDateLong: Long = 0L
    private var isNewFollowUp: Boolean = false

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
        title = resources.getString(R.string.leprosy_status),
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
        title = "Any Leprosy Symptoms Present?",
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
        required = false,
        isEnabled = false,
        max = System.currentTimeMillis(),
    )

    private val mdt_blister_pack_recived = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title = "MDT/BLISTER PACK RECIVED",
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        isEnabled = true,
    )

    private val treatmentStatus = FormElement(
        id = 19,
        inputType = InputType.DROPDOWN,
        title = "Treatment Status",
        arrayId = R.array.leprosy_treatment_status,
        entries = resources.getStringArray(R.array.leprosy_treatment_status),
        required = true,
        isEnabled = true,
    )

    private val treatmentEndDate = FormElement(
        id = 20,
        inputType = InputType.DATE_PICKER,
        title = "Treatment End Date",
        required = false,
        isEnabled = true,
        max = System.currentTimeMillis(),
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: LeprosyScreeningCache?, followUp: LeprosyFollowUpCache?) {
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

        // Initialize date constraints
        homeVisitDateLong = saved?.homeVisitDate ?: System.currentTimeMillis()
        treatmentStartDateLong = saved?.treatmentStartDate ?: 0L
        lastFollowUpDateLong = followUp?.followUpDate ?: 0L
        isNewFollowUp = followUp == null

        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[0]
            visitLabel.value = "Visit -1"
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no)[1]

            // Set initial min dates
            updateDateConstraints()
        } else {
            dateOfCase.value = getDateFromLong(saved.homeVisitDate)
            leprosySymptoms.value = getLocalValueInArray(leprosySymptoms.arrayId, saved.leprosySymptoms)
            typeOfLeprosy.value = getLocalValueInArray(typeOfLeprosy.arrayId, saved.typeOfLeprosy)
            treatmentStartDate.value = getDateFromLong(saved.treatmentStartDate)
            val symptomsPosition = saved.leprosySymptomsPosition ?: 1
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no).getOrNull(symptomsPosition)
                ?: resources.getStringArray(R.array.yes_no)[1]
            val visit_value = saved.visitLabel
            visitLabel.value = visit_value ?: "Visit -1"
            leprosyStatus.value = getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)

            if (leprosyStatus.value == leprosyStatus.entries!!.last()) {
                leprosyStatus.value = saved.leprosyStatus
                list.add(list.indexOf(leprosyStatus) + 1, other)
            } else {
                leprosyStatus.value = getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)
            }

            remarks.value = saved.remarks
            referredTo.value = getLocalValueInArray(referredTo.arrayId, saved.referToName)
            if (referredTo.value == referredTo.entries!!.last()) {
                referredTo.value = saved.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value = getLocalValueInArray(referredTo.arrayId, saved.referToName)
            }
            treatmentStartDate.value = getDateFromLong(saved.treatmentStartDate)

            other.value = saved.otherReferredTo

            // Update date constraints
            updateDateConstraints()
        }

        if (followUp == null) {
            visitLabel.value = "Visit -${saved?.currentVisitNumber ?: 1}"
            followUpdate.value = getDateFromLong(System.currentTimeMillis())
        } else {
            visitLabel.value = "Visit -${saved?.currentVisitNumber ?: 1}"
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

            remarks.value = followUp.remarks

            // Update date constraints for existing follow-up
            updateDateConstraints()
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

        // Initialize date constraints
        homeVisitDateLong = followUp?.homeVisitDate ?: System.currentTimeMillis()
        treatmentStartDateLong = followUp?.treatmentStartDate ?: 0L
        lastFollowUpDateLong = followUp?.followUpDate ?: 0L
        isNewFollowUp = followUp == null

        if (followUp == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[0]
            visitLabel.value = "Visit -1"
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no)[1]

            // Set initial min dates
            updateDateConstraints()
        } else {
            dateOfCase.value = getDateFromLong(followUp.homeVisitDate)
            leprosySymptoms.value = getLocalValueInArray(leprosySymptoms.arrayId, followUp.leprosySymptoms)
            typeOfLeprosy.value = getLocalValueInArray(typeOfLeprosy.arrayId, followUp.typeOfLeprosy)
            treatmentStartDate.value = getDateFromLong(followUp.treatmentStartDate)
            val symptomsPosition = followUp.leprosySymptomsPosition ?: 1
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no).getOrNull(symptomsPosition)
                ?: resources.getStringArray(R.array.yes_no)[1]
            val visit_value = followUp.visitLabel
            visitLabel.value = visit_value ?: "Visit -1"
            leprosyStatus.value = getLocalValueInArray(leprosyStatus.arrayId, followUp.leprosyStatus)

            if (leprosyStatus.value == leprosyStatus.entries!!.last()) {
                leprosyStatus.value = followUp.leprosyStatus
                list.add(list.indexOf(leprosyStatus) + 1, other)
            } else {
                leprosyStatus.value = getLocalValueInArray(leprosyStatus.arrayId, followUp.leprosyStatus)
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

            // Update date constraints for existing follow-up
            updateDateConstraints()
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
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
                // Update treatment start date and refresh constraints
                treatmentStartDateLong = getLongFromDate(treatmentStartDate.value)
                updateDateConstraints()
                return 0
            }

            followUpdate.id -> {
                // Update follow-up date and refresh constraints
                lastFollowUpDateLong = getLongFromDate(followUpdate.value)
                updateDateConstraints()
                return 0
            }

            else -> {
                return 0
            }
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as LeprosyFollowUpCache).let { followUp ->
            followUp.followUpDate = getLongFromDate(followUpdate.value)
            followUp.mdtBlisterPackReceived = mdt_blister_pack_recived.value
            followUp.treatmentStatus = treatmentStatus.value

            if (treatmentStatus.value == treatmentStatus.entries?.last()) {
                followUp.treatmentCompleteDate = getLongFromDate(treatmentEndDate.value)
            } else {
                followUp.treatmentCompleteDate = 0
            }

            followUp.remarks = remarks.value
        }
    }


    /**
     * Validate form data before submission
     * @return null if validation passes, error message if validation fails
     */
    fun validateForm(): String? {
        // Check 15-day gap for new follow-ups
        if (isNewFollowUp && lastFollowUpDateLong > 0) {
            val selectedFollowUpDate = getLongFromDate(followUpdate.value)
            val daysGap = (selectedFollowUpDate - lastFollowUpDateLong) / (24 * 60 * 60 * 1000)

            if (daysGap < 15) {
                return "Follow-up date must be at least 15 days after the last follow-up. Current gap is ${daysGap.toInt()} days."
            }
        }

        // Add other basic validations
        if (followUpdate.value!!.isBlank()) {
            return "Follow-up date is required"
        }

        if (treatmentStatus.value!!.isBlank()) {
            return "Treatment status is required"
        }

        // Validate treatment end date if treatment is completed
        if (treatmentStatus.value == treatmentStatus.entries?.last() && treatmentEndDate.value!!.isBlank()) {
            return "Treatment end date is required when treatment status is completed"
        }

        return null // Return null if validation passes
    }
    fun getIndexOfDate(): Int {
        return getIndexById(followUpdate.id)
    }

    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus = englishResources.getStringArray(R.array.nbr_reproductive_status_array)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
    }

    /**
     * Update date constraints based on the business logic:
     * - Treatment start date minimum should be home visit date
     * - Follow-up date minimum should be treatment start date or last follow-up date
     * - Treatment end date minimum should be follow-up date
     * - For new follow-ups, enforce 15-day gap from last follow-up
     */
    private fun updateDateConstraints() {
        treatmentStartDate.min = homeVisitDateLong

        val followUpMinDate = maxOf(treatmentStartDateLong, lastFollowUpDateLong, homeVisitDateLong)
        followUpdate.min = if (followUpMinDate > 0) followUpMinDate else homeVisitDateLong

        if (isNewFollowUp && lastFollowUpDateLong > 0) {
            val minDateWithGap = lastFollowUpDateLong + (15L * 24 * 60 * 60 * 1000)
            followUpdate.min = maxOf(followUpdate.min!!.toLong(), minDateWithGap)
        }

        val followUpDateLong = getLongFromDate(followUpdate.value)
        treatmentEndDate.min = if (followUpDateLong > 0) followUpDateLong else followUpdate.min
    }
}
package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.*

class SuspectedTBDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val isSputumCollected = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_sputum_sample_collected),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var sputumSubmittedAt = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.sputum_sample_submitted_at),
        entries = resources.getStringArray(R.array.tb_submitted_yet),
        required = false,
        hasDependants = false
    )

    private var nikshayId = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.nikshay_id),
        required = false,
        hasDependants = false
    )

    private var sputumTestResult = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.sputum_test_result),
        entries = resources.getStringArray(R.array.tb_test_result),
        required = false,
        hasDependants = false
    )

    private var referred = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.referred_to_facility),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var followUps = FormElement(
        id = 7,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.facility_referral_follow_ups),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = false
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: TBSuspectedCache?) {
        var list = mutableListOf(
            dateOfVisit,
            isSputumCollected,
//            sputumSubmittedAt,
//            nikshayId,
//            sputumTestResult,
            referred,
            followUps
        )
        if (saved == null) {
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
        } else {
            if (saved.isSputumCollected == true) {
                list = mutableListOf(
                    dateOfVisit,
                    isSputumCollected,
                    sputumSubmittedAt,
                    nikshayId,
                    sputumTestResult,
                    referred,
                    followUps
                )
                dateOfVisit.value = getDateFromLong(saved.visitDate)
                isSputumCollected.value = if (saved.isSputumCollected == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                sputumSubmittedAt.value = resources.getStringArray(R.array.tb_submitted_yet)[englishResources.getStringArray(R.array.tb_submitted_yet).indexOf(saved.sputumSubmittedAt)]
                nikshayId.value = saved.nikshayId
                sputumTestResult.value = resources.getStringArray(R.array.tb_test_result)[englishResources.getStringArray(R.array.tb_test_result).indexOf(saved.sputumTestResult)]
                referred.value = if (saved.referred == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                followUps.value = saved.followUps
            } else {
                dateOfVisit.value = getDateFromLong(saved.visitDate)
                isSputumCollected.value = if (saved.isSputumCollected == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                referred.value = if (saved.referred == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                followUps.value = saved.followUps
            }
        }


        ben?.let {
            dateOfVisit.min = it.regDate
        }
        setUpPage(list)

    }
    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            isSputumCollected.id -> {
                triggerDependants(
                    source = isSputumCollected,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = sputumTestResult
                )
                triggerDependants(
                    source = isSputumCollected,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = nikshayId
                )
                triggerDependants(
                    source = isSputumCollected,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = sputumSubmittedAt
                )
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as TBSuspectedCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.isSputumCollected = isSputumCollected.value == resources.getStringArray(R.array.yes_no)[0]
            form.sputumSubmittedAt = englishResources.getStringArray(R.array.tb_submitted_yet)[sputumSubmittedAt.entries!!.indexOf(sputumSubmittedAt.value)]
            form.nikshayId = nikshayId.value
            form.sputumTestResult = englishResources.getStringArray(R.array.tb_test_result)[sputumTestResult.entries!!.indexOf(sputumTestResult.value)]
            form.referred = referred.value == resources.getStringArray(R.array.yes_no)[0]
            form.followUps = followUps.value
        }
    }


    fun isTestPositive(): String? {
        return if ( sputumTestResult.value == resources.getStringArray(R.array.tb_test_result)[0] )
            resources.getString(R.string.tb_suspected_alert_positive) else null
    }

}
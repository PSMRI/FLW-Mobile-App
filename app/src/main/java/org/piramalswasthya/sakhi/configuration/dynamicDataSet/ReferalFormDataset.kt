package org.piramalswasthya.sakhi.configuration.dynamicDataSet
import android.content.Context
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.Dataset
import org.piramalswasthya.sakhi.configuration.FormDataModel
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.ReferalCache
import java.util.Calendar

class ReferalFormDataset(context: Context, language: Languages,var preferenceDao: PreferenceDao) : Dataset(context, language) {

    private val healthCenter = FormElement(
        id = 1,
        inputType = DROPDOWN,
        title = resources.getString(R.string.referal_facility),
        arrayId = R.array.TB_referral_facility,
        entries = resources.getStringArray(R.array.TB_referral_facility),
        required = true,
        hasDependants = true
    )
    private val reasonForReferal = FormElement(
        id = 2,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = resources.getString(R.string.referral_reason),
        arrayId = -1,
        required = false,

    )

    private val additionalService = FormElement(
        id = 3,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = resources.getString(R.string.additional_services),
        value = "FLW",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        )

    private val referDate = FormElement(
        id = 4,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.refer_date),
        arrayId = -1,
        required = true,
        isEnabled = true,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
    )

    private val referralReasonResIds = listOf(
        R.string.tb_suspected_ncd_case,
        R.string.tb_suspected_leprosy_case,
        R.string.tb_suspected_copd_case,
        R.string.tb_suspected_form,
        R.string.further_depression,
        R.string.suspected_c_case,
        R.string.tb_screening_form,
        R.string.suspected_newborn_complication,
        R.string.suspected_illness_young_child
    )

    private fun toEnglishReferralReason(reason: String?): String? {
        val resId = referralReasonResIds.firstOrNull { resources.getString(it) == reason } ?: return reason
        return englishResources.getString(resId)
    }

    private val priority = FormElement(
        id = 5,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = resources.getString(R.string.gc_priority),
        arrayId = -1,
        required = false,
    )

    var referralTypes = ""
    private var gcPriority: String? = null
    suspend fun setUpPage(
        referral: String,
        referralType: String,
        referralDate: Long = 0L,
        referralPriority: String = ""
    ) {
        val list = mutableListOf(
            referDate,
            healthCenter,
            reasonForReferal,

            )
        referralTypes = referralType
        referDate.value = getDateFromLong(System.currentTimeMillis())
        reasonForReferal.value = referral
        if (referralType == GENERAL_CHECKUP) setUpGeneralCheckup(list, referralDate, referralPriority)
        setUpPage(list)
    }

    /** General Check-up BRD FR7: 5 facility levels, date prefilled from the check-up, today − 1 month..today. */
    private fun setUpGeneralCheckup(list: MutableList<FormElement>, referralDate: Long, referralPriority: String) {
        healthCenter.arrayId = R.array.gc_referral_facility
        healthCenter.entries = resources.getStringArray(R.array.gc_referral_facility)
        referDate.min = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        if (referralDate > 0L) referDate.value = getDateFromLong(referralDate)
        gcPriority = referralPriority.ifBlank { "ROUTINE" }
        priority.value = resources.getString(
            if (referralPriority == "EMERGENCY") R.string.gc_priority_emergency_short
            else R.string.gc_priority_routine_short
        )
        list.add(priority)
    }

    companion object {
        const val GENERAL_CHECKUP = "GENERAL_CHECKUP"

        /**
         * Institute IDs for R.array.gc_referral_facility (HWC, PHC, CHC, Medical College, District Hospital),
         * in array order. They reuse the IDs the TB facility list already sends for the same level
         * (HWC-Subcenter 2, PHC 3, CHC 4, District Hospital 5, Govt. Medical College 7), so the server
         * reads one meaning per ID. To be confirmed with the server team (FLW-1228).
         */
        val GC_FACILITY_IDS = intArrayOf(2, 3, 4, 7, 5)
    }

    override suspend fun handleListOnValueChanged(
        formId: Int,
        index: Int
    ): Int {
        return when (formId) {
            reasonForReferal.id -> {
                validateEmptyOnEditText(reasonForReferal)
            }

            healthCenter.id -> {
                validateEmptyOnEditText(healthCenter)
            }

            else -> {
                -1
            }
        }



    }

    override fun mapValues(
        cacheModel: FormDataModel,
        pageNumber: Int
    ) {
        (cacheModel as ReferalCache).let { form ->
            form.revisitDate = getLongFromDate(referDate.value)
            form.referralReason = toEnglishReferralReason(reasonForReferal.value)
            form.refrredToAdditionalServiceList = listOf("FLW")
            form.referredToInstituteID = healthCenter.getPosition()
            if (referralTypes == GENERAL_CHECKUP) {
                form.referredToInstituteID = GC_FACILITY_IDS.getOrNull(healthCenter.getPosition() - 1)
                form.priority = gcPriority
            }
            form.referredToInstituteName = healthCenter.getEnglishStringFromPosition(healthCenter.getPosition())
            form.createdBy =  preferenceDao.getLoggedInUser()?.userName
            form.vanID =  preferenceDao.getLoggedInUser()?.vanId
            form.providerServiceMapID =  preferenceDao.getLoggedInUser()?.serviceMapId
            form.parkingPlaceID =  preferenceDao.getLoggedInUser()?.serviceMapId
            form.type = referralTypes




        }

    }

}
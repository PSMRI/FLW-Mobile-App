package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.text.InputType
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.TBScreeningCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdolescentHealthFormDataset(context: Context, language: Languages) : Dataset(context, language) {

    companion object {
        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

        private fun getCurrentDateMillis(): Long {
            return Calendar.getInstance().timeInMillis
        }
    }


    private val visitDate = FormElement(
        id = 3,
        inputType = DATE_PICKER,
        title = "Visit Date",
        arrayId = -1,
        required = true,
        max = getCurrentDateMillis()
    )

    private val healthStatus = FormElement(
        id = 4,
        inputType = DROPDOWN,
        title = "Health Status",
        arrayId = -1,
        entries = arrayOf("Healthy", "Anemic", "Malnourished"),
        required = true,
        hasDependants = true
    )

    private val ifaTabletDistribution = FormElement(
        id = 5,
        inputType = RADIO,
        title = "IFA Tablet Distribution",
        arrayId = -1,
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val ifaTabletQuantity = FormElement(
        id = 6,
        inputType = EDIT_TEXT,
        title = "Quantity of IFA Tablets",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        min = 1L,
        max = 100L
    )

    private val menstrualHygieneAwareness = FormElement(
        id = 7,
        inputType = RADIO,
        title = "Menstrual Hygiene Awareness",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false
    )

    private val sanitaryNapkinDistributed = FormElement(
        id = 8,
        inputType = RADIO,
        title = "Sanitary Napkin Distributed",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val noOfPacketsDistributed = FormElement(
        id = 9,
        inputType = EDIT_TEXT,
        title = "No. of Packets Distributed",
        arrayId = -1,
        required = true,
        min = 1L,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        max = 20L
    )

    private val place = FormElement(
        id = 10,
        inputType = DROPDOWN,
        title = "Place",
        arrayId = -1,
        entries = arrayOf("Home", "Community center", "School", "Subcenter"),
        required = true
    )


    private val referredToHealthFacility = FormElement(
        id = 12,
        inputType = EDIT_TEXT,
        title = "Referred to Health Facility",
        arrayId = -1,
        required = false,
    )

    private val counselingProvided = FormElement(
        id = 13,
        inputType = RADIO,
        title = "Counseling Provided",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = true
    )

    private val counselingType = FormElement(
        id = 14,
        inputType = DROPDOWN,
        title = "Counseling Type",
        arrayId = -1,
        entries = arrayOf("Individual", "Group"),
        required = false
    )

    private val followUpDate = FormElement(
        id = 15,
        inputType = DATE_PICKER,
        title = "Follow-up Date",
        arrayId = -1,
        required = false,
        max = getCurrentDateMillis()
    )

    private val referralStatus = FormElement(
        id = 16,
        inputType = DROPDOWN,
        title = "Referral Status",
        arrayId = -1,
        entries = arrayOf("Pending", "Completed"),
        required = false
    )

    private val sDate = FormElement(
        id = 15,
        inputType = DATE_PICKER,
        title = "Date",
        arrayId = -1,
        required = false,
        max = getCurrentDateMillis()
    )

    private val firstPage: List<FormElement> by lazy {
        listOf(
            visitDate,
            healthStatus,
            ifaTabletDistribution,
            menstrualHygieneAwareness,
            sanitaryNapkinDistributed,
            counselingProvided,
            counselingType,
            followUpDate,
            referralStatus
        )
    }

    suspend fun setFirstPage(ben: BenRegCache?, saved: AdolescentHealthCache?) {
        val list = firstPage.toMutableList()
        if (visitDate.value == null) {
            visitDate.value = getCurrentDateString()
        }
        saved?.let { saved ->
            visitDate.value = saved.visitDate?.let { getDateFromLong(it) }
            healthStatus.value = saved.healthStatus
            ifaTabletDistribution.value = if (saved.ifaTabletDistributed == true) ifaTabletDistribution.entries!![0] else ifaTabletDistribution.entries!![1]
            ifaTabletQuantity.value = saved.ifaTabletDistributed?.toString()
            menstrualHygieneAwareness.value = if (saved.menstrualHygieneAwarenessGiven == true) menstrualHygieneAwareness.entries!![0] else menstrualHygieneAwareness.entries!![1]
            sanitaryNapkinDistributed.value = if (saved.sanitaryNapkinDistributed == true) sanitaryNapkinDistributed.entries!![0] else sanitaryNapkinDistributed.entries!![1]
            noOfPacketsDistributed.value = saved.noOfPacketsDistributed?.toString()
            place.value = saved.place
            sDate.value = saved.distributionDate?.let { getDateFromLong(it) }
            referredToHealthFacility.value = saved.referredToHealthFacility
            counselingProvided.value = if (saved.counselingProvided == true) counselingProvided.entries!![0] else counselingProvided.entries!![1]
            counselingType.value = saved.counselingType
            followUpDate.value = saved.followUpDate?.let { getDateFromLong(it) }
            referralStatus.value = saved.referralStatus
        }

        if (ifaTabletDistribution.value == ifaTabletDistribution.entries!![0]) {
            list.add(list.indexOf(ifaTabletDistribution) + 1, ifaTabletQuantity)

        }

        if (sanitaryNapkinDistributed.value == sanitaryNapkinDistributed.entries!![0]) {
            list.add(list.indexOf(sanitaryNapkinDistributed) + 1, noOfPacketsDistributed)
            list.add(list.indexOf(noOfPacketsDistributed) + 1, place)
            list.add(list.indexOf(place) + 1, sDate)
        }

        if (healthStatus.value == healthStatus.entries!![1] || healthStatus.value == healthStatus.entries!![2]) {
            referredToHealthFacility.required = true
            list.add(list.indexOf(healthStatus) + 1, referredToHealthFacility)
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

            visitDate.id -> {
                validateEmptyOnEditText(visitDate)
                -1
            }
            healthStatus.id -> {
                referredToHealthFacility.required = (index == 1 || index == 2)
                triggerDependants(
                    source = healthStatus,
                    addItems = if (index == 1 || index == 2) listOf(referredToHealthFacility) else emptyList(),
                    removeItems = listOf(referredToHealthFacility)
                )
            }
            ifaTabletDistribution.id -> {
                triggerDependants(
                    source = ifaTabletDistribution,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = ifaTabletQuantity
                )
            }
            ifaTabletQuantity.id -> {
                validateEmptyOnEditText(ifaTabletQuantity)
                validateIntMinMax(ifaTabletQuantity)
            }

            sanitaryNapkinDistributed.id -> {
                triggerDependants(
                    source = sanitaryNapkinDistributed,
                    triggerIndex = 0,
                    passedIndex = index,
                    target = listOf(noOfPacketsDistributed,place,sDate)

                )
            }
            menstrualHygieneAwareness.id -> -1

            noOfPacketsDistributed.id -> {
                validateEmptyOnEditText(noOfPacketsDistributed)
                validateIntMinMax(noOfPacketsDistributed)
            }

            referredToHealthFacility.id -> {
                validateEmptyOnEditText(referredToHealthFacility)
            }



            counselingProvided.id ->-1
            place.id -> {
                validateEmptyOnEditText(place)
                -1
            }
            counselingType.id -> -1
            followUpDate.id -> {
                validateEmptyOnEditText(visitDate)
                -1
            }

            referralStatus.id -> -1
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as AdolescentHealthCache).let { ben ->
            ben.benId = ben.benId
            ben.visitDate = getLongFromDate(visitDate.value)
            ben.healthStatus = healthStatus.value
            ben.ifaTabletDistributed =  ifaTabletDistribution.value == resources.getStringArray(
                R.array.yes_no)[0]
            ben.quantityOfIfaTablets = ifaTabletQuantity.value?.toInt()
            ben.menstrualHygieneAwarenessGiven = menstrualHygieneAwareness.value == menstrualHygieneAwareness.entries!![0]
            ben.sanitaryNapkinDistributed = sanitaryNapkinDistributed.value == sanitaryNapkinDistributed.entries!![0]
            ben.noOfPacketsDistributed = noOfPacketsDistributed.value?.toInt()
            ben.place = place.value
            ben.distributionDate = getLongFromDate(sDate.value)
            ben.referredToHealthFacility = referredToHealthFacility.value
            ben.counselingProvided = counselingProvided.value == counselingProvided.entries!![0]
            ben.counselingType = counselingType.value
            ben.followUpDate = getLongFromDate(followUpDate.value)
            ben.referralStatus = referralStatus.value
        }
    }

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
    }
}
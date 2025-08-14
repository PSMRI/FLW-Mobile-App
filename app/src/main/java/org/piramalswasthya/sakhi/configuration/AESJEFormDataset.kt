package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.TBScreeningCache

class AESJEFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.visit_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = false

    )
    private val beneficiaryStatus = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.beneficiary_status),
        arrayId = R.array.benificary_case_status_kalaazar,
        entries = resources.getStringArray(R.array.benificary_case_status_kalaazar),
        required = true,
        hasDependants = false

    )
    private val dateOfDeath = FormElement(
        id = 3,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.death_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val placeOfDeath = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.place_of_Death),
        arrayId = R.array.death_place,
        entries = resources.getStringArray(R.array.death_place),
        required = true,
        hasDependants = true

    )

    private var otherPlaceOfDeath = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other_place),
        required = true,
        hasDependants = false
    )

    private val reasonOfDeath = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.reason_of_Death),
        arrayId = R.array.reason_death,
        entries = resources.getStringArray(R.array.reason_death),
        required = true,
        hasDependants = true

    )
    private var otherReasonOfDeath = FormElement(
        id = 7,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other_reason),
        required = true,
        hasDependants = false
    )


    private val caseStatus = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.aes_case_status_date),
        arrayId = R.array.dc_case_status,
        entries = resources.getStringArray(R.array.dc_case_status),
        required = false,
        hasDependants = true

    )

    private var followUpPoint = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.follow_up),
        arrayId = R.array.follow_up_array,
        entries = resources.getStringArray(R.array.follow_up_array),
        required = false,
        hasDependants = true
    )

    private var referredTo = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.refer_to),
        arrayId = R.array.dc_refer,
        entries = resources.getStringArray(R.array.dc_refer),
        required = false,
        hasDependants = true
    )
    private var other = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )







    suspend fun setUpPage(ben: BenRegCache?, saved: AESScreeningCache?) {
        val list = mutableListOf(
            dateOfCase,
            beneficiaryStatus,


        )
        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            beneficiaryStatus.value = resources.getStringArray(R.array.benificary_case_status)[0]
            caseStatus.value = resources.getStringArray(R.array.dc_case_status)[0]
        } else {

            dateOfCase.value = getDateFromLong(saved.visitDate)


            if (saved.aesJeCaseStatus != null) {
                caseStatus.value =
                    getLocalValueInArray(R.array.dc_case_status, saved.aesJeCaseStatus)
            }
            referredTo.value =
                getLocalValueInArray(referredTo.arrayId, saved.referToName)
            if (referredTo.value == referredTo.entries!!.last()) {
                referredTo.value = saved.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value =
                    getLocalValueInArray(referredTo.arrayId, saved.referToName)
            }
            beneficiaryStatus.value =
                getLocalValueInArray(beneficiaryStatus.arrayId, saved.beneficiaryStatus)
            other.value = saved.otherReferredFacility
            if (beneficiaryStatus.value == beneficiaryStatus.entries!![beneficiaryStatus.entries!!.size - 2]) {
                list.add(list.indexOf(beneficiaryStatus) + 1, dateOfDeath)
                list.add(list.indexOf(beneficiaryStatus) + 2, placeOfDeath)
                list.add(list.indexOf(beneficiaryStatus) + 3, reasonOfDeath)
                dateOfDeath.value =
                    getDateFromLong(saved.dateOfDeath)
                placeOfDeath.value =
                    getLocalValueInArray(placeOfDeath.arrayId, saved.placeOfDeath)
                if (placeOfDeath.value == placeOfDeath.entries!!.last()) {
                    list.add(list.indexOf(placeOfDeath) + 1, otherPlaceOfDeath)
                    otherPlaceOfDeath.value = saved.otherPlaceOfDeath
                }

                reasonOfDeath.value =
                    getLocalValueInArray(reasonOfDeath.arrayId, saved.reasonForDeath)
                if (reasonOfDeath.value == reasonOfDeath.entries!!.last()) {
                    list.add(list.indexOf(reasonOfDeath) + 1, otherReasonOfDeath)
                    otherReasonOfDeath.value = saved.otherReasonForDeath
                }
            } else {
                list.add(list.indexOf(beneficiaryStatus) + 1, caseStatus)
                list.add(list.indexOf(beneficiaryStatus) + 2, followUpPoint)
                list.add(list.indexOf(beneficiaryStatus) + 3, referredTo)




            }
            referredTo.value =
                getLocalValueInArray(referredTo.arrayId, saved.referToName)
            if (referredTo.value == referredTo.entries!!.last()) {
                referredTo.value = saved.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value =
                    getLocalValueInArray(referredTo.arrayId, saved.referToName)
            }

            other.value = saved.otherReferredFacility
        }


        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            beneficiaryStatus.id -> {
                if (beneficiaryStatus.value == beneficiaryStatus.entries!![beneficiaryStatus.entries!!.size - 2]
                ) {
                    triggerDependants(
                        source = beneficiaryStatus,
                        addItems = listOf(dateOfDeath, placeOfDeath, reasonOfDeath),
                        removeItems = listOf(
                            caseStatus,followUpPoint, referredTo,
                        )
                    )
                } else {
                    triggerDependants(
                        source = beneficiaryStatus,
                        addItems = listOf(
                            caseStatus, referredTo
                        ),
                        removeItems = listOf(dateOfDeath, placeOfDeath, reasonOfDeath,otherPlaceOfDeath,otherReasonOfDeath)
                    )
                }
                0
            }

            referredTo.id -> {
                if (referredTo.value == referredTo.entries!!.last()) {
                    triggerDependants(
                        source = referredTo,
                        addItems = listOf(other),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = referredTo,
                        addItems = listOf(),
                        removeItems = listOf(other)
                    )
                }
                0
            }

            placeOfDeath.id -> {
                if (placeOfDeath.value == placeOfDeath.entries!!.last()) {
                    triggerDependants(
                        source = placeOfDeath,
                        addItems = listOf(otherPlaceOfDeath),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = placeOfDeath,
                        addItems = listOf(),
                        removeItems = listOf(otherPlaceOfDeath)
                    )
                }
                0
            }

            other.id -> {
                validateEmptyOnEditText(other)
            }



            reasonOfDeath.id -> {
                if (reasonOfDeath.value == reasonOfDeath.entries!!.last()) {
                    triggerDependants(
                        source = reasonOfDeath,
                        addItems = listOf(otherReasonOfDeath),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = reasonOfDeath,
                        addItems = listOf(),
                        removeItems = listOf(otherReasonOfDeath)
                    )
                }
                0
            }

            else -> {
                -1
            }
        }
    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as AESScreeningCache).let { form ->
            form.visitDate = getLongFromDate(dateOfCase.value)
            form.referToName = referredTo.value
            form.referredTo = referredTo.getPosition()
            form.beneficiaryStatus = beneficiaryStatus.value
            form.beneficiaryStatusId = beneficiaryStatus.getPosition()
            form.reasonForDeath = reasonOfDeath.value
            form.aesJeCaseStatus = getEnglishValueInArray(R.array.dc_case_status, caseStatus.value)
            form.otherPlaceOfDeath = otherPlaceOfDeath.value
            form.otherReasonForDeath = otherReasonOfDeath.value
            form.dateOfDeath = getLongFromDate(dateOfDeath.value)
            form.placeOfDeath = placeOfDeath.value
            form.otherReferredFacility = other.value
            form.diseaseTypeID = 3
            form.createdDate = getLongFromDate(dateOfCase.value)
            form.followUpPoint = followUpPoint.value?.toIntOrNull() ?: 0

        }
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
}
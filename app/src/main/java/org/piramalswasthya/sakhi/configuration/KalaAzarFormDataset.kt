package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.KalaAzarScreeningCache

class KalaAzarFormDataset(
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
        hasDependants = true

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
        title = resources.getString(R.string.case_status),
        arrayId = R.array.dc_case_status,
        entries = resources.getStringArray(R.array.dc_case_status),
        required = false,
        hasDependants = true

    )
    private var rapidDiagnostic = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.rapid_diagnostic_kala),
        entries = resources.getStringArray(R.array.positive_negative),
        required = false,
        hasDependants = true
    )

    private val dateOfTest = FormElement(
        id = 10,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.test_up_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        hasDependants = true

    )
    private var followUpPoint = FormElement(
        id = 11,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.follow_up),
        arrayId = R.array.follow_up_array,
        entries = resources.getStringArray(R.array.follow_up_array),
        required = false,
        hasDependants = true
    )

    private var referredTo = FormElement(
        id = 12,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.refer_to),
        arrayId = R.array.dc_refer,
        entries = resources.getStringArray(R.array.dc_refer),
        required = false,
        hasDependants = true
    )
    private var other = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )






    suspend fun setUpPage(ben: BenRegCache?, saved: KalaAzarScreeningCache?) {
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

            rapidDiagnostic.value =
                if (saved.rapidDiagnosticTest == "Positive") resources.getStringArray(R.array.positive_negative)[0]
                else if (saved.rapidDiagnosticTest == "Negative") resources.getStringArray(R.array.positive_negative)[1]
                else resources.getStringArray(R.array.positive_negative)[2]

            if (saved.kalaAzarCaseStatus == "Suspected") {
                caseStatus.entries = resources.getStringArray(R.array.dc_case_status)
                    .filter { it == "Confirmed" || it == "Not Confirmed" }
                    .toTypedArray()
            } else if (saved.kalaAzarCaseStatus == "Confirmed") {
                caseStatus.entries = resources.getStringArray(R.array.dc_case_status)
                    .filter { it == "Treatment Started"}
                    .toTypedArray()
            } else {
                caseStatus.entries = resources.getStringArray(R.array.dc_case_status)
            }
            if (saved.kalaAzarCaseStatus != null) {
                caseStatus.value =
                    getLocalValueInArray(R.array.dc_case_status, saved.kalaAzarCaseStatus)
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
                list.add(list.indexOf(beneficiaryStatus) + 2, rapidDiagnostic)
                list.add(list.indexOf(beneficiaryStatus) + 3, followUpPoint)
                list.add(list.indexOf(beneficiaryStatus) + 4, referredTo)
                if (rapidDiagnostic.value != "Not Performed") {
                    list.add(list.indexOf(rapidDiagnostic) + 1, dateOfTest)
                    dateOfTest.value = getDateFromLong(saved.dateOfRdt)
                }



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
                            caseStatus, rapidDiagnostic,followUpPoint, referredTo,
                        )
                    )
                } else {
                    triggerDependants(
                        source = beneficiaryStatus,
                        addItems = listOf(
                            caseStatus, rapidDiagnostic, referredTo
                        ),
                        removeItems = listOf(dateOfDeath, placeOfDeath, reasonOfDeath,otherPlaceOfDeath,otherReasonOfDeath)
                    )
                }
                0
            }

            other.id -> {
                validateEmptyOnEditText(other)
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

            otherPlaceOfDeath.id -> {
                validateEmptyOnEditText(otherPlaceOfDeath)
            }

            rapidDiagnostic.id -> {
                if (rapidDiagnostic.value == resources.getStringArray(R.array.positive_negative)[2]) {
                    triggerDependants(
                        source = rapidDiagnostic,
                        addItems = listOf(),
                        removeItems = listOf(dateOfTest)
                    )
                    caseStatus.value =
                        getLocalValueInArray(
                            caseStatus.arrayId,
                            resources.getStringArray(R.array.dc_case_status)[0]
                        )
                } else if (rapidDiagnostic.value == resources.getStringArray(R.array.positive_negative)[1]) {
                    triggerDependants(
                        source = rapidDiagnostic,
                        addItems = listOf(dateOfTest),
                        removeItems = listOf()
                    )
                    caseStatus.value =
                        getLocalValueInArray(
                            caseStatus.arrayId,
                            resources.getStringArray(R.array.dc_case_status)[1]
                        )
                } else {
                    triggerDependants(
                        source = rapidDiagnostic,
                        addItems = listOf(dateOfTest),
                        removeItems = listOf()
                    )
                    caseStatus.value =
                        getLocalValueInArray(
                            caseStatus.arrayId,
                            resources.getStringArray(R.array.dc_case_status)[0]
                        )
                }
                0
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
            otherReasonOfDeath.id -> {
                validateEmptyOnEditText(otherReasonOfDeath)
            }

            else -> {
                0
            }
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as KalaAzarScreeningCache).let { form ->
            form.visitDate = getLongFromDate(dateOfCase.value)
            form.referToName = referredTo.value
            form.referredTo = referredTo.getPosition()
            form.beneficiaryStatus = beneficiaryStatus.value
            form.beneficiaryStatusId = beneficiaryStatus.getPosition()
            form.reasonForDeath = reasonOfDeath.value
            form.kalaAzarCaseStatus = getEnglishValueInArray(R.array.dc_case_status, caseStatus.value)
            form.otherPlaceOfDeath = otherPlaceOfDeath.value
            form.otherReasonForDeath = otherReasonOfDeath.value
            form.dateOfDeath = getLongFromDate(dateOfDeath.value)
            form.dateOfRdt = getLongFromDate(dateOfTest.value)
            form.placeOfDeath = placeOfDeath.value
            form.otherReferredFacility = other.value
            form.rapidDiagnosticTest = rapidDiagnostic.value
            form.diseaseTypeID = 2
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
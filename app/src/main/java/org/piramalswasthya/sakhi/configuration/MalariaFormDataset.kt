package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import timber.log.Timber
import java.util.Calendar

class MalariaFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {



    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.case_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )
    private val beneficiaryStatus = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.beneficiary_status),
        arrayId = R.array.benificary_case_status,
        entries = resources.getStringArray(R.array.benificary_case_status),
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
        id = 28,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other_reason),
        required = true,
        hasDependants = false
    )

    private var headline = FormElement(
        id = 27,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.sympt),
        required = false,
        headingLine = false,
        hasDependants = false
    )

    private var isFever = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.cbac_feverwks),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }

    private var isFluLikeIllness = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.flu_like_illness),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }

    private var isShakingchills = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.shakingchills),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }

    private var isHeadache = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.headache),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }

    private var isMuscleaches = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.muscleaches),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }

    private var isTiredness = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.tiredness),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }
    private var isNausea = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.nausea),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }
    private var isVomiting = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.vomiting),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }

    private var isDiarrhea = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.diarrhea),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    ).apply {
        value = resources.getStringArray(R.array.yes_no)[1]
    }

    private val caseStatus = FormElement(
        id = 16,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.case_status),
        arrayId = -1,
        entries = resources.getStringArray(R.array.dc_case_status),
        required = false,
        isEnabled = false,
        hasDependants = true

    )

    private val testType = FormElement(
        id = 28,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.m_test_type),
        arrayId = -1,
        entries = resources.getStringArray(R.array.test_type),
        required = false,
        isEnabled = true,
        hasDependants = true

    )
    private var rapidDiagnostic = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.rapid_diagnostic),
        entries = resources.getStringArray(R.array.positive_negative),
        required = false,
        hasDependants = true
    )

    private val dateOfTest = FormElement(
        id = 18,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.test_up_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private var slideTestOptions = FormElement(
        id = 27,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.slide_test),
        entries = resources.getStringArray(R.array.pf_pv),
        required = false,
        hasDependants = true
    )

    private var slideTestPf = FormElement(
        id = 19,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.slide_test_pf),
        entries = resources.getStringArray(R.array.positive_negative),
        required = false,
        hasDependants = true
    )

    private var slideTestPv = FormElement(
        id = 20,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.slide_test_pv),
        entries = resources.getStringArray(R.array.positive_negative),
        required = false,
        hasDependants = true
    )

    private val dateOfSlidetest = FormElement(
        id = 21,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.test_up_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private var referredTo = FormElement(
        id = 22,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.refer_to),
        arrayId = R.array.dc_refer_malaria,
        entries = resources.getStringArray(R.array.dc_refer_malaria),
        required = false,
        hasDependants = true
    )
    private var other = FormElement(
        id = 23,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )
    private val followUpdate = FormElement(
        id = 24,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.follow_up_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        hasDependants = true

    )


    private val remarks = FormElement(
        id = 25,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_remarks),
        required = false,

    )
    private val dateOfVisitBySupervisor = FormElement(
        id = 26,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.supervisor_visit_date),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        hasDependants = true

    )

    var visitId = 0L

    suspend fun setUpPage(ben: BenRegCache?, saved: MalariaScreeningCache?) {
        val list = mutableListOf(
            dateOfCase,
            beneficiaryStatus,
            headline,
            isFever,
            isFluLikeIllness,
            isShakingchills,
            isHeadache,
            isMuscleaches,
            isTiredness,
            isNausea,
            isVomiting,
            isDiarrhea,
            caseStatus
        )
        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            beneficiaryStatus.value = resources.getStringArray(R.array.benificary_case_status)[0]
            visitId = 0
//            caseStatus.value = resources.getStringArray(R.array.dc_case_status)[0]
        } else {
            dateOfCase.value = getDateFromLong(saved.caseDate)
            followUpdate.value = getDateFromLong(saved.followUpDate)
            visitId = saved.visitId
            val entries = resources.getStringArray(R.array.test_type)
            val index = saved.malariaTestType ?: 0
            if (index != 0) {
                testType.value = entries[index-1]
            }
            remarks.value = saved.remarks
            if (saved.caseStatus == "Suspected") {
                caseStatus.entries = resources.getStringArray(R.array.dc_case_status)
                    .filter { it == "Confirmed" || it == "Not Confirmed" }
                    .toTypedArray()
            } else {
                caseStatus.entries = resources.getStringArray(R.array.dc_case_status)
            }
            saved.caseStatus
            isFever.value =
                if (saved.feverMoreThanTwoWeeks == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isFluLikeIllness.value =
                if (saved.fluLikeIllness == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isShakingchills.value =
                if (saved.shakingChills == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isHeadache.value =
                if (saved.headache == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isMuscleaches.value =
                if (saved.muscleAches == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isTiredness.value =
                if (saved.tiredness == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isNausea.value =
                if (saved.nausea == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isVomiting.value =
                if (saved.vomiting == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            isDiarrhea.value =
                if (saved.diarrhea == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                    R.array.yes_no
                )[1]
            if (saved.caseStatus != null) {
                caseStatus.value =
                    getLocalValueInArray(R.array.dc_case_status, saved.caseStatus)
            }

            rapidDiagnostic.value =
                if (saved.rapidDiagnosticTest == "Positive") resources.getStringArray(R.array.positive_negative)[0]
                else if (saved.rapidDiagnosticTest == "Negative") resources.getStringArray(R.array.positive_negative)[1]
                else resources.getStringArray(R.array.positive_negative)[2]

            slideTestPv.value =
                if (saved.slideTestPv == "Positive") resources.getStringArray(R.array.positive_negative)[0]
                else if (saved.slideTestPv == "Negative") resources.getStringArray(R.array.positive_negative)[1]
                else resources.getStringArray(R.array.positive_negative)[2]

            slideTestPf.value =
                if (saved.slideTestPf == "Positive") resources.getStringArray(R.array.positive_negative)[0]
                else if (saved.slideTestPf == "Negative") resources.getStringArray(R.array.positive_negative)[1]
                else resources.getStringArray(R.array.positive_negative)[2]


            if (saved.malariaSlideTestType != 0) {
                slideTestOptions.value = resources.getStringArray(R.array.pf_pv)[saved.malariaSlideTestType!!-1]

            }
            beneficiaryStatus.value =
                getLocalValueInArray(beneficiaryStatus.arrayId, saved.beneficiaryStatus)


            if (beneficiaryStatus.value == beneficiaryStatus.entries!!.last()) {
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
              /*  list.add(list.indexOf(beneficiaryStatus) + 1, headline)
                list.add(list.indexOf(beneficiaryStatus) + 2, isFever)
                list.add(list.indexOf(beneficiaryStatus) + 3, isFluLikeIllness)
                list.add(list.indexOf(beneficiaryStatus) + 4, isShakingchills)
                list.add(list.indexOf(beneficiaryStatus) + 5, isHeadache)
                list.add(list.indexOf(beneficiaryStatus) + 6, isMuscleaches)
                list.add(list.indexOf(beneficiaryStatus) + 7, isTiredness)
                list.add(list.indexOf(beneficiaryStatus) + 8, isNausea)
                list.add(list.indexOf(beneficiaryStatus) + 9, isVomiting)
                list.add(list.indexOf(beneficiaryStatus) + 10, isDiarrhea)
                list.add(list.indexOf(beneficiaryStatus) + 11, caseStatus)*/
               /* list.add(list.indexOf(beneficiaryStatus) + 12, rapidDiagnostic)
                list.add(list.indexOf(beneficiaryStatus) + 13, referredTo)
                list.add(list.indexOf(beneficiaryStatus) + 14, remarks)
                list.add(list.indexOf(beneficiaryStatus) + 15, dateOfVisitBySupervisor)*/


                if (caseStatus.value == resources.getStringArray(R.array.dc_case_status)[0] || caseStatus.value == resources.getStringArray(R.array.dc_case_status)[1] || caseStatus.value == resources.getStringArray(R.array.dc_case_status)[2]) {
                    list.add(list.indexOf(caseStatus) + 1, testType)
                    if (testType.value ==  resources.getStringArray(R.array.test_type)[0]) {
                        list.add(list.indexOf(testType) + 1, rapidDiagnostic)
                        if (rapidDiagnostic.value != "Not Performed") {
                            list.add(list.indexOf(rapidDiagnostic) + 1, dateOfTest)
                            dateOfTest.value = getDateFromLong(saved.dateOfRdt)

                        }


                    } else if (testType.value == resources.getStringArray(R.array.test_type)[1]) {
                        list.add(list.indexOf(testType) + 1, slideTestOptions)
                        if (slideTestOptions.value ==  resources.getStringArray(R.array.pf_pv)[0]) {
                            list.add(list.indexOf(slideTestOptions) + 1, slideTestPf)
                        } else {
                            list.add(list.indexOf(slideTestOptions) + 1, slideTestPv)
                        }
                        if (slideTestPv.value != "Not Performed") {
                            list.add(list.indexOf(slideTestPv) + 1, dateOfSlidetest)
                            dateOfSlidetest.value = getDateFromLong(saved.dateOfSlideTest)
                        }

                        if (slideTestPf.value != "Not Performed") {
                            list.add(list.indexOf(slideTestPf) + 1, dateOfSlidetest)
                            dateOfSlidetest.value = getDateFromLong(saved.dateOfSlideTest)
                        }

                    } else {
                        list.add(list.indexOf(testType) + 1, rapidDiagnostic)
                        if (rapidDiagnostic.value != "Not Performed") {
                            list.add(list.indexOf(rapidDiagnostic) + 1, dateOfTest)
                            dateOfTest.value = getDateFromLong(saved.dateOfRdt)
                            list.add(list.indexOf(rapidDiagnostic) + 2, slideTestOptions)

                        }

                        if (slideTestOptions.value ==  resources.getStringArray(R.array.pf_pv)[0]) {
                            list.add(list.indexOf(slideTestOptions) + 1, slideTestPf)
                        } else {
                            list.add(list.indexOf(slideTestOptions) + 1, slideTestPv)
                        }
                        if (slideTestPv.value != "Not Performed") {
                            list.add(list.indexOf(slideTestPv) + 1, dateOfSlidetest)
                            dateOfSlidetest.value = getDateFromLong(saved.dateOfSlideTest)
                        }

                        if (slideTestPf.value != "Not Performed") {
                            list.add(list.indexOf(slideTestPf) + 1, dateOfSlidetest)
                            dateOfSlidetest.value = getDateFromLong(saved.dateOfSlideTest)
                        }
                    }

                } else if (caseStatus.value == resources.getStringArray(R.array.dc_case_status)[1]) {
                    list.add(list.indexOf(caseStatus) + 1, testType)
                }







            }
            referredTo.value =
                getLocalValueInArray(referredTo.arrayId, saved.referToName)
            if (referredTo.value == referredTo.entries!![referredTo.entries!!.size - 2]) {
                referredTo.value = saved.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value =
                    getLocalValueInArray(referredTo.arrayId, saved.referToName)
            }

            other.value = saved.otherReferredFacility
            followUpdate.value = getDateFromLong(saved.followUpDate)

        }

        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {

        return when (formId) {

            isFever.id,
            isFluLikeIllness.id,
            isShakingchills.id,
            isHeadache.id,
            isMuscleaches.id,
            isTiredness.id,
            isNausea.id,
            isVomiting.id,
            isDiarrhea.id -> {

                val yesValue = resources.getStringArray(R.array.yes_no)[0]
                val suspectedValue = resources.getStringArray(R.array.dc_case_status)[0]
                val notConfirmedValue = resources.getStringArray(R.array.dc_case_status)[2]

                val symptoms = listOf(
                    isFluLikeIllness.value,
                    isShakingchills.value,
                    isHeadache.value,
                    isMuscleaches.value,
                    isTiredness.value,
                    isNausea.value,
                    isVomiting.value,
                    isDiarrhea.value
                )

                val nonFeverYesCount = symptoms.count { it == yesValue }

                caseStatus.value = when {
                    isFever.value == yesValue -> suspectedValue
                    nonFeverYesCount >= 2 -> suspectedValue
                    else -> notConfirmedValue
                }

                if(caseStatus.value == suspectedValue){
                    triggerDependants(
                        source = caseStatus,
                        addItems = listOf(testType),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = caseStatus,
                        addItems = listOf(),
                        removeItems = listOf(testType)
                    )
                }
            }

            caseStatus.id -> {
                if(caseStatus.value == resources.getStringArray(R.array.dc_case_status)[0]){
                    triggerDependants(
                        source = caseStatus,
                        addItems = listOf(testType),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = caseStatus,
                        addItems = listOf(),
                        removeItems = listOf(testType)
                    )
                }
                0
            }


            testType.id -> {
                if(testType.value == resources.getStringArray(R.array.test_type)[0]){
                    triggerDependants(
                        source = testType,
                        addItems = listOf(rapidDiagnostic),
                        removeItems = listOf(slideTestOptions,slideTestPf,slideTestPv,dateOfTest,dateOfSlidetest)
                    )
                } else if(testType.value == resources.getStringArray(R.array.test_type)[1]){
                    triggerDependants(
                        source = testType,
                        addItems = listOf(slideTestOptions),
                        removeItems = listOf(rapidDiagnostic,slideTestPf,slideTestPv,dateOfTest,dateOfSlidetest)
                    )
                } else {
                    triggerDependants(
                        source = testType,
                        addItems = listOf(rapidDiagnostic,slideTestOptions),
                        removeItems = listOf(slideTestPf,slideTestPv,dateOfTest,dateOfSlidetest)
                    )
                }
                0
            }

            beneficiaryStatus.id -> {
                if (beneficiaryStatus.value == beneficiaryStatus.entries!!.last()) {
                    triggerDependants(
                        source = beneficiaryStatus,
                        addItems = listOf(dateOfDeath,placeOfDeath,reasonOfDeath),
                        removeItems = listOf(headline,isFever,isFluLikeIllness,isShakingchills,isHeadache,
                            isMuscleaches,isTiredness,isNausea,isVomiting,isDiarrhea,
                            caseStatus,rapidDiagnostic,slideTestPf,slideTestPv,referredTo,
                            remarks,dateOfVisitBySupervisor)
                    )
                } else {
                    triggerDependants(
                        source = beneficiaryStatus,
                        addItems = listOf(headline,isFever,isFluLikeIllness,isShakingchills,isHeadache,
                            isMuscleaches,isTiredness,isNausea,isVomiting,isDiarrhea,
                            caseStatus),
                        removeItems = listOf(dateOfDeath,placeOfDeath,reasonOfDeath)
                    )
                }
                0
            }

            referredTo.id -> {
                if (referredTo.value == referredTo.entries!![referredTo.entries!!.size - 2]) {
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

            other.id -> {
                validateEmptyOnEditText(other)
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
                        removeItems = listOf(dateOfTest,slideTestOptions,slideTestPf,slideTestPv,dateOfSlidetest)
                    )
                } else if (rapidDiagnostic.value == resources.getStringArray(R.array.positive_negative)[1]) {
                    if (testType.value == "Both") {
                        triggerDependants(
                            source = rapidDiagnostic,
                            addItems = listOf(dateOfTest,slideTestOptions),
                            removeItems = listOf()
                        )
                    } else {
                        triggerDependants(
                            source = rapidDiagnostic,
                            addItems = listOf(dateOfTest),
                            removeItems = listOf(slideTestOptions,slideTestPf,slideTestPv)
                        )
                    }

                    if (visitId != 0L){
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[2]
                    }

//                    caseStatus.value = getLocalValueInArray(caseStatus.arrayId, resources.getStringArray(R.array.dc_case_status)[1])
                } else {
                    if (testType.value == "Both") {
                        triggerDependants(
                            source = rapidDiagnostic,
                            addItems = listOf(dateOfTest,slideTestOptions),
                            removeItems = listOf()
                        )
                    } else {
                        triggerDependants(
                            source = rapidDiagnostic,
                            addItems = listOf(dateOfTest),
                            removeItems = listOf(slideTestOptions,slideTestPf,slideTestPv)
                        )
                    }
                  /*  triggerDependants(
                        source = rapidDiagnostic,
                        addItems = listOf(dateOfTest),
                        removeItems = listOf()
                    )*/
                    if (visitId != 0L){
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[1]

                    }
                }
                0
            }

            slideTestOptions.id -> {
                slideTestOptions.isEnabled = true
                if (slideTestOptions.value == resources.getStringArray(R.array.pf_pv)[0]) {
                    triggerDependants(
                        source = slideTestOptions,
                        addItems = listOf(slideTestPf),
                        removeItems = listOf(slideTestPv)
                    )
                } else if (slideTestOptions.value == resources.getStringArray(R.array.pf_pv)[1]) {

                    triggerDependants(
                        source = slideTestOptions,
                        addItems = listOf(slideTestPv),
                        removeItems = listOf(slideTestPf)
                    )
                }
                0
            }

            slideTestPv.id -> {
                slideTestPv.isEnabled = true
                if (slideTestPv.value == resources.getStringArray(R.array.positive_negative)[0]) {
                    triggerDependants(
                        source = slideTestPv,
                        addItems = listOf(dateOfSlidetest),
                        removeItems = listOf()
                    )
                    if (visitId != 1L) {
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[1]

                    }

                } else if (slideTestPv.value == resources.getStringArray(R.array.positive_negative)[1]) {

                    triggerDependants(
                        source = slideTestPv,
                        addItems = listOf(dateOfSlidetest),
                        removeItems = listOf()
                    )
                    if (visitId != 0L) {
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[2]

                    }

                } else {
                    triggerDependants(
                        source = slideTestPv,
                        addItems = listOf(),
                        removeItems = listOf(dateOfSlidetest)
                    )
                    if (visitId != 0L) {
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[2]

                    }

                }
                0
            }

            slideTestPf.id -> {
                slideTestPf.isEnabled = true
                if (slideTestPf.value == resources.getStringArray(R.array.positive_negative)[0]) {
                    triggerDependants(
                        source = slideTestPf,
                        addItems = listOf(dateOfSlidetest),
                        removeItems = listOf()
                    )
                    if (visitId != 0L) {
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[1]

                    }

                } else if (slideTestPf.value == resources.getStringArray(R.array.positive_negative)[1]) {

                    triggerDependants(
                        source = slideTestPf,
                        addItems = listOf(dateOfSlidetest),
                        removeItems = listOf()
                    )
                    if (visitId != 0L) {
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[2]

                    }

                } else {
                    triggerDependants(
                        source = slideTestPf,
                        addItems = listOf(),
                        removeItems = listOf(dateOfSlidetest)
                    )
                    if (visitId != 0L) {
                        caseStatus.value = resources.getStringArray(R.array.dc_case_status)[2]

                    }

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
        (cacheModel as MalariaScreeningCache).let { form ->
            form.caseDate = getLongFromDate(dateOfCase.value)
            form.feverMoreThanTwoWeeks =
                isFever.value == resources.getStringArray(R.array.yes_no)[0]
            form.fluLikeIllness = isFluLikeIllness.value == resources.getStringArray(R.array.yes_no)[0]
            form.shakingChills = isShakingchills.value == resources.getStringArray(R.array.yes_no)[0]
            form.headache = isHeadache.value == resources.getStringArray(R.array.yes_no)[0]
            form.muscleAches = isMuscleaches.value == resources.getStringArray(R.array.yes_no)[0]
            form.tiredness = isTiredness.value == resources.getStringArray(R.array.yes_no)[0]
            form.nausea =
                isNausea.value == resources.getStringArray(R.array.yes_no)[0]
            form.vomiting =
                isVomiting.value == resources.getStringArray(R.array.yes_no)[0]
            form.diarrhea =
                isDiarrhea.value == resources.getStringArray(R.array.yes_no)[0]
            form.caseStatus = getEnglishValueInArray(R.array.dc_case_status, caseStatus.value)
            form.referToName = referredTo.value
            form.referredTo = referredTo.getPosition()
            form.beneficiaryStatus = beneficiaryStatus.value
            form.beneficiaryStatusId = beneficiaryStatus.getPosition()
            form.reasonForDeath = reasonOfDeath.value
            form.otherPlaceOfDeath = otherPlaceOfDeath.value
            form.otherReasonForDeath = otherReasonOfDeath.value
            form.dateOfDeath = getLongFromDate(dateOfDeath.value)
            form.dateOfRdt = getLongFromDate(dateOfTest.value)
            form.dateOfSlideTest = getLongFromDate(dateOfSlidetest.value)
            form.placeOfDeath = placeOfDeath.value
            form.otherReferredFacility = other.value
            form.rapidDiagnosticTest = rapidDiagnostic.value
            form.slideTestPv = slideTestPv.value
            form.slideTestPf = slideTestPf.value
            form.slideTestName = slideTestOptions.value
            form.remarks = remarks.value
            form.dateOfVisitBySupervisor = getLongFromDate(dateOfVisitBySupervisor.value)
            form.diseaseTypeID = 1
            form.followUpDate = getLongFromDate(followUpdate.value)
            form.visitId = 1
            form.malariaTestType = testType.getPosition()
            form.malariaSlideTestType = slideTestOptions.getPosition()

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
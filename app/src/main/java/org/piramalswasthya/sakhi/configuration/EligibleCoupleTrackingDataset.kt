package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.PregnantWomanRegistrationDataset.Companion.getMinLmpMillis
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EligibleCoupleTrackingDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    var antraDoseValue="N/A"
    var noOfChildrens=-1

    private var dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private var lmpDate = FormElement(
        id = 11,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.lmp_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = getMinLmpMillis(),
        hasDependants = true
    )

    private val financialYear = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.ectdset_fin_yrs),
        required = false,
    )

    private val month = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.ectdset_month),
        arrayId = R.array.visit_months,
        entries = resources.getStringArray(R.array.visit_months),
        required = false
    )

    private val isPregnancyTestDone = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ectdset_is_preg_tst_done),
        entries = resources.getStringArray(R.array.ectdset_yes_no_dont),
        required = false,
        hasDependants = true
    )

    private val pregnancyTestResult = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ectdset_preg_tst_rlt),
        entries = resources.getStringArray(R.array.ectdset_po_neg),
        required = true,
        hasDependants = true
    )

    private val isPregnant = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ectdset_name_wo_preg),
        entries = resources.getStringArray(R.array.ectdset_yes_no_dont),
        required = false,
        hasDependants = true
    )

    private val usingFamilyPlanning = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ectdset_fly_plan_mthd),
        entries = resources.getStringArray(R.array.ectdset_yes_no),
        required = false,
        hasDependants = true
    )
    // Q2 – Only for Mitanin
    private val usingFamilyPlanningMitanin = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ectdset_fly_plan_mthd_mitanin),
        entries = resources.getStringArray(R.array.ectdset_yes_no),
        required = false,
        hasDependants = true
    )

    private val wantToUseFamilyPlanning = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ectdset_want_to_use_fly_plan_mthd_mitanin),
        entries = resources.getStringArray(R.array.ectdset_yes_no),
        required = false,
        hasDependants = true
    )

    private val methodOfContraception = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.ectdset_mthd_conpt),
        arrayId = R.array.method_of_contraception,
        entries = resources.getStringArray(R.array.method_of_contraception),
        required = false,
        hasDependants = true

    )

    private val antraDoses = FormElement(
        id = 11,
        inputType = InputType.DROPDOWN,
        title = "Antra injection",
        arrayId = R.array.antra_doses,
        entries = resources.getStringArray(R.array.antra_doses),
        required = true,
        hasDependants = true

    )

    private val anyOtherMethod = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.ectdset_other_mthd),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT,
        etMaxLength = 50
    )

    private var dateOfAntraInjection = FormElement(
        id = 13,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.date_of_antra_injection),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )
    private var dueDateOfAntraInjection = FormElement(
        id = 14,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.due_date_of_next_injection),
        required = false,

        )

    private val mpaFileUpload1 = FormElement(
        id = 23,
        inputType = InputType.FILE_UPLOAD,
        title = context.getString(R.string.mpa_card),
        required = false,
    )


    private val deliveryDischargeSummary1  = FormElement(
        id = 60,
        inputType = InputType.FILE_UPLOAD,
        title = resources.getString(R.string.ectdset_discharge_summary_1),
        required = false

    )
    private val deliveryDischargeSummary2 = FormElement(
        id =61,
        inputType = InputType.FILE_UPLOAD,
        title = resources.getString(R.string.ectdset_discharge_summary_2),
        required = false
    )
    var lastDose: String? = null
    var lastDateofDose: String? = null
    fun getIndexOfIsPregnant() = getIndexById(isPregnant.id)

    suspend fun setUpPage(
        ben: BenRegCache?,
        dateOfReg: Long,
        lastTrack: EligibleCoupleTrackingCache?,
        saved: EligibleCoupleTrackingCache?,
        noOfChildren: Int?
    ) {
        noOfChildrens = noOfChildren!!
        lastDose = lastTrack?.antraDose
        lastDateofDose = lastTrack?.dateOfAntraInjection

        methodOfContraception.entries =
            if (noOfChildren == 0)
                resources.getStringArray(R.array.method_of_contraception_for_zero_child)
            else
                resources.getStringArray(R.array.method_of_contraception)

        val list = mutableListOf(
            dateOfVisit,
            lmpDate,
            financialYear,
            month,
            isPregnancyTestDone,
        )

        // Add FP question (Q1)
        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            list.add(usingFamilyPlanning)
            list.remove(usingFamilyPlanningMitanin)
        } else {
            list.add(usingFamilyPlanningMitanin)
            list.remove(usingFamilyPlanning)
        }

        if (saved == null) {

            // -------- NEW ENTRY --------

            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
            dateOfVisit.value?.let {
                financialYear.value = getFinancialYear(it)
                month.value =
                    resources.getStringArray(R.array.visit_months)[Companion.getMonth(it)!!]
            }

            if (ben != null) {
                dateOfAntraInjection.min = ben.regDate
            }

            dateOfVisit.min = lastTrack?.let {
                Calendar.getInstance().apply {
                    timeInMillis = it.visitDate
                    val currentMonth = get(Calendar.MONTH)
                    if (currentMonth == 11) {
                        set(Calendar.YEAR, get(Calendar.YEAR) + 1)
                        set(Calendar.MONTH, 0)
                    } else {
                        set(Calendar.MONTH, currentMonth + 1)
                    }
                    set(Calendar.DAY_OF_MONTH, 1)
                    setToStartOfTheDay()
                }.timeInMillis
            } ?: dateOfReg

        } else {

            // -------- EDIT ENTRY --------

            dateOfVisit.value = getDateFromLong(saved.visitDate)
            lmpDate.value =
                if (saved.lmpDate != null)
                    getDateFromLong(saved.lmpDate)
                else
                    getDateFromLong(System.currentTimeMillis())

            financialYear.value = getFinancialYear(dateString = dateOfVisit.value)
            month.value =
                resources.getStringArray(R.array.visit_months)[Companion.getMonth(dateOfVisit.value)!!]

            isPregnancyTestDone.value =
                getLocalValueInArray(R.array.yes_no, saved.isPregnancyTestDone)

            if (isPregnancyTestDone.value == resources.getStringArray(R.array.yes_no)[0]) {

                list.add(list.indexOf(isPregnancyTestDone) + 1, pregnancyTestResult)
                pregnancyTestResult.value = saved.pregnancyTestResult

            } else {

                // Restore FP answers
                if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {

                    usingFamilyPlanning.value =
                        if (saved.usingFamilyPlanning == true)
                            resources.getStringArray(R.array.yes_no)[0]
                        else
                            resources.getStringArray(R.array.yes_no)[1]

                    if (saved.usingFamilyPlanning == true) {
                        list.add(methodOfContraception)
                    }

                } else {

                    usingFamilyPlanningMitanin.value =
                        if (saved.usingFamilyPlanning == true)
                            resources.getStringArray(R.array.ectdset_yes_no)[0]
                        else
                            resources.getStringArray(R.array.ectdset_yes_no)[1]

                    if (saved.usingFamilyPlanning == true) {
                        list.add(methodOfContraception)
                    } else {
                        list.add(wantToUseFamilyPlanning)

                        if (saved.methodOfContraception != null) {
                            wantToUseFamilyPlanning.value =
                                resources.getStringArray(R.array.ectdset_yes_no)[0]
                            list.add(methodOfContraception)
                        }
                    }
                }

                // Restore FP method details
                saved.methodOfContraception?.let { method ->
                    val methods = resources.getStringArray(R.array.method_of_contraception)
                    val sterilizationIndices = listOf(7, 8, 9)

                    when {
                        method in methods -> {
                            methodOfContraception.value = method
                            val selectedIndex = methods.indexOf(method)

                            if (selectedIndex in sterilizationIndices &&
                                !BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)
                            ) {
                                list.add(deliveryDischargeSummary1)
                                list.add(deliveryDischargeSummary2)
                                deliveryDischargeSummary1.value = saved.dischargeSummary1
                                deliveryDischargeSummary2.value = saved.dischargeSummary2
                            }
                        }

                        method.split("/")[0] == methods[1] -> {
                            methodOfContraception.value = methods[1]
                            list.add(antraDoses)
                            list.add(dateOfAntraInjection)
                            list.add(dueDateOfAntraInjection)

                            if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                                list.add(mpaFileUpload1)
                                mpaFileUpload1.value = saved.mpaFile
                            }

                            dateOfAntraInjection.value = saved.dateOfAntraInjection
                            dueDateOfAntraInjection.value = saved.dueDateOfAntraInjection

                            if (saved.antraDose != null) {
                                antraDoseValue = saved.antraDose!!
                                antraDoses.value = saved.antraDose
                            }
                        }

                        else -> {
                            methodOfContraception.value = methods.last()
                            list.add(anyOtherMethod)
                            anyOtherMethod.value = method
                        }
                    }
                }
            }

            isPregnant.value = getLocalValueInArray(R.array.yes_no, saved.isPregnant)
        }

        val nextDose = getNextDose(lastDose, lastDateofDose, dateOfVisit.value!!)
        antraDoses.value = nextDose
        antraDoseValue = nextDose
        antraDoses.isEnabled = false

        setUpPage(list)
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dateOfVisit.id -> {
                financialYear.value = Companion.getFinancialYear(dateOfVisit.value)
                month.value = resources.getStringArray(R.array.visit_months)[Companion.getMonth(dateOfVisit.value)!!]
                val nextDose = getNextDose(lastDose, lastDateofDose, dateOfVisit.value!!)
                antraDoses.value = nextDose
                antraDoseValue=nextDose
                antraDoses.isEnabled = false
                -1
            }
            dateOfAntraInjection.id -> {
                val injectionDate = dateOfAntraInjection.value ?: ""

                val (minDate, maxDate) = calculateNextInjectionDate(injectionDate, 76, 120)

                dueDateOfAntraInjection.value =
                    if (minDate.isNotEmpty() && maxDate.isNotEmpty()) {
                        "$minDate to $maxDate"
                    } else {
                        resources.getString(R.string.invalid_injection_date)
                    }
                -1
            }

            isPregnancyTestDone.id -> {
                isPregnant.isEnabled = true
                if (isPregnancyTestDone.value == resources.getStringArray(R.array.yes_no_donno)[0]) {
                    triggerDependants(
                        source = isPregnancyTestDone,
                        removeItems = listOf(isPregnant,usingFamilyPlanning,methodOfContraception,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,anyOtherMethod,mpaFileUpload1),
                        addItems = listOf(pregnancyTestResult)
                    )
                }
                else{
                    if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        triggerDependants(
                            source = isPregnancyTestDone,
                            removeItems = listOf(isPregnant,pregnancyTestResult),
                            addItems = listOf(usingFamilyPlanning)
                        )
                    }else{
                        triggerDependants(
                            source = isPregnancyTestDone,
                            removeItems = listOf(isPregnant,pregnancyTestResult),
                            addItems = listOf(usingFamilyPlanningMitanin)
                        )
                    }
                }

                return 0
            }

            pregnancyTestResult.id -> {
                if (pregnancyTestResult.value == resources.getStringArray(R.array.ectdset_po_neg)[0]) {
                    isPregnant.value = resources.getStringArray(R.array.yes_no)[0]
                    isPregnant.isEnabled = false

                    if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        triggerDependants(
                            source = pregnancyTestResult,
                            passedIndex = index,
                            triggerIndex = 0,
                            target = isPregnant,
                            targetSideEffect = listOf(isPregnant,usingFamilyPlanning,methodOfContraception, anyOtherMethod)
                        )
                        triggerforHide(
                            source = pregnancyTestResult,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanning,
                            targetSideEffect = listOf(usingFamilyPlanning,methodOfContraception,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,anyOtherMethod)
                        )
                    }else{

                        triggerDependants(
                            source = pregnancyTestResult,
                            passedIndex = index,
                            triggerIndex = 0,
                            target = isPregnant,
                            targetSideEffect = listOf(isPregnant,usingFamilyPlanningMitanin,methodOfContraception, anyOtherMethod)
                        )
                        triggerforHide(
                            source = pregnancyTestResult,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanningMitanin,
                            targetSideEffect = listOf(usingFamilyPlanningMitanin,methodOfContraception,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,anyOtherMethod)
                        )
                    }
                }
                else if (pregnancyTestResult.value == resources.getStringArray(R.array.ectdset_po_neg)[1]) {
                    isPregnant.isEnabled = true
                    isPregnant.value = resources.getStringArray(R.array.yes_no)[1]


                    if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        triggerDependants(
                            source = pregnancyTestResult,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = isPregnant,
                            targetSideEffect = listOf(isPregnant,usingFamilyPlanning,methodOfContraception, anyOtherMethod,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection)
                        )
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanning,
                            targetSideEffect = listOf(methodOfContraception, anyOtherMethod,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection)
                        )
                    }else{

                        triggerDependants(
                            source = pregnancyTestResult,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = isPregnant,
                            targetSideEffect = listOf(isPregnant,usingFamilyPlanningMitanin,methodOfContraception, anyOtherMethod,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection)
                        )
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanningMitanin,
                            targetSideEffect = listOf(methodOfContraception, anyOtherMethod,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection)
                        )

                    }


                }
                else {
                    isPregnant.value = null
                    isPregnant.isEnabled = true
                }

                return 0
            }

            isPregnant.id -> {
                var list1: List<Any> = emptyList()
                if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                    list1=listOf(methodOfContraception, anyOtherMethod,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,mpaFileUpload1)
                }else{
                    list1= listOf(methodOfContraception, anyOtherMethod,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection)

                }
                if (isPregnant.value == resources.getStringArray(R.array.yes_no_donno)[0]) {
                    if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanning,
                            targetSideEffect = list1
                        )
                    }else{
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanningMitanin,
                            targetSideEffect = list1
                        )
                    }
                }
                else if (isPregnant.value == resources.getStringArray(R.array.yes_no_donno)[1]) {
                    if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanning,
                            targetSideEffect = list1
                        )
                    }else{
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 1,
                            target = usingFamilyPlanningMitanin,
                            targetSideEffect = list1
                        )
                    }
                }
                else {
                    if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 2,
                            target = usingFamilyPlanning,
                            targetSideEffect = list1 )
                    }else{
                        triggerDependants(
                            source = isPregnant,
                            passedIndex = index,
                            triggerIndex = 2,
                            target = usingFamilyPlanningMitanin,
                            targetSideEffect = list1 )
                    }
                }
                return 0


            }

            usingFamilyPlanning.id -> {
                antraDoses.value = antraDoseValue

                if (usingFamilyPlanning.value == resources.getStringArray(R.array.yes_no_donno)[0]) {
                    triggerDependants(
                        source = usingFamilyPlanning,
                        removeItems = emptyList(),
                        addItems = listOf(methodOfContraception)
                    )
                }
                else{
                    triggerDependants(
                        source = usingFamilyPlanning,
                        removeItems = listOf(methodOfContraception,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,anyOtherMethod,mpaFileUpload1),
                        addItems = emptyList()
                    )
                }
//

            }

            methodOfContraception.id -> {

                when (methodOfContraception.value) {

                    resources.getStringArray(R.array.method_of_contraception)[1] -> {
                        var list1: List<Any> = emptyList()
                        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                            list1=listOf(antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,mpaFileUpload1)

                        }else{
                            list1= listOf(antraDoses,dateOfAntraInjection,dueDateOfAntraInjection)

                        }

                        triggerDependants(
                            source = methodOfContraception,
                            addItems = list1,
                            removeItems = listOf(anyOtherMethod,deliveryDischargeSummary1,
                                deliveryDischargeSummary2),
                            position = getIndexById(methodOfContraception.id) + 1
                        )
                    }

                    resources.getStringArray(R.array.method_of_contraception).last() -> {
                        triggerDependants(
                            source = methodOfContraception,
                            addItems = listOf(anyOtherMethod),
                            removeItems = listOf(antraDoses,deliveryDischargeSummary1,
                                deliveryDischargeSummary2,mpaFileUpload1),
                            position = getIndexById(methodOfContraception.id) + 1
                        )
                    }

                    else -> {

                        if(noOfChildrens!=0)
                        {
                            val methods = resources.getStringArray(R.array.method_of_contraception).toMutableList()
                            val sterilizationIndices = listOf(7, 8, 9)
                            val selectedIndex = methods.indexOf(methodOfContraception.value)
                            if (selectedIndex in sterilizationIndices) {
                                triggerDependants(
                                    source = methodOfContraception,
                                    addItems =
                                        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true))
                                            listOf(deliveryDischargeSummary1, deliveryDischargeSummary2)
                                        else
                                            emptyList(),
                                    removeItems = listOf(
                                        antraDoses,
                                        anyOtherMethod,
                                        dateOfAntraInjection,
                                        dueDateOfAntraInjection,
                                        mpaFileUpload1,
                                    ),
                                    position = -1
                                )
                            }
                            else{
                                triggerDependants(
                                    source = methodOfContraception,
                                    addItems = emptyList(),
                                    removeItems = listOf(antraDoses, anyOtherMethod,dateOfAntraInjection,dueDateOfAntraInjection,mpaFileUpload1, deliveryDischargeSummary1,
                                        deliveryDischargeSummary2),
                                    position = -1
                                )
                            }
                        }else{
                            triggerDependants(
                                source = methodOfContraception,
                                addItems = emptyList(),
                                removeItems = listOf(antraDoses, anyOtherMethod,dateOfAntraInjection,dueDateOfAntraInjection,mpaFileUpload1, deliveryDischargeSummary1,
                                    deliveryDischargeSummary2),
                                position = -1
                            )
                        }

                    }
                }
                return 0
            }


            anyOtherMethod.id -> {
                validateAllAlphabetsSpaceOnEditText(anyOtherMethod)
            }

            usingFamilyPlanningMitanin.id -> {

                if (usingFamilyPlanningMitanin.value == resources.getStringArray(R.array.ectdset_yes_no)[0]) {
                    // YES → show FP Methods, hide Q2
                    triggerDependants(
                        source = usingFamilyPlanningMitanin,
                        removeItems = listOf(wantToUseFamilyPlanning),
                        addItems = listOf(methodOfContraception)
                    )
                } else {
                    // NO → show Q2, hide FP Methods
                    triggerDependants(
                        source = usingFamilyPlanningMitanin,
                        removeItems = listOf(
                            methodOfContraception,
                            antraDoses,
                            dateOfAntraInjection,
                            dueDateOfAntraInjection,
                            anyOtherMethod,
                            mpaFileUpload1,
                            deliveryDischargeSummary1,
                            deliveryDischargeSummary2
                        ),
                        addItems = listOf(wantToUseFamilyPlanning)
                    )
                }
                return 0
            }
            wantToUseFamilyPlanning.id -> {

                if (wantToUseFamilyPlanning.value == resources.getStringArray(R.array.ectdset_yes_no)[0]) {
                    // YES → show FP Methods
                    triggerDependants(
                        source = wantToUseFamilyPlanning,
                        removeItems = emptyList(),
                        addItems = listOf(methodOfContraception)
                    )
                } else {
                    // NO → END FLOW
                    triggerDependants(
                        source = wantToUseFamilyPlanning,
                        removeItems = listOf(
                            methodOfContraception,
                            antraDoses,
                            dateOfAntraInjection,
                            dueDateOfAntraInjection,
                            anyOtherMethod,
                            mpaFileUpload1,
                            deliveryDischargeSummary1,
                            deliveryDischargeSummary2
                        ),
                        addItems = emptyList()
                    )
                }
                return 0
            }


            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EligibleCoupleTrackingCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.lmpDate = getLongFromDate(lmpDate.value)
            form.dateOfAntraInjection=dateOfAntraInjection.value
            form.dueDateOfAntraInjection=dueDateOfAntraInjection.value
            form.mpaFile=mpaFileUpload1.value
            form.antraDose=antraDoses.value
            form.isPregnancyTestDone = isPregnancyTestDone.value
            form.pregnancyTestResult = pregnancyTestResult.value
            form.isPregnant = isPregnant.value
            form.dischargeSummary1 = deliveryDischargeSummary1.value
            form.dischargeSummary2 = deliveryDischargeSummary2.value


            form.usingFamilyPlanning =
                if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                    usingFamilyPlanningMitanin.value == resources.getStringArray(R.array.ectdset_yes_no)[0] ||
                            wantToUseFamilyPlanning.value == resources.getStringArray(R.array.ectdset_yes_no)[0]
                } else {
                    usingFamilyPlanning.value == resources.getStringArray(R.array.yes_no)[0]
                }

            if (methodOfContraception.value == resources.getStringArray(R.array.method_of_contraception)
                    .last()
            ) {
                form.methodOfContraception = anyOtherMethod.value
            } else  if (methodOfContraception.value == resources.getStringArray(R.array.method_of_contraception)[1]) {
                form.methodOfContraception = "${methodOfContraception.value}/${antraDoses.value}"
            }
            else {
                form.methodOfContraception = methodOfContraception.value
            }
        }
    }

    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array2)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
        benRegCache.syncState = SyncState.UNSYNCED
    }


    fun getIndexOfMPA() = getIndexById(mpaFileUpload1.id)
    fun getIndexDeliveryDischargeSummary1 () = getIndexById(deliveryDischargeSummary1.id)
    fun getIndexDeliveryDischargeSummary2 () = getIndexById(deliveryDischargeSummary2.id)
    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {

        when (lastImageFormId) {
            21 -> {
                mpaFileUpload1.value = dpUri.toString()
                mpaFileUpload1.errorText = null
            }
            58 -> {
                deliveryDischargeSummary1.value = dpUri.toString()
                deliveryDischargeSummary1.errorText = null
            }
            59 -> {
                deliveryDischargeSummary2.value = dpUri.toString()
                deliveryDischargeSummary2.errorText = null
            }

        }
    }
    private fun calculateNextInjectionDate(
        injectionDate: String?,
        minDays: Int,
        maxDays: Int
    ): Pair<String, String> {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = sdf.parse(injectionDate ?: "") ?: return "" to ""

            val cal = Calendar.getInstance()
            cal.time = date

            cal.add(Calendar.DAY_OF_YEAR, minDays)
            val minDate = sdf.format(cal.time)

            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, maxDays)
            val maxDate = sdf.format(cal.time)

            minDate to maxDate
        } catch (e: Exception) {
            "" to ""
        }
    }


    private fun getNextDose(lastDose: String?, lastDate: String?, visitDate: String): String {

        if (lastDose == null || lastDate == null) {
            return "Dose-1"
        }

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val last = sdf.parse(lastDate) ?: return "Dose-1"
        val visit = sdf.parse(visitDate) ?: return "Dose-1"

        val diffDays = ((visit.time - last.time) / (1000 * 60 * 60 * 24))
        if (diffDays > 120) {
            return "Dose-1"
        }
        val doseNum = lastDose.filter { it.isDigit() }.toIntOrNull() ?: 0
        val next = doseNum + 1
        return if (next in 1..10) "Dose-$next" else "No More Doses"
    }



}
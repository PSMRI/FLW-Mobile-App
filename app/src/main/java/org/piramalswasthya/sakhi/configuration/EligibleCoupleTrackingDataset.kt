package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.util.Log
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class EligibleCoupleTrackingDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
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

    private val methodOfContraception = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.ectdset_mthd_conpt),
        arrayId = R.array.method_of_contraception,
        entries = resources.getStringArray(R.array.method_of_contraception),
        required = false,
        hasDependants = true

    )

    private val antraDoses = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = "Antra injection",
        arrayId = R.array.antra_doses,
        entries = resources.getStringArray(R.array.antra_doses),
        required = true,
        hasDependants = true

    )

    private val anyOtherMethod = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.ectdset_other_mthd),
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT,
        etMaxLength = 50
    )

    private var dateOfAntraInjection = FormElement(
        id = 11,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.date_of_antra_injection),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )
    private var dueDateOfAntraInjection = FormElement(
        id = 12,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.due_date_of_next_injection),
        required = false,

    )

    private val mpaFileUpload1 = FormElement(
        id = 21,
        inputType = InputType.FILE_UPLOAD,
        title = context.getString(R.string.mpa_card),
        required = false,
    )
    fun getIndexOfIsPregnant() = getIndexById(isPregnant.id)
    suspend fun setUpPage(
        ben: BenRegCache?,
        dateOfReg: Long,
        lastTrack: EligibleCoupleTrackingCache?,
        saved: EligibleCoupleTrackingCache?
    ) {
        val list = mutableListOf(
            dateOfVisit,
            financialYear,
            month,
            isPregnancyTestDone,
        )
        if (saved == null) {
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
            dateOfVisit.value?.let {
                financialYear.value = getFinancialYear(it)
                month.value =
                    resources.getStringArray(R.array.visit_months)[Companion.getMonth(it)!!]
            }
            if (ben != null) {
                dateOfAntraInjection.min=ben.regDate
            }
            val nextDose = getNextDose(lastTrack?.antraDose, lastTrack?.dateOfAntraInjection)
            antraDoses.value = nextDose
            antraDoses.isEnabled = false

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
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            financialYear.value = getFinancialYear(dateString = dateOfVisit.value)
            month.value =
                resources.getStringArray(R.array.visit_months)[Companion.getMonth(dateOfVisit.value)!!]
            isPregnancyTestDone.value =
                getLocalValueInArray(R.array.yes_no, saved.isPregnancyTestDone)
            if (isPregnancyTestDone.value == resources.getStringArray(R.array.yes_no)[0]) {
                list.add(list.indexOf(isPregnancyTestDone) + 1, pregnancyTestResult)
                pregnancyTestResult.value = saved.pregnancyTestResult
            }
            isPregnant.value = getLocalValueInArray(R.array.yes_no, saved.isPregnant)
            if (isPregnant.value == resources.getStringArray(R.array.yes_no)[1]) {
                list.add(usingFamilyPlanning)
                saved.usingFamilyPlanning?.let {
                    usingFamilyPlanning.value =
                        if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                            R.array.yes_no
                        )[1]
                }
                usingFamilyPlanning.value =
                    if (saved.usingFamilyPlanning == true) resources.getStringArray(R.array.yes_no)[1] else resources.getStringArray(
                        R.array.yes_no
                    )[1]
                if (saved.usingFamilyPlanning == true) {
                    list.add(methodOfContraception)
                    if (saved.methodOfContraception in resources.getStringArray(R.array.method_of_contraception)) {
                        methodOfContraception.value = saved.methodOfContraception
                    } else if (saved.methodOfContraception != null) {
                        if (saved.methodOfContraception!!.split("/")[0] == resources.getStringArray(R.array.method_of_contraception)[1]) {
                            methodOfContraception.value =
                                resources.getStringArray(R.array.method_of_contraception)[1]
                            list.add(antraDoses)
                            list.add(dateOfAntraInjection)
                            list.add(dueDateOfAntraInjection)
                            list.add(mpaFileUpload1)
                            dateOfAntraInjection.value=saved.dateOfAntraInjection
                            dueDateOfAntraInjection.value=saved.dueDateOfAntraInjection
                            mpaFileUpload1.value=saved.mpaFile

//                            antraDoses.value = saved.methodOfContraception!!.split("/")[1]
                            if(saved.antraDose!=null){
                                antraDoses.value = saved.antraDose

                            }
                        }
                        else {
                            methodOfContraception.value =
                                resources.getStringArray(R.array.method_of_contraception).last()
                            list.add(anyOtherMethod)
                            anyOtherMethod.value = saved.methodOfContraception
                        }

                    }

                }
            }

        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dateOfVisit.id -> {
                financialYear.value = Companion.getFinancialYear(dateOfVisit.value)
                month.value = resources.getStringArray(R.array.visit_months)[Companion.getMonth(dateOfVisit.value)!!]
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
                if (isPregnant.value == resources.getStringArray(R.array.yes_no_donno)[0]) {
                    triggerDependants(
                        source = isPregnancyTestDone,
                        passedIndex = index,
                        triggerIndex = 0,
                        target = pregnancyTestResult
                    )
                }
                else if (isPregnant.value == resources.getStringArray(R.array.yes_no_donno)[1]) {
                    triggerDependants(
                        source = isPregnancyTestDone,
                        passedIndex = index,
                        triggerIndex = 0,
                        target = pregnancyTestResult
                    )
                    triggerforHide(
                        source = isPregnancyTestDone,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = isPregnant,
                        targetSideEffect = listOf(isPregnant,usingFamilyPlanning,methodOfContraception,antraDoses,anyOtherMethod)
                    )

                }
                else {
                    triggerDependants(
                        source = isPregnancyTestDone,
                        passedIndex = index,
                        triggerIndex = 0,
                        target = pregnancyTestResult
                    )
                    triggerforHide(
                        source = isPregnancyTestDone,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = isPregnant,
                        targetSideEffect = listOf(isPregnant,usingFamilyPlanning,methodOfContraception,antraDoses,anyOtherMethod)
                    )
                }
                return 0


            }

            pregnancyTestResult.id -> {
                if (pregnancyTestResult.value == resources.getStringArray(R.array.ectdset_po_neg)[0]) {
                    isPregnant.value = resources.getStringArray(R.array.yes_no)[0]
                    isPregnant.isEnabled = false
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
                        targetSideEffect = listOf(usingFamilyPlanning,methodOfContraception,antraDoses,anyOtherMethod)
                    )
                }
               else if (pregnancyTestResult.value == resources.getStringArray(R.array.ectdset_po_neg)[1]) {
                    isPregnant.isEnabled = true
                    isPregnant.value = resources.getStringArray(R.array.yes_no)[1]
                    triggerDependants(
                        source = pregnancyTestResult,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = isPregnant,
                        targetSideEffect = listOf(isPregnant,usingFamilyPlanning,methodOfContraception, anyOtherMethod,antraDoses)
                    )
                    triggerDependants(
                        source = isPregnant,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = usingFamilyPlanning,
                        targetSideEffect = listOf(methodOfContraception, anyOtherMethod,antraDoses)
                    )

                }
                else {
                    isPregnant.value = null
                    isPregnant.isEnabled = true
                }

               return 0
            }

            isPregnant.id -> {
                if (isPregnant.value == resources.getStringArray(R.array.yes_no_donno)[0]) {
                    triggerDependants(
                        source = isPregnant,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = usingFamilyPlanning,
                        targetSideEffect = listOf(methodOfContraception, anyOtherMethod,antraDoses)
                    )
                }
                else if (isPregnant.value == resources.getStringArray(R.array.yes_no_donno)[1]) {
                    triggerDependants(
                        source = isPregnant,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = usingFamilyPlanning,
                        targetSideEffect = listOf(methodOfContraception, anyOtherMethod,antraDoses)
                    )
                }
                else {
                    triggerDependants(
                        source = isPregnant,
                        passedIndex = index,
                        triggerIndex = 2,
                        target = usingFamilyPlanning,
                        targetSideEffect = listOf(methodOfContraception, anyOtherMethod,antraDoses)
                    )
                }
                return 0


            }

            usingFamilyPlanning.id -> {
                triggerDependants(
                    source = usingFamilyPlanning,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = methodOfContraception,
                    targetSideEffect = listOf(anyOtherMethod,antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,mpaFileUpload1)
                )
            }

            methodOfContraception.id -> {
                when (methodOfContraception.value) {
                    resources.getStringArray(R.array.method_of_contraception)[1] -> {
                        triggerDependants(
                            source = methodOfContraception,
                            addItems = listOf(antraDoses,dateOfAntraInjection,dueDateOfAntraInjection,mpaFileUpload1),
                            removeItems = listOf(anyOtherMethod),
                            position = getIndexById(methodOfContraception.id) + 1
                        )
                    }

                    resources.getStringArray(R.array.method_of_contraception).last() -> {
                        triggerDependants(
                            source = methodOfContraception,
                            addItems = listOf(anyOtherMethod),
                            removeItems = listOf(antraDoses),
                            position = getIndexById(methodOfContraception.id) + 1
                        )
                    }

                    else -> {
                        triggerDependants(
                            source = methodOfContraception,
                            addItems = emptyList(),
                            removeItems = listOf(antraDoses, anyOtherMethod,dateOfAntraInjection,dueDateOfAntraInjection,mpaFileUpload1),
                            position = -1
                        )
                    }
                }
                return 0
            }


            anyOtherMethod.id -> {
                validateAllAlphabetsSpaceOnEditText(anyOtherMethod)
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EligibleCoupleTrackingCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.dateOfAntraInjection=dateOfAntraInjection.value
            form.dueDateOfAntraInjection=dueDateOfAntraInjection.value
            form.mpaFile=mpaFileUpload1.value
            form.antraDose=antraDoses.value
            form.isPregnancyTestDone = isPregnancyTestDone.value
            form.pregnancyTestResult = pregnancyTestResult.value
            form.isPregnant = isPregnant.value
            form.usingFamilyPlanning = usingFamilyPlanning.value?.let { it == resources.getStringArray(R.array.yes_no)[0] }
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
                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
        benRegCache.syncState = SyncState.UNSYNCED
    }


    fun getIndexOfMPA() = getIndexById(mpaFileUpload1.id)
    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {

        when (lastImageFormId) {
            21 -> {
                mpaFileUpload1.value = dpUri.toString()
                mpaFileUpload1.errorText = null
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

    private fun getNextDose(lastDose: String?, lastDate: String?): String {
        if (lastDose == null || lastDate == null) {
            return "Dose-1"
        }

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = Calendar.getInstance().time
        val last = sdf.parse(lastDate) ?: return "Dose-1"

        val diffDays = ((today.time - last.time) / (1000 * 60 * 60 * 24))

        if (diffDays > 120) {
            return "Dose-1"
        }

        val doseNum = lastDose.filter { it.isDigit() }.toIntOrNull() ?: 0
        val next = doseNum + 1

        return if (next in 1..10) "Dose-$next" else "No More Doses"
    }





}
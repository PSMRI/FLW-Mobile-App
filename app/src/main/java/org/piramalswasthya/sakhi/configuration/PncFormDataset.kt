package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.model.getDateStrFromLong
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.form.PncFormViewModel
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PncFormDataset(
    context: Context, currentLanguage: Languages,  private val viewModel: PncFormViewModel? = null
) : Dataset(context, currentLanguage) {

    private var visit: Int = 0
    private var dateOfDelivery: Long = 0L

    companion object{
        fun getMinDeliveryDate(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1)
            return cal.timeInMillis
        }
    }


    private val deliveryDate = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = "Date of Delivery",
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = getMinDeliveryDate(),
        isEnabled = true
    )

    private val pncPeriod = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_period),
//        entries = resources.getStringArray(R.array.pnc_period_array),
        arrayId = -1,
        required = true,
        hasDependants = false
    )

    private val visitDate = FormElement(
        id = 3,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.pnc_visit_date),
        arrayId = -1,
        required = true,
        hasDependants = false
    )


    private val ifaTabsGiven = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_ifa_tabs_given),
        required = false,
        hasDependants = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        max = 400,
        min = 0,
    )

    private val anyContraceptionMethod = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pnc_any_contraception_method),
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true
    )

    private val contraceptionMethod = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_contraception_method),
        entries = resources.getStringArray(R.array.pnc_contraception_method_array),
        required = false,
        hasDependants = true
    )

    private val otherPpcMethod = FormElement(
        id = 7,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_ppc_method),
        required = true,
        hasDependants = false
    )

    private val motherDangerSign = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_mother_danger_sign),
        entries = resources.getStringArray(R.array.pnc_mother_danger_sign_array),
        required = true,
        hasDependants = true
    )

    private val otherDangerSign = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_danger_sign),
        required = true,
        hasDependants = false
    )

    private val referralFacility = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_referral_facility),
        entries = resources.getStringArray(R.array.pnc_referral_facility_array),
        required = false,
        hasDependants = false
    )

    private val motherDeath = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pnc_mother_death),
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true,
    )

    private val deathDate = FormElement(
        id = 12,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.pnc_death_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val causeOfDeath = FormElement(
        id = 13,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_death_cause),
        entries = resources.getStringArray(R.array.pnc_death_cause_array),
        required = true,
        hasDependants = true,
    )

    private val otherDeathCause = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_other_death_cause),
        required = true,
        hasDependants = false
    )

    private val placeOfDeath = FormElement(
        id = 15,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.pnc_death_place),
        entries = resources.getStringArray(R.array.death_place_array),
        required = true,
        hasDependants = false,
    )

    private val remarks = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_remarks),
        required = false,
        hasDependants = false
    )
    private val otherPlaceOfDeath = FormElement(
        id = 55,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.other_place_of_death),
        required = true,
        hasDependants = true,
    )

    private val dateOfSterilisation = FormElement(
        id = 56,
        inputType = InputType.DATE_PICKER,
        title = "Date of Sterilisation",
        arrayId = -1,
        max = System.currentTimeMillis(),
        min = getMinDeliveryDate(),
        required = true,
        hasDependants = false

    )
    private val anySignOfDanger = FormElement(
        id = 57,
        inputType = InputType.RADIO,
        title = "Any Danger Signs?\n" +
                "Any High-risk identified?",
        entries = resources.getStringArray(R.array.pnc_confirmation_array),
        required = false,
        hasDependants = true
    )

    private val deliveryDischargeSummary1  = FormElement(
        id = 58,
        inputType = InputType.FILE_UPLOAD,
        title = "Delivery Discharge Summary 1",
        required = false

    )

    private val deliveryDischargeSummary2 = FormElement(
        id =59,
        inputType = InputType.FILE_UPLOAD,
        title = "Delivery Discharge Summary 2",
        required = false
    )

    private val deliveryDischargeSummary3 = FormElement(
        id =60,
        inputType = InputType.FILE_UPLOAD,
        title = "Delivery Discharge Summary 3",
        required = false
    )

    private val deliveryDischargeSummary4 = FormElement(
        id =61,
        inputType = InputType.FILE_UPLOAD,
        title = "Delivery Discharge Summary 4",
        required = false
    )

    private val sterilisation : Array<String> by lazy {
        resources.getStringArray(R.array.sterilization_methods_array)
    }

    suspend fun
            setUpPage(
        visitNumber: Int,
        ben: BenRegCache,
        deliveryOutcomeCache: DeliveryOutcomeCache?=null,
        previousPnc: PNCVisitCache?,
        saved: PNCVisitCache?,
        hasPreviousPermanentSterilization: Boolean = false
    ) {
        val list = mutableListOf(
            deliveryDate,
            pncPeriod,
            visitDate,
            motherDeath,
            ifaTabsGiven,
            anyContraceptionMethod,
            anySignOfDanger,
            referralFacility,
            remarks,
            deliveryDischargeSummary1,
            deliveryDischargeSummary2,
            deliveryDischargeSummary3,
            deliveryDischargeSummary4

        )

        dateOfDelivery = deliveryOutcomeCache?.dateOfDelivery?:0L
        if (dateOfDelivery!=0L){
            deathDate.min = dateOfDelivery
            deliveryDate.isEnabled = false
        }

        deathDate.max = System.currentTimeMillis()
        anySignOfDanger.value = anySignOfDanger.entries!!.last()
        motherDeath.value = motherDeath.entries!!.last()
        val daysSinceDeliveryMillis = Calendar.getInstance()
            .setToStartOfTheDay().timeInMillis - deliveryOutcomeCache?.dateOfDelivery.let {
            val cal = Calendar.getInstance()
            if (it != null) {
                cal.timeInMillis = it
            }
            cal.setToStartOfTheDay()
            cal.timeInMillis
        }
        val daysSinceDelivery = TimeUnit.MILLISECONDS.toDays(daysSinceDeliveryMillis)
        deliveryDate.value = getDateFromLong(dateOfDelivery)
        pncPeriod.entries =
            listOf(
                1,
                3,
                7,
                14,
                21,
                28,
                42
            ).filter { if (daysSinceDelivery == 0L) it <= 1 else it <= daysSinceDelivery }
                .filter { it > (previousPnc?.pncPeriod ?: 0) }
                .map { "Day $it" }.toTypedArray()

        if (hasPreviousPermanentSterilization) {

            anyContraceptionMethod.isEnabled = false
            contraceptionMethod.isEnabled = false
            dateOfSterilisation.isEnabled = false
            otherPpcMethod.isEnabled = false


            val lastSterilizationVisit =
                viewModel?.getLastPermanentSterilizationVisit(ben.beneficiaryId, visitNumber)
            lastSterilizationVisit?.let { sterilizationVisit ->

                anyContraceptionMethod.value = if (sterilizationVisit.anyContraceptionMethod == true)
                    anyContraceptionMethod.entries!!.first() else anyContraceptionMethod.entries!!.last()

                contraceptionMethod.value = sterilizationVisit.contraceptionMethod
                dateOfSterilisation.value = getDateFromLong(sterilizationVisit.sterilisationDate!!)
                otherPpcMethod.value = sterilizationVisit.otherPpcMethod

                if (sterilizationVisit.anyContraceptionMethod == true) {
                    list.add(list.indexOf(anyContraceptionMethod) + 1, contraceptionMethod)

                    if (sterilizationVisit.contraceptionMethod in sterilisation) {
                        list.add(list.indexOf(contraceptionMethod) + 1, dateOfSterilisation)
                    }

                    if (sterilizationVisit.contraceptionMethod == contraceptionMethod.entries!!.last()) {
                        list.add(list.indexOf(contraceptionMethod) + 1, otherPpcMethod)
                    }
                }


            }}



        saved?.let {
            pncPeriod.value = "Day ${it.pncPeriod}"
            visitDate.value = getDateFromLong(it.pncDate)
            ifaTabsGiven.value = it.ifaTabsGiven?.toString()
            anyContraceptionMethod.value = it.anyContraceptionMethod?.let {
                if (it)
                    anyContraceptionMethod.entries!!.first()
                else
                    anyContraceptionMethod.entries!!.last()
            }
            if (it.anyContraceptionMethod == true) {
                list.add(list.indexOf(anyContraceptionMethod) + 1, contraceptionMethod)
            }
            contraceptionMethod.value = it.contraceptionMethod
            dateOfSterilisation.value = getDateFromLong(it.sterilisationDate!!)
            if (it.contraceptionMethod == contraceptionMethod.entries!!.last()) {
                list.add(list.indexOf(contraceptionMethod) + 1, otherPpcMethod)

            }
            if(it.contraceptionMethod in sterilisation )
            {
                list.add(list.indexOf(contraceptionMethod) + 1, dateOfSterilisation)

            }

            otherPpcMethod.value = it.otherPpcMethod
            anySignOfDanger.value = it.anyDangerSign
            anySignOfDanger.value?.let { dangerSignValue ->
                val isDangerSignYes = dangerSignValue == anySignOfDanger.entries!!.first()
                referralFacility.required = isDangerSignYes
            }
            motherDangerSign.value = it.motherDangerSign
            if (it.motherDangerSign == motherDangerSign.entries!!.last()) {
                list.add(list.indexOf(motherDangerSign) + 1, otherDangerSign)
            }
            otherDangerSign.value = it.otherDangerSign
            referralFacility.value = it.referralFacility
            motherDeath.value =
                if (it.motherDeath) motherDeath.entries!!.first() else motherDeath.entries!!.last()
            if (it.motherDeath) {
                deathDate.value = getDateStrFromLong(it.deathDate)
                causeOfDeath.value = it.causeOfDeath
                otherDeathCause.value = it.otherDeathCause
                placeOfDeath.value = it.placeOfDeath
                otherPlaceOfDeath.value = it.otherPlaceOfDeath
                placeOfDeath.entries?.indexOf(saved.placeOfDeath)?.takeIf { it >= 0 }?.let { index ->
                    if (index == 8) {
                        list.add(list.indexOf(placeOfDeath) + 1, otherPlaceOfDeath)
                    }
                }
                list.addAll(
                    list.indexOf(motherDeath) + 1,
                    listOf(deathDate, causeOfDeath, placeOfDeath)
                )
                if (causeOfDeath.value == causeOfDeath.entries!!.last())
                    list.add(list.indexOf(causeOfDeath) + 1, otherDeathCause)
            }
            remarks.value = it.remarks
            deliveryDischargeSummary1.value = it.deliveryDischargeSummary1
            deliveryDischargeSummary2.value = it.deliveryDischargeSummary2
            deliveryDischargeSummary3.value = it.deliveryDischargeSummary3
            deliveryDischargeSummary4.value = it.deliveryDischargeSummary4

        }

//        pncPeriod.entries = pncPeriod.entries!!.
//        if (saved == null) {
//            dateOfDelivery.value = Dataset.getDateFromLong(System.currentTimeMillis())
//            dateOfDischarge.value = Dataset.getDateFromLong(System.currentTimeMillis())
//        } else {
//            list = mutableListOf(
//                dateOfDelivery,
//                timeOfDelivery,
//                placeOfDelivery,
//                typeOfDelivery,
//                hadComplications,
////                complication,
////                causeOfDeath,
////                otherCauseOfDeath,
////                otherComplication,
//                deliveryOutcome,
//                liveBirth,
//                stillBirth,
//                dateOfDischarge,
//                timeOfDischarge,
//                isJSYBenificiary
//            )
//            dateOfDelivery.value = Dataset.getDateFromLong(saved.dateOfDelivery)
//            timeOfDelivery.value = saved.timeOfDelivery
//            placeOfDelivery.value = saved.placeOfDelivery
//            typeOfDelivery.value = saved.typeOfDelivery
//            hadComplications.value = if (saved.hadComplications == true) "Yes" else "No"
//            complication.value = saved.complication
//            causeOfDeath.value = saved.causeOfDeath
//            otherCauseOfDeath.value = saved.otherCauseOfDeath
//            otherComplication.value = saved.otherComplication
//            deliveryOutcome.value = saved.deliveryOutcome.toString()
//            liveBirth.value = saved.liveBirth.toString()
//            stillBirth.value = saved.stillBirth.toString()
//            dateOfDischarge.value = Dataset.getDateFromLong(saved.dateOfDischarge)
//            timeOfDischarge.value = saved.timeOfDischarge
//            isJSYBenificiary.value = if (saved.isJSYBenificiary == true) "Yes" else "No"
//        }
//        ben?.let {
//            dateOfDelivery.min = it.regDate
//        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            pncPeriod.id -> {
                dateOfDelivery = getLongFromDate(deliveryDate.value)

                visitDate.value = null
                visitDate.inputType = InputType.DATE_PICKER



                val today = Calendar.getInstance().setToStartOfTheDay().timeInMillis
                when (val visitNumber = pncPeriod.value!!.substring(4).toInt()) {
                    1 -> {
                        visitDate.min = minOf(today, dateOfDelivery)
                        visitDate.max = minOf(
                            today,
                            dateOfDelivery + TimeUnit.DAYS.toMillis(1)
                        )
                    }

                    3 -> {
                        visitDate.min = minOf(today, dateOfDelivery + TimeUnit.DAYS.toMillis(3))
                        visitDate.max = minOf(
                            today,
                            dateOfDelivery + TimeUnit.DAYS.toMillis(3)
                        )
                    }

                    7 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(7) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(7) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    14 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(14) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(14) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    21 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(21) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(21) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    28 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(28) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(28) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    42 -> {
                        visitDate.min =
                            minOf(
                                today,
                                dateOfDelivery + TimeUnit.DAYS.toMillis(42) - TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                        visitDate.max =
                            minOf(
                                System.currentTimeMillis(),
                                dateOfDelivery + TimeUnit.DAYS.toMillis(42) + TimeUnit.DAYS.toMillis(
                                    3
                                )
                            )
                    }

                    else -> throw IllegalStateException("Illegal PNC Date $visitNumber")
                }
                return -1
            }

            placeOfDeath.id -> {
                val index = placeOfDeath.entries?.indexOf(placeOfDeath.value).takeIf { it!! >= 0 } ?: return -1
                val triggerIndex = 8
                return triggerDependants(
                    source = placeOfDeath,
                    passedIndex = index,
                    triggerIndex = triggerIndex,
                    target = otherPlaceOfDeath
                )
            }

            ifaTabsGiven.id -> validateIntMinMax(ifaTabsGiven)

            anyContraceptionMethod.id -> triggerDependants(
                source = anyContraceptionMethod,
                passedIndex = index,
                triggerIndex = 0,
                target = contraceptionMethod,
                targetSideEffect =  listOf(
                    otherPpcMethod,
                    dateOfSterilisation
                )
            )


            contraceptionMethod.id -> {
                val selected = contraceptionMethod.entries?.getOrNull(index)?.trim() ?: ""
                Timber.d("Selected contraception: '$selected' (index=$index)")

                val requiresIncentiveAlert = selected.isNotEmpty() &&
                        !selected.equals("CONDOM", ignoreCase = true) &&
                        !selected.equals(contraceptionMethod.entries!!.last().trim(), ignoreCase = true)

                if (requiresIncentiveAlert) {
                    viewModel?.triggerIncentiveAlert()
                }

                val anyOtherValue = contraceptionMethod.entries!!.last().trim()
                val result1 = if (selected.equals(anyOtherValue, ignoreCase = true)) {
                    Timber.d("Will add OtherPPCMethod")
                    triggerDependants(
                        source = contraceptionMethod,
                        passedIndex = index,
                        triggerIndex = contraceptionMethod.entries!!.lastIndex,
                        target = otherPpcMethod
                    )
                } else {

                    triggerDependants(
                        source = contraceptionMethod,
                        passedIndex = -1, // Force removal
                        triggerIndex = contraceptionMethod.entries!!.lastIndex,
                        target = otherPpcMethod
                    )
                }


                val isSterilisation = sterilisation.any { it.equals(selected, ignoreCase = true) }
                Timber.d("isSterilisation = $isSterilisation")
                val result2 = if (isSterilisation) {
                    dateOfSterilisation.min = dateOfDelivery
                    dateOfSterilisation.max = System.currentTimeMillis()
                    Timber.d("Will add DateOfSterilisation")
                    triggerDependants(
                        source = contraceptionMethod,
                        passedIndex = index,
                        triggerIndex = index,
                        target = dateOfSterilisation
                    )
                } else {
                    dateOfSterilisation.value = null

                    triggerDependants(
                        source = contraceptionMethod,
                        passedIndex = -1,
                        triggerIndex = index,
                        target = dateOfSterilisation
                    )
                }

                if (result1 != -1) result1 else result2
            }

            anySignOfDanger.id -> {
                val result = triggerDependants(
                    source = anySignOfDanger,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = motherDangerSign,
                    targetSideEffect = listOf(otherDangerSign)
                )

                val oldRequiredState = referralFacility.required

                if (index == 0) {
                    referralFacility.required = true

                   /* if (referralFacility.value.isNullOrEmpty()) {
                        referralFacility.errorText = "This field is required"
                    }*/
                } else {
                    referralFacility.required = false
                    referralFacility.errorText = null
                }
                val referralFacilityIndex = getIndexById(referralFacility.id)
                return if (oldRequiredState != referralFacility.required && referralFacilityIndex != -1) {
                    referralFacilityIndex
                } else {
                    result
                }}



            motherDangerSign.id ->
                triggerDependants(
                    source = motherDangerSign,
                    passedIndex = index,
                    triggerIndex = motherDangerSign.entries!!.lastIndex,
                    target = otherDangerSign
                )

         /*   motherDeath.id -> {

                triggerDependants(
                    source = motherDeath,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = listOf(deathDate, causeOfDeath, placeOfDeath),
                    targetSideEffect = listOf(otherDeathCause)
                )

            }*/

            motherDeath.id -> {
                if (index == 0) {
                    triggerDependants(
                        source = motherDeath,
                        removeItems = listOf(
                            ifaTabsGiven,
                            anyContraceptionMethod,
                            anySignOfDanger,
                            referralFacility,
                            remarks

                        ),
                        addItems = listOf(
                            deathDate,
                            causeOfDeath,
                            placeOfDeath,
                            otherDeathCause
                        )
                    )
                } else {
                    triggerDependants(
                        source = motherDeath,
                        removeItems = listOf(
                            deathDate,
                            causeOfDeath,
                            placeOfDeath,
                            otherDeathCause
                        ),
                        addItems = listOf(
                            ifaTabsGiven,
                            anyContraceptionMethod,
                            anySignOfDanger,
                            referralFacility,
                            remarks

                        )
                    )
                }
            }



            causeOfDeath.id -> {
                triggerDependants(
                    source = causeOfDeath,
                    passedIndex = index,
                    triggerIndex = causeOfDeath.entries!!.lastIndex,
                    target = otherDeathCause
                )
            }

//
            else -> -1
        }
    }

//    private fun validateMaxDeliveryOutcome() : Int {
//        if(!liveBirth.value.isNullOrEmpty() && !stillBirth.value.isNullOrEmpty() &&
//            !deliveryOutcome.value.isNullOrEmpty() && deliveryOutcome.errorText.isNullOrEmpty()) {
//            if(deliveryOutcome.value!!.toInt() != liveBirth.value!!.toInt() + stillBirth.value!!.toInt()) {
//                deliveryOutcome.errorText = "Outcome of Delivery should equal to sum of Live and Still births"
//            }
//        }
//        return -1
//    }



    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PNCVisitCache).let { form ->
            form.pncPeriod = pncPeriod.value!!.substring(4).toInt()
            form.pncDate = getLongFromDate(visitDate.value!!)
            form.otherPlaceOfDeath=otherPlaceOfDeath.value
            form.dateOfDelivery = getLongFromDate(deliveryDate.value)
            form.ifaTabsGiven = ifaTabsGiven.value?.takeIf { it.isNotEmpty() }?.toInt()
            form.anyContraceptionMethod =
                anyContraceptionMethod.value?.let { it == anyContraceptionMethod.entries!!.first() }
            form.contraceptionMethod = contraceptionMethod.value?.takeIf { it.isNotEmpty() }
            form.otherPpcMethod = otherPpcMethod.value?.takeIf { it.isNotEmpty() }
            form.motherDangerSign = motherDangerSign.value?.takeIf { it.isNotEmpty() }
            form.otherDangerSign = otherDangerSign.value?.takeIf { it.isNotEmpty() }
            form.referralFacility = referralFacility.value?.takeIf { it.isNotEmpty() }
            form.motherDeath =
                motherDeath.value?.let { it == motherDeath.entries!!.first() } ?: false
            form.deathDate = deathDate.value?.let { getLongFromDate(it) }
            form.causeOfDeath = causeOfDeath.value?.takeIf { it.isNotEmpty() }
            form.otherDeathCause = otherDeathCause.value?.takeIf { it.isNotEmpty() }
            form.placeOfDeath = placeOfDeath.value?.takeIf { it.isNotEmpty() }
            form.remarks = remarks.value?.takeIf { it.isNotEmpty() }
            form.deliveryDischargeSummary1 = deliveryDischargeSummary1.value?.takeIf { it.isNotEmpty() }
            form.deliveryDischargeSummary2 = deliveryDischargeSummary2.value?.takeIf { it.isNotEmpty() }
            form.deliveryDischargeSummary3 = deliveryDischargeSummary3.value?.takeIf { it.isNotEmpty() }
            form.deliveryDischargeSummary4 = deliveryDischargeSummary4.value?.takeIf { it.isNotEmpty() }


        }
    }

    fun getIndexDeliveryDischargeSummary1 () = getIndexById(deliveryDischargeSummary1.id)
    fun getIndexDeliveryDischargeSummary2 () = getIndexById(deliveryDischargeSummary2.id)
    fun getIndexDeliveryDischargeSummary3 () = getIndexById(deliveryDischargeSummary3.id)
    fun getIndexDeliveryDischargeSummary4 () = getIndexById(deliveryDischargeSummary4.id)


    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            58 -> {
                deliveryDischargeSummary1.value = dpUri.toString()
                deliveryDischargeSummary1.errorText = null
            }
            59 -> {
                deliveryDischargeSummary2.value = dpUri.toString()
                deliveryDischargeSummary2.errorText = null
            }
            60 -> {
                deliveryDischargeSummary3.value = dpUri.toString()
                deliveryDischargeSummary3.errorText = null
            }
            61 -> {
                deliveryDischargeSummary4.value = dpUri.toString()
                deliveryDischargeSummary4.errorText = null
            }

        }
    }





}
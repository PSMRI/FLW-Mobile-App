package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.getMinAncFillDate
import org.piramalswasthya.sakhi.helpers.getWeeksOfPregnancy
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PregnantWomanAncVisitDataset(
    private val visitNumber: Int, context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var lmp: Long = 0L

    private val ancDate = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = "ANC Date",
        required = true,
        hasDependants = true,
    )
    private val weekOfPregnancy = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = "Weeks of Pregnancy",
        required = false,
    )

    private val ancVisit = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = "ANC Period",
        required = false,
    )
    private val isAborted = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Abortion If Any",
        entries = arrayOf("No", "Yes"),
        required = false,
        hasDependants = true
    )
    private val abortionType = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Abortion Type",
        entries = arrayOf("Induced", "Spontaneous"),
        required = true,
        hasDependants = true
    )
    private val abortionFacility = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Facility",
        entries = arrayOf("Govt. Hospital", "Pvt. Hospital"),
        required = true,
        hasDependants = true
    )
    private val abortionDate = FormElement(
        id = 7,
        inputType = InputType.DATE_PICKER,
        title = "Abortion Date",
        required = true,
        max = System.currentTimeMillis(),
    )

    private val weight = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = "Weight of PW (Kg) at time Registration",
        arrayId = -1,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 30,
        max = 200
    )
    private val bpSystolic = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = "BP of PW (mm Hg) – Systolic",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 50,
        max = 300
    )
    private val bpDiastolic = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = "BP of PW (mm Hg) – Diastolic",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 30,
        max = 200
    )

    private val pulseRate = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = "Pulse Rate",
        required = false,
    )

    private val hb = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = "HB (gm/dl)",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 4,
        required = false,
    )

    private val fundalHeight = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = "Fundal Height / Size of the Uterus weeks",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        required = false,
    )
    private val urineAlbumin = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        title = "Urine Albumin",
        entries = arrayOf("Absent", "Present"),
        required = false,
    )
    private val randomBloodSugarTest = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = "Random Blood Sugar Test",
        entries = arrayOf("Not Done", "Done"),
        required = false,
    )
    private val dateOfTTOrTd1 = FormElement(
        id = 16,
        inputType = InputType.DATE_PICKER,
        title = "Date of Td TT (1st Dose)",
        required = false,
        max = System.currentTimeMillis(),
    )
    private val dateOfTTOrTd2 = FormElement(
        id = 17,
        inputType = InputType.DATE_PICKER,
        title = "Date of Td TT (2nd Dose)",
        required = false,
        max = System.currentTimeMillis(),
    )
    private val dateOfTTOrTdBooster = FormElement(
        id = 18,
        inputType = InputType.DATE_PICKER,
        title = "Date of Td TT (Boooster Dose)",
        required = false,
        max = System.currentTimeMillis(),
    )
    private val numFolicAcidTabGiven = FormElement(
        id = 19,
        inputType = InputType.EDIT_TEXT,
        title = "No. of Folic Acid Tabs given",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        required = false,
        min = 0,
        max = 60
    )
    private val numIfaAcidTabGiven = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = "No. of IFA Tabs given",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3,
        required = false,
        min = 0,
        max = 400
    )
    private val anyHighRisk = FormElement(
        id = 21,
        inputType = InputType.RADIO,
        title = "Any High Risk conditions",
        entries = arrayOf("No", "Yes"),
        required = false,
        hasDependants = true
    )
    private val highRiskCondition = FormElement(
        id = 22, inputType = InputType.DROPDOWN, title = "High Risk Conditions", entries = arrayOf(
            "NONE",
            "HIGH BP (SYSTOLIC>=140 AND OR DIASTOLIC >=90mmHg)",
            "CONVULSIONS",
            "VAGINAL BLEEDING",
            "FOUL SMELLING DISCHARGE",
            "SEVERE ANAEMIA (HB<7 gm/dl)",
            "DIABETES",
            "TWINS",
            "OTHER",
        ), required = false, hasDependants = true
    )
    private val otherHighRiskCondition = FormElement(
        id = 23,
        inputType = InputType.EDIT_TEXT,
        title = "Any other High Risk conditions",
        required = true,
    )
    private val highRiskReferralFacility = FormElement(
        id = 24,
        inputType = InputType.DROPDOWN,
        title = "Referral Facility",
        entries = arrayOf(
            "Primary Health Centre",
            "Community Health Centre",
            "District Hospital",
            "Other Private Hospital",
        ),
        required = false,
    )
    private val hrpConfirm = FormElement(
        id = 25, inputType = InputType.RADIO, title = "Is HRP Confirmed?", entries = arrayOf(
            "No", "Yes",
        ), required = false, hasDependants = true
    )
    private val hrpConfirmedBy = FormElement(
        id = 26,
        inputType = InputType.DROPDOWN,
        title = "Who had identified as HRP?",
        entries = arrayOf(
            "ANM",
            "CHO",
            "PHC – MO",
            "Specialist at Higher Facility",
        ),
        required = true,
    )
    private val maternalDeath = FormElement(
        id = 27, inputType = InputType.RADIO, title = "Maternal Death", entries = arrayOf(
            "No", "Yes",
        ), required = false, hasDependants = true
    )
    private val maternalDeathProbableCause = FormElement(
        id = 28,
        inputType = InputType.DROPDOWN,
        title = "Probable Cause of Death",
        entries = arrayOf(
            "A. ECLAMPSIA", "B. HAEMORRHAGE", "C. HIGH FEVER", "D. ABORTION", "E. OTHER"
        ),
        required = true,
        hasDependants = true
    )
    private val otherMaternalDeathProbableCause = FormElement(
        id = 29,
        inputType = InputType.EDIT_TEXT,
        title = "Other Death Cause",
        required = true,
    )
    private val maternalDateOfDeath = FormElement(
        id = 30,
        inputType = InputType.DATE_PICKER,
        title = "Death Date",
        required = true,
        max = System.currentTimeMillis(),
    )

    private val deliveryDone = FormElement(
        id = 31,
        inputType = InputType.RADIO,
        title = "Has the pregnant woman delivered?",
        entries = arrayOf(
            "Yes", "No",
        ),
        required = false,
    )


    suspend fun setUpPage(
        ben: BenRegCache?,
        regis: PregnantWomanRegistrationCache,
        lastAnc: PregnantWomanAncCache?,
        saved: PregnantWomanAncCache?
    ) {
        lmp = regis.lmpDate
        val list = mutableListOf(
            ancDate,
            weekOfPregnancy,
            ancVisit,
            isAborted,
            weight,
            bpSystolic,
            bpDiastolic,
            pulseRate,
            hb,
            fundalHeight,
            urineAlbumin,
            randomBloodSugarTest,
            dateOfTTOrTd1,
            dateOfTTOrTd2,
            dateOfTTOrTdBooster,
            numFolicAcidTabGiven,
            numIfaAcidTabGiven,
            anyHighRisk,
            highRiskReferralFacility,
            hrpConfirm,
            maternalDeath

        )
        ben?.let {
            if (visitNumber == 1) {
                ancDate.min =
                    (lmp + TimeUnit.DAYS.toMillis(Konstants.minAnc1Week * 7L+1L)).also {
                        ancDate.value = getDateFromLong(it)
                    }
                ancDate.max = minOf(Calendar.getInstance().setToStartOfTheDay().timeInMillis,lmp + TimeUnit.DAYS.toMillis((Konstants.maxAnc1Week+1) * 7L-1))
            } else {
                lastAnc?.let {
                    val minWeekInit = when (visitNumber) {
                        2 -> Konstants.minAnc2Week
                        3 -> Konstants.minAnc3Week
                        4 -> Konstants.minAnc4Week
                        else -> throw IllegalStateException("visit number not in [2,4]")
                    }
                    val maxWeek = when (visitNumber) {
                        2 -> Konstants.maxAnc2Week
                        3 -> Konstants.maxAnc3Week
                        4 -> Konstants.maxAnc4Week
                        else -> throw IllegalStateException("visit number not in [2,4]")
                    }
                    val minWeek = getMinAncFillDate(
                        minWeekInit,
                        getWeeksOfPregnancy(
                            it.ancDate,
                            lmp,

                            )
                    )
                    ancDate.min = (lmp + TimeUnit.DAYS.toMillis(minWeek * 7L + 1L)).also {
                        ancDate.value = getDateFromLong(it)
                    }
                    ancDate.max = minOf(Calendar.getInstance().setToStartOfTheDay().timeInMillis,lmp + TimeUnit.DAYS.toMillis((Konstants.maxAnc1Week+1) * 7L-1))
                }
            }
        }
//        ancDate.value = getDateFromLong(System.currentTimeMillis())
        weekOfPregnancy.value = ancDate.value?.let {
            val long = getLongFromDate(it)
            val weeks = getWeeksOfPregnancy(long, lmp)
            if (weeks > 22) {
                list.add(deliveryDone)
            }
            weeks.toString()
        }
        ancVisit.value = visitNumber.toString()
        if (visitNumber == 1) {
            list.remove(fundalHeight)
            list.remove(numIfaAcidTabGiven)
        } else {
            list.remove(numFolicAcidTabGiven)
        }
        saved?.let {
            ancDate.value = getDateFromLong(it.ancDate)
            weekOfPregnancy.value = getWeeksOfPregnancy(it.ancDate, lmp).toString()
            isAborted.value =
                if (it.isAborted) isAborted.entries!!.last() else isAborted.entries!!.first()
            if (it.isAborted) {
                abortionType.value = abortionType.getStringFromPosition(it.abortionTypeId)
                abortionFacility.value =
                    abortionFacility.getStringFromPosition(it.abortionFacilityId)
                abortionDate.value = it.abortionDate?.let { getDateFromLong(it) }
                list.addAll(
                    list.indexOf(isAborted) + 1,
                    listOf(abortionType, abortionFacility, abortionDate)
                )
            }
            weight.value = it.weight?.toString()
            bpSystolic.value = it.bpSystolic?.toString()
            bpDiastolic.value = it.bpDiastolic?.toString()
            pulseRate.value = it.pulseRate
            hb.value = it.hb?.toString()
            fundalHeight.value = it.fundalHeight?.toString()
            urineAlbumin.value = urineAlbumin.getStringFromPosition(it.urineAlbuminId)
            randomBloodSugarTest.value =
                randomBloodSugarTest.getStringFromPosition(it.randomBloodSugarTestId)
            dateOfTTOrTd1.value = it.tt1?.let { getDateFromLong(it) }
            dateOfTTOrTd2.value = it.tt2?.let { getDateFromLong(it) }
            dateOfTTOrTdBooster.value = it.ttBooster?.let { getDateFromLong(it) }
            numFolicAcidTabGiven.value = it.numFolicAcidTabGiven.toString()
            numIfaAcidTabGiven.value = it.numIfaAcidTabGiven.toString()
            anyHighRisk.value =
                if (it.anyHighRisk) anyHighRisk.entries!!.last() else anyHighRisk.entries!!.first()
            if (it.anyHighRisk) {
                highRiskCondition.value = highRiskCondition.getStringFromPosition(it.highRiskId)
                list.add(list.indexOf(anyHighRisk) + 1, highRiskCondition)
                if (highRiskCondition.value == highRiskCondition.entries!!.last()) {
                    otherHighRiskCondition.value = it.otherHighRisk
                    list.add(list.indexOf(highRiskCondition) + 1, otherHighRiskCondition)
                }
            }
            highRiskReferralFacility.value =
                highRiskReferralFacility.getStringFromPosition(it.referralFacilityId)
            hrpConfirm.value =
                it.hrpConfirmed?.let { if (it) hrpConfirm.entries!!.last() else hrpConfirm.entries!!.first() }
            if (it.hrpConfirmed == true) {
                hrpConfirmedBy.value = hrpConfirmedBy.getStringFromPosition(it.hrpConfirmedById)
                list.add(list.indexOf(hrpConfirm) + 1, hrpConfirmedBy)
            }
            maternalDeath.value =
                if (it.maternalDeath) maternalDeath.entries!!.last() else maternalDeath.entries!!.first()
            if (it.maternalDeath) {
                maternalDeathProbableCause.value =
                    maternalDeathProbableCause.getStringFromPosition(it.maternalDeathProbableCauseId)
                maternalDateOfDeath.value = it.deathDate?.let { it1 -> getDateFromLong(it1) }
                list.addAll(
                    list.indexOf(maternalDeath) + 1,
                    listOf(maternalDeathProbableCause, maternalDateOfDeath)
                )
                otherMaternalDeathProbableCause.value = it.otherMaternalDeathProbableCause
                if (maternalDeathProbableCause.value == maternalDeathProbableCause.entries!!.last()) list.add(
                    list.indexOf(maternalDeathProbableCause) + 1, otherMaternalDeathProbableCause
                )
            }
            deliveryDone.value =
                if (it.pregnantWomanDelivered == true) deliveryDone.entries!!.first() else deliveryDone.entries!!.last()
        }
        setUpPage(list)

    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            ancDate.id -> {
                ancDate.value?.let {
                    val long = getLongFromDate(it)
                    val weeks = getWeeksOfPregnancy(long, lmp)
                    if (weeks > 22) {
                        triggerDependants(
                            source = maternalDeath,
                            addItems = listOf(deliveryDone),
                            removeItems = emptyList(),
                        )
                    } else {
                        triggerDependants(
                            source = maternalDeath,
                            addItems = emptyList(),
                            removeItems = listOf(deliveryDone),
                        )
                    }
                    weekOfPregnancy.value = weeks.toString()
                }
                -1
            }

            isAborted.id -> triggerDependants(
                source = isAborted,
                passedIndex = index,
                triggerIndex = 1,
                target = listOf(abortionType, abortionDate),
                targetSideEffect = listOf(abortionFacility)
            )

            abortionType.id -> triggerDependants(
                source = abortionType,
                passedIndex = index,
                triggerIndex = 0,
                target = abortionFacility,
            )

            weight.id -> validateIntMinMax(weight)
            bpSystolic.id, bpDiastolic.id -> {
                validateIntMinMax(bpSystolic)
                validateIntMinMax(bpDiastolic)
                if (bpSystolic.value?.isNotEmpty() == true || bpDiastolic.value?.isNotEmpty() == true) {
                    validateEmptyOnEditText(bpSystolic)
                    validateEmptyOnEditText(bpDiastolic)
                }
                -1
            }

            pulseRate.id -> {
                validateAllAlphabetsSpaceOnEditText(pulseRate)
            }

            hb.id -> {
                validateDoubleMinMax(hb)
            }

            numFolicAcidTabGiven.id -> validateIntMinMax(numFolicAcidTabGiven)
            numIfaAcidTabGiven.id -> validateIntMinMax(numIfaAcidTabGiven)
            anyHighRisk.id -> triggerDependants(
                source = anyHighRisk,
                passedIndex = index,
                triggerIndex = 1,
                target = highRiskCondition,
                targetSideEffect = listOf(otherHighRiskCondition)
            )

            highRiskCondition.id -> triggerDependants(
                source = highRiskCondition,
                passedIndex = index,
                triggerIndex = highRiskCondition.entries!!.lastIndex,
                target = otherHighRiskCondition,
            )

            otherHighRiskCondition.id -> {
                validateEmptyOnEditText(otherHighRiskCondition)
                validateAllAlphabetsSpaceOnEditText(otherHighRiskCondition)
            }

            hrpConfirm.id -> triggerDependants(
                source = hrpConfirm,
                passedIndex = index,
                triggerIndex = 1,
                target = hrpConfirmedBy,
            )

            maternalDeath.id -> triggerDependants(
                source = maternalDeath,
                passedIndex = index,
                triggerIndex = 1,
                target = listOf(maternalDeathProbableCause, maternalDateOfDeath),
                targetSideEffect = listOf(otherMaternalDeathProbableCause)
            )

            maternalDeathProbableCause.id -> triggerDependants(
                source = maternalDeathProbableCause,
                passedIndex = index,
                triggerIndex = maternalDeathProbableCause.entries!!.lastIndex,
                target = otherMaternalDeathProbableCause,
            )

            otherMaternalDeathProbableCause.id -> {
                validateEmptyOnEditText(otherMaternalDeathProbableCause)
                validateAllAlphabetsSpaceOnEditText(otherMaternalDeathProbableCause)
            }

            else -> -1
        }

    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PregnantWomanAncCache).let { cache ->
            cache.ancDate = getLongFromDate(ancDate.value)
            cache.isAborted = isAborted.value == isAborted.entries!!.last()
            cache.abortionType = abortionType.value
            cache.abortionTypeId = abortionType.getPosition()
            cache.abortionFacility = abortionFacility.value
            cache.abortionFacilityId = abortionFacility.getPosition()
            cache.abortionDate = abortionDate.value?.let { getLongFromDate(it) }
            cache.weight = weight.value?.toInt()
            cache.bpSystolic = bpSystolic.value?.toInt()
            cache.bpDiastolic = bpDiastolic.value?.toInt()
            cache.pulseRate = pulseRate.value
            cache.hb = hb.value?.toDouble()
            cache.fundalHeight = fundalHeight.value?.toInt()
            cache.urineAlbumin = urineAlbumin.value
            cache.urineAlbuminId = urineAlbumin.getPosition()
            cache.randomBloodSugarTest = randomBloodSugarTest.value
            cache.randomBloodSugarTestId = randomBloodSugarTest.getPosition()
            cache.tt1 = dateOfTTOrTd1.value?.let { getLongFromDate(it) }
            cache.tt2 = dateOfTTOrTd2.value?.let { getLongFromDate(it) }
            cache.ttBooster = dateOfTTOrTdBooster.value?.let { getLongFromDate(it) }
            cache.numFolicAcidTabGiven = numFolicAcidTabGiven.value?.toInt() ?: 0
            cache.numIfaAcidTabGiven = numFolicAcidTabGiven.value?.toInt() ?: 0
            cache.anyHighRisk = anyHighRisk.value == anyHighRisk.entries!!.last()
            cache.highRisk = highRiskCondition.value
            cache.highRiskId = highRiskCondition.getPosition()
            cache.otherHighRisk = otherHighRiskCondition.value
            cache.referralFacility = highRiskReferralFacility.value
            cache.referralFacilityId = highRiskReferralFacility.getPosition()
            cache.hrpConfirmed = hrpConfirm.value?.let { it == hrpConfirm.entries!!.last() }
            cache.hrpConfirmedBy = hrpConfirmedBy.value
            cache.hrpConfirmedById = hrpConfirmedBy.getPosition()
            cache.maternalDeath = maternalDeath.value == maternalDeath.entries!!.last()
            cache.maternalDeathProbableCause = maternalDeathProbableCause.value
            cache.maternalDeathProbableCauseId = maternalDeathProbableCause.getPosition()
            cache.otherMaternalDeathProbableCause = otherMaternalDeathProbableCause.value
            cache.deathDate = maternalDateOfDeath.value?.let { getLongFromDate(it) }
            cache.pregnantWomanDelivered =
                deliveryDone.value?.let { it == deliveryDone.entries!!.first() }
        }
    }

    fun getWeeksOfPregnancy(): Int = getIndexById(weekOfPregnancy.id)
    fun updateBenRecordForDelivered(it: BenRegCache) {
        it.genDetails?.apply {
            reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[2]
            reproductiveStatusId = 3
        }
        it.processed = "U"
    }
}
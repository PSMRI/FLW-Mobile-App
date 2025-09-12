package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.getWeeksOfPregnancy
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import java.util.concurrent.TimeUnit

class InfantRegistrationDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var babyName = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.ir_baby_name),
        required = false,
        hasDependants = false
    )

    private var infantTerm = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.ir_infant_term),
        entries = arrayOf("Full Term", "Pre Term"),
        required = false,
        hasDependants = false
    )

    private var corticosteroidGiven = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_corticosteroid_given),
        entries = arrayOf("Yes", "No", "Don't Know"),
        required = false,
        hasDependants = false
    )

    private var gender = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.ir_gender),
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private var babyCriedAtBirth = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_baby_cried_at_birth),
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )

    private var resuscitation = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_resuscitation),
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private var referred = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_referred),
        entries = arrayOf("Yes", "No", "NA"),
        required = false,
        hasDependants = false
    )

    private var hadBirthDefect = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_had_birth_defect),
        entries = arrayOf("Yes", "No", "NA"),
        required = false,
        hasDependants = true
    )

    private var birthDefect = FormElement(
        id = 9, inputType = InputType.DROPDOWN, title = resources.getString(R.string.ir_birth_defect), entries = arrayOf(
            "Cleft Lip / Cleft Palate",
            "Club Foot",
            "Down's Syndrome",
            "Hydrocephalus",
            "Imperforate Anus",
            "Neural Tube Defect (Spinal Bifida)",
            "Other"
        ), required = false, hasDependants = true
    )

    private var otherDefect = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.ir_other_defect),
        required = false,
        hasDependants = false,
    )

    private var weight = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.ir_weight),
        required = false,
        hasDependants = false,
        etMaxLength = 5,
        min = 1,
        max = 7000,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
    )

    private var breastFeedingStarted = FormElement(
        id = 12,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.ir_breast_feeding_started),
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = false,
    )

//    private val opv0Dose = FormElement(
//        id = 13,
//        inputType = InputType.DATE_PICKER,
//        title = "OPV0 Dose",
//        arrayId = -1,
//        required = false,
//        max = System.currentTimeMillis(),
//        hasDependants = true
//    )

//    private val bcgDose = FormElement(
//        id = 14,
//        inputType = InputType.DATE_PICKER,
//        title = "BCG Dose",
//        arrayId = -1,
//        required = false,
//        max = System.currentTimeMillis(),
//        hasDependants = true
//    )

//    private val hepBDose = FormElement(
//        id = 15,
//        inputType = InputType.DATE_PICKER,
//        title = "HEP B-0 Dose",
//        arrayId = -1,
//        required = false,
//        max = System.currentTimeMillis(),
//        hasDependants = true
//    )

//    private val vitkDose = FormElement(
//        id = 16,
//        inputType = InputType.DATE_PICKER,
//        title = "VITK Dose",
//        arrayId = -1,
//        required = false,
//        max = System.currentTimeMillis(),
//        hasDependants = true
//    )


    suspend fun setUpPage(
        ben: BenRegCache?,
        deliveryOutcomeCache: DeliveryOutcomeCache,
        babyIndex: Int,
        pwrCache: PregnantWomanRegistrationCache?,
        saved: InfantRegCache?
    ) {
        var list = mutableListOf(
            babyName, infantTerm, corticosteroidGiven, gender, babyCriedAtBirth,
//            resuscitation,
//            referred,
            hadBirthDefect,
//            birthDefect,
//            otherDefect,
            weight, breastFeedingStarted, //opv0Dose, bcgDose, hepBDose, vitkDose
        )
        if (deliveryOutcomeCache != null) {
     /*       opv0Dose.min = deliveryOutcomeCache.dateOfDelivery
            bcgDose.min = deliveryOutcomeCache.dateOfDelivery
            hepBDose.min = deliveryOutcomeCache.dateOfDelivery
            vitkDose.min = deliveryOutcomeCache.dateOfDelivery
            deliveryOutcomeCache.dateOfDelivery.let {
                if (it != null) {
                    if (it + TimeUnit.DAYS.toMillis(15) < System.currentTimeMillis()) opv0Dose.max =
                        it + TimeUnit.DAYS.toMillis(15)
                    if (it + TimeUnit.DAYS.toMillis(365) < System.currentTimeMillis()) bcgDose.max =
                        it + TimeUnit.DAYS.toMillis(365)
                    if (it + 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                        hepBDose.max = it + TimeUnit.DAYS.toMillis(


                            1
                        )
                        vitkDose.max = it + TimeUnit.DAYS.toMillis(1)
                    }
                }
            }*/
        }
        if (pwrCache != null && deliveryOutcomeCache != null) {
            val weeksOfPregnancy = deliveryOutcomeCache.dateOfDelivery?.let {
                getWeeksOfPregnancy(
                    it, pwrCache.lmpDate
                )
            }
            if (weeksOfPregnancy in 38..41) {
                infantTerm.value = infantTerm.entries!!.first()
            } else if (weeksOfPregnancy != null) {
                if (weeksOfPregnancy <= 37) infantTerm.value = infantTerm.entries!!.last()
            }
        }
        if (saved == null) {
            if (ben != null) {
                babyName.value =
                    if (deliveryOutcomeCache.liveBirth == null || deliveryOutcomeCache.liveBirth == 1) "baby of ${ben.firstName}"
                    else "baby $babyIndex of ${ben.firstName}"
            }
//            opv0Dose.value = getDateFromLong(System.currentTimeMillis())
        } else {
            list = mutableListOf(
                babyName,
                infantTerm,
                corticosteroidGiven,
                gender,
                babyCriedAtBirth,
                resuscitation,
                referred,
                hadBirthDefect,
                birthDefect,
                otherDefect,
                weight,
                breastFeedingStarted,
         /*       opv0Dose,
                bcgDose,
                hepBDose,
                vitkDose*/
            )
            babyName.value = saved.babyName
            infantTerm.value = saved.infantTerm
            corticosteroidGiven.value = saved.corticosteroidGiven
            gender.value = saved.gender?.let { gender.entries?.get(it.ordinal) }
            babyCriedAtBirth.value = if (saved.babyCriedAtBirth == true) "Yes" else "No"
            resuscitation.value = if (saved.resuscitation == true) "Yes" else "No"
            referred.value = saved.referred
            hadBirthDefect.value = saved.hadBirthDefect
            birthDefect.value = saved.birthDefect
            otherDefect.value = saved.otherDefect
            weight.value = saved.weight.toString()
            breastFeedingStarted.value = if (saved.breastFeedingStarted == true) "Yes" else "No"
//            opv0Dose.value = saved.opv0Dose?.let { getDateFromLong(it) }
//            bcgDose.value = saved.bcgDose?.let { getDateFromLong(it) }
//            hepBDose.value = saved.hepBDose?.let { getDateFromLong(it) }
//            vitkDose.value = saved.vitkDose?.let { getDateFromLong(it) }
        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            babyCriedAtBirth.id -> {
                triggerDependants(
                    source = babyCriedAtBirth,
                    passedIndex = index,
                    triggerIndex = 1,
                    target = resuscitation
                )
            }

            hadBirthDefect.id -> {
                triggerDependants(
                    source = hadBirthDefect,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = birthDefect,
                    targetSideEffect = listOf(otherDefect, birthDefect)
                )
            }

            birthDefect.id -> {
                triggerDependants(
                    source = birthDefect,
                    passedIndex = index,
                    triggerIndex = 6,
                    target = otherDefect
                )
            }

            otherDefect.id -> {
                validateAllAlphabetsSpecialOnEditText(otherDefect)
            }

            weight.id -> {
                validateWeightOnEditText(weight)

            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as InfantRegCache).let { form ->
            form.babyName = babyName.value
            form.infantTerm = infantTerm.value
            form.corticosteroidGiven = corticosteroidGiven.value
            form.gender = gender.value?.let {
                Gender.values()[gender.getPosition() - 1]
            }
            form.babyCriedAtBirth = babyCriedAtBirth.value == "Yes"
            form.resuscitation = resuscitation.value == "Yes"
            form.referred = referred.value
            form.hadBirthDefect = hadBirthDefect.value
            form.birthDefect = birthDefect.value
            form.otherDefect = otherDefect.value
            form.weight = weight.value?.toDouble()
            form.breastFeedingStarted = breastFeedingStarted.value == "Yes"
           /* form.opv0Dose = getLongFromDate(opv0Dose.value)
            form.bcgDose = getLongFromDate(bcgDose.value)
            form.hepBDose = getLongFromDate(hepBDose.value)
            form.vitkDose = getLongFromDate(vitkDose.value)*/
        }
    }
}
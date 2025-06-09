package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.InputType
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HRPNonPregnantTrackDataset(
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

    private val anemia = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.visible_signs_of_anemia_as_per_appearance),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val ancLabel = FormElement(
        id = 3,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.for_clinical_assessment_to_be_filled_consulting_with_anm),
        subtitle = resources.getString(R.string.to_be_filled_consulting_with_anm),
        required = false
    )

    private val hypertension = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.hypertension),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val diabetes = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.diabetes),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val severeAnemia = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.severe_anemia),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var pregLabel = FormElement(
        id = 7,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.current_fp_pregnancy_status),
        required = false
    )

    private val fp = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.adoption_of_family_planning),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var lmp = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.lmp),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val missedPeriod = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.missed_period),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private val isPregnant = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_pregnant),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private val riskStatus = FormElement(
        id = 12,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.risk_status),
        required = false,
        hasDependants = false
    )

    private val followUpLabel = FormElement(
        id = 13,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.follow_up_for_high_risk_conditions_in_the_non_pregnant_women),
        required = false
    )

    private val bpLabel = FormElement(
        id = 14,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.blood_pressure),
        required = false
    )

    private val systolic = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.systolic),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val diastolic = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.diastolic),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 4
    )

    private val bloodGlucoseTest = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.blood_glucose_test),
        entries = resources.getStringArray(R.array.sugar_test_types),
        required = false,
        hasDependants = true
    )

    private val rbg = FormElement(
        id = 18,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.random_blood_glucose),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val fbg = FormElement(
        id = 19,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.fasting_glucose_test),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val ppbg = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.post_prandial_glucose_test),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val hemoglobinTest = FormElement(
        id = 21,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.hemoglobin_test),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 4
    )

    private val ifaGiven = FormElement(
        id = 22,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.whether_ifa_supplement_provided),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val ifaQuantity = FormElement(
        id = 23,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.issued_quantity_ifa),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )


    private var lmpMinVar: Long? = null
    suspend fun setUpPage(
        ben: BenRegCache?,
        saved: HRPNonPregnantTrackCache?,
        lmpMin: Long?,
        dateOfVisitMin: Long?
    ) {
        val list = mutableListOf(
            followUpLabel,
            dateOfVisit,
            anemia,
            ancLabel,
            hypertension,
            bpLabel,
            systolic,
            diastolic,
            diabetes,
            bloodGlucoseTest,
            severeAnemia,
            hemoglobinTest,
            ifaGiven,
            riskStatus,
            pregLabel,
            fp,
            lmp,
            missedPeriod,
            isPregnant
        )

        saved?.let {
            dateOfVisit.value = it.visitDate?.let { it1 -> getDateFromLong(it1) }
            anemia.value = getLocalValueInArray(R.array.yes_no, it.anemia)
            hypertension.value = getLocalValueInArray(R.array.yes_no, it.hypertension)
            systolic.value = it.systolic?.toString()
            diastolic.value = it.diastolic?.toString()
            diabetes.value = getLocalValueInArray(R.array.yes_no, it.diabetes)
            bloodGlucoseTest.value =
                getLocalValueInArray(R.array.sugar_test_types, it.bloodGlucoseTest)
            if (bloodGlucoseTest.value == resources.getStringArray(R.array.sugar_test_types)[0]) {
                list.add(list.indexOf(bloodGlucoseTest) + 1, rbg)
                rbg.value = it.rbg?.toString()
            } else if (bloodGlucoseTest.value == resources.getStringArray(R.array.sugar_test_types)[1]) {
                list.add(list.indexOf(bloodGlucoseTest) + 1, fbg)
                list.add(list.indexOf(fbg) + 1, ppbg)
                fbg.value = it.fbg?.toString()
                ppbg.value = it.ppbg?.toString()
            }
            severeAnemia.value = getLocalValueInArray(R.array.yes_no, it.severeAnemia)
            hemoglobinTest.value = it.hemoglobinTest
            ifaGiven.value = getLocalValueInArray(R.array.yes_no, it.ifaGiven)
            if (ifaGiven.value == resources.getStringArray(R.array.yes_no)[0]) {
                list.add(list.indexOf(ifaGiven) + 1, ifaQuantity)
                ifaQuantity.value = it.ifaQuantity?.toString()
            }
            fp.value = getLocalValueInArray(R.array.yes_no, it.fp)
            lmp.value = it.lmp?.let { it2 -> getDateFromLong(it2) }
            missedPeriod.value = getLocalValueInArray(R.array.yes_no, it.missedPeriod)
            isPregnant.value = getLocalValueInArray(R.array.yes_no, it.isPregnant)

            anemia.showHighRisk = anemia.value == resources.getStringArray(R.array.yes_no)[0]

            ancLabel.showHighRisk =
                (hypertension.value == resources.getStringArray(R.array.yes_no)[0]
                        || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(
                    R.array.yes_no
                )[0])

            riskStatus.showHighRisk =
                (anemia.value == resources.getStringArray(R.array.yes_no)[0] || hypertension.value == resources.getStringArray(
                    R.array.yes_no
                )[0]
                        || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(
                    R.array.yes_no
                )[0])
        }

        lmp.min = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40)
        lmpMin?.let {
            lmpMinVar = lmpMin
            if (it > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40)) {
                lmp.min = lmpMin
            }
        }

        ben?.let {
            dateOfVisit.min = it.regDate
            dateOfVisitMin?.let { dov ->
                val calReg = Calendar.getInstance().setToStartOfTheDay()
                calReg.timeInMillis = it.regDate
                calReg.setToStartOfTheDay()
                val calMaxVisit = Calendar.getInstance()
                calMaxVisit.timeInMillis = dov
                calMaxVisit.setToStartOfTheDay()

                if ((calMaxVisit.timeInMillis - calReg.timeInMillis) >= 0) {
                    dateOfVisit.min = minOf(
                        calMaxVisit.timeInMillis + TimeUnit.DAYS.toMillis(1),
                        System.currentTimeMillis()
                    )
                }
            }
            dateOfVisit.max = System.currentTimeMillis()
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            anemia.id -> {
                anemia.showHighRisk = anemia.value == resources.getStringArray(R.array.yes_no)[0]
                riskStatus.showHighRisk =
                    (anemia.value == resources.getStringArray(R.array.yes_no)[0] || hypertension.value == resources.getStringArray(
                        R.array.yes_no
                    )[0]
                            || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(
                        R.array.yes_no
                    )[0])
                -1
            }

            hypertension.id, diabetes.id, severeAnemia.id -> {
                ancLabel.showHighRisk =
                    (hypertension.value == resources.getStringArray(R.array.yes_no)[0]
                            || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(
                        R.array.yes_no
                    )[0])
                riskStatus.showHighRisk =
                    (anemia.value == resources.getStringArray(R.array.yes_no)[0] || hypertension.value == resources.getStringArray(
                        R.array.yes_no
                    )[0]
                            || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(
                        R.array.yes_no
                    )[0])
                -1
            }

            dateOfVisit.id -> {
                lmp.min = getLongFromDate(dateOfVisit.value) - TimeUnit.DAYS.toMillis(40)
                lmpMinVar?.let {
                    if (it > getLongFromDate(dateOfVisit.value) - TimeUnit.DAYS.toMillis(40)) {
                        lmp.min = it
                    }
                }
                lmp.value = null
                lmp.max = getLongFromDate(dateOfVisit.value)
                -1
            }

            bloodGlucoseTest.id -> {
                if (bloodGlucoseTest.value == resources.getStringArray(R.array.sugar_test_types)[0]) {
                    triggerDependants(
                        source = bloodGlucoseTest,
                        addItems = listOf(
                            rbg
                        ),
                        removeItems = listOf(
                            fbg, ppbg
                        )
                    )
                } else {
                    triggerDependants(
                        source = bloodGlucoseTest,
                        addItems = listOf(
                            fbg, ppbg
                        ),
                        removeItems = listOf(
                            rbg
                        )
                    )
                }
                1
            }

            ifaGiven.id -> {
                if (ifaGiven.value == resources.getStringArray(R.array.yes_no)[0]) {
                    triggerDependants(
                        source = ifaGiven,
                        addItems = listOf(ifaQuantity),
                        removeItems = listOf()
                    )
                } else if (ifaGiven.value == resources.getStringArray(R.array.yes_no)[1]) {
                    triggerDependants(
                        source = ifaGiven,
                        addItems = listOf(),
                        removeItems = listOf(ifaQuantity)
                    )
                }
                1
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPNonPregnantTrackCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.anemia = getEnglishValueInArray(R.array.yes_no, anemia.value)
            form.hypertension = getEnglishValueInArray(R.array.yes_no, hypertension.value)
            form.systolic = systolic.value?.toInt()
            form.diastolic = diastolic.value?.toInt()
            form.diabetes = getEnglishValueInArray(R.array.yes_no, diabetes.value)
            form.bloodGlucoseTest =
                getEnglishValueInArray(R.array.sugar_test_types, bloodGlucoseTest.value)
            form.rbg = rbg.value?.toInt()
            form.fbg = fbg.value?.toInt()
            form.ppbg = ppbg.value?.toInt()
            form.severeAnemia = getEnglishValueInArray(R.array.yes_no, severeAnemia.value)
            form.hemoglobinTest = hemoglobinTest.value
            form.ifaGiven = getEnglishValueInArray(R.array.yes_no, ifaGiven.value)
            form.ifaQuantity = ifaQuantity.value?.toInt()
            form.fp = getEnglishValueInArray(R.array.yes_no, fp.value)
            form.lmp = getLongFromDate(lmp.value)
            form.missedPeriod = getEnglishValueInArray(R.array.yes_no, missedPeriod.value)
            form.isPregnant = getEnglishValueInArray(R.array.yes_no, isPregnant.value)
            Timber.d("Form $form")
        }
    }

    fun getIndexOfAncLabel() = getIndexById(ancLabel.id)

    fun getIndexOfAnemia() = getIndexById(anemia.id)

    fun getIndexOfRisk() = getIndexById(riskStatus.id)

    fun getIndexOfLmp() = getIndexById(lmp.id)

    fun getIndexOfRbg() = getIndexById(rbg.id)
    fun getIndexOfFbg() = getIndexById(fbg.id)
    fun getIndexOfPpbg() = getIndexById(ppbg.id)
    fun getIndexOfIfaQuantity() = getIndexById(ifaQuantity.id)

    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[1]
            it.reproductiveStatusId = 2
            it.lastMenstrualPeriod = getLongFromDate(lmp.value)
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
        benRegCache.syncState = SyncState.UNSYNCED
    }

    fun updateAssess(hrpPregnantAssessCache: HRPPregnantAssessCache) {
        hrpPregnantAssessCache.lmpDate = getLongFromDate(lmp.value)
        hrpPregnantAssessCache.edd = getLongFromDate(lmp.value) + TimeUnit.DAYS.toMillis(280)
        hrpPregnantAssessCache.syncState = SyncState.UNSYNCED
    }
}
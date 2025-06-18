package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.model.InputType
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HRPPregnantTrackDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val rdPmsa = FormElement(
        id = 1,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.rd_pmsa),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val severeAnemia = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.severe_anemia),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val pregInducedHypertension = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.pregnancy_induced_hypertension),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val gestDiabetesMellitus = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.gestational_diabetes_mellitus),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val hypothyroidism = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.hypothyrodism),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val polyhydromnios = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.polyhydromnios),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val oligohydromnios = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.oligohydromnios),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val antepartumHem = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.antepartum_hemorrhage),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val malPresentation = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.mal_presentation),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val hivsyph = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.hiv_syphilis_hep_b),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private var dateOfVisit = FormElement(
        id = 11,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val followUpLabel = FormElement(
        id = 12,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.follow_up_for_high_risk_conditions_in_the_pregnant_women),
        required = false
    )

    private val rdDengue = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.rd_dengue),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val rdFilaria = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.rd_filaria),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val bpLabel = FormElement(
        id = 15,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.blood_pressure),
        required = false
    )

    private val systolic = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.systolic),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val diastolic = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.diastolic),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 4
    )

    private val bloodGlucoseTest = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.blood_glucose_test),
        entries = resources.getStringArray(R.array.sugar_test_preg_types),
        required = false,
        hasDependants = true
    )

    private val rbg = FormElement(
        id = 19,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.random_blood_glucose),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val fbg = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.fasting_glucose_test),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val ppbg = FormElement(
        id = 21,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.post_prandial_glucose_test),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val usingOgttLabel = FormElement(
        id = 22,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.using_75gm_ogtt),
        required = true
    )

    private val fastingGlucose = FormElement(
        id = 23,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.fasting_glucose_test),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val after2hrs = FormElement(
        id = 24,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.after_2hrs),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val hemoglobinTest = FormElement(
        id = 25,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.hemoglobin_test),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 4
    )

    private val ifaGiven = FormElement(
        id = 26,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.whether_ifa_supplement_or_folic_acid_provided),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val ifaQuantity = FormElement(
        id = 27,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.issued_quantity_ifa_or_folic_acid),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = 3
    )

    private val highRiskLabel = FormElement(
        id = 28,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.high_risk_conditions),
        required = false
    )

    private val visit = FormElement(
        id = 29,
        inputType = InputType.HEADLINE,
        title = "",
        required = false
    )

    suspend fun setUpPage(
        ben: BenRegCache?,
        visitStr: String?,
        saved: HRPPregnantTrackCache?,
        dateOfVisitMin: Long?
    ) {
        val list = mutableListOf(
            followUpLabel,
            visit,
            highRiskLabel,
            dateOfVisit,
            rdPmsa,
            rdDengue,
            rdFilaria,
            severeAnemia,
            hemoglobinTest,
            ifaGiven,
            pregInducedHypertension,
            bpLabel,
            systolic,
            diastolic,
            gestDiabetesMellitus,
            bloodGlucoseTest,
            hypothyroidism,
            polyhydromnios,
            oligohydromnios,
            antepartumHem,
            malPresentation,
            hivsyph
        )

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

        visitStr?.let {
            followUpLabel.title += it
            visit.title = it
        }
        saved?.let {
            dateOfVisit.value = it.visitDate?.let { it1 -> getDateFromLong(it1) }
            rdPmsa.value = getLocalValueInArray(R.array.yes_no, it.rdPmsa)
            rdPmsa.showHighRisk = it.rdPmsa == englishResources.getStringArray(R.array.yes_no)[0]

            rdDengue.value = getLocalValueInArray(R.array.yes_no, it.rdDengue)
            rdDengue.showHighRisk =
                it.rdDengue == englishResources.getStringArray(R.array.yes_no)[0]

            rdFilaria.value = getLocalValueInArray(R.array.yes_no, it.rdFilaria)
            rdFilaria.showHighRisk =
                it.rdFilaria == englishResources.getStringArray(R.array.yes_no)[0]

            severeAnemia.value = getLocalValueInArray(R.array.yes_no, it.severeAnemia)
            severeAnemia.showHighRisk =
                it.severeAnemia == englishResources.getStringArray(R.array.yes_no)[0]

            hemoglobinTest.value = it.hemoglobinTest
            ifaGiven.value = getLocalValueInArray(R.array.yes_no, it.ifaGiven)
            if (ifaGiven.value == resources.getStringArray(R.array.yes_no)[0]) {
                list.add(list.indexOf(ifaGiven) + 1, ifaQuantity)
                ifaQuantity.value = it.ifaQuantity?.toString()
            }
            pregInducedHypertension.value =
                getLocalValueInArray(R.array.yes_no, it.pregInducedHypertension)
            pregInducedHypertension.showHighRisk =
                it.pregInducedHypertension == englishResources.getStringArray(R.array.yes_no)[0]
            systolic.value = it.systolic?.toString()
            diastolic.value = it.diastolic?.toString()

            gestDiabetesMellitus.value =
                getLocalValueInArray(R.array.yes_no, it.gestDiabetesMellitus)
            gestDiabetesMellitus.showHighRisk =
                it.gestDiabetesMellitus == englishResources.getStringArray(R.array.yes_no)[0]

            bloodGlucoseTest.value =
                getLocalValueInArray(R.array.sugar_test_preg_types, it.bloodGlucoseTest)
            when (bloodGlucoseTest.value) {
                resources.getStringArray(R.array.sugar_test_preg_types)[0] -> {
                    list.add(list.indexOf(bloodGlucoseTest) + 1, rbg)
                    rbg.value = it.rbg?.toString()
                }

                resources.getStringArray(R.array.sugar_test_preg_types)[1] -> {
                    list.add(list.indexOf(bloodGlucoseTest) + 1, fbg)
                    list.add(list.indexOf(fbg) + 1, ppbg)
                    fbg.value = it.fbg?.toString()
                    ppbg.value = it.ppbg?.toString()
                }

                resources.getStringArray(R.array.sugar_test_preg_types)[2] -> {
                    list.add(list.indexOf(bloodGlucoseTest) + 1, usingOgttLabel)
                    list.add(list.indexOf(usingOgttLabel) + 1, fastingGlucose)
                    list.add(list.indexOf(fastingGlucose) + 1, after2hrs)
                    fastingGlucose.value = it.fastingOgtt?.toString()
                    after2hrs.value = it.after2hrsOgtt?.toString()
                }

                else -> {
                    // not expected
                }
            }
            hypothyroidism.value = getLocalValueInArray(R.array.yes_no, it.hypothyrodism)
            hypothyroidism.showHighRisk =
                it.hypothyrodism == englishResources.getStringArray(R.array.yes_no)[0]

            polyhydromnios.value = getLocalValueInArray(R.array.yes_no, it.polyhydromnios)
            polyhydromnios.showHighRisk =
                it.polyhydromnios == englishResources.getStringArray(R.array.yes_no)[0]

            oligohydromnios.value = getLocalValueInArray(R.array.yes_no, it.oligohydromnios)
            oligohydromnios.showHighRisk =
                it.oligohydromnios == englishResources.getStringArray(R.array.yes_no)[0]

            antepartumHem.value = getLocalValueInArray(R.array.yes_no, it.antepartumHem)
            antepartumHem.showHighRisk =
                it.antepartumHem == englishResources.getStringArray(R.array.yes_no)[0]

            malPresentation.value = getLocalValueInArray(R.array.yes_no, it.malPresentation)
            malPresentation.showHighRisk =
                it.malPresentation == englishResources.getStringArray(R.array.yes_no)[0]

            hivsyph.value = getLocalValueInArray(R.array.yes_no, it.hivsyph)
            hivsyph.showHighRisk = it.hivsyph == englishResources.getStringArray(R.array.yes_no)[0]
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            rdPmsa.id -> {
                rdPmsa.showHighRisk = rdPmsa.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            rdDengue.id -> {
                rdDengue.showHighRisk =
                    rdDengue.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            rdFilaria.id -> {
                rdFilaria.showHighRisk =
                    rdFilaria.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            severeAnemia.id -> {
                severeAnemia.showHighRisk =
                    severeAnemia.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            pregInducedHypertension.id -> {
                pregInducedHypertension.showHighRisk =
                    pregInducedHypertension.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            gestDiabetesMellitus.id -> {
                gestDiabetesMellitus.showHighRisk =
                    gestDiabetesMellitus.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            hypothyroidism.id -> {
                hypothyroidism.showHighRisk =
                    hypothyroidism.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            polyhydromnios.id -> {
                polyhydromnios.showHighRisk =
                    polyhydromnios.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            oligohydromnios.id -> {
                oligohydromnios.showHighRisk =
                    oligohydromnios.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            antepartumHem.id -> {
                antepartumHem.showHighRisk =
                    antepartumHem.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            malPresentation.id -> {
                malPresentation.showHighRisk =
                    malPresentation.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            hivsyph.id -> {
                hivsyph.showHighRisk = hivsyph.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            bloodGlucoseTest.id -> {
                when (bloodGlucoseTest.value) {
                    resources.getStringArray(R.array.sugar_test_preg_types)[0] -> {
                        triggerDependants(
                            source = bloodGlucoseTest,
                            addItems = listOf(
                                rbg
                            ),
                            removeItems = listOf(
                                fbg, ppbg, usingOgttLabel, fastingGlucose, after2hrs
                            )
                        )
                    }

                    resources.getStringArray(R.array.sugar_test_preg_types)[1] -> {
                        triggerDependants(
                            source = bloodGlucoseTest,
                            addItems = listOf(
                                fbg, ppbg
                            ),
                            removeItems = listOf(
                                rbg, usingOgttLabel, fastingGlucose, after2hrs
                            )
                        )
                    }

                    resources.getStringArray(R.array.sugar_test_preg_types)[2] -> {
                        triggerDependants(
                            source = bloodGlucoseTest,
                            addItems = listOf(
                                usingOgttLabel, fastingGlucose, after2hrs
                            ),
                            removeItems = listOf(
                                rbg, fbg, ppbg
                            )
                        )
                    }
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

            systolic.id -> {
                validateInput(systolic)
                1
            }
            diastolic.id -> {
                validateInput(diastolic)
                1
            }
            hemoglobinTest.id->{
                validateInput(diastolic)
                1
            }

            else -> -1
        }
    }


    private fun validateInput(formElement: FormElement): Int {
        formElement.errorText = if (formElement.value.isNullOrEmpty()){
            resources.getString(
                R.string.form_input_empty_error
            )

        }else{
            null
        }

        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPPregnantTrackCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.rdPmsa = getEnglishValueInArray(R.array.yes_no, rdPmsa.value)
            form.rdDengue = getEnglishValueInArray(R.array.yes_no, rdDengue.value)
            form.rdFilaria = getEnglishValueInArray(R.array.yes_no, rdFilaria.value)
            form.severeAnemia = getEnglishValueInArray(R.array.yes_no, severeAnemia.value)
            form.hemoglobinTest = hemoglobinTest.value
            form.ifaGiven = getEnglishValueInArray(R.array.yes_no, ifaGiven.value)
            form.ifaQuantity = ifaQuantity.value?.toInt()
            form.pregInducedHypertension =
                getEnglishValueInArray(R.array.yes_no, pregInducedHypertension.value)
            form.systolic = systolic.value?.toInt()
            form.diastolic = diastolic.value?.toInt()
            form.gestDiabetesMellitus =
                getEnglishValueInArray(R.array.yes_no, gestDiabetesMellitus.value)
            form.bloodGlucoseTest =
                getEnglishValueInArray(R.array.sugar_test_preg_types, bloodGlucoseTest.value)
            form.rbg = rbg.value?.toInt()
            form.fbg = fbg.value?.toInt()
            form.ppbg = ppbg.value?.toInt()
            form.fastingOgtt = fastingGlucose.value?.toInt()
            form.after2hrsOgtt = after2hrs.value?.toInt()
            form.hypothyrodism = getEnglishValueInArray(R.array.yes_no, hypothyroidism.value)
            form.polyhydromnios = getEnglishValueInArray(R.array.yes_no, polyhydromnios.value)
            form.oligohydromnios = getEnglishValueInArray(R.array.yes_no, oligohydromnios.value)
            form.antepartumHem = getEnglishValueInArray(R.array.yes_no, antepartumHem.value)
            form.malPresentation = getEnglishValueInArray(R.array.yes_no, malPresentation.value)
            form.hivsyph = getEnglishValueInArray(R.array.yes_no, hivsyph.value)
        }
    }

    fun getIndexOfRdPmsa() = getIndexById(rdPmsa.id)
    fun getIndexOfRdDengue() = getIndexById(rdDengue.id)
    fun getIndexOfRdFilaria() = getIndexById(rdFilaria.id)
    fun getIndexOfSevereAnemia() = getIndexById(severeAnemia.id)
    fun getIndexOfPregInduced() = getIndexById(pregInducedHypertension.id)
    fun getIndexOfGest() = getIndexById(gestDiabetesMellitus.id)
    fun getIndexOfHypothyroidism() = getIndexById(hypothyroidism.id)
    fun getIndexOfPolyhydromnios() = getIndexById(polyhydromnios.id)
    fun getIndexOfOligohydromnios() = getIndexById(oligohydromnios.id)
    fun getIndexOfAntepartum() = getIndexById(antepartumHem.id)
    fun getIndexOfMalPre() = getIndexById(malPresentation.id)
    fun getIndexOfHiv() = getIndexById(hivsyph.id)

    fun getIndexOfRbg() = getIndexById(rbg.id)
    fun getIndexOfFbg() = getIndexById(fbg.id)
    fun getIndexOfPpbg() = getIndexById(ppbg.id)

    fun getIndexOfOgttLabel() = getIndexById(usingOgttLabel.id)
    fun getIndexOfFasting() = getIndexById(fastingGlucose.id)
    fun getIndexOfafter() = getIndexById(after2hrs.id)
    fun getIndexOfIfaQuantity() = getIndexById(ifaQuantity.id)
}
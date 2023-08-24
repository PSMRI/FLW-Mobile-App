package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HRPPregnantTrackCache
import org.piramalswasthya.sakhi.model.InputType
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
    suspend fun setUpPage(ben: BenRegCache?, saved: HRPPregnantTrackCache?, dateOfVisitMin: Long?) {
        val list = mutableListOf(
            followUpLabel,
            dateOfVisit,
            rdPmsa,
            severeAnemia,
            pregInducedHypertension,
            gestDiabetesMellitus,
            hypothyroidism,
            polyhydromnios,
            oligohydromnios,
            antepartumHem,
            malPresentation,
            hivsyph
        )
        ben?.let {
            dateOfVisit.min = it.regDate - TimeUnit.DAYS.toMillis(60)
            dateOfVisitMin?.let { dov ->
//                val cal = Calendar.getInstance()
//                cal.timeInMillis = dov
//                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1)
//                cal.set(Calendar.DAY_OF_MONTH, 1)
//                if (cal.timeInMillis > it.regDate) {
//                    dateOfVisit.min = cal.timeInMillis
//                }
                if (dov > it.regDate - TimeUnit.DAYS.toMillis(60)) dateOfVisit.min =
                    dov + TimeUnit.DAYS.toMillis(1)
            }
            dateOfVisit.max = System.currentTimeMillis()
        }


        saved?.let {
            dateOfVisit.value = it.visitDate?.let { it1 -> getDateFromLong(it1) }
            rdPmsa.value = getLocalValueInArray(R.array.yes_no, it.rdPmsa)
            rdPmsa.showHighRisk = it.rdPmsa == englishResources.getStringArray(R.array.yes_no)[0]

            severeAnemia.value = getLocalValueInArray(R.array.yes_no, it.severeAnemia)
            severeAnemia.showHighRisk = it.severeAnemia == englishResources.getStringArray(R.array.yes_no)[0]

            pregInducedHypertension.value = getLocalValueInArray(R.array.yes_no, it.pregInducedHypertension)
            pregInducedHypertension.showHighRisk = it.pregInducedHypertension == englishResources.getStringArray(R.array.yes_no)[0]

            gestDiabetesMellitus.value = getLocalValueInArray(R.array.yes_no, it.gestDiabetesMellitus)
            gestDiabetesMellitus.showHighRisk = it.gestDiabetesMellitus == englishResources.getStringArray(R.array.yes_no)[0]

            hypothyroidism.value = getLocalValueInArray(R.array.yes_no, it.hypothyrodism)
            hypothyroidism.showHighRisk = it.hypothyrodism == englishResources.getStringArray(R.array.yes_no)[0]

            polyhydromnios.value = getLocalValueInArray(R.array.yes_no, it.polyhydromnios)
            polyhydromnios.showHighRisk = it.polyhydromnios == englishResources.getStringArray(R.array.yes_no)[0]

            oligohydromnios.value = getLocalValueInArray(R.array.yes_no, it.oligohydromnios)
            oligohydromnios.showHighRisk = it.oligohydromnios == englishResources.getStringArray(R.array.yes_no)[0]

            antepartumHem.value = getLocalValueInArray(R.array.yes_no, it.antepartumHem)
            antepartumHem.showHighRisk = it.antepartumHem == englishResources.getStringArray(R.array.yes_no)[0]

            malPresentation.value = getLocalValueInArray(R.array.yes_no, it.malPresentation)
            malPresentation.showHighRisk = it.malPresentation == englishResources.getStringArray(R.array.yes_no)[0]

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

            severeAnemia.id -> {
                severeAnemia.showHighRisk = severeAnemia.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            pregInducedHypertension.id -> {
                pregInducedHypertension.showHighRisk = pregInducedHypertension.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            gestDiabetesMellitus.id -> {
                gestDiabetesMellitus.showHighRisk = gestDiabetesMellitus.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            hypothyroidism.id -> {
                hypothyroidism.showHighRisk = hypothyroidism.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            polyhydromnios.id -> {
                polyhydromnios.showHighRisk = polyhydromnios.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            oligohydromnios.id -> {
                oligohydromnios.showHighRisk = oligohydromnios.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            antepartumHem.id -> {
                antepartumHem.showHighRisk = antepartumHem.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            malPresentation.id -> {
                malPresentation.showHighRisk = malPresentation.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            hivsyph.id -> {
                hivsyph.showHighRisk = hivsyph.value == resources.getStringArray(R.array.yes_no)[0]
                -1
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPPregnantTrackCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.rdPmsa = getEnglishValueInArray(R.array.yes_no, rdPmsa.value)
            form.severeAnemia = getEnglishValueInArray(R.array.yes_no, severeAnemia.value)
            form.pregInducedHypertension = getEnglishValueInArray(R.array.yes_no, pregInducedHypertension.value)
            form.gestDiabetesMellitus = getEnglishValueInArray(R.array.yes_no, gestDiabetesMellitus.value)
            form.hypothyrodism = getEnglishValueInArray(R.array.yes_no, hypothyroidism.value)
            form.polyhydromnios = getEnglishValueInArray(R.array.yes_no, polyhydromnios.value)
            form.oligohydromnios = getEnglishValueInArray(R.array.yes_no, oligohydromnios.value)
            form.antepartumHem = getEnglishValueInArray(R.array.yes_no, antepartumHem.value)
            form.malPresentation = getEnglishValueInArray(R.array.yes_no, malPresentation.value)
            form.hivsyph = getEnglishValueInArray(R.array.yes_no, hivsyph.value)
        }
    }

}
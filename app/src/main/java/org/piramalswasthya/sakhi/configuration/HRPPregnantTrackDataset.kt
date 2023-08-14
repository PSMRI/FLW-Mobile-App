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
        title = "RD pmsa",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val severeAnemia = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = "Severe anemia",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val pregInducedHypertension = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = "Pregnancy induced hypertension",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val gestDiabetesMellitus = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Gestational diabetes mellitus",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val hypothyrodism = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Hypothyrodism",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val polyhydromnios = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Polyhydromnios",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val oligohydromnios = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Oligohydromnios",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val antepartumHem = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Antepartum hemorrhage",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val malPresentation = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = "Mal Presentation",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val hivsyph = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = "HIV/Syphilis/Hep B",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private var dateOfVisit = FormElement(
        id = 11,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: HRPPregnantTrackCache?, dateOfVisitMin: Long?) {
        val list = mutableListOf(
            dateOfVisit,
            rdPmsa,
            severeAnemia,
            pregInducedHypertension,
            gestDiabetesMellitus,
            hypothyrodism,
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
                if (dov > it.regDate - TimeUnit.DAYS.toMillis(60)) dateOfVisit.min = dov
            }
        }


        saved?.let {
            dateOfVisit.value = it.visitDate?.let { it1 -> getDateFromLong(it1) }
            rdPmsa.value = it.rdPmsa
            rdPmsa.showHighRisk = it.rdPmsa == "Yes"
            severeAnemia.value = it.severeAnemia
            severeAnemia.showHighRisk = it.severeAnemia == "Yes"
            pregInducedHypertension.value = it.pregInducedHypertension
            pregInducedHypertension.showHighRisk = it.pregInducedHypertension == "Yes"
            gestDiabetesMellitus.value = it.gestDiabetesMellitus
            gestDiabetesMellitus.showHighRisk = it.gestDiabetesMellitus == "Yes"
            hypothyrodism.value = it.hypothyrodism
            hypothyrodism.showHighRisk = it.hypothyrodism == "Yes"
            polyhydromnios.value = it.polyhydromnios
            polyhydromnios.showHighRisk = it.polyhydromnios == "Yes"
            oligohydromnios.value = it.oligohydromnios
            oligohydromnios.showHighRisk = it.oligohydromnios == "Yes"
            antepartumHem.value = it.antepartumHem
            antepartumHem.showHighRisk = it.antepartumHem == "Yes"
            malPresentation.value = it.malPresentation
            malPresentation.showHighRisk = it.malPresentation == "Yes"
            hivsyph.value = it.hivsyph
            hivsyph.showHighRisk = it.hivsyph == "Yes"
        }
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            rdPmsa.id -> {
                rdPmsa.showHighRisk = rdPmsa.value == "Yes"
                -1
            }

            severeAnemia.id -> {
                severeAnemia.showHighRisk = severeAnemia.value == "Yes"
                -1
            }

            pregInducedHypertension.id -> {
                pregInducedHypertension.showHighRisk = pregInducedHypertension.value == "Yes"
                -1
            }

            gestDiabetesMellitus.id -> {
                gestDiabetesMellitus.showHighRisk = gestDiabetesMellitus.value == "Yes"
                -1
            }

            hypothyrodism.id -> {
                hypothyrodism.showHighRisk = hypothyrodism.value == "Yes"
                -1
            }

            polyhydromnios.id -> {
                polyhydromnios.showHighRisk = polyhydromnios.value == "Yes"
                -1
            }

            oligohydromnios.id -> {
                oligohydromnios.showHighRisk = oligohydromnios.value == "Yes"
                -1
            }

            antepartumHem.id -> {
                antepartumHem.showHighRisk = antepartumHem.value == "Yes"
                -1
            }

            malPresentation.id -> {
                malPresentation.showHighRisk = malPresentation.value == "Yes"
                -1
            }

            hivsyph.id -> {
                hivsyph.showHighRisk = hivsyph.value == "Yes"
                -1
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPPregnantTrackCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.rdPmsa = rdPmsa.value
            form.severeAnemia = severeAnemia.value
            form.pregInducedHypertension = pregInducedHypertension.value
            form.gestDiabetesMellitus = gestDiabetesMellitus.value
            form.hypothyrodism = hypothyrodism.value
            form.polyhydromnios = polyhydromnios.value
            form.oligohydromnios = oligohydromnios.value
            form.antepartumHem = antepartumHem.value
            form.malPresentation = malPresentation.value
            form.hivsyph = hivsyph.value
        }
    }

}
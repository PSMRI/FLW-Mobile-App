package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class HRPNonPregnantTrackDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private val anemia = FormElement(
        id = 2,
        inputType = InputType.RADIO,
        title = "Visible signs of Anemia as per appearance",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val ancLabel = FormElement(
        id = 3,
        inputType = InputType.HEADLINE,
        title = "For Clinical Assessment (to be filled consulting with ANM)",
        required = false
    )

    private val hypertension = FormElement(
        id = 4,
        inputType = InputType.RADIO,
        title = "Hypertension",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val diabetes = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Diabetes",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private val severeAnemia = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = "Severe Anemia",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private var pregLabel = FormElement(
        id = 7,
        inputType = InputType.HEADLINE,
        title = "CURRENT FP/ PREGNANCY STATUS",
        required = false
    )

    private val fp = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Adoption of Family Planning",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = true
    )

    private var lmp = FormElement(
        id = 9,
        inputType = InputType.DATE_PICKER,
        title = "LMP",
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = false
    )

    private val missedPeriod = FormElement(
        id = 10,
        inputType = InputType.RADIO,
        title = "Missed Period",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private val isPregnant = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = "Is Pregnant",
        entries = arrayOf("Yes", "No"),
        required = true,
        hasDependants = false
    )

    private val riskStatus = FormElement(
        id = 12,
        inputType = InputType.HEADLINE,
        title = "RISK STATUS",
        required = false,
        hasDependants = false
    )

    private var lmpMinVar: Long? = null
    suspend fun setUpPage(ben: BenRegCache?, saved: HRPNonPregnantTrackCache?, lmpMin: Long?, dateOfVisitMin: Long?) {
        val list = mutableListOf(
            dateOfVisit,
            anemia,
            ancLabel,
            hypertension,
            diabetes,
            severeAnemia,
            riskStatus,
            pregLabel,
            fp,
            lmp,
            missedPeriod,
            isPregnant
        )

        saved?.let {
            dateOfVisit.value = it.visitDate?.let { it1 -> getDateFromLong(it1) }
            anemia.value = it.anemia
            hypertension.value = it.hypertension
            diabetes.value = it.diabetes
            severeAnemia.value = it.severeAnemia
            fp.value = it.fp
            lmp.value = it.lmp?.let { it2 -> getDateFromLong(it2) }
            missedPeriod.value = it.missedPeriod
            isPregnant.value = it.isPregnant

            anemia.showHighRisk = anemia.value == "Yes"

            ancLabel.showHighRisk = (hypertension.value == "Yes"
                    || diabetes.value == "Yes" || severeAnemia.value == "Yes")

            riskStatus.showHighRisk = (anemia.value == "Yes" || hypertension.value == "Yes"
                    || diabetes.value == "Yes" || severeAnemia.value == "Yes")
        }

        lmp.min = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40)
        lmpMin?.let {
            lmpMinVar = lmpMin
            if (it > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40)) {
                lmp.min = lmpMin
            }
        }

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
                if (dov > it.regDate - TimeUnit.DAYS.toMillis(60))  dateOfVisit.min = dov
            }
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            anemia.id -> {
                anemia.showHighRisk = anemia.value == "Yes"
                riskStatus.showHighRisk = (anemia.value == "Yes" || hypertension.value == "Yes"
                        || diabetes.value == "Yes" || severeAnemia.value == "Yes")
                -1
            }

            hypertension.id, diabetes.id, severeAnemia.id -> {
                ancLabel.showHighRisk = (hypertension.value == "Yes"
                        || diabetes.value == "Yes" || severeAnemia.value == "Yes")
                riskStatus.showHighRisk = (anemia.value == "Yes" || hypertension.value == "Yes"
                        || diabetes.value == "Yes" || severeAnemia.value == "Yes")
                -1
            }

            dateOfVisit.id -> {
                lmp.min = getLongFromDate(dateOfVisit.value) - TimeUnit.DAYS.toMillis(40)
                lmpMinVar?.let {
                    if (it > getLongFromDate(dateOfVisit.value) - TimeUnit.DAYS.toMillis(40)) {
                        lmp.min = it
                    }
                }
                lmp.max = getLongFromDate(dateOfVisit.value)
                -1
            }
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPNonPregnantTrackCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.anemia = anemia.value
            form.hypertension = hypertension.value
            form.diabetes = diabetes.value
            form.severeAnemia = severeAnemia.value
            form.fp = fp.value
            form.lmp = getLongFromDate(lmp.value)
            form.missedPeriod = missedPeriod.value
            form.isPregnant = isPregnant.value
            Timber.d("Form $form")
        }
    }

    fun getIndexOfAncLabel() = getIndexById(ancLabel.id)

    fun getIndexOfAnemia() = getIndexById(anemia.id)

    fun getIndexOfRisk() = getIndexById(riskStatus.id)

    fun getIndexOfLmp() = getIndexById(lmp.id)

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
}
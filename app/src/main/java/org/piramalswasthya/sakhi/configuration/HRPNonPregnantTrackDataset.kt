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
        title = resources.getString(R.string.visible_signs_of_anemia_as_per_appearance),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )

    private val ancLabel = FormElement(
        id = 3,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.for_clinical_assessment_to_be_filled_consulting_with_anm),
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
        title = context.getString(R.string.risk_status),
        required = false,
        hasDependants = false
    )

    private val followUpLabel = FormElement(
        id = 13,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.follow_up_for_high_risk_conditions_in_the_non_pregnant_women),
        required = false
    )
    private var lmpMinVar: Long? = null
    suspend fun setUpPage(ben: BenRegCache?, saved: HRPNonPregnantTrackCache?, lmpMin: Long?, dateOfVisitMin: Long?) {
        val list = mutableListOf(
            followUpLabel,
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
            anemia.value = getLocalValueInArray(R.array.yes_no,it.anemia)
            hypertension.value = getLocalValueInArray(R.array.yes_no,it.hypertension)
            diabetes.value = getLocalValueInArray(R.array.yes_no,it.diabetes)
            severeAnemia.value = getLocalValueInArray(R.array.yes_no,it.severeAnemia)
            fp.value = getLocalValueInArray(R.array.yes_no,it.fp)
            lmp.value = it.lmp?.let { it2 -> getDateFromLong(it2) }
            missedPeriod.value = getLocalValueInArray(R.array.yes_no,it.missedPeriod)
            isPregnant.value = getLocalValueInArray(R.array.yes_no,it.isPregnant)

            anemia.showHighRisk = anemia.value == resources.getStringArray(R.array.yes_no)[0]

            ancLabel.showHighRisk = (hypertension.value == resources.getStringArray(R.array.yes_no)[0]
                    || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(R.array.yes_no)[0])

            riskStatus.showHighRisk = (anemia.value == resources.getStringArray(R.array.yes_no)[0] || hypertension.value == resources.getStringArray(R.array.yes_no)[0]
                    || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(R.array.yes_no)[0])
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
                if (dov > it.regDate - TimeUnit.DAYS.toMillis(60))
                    dateOfVisit.min = dov + TimeUnit.DAYS.toMillis(1)
            }
            dateOfVisit.max = System.currentTimeMillis()
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            anemia.id -> {
                anemia.showHighRisk = anemia.value == resources.getStringArray(R.array.yes_no)[0]
                riskStatus.showHighRisk = (anemia.value == resources.getStringArray(R.array.yes_no)[0] || hypertension.value == resources.getStringArray(R.array.yes_no)[0]
                        || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(R.array.yes_no)[0])
                -1
            }

            hypertension.id, diabetes.id, severeAnemia.id -> {
                ancLabel.showHighRisk = (hypertension.value == resources.getStringArray(R.array.yes_no)[0]
                        || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(R.array.yes_no)[0])
                riskStatus.showHighRisk = (anemia.value == resources.getStringArray(R.array.yes_no)[0] || hypertension.value == resources.getStringArray(R.array.yes_no)[0]
                        || diabetes.value == resources.getStringArray(R.array.yes_no)[0] || severeAnemia.value == resources.getStringArray(R.array.yes_no)[0])
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
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as HRPNonPregnantTrackCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.anemia = getEnglishValueInArray(R.array.yes_no, anemia.value)
            form.hypertension = getEnglishValueInArray(R.array.yes_no, hypertension.value)
            form.diabetes = getEnglishValueInArray(R.array.yes_no, diabetes.value)
            form.severeAnemia = getEnglishValueInArray(R.array.yes_no, severeAnemia.value)
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
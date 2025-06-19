package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache

class MalariaConfirmCasesDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {


    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.start_t_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )


    private var treatmentGiven = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.treatment_given),
        entries = resources.getStringArray(R.array.pf_pv),
        required = false,
        hasDependants = false
    )


    private var dayWiseTrackingPf = FormElement(
        id = 22,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.day_wise_tracking_pf),
        arrayId = R.array.daysPf,
        entries = resources.getStringArray(R.array.daysPf),
        required = false,
        hasDependants = true
    )

    private var dayWiseTrackingPV = FormElement(
        id = 22,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.day_wise_tracking_pv),
        arrayId = R.array.daysPv,
        entries = resources.getStringArray(R.array.daysPv),
        required = false,
        hasDependants = true
    )

    private val dateOfCompletion = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date_of_completion),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (3L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val referalDate = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.date_of_referal),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (3L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )


    suspend fun setUpPage(ben: BenRegCache?, saved: MalariaConfirmedCasesCache?) {
        val list = mutableListOf(
            dateOfCase,
            treatmentGiven,
            dateOfCompletion,
            referalDate
        )
        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            dayWiseTrackingPf.value = resources.getStringArray(R.array.daysPf)[0]
            dayWiseTrackingPV.value = resources.getStringArray(R.array.daysPv)[0]
        } else {
            dateOfCase.value = getDateFromLong(saved.dateOfDiagnosis)
            dateOfCompletion.value = getDateFromLong(saved.treatmentCompletionDate)
            referalDate.value = getDateFromLong(saved.referralDate)
            treatmentGiven.value = saved.treatmentGiven
            if(treatmentGiven.value == resources.getStringArray(R.array.pf_pv)[0]){
                dayWiseTrackingPf.value = getLocalValueInArray(R.array.daysPf,saved.day)
            } else {
                dayWiseTrackingPV.value = getLocalValueInArray(R.array.daysPv,saved.day)

            }


        }

        ben?.let {
            dateOfCase.min = it.regDate
        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {

        return when (formId) {

            treatmentGiven.id -> {
                if(treatmentGiven.value == resources.getStringArray(R.array.pf_pv)[0]){
                    triggerDependants(
                        source = treatmentGiven,
                        addItems = listOf(dayWiseTrackingPf),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = treatmentGiven,
                        addItems = listOf(dayWiseTrackingPV),
                        removeItems = listOf()
                    )
                }
                0
            }


            else -> {
                0
            }
        }


    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as MalariaConfirmedCasesCache).let { form ->
            form.dateOfDiagnosis = getLongFromDate(dateOfCase.value)
            form.treatmentStartDate = getLongFromDate(dateOfCase.value)
            form.treatmentGiven = getEnglishValueInArray(R.array.pf_pv, treatmentGiven.value)
            if(treatmentGiven.value == resources.getStringArray(R.array.pf_pv)[0]){
                form.day = getEnglishValueInArray(R.array.daysPv, dayWiseTrackingPf.value)

            } else {
                form.day = getEnglishValueInArray(R.array.daysPv, dayWiseTrackingPV.value)

            }
            form.treatmentCompletionDate= getLongFromDate(dateOfCompletion.value)
            form.referralDate= getLongFromDate(referalDate.value)

        }
    }


    fun updateBen(benRegCache: BenRegCache) {
        benRegCache.genDetails?.let {
            it.reproductiveStatus =
                englishResources.getStringArray(R.array.nbr_reproductive_status_array)[1]
            it.reproductiveStatusId = 2
        }
        if (benRegCache.processed != "N") benRegCache.processed = "U"
    }


    fun getIndexOfDate(): Int {
        return getIndexById(dateOfCase.id)
    }
}
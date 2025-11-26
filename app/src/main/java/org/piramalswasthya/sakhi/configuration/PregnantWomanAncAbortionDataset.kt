package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.util.Log
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.getWeeksOfPregnancy
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import java.util.concurrent.TimeUnit

class PregnantWomanAncAbortionDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private var lastAncVisitDate: Long = 0L
    private lateinit var regis: PregnantWomanAncCache

    private val visitDate = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.visit_date),
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true,
        backgroundDrawable = R.drawable.ic_bg_circular,
        iconDrawableRes = R.drawable.ic_anc_date,
        showDrawable = true
    )

    private val weekOfPregnancy = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.weeks_of_pregnancy),
        required = false,
        showDrawable = true,
        backgroundDrawable = R.drawable.ic_bg_circular,
        iconDrawableRes = R.drawable.ic_bmi,
    )

    private val abortionDate = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.abortion_date),
        required = false,
        showDrawable = true,
        backgroundDrawable = R.drawable.ic_bg_circular,
        iconDrawableRes = R.drawable.ic_bmi,
    )

    private val abortionType = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.abortion_type),
        required = false,
    )

    private val abortionFacility = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = context.getString(R.string.facility_place_of_abortion),
        required = false,
    )

    private val serialNoAsPerAdmission = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = context.getString(R.string.serial_no_as_per_admission_evacuation_register),
        arrayId = -1,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED,
        etMaxLength = 10,
        required = false,
    )

    private val methodOfTermination = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.method_of_termination),
        arrayId = R.array.anc_method_of_termination,
        entries = resources.getStringArray(R.array.anc_method_of_termination),
        required = true,
        hasDependants = true,
    )

    private val terminationDoneBy = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.termination_done_by),
        arrayId = R.array.anc_termination_done_by,
        entries = resources.getStringArray(R.array.anc_termination_done_by),
        required = true,
    )

    private val isPlanig = FormElement(
        id = 25,
        inputType = InputType.HEADLINE,
        title = context.getString(R.string.what_family_planning_method_has_been_chosen_after_the_abortion),
        required = false,
        hasDependants = false
    )
    private val isPaiucd = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = " ",
        arrayId = R.array.abortion_isplaning_array,
        entries = resources.getStringArray(R.array.abortion_isplaning_array),
        required = false,
        orientation = 1,
        hasDependants = true
    )

    private val isYesOrNo = FormElement(
        id = 23,
        inputType = InputType.RADIO,
        title = " ",
        arrayId = R.array.anc_confirmation_array,
        entries = resources.getStringArray(R.array.anc_confirmation_array),
        required = true,
        orientation = 1,
        hasDependants = true
    )
    private val dateOfSterilization = FormElement(
        id = 24,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.date_of_sterilisation),
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true,
    )

    private val remarks = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = context.getString(R.string.remarks),
        arrayId = -1,
        etMaxLength = 100,
        required = false,
    )

    private val abortionDischargeSummaryImg1 = FormElement(
        id = 21,
        inputType = InputType.FILE_UPLOAD,
        title = context.getString(R.string.abortion_discharge_summary_1),
        required = false,
    )

    private val abortionDischargeSummaryImg2 = FormElement(
        id = 22,
        inputType = InputType.FILE_UPLOAD,
        title = context.getString(R.string.abortion_discharge_summary_2),
        required = false,
    )

    private var toggleBp = false

    fun resetBpToggle() {
        toggleBp = false
    }

    fun triggerBpToggle() = toggleBp

    suspend fun setUpPage(
        ben: BenRegCache?,
        lastAnc: PregnantWomanAncCache?,
        saved: PregnantWomanAncCache?
    ) {
        this.regis = lastAnc!!

        val list = mutableListOf(
            visitDate,
            weekOfPregnancy,
            abortionDate,
            abortionType,
            abortionFacility,
            serialNoAsPerAdmission,
            methodOfTermination,
            terminationDoneBy,
            isPlanig,
            isPaiucd,
            remarks,
            abortionDischargeSummaryImg1,
            abortionDischargeSummaryImg2,
        )


        abortionDate.min = regis.lmpDate!! + TimeUnit.DAYS.toMillis(5 * 7 + 1)
        abortionDate.max =
            minOf(System.currentTimeMillis(), regis.lmpDate!! + TimeUnit.DAYS.toMillis(21 * 7))

        if (saved == null) {
            visitDate.min = regis.abortionDate
            dateOfSterilization.min = regis.abortionDate
            val woP = getWeeksOfPregnancy(regis.ancDate, regis.lmpDate!!)
            weekOfPregnancy.value = woP.toString()
            abortionType.value = regis.abortionType
            abortionFacility.value = regis.abortionFacility
            abortionDate.value = regis.abortionDate?.let { getDateFromLong(it) }
        } else {
            isPaiucd.value = when (saved.isPaiucdId) {
                1 -> isPaiucd.entries?.getOrNull(0)
                2 -> isPaiucd.entries?.getOrNull(1)
                else -> null
            }
            if (saved.isPaiucdId != 0) {
                list.add(list.indexOf(isPaiucd) + 1, isYesOrNo)
                isYesOrNo.value = if (saved.isYesOrNo == true) "Yes" else "No"

                if (saved.isPaiucdId == 2 && saved.isYesOrNo == true) {
                    list.add(list.indexOf(isYesOrNo) + 1, dateOfSterilization)
                    dateOfSterilization.value = saved.dateSterilisation?.let { getDateFromLong(it) }
                }
            }



            dateOfSterilization.min=saved.abortionDate
            visitDate.min = saved.abortionDate
            visitDate.value = saved.visitDate?.let { getDateFromLong(it) }

            val woP = getWeeksOfPregnancy(saved.ancDate, regis.lmpDate!!)
            weekOfPregnancy.value = woP.toString()
            abortionType.value = saved.abortionType
            abortionFacility.value = saved.abortionFacility
            abortionDate.value = saved.abortionDate?.let { getDateFromLong(it) }
            serialNoAsPerAdmission.value = saved.serialNo
            methodOfTermination.value = saved.methodOfTermination
            terminationDoneBy.value = saved.terminationDoneBy
            isPaiucd.value = saved.isPaiucd
            remarks.value = saved.remarks
            abortionDischargeSummaryImg1.value = saved.abortionImg1
            abortionDischargeSummaryImg2.value = saved.abortionImg2
        }

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {

        return when (formId) {
            isPaiucd.id -> {
                isYesOrNo.value=null
                triggerDependants(
                    source = isPaiucd,
                    addItems = listOf(isYesOrNo),
                    removeItems = listOf(dateOfSterilization),
                )
            }


            isYesOrNo.id -> {
                val isPaiucdSecondOption = (isPaiucd.entries?.indexOf(isPaiucd.value ?: "") ?: -1) == 1
                if (isYesOrNo.value == "Yes" && isPaiucdSecondOption) {
                    triggerDependants(
                        source = isYesOrNo,
                        addItems = listOf(dateOfSterilization),
                        removeItems = emptyList()
                    )
                } else {
                    triggerDependants(
                        source = isYesOrNo,
                        addItems = emptyList(),
                        removeItems = listOf(dateOfSterilization)
                    )
                }
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as PregnantWomanAncCache).let { cache ->
            cache.visitDate = visitDate.value?.let { getLongFromDate(it) }
            cache.lmpDate = regis.lmpDate
            cache.abortionType = abortionType.value
            cache.abortionTypeId = abortionType.getPosition()
            cache.abortionFacility = abortionFacility.value
            cache.abortionFacilityId = abortionFacility.getPosition()
            cache.abortionDate = abortionDate.value?.let { getLongFromDate(it) }
            cache.serialNo = serialNoAsPerAdmission.value
            cache.methodOfTermination = methodOfTermination.value
            cache.methodOfTerminationId =
                methodOfTermination.entries?.indexOf(methodOfTermination.value ?: "")
                    ?.takeIf { it != -1 }
            cache.terminationDoneBy = terminationDoneBy.value
            cache.terminationDoneById =
                terminationDoneBy.entries?.indexOf(terminationDoneBy.value ?: "")
                    ?.takeIf { it != -1 }
            cache.isPaiucd = isPaiucd.value
            cache.isPaiucdId = (isPaiucd.entries?.indexOf(isPaiucd.value ?: "") ?: -1) + 1
            cache.isYesOrNo =isYesOrNo.entries?.indexOf(isYesOrNo.value ?: "") == 0
            cache.remarks = remarks.value
            cache.dateSterilisation = dateOfSterilization.value?.let { getLongFromDate(it) }
            cache.abortionImg1 = abortionDischargeSummaryImg1.value
            cache.abortionImg2 = abortionDischargeSummaryImg2.value
        }
    }

    fun getWeeksOfPregnancy(): Int = getIndexById(weekOfPregnancy.id)
    fun getIndexOfAbortionDischarge1() = getIndexById(abortionDischargeSummaryImg1.id)
    fun getIndexOfAbortionDischarge2() = getIndexById(abortionDischargeSummaryImg2.id)

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            21 -> {
                abortionDischargeSummaryImg1.value = dpUri.toString()
                abortionDischargeSummaryImg1.errorText = null
            }

            22 -> {
                abortionDischargeSummaryImg2.value = dpUri.toString()
                abortionDischargeSummaryImg2.errorText = null
            }
        }
    }
}

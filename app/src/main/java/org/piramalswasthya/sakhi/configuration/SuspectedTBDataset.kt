package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.TBSuspectedCache

class SuspectedTBDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {
    private var isFollowUpVisit = false

    private val dateOfVisit = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.tracking_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )
    private val visitLabel = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.visits),
        arrayId = -1,
        required = false,
        hasDependants = false
    )

    private val typeOfTBCase = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.type_of_tb_case),
        arrayId = R.array.type_of_tb_case,
        entries = resources.getStringArray(R.array.type_of_tb_case),
        required = true,
        hasDependants = true
    )

    private val reasonForSuspicion = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.reason_for_suspicion),
        arrayId = R.array.reason_for_suspicion,
        entries = resources.getStringArray(R.array.reason_for_suspicion),
        required = true,
        hasDependants = true
    )

    private val hasSymptoms = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.has_symptoms),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )
    private val isChestXRayDone = FormElement(
        id = 6,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_chest_xray_done),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val chestXRayResult = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.chest_xray_result),
        arrayId = R.array.chest_xray_result,
        entries = resources.getStringArray(R.array.chest_xray_result),
        required = false,
        hasDependants = false
    )

    private val isSputumCollected = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_sputum_sample_collected),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )


    private val sputumSubmittedAt = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.sputum_sample_submitted_at),
        arrayId = R.array.tb_sputum_sample_submitted_at,
        entries = resources.getStringArray(R.array.tb_sputum_sample_submitted_at),
        required = false,
        hasDependants = false
    )


    private val nikshayId = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.nikshay_id),
        required = false,
        hasDependants = false,
        regex = "^[0-9]+$",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER,
        etMaxLength = Int.MAX_VALUE
    )

    private val sputumTestResult = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.sputum_test_result),
        arrayId = R.array.tb_test_result,
        entries = resources.getStringArray(R.array.tb_test_result),
        required = true,
        hasDependants = false
    )



    private val isTBConfirmed = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_tb_confirmed),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )
    private val isDRTBConfirmed = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_drtb_confirmed),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private val isRecommendedForTpt = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.recommended_for_tpt),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private var followUps = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.facility_referral_follow_ups),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = false
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: TBSuspectedCache?) {
        isFollowUpVisit = saved != null
        visitLabel.value = resources.getString(R.string.visit_format, 1)
        var list =  mutableListOf<FormElement>()

        if (saved == null) {
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
            typeOfTBCase.value = null
            hasSymptoms.value = resources.getStringArray(R.array.yes_no)[0]

            list.addAll(listOf(
                dateOfVisit,
                visitLabel,
                typeOfTBCase,

            ))

            if (hasSymptoms.value == resources.getStringArray(R.array.yes_no)[0]) {
                isSputumCollected.value = resources.getStringArray(R.array.yes_no)[1]
                list.add(isSputumCollected)
            } else {
                isChestXRayDone.value = resources.getStringArray(R.array.yes_no)[1]
                list.add(isChestXRayDone)
            }

        }
        else {
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            visitLabel.value =  resources.getString(R.string.visit_format, 1)
            typeOfTBCase.value = getLocalValueInArray(R.array.type_of_tb_case, saved.typeOfTBCase)
            hasSymptoms.value = saved.hasSymptoms?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
            }
            isSputumCollected.value = saved.isSputumCollected?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
            }

            list.addAll(listOf(
                dateOfVisit,
                visitLabel,
                typeOfTBCase
            ))

            if (typeOfTBCase.value in listOf(
                    resources.getStringArray(R.array.type_of_tb_case)[1],
                    resources.getStringArray(R.array.type_of_tb_case)[2]
                )) {
                reasonForSuspicion.value = getLocalValueInArray(R.array.reason_for_suspicion, saved.reasonForSuspicion)
                list.add(reasonForSuspicion)
            }

            //list.add(hasSymptoms)
            list.add(isSputumCollected)

            if (saved.isSputumCollected == true) {
                sputumSubmittedAt.value = getLocalValueInArray(R.array.tb_sputum_sample_submitted_at, saved.sputumSubmittedAt)
                nikshayId.value = saved.nikshayId
                sputumTestResult.value = getLocalValueInArray(R.array.tb_test_result, saved.sputumTestResult)

                list.addAll(listOf(
                    sputumSubmittedAt,
                    nikshayId,
                    sputumTestResult
                ))
            }


            if (typeOfTBCase.value == typeOfTBCase.entries!![0]) {
                isTBConfirmed.value = saved.isTBConfirmed?.let {
                    if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                }
                list.add(isTBConfirmed)
            } else if (typeOfTBCase.value in listOf(
                    typeOfTBCase.entries!![1],
                    typeOfTBCase.entries!![2]
                )) {
                isDRTBConfirmed.value = saved.isDRTBConfirmed?.let {
                    if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                }
                list.add(isDRTBConfirmed)
            }

        }

       



        if (saved == null) {
            list.clear()
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
            typeOfTBCase.value = null
            isSputumCollected.value = resources.getStringArray(R.array.yes_no)[1]
            sputumSubmittedAt.value = null
            nikshayId.value = null
            visitLabel.value = resources.getString(R.string.visit_format, 1)
            list.addAll(listOf(dateOfVisit, visitLabel, typeOfTBCase, isSputumCollected))
            if (isSputumCollected.value == resources.getStringArray(R.array.yes_no)[0]) {
                list.addAll(listOf(sputumSubmittedAt, nikshayId))
            }
            if (typeOfTBCase.value in listOf(typeOfTBCase.entries!![1], typeOfTBCase.entries!![2])) {
                list.add(reasonForSuspicion)
            }
        } else {
            val visit2Complete = !saved.sputumTestResult.isNullOrBlank() &&
                saved.isTBConfirmed != null &&
                (saved.isTBConfirmed == true || saved.isRecommendedForTpt != null)
            list.clear()
            dateOfVisit.value = getDateFromLong(System.currentTimeMillis())
            visitLabel.value = resources.getString(R.string.visit_format, 2)
            typeOfTBCase.value = getLocalValueInArray(R.array.type_of_tb_case, saved.typeOfTBCase)
            reasonForSuspicion.value = getLocalValueInArray(R.array.reason_for_suspicion, saved.reasonForSuspicion)
            isChestXRayDone.value = saved.isChestXRayDone?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
            }
            chestXRayResult.value = getLocalValueInArray(R.array.chest_xray_result, saved.chestXRayResult)
            isDRTBConfirmed.value = saved.isDRTBConfirmed?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
            }
            isSputumCollected.value = saved.isSputumCollected?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
            }
            sputumSubmittedAt.value = getLocalValueInArray(R.array.tb_sputum_sample_submitted_at, saved.sputumSubmittedAt)
            nikshayId.value = saved.nikshayId
            sputumTestResult.value = getLocalValueInArray(R.array.tb_test_result, saved.sputumTestResult)
            isTBConfirmed.value = saved.isTBConfirmed?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
            }
            isRecommendedForTpt.value = saved.isRecommendedForTpt?.let {
                if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
            }
            dateOfVisit.isEnabled = false
            typeOfTBCase.isEnabled = false
            reasonForSuspicion.isEnabled = false
            isSputumCollected.isEnabled = false
            sputumSubmittedAt.isEnabled = false
            nikshayId.isEnabled = false
            sputumTestResult.isEnabled = !visit2Complete
            isTBConfirmed.isEnabled = !visit2Complete
            isRecommendedForTpt.isEnabled = !visit2Complete
            list.addAll(listOf(dateOfVisit, visitLabel, typeOfTBCase, isSputumCollected))
            if (typeOfTBCase.value in listOf(typeOfTBCase.entries!![1], typeOfTBCase.entries!![2])) {
                list.add(3, reasonForSuspicion)
            }
            if (saved.isSputumCollected == true) {
                list.addAll(list.indexOf(isSputumCollected) + 1, listOf(sputumSubmittedAt, nikshayId))
            }
            list.addAll(listOf(sputumTestResult, isTBConfirmed))
            if (isTBConfirmed.value == resources.getStringArray(R.array.yes_no)[1]) {
                list.add(isRecommendedForTpt)
            }
        }

        ben?.let {
            dateOfVisit.min = it.regDate
        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
/*
            typeOfTBCase.id ->{
                if (typeOfTBCase.value != resources.getStringArray(R.array.type_of_tb_case)[0])
                {
                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(reasonForSuspicion),
                        removeItems = listOf(),
                    )
                    triggerDependants(source = referralFacility,
                        addItems = listOf(isTBConfirmed), removeItems = listOf(), position = -2)
                }
                else
                {
                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(),
                        removeItems = listOf(reasonForSuspicion),
                    )
                    triggerDependants(source = referralFacility,
                        addItems = listOf(), removeItems = listOf(isTBConfirmed), position = 12)
                }
            }*/
            typeOfTBCase.id -> {
                if (!isFollowUpVisit) {
                    val needsReason = typeOfTBCase.value in listOf(
                        typeOfTBCase.entries!![1], typeOfTBCase.entries!![2]
                    )
                    return triggerDependants(
                        source = typeOfTBCase,
                        addItems = if (needsReason) listOf(reasonForSuspicion) else emptyList(),
                        removeItems = if (needsReason) emptyList() else listOf(reasonForSuspicion),
                        position = 3
                    )
                }
                val currentValue = typeOfTBCase.value

                if (currentValue == typeOfTBCase.entries!![0]) {
                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(),
                        removeItems = listOf(reasonForSuspicion, isDRTBConfirmed),
                    )

                    return triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(isTBConfirmed),
                        removeItems = listOf(),
                        position = -2
                    )
                } else if (currentValue == typeOfTBCase.entries!![1] || currentValue == typeOfTBCase.entries!![2]) {
                    val result1 = triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(reasonForSuspicion),
                        removeItems = listOf(),
                    )

                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(isDRTBConfirmed),
                        removeItems = listOf(),
                        position = -2
                    )

                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(),
                        removeItems = listOf(isTBConfirmed),
                    )

                    return result1
                } else {
                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(),
                        removeItems = listOf(reasonForSuspicion, isTBConfirmed, isDRTBConfirmed),
                    )
                    return -1
                }
            }

            isSputumCollected.id -> {
                val isCollected = isSputumCollected.value == resources.getStringArray(R.array.yes_no)[0]
                if (isCollected) {
                    sputumSubmittedAt.value = null
                    nikshayId.value = null
                }
                return triggerDependants(
                    source = isSputumCollected,
                    removeItems = if (isCollected) emptyList() else listOf(sputumSubmittedAt, nikshayId),
                    addItems = if (isCollected) listOf(sputumSubmittedAt, nikshayId) else emptyList(),
                    position = formElements.indexOf(isSputumCollected) + 1
                )
            }

            isTBConfirmed.id -> {
                val notConfirmed = isTBConfirmed.value == resources.getStringArray(R.array.yes_no)[1]
                if (notConfirmed) {
                    triggerDependants(
                        source = isTBConfirmed,
                        addItems = listOf(isRecommendedForTpt),
                        removeItems = emptyList()
                    )
                } else {
                    isRecommendedForTpt.value = null
                    triggerDependants(
                        source = isTBConfirmed,
                        addItems = emptyList(),
                        removeItems = listOf(isRecommendedForTpt)
                    )
                }
            }



            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as TBSuspectedCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.visitLabel = visitLabel.value
            form.typeOfTBCase = getEnglishValueInArray(R.array.type_of_tb_case, typeOfTBCase.value)
            form.reasonForSuspicion = getEnglishValueInArray(R.array.reason_for_suspicion, reasonForSuspicion.value)
            form.isChestXRayDone = isChestXRayDone.value?.let {
                it == isChestXRayDone.entries!![0]
            }
            form.chestXRayResult = getEnglishValueInArray(R.array.chest_xray_result, chestXRayResult.value)

            form.isSputumCollected = isSputumCollected.value?.let {
                it == isSputumCollected.entries!![0]
            }
            form.sputumSubmittedAt = getEnglishValueInArray(R.array.tb_sputum_sample_submitted_at, sputumSubmittedAt.value)
            form.nikshayId = nikshayId.value
            form.sputumTestResult = getEnglishValueInArray(R.array.tb_test_result, sputumTestResult.value)

            form.referralFacility = null
            form.isTBConfirmed = isTBConfirmed.value?.let {
                it == isTBConfirmed.entries!![0]
            }
            form.isRecommendedForTpt = isRecommendedForTpt.value?.let {
                it == isRecommendedForTpt.entries!![0]
            }
            form.isDRTBConfirmed = isDRTBConfirmed.value?.let {
                it == isDRTBConfirmed.entries!![0]
            }


            form.isConfirmed = form.isTBConfirmed == true || form.isDRTBConfirmed == true
        }
    }


    fun isTestPositive(): String? {
        return if (sputumTestResult.value == resources.getStringArray(R.array.tb_test_result)[0])
            resources.getString(R.string.tb_suspected_alert_positive) else null
    }

}

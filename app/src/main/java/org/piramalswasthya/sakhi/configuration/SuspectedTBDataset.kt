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
        title = resources.getString(R.string.visit),
        arrayId = -1,
        required = false,
        hasDependants = false
    )

    private val typeOfTBCase = FormElement(
        id = 3,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.type_of_tb_case),
        entries = resources.getStringArray(R.array.type_of_tb_case),
        required = true,
        hasDependants = true
    )

    private val reasonForSuspicion = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.reason_for_suspicion),
        entries = resources.getStringArray(R.array.reason_for_suspicion),
        required = false,
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
        entries = resources.getStringArray(R.array.tb_sputum_sample_submitted_at),
        required = false,
        hasDependants = false
    )


    private val nikshayId = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.nikshay_id),
        required = false,
        hasDependants = false
    )

    private val sputumTestResult = FormElement(
        id = 11,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.sputum_test_result),
        entries = resources.getStringArray(R.array.tb_test_result),
        required = false,
        hasDependants = false
    )



    private val referralFacility = FormElement(
        id = 12,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.referral_facility),
        entries = resources.getStringArray(R.array.referral_facility),
        required = true,
        hasDependants = false
    )

    private val isTBConfirmed = FormElement(
        id = 13,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_tb_confirmed),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )
    private val isDRTBConfirmed = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_drtb_confirmed),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
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
        visitLabel.value = resources.getString(R.string.visit_format, 1)
        var list =  mutableListOf<FormElement>()
        /*var list = mutableListOf(
            dateOfVisit,
            visitLabel,
            typeOfTBCase,
            reasonForSuspicion,
            isSputumCollected,
            sputumSubmittedAt,
//            sputumSubmittedAt,
//            nikshayId,
//            sputumTestResult,
            nikshayId,
            sputumTestResult,
            referralFacility,
            followUps
        )*/
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

            list.addAll(listOf(
                referralFacility,
                followUps
            ))
        }
        else {
            dateOfVisit.value = getDateFromLong(saved.visitDate)
            visitLabel.value =  resources.getString(R.string.visit_format, 1)
            typeOfTBCase.value = saved.typeOfTBCase
            hasSymptoms.value = saved.hasSymptoms?.let {
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
                reasonForSuspicion.value = saved.reasonForSuspicion
                list.add(reasonForSuspicion)
            }

            list.add(hasSymptoms)

            if (saved.hasSymptoms == true) {
                isSputumCollected.value = saved.isSputumCollected?.let {
                    if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                }
                list.add(isSputumCollected)

                if (saved.isSputumCollected == true) {
                    sputumSubmittedAt.value = saved.sputumSubmittedAt
                    nikshayId.value = saved.nikshayId
                    sputumTestResult.value = saved.sputumTestResult

                    list.addAll(listOf(
                        sputumSubmittedAt,
                        nikshayId,
                        sputumTestResult
                    ))
                }
            } else {
                isChestXRayDone.value = saved.isChestXRayDone?.let {
                    if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                }
                list.add(isChestXRayDone)

                if (saved.isChestXRayDone == true) {
                    chestXRayResult.value = saved.chestXRayResult
                    nikshayId.value = saved.nikshayId

                    list.addAll(listOf(
                        chestXRayResult,
                        nikshayId
                    ))
                }
            }

            referralFacility.value = saved.referralFacility

            if (saved.typeOfTBCase == resources.getStringArray(R.array.type_of_tb_case)[0]) {
                isTBConfirmed.value = saved.isTBConfirmed?.let {
                    if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                }
                list.add(isTBConfirmed)
            } else if (saved.typeOfTBCase in listOf(
                    resources.getStringArray(R.array.type_of_tb_case)[1],
                    resources.getStringArray(R.array.type_of_tb_case)[2]
                )) {
                isDRTBConfirmed.value = saved.isDRTBConfirmed?.let {
                    if (it) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(R.array.yes_no)[1]
                }
                list.add(isDRTBConfirmed)
            }

            list.addAll(listOf(
                referralFacility,
                followUps
            ))
        }

       
       /* else {
            if (saved.isSputumCollected == true) {
                list = mutableListOf(
                    dateOfVisit,
                    visitLabel,
                    typeOfTBCase,
                    reasonForSuspicion,
                    isSputumCollected,
                    sputumSubmittedAt,
                    nikshayId,
                    sputumTestResult,
                    referralFacility,
                    isTBConfirmed,
                    isDRTBConfirmed,
                    followUps
                )
                dateOfVisit.value = getDateFromLong(saved.visitDate)
                isSputumCollected.value =
                    if (saved.isSputumCollected == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                        R.array.yes_no
                    )[1]
                sputumSubmittedAt.value =
                    if (saved.sputumSubmittedAt == null) null else resources.getStringArray(R.array.tb_submitted_yet)[englishResources.getStringArray(
                        R.array.tb_submitted_yet
                    ).indexOf(saved.sputumSubmittedAt)]
                nikshayId.value = saved.nikshayId
                sputumTestResult.value =
                    if (saved.sputumTestResult == null) null else resources.getStringArray(R.array.tb_test_result)[englishResources.getStringArray(
                        R.array.tb_test_result
                    ).indexOf(saved.sputumTestResult)]
               *//* referred.value =
                    if (saved.referred == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                        R.array.yes_no
                    )[1]*//*
                followUps.value = saved.followUps
            } else {
                dateOfVisit.value = getDateFromLong(saved.visitDate)
                isSputumCollected.value =
                    if (saved.isSputumCollected == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                        R.array.yes_no
                    )[1]
                *//*referred.value =
                    if (saved.referred == true) resources.getStringArray(R.array.yes_no)[0] else resources.getStringArray(
                        R.array.yes_no
                    )[1]*//*
                followUps.value = saved.followUps
            }
        }*/


        ben?.let {
            dateOfVisit.min = it.regDate
        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

            typeOfTBCase.id ->{
                if (typeOfTBCase.value != resources.getStringArray(R.array.type_of_tb_case)[0])
                {
                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(reasonForSuspicion),
                        removeItems = listOf(),
                    )
                }
                else
                {
                    triggerDependants(
                        source = typeOfTBCase,
                        addItems = listOf(),
                        removeItems = listOf(reasonForSuspicion),
                    )
                }
            }
            isSputumCollected.id -> {
                triggerDependants(
                    source = isSputumCollected,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = sputumTestResult
                )
                triggerDependants(
                    source = isSputumCollected,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = nikshayId
                )
                triggerDependants(
                    source = isSputumCollected,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = sputumSubmittedAt
                )
            }

            referralFacility.id -> {
                if(referralFacility.value == resources.getStringArray(R.array.referral_facility)[0])
                {   triggerDependants(source = referralFacility,
                    addItems = listOf(isTBConfirmed), removeItems = listOf())}
                else{
                    triggerDependants(source = referralFacility,
                        addItems = listOf(), removeItems = listOf(isTBConfirmed))
                }

                if(referralFacility.value == resources.getStringArray(R.array.referral_facility)[1] || referralFacility.value == resources.getStringArray(R.array.referral_facility)[2] )
                {
                    triggerDependants(source = referralFacility,
                        addItems = listOf(isDRTBConfirmed), removeItems = listOf())
                }
                else{ triggerDependants(source = referralFacility,
                    addItems = listOf(), removeItems = listOf(isDRTBConfirmed))}
            }

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as TBSuspectedCache).let { form ->
            form.visitDate = getLongFromDate(dateOfVisit.value)
            form.isSputumCollected =
                isSputumCollected.value == resources.getStringArray(R.array.yes_no)[0]
            form.sputumSubmittedAt =
                if (sputumSubmittedAt.value == null) null else englishResources.getStringArray(R.array.tb_submitted_yet)[sputumSubmittedAt.entries!!.indexOf(
                    sputumSubmittedAt.value
                )]
            form.nikshayId = nikshayId.value
            form.sputumTestResult =
                if (sputumTestResult.value == null) null else englishResources.getStringArray(R.array.tb_test_result)[sputumTestResult.entries!!.indexOf(
                    sputumTestResult.value
                )]
            //form.referred = referred.value == resources.getStringArray(R.array.yes_no)[0]
            form.followUps = followUps.value
        }
    }


    fun isTestPositive(): String? {
        return if (sputumTestResult.value == resources.getStringArray(R.array.tb_test_result)[0])
            resources.getString(R.string.tb_suspected_alert_positive) else null
    }

}
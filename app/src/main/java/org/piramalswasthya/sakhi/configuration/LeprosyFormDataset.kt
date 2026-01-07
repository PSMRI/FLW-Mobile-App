package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache

class LeprosyFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfCase = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.home_visit_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true

    )
    private val beneficiaryStatus = FormElement(
        id = 2,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.beneficiary_status),
        arrayId = R.array.benificary_case_status_leprosy,
        entries = resources.getStringArray(R.array.benificary_case_status_leprosy),
        required = true,
        hasDependants = true

    )
    private val dateOfDeath = FormElement(
        id = 3,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.death_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis() -  (90L * 24 * 60 * 60 * 1000),
        max = System.currentTimeMillis(),
        hasDependants = true

    )

    private val placeOfDeath = FormElement(
        id = 4,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.place_of_Death),
        arrayId = R.array.death_place,
        entries = resources.getStringArray(R.array.death_place),
        required = true,
        hasDependants = true

    )

    private var otherPlaceOfDeath = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other_place),
        required = true,
        hasDependants = false
    )

    private val reasonOfDeath = FormElement(
        id = 6,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.reason_of_Death),
        arrayId = R.array.reason_death,
        entries = resources.getStringArray(R.array.reason_death),
        required = true,
        hasDependants = true

    )
    private var otherReasonOfDeath = FormElement(
        id = 7,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other_reason),
        required = true,
        hasDependants = false
    )
    private val leprosyStatus = FormElement(
        id = 8,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_leprosy_status),
        arrayId = R.array.leprosy_status,
        entries = resources.getStringArray(R.array.leprosy_status),
        required = false,
        hasDependants = true

    )

    private var referredTo = FormElement(
        id = 9,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.refer_to),
        arrayId = R.array.dc_refer,
        entries = resources.getStringArray(R.array.dc_refer),
        required = false,
        hasDependants = true
    )
    private var other = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.other),
        required = true,
        hasDependants = false
    )
    private var typeOfLeprosy = FormElement(
        id = 11,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.type_of_leprocy),
        arrayId = R.array.type_of_leprocy,
        entries = resources.getStringArray(R.array.type_of_leprocy),
        required = false,
        hasDependants = true
    )

    private val followUpdate = FormElement(
        id = 12,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.follow_up_date),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
        hasDependants = true

    )
    private val remarks = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.pnc_remarks),
        required = false,

        )

    private val recurrentUlceration = FormElement(
        id = 16,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_recurrent_ulceration),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )

    private val cbacRecurrentTingling = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_recurrent_tingling),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacAnyHyperPigmented = FormElement(
        id = 18,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_Any_hyper_pigmented),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )

    private val cbacAnyThickendSkin = FormElement(
        id = 19,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_any_thickend_skin),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacAnyNodulesSkin = FormElement(
        id = 20,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_any_nodules_skin),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacRecurrentNumbness = FormElement(
        id = 21,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_Recurrent_numbness),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacClawingOfFingers = FormElement(
        id = 22,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_Clawing_of_fingers),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacTinglingOrNumbness = FormElement(
        id = 23,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_Tingling_or_Numbness),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacInabilityCloseEyelid = FormElement(
        id = 24,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_Inability_close_eyelid),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacDiffHoldingObjects = FormElement(
        id = 25,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_diff_holding_objects),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )
    private val cbacWeeknessInFeet = FormElement(
        id = 26,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = resources.getString(R.string.cbac_Weekness_in_feet),
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )

    private val leprosySymptoms = FormElement(
        id = 14,
        inputType = InputType.RADIO,
        arrayId = R.array.yes_no,
        title = "Any Leprosy Symptoms Present?",
        entries = resources.getStringArray(R.array.yes_no),
        hasDependants = true,
        required = true,
        isEnabled = true
    )

    private val visitLabel = FormElement(
        id = 15,
        inputType = InputType.TEXT_VIEW,
        title = "Visit",
        required = true,
        isEnabled = false
    )

    suspend fun setUpPage(ben: BenRegCache?, saved: LeprosyScreeningCache?) {
        val list = mutableListOf(
            dateOfCase,
           // beneficiaryStatus,
            recurrentUlceration,
            cbacRecurrentTingling,
            cbacAnyHyperPigmented,
            cbacAnyThickendSkin,
            cbacAnyNodulesSkin,
            cbacRecurrentNumbness,
            cbacClawingOfFingers,
            cbacTinglingOrNumbness,
            cbacInabilityCloseEyelid,
            cbacDiffHoldingObjects,
            cbacWeeknessInFeet,
            leprosySymptoms,
            visitLabel,
            leprosyStatus,


        )
        if (saved == null) {
            dateOfCase.value = getDateFromLong(System.currentTimeMillis())
            leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[0]
            visitLabel.value = "Visit -1"
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no)[1]
        } else {
            dateOfCase.value = getDateFromLong(saved.homeVisitDate)
            val symptomsPosition = saved.leprosySymptomsPosition ?: 1
            leprosySymptoms.value = resources.getStringArray(R.array.yes_no).getOrNull(symptomsPosition)
                ?: resources.getStringArray(R.array.yes_no)[1]
            visitLabel.value = "Visit -${saved?.currentVisitNumber ?: 1}"
            leprosyStatus.value =
                getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)

            if (leprosyStatus.value == leprosyStatus.entries!!.last()) {
                leprosyStatus.value = saved.leprosyStatus
                list.add(list.indexOf(leprosyStatus) + 1, other)
            } else {
                leprosyStatus.value =
                    getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)
            }
            list.add(list.indexOf(leprosyStatus) + 1, referredTo)
            referredTo.value =
                getLocalValueInArray(referredTo.arrayId, saved.referToName)
            if (referredTo.value == referredTo.entries!!.last()) {
                referredTo.value = saved.referToName
                list.add(list.indexOf(referredTo) + 1, other)
            } else {
                referredTo.value =
                    getLocalValueInArray(referredTo.arrayId, saved.referToName)
            }
            val recurrentUlcerationId = saved.recurrentUlcerationId ?: 1
            val recurrentTinglingId  = saved.recurrentTinglingId  ?: 1
            val hypopigmentedPatchId  = saved.hypopigmentedPatchId  ?: 1
            val thickenedSkinId  = saved.thickenedSkinId  ?: 1
            val skinNodulesId  = saved.skinNodulesId  ?: 1
            val skinPatchDiscolorationId  = saved.skinPatchDiscolorationId  ?: 1
            val recurrentNumbnessId  = saved.recurrentNumbnessId  ?: 1
            val clawingFingersId  = saved.clawingFingersId  ?: 1
            val tinglingNumbnessExtremitiesId  = saved.tinglingNumbnessExtremitiesId  ?: 1
            val inabilityCloseEyelidId  = saved.inabilityCloseEyelidId  ?: 1
            val difficultyHoldingObjectsId  = saved.difficultyHoldingObjectsId  ?: 1
            val weaknessFeetId   = saved.weaknessFeetId   ?: 1
            recurrentUlceration.value = resources.getStringArray(R.array.yes_no).getOrNull(recurrentUlcerationId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacRecurrentTingling.value = resources.getStringArray(R.array.yes_no).getOrNull(recurrentTinglingId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacAnyHyperPigmented.value = resources.getStringArray(R.array.yes_no).getOrNull(hypopigmentedPatchId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacAnyThickendSkin.value = resources.getStringArray(R.array.yes_no).getOrNull(thickenedSkinId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacAnyNodulesSkin.value = resources.getStringArray(R.array.yes_no).getOrNull(skinNodulesId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacRecurrentNumbness.value = resources.getStringArray(R.array.yes_no).getOrNull(recurrentNumbnessId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacClawingOfFingers.value = resources.getStringArray(R.array.yes_no).getOrNull(clawingFingersId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacTinglingOrNumbness.value = resources.getStringArray(R.array.yes_no).getOrNull(tinglingNumbnessExtremitiesId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacInabilityCloseEyelid.value = resources.getStringArray(R.array.yes_no).getOrNull(inabilityCloseEyelidId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacDiffHoldingObjects.value = resources.getStringArray(R.array.yes_no).getOrNull(difficultyHoldingObjectsId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            cbacWeeknessInFeet.value = resources.getStringArray(R.array.yes_no).getOrNull(weaknessFeetId)
                ?: resources.getStringArray(R.array.yes_no)[1]
            /*if
            (beneficiaryStatus.value == beneficiaryStatus.entries!![3]) {
                list.add(list.indexOf(beneficiaryStatus) + 1, dateOfDeath)
                list.add(list.indexOf(beneficiaryStatus) + 2, placeOfDeath)
                list.add(list.indexOf(beneficiaryStatus) + 3, reasonOfDeath)
                dateOfDeath.value =
                    getDateFromLong(saved.dateOfDeath)
                placeOfDeath.value =
                    getLocalValueInArray(placeOfDeath.arrayId, saved.placeOfDeath)
                if (placeOfDeath.value == placeOfDeath.entries!!.last()) {
                    list.add(list.indexOf(placeOfDeath) + 1, otherPlaceOfDeath)
                    otherPlaceOfDeath.value = saved.otherPlaceOfDeath
                }

                reasonOfDeath.value =
                    getLocalValueInArray(reasonOfDeath.arrayId, saved.reasonForDeath)
                if (reasonOfDeath.value == reasonOfDeath.entries!!.last()) {
                    list.add(list.indexOf(reasonOfDeath) + 1, otherReasonOfDeath)
                    otherReasonOfDeath.value = saved.otherReasonForDeath
                }
            } else {
                list.add(list.indexOf(beneficiaryStatus) + 1, leprosyStatus)
                list.add(list.indexOf(beneficiaryStatus) + 2, typeOfLeprosy)
                list.add(list.indexOf(beneficiaryStatus) + 3, followUpdate)
                list.add(list.indexOf(beneficiaryStatus) + 4, remarks)
                remarks.value = saved.remarks
                leprosyStatus.value = getLocalValueInArray(leprosyStatus.arrayId, saved.leprosyStatus)
                if (leprosyStatus.value == leprosyStatus.entries!![leprosyStatus.entries!!.size-3]) {
                    list.add(list.indexOf(leprosyStatus) + 1, referredTo)

                }
                typeOfLeprosy.value = getLocalValueInArray(typeOfLeprosy.arrayId, saved.typeOfLeprosy)

            }


            other.value = saved.otherReferredTo*/

        }



        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {

        return when (formId) {

            recurrentUlceration.id,
            cbacRecurrentTingling.id,
            cbacAnyHyperPigmented.id,
            cbacAnyThickendSkin.id,
            cbacAnyNodulesSkin.id,
            cbacRecurrentNumbness.id,
            cbacClawingOfFingers.id,
            cbacTinglingOrNumbness.id,
            cbacInabilityCloseEyelid.id,
            cbacDiffHoldingObjects.id,
            cbacWeeknessInFeet.id -> {

                updateLeprosySymptomsFromChecklist()
                0
            }

            leprosySymptoms.id -> {
                if (leprosySymptoms.value == resources.getStringArray(R.array.yes_no)[0]) {

                    leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[3]
                    visitLabel.value = "Visit -1"

                    triggerDependants(
                        source = leprosySymptoms,
                        addItems = listOf(visitLabel,leprosyStatus,referredTo),
                        removeItems = listOf()
                    )

                } else {
                    leprosyStatus.value = resources.getStringArray(R.array.leprosy_status)[0]
                    triggerDependants(
                        source = leprosySymptoms,
                        addItems = listOf(),
                        removeItems = listOf(referredTo)
                    )
                }
                0
            }
            beneficiaryStatus.id -> {
                if (beneficiaryStatus.value == beneficiaryStatus.entries!![3]!!) {
                    triggerDependants(
                        source = beneficiaryStatus,
                        addItems = listOf(dateOfDeath,placeOfDeath,reasonOfDeath),
                        removeItems = listOf(leprosyStatus,referredTo,typeOfLeprosy,followUpdate,
                            remarks)
                    )
                } else {
                    triggerDependants(
                        source = beneficiaryStatus,
                        addItems = listOf(leprosyStatus,typeOfLeprosy,followUpdate,
                            remarks),
                        removeItems = listOf(dateOfDeath,placeOfDeath,reasonOfDeath,otherPlaceOfDeath,otherReasonOfDeath)
                    )
                }
                0
            }


            leprosyStatus.id -> {
                if (leprosyStatus.value == leprosyStatus.entries!![leprosyStatus.entries!!.size-3]) {
                    triggerDependants(
                        source = leprosyStatus,
                        addItems = listOf(referredTo),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = leprosyStatus,
                        addItems = listOf(),
                        removeItems = listOf(referredTo)
                    )
                }
                0
            }

            referredTo.id -> {
                if (referredTo.value == referredTo.entries!!.last()) {
                    triggerDependants(
                        source = referredTo,
                        addItems = listOf(other),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = referredTo,
                        addItems = listOf(),
                        removeItems = listOf(other)
                    )
                }
                0
            }



            placeOfDeath.id -> {
                if (placeOfDeath.value == placeOfDeath.entries!!.last()) {
                    triggerDependants(
                        source = placeOfDeath,
                        addItems = listOf(otherPlaceOfDeath),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = placeOfDeath,
                        addItems = listOf(),
                        removeItems = listOf(otherPlaceOfDeath)
                    )
                }
                0
            }
            reasonOfDeath.id -> {
                if (reasonOfDeath.value == reasonOfDeath.entries!!.last()) {
                    triggerDependants(
                        source = reasonOfDeath,
                        addItems = listOf(otherReasonOfDeath),
                        removeItems = listOf()
                    )
                } else {
                    triggerDependants(
                        source = reasonOfDeath,
                        addItems = listOf(),
                        removeItems = listOf(otherReasonOfDeath)
                    )
                }
                0
            }

            other.id -> {
                validateEmptyOnEditText(other)
            }

            otherReasonOfDeath.id -> {
                validateEmptyOnEditText(otherReasonOfDeath)
            }
            otherPlaceOfDeath.id -> {
                validateEmptyOnEditText(otherPlaceOfDeath)

            }

            else -> {
                0
            }
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as LeprosyScreeningCache).let { form ->
            form.homeVisitDate = getLongFromDate(dateOfCase.value)
            form.referToName = referredTo.value
            form.referredTo = referredTo.getPosition()
            form.beneficiaryStatus = beneficiaryStatus.value
            form.beneficiaryStatusId = beneficiaryStatus.getPosition()
            form.reasonForDeath = reasonOfDeath.value
            form.otherPlaceOfDeath = otherPlaceOfDeath.value
            form.otherReasonForDeath = otherReasonOfDeath.value
            form.dateOfDeath = getLongFromDate(dateOfDeath.value)
            form.placeOfDeath = placeOfDeath.value
            form.otherReferredTo = other.value
            form.remarks = remarks.value
            form.leprosyStatus = leprosyStatus.value
            form.typeOfLeprosy = typeOfLeprosy.value
            form.diseaseTypeID = 5
            form.leprosySymptoms = leprosySymptoms.value
            form.visitLabel = visitLabel.value
            form.syncState = SyncState.UNSYNCED
            form.leprosySymptomsPosition = when (leprosySymptoms.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.recurrentUlcerationId = when (recurrentUlceration.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.recurrentTinglingId = when (cbacRecurrentTingling.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.hypopigmentedPatchId = when (cbacAnyHyperPigmented.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.thickenedSkinId = when (cbacAnyThickendSkin.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.skinNodulesId = when (cbacAnyNodulesSkin.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.skinPatchDiscolorationId = when (cbacAnyHyperPigmented.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.recurrentNumbnessId = when (cbacRecurrentNumbness .value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.clawingFingersId = when (cbacClawingOfFingers.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.tinglingNumbnessExtremitiesId = when (cbacTinglingOrNumbness .value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.inabilityCloseEyelidId = when (cbacInabilityCloseEyelid.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.difficultyHoldingObjectsId = when (cbacDiffHoldingObjects.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.weaknessFeetId = when (cbacWeeknessInFeet.value) {
                resources.getStringArray(R.array.yes_no)[0] -> 0
                resources.getStringArray(R.array.yes_no)[1] -> 1
                else -> 1
            }
            form.recurrentUlceration = recurrentUlceration.value
            form.recurrentTingling = cbacRecurrentTingling.value
            form.hypopigmentedPatch  = cbacAnyHyperPigmented.value
            form.thickenedSkin  = cbacAnyThickendSkin.value
            form.skinNodules  = cbacAnyNodulesSkin.value
            form.skinPatchDiscoloration  = cbacAnyHyperPigmented.value
            form.recurrentNumbness  = cbacRecurrentNumbness.value
            form.clawingFingers  = cbacClawingOfFingers.value
            form.tinglingNumbnessExtremities  = cbacTinglingOrNumbness.value
            form.inabilityCloseEyelid  = cbacInabilityCloseEyelid.value
            form.difficultyHoldingObjects   = cbacDiffHoldingObjects.value
            form.weaknessFeet   = cbacWeeknessInFeet.value


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

//

    fun getIndexOfDate(): Int {
        return getIndexById(dateOfCase.id)
    }

    private fun updateLeprosySymptomsFromChecklist() {
        val yesValue = resources.getStringArray(R.array.yes_no)[0]

        val anyYes = listOf(
            recurrentUlceration.value,
            cbacRecurrentTingling.value,
            cbacAnyHyperPigmented.value,
            cbacAnyThickendSkin.value,
            cbacAnyNodulesSkin.value,
            cbacRecurrentNumbness.value,
            cbacClawingOfFingers.value,
            cbacTinglingOrNumbness.value,
            cbacInabilityCloseEyelid.value,
            cbacDiffHoldingObjects.value,
            cbacWeeknessInFeet.value
        ).any { it == yesValue }

        leprosySymptoms.value =
            if (anyYes) resources.getStringArray(R.array.yes_no)[0]
            else resources.getStringArray(R.array.yes_no)[1]
    }
}
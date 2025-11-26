package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HbncHomeVisit
import org.piramalswasthya.sakhi.model.HbncPartI
import org.piramalswasthya.sakhi.model.HbncPartII
import org.piramalswasthya.sakhi.model.HbncVisitCard
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import timber.log.Timber


class HBNCFormDataset(
    context: Context,
    language: Languages,
    private val nthDay: Int
) : Dataset(context, language) {

    suspend fun setCardPageToList(
        location: LocationRecord?,
        asha: User,
        childBen: BenRegCache,
        motherBen: BenRegCache?,
        visitCard: HbncVisitCard?
    ) {

        visitCard?.let { setExistingValuesForCardPage(it) } ?: run {
            ashaName.value = asha.userName
            villageName.value = location?.village?.name
            blockName.value = location?.block?.name
            motherName.value = childBen.motherName
            fatherName.value = childBen.fatherName
            placeOfDelivery.value = childBen.kidDetails?.birthPlace
            gender.value = gender.entries?.get(childBen.genderId)
            typeOfDelivery.value =
                childBen.kidDetails?.deliveryTypeId?.let { typeOfDelivery.getStringFromPosition(it) }
//            motherBen?.let {
//                dateOfDelivery.value = it.genDetails?.deliveryDate
//            }
        }
//        Timber.d("list before adding $list")
        setUpPage(cardPage)
//        Timber.d("list after adding $list")
    }


    suspend fun setPart1PageToList(visitCard: HbncVisitCard?, hbncPart1: HbncPartI?) {
        val list = partIPage.toMutableList()
        babyAlive.value = visitCard?.stillBirth?.let {
            when (it) {
                0 -> null
                1 -> babyAlive.entries?.get(1).also {
                    if (hbncPart1 == null) list.addAll(
                        list.indexOf(babyAlive) + 1, listOf(
                            dateOfBabyDeath,
                            timeOfBabyDeath,
                            placeOfBabyDeath,
                        )
                    )
                }

                2 -> babyAlive.entries?.get(0)
                else -> null
            }
        }

        if (hbncPart1 == null) {
            dateOfHomeVisit.value = getDateFromLong(System.currentTimeMillis())
        } else {
            setExistingValuesForPartIPage(hbncPart1, list)
        }
        setUpPage(list)
    }

    suspend fun setPart2PageToList(hbncPart2: HbncPartII?) {
        val list = partIIPage.toMutableList()
        if (hbncPart2 == null) {
            dateOfHomeVisit.value = getDateFromLong(System.currentTimeMillis())
        } else {
            setExistingValuesForPartIIPage(hbncPart2, list)
        }
        setUpPage(list)
    }

    suspend fun setVisitToList(
        firstDay: HbncHomeVisit?, currentDay: HbncHomeVisit?
    ) {
        val list = visitPage.toMutableList()

        if (currentDay == null) {
            dateOfHomeVisit.value = getDateFromLong(System.currentTimeMillis())
            firstDay?.let {
                childImmunizationStatus.value = it.babyImmunizationStatus
            }
        } else {
            setExistingValuesForVisitPage(currentDay, list)
        }
        setUpPage(list)
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (nthDay) {
            Konstants.hbncCardDay -> handleForCardDay(formId, index)
            Konstants.hbncPart1Day -> handleForPart1Day(formId, index)
            Konstants.hbncPart2Day -> handleForPart2Day(formId, index)
            else -> handleForVisitDay(formId, index)
        }
//        if (updateIndex != -1) {
//            val newList = list.toMutableList()
//            if (updateUIForCurrentElement) {
//                Timber.d("Updating UI element ...")
//                newList[updateIndex] = list[updateIndex].cloneForm()
//                updateUIForCurrentElement = false
//            }
//            Timber.d("Emitting ${newList}}")
//            _listFlow.emit(newList)
//        }
//        Timber.d("Take ${newList.map { it.hashCode() }}")
//        Timber.d("Make ${list.map { it.hashCode() }}")
//        Timber.d("Current list : ${list.map { Pair(it.id, it.errorText) }}")

    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        when (nthDay) {
            Konstants.hbncCardDay -> mapCardValues(cacheModel as HBNCCache)
            Konstants.hbncPart1Day -> mapPartIValues(cacheModel as HBNCCache)
            Konstants.hbncPart2Day -> mapPartIIValues(cacheModel as HBNCCache)
            else -> mapVisitValues(cacheModel as HBNCCache)
        }
    }


    private fun handleForCardDay(formId: Int, index: Int): Int {
        when (formId) {
            healthSubCenterName.id -> {
                healthSubCenterName.value?.let {
                    if (it.length > 10 && healthSubCenterName.errorText == null) {
                        healthSubCenterName.errorText = resources.getString(R.string.str_yay_it_is_working)
                        Timber.d("Yay, it is working!!!")
                        return -1
                    }
                    if (it.length <= 10 && healthSubCenterName.errorText != null) {
                        healthSubCenterName.errorText = null
                        Timber.d("Yay, it is not working!!!")
                        return -1
                    }
                }
            }
        }
        Timber.d("Handle Card day called : formId : $formId index : $index")
        return -1
    }

    private suspend fun handleForPart1Day(formId: Int, index: Int): Int {
        return when (formId) {
            babyAlive.id -> {
                if (index == babyAlive.entries!!.size - 1) emitAlertErrorMessage(
                    R.string.hbnc_baby_dead_alert
                )
                triggerDependants(
                    source = babyAlive,
                    passedIndex = index,
                    triggerIndex = babyAlive.entries!!.size - 1,
                    target = listOf(
                        dateOfBabyDeath,
                        timeOfBabyDeath,
                        placeOfBabyDeath,
                    ),
                    targetSideEffect = listOf(otherPlaceOfBabyDeath)
                )
            }

            placeOfBabyDeath.id -> triggerDependants(
                source = placeOfBabyDeath,
                passedIndex = index,
                triggerIndex = placeOfBabyDeath.entries!!.size - 1,
                target = otherPlaceOfBabyDeath
            )

            motherAlive.id -> {
                if (index == motherAlive.entries!!.size - 1) emitAlertErrorMessage(
                    R.string.hbnc_mother_dead_alert
                )
                triggerDependants(
                    source = motherAlive,
                    passedIndex = index,
                    triggerIndex = motherAlive.entries!!.size - 1,
                    target = listOf(
                        dateOfMotherDeath,
                        timeOfMotherDeath,
                        placeOfMotherDeath,
                    ),
                    targetSideEffect = listOf(otherPlaceOfMotherDeath)
                )
            }

            placeOfMotherDeath.id -> triggerDependants(
                source = placeOfMotherDeath,
                passedIndex = index,
                triggerIndex = placeOfMotherDeath.entries!!.size - 1,
                target = otherPlaceOfMotherDeath
            )

            babyPreterm.id -> triggerDependants(
                source = babyPreterm, passedIndex = index, triggerIndex = 0, target = gestationalAge
            )

            gestationalAge.id -> {
                if (index == 0) emitAlertErrorMessage(R.string.hbnc_baby_gestational_age_alert)
                -1
            }

            motherProblems.id -> {
                emitAlertErrorMessage(
                    errorMessage = R.string.hbnc_mother_problem_alert
                )
                -1
            }

            babyFedAfterBirth.id -> triggerDependants(
                source = babyFedAfterBirth,
                passedIndex = index,
                triggerIndex = babyFedAfterBirth.entries!!.size - 1,
                target = otherBabyFedAfterBirth
            )

            motherHasBreastFeedProblem.id -> triggerDependants(
                motherHasBreastFeedProblem, index, 0, motherBreastFeedProblem
            )

            else -> -1
        }
    }

    private fun handleForPart2Day(formId: Int, index: Int): Int {
        return when (formId) {
            unusualWithBaby.id -> triggerDependants(
                source = unusualWithBaby,
                passedIndex = index,
                triggerIndex = 2,
                target = otherUnusualWithBaby
            )

            else -> -1
        }
    }

    private suspend fun handleForVisitDay(formId: Int, index: Int): Int {
        return when (formId) {
            timesMotherFed24hr.id -> {
                timesMotherFed24hr.value?.takeIf { it.isNotEmpty() }?.toInt()?.let {
                    if (it < 4) emitAlertErrorMessage(R.string.hbnc_mother_num_eat_alert)
                }
                -1
            }

            timesPadChanged.id -> {
                timesPadChanged.value?.takeIf { it.isNotEmpty() }?.toInt()?.let {
                    if (it > 5) emitAlertErrorMessage(R.string.hbnc_mother_num_pad_alert)
                }
                -1
            }

            babyKeptWarmWinter.id -> {
                if (index == 1) emitAlertErrorMessage(R.string.hbnc_baby_warm_winter_alert)
                -1
            }

            babyBreastFedProperly.id -> {
                if (index == 1) emitAlertErrorMessage(R.string.hbnc_baby_fed_properly_alert)
                -1
            }

            babyCryContinuously.id -> {
                if (index == 0) emitAlertErrorMessage(R.string.hbnc_baby_cry_incessant_alert)
                -1
            }

            motherBodyTemperature.id -> {
                motherBodyTemperature.value?.takeIf { it.isNotEmpty() }?.toInt()?.let {
                    if (it in 99..102) {
                        emitAlertErrorMessage(R.string.hbnc_mother_temp_case_1)
                    } else if (it > 102) emitAlertErrorMessage(R.string.hbnc_mother_temp_case_2)
                }
                -1
            }

            motherWaterDischarge.id -> {
                if (index == 0) emitAlertErrorMessage(R.string.hbnc_mother_foul_discharge_alert)
                -1
            }

            motherSpeakAbnormalFits.id -> {
                if (index == 0) emitAlertErrorMessage(R.string.hbnc_mother_speak_abnormal_fits_alert)
                -1
            }

            motherNoOrLessMilk.id -> {
                if (index == 0) emitAlertErrorMessage(R.string.hbnc_mother_less_no_milk_alert)
                -1
            }

            motherBreastProblem.id -> {
                if (index == 0) emitAlertErrorMessage(R.string.hbnc_mother_breast_problem_alert)
                -1
            }

            babyEyesSwollen.id -> {
                if (index == 0) emitAlertErrorMessage(R.string.hbnc_baby_eye_pus_alert)
                -1
            }

            babyWeight.id -> {
                babyWeight.value?.takeIf { it.isNotEmpty() }?.toDouble()?.let {
                    if (it <= 1.8) emitAlertErrorMessage(R.string.hbnc_baby_weight_1_8_alert)
                    else if (it <= 2.5) emitAlertErrorMessage(R.string.hbnc_baby_weight_2_5_alert)
                }
                -1
            }

            babyTemperature.id -> {
                babyTemperature.value?.takeIf { it.isNotEmpty() }?.toInt()?.let {
                    if (it < 96) emitAlertErrorMessage(R.string.hbnc_baby_temp_96_alert)
                    else if (it < 97) emitAlertErrorMessage(R.string.hbnc_baby_temp_97_alert)
                    else if (it > 99) emitAlertErrorMessage(R.string.hbnc_baby_temp_99_alert)
                }
                -1
            }

            babyReferred.id -> triggerDependants(
                source = babyReferred,
                passedIndex = index,
                triggerIndex = 0,
                target = listOf(dateOfBabyReferral, placeOfBabyReferral),
                targetSideEffect = listOf(otherPlaceOfBabyReferral)
            )

            placeOfBabyReferral.id -> triggerDependants(
                source = placeOfBabyReferral,
                passedIndex = index,
                triggerIndex = placeOfBabyReferral.entries!!.size - 1,
                target = otherPlaceOfBabyReferral,
            )

            motherReferred.id -> triggerDependants(
                source = motherReferred,
                passedIndex = index,
                triggerIndex = 0,
                target = listOf(dateOfMotherReferral, placeOfMotherReferral),
                targetSideEffect = listOf(otherPlaceOfMotherReferral)
            )

            placeOfMotherReferral.id -> triggerDependants(
                source = placeOfMotherReferral,
                passedIndex = index,
                triggerIndex = placeOfMotherReferral.entries!!.size - 1,
                target = otherPlaceOfMotherReferral,
            )

            else -> -1
        }
    }


    private fun mapCardValues(hbnc: HBNCCache) {
        hbnc.visitCard = HbncVisitCard(
            ashaName = ashaName.value,
            villageName = villageName.value,
            subCenterName = healthSubCenterName.value,
            blockName = blockName.value,
            motherName = motherName.value,
            fatherName = fatherName.value,
            dateOfDelivery = getLongFromDate(dateOfDelivery.value),
            placeOfDelivery = placeOfDelivery.getPosition(),
            babyGender = gender.getPosition(),
            typeOfDelivery = typeOfDelivery.getPosition(),
            stillBirth = stillBirth.getPosition(),
            startedBreastFeeding = startedBreastFeeding.getPosition(),
            dischargeDateMother = getLongFromDate(dateOfDischargeFromHospitalMother.value),
            dischargeDateBaby = getLongFromDate(dateOfDischargeFromHospitalBaby.value),
            weightInGrams = weightAtBirth.value?.takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            registrationOfBirth = registrationOfBirth.getPosition(),
        )
    }

    private fun mapPartIValues(hbnc: HBNCCache) {
        hbnc.part1 = HbncPartI(
            dateOfVisit = getLongFromDate(dateOfHomeVisit.value),
            babyAlive = babyAlive.getPosition(),
            dateOfBabyDeath = getLongFromDate(dateOfBabyDeath.value),
            timeOfBabyDeath = timeOfBabyDeath.value,
            placeOfBabyDeath = placeOfBabyDeath.getPosition(),
            otherPlaceOfBabyDeath = otherPlaceOfBabyDeath.value,
            isBabyPreterm = babyPreterm.getPosition(),
            gestationalAge = gestationalAge.getPosition(),
            dateOfFirstExamination = getLongFromDate(dateOfBabyFirstExamination.value),
            timeOfFirstExamination = timeOfBabyFirstExamination.value,
            motherAlive = motherAlive.getPosition(),
            dateOfMotherDeath = getLongFromDate(dateOfMotherDeath.value),
            timeOfMotherDeath = timeOfMotherDeath.value,
            placeOfMotherDeath = placeOfMotherDeath.getPosition(),
            otherPlaceOfMotherDeath = otherPlaceOfMotherDeath.value,
            motherAnyProblem = motherProblems.value,
            babyFirstFed = babyFedAfterBirth.getPosition(),
            otherBabyFirstFed = otherBabyFedAfterBirth.value,
            timeBabyFirstFed = whenBabyFirstFed.value,
            howBabyTookFirstFeed = howBabyTookFirstFeed.getPosition(),
            motherHasBreastFeedProblem = motherHasBreastFeedProblem.getPosition(),
            motherBreastFeedProblem = motherBreastFeedProblem.value,
        )
    }

    private fun mapPartIIValues(hbnc: HBNCCache) {
        hbnc.part2 = HbncPartII(
            dateOfVisit = getLongFromDate(dateOfHomeVisit.value),
            babyTemperature = babyTemperature.value,
            babyEyeCondition = babyEyeCondition.getPosition(),
            babyUmbilicalBleed = babyBleedUmbilicalCord.getPosition(),
            actionBabyUmbilicalBleed = actionUmbilicalBleed.getPosition(),
            babyWeight = babyWeight.value ?: "0",
            babyWeightMatchesColor = babyWeigntMatchesColor.getPosition(),
            babyWeightColorOnScale = babyWeightColor.getPosition(),
            allLimbsLimp = allLimbsLimp.getPosition(),
            feedLessStop = feedingLessStop.getPosition(),
            cryWeakStop = cryWeakStopped.getPosition(),
            dryBaby = babyDry.getPosition(),
            wrapClothCloseToMother = wrapClothKeptMother.getPosition(),
            exclusiveBreastFeeding = onlyBreastMilk.getPosition(),
            cordCleanDry = cordCleanDry.getPosition(),
            unusualInBaby = unusualWithBaby.getPosition(),
            otherUnusualInBaby = otherUnusualWithBaby.value,
        )
    }

    private fun mapVisitValues(hbnc: HBNCCache) {
        hbnc.homeVisitForm = HbncHomeVisit(
            dateOfVisit = getLongFromDate(dateOfMotherDeath.value),
            babyAlive = babyAlive.getPosition(),
            numTimesFullMeal24hr = timesMotherFed24hr.value?.takeIf { it.isNotEmpty() }?.toInt()
                ?: 0,
            numPadChanged24hr = timesPadChanged.value?.takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            babyKeptWarmWinter = babyKeptWarmWinter.getPosition(),
            babyFedProperly = babyBreastFedProperly.getPosition(),
            babyCryContinuously = babyCryContinuously.getPosition(),
            motherTemperature = motherBodyTemperature.value,
            foulDischargeFever = motherWaterDischarge.getPosition(),
            motherSpeakAbnormallyFits = motherSpeakAbnormalFits.getPosition(),
            motherLessNoMilk = motherNoOrLessMilk.getPosition(),
            motherBreastProblem = motherBreastProblem.getPosition(),
            babyEyesSwollen = babyEyesSwollen.getPosition(),
            babyWeight = babyWeight.value,
            babyTemperature = babyTemperature.value,
            babyYellow = yellowJaundice.getPosition(),
            babyImmunizationStatus = childImmunizationStatus.value,
            babyReferred = babyReferred.getPosition(),
            dateOfBabyReferral = getLongFromDate(dateOfBabyReferral.value),
            placeOfBabyReferral = placeOfBabyReferral.getPosition(),
            otherPlaceOfBabyReferral = otherPlaceOfBabyReferral.value,
            motherReferred = motherReferred.getPosition(),
            dateOfMotherReferral = getLongFromDate(dateOfMotherReferral.value),
            placeOfMotherReferral = placeOfMotherReferral.getPosition(),
            otherPlaceOfMotherReferral = otherPlaceOfMotherReferral.value,
            allLimbsLimp = allLimbsLimp.getPosition(),
            feedingLessStopped = feedingLessStop.getPosition(),
            cryWeakStopped = cryWeakStopped.getPosition(),
            bloatedStomach = bloatedStomach.getPosition(),
            coldOnTouch = childColdOnTouch.getPosition(),
            chestDrawing = childChestDrawing.getPosition(),
            breathFast = breathFast.getPosition(),
            pusNavel = pusNavel.getPosition(),
            sup = sup.getPosition(),
            supName = supName.value,
            supComment = supRemark.value,
            supSignDate = getLongFromDate(dateOfSupSig.value),
        )
    }

    fun setVillageName(village: String) {
        villageName.value = village
    }

    fun setBlockName(block: String) {
        blockName.value = block
    }

    fun setAshaName(userName: String) {
        ashaName.value = userName
    }


    private val healthSubCenterName = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_health_subcenter_name),
        arrayId = -1,
//        etMaxLength = 6,
        required = false,
        allCaps = true,
        etInputType = TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val motherName = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.mother_name),
        arrayId = -1,
        required = false
    )
    private val fatherName = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.tv_father_name_ph),
        arrayId = -1,
        required = false
    )

    private val dateOfDelivery = FormElement(
        id = 6,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_date_of_delivery),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0
    )

    private val placeOfDelivery = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_place_of_delivery),
        arrayId = -1,
        entries =  resources.getStringArray(R.array.hbnc_place_of_delivery_array),
        required = false
    )
    private val gender = FormElement(
        id = 8, inputType = InputType.RADIO, title = resources.getString(R.string.str_baby_gender), arrayId = -1, entries = resources.getStringArray(R.array.hbnc_baby_gender), required = false
    )

    private val typeOfDelivery = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_type_of_delivery),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_type_of_delivery),
        required = false
    )
    private val startedBreastFeeding = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_started_breastfeeding),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_started_breastfeeding),
        required = false
    )
    private val weightAtBirth = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_weight_at_birth_gram),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL
    )
    private val dateOfDischargeFromHospitalMother = FormElement(
        id = 12,
        inputType = InputType.DATE_PICKER,
        title =  resources.getString(R.string.str_discharge_date_of_mother),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0
    )
    private val dateOfDischargeFromHospitalBaby = FormElement(
        id = 13,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_discharge_date_of_baby),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0
    )
    private val registrationOfBirth = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_registration_of_birth),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )

    private val childImmunizationStatus = FormElement(
        id = 18,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.str_child_immunizaation_status),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_child_immunization_status),
        required = false
    )

    private val babyFedAfterBirth = FormElement(
        id = 26,
        inputType = InputType.DROPDOWN,
        title =resources.getString(R.string.str_child_feed_after_birth),
        arrayId = -1,
        entries =resources.getStringArray(R.array.hbnc_feeding_of_baby),
        required = false,
        hasDependants = true
    )

    private val howBabyTookFirstFeed = FormElement(
        id = 27,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_how_did_the_baby_breastfeed),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_how_breastfeed_baby),
        required = false
    )
    private val babyEyeCondition = FormElement(
        id = 32,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_baby_eye_condition),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_baby_eye_condition),
        required = false
    )
    private val babyBleedUmbilicalCord = FormElement(
        id = 33,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_is_there_bleeding),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )
    private val babyWeightColor = FormElement(
        id = 34,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_weighing_machine_scale_coloer),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_scale_color),
        required = false
    )

    private val titleBabyPhysicalCondition = FormElement(
        id = 35,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.str_child_physical_condition),
        arrayId = -1,
        required = false
    )
    private val allLimbsLimp = FormElement(
        id = 36,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_all_limbs_limp),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val feedingLessStop = FormElement(
        id = 37,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_feeding_less_stop),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val wrapClothKeptMother = FormElement(
        id = 45,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_the_child_is_wrapped_in_cloth),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val onlyBreastMilk = FormElement(
        id = 46,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_started_breastfeeding_only),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )


////////////////////// Newborn first training (A) ask mother

    private val titleAskMotherA = FormElement(
        id = 48,
        inputType = InputType.HEADLINE,
        title =  resources.getString(R.string.str_newborn_first_training),
        arrayId = -1,
        required = false
    )

    private val timesMotherFed24hr = FormElement(
        id = 49,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.str_baby_stomach_feeding),
        arrayId = -1,
        required = false,
        hasAlertError = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1
    )


    private val timesPadChanged = FormElement(
        id = 50,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_baby_pad_changed),
        arrayId = -1,
        required = false,
        hasAlertError = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2
    )

    private val babyKeptWarmWinter = FormElement(
        id = 51,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_baby_in_winter),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val babyBreastFedProperly = FormElement(
        id = 52,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_baby_breastfed_properly),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val babyCryContinuously = FormElement(
        id = 53,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_baby_urination_per_day),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val motherBodyTemperature = FormElement(
        id = 55,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_mother_body_temperature),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3
    )
    private val motherWaterDischarge = FormElement(
        id = 56,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_water_discharge),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val motherSpeakAbnormalFits = FormElement(
        id = 57,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_is_mother_speaking_abnormally),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val motherNoOrLessMilk = FormElement(
        id = 58,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_mother_milk_is_not_produced),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val motherBreastProblem = FormElement(
        id = 59,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_mother_cracked_nipple),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val babyEyesSwollen = FormElement(
        id = 61,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_are_the_eyes_swollen),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val babyWeight = FormElement(
        id = 62,
        inputType = InputType.EDIT_TEXT,
        title = "${resources.getString(R.string.str_weight_on_day)} ${if (nthDay > 0) nthDay else 1}",
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 4,
        minDecimal = 0.5,
        maxDecimal = 7.0,
    )
    private val yellowJaundice = FormElement(
        id = 66,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_yellowing_of_eye),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false
    )


    private val breathFast = FormElement(
        id = 68,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_respiratory_rate),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val titleSepsisD = FormElement(
        id = 70,
        inputType = InputType.HEADLINE,
        title =resources.getString(R.string.str_d_sepsis),
        subtitle = resources.getString(R.string.str_symptoms_of_sepsis),
        arrayId = -1,
        required = false
    )

    private val bloatedStomach = FormElement(
        id = 74,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_distended_abdomen),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )
    private val childColdOnTouch = FormElement(
        id = 75,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_child_feels_cold),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )

    private val childChestDrawing = FormElement(
        id = 76,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_chest_is_pulled),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )


    private val pusNavel = FormElement(
        id = 77,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.str_pus_in_the_navel),
        arrayId = -1,
        required = false
    )
    private val ashaName = FormElement(
        id = 78,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.str_asha_name),
        arrayId = -1,
        required = false
    )
    private val villageName = FormElement(
        id = 79,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.str_village_name),
        arrayId = -1,
        required = false
    )
    private val blockName = FormElement(
        id = 80,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.str_block_name),
        arrayId = -1,
        required = false
    )
    private val stillBirth = FormElement(
        id = 81,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_still_birth),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )
    private val supRemark = FormElement(
        id = 82,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.str_supervisors_remark),
        arrayId = -1,
        required = false,
        etMaxLength = 500,
        multiLine = true
    )
    private val sup = FormElement(
        id = 83,
        inputType = InputType.DROPDOWN,
        title =  resources.getString(R.string.str_supervisor),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_supervisor),
        required = false
    )
    private val supName = FormElement(
        id = 84,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_supervisor_name),
        arrayId = -1,
        required = false,
        allCaps = true,
        etMaxLength = 100
    )
    private val dateOfSupSig = FormElement(
        id = 86,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_signature_with_date),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )

    private val titleVisitCard = FormElement(
        id = 87,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.str_mother_newborn_home_visit_card),
        arrayId = -1,
        required = false
    )
    private val titleVisitCardDischarge = FormElement(
        id = 88,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.str_discharge_of_institutinal_delivery),
        arrayId = -1,
        required = false
    )

    private val dateOfHomeVisit = FormElement(
        id = 89,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.str_date_of_home_visit),
        arrayId = -1,
        required = false
    )
    private val babyAlive = FormElement(
        id = 90,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_baby_alive),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false,
        hasDependants = true
    )
    private val dateOfBabyDeath = FormElement(
        id = 91,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_date_of_death_of_baby),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val timeOfBabyDeath = FormElement(
        id = 92,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.str_time_of_death),
        arrayId = -1,
        required = false
    )
    private val placeOfBabyDeath = FormElement(
        id = 93,
        inputType = InputType.DROPDOWN,
        title =  resources.getString(R.string.str_place_of_baby_death),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_place_of_baby_death),
        required = false,
        hasDependants = true,
    )
    private val otherPlaceOfBabyDeath = FormElement(
        id = 94,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_other_place_of_baby_death),
        arrayId = -1,
        required = false
    )
    private val babyPreterm = FormElement(
        id = 95,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_is_baby_preterm),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false,
        hasDependants = true,
        hasAlertError = true,
    )
    private val gestationalAge = FormElement(
        id = 96,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_how_many_weeks),
//        orientation = LinearLayout.VERTICAL,
        arrayId = -1,
        entries =  resources.getStringArray(R.array.hbnc_gestational_age),
        required = true,
        hasAlertError = true,
    )
    private val dateOfBabyFirstExamination = FormElement(
        id = 97,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_date_of_first_examination),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val timeOfBabyFirstExamination = FormElement(
        id = 98,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.str_time_of_first_examination),
        arrayId = -1,
        required = false
    )


    private val motherAlive = FormElement(
        id = 99,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_is_the_mother_alive),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false,
        hasDependants = true
    )
    private val dateOfMotherDeath = FormElement(
        id = 100,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_date_of_death_of_mother),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val timeOfMotherDeath = FormElement(
        id = 101,
        inputType = InputType.TIME_PICKER,
        title =  resources.getString(R.string.str_time_of_death_of_mother),
        arrayId = -1,
        required = false
    )
    private val placeOfMotherDeath = FormElement(
        id = 102,
        inputType = InputType.DROPDOWN,
        title =   resources.getString(R.string.str_place_of_mother_death),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_place_of_mother_death),
        required = false,
        hasDependants = true,
    )
    private val otherPlaceOfMotherDeath = FormElement(
        id = 103,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_other_place_of_mother_death),
        arrayId = -1,
        required = false,
        hasDependants = true
    )
    private val motherProblems = FormElement(
        id = 104,
        inputType = InputType.CHECKBOXES,
        title = resources.getString(R.string.str_does_mother_have_any_problem),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_mother_problems),
        required = false,
        hasAlertError = true
    )

    private val otherBabyFedAfterBirth = FormElement(
        id = 105,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.str_first_feed_to_baby),
        arrayId = -1,
        required = false
    )
    private val whenBabyFirstFed = FormElement(
        id = 106,
        inputType = InputType.TIME_PICKER,
        title = resources.getString(R.string.str_when_first_feed_to_baby),
        arrayId = -1,
        required = false
    )
    private val motherHasBreastFeedProblem = FormElement(
        id = 107,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_does_the_mother_have_breatfeeding),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true,
    )
    private val motherBreastFeedProblem = FormElement(
        id = 108,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_write_the_problem),
        arrayId = -1,
        required = false
    )


    ///////////////////////////Part II////////////////////////////
    private val titleBabyFirstHealthCheckup = FormElement(
        id = 109,
        inputType = InputType.HEADLINE,
        title =resources.getString(R.string.str_baby_first_health_checkup),
        arrayId = -1,
        required = false
    )
    private val babyTemperature = FormElement(
        id = 110,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_temperature_ot_the_baby),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3
    )

    private val actionUmbilicalBleed = FormElement(
        id = 111,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_tie_again_with_clean_thread),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )
    private val babyWeigntMatchesColor = FormElement(
        id = 112,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_weighing_matches),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )
    private val titleRoutineNewBornCare = FormElement(
        id = 113,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.str_routine_newborn_care),
        arrayId = -1,
        required = false
    )
    private val babyDry = FormElement(
        id = 114,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_dry_the_baby),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )
    private val cryWeakStopped = FormElement(
        id = 115,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_cry_weak_stopped),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )
    private val cordCleanDry = FormElement(
        id = 116,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_keep_the_cord_clean_and_dry),
        arrayId = -1,
        entries = arrayOf(
            "Yes",
            "No",
        ),
        required = false
    )

    private val unusualWithBaby = FormElement(
        id = 117,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_was_there_anything_unusual),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_unusual_with_the_baby),
        required = false,
        hasDependants = true,
    )
    private val otherUnusualWithBaby = FormElement(
        id = 118,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_other_unusual_with_the_baby),
        arrayId = -1,
        required = false
    )

/////////////// Part Visit //////////

    private val titleWashHands = FormElement(
        id = 119,
        inputType = InputType.HEADLINE,
        title = resources.getString(R.string.str_asha_hygeine),
        arrayId = -1,
        required = false
    )
    private val babyReferred = FormElement(
        id = 120,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.str_baby_referred_for_any_reason),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )
    private val dateOfBabyReferral = FormElement(
        id = 121,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_date_of_baby_referral),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val placeOfBabyReferral = FormElement(
        id = 122,
        inputType = InputType.DROPDOWN,
        title = resources.getString(R.string.str_place_of_baby_referral),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_place_of_baby_referral),
        required = false,
        hasDependants = true
    )
    private val otherPlaceOfBabyReferral = FormElement(
        id = 123,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.str_other_place_of_baby_referral),
        arrayId = -1,
        required = false
    )
    private val motherReferred = FormElement(
        id = 124,
        inputType = InputType.RADIO,
        title =  resources.getString(R.string.str_mother_referred_for_any_reason),
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )
    private val dateOfMotherReferral = FormElement(
        id = 125,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_date_of_mother_referral),
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val placeOfMotherReferral = FormElement(
        id = 126,
        inputType = InputType.DROPDOWN,
        title =  resources.getString(R.string.str_place_of_mother_referral),
        arrayId = -1,
        entries = resources.getStringArray(R.array.hbnc_place_of_mother_referral),
        required = false,
        hasDependants = true
    )
    private val otherPlaceOfMotherReferral = FormElement(
        id = 127,
        inputType = InputType.EDIT_TEXT,
        title =  resources.getString(R.string.str_other_place_of_mother_referral),
        arrayId = -1,
        required = false
    )


    private val cardPage by lazy {
        listOf(
            titleVisitCard,
            ashaName,
            villageName,
            healthSubCenterName,
            blockName,
            motherName,
            fatherName,
            dateOfDelivery,
            placeOfDelivery,
            gender,
            typeOfDelivery,
            stillBirth,
            startedBreastFeeding,
            titleVisitCardDischarge,
            dateOfDischargeFromHospitalMother,
            dateOfDischargeFromHospitalBaby,
            weightAtBirth,
            registrationOfBirth
        )
    }


    private fun setExistingValuesForCardPage(visitCard: HbncVisitCard?) {
        visitCard?.let {
            ashaName.value = it.ashaName
            villageName.value = it.villageName
            blockName.value = it.blockName
            motherName.value = it.motherName
            fatherName.value = it.fatherName
            placeOfDelivery.value = placeOfDelivery.getStringFromPosition(it.placeOfDelivery)
            gender.value = gender.getStringFromPosition(it.babyGender)
            typeOfDelivery.value = typeOfDelivery.getStringFromPosition(it.typeOfDelivery)
            dateOfDelivery.value = getDateFromLong(it.dateOfDelivery)
            healthSubCenterName.value = it.subCenterName
            dateOfDelivery.value = getDateFromLong(it.dateOfDelivery)
            gender.value = gender.entries?.get(it.babyGender)
            stillBirth.value = stillBirth.getStringFromPosition(it.stillBirth)
            startedBreastFeeding.value =
                startedBreastFeeding.getStringFromPosition(it.startedBreastFeeding)
            dateOfDischargeFromHospitalMother.value = getDateFromLong(it.dischargeDateMother)
            dateOfDischargeFromHospitalBaby.value = getDateFromLong(it.dischargeDateMother)
            weightAtBirth.value = it.weightInGrams.toString()
            registrationOfBirth.value =
                registrationOfBirth.getStringFromPosition(it.registrationOfBirth)

        }

    }


    private val partIPage by lazy {
        listOf(
            dateOfHomeVisit,
            babyAlive,
            babyPreterm,
            dateOfBabyFirstExamination,
            timeOfBabyFirstExamination,
            motherAlive,
            motherProblems,
            babyFedAfterBirth,
            whenBabyFirstFed,
            howBabyTookFirstFeed,
            motherHasBreastFeedProblem,
        )
    }


    private fun setExistingValuesForPartIPage(
        hbncPartI: HbncPartI?, list: MutableList<FormElement>
    ) {
        hbncPartI?.let {
            dateOfHomeVisit.value = getDateFromLong(it.dateOfVisit)
            babyAlive.value = babyAlive.getStringFromPosition(it.babyAlive)
            dateOfBabyDeath.value = getDateFromLong(it.dateOfBabyDeath)
            timeOfBabyDeath.value = it.timeOfBabyDeath
            placeOfBabyDeath.value = placeOfBabyDeath.getStringFromPosition(it.placeOfBabyDeath)
            otherPlaceOfBabyDeath.value = it.otherPlaceOfBabyDeath
            dateOfMotherDeath.value = getDateFromLong(it.dateOfMotherDeath)
            timeOfMotherDeath.value = it.timeOfMotherDeath
            placeOfMotherDeath.value =
                placeOfMotherDeath.getStringFromPosition(it.placeOfMotherDeath)
            otherPlaceOfMotherDeath.value = it.otherPlaceOfMotherDeath
            babyPreterm.value = babyPreterm.getStringFromPosition(it.isBabyPreterm)
            gestationalAge.value = gestationalAge.getStringFromPosition(it.gestationalAge)
            dateOfBabyFirstExamination.value = getDateFromLong(it.dateOfFirstExamination)
            timeOfBabyFirstExamination.value = it.timeOfFirstExamination
            motherAlive.value = motherAlive.getStringFromPosition(it.motherAlive)
            motherProblems.value = it.motherAnyProblem
            babyFedAfterBirth.value = babyFedAfterBirth.getStringFromPosition(it.babyFirstFed)
            otherBabyFedAfterBirth.value = it.otherBabyFirstFed
            whenBabyFirstFed.value = it.timeBabyFirstFed
            howBabyTookFirstFeed.value =
                howBabyTookFirstFeed.getStringFromPosition(it.howBabyTookFirstFeed)
            motherHasBreastFeedProblem.value =
                motherHasBreastFeedProblem.getStringFromPosition(it.motherHasBreastFeedProblem)
            motherBreastProblem.value = it.motherBreastFeedProblem
            addNecessaryDependantFieldsToListForPart1(it, list)
        }

    }

    private fun addNecessaryDependantFieldsToListForPart1(
        hbncPartI: HbncPartI, list: MutableList<FormElement>
    ) {
        hbncPartI.let {
            if (it.babyAlive == 2) {
                list.addAll(
                    list.indexOf(babyAlive) + 1, listOf(
                        dateOfBabyDeath,
                        timeOfBabyDeath,
                        placeOfBabyDeath,
                    )
                )
                if (it.placeOfBabyDeath == (placeOfBabyDeath.entries!!.size)) list.add(
                    list.indexOf(
                        placeOfBabyDeath
                    ) + 1, otherPlaceOfBabyDeath
                )

            }
            if (it.motherAlive == 2) {
                list.addAll(
                    list.indexOf(motherAlive) + 1, listOf(
                        dateOfMotherDeath, timeOfMotherDeath, placeOfMotherDeath
                    )
                )
                if (it.placeOfMotherDeath == (placeOfMotherDeath.entries!!.size)) list.add(
                    list.indexOf(
                        placeOfMotherDeath
                    ) + 1, otherPlaceOfMotherDeath
                )
            }
            if (it.isBabyPreterm == 1) {
                list.add(list.indexOf(babyPreterm) + 1, gestationalAge)
            }
            if (it.babyFirstFed == babyFedAfterBirth.entries!!.size) list.add(
                list.indexOf(
                    babyFedAfterBirth
                ) + 1, otherBabyFedAfterBirth
            )
            if (it.motherHasBreastFeedProblem == 1) list.add(
                list.indexOf(motherHasBreastFeedProblem) + 1, motherBreastFeedProblem
            )

        }
    }


    private val partIIPage by lazy {
        listOf(
            dateOfHomeVisit,
            titleBabyFirstHealthCheckup,
            babyTemperature,
            babyEyeCondition,
            babyBleedUmbilicalCord,
            actionUmbilicalBleed,
            babyWeight,
            babyWeigntMatchesColor,
            babyWeightColor,
            titleBabyPhysicalCondition,
            allLimbsLimp,
            feedingLessStop,
            cryWeakStopped,
            titleRoutineNewBornCare,
            babyDry,
            wrapClothKeptMother,
            onlyBreastMilk,
            cordCleanDry,
            unusualWithBaby
        )
    }

    private fun setExistingValuesForPartIIPage(part2: HbncPartII?, list: MutableList<FormElement>) {
        part2?.let {
            dateOfHomeVisit.value = getDateFromLong(it.dateOfVisit)
            babyTemperature.value = it.babyTemperature
            babyEyeCondition.value = babyEyeCondition.getStringFromPosition(it.babyEyeCondition)
            babyBleedUmbilicalCord.value =
                babyBleedUmbilicalCord.getStringFromPosition(it.babyUmbilicalBleed)
            actionUmbilicalBleed.value =
                actionUmbilicalBleed.getStringFromPosition(it.actionBabyUmbilicalBleed)
            babyWeight.value = it.babyWeight
            babyWeigntMatchesColor.value =
                babyWeigntMatchesColor.getStringFromPosition(it.babyWeightMatchesColor)
            babyWeightColor.value = babyWeightColor.getStringFromPosition(it.babyWeightColorOnScale)
            allLimbsLimp.value = allLimbsLimp.getStringFromPosition(it.allLimbsLimp)
            feedingLessStop.value = feedingLessStop.getStringFromPosition(it.feedLessStop)
            cryWeakStopped.value = cryWeakStopped.getStringFromPosition(it.cryWeakStop)
            babyDry.value = babyDry.getStringFromPosition(it.dryBaby)
            wrapClothKeptMother.value =
                wrapClothKeptMother.getStringFromPosition(it.wrapClothCloseToMother)
            onlyBreastMilk.value = onlyBreastMilk.getStringFromPosition(it.exclusiveBreastFeeding)
            cordCleanDry.value = cordCleanDry.getStringFromPosition(it.cordCleanDry)
            unusualWithBaby.value = unusualWithBaby.getStringFromPosition(it.unusualInBaby)
            otherUnusualWithBaby.value = it.otherUnusualInBaby
            addNecessaryDependantFieldsToListForPart2(it, list)
        }

    }

    private fun addNecessaryDependantFieldsToListForPart2(
        part2: HbncPartII, list: MutableList<FormElement>
    ) {
        part2.let {
            if (it.unusualInBaby == unusualWithBaby.entries!!.size) list.add(
                list.indexOf(
                    unusualWithBaby
                ) + 1, otherUnusualWithBaby
            )
        }

    }


    private val visitPage by lazy {
        listOf(
            dateOfHomeVisit,
            titleAskMotherA,
            babyAlive,
            timesMotherFed24hr,
            timesPadChanged,
            babyKeptWarmWinter,
            babyBreastFedProperly,
            babyCryContinuously,
            motherBodyTemperature,
            motherWaterDischarge,
            motherSpeakAbnormalFits,
            motherNoOrLessMilk,
            motherBreastProblem,

            titleWashHands,
            babyEyesSwollen,
            babyWeight,
            babyTemperature,
            yellowJaundice,
            childImmunizationStatus,
            babyReferred,
            motherReferred,
            titleSepsisD,
            allLimbsLimp,
            feedingLessStop,
            cryWeakStopped,
            bloatedStomach,
            childColdOnTouch,
            childChestDrawing,
            breathFast,
            pusNavel,
            sup,
            supName,
            supRemark,
            dateOfSupSig,
        )
    }

    private fun setExistingValuesForVisitPage(
        visit: HbncHomeVisit?, list: MutableList<FormElement>
    ) {
        visit?.let {
            dateOfHomeVisit.value = getDateFromLong(it.dateOfVisit)
            babyAlive.value = babyAlive.getStringFromPosition(it.babyAlive)
            timesMotherFed24hr.value = it.numTimesFullMeal24hr.toString()
            timesPadChanged.value = it.numPadChanged24hr.toString()
            babyKeptWarmWinter.value =
                babyKeptWarmWinter.getStringFromPosition(it.babyKeptWarmWinter)
            babyBreastFedProperly.value =
                babyBreastFedProperly.getStringFromPosition(it.babyFedProperly)
            babyCryContinuously.value =
                babyCryContinuously.getStringFromPosition(it.babyCryContinuously)
            motherBodyTemperature.value = it.motherTemperature
            motherWaterDischarge.value =
                motherWaterDischarge.getStringFromPosition(it.foulDischargeFever)
            motherSpeakAbnormalFits.value =
                motherSpeakAbnormalFits.getStringFromPosition(it.motherSpeakAbnormallyFits)
            motherNoOrLessMilk.value = motherNoOrLessMilk.getStringFromPosition(it.motherLessNoMilk)
            motherBreastProblem.value =
                motherBreastProblem.getStringFromPosition(it.motherBreastProblem)
            babyEyesSwollen.value = babyEyesSwollen.getStringFromPosition(it.babyEyesSwollen)
            babyWeight.value = it.babyWeight
            babyTemperature.value = it.babyTemperature
            yellowJaundice.value = yellowJaundice.getStringFromPosition(it.babyYellow)
            childImmunizationStatus.value = it.babyImmunizationStatus
            babyReferred.value = babyReferred.getStringFromPosition(it.babyReferred)
            motherReferred.value = motherReferred.getStringFromPosition(it.motherReferred)
            allLimbsLimp.value = allLimbsLimp.getStringFromPosition(it.allLimbsLimp)
            feedingLessStop.value = feedingLessStop.getStringFromPosition(it.feedingLessStopped)
            cryWeakStopped.value = cryWeakStopped.getStringFromPosition(it.cryWeakStopped)
            bloatedStomach.value = bloatedStomach.getStringFromPosition(it.bloatedStomach)
            childColdOnTouch.value = childColdOnTouch.getStringFromPosition(it.coldOnTouch)
            childChestDrawing.value = childChestDrawing.getStringFromPosition(it.chestDrawing)
            breathFast.value = breathFast.getStringFromPosition(it.breathFast)
            pusNavel.value = pusNavel.getStringFromPosition(it.pusNavel)
            sup.value = sup.getStringFromPosition(it.sup)
            supName.value = it.supName
            supRemark.value = it.supComment
            dateOfSupSig.value = getDateFromLong(it.supSignDate)
            addNecessaryDependantFieldsToListForVisit(it, list)
        }

    }

    private fun addNecessaryDependantFieldsToListForVisit(
        visit: HbncHomeVisit, list: MutableList<FormElement>
    ) {
        if (visit.babyReferred == 1) {
            list.addAll(
                listOf(
                    dateOfBabyReferral, placeOfBabyReferral
                )
            )
            if (visit.placeOfBabyReferral == placeOfBabyReferral.entries!!.size) list.add(
                list.indexOf(
                    placeOfBabyReferral
                ) + 1, otherPlaceOfBabyReferral
            )
        }
        if (visit.motherReferred == 1) {
            list.addAll(
                listOf(
                    dateOfMotherReferral, placeOfMotherReferral
                )
            )
            if (visit.placeOfMotherReferral == placeOfMotherReferral.entries!!.size) list.add(
                list.indexOf(
                    placeOfMotherReferral
                ) + 1, otherPlaceOfMotherReferral
            )
        }
    }
}

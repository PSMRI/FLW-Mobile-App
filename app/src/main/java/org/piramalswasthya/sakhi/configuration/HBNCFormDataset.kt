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
                        healthSubCenterName.errorText = "Yay, it is working!!!"
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
        title = "Health Subcenter Name ",
        arrayId = -1,
//        etMaxLength = 6,
        required = false,
        allCaps = true,
        etInputType = TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val motherName = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = "Mother Name",
        arrayId = -1,
        required = false
    )
    private val fatherName = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = "Father Name",
        arrayId = -1,
        required = false
    )

    private val dateOfDelivery = FormElement(
        id = 6,
        inputType = InputType.DATE_PICKER,
        title = "Date of Delivery",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0
    )

    private val placeOfDelivery = FormElement(
        id = 7,
        inputType = InputType.DROPDOWN,
        title = "Place of Delivery",
        arrayId = -1,
        entries = arrayOf(
            "House",
            "Health center",
            "CHC",
            "PHC",
        ),
        required = false
    )
    private val gender = FormElement(
        id = 8, inputType = InputType.RADIO, title = "Baby Gender", arrayId = -1, entries = arrayOf(
            "Male",
            "Female",
            "Transgender",
        ), required = false
    )

    private val typeOfDelivery = FormElement(
        id = 9,
        inputType = InputType.RADIO,
        title = "Type of Delivery",
        arrayId = -1,
        entries = arrayOf(
            "Normal Delivery", "C - Section", "Assisted"
        ),
        required = false
    )
    private val startedBreastFeeding = FormElement(
        id = 10,
        inputType = InputType.DROPDOWN,
        title = "Started Breastfeeding",
        arrayId = -1,
        entries = arrayOf(
            "Within an hour", "1 - 4 hours", "4.1 - 24 hours", "After 24 hours"
        ),
        required = false
    )
    private val weightAtBirth = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = "Weight at birth ( grams )",
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL
    )
    private val dateOfDischargeFromHospitalMother = FormElement(
        id = 12,
        inputType = InputType.DATE_PICKER,
        title = "Discharge Date of Mother",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0
    )
    private val dateOfDischargeFromHospitalBaby = FormElement(
        id = 13,
        inputType = InputType.DATE_PICKER,
        title = "Discharge Date of Baby",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0
    )
    private val registrationOfBirth = FormElement(
        id = 15,
        inputType = InputType.RADIO,
        title = "Registration Of Birth",
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
        title = "Child Immunization Status",
        arrayId = -1,
        entries = arrayOf(
            "BCG", "Polio", "DPT 1", "Hepatitis-B"
        ),
        required = false
    )

    private val babyFedAfterBirth = FormElement(
        id = 26,
        inputType = InputType.DROPDOWN,
        title = "What was the baby fed after birth ",
        arrayId = -1,
        entries = arrayOf(
            "Mother Milk",
            "Water",
            "Honey",
            "Mishri water",
            "Goat Milk",
            "Other",
        ),
        required = false,
        hasDependants = true
    )

    private val howBabyTookFirstFeed = FormElement(
        id = 27,
        inputType = InputType.DROPDOWN,
        title = "How did the baby breastfeed? ",
        arrayId = -1,
        entries = arrayOf(
            "Forcefully",
            "Weakly ",
            "Could not breastfeed but had to be fed with spoon",
            "Could neither breast-feed nor could take milk given by spoon",
        ),
        required = false
    )
    private val babyEyeCondition = FormElement(
        id = 32,
        inputType = InputType.RADIO,
        title = "Baby eye condition",
        arrayId = -1,
        entries = arrayOf(
            "Normal ", "Swelling", "oozing pus"
        ),
        required = false
    )
    private val babyBleedUmbilicalCord = FormElement(
        id = 33,
        inputType = InputType.RADIO,
        title = "Is there bleeding from the baby umbilical cord ",
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
        title = "Weighing machine scale color",
        arrayId = -1,
        entries = arrayOf(
            "Red", "Yellow", "Green"
        ),
        required = false
    )

    private val titleBabyPhysicalCondition = FormElement(
        id = 35,
        inputType = InputType.HEADLINE,
        title = "Enter the child physical condition",
        arrayId = -1,
        required = false
    )
    private val allLimbsLimp = FormElement(
        id = 36,
        inputType = InputType.RADIO,
        title = "All limbs limp",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val feedingLessStop = FormElement(
        id = 37,
        inputType = InputType.RADIO,
        title = "Feeding less/stop",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val wrapClothKeptMother = FormElement(
        id = 45,
        inputType = InputType.RADIO,
        title = "The child is wrapped in cloth and kept to the mother",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val onlyBreastMilk = FormElement(
        id = 46,
        inputType = InputType.RADIO,
        title = "Started breastfeeding only/ only given breast milk",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )


////////////////////// Newborn first training (A) ask mother

    private val titleAskMotherA = FormElement(
        id = 48,
        inputType = InputType.HEADLINE,
        title = "Newborn first training (A) Ask mother",
        arrayId = -1,
        required = false
    )

    private val timesMotherFed24hr = FormElement(
        id = 49,
        inputType = InputType.EDIT_TEXT,
        title = "How many times the mother feeds her stomach in 24 hours. Action – If the mother does not eat full stomach or eat less than 4 times, advise mother to do so",
        arrayId = -1,
        required = false,
        hasAlertError = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1
    )


    private val timesPadChanged = FormElement(
        id = 50,
        inputType = InputType.EDIT_TEXT,
        title = "How many pads have been changed in a day for bleeding? Action – If more than 2 pad, refer the mother to the hospital.",
        arrayId = -1,
        required = false,
        hasAlertError = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2
    )

    private val babyKeptWarmWinter = FormElement(
        id = 51,
        inputType = InputType.RADIO,
        title = "During the winter season, is the baby kept warm? (Closer to the mother, dressed well and wrapped). - If it is not being done, ask the mother to do it.",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val babyBreastFedProperly = FormElement(
        id = 52,
        inputType = InputType.RADIO,
        title = "Is the child breastfed properly? (Whenever feeling hungry or breastfeeding at least 7 – 8 times in 24 hours). Action – if it is not being done then ask the mother to do it. ",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val babyCryContinuously = FormElement(
        id = 53,
        inputType = InputType.RADIO,
        title = "Does the child cry continuously or urinate less than 6 times a day? Action – Advice the mother for breast-feeding",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val motherBodyTemperature = FormElement(
        id = 55,
        inputType = InputType.EDIT_TEXT,
        title = "Measure and check the temperature. Action – Give the patient paracetamol tablet if the temperature is 102°F (38.9°C) and refer to the hospital if the temperature is higher than this.",
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3
    )
    private val motherWaterDischarge = FormElement(
        id = 56,
        inputType = InputType.RADIO,
        title = "Water discharge with foul smell and fever 102 degree Fahrenheit (38.9 degree C). ",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val motherSpeakAbnormalFits = FormElement(
        id = 57,
        inputType = InputType.RADIO,
        title = "Is mother speaking abnormally or having fits?",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val motherNoOrLessMilk = FormElement(
        id = 58,
        inputType = InputType.RADIO,
        title = "Mothers milk is not being produced after delivery or she thinks less milk is being produced.",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val motherBreastProblem = FormElement(
        id = 59,
        inputType = InputType.RADIO,
        title = "Does the mother have cracked nipple / pain and / or hard breasts",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val babyEyesSwollen = FormElement(
        id = 61,
        inputType = InputType.RADIO,
        title = "Are the eyes swollen / Are there pus from the eyes?",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )
    private val babyWeight = FormElement(
        id = 62,
        inputType = InputType.EDIT_TEXT,
        title = "Weight on Day ${if (nthDay > 0) nthDay else 1}",
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
        title = "Yellowing of the eye/palm/sole/skin (jaundice)",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false
    )


    private val breathFast = FormElement(
        id = 68,
        inputType = InputType.RADIO,
        title = "Respiratory rate more than 60 per minute",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
    )

    private val titleSepsisD = FormElement(
        id = 70,
        inputType = InputType.HEADLINE,
        title = "(D) Sepsis",
        subtitle = "Examine the following symptoms of sepsis. If symptoms are present, then write, Yes, if symptoms are not present, then do not write. Enter the symptoms seen from the health check-up on the first day of the newborns birth.",
        arrayId = -1,
        required = false
    )

    private val bloatedStomach = FormElement(
        id = 74,
        inputType = InputType.RADIO,
        title = "Distended abdomen or mother says baby vomits often",
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
        title = "The mother tells that the child feels cold when touching or the temperature of the child is more than 89 degrees Fahrenheit (37.5 degrees C)",
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
        title = "and the chest is pulled inward while breathing.",
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
        title = "Pus in the navel",
        arrayId = -1,
        required = false
    )
    private val ashaName = FormElement(
        id = 78,
        inputType = InputType.TEXT_VIEW,
        title = "ASHA NAME",
        arrayId = -1,
        required = false
    )
    private val villageName = FormElement(
        id = 79,
        inputType = InputType.TEXT_VIEW,
        title = "Village Name",
        arrayId = -1,
        required = false
    )
    private val blockName = FormElement(
        id = 80,
        inputType = InputType.TEXT_VIEW,
        title = "Block Name",
        arrayId = -1,
        required = false
    )
    private val stillBirth = FormElement(
        id = 81,
        inputType = InputType.RADIO,
        title = "Still Birth",
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
        title = "Supervisors Remark ",
        arrayId = -1,
        required = false,
        etMaxLength = 500,
        multiLine = true
    )
    private val sup = FormElement(
        id = 83,
        inputType = InputType.DROPDOWN,
        title = "Supervisor",
        arrayId = -1,
        entries = arrayOf(
            "ASHA Facilitator",
            "ANM",
            "MPW",
        ),
        required = false
    )
    private val supName = FormElement(
        id = 84,
        inputType = InputType.EDIT_TEXT,
        title = "Supervisor name",
        arrayId = -1,
        required = false,
        allCaps = true,
        etMaxLength = 100
    )
    private val dateOfSupSig = FormElement(
        id = 86,
        inputType = InputType.DATE_PICKER,
        title = "Signature with Date of Supervisor",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )

    private val titleVisitCard = FormElement(
        id = 87,
        inputType = InputType.HEADLINE,
        title = "Mother-Newborn Home Visit Card",
        arrayId = -1,
        required = false
    )
    private val titleVisitCardDischarge = FormElement(
        id = 88,
        inputType = InputType.HEADLINE,
        title = "Discharge of Institutional Delivery",
        arrayId = -1,
        required = false
    )

    private val dateOfHomeVisit = FormElement(
        id = 89,
        inputType = InputType.TEXT_VIEW,
        title = "Date of Home Visit",
        arrayId = -1,
        required = false
    )
    private val babyAlive = FormElement(
        id = 90,
        inputType = InputType.RADIO,
        title = "Is the baby alive?",
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
        title = "Date of death of baby",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val timeOfBabyDeath = FormElement(
        id = 92,
        inputType = InputType.TIME_PICKER,
        title = "Time of death of baby",
        arrayId = -1,
        required = false
    )
    private val placeOfBabyDeath = FormElement(
        id = 93,
        inputType = InputType.DROPDOWN,
        title = "Place of Baby Death",
        arrayId = -1,
        entries = arrayOf(
            "Home",
            "Sub-center",
            "PHC",
            "CHC",
            "Other",
        ),
        required = false,
        hasDependants = true,
    )
    private val otherPlaceOfBabyDeath = FormElement(
        id = 94,
        inputType = InputType.EDIT_TEXT,
        title = "Other place of Baby Death",
        arrayId = -1,
        required = false
    )
    private val babyPreterm = FormElement(
        id = 95,
        inputType = InputType.RADIO,
        title = "Is the baby preterm?",
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
        title = "How many weeks has it been since baby born (Gestational Age)",
//        orientation = LinearLayout.VERTICAL,
        arrayId = -1,
        entries = arrayOf(
            "24 – 34 Weeks",
            "34 – 36 Weeks",
            "36 – 38 Weeks",
        ),
        required = true,
        hasAlertError = true,
    )
    private val dateOfBabyFirstExamination = FormElement(
        id = 97,
        inputType = InputType.DATE_PICKER,
        title = "Date of First examination of baby",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val timeOfBabyFirstExamination = FormElement(
        id = 98,
        inputType = InputType.TIME_PICKER,
        title = "Time of First examination of baby",
        arrayId = -1,
        required = false
    )


    private val motherAlive = FormElement(
        id = 99,
        inputType = InputType.RADIO,
        title = "Is the mother alive?",
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
        title = "Date of death of mother",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val timeOfMotherDeath = FormElement(
        id = 101,
        inputType = InputType.TIME_PICKER,
        title = "Time of death of mother",
        arrayId = -1,
        required = false
    )
    private val placeOfMotherDeath = FormElement(
        id = 102,
        inputType = InputType.DROPDOWN,
        title = "Place of mother Death",
        arrayId = -1,
        entries = arrayOf(
            "Home",
            "Sub-center",
            "PHC",
            "CHC",
            "Other",
        ),
        required = false,
        hasDependants = true,
    )
    private val otherPlaceOfMotherDeath = FormElement(
        id = 103,
        inputType = InputType.EDIT_TEXT,
        title = "Other place of mother Death",
        arrayId = -1,
        required = false,
        hasDependants = true
    )
    private val motherProblems = FormElement(
        id = 104,
        inputType = InputType.CHECKBOXES,
        title = "Does Mother have any problems",
        arrayId = -1,
        entries = arrayOf(
            "Excessive Bleeding",
            "Unconscious / Fits",
        ),
        required = false,
        hasAlertError = true
    )

    private val otherBabyFedAfterBirth = FormElement(
        id = 105,
        inputType = InputType.EDIT_TEXT,
        title = "Other - What was given as the first feed to baby after birth?",
        arrayId = -1,
        required = false
    )
    private val whenBabyFirstFed = FormElement(
        id = 106,
        inputType = InputType.TIME_PICKER,
        title = "When was the baby first fed",
        arrayId = -1,
        required = false
    )
    private val motherHasBreastFeedProblem = FormElement(
        id = 107,
        inputType = InputType.RADIO,
        title = "Does the mother have breastfeeding problem?",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true,
    )
    private val motherBreastFeedProblem = FormElement(
        id = 108,
        inputType = InputType.EDIT_TEXT,
        title = "Write the problem, if there is any problem in breast feeding, help the mother to overcome it",
        arrayId = -1,
        required = false
    )


    ///////////////////////////Part II////////////////////////////
    private val titleBabyFirstHealthCheckup = FormElement(
        id = 109,
        inputType = InputType.HEADLINE,
        title = "Part 2: Baby first health check-up",
        arrayId = -1,
        required = false
    )
    private val babyTemperature = FormElement(
        id = 110,
        inputType = InputType.EDIT_TEXT,
        title = "Temperature of the baby",
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3
    )

    private val actionUmbilicalBleed = FormElement(
        id = 111,
        inputType = InputType.RADIO,
        title = "If yes, either ASHA, ANM/MPW or TBA can tie again with a clean thread. Action taken? ",
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
        title = "Weighing matches with the colour?",
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
        title = "Routine Newborn Care: whether the task was performed",
        arrayId = -1,
        required = false
    )
    private val babyDry = FormElement(
        id = 114,
        inputType = InputType.RADIO,
        title = "Dry the baby",
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
        title = "Cry weak/ stopped",
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
        title = "Keep the cord clean and dry",
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
        title = "Was there anything unusual with the baby?",
        arrayId = -1,
        entries = arrayOf("Curved limbs", "cleft lip", "Other"),
        required = false,
        hasDependants = true,
    )
    private val otherUnusualWithBaby = FormElement(
        id = 118,
        inputType = InputType.EDIT_TEXT,
        title = "Other - unusual with the baby",
        arrayId = -1,
        required = false
    )

/////////////// Part Visit //////////

    private val titleWashHands = FormElement(
        id = 119,
        inputType = InputType.HEADLINE,
        title = "ASHA should wash hands with soap and water before touching the baby during each visit",
        arrayId = -1,
        required = false
    )
    private val babyReferred = FormElement(
        id = 120,
        inputType = InputType.RADIO,
        title = "Baby referred for any reason?",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )
    private val dateOfBabyReferral = FormElement(
        id = 121,
        inputType = InputType.DATE_PICKER,
        title = "Date of baby referral",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val placeOfBabyReferral = FormElement(
        id = 122,
        inputType = InputType.DROPDOWN,
        title = "Place of baby referral",
        arrayId = -1,
        entries = arrayOf(
            "Sub-Centre",
            "PHC",
            "CHC",
            "Sub-District Hospital",
            "District Hospital",
            "Medical College Hospital",
            "In Transit",
            "Private Hospital",
            "Accredited Private Hospital",
            "Other",
        ),
        required = false,
        hasDependants = true
    )
    private val otherPlaceOfBabyReferral = FormElement(
        id = 123,
        inputType = InputType.EDIT_TEXT,
        title = "Other -Place of baby referral",
        arrayId = -1,
        required = false
    )
    private val motherReferred = FormElement(
        id = 124,
        inputType = InputType.RADIO,
        title = "Mother referred for any reason?",
        arrayId = -1,
        entries = arrayOf("Yes", "No"),
        required = false,
        hasDependants = true
    )
    private val dateOfMotherReferral = FormElement(
        id = 125,
        inputType = InputType.DATE_PICKER,
        title = "Date of mother referral",
        arrayId = -1,
        required = false,
        max = System.currentTimeMillis(),
        min = 0L
    )
    private val placeOfMotherReferral = FormElement(
        id = 126,
        inputType = InputType.DROPDOWN,
        title = "Place of mother referral",
        arrayId = -1,
        entries = arrayOf(
            "Sub-Centre",
            "PHC",
            "CHC",
            "Sub-District Hospital",
            "District Hospital",
            "Medical College Hospital",
            "In Transit",
            "Private Hospital",
            "Accredited Private Hospital",
            "Other",
        ),
        required = false,
        hasDependants = true
    )
    private val otherPlaceOfMotherReferral = FormElement(
        id = 127,
        inputType = InputType.EDIT_TEXT,
        title = "Other -Place of mother referral",
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

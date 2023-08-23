package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.text.InputType
import android.util.Range
import android.widget.LinearLayout
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender.FEMALE
import org.piramalswasthya.sakhi.model.Gender.MALE
import org.piramalswasthya.sakhi.model.Gender.TRANSGENDER
import org.piramalswasthya.sakhi.model.InputType.CHECKBOXES
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.IMAGE_VIEW
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BenKidRegFormDataset(context: Context, language: Languages) : Dataset(context, language) {


    companion object {
        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

        private fun getMinDobMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -15)
            cal.add(Calendar.DAY_OF_MONTH, +1)
            return cal.timeInMillis
        }

        private fun getMaxDobMillis(): Long {
            return System.currentTimeMillis()
        }
    }

    private var familyHeadPhoneNo: String? = null

    //////////////////////////////////First Page////////////////////////////////////

    private val pic = FormElement(
        id = 1,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.nbr_image),
        subtitle = resources.getString(R.string.nbr_image_sub),
        arrayId = -1,
        required = false
    )

    private val dateOfReg = FormElement(
        id = 2,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.nbr_dor),
        arrayId = -1,
        required = true,
        min = getMinDateOfReg(),
        max = System.currentTimeMillis()
    )
    private val firstName = FormElement(
        id = 3,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_nb_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val lastName = FormElement(
        id = 4,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_nb_last_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
    )
    val ageUnit = FormElement(
        id = 5,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_nb_age_unit),
        arrayId = -1,
        entries = resources.getStringArray(R.array.nbr_age_unit_array),
        required = true,
        hasDependants = true,
    )
    val age = FormElement(
        id = 7,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_age),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etMaxLength = 2,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )
    val dob = FormElement(
        id = 8,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.nbr_dob),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )
    val gender = FormElement(
        id = 9,
        inputType = RADIO,
        title = resources.getString(R.string.nbr_gender),
        arrayId = -1,
        entries = resources.getStringArray(R.array.nbr_gender_array),
        required = true,
        hasDependants = true,
    )
    private val fatherName = FormElement(
        id = 10,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_father_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val motherName = FormElement(
        id = 11,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_mother_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    val mobileNoOfRelation = FormElement(
        id = 12,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_mobile_number_of),
        arrayId = R.array.nbr_mobile_no_relation_array,
        entries = resources.getStringArray(R.array.nbr_mobile_no_relation_array),
        required = true,
        hasDependants = true,
    )
    private val otherMobileNoOfRelation = FormElement(
        id = 13,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.other_mobile_number_of_kid),
        arrayId = -1,
        required = true
    )
    private val contactNumber = FormElement(
        id = 14,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nrb_contact_number),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )
    private val contactNumberFamilyHead = FormElement(
        id = 114,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.nrb_contact_number),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
    )


    private val relationToHeadListDefault = 
        resources.getStringArray(R.array.nbr_relationship_to_head)
    
    private val relationToHeadListMale =
        resources.getStringArray(R.array.nbr_relationship_to_head_male)
    
    private val relationToHeadListFemale =
        resources.getStringArray(R.array.nbr_relationship_to_head_female)
    
    private val relationToHead = FormElement(
        id = 15,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_rel_to_head),
        arrayId = -1,
        entries = relationToHeadListDefault,
        required = true,
        hasDependants = true,
    )
    private val otherRelationToHead = FormElement(
        id = 16,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_rel_to_head_other),
        arrayId = -1,
        required = true,
        allCaps = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val community = FormElement(
        id = 17,
        inputType = DROPDOWN,
        title = resources.getString(R.string.community),
        arrayId = R.array.community_array,
        entries = resources.getStringArray(R.array.community_array),
        required = true
    )
    val religion = FormElement(
        id = 18,
        inputType = DROPDOWN,
        title = resources.getString(R.string.religion),
        arrayId = R.array.religion_array,
        entries = resources.getStringArray(R.array.religion_array),
        required = true,
        hasDependants = true
    )
    private val otherReligion = FormElement(
        id = 19,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_religion_other),
        arrayId = -1,
        required = true,
        allCaps = true
    )

    private val childRegisteredAtAwc = FormElement(
        id = 20,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_awc),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = true
    )

    private val childRegisteredAtSchool = FormElement(
        id = 21,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_reg_school),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true
    )


    private val typeOfSchool = FormElement(
        id = 22,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_type_school),
        arrayId = R.array.school_type_array,
        entries = resources.getStringArray(R.array.school_type_array),
        required = true
    )


    val rchId = FormElement(
        id = 23,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_rch_id),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12,
        max = 999999999999,
        min = 100000000000

    )
    val firstPage by lazy {
        listOf(
            pic,
            dateOfReg,
            firstName,
            lastName,
            dob,
            age,
            ageUnit,
            gender,
            fatherName,
            motherName,
            relationToHead,
            mobileNoOfRelation,
            community,
            religion,
            rchId,
        )
    }

    suspend fun setFirstPage(ben: BenRegCache?, familyHeadPhoneNo: Long?) {
        val list = firstPage.toMutableList()
        this.familyHeadPhoneNo = familyHeadPhoneNo?.toString()
        if (dateOfReg.value == null)
            dateOfReg.value = getCurrentDateString()
        contactNumberFamilyHead.value = familyHeadPhoneNo?.toString()
        ben?.takeIf { !it.isDraft }?.let { saved ->
            pic.value = saved.userImage
            dateOfReg.value = getDateFromLong(saved.regDate)
            firstName.value = saved.firstName
            lastName.value = saved.lastName
            dob.value = getDateFromLong(saved.dob)
            age.value = BenBasicCache.getAgeFromDob(saved.dob).toString()
            ageUnit.value = ageUnit.getStringFromPosition(saved.ageUnitId)
            gender.value = gender.getStringFromPosition(saved.genderId)
            fatherName.value = saved.fatherName
            motherName.value = saved.motherName
            mobileNoOfRelation.value =
                mobileNoOfRelation.getStringFromPosition(saved.mobileNoOfRelationId)
            otherMobileNoOfRelation.value = saved.mobileOthers
            contactNumber.value = saved.contactNumber.toString()
            contactNumberFamilyHead.value = this.familyHeadPhoneNo
            relationToHead.value = relationToHeadListDefault[saved.familyHeadRelationPosition - 1]
            otherRelationToHead.value = saved.familyHeadRelationOther
            community.value = community.getStringFromPosition(saved.communityId)
            religion.value = religion.getStringFromPosition(saved.religionId)
            otherReligion.value = saved.religionOthers
            childRegisteredAtAwc.value = childRegisteredAtAwc.getStringFromPosition(
                saved.kidDetails?.childRegisteredSchoolId ?: 0
            )
//                saved.kidDetails?.childRegisteredAWCId?.takeIf { it > 0 }
//                    ?.let { childRegisteredAtAwc.entries?.get(it - 1) }
            childRegisteredAtSchool.value = childRegisteredAtSchool.getStringFromPosition(
                saved.kidDetails?.childRegisteredSchoolId ?: 0
            )
//                saved.kidDetails?.childRegisteredSchoolId?.takeIf { it > 0 }
//                    ?.let { childRegisteredAtSchool.entries?.get(it - 1) }
            typeOfSchool.value =
                typeOfSchool.getStringFromPosition(saved.kidDetails?.typeOfSchoolId ?: 0)
//                saved.kidDetails?.typeOfSchoolId?.takeIf { it > 0 }
//                ?.let { typeOfSchool.entries?.get(it - 1) }
            rchId.value = saved.rchId


            relationToHead.entries = when (saved.gender) {
                MALE -> relationToHeadListMale
                FEMALE -> relationToHeadListFemale
                TRANSGENDER -> relationToHeadListDefault
                null -> null
            }
            relationToHead.arrayId = when (saved.gender) {
                MALE -> R.array.nbr_relationship_to_head_male
                FEMALE -> R.array.nbr_relationship_to_head_female
                TRANSGENDER -> R.array.nbr_relationship_to_head
                null -> -1
            }
        }
        if (mobileNoOfRelation.value == mobileNoOfRelation.entries!!.last()) {
            list.add(list.indexOf(mobileNoOfRelation) + 1, otherMobileNoOfRelation)
        }
        if (mobileNoOfRelation.value == mobileNoOfRelation.entries!![4]) {
            list.add(list.indexOf(mobileNoOfRelation) + 1, contactNumberFamilyHead)
        } else
            list.add(list.indexOf(community), contactNumber)
        if (relationToHead.value == relationToHead.entries!!.last()) {
            list.add(list.indexOf(relationToHead) + 1, otherRelationToHead)
        }
        if (religion.value == religion.entries!![7]) {
            list.add(list.indexOf(religion) + 1, otherReligion)
        }
//        if (ageUnit.value == ageUnit.entries?.last() && (age.value?.toInt() ?: 0) in 3..5) {
//            list.add((list.indexOf(rchId)), childRegisteredAtAwc)
//        }
        if (ageUnit.value == ageUnit.entries?.last() && (age.value?.toInt() ?: 0) in 4..14) {
            list.add((list.indexOf(rchId)), childRegisteredAtSchool)
        }
        if (childRegisteredAtSchool.value == childRegisteredAtSchool.entries?.first()) list.add(
            list.indexOf(
                childRegisteredAtSchool
            ) + 1, typeOfSchool
        )
        setUpPage(list)
    }

    //////////////////////////////////////////Second Page///////////////////////////////////////////

    private val placeOfBirth = FormElement(
        id = 24,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_pob),
        arrayId = R.array.place_of_birth,
        entries = resources.getStringArray(R.array.place_of_birth),
        required = true,
        hasDependants = true
    )
    private val facility = FormElement(
        id = 25,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_facility),
        arrayId = R.array.facility_place,
        entries = resources.getStringArray(R.array.facility_place),
        required = true,
        hasDependants = true
    )
    private val otherFacility = FormElement(
        id = 26,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_pob_other_facility),
        arrayId = -1,
        required = true
    )
    private val otherPlaceOfBirth = FormElement(
        id = 27,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_pob_other),
        arrayId = -1,
        required = true
    )
    private val whoConductedDelivery = FormElement(
        id = 28,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_who_cond_del),
        arrayId = R.array.delivery_conducted,
        entries = resources.getStringArray(R.array.delivery_conducted),
        required = true,
        hasDependants = true
    )
    private val otherWhoConductedDelivery = FormElement(
        id = 29,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_who_cond_del_other),
        arrayId = -1,
        required = true
    )
    private val typeOfDelivery = FormElement(
        id = 30,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_type_del),
        arrayId = R.array.type_of_delivery,
        entries = resources.getStringArray(R.array.type_of_delivery),
        required = true
    )
    private val complicationsDuringDelivery = FormElement(
        id = 31,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_comp_del),
        arrayId = R.array.complications_array,
        entries = resources.getStringArray(R.array.complications_array),
        required = true,
        hasDependants = true
    )
    private val breastFeedWithin1Hr = FormElement(
        id = 32,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_feed_1_hr),
        arrayId = R.array.yes_no_donno,
        entries = resources.getStringArray(R.array.yes_no_donno),
        required = true
    )
    private val birthDose = FormElement(
        id = 33,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_birth_dose),
        arrayId = R.array.given_array,
        entries = resources.getStringArray(R.array.given_array),
        required = true,
        hasDependants = true
    )
    private val birthDoseGiven = FormElement(
        id = 34,
        inputType = CHECKBOXES,
        title = resources.getString(R.string.nbr_child_birth_dose_details),
        arrayId = R.array.birth_doses,
        entries = resources.getStringArray(R.array.birth_doses),
        required = true
    )

    private val term = FormElement(
        id = 35,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_term),
        arrayId = R.array.term_array,
        entries = resources.getStringArray(R.array.term_array),
        required = true,
        hasDependants = true
    )

    private val termGestationalAge = FormElement(
        id = 36,
        inputType = RADIO,
        title = resources.getString(R.string.nbr_child_gest_age),
        arrayId = R.array.gest_age,
        entries = resources.getStringArray(R.array.gest_age),
        required = true,
        hasDependants = true
    )
    private val corticosteroidGivenAtLabor = FormElement(
        id = 37,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_corticosteroid),
        arrayId = R.array.yes_no_donno,
        entries = resources.getStringArray(R.array.yes_no_donno),
        required = true
    )
    private val babyCriedImmediatelyAfterBirth = FormElement(
        id = 38,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_cry_imm_birth),
        arrayId = R.array.yes_no_donno,
        entries = resources.getStringArray(R.array.yes_no_donno),
        required = true
    )
    private val anyDefectAtBirth = FormElement(
        id = 39,
        inputType = DROPDOWN,
        title = resources.getString(R.string.nbr_child_defect_at_birth),
        arrayId = R.array.defects,
        entries = resources.getStringArray(R.array.defects),
        required = true
    )
    private val motherUnselected = FormElement(
        id = 40,
        inputType = CHECKBOXES,
        title = resources.getString(R.string.mother_unselected),
        arrayId = R.array.yes,
        entries = resources.getStringArray(R.array.yes),
        required = false,
        hasDependants = true,
        orientation = LinearLayout.HORIZONTAL
    )
    private val motherOfChild = FormElement(
        id = 41, inputType = DROPDOWN, title = resources.getString(R.string.mother_of_the_child),
//        entries = pncMotherList?.toTypedArray(),
        arrayId = -1,
        required = true
    )


    private val babyHeight = FormElement(
        id = 42,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.height_at_birth_cm),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 3
    )
    private val babyWeight = FormElement(
        id = 43,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.weight_at_birth_kg),
        arrayId = -1,
        required = false,
        minDecimal = 0.0,
        maxDecimal = 10.0,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
        etMaxLength = 4
    )

    private val deathRemoveList by lazy {
        listOf(
            breastFeedWithin1Hr,
            birthDose,
            term,
            babyCriedImmediatelyAfterBirth,
            anyDefectAtBirth,
            babyHeight,
            babyWeight
        )
    }

    private val secondPage by lazy {
        listOf(
            placeOfBirth,
            whoConductedDelivery,
            typeOfDelivery,
            motherUnselected,
            complicationsDuringDelivery,
            breastFeedWithin1Hr,
            birthDose,
            term,
            babyCriedImmediatelyAfterBirth,
            anyDefectAtBirth,
            babyHeight,
            babyWeight,
        )
    }

    suspend fun setSecondPage(ben: BenRegCache?) {
        val list = secondPage.toMutableList()
        ben?.takeIf { !it.isDraft }?.let { saved ->
            placeOfBirth.value =
                placeOfBirth.getStringFromPosition(saved.kidDetails?.birthPlaceId ?: 0)
            facility.value = facility.getStringFromPosition(saved.kidDetails?.facilityId ?: 0)
            otherFacility.value = saved.kidDetails?.facilityOther
            otherPlaceOfBirth.value = saved.kidDetails?.placeName
            whoConductedDelivery.value = whoConductedDelivery.getStringFromPosition(
                saved.kidDetails?.conductedDeliveryId ?: 0
            )
            otherWhoConductedDelivery.value = saved.kidDetails?.conductedDeliveryOther
            typeOfDelivery.value =
                typeOfDelivery.getStringFromPosition(saved.kidDetails?.deliveryTypeId ?: 0)
            complicationsDuringDelivery.value = complicationsDuringDelivery.getStringFromPosition(
                saved.kidDetails?.complicationsId ?: 0
            )
            breastFeedWithin1Hr.value =
                breastFeedWithin1Hr.getStringFromPosition(saved.kidDetails?.feedingStartedId ?: 0)
            birthDose.value = birthDose.getStringFromPosition(saved.kidDetails?.birthDosageId ?: 0)
            birthDoseGiven.value =
                "${if (saved.kidDetails?.birthBCG == true) birthDoseGiven.entries?.get(0) else ""}${if (saved.kidDetails?.birthHepB == true) birthDoseGiven.entries?.get(1) else ""}${if (saved.kidDetails?.birthOPV == true) birthDoseGiven.entries?.get(2) else ""}"
            term.value = term.getStringFromPosition(saved.kidDetails?.termId ?: 0)
            termGestationalAge.value =
                termGestationalAge.getStringFromPosition(saved.kidDetails?.gestationalAgeId ?: 0)
            corticosteroidGivenAtLabor.value = corticosteroidGivenAtLabor.getStringFromPosition(
                saved.kidDetails?.corticosteroidGivenMotherId ?: 0
            )
            babyCriedImmediatelyAfterBirth.value =
                babyCriedImmediatelyAfterBirth.getStringFromPosition(
                    saved.kidDetails?.criedImmediatelyId ?: 0
                )
            anyDefectAtBirth.value =
                anyDefectAtBirth.getStringFromPosition(saved.kidDetails?.birthDefectsId ?: 0)
            motherOfChild.value = saved.kidDetails?.childMotherName
            babyHeight.value = saved.kidDetails?.heightAtBirth?.toString()
            babyWeight.value = saved.kidDetails?.weightAtBirth?.toString()
        }
        if (placeOfBirth.value == placeOfBirth.entries!![1]) list.add(
            list.indexOf(placeOfBirth) + 1, facility
        )
        if (placeOfBirth.value == placeOfBirth.entries!!.last()) list.add(
            list.indexOf(placeOfBirth) + 1, otherPlaceOfBirth
        )
        if (facility.value == facility.entries!!.last()) list.add(
            list.indexOf(facility) + 1, otherFacility
        )
        if (birthDose.value == birthDose.entries!!.first())
            list.add(list.indexOf(birthDose) + 1, birthDoseGiven)
        if (term.value == term.entries!![1])
            list.add(list.indexOf(term) + 1, termGestationalAge)
        if (termGestationalAge.value == termGestationalAge.entries!!.first())
            list.add(list.indexOf(termGestationalAge) + 1, corticosteroidGivenAtLabor)
        if (whoConductedDelivery.value == whoConductedDelivery.entries!!.last()) list.add(
            list.indexOf(whoConductedDelivery) + 1, otherWhoConductedDelivery
        )
        if (complicationsDuringDelivery.value == complicationsDuringDelivery.entries!![4]) list.removeAll(
            deathRemoveList
        )

        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            firstName.id -> {
                validateEmptyOnEditText(firstName)
                validateAllCapsOrSpaceOnEditText(firstName)
            }

            lastName.id -> {
                validateAllCapsOrSpaceOnEditText(lastName)
            }

            dob.id -> {
                assignValuesToAgeAndAgeUnitFromDob(getLongFromDate(dob.value), age, ageUnit)
//                val case1 = triggerDependants(
//                    age = age.value!!.toInt(),
//                    ageUnit = ageUnit,
//                    ageTriggerRange = Range(3, 5),
//                    ageUnitTriggerIndex = 2,
//                    target = childRegisteredAtAwc ,
//                    placeAfter = religion,
//                )
                /*val case2 = */triggerDependants(
                    age = age.value!!.toInt(),
                    ageUnit = ageUnit,
                    ageTriggerRange = Range(4, 14),
                    ageUnitTriggerIndex = 2,
                    target = childRegisteredAtSchool,
                    placeAfter = religion,
                    targetSideEffect = listOf(typeOfSchool)

                )

            }

            ageUnit.id, age.id -> {
                if (age.value.isNullOrEmpty() || ageUnit.value == null) {
                    validateEmptyOnEditText(age)
                    validateEmptyOnEditText(ageUnit)
                    return -1
                }
                when (ageUnit.value) {
                    ageUnit.entries?.get(0) -> {
                        age.min = 0
                        age.max = 31
                    }

                    ageUnit.entries?.get(1) -> {
                        age.min = 1
                        age.max = 11
                    }

                    ageUnit.entries?.get(2) -> {
                        age.min = 1
                        age.max = 14
                    }

                    else -> return -1
                }
                validateIntMinMax(age)
                if (age.errorText == null) {
                    val cal = Calendar.getInstance()
                    when (ageUnit.value) {
                        ageUnit.entries?.get(2) -> {
                            cal.add(
                                Calendar.YEAR, -1 * age.value!!.toInt()
                            )
                        }

                        ageUnit.entries?.get(1) -> {
                            cal.add(
                                Calendar.MONTH, -1 * age.value!!.toInt()
                            )
                        }

                        ageUnit.entries?.get(0) -> {
                            cal.add(
                                Calendar.DAY_OF_YEAR, -1 * age.value!!.toInt()
                            )
                        }
                    }
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH) + 1
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    val newDob =
                        "${if (day > 9) day else "0$day"}-${if (month > 9) month else "0$month"}-$year"
                    if (dob.value != newDob) {
                        dob.value = newDob
                        dob.errorText = null
                    }
                }
                triggerDependants(
                    age = age.value!!.toInt(),
                    ageUnit = ageUnit,
                    ageTriggerRange = Range(4, 14),
                    ageUnitTriggerIndex = 2,
                    target = childRegisteredAtSchool,
                    placeAfter = religion,
                    targetSideEffect = listOf(typeOfSchool)
                )
            }

            childRegisteredAtSchool.id -> {
                triggerDependants(
                    source = childRegisteredAtSchool,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = typeOfSchool
                )
            }

            gender.id -> {
                relationToHead.value = null
                relationToHead.entries = when (index) {
                    0 -> relationToHeadListMale
                    1 -> relationToHeadListFemale
                    else -> relationToHeadListDefault
                }
                triggerDependants(
                    source = gender,
                    removeItems = listOf(otherRelationToHead),
                    addItems = emptyList()
                )
            }

            otherRelationToHead.id -> {
                validateEmptyOnEditText(otherRelationToHead)
            }

            otherMobileNoOfRelation.id -> {
                validateEmptyOnEditText(otherMobileNoOfRelation)
            }

            fatherName.id -> {
                validateEmptyOnEditText(fatherName)
                validateAllCapsOrSpaceOnEditText(fatherName)
            }

            motherName.id -> {
                validateEmptyOnEditText(motherName)
                validateAllCapsOrSpaceOnEditText(motherName)
            }

            contactNumber.id -> {
                validateEmptyOnEditText(contactNumber)
                validateMobileNumberOnEditText(contactNumber)
            }

            mobileNoOfRelation.id -> {
                when (index) {
                    0, 1, 2, 3 -> triggerDependants(
                        source = mobileNoOfRelation,
                        removeItems = listOf(otherMobileNoOfRelation, contactNumberFamilyHead),
                        addItems = listOf(contactNumber)
                    )

                    4 -> {
                        contactNumberFamilyHead.value = familyHeadPhoneNo
                        triggerDependants(
                            source = mobileNoOfRelation,
                            addItems = listOf(contactNumberFamilyHead),
                            removeItems = listOf(otherMobileNoOfRelation, contactNumber)
                        )
                    }

                    else -> triggerDependants(
                        source = mobileNoOfRelation,
                        removeItems = listOf(contactNumberFamilyHead),
                        addItems = listOf(otherMobileNoOfRelation, contactNumber)
                    )
                }
            }

            relationToHead.id -> {
                triggerDependants(
                    source = relationToHead,
                    passedIndex = index,
                    triggerIndex = relationToHead.entries!!.lastIndex,
                    target = otherRelationToHead
                )
            }

            religion.id -> {
                triggerDependants(
                    source = religion, passedIndex = index, triggerIndex = 7, target = otherReligion
                )
            }

            otherReligion.id -> validateEmptyOnEditText(otherReligion)
            rchId.id -> validateRchIdOnEditText(rchId)
            ///Page 2///
            placeOfBirth.id -> {
                when (index) {
                    0 -> triggerDependants(
                        source = placeOfBirth,
                        removeItems = listOf(otherPlaceOfBirth, facility, otherFacility),
                        addItems = emptyList()
                    )

                    1 -> triggerDependants(
                        source = placeOfBirth,
                        removeItems = listOf(otherPlaceOfBirth, otherFacility),
                        addItems = listOf(facility)
                    )

                    2 -> triggerDependants(
                        source = placeOfBirth,
                        removeItems = listOf(facility, otherFacility),
                        addItems = listOf(otherPlaceOfBirth)
                    )

                    else -> -1
                }

            }

            facility.id -> {
                triggerDependants(
                    source = facility,
                    passedIndex = index,
                    triggerIndex = facility.entries!!.lastIndex,
                    target = otherFacility,
                )
            }

            otherPlaceOfBirth.id -> validateEmptyOnEditText(otherPlaceOfBirth)
            otherFacility.id -> validateEmptyOnEditText(otherFacility)

            whoConductedDelivery.id -> {
                triggerDependants(
                    source = whoConductedDelivery,
                    passedIndex = index,
                    triggerIndex = whoConductedDelivery.entries!!.lastIndex,
                    target = otherWhoConductedDelivery,
                )
            }

            otherWhoConductedDelivery.id -> validateEmptyOnEditText(otherWhoConductedDelivery)
            complicationsDuringDelivery.id -> {
                triggerDependantsReverse(
                    source = complicationsDuringDelivery,
                    passedIndex = index,
                    triggerIndex = 4,
                    target = deathRemoveList,
                    targetSideEffect = listOf(
                        birthDoseGiven,
                        termGestationalAge,
                        corticosteroidGivenAtLabor,
                        motherOfChild
                    )
                )
            }

            motherUnselected.id -> {
                triggerDependants(
                    source = motherUnselected,
                    passedIndex = index,
                    triggerIndex = 1,
                    target = motherOfChild,
                )
            }

            birthDose.id -> {
                triggerDependants(
                    source = birthDose,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = birthDoseGiven,

                    )
            }

            term.id -> {
                triggerDependants(
                    source = term,
                    passedIndex = index,
                    triggerIndex = 1,
                    target = termGestationalAge,
                    targetSideEffect = listOf(corticosteroidGivenAtLabor)
                )
            }

            termGestationalAge.id -> {
                triggerDependants(
                    source = termGestationalAge,
                    passedIndex = index,
                    triggerIndex = 0,
                    target = corticosteroidGivenAtLabor,
                )
            }

            babyWeight.id -> {
                validateDoubleMinMax(babyWeight)
            }


            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as BenRegCache).let { ben ->
            // Page 001
            ben.userImage = pic.value
            ben.regDate = getLongFromDate(dateOfReg.value!!)
            ben.firstName = firstName.value
            ben.lastName = lastName.value
            ben.dob = getLongFromDate(dob.value!!)
            ben.age = age.value?.toInt() ?: 0
            ben.ageUnitId = when (ageUnit.value) {
                ageUnit.entries!![2] -> 3
                ageUnit.entries!![1] -> 2
                ageUnit.entries!![0] -> 1
                else -> 0
            }
            ben.ageUnit = when (ben.ageUnitId) {
                3 -> AgeUnit.YEARS
                2 -> AgeUnit.MONTHS
                1 -> AgeUnit.DAYS
                else -> null
            }
//            ben.registrationType = getTypeFromAge(ben.age, ben.ageUnit)
            ben.genderId = when (gender.value) {
                gender.entries!![0] -> 1
                gender.entries!![1] -> 2
                gender.entries!![2] -> 3
                else -> 0
            }
            ben.gender = when (ben.genderId) {
                1 -> MALE
                2 -> FEMALE
                3 -> TRANSGENDER
                else -> null
            }
            ben.fatherName = fatherName.value
            ben.motherName = motherName.value
            ben.familyHeadRelationPosition =
                relationToHeadListDefault.indexOf(relationToHead.value) + 1
            ben.familyHeadRelation = relationToHead.value
            ben.familyHeadRelationOther = otherRelationToHead.value
            ben.mobileNoOfRelationId = mobileNoOfRelation.getPosition()
            ben.mobileNoOfRelation =
                mobileNoOfRelation.getEnglishStringFromPosition(ben.mobileNoOfRelationId)
            ben.mobileOthers = otherMobileNoOfRelation.value
            ben.contactNumber =
                if (ben.mobileNoOfRelationId == 5) familyHeadPhoneNo!!.toLong() else contactNumber.value!!.toLong()
            ben.communityId = community.getPosition()
            ben.community = community.getEnglishStringFromPosition(ben.communityId)
            ben.religionId = religion.getPosition()
            ben.religion = religion.getEnglishStringFromPosition(ben.religionId)
            ben.religionOthers = otherReligion.value
//            ben.kidDetails?.childRegisteredAWC = childRegisteredAtAwc.value
//            ben.kidDetails?.childRegisteredAWCId =
            ben.kidDetails?.childRegisteredSchoolId = childRegisteredAtSchool.getPosition()
            ben.kidDetails?.childRegisteredSchool =
                childRegisteredAtSchool.getEnglishStringFromPosition(childRegisteredAtSchool.getPosition())

            ben.kidDetails?.typeOfSchoolId = typeOfSchool.getPosition()
            ben.kidDetails?.typeOfSchool = typeOfSchool.getEnglishStringFromPosition(typeOfSchool.getPosition())
            ben.rchId = rchId.value

            // Page 002

            ben.kidDetails?.birthPlaceId =
                placeOfBirth.getPosition()
            ben.kidDetails?.birthPlace = placeOfBirth.getEnglishStringFromPosition(placeOfBirth.getPosition())
            ben.kidDetails?.placeName = otherPlaceOfBirth.value
            ben.kidDetails?.facilityId = facility.getPosition()
            ben.kidDetails?.facilityOther = otherFacility.value

            ben.kidDetails?.conductedDeliveryId =
                whoConductedDelivery.getPosition()
            ben.kidDetails?.conductedDelivery = whoConductedDelivery.getEnglishStringFromPosition(whoConductedDelivery.getPosition())

            ben.kidDetails?.conductedDeliveryOther =
                otherWhoConductedDelivery.value

            ben.kidDetails?.deliveryTypeId =
                typeOfDelivery.getPosition()
            ben.kidDetails?.deliveryType =
                typeOfDelivery.getEnglishStringFromPosition(typeOfDelivery.getPosition())

            ben.kidDetails?.complicationsId =
                complicationsDuringDelivery.getPosition()
            ben.kidDetails?.complications =
                complicationsDuringDelivery.getEnglishStringFromPosition(complicationsDuringDelivery.getPosition())

            ben.kidDetails?.feedingStartedId =
                breastFeedWithin1Hr.getPosition()
            ben.kidDetails?.feedingStarted =
                breastFeedWithin1Hr.getEnglishStringFromPosition(breastFeedWithin1Hr.getPosition())

            ben.kidDetails?.birthDosageId =
                birthDose.getPosition()
            ben.kidDetails?.birthDosage =
                birthDose.getEnglishStringFromPosition(ben.kidDetails!!.birthDosageId)

            ben.kidDetails?.birthBCG =
                birthDoseGiven.value?.contains(birthDoseGiven.entries!![0]) ?: false
            ben.kidDetails?.birthHepB =
                birthDoseGiven.value?.contains(birthDoseGiven.entries!![1]) ?: false
            ben.kidDetails?.birthOPV =
                birthDoseGiven.value?.contains(birthDoseGiven.entries!![2]) ?: false
            ben.kidDetails?.termId =
                term.getPosition()
            ben.kidDetails?.term = term.getEnglishStringFromPosition(term.getPosition())

            ben.kidDetails?.gestationalAgeId =
                termGestationalAge.getPosition()
            ben.kidDetails?.gestationalAge =
                termGestationalAge.getEnglishStringFromPosition(termGestationalAge.getPosition())

            ben.kidDetails?.corticosteroidGivenMotherId =
                corticosteroidGivenAtLabor.getPosition()
            ben.kidDetails?.corticosteroidGivenMother =
                corticosteroidGivenAtLabor.getEnglishStringFromPosition(corticosteroidGivenAtLabor.getPosition())

            ben.kidDetails?.criedImmediatelyId =
                babyCriedImmediatelyAfterBirth.getPosition()
            ben.kidDetails?.criedImmediately =
                babyCriedImmediatelyAfterBirth.getEnglishStringFromPosition(babyCriedImmediatelyAfterBirth.getPosition())
            ben.kidDetails?.birthDefectsId =
                anyDefectAtBirth.getPosition()
            ben.kidDetails?.birthDefects = anyDefectAtBirth.getEnglishStringFromPosition(anyDefectAtBirth.getPosition())

            ben.kidDetails?.heightAtBirth =
                babyHeight.value?.takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0
            ben.kidDetails?.weightAtBirth =
                babyWeight.value?.takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0

        }
    }

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            pic.id -> {
                pic.value = dpUri.toString()
                pic.errorText = null
            }
        }

    }
}
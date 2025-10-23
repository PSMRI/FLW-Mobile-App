package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.text.InputType
import android.util.Range
import android.widget.LinearLayout
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenBasicCache.Companion.getAgeFromDob
import org.piramalswasthya.sakhi.model.BenBasicCache.Companion.getYearsFromDate
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.Gender.FEMALE
import org.piramalswasthya.sakhi.model.Gender.MALE
import org.piramalswasthya.sakhi.model.Gender.TRANSGENDER
import org.piramalswasthya.sakhi.model.HouseholdCache
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
import java.util.concurrent.TimeUnit

class BenRegFormDataset(context: Context, language: Languages) : Dataset(context, language) {


    companion object {
        fun calculateMaxSonAge(
            parentYears: Int,
            parentMonths: Int,
            marriageYears: Int,
            marriageMonths: Int
        ): Pair<Int, Int> {
            val parentTotalMonths = parentYears * 12 + parentMonths
            val bufferTotalMonths = marriageYears * 12 + marriageMonths

            val diffMonths = (parentTotalMonths - bufferTotalMonths).coerceAtLeast(0)

            val years = diffMonths / 12
            val months = diffMonths % 12

            return Pair(years, months)
        }
        private var keepReproOnEditSticky: Boolean = false


        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

        private fun getMinDobMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1 * Konstants.maxAgeForGenBen)
            return cal.timeInMillis
        }

        private fun getHoFMinDobMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1 * Konstants.maxAgeForGenBen)
            return cal.timeInMillis
        }

        private fun getHofMaxDobMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1 * Konstants.minAgeForGenBen)
            return cal.timeInMillis
        }

    }

    private var familyHeadPhoneNo: String? = null
    private var timeStampDateOfMarriageFromSpouse: Long? = null
    private var isHoF: Boolean = false

    private var hof: BenRegCache? = null
    private var benIfDataExist: BenRegCache? = null

    private var minAgeYear: Int = 0
    private var maxAgeYear: Int = Konstants.maxAgeForGenBen

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
        required = false,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
    )
    private val agePopup = FormElement(
        id = 115,
        inputType = org.piramalswasthya.sakhi.model.InputType.AGE_PICKER,
        title = resources.getString(R.string.nbr_age),
        subtitle = resources.getString(R.string.nbr_dob),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
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

    private val maritalStatusMale = resources.getStringArray(R.array.nbr_marital_status_male_array)

    private val maritalStatusFemale =
        resources.getStringArray(R.array.nbr_marital_status_female_array)
    private val maritalStatus = FormElement(
        id = 1008,
        inputType = DROPDOWN,
        title = resources.getString(R.string.marital_status),
        arrayId = R.array.nbr_marital_status_male_array,
        entries = maritalStatusMale,
        required = true,
        hasDependants = true,
    )
    private val husbandName = FormElement(
        id = 1009,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.husband_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,

        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val wifeName = FormElement(
        id = 1010,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.wife_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,

        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val spouseName = FormElement(
        id = 1011,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.spouse_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val ageAtMarriage = FormElement(
        id = 1012,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.age_at_marriage),
        etMaxLength = 2,
        arrayId = -1,
        required = true,
        hasDependants = true,

        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        min = Konstants.minAgeForMarriage.toLong(),
        max = Konstants.maxAgeForGenBen.toLong()
    )
    private val dateOfMarriage = FormElement(
        id = 1013,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.date_of_marriage),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = getMinDobMillis(),
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


    private val relationToHeadListDefault =
        resources.getStringArray(R.array.nbr_relationship_to_head_src)

    private val relationToHeadListMale =
        resources.getStringArray(R.array.nbr_relationship_to_head_male)

    private val relationToHeadListFemale =
        resources.getStringArray(R.array.nbr_relationship_to_head_female)

    private val relationToHead = FormElement(
        id = 15,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.nbr_rel_to_head),
        arrayId = R.array.nbr_relationship_to_head_src,
        entries = resources.getStringArray(R.array.nbr_relationship_to_head_src),
        required = true,
//        hasDependants = true,
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


    private val hasAadharNo = FormElement(
        id = 1024,
        inputType = RADIO,
        title = resources.getString(R.string.has_aadhaar_number),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val aadharNo = FormElement(
        id = 1025,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.enter_aadhaar_number_ben),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12,
        max = 999999999999L,
        min = 100000000000L
    )

    private val reproductiveStatus = FormElement(
        id = 1028,
        inputType = DROPDOWN,
        title = resources.getString(R.string.reproductive_status),
        arrayId = R.array.nbr_reproductive_status_array,
        entries = resources.getStringArray(R.array.nbr_reproductive_status_array),
        required = true,
        hasDependants = false
    )
    private val birthCertificateNumber = FormElement(
        id = 1029,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.birth_certificate_number),
        arrayId = -1,
        required = false,
    )
    val firstPage by lazy {
        listOf(
            pic,
            dateOfReg,
            firstName,
            lastName,
            agePopup,
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

    suspend fun setFirstPageToRead(ben: BenRegCache?, familyHeadPhoneNo: Long?) {
        val list = mutableListOf(
            pic,
            dateOfReg,
            firstName,
            lastName,
            agePopup,
            gender,
            maritalStatus,
            fatherName,
            motherName,
            relationToHead,
            mobileNoOfRelation,
            contactNumber,
            community,
            religion,
            rchId,
        )

        this.familyHeadPhoneNo = familyHeadPhoneNo?.toString()
        keepReproOnEditSticky = false

        ben?.takeIf { !it.isDraft }?.let { saved ->
            pic.value = saved.userImage
            dateOfReg.value = getDateFromLong(saved.regDate)
            firstName.value = saved.firstName
            lastName.value = saved.lastName
            agePopup.value = getDateFromLong(saved.dob)
            gender.value = gender.getStringFromPosition(saved.genderId)
            gender.inputType = TEXT_VIEW

            fatherName.value = saved.fatherName
            saved.fatherName?.takeIf { it.isNotEmpty() }?.let { fatherName.inputType = TEXT_VIEW }

            motherName.value = saved.motherName
            saved.motherName?.takeIf { it.isNotEmpty() }?.let { motherName.inputType = TEXT_VIEW }

            saved.genDetails?.spouseName?.let {
                maritalStatus.inputType = TEXT_VIEW
                when (saved.genderId) {
                    1 -> {
                        wifeName.value = it
                        if (it.isNotEmpty()) wifeName.inputType = TEXT_VIEW
                    }
                    2 -> {
                        husbandName.value = it
                        if (it.isNotEmpty()) husbandName.inputType = TEXT_VIEW
                    }
                    3 -> {
                        spouseName.value = it
                        if (it.isNotEmpty()) spouseName.inputType = TEXT_VIEW
                    }
                }
            }

            maritalStatus.entries = when (saved.gender) {
                MALE -> maritalStatusMale
                FEMALE -> maritalStatusFemale
                else -> maritalStatusMale
            }
            maritalStatus.arrayId = when (saved.gender) {
                MALE -> R.array.nbr_marital_status_male_array
                FEMALE -> R.array.nbr_marital_status_female_array
                else -> R.array.nbr_marital_status_male_array
            }
            maritalStatus.value = saved.genDetails?.maritalStatusId?.let { maritalStatus.getStringFromPosition(it) }

            ageAtMarriage.value = saved.genDetails?.ageAtMarriage?.toString() ?: ""
            dateOfMarriage.value = getDateFromLong(saved.genDetails?.marriageDate ?: 0)

            mobileNoOfRelation.value = mobileNoOfRelation.getStringFromPosition(saved.mobileNoOfRelationId)
            otherMobileNoOfRelation.value = saved.mobileOthers
            contactNumber.value = saved.contactNumber.toString()

            val relationIndex = (saved.familyHeadRelationPosition - 1).coerceIn(0, relationToHeadListDefault.lastIndex)
            relationToHead.value = relationToHeadListDefault.getOrNull(relationIndex)

            otherRelationToHead.value = saved.familyHeadRelationOther
            community.value = community.getStringFromPosition(saved.communityId)
            religion.value = religion.getStringFromPosition(saved.religionId)
            otherReligion.value = saved.religionOthers

            childRegisteredAtAwc.value = childRegisteredAtAwc.getStringFromPosition(saved.kidDetails?.childRegisteredSchoolId ?: 0)
            childRegisteredAtSchool.value = childRegisteredAtSchool.getStringFromPosition(saved.kidDetails?.childRegisteredSchoolId ?: 0)
            typeOfSchool.value = typeOfSchool.getStringFromPosition(saved.kidDetails?.typeOfSchoolId ?: 0)
            rchId.value = saved.rchId

            reproductiveStatus.value = saved.genDetails?.reproductiveStatusId?.let {
                reproductiveStatus.getStringFromPosition(it)
            }
        }

        val savedIsFemale = gender.entries != null && gender.value == gender.entries!![1]
        val savedIsMarried = maritalStatus.entries != null && maritalStatus.value == maritalStatus.entries!![1]
        keepReproOnEditSticky = (ben?.isDraft == false) && savedIsFemale && savedIsMarried

        val maritalIndex = list.indexOf(maritalStatus)
        if (!maritalStatus.value.isNullOrEmpty() && maritalStatus.entries != null && gender.value != null && maritalIndex >= 0) {
            val genderField = when (gender.value) {
                gender.entries!![0] -> wifeName
                gender.entries!![1] -> husbandName
                gender.entries!![2] -> spouseName
                else -> null
            }
            genderField?.let {
                list.add(maritalIndex + 3, it)
                list.add(maritalIndex + 4, ageAtMarriage)
            }
        }

        if (maritalStatus.entries != null && maritalStatus.value == maritalStatus.entries!![1] && gender.value == gender.entries!![1]) {
            fatherName.required = false
            motherName.required = false
        }

        if (maritalStatus.entries != null && maritalStatus.value == maritalStatus.entries!![2]) {
            husbandName.required = false
            wifeName.required = false
            spouseName.required = false
        }

        ageAtMarriage.value?.takeIf {
            it.isNotEmpty() && it == getAgeFromDob(getLongFromDate(agePopup.value)).toString()
        }?.let {
            if (!list.contains(dateOfMarriage)) {
                val ageIndex = list.indexOf(ageAtMarriage)
                if (ageIndex >= 0) list.add(ageIndex + 1, dateOfMarriage)
            }
        }

        val mobileIndex = list.indexOf(mobileNoOfRelation)
        if (mobileNoOfRelation.entries != null && mobileNoOfRelation.value == mobileNoOfRelation.entries!!.last() && mobileIndex >= 0) {
            list.add(mobileIndex + 1, otherMobileNoOfRelation)
        }

        val relationHeadIndex = list.indexOf(relationToHead)
        if (relationToHead.entries != null && relationToHead.value == relationToHead.entries!!.last() && relationHeadIndex >= 0) {
            list.add(relationHeadIndex + 1, otherRelationToHead)
        }

        val religionIndex = list.indexOf(religion)
        if (religion.entries != null && religion.value == religion.entries!![7] && religionIndex >= 0) {
            list.add(religionIndex + 1, otherReligion)
        }

        val dob = agePopup.value?.takeIf { it.isNotEmpty() }?.let { getLongFromDate(it) }
        val rchIndex = list.indexOf(rchId)
        if (dob != null && getAgeFromDob(dob) in 4..14 && rchIndex >= 0) {
            list.add(rchIndex, childRegisteredAtSchool)
        }

        val schoolIndex = list.indexOf(childRegisteredAtSchool)
        if (childRegisteredAtSchool.entries != null && childRegisteredAtSchool.value == childRegisteredAtSchool.entries?.first() && schoolIndex >= 0) {
            list.add(schoolIndex + 1, typeOfSchool)
        }

        birthCertificateNumber.value = ben?.kidDetails?.birthCertificateNumber
        placeOfBirth.value = ben?.kidDetails?.birthPlaceId?.let { placeOfBirth.getStringFromPosition(it) }

        if (hasThirdPage()
            && gender.entries != null
            && gender.value == gender.entries!![1] // Female
            && maritalStatus.entries != null
            && maritalStatus.value == maritalStatus.entries!![1]) {
            if (!list.contains(reproductiveStatus)) list.add(reproductiveStatus)
        } else if (keepReproOnEditSticky && gender.value == gender.entries!![1]) {
            if (!list.contains(reproductiveStatus)) {
                val rchIndex = list.indexOf(rchId)
                if (rchIndex >= 0) list.add(rchIndex, reproductiveStatus) else list.add(reproductiveStatus)
            }
        }
        if (isKid()) {
            list.removeAll(
                listOf(
                    maritalStatus,
                    husbandName,
                    wifeName,
                    spouseName,
                    ageAtMarriage,
                    dateOfMarriage,
                    reproductiveStatus
                )
            )
            list.addAll(listOf(birthCertificateNumber, placeOfBirth))
        }

        if (!isKid() && !hasThirdPage()) {
            list.remove(rchId)
        }

        setUpPage(list)
    }


    suspend fun setPageForHof(ben: BenRegCache?, household: HouseholdCache) {
        val list = mutableListOf(
            pic,
            dateOfReg,
            firstName,
            lastName,
            agePopup,
            gender,
            maritalStatus,
            fatherName,
            motherName,
            contactNumber,
            community,
            religion,
            rchId,
        )
        this.familyHeadPhoneNo = household.family?.familyHeadPhoneNo?.toString()
        this.isHoF = true
        if (dateOfReg.value == null)
            dateOfReg.value = getCurrentDateString()
        contactNumber.value = familyHeadPhoneNo
        household.family?.let {
            firstName.value = it.familyHeadName?.also {
                firstName.inputType = TEXT_VIEW
            }

            lastName.value = it.familyName?.also {
                lastName.inputType = TEXT_VIEW
            }

            contactNumber.value = it.familyHeadPhoneNo?.toString()?.also {
                contactNumber.inputType = TEXT_VIEW
            }
        }

        agePopup.min = getHoFMinDobMillis()
        agePopup.max = getHofMaxDobMillis()
        ben?.takeIf { !it.isDraft }?.let { saved ->
            pic.value = saved.userImage
            dateOfReg.value = getDateFromLong(saved.regDate)
            firstName.value = saved.firstName
            lastName.value = saved.lastName
            agePopup.value = getDateFromLong(saved.dob)
            gender.value = gender.getStringFromPosition(saved.genderId)
            gender.inputType = TEXT_VIEW
            fatherName.value = saved.fatherName
            saved.fatherName?.let {
                if (it.isNotEmpty()) fatherName.inputType = TEXT_VIEW
            }
            motherName.value = saved.motherName
            saved.motherName?.let {
                if (it.isNotEmpty()) motherName.inputType = TEXT_VIEW
            }
            saved.genDetails?.spouseName?.let {
                when (saved.genderId) {
                    1 -> wifeName.value = it
                    2 -> husbandName.value = it
                    3 -> spouseName.value = it
                }
            }
            maritalStatus.entries = when (saved.gender) {
                MALE -> maritalStatusMale
                FEMALE -> maritalStatusFemale
                else -> maritalStatusMale
            }
            maritalStatus.arrayId = when (saved.gender) {
                MALE -> R.array.nbr_marital_status_male_array
                FEMALE -> R.array.nbr_marital_status_female_array
                else -> R.array.nbr_marital_status_male_array
            }
            maritalStatus.value =
                maritalStatus.getStringFromPosition(saved.genDetails?.maritalStatusId ?: 0)
            ageAtMarriage.value = saved.genDetails?.ageAtMarriage?.toString() ?: ""
            dateOfMarriage.value = getDateFromLong(
                saved.genDetails?.marriageDate ?: 0
            )
            mobileNoOfRelation.value =
                mobileNoOfRelation.getStringFromPosition(saved.mobileNoOfRelationId)
            otherMobileNoOfRelation.value = saved.mobileOthers
            contactNumber.value = saved.contactNumber.toString()
            relationToHead.value = relationToHeadListDefault[saved.familyHeadRelationPosition - 1]
            otherRelationToHead.value = saved.familyHeadRelationOther
            community.value = community.getStringFromPosition(saved.communityId)
            religion.value = religion.getStringFromPosition(saved.religionId)
            otherReligion.value = saved.religionOthers
            childRegisteredAtAwc.value = childRegisteredAtAwc.getStringFromPosition(
                saved.kidDetails?.childRegisteredSchoolId ?: 0
            )
            childRegisteredAtSchool.value = childRegisteredAtSchool.getStringFromPosition(
                saved.kidDetails?.childRegisteredSchoolId ?: 0
            )
            typeOfSchool.value =
                typeOfSchool.getStringFromPosition(saved.kidDetails?.typeOfSchoolId ?: 0)
            rchId.value = saved.rchId

        }
        if (mobileNoOfRelation.value == mobileNoOfRelation.entries!!.last()) {
            list.add(list.indexOf(mobileNoOfRelation) + 1, otherMobileNoOfRelation)
        }
        if (religion.value == religion.entries!![7]) {
            list.add(list.indexOf(religion) + 1, otherReligion)
        }
        if ((getAgeFromDob(getLongFromDate(agePopup.value))) in 4..14) {
            list.add((list.indexOf(rchId)), childRegisteredAtSchool)
        }
        if (childRegisteredAtSchool.value == childRegisteredAtSchool.entries?.first()) list.add(
            list.indexOf(
                childRegisteredAtSchool
            ) + 1, typeOfSchool
        )

        if (!isKid() and !hasThirdPage()) {
            list.remove(rchId)
        }
        setUpPage(list)
    }

    suspend fun setPageForFamilyMember(
        ben: BenRegCache?,
        household: HouseholdCache,
        hoF: BenRegCache?,
        benGender: Gender,
        relationToHeadId: Int,
        hoFSpouse: List<BenRegCache> = emptyList()
    ) {
        val list = mutableListOf(
            pic,
            dateOfReg,
            firstName,
            lastName,
            agePopup,
            gender,
            maritalStatus,
            fatherName,
            motherName,
            relationToHead,
            mobileNoOfRelation,
            contactNumber,
            community,
            religion,
            rchId,
            reproductiveStatus,
//            ageAtMarriage
        )

        this.familyHeadPhoneNo = household.family?.familyHeadPhoneNo?.toString()
        this.hof = hoF
        this.benIfDataExist = ben

        if (ben == null) {
            dateOfReg.value = getCurrentDateString()
            mobileNoOfRelation.value = mobileNoOfRelation.entries!![4]
            contactNumber.value = familyHeadPhoneNo
            community.value = hoF?.communityId?.let { community.getStringFromPosition(it) }
            religion.value = hoF?.religionId?.let { religion.getStringFromPosition(it) }
            gender.value = when (benGender) {
                MALE -> gender.entries!!.first()
                FEMALE -> gender.entries!![1]
                TRANSGENDER -> gender.entries!!.last()
            }
            maritalStatus.entries = when (benGender) {
                MALE -> maritalStatusMale
                FEMALE -> maritalStatusFemale
                TRANSGENDER -> maritalStatusMale
            }
            maritalStatus.arrayId = when (benGender) {
                MALE -> R.array.nbr_marital_status_male_array
                FEMALE -> R.array.nbr_marital_status_female_array
                TRANSGENDER -> R.array.nbr_marital_status_male_array
            }
            gender.inputType = TEXT_VIEW
            relationToHead.value = relationToHead.getStringFromPosition(relationToHeadId + 1)
            if (relationToHeadId == relationToHead.entries!!.lastIndex) {
                list.add(list.indexOf(relationToHead) + 1, otherRelationToHead)
            }
        }

        agePopup.min = getMinDobMillis()
        agePopup.max = System.currentTimeMillis()

        if (relationToHeadId == 4 || relationToHeadId == 5) hoF?.let {
            setUpForSpouse(it, hoFSpouse)
        }
        if (relationToHeadId == 8 || relationToHeadId == 9) hoF?.let {
            setUpForChild(it, hoFSpouse.firstOrNull())
        }
        if (relationToHeadId == 0 || relationToHeadId == 1) hoF?.let {
            setUpForParents(it, benGender)
        }

        ben?.takeIf { !it.isDraft }?.let { saved ->
            pic.value = saved.userImage
            dateOfReg.value = getDateFromLong(saved.regDate)
            firstName.value = saved.firstName
            lastName.value = saved.lastName

            agePopup.value = getDateFromLong(saved.dob)
            ageAtMarriage.max = getAgeFromDob(saved.dob).toLong()

            gender.value = gender.getStringFromPosition(saved.genderId)
            gender.inputType = TEXT_VIEW

            fatherName.value = saved.fatherName
            saved.fatherName?.takeIf { it.isNotEmpty() }?.let { fatherName.inputType = TEXT_VIEW }

            motherName.value = saved.motherName
            saved.motherName?.takeIf { it.isNotEmpty() }?.let { motherName.inputType = TEXT_VIEW }

            saved.genDetails?.spouseName?.let {
                when (saved.genderId) {
                    1 -> {
                        wifeName.value = it
                        if (it.isNotEmpty()) wifeName.inputType = TEXT_VIEW
                    }
                    2 -> {
                        husbandName.value = it
                        if (it.isNotEmpty()) husbandName.inputType = TEXT_VIEW
                    }
                    3 -> {
                        spouseName.value = it
                        if (it.isNotEmpty()) spouseName.inputType = TEXT_VIEW
                    }
                }
            }

            maritalStatus.entries = when (saved.gender) {
                MALE -> maritalStatusMale
                FEMALE -> maritalStatusFemale
                else -> maritalStatusMale
            }
            maritalStatus.arrayId = when (saved.gender) {
                MALE -> R.array.nbr_marital_status_male_array
                FEMALE -> R.array.nbr_marital_status_female_array
                else -> R.array.nbr_marital_status_male_array
            }

            maritalStatus.value = maritalStatus.getStringFromPosition(saved.genDetails?.maritalStatusId ?: 0)
            ageAtMarriage.value = saved.genDetails?.ageAtMarriage?.toString() ?: ""
            dateOfMarriage.value = getDateFromLong(saved.genDetails?.marriageDate ?: 0)
            mobileNoOfRelation.value = mobileNoOfRelation.getStringFromPosition(saved.mobileNoOfRelationId)
            otherMobileNoOfRelation.value = saved.mobileOthers
            contactNumber.value = saved.contactNumber.toString()

            relationToHead.value = relationToHeadListDefault[(saved.familyHeadRelationPosition - 1).coerceIn(0, relationToHeadListDefault.lastIndex)]
            otherRelationToHead.value = saved.familyHeadRelationOther
            community.value = community.getStringFromPosition(saved.communityId)
            religion.value = religion.getStringFromPosition(saved.religionId)
            otherReligion.value = saved.religionOthers

            childRegisteredAtAwc.value = childRegisteredAtAwc.getStringFromPosition(saved.kidDetails?.childRegisteredSchoolId ?: 0)
            childRegisteredAtSchool.value = childRegisteredAtSchool.getStringFromPosition(saved.kidDetails?.childRegisteredSchoolId ?: 0)
            typeOfSchool.value = typeOfSchool.getStringFromPosition(saved.kidDetails?.typeOfSchoolId ?: 0)
            rchId.value = saved.rchId

            handleForAgeDob(formId = agePopup.id)
        }

        if (mobileNoOfRelation.value == mobileNoOfRelation.entries!!.last()) {
            list.add(list.indexOf(mobileNoOfRelation) + 1, otherMobileNoOfRelation)
        }
        if (religion.value == religion.entries!![7]) {
            list.add(list.indexOf(religion) + 1, otherReligion)
        }
        val dob = agePopup.value?.takeIf { it.isNotEmpty() }?.let { getLongFromDate(it) }
        if (dob != null && getAgeFromDob(dob) in 4..14) {
            val rchIndex = list.indexOf(rchId)
            if (rchIndex >= 0) list.add(rchIndex, childRegisteredAtSchool)
        }
        val schoolIndex = list.indexOf(childRegisteredAtSchool)
        if (childRegisteredAtSchool.entries != null && childRegisteredAtSchool.value == childRegisteredAtSchool.entries?.first() && schoolIndex >= 0) {
            list.add(schoolIndex + 1, typeOfSchool)
        }

        if (isKid()) {
            list.removeAll(
                listOf(
                    maritalStatus,
                    husbandName,
                    wifeName,
                    spouseName,
                    ageAtMarriage,
                    dateOfMarriage,
                    reproductiveStatus
                )
            )
            list.addAll(listOf(birthCertificateNumber, placeOfBirth))
        }

        val shouldShowRepro =
            gender.value == "Female" &&
                    maritalStatus.value == "Married"

        if (!shouldShowRepro) {
            list.remove(reproductiveStatus)
        }

     /*   val shouldAgeAtMarraige = maritalStatus.value == "Married"
        if (!shouldAgeAtMarraige) {
            list.remove(ageAtMarriage)
        }*/

        if (!isKid() && !hasThirdPage()) {
            list.remove(rchId)
        }

        setUpPage(list)
    }

    private fun setUpForChild(hoF: BenRegCache, hoFSpouse: BenRegCache?) {
        if (hoF.gender == MALE) {
            fatherName.value = "${hoF.firstName} ${hoF.lastName ?: ""}"
            motherName.value = hoFSpouse?.let {
                "${it.firstName} ${it.lastName ?: ""}"
            } ?: hoF.genDetails?.spouseName

            fatherName.value?.let {
                if (it.isNotEmpty()) fatherName.inputType = TEXT_VIEW
            }
            motherName.value?.let {
                if (it.isNotEmpty()) motherName.inputType = TEXT_VIEW
            }
        } else {
            motherName.value = "${hoF.firstName} ${hoF.lastName ?: ""}"
            fatherName.value = hoFSpouse?.let {
                "${it.firstName} ${it.lastName ?: ""}"
            } ?: hoF.genDetails?.spouseName
            fatherName.value?.let {
                if (it.isNotEmpty()) fatherName.inputType = TEXT_VIEW
            }
            motherName.value?.let {
                if (it.isNotEmpty()) motherName.inputType = TEXT_VIEW
            }
        }


        val hoFAge = getAgeFromDob(hoF.dob)
        val hoFSpouseAge = hoFSpouse?.dob?.let { h -> getAgeFromDob(h) }
        val minParentAge = hoFSpouseAge?.let { minOf(hoFAge, it) } ?: hoFAge

        val hofAgeAtMarriage = hoF.genDetails?.ageAtMarriage ?: 0
        var hoFSpouseAgeAtMarriage=  hoFSpouse?.genDetails?.ageAtMarriage
        val minAgeAtMarriage = hoFSpouseAgeAtMarriage?.let { minOf(hofAgeAtMarriage, it) } ?: hofAgeAtMarriage


        val (maxSonYears, maxSonMonths) = calculateMaxSonAge(
            parentYears = minParentAge,
            parentMonths = 0,
            marriageYears = minAgeAtMarriage,
            marriageMonths = 7
        )

        val totalMonthsToSubtract = maxSonYears * 12 + maxSonMonths
        val maxAllowedDobMillis = Calendar.getInstance().setToStartOfTheDay().apply {
            add(Calendar.MONTH, -totalMonthsToSubtract)
        }.timeInMillis
        agePopup.min = maxAllowedDobMillis

        maxAgeYear = maxSonYears

        lastName.value = hoF.lastName
    }

    private fun setUpForParents(hoF: BenRegCache, benGender: Gender) {
        val hoFAge = getAgeFromDob(hoF.dob)
        val minAge = hoFAge + Konstants.minAgeForGenBen
        agePopup.max = Calendar.getInstance().setToStartOfTheDay().let {
            it.add(Calendar.YEAR, -1 * minAge)
            it.timeInMillis
        }
        minAgeYear = minAge
        firstName.value =
            when (benGender) {
                MALE -> hoF.fatherName?.also { firstName.inputType = TEXT_VIEW }
                FEMALE -> hoF.motherName?.also { firstName.inputType = TEXT_VIEW }
                else -> null
            }
        if (benGender == MALE) wifeName.value = hof?.motherName
        if (benGender == FEMALE) husbandName.value = hof?.fatherName
        lastName.value = hoF.lastName?.also { firstName.inputType = TEXT_VIEW }
        ageAtMarriage.max = getAgeFromDob(hoF.dob).toLong()
    }

    private fun setUpForSpouse(hoFSpouse: BenRegCache, hoFSpouse1: List<BenRegCache>) {
        if (hoFSpouse.genDetails?.maritalStatusId == 2) {
            if (hoFSpouse1.isEmpty()) {
                firstName.value = hoFSpouse.genDetails?.spouseName
                firstName.inputType = TEXT_VIEW
                lastName.value = hoFSpouse.lastName
                lastName.inputType = EDIT_TEXT
            } else {
                lastName.value = hoFSpouse.lastName
                lastName.inputType = EDIT_TEXT
            }
            if (hoFSpouse.gender == FEMALE) {
                wifeName.value = "${hoFSpouse.firstName} ${hoFSpouse.lastName ?: ""}"
                wifeName.inputType = TEXT_VIEW
            } else {
                husbandName.value = "${hoFSpouse.firstName} ${hoFSpouse.lastName ?: ""}"
                husbandName.inputType = TEXT_VIEW
            }
            maritalStatus.value = maritalStatus.getStringFromPosition(2)
            maritalStatus.inputType = TEXT_VIEW
            timeStampDateOfMarriageFromSpouse = hoFSpouse.genDetails?.marriageDate
            agePopup.min = getHoFMinDobMillis()
            agePopup.max = getHofMaxDobMillis()
        }
    }

    private fun hasThirdPage(): Boolean {
        return ((getAgeFromDob(getLongFromDate(agePopup.value))) >= Konstants.minAgeForGenBen &&
                (gender.value == gender.entries!![1]))

    }

    fun isKid(): Boolean {
        return ((getAgeFromDob(getLongFromDate(agePopup.value))) < 15)
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
                "${if (saved.kidDetails?.birthBCG == true) birthDoseGiven.entries?.get(0) else ""}${
                    if (saved.kidDetails?.birthHepB == true) birthDoseGiven.entries?.get(
                        1
                    ) else ""
                }${if (saved.kidDetails?.birthOPV == true) birthDoseGiven.entries?.get(2) else ""}"
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
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(firstName)
            }

            lastName.id -> {
                // validateAllCapsOrSpaceOnEditText(lastName)
                validateEmptyOnEditText(lastName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(lastName)
            }

            agePopup.id -> {
                assignValuesToAgeAndAgeUnitFromDob(
                    getLongFromDate(agePopup.value),
                    ageAtMarriage,
                    timeStampDateOfMarriageFromSpouse
                )

                handleForAgeDob(formId = agePopup.id)

            }

            ageAtMarriage.id -> {

                (getAgeFromDob(getLongFromDate(agePopup.value))).takeIf { it > 0 && !ageAtMarriage.value.isNullOrEmpty() }
                    ?.toInt()?.let {
                        validateEmptyOnEditText(ageAtMarriage)
                        validateIntMinMax(ageAtMarriage)
                        if (it == ageAtMarriage.value?.toInt()) {
                            val cal = Calendar.getInstance()
                            dateOfMarriage.max = cal.timeInMillis
                            cal.add(Calendar.YEAR, -1)
                            dateOfMarriage.min = cal.timeInMillis

                        }
                        triggerDependants(
                            source = ageAtMarriage,
                            passedIndex = ageAtMarriage.value!!.toInt(),
                            triggerIndex = it,
                            target = dateOfMarriage
                        )
                    } ?: -1
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
                maritalStatus.value = null
                maritalStatus.entries = when (index) {
                    1 -> maritalStatusFemale
                    else -> maritalStatusMale
                }

                maritalStatus.arrayId = when (index) {
                    1 -> R.array.nbr_marital_status_female_array
                    else -> R.array.nbr_marital_status_male_array
                }

                relationToHead.entries = when (index) {
                    0 -> relationToHeadListMale
                    1 -> relationToHeadListFemale
                    else -> relationToHeadListDefault
                }

                val isFemale = (gender.entries != null && gender.value == gender.entries!![1])
                val isMarried = (maritalStatus.entries != null && maritalStatus.value == maritalStatus.entries!![1])

                val listChanged = if (hasThirdPage() && isFemale && isMarried) {
                    updateReproductiveOptionsBasedOnAgeGender()
                    triggerDependants(
                        source = rchId,
                        addItems = listOf(reproductiveStatus),
                        removeItems = emptyList(),
                        position = -2
                    )
                } else {
                    fatherName.required = true
                    motherName.required = true
                    if (keepReproOnEditSticky && isFemale) {
                        updateReproductiveOptionsBasedOnAgeGender()
                        triggerDependants(
                            source = rchId,
                            addItems = emptyList(),
                            removeItems = emptyList()
                        )
                    } else {
                        triggerDependants(
                            source = rchId,
                            addItems = emptyList(),
                            removeItems = listOf(reproductiveStatus)
                        )
                    }
                }!= -1

                val listChanged2 = triggerDependants(
                    source = gender,
                    removeItems = listOf(otherRelationToHead),
                    addItems = emptyList()
                ) != -1

                if (listChanged || listChanged2) 1 else -1
            }

            maritalStatus.id -> {
                if (index == 0 && isBenParentOfHoF()) {
                    maritalStatus.errorText = "Parents cannot be unmarried!"
                }

                val isFemale = (gender.value == gender.entries!![1])
                val isUnmarried = (maritalStatus.value == maritalStatus.entries!![0])
                val isMarried = (maritalStatus.value == maritalStatus.entries!![1])

                when {
                    isUnmarried -> {
                        fatherName.required = true
                        motherName.required = true
                        return triggerDependants(
                            source = maritalStatus,
                            addItems = emptyList(),
                            removeItems = listOf(
                                spouseName,
                                husbandName,
                                wifeName,
                                ageAtMarriage,
                                reproductiveStatus
                            )
                        )
                    }

                    isMarried -> {
                        if (isFemale) {
                            fatherName.required = false
                            motherName.required = false
                        } else {
                            fatherName.required = true
                            motherName.required = true
                        }
                        husbandName.required = true
                        wifeName.required = true
                        wifeName.allCaps = true

                        val spouseResult = if (isFemale) {
                            updateReproductiveOptionsBasedOnAgeGender()
                            triggerDependants(
                                source = maritalStatus,
                                addItems = listOf(husbandName, ageAtMarriage, reproductiveStatus),
                                removeItems = listOf(wifeName, spouseName),
                                position = -2
                            )
                        } else {
                            triggerDependants(
                                source = maritalStatus,
                                addItems = listOf(wifeName, ageAtMarriage),
                                removeItems = listOf(husbandName, spouseName, reproductiveStatus)
                            )
                        }

                        if (relationToHead.value == relationToHead.entries!![0]) {
                            husbandName.value = hof?.fatherName
                        }
                        if (relationToHead.value == relationToHead.entries!![1]) {
                            wifeName.value = hof?.motherName
                        }

                        return spouseResult
                    }

                    else -> {
                        husbandName.required = maritalStatus.value != maritalStatus.entries!![2]
                        wifeName.required = maritalStatus.value != maritalStatus.entries!![2]
                        wifeName.allCaps = true
                        fatherName.required = true
                        motherName.required = true

                        return triggerDependants(
                            source = maritalStatus,
                            addItems = when (gender.value) {
                                gender.entries!![0] -> listOf(wifeName, ageAtMarriage)
                                gender.entries!![1] -> listOf(husbandName, ageAtMarriage)
                                else -> listOf(spouseName, ageAtMarriage)
                            },
                            removeItems = listOf(
                                wifeName,
                                husbandName,
                                spouseName,
                                ageAtMarriage,
                                reproductiveStatus
                            )
                        )
                    }
                }
            }


            otherRelationToHead.id -> {
                validateEmptyOnEditText(otherRelationToHead)
            }

            otherMobileNoOfRelation.id -> {
                validateEmptyOnEditText(otherMobileNoOfRelation)
            }

            fatherName.id -> {
                validateEmptyOnEditText(fatherName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(fatherName)
            }

            motherName.id -> {
                validateEmptyOnEditText(motherName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(motherName)
            }

            husbandName.id -> {
                validateEmptyOnEditText(husbandName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(husbandName)
            }

            wifeName.id -> {
                validateEmptyOnEditText(wifeName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(wifeName)
            }

            spouseName.id -> {
                validateEmptyOnEditText(spouseName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(spouseName)
            }

            contactNumber.id -> {
                validateEmptyOnEditText(contactNumber)
                validateMobileNumberOnEditText(contactNumber)
            }

            mobileNoOfRelation.id -> {
                contactNumber.value = null
                when (index) {
                    0, 1, 2, 3 -> {
                        val isHusbandNumberForWifeHoF =
                            index == 1 && relationToHead.value == relationToHead.getStringFromPosition(
                                5
                            )
                        val isSonOrDaughterOfHoF =
                            index == 3 && (relationToHead.value == relationToHead.getStringFromPosition(
                                9
                            ) || relationToHead.value == relationToHead.getStringFromPosition(
                                10
                            ))
                        if (isHusbandNumberForWifeHoF || isSonOrDaughterOfHoF) {
                            contactNumber.value = familyHeadPhoneNo
                        }
                        triggerDependants(
                            source = mobileNoOfRelation,
                            removeItems = listOf(otherMobileNoOfRelation),
                            addItems = emptyList(),
                        )
                    }

                    4 -> {

                        val ret = triggerDependants(
                            source = mobileNoOfRelation,
                            addItems = emptyList(),
                            removeItems = listOf(otherMobileNoOfRelation)
                        )
                        contactNumber.errorText = null
                        contactNumber.value = familyHeadPhoneNo
                        ret

                    }

                    else -> {
                        contactNumber.errorText = null
                        triggerDependants(
                            source = mobileNoOfRelation,
                            removeItems = emptyList(),
                            addItems = listOf(otherMobileNoOfRelation)
                        )
                    }
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
            birthCertificateNumber.id -> validateNoAlphabetSpaceOnEditText(birthCertificateNumber)

            else -> -1
        }
    }
    private fun handleForAgeDob(formId: Int): Int {
        if (formId == agePopup.id) {
            if (agePopup.errorText == null) {
                val ageAtMarriageMax = if (isBenParentOfHoF())
                    getAgeFromDob(getLongFromDate(agePopup.value)) - (hof?.dob?.let { getAgeFromDob(it) } ?: 0)
                else
                    getAgeFromDob(getLongFromDate(agePopup.value))
                ageAtMarriage.max = ageAtMarriageMax.toLong()

                val existingSavedAgeAtMarriage = benIfDataExist?.genDetails?.ageAtMarriage
                if (benIfDataExist == null) {
                    ageAtMarriage.value = null
                } else {
                    if (existingSavedAgeAtMarriage != null && existingSavedAgeAtMarriage > ageAtMarriageMax) {
                        ageAtMarriage.value = null
                    } else {
                        if (ageAtMarriage.value.isNullOrBlank()) {
                            ageAtMarriage.value = existingSavedAgeAtMarriage?.toString() ?: ""
                        }
                    }
                }

            }

            val age = getAgeFromDob(getLongFromDate(agePopup.value))
            val genderIsFemale = gender.value == gender.entries?.get(1)
            if (benIfDataExist == null) {
                reproductiveStatus.isEnabled = true
                val updatedReproductiveOptions = when {
                    !genderIsFemale -> emptyList()
                    age in 15..19 -> listOf(
                        "Adolescent Girl", "Eligible Couple", "Pregnant Woman",
                        "Postnatal Mother", "Permanently Sterilised"
                    )
                    age in 20..49 -> listOf(
                        "Eligible Couple", "Pregnant Woman",
                        "Postnatal Mother", "Permanently Sterilised"
                    )
                    age >= 50 -> listOf("Elderly Woman")
                    else -> emptyList()
                }

                reproductiveStatus.entries = updatedReproductiveOptions.toTypedArray()
                reproductiveStatus.value = null
            } else {
                val saved = benIfDataExist
                reproductiveStatus.isEnabled = true
                if (saved != null) {
                    reproductiveStatus.value = saved.genDetails?.reproductiveStatusId?.let {
                        reproductiveStatus.getStringFromPosition(it)
                    }
                }

                when (reproductiveStatus.value) {
                    "Adolescent Girl" -> {
                        agePopup.max = yearsAgo(15)
                        agePopup.min = yearsAgo(19)
                    }
                    "Eligible Couple", "Pregnant Woman", "Postnatal Mother", "Permanently Sterilised" -> {
                        agePopup.max = yearsAgo(20)
                        agePopup.min = yearsAgo(49)
                    }
                    "Elderly Woman" -> {
                        agePopup.max = yearsAgo(50)
                        agePopup.min = yearsAgo(100)
                    }
                }
            }
        }
        val listChanged: Int = if (hasThirdPage() && gender.value == gender.entries!![1]) {
            triggerDependants(
                source = rchId,
                addItems = listOf(reproductiveStatus),
                removeItems = emptyList(),
                position = -2
            )
        } else {
            fatherName.required = true
            motherName.required = true
            triggerDependants(
                source = rchId,
                removeItems = listOf(reproductiveStatus),
                addItems = emptyList()
            )
        }

        val listChanged2: Int = triggerDependants(
            age = getAgeFromDob(getLongFromDate(agePopup.value)),
            ageTriggerRange = Range(4, 14),
            target = childRegisteredAtSchool,
            placeAfter = religion,
            targetSideEffect = listOf(typeOfSchool)
        )

        val listChanged3: Int =
            if (maritalStatus.inputType == TEXT_VIEW) {
                -1
            } else {
                if (getYearsFromDate(agePopup.value.toString()) <= Konstants.maxAgeForAdolescent) {
                    fatherName.required = true
                    motherName.required = true

                    triggerDependants(
                        source = rchId,
                        addItems = listOf(birthCertificateNumber, placeOfBirth),
                        removeItems = listOf(
                            husbandName,
                            wifeName,
                            spouseName,
                            ageAtMarriage,
                            dateOfMarriage,
                            maritalStatus,
                            reproductiveStatus
                        ),
                        position = -2
                    )
                } else {
                    maritalStatus.value = null
                    triggerDependants(
                        source = gender,
                        removeItems = listOf(birthCertificateNumber, placeOfBirth),
                        addItems = listOf(maritalStatus)
                    )
                    if (gender.value == "Female") {
                        triggerDependants(
                            source = rchId,
                            removeItems = emptyList(),
                            addItems = listOf(reproductiveStatus),
                            position = -2
                        )
                    } else {
                        triggerDependants(
                            source = rchId,
                            removeItems = listOf(reproductiveStatus),
                            addItems = emptyList()
                        )
                    }
                }
            }

        val listChanged4: Int =
            if (maritalStatus.inputType == TEXT_VIEW) {
                -1
            } else {
                if (getYearsFromDate(agePopup.value.toString()) <= Konstants.maxAgeForAdolescent || gender.value == gender.entries!![1]) {
                    triggerDependants(
                        source = religion,
                        removeItems = emptyList(),
                        addItems = listOf(rchId)
                    )
                } else {
                    triggerDependants(
                        source = religion,
                        removeItems = listOf(rchId),
                        addItems = emptyList()
                    )
                }
            }

        return if (listChanged != -1 || listChanged2 != -1 || listChanged3 != -1 || listChanged4 != -1) 1 else -1
    }

    private fun isBenParentOfHoF() =
        relationToHead.value == relationToHead.entries!![0] || relationToHead.value == relationToHead.entries!![1]

    fun getIndexOfAgeAtMarriage() = getIndexOfElement(ageAtMarriage)
    fun getIndexOfContactNumber() = getIndexOfElement(contactNumber)
    fun getIndexOfMaritalStatus() = getIndexOfElement(maritalStatus)


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as BenRegCache).let { ben ->
            // Page 001
            ben.userImage = pic.value
            ben.regDate = getLongFromDate(dateOfReg.value!!)
            ben.firstName = firstName.value
            ben.lastName = lastName.value
            ben.dob = getLongFromDate(agePopup.value!!)
            ben.age = (getAgeFromDob(getLongFromDate(agePopup.value)))
            ben.ageUnitId = 3
            ben.ageUnit = AgeUnit.YEARS
            ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
            ben.isKid = !ben.isAdult
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
            ben.familyHeadRelationPosition = if (isHoF) 19 else {
                relationToHeadListDefault.indexOf(relationToHead.value) + 1
            }
            ben.familyHeadRelation =
                getEnglishValueInArray(R.array.nbr_relationship_to_head_src, relationToHead.value)
            ben.familyHeadRelationOther = otherRelationToHead.value
            ben.mobileNoOfRelationId = if (isHoF) 1 else mobileNoOfRelation.getPosition()
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
            ben.kidDetails?.childRegisteredSchoolId = childRegisteredAtSchool.getPosition()
            ben.kidDetails?.childRegisteredSchool =
                childRegisteredAtSchool.getEnglishStringFromPosition(childRegisteredAtSchool.getPosition())

            ben.kidDetails?.typeOfSchoolId = typeOfSchool.getPosition()
            ben.kidDetails?.typeOfSchool =
                typeOfSchool.getEnglishStringFromPosition(typeOfSchool.getPosition())
            ben.rchId = rchId.value

            ben.genDetails?.maritalStatusId = maritalStatus.getPosition()
            ben.genDetails?.maritalStatus =
                maritalStatus.getEnglishStringFromPosition(ben.genDetails?.maritalStatusId ?: 0)
            ben.genDetails?.spouseName = husbandName.value.takeIf { !it.isNullOrEmpty() }
                ?: wifeName.value.takeIf { !it.isNullOrEmpty() }
                        ?: spouseName.value.takeIf { !it.isNullOrEmpty() }
            ben.genDetails?.ageAtMarriage =
                ageAtMarriage.value?.toInt() ?: 0
            ben.genDetails?.marriageDate =
                timeStampDateOfMarriageFromSpouse.takeIf { it != null }?.also {
                    ben.genDetails?.ageAtMarriage =
                        (TimeUnit.MILLISECONDS.toDays(it - ben.dob) / 365).toInt()
                } ?: run {
                    dateOfMarriage.value?.let { getLongFromDate(it) }
                        ?: run {
                            ben.genDetails?.ageAtMarriage?.takeIf { it > 0 }?.let {
                                getDoMFromDoR(
                                    if (ben.genDetails?.ageAtMarriage == null) 0 else (ben.age - ben.genDetails!!.ageAtMarriage),
                                    ben.regDate
                                )
                            }
                        }
                }

            ben.genDetails?.let { gen ->
                val selectedValue = reproductiveStatus.value?.trim() ?: ""
                val reproductiveMap = mapOf(
                    "Eligible Couple" to 1,
                    "Pregnant Woman" to 2,
                    "Postnatal Mother" to 3,
                    "Elderly Woman" to 4,
                    "Adolescent Girl" to 5,
                    "Permanently Sterilised" to 6
                )

                val selectedId = reproductiveMap[selectedValue] ?: 0

                gen.reproductiveStatusId = selectedId
                gen.reproductiveStatus = selectedValue
            }

            ben.kidDetails?.birthCertificateNumber =
                birthCertificateNumber.value

            ben.kidDetails?.birthPlaceId =
                placeOfBirth.getPosition()
            ben.kidDetails?.birthPlace =
                placeOfBirth.getEnglishStringFromPosition(placeOfBirth.getPosition())
            ben.kidDetails?.placeName = otherPlaceOfBirth.value
            ben.kidDetails?.facilityId = facility.getPosition()
            ben.kidDetails?.facilityOther = otherFacility.value

            ben.kidDetails?.conductedDeliveryId =
                whoConductedDelivery.getPosition()
            ben.kidDetails?.conductedDelivery =
                whoConductedDelivery.getEnglishStringFromPosition(whoConductedDelivery.getPosition())

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
                babyCriedImmediatelyAfterBirth.getEnglishStringFromPosition(
                    babyCriedImmediatelyAfterBirth.getPosition()
                )
            ben.kidDetails?.birthDefectsId =
                anyDefectAtBirth.getPosition()
            ben.kidDetails?.birthDefects =
                anyDefectAtBirth.getEnglishStringFromPosition(anyDefectAtBirth.getPosition())

            ben.kidDetails?.heightAtBirth =
                babyHeight.value?.takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0
            ben.kidDetails?.weightAtBirth =
                babyWeight.value?.takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0


            ben.isDraft = false

        }
    }

    fun updateHouseholdWithHoFDetails(household: HouseholdCache, ben: BenRegCache) {
        household.family?.familyHeadName = ben.firstName
        household.family?.familyName = ben.lastName
    }

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            pic.id -> {
                pic.value = dpUri.toString()
                pic.errorText = null
            }
        }
    }


    fun yearsAgo(years: Int): Long {
        return Calendar.getInstance().apply {
            val today = Calendar.getInstance().setToStartOfTheDay()
            timeInMillis = today.timeInMillis
            add(Calendar.YEAR, -years)
        }.timeInMillis
    }
    private fun updateReproductiveOptionsBasedOnAgeGender() {
        val age = getAgeFromDob(getLongFromDate(agePopup.value))
        val genderIsFemale = gender.value == gender.entries?.get(1)

        if (benIfDataExist == null) {
            reproductiveStatus.isEnabled = genderIsFemale

            val updatedReproductiveOptions = when {
                !genderIsFemale -> emptyList()
                age in 15..19 -> listOf(
                    "Adolescent Girl", "Eligible Couple", "Pregnant Woman",
                    "Postnatal Mother", "Permanently Sterilised"
                )
                age in 20..49 -> listOf(
                    "Eligible Couple", "Pregnant Woman",
                    "Postnatal Mother", "Permanently Sterilised"
                )
                age >= 50 -> listOf("Elderly Woman")
                else -> emptyList()
            }

            reproductiveStatus.entries = updatedReproductiveOptions.toTypedArray()
            reproductiveStatus.value = null
        } else {
            // If data is pre-filled, disable the field and set value from saved data
            reproductiveStatus.isEnabled = false
            reproductiveStatus.value = benIfDataExist?.genDetails?.reproductiveStatusId?.let {
                reproductiveStatus.getStringFromPosition(it)
            }

            // Set DOB constraints based on existing reproductive status
            when (reproductiveStatus.value) {
                "Adolescent Girl" -> {
                    agePopup.max = yearsAgo(15)
                    agePopup.min = yearsAgo(19)
                }
                "Eligible Couple", "Pregnant Woman", "Postnatal Mother", "Permanently Sterilised" -> {
                    agePopup.max = yearsAgo(20)
                    agePopup.min = yearsAgo(49)
                }
                "Elderly Woman" -> {
                    agePopup.max = yearsAgo(50)
                    agePopup.min = yearsAgo(100)
                }
            }
        }
    }

}
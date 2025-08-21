package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.text.InputType
import androidx.core.text.isDigitsOnly
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants.maxAgeForGenBen
import org.piramalswasthya.sakhi.helpers.Konstants.minAgeForGenBen
import org.piramalswasthya.sakhi.helpers.Konstants.minAgeForMarriage
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.DROPDOWN
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.IMAGE_VIEW
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BenGenRegFormDataset(context: Context, language: Languages) : Dataset(context, language) {
    companion object {

        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }


        private fun getMinDobMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1 * maxAgeForGenBen)
            return cal.timeInMillis
        }

        private fun getMaxDobMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1 * minAgeForGenBen)
            return cal.timeInMillis
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
    private val age = FormElement(
        id = 5,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_age),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = maxAgeForGenBen.toLong(),
        min = minAgeForGenBen.toLong(),
    )
    private val dob = FormElement(
        id = 6,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.nbr_dob),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )
    private val gender = FormElement(
        id = 7,
        inputType = RADIO,
        title = resources.getString(R.string.nbr_gender),
        arrayId = R.array.nbr_gender_array,
        entries = resources.getStringArray(R.array.nbr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val maritalStatusMale = resources.getStringArray(R.array.nbr_marital_status_male_array)

    private val maritalStatusFemale =
        resources.getStringArray(R.array.nbr_marital_status_male_array)
    private val maritalStatus = FormElement(
        id = 8,
        inputType = DROPDOWN,
        title = resources.getString(R.string.marital_status),
        arrayId = R.array.nbr_marital_status_male_array,
        entries = maritalStatusMale,
        required = true,
        hasDependants = true,
    )
    private val husbandName = FormElement(
        id = 9,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.husband_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,

        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val wifeName = FormElement(
        id = 10,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.wife_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,

        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val spouseName = FormElement(
        id = 11,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.spouse_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val ageAtMarriage = FormElement(
        id = 12,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.age_at_marriage),
        etMaxLength = 2,
        arrayId = -1,
        required = true,
        hasDependants = true,

        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        min = minAgeForMarriage.toLong(),
        max = maxAgeForGenBen.toLong()
    )
    private val dateOfMarriage = FormElement(
        id = 13,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.date_of_marriage),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = getMinDobMillis(),
    )
    private val fatherName = FormElement(
        id = 14,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.father_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,

        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val motherName = FormElement(
        id = 15,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.mother_s_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,

        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val mobileNoOfRelation = FormElement(
        id = 16,
        inputType = DROPDOWN,
        title = resources.getString(R.string.mobile_number_of),
        arrayId = R.array.nbr_mobile_no_relation_array,
        entries = resources.getStringArray(R.array.nbr_mobile_no_relation_array),
        required = true,
        hasDependants = true,
    )
    private val otherMobileNoOfRelation = FormElement(
        id = 17,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.other_mobile_number_of),
        arrayId = -1,
        required = true
    )
    private val contactNumber = FormElement(
        id = 18,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.contact_number),
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
        id = 19,
        inputType = DROPDOWN,
        title = resources.getString(R.string.relation_with_family_head),
        arrayId = R.array.nbr_relationship_to_head,
        entries = relationToHeadListDefault,
        required = true,
        hasDependants = true,
    )
    private val otherRelationToHead = FormElement(
        id = 20,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.other_enter_relation_to_head),
        arrayId = -1,
        required = true,
        allCaps = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val community = FormElement(
        id = 21,
        inputType = DROPDOWN,
        title = resources.getString(R.string.community),
        arrayId = R.array.community_array,
        entries = resources.getStringArray(R.array.community_array),
        required = true
    )
    private val religion = FormElement(
        id = 22,
        inputType = DROPDOWN,
        title = resources.getString(R.string.religion),
        arrayId = R.array.religion_array,
        entries = resources.getStringArray(R.array.religion_array),
        required = true,
        hasDependants = true
    )
    private val otherReligion = FormElement(
        id = 23,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.other_enter_religion),
        arrayId = -1,
//        allCaps = true,
        required = true
    )

    private val firstPage: List<FormElement> by lazy {
        listOf(
            pic,
            dateOfReg,
            firstName,
            lastName,
            dob,
            age,
            gender,
            maritalStatus,
            fatherName,
            motherName,
            relationToHead,
            mobileNoOfRelation,
            community,
            religion,
        )
    }

    //////////////////////////////////Second Page////////////////////////////////////

    private val hasAadharNo = FormElement(
        id = 24,
        inputType = RADIO,
        title = resources.getString(R.string.has_aadhaar_number),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val aadharNo = FormElement(
        id = 25,
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

    private val rchId = FormElement(
        id = 26,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.rch_id_ben),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12

    )

    private val secondPage = listOf(
        hasAadharNo, rchId
    )

    private val reproductiveStatus = FormElement(
        id = 28,
        inputType = DROPDOWN,
        title = resources.getString(R.string.reproductive_status),
        arrayId = R.array.nbr_reproductive_status_array,
        entries = resources.getStringArray(R.array.nbr_reproductive_status_array),
        required = true,
        hasDependants = true
    )

    private val thirdPage = listOf(
        reproductiveStatus,
//        lastMenstrualPeriod,

    )

    suspend fun setFirstPage(ben: BenRegCache?, familyHeadPhoneNo: Long?) {
        val list = firstPage.toMutableList()
        contactNumberFamilyHead.value = familyHeadPhoneNo?.toString()
        this.familyHeadPhoneNo = familyHeadPhoneNo?.toString()
        if (dateOfReg.value == null)
            dateOfReg.value = getCurrentDateString()
        ben?.takeIf { !it.isDraft }?.let { saved ->
            pic.value = saved.userImage
            dateOfReg.value = getDateFromLong(saved.regDate)
            firstName.value = saved.firstName
            lastName.value = saved.lastName
            dob.value = getDateFromLong(saved.dob)
            age.value = BenBasicCache.getAgeFromDob(saved.dob).toString()
            gender.value = gender.getStringFromPosition(saved.genderId)
            fatherName.value = saved.fatherName
            motherName.value = saved.motherName
            saved.genDetails?.spouseName?.let {
                when (saved.genderId) {
                    1 -> wifeName.value = it
                    2 -> husbandName.value = it
                    3 -> spouseName.value = it
                }
            }
            maritalStatus.value =
                maritalStatus.getStringFromPosition(saved.genDetails?.maritalStatusId ?: 0)
            ageAtMarriage.value = saved.genDetails?.ageAtMarriage.toString()
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

        }
        maritalStatus.entries = when (gender.value) {
            gender.entries!![1] -> maritalStatusFemale
            else -> maritalStatusMale
        }
        relationToHead.entries = when (gender.value) {
            gender.entries!![0] -> relationToHeadListMale
            gender.entries!![1] -> relationToHeadListFemale
            else -> relationToHeadListDefault
        }
        /// Set up fields
        if (maritalStatus.value != maritalStatus.entries!![0] && gender.value != null) {
//            if(maritalStatus.value ==maritalStatus.entries!![1])
//            list.removeAll(listOf(fatherName, motherName))
            list.add(
                list.indexOf(maritalStatus) + 3, when (gender.value) {
                    gender.entries!![0] -> wifeName
                    gender.entries!![1] -> husbandName
                    gender.entries!![2] -> spouseName
                    else -> throw java.lang.IllegalStateException("Gender unspecified with non empty marital status value!")
                }
            )
            list.add(list.indexOf(maritalStatus) + 4, ageAtMarriage)
        }
        if (maritalStatus.value == maritalStatus.entries!![1] && gender.value == gender.entries!![1]) {
            fatherName.required = false
            motherName.required = false

        }
        if (maritalStatus.value == maritalStatus.entries!![2]) {
            husbandName.required = false
            wifeName.required = false
            spouseName.required = false

        }
        ageAtMarriage.value?.takeIf { it.isNotEmpty() && it == age.value }?.let {
            list.add(list.indexOf(ageAtMarriage) + 1, dateOfMarriage)
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
        if (religion.value == religion.entries!![6]) {
            list.add(list.indexOf(religion) + 1, otherReligion)
        }

        setUpPage(list)
    }

    fun getIndexOfRelationToHead() = getIndexOfElement(relationToHead)
    fun getIndexOfAgeAtMarriage() = getIndexOfElement(ageAtMarriage)

    fun getIndexOfFatherName() = getIndexById(fatherName.id)
    fun getIndexOfMotherName() = getIndexById(motherName.id)
    fun getIndexOfSpouseName() = getIndexById(husbandName.id).takeIf { it != -1 }
        ?: getIndexById(wifeName.id).takeIf { it != -1 } ?: getIndexById(spouseName.id)

    fun getIndexOfMaritalStatus() = getIndexById(maritalStatus.id)

    suspend fun setSecondPage(ben: BenRegCache?) {
        val list = secondPage.toMutableList()
        ben?.takeIf { !it.isDraft }?.let { saved ->
            hasAadharNo.value = hasAadharNo.getStringFromPosition(saved.hasAadharId)
            aadharNo.value = saved.aadharNum
            rchId.value = saved.rchId
        }
        if (hasAadharNo.value == hasAadharNo.entries!!.first()) list.add(
            list.indexOf(hasAadharNo) + 1, aadharNo
        )
        setUpPage(list)
    }

    suspend fun setThirdPage(ben: BenRegCache?) {
        val list = thirdPage.toMutableList()
        ben?.takeIf { !it.isDraft }?.let { saved ->
//            lastMenstrualPeriod.value = saved.genDetails?.lastMenstrualPeriod?.let {
//                getDateFromLong(
//                    it
//                )
//            }
            reproductiveStatus.value = reproductiveStatus.getStringFromPosition(
                saved.genDetails?.reproductiveStatusId ?: 0
            )
        }
        setUpPage(list)
    }


    fun hasThirdPage(): Boolean {
        return gender.value == gender.entries!![1] && maritalStatus.value != maritalStatus.entries!!.first()

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            firstName.id -> {
                validateEmptyOnEditText(firstName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(firstName)
               // validateAllCapsOrSpaceOnEditText(firstName)

            }

            lastName.id -> {
               // validateAllCapsOrSpaceOnEditText(lastName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(lastName)
            }

            dob.id -> {
                assignValuesToAgeFromDob(getLongFromDate(dob.value), age)
                age.value?.takeIf { it.isNotEmpty() }?.toLong()?.let { ageAtMarriage.max = it }
                -1
            }

            age.id -> {

                if (age.value.isNullOrEmpty()) {
                    validateEmptyOnEditText(age)
                    return -1
                }
                age.min = 15
                age.max = 99
                validateIntMinMax(age)
                if (age.errorText == null) {
                    val cal = Calendar.getInstance()
                    cal.add(
                        Calendar.YEAR, -1 * age.value!!.toInt()
                    )
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
                ageAtMarriage.value = null
                age.value?.toLong()?.let {
                    ageAtMarriage.max = it
                }
                triggerDependants(
                    source = age, passedIndex = index, triggerIndex = 0, target = dateOfMarriage
                )
            }

            gender.id, maritalStatus.id -> {
                relationToHead.value = null
                if (formId == gender.id) {
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

                    relationToHead.arrayId = when (index) {
                        0 -> R.array.nbr_relationship_to_head_male
                        1 -> R.array.nbr_relationship_to_head_female
                        else -> R.array.nbr_relationship_to_head
                    }

                    maritalStatus.value = null
                    return triggerDependants(
                        source = gender,
                        removeItems = listOf(otherRelationToHead),
                        addItems = emptyList()
                    )
                } else {
                    when (maritalStatus.value) {
                        maritalStatus.entries!![0] -> {
                            fatherName.required = true
                            motherName.required = true
                            return triggerDependants(
                                source = maritalStatus,
                                addItems = emptyList(),//listOf(fatherName, motherName),
                                removeItems = listOf(
                                    spouseName,
                                    husbandName,
                                    wifeName,
                                    ageAtMarriage
                                )
                            )
                        }

                        maritalStatus.entries!![1] -> {
                            if (gender.value == gender.entries!![1]) {
                                fatherName.required = false
                                motherName.required = false
                            } else {
                                fatherName.required = true
                                motherName.required = true
                            }
                            husbandName.required = true
                            wifeName.required = true
                            return triggerDependants(
                                source = maritalStatus, addItems = when (gender.value) {
                                    gender.entries!![0] -> listOf(wifeName, ageAtMarriage)
                                    gender.entries!![1] -> listOf(husbandName, ageAtMarriage)
                                    else -> listOf(spouseName, ageAtMarriage)
                                }, removeItems = listOf(
                                    wifeName,
                                    husbandName,
                                    spouseName,
                                    ageAtMarriage
                                )
                            )
                        }

                        else -> {
                            husbandName.required = maritalStatus.value != maritalStatus.entries!![2]
                            wifeName.required = maritalStatus.value != maritalStatus.entries!![2]
                            fatherName.required = true
                            motherName.required = true
//                            ().let {
////                            wifeName.required = it
////                            husbandName.required = it
//                                spouseName.required = it
//                            }
                            return triggerDependants(
                                source = maritalStatus, addItems = when (gender.value) {
                                    gender.entries!![0] -> listOf(wifeName, ageAtMarriage)
                                    gender.entries!![1] -> listOf(husbandName, ageAtMarriage)
                                    else -> listOf(spouseName, ageAtMarriage)
                                }, removeItems = listOf(
                                    wifeName,
                                    husbandName,
                                    spouseName,
                                    ageAtMarriage
                                )
                            )
                        }
                    }

                }

            }

            ageAtMarriage.id -> age.value?.takeIf { it.isNotEmpty() && it.isDigitsOnly() && !ageAtMarriage.value.isNullOrEmpty() }
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

            fatherName.id -> {
                validateEmptyOnEditText(fatherName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(fatherName)
               // validateAllCapsOrSpaceOnEditText(fatherName)
            }

            motherName.id -> {
                validateEmptyOnEditText(motherName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(motherName)
               // validateAllCapsOrSpaceOnEditText(motherName)
            }

            husbandName.id -> {
                validateEmptyOnEditText(husbandName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(husbandName)
               // validateAllCapsOrSpaceOnEditText(husbandName)
            }

            wifeName.id -> {
                validateEmptyOnEditText(wifeName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(wifeName)
              //  validateAllCapsOrSpaceOnEditText(wifeName)
            }

            spouseName.id -> {
                validateEmptyOnEditText(spouseName)
                //validateAllCapsOrSpaceOnEditText(spouseName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(spouseName)
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

            otherMobileNoOfRelation.id -> validateEmptyOnEditText(otherMobileNoOfRelation)
            relationToHead.id -> {
                triggerDependants(
                    source = relationToHead,
                    passedIndex = index,
                    triggerIndex = relationToHead.entries!!.lastIndex,
                    target = otherRelationToHead
                )
            }

            otherRelationToHead.id -> validateEmptyOnEditText(otherRelationToHead)
            religion.id -> {
                triggerDependants(
                    source = religion, passedIndex = index, triggerIndex = 7, target = otherReligion
                )
            }

            otherReligion.id -> validateEmptyOnEditText(otherReligion)
            hasAadharNo.id -> triggerDependants(
                source = hasAadharNo, passedIndex = index, triggerIndex = 0, target = aadharNo
            )

            aadharNo.id -> validateAadharNoOnEditText(aadharNo)
            rchId.id -> validateRchIdOnEditText(rchId)

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as BenRegCache).let { ben ->
            //      Page 001
            ben.userImage = pic.value
            ben.regDate = getLongFromDate(dateOfReg.value!!)
            ben.firstName = firstName.value
            ben.lastName = lastName.value
            ben.dob = getLongFromDate(dob.value!!)
            ben.age = age.value?.toInt() ?: 0
            ben.ageUnit = AgeUnit.YEARS
            ben.ageUnitId = 3
            ben.genderId = when (gender.value) {
                gender.entries!![0] -> 1
                gender.entries!![1] -> 2
                gender.entries!![2] -> 3
                else -> 0
            }
            ben.gender = when (ben.genderId) {
                1 -> Gender.MALE
                2 -> Gender.FEMALE
                3 -> Gender.TRANSGENDER
                else -> null
            }
//            ben.registrationType = TypeOfList.GENERAL
            ben.genDetails?.maritalStatusId = maritalStatus.getPosition()
            ben.genDetails?.maritalStatus =
                maritalStatus.getEnglishStringFromPosition(ben.genDetails?.maritalStatusId ?: 0)
            ben.genDetails?.spouseName = husbandName.value.takeIf { !it.isNullOrEmpty() }
                ?: wifeName.value.takeIf { !it.isNullOrEmpty() }
                        ?: spouseName.value.takeIf { !it.isNullOrEmpty() }
            ben.genDetails?.ageAtMarriage =
                ageAtMarriage.value?.toInt() ?: 0
            ben.genDetails?.marriageDate =
                dateOfMarriage.value?.let { getLongFromDate(it) }
                    ?: getDoMFromDoR(ben.genDetails?.ageAtMarriage, ben.regDate)
            ben.fatherName = fatherName.value
            ben.motherName = motherName.value
            ben.familyHeadRelationPosition =
                relationToHeadListDefault.indexOf(relationToHead.value) + 1
            ben.familyHeadRelation =
                englishResources.getStringArray(R.array.nbr_relationship_to_head)[ben.familyHeadRelationPosition - 1]

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
            ben.rchId = rchId.value

            //Page 002
            ben.hasAadhar = when (hasAadharNo.value) {
                hasAadharNo.entries!![0] -> true
                hasAadharNo.entries!![1] -> false
                else -> null
            }
            ben.hasAadharId = when (ben.hasAadhar) {
                true -> 1
                false -> 2
                else -> 0
            }
            ben.aadharNum = aadharNo.value
            ben.rchId = rchId.value

            //Page 003
            ben.genDetails?.let { gen ->
                gen.reproductiveStatusId = reproductiveStatus.getPosition()
                gen.reproductiveStatus =
                    reproductiveStatus.getEnglishStringFromPosition(gen.reproductiveStatusId)
            }
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
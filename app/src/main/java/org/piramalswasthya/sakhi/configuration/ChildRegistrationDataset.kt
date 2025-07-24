package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegKid
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User

class ChildRegistrationDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfReg = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = context.getString(R.string.nbr_dor),
        arrayId = -1,
        required = true,
        min = getMinDateOfReg(),
        max = System.currentTimeMillis()
    )

    private val childName = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = "Name of Child",
        required = false,
        hasDependants = false
    )
    private val rchId = FormElement(
        id = 3,
        inputType = InputType.EDIT_TEXT,
        title = "RCH ID No. of Child",
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12

    )
    private val dob = FormElement(
        id = 4,
        inputType = InputType.DATE_PICKER,
        title = "Date of Birth",
        arrayId = -1,
        required = true,
        min = getMinDateOfReg(),
        isEnabled = false,
        max = System.currentTimeMillis()
    )

    private val childGender = FormElement(
        id = 5,
        inputType = InputType.RADIO,
        title = "Sex of Child",
        entries = arrayOf(resources.getString(R.string.male), resources.getString(R.string.female)),
        isEnabled = false,
        required = false,
        hasDependants = false
    )


    private val motherName = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = "Mother's Name",
        arrayId = -1,
        required = true,
        allCaps = true,
        isEnabled = false,
        hasSpeechToText = true,

        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val fatherName = FormElement(
        id = 7,
        inputType = InputType.EDIT_TEXT,
        title = "Father's Name",
        arrayId = -1,
        required = true,
        allCaps = true,
        isEnabled = false,
        hasSpeechToText = true,

        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val mobileNumberOf = FormElement(
        id = 8,
        inputType = InputType.RADIO,
        title = "Whose Mobile Number",
        entries = arrayOf(
            "Mother",
            "Father",
            "Others"
        ),
        isEnabled = false,

        required = false,
        hasDependants = false
    )

    private val mobileNumber = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title = "Mobile No.",
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        isEnabled = false,

        max = 9999999999,
        min = 6000000000
    )
    private val rchIdMother = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = "RCH ID No. of Mother",
        arrayId = -1,
        required = false,
        isEnabled = false,

        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12

    )

    private val birthCertificateNo = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = "Birth Certificate Number",
        arrayId = -1,
        required = false,
    )
    private val weightAtBirth = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = "Weight at Birth (kg)",
        arrayId = -1,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL,
        minDecimal = 0.5,
        maxDecimal = 7.0,
        etMaxLength = 3,
        required = false,
    )

    private val placeOfBirth = FormElement(
        id = 13,
        inputType = InputType.DROPDOWN,
        title = "Place of Birth",
        entries = arrayOf(
            "District Hospital",
            "Community Health Centre",
            "Primary Health Centre",
            "Sub Centre",
            "Other Public Facility",
            "Accredited Private Hospital",
            "Other Private Hospital ",
            "Home",
            "Sub District Hospital",
            "Medical College Hospital",
            "In Transit"
        ),
        required = false,
        hasDependants = false
    )
    private val headLine = FormElement(
        id = 13,
        inputType = InputType.HEADLINE,
        title = "Birth Certificate Uploads",
        headingLine = false,
        required = false,
    )
    private val fileUploadFront = FormElement(
        id = 14,
        inputType = InputType.FILE_UPLOAD,
        title = "Front Side",
        required = false,
    )
    private val fileUploadBack = FormElement(
        id = 15,
        inputType = InputType.FILE_UPLOAD,
        title = "Back Side",
        required = false,
    )


    fun getIndexOfBirthCertificateFrontPath() = getIndexById(fileUploadFront.id)
    fun getIndexOfBirthCertificateBackPath() = getIndexById(fileUploadBack.id)

    suspend fun setUpPage(
        motherBen: BenRegCache?,
        deliveryOutcomeCache: DeliveryOutcomeCache?,
        infantRegCache: InfantRegCache?,
    ) {
        val list = mutableListOf(
            dateOfReg,
            childName,
            rchId,
            dob,
            childGender,
            motherName,
            fatherName,
            mobileNumberOf,
            mobileNumber,
            rchIdMother,
            birthCertificateNo,
            weightAtBirth,
            placeOfBirth,
            headLine,
            fileUploadFront,
            fileUploadBack

        )
        dateOfReg.value = getDateFromLong(System.currentTimeMillis())
        dateOfReg.min = deliveryOutcomeCache?.dateOfDelivery
        motherBen?.let {
            it.genDetails?.spouseName?.let {
                fatherName.value = it
            } ?: run {
                fatherName.inputType = InputType.EDIT_TEXT
            }
            motherName.value = "${it.firstName} ${it.lastName ?: ""}"
            mobileNumberOf.value = mobileNumberOf.entries?.first()
            mobileNumber.value = it.contactNumber.toString()
            rchIdMother.value = it.rchId
        }
        deliveryOutcomeCache?.dateOfDelivery?.let {
            dob.value = getDateFromLong(it)
        }
        deliveryOutcomeCache?.placeOfDelivery?.let {
            placeOfBirth.value = it
        }
        infantRegCache?.let { infant ->
            childName.value = infant.babyName
            weightAtBirth.value = infant.weight?.toString()
            infant.gender?.let {
                childGender.value = childGender.entries?.get(it.ordinal)
            }
        }


        //TODO(Set up mapping values!)
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            childName.id -> validateAllCapsOrSpaceOnEditText(childName)
            rchId.id -> validateRchIdOnEditText(rchId)
            rchIdMother.id -> validateRchIdOnEditText(rchIdMother)
            mobileNumber.id -> validateMobileNumberOnEditText(mobileNumber)
            fatherName.id -> validateAllCapsOrSpaceOnEditText(fatherName)
            weightAtBirth.id -> validateDoubleMinMax(weightAtBirth)
            birthCertificateNo.id -> validateNoAlphabetSpaceOnEditText(birthCertificateNo)

            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as InfantRegCache).let { form ->


        }
    }

    fun mapAsBeneficiary(
        motherBen: BenRegCache,
        user: User,
        locationRecord: LocationRecord
    ): BenRegCache {
        val dob = getLongFromDate(dob.value)
        val age = BenBasicCache.getAgeFromDob(dob)
        val ageUnit = BenBasicCache.getAgeUnitFromDob(dob)
        val gender = when (childGender.value) {
            childGender.entries!!.first() -> Gender.MALE
            else -> Gender.FEMALE
        }
        val familyHeadRelationId =
            getFamilyHeadRelationFromMother(gender, motherBen.familyHeadRelationPosition)
        val familyHeadRelation = getRelationStringFromId(familyHeadRelationId)

        return BenRegCache(
            ashaId = user.userId,
            beneficiaryId = 0,
            createdDate = System.currentTimeMillis(),
            updatedBy = user.userName,
            createdBy = user.userName,
            updatedDate = System.currentTimeMillis(),
            householdId = motherBen.householdId,
            isAdult = false,
            isKid = true,
            isDraft = true,
            locationRecord = locationRecord,
            syncState = SyncState.UNSYNCED,
            //Mapping values
            firstName = childName.value,
            lastName = null,
            regDate = getLongFromDate(dateOfReg.value),
            dob = dob,
            age = age,
            ageUnit = ageUnit,
            ageUnitId = ageUnit.ordinal + 1,
            gender = gender,
            genderId = gender.ordinal + 1,
            fatherName = fatherName.value,
            motherName = motherName.value,
            familyHeadRelation = familyHeadRelation,
            familyHeadRelationPosition = familyHeadRelationId,
            contactNumber = motherBen.contactNumber,
            mobileNoOfRelation = "Mother",
            mobileNoOfRelationId = 3,
            community = motherBen.community,
            communityId = motherBen.communityId,
            religion = motherBen.religion,
            religionId = motherBen.religionId,
            rchId = rchId.value,
            processed = "N",
            kidDetails = BenRegKid(
                childName = childName.value,
                birthPlace = placeOfBirth.value,
                birthPlaceId = placeOfBirth.getPosition(),
                birthCertificateNumber = birthCertificateNo.value,
                birthCertificateFileFrontView = fileUploadFront.value.toString(),
                birthCertificateFileBackView = fileUploadBack.value.toString()
            ),
            isConsent = false
        )
    }

    private fun getRelationStringFromId(familyHeadRelationId: Int): String {
        return if (familyHeadRelationId == 9)
            "Son"
        else
            "Daughter"
    }

    private fun getFamilyHeadRelationFromMother(childGender: Gender, motherRelationId: Int): Int {
        return if (childGender == Gender.MALE)
            9
        else
            10
    }

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        if (lastImageFormId == 14) {
                fileUploadFront.value = dpUri.toString()
                fileUploadFront.errorText = null

        } else {
                fileUploadBack.value = dpUri.toString()
                fileUploadBack.errorText = null

        }


    }
}
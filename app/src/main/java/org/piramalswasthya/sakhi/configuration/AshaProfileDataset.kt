package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.AshaProfileRepo

class AshaProfileDataset(
    context: Context, currentLanguage: Languages,var ashaProfileRepo: AshaProfileRepo
) : Dataset(context, currentLanguage) {

    private val pic = FormElement(
        id = 1,
        inputType = InputType.IMAGE_VIEW,
        title = resources.getString(R.string.nbr_image),
        subtitle = resources.getString(R.string.nbr_image_sub),
        arrayId = -1,
        required = false
    )

    private var ashaName = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.ashaname),
        required = true,
        allCaps = true,
       
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS

    )

    private val village = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.village),
        required = false,
    )

    private val loginuserName = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.loginusername),
        required = false,
    )

    private val userId = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.asha_empid),
        required = false,
        etMaxLength = 12

    )

    private val dob = FormElement(
        id = 4,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.date_of_birth),
        required = false,
        max = BenGenRegFormDataset.getMaxDobMillis(),
        min = BenGenRegFormDataset.getMinDobMillis(),
    )

    private val ages = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.age),
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForGenBen.toLong(),
        min = Konstants.minAgeForGenBen.toLong(),
    )

    private val mobileNumber = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.mobile_number),
        required = true,
        isMobileNumber = true,
        etMaxLength = 10,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,


        )

    private val alternameMobileNumber = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = "Alternate Mobile No.",
        required = false,
        etMaxLength = 10,
        isMobileNumber = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,

        )

    private val fatherOrspouse = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = "Father or Spouse",
        required = false,
        arrayId = R.array.fathers_spouse,
        value = resources.getStringArray(R.array.fathers_spouse)[0],
        entries = resources.getStringArray(R.array.fathers_spouse),
        hasDependants = true,
    )

    private val spouseOrFatherNameEdt = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = "Father or Spouse",
        required = false,
        arrayId = R.array.fathers_spouse,
        hasDependants = true,
        allCaps = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dateOfJoining = FormElement(
        id = 9,
        inputType = InputType.TEXT_VIEW,
        title = "Date of Joining",
        required = false,

        )


    private val bankAccount = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.bank_acc),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 18,
        max = 999999999999999999L,
        min = 100000000L,
        showDrawable = false
    )
    private val Ifsc = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title = "IFSC",
        required = false,
        etMaxLength = 11
        )
    private val populationCovered = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title = "Population Covered under ASHA",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false,
        etMaxLength = 4,
    )
    private val ashaSupervisorName = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = "Asha Supervisor name",
        required = false,
        allCaps = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS

    )
    private val ashaSupervisorContactNumber = FormElement(
        id = 14,
        inputType = InputType.EDIT_TEXT,
        title = "Asha Supervisor Contact No.",
        required = false,
        etMaxLength = 10,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,

        )
    private val ChoName = FormElement(
        id = 15,
        inputType = InputType.EDIT_TEXT,
        title = "CHO name.",
        required = false,
        allCaps = true,
       
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val ChoMobileNo = FormElement(
        id = 16,
        inputType = InputType.EDIT_TEXT,
        title = "Mobile No. of CHO",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 10,
    )
    private val nameOfAWW = FormElement(
        id = 17,
        inputType = InputType.EDIT_TEXT,
        title = "Name of AWW",
        required = false,
        allCaps = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val  mobieNoOfAWW = FormElement(
        id = 18,
        inputType = InputType.EDIT_TEXT,
        title = "Mobile No. AWW",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 10,
    )
    private val nameOfANM1 = FormElement(
        id = 19,
        inputType = InputType.EDIT_TEXT,
        title = "Name of ANM1",
        required = false,
        allCaps = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val  mobileNoOfANM1 = FormElement(
        id = 20,
        inputType = InputType.EDIT_TEXT,
        title = "Mobile number of ANM1",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false,
        etMaxLength = 10,
    )
    private val nameOfANM2 = FormElement(
        id = 21,
        inputType = InputType.EDIT_TEXT,
        title = "Name of ANM2",
        required = false,
        allCaps = true,
       
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val  mobileNoOfANM2 = FormElement(
        id = 22,
        inputType = InputType.EDIT_TEXT,
        title = "Mobile number of ANM2",
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false,
        etMaxLength = 10,
    )
    private val  abhaNumber = FormElement(
        id = 23,
        inputType = InputType.EDIT_TEXT,
        title = "ABHA number",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 14,
        min = 10000000000000L,
        max = 99999999999999L


    )
    private val  ashaHouseholdRegistrationNo = FormElement(
        id = 24,
        inputType = InputType.EDIT_TEXT,
        title = "Asha Household registration",
        required = false,
    )
    private val  ashaFamilymember = FormElement(
        id = 25,
        inputType = InputType.EDIT_TEXT,
        title = "Asha Family member",
        required = false,
    )


    suspend fun setUpPage(
        currentUser: User,
        ashaProfile: ProfileActivityCache?,
    ) {
        val list = mutableListOf(
            pic,
            ashaName,
            village,
            loginuserName,
            userId,
            dob,
            ages,
            mobileNumber,
            alternameMobileNumber,
            fatherOrspouse,
            spouseOrFatherNameEdt,
            dateOfJoining,
            bankAccount,
            Ifsc,
            populationCovered,
            ashaSupervisorName,
            ashaSupervisorContactNumber,
            ChoName,
            ChoMobileNo,
            nameOfAWW,
            mobieNoOfAWW,
            nameOfANM1,
            mobileNoOfANM1,
            nameOfANM2,
            mobileNoOfANM2,
            abhaNumber,
            ashaHouseholdRegistrationNo,
            ashaFamilymember


        )

        ashaName.value = ashaProfile?.name
        pic.value = ashaProfile?.profileImage
        village.value = ashaProfile?.village
        loginuserName.value = currentUser.userName
        userId.value = ashaProfile?.employeeId.toString()
        mobileNumber.value = ashaProfile?.mobileNumber.toString()
        alternameMobileNumber.value = ashaProfile?.alternateMobileNumber.toString()
        dateOfJoining.value = ashaProfile?.dateOfJoining.toString()
        bankAccount.value = ashaProfile?.bankAccount.toString()
        Ifsc.value = ashaProfile?.ifsc.toString()
        dob.value =dateFormate(ashaProfile?.dob.toString())
        ages.value = ashaProfile?.dob?.let { BenBasicCache.getAgeFromDob(getLongFromDate(dateFormate(ashaProfile?.dob.toString()))).toString() }
        populationCovered.value = ashaProfile?.populationCovered.toString()
        spouseOrFatherNameEdt.value = ashaProfile?.fatherOrSpouseName.toString()
        fatherOrspouse.value = fatherOrspouse.getStringSpauseFromPosition(if(ashaProfile?.isFatherOrSpouse==true) 1 else 0)
        ashaSupervisorName.value = ashaProfile?.supervisorName.toString()
        ashaSupervisorContactNumber.value = ashaProfile?.supervisorMobile.toString()
        ChoName.value = ashaProfile?.choName.toString()
        ChoMobileNo.value = ashaProfile?.choMobile.toString()
        nameOfAWW.value = ashaProfile?.awwName.toString()
        mobieNoOfAWW.value = ashaProfile?.awwMobile.toString()
        nameOfANM1.value = ashaProfile?.anm1Name.toString()
        mobileNoOfANM1.value = ashaProfile?.anm1Mobile.toString()
        nameOfANM2.value = ashaProfile?.anm2Name.toString()
        mobileNoOfANM2.value = ashaProfile?.anm2Mobile.toString()
        abhaNumber.value = ashaProfile?.abhaNumber.toString()
        ashaHouseholdRegistrationNo.value = ashaProfile?.ashaHouseholdRegistration.toString()
        ashaFamilymember.value = ashaProfile?.ashaFamilyMember.toString()
        setUpPage(list)

    }



    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            ashaName.id -> {
                validateEmptyOnEditText(ashaName)
                validateAllCapsOrSpaceOnEditText(ashaName)
            }

            spouseOrFatherNameEdt.id -> {
                validateEmptyOnEditText(spouseOrFatherNameEdt)
                validateAllCapsOrSpaceOnEditText(spouseOrFatherNameEdt)
            }
            ashaSupervisorName.id -> {
                validateEmptyOnEditText(ashaSupervisorName)
                validateAllCapsOrSpaceOnEditText(ashaSupervisorName)
            }

            nameOfAWW.id -> {
                validateEmptyOnEditText(nameOfAWW)
                validateAllCapsOrSpaceOnEditText(nameOfAWW)
            }

            nameOfANM1.id -> {
                validateEmptyOnEditText(nameOfANM1)
                validateAllCapsOrSpaceOnEditText(nameOfANM1)
            }

            nameOfANM2.id -> {
                validateEmptyOnEditText(nameOfANM2)
                validateAllCapsOrSpaceOnEditText(nameOfANM2)
            }

            ChoName.id -> {
                validateEmptyOnEditText(ChoName)
                validateAllCapsOrSpaceOnEditText(ChoName)
            }

            mobileNumber.id -> {
                validateEmptyOnEditText(mobileNumber)
                validateMobileNumberOnEditText(mobileNumber)
            }
            alternameMobileNumber.id -> {
                validateEmptyOnEditText(alternameMobileNumber)
                validateMobileNumberOnEditText(alternameMobileNumber)
            }
            bankAccount.id -> {
                validateIntMinMax(bankAccount)
            }
            ashaSupervisorContactNumber.id -> {
                validateEmptyOnEditText(ashaSupervisorContactNumber)
                validateMobileNumberOnEditText(ashaSupervisorContactNumber)
            }
            mobileNoOfANM1.id -> {
                validateEmptyOnEditText(mobileNoOfANM1)
                validateMobileNumberOnEditText(mobileNoOfANM1)
            }
            mobileNoOfANM2.id -> {
                validateEmptyOnEditText(mobileNoOfANM2)
                validateMobileNumberOnEditText(mobileNoOfANM2)
            }
            mobieNoOfAWW.id -> {
                validateEmptyOnEditText(mobieNoOfAWW)
                validateMobileNumberOnEditText(mobieNoOfAWW)
            }
            ChoMobileNo.id -> {
                validateEmptyOnEditText(ChoMobileNo)
                validateMobileNumberOnEditText(ChoMobileNo)
            }
            Ifsc.id -> {
                validateIFSCEditText(Ifsc)
            }

            abhaNumber.id -> {
                validateABHANumberEditText(abhaNumber)

            }
            dob.id -> {
                assignValuesToAgeFromDob(getLongFromDate(dob.value), ages)
                ages.value?.takeIf { it.isNotEmpty() }?.toLong()?.let {  }
                -1
            }

            else -> 1
        }
    }

    suspend fun mapProfileValues(cacheModel: ProfileActivityCache,context: Context){
        (cacheModel).let { dataModel ->
            dataModel.name = ashaName.value
            dataModel.profileImage = pic.value
            dataModel.village = village.value
            dataModel.dob = dateReverseFormat(dob.value.toString())
            dataModel.age = ages.value!!.toInt()
            dataModel.mobileNumber = mobileNumber.value
            dataModel.alternateMobileNumber = alternameMobileNumber.value
            dataModel.fatherOrSpouseName = spouseOrFatherNameEdt.value
            dataModel.supervisorName = ashaSupervisorName.value
            dataModel.supervisorMobile = ashaSupervisorContactNumber.value
            dataModel.dateOfJoining = dateOfJoining.value
            dataModel.bankAccount = bankAccount.value
            dataModel.ifsc = Ifsc.value
            dataModel.populationCovered = populationCovered.value!!.toInt()
            dataModel.choName = ChoName.value
            dataModel.choMobile = ChoMobileNo.value
            dataModel.awwName = nameOfAWW.value
            dataModel.awwMobile = mobieNoOfAWW.value
            dataModel.anm1Name = nameOfANM1.value
            dataModel.anm1Mobile = mobileNoOfANM1.value
            dataModel.anm2Name = nameOfANM2.value
            dataModel.anm2Mobile = mobileNoOfANM2.value
            dataModel.abhaNumber = abhaNumber.value
            dataModel.ashaHouseholdRegistration = ashaHouseholdRegistrationNo.value
            dataModel.ashaFamilyMember = ashaFamilymember.value
            dataModel.isFatherOrSpouse = when (fatherOrspouse.value) {
                fatherOrspouse.entries!![0] -> true
                fatherOrspouse.entries!![1] -> false
                else -> false
            }


        }
        ashaProfileRepo.postDataToAmritServer(cacheModel)
    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {

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
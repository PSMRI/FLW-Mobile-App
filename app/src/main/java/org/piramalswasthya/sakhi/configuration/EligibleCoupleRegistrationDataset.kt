package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EligibleCoupleRegistrationDataset(context: Context, language: Languages) :
    Dataset(context, language) {

    companion object {
        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

        private fun getLongFromDate(dateString: String): Long {
            val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date = f.parse(dateString)
            return date?.time ?: throw IllegalStateException("Invalid date for dateReg")
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

    //////////////////////////////// First Page /////////////////////////////////////////

    private val dateOfReg = FormElement(
        id = 0,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "Date of Registration",
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = 0L,
    )

    private val rchId = FormElement(
        id = 1,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "RCH ID No. of Woman",
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12,
        max = 999999999999L,
        min = 0L
    )

    private val name = FormElement(
        id = 2,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Name of Woman",
        arrayId = -1,
        required = false,
        allCaps = true,
        hasSpeechToText = false,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
        isEnabled = false
    )

    private val husbandName = FormElement(
        id = 3,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Name of Husband",
        arrayId = -1,
        required = false,
        allCaps = true,
        hasSpeechToText = false,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
        isEnabled = false
    )

    private val age = FormElement(
        id = 4,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Current Age of Woman",
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForGenBen.toLong(),
        min = Konstants.minAgeForGenBen.toLong(),
    )

    private val ageAtMarriage = FormElement(
        id = 5,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Age of Woman at Marriage",
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForGenBen.toLong(),
        min = Konstants.minAgeForGenBen.toLong(),
        isEnabled = false
    )

    private val womanDetails = FormElement(
        id = 6,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of Woman",
        arrayId = -1,
        required = false
    )

    private val aadharNo = FormElement(
        id = 7,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "Aadhaar Number of Woman",
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12,
        max = 999999999999L,
        min = 100000000000L
    )

    private val bankAccount = FormElement(
        id = 8,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "Bank AC No or Post Office AC No",
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 18,
        max = 999999999999999999L,
        min = 0L
    )

    private val bankName = FormElement(
        id = 9,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "Bank Name",
        arrayId = -1,
        required = false,
        etMaxLength = 50
    )

    private val branchName = FormElement(
        id = 10,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "Branch Name",
        arrayId = -1,
        required = false,
        etMaxLength = 50
    )

    private val ifsc = FormElement(
        id = 11,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "IFSC Code",
        arrayId = -1,
        required = false,
        etMaxLength = 11,
    )

    private val noOfChildren = FormElement(
        id = 12,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "Total No. of Children Born",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
    )

    private val noOfLiveChildren = FormElement(
        id = 13,
        inputType = org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT,
        title = "No. of Live Children",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0
    )

    private val numMale = FormElement(
        id = 14,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Male",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
    )

    private val numFemale = FormElement(
        id = 15,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Female",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
    )

    private val firstChildDetails = FormElement(
        id = 16,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 1st Child",
        arrayId = -1,
        required = false
    )

    private val dob1 = FormElement(
        id = 17,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "1st Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age1 = FormElement(
        id = 18,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "1st Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender1 = FormElement(
        id = 19,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "1st Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val marriageFirstChildGap = FormElement(
        id = 20,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between Date of marriage and 1st child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val secondChildDetails = FormElement(
        id = 21,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 2nd Child",
        arrayId = -1,
        required = false
    )

    private val dob2 = FormElement(
        id = 22,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "2nd Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age2 = FormElement(
        id = 23,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "2nd Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender2 = FormElement(
        id = 24,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "2nd Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val firstAndSecondChildGap = FormElement(
        id = 25,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 1st child and 2nd Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val thirdChildDetails = FormElement(
        id = 26,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 3rd Child",
        arrayId = -1,
        required = false
    )

    private val dob3 = FormElement(
        id = 27,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "3rd Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age3 = FormElement(
        id = 28,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "3rd Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender3 = FormElement(
        id = 29,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "3rd Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val secondAndThirdChildGap = FormElement(
        id = 30,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 2nd child and 3rd Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val fourthChildDetails = FormElement(
        id = 31,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 4th Child",
        arrayId = -1,
        required = false
    )

    private val dob4 = FormElement(
        id = 32,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "4th Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age4 = FormElement(
        id = 33,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "4th Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender4 = FormElement(
        id = 34,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "4th Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val thirdAndFourthChildGap = FormElement(
        id = 35,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 3rd Child and 4th Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val fifthChildDetails = FormElement(
        id = 36,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 5th Child",
        arrayId = -1,
        required = false
    )

    private val dob5 = FormElement(
        id = 37,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "5th Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age5 = FormElement(
        id = 38,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "5th Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender5 = FormElement(
        id = 39,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "5th Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val fourthAndFifthChildGap = FormElement(
        id = 40,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 4th child and 5th Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val sixthChildDetails = FormElement(
        id = 41,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 6th Child",
        arrayId = -1,
        required = false
    )

    private val dob6 = FormElement(
        id = 42,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "6th Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age6 = FormElement(
        id = 43,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "6th Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender6 = FormElement(
        id = 44,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "6th Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val fifthAndSixthChildGap = FormElement(
        id = 45,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 5th child and 6th Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val seventhChildDetails = FormElement(
        id = 46,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 7th Child",
        arrayId = -1,
        required = false
    )

    private val dob7 = FormElement(
        id = 47,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "7th Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age7 = FormElement(
        id = 48,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "7th Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender7 = FormElement(
        id = 49,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "7th Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val sixthAndSeventhChildGap = FormElement(
        id = 50,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 6th child and 7th Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val eighthChildDetails = FormElement(
        id = 51,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 8th Child",
        arrayId = -1,
        required = false
    )

    private val dob8 = FormElement(
        id = 52,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "8th Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age8 = FormElement(
        id = 53,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "8th Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender8 = FormElement(
        id = 54,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "8th Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val seventhAndEighthChildGap = FormElement(
        id = 55,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 7th child and 8th Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val ninthChildDetails = FormElement(
        id = 56,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = "Details of 9th Child",
        arrayId = -1,
        required = false
    )

    private val dob9 = FormElement(
        id = 57,
        inputType = org.piramalswasthya.sakhi.model.InputType.DATE_PICKER,
        title = "9th Child Date of Birth",
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age9 = FormElement(
        id = 58,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "9th Child Age ( in Years)",
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 1,
    )

    private val gender9 = FormElement(
        id = 59,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = "9th Child Sex",
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private var timeAtMarriage: Long = 0L
    private val eighthAndNinthChildGap = FormElement(
        id = 60,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = "Gap between 8th child and 9th Child",
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private var maleChild = 0

    private var femaleChild = 0

    suspend fun setUpPage(ben: BenRegCache?, saved: EligibleCoupleRegCache?) {
        val list = mutableListOf(
            dateOfReg,
            rchId,
//            name,
            husbandName,
//            age,
            ageAtMarriage,
            womanDetails,
            aadharNo,
            bankAccount,
            bankName,
            branchName,
            ifsc,
            noOfChildren,
            noOfLiveChildren,
            numMale,
            numFemale
        )
        dateOfReg.value = getDateFromLong(System.currentTimeMillis())
//        dateOfReg.value?.let {
//            val long = Dataset.getLongFromDate(it)
//            dateOfhivTestDone.min = long
//        }

        ben?.let {
            dateOfReg.min = it.regDate
            rchId.value = ben.rchId
            aadharNo.value = ben.aadharNum
            name.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            husbandName.value = ben.genDetails?.spouseName
            age.value = BenBasicCache.getAgeFromDob(ben.dob).toString()
            ben.genDetails?.ageAtMarriage?.let { it1 ->
                ageAtMarriage.value = it1.toString()
                val cal = Calendar.getInstance()
                cal.timeInMillis = ben.dob
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + it1)
                dob1.min = cal.timeInMillis
                timeAtMarriage = cal.timeInMillis
            }
        }
        saved?.let { ecCache ->
            dateOfReg.value = getDateFromLong(ecCache.dateOfReg)
            bankAccount.value = ecCache.bankAccount?.toString()
            bankName.value = ecCache.bankName
            branchName.value = ecCache.branchName
            ifsc.value = ecCache.ifsc
            noOfChildren.value = ecCache.noOfChildren.toString()
            noOfLiveChildren.value = ecCache.noOfLiveChildren.toString()
            numMale.value = ecCache.noOfMaleChildren.toString()
            numFemale.value = ecCache.noOfFemaleChildren.toString()
            if (ecCache.noOfLiveChildren > 0) {
                ecCache.dob1?.let {
                    dob1.value = getDateFromLong(it)
                    age1.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"

                }
                ecCache.gender1?.let {
                    gender1.value =
                        if (it == Gender.MALE) gender1.entries!![0] else gender1.entries!![1]
                }
                marriageFirstChildGap.value = ecCache.marriageFirstChildGap?.toString()

                list.addAll(
                    list.indexOf(noOfLiveChildren) + 1,
                    listOf(firstChildDetails, dob1, age1, gender1, marriageFirstChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 1) {
                ecCache.dob2?.let {
                    dob2.value = getDateFromLong(it)
                    age2.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"

                }
                ecCache.gender2?.let {
                    gender2.value =
                        if (it == Gender.MALE) gender2.entries!![0] else gender2.entries!![1]
                }

                firstAndSecondChildGap.value = ecCache.firstAndSecondChildGap?.toString()

                list.addAll(
                    list.indexOf(marriageFirstChildGap) + 1,
                    listOf(secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 2) {
                ecCache.dob3?.let {
                    dob3.value = getDateFromLong(it)
                    age3.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"
                }
                ecCache.gender3?.let {
                    gender3.value =
                        if (it == Gender.MALE) gender3.entries!![0] else gender3.entries!![1]
                }

                secondAndThirdChildGap.value = ecCache.secondAndThirdChildGap?.toString()

                list.addAll(
                    list.indexOf(firstAndSecondChildGap) + 1,
                    listOf(thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 3) {
                ecCache.dob4?.let {
                    dob4.value = getDateFromLong(it)
                    age4.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"
                }
                ecCache.gender4?.let {
                    gender4.value =
                        if (it == Gender.MALE) gender4.entries!![0] else gender4.entries!![1]
                }

                thirdAndFourthChildGap.value = ecCache.thirdAndFourthChildGap?.toString()

                list.addAll(
                    list.indexOf(secondAndThirdChildGap) + 1,
                    listOf(fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 4) {
                ecCache.dob5?.let {
                    dob5.value = getDateFromLong(it)
                    age5.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"
                }
                ecCache.gender5?.let {
                    gender5.value =
                        if (it == Gender.MALE) gender5.entries!![0] else gender5.entries!![1]
                }

                fourthAndFifthChildGap.value = ecCache.fourthAndFifthChildGap?.toString()

                list.addAll(
                    list.indexOf(thirdAndFourthChildGap) + 1,
                    listOf(fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 5) {
                ecCache.dob6?.let {
                    dob6.value = getDateFromLong(it)
                    age6.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"
                }
                ecCache.gender6?.let {
                    gender6.value =
                        if (it == Gender.MALE) gender6.entries!![0] else gender6.entries!![1]
                }

                fifthAndSixthChildGap.value = ecCache.fifthANdSixthChildGap?.toString()

                list.addAll(
                    list.indexOf(fourthAndFifthChildGap) + 1,
                    listOf(sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 6) {
                ecCache.dob7?.let {
                    dob7.value = getDateFromLong(it)
                    age7.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"
                }
                ecCache.gender7?.let {
                    gender7.value =
                        if (it == Gender.MALE) gender7.entries!![0] else gender7.entries!![1]
                }

                sixthAndSeventhChildGap.value = ecCache.sixthAndSeventhChildGap?.toString()

                list.addAll(
                    list.indexOf(fifthAndSixthChildGap) + 1,
                    listOf(seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 7) {
                ecCache.dob8?.let {
                    dob8.value = getDateFromLong(it)
                    age8.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"
                }
                ecCache.gender8?.let {
                    gender8.value =
                        if (it == Gender.MALE) gender8.entries!![0] else gender8.entries!![1]
                }

                seventhAndEighthChildGap.value = ecCache.seventhAndEighthChildGap?.toString()

                list.addAll(
                    list.indexOf(sixthAndSeventhChildGap) + 1,
                    listOf(eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap)
                )
            }
            if (ecCache.noOfLiveChildren > 8) {
                ecCache.dob9?.let {
                    dob9.value = getDateFromLong(it)
                    age9.value = if (BenBasicCache.getAgeUnitFromDob(it)
                        == AgeUnit.YEARS
                    ) {
                        BenBasicCache.getAgeFromDob(it).toString()
                    } else "0"
                }
                ecCache.gender9?.let {
                    gender9.value =
                        if (it == Gender.MALE) gender9.entries!![0] else gender9.entries!![1]
                }

                eighthAndNinthChildGap.value = ecCache.eighthAndNinthChildGap?.toString()
                list.addAll(
                    list.indexOf(seventhAndEighthChildGap) + 1,
                    listOf(ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap)
                )
            }
        }
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            rchId.id -> {
                validateRchIdOnEditText(rchId)
            }

            aadharNo.id -> {
                validateIntMinMax(aadharNo)
            }

            bankAccount.id -> {
                validateAllZerosOnEditText(bankAccount)
            }

            bankName.id -> {
                validateAllAlphaNumericSpaceOnEditText(bankName)
            }

            branchName.id -> {
                validateAllAlphabetsSpaceOnEditText(branchName)
            }

            ifsc.id -> {
                validateAllAlphaNumericSpaceOnEditText(ifsc)
            }

            ageAtMarriage.id -> {
                if (ageAtMarriage.value.isNullOrEmpty()) {
                    validateEmptyOnEditText(ageAtMarriage)
                    return -1
                }
                ageAtMarriage.min = 14
                ageAtMarriage.max = age.value?.toLong()
                validateIntMinMax(ageAtMarriage)
                -1
            }

            noOfChildren.id -> {
                if (noOfChildren.value.isNullOrEmpty() ||
                    noOfChildren.value?.takeIf { it.isNotEmpty() }?.toInt() == 0
                ) {
                    noOfLiveChildren.value = "0"
//                    noOfLiveChildren.isEnabled = false
                    numMale.value = "0"
                    numFemale.value = "0"
                } else {
                    noOfLiveChildren.max = noOfChildren.value?.takeIf { it.isNotEmpty() }?.toLong()
                    validateIntMinMax(noOfLiveChildren)
                    validateIntMinMax(noOfChildren)
                    if (noOfLiveChildren.value == "0") noOfLiveChildren.value = null
//                    noOfLiveChildren.isEnabled = true
                    numMale.value = null
                    numFemale.value = null
                }
                handleListOnValueChanged(noOfLiveChildren.id, 0)
            }

            dob1.id -> {
                if (dob1.value != null && timeAtMarriage != 0L) {
                    val dob1Long = getLongFromDate(dob1.value)
                    assignValuesToAgeFromDob(dob1Long, age1)
                    setSiblingAgeDiff(timeAtMarriage, dob1Long, marriageFirstChildGap)
                    dob2.min = dob1Long
                }
                -1
            }

            dob2.id -> {
                if (dob1.value != null && dob2.value != null) {
                    val dob2Long = getLongFromDate(dob2.value)
                    val dob1Long = getLongFromDate(dob1.value)
                    assignValuesToAgeFromDob(dob2Long, age2)
                    setSiblingAgeDiff(dob1Long, dob2Long, firstAndSecondChildGap)
                    dob3.min = dob2Long
                }
                -1
            }

            dob3.id -> {
                if (dob2.value != null && dob3.value != null) {
                    val dob2Long = getLongFromDate(dob2.value)
                    val dob3Long = getLongFromDate(dob3.value)
                    assignValuesToAgeFromDob(dob3Long, age3)
                    setSiblingAgeDiff(dob2Long, dob3Long, secondAndThirdChildGap)
                    dob4.min = dob3Long
                }
                -1
            }

            dob4.id -> {
                if (dob3.value != null && dob4.value != null) {
                    val dob3Long = getLongFromDate(dob3.value)
                    val dob4Long = getLongFromDate(dob4.value)
                    assignValuesToAgeFromDob(dob4Long, age4)
                    setSiblingAgeDiff(dob3Long, dob4Long, thirdAndFourthChildGap)
                    dob5.min = dob4Long
                }
                -1
            }

            dob5.id -> {
                if (dob4.value != null && dob5.value != null) {
                    val dob4Long = getLongFromDate(dob4.value)
                    val dob5Long = getLongFromDate(dob5.value)
                    assignValuesToAgeFromDob(dob5Long, age5)
                    setSiblingAgeDiff(dob4Long, dob5Long, fourthAndFifthChildGap)
                    dob6.min = dob5Long
                }
                -1
            }

            dob6.id -> {
                if (dob5.value != null && dob6.value != null) {
                    val dob5Long = getLongFromDate(dob5.value)
                    val dob6Long = getLongFromDate(dob6.value)
                    assignValuesToAgeFromDob(dob6Long, age6)
                    setSiblingAgeDiff(dob5Long, dob6Long, fifthAndSixthChildGap)
                    dob7.min = dob6Long
                }
                -1
            }

            dob7.id -> {
                if (dob6.value != null && dob7.value != null) {
                    val dob6Long = getLongFromDate(dob6.value)
                    val dob7Long = getLongFromDate(dob7.value)
                    assignValuesToAgeFromDob(dob7Long, age7)
                    setSiblingAgeDiff(dob6Long, dob7Long, sixthAndSeventhChildGap)
                    dob8.min = dob7Long
                }
                -1
            }

            dob8.id -> {
                if (dob7.value != null && dob8.value != null) {
                    val dob7Long = getLongFromDate(dob7.value)
                    val dob8Long = getLongFromDate(dob8.value)
                    assignValuesToAgeFromDob(dob8Long, age8)
                    setSiblingAgeDiff(dob7Long, dob8Long, seventhAndEighthChildGap)
                    dob9.min = dob8Long
                }
                -1
            }

            dob9.id -> {
                if (dob8.value != null && dob9.value != null) {
                    val dob8Long = getLongFromDate(dob8.value)
                    val dob9Long = getLongFromDate(dob9.value)
                    assignValuesToAgeFromDob(dob9Long, age9)
                    setSiblingAgeDiff(dob8Long, dob9Long, eighthAndNinthChildGap)
                }
                -1
            }

            noOfLiveChildren.id -> {
                noOfChildren.min = noOfLiveChildren.value.takeIf { !it.isNullOrEmpty() }?.toLong()
                validateIntMinMax(noOfLiveChildren)
                validateIntMinMax(noOfChildren)
                if (noOfLiveChildren.value.isNullOrEmpty()) {
                    triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )
                }
                when (noOfLiveChildren.value.takeIf { !it.isNullOrEmpty() }?.toInt() ?: 0) {
                    0 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    1 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails,
                            dob1,
                            age1,
                            gender1,
                            marriageFirstChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    2 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    3 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    4 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    5 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    6 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    7 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    8 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    9 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    else -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails,
                            dob1,
                            age1,
                            gender1,
                            marriageFirstChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )
                }
                return 1
            }

            gender1.id, gender2.id, gender3.id, gender4.id, gender5.id,
            gender6.id, gender7.id, gender8.id, gender9.id -> {
                maleChild = 0
                femaleChild = 0

                if (gender1.value == "Male") {
                    maleChild += 1
                } else if (gender1.value == "Female") {
                    femaleChild += 1
                }

                if (gender2.value == "Male") {
                    maleChild += 1
                } else if (gender2.value == "Female") {
                    femaleChild += 1
                }

                if (gender3.value == "Male") {
                    maleChild += 1
                } else if (gender3.value == "Female") {
                    femaleChild += 1
                }

                if (gender4.value == "Male") {
                    maleChild += 1
                } else if (gender4.value == "Female") {
                    femaleChild += 1
                }

                if (gender5.value == "Male") {
                    maleChild += 1
                } else if (gender5.value == "Female") {
                    femaleChild += 1
                }

                if (gender6.value == "Male") {
                    maleChild += 1
                } else if (gender6.value == "Female") {
                    femaleChild += 1
                }

                if (gender7.value == "Male") {
                    maleChild += 1
                } else if (gender7.value == "Female") {
                    femaleChild += 1
                }

                if (gender8.value == "Male") {
                    maleChild += 1
                } else if (gender8.value == "Female") {
                    femaleChild += 1
                }

                if (gender9.value == "Male") {
                    maleChild += 1
                } else if (gender9.value == "Female") {
                    femaleChild += 1
                }

                numFemale.value = femaleChild.toString()
                numMale.value = maleChild.toString()
                -1
            }

            else -> -1
        }
    }

//    private fun setMarriageAndFirstChildGap() {
//        marriageFirstChildGap.value =
//            (age1.value?.let { age1 ->
//                (ageAtMarriage.value?.toInt()?.let { age.value?.toInt()?.minus(it) })?.minus(
//                    age1.toInt()
//                )
//            }).toString()
//    }

    private fun setSiblingAgeDiff(old: Long, new: Long, target: FormElement) {
        val calOld = Calendar.getInstance().setToStartOfTheDay().apply {
            timeInMillis = old
        }
        val calNew = Calendar.getInstance().setToStartOfTheDay().apply {
            timeInMillis = new
        }
        val diff = getDiffYears(calOld, calNew)
        target.value = diff.toString()
    }


//    private fun setFirstChildAndSecondChildGap() {
//        firstAndSecondChildGap.value =
//            (age2.value?.toInt()?.let { age1.value?.toInt()?.minus(it) }).toString()
//    }
//
//    private fun setSecondAndThirdChildGap() {
//        secondAndThirdChildGap.value =
//            (age3.value?.toInt()?.let { age2.value?.toInt()?.minus(it) }).toString()
//    }
//
//    private fun setThirdAndFourthChildGap() {
//        thirdAndFourthChildGap.value =
//            (age4.value?.toInt()?.let { age3.value?.toInt()?.minus(it) }).toString()
//    }
//
//    private fun setFourthAndFifthChildGap() {
//        fourthAndFifthChildGap.value =
//            (age5.value?.toInt()?.let { age4.value?.toInt()?.minus(it) }).toString()
//    }
//
//    private fun setFifthAndSixthChildGap() {
//        fifthAndSixthChildGap.value =
//            (age6.value?.toInt()?.let { age5.value?.toInt()?.minus(it) }).toString()
//    }
//
//    private fun setSixthAndSeventhChildGap() {
//        sixthAndSeventhChildGap.value =
//            (age7.value?.toInt()?.let { age6.value?.toInt()?.minus(it) }).toString()
//    }
//
//    private fun setSeventhAndEighthChildGap() {
//        seventhAndEighthChildGap.value =
//            (age8.value?.toInt()?.let { age7.value?.toInt()?.minus(it) }).toString()
//    }
//
//    private fun setEighthAndNinthChildGap() {
//        eighthAndNinthChildGap.value =
//            (age9.value?.toInt()?.let { age8.value?.toInt()?.minus(it) }).toString()
//    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EligibleCoupleRegCache).let { ecr ->
            ecr.dateOfReg = getLongFromDate(dateOfReg.value!!)
            ecr.bankAccount = bankAccount.value?.toLong()
            ecr.bankName = bankName.value
            ecr.branchName = branchName.value
            ecr.ifsc = ifsc.value
            ecr.noOfChildren = noOfChildren.value?.toInt() ?: 0
            ecr.noOfLiveChildren = noOfLiveChildren.value?.toInt() ?: 0
            ecr.noOfMaleChildren = numMale.value?.toInt() ?: 0
            ecr.noOfFemaleChildren = numFemale.value?.toInt() ?: 0
            ecr.dob1 = getLongFromDate(dob1.value)
            ecr.age1 = age1.value?.toInt()
            ecr.gender1 = when (gender1.value) {
                gender1.entries!![0] -> Gender.MALE
                gender1.entries!![1] -> Gender.FEMALE
                else -> null
            }
            ecr.marriageFirstChildGap = marriageFirstChildGap.value?.toInt()
            if (noOfLiveChildren.value?.toInt()!! > 1) {
                ecr.dob2 = getLongFromDate(dob2.value)
                ecr.age2 = age2.value?.toInt()
                ecr.gender2 = when (gender2.value) {
                    gender2.entries!![0] -> Gender.MALE
                    gender2.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.firstAndSecondChildGap = firstAndSecondChildGap.value?.toInt()
            }
            if (noOfLiveChildren.value?.toInt()!! > 2) {
                ecr.dob3 = getLongFromDate(dob3.value)
                ecr.age3 = age3.value?.toInt()
                ecr.gender3 = when (gender3.value) {
                    gender3.entries!![0] -> Gender.MALE
                    gender3.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.secondAndThirdChildGap = secondAndThirdChildGap.value?.toInt()
            }
            if (noOfLiveChildren.value?.toInt()!! > 3) {
                ecr.dob4 = getLongFromDate(dob4.value)
                ecr.age4 = age4.value?.toInt()
                ecr.gender4 = when (gender4.value) {
                    gender4.entries!![0] -> Gender.MALE
                    gender4.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.thirdAndFourthChildGap = thirdAndFourthChildGap.value?.toInt()
            }
            if (noOfLiveChildren.value?.toInt()!! > 4) {
                ecr.dob5 = getLongFromDate(dob5.value)
                ecr.age5 = age5.value?.toInt()
                ecr.gender5 = when (gender5.value) {
                    gender5.entries!![0] -> Gender.MALE
                    gender5.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.fourthAndFifthChildGap = fourthAndFifthChildGap.value?.toInt()
            }
            if (noOfLiveChildren.value?.toInt()!! > 5) {
                ecr.dob6 = getLongFromDate(dob6.value)
                ecr.age6 = age6.value?.toInt()
                ecr.gender6 = when (gender6.value) {
                    gender6.entries!![0] -> Gender.MALE
                    gender6.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.fifthANdSixthChildGap = fifthAndSixthChildGap.value?.toInt()
            }
            if (noOfLiveChildren.value?.toInt()!! > 6) {
                ecr.dob7 = getLongFromDate(dob7.value)
                ecr.age7 = age7.value?.toInt()
                ecr.gender7 = when (gender7.value) {
                    gender7.entries!![0] -> Gender.MALE
                    gender7.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.sixthAndSeventhChildGap = sixthAndSeventhChildGap.value?.toInt()
            }
            if (noOfLiveChildren.value?.toInt()!! > 7) {
                ecr.dob8 = getLongFromDate(dob8.value)
                ecr.age8 = age8.value?.toInt()
                ecr.gender8 = when (gender8.value) {
                    gender8.entries!![0] -> Gender.MALE
                    gender8.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.seventhAndEighthChildGap = seventhAndEighthChildGap.value?.toInt()
            }
            if (noOfLiveChildren.value?.toInt()!! > 8) {
                ecr.dob9 = getLongFromDate(dob9.value)
                ecr.age9 = age9.value?.toInt()
                ecr.gender9 = when (gender9.value) {
                    gender9.entries!![0] -> Gender.MALE
                    gender9.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.eighthAndNinthChildGap = eighthAndNinthChildGap.value?.toInt()
            }
        }
    }

    fun mapValueToBen(ben: BenRegCache?): Boolean {
        var isUpdated = false;
        val rchIdFromBen = ben?.rchId?.takeIf { it.isNotEmpty() }?.toLong()
        val aadharNoFromBen = ben?.aadharNum?.takeIf { it.isNotEmpty() }?.toLong()
        rchId.value?.takeIf {
            it.isNotEmpty()
        }?.toLong()?.let {
            if (it != rchIdFromBen) {
                ben?.rchId = it.toString()
                isUpdated = true
            }
        }
        aadharNo.value?.takeIf {
            it.isNotEmpty()
        }?.toLong()?.let {
            if (it != aadharNoFromBen) {
                ben?.aadharNum = it.toString()
                isUpdated = true
            }
        }
        if (isUpdated) {
            if(ben?.processed!="N")
                ben?.processed = "U"
            ben?.syncState = SyncState.UNSYNCED
        }
        return isUpdated
    }

    fun getIndexOfChildren() = getIndexById(noOfChildren.id)

    fun getIndexOfLiveChildren() = getIndexById(noOfLiveChildren.id)
    fun getIndexOfMaleChildren() = getIndexById(numMale.id)
    fun getIndexOfFeMaleChildren() = getIndexById(numFemale.id)
    fun getIndexOfAge1() = getIndexById(age1.id)
    fun getIndexOfGap1() = getIndexById(marriageFirstChildGap.id)
    fun getIndexOfAge2() = getIndexById(age2.id)
    fun getIndexOfGap2() = getIndexById(firstAndSecondChildGap.id)
    fun getIndexOfAge3() = getIndexById(age3.id)
    fun getIndexOfGap3() = getIndexById(secondAndThirdChildGap.id)
    fun getIndexOfAge4() = getIndexById(age4.id)
    fun getIndexOfGap4() = getIndexById(thirdAndFourthChildGap.id)
    fun getIndexOfAge5() = getIndexById(age5.id)
    fun getIndexOfGap5() = getIndexById(fourthAndFifthChildGap.id)

    fun getIndexOfAge6() = getIndexById(age6.id)
    fun getIndexOfGap6() = getIndexById(fifthAndSixthChildGap.id)

    fun getIndexOfAge7() = getIndexById(age7.id)
    fun getIndexOfGap7() = getIndexById(sixthAndSeventhChildGap.id)

    fun getIndexOfAge8() = getIndexById(age8.id)
    fun getIndexOfGap8() = getIndexById(seventhAndEighthChildGap.id)

    fun getIndexOfAge9() = getIndexById(age9.id)
    fun getIndexOfGap9() = getIndexById(eighthAndNinthChildGap.id)
}
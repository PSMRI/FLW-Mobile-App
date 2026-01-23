package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.Gender.FEMALE
import org.piramalswasthya.sakhi.model.Gender.MALE
import org.piramalswasthya.sakhi.model.Gender.TRANSGENDER
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.HEADLINE
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.NewBenRegViewModel.Companion.isOtpVerified
import org.piramalswasthya.sakhi.utils.HelperUtil.getDiffYears
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



class NewChildBenRegDataset(context: Context, language: Languages) : Dataset(context, language) {


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

        private fun getMinLmpMillis(): Long {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1 * 400) //before it is 280
            return cal.timeInMillis
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

        fun getMinimumSecondChildDob(firstChildDobStr: String?): String {

            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val firstChildDob = dateFormat.parse(firstChildDobStr)

            val calendar = Calendar.getInstance()
            calendar.time = firstChildDob!!

            return dateFormat.format(calendar.time)
        }
    }

    private var isExistingRecord = false
    private var selectedBen: BenRegCache? = null
    private val rchId = 0L
    private val dateOfReg = FormElement(
        id = 0,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_date_of_reg),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = 0L,
        hasDependants = true,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_anc_date,
        showDrawable = true
    )

    private val elderChildrenCount = FormElement(
        id = 1,
        inputType = org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW,
        title = resources.getString(R.string.no_of_elderly_children),
        arrayId = -1,
        required = true,
        hasDependants = true,

    )


    private val ageRestrictionLabel = FormElement(
        id = 69,
        inputType = org.piramalswasthya.sakhi.model.InputType.CHECKBOXES,
        title = resources.getString(R.string.ecrdset_reg_children_15_below),
        arrayId = -1,
        required = false,
        headingLine = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
    )

     val noOfChildren = FormElement(
        id = 12,
        inputType = org.piramalswasthya.sakhi.model.InputType.NUMBER_PICKER,
        title = resources.getString(R.string.ecrdset_no_live_child),
        required = false,
         hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,

    )


    private val numMale = FormElement(
        id = 14,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_male),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_male,
        showDrawable = true
    )

    private val numFemale = FormElement(
        id = 15,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_female),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_female,
        showDrawable = true

    )

    private val ageAtMarriage = FormElement(
        id = 5,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_cur_ag_of_wo_marr),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForGenBen.toLong(),
        min = Konstants.minAgeForGenBen.toLong(),
        isEnabled = false,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_age_of_women_at_marriage,
        showDrawable = true
    )

    private val firstChildDetails = FormElement(
        id = 16,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dls_1_child),
        arrayId = -1,
        required = false,

        )

    private val firstChildName = FormElement(
        id = 111,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val dob1 = FormElement(
        id = 17,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_1_child_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_anc_date,
        showDrawable = true
    )

    private val age1 = FormElement(
        id = 18,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_1_child_age_in_yrs),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_no_of_live_child,
        showDrawable = true
    )

    private val gender1 = FormElement(
        id = 19,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_1_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val marriageFirstChildGap = FormElement(
        id = 20,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_1_child_marr),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_gap_bet_marriage_child,
        showDrawable = true
    )

    private val secondChildDetails = FormElement(
        id = 21,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_2_child),
        arrayId = -1,
        required = false
    )
    private val secondChildName = FormElement(
        id = 112,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dob2 = FormElement(
        id = 22,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_2_child_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age2 = FormElement(
        id = 23,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_2_child_age_yrs),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender2 = FormElement(
        id = 24,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_2_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val firstAndSecondChildGap = FormElement(
        id = 25,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_1_child_2_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val thirdChildDetails = FormElement(
        id = 26,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_3_child),
        arrayId = -1,
        required = false
    )

    private val thirdChildName = FormElement(
        id = 113,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dob3 = FormElement(
        id = 27,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_3_child_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age3 = FormElement(
        id = 28,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_3_child_age_yrs),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender3 = FormElement(
        id = 29,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_3_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val secondAndThirdChildGap = FormElement(
        id = 30,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_bet_2_3_child_sex),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val fourthChildDetails = FormElement(
        id = 31,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_4_child),
        arrayId = -1,
        required = false
    )

    private val forthChildName = FormElement(
        id = 114,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dob4 = FormElement(
        id = 32,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_4_child_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age4 = FormElement(
        id = 33,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_4_child_age_yrs),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender4 = FormElement(
        id = 34,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_4_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val thirdAndFourthChildGap = FormElement(
        id = 35,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_bet_3_4_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val fifthChildDetails = FormElement(
        id = 36,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_5_child),
        arrayId = -1,
        required = false
    )

    private val fifthChildName = FormElement(
        id = 115,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dob5 = FormElement(
        id = 37,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_5_child_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age5 = FormElement(
        id = 38,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_5_child_age_yrs),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender5 = FormElement(
        id = 39,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_5_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val fourthAndFifthChildGap = FormElement(
        id = 40,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_4_5_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val sixthChildDetails = FormElement(
        id = 41,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_6_child),
        arrayId = -1,
        required = false
    )

    private val sixthChildName = FormElement(
        id = 116,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dob6 = FormElement(
        id = 42,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_dts_6_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age6 = FormElement(
        id = 43,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_6_child_age),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender6 = FormElement(
        id = 44,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_6_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val fifthAndSixthChildGap = FormElement(
        id = 45,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_5_6_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val seventhChildDetails = FormElement(
        id = 46,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_7_dts_child),
        arrayId = -1,
        required = false
    )

    private val seventhChildName = FormElement(
        id = 117,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val dob7 = FormElement(
        id = 47,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_7_child_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age7 = FormElement(
        id = 48,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_7_child_age_yrs),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender7 = FormElement(
        id = 49,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_7_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val sixthAndSeventhChildGap = FormElement(
        id = 50,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_6_7_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val eighthChildDetails = FormElement(
        id = 51,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_8_child),
        arrayId = -1,
        required = false
    )

    private val eightChildName = FormElement(
        id = 118,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dob8 = FormElement(
        id = 52,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_8_child_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age8 = FormElement(
        id = 53,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_8_child_age),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender8 = FormElement(
        id = 54,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_8_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val seventhAndEighthChildGap = FormElement(
        id = 55,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_7_8_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val ninthChildDetails = FormElement(
        id = 56,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_9_child),
        arrayId = -1,
        required = false
    )

    private val ninthChildName = FormElement(
        id = 119,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.nbr_child_first_name),
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )
    private val dob9 = FormElement(
        id = 57,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_9_bth),
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age9 = FormElement(
        id = 58,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_9_age_yrs),
        arrayId = -1,
        required = true,
        hasDependants = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender9 = FormElement(
        id = 59,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_9_child_sex),
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private var maleChild = 0

    private var femaleChild = 0

    private var dateOfBirth = 0L

    private var lastDeliveryDate = 0L
    private var timeAtMarriage: Long = 0L
    private val eighthAndNinthChildGap = FormElement(
        id = 60,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_8_9_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    suspend fun setUpPage(
        ben: EligibleCoupleRegCache?,
        household: HouseholdCache,
        hoF: BenRegCache?,
        benGender: Gender,
        relationToHeadId: Int,
        hoFSpouse: List<BenRegCache> = emptyList(),
        selectedben: BenRegCache?,
        isAddspouse: Int,
        childList: List<BenRegCache>,
        elderChildCount: Int

        ) {
        val list = mutableListOf(
            dateOfReg,
            elderChildrenCount,
            ageRestrictionLabel,
            noOfChildren,



        )

        isExistingRecord = ben != null

        selectedBen = selectedben
        selectedben?.let {
            dateOfReg.min = it.regDate
            selectedben.genDetails?.ageAtMarriage?.let { it1 ->
                ageAtMarriage.value = it1.toString()
                val cal = Calendar.getInstance()
                cal.timeInMillis = selectedben.dob
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + it1)
                timeAtMarriage = cal.timeInMillis
            }
        }



           /* if (ecCache.dateOfReg == 0L) {
                ecCache.dateOfReg = System.currentTimeMillis()
            }*/

            dateOfReg.value = getDateFromLong(System.currentTimeMillis())

            noOfChildren.value = childList.size.toString()

            elderChildrenCount.value = elderChildCount.coerceAtMost(5).toString()


        var insertIndex = list.indexOf(noOfChildren) + 1

        childList.forEachIndexed { index, child ->

            val bundle = children[index]

            bundle.name.value = child.firstName
            bundle.dob.value = getDateFromLong(child.dob)
            bundle.age.value = child.age?.toString()

            bundle.gender.value = getLocalValueInArray(
                R.array.ecr_gender_array,
                child.gender?.name
                    ?.lowercase()
                    ?.replaceFirstChar { it.uppercase() }
            )

            if (index == 0) {
                setSiblingAgeDiff(timeAtMarriage, child.dob, bundle.gap)
            } else {
                setSiblingAgeDiff(
                    childList[index - 1].dob,
                    child.dob,
                    bundle.gap
                )
            }

            val childViews = listOf(
                bundle.name,
                bundle.dob,
                bundle.age,
                bundle.gender,
                bundle.gap
            )

            list.addAll(insertIndex, childViews)

            insertIndex += childViews.size
        }

        setUpPage(list)

    }


    private suspend fun updateTimeLessThan18() {
        val dobStrings = listOf(
           dob1.value, dob2.value, dob3.value, dob4.value, dob5.value,
            dob6.value, dob7.value, dob8.value, dob9.value
        ).filter { !it.isNullOrBlank() }

         if (dobStrings.isNotEmpty()) {
            val dobLongs = dobStrings.map { getLongFromDate(it!!) }
             lastDeliveryDate = dobLongs.maxOrNull()!!
            }

    }

    private fun setSiblingAgeDiff(old: Long, new: Long, target: FormElement) {
        val calOld = Calendar.getInstance().setToStartOfTheDay().apply {
            timeInMillis = old
        }
        val calNew = Calendar.getInstance().setToStartOfTheDay().apply {
            timeInMillis = new
        }
        val diff = getDiffYears(calOld, calNew)
        target.value = "${diff.toString()} years"
    }

    private fun getRelationStringFromId(familyHeadRelationId: Int): String {
        return if (familyHeadRelationId == 9)
            "Son"
        else
            "Daughter"
    }

    private fun getFamilyHeadRelationFromMother(childGender: Gender): Int {
        return if (childGender == Gender.MALE)
            9
        else
            10
    }
    fun mapChild(
        cacheModel: BenRegCache,
        childIndex: Int
    ): BenRegCache {

        val ben = cacheModel.copy()
        val childGender = getChildGender(childIndex)
        val familyHeadRelationId =
            getFamilyHeadRelationFromMother(childGender!!)
        val familyHeadRelation = getRelationStringFromId(familyHeadRelationId)
        when(childIndex) {

            1 -> {
                ben.firstName = firstChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob1.value!!)
                ben.age = age1.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender1.value) {
                    gender1.entries!![0] -> 1
                    gender1.entries!![1] -> 2
                    gender1.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }
            2 -> {
                ben.firstName = secondChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob2.value!!)
                ben.age = age2.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender2.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }

            3 -> {
                ben.firstName = thirdChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob3.value!!)
                ben.age = age3.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender3.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }

            4 -> {
                ben.firstName = forthChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob4.value!!)
                ben.age = age4.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender4.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }
            5 -> {
                ben.firstName = fifthChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob5.value!!)
                ben.age = age5.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender5.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }
            6 -> {
                ben.firstName = sixthChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob6.value!!)
                ben.age = age6.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender6.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }
            7 -> {
                ben.firstName = seventhChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob7.value!!)
                ben.age = age7.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender7.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }
            8 -> {
                ben.firstName = eightChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob8.value!!)
                ben.age = age8.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender8.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }
            9 -> {
                ben.firstName = ninthChildName.value
                ben.lastName = selectedBen?.lastName
                ben.dob = Dataset.getLongFromDate(dob9.value!!)
                ben.age = age9.value!!.toInt()
                ben.ageUnitId = 3
                ben.ageUnit = AgeUnit.YEARS
                ben.isAdult = ben.ageUnit == AgeUnit.YEARS && ben.age >= 15
                ben.isKid = !ben.isAdult
                ben.genderId = when (gender9.value) {
                    gender2.entries!![0] -> 1
                    gender2.entries!![1] -> 2
                    gender2.entries!![2] -> 3
                    else -> 0
                }
                ben.gender = when (ben.genderId) {
                    1 -> MALE
                    2 -> FEMALE
                    3 -> TRANSGENDER
                    else -> null
                }
                ben.familyHeadRelation = familyHeadRelation
                ben.familyHeadRelationPosition = familyHeadRelationId
            }
        }

        ben.householdId = selectedBen?.householdId!!
        ben.regDate = Dataset.Companion.getLongFromDate(dateOfReg.value!!)
        ben.fatherName = "${selectedBen?.genDetails?.spouseName}"
        ben.motherName = "${selectedBen?.firstName}"
        ben.isDeath = false
        ben.isDeathValue = "false"
        ben.dateOfDeath = null
        ben.timeOfDeath = null
        ben.reasonOfDeath = null
        ben.doYouHavechildren = false
        ben.placeOfDeath = null
        ben.mobileNoOfRelationId = 5
        ben.otherPlaceOfDeath = null
        ben.contactNumber = selectedBen!!.contactNumber
        ben.mobileNoOfRelationId = 5
        ben.isDraft = false
        ben.isConsent = isOtpVerified
        ben.isSpouseAdded = false
        ben.isChildrenAdded = false
        ben.isMarried = false
        ben.doYouHavechildren = false
        ben.community = selectedBen!!.community
        ben.communityId = selectedBen!!.communityId

        return ben
    }

    private fun mapGender(genderField: FormElement): Gender? {
        return when (genderField.value) {
            genderField.entries?.getOrNull(0) -> MALE
            genderField.entries?.getOrNull(1) -> FEMALE
            else -> null
        }
    }

    private fun getChildGender(childIndex: Int): Gender? {
        return when (childIndex) {
            1 -> mapGender(gender1)
            2 -> mapGender(gender2)
            3 -> mapGender(gender3)
            4 -> mapGender(gender4)
            5 -> mapGender(gender5)
            6 -> mapGender(gender6)
            7 -> mapGender(gender7)
            8 -> mapGender(gender8)
            9 -> mapGender(gender9)
            else -> null
        }
    }
    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EligibleCoupleRegCache).let { ecr ->
            ecr.dateOfReg =
                getLongFromDate(dateOfReg.value!!)

            ecr.noOfChildren = noOfChildren.value?.takeIf { it.isNotBlank() }?.toInt() ?: 0
            ecr.noOfMaleChildren = numMale.value?.takeIf { it.isNotBlank() }?.toInt() ?: 0
            ecr.noOfFemaleChildren = numFemale.value?.takeIf { it.isNotBlank() }?.toInt() ?: 0
            ecr.dob1 = getLongFromDate(dob1.value)
            ecr.age1 = age1.value?.toInt()
            ecr.gender1 = when (gender1.value) {
                gender1.entries!![0] -> Gender.MALE
                gender1.entries!![1] -> Gender.FEMALE
                else -> null
            }
            ecr.marriageFirstChildGap =marriageFirstChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            if ((noOfChildren.value?.toIntOrNull() ?: 0) > 1){
                ecr.dob2 = getLongFromDate(dob2.value)
                ecr.age2 = age2.value?.toInt()
                ecr.gender2 = when (gender2.value) {
                    gender2.entries!![0] -> Gender.MALE
                    gender2.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.firstAndSecondChildGap =firstAndSecondChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfChildren.value?.toInt()!! > 2) {
                ecr.dob3 = getLongFromDate(dob3.value)
                ecr.age3 = age3.value?.toInt()
                ecr.gender3 = when (gender3.value) {
                    gender3.entries!![0] -> Gender.MALE
                    gender3.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.secondAndThirdChildGap =secondAndThirdChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfChildren.value?.toInt()!! > 3) {
                ecr.dob4 = getLongFromDate(dob4.value)
                ecr.age4 = age4.value?.toInt()
                ecr.gender4 = when (gender4.value) {
                    gender4.entries!![0] -> Gender.MALE
                    gender4.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.thirdAndFourthChildGap =thirdAndFourthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfChildren.value?.toInt()!! > 4) {
                ecr.dob5 = getLongFromDate(dob5.value)
                ecr.age5 = age5.value?.toInt()
                ecr.gender5 = when (gender5.value) {
                    gender5.entries!![0] -> Gender.MALE
                    gender5.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.fourthAndFifthChildGap =fourthAndFifthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfChildren.value?.toInt()!! > 5) {
                ecr.dob6 = getLongFromDate(dob6.value)
                ecr.age6 = age6.value?.toInt()
                ecr.gender6 = when (gender6.value) {
                    gender6.entries!![0] -> Gender.MALE
                    gender6.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.fifthANdSixthChildGap =fifthAndSixthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfChildren.value?.toInt()!! > 6) {
                ecr.dob7 = getLongFromDate(dob7.value)
                ecr.age7 = age7.value?.toInt()
                ecr.gender7 = when (gender7.value) {
                    gender7.entries!![0] -> Gender.MALE
                    gender7.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.sixthAndSeventhChildGap =sixthAndSeventhChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfChildren.value?.toInt()!! > 7) {
                ecr.dob8 = getLongFromDate(dob8.value)
                ecr.age8 = age8.value?.toInt()
                ecr.gender8 = when (gender8.value) {
                    gender8.entries!![0] -> Gender.MALE
                    gender8.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.seventhAndEighthChildGap = seventhAndEighthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfChildren.value?.toInt()!! > 8) {
                ecr.dob9 = getLongFromDate(dob9.value)
                ecr.age9 = age9.value?.toInt()
                ecr.gender9 = when (gender9.value) {
                    gender9.entries!![0] -> Gender.MALE
                    gender9.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.eighthAndNinthChildGap = eighthAndNinthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
        }
    }


    private val children = listOf(
        ChildBundle(firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap),
        ChildBundle(secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap),
        ChildBundle(thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap),
        ChildBundle(fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap),
        ChildBundle(fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap),
        ChildBundle(sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap),
        ChildBundle(seventhChildDetails, seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap),
        ChildBundle(eighthChildDetails, eightChildName, dob8, age8, gender8, seventhAndEighthChildGap),
        ChildBundle(ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap)
    )

    private val childNameFields = listOf(
        firstChildName,
        secondChildName,
        thirdChildName,
        forthChildName,
        fifthChildName,
        sixthChildName,
        seventhChildName,
        eightChildName,
        ninthChildName
    )


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        childNameFields.firstOrNull { it.id == formId }?.let { childName ->
            validateEmptyOnEditText(childName)
            validateAllCapsOrSpaceOnEditTextWithHindiEnabled(childName)
        }


        children.forEachIndexed { idx, child ->
            if (formId == child.dob.id) {
                val prevDobValue = if (idx == 0) {
                    null
                } else children[idx - 1].dob.value

                prevDobValue?.let { isValidChildGap(child.dob, it) }

                val currentDobStr = child.dob.value
                if ((idx == 0 && currentDobStr != null && timeAtMarriage != 0L) ||
                    (idx != 0 && children[idx - 1].dob.value != null && currentDobStr != null)
                ) {
                    val currDobLong = getLongFromDate(currentDobStr)
                    assignValuesToAgeFromDob(currDobLong, child.age)
                    validateIntMinMax(child.age)

                    val months = getMonthsFromDob(currDobLong)
                    val existingName = child.name.value
                    if (months <= 3) {
                        val motherName = selectedBen?.firstName ?: ""
                        child.name.value = "Baby of $motherName"
                        child.name.required = false
                    } else {
                        child.name.required = true
                        if (existingName.isNullOrBlank()) {
                            child.name.value = ""
                        }
                    }


                    val prevLong = when {
                        idx == 0 -> timeAtMarriage
                        else -> getLongFromDate(children[idx - 1].dob.value!!)
                    }
                    setSiblingAgeDiff(prevLong, currDobLong, child.gap)

                    children.getOrNull(idx + 1)?.let { nextChild ->
                        nextChild.dob.min = getLongFromDate(getMinimumSecondChildDob(currentDobStr))
                    }

                    updateTimeLessThan18()
                }
                return -1
            }
        }

        if (formId == noOfChildren.id) {
            noOfChildren.min = noOfChildren.value.takeIf { !it.isNullOrEmpty() }?.toLong()
            validateIntMinMax(noOfChildren)

            if (isExistingRecord) {

                val newCount = noOfChildren.value?.toIntOrNull() ?: 0

                val oldCount = children.count { child ->
                    child.dob.value != null ||
                            child.gender.value != null ||
                            child.age.value != null
                }

                if (newCount > oldCount) {
                    for (i in oldCount until newCount) {
                        if (i == 0) {
                            if (timeAtMarriage != 0L) {
                                children[i].dob.min = timeAtMarriage
                            }
                        } else {
                            val prevDob = children[i - 1].dob.value
                            if (!prevDob.isNullOrEmpty()) {
                                children[i].dob.min =
                                    getLongFromDate(getMinimumSecondChildDob(prevDob))
                            }
                        }
                    }

                    val addItems = children.subList(oldCount, newCount)
                        .flatMap { it.toFormList() }

                    infantTriggerDependants(
                        source = noOfChildren,
                        addItems = addItems,
                        removeItems = emptyList()
                    )
                }
            } else {
                val count = noOfChildren.value?.toIntOrNull() ?: 0
                val addItems = children.take(count).flatMap { it.toFormList() }
                val removeItems = children.drop(count).flatMap { it.toFormList() }
                triggerDependants( source = noOfChildren, addItems = addItems, removeItems = removeItems )
                children.drop(count).forEach { it.clearValues() }

            }





            return 1
        }

        val genderIds = children.map { it.gender.id }
        if (genderIds.contains(formId)) {
            var male = 0
            var female = 0
            val genderArray = resources.getStringArray(R.array.ecr_gender_array)

           children.forEach { child ->
                val g = child.gender.value
                if (g == genderArray[0]) male += 1
                else if (g == genderArray[1]) female += 1
            }

            numFemale.value = female.toString()
            numMale.value = male.toString()
            return -1
        }

        return -1
    }

    fun getMonthsFromDob(dob: Long): Int {
        val now = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { timeInMillis = dob }

        val years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        val months = now.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        return years * 12 + months
    }
    fun getIndexOfChildren() = getIndexById(noOfChildren.id)

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


data class ChildBundle(
    val details: FormElement,
    val name: FormElement,
    val dob: FormElement,
    val age: FormElement,
    val gender: FormElement,
    val gap: FormElement
) {
    fun isEmpty(): Boolean {
        return dob.value.isNullOrEmpty() &&
                age.value.isNullOrEmpty() &&
                gender.value.isNullOrEmpty() &&
                name.value.isNullOrEmpty()
    }
    fun toFormList() = listOf(details, name, dob, age, gender, gap)
    fun clearValues() {
        name.value = null
        dob.value = null
        age.value = null
        gender.value = null
        gap.value = null
    }
}


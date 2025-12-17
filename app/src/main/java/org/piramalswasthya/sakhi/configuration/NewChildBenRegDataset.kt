package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.text.InputType
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenBasicCache
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

     val noOfChildren = FormElement(
        id = 12,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_ttl_child_born),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_total_no_child_born,
        showDrawable = true
    )

     val noOfLiveChildren = FormElement(
        id = 13,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_no_live_child),
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_no_of_live_child,
        showDrawable = true
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
        isAddspouse: Int
    ) {
        val list = mutableListOf(
            dateOfReg,
            noOfChildren,
            noOfLiveChildren,


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
                dob1.min = cal.timeInMillis
                timeAtMarriage = cal.timeInMillis
            }
        }


        ben?.let { ecCache ->
            if (ecCache.dateOfReg == 0L) {
                ecCache.dateOfReg = System.currentTimeMillis()
            }

            dateOfReg.value = getDateFromLong(ecCache.dateOfReg)
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
                    setSiblingAgeDiff(timeAtMarriage, it, marriageFirstChildGap)
                }
                ecCache.gender1?.let {
                    gender1.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }


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
                    ecCache.dob1?.let { it1 -> setSiblingAgeDiff(it1, it, firstAndSecondChildGap) }
                }
                ecCache.gender2?.let {
                    gender2.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }

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
                    ecCache.dob2?.let { it1 -> setSiblingAgeDiff(it1, it, secondAndThirdChildGap) }

                }
                ecCache.gender3?.let {
                    gender3.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }

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
                    ecCache.dob3?.let { it1 -> setSiblingAgeDiff(it1, it, thirdAndFourthChildGap) }

                }
                ecCache.gender4?.let {
                    gender4.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }


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
                    ecCache.dob4?.let { it1 -> setSiblingAgeDiff(it1, it, fourthAndFifthChildGap) }
                }
                ecCache.gender5?.let {
                    gender5.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }


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
                    ecCache.dob5?.let { it1 -> setSiblingAgeDiff(it1, it, fifthAndSixthChildGap) }
                }
                ecCache.gender6?.let {
                    gender6.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }


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
                    ecCache.dob6?.let { it1 -> setSiblingAgeDiff(it1, it, sixthAndSeventhChildGap) }

                }
                ecCache.gender7?.let {
                    gender7.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }


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
                    ecCache.dob7?.let { it1 ->
                        setSiblingAgeDiff(
                            it1,
                            it,
                            seventhAndEighthChildGap
                        )
                    }
                }
                ecCache.gender8?.let {
                    gender8.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }


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
                    ecCache.dob8?.let { it1 -> setSiblingAgeDiff(it1, it, eighthAndNinthChildGap) }
                }
                ecCache.gender9?.let {
                    gender9.value = getLocalValueInArray(R.array.ecr_gender_array, it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
                }

                list.addAll(
                    list.indexOf(seventhAndEighthChildGap) + 1,
                    listOf(ninthChildDetails, dob9, age9, gender9, eighthAndNinthChildGap)
                )
            }
        }


        setUpPage(list)

    }

   /* override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

//

            noOfChildren.id -> {
                if (noOfChildren.value.isNullOrEmpty() ||
                    noOfChildren.value?.takeIf { it.isNotEmpty() }?.toInt() == 0
                ) {
                    noOfLiveChildren.value = "0"
                    numMale.value = "0"
                    numFemale.value = "0"


                } else {
                    noOfLiveChildren.max = noOfChildren.value?.takeIf { it.isNotEmpty() }?.toLong()
                    validateIntMinMax(noOfLiveChildren)
                    validateIntMinMax(noOfChildren)
                    if (noOfLiveChildren.value == "0") noOfLiveChildren.value = null
                    numMale.value = null
                    numFemale.value = null
                }
                handleListOnValueChanged(noOfLiveChildren.id, 0)
            }

            dob1.id -> {
                if (dob1.value != null && timeAtMarriage != 0L) {

                    val dob1Long = getLongFromDate(dob1.value)
                    assignValuesToAgeFromDob(dob1Long, age1)
                    validateIntMinMax(age1)
                    setSiblingAgeDiff(timeAtMarriage, dob1Long, marriageFirstChildGap)
                    dob2.min = getLongFromDate(
                        getMinimumSecondChildDob(dob1.value)
                    )
                    updateTimeLessThan18()
                }
                -1
            }

            dob2.id -> {
                isValidChildGap(dob2,dob1.value)
                if (dob1.value != null && dob2.value != null) {
                    val dob2Long = getLongFromDate(dob2.value)
                    val dob1Long = getLongFromDate(dob1.value)
                    assignValuesToAgeFromDob(dob2Long, age2)
                    setSiblingAgeDiff(dob1Long, dob2Long, firstAndSecondChildGap)
                    dob3.min = getLongFromDate(getMinimumSecondChildDob(dob2.value)) //dob2Long
                    updateTimeLessThan18()
                }
                -1
            }

            dob3.id -> {
                isValidChildGap(dob3,dob2.value)
                if (dob2.value != null && dob3.value != null) {
                    val dob2Long = getLongFromDate(dob2.value)
                    val dob3Long = getLongFromDate(dob3.value)
                    assignValuesToAgeFromDob(dob3Long, age3)
                    setSiblingAgeDiff(dob2Long, dob3Long, secondAndThirdChildGap)
                    dob4.min = getLongFromDate(getMinimumSecondChildDob(dob3.value))//dob3Long
                    updateTimeLessThan18()
                }
                -1
            }

            dob4.id -> {
                isValidChildGap(dob4,dob3.value)
                if (dob3.value != null && dob4.value != null) {
                    val dob3Long = getLongFromDate(dob3.value)
                    val dob4Long = getLongFromDate(dob4.value)
                    assignValuesToAgeFromDob(dob4Long, age4)
                    setSiblingAgeDiff(dob3Long, dob4Long, thirdAndFourthChildGap)
                    dob5.min = getLongFromDate(getMinimumSecondChildDob(dob4.value))//dob4Long
                    updateTimeLessThan18()
                }
                -1
            }

            dob5.id -> {
                isValidChildGap(dob5,dob4.value)
                if (dob4.value != null && dob5.value != null) {
                    val dob4Long = getLongFromDate(dob4.value)
                    val dob5Long = getLongFromDate(dob5.value)
                    assignValuesToAgeFromDob(dob5Long, age5)
                    setSiblingAgeDiff(dob4Long, dob5Long, fourthAndFifthChildGap)
                    dob6.min = getLongFromDate(getMinimumSecondChildDob(dob5.value)) //dob5Long
                    updateTimeLessThan18()
                }
                -1
            }

            dob6.id -> {
                isValidChildGap(dob6,dob5.value)
                if (dob5.value != null && dob6.value != null) {
                    val dob5Long = getLongFromDate(dob5.value)
                    val dob6Long = getLongFromDate(dob6.value)
                    assignValuesToAgeFromDob(dob6Long, age6)
                    setSiblingAgeDiff(dob5Long, dob6Long, fifthAndSixthChildGap)
                    dob7.min = getLongFromDate(getMinimumSecondChildDob(dob6.value)) //dob6Long
                    updateTimeLessThan18()
                }
                -1
            }

            dob7.id -> {
                isValidChildGap(dob7,dob6.value)
                if (dob6.value != null && dob7.value != null) {
                    val dob6Long = getLongFromDate(dob6.value)
                    val dob7Long = getLongFromDate(dob7.value)
                    assignValuesToAgeFromDob(dob7Long, age7)
                    setSiblingAgeDiff(dob6Long, dob7Long, sixthAndSeventhChildGap)
                    dob8.min =getLongFromDate(getMinimumSecondChildDob(dob7.value))// dob7Long
                    updateTimeLessThan18()
                }
                -1
            }

            dob8.id -> {
                isValidChildGap(dob8,dob7.value)
                if (dob7.value != null && dob8.value != null) {
                    val dob7Long = getLongFromDate(dob7.value)
                    val dob8Long = getLongFromDate(dob8.value)
                    assignValuesToAgeFromDob(dob8Long, age8)
                    setSiblingAgeDiff(dob7Long, dob8Long, seventhAndEighthChildGap)
                    dob9.min =getLongFromDate(getMinimumSecondChildDob(dob8.value)) // dob8Long
                    updateTimeLessThan18()
                }
                -1
            }

            dob9.id -> {
                isValidChildGap(dob9,dob8.value)
                if (dob8.value != null && dob9.value != null) {
                    val dob8Long = getLongFromDate(dob8.value)
                    val dob9Long = getLongFromDate(dob9.value)
                    assignValuesToAgeFromDob(dob9Long, age9)
                    setSiblingAgeDiff(dob8Long, dob9Long, eighthAndNinthChildGap)
                    updateTimeLessThan18()
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
                            firstChildName,
                            dob1,
                            age1,
                            gender1,
                            marriageFirstChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    2 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails,firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    3 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    4 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails,thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails,forthChildName, dob4, age4, gender4, thirdAndFourthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    5 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails,thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails,forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails,fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    6 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails,thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails,forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails,fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    7 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails,thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails,forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails,fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,  seventhChildName,dob7, age7, gender7, sixthAndSeventhChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    8 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails,thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails,forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails,fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,  seventhChildName,dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, eightChildName, dob8, age8, gender8, seventhAndEighthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    9 -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails, secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails,thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails,forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails,fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,  seventhChildName,dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails, eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails,ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )

                    else -> triggerDependants(
                        source = noOfLiveChildren,
                        addItems = listOf(
                            firstChildDetails,
                            firstChildName,
                            dob1,
                            age1,
                            gender1,
                            marriageFirstChildGap
                        ),
                        removeItems = listOf(
                            firstChildDetails, firstChildName, dob1, age1, gender1, marriageFirstChildGap,
                            secondChildDetails,secondChildName, dob2, age2, gender2, firstAndSecondChildGap,
                            thirdChildDetails, thirdChildName, dob3, age3, gender3, secondAndThirdChildGap,
                            fourthChildDetails, forthChildName, dob4, age4, gender4, thirdAndFourthChildGap,
                            fifthChildDetails, fifthChildName, dob5, age5, gender5, fourthAndFifthChildGap,
                            sixthChildDetails, sixthChildName, dob6, age6, gender6, fifthAndSixthChildGap,
                            seventhChildDetails,seventhChildName, dob7, age7, gender7, sixthAndSeventhChildGap,
                            eighthChildDetails,eightChildName, dob8, age8, gender8, seventhAndEighthChildGap,
                            ninthChildDetails, ninthChildName, dob9, age9, gender9, eighthAndNinthChildGap
                        )
                    )
                }
                return 1
            }

            gender1.id, gender2.id, gender3.id, gender4.id, gender5.id,
            gender6.id, gender7.id, gender8.id, gender9.id -> {
                maleChild = 0
                femaleChild = 0

                if (gender1.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender1.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender2.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender2.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender3.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender3.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender4.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender4.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender5.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender5.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender6.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender6.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender7.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender7.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender8.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender8.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                if (gender9.value == resources.getStringArray(R.array.ecr_gender_array)[0]) {
                    maleChild += 1
                } else if (gender9.value == resources.getStringArray(R.array.ecr_gender_array)[1]) {
                    femaleChild += 1
                }

                numFemale.value = femaleChild.toString()
                numMale.value = maleChild.toString()
                -1
            }


            else -> -1
        }
    }*/



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

    fun mapChild(
        cacheModel: BenRegCache,
        childIndex: Int
    ): BenRegCache {

        val ben = cacheModel.copy()

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
        ben.familyHeadRelation = selectedBen!!.familyHeadRelation
        ben.isSpouseAdded = false
        ben.isChildrenAdded = false
        ben.isMarried = false
        ben.doYouHavechildren = false
        ben.community = selectedBen!!.community
        ben.communityId = selectedBen!!.communityId

        return ben
    }


    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as EligibleCoupleRegCache).let { ecr ->
            ecr.dateOfReg =
                getLongFromDate(dateOfReg.value!!)

            ecr.noOfChildren = noOfChildren.value?.takeIf { it.isNotBlank() }?.toInt() ?: 0
            ecr.noOfLiveChildren = noOfLiveChildren.value?.takeIf { it.isNotBlank() }?.toInt() ?: 0
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
            if ((noOfLiveChildren.value?.toIntOrNull() ?: 0) > 1){
                ecr.dob2 = getLongFromDate(dob2.value)
                ecr.age2 = age2.value?.toInt()
                ecr.gender2 = when (gender2.value) {
                    gender2.entries!![0] -> Gender.MALE
                    gender2.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.firstAndSecondChildGap =firstAndSecondChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfLiveChildren.value?.toInt()!! > 2) {
                ecr.dob3 = getLongFromDate(dob3.value)
                ecr.age3 = age3.value?.toInt()
                ecr.gender3 = when (gender3.value) {
                    gender3.entries!![0] -> Gender.MALE
                    gender3.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.secondAndThirdChildGap =secondAndThirdChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfLiveChildren.value?.toInt()!! > 3) {
                ecr.dob4 = getLongFromDate(dob4.value)
                ecr.age4 = age4.value?.toInt()
                ecr.gender4 = when (gender4.value) {
                    gender4.entries!![0] -> Gender.MALE
                    gender4.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.thirdAndFourthChildGap =thirdAndFourthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfLiveChildren.value?.toInt()!! > 4) {
                ecr.dob5 = getLongFromDate(dob5.value)
                ecr.age5 = age5.value?.toInt()
                ecr.gender5 = when (gender5.value) {
                    gender5.entries!![0] -> Gender.MALE
                    gender5.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.fourthAndFifthChildGap =fourthAndFifthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfLiveChildren.value?.toInt()!! > 5) {
                ecr.dob6 = getLongFromDate(dob6.value)
                ecr.age6 = age6.value?.toInt()
                ecr.gender6 = when (gender6.value) {
                    gender6.entries!![0] -> Gender.MALE
                    gender6.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.fifthANdSixthChildGap =fifthAndSixthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfLiveChildren.value?.toInt()!! > 6) {
                ecr.dob7 = getLongFromDate(dob7.value)
                ecr.age7 = age7.value?.toInt()
                ecr.gender7 = when (gender7.value) {
                    gender7.entries!![0] -> Gender.MALE
                    gender7.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.sixthAndSeventhChildGap =sixthAndSeventhChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfLiveChildren.value?.toInt()!! > 7) {
                ecr.dob8 = getLongFromDate(dob8.value)
                ecr.age8 = age8.value?.toInt()
                ecr.gender8 = when (gender8.value) {
                    gender8.entries!![0] -> Gender.MALE
                    gender8.entries!![1] -> Gender.FEMALE
                    else -> null
                }
                ecr.seventhAndEighthChildGap = seventhAndEighthChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            }
            if (noOfLiveChildren.value?.toInt()!! > 8) {
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

        if (formId == noOfChildren.id) {
            if (noOfChildren.value.isNullOrEmpty() ||
                noOfChildren.value?.takeIf { it.isNotEmpty() }?.toInt() == 0
            ) {
                noOfLiveChildren.value = "0"
                numMale.value = "0"
                numFemale.value = "0"
            } else {
                noOfLiveChildren.max = noOfChildren.value?.takeIf { it.isNotEmpty() }?.toLong()
                validateIntMinMax(noOfLiveChildren)
                validateIntMinMax(noOfChildren)
                if (noOfLiveChildren.value == "0") noOfLiveChildren.value = null
                numMale.value = null
                numFemale.value = null
            }
            return handleListOnValueChanged(noOfLiveChildren.id, 0)
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

        if (formId == noOfLiveChildren.id) {
            noOfChildren.min = noOfLiveChildren.value.takeIf { !it.isNullOrEmpty() }?.toLong()
            validateIntMinMax(noOfLiveChildren)
            validateIntMinMax(noOfChildren)

            if (isExistingRecord) {

                val newCount = noOfLiveChildren.value?.toIntOrNull() ?: 0

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
                        source = noOfLiveChildren,
                        addItems = addItems,
                        removeItems = emptyList()
                    )
                }
            } else {
                val count = noOfLiveChildren.value?.toIntOrNull() ?: 0
                val addItems = children.take(count).flatMap { it.toFormList() }
                val removeItems = children.drop(count).flatMap { it.toFormList() }
                triggerDependants( source = noOfLiveChildren, addItems = addItems, removeItems = removeItems )
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


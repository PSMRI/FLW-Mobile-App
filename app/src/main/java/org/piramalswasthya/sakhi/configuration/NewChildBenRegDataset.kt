package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.text.InputType
import android.util.Log
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
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
import java.util.concurrent.TimeUnit


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

    private val noOfChildren = FormElement(
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

    private val noOfLiveChildren = FormElement(
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
        ben: BenRegCache?,
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
        dateOfReg.value = getDateFromLong(System.currentTimeMillis())

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
        setUpPage(list)

    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
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

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as BenRegCache).let { ben ->

            ben.householdId = selectedBen?.householdId!!
            ben.regDate = Dataset.Companion.getLongFromDate(dateOfReg.value!!)
            ben.firstName = "Baby of ${selectedBen?.firstName}"
            ben.lastName =  "${selectedBen?.lastName}"
            ben.dob = Dataset.Companion.getLongFromDate(dob1.value!!)
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
            ben.fatherName = "${selectedBen?.genDetails?.spouseName}"
            ben.motherName = "${selectedBen?.firstName}"
            ben.familyHeadRelationPosition = 20
            ben.isDeath = false
            ben.isDeathValue = "false"
            ben.dateOfDeath = null
            ben.timeOfDeath = null
            ben.reasonOfDeath = null
            ben.doYouHavechildren = false
            ben.placeOfDeath = null
            ben.mobileNoOfRelationId = 5
            ben.familyHeadRelation = "Other"
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


        }
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
package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.text.InputType
import androidx.lifecycle.MutableLiveData
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.setToStartOfTheDay
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.HEADLINE
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.InputType.TEXT_VIEW
import org.piramalswasthya.sakhi.utils.HelperUtil.getDiffYears
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class EligibleCoupleRegistrationDataset(
    var reqContext: Context,
    var context: Context,
    language: Languages,
    var showDialogEvent: MutableLiveData<String>
) :
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

            // Instead of adding 12 months as minimum, allow same day or after 12 months
            // So minimum acceptable DOB is the same date
            return dateFormat.format(calendar.time)

           /* val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val firstChildDob = dateFormat.parse(firstChildDobStr)

            val calendar = Calendar.getInstance()
            calendar.time = firstChildDob!!
            calendar.add(Calendar.DAY_OF_YEAR, 365) // Add 365 days (approx. 12 months)

            return dateFormat.format(calendar.time)*/
        }
    }

    //////////////////////////////// First Page /////////////////////////////////////////

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

    private val rchId = FormElement(
        id = 1,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_rch_id_wo),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12,
        max = 999999999999L,
        min = 0L,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_rch_id_no,
        showDrawable = true
    )

    private val name = FormElement(
        id = 2,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_name_of_wo),
        arrayId = -1,
        required = false,
        allCaps = true,
        hasSpeechToText = false,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
        isEnabled = false,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_women,
        showDrawable = true
    )

    private val husbandName = FormElement(
        id = 3,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_name_of_hu),
        arrayId = -1,
        required = false,
        allCaps = true,
        hasSpeechToText = false,
        etInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
        isEnabled = false,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_husbund,
        showDrawable = true
    )

    private val age = FormElement(
        id = 4,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_cur_ag_of_wo),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForGenBen.toLong(),
        min = Konstants.minAgeForGenBen.toLong(),
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic__reproductive_age,
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

    private var lmpDate = FormElement(
        id = 73,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.lmp_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = getMinLmpMillis(),
        hasDependants = true,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_anc_date,
        showDrawable = true
    )

    private val nayiPahelKitHandOver = FormElement(
        id = 78,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_is_nai_pahel_kit_handover),
        arrayId = -1,
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = true,
    )

    private val kithandOverDate = FormElement(
        id = 74,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.ecrdset_is_nai_pahel_kit_handover_date),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        min = 0L,
        hasDependants = true,
        showDrawable = true
    )

    private val ashaPhotoTitle = FormElement(
        id = 77,
        inputType = HEADLINE,
        title = resources.getString(R.string.asha_photo),
        arrayId = -1,
        required = false
    )

    private val kitPhotoUploadOne = FormElement(
        id = 75,
        inputType = org.piramalswasthya.sakhi.model.InputType.FILE_UPLOAD,
        title = context.getString(R.string.asha_photo_one),
        required = false,
    )
    private val kitPhotoUploadTwo = FormElement(
        id = 76,
        inputType = org.piramalswasthya.sakhi.model.InputType.FILE_UPLOAD,
        title = context.getString(R.string.asha_photo_two),
        required = false,
    )
    private val womanDetails = FormElement(
        id = 6,
        inputType = HEADLINE,
        title = resources.getString(R.string.ecrdset_dts_of_wo),
        arrayId = -1,
        required = false
    )

    private val aadharNo = FormElement(
        id = 7,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_aad_num_of_wo),
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
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_bank_ac),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 20,
        max = 999999999999999999L,
        min = 10000000L,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_bank_acc_no,
        showDrawable = true
    )

    private val bankName = FormElement(
        id = 9,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_bank_name),
        arrayId = -1,
        required = false,
        etMaxLength = 50,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_bank_name,
        showDrawable = true
    )

    private val branchName = FormElement(
        id = 10,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_branch_name),
        arrayId = -1,
        required = false,
        etMaxLength = 50,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_branch_name,
        showDrawable = true
    )

    private val ifsc = FormElement(
        id = 11,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_ifsc_code),
        arrayId = -1,
        required = false,
        etMaxLength = 11,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_ifsc_code,
        showDrawable = true
    )

    private val noOfChildren = FormElement(
        id = 12,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_ttl_child_born),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
        value = "0",
        isEnabled = false,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_total_no_child_born,
        showDrawable = true
    )

    private val noOfLiveChildren = FormElement(
        id = 13,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.ecrdset_no_live_child),
        arrayId = -1,
        required = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        value = "0",
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 1,
        max = 9,
        min = 0,
        backgroundDrawable=R.drawable.ic_bg_circular,
        iconDrawableRes=R.drawable.ic_female,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private val thirdAndFourthChildGap = FormElement(
        id = 35,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_bet_3_4_child),
        arrayId = -1,
        isEnabled = false,
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
        isEnabled = false,
        arrayId = -1,
        required = true,
        hasDependants = true,
        max = getMaxDobMillis(),
        min = getMinDobMillis(),
    )

    private val age5 = FormElement(
        id = 38,
        inputType = TEXT_VIEW,
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = Konstants.maxAgeForAdolescent.toLong(),
        min = 0,
    )

    private val gender8 = FormElement(
        id = 54,
        inputType = RADIO,
        title = resources.getString(R.string.ecrdset_8_child_sex),
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
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
        isEnabled = false,
        arrayId = -1,
        entries = resources.getStringArray(R.array.ecr_gender_array),
        required = true,
        hasDependants = true,
    )

    private var timeAtMarriage: Long = 0L
    private val eighthAndNinthChildGap = FormElement(
        id = 60,
        inputType = TEXT_VIEW,
        title = resources.getString(R.string.ecrdset_gap_8_9_child),
        isEnabled = false,
        arrayId = -1,
        required = true,
        etInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL,
        etMaxLength = 2,
        max = 99,
        min = 0,
    )

    private val noOfDeliveries = FormElement(
        id = 61,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.no_of_deliveries_is_more_than_3),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val timeLessThan18m = FormElement(
        id = 62,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.time_from_last_delivery_is_less_than_18_months),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val heightShort = FormElement(
        id = 63,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.height_is_very_short_or_less_than_140_cms),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val ageCheck = FormElement(
        id = 64,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.age_is_less_than_18_or_more_than_35_years),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val misCarriage = FormElement(
        id = 65,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.miscarriage_abortion),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val homeDelivery = FormElement(
        id = 66,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.home_delivery_of_previous_pregnancy),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val medicalIssues = FormElement(
        id = 67,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.during_pregnancy_or_delivery_you_faced_any_medical_issues),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )


    private val pastCSection = FormElement(
        id = 68,
        inputType = org.piramalswasthya.sakhi.model.InputType.RADIO,
        title = resources.getString(R.string.past_c_section_cs),
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true
    )

    private val infoChildLabel = FormElement(
        id = 69,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = resources.getString(R.string.information_on_children),
        required = false
    )

    private val physicalObsLabel = FormElement(
        id = 70,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = resources.getString(R.string.physical_observation),
        required = false
    )

    private val obsHistoryLabel = FormElement(
        id = 71,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = resources.getString(R.string.obstetric_history),
        required = false
    )

    private val assesLabel = FormElement(
        id = 72,
        inputType = org.piramalswasthya.sakhi.model.InputType.HEADLINE,
        title = resources.getString(R.string.assess_for_high_risk_conditions_in_the_non_pregnant_women),
        required = false
    )

    private var maleChild = 0

    private var femaleChild = 0

    private var dateOfBirth = 0L

    private var lastDeliveryDate = 0L


    fun getIndexofAshaKitPhotoOne() = getIndexById(kitPhotoUploadOne.id)
    fun getIndexofAshaKitPhoto() = getIndexById(kitPhotoUploadTwo.id)



    suspend fun setUpPage(
        ben: BenRegCache?,
        assess: HRPNonPregnantAssessCache?,
        saved: EligibleCoupleRegCache?,
        childList: List<BenRegCache>,
    ) {
        val list = mutableListOf(
            dateOfReg,
            rchId,
//            name,
            husbandName,
//            age,
            ageAtMarriage,
            lmpDate,
            nayiPahelKitHandOver,
            womanDetails,
//            aadharNo,
//            noOfChildren,
            noOfLiveChildren,
            numMale,
            numFemale,
            assesLabel,
            infoChildLabel,
            noOfDeliveries,
            timeLessThan18m,
            physicalObsLabel,
            heightShort,
            ageCheck,
            obsHistoryLabel,
            misCarriage,
            homeDelivery,
            medicalIssues,
            pastCSection,
        )
        dateOfReg.value = getDateFromLong(System.currentTimeMillis())
//        lmpDate.value = getDateFromLong(1735237800000L)
//        dateOfReg.value?.let {
//            val long = Dataset.getLongFromDate(it)
//            dateOfhivTestDone.min = long
//        }

        ben?.let {
            dateOfReg.min = it.regDate
            rchId.value = ben.rchId
            aadharNo.value = ben.aadharNum?.takeIf { it.isNotEmpty() }?.also {
                aadharNo.inputType = TEXT_VIEW
            }
            name.value = "${ben.firstName} ${if (ben.lastName == null) "" else ben.lastName}"
            husbandName.value = ben.genDetails?.spouseName
            age.value = BenBasicCache.getAgeFromDob(ben.dob).toString()
            dateOfBirth = ben.dob
            updateAgeCheck(dateOfBirth, Calendar.getInstance().timeInMillis)
            ben.genDetails?.ageAtMarriage?.let { it1 ->
                ageAtMarriage.value = it1.toString()
                val cal = Calendar.getInstance()
                cal.timeInMillis = ben.dob
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + it1)
                dob1.min = cal.timeInMillis
                timeAtMarriage = cal.timeInMillis
            }
        }
        assess?.let {
            noOfDeliveries.value = getLocalValueInArray(R.array.yes_no, it.noOfDeliveries)
            timeLessThan18m.value = getLocalValueInArray(R.array.yes_no, it.timeLessThan18m)
            heightShort.value = getLocalValueInArray(R.array.yes_no, it.heightShort)
//            ageCheck.value = getLocalValueInArray(R.array.yes_no,it.age)
            misCarriage.value = getLocalValueInArray(R.array.yes_no, it.misCarriage)
            homeDelivery.value = getLocalValueInArray(R.array.yes_no, it.homeDelivery)
            medicalIssues.value = getLocalValueInArray(R.array.yes_no, it.medicalIssues)
            pastCSection.value = getLocalValueInArray(R.array.yes_no, it.pastCSection)

            infoChildLabel.showHighRisk =
                (noOfDeliveries.value == resources.getStringArray(R.array.yes_no)[0] || timeLessThan18m.value == resources.getStringArray(
                    R.array.yes_no
                )[0])

            physicalObsLabel.showHighRisk =
                (heightShort.value == resources.getStringArray(R.array.yes_no)[0] || ageCheck.value == resources.getStringArray(
                    R.array.yes_no
                )[0])

            obsHistoryLabel.showHighRisk =
                (misCarriage.value == resources.getStringArray(R.array.yes_no)[0] || homeDelivery.value == resources.getStringArray(
                    R.array.yes_no
                )[0]
                        || medicalIssues.value == resources.getStringArray(R.array.yes_no)[0] || pastCSection.value == resources.getStringArray(
                    R.array.yes_no
                )[0])

        }
        var insertIndex = list.indexOf(noOfLiveChildren) + 1

        noOfLiveChildren.value = childList.size.coerceAtMost(9).toString()
        val limitedChildList = childList.take(9)
        numFemale.value = limitedChildList.count { it.gender == Gender.FEMALE }.toString()
        numMale.value = limitedChildList.count { it.gender == Gender.MALE }.toString()
        childList.take(9).forEachIndexed { index, child ->
            val bundle = children[index]
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
                bundle.dob,
                bundle.age,
                bundle.gender,
                bundle.gap
            )

            list.addAll(insertIndex, childViews)

            insertIndex += childViews.size
        }

        saved?.let { ecCache ->
            dateOfReg.value = getDateFromLong(ecCache.dateOfReg)
            bankAccount.value = ecCache.bankAccount?.toString()
            bankName.value = ecCache.bankName
            branchName.value = ecCache.branchName
            ifsc.value = ecCache.ifsc
//            noOfChildren.value = ecCache.noOfChildren.toString()
//            noOfLiveChildren.value = ecCache.noOfLiveChildren.toString()
            lmpDate.value = getDateFromLong(ecCache.lmpDate)
//            numMale.value = ecCache.noOfMaleChildren.toString()
//            numFemale.value = ecCache.noOfFemaleChildren.toString()
            val isKitHandedOver = ecCache.isKitHandedOver == true
            nayiPahelKitHandOver.value = if (isKitHandedOver) "Yes" else "No"
            if (isKitHandedOver) {
                list.addAll(
                    list.indexOf(nayiPahelKitHandOver) + 1,
                    listOf(kithandOverDate)
                )
                if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                    list.addAll(
                        list.indexOf(kithandOverDate) + 1,
                        listOf(ashaPhotoTitle,kitPhotoUploadOne, kitPhotoUploadTwo)
                    )
                }




                ecCache.kitHandedOverDate?.let { kithandOverDate.value = getDateFromLong(it) }
                kitPhotoUploadOne.value = ecCache.kitPhoto1
                kitPhotoUploadTwo.value = ecCache.kitPhoto2
            } else {
                kitPhotoUploadOne.value = null
                kitPhotoUploadTwo.value = null
            }


           /* if (ecCache.noOfLiveChildren > 0) {
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
            }*/
        }
        setUpPage(list)

    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {

//            dateOfReg.id -> {
//                updateTimeLessThan18()
//                handleListOnValueChanged(timeLessThan18m.id, 0)
//                updateAgeCheck(dateOfBirth, getLongFromDate(dateOfReg.value))
//                handleListOnValueChanged(ageCheck.id, 0)
//            }
            nayiPahelKitHandOver.id -> {
                nayiPahelKitHandOver.isEnabled = true
                if (nayiPahelKitHandOver.value == resources.getStringArray(R.array.yes_no)[0] )
                {

                    showDialogEvent.value = "Please upload photo of \"couple with Nayi Pahal kit\", to claim your Incentive."
                    var list1: List<Any> = emptyList()
                    if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        list1 = listOf(
                            kithandOverDate,
                            ashaPhotoTitle,
                            kitPhotoUploadOne,
                            kitPhotoUploadTwo
                        )
                    } else {
                        list1 = listOf(kithandOverDate)
                    }
                    triggerDependants(
                        source = nayiPahelKitHandOver,
                        passedIndex = index,
                        triggerIndex = 0,
                        target =list1 //listOf(kithandOverDate, ashaPhotoTitle,kitPhotoUploadOne, kitPhotoUploadTwo),
                    )
                } else if (nayiPahelKitHandOver.value == resources.getStringArray(R.array.yes_no)[1]) {
                    triggerforHide(
                        source = nayiPahelKitHandOver,
                        passedIndex = index,
                        triggerIndex = 1,
                        target = kithandOverDate,
                        targetSideEffect = listOf(kithandOverDate,ashaPhotoTitle, kitPhotoUploadOne, kitPhotoUploadTwo),
                    )
                }
                0
            }

            rchId.id -> {
                validateRchIdOnEditText(rchId)
            }

            aadharNo.id -> {
                validateIntMinMax(aadharNo)
            }

            bankAccount.id -> {
                validateIntMinMax(bankAccount)
            }

            bankName.id -> {
                validateAllAlphabetsSpaceOnEditText(bankName)
            }

            branchName.id -> {
                validateAllAlphabetsSpaceOnEditText(branchName)
            }

            ifsc.id -> {
                validateAllAlphaNumericOnEditText(ifsc)
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
                    noOfDeliveries.value = resources.getStringArray(R.array.yes_no)[1]
                    noOfDeliveries.isEnabled = false
                    handleListOnValueChanged(noOfDeliveries.id, 0)
                    timeLessThan18m.value = null
                    timeLessThan18m.isEnabled = true
                    handleListOnValueChanged(timeLessThan18m.id, 0)

                } else {
                    noOfLiveChildren.max = noOfChildren.value?.takeIf { it.isNotEmpty() }?.toLong()
                    validateIntMinMax(noOfLiveChildren)
                    validateIntMinMax(noOfChildren)
                    noOfChildren.value?.let {
                        if (it.toInt() > 3) {
                            noOfDeliveries.value = resources.getStringArray(R.array.yes_no)[0]
                            noOfDeliveries.isEnabled = false
                        } else {
                            noOfDeliveries.value = resources.getStringArray(R.array.yes_no)[1]
                            noOfDeliveries.isEnabled = false
                        }
                        handleListOnValueChanged(noOfDeliveries.id, 0)
                    }
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
                    validateIntMinMax(age1)
                    setSiblingAgeDiff(timeAtMarriage, dob1Long, marriageFirstChildGap)
                 //   dob2.min = dob1Long
                    dob2.min = getLongFromDate(getMinimumSecondChildDob(dob1.value))
                    updateTimeLessThan18()
                }
                -1
            }

            dob2.id -> {
             //   dob2.inputType = EDIT_TEXT
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

            noOfDeliveries.id, timeLessThan18m.id -> {
                infoChildLabel.showHighRisk =
                    (noOfDeliveries.value == resources.getStringArray(R.array.yes_no)[0] || timeLessThan18m.value == resources.getStringArray(
                        R.array.yes_no
                    )[0])
                -1
            }

            heightShort.id, ageCheck.id -> {
                physicalObsLabel.showHighRisk =
                    (heightShort.value == resources.getStringArray(R.array.yes_no)[0] || ageCheck.value == resources.getStringArray(
                        R.array.yes_no
                    )[0])
                -1
            }

            misCarriage.id, homeDelivery.id, medicalIssues.id, pastCSection.id -> {
                obsHistoryLabel.showHighRisk =
                    (misCarriage.value == resources.getStringArray(R.array.yes_no)[0] || homeDelivery.value == resources.getStringArray(
                        R.array.yes_no
                    )[0]
                            || medicalIssues.value == resources.getStringArray(R.array.yes_no)[0] || pastCSection.value == resources.getStringArray(
                        R.array.yes_no
                    )[0])
                -1
            }

            else -> -1
        }
    }

    private suspend fun updateAgeCheck(dob: Long, current: Long) {
        val calReg = Calendar.getInstance()
        calReg.timeInMillis = current
        val calDob = Calendar.getInstance()
        calDob.timeInMillis = dateOfBirth

        if (getDiffYears(calDob, calReg) < 18 || getDiffYears(calDob, calReg) > 35) {
            ageCheck.value = resources.getStringArray(R.array.yes_no)[0]
            ageCheck.isEnabled = false
        } else {
            ageCheck.value = resources.getStringArray(R.array.yes_no)[1]
            ageCheck.isEnabled = false
        }
        handleListOnValueChanged(ageCheck.id, 0)
    }

    private suspend fun updateTimeLessThan18() {
        lastDeliveryDate = maxOf(
            getLongFromDate(dob1.value),
            getLongFromDate(dob2.value),
            getLongFromDate(dob3.value),
            getLongFromDate(dob4.value),
            getLongFromDate(dob5.value),
            getLongFromDate(dob6.value),
            getLongFromDate(dob7.value),
            getLongFromDate(dob8.value),
            getLongFromDate(dob9.value)
        )
        if (lastDeliveryDate > 0) {
            if (Calendar.getInstance().timeInMillis - lastDeliveryDate <= TimeUnit.DAYS.toMillis(548)) {
                timeLessThan18m.value = resources.getStringArray(R.array.yes_no)[0]
                timeLessThan18m.isEnabled = false
            } else {
                timeLessThan18m.value = resources.getStringArray(R.array.yes_no)[1]
                timeLessThan18m.isEnabled = false
            }
            handleListOnValueChanged(timeLessThan18m.id, 0)
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
        (cacheModel as EligibleCoupleRegCache).let { ecr ->
            ecr.dateOfReg = getLongFromDate(dateOfReg.value!!)
            lmpDate.value?.let {
                ecr.lmpDate = getLongFromDate(it)
            }
            ecr.bankAccount = bankAccount.value?.takeIf { it.isNotBlank() }?.toLong()
            ecr.bankName = bankName.value
            ecr.branchName = branchName.value
            ecr.ifsc = ifsc.value
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
            ecr.isKitHandedOver = nayiPahelKitHandOver.value.equals("yes", ignoreCase = true)
            ecr.kitHandedOverDate = getLongFromDate(kithandOverDate.value)
            ecr.kitPhoto1 = kitPhotoUploadOne.value
            ecr.kitPhoto2 = kitPhotoUploadTwo.value
            ecr.marriageFirstChildGap =marriageFirstChildGap.value?.filter { it.isDigit() }?.toIntOrNull() ?: 0
              //  marriageFirstChildGap.value?.takeIf { it.isNotBlank() }?.toInt()
            if (noOfLiveChildren.value?.toInt()!! > 1) {
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

    fun mapValueToBen(ben: BenRegCache?): Boolean {
        var isUpdated = false
        val rchIdFromBen = ben?.rchId?.takeIf { it.isNotEmpty() }?.toLong()
        val aadharNoFromBen = ben?.aadharNum?.takeIf { it.isNotEmpty() }
        rchId.value?.takeIf {
            it.isNotEmpty()
        }?.toLong()?.let {
            if (it != rchIdFromBen) {
                ben?.rchId = it.toString()
                isUpdated = true
            }
        }

        isHighRisk().let { highRisk ->
            if (highRisk) {
                ben?.isHrpStatus = true
            }
        }
        if (isUpdated) {
            if (ben?.processed != "N")
                ben?.processed = "U"
            ben?.syncState = SyncState.UNSYNCED
        }
        return isUpdated
    }

    fun mapValuesToAssess(assess: HRPNonPregnantAssessCache?, pageNumber: Int) {
        assess?.let { form ->
            form.noOfDeliveries = if (noOfDeliveries.value != null) getEnglishValueInArray(
                R.array.yes_no,
                noOfDeliveries.value
            ) else null
            form.heightShort = if (heightShort.value != null) getEnglishValueInArray(
                R.array.yes_no,
                heightShort.value
            ) else null
            form.age = if (ageCheck.value != null) getEnglishValueInArray(
                R.array.yes_no,
                ageCheck.value
            ) else null
            form.misCarriage = if (misCarriage.value != null) getEnglishValueInArray(
                R.array.yes_no,
                misCarriage.value
            ) else null
            form.homeDelivery = if (homeDelivery.value != null) getEnglishValueInArray(
                R.array.yes_no,
                homeDelivery.value
            ) else null
            form.medicalIssues = if (medicalIssues.value != null) getEnglishValueInArray(
                R.array.yes_no,
                medicalIssues.value
            ) else null
            form.timeLessThan18m = if (timeLessThan18m.value != null) getEnglishValueInArray(
                R.array.yes_no,
                timeLessThan18m.value
            ) else null
            form.pastCSection = if (pastCSection.value != null) getEnglishValueInArray(
                R.array.yes_no,
                pastCSection.value
            ) else null
            form.isHighRisk = isHighRisk()
            form.syncState = SyncState.UNSYNCED
        }
    }

    fun getIndexOfTimeLessThan18m() = getIndexById(timeLessThan18m.id)
    fun getIndexOfChildLabel() = getIndexById(infoChildLabel.id)

    fun getIndexOfPhysicalObservationLabel() = getIndexById(physicalObsLabel.id)

    fun getIndexOfObstetricHistoryLabel() = getIndexById(obsHistoryLabel.id)

    fun isHighRisk(): Boolean {
        return noOfDeliveries.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                timeLessThan18m.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                heightShort.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                age.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                misCarriage.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                homeDelivery.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                medicalIssues.value.contentEquals(resources.getStringArray(R.array.yes_no)[0]) ||
                pastCSection.value.contentEquals(resources.getStringArray(R.array.yes_no)[0])
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

    fun getIndexOfTimeLessThan18() = getIndexById(timeLessThan18m.id)

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        if (lastImageFormId == 75) {
            kitPhotoUploadOne.value = dpUri.toString()
            kitPhotoUploadOne.errorText = null

        } else {
            kitPhotoUploadTwo.value = dpUri.toString()
            kitPhotoUploadTwo.errorText = null

        }

    }

    private val children = listOf(
        ChildBundle(firstChildDetails,  dob1, age1, gender1, marriageFirstChildGap),
        ChildBundle(secondChildDetails,  dob2, age2, gender2, firstAndSecondChildGap),
        ChildBundle(thirdChildDetails,  dob3, age3, gender3, secondAndThirdChildGap),
        ChildBundle(fourthChildDetails,  dob4, age4, gender4, thirdAndFourthChildGap),
        ChildBundle(fifthChildDetails,  dob5, age5, gender5, fourthAndFifthChildGap),
        ChildBundle(sixthChildDetails,  dob6, age6, gender6, fifthAndSixthChildGap),
        ChildBundle(seventhChildDetails,  dob7, age7, gender7, sixthAndSeventhChildGap),
        ChildBundle(eighthChildDetails,  dob8, age8, gender8, seventhAndEighthChildGap),
        ChildBundle(ninthChildDetails,  dob9, age9, gender9, eighthAndNinthChildGap)
    )

    data class ChildBundle(
        val details: FormElement,
        val dob: FormElement,
        val age: FormElement,
        val gender: FormElement,
        val gap: FormElement
    ) {
        fun isEmpty(): Boolean {
            return dob.value.isNullOrEmpty() &&
                    age.value.isNullOrEmpty() &&
                    gender.value.isNullOrEmpty()
        }
        fun toFormList() = listOf(details, dob, age, gender, gap)
        fun clearValues() {

            dob.value = null
            age.value = null
            gender.value = null
            gap.value = null
        }
    }
}
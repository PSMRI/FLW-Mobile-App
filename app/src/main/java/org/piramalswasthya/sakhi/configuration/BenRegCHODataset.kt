package org.piramalswasthya.sakhi.configuration

import android.content.Context
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.utils.HelperUtil.getDiffYears
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BenRegCHODataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val dateOfReg = FormElement(
        id = 1,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.hrp_dor),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis()
    )
    private val name = FormElement(
        id = 2,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.hrp_name),
        arrayId = -1,
        required = true,
        hasSpeechToText = true,
        allCaps = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val dob = FormElement(
        id = 3,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.hrp_dob),
        arrayId = -1,
        required = true,
        max = System.currentTimeMillis(),
        hasDependants = true
    )

    private val husbandName = FormElement(
        id = 4,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.husband_s_name),
        arrayId = -1,
        required = true,
        hasSpeechToText = true,
        allCaps = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val contactNumber = FormElement(
        id = 5,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.contact_number),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 10,
        max = 9999999999,
        min = 6000000000
    )

    private val rchId = FormElement(
        id = 6,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.rch_id),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 12
    )

    private val isPregnant = FormElement(
        id = 7,
        inputType = InputType.RADIO,
        title = resources.getString(R.string.is_pregnant),
        entries = resources.getStringArray(R.array.yes_no),
        required = true,
        hasDependants = false
    )

    private val age = FormElement(
        id = 8,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.age_in_years),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 2,
        min = 12,
        max = 99,
        hasDependants = true
    )

    suspend fun setUpPage() {
        val list = mutableListOf(
            dateOfReg,
            name,
            dob,
            age,
            husbandName,
            contactNumber,
            rchId,
            isPregnant
        )
        dateOfReg.min = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
        val now = Calendar.getInstance()
        val past = Calendar.getInstance()
        past.set(Calendar.YEAR, now.get(Calendar.YEAR) - 99)
        dob.min = past.timeInMillis
        past.set(Calendar.YEAR, now.get(Calendar.YEAR) - 12)
        dob.max = past.timeInMillis
        setUpPage(list)
    }

    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            dob.id -> {
                age.value = calculateAge(getLongFromDate(dob.value)).toString()
                validateIntMinMax(age)
            }

            name.id -> {
               // validateAllCapsOrSpaceOnEditText(name)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(name)
            }

            husbandName.id -> {
             //   validateAllCapsOrSpaceOnEditText(husbandName)
                validateAllCapsOrSpaceOnEditTextWithHindiEnabled(husbandName)
            }

            age.id -> {
                age.value?.let {
                    validateIntMinMax(age)
                    if (it.isNotBlank() && it.isNotEmpty()) {
                        dob.value = getDateFromLong(calculateDob(it.toInt()))
                    }
                }
                -1
            }

            contactNumber.id -> validateMobileNumberOnEditText(contactNumber)
            else -> -1
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as BenRegCache).let { form ->
            form.regDate = getLongFromDate(dateOfReg.value)
            form.firstName = name.value
            form.genDetails?.spouseName = husbandName.value
            form.contactNumber = contactNumber.value?.toLong()!!
            form.rchId = rchId.value
            form.dob = getLongFromDate(dob.value)
            val calDob = Calendar.getInstance().apply {
                timeInMillis = getLongFromDate(dob.value)
            }
            val calNow = Calendar.getInstance()
            form.age = getDiffYears(calDob, calNow)
            form.ageUnit = AgeUnit.YEARS
            form.ageUnitId = 3
            if (isPregnant.value == resources.getStringArray(R.array.yes_no)[0]) {
                form.genDetails?.let {
                    it.reproductiveStatus =
                        englishResources.getStringArray(R.array.nbr_reproductive_status_array2)[1]
                    it.reproductiveStatusId = 2
                }
                form.processed = "N"
            }
        }
    }

    fun getIndexOfDOB() = getIndexById(dob.id)

    fun getIndexOfAge() = getIndexById(age.id)
}

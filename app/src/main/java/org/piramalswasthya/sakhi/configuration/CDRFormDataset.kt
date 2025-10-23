package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.widget.LinearLayout
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType

class CDRFormDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    private val childName = FormElement(
        id = 1,
        inputType = InputType.TEXT_VIEW,
        title = "Name of the Child",
        required = false
    )
    private val dateOfBirth = FormElement(
        id = 2,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.date_of_birth),
        required = false
    )
    private val age = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.mdsr_age),
        required = false
    )
    private val visitDate = FormElement(
        id = 4,
        inputType = InputType.DATE_PICKER,
        min = 0L,
        max = System.currentTimeMillis(),
        title = resources.getString(R.string.str_visit),
        required = true
    )
    private val gender = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.str_gender),
        required = false
    )
    private val motherName = FormElement(
        id = 6,
        inputType = InputType.TEXT_VIEW,
        title =  resources.getString(R.string.mother_name),
        required = false
    )
    private val fatherName = FormElement(
        id = 7,
        inputType = InputType.TEXT_VIEW,
        title = resources.getString(R.string.father_s_name),
        required = false
    )
    private val address = FormElement(
        id = 8,
        inputType = InputType.TEXT_VIEW,
        title =resources.getString(R.string.str_address),
        required = false
    )
    private val houseNumber = FormElement(
        id = 9,
        inputType = InputType.EDIT_TEXT,
        title =resources.getString(R.string.str_house_number),
        required = false
    )
    private val mohalla = FormElement(
        id = 10,
        inputType = InputType.EDIT_TEXT,
        title =resources.getString(R.string.str_mohalla_colony),
        required = false
    )
    private val landmarks = FormElement(
        id = 11,
        inputType = InputType.EDIT_TEXT,
        title =resources.getString(R.string.str_landmark_if_any),
        required = false
    )
    private val pincode = FormElement(
        id = 12,
        inputType = InputType.EDIT_TEXT,
        title =resources.getString(R.string.str_pincode),
        etMaxLength = 6,
        min = 100000,
        max = 999999,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val landline = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title =resources.getString(R.string.str_landline),
        etMaxLength = 12,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val mobileNumber = FormElement(
        id = 14,
        inputType = InputType.TEXT_VIEW,
        title =resources.getString(R.string.str_mobile_number),
        required = false
    )
    private val dateOfDeath = FormElement(
        id = 15,
        inputType = InputType.DATE_PICKER,
        title =resources.getString(R.string.mdsr_date_of_deceased),
        min = 0L,
        max = System.currentTimeMillis(),
        required = true
    )
    private val timeOfDeath = FormElement(
        id = 16,
        inputType = InputType.TIME_PICKER,
        title =resources.getString(R.string.str_time),
        required = false
    )
    private val placeOfDeath = FormElement(
        id = 17,
        inputType = InputType.RADIO,
        title =resources.getString(R.string.str_place_of_death),
        required = true,
        orientation = LinearLayout.VERTICAL,
        entries = arrayOf("Home", "Hospital", "In transit")
    )
    private val firstInformant = FormElement(
        id = 18,
        inputType = InputType.TEXT_VIEW,
        title =resources.getString(R.string.str_name_of_first_informant),
        required = false
    )
    private val ashaSign = FormElement(
        id = 19,
        inputType = InputType.EDIT_TEXT,
        title = resources.getString(R.string.str_name_of_asha),
        required = false
    )
    private val dateOfNotification = FormElement(
        id = 20,
        inputType = InputType.DATE_PICKER,
        title = resources.getString(R.string.str_date_of_notification),
        min = 0L,
        max = System.currentTimeMillis(),
        required = false
    )

    suspend fun setUpPage(
        ben: BenRegCache,
        currentAddress: String?,
        currentHouseNumber: String?,
        currentMohalla: String?,
        saved: CDRCache?
    ) {
        val list = listOf(
            childName,
            dateOfBirth,
            age,
            visitDate,
            gender,
            motherName,
            fatherName,
            address,
            houseNumber,
            mohalla,
            landmarks,
            pincode,
            landline,
            mobileNumber,
            dateOfDeath,
            timeOfDeath,
            placeOfDeath,
            firstInformant,
            ashaSign,
            dateOfNotification
        )
        childName.value = "${ben.firstName} ${ben.lastName}"
        dateOfBirth.value = getDateFromLong(ben.dob)
        age.value =
            "${BenBasicCache.getAgeFromDob(ben.dob)} ${BenBasicCache.getAgeUnitFromDob(ben.dob)}"
        visitDate.value = getDateFromLong(System.currentTimeMillis())
        gender.value = ben.gender?.name
        motherName.value = ben.motherName
        fatherName.value = ben.fatherName
        address.value = currentAddress
        houseNumber.value = currentHouseNumber
        mohalla.value = currentMohalla
        mobileNumber.value = ben.contactNumber.toString()
        saved?.let { savedCdr ->
            visitDate.value = savedCdr.visitDate?.let { it1 -> getDateFromLong(it1) }
            landmarks.value = savedCdr.landmarks
            pincode.value = savedCdr.pincode?.toString()
            landline.value = savedCdr.landline?.toString()
            mobileNumber.value = savedCdr.mobileNumber.toString()
            dateOfDeath.value = getDateFromLong(savedCdr.dateOfDeath)
            timeOfDeath.value = savedCdr.timeOfDeath?.let { getDateFromLong(it) }
            placeOfDeath.value = savedCdr.placeOfDeath
            firstInformant.value = savedCdr.firstInformant
            ashaSign.value = savedCdr.ashaSign
            dateOfNotification.value = savedCdr.dateOfNotification?.let { getDateFromLong(it) }
        }


        setUpPage(list)
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return -1
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as CDRCache).let { cdr ->
            cdr.visitDate = getLongFromDate(visitDate.value)
            cdr.motherName = motherName.value
            cdr.fatherName = fatherName.value
            cdr.address = address.value
            cdr.houseNumber = houseNumber.value
            cdr.mohalla = mohalla.value
            cdr.landmarks = landmarks.value
            cdr.pincode = pincode.value?.toInt()
            cdr.landline = landline.value?.toLong()
            cdr.mobileNumber = mobileNumber.value!!.toLong()
            cdr.dateOfDeath = getLongFromDate(dateOfDeath.value)
            cdr.timeOfDeath = timeOfDeath.value?.let { getLongFromDate(it) }
            cdr.placeOfDeath = placeOfDeath.value
            cdr.firstInformant = firstInformant.value
            cdr.ashaSign = ashaSign.value
            cdr.dateOfNotification = dateOfNotification.value?.let { getLongFromDate(it) }
        }
    }
}
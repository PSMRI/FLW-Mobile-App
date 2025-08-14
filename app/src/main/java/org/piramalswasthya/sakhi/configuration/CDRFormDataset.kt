package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.LinearLayout
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CDRFormDataset(
    context: Context, currentLanguage: Languages,
    val preferences: PreferenceDao
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
        title = "Date of Birth",
        required = false
    )
    private val age = FormElement(
        id = 3,
        inputType = InputType.TEXT_VIEW,
        title = "Age",
        required = false
    )
    private val visitDate = FormElement(
        id = 4,
        inputType = InputType.DATE_PICKER,
        min = 0L,
        max = System.currentTimeMillis(),
        title = "Visit Date",
        required = true
    )
    private val gender = FormElement(
        id = 5,
        inputType = InputType.TEXT_VIEW,
        title = "Gender",
        required = false
    )
    private val motherName = FormElement(
        id = 6,
        inputType = InputType.TEXT_VIEW,
        title = "Mother’s Name",
        required = false
    )
    private val fatherName = FormElement(
        id = 7,
        inputType = InputType.TEXT_VIEW,
        title = "Father’s Name",
        required = false
    )
    private val address = FormElement(
        id = 8,
        inputType = InputType.TEXT_VIEW,
        title = "Address",
        required = false
    )
    private val houseNumber = FormElement(
        id = 9,
        inputType = InputType.TEXT_VIEW,
        title = "House number",
        required = false
    )
    private val mohalla = FormElement(
        id = 10,
        inputType = InputType.TEXT_VIEW,
        title = "Mohalla/Colony ",
        required = false
    )
    private val landmarks = FormElement(
        id = 11,
        inputType = InputType.TEXT_VIEW,
        title = "Landmarks, if any ",
        required = false
    )
    private val pincode = FormElement(
        id = 12,
        inputType = InputType.TEXT_VIEW,
        title = "Pincode",
        etMaxLength = 6,
        min = 100000,
        max = 999999,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val landline = FormElement(
        id = 13,
        inputType = InputType.EDIT_TEXT,
        title = "Landline",
        etMaxLength = 12,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        required = false
    )
    private val mobileNumber = FormElement(
        id = 14,
        inputType = InputType.TEXT_VIEW,
        title = "Mobile number",
        required = false
    )
    private val dateOfDeath = FormElement(
        id = 15,
        inputType = InputType.TEXT_VIEW,
        title = "Date of death",
        min = 0L,
        max = System.currentTimeMillis(),
        required = true
    )
    private val timeOfDeath = FormElement(
        id = 16,
        inputType = InputType.TIME_PICKER,
        title = "Time",
        required = false
    )
    private val placeOfDeath = FormElement(
        id = 17,
        inputType = InputType.TEXT_VIEW,
        title = "Place of Death",
        required = true,
    )
    private val firstInformant = FormElement(
        id = 18,
        inputType = InputType.TEXT_VIEW,
        title = "Name of First Informant ",
        required = false
    )
    private val ashaSign = FormElement(
        id = 19,
        inputType = InputType.EDIT_TEXT,
        title = "Signature/Name of ASHA",
        required = false
    )
    private val dateOfNotification = FormElement(
        id = 20,
        inputType = InputType.DATE_PICKER,
        title = "Date of Notification ",
        min = 0L,
        max = System.currentTimeMillis(),
        required = false
    )

    private val cdrFileUpload1 = FormElement(
        id = 21,
        inputType = InputType.FILE_UPLOAD,
        title = "CDR form from ANM 1",
        required = false,
    )
    private val cdrFileUpload2 = FormElement(
        id = 22,
        inputType = InputType.FILE_UPLOAD,
        title = "CDR form from ANM 2",
        required = false,
    )
    private val cdrDeathFileUpload = FormElement(
        id = 23,
        inputType = InputType.FILE_UPLOAD,
        title = "Death Certificate",
        required = false,
    )

    private val state = FormElement(
        id = 24,
        inputType = InputType.TEXT_VIEW,
        title = "State ",
        required = false
    )
    private val district = FormElement(
        id = 25,
        inputType = InputType.TEXT_VIEW,
        title = "District/Tehsil",
        required = false
    )
    private val block = FormElement(
        id = 26,
        inputType = InputType.TEXT_VIEW,
        title = "Block ",
        required = false
    )
    private val village = FormElement(
        id = 27,
        inputType = InputType.TEXT_VIEW,
        title = "Village/Town/City",
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
            state,
            district,
            block,
            village,
            houseNumber,
            mohalla,
//            landmarks,
//            pincode,
            landline,
            mobileNumber,
            dateOfDeath,
            placeOfDeath,
            firstInformant,
//            ashaSign,
            timeOfDeath,

            dateOfNotification,
            cdrFileUpload1, cdrFileUpload2, cdrDeathFileUpload
        )
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val deathDateMillis = ben.dateOfDeath?.let {
            try {
                dateFormat.parse(it)?.time
            } catch (e: Exception) {
                Timber.tag("CDRFormDataset").e(e, "Failed to parse death date: " + it)
                null
            }
        } ?: System.currentTimeMillis()
        dateOfNotification.min = deathDateMillis
        dateOfNotification.max = System.currentTimeMillis()

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
        dateOfDeath.value = ben.dateOfDeath
//        timeOfDeath.value =ben.timeOfDeath
        placeOfDeath.value = ben.placeOfDeath
        mobileNumber.value = ben.contactNumber.toString()
        val user = preferences.getLoggedInUser()
        firstInformant.value = user?.userName ?: ""
        state.value = user?.state?.name ?: ""
        district.value = user?.district?.name ?: ""
        block.value = user?.block?.name ?: ""
        village.value = user!!.villages[0].name
        saved?.let { savedCdr ->
            visitDate.value = savedCdr.visitDate?.let { it1 -> getDateFromLong(it1) }
            landmarks.value = savedCdr.landmarks
            cdrFileUpload2.value = savedCdr.cdr2File
            cdrFileUpload1.value = savedCdr.cdr1File
            cdrDeathFileUpload.value = savedCdr.cdrDeathCertFile
            pincode.value = savedCdr.pincode?.toString()
            landline.value = savedCdr.landline?.toString()
            mobileNumber.value = savedCdr.mobileNumber.toString()
//            dateOfDeath.value = getDateFromLong(savedCdr.dateOfDeath)
            timeOfDeath.value = savedCdr.timeOfDeath?.let { getDateFromLong(it) }
//            placeOfDeath.value = savedCdr.placeOfDeath
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
            cdr.cdr1File = cdrFileUpload1.value
            cdr.cdr2File = cdrFileUpload2.value
            cdr.cdrDeathCertFile = cdrDeathFileUpload.value
            cdr.houseNumber = houseNumber.value
            cdr.mohalla = mohalla.value
            cdr.landmarks = landmarks.value
            cdr.pincode = pincode.value?.toInt()
            cdr.landline = landline.value?.toLong()
            cdr.mobileNumber = mobileNumber.value!!.toLong()
            cdr.dateOfDeath = getLongFromDate(dateOfDeath.value)
            cdr.timeOfDeath = convertTimeToLong(timeOfDeath.value)
            cdr.placeOfDeath = placeOfDeath.value
            cdr.firstInformant = firstInformant.value
            cdr.ashaSign = ashaSign.value
            cdr.dateOfNotification = dateOfNotification.value?.let { getLongFromDate(it) }
        }
    }

    fun getIndexOfCDR1() = getIndexById(cdrFileUpload1.id)
    fun getIndexOfCDR2() = getIndexById(cdrFileUpload2.id)
    fun getIndexOfIsDeathCertificate() = getIndexById(cdrDeathFileUpload.id)


    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {

        when (lastImageFormId) {
            21 -> {
                cdrFileUpload1.value = dpUri.toString()
                cdrFileUpload1.errorText = null
            }

            22 -> {
                cdrFileUpload2.value = dpUri.toString()
                cdrFileUpload2.errorText = null
            }

            23 -> {
                cdrDeathFileUpload.value = dpUri.toString()
                cdrDeathFileUpload.errorText = null
            }

        }
    }

    private fun convertTimeToLong(value: Any?): Long {
        return when (value) {
            is Long -> value
            is String -> {
                try {
                    val parts = value.split(":")
                    val hours = parts.getOrNull(0)?.toIntOrNull() ?: return 0L
                    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: return 0L
                    return (hours * 60 * 60 * 1000 + minutes * 60 * 1000).toLong()
                } catch (e: Exception) {
                    0L
                }
            }

            else -> 0L
        }
    }


}
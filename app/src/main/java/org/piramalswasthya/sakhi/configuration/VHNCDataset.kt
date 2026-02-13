package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.util.Log
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.IMAGE_VIEW
import org.piramalswasthya.sakhi.model.InputType.RADIO
import org.piramalswasthya.sakhi.model.VHNCCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VHNCDataset(
    context: Context, currentLanguage: Languages,
) : Dataset(context, currentLanguage) {

    companion object {
        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }
        private fun getTwoMonthsBackMillis(): Long {
            return Calendar.getInstance().apply {
                add(Calendar.MONTH, -2)
            }.timeInMillis
        }
    }


    private val vhncDate = FormElement(
        id = 2,
        inputType = DATE_PICKER,
        title = context.getString(R.string.vhsnc_meeting_date),
        arrayId = -1,
        required = true,
        min = getTwoMonthsBackMillis(),
        max = System.currentTimeMillis()
    )
    private val place = FormElement(
        id = 3,
        inputType = InputType.DROPDOWN,
        title = context.getString(R.string.place_of_vhsnc_meeting),
        entries = resources.getStringArray(R.array.place_of_vhsnc),
        arrayId = R.array.place_of_vhsnc,
        required = true,
    )

    private val villageName = FormElement(
        id = 9,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.village_name),
        arrayId = -1,
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val noOfBeneficiariesAttended = FormElement(
        id = 4,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.no_b_attended),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )

    private val noOfANM = FormElement(
        id = 11,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.number_of_anm_mpw_attended),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )
    private val noOfAWW = FormElement(
        id = 12,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.number_of_aww_attended),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )
    private val noOfPW = FormElement(
        id = 13,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.number_of_pregnant_woman),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )
  private val noOfLM = FormElement(
        id = 14,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.number_of_lactating_mothers),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )
    private val noOfCommentte = FormElement(
        id = 15,
        inputType = EDIT_TEXT,
        title = context.getString(R.string.number_of_committee_members_present),
        arrayId = -1,
        value = "0",
        required = false,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 3,
        max = 999,
        min = 1
    )

    private val followupPrevius = FormElement(
        id = 16,
        inputType = RADIO,
        title = context.getString(R.string.follow_up_the_previous_meeting_minutes),
        arrayId = R.array.yes_no,
        entries = resources.getStringArray(R.array.yes_no),
        required = false,
        hasDependants = true,
    )
    private val pic1 = FormElement(
        id = 1,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.upload_image),
        arrayId = -1,
        required = false,
    )
    private val pic2 = FormElement(
        id = 2,
        inputType = IMAGE_VIEW,
        title = resources.getString(R.string.upload_image),
        arrayId = -1,
        required = false,
    )

    suspend fun setUpPage( vhnd: VHNCCache?) {


        if (pic1.value.isNullOrBlank()) {
            pic1.value = "default"
        }

        if (pic2.value.isNullOrBlank()) {
            pic2.value = "default"
        }
        val list = mutableListOf(
            vhncDate,
            place,
        )


        if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            list.add(villageName)
            list.add(noOfBeneficiariesAttended)
            list.add(noOfANM)
            list.add(noOfAWW)
            list.add(noOfPW)
            list.add(noOfLM)
            list.add(noOfCommentte)
            list.add(followupPrevius)
            list.add(pic1)
            list.add(pic2)

            noOfBeneficiariesAttended.title=
                resources.getString(R.string.number_of_mitanins_participated_in_vhsnc_meeting)

        }
        else{
            list.add(noOfBeneficiariesAttended)
            list.add(pic1)
            list.add(pic2)
            noOfBeneficiariesAttended.title= resources.getString(R.string.number_of_asha_participated_in_vhsnc_meeting)

        }
        vhncDate.value = getCurrentDateString()
        vhnd?.let {
            vhncDate.value = it.vhncDate
            pic1.value = it.image1
            pic2.value = it.image2
            place.value = it.place
            villageName.value = it.villageName
            noOfBeneficiariesAttended.value = it.noOfBeneficiariesAttended.toString()
            noOfANM.value = it.anm.toString()
            noOfAWW.value = it.aww.toString()
            noOfPW.value = it.noOfPragnentWoment.toString()
            noOfLM.value = it.noOfLactingMother.toString()
            noOfCommentte.value = it.noOfCommittee.toString()
            followupPrevius.value = it.followupPrevius?.let { value ->
                val options = resources.getStringArray(R.array.yes_no)
                if (value) options[0] else options[1]
            }
        }
        setUpPage(list)
    }


    override suspend fun handleListOnValueChanged(formId: Int, index: Int): Int {
        return when (formId) {
            place.id -> {
                validateEmptyOnEditText(place)
                validateAllAlphabetsSpecialAndNumericOnEditText(place)
                -1
            }

            noOfBeneficiariesAttended.id -> {
                validateEmptyOnEditText(noOfBeneficiariesAttended)
                -1
            }

            else -> {
                -1
            }
        }
    }

    override fun mapValues(cacheModel: FormDataModel, pageNumber: Int) {
        (cacheModel as VHNCCache).let { form ->

            form.vhncDate = vhncDate.value!!
            form.place = place.value
            form.noOfBeneficiariesAttended = noOfBeneficiariesAttended.value!!.toInt()
            form.image1 = pic1.value?.takeIf { it.isNotEmpty() }
            form.image2 = pic2.value?.takeIf { it.isNotEmpty() }
            form.villageName = villageName.value
            form.anm = noOfANM.value?.trim()?.toIntOrNull() ?: 0
            form.aww = noOfAWW.value?.trim()?.toIntOrNull() ?: 0
            form.noOfPragnentWoment = noOfPW.value?.trim()?.toIntOrNull() ?: 0
            form.noOfLactingMother = noOfLM.value?.trim()?.toIntOrNull() ?: 0
            form.noOfCommittee = noOfCommentte.value?.trim()?.toIntOrNull() ?: 0
            form.followupPrevius=  followupPrevius.value?.let { value ->
                val options = resources.getStringArray(R.array.yes_no)
                when (value.trim()) {
                    options[0] -> true
                    options[1] -> false
                    else -> null
                }
            }

        }

    }

    fun getFileIndex1() = getIndexById(pic1.id)
    fun getFileIndex2() = getIndexById(pic2.id)


//    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
//        when (lastImageFormId) {
//            1 -> {
//                pic1.value = dpUri.toString()
//                pic1.errorText = null
//            }
//
//            2-> {
//                pic2.value = dpUri.toString()
//                pic2.errorText = null
//            }
//        }
//    }

    fun setImageUriToFormElement(lastImageFormId: Int, dpUri: Uri) {
        when (lastImageFormId) {
            pic1.id -> {
                pic1.value = dpUri.toString()
                pic1.errorText = null
            }
            pic2.id -> {
                pic2.value = dpUri.toString()
                pic2.errorText = null
            }
        }

    }

}
package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.net.Uri
import android.util.Log
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType.DATE_PICKER
import org.piramalswasthya.sakhi.model.InputType.EDIT_TEXT
import org.piramalswasthya.sakhi.model.InputType.IMAGE_VIEW
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.VHNCCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PHCReviewDataset(
    context: Context, currentLanguage: Languages
) : Dataset(context, currentLanguage) {

    companion object {
        private fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            val mdFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            return mdFormat.format(calendar.time)
        }

    }


    private val phcReviewDate = FormElement(
        id = 5,
        inputType = DATE_PICKER,
        title = resources.getString(R.string.phc_review_meeting_date),
        arrayId = -1,
        required = true,
        min = System.currentTimeMillis(),
        max = System.currentTimeMillis()
    )
    private val place = FormElement(
        id = 3,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.place),
        etMaxLength = 100,
        arrayId = -1,
        required = true,
        allCaps = true,
        hasSpeechToText = true,
        etInputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    )

    private val noOfBeneficiariesAttended = FormElement(
        id = 4,
        inputType = EDIT_TEXT,
        title = resources.getString(R.string.no_b_attended),
        arrayId = -1,
        required = true,
        etInputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL,
        isMobileNumber = true,
        etMaxLength = 4,
        max = 9999,
        min = 1
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

    suspend fun setUpPage(phc: PHCReviewMeetingCache?) {
        if (pic1.value.isNullOrBlank()) {
            pic1.value = "default"
        }

        if (pic2.value.isNullOrBlank()) {
            pic2.value = "default"
        }
        val list = mutableListOf(
            phcReviewDate,
            place,
            noOfBeneficiariesAttended,
            pic1,
            pic2
        )

        phcReviewDate.value = getCurrentDateString()
        phc?.let {
            phcReviewDate.value = it.phcReviewDate
            pic1.value = it.image1
            pic2.value = it.image2
            place.value = it.place
            noOfBeneficiariesAttended.value = it.noOfBeneficiariesAttended.toString()

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
        (cacheModel as PHCReviewMeetingCache).let { form ->

            form.phcReviewDate = phcReviewDate.value!!
            form.place = place.value
            form.noOfBeneficiariesAttended = noOfBeneficiariesAttended.value!!.toInt()
            form.image1 = pic1.value
            form.image2 = pic2.value

        }

    }
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